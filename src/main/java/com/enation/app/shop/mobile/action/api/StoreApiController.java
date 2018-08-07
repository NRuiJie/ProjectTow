package com.enation.app.shop.mobile.action.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.enation.app.b2b2c.component.bonus.model.StoreBonusType;
import com.enation.app.b2b2c.component.bonus.service.IB2b2cBonusManager;
import com.enation.app.b2b2c.core.goods.model.StoreProduct;
import com.enation.app.b2b2c.core.goods.service.IB2b2cPlatformGoodsManager;
import com.enation.app.b2b2c.core.goods.service.IStoreGoodsCatManager;
import com.enation.app.b2b2c.core.goods.service.IStoreGoodsManager;
import com.enation.app.b2b2c.core.goods.service.IStoreGoodsTagManager;
import com.enation.app.b2b2c.core.member.model.MemberCode;
import com.enation.app.b2b2c.core.member.model.MemberCollect;
import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IMemberCodeManager;
import com.enation.app.b2b2c.core.member.service.IStoreCollectManager;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.b2b2c.core.order.service.IStoreOrderManager;
import com.enation.app.b2b2c.core.order.service.cart.IStoreCartManager;
import com.enation.app.b2b2c.core.order.service.cart.IStoreProductManager;
import com.enation.app.b2b2c.core.store.model.Store;
import com.enation.app.b2b2c.core.store.service.IStoreManager;
import com.enation.app.b2b2c.front.api.order.publicmethod.StoreCartPublicMethod;
import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.app.base.core.service.IShortUrlManager;
import com.enation.app.base.core.upload.IUploader;
import com.enation.app.base.core.upload.UploadFacatory;
import com.enation.app.shop.core.goods.model.Goods;
import com.enation.app.shop.core.goods.service.GoodsFlagEnum;
import com.enation.app.shop.core.goods.service.IGoodsManager;
import com.enation.app.shop.core.order.model.Cart;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.service.ICartManager;
import com.enation.app.shop.mobile.model.ApiBonus;
import com.enation.app.shop.mobile.model.ApiGoods;
import com.enation.app.shop.mobile.model.ApiStore;
import com.enation.app.shop.mobile.model.ApiStoreCat;
import com.enation.app.shop.mobile.service.ApiStoreManager;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.FileUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

/**
 * 
 * @ClassName: StoreApiController 
 * @Description: 店铺相关 
 * @author zwb 
 * @version 1.0
 * @since v6.2.1
 * @date 2017年8月27日 下午1:11:25 
 *
 */
@Controller("mobileStoreApiController")
@RequestMapping("/api/mobile/store")
public class StoreApiController {

	protected Logger logger = Logger.getLogger(getClass());
	
    @Autowired
    private IStoreManager storeManager;

    @Autowired
    private IB2b2cBonusManager b2b2cBonusManager;

    @Autowired
    private IStoreGoodsTagManager storeGoodsTagManager;

    @Autowired
    private IStoreGoodsManager storeGoodsManager;

    @Autowired
    private IStoreMemberManager storeMemberManager;

    @Autowired
    private IStoreCollectManager storeCollectManager;

    @Autowired
    private ApiStoreManager apiStoreManager;

    @Autowired
    private IStoreGoodsCatManager storeGoodsCatManager;
    
    @Autowired
	private IMemberCodeManager memberCodeManager;

	@Autowired
	private IMemberManager memberManager;
	
	@Resource
	private IShortUrlManager shortUrlManager;
	
	@Autowired
	private IGoodsManager goodsManager;
	
	@Autowired
	private StoreCartPublicMethod storeCartPublicMethod;
	
	@Autowired
	private IStoreCartManager storeCartManager;
	
	@Autowired
	private ICartManager cartManager;
	
	@Autowired
	private IStoreProductManager storeProductManager;
	
	@Autowired
	private IStoreOrderManager storeOrderManager;
	@Autowired
	private IB2b2cPlatformGoodsManager b2b2cPlatformGoodsManager;

    private static final int PAGE_SIZE = 20;

