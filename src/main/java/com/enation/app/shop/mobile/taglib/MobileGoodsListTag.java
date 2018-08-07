package com.enation.app.shop.mobile.taglib;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.enation.app.shop.core.goods.model.Goods;
import com.enation.app.shop.mobile.service.ApiGoodsManager;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;

@Component
@Scope("prototype")
public class MobileGoodsListTag extends BaseFreeMarkerTag {

	@Autowired
	private ApiGoodsManager apiGoodsManager;
	
	/**
	 * 根据参数获取商品列表
	 * @param catid:分类id,可选项，如果为空则查询所有分类下的商品
	 * @param tagid:标签id，可选项
	 * @param goodsnum:要读取的商品数量，必填项。
	 * @return 商品列表
	 * {@link Goods}
	 */
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		int catid = NumberUtils.toInt((String)params.get("catid"), 0);	
		int goodsnum = NumberUtils.toInt((String)params.get("goodsnum"), 1);
		List goodsList  = apiGoodsManager.listByCat(catid, goodsnum);		 
		return goodsList;
	}	
	
}
