package com.enation.app.shop.mobile.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Dawei
 * Datetime: 2016-11-06 18:25
 */
public class ApiStoreCat {

    private Integer store_cat_id;

    private String store_cat_name;

    private Integer store_cat_pid;

    private List<ApiStoreCat> children = new ArrayList<>();

    public Integer getStore_cat_id() {
        return store_cat_id;
    }

    public void setStore_cat_id(Integer store_cat_id) {
        this.store_cat_id = store_cat_id;
    }

    public String getStore_cat_name() {
        return store_cat_name;
    }

    public void setStore_cat_name(String store_cat_name) {
        this.store_cat_name = store_cat_name;
    }

    public Integer getStore_cat_pid() {
        return store_cat_pid;
    }

    public void setStore_cat_pid(Integer store_cat_pid) {
        this.store_cat_pid = store_cat_pid;
    }

    public List<ApiStoreCat> getChildren() {
        return children;
    }

    public void setChildren(List<ApiStoreCat> children) {
        this.children = children;
    }
}
