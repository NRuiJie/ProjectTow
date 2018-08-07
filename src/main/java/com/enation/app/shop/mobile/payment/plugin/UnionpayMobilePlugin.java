package com.enation.app.shop.mobile.payment.plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.enation.app.shop.core.order.model.PayCfg;
import com.enation.app.shop.core.order.model.PayEnable;
import com.enation.app.shop.core.order.plugin.payment.AbstractPaymentPlugin;
import com.enation.app.shop.core.order.plugin.payment.IPaymentEvent;
import com.enation.app.shop.core.order.plugin.payment.PaymentPluginBundle;
import com.enation.app.shop.mobile.payment.unionpay.AcpService;
import com.enation.app.shop.mobile.payment.unionpay.DemoBase;
import com.enation.app.shop.mobile.payment.unionpay.LogUtil;
import com.enation.app.shop.mobile.payment.unionpay.SDKConfig;
import com.enation.app.shop.mobile.payment.unionpay.SDKConstants;
import com.enation.framework.context.webcontext.ThreadContextHolder;

@Component("unionpayMobilePlugin")
public class UnionpayMobilePlugin extends AbstractPaymentPlugin implements
		IPaymentEvent {

	// 支付插件桩
	private PaymentPluginBundle paymentPluginBundle;
	
	public static int is_load=0;

	@Override
	public String onCallBack(String ordertype) {
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String encoding = request.getParameter(SDKConstants.param_encoding);
		// 获取银联通知服务器发送的后台通知参数
		Map<String, String> reqParam = getAllRequestParam(request);

		LogUtil.printRequestLog(reqParam);

		Map<String, String> valideData = null;
		if (null != reqParam && !reqParam.isEmpty()) {
			Iterator<Entry<String, String>> it = reqParam.entrySet().iterator();
			valideData = new HashMap<String, String>(reqParam.size());
			while (it.hasNext()) {
				Entry<String, String> e = it.next();
				String key = (String) e.getKey();
				String value = (String) e.getValue();
				try{
					value = new String(value.getBytes("ISO-8859-1"), encoding);
				}catch(Exception ex){}
				valideData.put(key, value);
			}
		}

		//重要！验证签名前不要修改reqParam中的键值对的内容，否则会验签不过
		if (!AcpService.validate(valideData, encoding)) {
			LogUtil.writeLog("验证签名结果[失败].");
			//验签失败，需解决验签问题
			
		} else {
			LogUtil.writeLog("验证签名结果[成功].");
			//【注：为了安全验签成功才应该写商户的成功处理逻辑】交易成功，更新商户订单状态
			
			String ordersn =valideData.get("orderId"); //获取后台通知的数据，其他字段也可用类似方式获取
			String respCode =valideData.get("respCode"); //获取应答码，收到后台通知了respCode的值一般是00，可以不需要根据这个应答码判断。
			String tradeno = valideData.get("traceNo");
			
			this.paySuccess(ordersn, tradeno, "", ordertype);
			
			return ordersn;
			
		}
		LogUtil.writeLog("BackRcvResponse接收后台通知结束");
		return "";
	}

	@Override
	public String onPay(PayCfg payCfg, PayEnable order) {
		if(is_load==0){
			SDKConfig.getConfig().loadPropertiesFromPath(Thread.currentThread().getContextClassLoader().getResource("").getPath()+"com/enation/app/shop/mobile/payment/plugin/");
			is_load=1;
		}
		Map<String,String> params = paymentManager.getConfigParams(this.getId());
		String merId = params.get("merId");
		
		Map<String, String> contentData = new HashMap<String, String>();

		/*** 银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改 ***/
		contentData.put("version", DemoBase.version); // 版本号 全渠道默认值
		contentData.put("encoding", DemoBase.encoding_UTF8); // 字符集编码
																// 可以使用UTF-8,GBK两种方式
		contentData.put("signMethod", "01"); // 签名方法 目前只支持01：RSA方式证书加密
		contentData.put("txnType", "01"); // 交易类型 01:消费
		contentData.put("txnSubType", "01"); // 交易子类 01：消费
		contentData.put("bizType", "000201"); // 填写000201
		contentData.put("channelType", "08"); // 渠道类型 08手机

		/*** 商户接入参数 ***/
		contentData.put("merId", merId); // 商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
		contentData.put("accessType", "0"); // 接入类型，商户接入填0 ，不需修改（0：直连商户， 1： 收单机构
											// 2：平台商户）
		contentData.put("orderId", order.getSn()); // 商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
		contentData.put("txnTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())); // 订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
		contentData.put("accType", "01"); // 账号类型 01：银行卡02：存折03：IC卡帐号类型(卡介质)
		contentData.put("txnAmt", "" + (int)(order.getNeedPayMoney() * 100)); // 交易金额 单位为分，不能带小数点
		contentData.put("currencyCode", "156"); // 境内商户固定 156 人民币
		// contentData.put("reqReserved", "透传字段"); //商户自定义保留域，交易应答时会原样返回

		// 后台通知地址（需设置为外网能访问 http
		// https均可），支付成功后银联会自动将异步通知报文post到商户上送的该地址，【支付失败的交易银联不会发送后台通知】
		// 后台通知参数详见open.unionpay.com帮助中心 下载 产品接口规范 网关支付产品接口规范 消费交易 商户通知
		// 注意:1.需设置为外网能访问，否则收不到通知 2.http https均可 3.收单后台通知后需要10秒内返回http200或302状态码
		// 4.如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200或302，那么银联会间隔一段时间再次发送。总共发送5次，银联后续间隔1、2、4、5
		// 分钟后会再次通知。
		// 5.后台通知地址如果上送了带有？的参数，例如：http://abc/web?a=b&c=d
		// 在后台通知处理程序验证签名之前需要编写逻辑将这些字段去掉再验签，否则将会验签失败
		contentData.put("backUrl", this.getCallBackUrl(payCfg, order));
//		contentData.put("backUrl", "http://106.120.61.193:18080/api/shop/s_unionpayMobilePlugin_payment-callback.do");

		/** 对请求参数进行签名并发送http post请求，接收同步应答报文 **/
		Map<String, String> reqData = AcpService.sign(contentData,
				DemoBase.encoding_UTF8); // 报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
		String requestAppUrl = SDKConfig.getConfig().getAppRequestUrl(); // 交易请求url从配置文件读取对应属性文件acp_sdk.properties中的
																			// acpsdk.backTransUrl
		Map<String, String> rspData = AcpService.post(reqData, requestAppUrl,
				DemoBase.encoding_UTF8); // 发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过

		/** 对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考-------------> **/
		// 应答码规范参考open.unionpay.com帮助中心 下载 产品接口规范 《平台接入接口规范-第5部分-附录》
		if (!rspData.isEmpty()) {
			if (AcpService.validate(rspData, DemoBase.encoding_UTF8)) {
				LogUtil.writeLog("验证签名成功");
				String respCode = rspData.get("respCode");
				if (("00").equals(respCode)) {
					// 成功,获取tn号
					String tn = rspData.get("tn");
					System.out.println("tn:" + tn);
					return tn;
				} else {
					System.out.println("respCode 不是 00");
				}
			} else {
				LogUtil.writeErrorLog("验证签名失败");
				System.out.println("验证签名失败");
			}
		} else {
			// 未返回正确的http状态
			LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
			System.out.println("未获取到返回报文或返回http状态码非200");
		}
		return "";
	}
	
	/**
	 * 获取请求参数中所有的信息
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, String> getAllRequestParam(final HttpServletRequest request) {
		Map<String, String> res = new HashMap<String, String>();
		Enumeration<?> temp = request.getParameterNames();
		if (null != temp) {
			while (temp.hasMoreElements()) {
				String en = (String) temp.nextElement();
				String value = request.getParameter(en);
				res.put(en, value);
				//在报文上送时，如果字段的值为空，则不上送<下面的处理为在获取所有参数数据时，判断若值为空，则删除这个字段>
				//System.out.println("ServletUtil类247行  temp数据的键=="+en+"     值==="+value);
				if (null == res.get(en) || "".equals(res.get(en))) {
					res.remove(en);
				}
			}
		}
		return res;
	}

	@Override
	public String onReturn(String arg0) {
		return null;
	}

	@Override
	public String getId() {
		return "unionpayMobilePlugin";
	}

	@Override
	public String getName() {
		return "银联移动支付接口";
	}

	public PaymentPluginBundle getPaymentPluginBundle() {
		return paymentPluginBundle;
	}

	public void setPaymentPluginBundle(PaymentPluginBundle paymentPluginBundle) {
		this.paymentPluginBundle = paymentPluginBundle;
	}
}
