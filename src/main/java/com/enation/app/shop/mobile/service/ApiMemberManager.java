package com.enation.app.shop.mobile.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.DateUtil;

/**
 * Author: Dawei
 * Datetime: 2016-11-07 12:18
 */
@Service
public class ApiMemberManager   {

	
	@Autowired
	private IDaoSupport daoSupport;
	
    /**
     * 获取会员优惠券列表
     * @param memberid
     * @param type     1:未使用; 2:已使用; 3:已过期
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Page getBonusListByMemberid(Integer memberid, Integer type, Integer pageNo, Integer pageSize) {
        String sql = "select b.type_id,b.type_money,b.type_name,b.use_start_date,"
                + "b.use_end_date,b.min_goods_amount"
                + ",s.store_name,s.store_id,m.used_time,m.order_id,m.used from es_member_bonus m left join es_bonus_type b on b.type_id = m.bonus_type_id"
                + " left join es_store s on b.store_id=s.store_id where m.member_id=" + memberid;
        switch (type.intValue()) {
            case 1:
                sql += " AND m.used=0";
                break;
            case 2:
                sql += " AND m.used=1";
                break;
            case 3:
                sql += " AND b.use_end_date<" + DateUtil.getDateline();
                break;
        }
        sql += " ORDER BY m.bonus_id DESC";
        return this.daoSupport.queryForPage(sql, pageNo, pageSize);
    }

    /**
     * 获取会员优惠券数量
     *
     * @param memberid
     * @param type
     * @return
     */
    public int getBounsCountByMemberId(Integer memberid, Integer type) {
        String sql = "select count(0) from es_member_bonus m left join es_bonus_type b on b.type_id = m.bonus_type_id"
                + " left join es_store s on b.store_id=s.store_id where m.member_id=" + memberid;
        switch (type.intValue()) {
            case 1:
                sql += " AND m.used=0";
                break;
            case 2:
                sql += " AND m.used=1";
                break;
            case 3:
                sql += " AND b.use_end_date<" + DateUtil.getDateline();
                break;
        }
        return this.daoSupport.queryForInt(sql);
    }

}
