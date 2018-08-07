package com.enation.app.shop.mobile.action.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.b2b2c.component.bonus.model.StoreBonus;
import com.enation.app.b2b2c.component.bonus.model.StoreBonusType;
import com.enation.app.b2b2c.component.bonus.service.B2b2cBonusSession;
import com.enation.app.b2b2c.component.bonus.service.IB2b2cBonusManager;
import com.enation.app.b2b2c.component.plugin.order.StoreCartPluginBundle;
import com.enation.app.b2b2c.core.goods.service.StoreCartContainer;
import com.enation.app.b2b2c.core.goods.service.StoreCartKeyEnum;
import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.b2b2c.core.order.model.cart.StoreCartItem;
import com.enation.app.b2b2c.core.order.service.IStoreOrderManager;
import com.enation.app.b2b2c.core.store.service.activity.IStoreActivityGiftManager;
import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.app.base.core.service.IRegionsManager;
import com.enation.app.shop.component.bonus.model.Bonus;
import com.enation.app.shop.component.bonus.model.MemberBonus;
import com.enation.app.shop.component.bonus.service.IBonusManager;
import com.enation.app.shop.component.receipt.service.IReceiptManager;
import com.enation.app.shop.core.goods.model.Goods;
import com.enation.app.shop.core.goods.service.GoodsFlagEnum;
import com.enation.app.shop.core.goods.service.IGoodsManager;
import com.enation.app.shop.core.member.model.MemberAddress;
import com.enation.app.shop.core.member.service.IMemberAddressManager;
import com.enation.app.shop.core.order.model.Delivery;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.model.OrderGift;
import com.enation.app.shop.core.order.model.OrderItem;
import com.enation.app.shop.core.order.model.SellBack;
import com.enation.app.shop.core.order.model.SellBackGoodsList;
import com.enation.app.shop.core.order.model.support.CartItem;
import com.enation.app.shop.core.order.model.support.OrderPrice;
import com.enation.app.shop.core.order.plugin.cart.CartPluginBundle;
import com.enation.app.shop.core.order.service.ICartManager;
import com.enation.app.shop.core.order.service.IExpressManager;
import com.enation.app.shop.core.order.service.IOrderBonusManager;
import com.enation.app.shop.core.order.service.IOrderFlowManager;
import com.enation.app.shop.core.order.service.IOrderGiftManager;
import com.enation.app.shop.core.order.service.IOrderManager;
import com.enation.app.shop.core.order.service.IOrderReportManager;
import com.enation.app.shop.core.order.service.IPaymentManager;
import com.enation.app.shop.core.order.service.ISellBackManager;
import com.enation.app.shop.core.order.service.OrderStatus;
import com.enation.app.shop.core.other.model.ActivityDetail;
import com.enation.app.shop.core.other.service.IActivityDetailManager;
import com.enation.app.shop.mobile.model.ApiSellBack;
import com.enation.app.shop.mobile.service.ApiOrderManager;
import com.enation.app.shop.mobile.vo.OrderVo;
import com.enation.eop.sdk.context.UserConext;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.util.CurrencyUtil;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

import net.sf.json.JSONArray;

/**
 * Created by Dawei on 4/29/15.
 */
@Controller("mobileOrderApiController")
@RequestMapping("/api/mobile/order")
public class OrderApiController {
    @Autowired
    private IOrderManager orderManager;
    
    @Autowired
	private IGoodsManager goodsManager;

    @Autowired
    private ApiOrderManager apiOrderManager;

    @Autowired
    private IMemberAddressManager memberAddressManager;

    @Autowired
    private ICartManager cartManager;

    @Autowired
    private IReceiptManager receiptManager;

    @Autowired
    private IOrderFlowManager orderFlowManager;

    @Autowired
    private CartPluginBundle cartPluginBundle;

    @Autowired
    private IBonusManager bonusManager;

    @Autowired
    private ISellBackManager sellBackManager;

    @Autowired
    private IOrderReportManager orderReportManager;

    @Autowired
    private IExpressManager expressManager;
    
    @Autowired
    private IB2b2cBonusManager b2b2cBonusManager;
    
    @Autowired
    private IStoreMemberManager storeMemberManager;

    /**
     * 促销活动优惠详细管理接口
     */
    @Autowired
    private IActivityDetailManager activityDetailManager;

    /**
     * 促销活动赠品管理接口
     */
    @Autowired
    private IStoreActivityGiftManager storeActivityGiftManager;


    /**
     * 行政区划管理接口
     */
    @Autowired
    private IRegionsManager regionsManager;

    @Autowired
    private IStoreOrderManager storeOrderManager;

    @Autowired
    private StoreCartPluginBundle storeCartPluginBundle;

    @Autowired
    private IOrderGiftManager orderGiftManager;

    @Autowired
    private IOrderBonusManager orderBonusManager;

    /**
     * 支付方式管理接口
     */
    @Autowired
    private IPaymentManager paymentManager;
    
