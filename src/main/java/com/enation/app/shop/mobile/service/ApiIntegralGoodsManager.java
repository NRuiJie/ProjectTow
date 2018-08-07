package com.enation.app.shop.mobile.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.shop.core.goods.model.Cat;
import com.enation.app.shop.core.goods.model.Goods;
import com.enation.app.shop.core.goods.service.IGoodsCatManager;
import com.enation.app.shop.core.order.service.OrderStatus;
import com.enation.app.shop.mobile.model.ApiCategory;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;

@Service
public class ApiIntegralGoodsManager {

	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private IGoodsCatManager goodsCatManager;
	
	/**
	 * 获取积分商品二级分类信息
	 * @return
	 */
	public List<ApiCategory> listSecondCat() {
		String sql = "select cat_id,name as cat_name,cat_path from es_goods_cat where parent_id = 1000 and is_integral_cat = 1 "
				+ "and list_show = 1 order by cat_order desc";
		List<ApiCategory> cList = this.daoSupport.queryForList(sql, ApiCategory.class);
		return cList;
	}
	
	/**
	 * 获取积分分类下指定数量的积分商品数据
	 * @param cat_id 积分商品分类id
	 * @param num 指定的数量
	 * @return
	 */
	public List<Goods> listGoodsByLimit(Integer cat_id, Integer num) {
		Cat cat = this.goodsCatManager.getById(cat_id);
		String sql = "select g.* from es_goods g where g.disabled = 0 and g.market_enable = 1 and g.is_integral_goods = 1"
					+ " and g.cat_id in(select c.cat_id from es_goods_cat c where c.cat_path like '%"+cat.getCat_path()+"%') "
						+ "order by create_time desc limit 0,"+num+"";
		List<Goods> goodsList = this.daoSupport.queryForList(sql, Goods.class);
		
		return goodsList;
	}
	
	/**
	 * 根据商品分类id获取商品分页列表信息
	 * @param cat_id 商品分类id
	 * @param pageNo 页数
	 * @param pageSize 每页记录数
	 * @return
	 */
	public Page listPageByCat(Integer cat_id, int pageNo, int pageSize) {
		String sql = "select g.* from es_goods g where g.disabled = 0 and g.market_enable = 1 and g.is_integral_goods = 1";
		
		if (cat_id.intValue() != 0) {
			Cat cat = this.goodsCatManager.getById(cat_id);
			sql += " and g.cat_id in(select c.cat_id from es_goods_cat c where c.cat_path like '" + cat.getCat_path() + "%')";
		}
		
		sql += " order by g.create_time desc";
		Page webpage = this.daoSupport.queryForPage(sql, pageNo, pageSize, Goods.class);
		return webpage;
	}
	
	/**
	 * 获取乐币商品分页列表信息  add_by haiyan 2018-5-23
	 * @param pageNo 页数
	 * @param pageSize 每页记录数
	 * @return
	 */
	public Page listIntegerGoods(int pageNo, int pageSize) {
		String sql = "select g.* from es_goods g where g.disabled = 0 and g.market_enable = 1 and g.is_integral_goods = 1 order by g.price asc";
		Page webpage = this.daoSupport.queryForPage(sql, pageNo, pageSize, Goods.class);
		return webpage;
	}

	/*
	public void payNew(Integer orderId) {
		// TODO Auto-generated method stub
		this.daoSupport.execute("update es_payment_logs set status=?  where order_id=?", OrderStatus.PAY_YES, orderId);
		this.daoSupport.execute("update es_order set status=?,pay_status=? where order_id=?", OrderStatus.ORDER_PAY,
				OrderStatus.PAY_YES, orderId);
		this.daoSupport.execute("update es_order set status=?,pay_status=?  where parent_id=?",OrderStatus.ORDER_PAY,OrderStatus.PAY_YES,orderId);
	}
	*/
	/***
	 * 只是修改订单的状态
	 * 
	 * @param orderId	订单ID
	 * @param payMoney	付款金额
	 */
	public void payNew(Integer orderId, Double payMoney) {
		// TODO Auto-generated method stub
		this.daoSupport.execute("update es_payment_logs set status=?  where order_id=?", OrderStatus.PAY_YES, orderId);
		this.daoSupport.execute("update es_order set status=?,pay_status=? where order_id=?",
				OrderStatus.ORDER_PAY, OrderStatus.PAY_YES, orderId);
		this.daoSupport.execute("update es_order set status=?,pay_status=?,paymoney=? where parent_id=?",
				OrderStatus.ORDER_PAY, OrderStatus.PAY_YES, payMoney, orderId);
	}	
}
