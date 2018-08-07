package com.enation.app.shop.mobile.action.api;

import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.upload.IUploader;
import com.enation.app.base.core.upload.UploadFacatory;
import com.enation.app.shop.core.goods.service.IGoodsManager;
import com.enation.app.shop.core.member.model.MemberComment;
import com.enation.app.shop.core.member.model.MemberOrderItem;
import com.enation.app.shop.core.member.service.IMemberCommentManager;
import com.enation.app.shop.core.member.service.IMemberOrderItemManager;
import com.enation.app.shop.mobile.service.ApiCommentManager;
import com.enation.eop.sdk.context.UserConext;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.util.FileUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("mobileCommentApiController")
@RequestMapping("/api/mobile/comment")
public class CommentApiController {

    private final int PAGE_SIZE = 20;

    @Autowired
    private IMemberCommentManager memberCommentManager;

    @Autowired
    private ApiCommentManager apiCommentManager;

    @Autowired
    private IMemberOrderItemManager memberOrderItemManager;

    @Autowired
    private IGoodsManager goodsManager;

    /**
     * 取每个商品最热门的几个评论
     *
     * @param goodsid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/hot", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult hot(Integer goodsid) {
        List<Map> commentList = apiCommentManager.hot(goodsid, 5);
        if(commentList != null && commentList.size() > 0){
            for(Map commentMap : commentList){
                int comment_id = NumberUtils.toInt(commentMap.get("comment_id").toString(), 0);
                List<Map> galleryList = apiCommentManager.getGallery(comment_id);
                if(galleryList != null && galleryList.size() > 0){
                    for(Map galleryMap : galleryList){
                        galleryMap.put("original", StaticResourcesUtil.convertToUrl(galleryMap.get("original").toString()));
                    }
                }
                commentMap.put("gallery", galleryList);
            }
        }
        return JsonResultUtil.getObjectJson(commentList);
    }

    /**
     * 获取一个商品的各个评分的评论数
     *
     * @param goodsid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult count(Integer goodsid) {
        Map<String, Integer> countMap = new HashMap<String, Integer>();
        // 全部评论数
        countMap.put("all", apiCommentManager.getCommentsCount(goodsid));
        // 好评数
        countMap.put("good", apiCommentManager.getCommentsCount(goodsid, 3, 5));
        // 中评数
        countMap.put("general",
                apiCommentManager.getCommentsCount(goodsid, 2, 3));
        // 差评数
        countMap.put("poor", apiCommentManager.getCommentsCount(goodsid, 0, 2));
        // 晒图数
        countMap.put("image", apiCommentManager.getImageCommentsCount(goodsid));
        return JsonResultUtil.getObjectJson(countMap);
    }

    /**
     * 获取评论列表
     *
     * @param goodsid   商品Id
     * @param page      第几页
     * @param min       最小评分  
     * @param max       最大评分
     * @param onlyimage 是否只包含图片
     * @zhushi  全部： min=0 max=5; 高：min=3 max=5; 中：min=2 max=3; 低：min=0 max=2;
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult list(Integer goodsid, Integer page, Integer min,
                           Integer max, Integer onlyimage) {
        if (page == null || page <= 0) {
            page = 1;
        }
        if (min == null || min < 0) {
            min = 0;
        }
        if (max == null || max < 0 || max > 5) {
            max = 5;
        }
        boolean haveImage = (onlyimage != null && onlyimage == 1);

        Page webPage = null;
        if(!haveImage) {
            webPage = apiCommentManager.getGoodsComments(goodsid, page,
                    PAGE_SIZE, min, max);
        }else{
            webPage = apiCommentManager.getImageGoodsComments(goodsid, page,PAGE_SIZE);
        }
        
        List list = (List) webPage.getResult();
        for (int i = 0; i < list.size(); i++) {
            //头像
            Map<String, Object> map = (Map<String, Object>) list.get(i);
            if (map.containsKey("face") && map.get("face") != null) {
                map.put("face", StaticResourcesUtil.convertToUrl(map
                        .get("face").toString()));
            }
            //图片
            int comment_id = NumberUtils.toInt(map.get("comment_id").toString(), 0);
            List<Map> galleryList = apiCommentManager.getGallery(comment_id);
            
            
            if(galleryList != null && galleryList.size() > 0){
                for(Map galleryMap : galleryList){
                    galleryMap.put("original", StaticResourcesUtil.convertToUrl(galleryMap.get("original").toString()));
                }
            }else if(haveImage){
            	list.remove(i);
            }
            map.put("gallery", galleryList);
        }
        return JsonResultUtil.getObjectJson(list);
    }

    /**
     * 获取待评论的商品列表
     *
     * @param page
     * @param order_id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/wait-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult waitCommentList(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page, Integer order_id) {
        Member member = UserConext.getCurrentMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
        }
        if (page == null || page <= 0) {
            page = 1;
        }
        Page webPage = apiCommentManager.getGoodsList(
                member.getMember_id(), order_id, page, PAGE_SIZE);
        List list = (List) webPage.getResult();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = (Map<String, Object>) list.get(i);
            if (map.containsKey("thumbnail") && map.get("thumbnail") != null) {
                map.put("thumbnail", StaticResourcesUtil.convertToUrl(map.get(
                        "thumbnail").toString()));
            }
        }
        return JsonResultUtil.getObjectJson(list);
    }

    /**
     * 发表评论
     *
     * @param memberComment
     * @param images
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult create(@ModelAttribute MemberComment memberComment, @RequestParam(value = "images", required = false) MultipartFile[] images) {
        HttpServletRequest request = ThreadContextHolder.getHttpRequest();
        memberComment.setType(1);

        if (memberComment.getGoods_id() <= 0 || goodsManager.get(memberComment.getGoods_id()) == null) {
            return JsonResultUtil.getErrorJson("此商品不存在！");
        }

        if (StringUtil.isEmpty(memberComment.getContent())) {
            return JsonResultUtil.getErrorJson("评论内容不能为空！");
        }
        if (memberComment.getContent().length() > 500) {
            return JsonResultUtil.getErrorJson("请输入1000以内的内容！");
        }
        memberComment.setContent(StringUtil.htmlDecode(memberComment.getContent()));

        Member member = UserConext.getCurrentMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("只有登录且成功购买过此商品的用户才能发表评论！");
        }
        int buyCount = memberOrderItemManager.count(member.getMember_id(),
                memberComment.getGoods_id());
        int commentCount = memberOrderItemManager.count(member.getMember_id(),
                memberComment.getGoods_id(), 1);
        if (buyCount <= 0) {
            return JsonResultUtil.getErrorJson("只有成功购买过此商品的用户才能发表评论！");
        }
        if (commentCount >= buyCount) {
            return JsonResultUtil.getErrorJson("对不起，您已经评论过此商品！");
        }
        if (memberComment.getGrade() < 0 || memberComment.getGrade() > 5) {
            memberComment.setGrade(5);
        }

        memberComment.setMember_id(member == null ? 0 : member.getMember_id());
        memberComment.setDateline(System.currentTimeMillis() / 1000);
        memberComment.setIp(request.getRemoteHost());

        try {
            memberCommentManager.add(memberComment);

            //上传评论图片
            String field = "comment";
            List<String> imageList = new ArrayList<String>();
            if (images != null && images.length > 0) {
                for (MultipartFile image : images) {
                    //判断文件类型
                    if (!FileUtil.isAllowUpImg(image.getOriginalFilename())) {
                        continue;
                    }

                    //判断文件大小
                    if (image.getSize() > 2000 * 1024) {
                        continue;
                    }

                    InputStream stream = null;
                    try {
                        stream = image.getInputStream();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return JsonResultUtil.getErrorJson("发表评论失败，请您重试！");
                    }

                    IUploader uploader = UploadFacatory.getUploaer();
                    String imgPath = uploader.upload(stream, field, image.getOriginalFilename());
                    imageList.add(imgPath);
                }
            }
            apiCommentManager.addGallery(memberComment.getComment_id(), imageList);

            // 更新为已经评论过此商品
            MemberOrderItem memberOrderItem = memberOrderItemManager.get(
                    member.getMember_id(), memberComment.getGoods_id(), 0);
            if (memberOrderItem != null) {
                memberOrderItem.setComment_time(System.currentTimeMillis());
                memberOrderItem.setCommented(1);
                memberOrderItemManager.update(memberOrderItem);
            }
            return JsonResultUtil.getSuccessJson("发表成功");
        } catch (RuntimeException e) {
            return JsonResultUtil.getErrorJson("发表评论出错，请您重试！");
        }
    }

}
