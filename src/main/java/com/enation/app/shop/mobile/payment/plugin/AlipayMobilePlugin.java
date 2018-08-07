package com.enation.app.shop.mobile.payment.plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.service.IOrderManager;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.enation.app.shop.mobile.payment.alipay.config.AlipayConfig;
import com.enation.app.shop.mobile.payment.alipay.util.AlipayNotify;
import com.enation.app.shop.core.order.model.PayCfg;
import com.enation.app.shop.core.order.model.PayEnable;
import com.enation.app.shop.core.order.plugin.payment.AbstractPaymentPlugin;
import com.enation.app.shop.core.order.plugin.payment.IPaymentEvent;
import com.enation.app.shop.core.order.plugin.payment.PaymentPluginBundle;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.StringUtil;

@Component("alipayMobilePlugin")
public class AlipayMobilePlugin extends AbstractPaymentPlugin implements IPaymentEvent{
	
	//支付插件桩
	private PaymentPluginBundle paymentPluginBundle;

	private IOrderManager orderManager;
		
	@Override
	public String onCallBack(String ordertype) {
		Map<String,String> cfgparams = paymentManager.getConfigParams(this.getId());
		
		AlipayConfig.partner = cfgparams.get("partner");
		AlipayConfig.ali_public_key = cfgparams.get("rsa_public");
		AlipayConfig.private_key = cfgparams.get("rsa_private");
		
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		 
		//获取支付宝POST过来反馈信息
		Map<String,String> params = new HashMap<String,String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
			//valueStr = StringUtil.toUTF8(valueStr);
			params.put(name, valueStr);
		}
		
		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
		//商户订单号
		 String out_trade_no = StringUtil.toUTF8(request.getParameter("out_trade_no"));
		//支付宝交易号

		String trade_no =StringUtil.toUTF8(request.getParameter("trade_no"));

		//交易状态
		String trade_status = StringUtil.toUTF8(request.getParameter("trade_status"));

		//买家支付宝账号
		String buyer_email = StringUtil.toUTF8(request.getParameter("buyer_email"));

		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//

		//获取总金额
		double total_amount = NumberUtils.toDouble(request.getParameter("total_fee"), 0.0d);

		if(AlipayNotify.verify(params)){
			if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
				Order order = orderManager.get(out_trade_no);
				if(order != null && order.getNeed_pay_money() <= total_amount) {
					this.paySuccess(out_trade_no, trade_no, buyer_email, ordertype);
					return ("success");
				}
			}
			return ("fail");
		}else{
			return ("fail");
		}
	}
	
	@Override
	public String onPay(PayCfg arg0, PayEnable arg1) {
		return null;
	}
	
	@Override
	public String onReturn(String arg0) {
		return "";
	}
	
	@Override
	public String getId() {
		return "alipayMobilePlugin";
	}
	
	@Override
	public String getName() {
		return "支付宝移动支付接口";
	}

	public PaymentPluginBundle getPaymentPluginBundle() {
		return paymentPluginBundle;
	}

	public void setPaymentPluginBundle(PaymentPluginBundle paymentPluginBundle) {
		this.paymentPluginBundle = paymentPluginBundle;
	}

	public IOrderManager getOrderManager() {
		return orderManager;
	}

	public void setOrderManager(IOrderManager orderManager) {
		this.orderManager = orderManager;
	}
}
