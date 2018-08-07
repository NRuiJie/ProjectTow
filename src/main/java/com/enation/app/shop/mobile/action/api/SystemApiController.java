package com.enation.app.shop.mobile.action.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.cms.core.service.IDataManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.JsonResultUtil;

@Controller("mobileSystemApiController")
@RequestMapping("/api/mobile/system")
public class SystemApiController {
	
	@Autowired
	private IDataManager dataManager;
	
	/**
	 * 获取热门搜索词
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/hot-keyword", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult detail() {
		List<String> keywordList = new ArrayList<String>();
		List<Map> dataList = dataManager.list(1);
		if(dataList != null){
			for(Map map : dataList){
				keywordList.add(map.get("hot_searchword").toString());
			}
		}
		return JsonResultUtil.getObjectJson(keywordList);		
	}
	
}
