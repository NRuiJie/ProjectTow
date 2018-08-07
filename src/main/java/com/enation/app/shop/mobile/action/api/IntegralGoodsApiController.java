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

import com.enation.app.shop.core.goods.model.Goods;
import com.enation.app.shop.core.goods.service.IGoodsManager;
import com.enation.app.shop.mobile.model.ApiCategory;
import com.enation.app.shop.mobile.service.ApiIntegralGoodsManager;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.database.Page;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

/**
 * 积分商品移动端API接口类
 * @author duanmingyu
 * @date 2018-5-6
 *
 */
@Controller("integralGoodsApiController")
@RequestMapping("/api/mobile/integral")
public class IntegralGoodsApiController {

	@Autowired
	private IGoodsManager goodsManager;
	
	@Autowired
	private ApiIntegralGoodsManager apiIntegralGoodsManager;
	
	/**
	 * 获取积分商品分类信息和商品信息
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/list-all-cat", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult listAllCat(Integer num) {
		try {
			if (num == null) {
				num = 4;
			}
			
			List topList = new ArrayList();
			List<ApiCategory> resultList = this.apiIntegralGoodsManager.listSecondCat();
			
			for (ApiCategory apiCategory : resultList) {
				Map newCat = new HashMap();
				newCat.put("cat_id", apiCategory.getCat_id());
				newCat.put("cat_name", apiCategory.getCat_name());
				List<Goods> goodsList = this.apiIntegralGoodsManager.listGoodsByLimit(apiCategory.getCat_id(), num);
				for (Goods goods : goodsList) {
					
					if (!StringUtil.isEmpty(goods.getThumbnail())) {
						goods.setThumbnail(StaticResourcesUtil.convertToUrl(goods.getThumbnail()));
	    			}
					
					if (!StringUtil.isEmpty(goods.getBig())) {
						goods.setBig(StaticResourcesUtil.convertToUrl(goods.getBig()));
					}
					
					if (!StringUtil.isEmpty(goods.getSmall())) {
						goods.setSmall(StaticResourcesUtil.convertToUrl(goods.getSmall()));
					}
					
					if (!StringUtil.isEmpty(goods.getOriginal())) {
						goods.setOriginal(StaticResourcesUtil.convertToUrl(goods.getOriginal()));
					}
					
				}
				newCat.put("goods_list", goodsList);
				topList.add(newCat);
			}
			return JsonResultUtil.getObjectJson(topList);
		} catch (Exception e) {
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("获取积分商品分类数据失败");
		}
	}
	
	/**
	 * 获取乐币商品信息
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/list-integral-goods", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult listIntegralGoods(Integer pageNo, Integer pageSize) {
		try {
			if (pageNo == null || pageNo == 0) {
				pageNo = 1;
			}
			
			if (pageSize == null || pageSize == 0) {
				pageSize = 20;
			}
			
			Page webpage = this.apiIntegralGoodsManager.listIntegerGoods(pageNo, pageSize);
			List<Goods> goodsList = (List<Goods>) webpage.getResult();
			for (Goods goods : goodsList) {
				if (!StringUtil.isEmpty(goods.getThumbnail())) {
					goods.setThumbnail(StaticResourcesUtil.convertToUrl(goods.getThumbnail()));
    			}
				
				if (!StringUtil.isEmpty(goods.getBig())) {
					goods.setBig(StaticResourcesUtil.convertToUrl(goods.getBig()));
				}
				
				if (!StringUtil.isEmpty(goods.getSmall())) {
					goods.setSmall(StaticResourcesUtil.convertToUrl(goods.getSmall()));
				}
				
				if (!StringUtil.isEmpty(goods.getOriginal())) {
					goods.setOriginal(StaticResourcesUtil.convertToUrl(goods.getOriginal()));
				}
			}
			return JsonResultUtil.getObjectJson(goodsList);
		} catch (Exception e) {
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("获取乐币商品数据失败");
		}
	}	
	
	/**
	 * 获取积分商品二级分类信息
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/list-goods-cat", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult listGoodsCat() {
		try {
			List<ApiCategory> resultList = this.apiIntegralGoodsManager.listSecondCat();
			return JsonResultUtil.getObjectJson(resultList);
		} catch (Exception e) {
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("获取积分商品分类数据失败");
		}
	}
	
	/**
	 * 获取指定数量商品和分类信息
	 * @param cat_id 积分商品分类id
	 * @param num 指定的数量
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/list-limit-goods", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult listLimitGoods(Integer cat_id, Integer num) {
		try {
			if (cat_id == null || cat_id == 0) {
				return JsonResultUtil.getErrorJson("获取积分商品数据失败：参数不正确！");
			}
			
			if (num == null) {
				num = 4;
			}
			
			List<Goods> resultList = this.apiIntegralGoodsManager.listGoodsByLimit(cat_id, num);
			for (Goods goods : resultList) {
				
				if (!StringUtil.isEmpty(goods.getThumbnail())) {
					goods.setThumbnail(StaticResourcesUtil.convertToUrl(goods.getThumbnail()));
    			}
				
				if (!StringUtil.isEmpty(goods.getBig())) {
					goods.setBig(StaticResourcesUtil.convertToUrl(goods.getBig()));
				}
				
				if (!StringUtil.isEmpty(goods.getSmall())) {
					goods.setSmall(StaticResourcesUtil.convertToUrl(goods.getSmall()));
				}
				
				if (!StringUtil.isEmpty(goods.getOriginal())) {
					goods.setOriginal(StaticResourcesUtil.convertToUrl(goods.getOriginal()));
				}
				
			}
			return JsonResultUtil.getObjectJson(resultList);
		} catch (Exception e) {
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("获取积分商品数据失败");
		}
	}
	
	/**
	 * 获取指定分类下的商品数据
	 * @param cat_id 分类id
	 * @param pageNo 页数
	 * @param pageSize 每页记录数
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/list-goods-by-cat", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult listGoodsByCat(Integer cat_id, Integer pageNo, Integer pageSize) {
		try {
			if (cat_id == null || cat_id == 0) {
				return JsonResultUtil.getErrorJson("获取积分商品数据失败：参数不正确！");
			}
			
			if (pageNo == null || pageNo == 0) {
				pageNo = 1;
			}
			
			if (pageSize == null || pageSize == 0) {
				pageSize = 20;
			}
			
			Page webpage = this.apiIntegralGoodsManager.listPageByCat(cat_id, pageNo, pageSize);
			List<Goods> goodsList = (List<Goods>) webpage.getResult();
			for (Goods goods : goodsList) {
				if (!StringUtil.isEmpty(goods.getThumbnail())) {
					goods.setThumbnail(StaticResourcesUtil.convertToUrl(goods.getThumbnail()));
    			}
				
				if (!StringUtil.isEmpty(goods.getBig())) {
					goods.setBig(StaticResourcesUtil.convertToUrl(goods.getBig()));
				}
				
				if (!StringUtil.isEmpty(goods.getSmall())) {
					goods.setSmall(StaticResourcesUtil.convertToUrl(goods.getSmall()));
				}
				
				if (!StringUtil.isEmpty(goods.getOriginal())) {
					goods.setOriginal(StaticResourcesUtil.convertToUrl(goods.getOriginal()));
				}
			}
			return JsonResultUtil.getObjectJson(goodsList);
		} catch (Exception e) {
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("获取积分商品数据失败");
		}
	}
	
	/**
	 * 新的支付状态，在支付宝完成收款之后，直接修改状态
	 * 
	 * @param orderId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/paynew",produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult pay(Integer orderId, Double payMoney) {
		try {			
			if (orderId != null) {
				this.apiIntegralGoodsManager.payNew(orderId, payMoney);
				return JsonResultUtil.getSuccessJson("状态：已经付款");
			} else {
				return JsonResultUtil.getErrorJson("状态：订单ID没有获取到");
			}
		} catch (RuntimeException e) {
			return JsonResultUtil.getErrorJson("状态：已确认");
		}
	}
}
