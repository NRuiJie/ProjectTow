/**
 *
 */
package com.enation.app.shop.mobile.action.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.b2b2c.core.version.model.AppVersion;
import com.enation.app.b2b2c.core.version.service.IAppVersionManager;
import com.enation.app.base.core.model.Adv;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.JsonResultUtil;

@Controller("mobileAppVersionApiController")
@RequestMapping("/api/mobile/app/version")
public class AppVersionApiController {

    @Autowired
    private IAppVersionManager appVersionManager;

    /**
     * ios
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/ios", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult ios(Long advid) {
        try {
        	AppVersion appVersion = appVersionManager.getAppVersion(1);
            return JsonResultUtil.getObjectJson(appVersion);
        } catch (Exception e) {
            return JsonResultUtil.getErrorJson("获取版本失败！");
        }
    }
    /**
     * 安卓
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/android", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult android(Long advid) {
    	try {
    		AppVersion appVersion = appVersionManager.getAppVersion(2);
            return JsonResultUtil.getObjectJson(appVersion);
    	} catch (Exception e) {
    		return JsonResultUtil.getErrorJson("获取版本失败！");
    	}
    }

}
