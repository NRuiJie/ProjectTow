package com.enation.app.shop.mobile.action.api;

import com.enation.app.shop.core.order.model.DlyType;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.model.PayCfg;
import com.enation.app.shop.core.order.plugin.payment.IPaymentEvent;
import com.enation.app.shop.core.order.service.ICartManager;
import com.enation.app.shop.core.order.service.IDlyTypeManager;
import com.enation.app.shop.core.order.service.IOrderManager;
import com.enation.app.shop.core.order.service.IPaymentManager;
import com.enation.app.shop.mobile.utils.DesUtils;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.spring.SpringContextHolder;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonResultUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("mobilePaymentApiController")
@RequestMapping("/api/mobile/payment")
public class PaymentApiController {

	@Autowired
	private IPaymentManager paymentManager;

	@Autowired
	private IOrderManager orderManager;
	
	@Autowired
	private IOrderManager selfStoreOrderManager;

	@Autowired
	private IDlyTypeManager dlyTypeManager;

	@Autowired
	private ICartManager cartManager;

	/**
	 * 获取支付及配送方式
	 *
	 * @param regionid
	 *            配送区域id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/payment-shipping", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult paymentShipping(Integer regionid) {
		if (regionid == null) {
			regionid = 0;
		}
		// 支付方式
		List dbPaymentList = this.paymentManager.list();
		List paymentList = new ArrayList();
		for (int i = 0; i < dbPaymentList.size(); i++) {
			PayCfg payment = (PayCfg) dbPaymentList.get(i);
			if (payment.getType().equals("cod")
					|| payment.getType().endsWith("MobilePlugin")) {
				try {
					payment.setConfig(DesUtils.encode(payment.getConfig()));
				} catch (Exception ex) {
					payment.setConfig("");
				}
				paymentList.add(payment);
			}
		}

		// 配送方式
		String sessionId = ThreadContextHolder.getHttpRequest().getSession()
				.getId();

		Double orderPrice = cartManager.countGoodsTotal(sessionId);
		Double weight = cartManager.countGoodsWeight(sessionId);
		List<DlyType> dlyTypeList = this.dlyTypeManager.list(weight,
				orderPrice, regionid.toString());

		Map data = new HashMap();
		data.put("payment", paymentList);
		data.put("shipping", dlyTypeList);
		return JsonResultUtil.getObjectJson(data);
	}

	/**
	 * 获取第三方发起支付的参数
	 *
	 * @param orderid
	 *            订单Id
	 * @param paymentid
	 *            支付方式id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/pay")
	public JsonResult execute(
			Integer orderid,
			@RequestParam(value = "paymentid", required = false) Integer paymentid) {
		//http://localhost:8080/fenxiao-b2b2c-tpl/api/mobile/member/login.do?username=13521284349&password=a12345678
		// 订单id参数
		if (orderid == null || orderid <= 0) {
			return JsonResultUtil.getErrorJson("订单ID错误！");
		}

		// 支付方式id参数
		Order order = this.selfStoreOrderManager.get(orderid);
		if (order == null) {
			return JsonResultUtil.getErrorJson("该订单不存在！");
		}

		// 如果没有传递支付方式id，则使用订单中的支付方式
		if (paymentid == null) {
			paymentid = order.getPayment_id();
		}

		PayCfg payCfg = this.paymentManager.get(paymentid);
		if (payCfg == null) {
			return JsonResultUtil.getErrorJson("不支持您选择的支付方式！");
		}
		if (!payCfg.getType().contains("MobilePlugin")) {
			return JsonResultUtil.getErrorJson("不支持您选择的支付方式！");
		}

		IPaymentEvent paymentPlugin = SpringContextHolder.getBean(payCfg.getType());
		String payhtml = paymentPlugin.onPay(payCfg, order);

		// 用户更换了支付方式，更新订单的数据
		if (order.getPayment_id().intValue() != paymentid.intValue()) {
			this.orderManager.updatePayMethod(orderid, paymentid,payCfg.getType(), payCfg.getName());
		}
		return JsonResultUtil.getObjectJson(payhtml);
	}

}
