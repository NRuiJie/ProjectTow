package com.enation.app.shop.mobile.action.api;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.b2b2c.core.store.service.activity.IStoreActivityManager;
import com.enation.app.base.core.model.Member;
import com.enation.app.shop.component.bonus.model.BonusType;
import com.enation.app.shop.component.bonus.service.IBonusTypeManager;
import com.enation.app.shop.component.gallery.model.GoodsGallery;
import com.enation.app.shop.component.gallery.service.IGoodsGalleryManager;
import com.enation.app.shop.core.goods.model.Attribute;
import com.enation.app.shop.core.goods.model.Product;
import com.enation.app.shop.core.goods.model.Specification;
import com.enation.app.shop.core.goods.plugin.search.SearchSelector;
import com.enation.app.shop.core.goods.service.IGoodsManager;
import com.enation.app.shop.core.goods.service.IGoodsSearchManager;
import com.enation.app.shop.core.goods.service.IGoodsTypeManager;
import com.enation.app.shop.core.goods.service.IProductManager;
import com.enation.app.shop.core.goods.service.SearchEngineFactory;
import com.enation.app.shop.core.other.model.Activity;
import com.enation.app.shop.core.other.model.ActivityGift;
import com.enation.app.shop.core.other.service.IActivityGiftManager;
import com.enation.app.shop.core.other.service.IActivityManager;
import com.enation.app.shop.mobile.model.ApiFilter;
import com.enation.app.shop.mobile.model.ApiFilterValue;
import com.enation.app.shop.mobile.model.ApiGoods;
import com.enation.app.shop.mobile.service.ApiCommentManager;
import com.enation.app.shop.mobile.service.ApiFavoriteManager;
import com.enation.app.shop.mobile.service.ApiGoodsManager;
import com.enation.app.shop.mobile.utils.QrCodeUtils;
import com.enation.app.shop.mobile.utils.UrlUtils;
import com.enation.eop.SystemSetting;
import com.enation.eop.processor.core.UrlNotFoundException;
import com.enation.eop.sdk.context.UserConext;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.database.Page;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;
import com.google.zxing.BarcodeFormat;

/**
 * Created by Dawei on 4/7/15.
 */
@Controller("mobileGoodsApiController")
@RequestMapping("/api/mobile/goods")
public class GoodsApiController {

	@Autowired
	private IGoodsManager goodsManager;

	@Autowired
	private ApiCommentManager apiCommentManager;

	@Autowired
	private IGoodsGalleryManager goodsGalleryManager;

	@Autowired
	private IProductManager productManager;

	@Autowired
	private ApiFavoriteManager apiFavoriteManager;

	@Autowired
	private IGoodsSearchManager goodsSearchManager;

	@Autowired
	private ApiGoodsManager apiGoodsManager;

	@Autowired
	private IStoreActivityManager storeActivityManager;

	@Autowired
	private IActivityManager activityManager;

	@Autowired
	private IActivityGiftManager activityGiftManager;

	@Autowired
	private IBonusTypeManager bonusTypeManager;

	@Autowired
	private IGoodsTypeManager goodsTypeManager;
	private final int PAGE_SIZE = 20;