    /**
     * 获取店铺详情
     *
     * @param storeid 店铺ID
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult detail(Integer storeid) {
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        Member member1 = memberManager.get(store.getMember_id());
        ApiStore apiStore = new ApiStore();
        try {
            BeanUtils.copyProperties(apiStore, store);
        } catch (Exception ex) {
        }
        if(apiStore.getStore_logo() != null){
        	apiStore.setStore_logo(StaticResourcesUtil.convertToUrl(apiStore.getStore_logo()));
        }else{
        	apiStore.setStore_logo(StaticResourcesUtil.convertToUrl(member1.getFace()));
        }
        //统计商品数量
        String[] marks = new String[]{"new", "hot", "recommend"};
        Map<String, List<Map>> goodsMap = new HashMap<>();
        for (String mark : marks) {
            Map map = new HashMap();
            map.put("mark", mark);
            map.put("storeid", storeid);

            Map result = new HashMap();
            //查询标签商品列表
            Page webpage = storeGoodsTagManager.getGoodsList(map, 1, 1);
            if (mark.equals("new")) {
                apiStore.setNew_num(webpage.getTotalCount());
            } else if (mark.equals("hot")) {
                apiStore.setHot_num(webpage.getTotalCount());
            } else if (mark.equals("recommend")) {
                apiStore.setRecommend_num(webpage.getTotalCount());
            }
        }
       Integer  platform_num=b2b2cPlatformGoodsManager.getGoodsNum(storeid);
       apiStore.setPlatform_num(platform_num);
        //统计是否收藏
        StoreMember member = storeMemberManager.getStoreMember();
        if (member != null) {
            boolean result = storeCollectManager.isCollect(member.getMember_id(), storeid);
            apiStore.setFavorited(result ? 1 : 0);
        } else {
            apiStore.setFavorited(0);
        }

        return JsonResultUtil.getObjectJson(apiStore);
    }

    /**
     * 获取店铺的优惠券列表
     * @param storeid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/bonus-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult bonusList(Integer storeid) {
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        Map result = new HashMap();
        result.put("add_time_from", "");
        result.put("add_time_to", "");
        //标识是店铺显示 只显示没有过期的优惠券 当当前日期>优惠券最后时间  则过期
        String sign_time = DateUtil.toString(new Date(), "yyyy-MM-dd");
        result.put("sign_time", sign_time);
        Page bonusPage = b2b2cBonusManager.getConditionBonusList(1, 100, storeid, result);

        List<ApiBonus> bonuses = new ArrayList<>();
        List<StoreBonusType> list = (List<StoreBonusType>) bonusPage.getResult();
        for (StoreBonusType bonus : list) {
            ApiBonus apiBonus = new ApiBonus();
            try {
                BeanUtils.copyProperties(apiBonus, bonus);
            } catch (Exception ex) {
            }
            bonuses.add(apiBonus);
        }
        return JsonResultUtil.getObjectJson(bonuses);
    }



    /**
     * 获取首页要显示的商品
     *
     * @param storeid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/index-goods", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult indexGoods(Integer storeid) {
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        String[] marks = new String[]{"new", "hot", "recommend"};
        Map<String, List<Map>> goodsMap = new HashMap<>();
        for (String mark : marks) {
            Map map = new HashMap();
            map.put("mark", mark);
            map.put("storeid", storeid);

            Map result = new HashMap();
            //查询标签商品列表
            Page webpage = storeGoodsTagManager.getGoodsList(map, 1, 8);
            List<Map> list = (List<Map>) webpage.getResult();
            for (Map m : list) {
                if (m.containsKey("thumbnail") && m.get("thumbnail") != null) {
                    m.put("thumbnail", StaticResourcesUtil.convertToUrl(m.get("thumbnail").toString()));
                }
            }
            goodsMap.put(mark, list);
        }
        return JsonResultUtil.getObjectJson(goodsMap);
    }

    /**
     * 按标签显示商品，支持new、hot、recommend
     *
     * @param storeid
     * @param tag
     * @param page
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/tag-goods", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult tagGoods(Integer storeid,String tag,@RequestParam(value = "page", defaultValue = "1", required = false) Integer page) {
        if (StringUtils.isEmpty(tag)) {
            return JsonResultUtil.getErrorJson("系统参数错误！");
        }
        if (!tag.equals("new") && !tag.equals("hot") && !tag.equals("recommend")) {
            return JsonResultUtil.getErrorJson("系统参数错误！");
        }
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }

        Map map = new HashMap();
        map.put("mark", tag);
        map.put("storeid", storeid);
        
        Map result = new HashMap();
        //查询标签商品列表
        Page webpage = storeGoodsTagManager.getGoodsList(map, page, 8);
        List<Map> list = (List<Map>) webpage.getResult();
        for (Map m : list) {
            if (m.containsKey("thumbnail") && m.get("thumbnail") != null) {
                m.put("thumbnail", StaticResourcesUtil.convertToUrl(m.get("thumbnail").toString()));
            }
        }
        return JsonResultUtil.getObjectJson(list);
    }
    /**
     *查询店铺代理商品
     *@author ayf  2017/8/29 10:57
     * @param storeid
     * @param page
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/platform-goods", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult platformGoods(Integer storeid,String tag,@RequestParam(value = "page", defaultValue = "1", required = false) Integer page) {
    	
    	
    	Store store = storeManager.getStore(storeid);
    	if (store == null) {
    		return JsonResultUtil.getErrorJson("此店铺不存在！");
    	}
    	Map map = new HashMap();
    	map.put("storeid", storeid);
    	Map result = new HashMap();
    	//查询标签商品列表
    	Page webpage = b2b2cPlatformGoodsManager.getGoodsList(map, page, 8);
    	List<Map> list = (List<Map>) webpage.getResult();
    	for (Map m : list) {
    		if (m.containsKey("thumbnail") && m.get("thumbnail") != null) {
    			m.put("thumbnail", StaticResourcesUtil.convertToUrl(m.get("thumbnail").toString()));
    		}
    	}
    	return JsonResultUtil.getObjectJson(list);
    }

    /**
     * 获取店铺的全部商品列表
     *
     * @param storeid
     * @param keyword
     * @param start_price
     * @param end_price
     * @param key
     * @param order
     * @param cat_id
     * @param page
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/goods-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult goodsList(Integer storeid,
                                @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                                @RequestParam(value = "start_price", required = false, defaultValue = "") String start_price,
                                @RequestParam(value = "end_price", required = false, defaultValue = "") String end_price,
                                @RequestParam(value = "key", required = false, defaultValue = "3") Integer key,
                                @RequestParam(value = "order", required = false, defaultValue = "DESC") String order,
                                @RequestParam(value = "cat_id", required = false, defaultValue = "0") Integer cat_id,
                                @RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("storeid", storeid);
        params.put("keyword", keyword);
        params.put("start_price", start_price);
        params.put("end_price", end_price);
        params.put("key", key);
        params.put("order", order);
        params.put("stc_id", cat_id);
        Page webpage = this.storeGoodsManager.store_searchGoodsList(page, PAGE_SIZE, params);
        List<Map> list = (List<Map>) webpage.getResult();
        List<ApiGoods> goodsList = new ArrayList<>();
        for (Map m : list) {
            if (m.containsKey("thumbnail") && m.get("thumbnail") != null) {
                m.put("thumbnail", StaticResourcesUtil.convertToUrl(m.get("thumbnail").toString()));
            }
            ApiGoods apiGoods = new ApiGoods();
            try {
                BeanUtils.copyProperties(apiGoods, m);
            } catch (Exception ex) {
            }
            goodsList.add(apiGoods);
        }
        return JsonResultUtil.getObjectJson(goodsList);
    }
    /**
     * 获取店铺的全部商品列表
     *
     * @param storeid
     * @param keyword
     * @param start_price
     * @param end_price
     * @param key
     * @param order
     * @param cat_id
     * @param page
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/platformgoods-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult platformgoodsList(Integer storeid,
    		@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
    		@RequestParam(value = "start_price", required = false, defaultValue = "") String start_price,
    		@RequestParam(value = "end_price", required = false, defaultValue = "") String end_price,
    		@RequestParam(value = "key", required = false, defaultValue = "3") Integer key,
    		@RequestParam(value = "order", required = false, defaultValue = "DESC") String order,
    		@RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
    	Store store = storeManager.getStore(storeid);
    	if (store == null) {
    		return JsonResultUtil.getErrorJson("此店铺不存在！");
    	}
    	Map<String, Object> params = new HashMap<>();
    	params.put("storeid", storeid);
    	params.put("keyword", keyword);
    	params.put("start_price", start_price);
    	params.put("end_price", end_price);
    	params.put("key", key);
    	params.put("order", order);
		Page webpage = this.b2b2cPlatformGoodsManager.store_searchGoodsList(page, PAGE_SIZE, params);
    	List<Map> list = (List<Map>) webpage.getResult();
    	List<ApiGoods> goodsList = new ArrayList<>();
    	for (Map m : list) {
    		if (m.containsKey("thumbnail") && m.get("thumbnail") != null) {
    			m.put("thumbnail", StaticResourcesUtil.convertToUrl(m.get("thumbnail").toString()));
    		}
    		ApiGoods apiGoods = new ApiGoods();
    		try {
    			BeanUtils.copyProperties(apiGoods, m);
    		} catch (Exception ex) {
    		}
    		goodsList.add(apiGoods);
    	}
    	return JsonResultUtil.getObjectJson(goodsList);
    }

    /**
     * 关注店铺
     *
     * @param storeid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/collect", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult collect(Integer storeid) {
        StoreMember member = storeMemberManager.getStoreMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("请您登录后再关注此店铺");
        }
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        MemberCollect collect = new MemberCollect();
        collect.setMember_id(member.getMember_id());
        collect.setStore_id(storeid);
        boolean result = storeCollectManager.isCollect(member.getMember_id(), storeid);
        if (!result) {
            this.storeCollectManager.addCollect(collect);
            this.storeManager.addcollectNum(storeid);
        }
        return JsonResultUtil.getSuccessJson("关注店铺成功！");
    }

    /**
     * 取消关注店铺
     *
     * @param storeid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "uncollect", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult uncollect(Integer storeid) {
        StoreMember member = storeMemberManager.getStoreMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("请您登录后再取消关注此店铺");
        }
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        apiStoreManager.deleteCollect(storeid, member.getMember_id());
        this.storeManager.reduceCollectNum(storeid);
        return JsonResultUtil.getSuccessJson("取消关注店铺成功！");
    }

    /**
     * 获取店铺的商品分类
     *
     * @param storeid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/category", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult category(Integer storeid) {
        Store store = storeManager.getStore(storeid);
        if (store == null) {
            return JsonResultUtil.getErrorJson("此店铺不存在！");
        }
        List<Map> catList = storeGoodsCatManager.storeCatList(storeid);

        List<ApiStoreCat> apiCatList = new ArrayList<>();
        List<ApiStoreCat> subCatList = new ArrayList<>();
        for (Map map : catList) {
            ApiStoreCat cat = new ApiStoreCat();
            try {
                BeanUtils.copyProperties(cat, map);
            } catch (Exception ex) {
            }
            if (cat.getStore_cat_pid() != null && cat.getStore_cat_pid().intValue() == 0) {
                apiCatList.add(cat);
            } else {
                subCatList.add(cat);
            }
        }

        for (ApiStoreCat cat : apiCatList) {
            for (ApiStoreCat subcat : subCatList) {
                if (subcat.getStore_cat_pid() != null && subcat.getStore_cat_pid().equals(cat.getStore_cat_id())) {
                    cat.getChildren().add(subcat);
                }
            }
        }
        return JsonResultUtil.getObjectJson(apiCatList);
    }

    /**
     * 店铺搜索
     *
     * @param keyword
     * @param page
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult search(String keyword,
                             @RequestParam(value = "page", defaultValue = "1", required = false) Integer page) {
    	if(keyword == null){
            keyword = "";
        }
        Map params = new HashMap();
        params.put("name", keyword);
        params.put("store_credit", "");
        params.put("searchType", "");
        Page webPage = storeManager.store_list(params, 1, page, 1000);

        List<ApiStore> storeList = new ArrayList<>();
        List<Map> list = (List<Map>)webPage.getResult();
        for(Map map : list){
            ApiStore apiStore = new ApiStore();
            try{
                BeanUtils.copyProperties(apiStore, map);
            }catch(Exception ex){}
            apiStore.setStore_logo(StaticResourcesUtil.convertToUrl(apiStore.getStore_logo()));

            //商品列表
            Map goodsParams = new HashMap();
            goodsParams.put("store_id", apiStore.getStore_id());
            goodsParams.put("disable", 0);
            goodsParams.put("market_enable", 1);
            Page goodsPage = storeGoodsManager.storeGoodsList(1, 5, goodsParams);
            if(goodsPage != null && goodsPage.getResult() != null){
                List<Map> goodsMapList = (List<Map>)goodsPage.getResult();
                for(Map m : goodsMapList){
                	Integer isIntegral = StringUtil.toInt(m.get("is_integral_goods").toString(), 0);
                	if (isIntegral.intValue() == 0) {
                		ApiGoods apiGoods = new ApiGoods();
                        try{
                            BeanUtils.copyProperties(apiGoods, m);
                        }catch(Exception ex){}
                        apiGoods.setThumbnail(StaticResourcesUtil.convertToUrl(apiGoods.getThumbnail()));
                        apiStore.getGoodsList().add(apiGoods);
					}
                }
            }

            storeList.add(apiStore);
        }
        return JsonResultUtil.getObjectJson(storeList);
    }
    
    /**
	 * 申请开店
	 * @param store 店铺信息,Store
	 * @return 返回json串
	 * result 	为1表示调用成功0表示失败
	 */
	@ResponseBody
	@RequestMapping(value="/apply",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult apply(Store store){
		try {
			StoreMember storeMember = storeMemberManager.getStoreMember();
			if(null==storeMember){
				return JsonResultUtil.getErrorJson("您没有登录不能申请开店");
			}else if(!storeManager.checkStore()){
				//店铺地址信息
				HttpServletRequest request = ThreadContextHolder.getHttpRequest();    
				String code = request.getParameter("code") == null ?"":request.getParameter("code").toString();
				
				if("".equals(code)){
					return JsonResultUtil.getErrorJson("没邀请码不能开店！");
				}
				
				store.setStore_provinceid(Integer.parseInt(request.getParameter("store_province_id").toString()));	//店铺省ID
				store.setStore_cityid(Integer.parseInt(request.getParameter("store_city_id").toString()));			//店铺市ID
				store.setStore_regionid(Integer.parseInt(request.getParameter("store_region_id").toString()));     //店铺区ID
				String store_town_id=request.getParameter("store_town_id");
				if(store_town_id!=null && !store_town_id.equals("")){ 
					store.setStore_townid(Integer.parseInt(store_town_id));                                       //店铺所在城镇Id
				}
				if(request.getParameter("bank_province_id") != null  && !request.getParameter("bank_province_id").equals("")){
					store.setBank_provinceid(Integer.parseInt(request.getParameter("bank_province_id").toString())); //开户银行所在省Id
				}
				if(request.getParameter("bank_city_id") != null && !request.getParameter("bank_city_id").equals("")){
					store.setBank_cityid(Integer.parseInt(request.getParameter("bank_city_id").toString()));		  //开户银行所在市Id
				}
				if(request.getParameter("bank_region_id") != null && !request.getParameter("bank_region_id").equals("")){
					store.setBank_regionid(Integer.parseInt(request.getParameter("bank_region_id").toString()));    //开户银行所在区Id
				}
				String bank_town_id=request.getParameter("bank_town_id");
				if(bank_town_id!=null && !bank_town_id.equals("")){ 
					store.setBank_townid(Integer.parseInt(bank_town_id));                                       //开户银行所在城镇Id
				}
				//暂时先将店铺等级定为默认等级，以后版本升级更改此处
				store.setStore_level(1);
				store.setCreate_time(DateUtil.getDateline());
				//店铺状态
				store.setDisabled(0);
				store.setStore_auth(0);
				
				//店铺佣金
				store.setCommission(0.0);
				//设置库存预警默认值
				store.setGoods_warning_count(5);
				storeManager.apply(store);
				// 设置邀请码为已使用
				memberCodeManager.updateIsUsed(1, storeMember.getMember_id(),code);
				
				Member member = memberManager.get(storeMember.getInvite_member());
				if(member == null){//如果不是链接注册用户 需要修改 用户  上级  推荐人
					MemberCode memberCode = memberCodeManager.getMemberByCode(code);
					memberManager.editMemberUp(storeMember.getMember_id(),memberCode.getMember_id());
					
				}
				return JsonResultUtil.getSuccessJson("申请成功,请等待审核");
			}else{
				return JsonResultUtil.getErrorJson("您已经申请过了，请不要重复申请");
			}
		} catch (Exception e) {
			this.logger.error("APP端店铺申请失败:"+e);
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("申请失败");
		}
	}
	
	/**
	 * 
	 * @param code				//邀请码
	 * @param store_name		//店铺名称
	 * @param store_provinceid //省ID
	 * @param store_cityid		//市Id
	 * @param store_regionid	//区Id
	 * @param store_townid		//城镇Id
	 * @param attr				//详细地址
	 * @param zip				//邮编
	 * @param tel				//联系方式
	 * @param id_number			//身份证号
	 * 
	 * @param bank_account_name		//银行开户名   
	 * @param bank_account_number	//公司银行账号
	 * @param bank_name				//开户银行支行名称
	 * @param bank_code				//支行联行号
	 * @param bank_provinceid		//开户银行所在省Id
	 * @param bank_cityid			//开户银行所在市Id
	 * @param bank_regionid			//开户银行所在区Id
	 * @param bank_townid			//开户银行所在城镇Id
	 * @param bank_attr				//开户银行所在详细地址
	 * @param id_img				//身份证正面照
	 * @param id_img_con			//身份证反面照
	 * @return
	 */
	
	@ResponseBody
	@RequestMapping(value="/save-store-info",produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult saveStoreInfo(
			//@RequestParam(value = "code", required = true) String code,
			@RequestParam(value = "store_name", required = true) String store_name,
			@RequestParam(value = "store_provinceid", required = false) Integer store_provinceid,
			@RequestParam(value = "store_cityid", required = false) Integer store_cityid,
			@RequestParam(value = "store_regionid", required = false) Integer store_regionid,
			@RequestParam(value = "store_townid", required = false) Integer store_townid,
			@RequestParam(value = "attr", required = false) String attr,
			@RequestParam(value = "zip", required = false) String zip,
			@RequestParam(value = "tel", required = false) String tel,
			@RequestParam(value = "id_number", required = false) String id_number,
			@RequestParam(value = "bank_account_name", required = false) String bank_account_name,
			@RequestParam(value = "bank_account_number", required = false) String bank_account_number,
			@RequestParam(value = "bank_name", required = false) String bank_name,
			@RequestParam(value = "bank_code", required = false) String bank_code,
			@RequestParam(value = "bank_type", required = false) Integer bank_type,
			@RequestParam(value = "alipay_code", required = false) String alipay_code,
			@RequestParam(value = "bank_center_name", required = false) String bank_center_name,
			@RequestParam(value = "bank_provinceid", required = false) Integer bank_provinceid,
			@RequestParam(value = "bank_cityid", required = false) Integer bank_cityid,
			@RequestParam(value = "bank_regionid", required = false) Integer bank_regionid,
			@RequestParam(value = "bank_townid", required = false) Integer bank_townid,
			@RequestParam(value = "bank_attr", required = false) String bank_attr,
			@RequestParam(value = "id_img", required = false) String id_img,
			@RequestParam(value = "id_img_con", required = false) String id_img_con){
		try {
			
			StoreMember storeMember = storeMemberManager.getStoreMember();
			if(null==storeMember){
				return JsonResultUtil.getErrorJson("您没有登录不能申请开店");
//			}else if(!storeManager.checkStore()){
			}else{
//				if(StringUtil.isEmpty(code)){
//					return JsonResultUtil.getErrorJson("没邀请码不能开店！");
//				}
				if(StringUtil.isEmpty(store_name)){
					return JsonResultUtil.getErrorJson("店铺名称不能为空！");
				}
				if(StringUtil.isEmpty(attr)){
					return JsonResultUtil.getErrorJson("详细地址不能为空！");
				}
				if(StringUtil.isEmpty(zip)){
					return JsonResultUtil.getErrorJson("邮编不能为空！");
				}
				if(StringUtil.isEmpty(tel)){
					return JsonResultUtil.getErrorJson("手机号码不能为空！");
				}
				
				if(StringUtil.isEmpty(id_number)){
					return JsonResultUtil.getErrorJson("身份证号不能为空！");
				}
//				if(StringUtil.isEmpty(bank_account_name)){
//					return JsonResultUtil.getErrorJson("银行开户名不能为空！");
//				}
//				if(StringUtil.isEmpty(bank_account_number)){
//					return JsonResultUtil.getErrorJson("银行账号不能为空！");
//				}
//				if(StringUtil.isEmpty(bank_name)){
//					return JsonResultUtil.getErrorJson("开户银行支行名称不能为空！");
//				}
//				if(StringUtil.isEmpty(bank_code)){
//					return JsonResultUtil.getErrorJson("支行联行号不能为空！");
//				}
//				if(StringUtil.isEmpty(bank_attr)){
//					return JsonResultUtil.getErrorJson("开户银行地址不能为空！");
//				}
				if(StringUtil.isEmpty(id_img)){
					return JsonResultUtil.getErrorJson("身份证正面照不能为空！");
				}
				if(StringUtil.isEmpty(id_img_con)){
					return JsonResultUtil.getErrorJson("身份证反面照不能为空！");
				}
				
				
				Store store = new Store();
				store.setStore_name(store_name); 			// 店铺名称
				store.setStore_provinceid(store_provinceid);// 店铺省ID
				store.setStore_cityid(store_cityid); 		// 店铺市ID
				store.setStore_regionid(store_regionid); 	// 店铺区ID
				store.setStore_townid(store_townid); 		// 城镇Id
				store.setAttr(attr); 						// 详细地址
				store.setZip(zip); 							// 邮编
				store.setTel(tel); 							// 联系方式
				store.setId_number(id_number); 				// 身份证号
				store.setStore_auth(0);
				//默认为1，银行卡
				if(bank_type==null){
					store.setBank_type(1);
				}else{
					store.setBank_type(bank_type);
				}
				store.setBank_center_name(bank_center_name);//银行名
				store.setAlipay_code(alipay_code);//支付宝号
				store.setBank_account_name(bank_account_name); 		// 银行支行开户名
				store.setBank_account_number(bank_account_number);	// 公司银行账号
				store.setBank_name(bank_name);						// 开户银行支行名称
				store.setBank_code(bank_code);						// 支行联行号
				store.setBank_provinceid(bank_provinceid); 			// 开户银行所在省ID
				store.setBank_cityid(bank_cityid); 					// 开户银行所在市ID
				store.setBank_regionid(bank_regionid); 				// 开户银行所在区ID
				store.setBank_townid(bank_townid);					// 开户银行所在城镇Id
				store.setBank_attr(bank_attr);					    // 开户银行所在详细地址
				store.setId_img(id_img);							//身份证正面照
				store.setId_img_con(id_img_con);					//身份证反面照
				
				//暂时先将店铺等级定为默认等级，以后版本升级更改此处
				store.setStore_level(1);
				store.setCreate_time(DateUtil.getDateline());
				//店铺状态
				store.setDisabled(0);
				
				//店铺佣金
				store.setCommission(0.0);
				//设置库存预警默认值
				store.setGoods_warning_count(5);
				if(storeManager.checkStore()){
					store.setStore_id(storeMember.getStore_id());
					storeManager.reApply(store);
				}else{
					storeManager.apply(store);

				}
				// 设置邀请码为已使用
				/*
				memberCodeManager.updateIsUsed(1, storeMember.getMember_id(),code);
				
				Member member = memberManager.get(storeMember.getInvite_member());
				MemberCode memberCode = memberCodeManager.getMemberByCode(code);
				if(member == null){//如果不是链接注册用户 需要修改 用户  上级  推荐人
					memberManager.editMemberUp(storeMember.getMember_id(),memberCode.getMember_id());
					
				}
				Map<String, Object> map = new HashMap<>();
				//是否需要支付写反了，暂且先加判断，进行更改
				if(memberManager.get(memberCode.getMember_id()).getType()==0){
					map.put("payType", 1+"");//开店成功返回会员类型判断是否支付开店技术服务费

				}else{
					map.put("payType", 0+"");//开店成功返回会员类型判断是否支付开店技术服务费
				}
//				map.put("payType", memberManager.get(memberCode.getMember_id()).getType()+"");//开店成功返回会员类型判断是否支付开店技术服务费
				return JsonResultUtil.getObjectJson(map);*/
				return JsonResultUtil.getSuccessJson("申请成功，请等待审核");
//			}else{
//				return JsonResultUtil.getErrorJson("您已经申请过了，请不要重复申请");
			}
			
		}catch (Exception e) {
			this.logger.error("APP端店铺申请失败:"+e);
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("申请失败");
		}
		
	}

	/**
	 * 上传图片
	 * @param 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/upload-img",produces = MediaType.APPLICATION_JSON_VALUE)
	private JsonResult uploadImg(MultipartFile imgFile) {
		//先上传图片
		String faceField = "faceFile";
		String imgPath = null;	
		if(imgFile!=null){
			if(imgFile!=null){
				//判断文件类型
				if(!FileUtil.isAllowUpImg(imgFile.getOriginalFilename())){
					return JsonResultUtil.getErrorJson("不允许上传的文件格式，请上传gif,jpg,bmp格式文件。");
				}
				//判断文件大小
				if(imgFile.getSize() > 5120 * 1024){
					return JsonResultUtil.getErrorJson("'对不起,图片不能大于5M！");				
					
				}
			}
			InputStream stream=null;
			try {
				stream=imgFile.getInputStream();
			} catch (Exception e) {
				e.printStackTrace();
			}
			IUploader uploader=UploadFacatory.getUploaer();
			imgPath = uploader.upload(stream, faceField,imgFile.getOriginalFilename());
		}
		Map<String, Object> map = new HashMap<>();
		map.put("imgPath", imgPath);
		return JsonResultUtil.getObjectJson(map);
	} 


	/**
	 * 开店支付创建订单
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/pay",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult pay(){
		try {
			StoreMember storeMember = storeMemberManager.getStoreMember();
			
			if(null==storeMember){
				return JsonResultUtil.getErrorJson("您没有登录不能申请开店");
			}else {
				
				Store store = storeManager.getStore(storeMember.getStore_id());
				if(store.getPay_order_id() != null){//如果已经创建订单就关联创建的订单
					Map<String, Object> orderMap = new HashMap<>();
					orderMap.put("order_id", store.getPay_order_id());
					return JsonResultUtil.getObjectJson(orderMap);
				}
				
				HttpServletRequest request = ThreadContextHolder.getHttpRequest();
				Goods goods = goodsManager.getGoodByFlag(GoodsFlagEnum.JISHU.getType());
				
				storeCartManager.updateCartCheckStatus(storeMember.getMember_id());
				
				StoreProduct product = storeProductManager.getByGoodsId(goods.getGoods_id());
				
				String sessionid = request.getSession().getId();
				Cart dbcart = this.cartManager.getCartByProductId(product.getProduct_id(), sessionid);
				if(dbcart!=null){
					cartManager.delete(sessionid, dbcart.getCart_id());
				}
				Map<String, String> map=storeCartPublicMethod.addKaiDianCart(product, 1, 0);
				if(map.get("code").equals("failed")){
					return JsonResultUtil.getErrorJson(map.get("msg"));
				}
			
				storeCartManager.countSelectPrice("yes");
				//创建订单
				Order order = innerCreateOrder();
				storeManager.editStorePayOrderId(order.getOrder_id(), storeMember.getStore_id());
				Map<String, Object> map1 = new HashMap<>();
				map1.put("order_id", order.getOrder_id());
				return JsonResultUtil.getObjectJson(map1);
			}
		} catch (Exception e) {
			this.logger.error("申请失败:"+e);
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("申请失败");
		}
	}
	/**
	 * 创建订单
	 * 
	 * @return
	 */
	private Order innerCreateOrder() {
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();

		Integer shippingId = 0; // 主订单没有配送方式

		Integer paymentId = StringUtil.toInt(request.getParameter("paymentId"), 0);

		Order order = new Order();
		order.setShipping_id(shippingId); // 配送方式
		order.setPayment_id(paymentId);// 支付方式
		if (paymentId == 0) { // 如果支付方式为0，是在线支付
			order.setIs_online(1);
		}

		order.setShip_day("任意时间");
		
		String sessionid = request.getSession().getId();
		order = this.storeOrderManager.createkaidianOrder(order, sessionid);
		return order;
	}
}
