package com.enation.app.shop.mobile.action.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.b2b2c.component.bonus.service.B2b2cBonusSession;
import com.enation.app.b2b2c.component.bonus.service.IB2b2cBonusManager;
import com.enation.app.b2b2c.core.goods.model.StoreProduct;
import com.enation.app.b2b2c.core.goods.service.StoreCartContainer;
import com.enation.app.b2b2c.core.order.model.cart.StoreCartItem;
import com.enation.app.b2b2c.core.order.service.cart.IStoreCartManager;
import com.enation.app.b2b2c.core.order.service.cart.IStoreProductManager;
import com.enation.app.b2b2c.core.store.service.activity.IStoreActivityManager;
import com.enation.app.b2b2c.front.api.order.publicmethod.StoreCartPublicMethod;
import com.enation.app.base.core.model.Member;
import com.enation.app.shop.component.bonus.model.BonusType;
import com.enation.app.shop.component.bonus.service.IBonusTypeManager;
import com.enation.app.shop.core.goods.model.Goods;
import com.enation.app.shop.core.goods.model.Product;
import com.enation.app.shop.core.goods.service.IGoodsManager;
import com.enation.app.shop.core.goods.service.IProductManager;
import com.enation.app.shop.core.order.model.support.CartItem;
import com.enation.app.shop.core.order.model.support.OrderPrice;
import com.enation.app.shop.core.order.plugin.cart.CartPluginBundle;
import com.enation.app.shop.core.order.service.ICartManager;
import com.enation.app.shop.core.other.model.Activity;
import com.enation.app.shop.core.other.model.ActivityGift;
import com.enation.app.shop.core.other.service.IActivityGiftManager;
import com.enation.app.shop.core.other.service.IActivityManager;
import com.enation.eop.sdk.context.UserConext;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.util.JsonResultUtil;

import net.sf.json.JSONObject;

/**
 * Created by Dawei on 4/28/15.
 */
@Controller("mobileCartApiController")
@RequestMapping("/api/mobile/cart")
public class CartApiController {

    @Autowired
    private IProductManager productManager;

    @Autowired
    private ICartManager cartManager;

    @Autowired
    private IStoreCartManager storeCartManager;

    @Autowired
    private CartPluginBundle cartPluginBundle;

    @Autowired
    private IB2b2cBonusManager b2b2cBonusManager;

    @Autowired
    private IStoreProductManager storeProductManager;

    @Autowired
    private StoreCartPublicMethod storeCartPublicMethod;

    @Autowired
    private IActivityManager activityManager;

    @Autowired
    private IStoreActivityManager storeActivityManager;

    @Autowired
    private IActivityGiftManager activityGiftManager;
    
    @Autowired
    private IBonusTypeManager bonusTypeManager;
	@Autowired
	private IGoodsManager goodsManager;

    /**
     * 将一个货品添加至购物车
     * 需要传递productid和num参数
     *
     * @param productid   货品id，int型
     * @param num         数量
     * @return 返回json串
     * result  为1表示调用成功0表示失败 ，int型
     * message 为提示信息
     */
    @ResponseBody
    @RequestMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject add(Integer productid, Integer num,Integer storeid) {
        if (num == null || num <= 0) {
            num = 1;
        }
        if (storeid == null) {
        	storeid = 0;
        }
        StoreProduct product = storeProductManager.get(productid);
        try {
            JsonResult jsonResult = storeCartPublicMethod.addCart(product, num, 0,storeid);
            if(jsonResult.getResult() == 1){
                String sessionId = ThreadContextHolder.getHttpRequest().getSession().getId();
                Integer count = this.cartManager.countItemNum(sessionId);
                return JSONObject.fromObject("{\"result\":1,\"count\":" + count + "}");
            }else{
            	return JSONObject.fromObject("{\"result\":0,\"message\":\"" + jsonResult.getMessage() + "\"}");
            }
        }catch (RuntimeException ex){
        	return JSONObject.fromObject("{\"result\":0,\"message\":\"" + ex.getMessage() + "\"}");
        }
    }

