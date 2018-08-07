package com.enation.app.shop.mobile.payment.plugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.model.PayCfg;
import com.enation.app.shop.core.order.model.PayEnable;
import com.enation.app.shop.core.order.plugin.payment.AbstractPaymentPlugin;
import com.enation.app.shop.core.order.plugin.payment.IPaymentEvent;
import com.enation.app.shop.core.order.plugin.payment.PaymentPluginBundle;
import com.enation.app.shop.core.order.service.IOrderManager;
import com.enation.app.shop.mobile.payment.WeixinUtil;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.CurrencyUtil;

@Component("wechatMobilePlugin")
public class WechatMobilePlugin extends AbstractPaymentPlugin implements IPaymentEvent {

    //支付插件桩
    private PaymentPluginBundle paymentPluginBundle;

    private IOrderManager orderManager;

    @Override
    public String onCallBack(String ordertype) {
        Map<String, String> cfgparams = paymentManager.getConfigParams(this
                .getId());
        String key = cfgparams.get("key");

        HttpServletRequest request = ThreadContextHolder.getHttpRequest();
        Map map = new HashMap();

        try {
            SAXReader saxReadr = new SAXReader();
            Document document = saxReadr.read(request.getInputStream());

            /** 调试时可以打开下面注释 ，以观察通知的xml内容 **/
            // String docstr = WeixinUtil.doc2String(document);
            // this.logger.debug("--------post xml-------");
            // this.logger.debug(docstr);
            // this.logger.debug("--------end-------");

            Map<String, String> params = WeixinUtil.xmlToMap(document);

            String return_code = params.get("return_code");
            String result_code = params.get("result_code");
            if ("SUCCESS".equals(return_code) && "SUCCESS".equals(result_code)) {
                String ordersn = params.get("out_trade_no");
                String sign = WeixinUtil.createSign(params, key);
                if (sign.equals(params.get("sign"))) {

                    int total_amount = NumberUtils.toInt(params.get("total_fee"), 0);

                    Order order = orderManager.get(ordersn);
                    if (order != null && toFen(order.getNeed_pay_money()) <= total_amount) {
                        this.paySuccess(ordersn, "", "", ordertype);
                        map.put("return_code", "SUCCESS");
                        this.logger.debug("签名校验成功");
                    } else {
                        map.put("return_code", "FAIL");
                        map.put("return_msg", "签名失败");
                    }
                } else {
                    this.logger.debug("-----------签名校验失败---------");
                    this.logger.debug("weixin sign:" + params.get("sign"));
                    this.logger.debug("my sign:" + sign);
                    map.put("return_code", "FAIL");
                    map.put("return_msg", "签名失败");
                }
            } else {
                map.put("return_code", "FAIL");
                this.logger.debug("微信通知的结果为失败");
            }

        } catch (IOException e) {
            map.put("return_code", "FAIL");
            map.put("return_msg", "");
            e.printStackTrace();
        } catch (DocumentException e) {
            map.put("return_code", "FAIL");
            map.put("return_msg", "");
            e.printStackTrace();
        }
        HttpServletResponse response = ThreadContextHolder.getHttpResponse();
        response.setHeader("Content-Type", "text/xml");
        return WeixinUtil.mapToXml(map);
    }

    @Override
    public String onPay(PayCfg arg0, PayEnable arg1) {
        return "";
    }

    @Override
    public String onReturn(String arg0) {
        return null;
    }

    @Override
    public String getId() {
        return "wechatMobilePlugin";
    }

    @Override
    public String getName() {
        return "微信移动支付接口";
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
    
    private int toFen(Double money) {
        money = CurrencyUtil.mul(money, 100);
        String str = String.valueOf(money);
        str = str.substring(0, str.indexOf("."));
        return NumberUtils.toInt(str,0);
    }
}
