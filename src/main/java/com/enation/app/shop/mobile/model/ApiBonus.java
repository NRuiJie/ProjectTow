package com.enation.app.shop.mobile.model;

/**
 * Author: Dawei
 * Datetime: 2016-11-05 11:50
 */
public class ApiBonus {

    private Integer type_id;

    private String type_name;

    private Double type_money;

    private Integer use_start_date;

    private Integer use_end_date;

    private Double min_goods_amount;

    public Integer getType_id() {
        return type_id;
    }

    public void setType_id(Integer type_id) {
        this.type_id = type_id;
    }

    public String getType_name() {
        return type_name;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public Double getType_money() {
        return type_money;
    }

    public void setType_money(Double type_money) {
        this.type_money = type_money;
    }

    public Integer getUse_start_date() {
        return use_start_date;
    }

    public void setUse_start_date(Integer use_start_date) {
        this.use_start_date = use_start_date;
    }

    public Integer getUse_end_date() {
        return use_end_date;
    }

    public void setUse_end_date(Integer use_end_date) {
        this.use_end_date = use_end_date;
    }

    public Double getMin_goods_amount() {
        return min_goods_amount;
    }

    public void setMin_goods_amount(Double min_goods_amount) {
        this.min_goods_amount = min_goods_amount;
    }
}
