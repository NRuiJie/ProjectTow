package com.enation.app.shop.mobile.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.shop.core.member.model.Favorite;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;

/**
 * Created by Dawei on 4/29/15.
 */
@Service
public class ApiFavoriteManager  {

	@Autowired
	private IDaoSupport daoSupport;
	
	
    /**
     * 根据ID获取收藏信息
     * @param favorite_id
     * @return
     */
    public Favorite get(int favorite_id) {
        String sql = "SELECT * FROM es_favorite WHERE favorite_id=?";
        return daoSupport.queryForObject(sql, Favorite.class, favorite_id);
    }
    
    /**
     * 获取某一个用户的收藏商品数
     * @param member_id
     * @return
     */
    public Integer count(int member_id){
    	return daoSupport.queryForInt("SELECT COUNT(0) FROM es_favorite WHERE member_id=?", member_id);
    }

    /**
     * 根据商品ID和会员ID获取收藏信息
     * @param goodsid
     * @param memberid
     * @return
     */
    public Favorite get(int goodsid, int memberid){
        String sql = "SELECT * FROM es_favorite WHERE goods_id=? AND member_id=?";
        List<Favorite> favoriteList = daoSupport.queryForList(sql, Favorite.class, goodsid, memberid);
        if(favoriteList.size() > 0){
            return favoriteList.get(0);
        }
        return null;
    }

    /**
     * 根据商品ID和会员ID删除收藏信息
     * @param goodsid
     * @param memberid
     */
    public void delete(int goodsid, int memberid){
        daoSupport.execute("DELETE FROM es_favorite WHERE goods_id=? AND member_id=?", goodsid, memberid);
    }


    /**
     * 是否已经收藏某个商品
     * @param goodsid
     * @param memberid
     * @return
     */
    public boolean isFavorited(int goodsid, int memberid){
        return this.daoSupport.queryForInt("SELECT COUNT(0) FROM es_favorite WHERE goods_id=? AND member_id=?", goodsid,memberid) > 0;
    }


    /**
     * 添加收藏
     * @param goodsid
     */
    public void add(Integer goodsid, int memberid) {
    	if(this.isFavorited(goodsid, memberid))
    		return;
        String sql = "INSERT INTO es_favorite(member_id,goods_id,favorite_time) VALUES(?,?,?)";
        daoSupport.execute(sql, memberid, goodsid, com.enation.framework.util.DateUtil.getDateline());
    }
    
    /**
     * 批量添加收藏
     * @param goodsids
     * @param memberid
     */
    public void batchAdd(Integer[] goodsids, int memberid){
    	if(goodsids == null || goodsids.length == 0)
    		return;
    	for(Integer goodsid : goodsids){
    		if(this.isFavorited(goodsid, memberid))
    			continue;
    		add(goodsid, memberid);
    	}
    }

    /**
     * 获取收藏的店铺列表
     *
     * @param memberid
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Page getStoreList(Integer memberid, Integer pageNo, Integer pageSize) {
        String sql = "select s.store_logo,s.store_name,s.store_id,s.goods_num,s.store_desccredit,s.store_servicecredit," +
                "s.store_deliverycredit,s.store_credit,m.id,m.create_time,m.member_id" +
                " from es_member_collect m INNER JOIN es_store s  ON s.store_id=m.store_id" +
                " where s.store_id in (select store_id from es_member_collect where member_id=?) and m.member_id=? " +
                "ORDER BY m.id DESC";
        Page webpage = this.daoSupport.queryForPage(sql, pageNo, pageSize, memberid, memberid);
        return webpage;
    }

    /**
     * 获取某一个用户的收藏店铺数
     * @param member_id
     * @return
     */
    public Integer storeCount(int member_id){
        return daoSupport.queryForInt("SELECT COUNT(0) FROM es_member_collect WHERE member_id=?", member_id);
    }

}