    @Autowired
    private IMemberManager memberManager;

    private final int PAGE_SIZE = 20;

    /**
     * 提交订单
     *
     * @param orderVo
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
    @RequestMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult create(@ModelAttribute OrderVo orderVo) {
    	
    	
        Member member = UserConext.getCurrentMember();
        if (member == null) {
        	return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
        }

        HttpServletRequest request = ThreadContextHolder.getHttpRequest();

        if (orderVo.getPaymentId() == null || orderVo.getPaymentId() <= 0) {
        	return JsonResultUtil.getErrorJson("支付方式不能为空");
        }

        // 收货地址
        if (orderVo.getAddressId() == null || orderVo.getAddressId() <= 0) {
        	return JsonResultUtil.getErrorJson("请选择收货地址");
        }
        MemberAddress address = memberAddressManager.getAddress(orderVo.getAddressId());
        if (address == null || !address.getMember_id().equals(member.getMember_id())) {
        	return JsonResultUtil.getErrorJson("收货地址不存在！");
        }
    	String sessionid = request.getSession().getId();
		// 读取所有的购物项，用于创建主订单
		List<CartItem> cartItemList = this.cartManager.listGoods(sessionid);
		if(!this.storeOrderManager.checkFreeCart(cartItemList)){
			return JsonResultUtil.getErrorJson("购物车免费商品太多，一天只能免费领取一件哦");
		}
		if(!this.storeOrderManager.checkFreeOrderToDay()){
			boolean flag = false;
			for (CartItem cartItem : cartItemList) {
				if (cartItem != null && cartItem.getIs_check() == 1) {
					Goods goods = goodsManager.getGoods(cartItem.getGoods_id());
					if(GoodsFlagEnum.FREE.getType() == goods.getFlag()){//该商品是否为免费领取
						flag = true;
						break;
					}
				}
			}
			if (flag) {
				return JsonResultUtil.getErrorJson("今天不能领取更多免费商品了");
			}
		}
		//如果是保税仓商品  需要 填写身份证信息
		if(!this.storeOrderManager.checkBaoshuicanCart(cartItemList)){
			if(orderVo.getId_number() == null || StringUtil.isEmpty(orderVo.getId_number())){
				if(StringUtil.isEmpty(address.getId_number())){
					return JsonResultUtil.getErrorJson("该商品为保税商品，请【更新APP】进行购买");
				}
			}
			address.setId_number(orderVo.getId_number());
			memberAddressManager.updateAddress(address);
//			if(StringUtil.isEmpty(address.getId_number()) && StringUtil.isEmpty(address.getIdf_img()) &&
//					StringUtil.isEmpty(address.getIdz_img())){
//				return JsonResultUtil.getErrorJson("请在收货地址里面填写身份证信息");
//			}
		}

        try {
            // 获取用户选择的支付方式ID
            // 通过支付ID获取支付方式类型
            String payType = this.paymentManager.get(orderVo.getPaymentId()).getType();
            // 如果用户选择的是货到付款
            if (payType.equals("cod")) {
                // 如果用户选择的收货地区不支持货到付款(对省、市、区三级都要做判断)
                if (regionsManager.get(address.getProvince_id()).getCod() != 1) {
                	return JsonResultUtil.getErrorJson("您选择的收货地址不支持货到付款！");
                }
               
                if (regionsManager.get(address.getCity_id()).getCod() != 1) {
                	return JsonResultUtil.getErrorJson("您选择的收货地址不支持货到付款！");
                }
                if (address.getRegion_id()!=null && address.getRegion_id()!=0 && regionsManager.get(address.getRegion_id()).getCod() != 1) {
                	return JsonResultUtil.getErrorJson("您选择的收货地址不支持货到付款！");
                }
                if (address.getTown_id()!=null && address.getTown_id()!=0 && regionsManager.get(address.getTown_id()).getCod() != 1) {
                	return JsonResultUtil.getErrorJson("您选择的收货地址不支持货到付款！");
                }
            }
            
            //创建订单
            Order order = new Order();
            order.setShipping_id(0);  // 主订单没有配送方式
            order.setPayment_id(orderVo.getPaymentId());// 支付方式

            order.setShip_provinceid(address.getProvince_id());
            order.setShip_cityid(address.getCity_id());
            order.setShip_regionid(address.getRegion_id());

            order.setShip_addr(address.getAddr());
            order.setShip_mobile(address.getMobile());
            order.setShip_tel(address.getTel());
            order.setShip_zip(address.getZip());
            String shiparea = address.getCity() + " " +address.getProvince() ;
            if(address.getRegion()!=null){
            	shiparea = shiparea + " " + address.getRegion();
            }
            if(address.getTown()!=null){
            	shiparea = shiparea + " " + address.getTown();
            }
            order.setShipping_area(shiparea);
            order.setShip_name(address.getName());
            order.setRegionid(address.getRegion_id());

            order.setMemberAddress(address);
            order.setShip_day(orderVo.getShippingTime());
            order.setShip_time("");
            order.setRemark(orderVo.getRemark());
            order.setAddress_id(address.getAddr_id());
            try {
            	 order = this.storeOrderManager.createOrder(order, sessionid);
            	 
            	//如果是积分商品订单，需要扣除会员的乐币并刷新当前session中的会员信息
     			if (order.getIs_integral_order().intValue() == 1) {
     				this.memberManager.updateMemberPoint(order.getMember_id(), order.getNeed_pay_money(), 2, "购买乐币商品");
     				this.memberManager.updateSessionMember(order.getMember_id());
     			}
			} catch (Exception e) {
				e.printStackTrace();
			}
            return JsonResultUtil.getObjectJson(order);
        } catch (RuntimeException e) {
        	return JsonResultUtil.getErrorJson("创建订单失败，请您重试！");
        }
    }
    /**
     * nieruijie
     * 2018-7-9
     * 创建大转盘支付订单
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/createpayorder",produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult createPayOrder(){
    	HttpServletRequest request = ThreadContextHolder.getHttpRequest();
    	Member member = UserConext.getCurrentMember();
		Integer shippingId = 0; // 主订单没有配送方式

		Map<Object, Object> map;
		try {
			Integer paymentId = StringUtil.toInt(request.getParameter("paymentId"), 0);

			Order order = new Order();
			order.setShipping_id(shippingId); // 配送方式
			order.setPayment_id(paymentId);// 支付方式
			if(member==null){
				String message="用户未登录！";
				return JsonResultUtil.getErrorJson(message);
			}else{
			order.setMember_id(member.getMember_id());
			}
			if (paymentId == 0) { // 如果支付方式为0，是在线支付
				order.setIs_online(1);
			}
			//获取抽奖设置的价格
			Double money = this.storeOrderManager.getMoneySet();
			order.setShip_day("任意时间");
			order.setNeed_pay_money(money);
			order.setGoods_amount(money); //商品价格
			order.setOrder_amount(money); //订单价格
			order.setShipping_amount(0.00); //配送费用
			order.setDiscount(0.00); //优惠金额
			order.setWeight(1.00); //订单重量
			order.setProtect_price(0.00); //保价
			order.setAct_discount(0.00); //促销活动优惠金额
			order.setAdmin_remark("购买抽奖次数订单"); //订单特殊处理
			String sessionid = request.getSession().getId();
			order = this.storeOrderManager.createpayOrderC(order);
			map = this.storeOrderManager.getOrderMap(order.getParent_id());
			return JsonResultUtil.getObjectJson(map);
		} catch (Exception e) {
			e.printStackTrace();
			return JsonResultUtil.getObjectJson("转盘订单--创建失败！");
		}
    }
    /**
     * 订单列表
     *
     * @param page   第几页
     * @param status 订单状态
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult list(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                       @RequestParam(value = "status", required = false, defaultValue = "") String status) {
        Member member = UserConext.getCurrentMember();
        if (member == null) {
        	return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
        }
        if (page == null || page <= 0) {
            page = 1;
        }
        Page ordersPage = storeOrderManager.pageBuyerOrders(page, PAGE_SIZE, status, "");
        List<Order> orderList = (List<Order>) ordersPage.getResult();

        //赠品及优惠券
        for (Order order : orderList) {
            if (order.getGift_id() != null && order.getGift_id().intValue() > 0) {
                OrderGift orderGift = orderGiftManager.getOrderGift(order.getGift_id(), order.getOrder_id());
                if(orderGift != null) {
                    orderGift.setGift_img(StaticResourcesUtil.convertToUrl(orderGift.getGift_img()));
                    order.getFields().put("gift", orderGift);
                }
            }
            if (order.getBonus_id() != null && order.getBonus_id().intValue() > 0) {
                order.getFields().put("bonus", orderBonusManager.getOrderBonus(order.getBonus_id(), order.getOrder_id()));
            } 
        }
        return JsonResultUtil.getObjectJson(JSONArray.fromObject((List)ordersPage.getResult()));
    }

    // private String getStackTraceAsString(Exception e) {
    // // StringWriter将包含堆栈信息
    // StringWriter stringWriter = new StringWriter();
    // //必须将StringWriter封装成PrintWriter对象，
    // //以满足printStackTrace的要求
    // PrintWriter printWriter = new PrintWriter(stringWriter);
    // //获取堆栈信息
    // e.printStackTrace(printWriter);
    // //转换成String，并返回该String
    // StringBuffer error = stringWriter.getBuffer();
    // return error.toString();
    // }

    /**
     * 订单详情
     *
     * @param orderid 订单id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult detail(Integer orderid) {
        Member member = UserConext.getCurrentMember();
        if (member == null) {
        	return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
        }

        Order order = orderManager.get(orderid);
        if (order == null) {
        	return JsonResultUtil.getErrorJson("此订单不存在！");
        }
        if (!order.getMember_id().equals(member.getMember_id())) {
        	return JsonResultUtil.getErrorJson("您没有权限进行此项操作！");
        }
        order.setItems_json("");

        List<OrderItem> orderItemList = orderManager.listGoodsItems(orderid);
        for (OrderItem orderItem : orderItemList) {
            orderItem.setImage(StaticResourcesUtil.convertToUrl(orderItem.getImage()));
        }
        order.setOrderItemList(orderManager.listGoodsItems(orderid));

        //赠品及优惠券
        if (order.getGift_id() != null && order.getGift_id().intValue() > 0) {
            OrderGift orderGift = orderGiftManager.getOrderGift(order.getGift_id(), order.getOrder_id());
            if(orderGift != null) {
                orderGift.setGift_img(StaticResourcesUtil.convertToUrl(orderGift.getGift_img()));
                order.getFields().put("gift", orderGift);
            }
        }
        if (order.getBonus_id() != null && order.getBonus_id().intValue() > 0) {
            order.getFields().put("bonus", orderBonusManager.getOrderBonus(order.getBonus_id(), order.getOrder_id()));
        }
        
        Map dataMap = new HashMap();
        dataMap.put("order", order);
        if(orderItemList != null && orderItemList.size() > 0){
        	dataMap.put("flag", goodsManager.get(orderItemList.get(0).getGoods_id()).get("flag"));
        }
        dataMap.put("receipt", receiptManager.getByOrderid(order.getOrder_id()));
        return JsonResultUtil.getObjectJson(dataMap);
    }

    /**
     * 取消订单
     *
     * @param orderid 订单id
     * @param reason  取消原因
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult cancel(Integer orderid, String reason) {
        Member member = UserConext.getCurrentMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
        }
        Order order = orderManager.get(orderid);
        if (order == null || !order.getMember_id().equals(member.getMember_id())) {
            return JsonResultUtil.getErrorJson("此订单不存在！");
        }
        if (order.getStatus().intValue() != OrderStatus.ORDER_NOT_PAY
                && order.getStatus().intValue() != OrderStatus.ORDER_PAY
                && order.getStatus().intValue() != OrderStatus.ORDER_CONFIRM) {
            return JsonResultUtil.getErrorJson("此订单不能取消，请联系客服人员！");
        }

        //不重复提交取消申请
        if (order.getIs_cancel().intValue() == 0) {
            this.orderFlowManager.cancel(orderid, reason);
        }
        return JsonResultUtil.getSuccessJson("取消订单成功");
    }

    /**
     * 确认收货
     *
     * @param orderid 订单id
     * @return 返回json串 result 为1表示添加成功0表示失败 ，int型 message 为提示信息
     */
    @ResponseBody
    @RequestMapping(value = "/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult rogConfirm(Integer orderid) {
        Member member = UserConext.getCurrentMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
        }
        Order order = orderManager.get(orderid);
        if (order == null || !order.getMember_id().equals(member.getMember_id())) {
            return JsonResultUtil.getErrorJson("此订单不存在！");
        }
        if (order.getStatus().intValue() != OrderStatus.ORDER_SHIP) {
            return JsonResultUtil.getErrorJson("此订单不能确认收货，请联系客服人员！");
        }