	/**
	 * 获取商品列表
	 *
	 * @param page
	 *            第几页
	 * @param cat
	 *            分类ID
	 * @param sort
	 *            排序
	 * @param brand
	 *            品牌ID
	 * @param seckill
	 *            是否只取秒杀商品
	 * @param keyword
	 *            搜索的关键字
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult list(Integer page, Integer cat, String sort, Integer brand, Integer seckill, String keyword) {
		if (page == null || page <= 0) {
			page = 1;
		}

		Page webpage = null;
		if (seckill == null || seckill == 0) {
			webpage = goodsSearchManager.search(page, PAGE_SIZE);
		} else {
			webpage = apiGoodsManager.getSeckillGoodsList(page, PAGE_SIZE);
		}

		List<ApiGoods> goodsList = new ArrayList<ApiGoods>();
		List list = (List) webpage.getResult();
		for (int i = 0; i < list.size(); i++) {
			Map map = (Map) list.get(i);
			Integer isIntegral = StringUtil.toInt(map.get("is_integral_goods").toString(), 0);
			if (isIntegral.intValue() == 0) {
				if (map.containsKey("thumbnail") && map.get("thumbnail") != null) {
					map.put("thumbnail", StaticResourcesUtil.convertToUrl(map.get("thumbnail").toString()));
				} else {
					map.put("thumbnail", "");
				}
				ApiGoods goods = new ApiGoods();
				try {
					BeanUtils.populate(goods, map);
				} catch (Exception ex) {
					return JsonResultUtil.getErrorJson("系统内部错误，请您重试！");
				}
				goodsList.add(goods);
			}
		}
		return JsonResultUtil.getObjectJson(goodsList);
	}

	/**
	 * 获取商品列表筛选器
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/filter", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult filter() {
		IGoodsSearchManager goodsSearchManager = SearchEngineFactory.getSearchEngine();
		Map map = goodsSearchManager.getSelector();

		// 品牌&价格
		List<ApiFilter> filterList = new ArrayList<ApiFilter>();
		String[] types = new String[] { "brand", "price" };
		String[] names = new String[] { "品牌", "价格" };
		for (int i = 0; i < types.length; i++) {
			if (map.containsKey(types[i])) {
				ApiFilter apiFilter = new ApiFilter();
				apiFilter.setType(types[i]);
				apiFilter.setName(names[i]);
				List<ApiFilterValue> filterValueList = new ArrayList<ApiFilterValue>();
				ArrayList valueList = (ArrayList) map.get(types[i]);
				for (Object obj : valueList) {
					SearchSelector selector = (SearchSelector) obj;
					filterValueList.add(
							new ApiFilterValue(selector.getName(), UrlUtils.getParameter(selector.getUrl(), types[i])));
				}
				apiFilter.setValueList(filterValueList);
				filterList.add(apiFilter);
			}
		}

		// 属性
		if (map.containsKey("prop")) {
			Map propMap = (Map) map.get("prop");
			for (Object key : propMap.keySet()) {
				ApiFilter apiFilter = new ApiFilter();
				apiFilter.setType("prop");
				apiFilter.setName(key.toString());

				List<ApiFilterValue> filterValueList = new ArrayList<ApiFilterValue>();
				ArrayList valueList = (ArrayList) propMap.get(key);
				for (Object obj : valueList) {
					SearchSelector selector = (SearchSelector) obj;
					filterValueList.add(
							new ApiFilterValue(selector.getName(), UrlUtils.getParameter(selector.getUrl(), "prop")));
				}
				apiFilter.setValueList(filterValueList);
				filterList.add(apiFilter);
			}
		}

		return JsonResultUtil.getObjectJson(filterList);
	}

	/**
	 * 获取商品详情
	 *
	 * @param goodsid
	 * @param act_id
	 * @param groupbuy_id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult detail(Integer goodsid, Integer act_id, Integer groupbuy_id) {

		Map<String, Object> productMap;
		try {
			Product product = productManager.getByGoodsId(goodsid);
			if (product == null) {
				return JsonResultUtil.getErrorJson("此商品不存在！");
			}
			productMap = BeanUtils.describe(product);

			Map goods = goodsManager.get(goodsid);
			if (goods.get("thumbnail") != null) {
				productMap.put("thumbnail", StaticResourcesUtil.convertToUrl(goods.get("thumbnail").toString()));
			}

			if (goods.get("store_id") != null) {
				productMap.put("store_id", goods.get("store_id"));
			}
			if (goods.get("flag") != null) {
				productMap.put("flag", goods.get("flag"));
			}

			if (goods.get("is_integral_goods") != null) {
				productMap.put("is_integral_goods", goods.get("is_integral_goods"));
			}

			// 设置库存
			productMap.put("store", apiGoodsManager.getStore(product.getProduct_id()));

			// 取货品的规格列表
			productMap.put("specList", apiGoodsManager.getProductSpecs(product.getProduct_id()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new UrlNotFoundException();
		}

		int commentCount = apiCommentManager.getCommentsCount(goodsid);
		// System.out.println(commentCount);
		int goodCommentCount = apiCommentManager.getCommentsCount(goodsid, 3, 5);
		// System.err.println(goodCommentCount);
		productMap.put("comment_count", commentCount);
		if (commentCount > 0) {
			java.text.NumberFormat percentFormat = java.text.NumberFormat.getPercentInstance();
			percentFormat.setMaximumFractionDigits(0); // 最大小数位数
			percentFormat.setMaximumIntegerDigits(3);// 最大整数位数
			percentFormat.setMinimumFractionDigits(0); // 最小小数位数
			percentFormat.setMinimumIntegerDigits(1);// 最小整数位数
			productMap.put("comment_percent", percentFormat.format((float) goodCommentCount / (float) commentCount));
		} else {
			productMap.put("comment_percent", "100%");
		}

		// 是否已收藏
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			productMap.put("favorited", false);
		} else {
			productMap.put("favorited", apiFavoriteManager.isFavorited(goodsid, member.getMember_id()));
		}

		// 是否秒杀
		if (act_id != null && act_id > 0) {
			Map seckillGoods = apiGoodsManager.getSeckillGoods(goodsid, act_id);
			if (seckillGoods != null) {
				productMap.put("price", seckillGoods.get("price"));
				productMap.put("is_seckill", 1);
			}
		}

		// 是否是团购
		if (groupbuy_id != null && groupbuy_id > 0) {
			Map groupbuyGoods = apiGoodsManager.getGroupbuyGoods(goodsid, groupbuy_id);
			if (groupbuyGoods != null) {
				productMap.put("price", groupbuyGoods.get("price"));
				productMap.put("is_groupby", 1);
			}
		}

		// 促销信息
		Activity activity = storeActivityManager
				.getCurrentAct(NumberUtils.toInt(productMap.get("store_id").toString(), 0));
		if (activity != null) {
			if (activity.getRange_type().intValue() == 1 || (activity.getRange_type().intValue() == 2
					&& storeActivityManager.checkGoodsAct(goodsid, activity.getActivity_id()) == 1)) {
				Map map = activityManager.getActMap(activity.getActivity_id());
				if (map.containsKey("is_send_gift") && NumberUtils.toInt(map.get("is_send_gift").toString(), 0) == 1) {
					int gift_id = NumberUtils.toInt(map.get("gift_id").toString(), 0);
					if (gift_id > 0) {
						ActivityGift gift = activityGiftManager.get(gift_id);
						if (gift != null) {
							gift.setGift_img(StaticResourcesUtil.convertToUrl(gift.getGift_img()));
							map.put("gift", gift);
						}
					}
				}
				// 判断是否包含优惠券
				if (map.containsKey("is_send_bonus")
						&& NumberUtils.toInt(map.get("is_send_bonus").toString(), 0) == 1) {
					int bonus_id = NumberUtils.toInt(map.get("bonus_id").toString(), 0);
					if (bonus_id > 0) {
						BonusType bonus = this.bonusTypeManager.get(bonus_id);
						map.put("bonus", bonus);
					}
				}
				productMap.put("activity", map);
			}
		}

		return JsonResultUtil.getObjectJson(productMap);
	}

	/**
	 * nieruijie 2018-7-12 查询大转盘奖品，长图片
	 * 
	 * @param goodsid
	 *            商品id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/turntabledetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult turntableGallery(Integer goodsid) {
		
		Map<String, Object> productMap;
		try {
			//Product product = productManager.getByGoodsId(goodsid);
//			if (product == null) {
//				return JsonResultUtil.getErrorJson("此商品不存在！");
//			}
//			productMap = BeanUtils.describe(product);
			Map<Object,Object> productMap1 = new HashMap<>();

			Map goods = goodsManager.getTurntable(goodsid);
			if (goods.get("thumbnail") != null) {
				productMap1.put("thumbnail", StaticResourcesUtil.convertToUrl(goods.get("thumbnail").toString()));
			}

			if (goods.get("store_id") != null) {
				productMap1.put("store_id", goods.get("store_id"));
			}
			if (goods.get("flag") != null) {
				productMap1.put("flag", goods.get("flag"));
			}

			if (goods.get("is_integral_goods") != null) {
				productMap1.put("is_integral_goods", goods.get("is_integral_goods"));
			}
			 String intro  =(String)goods.get("intro");
			 productMap1.put("intro",intro);
			// 设置库存
			//productMap.put("store", apiGoodsManager.getStore(product.getProduct_id()));

			// 取货品的规格列表
			//productMap.put("specList", apiGoodsManager.getProductSpecs(product.getProduct_id()));
			return JsonResultUtil.getObjectJson(productMap1);
		} catch (Exception e) {
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("获取奖品图片异常");
			//throw new UrlNotFoundException();
		}
	}
	/**
	 * 获取商品介绍
	 * 
	 * @param goodsid
	 * @return by: laiyunchuan 2016-11-09 12:57:13
	 */
	@ResponseBody
	@RequestMapping(value = "/intro", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult intro(Integer goodsid) {

		Map<String, Object> goodsIntro;
		try {
			Product product = productManager.getByGoodsId(goodsid);
			if (product == null) {
				return JsonResultUtil.getErrorJson("此商品不存在！");
			}
			goodsIntro = BeanUtils.describe(product);

			Map goods = goodsManager.get(goodsid);

			// 获取详情介绍
			if (goods.get("intro") != null) {
				goodsIntro.put("intro", goods.get("intro"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new UrlNotFoundException();
		}
		return JsonResultUtil.getObjectJson(goodsIntro);
	}

	/**
	 * 获取活动详情
	 * 
	 * @param activity_id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/activity", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult activity(Integer activity_id) {
		if (activity_id == null || activity_id <= 0) {
			return JsonResultUtil.getErrorJson("系统参数错误！");
		}
		Map map = activityManager.getActMap(activity_id);
		if (map == null) {
			return JsonResultUtil.getErrorJson("此活不存在或已经停止！");
		}
		if (map.containsKey("is_send_gift") && NumberUtils.toInt(map.get("is_send_gift").toString(), 0) == 1) {
			int gift_id = NumberUtils.toInt(map.get("gift_id").toString(), 0);
			if (gift_id > 0) {
				ActivityGift gift = activityGiftManager.get(gift_id);
				if (gift != null) {
					gift.setGift_img(StaticResourcesUtil.convertToUrl(gift.getGift_img()));
					map.put("gift", gift);
				}
			}
		}
		return JsonResultUtil.getObjectJson(map);
	}

	/**
	 * 获取商品相册
	 *
	 * @param goodsid
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/gallery", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult gallery(Integer goodsid) {
		List<GoodsGallery> galleryList = this.goodsGalleryManager.list(goodsid);
		if (galleryList == null || galleryList.size() == 0) {
			String img = SystemSetting.getDefault_img_url();
			GoodsGallery gallery = new GoodsGallery();
			gallery.setSmall(img);
			gallery.setBig(img);
			gallery.setThumbnail(img);
			gallery.setTiny(img);
			gallery.setOriginal(img);
			gallery.setIsdefault(1);
			galleryList.add(gallery);
		}
		return JsonResultUtil.getObjectJson(galleryList);
	}

	/**
	 * 获取商品规格
	 *
	 * @param goodsid
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/spec", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult spec(Integer goodsid) {
		Map goods = goodsManager.get(goodsid);

		List<Product> productList = this.productManager.list(goodsid);

		// 获取库存
		for (Product product : productList) {
			product.setStore(apiGoodsManager.getStore(product.getProduct_id()));
		}

		Map data = new HashMap();

		if (goods.get("is_integral_goods") != null) {
			data.put("is_integral_goods", goods.get("is_integral_goods"));
		}

		if (("" + goods.get("have_spec")).equals("0")) {
			data.put("productid", productList.get(0).getProduct_id());// 商品的货品id
			data.put("productList", productList);// 商品的货品列表
		} else {
			List<Specification> specList = this.productManager.listSpecs(goodsid);
			data.put("specList", specList);// 商品规格数据列表
			data.put("productList", productList);// 商品的货品列表
		}
		data.put("have_spec", goods.get("have_spec") == null ? 0 : goods.get("have_spec"));// 是否有规格

		return JsonResultUtil.getObjectJson(data);
	}

	/**
	 * 根据标签获取商品列表
	 *
	 * @param tagid
	 * @param page
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/listbytag", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult listbytag(Integer tagid, Integer page) {
		if (page == null || page <= 0)
			page = 1;
		int pageSize = 10;
		List<Map> goodsList = apiGoodsManager.listByTag(tagid, page, pageSize);

		if (goodsList != null && goodsList.size() > 0) {
			for (Map map : goodsList) {
				map.put("thumbnail", StaticResourcesUtil.convertToUrl((String) map.get("thumbnail")));
			}
		}
		return JsonResultUtil.getObjectJson(goodsList);
	}

	/**
	 * 显示商品二维码
	 * 
	 * @param goods_id
	 */
	@RequestMapping("/qrcode")
	public void qrcode(Integer goods_id, HttpServletResponse response) throws Exception {
		if (goods_id == null || goods_id <= 0) {
			return;
		}

		// 将图片输出给浏览器
		BufferedImage image = QrCodeUtils.create("javashop://" + goods_id, BarcodeFormat.QR_CODE, 150, 150);
		response.setContentType("image/png");
		OutputStream os = response.getOutputStream();
		ImageIO.write(image, "jpg", os);
	}

	/**
	 * 获取商品属性标签
	 *
	 * @param goodsid
	 *            商品id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/attributeList", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult attributeList(Integer goodsid) {
		Map goodsmap = this.goodsManager.get(goodsid);
		Integer typeid = (Integer) goodsmap.get("type_id");
		List<Attribute> list = this.goodsTypeManager.getAttrListByTypeId(typeid);
		List attrList = new ArrayList();

		Map attrmap = null;
		attrmap = new HashMap();
		attrmap.put("attrName", "计价单位");
		attrmap.put("attrValue", goodsmap.get("unit"));
		attrList.add(attrmap);

		Map weightMap = new HashMap();
		weightMap.put("attrName", "品牌");
		weightMap.put("attrValue", goodsmap.get("brand_name"));
		attrList.add(weightMap);

		int i = 1;
		for (Attribute attribute : list) {
			attrmap = new HashMap();

			// 如果是选择项的
			if (attribute.getType() == 3) {
				String[] s = attribute.getOptionAr();
				String p = (String) goodsmap.get("p" + i);
				Integer num = 0;
				if (!StringUtil.isEmpty(p) && StringUtil.toInt(p, false) != 0 && s != null && s.length > 0) {
					num = Integer.parseInt(p);
					if (num < s.length) {
						attrmap.put("attrValue", s[num]);
					}
				}
				attrmap.put("attrName", attribute.getName());
				// 输入项
			} else {
				attrmap.put("attrName", attribute.getName());
				attrmap.put("attrValue", goodsmap.get("p" + i));
			}
			attrList.add(attrmap);
			i++;
		}
		return JsonResultUtil.getObjectJson(attrList);
	}
}
