package com.enation.app.shop.mobile.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.framework.database.IDaoSupport;

/**
 * Author: Dawei
 * Datetime: 2016-11-06 14:39
 */
@Service
public class ApiStoreManager   {
	
	@Autowired
	private IDaoSupport daoSupport;
	
	
    /**
     * 删除收藏
     *
     * @param storeid
     * @param memberid
     */
    public void deleteCollect(Integer storeid, Integer memberid) {
        this.daoSupport.execute("delete from es_member_collect where store_id=? AND member_id=?", storeid, memberid);
    }

}
