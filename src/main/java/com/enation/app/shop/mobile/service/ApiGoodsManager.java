package com.enation.app.shop.mobile.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.shop.core.goods.model.Cat;
import com.enation.app.shop.core.goods.model.Goods;
import com.enation.app.shop.core.goods.service.IGoodsCatManager;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;

@Service
public class ApiGoodsManager  {
	
	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private IGoodsCatManager goodsCatManager;
	
	public List listByCat(Integer catid, int number) {
		String sql = "SELECT g.* from es_goods  g WHERE g.disabled=0 and g.market_enable=1 ";
		if (catid.intValue() != 0) {
			Cat cat = this.goodsCatManager.getById(catid);
			sql += " and  g.cat_id in(";
			sql += "select c.cat_id from es_goods_cat c where c.cat_path like '" + cat.getCat_path()
					+ "%') ";
		}
		sql += " ORDER BY g.goods_id asc ";
		return this.daoSupport.queryForListPage(sql, 1, number);
	}
	
	public Map getSeckillGoods(int goodsid, int act_id){
		return this.daoSupport.queryForMap("SELECT * FROM es_seckill_goods  WHERE goods_id=? AND act_id=?", goodsid, act_id);
	}
	
	public Page getSeckillGoodsList(int pageNo,int pageSize){
		long timestamp = System.currentTimeMillis() / 1000;
		List goodslist = this.daoSupport.queryForListPage("select g.* from es_tag_rel  r LEFT JOIN es_goods "
				+ " g ON g.goods_id=r.rel_id where g.disabled=0 and g.market_enable=1 AND r.tag_id in (SELECT act_tag_id FROM es_seckill_active"
				+ " WHERE start_time<? AND end_time>?) order by r.ordernum desc", pageNo, pageSize, timestamp, timestamp);
		
		int count = this.daoSupport.queryForInt("select COUNT(0) from es_tag_rel  r LEFT JOIN es_goods" 
				+ " g ON g.goods_id=r.rel_id where g.disabled=0 and g.market_enable=1 AND r.tag_id in (SELECT act_tag_id FROM es_seckill_active"
				+ " WHERE start_time<? AND end_time>?) order by r.ordernum desc", timestamp, timestamp);
		return new Page(0, count, pageSize, goodslist);
	}
	
	/**
	 * 获取团购商品详情
	 * @param goodsid
	 * @param groupby_id
	 * @return
	 */
	public Map getGroupbuyGoods(int goodsid, int groupby_id){
		return this.daoSupport.queryForMap("SELECT * FROM es_groupbuy_goods  WHERE goods_id=? AND act_id=?", goodsid, groupby_id);
	}
	
	/**
	 * 获取一个货品的库存
	 * @param productId
	 * @return
	 */
	public int getStore(int productId){
		return this.daoSupport.queryForInt("SELECT SUM(store) FROM es_product_store  WHERE productid=?", productId);
	}
	
	/**
	 * 获取一个货品的规格列表
	 * @param productId
	 * @return
	 */
	public List getProductSpecs(int productId){
		return this.daoSupport.queryForList("SELECT spec_id,spec_value_id FROM es_goods_spec WHERE product_id=?", productId);
	}
	
	/**
	 * 获取热卖商品列表
	 * @param tag_id
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public List<Map> listByTag(int tag_id, int pageNo, int pageSize){
		String sql = "select g.* from es_tag_rel r LEFT JOIN es_goods g ON g.goods_id=r.rel_id "
				+ "where g.disabled=0 and g.market_enable=1 AND r.tag_id=? order by r.ordernum asc ";
		
		return this.daoSupport.queryForListPage(sql, pageNo, pageSize, tag_id);

	}
	
}
