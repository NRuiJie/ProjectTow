/**
 *
 */
package com.enation.app.shop.mobile.action.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.base.core.model.Adv;
import com.enation.app.base.core.service.IAdvManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.JsonResultUtil;

@Controller("mobileAdvApiController")
@RequestMapping("/api/mobile/adv")
public class AdvApiController {

    @Autowired
    private IAdvManager advManager;

    /**
     * 根据某个广告id获取广告信息
     *
     * @param advid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult detail(Long advid) {
        try {
            Adv adv = advManager.getAdvDetail(advid);
            return JsonResultUtil.getObjectJson(adv);
        } catch (Exception e) {
            return JsonResultUtil.getErrorJson("获取广告失败，请您重试！");
        }
    }
    
    
    /**
     * 根据某个广告分类id获取广告列表
     *
     * @param acid
     * @return
     * by: laiyunchuan
     * 2016-11-09 12:55:39
     */
    @ResponseBody
    @RequestMapping(value = "/adv-cat", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult advcat(Long acid) {
        try {
            java.util.List<?> advcat = advManager.listAdv(acid);
            return JsonResultUtil.getObjectJson(advcat);
        } catch (Exception e) {
            return JsonResultUtil.getErrorJson("获取广告失败，请您重试！");
        }
    }

}
