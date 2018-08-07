package com.enation.app.shop.mobile.action.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enation.app.b2b2c.core.goods.service.IStoreGoodsManager;
import com.enation.app.shop.mobile.model.ApiGoods;
import com.enation.app.shop.mobile.service.ApiMemberManager;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.base.core.model.Member;
import com.enation.app.shop.core.member.model.Favorite;
import com.enation.app.shop.core.member.service.IFavoriteManager;
import com.enation.app.shop.mobile.service.ApiFavoriteManager;
import com.enation.eop.sdk.context.UserConext;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.database.Page;
import com.enation.framework.util.JsonResultUtil;

/**
 * Created by Dawei on 4/29/15.
 */
@Controller("mobileFavoriteApiController")
@RequestMapping("/api/mobile/favorite")
public class FavoriteApiController {

	@Autowired
    private IFavoriteManager favoriteManager;
	
	@Autowired
    private ApiFavoriteManager apiFavoriteManager;

    @Autowired
    private IStoreGoodsManager storeGoodsManager;

    private final int PAGE_SIZE = 20;

    /**
	 * 添加收藏
	 * 
	 * @param goodsid	商品ID
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult add(Integer goodsid) {
        Member member = UserConext.getCurrentMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("请登录后再收藏商品！");
        }
        apiFavoriteManager.add(goodsid, member.getMember_id());
        return JsonResultUtil.getSuccessJson("收藏成功！");
    }
	
	/**
	 * 批量添加收藏
	 * @param goodsids
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/batch-add", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult batchAdd(Integer[] goodsids){
		Member member = UserConext.getCurrentMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("请登录后再收藏商品！");
        }
        apiFavoriteManager.batchAdd(goodsids, member.getMember_id());
        return JsonResultUtil.getSuccessJson("收藏成功！");
	}
    
    /**
     * 取消收藏
     * @param goodsid
     * @return
     */
	@ResponseBody
	@RequestMapping(value = "/unfavorite", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult unfavorite(Integer goodsid){
    	Member member = UserConext.getCurrentMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
        }
        apiFavoriteManager.delete(goodsid, member.getMember_id());
        return JsonResultUtil.getSuccessJson("取消收藏成功！");
    }


    /**
     * 删除一个会员收藏
     * @param favoriteid
     * @return
     */
	@ResponseBody
	@RequestMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult delete(Integer favoriteid) {
        Member member = UserConext.getCurrentMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
        }
        if(favoriteid <= 0){
        	return JsonResultUtil.getErrorJson("系统参数错误！");
        }
        
        Favorite favorite = apiFavoriteManager.get(favoriteid);
        if (favorite.getMember_id() != member.getMember_id()) {
            return JsonResultUtil.getErrorJson("您没有权限进行此项操作！");
        }

        try {
            this.favoriteManager.delete(favoriteid);
            return JsonResultUtil.getSuccessJson("删除成功");
        } catch (Exception e) {
            return JsonResultUtil.getErrorJson("删除失败，请您重试！");
        }        
    }

    /**
     * 所有收藏列表
     * @param page
     * @return
     */
	@ResponseBody
	@RequestMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult list(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
        Member member =UserConext.getCurrentMember();
        if (member == null) {
        	return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
        }
        if(page == null || page <= 0)
        	page = 1;

        Page webpage = favoriteManager.list(member.getMember_id(), page, PAGE_SIZE);
        List list = (List) webpage.getResult();
        for(int i = 0; i < list.size(); i++){
            Map map = (Map)list.get(i);
            map.put("thumbnail", StaticResourcesUtil.convertToUrl(map.get("thumbnail").toString()));
        }
        return JsonResultUtil.getObjectJson(list);
    }

    /**
     * 收藏的店铺列表
     * @param page
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/store-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult storeList(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page){
        Member member =UserConext.getCurrentMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
        }
        Page webPage = apiFavoriteManager.getStoreList(member.getMember_id(), page, PAGE_SIZE);
        List<Map> list = (List<Map>)webPage.getResult();
        if(list != null && list.size() > 0){
            for(Map map : list){

                if(map.containsKey("store_logo") && map.get("store_logo") != null){
                    map.put("store_logo", StaticResourcesUtil.convertToUrl(map.get("store_logo").toString()));
                }

                List<ApiGoods> goodsList = new ArrayList<>();

                //商品列表
                Map params = new HashMap();
                params.put("store_id", NumberUtils.toInt(map.get("store_id").toString(), 0));
                params.put("disable", 0);
                params.put("market_enable", 1);
                Page goodsPage = storeGoodsManager.storeGoodsList(1, 5, params);
                if(goodsPage != null && goodsPage.getResult() != null){
                    List<Map> goodsMapList = (List<Map>)goodsPage.getResult();
                    for(Map m : goodsMapList){
                        ApiGoods apiGoods = new ApiGoods();
                        try{
                            BeanUtils.copyProperties(apiGoods, m);
                        }catch(Exception ex){}
                        apiGoods.setThumbnail(StaticResourcesUtil.convertToUrl(apiGoods.getThumbnail()));
                        goodsList.add(apiGoods);
                    }
                }
                map.put("goodslist", goodsList);
            }
        }
        return JsonResultUtil.getObjectJson(list);
    }
}