        this.orderFlowManager.rogConfirm(orderid, member.getMember_id(),
                member.getUname(), member.getUname(),
                DateUtil.getDateline());
        return JsonResultUtil.getSuccessJson("确认收货成功");
    }

    /**
     * 可用优惠券列表
     * @author xulipeng 2016年12月12日  修复前：读取的优惠券按照所有店铺的总价的问题<br>
     * 修改后：根据每个店铺的商品总价读取可用优惠券
     * 返回格式：Map{totalNum:*,totalStoreBonus:List<StoreBonus>} 
     * <br>其中totalNum：所有店铺(可用或者不可用)优惠券数量,totalStoreBonus:所有店铺的(可用或者不可用)优惠券对象
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
    @RequestMapping(value = "/bonus", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult bonus() {
    	// 获取以店铺id分类的购物车列表
		List<Map> storeGoodsList = StoreCartContainer.getSelectStoreCartListFromSession();
		Member member = UserConext.getCurrentMember();
		
		//所有店铺优惠券集合
		List<StoreBonus> totalStoreBonus = new ArrayList();
		long totalNum = 0;
		
        for (Map map : storeGoodsList) {
        	// 获取此店铺id
			int store_id = (Integer) map.get(StoreCartKeyEnum.store_id.toString());
			
			// 获取店铺名称
			String store_name = (String) map.get(StoreCartKeyEnum.store_name.toString());
			
			// 调用核心api计算总订单的价格，商品价：所有商品，商品重量：
			OrderPrice orderPrice = (OrderPrice) map.get(StoreCartKeyEnum.storeprice.toString());
			
			Page webPage=  this.b2b2cBonusManager.getMyBonusByIsUsable(1, 100, member.getMember_id(), 1, orderPrice.getGoodsPrice(), store_id); 
			
			Long totalCount = webPage.getTotalCount();
			List<Bonus> bonusList = (List) webPage.getResult();
			bonusList = bonusList == null ? new ArrayList() : bonusList;
			
			//获得此店铺已使用的优惠券
			MemberBonus memberBonus = B2b2cBonusSession.getB2b2cBonus(store_id);
			
			//判断是否为空 并且 查询的是可用优惠券
			if(memberBonus!=null){
				
				for (Bonus bonus : bonusList) {
					//如果相等 设为已使用的状态
					if(bonus.getBonus_id().equals(memberBonus.getBonus_id())){
						bonus.setIs_used(1);
					}else{
						bonus.setIs_used(0);
					}
				}
			}
			
			//生成店铺优惠券对象
			StoreBonus storeBonus = new StoreBonus();
			storeBonus.setStore_id(store_id);
			storeBonus.setStore_name(store_name);
			storeBonus.setBonusList(bonusList);
			
			//所有店铺优惠券数量 计数
			totalNum += totalCount;
			
			totalStoreBonus.add(storeBonus);
		}
        
        Map map = new HashMap();
		map.put("totalNum", totalNum);
		map.put("totalStoreBonus", totalStoreBonus);
    	
    	return JsonResultUtil.getObjectJson(map);
    }
    

    /**
     * 
     * 此api作废，请使用 /api/b2b2c/bonus/use-bonus.do  by_xulipeng  2017年01月09日
     */
    @ResponseBody
    @RequestMapping(value = "/use-bonus")
    public JsonResult useOne(Integer bonusid) {
//        try {
//            Member member = UserConext.getCurrentMember();
//            if (member == null) {
//                return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
//            }
//
//            // 清除之前使用的红包
//            if (bonusid == null || bonusid <= 0) {
//                BonusSession.clean();
//                return JsonResultUtil.getSuccessJson("清除优惠券信息成功！");
//            }
//
//            MemberBonus bonus = b2b2cBonusManager.getOneMyBonus(member.getMember_id(), store_id, bonusid);
//            if (!bonus.getMember_id().equals(member.getMember_id())) {
//                return JsonResultUtil.getErrorJson("您没有权限进行此项操作！");
//            }
//
//            Double goodsprice = cartManager.countGoodsTotal(RequestContextHolder.getRequestAttributes().getSessionId());
//            if (goodsprice <= bonus.getMin_goods_amount()) {
//                return JsonResultUtil.getErrorJson("订单的商品金额不足["
//                        + bonus.getMin_goods_amount() + "],不能使用此红包");
//            }
//            BonusSession.useOne(bonus);
//            return JsonResultUtil.getSuccessJson("红包使用成功");
//        } catch (Exception e) {
//            return JsonResultUtil.getErrorJson("使用红包发生错误，请您重试！");
//        }
    	return null;
    }

    @ResponseBody
    @RequestMapping(value = "/orderprice")
    public JsonResult orderPrice(Integer regionid, Integer shippingid) {
        HttpServletRequest request = ThreadContextHolder.getHttpRequest();
        String sessionid = request.getSession().getId();
        
        List<CartItem> cartList  = cartManager.listGoods(sessionid);
		//计算订单价格
		OrderPrice orderprice  =this.cartManager.countPrice(cartList, shippingid,regionid.toString());
		//激发价格计算事件
		orderprice  = this.cartPluginBundle.coutPrice(orderprice);
		
        return JsonResultUtil.getObjectJson(orderprice);
    }

    /**
     * 退货申请
     *
     * @param order_id       订单id
     * @param goods_num      商品退货申请数量
     * @param item_id        【必填】订单项id
     * @param refund_way     【必填】退款方式
     * @param remark         【必填】 描述
     * @param return_account 【必填】退款账号
     * @param reason         退货原因
     * @param ship_status    是否收到货
     * @param apply_alltotal 退款金额
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/returned", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult apply(int order_id, Integer[] goods_num, Integer[] item_id,
                            String refund_way, String remark, String return_account,
                            String reason, Integer ship_status, double apply_alltotal) {
        try {
            if (StringUtil.isEmpty(refund_way)) {
                return JsonResultUtil.getErrorJson("退款方式不能为空");
            }

            if (StringUtil.isEmpty(return_account)) {
                return JsonResultUtil.getErrorJson("退款账号不能为空");
            }

            Member member = UserConext.getCurrentMember();
            if (member == null) {
                return JsonResultUtil.getErrorJson("请您在登录后再申请退货！");
            }

            Order order = orderManager.get(order_id);
            if (order == null
                    || !order.getMember_id().equals(member.getMember_id())) {
                return JsonResultUtil.getErrorJson("此订单不存在！");
            }
            ArrayList<OrderItem> orderItems = new ArrayList<>();
            for (int i = 0; i < item_id.length; i++) {
            	 OrderItem orderItem = apiOrderManager.getItem(item_id[i]);	
            	 orderItems.add(orderItem);
			}
            if (orderItems.get(0) == null
                    && !orderItems.get(0).getOrder_id().equals(order.getOrder_id())) {
                return JsonResultUtil.getErrorJson("系统参数错误！");
            }
            for (int i = 0; i < orderItems.size(); i++) {
            	if (goods_num[i] > orderItems.get(i).getNum()) {
                    return JsonResultUtil.getErrorJson("退货数据错误，请您确认后再进行此项操作！");
                }	
			}
           
            // 记录会员信息
            SellBack sellBack = new SellBack();
            sellBack.setMember_id(member.getMember_id());
            sellBack.setSndto(member.getName());
            sellBack.setTradeno(com.enation.framework.util.DateUtil.toString(
                    DateUtil.getDateline(), "yyMMddhhmmss"));// 退货单号
            sellBack.setOrderid(order_id);
            sellBack.setRegoperator("会员[" + member.getUname() + "]");
            sellBack.setTradestatus(0);
            sellBack.setRegtime(DateUtil.getDateline());
            sellBack.setRemark(remark);
            sellBack.setType(2);
            sellBack.setRefund_way(refund_way);
            sellBack.setReturn_account(return_account);
            sellBack.setReason(reason);
            sellBack.setShip_status(ship_status.toString());
            sellBack.setApply_alltotal(apply_alltotal);

            /**
             * 循环页面中选中的商品，形成退货明细:goodsList
             */
            List goodsList = new ArrayList();
            for (int i = 0; i < item_id.length; i++) {
            	SellBackGoodsList sellBackGoods = new SellBackGoodsList();
                sellBackGoods.setPrice(orderItems.get(i).getPrice());
                sellBackGoods.setReturn_num(goods_num[i]);
                sellBackGoods.setShip_num(orderItems.get(i).getNum());
                sellBackGoods.setGoods_id(orderItems.get(i).getGoods_id());
                sellBackGoods.setGoods_remark(remark);
                sellBackGoods.setProduct_id(orderItems.get(i).getProduct_id());
                sellBackGoods.setItem_id(item_id[i]);
                goodsList.add(sellBackGoods);	
			}
            this.sellBackManager.addSellBack(sellBack, goodsList);
            return JsonResultUtil.getSuccessJson("申请退货成功，请等待客服人员审核！");
        } catch (RuntimeException e) {
            return JsonResultUtil.getErrorJson("申请退货失败，请您重试！");
        }
    }

    /**
     * 退款申请
     *
     * @param order_id       订单id
     * @param remark         【必填】 描述
     * @param reason         退货原因
     * @param ship_status    是否收到货
     * @param apply_alltotal 退款金额
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/refund", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult refund(int order_id, String remark,
                             String reason, Integer ship_status,
                             double apply_alltotal) {
        try {
            Member member = UserConext.getCurrentMember();
            if (member == null) {
                return JsonResultUtil.getErrorJson("请您在登录后再申请退款！");
            }

            Order order = orderManager.get(order_id);
            if (order == null
                    || !order.getMember_id().equals(member.getMember_id())) {
                return JsonResultUtil.getErrorJson("此订单不存在！");
            }

            // 记录会员信息
            SellBack sellBack = new SellBack();
            sellBack.setMember_id(member.getMember_id());
            sellBack.setSndto(member.getName());
            sellBack.setTradeno(com.enation.framework.util.DateUtil.toString(
                    DateUtil.getDateline(), "yyMMddhhmmss"));// 退货单号
            sellBack.setOrderid(order_id);
            sellBack.setRegoperator("会员[" + member.getUname() + "]");
            sellBack.setTradestatus(0);
            sellBack.setRegtime(DateUtil.getDateline());
            sellBack.setRemark(remark);
            sellBack.setType(1);
            sellBack.setReason(reason);
            sellBack.setShip_status(ship_status.toString());
            sellBack.setApply_alltotal(apply_alltotal);
            sellBack.setRefund_way(order.getPayment_name());
            sellBack.setReturn_account(order.getPayment_account());

            this.sellBackManager.addSellBack(sellBack);
            return JsonResultUtil.getSuccessJson("申请退款成功，请等待客服人员审核！");
        } catch (RuntimeException e) {
            return JsonResultUtil.getErrorJson("申请退款失败，请您重试！");
        }
    }

    /**
     * 退换货列表
     *
     * @param page
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/sell-back-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult SellBack(Integer page) {
        if (page == null || page <= 0) {
            page = 1;
        }
        Member member = UserConext.getCurrentMember();
        if (member == null) {
            return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
        }

        Page webpage = sellBackManager.list(member.getMember_id(), page.intValue(), PAGE_SIZE);
        if (webpage == null) {
            return JsonResultUtil.getErrorJson("获取退换货数据失败，请您重试！");
        }
        List<Map> list = (List<Map>) webpage.getResult();
        for (Map map : list) {
            Integer id = (Integer) map.get("id");
            List<Map> goodsList = sellBackManager.getGoodsList(id);
            for (Map goodsMap : goodsList) {
                goodsMap.put("goods_image", StaticResourcesUtil.convertToUrl(goodsMap.get(
                        "goods_image").toString()));
            }
            map.put("goodsList", goodsList);
        }
        return JsonResultUtil.getObjectJson(list);
    }

    /**
     * 退换货详情
     *
     * @param id
     * @param orderid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/sell-back", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult sellBack(Integer id, Integer orderid) {
        SellBack sellBack = null;
        if (id != null && id.intValue() > 0) {
        	sellBack = sellBackManager.get(id);
        }
        if (orderid != null && orderid.intValue() > 0) {
        	sellBack = apiOrderManager.getSellBack(orderid);
        }
        if (sellBack == null) {
            return JsonResultUtil.getErrorJson("此售后信息不存在！");
        }
        ApiSellBack apiSellBack = new ApiSellBack();
        try {
            BeanUtils.copyProperties(sellBack, sellBack);
        } catch (Exception ex) {
        }

        List<Map> goodsList = sellBackManager.getGoodsList(sellBack.getId());
        for (Map goodsMap : goodsList) {
            goodsMap.put("goods_image", StaticResourcesUtil.convertToUrl(goodsMap.get(
                    "goods_image").toString()));
        }
        apiSellBack.setGoodsList(goodsList);
        return JsonResultUtil.getObjectJson(apiSellBack);
    }
    
    /**
     * 查询订单物流信息
     *
     * @param orderid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delivery", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult delivery(int orderid) {
        Delivery delivery = null;
        List<Delivery> deliveryList = orderReportManager.getDeliveryList(orderid);
        for (Delivery d : deliveryList) {
            if (d.getType().intValue() == 1 && !StringUtils.isEmpty(d.getLogi_no())) {
                delivery = d;
                break;
            }
        }

        if (delivery == null) {
            return JsonResultUtil.getErrorJson("此订单暂时没有物流信息！");
        }
        List expressList = new ArrayList();
        Map expressMap = this.expressManager.getDefPlatform(delivery.getLogi_code(),delivery.getLogi_no());
        if (expressMap != null && expressMap.containsKey("message") && expressMap.get("message").toString().equals("ok")) {
            expressList = (List) expressMap.get("data");
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("delivery", delivery);
        dataMap.put("express", expressList);
        return JsonResultUtil.getObjectJson(dataMap);
    }

    /**
     * 改变店铺的配送方式以及红包<br>
     * 调用此api时必须已经访问过购物车列表<br>
     *
     * @return 含有价格信息的json串
     */
    @ResponseBody
    @RequestMapping(value = "change-ship-bonus")
    public JsonResult changeShipAndBonus(Integer regionid, Integer[] store_ids, Integer[] type_ids, Integer[] bonus_ids) {
        if (store_ids == null || store_ids.length == 0 || type_ids == null || type_ids.length == 0 || bonus_ids == null || bonus_ids.length == 0) {
            return JsonResultUtil.getErrorJson("系统参数错误！");
        }
        if (store_ids.length != type_ids.length || type_ids.length != bonus_ids.length) {
            return JsonResultUtil.getErrorJson("系统参数错误！");
        }
        OrderPrice orderPrice = null;
        for (int i = 0; i < store_ids.length; i++) {
            orderPrice = this.changeArgsType(regionid, store_ids[i], type_ids[i], bonus_ids[i]);
        }
        return JsonResultUtil.getObjectJson(orderPrice);
    }
    @ResponseBody
	@RequestMapping(value="/extract", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult extract(Integer orderid) {
		System.out.println(orderid);
		this.orderFlowManager.extract(orderid);
		
		return null;
	}

    /**
     * 更改配送方式及优惠券
     *
     * @param regionid
     * @param store_id
     * @param type_id
     * @param bonus_id
     * @return
     */
    @SuppressWarnings("rawtypes")
	private OrderPrice changeArgsType(Integer regionid, Integer store_id, Integer type_id, Integer bonus_id) {

    	StoreMember  member=  this.storeMemberManager.getStoreMember();
    	
        // 由购物车列表中获取此店铺的相关信息
        Map storeData = StoreCartContainer.getStoreData(store_id);

        // 获取此店铺的购物列表
        List list = (List) storeData.get(StoreCartKeyEnum.goodslist.toString());

        // 配送地区
        String regionid_str = regionid == null ? "" : regionid + "";

        // 计算此配送方式时的店铺相关价格
        OrderPrice orderPrice = this.cartManager.countPrice(list, type_id, regionid_str);

        // 激发计算子订单价格事件
        orderPrice = storeCartPluginBundle.countChildPrice(orderPrice);

        // 获取购物车中已经选择的商品的订单价格 by_DMRain 2016-6-28
        OrderPrice storePrice = (OrderPrice) storeData.get("storeprice");
        Double act_discount = storePrice.getActDiscount();

        // 如果促销活动优惠的现金不为空 by_DMRain 2016-6-28
        if (act_discount != null && act_discount != 0) {
            orderPrice.setActDiscount(act_discount);
            orderPrice.setNeedPayMoney(orderPrice.getNeedPayMoney() - act_discount);
        }

        Integer activity_id = (Integer) storeData.get("activity_id");

        // 如果促销活动id不为空 by_DMRain 2016-6-28
        if (activity_id != null) {
        	
        	//获取参加促销活动商品价格总计
			Double actTotalPrice = this.getTotalPrice(list);
        	
            ActivityDetail detail = this.activityDetailManager.getDetail(activity_id);
            
            //增加判断：如果参加促销活动商品价格总计大于或者等于促销活动一系列优惠的基础钱数
            if(actTotalPrice >= detail.getFull_money()){
	            // 如果促销活动包含了免运费的优惠内容 by_DMRain 2016-6-28
	            if (detail.getIs_free_ship() == 1) {
	                orderPrice.setIs_free_ship(1);
	                orderPrice.setAct_free_ship(orderPrice.getShippingPrice());
	                orderPrice.setShippingPrice(0d);
	            }
	
	            // 如果促销含有送积分的活动
	            if (detail.getIs_send_point() == 1) {
	                orderPrice.setPoint(detail.getPoint_value());
	                
	                //修复促销活动赠送积分，选中优惠券后没有送积分的bug
	                orderPrice.setActivity_point(detail.getPoint_value());
	            }
	
	            // 如果促销含有送赠品的活动
	            if (detail.getIs_send_gift() == 1) {
	                // 获取赠品的可用库存
	                Integer enable_store = this.storeActivityGiftManager.get(detail.getGift_id()).getEnable_store();
	
	                // 如果赠品的可用库存大于0
	                if (enable_store > 0) {
	                    orderPrice.setGift_id(detail.getGift_id());
	                }
	            }
	
	            // 如果促销含有送优惠券的活动
	            if (detail.getIs_send_bonus() == 1) {
	                // 获取店铺优惠券信息
	            	StoreBonusType bonus = this.b2b2cBonusManager.getBonus(detail.getBonus_id());
	
	                // 优惠券发行量
	                int createNum = bonus.getCreate_num();
	
	                // 获取优惠券已被领取的数量
	                int count = this.b2b2cBonusManager.getCountBonus(detail.getBonus_id());
	
	                // 如果优惠券的发行量大于已经被领取的优惠券数量
	                if (createNum > count) {
	                    orderPrice.setBonus_id(detail.getBonus_id());
	                }
	            }
            }
        }

        // 新增如果选择不使用优惠券，就将已经放进session中的店铺优惠券信息删除 add_by DMRain 2016-7-14
        if (bonus_id != 0) {
            MemberBonus bonuss = this.b2b2cBonusManager.getOneMyBonus(member.getMember_id(), store_id, bonus_id);
            changeBonus(orderPrice, bonuss, store_id);
        } else {
            B2b2cBonusSession.cancelB2b2cBonus(store_id);
        }

        // 重新压入此店铺的订单价格和配送方式id
        storeData.put(StoreCartKeyEnum.storeprice.toString(), orderPrice);
        storeData.put(StoreCartKeyEnum.shiptypeid.toString(), type_id);
        return orderPrice;
    }

    /**
	 * 修改优惠券选项
	 * @author liushuai
	 * @version v1.0,2015年9月22日18:21:33
	 * @param bonuss
	 * @since v1.0
	 */
	private OrderPrice changeBonus(OrderPrice orderprice, MemberBonus bonus, Integer storeid) {

		// set 红包
		B2b2cBonusSession.useBonus(storeid, bonus);

		// 如果优惠券面额大于商品优惠价格的话 那么优惠价格为商品价格
		if (orderprice.getNeedPayMoney() <= bonus.getType_money()) {
			orderprice.setDiscountPrice(orderprice.getNeedPayMoney());
			orderprice.setNeedPayMoney(0.0);
		} else {
			// 计算需要支付的金额		xulipeng 修改为订单价格减优惠券金额  2016年12月28日
			orderprice.setNeedPayMoney(CurrencyUtil.add(orderprice.getOrderPrice(), -bonus.getType_money()));
			orderprice.setDiscountPrice(bonus.getType_money());
		}
		return orderprice;

	}
    
    /**
	 * 获取参加促销活动商品价格总计
	 * add by DMRain 2016-1-12
	 * @param cartItemList
	 * @return
	 */
	private Double getTotalPrice(List<StoreCartItem> cartItemList){
		Double actTotalPrice = 0d;
		Double sameGoodsTotal = 0d;

		for(StoreCartItem cartItem : cartItemList){
			Integer activity_id = cartItem.getActivity_id();

			//如果促销活动信息ID不为空
			if(activity_id != null){
				sameGoodsTotal = CurrencyUtil.mul(cartItem.getPrice(), cartItem.getNum());
				actTotalPrice = CurrencyUtil.add(actTotalPrice, sameGoodsTotal);
			}
		}

		return actTotalPrice;
	}

}