    /**
     * 删除购物车中的报单商品和积分商品
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delete-declar-goods", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult deleteDeclarOrIntegral() {
    	try {
    		Member member = UserConext.getCurrentMember();
    		if (member == null) {
				return JsonResultUtil.getErrorJson("会员未登录！");
			}
    		List<Map> itemList = this.cartManager.listGoods(member.getMember_id());
    		if (itemList != null) {
    			for (Map map : itemList) {
    				Goods goods = this.goodsManager.getGoods(Integer.parseInt(map.get("goods_id").toString()));
					if (goods.getIs_integral_goods().intValue() != 0) {
						this.cartManager.deleteDeclarOrPointGoods(member.getMember_id(), Integer.parseInt(map.get("goods_id").toString()));
					}
    			}
    		}
    		return JsonResultUtil.getSuccessJson("操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("操作失败");
		}
    }
    
    /**
     * 获取购物车中的商品列表
     *
     * @return 购物车商品列表json
     */
    @ResponseBody
    @RequestMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult list() {
        //获取购物车商品列表
        storeCartManager.countPrice("no");
        List<Map> cartItemList = StoreCartContainer.getStoreCartListFromSession();
        if(cartItemList != null && cartItemList.size() > 0) {
            for (Map map : cartItemList) {
                List<StoreCartItem> List = (List) map.get("goodslist");
                for (StoreCartItem storeCartItem : List) {
                    storeCartItem.setImage_default(StaticResourcesUtil.convertToUrl(storeCartItem.getImage_default()));
                }

                //店铺活动详情
                if(map.containsKey("activity_id")){
                    int activity_id = NumberUtils.toInt(map.get("activity_id").toString(), 0);
                    Activity activity = activityManager.get(activity_id);
                    if(activity != null){
                        Map activityMap = activityManager.getActMap(activity.getActivity_id());
                        if(activityMap.containsKey("is_send_gift") && NumberUtils.toInt(activityMap.get("is_send_gift").toString(), 0) == 1){
                            int gift_id = NumberUtils.toInt(activityMap.get("gift_id").toString(), 0);
                            if(gift_id > 0){
                                ActivityGift gift = activityGiftManager.get(gift_id);
                                if(gift != null){
                                    gift.setGift_img(StaticResourcesUtil.convertToUrl(gift.getGift_img()));
                                    activityMap.put("gift", gift);
                                }
                            }
                        }
                        map.put("activity", activityMap);
                    }
                }
            }
        }

        //计算总价
        String sessionId = ThreadContextHolder.getHttpRequest().getSession().getId();
        OrderPrice orderprice = this.cartManager.countPrice(cartManager.selectListGoods(sessionId), null, null);
        storeCartManager.countSelectPrice("no");
        orderprice = this.cartPluginBundle.coutPrice(orderprice);

        //获取购物车数量
        int count = this.cartManager.countItemNum(sessionId);

        Map<String, Object> data = new HashMap<>();
        data.put("count", count);//购物车中的商品数量
        data.put("total", orderprice.getOrderPrice());//总价

        //购物车中的商品列表
        data.put("storelist", cartItemList);

        return JsonResultUtil.getObjectJson(data);
    }

