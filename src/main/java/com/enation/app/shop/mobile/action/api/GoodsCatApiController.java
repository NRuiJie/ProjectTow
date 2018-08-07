package com.enation.app.shop.mobile.action.api;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.shop.core.goods.model.Cat;
import com.enation.app.shop.core.goods.service.IGoodsCatManager;
import com.enation.app.shop.mobile.model.ApiCat;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonResultUtil;

/**
 * Created by Dawei on 4/1/15.
 */
@Controller("mobileGoodsCatApiController")
@RequestMapping("/api/mobile/goodscat")
public class GoodsCatApiController {

	@Autowired
	private IGoodsCatManager goodsCatManager;

	/**
	 * 获取分类列表
	 * 
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult list() throws Exception {
		
		HttpServletRequest request =  ThreadContextHolder.getHttpRequest();
		String cat_id = request.getParameter("cat_id");
		
		int catId = 0;
		if(cat_id!=null){
			catId = Integer.parseInt(cat_id);
		}
		
		List<Cat> cat_tree = goodsCatManager.listAllChildren(catId);

		List<ApiCat> apiCatTree = new ArrayList<ApiCat>();
		for (Cat cat : cat_tree) {
			apiCatTree.add(toApiCat(cat));
		}
		return JsonResultUtil.getObjectJson(apiCatTree);
	}

	/**
	 * 将Cat对象转换为ApiCat对象
	 * @param cat
	 * @return
     */
	private ApiCat toApiCat(Cat cat) {
		ApiCat apiCat = new ApiCat();
		apiCat.setName(cat.getName());
		apiCat.setCat_id(cat.getCat_id());
		apiCat.setImage(cat.getImage());
		apiCat.setLevel(cat.getCat_path().split("\\|").length - 1);
		apiCat.setParent_id(cat.getParent_id());
		if (cat.getHasChildren()) {
			for (Cat subcat : cat.getChildren()) {
				apiCat.getChildren().add(toApiCat(subcat));
			}
		}
		return apiCat;
	}
}
