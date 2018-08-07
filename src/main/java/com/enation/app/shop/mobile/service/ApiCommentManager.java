package com.enation.app.shop.mobile.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.StringUtil;

/**
 * Created by Dawei on 4/11/15.
 */
@Service
public class ApiCommentManager   {

	
	@Autowired
	private IDaoSupport daoSupport;
	
    /**
     * 获取商品评论数
     * @param goods_id
     * @return
     */
    public int getCommentsCount(int goods_id) {
        return daoSupport.queryForInt("SELECT COUNT(0) FROM es_member_comment WHERE goods_id=? AND status=1 AND type=1", goods_id);
    }

    /**
     * 获取某个评分区间的评论数
     * @param goods_id
     * @param minGrade
     * @param maxGrade
     * @return
     */
    public int getCommentsCount(int goods_id, int minGrade, int maxGrade){
        return daoSupport.queryForInt("SELECT COUNT(0) FROM es_member_comment WHERE goods_id=? AND status=1 AND type=1 AND grade > ? AND grade <= ?", goods_id, minGrade, maxGrade);
    }
    
    /**
     * 获取一个商品的晒图评论数
     * @param goods_id
     * @return
     */
    public int getImageCommentsCount(int goods_id){
		String sql = "select count(distinct(c.comment_id)) from es_member_comment c INNER JOIN es_member_comment_gallery g on c.comment_id=g.comment_id WHERE c.status=1 and c.goods_id=?";
    	return daoSupport.queryForInt(sql, goods_id);
    }
    
    /**
     * 取某一个商品最热门的几个评论
     * @param goods_id
     * @param number
     * @return
     */
    public List<Map> hot(int goods_id, int number) {
    	
    	String sql =  "SELECT m.lv_id,m.sex,m.uname,m.face,c.* FROM  es_member_comment  c LEFT JOIN es_member  m ON c.member_id=m.member_id " +
				"WHERE c.goods_id=? AND c.type=1 AND c.status=1 ORDER BY c.grade DESC, c.img desc";
    	
    	return this.daoSupport.queryForListPage(sql, 1, number, goods_id);
    }
    
    /**
     * 获取一个商品的评论
     * @param goods_id	商品ID
     * @param page		第几页
     * @param pageSize	每页显示几个
     * @param minGrade	最小评分
     * @param maxGrade	最大评分
     * @return
     */
    public Page getGoodsComments(int goods_id, int page, int pageSize, Integer minGrade, Integer maxGrade) {
		String sql = "SELECT m.lv_id,m.sex,m.uname,m.face,l.name as levelname,c.* FROM es_member_comment c LEFT JOIN es_member  m ON c.member_id=m.member_id LEFT JOIN es_member_lv l ON m.lv_id=l.lv_id " +
				"WHERE c.goods_id=? AND c.type=1 AND c.status=1 ";
		if(minGrade != null){
			sql += " and c.grade > " + minGrade;
		}
		if(maxGrade != null){
			sql += " AND c.grade <= " + maxGrade;
		}
		sql += " ORDER BY c.comment_id DESC";
		return this.daoSupport.queryForPage(sql, page, pageSize, goods_id);
	}

	/**
	 * 获取一个商品的图片评论
	 * @param goods_id	商品ID
	 * @param page		第几页
	 * @param pageSize	每页显示几个
	 * @return
	 */
	public Page getImageGoodsComments(int goods_id, int page, int pageSize) {
		//svn上种方式分页会有问题，所以逻辑修改
		String commentSql="select distinct(c.comment_id) from es_member_comment c inner JOIN es_member_comment_gallery g "
				+ "on c.comment_id=g.comment_id where c.goods_id=? and  c.type=1 and c.status=1 ";
		
		List<Map> list = this.daoSupport.queryForList(commentSql, goods_id);
		
		String sql = "select  m.lv_id,m.sex,m.uname,m.face,l.name as levelname, c.*  "
				+ "from  es_member_comment c  LEFT JOIN es_member m ON c.member_id=m.member_id LEFT JOIN es_member_lv l ON m.lv_id=l.lv_id  ";
		String comment="0";
		if(list!=null && list.size()>0){
			int i = 0;
			for(Map map : list){
				int id = (int) map.get("comment_id");
				if(i==0){
					comment=id+"";
				}else{
					comment+=","+id;
				}
				i++;
			}
		}
		sql+= " where c.comment_id in("+comment+") ORDER BY c.comment_id DESC ";
		return this.daoSupport.queryForPage(sql, page, pageSize);
	}

    /**
     * 添加评论晒图
     * @param comment_id
     * @param imageList
     */
    public void addGallery(Integer comment_id, List<String> imageList){
    	if(imageList == null || imageList.size() == 0)
    		return;
    	for (int i = 0; i < imageList.size(); i++) {
			Map map = new HashMap();
			map.put("comment_id", comment_id);
			map.put("original", imageList.get(i));
			map.put("sort", i);
			this.daoSupport.insert("es_member_comment_gallery", map);
		}
    }
    
    /**
     * 获取待评价的商品列表
     * @param member_id
     * @param order_id
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Page getGoodsList(Integer member_id,Integer order_id, Integer pageNo, Integer pageSize){
		String sql = "SELECT g.* FROM es_member_order_item m LEFT JOIN es_goods g ON m.goods_id=g.goods_id WHERE m.member_id=? AND m.commented=0";
		if(order_id != null && order_id > 0){
			sql += " AND m.order_id=" + order_id;
		}
		sql += " ORDER BY m.id DESC";
		return this.daoSupport.queryForPage(sql, pageNo, pageSize, member_id);
	}

	/**
	 * 获取一个评论的图片列表
	 * @param comment_id
	 * @return
     */
	public List<Map> getGallery(int comment_id){
		return this.daoSupport.queryForList("SELECT * FROM es_member_comment_gallery WHERE comment_id=? ORDER BY img_id ASC", comment_id);
	}

}