    /**
     * 获取购物车中的选中的商品列表
     * @return 购物车商品列表json
     */
    @ResponseBody
    @RequestMapping(value = "/list-selected", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult listSelected() {
    	//清空存放在session中的已勾选的优惠券，防止用户进入订单结算页，选定优惠券后，返回购物车添加或修改商品时，选定的优惠券还存在的问题。
    	B2b2cBonusSession.cleanAll();
    	
        Member member = UserConext.getCurrentMember();
        //获取购物车商品列表
        storeCartManager.countSelectPrice("yes");
        List<Map> cartItemList = StoreCartContainer.getSelectStoreCartListFromSession();
        if(cartItemList != null && cartItemList.size() > 0) {
            for (Map map : cartItemList) {
                List<StoreCartItem> List = (List) map.get("goodslist");
                for (StoreCartItem storeCartItem : List) {
                    storeCartItem.setImage_default(StaticResourcesUtil.convertToUrl(storeCartItem.getImage_default()));
                    //商品类型
                    storeCartItem.setFlag(storeCartItem.getFlag());
                }
                //店铺的优惠券列表
                Integer store_id = NumberUtils.toInt(map.get("store_id").toString(), 0);
                OrderPrice orderPrice = (OrderPrice)map.get("storeprice");
                if(member != null){
                	
                   Page webPage=  this.b2b2cBonusManager.getMyBonusByIsUsable(1, 100, member.getMember_id(), 1, orderPrice.getGoodsPrice(), store_id); 
                   List bonusList = (java.util.List) webPage.getResult();
                   map.put("bonuslist", bonusList);
                }
                
              //店铺活动详情
                if(map.containsKey("activity_id")){
                    int activity_id = NumberUtils.toInt(map.get("activity_id").toString(), 0);
                    Activity activity = activityManager.get(activity_id);
                    if(activity != null){
                        Map activityMap = activityManager.getActMap(activity.getActivity_id());
                        if(activityMap.containsKey("is_send_gift") && NumberUtils.toInt(activityMap.get("is_send_gift").toString(), 0) == 1){
                            int gift_id = NumberUtils.toInt(activityMap.get("gift_id").toString(), 0);
                            if(gift_id > 0){
                                ActivityGift gift = activityGiftManager.get(gift_id);
                                if(gift != null){
                                    gift.setGift_img(StaticResourcesUtil.convertToUrl(gift.getGift_img()));
                                    activityMap.put("gift", gift);
                                }
                            }
                        }
                        //判断是否包含优惠券
        				if(activityMap.containsKey("is_send_bonus") && NumberUtils.toInt(activityMap.get("is_send_bonus").toString(), 0) == 1){
        					int bonus_id = NumberUtils.toInt(activityMap.get("bonus_id").toString(), 0);
        					if(bonus_id>0){
        						BonusType bonus =  this.bonusTypeManager.get(bonus_id);
        						activityMap.put("bonus", bonus);
        					}
        				}
                        map.put("activity", activityMap);
                    }
                }
            }
        }
        return JsonResultUtil.getObjectJson(cartItemList);
    }

    /**
     * 选择或取消选择货品进行下单
     *
     * @param product_id 货品id
     * @param checked    是否选中
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/check-product", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult checkProduct(Integer product_id, boolean checked) {
        String sessionId = ThreadContextHolder.getHttpRequest().getSession().getId();
        cartManager.checkProduct(sessionId, product_id, checked);
        return JsonResultUtil.getSuccessJson("选择购物车商品成功");
    }

    /**
     * 选择或取消选择所有货品
     *
     * @param checked 是否选中
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/check-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult checkAll(boolean checked) {
        String sessionId = ThreadContextHolder.getHttpRequest().getSession().getId();
        cartManager.checkAll(sessionId, checked);
        return JsonResultUtil.getSuccessJson("选择购物车商品成功");
    }

    /**
     * 选择或取消选择店铺下的货品
     *
     * @param store_id 店铺id
     * @param checked  是否选中
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/check-store", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult checkStore(Integer store_id, boolean checked) {
        HttpServletRequest request = ThreadContextHolder.getHttpRequest();
        try {
            String sessionid = request.getSession().getId();
            storeCartManager.checkStoreAll(sessionid, checked, store_id);
            return JsonResultUtil.getSuccessJson("选择购物车商品成功");
        } catch (RuntimeException e) {
            return JsonResultUtil.getErrorJson("选择购物车商品出错");
        }
    }

    /**
     * 删除购物车一项
     *
     * @param cartids:要删除的购物车id,int型,即 CartItem.item_id
     * @return 返回json字串
     */
    @ResponseBody
    @RequestMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject delete(Integer[] cartids) {
        if (cartids == null || cartids.length == 0) {
            return JSONObject.fromObject("{\"result\":0,\"message\":\"删除购物项失败\"}");
        }
        try {
            HttpServletRequest request = ThreadContextHolder.getHttpRequest();
            for (Integer cartid : cartids) {
                cartManager.delete(request.getSession().getId(), cartid);
            }

            Integer count = this.cartManager.countItemNum(request.getSession().getId());
            return JSONObject.fromObject("{\"result\":1,\"count\":" + count + "}");
        } catch (RuntimeException e) {
        	return JSONObject.fromObject("{\"result\":0,\"message\":\"删除购物项失败\"}");
        }
    }

    /**
     * 更新购物车的数量
     *
     * @param cartid:要更新的购物车项id，int型，即 CartItem.item_id
     * @param num:要更新数量,int型
     * @param productid
     * @return 返回json字串
     */
    @ResponseBody
    @RequestMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult updateNum(Integer cartid, Integer num, Integer productid) {
        try {
            Product product = productManager.get(Integer.valueOf(productid));
            if (product == null) {
                return JsonResultUtil.getErrorJson("此商品不存在！");
            }

            Integer store = product.getEnable_store();
            if (store == null)
                store = 0;

            if (store < num) {
                return JsonResultUtil.getErrorJson("要购买的商品数量超出库存！");
            }
        	Goods goods = goodsManager.getGoods(product.getGoods_id());
			if(goods.getFlag() == 4){
				return JsonResultUtil.getErrorJson("免费商品只能是一件哦");
			}
            cartManager.updateNum(ThreadContextHolder.getHttpRequest()
                    .getSession().getId(), cartid, num);
            return JsonResultUtil.getSuccessJson("更新数量成功！");

        } catch (RuntimeException e) {
            return JsonResultUtil.getErrorJson("更新购物车数量出现意外错误");
        }
    }

    /**
     * 购物车的价格总计信息
     *
     * @return 返回json字串
     */
    @ResponseBody
    @RequestMapping(value = "/total", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult total() {
        HttpServletRequest request = ThreadContextHolder.getHttpRequest();
        List<CartItem> itemList = cartManager.selectListGoods(request.getSession().getId());
        OrderPrice orderprice = this.cartManager.countPrice(itemList, null, null);
        //触发价格计算事件
        orderprice  = this.cartPluginBundle.coutPrice(orderprice);
        return JsonResultUtil.getObjectJson(orderprice);
    }

    /**
     * 计算购物车货物总数
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject count() {
        HttpServletRequest request = ThreadContextHolder.getHttpRequest();
        Integer count = this.cartManager.countItemNum(request.getSession()
                .getId());
        return JSONObject.fromObject("{\"result\":1,\"count\":" + count + "}");
    }

    /**
     * 清空购物车
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/clean", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult clean() {
        cartManager.clean(ThreadContextHolder.getHttpRequest().getSession()
                .getId());
        return JsonResultUtil.getSuccessJson("清空购物车成功");
    }
}
