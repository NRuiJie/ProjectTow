package com.enation.app.shop.mobile.model;

import com.enation.app.shop.core.order.model.SellBack;

import java.util.List;

/**
 * Author: Dawei
 * Datetime: 2016-10-08 18:37
 */
public class ApiSellBack extends SellBack {

    private List goodsList;

    public List getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List goodsList) {
        this.goodsList = goodsList;
    }
}
