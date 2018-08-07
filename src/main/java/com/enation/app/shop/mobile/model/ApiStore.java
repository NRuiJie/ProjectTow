package com.enation.app.shop.mobile.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Dawei
 * Datetime: 2016-11-02 17:13
 */
public class ApiStore {

    private Integer store_id;	//店铺Id
    private String store_name;	//店铺名称
    private Integer store_level;//店铺等级
    private String  store_logo;	//店铺logo
    private String store_banner;//店铺横幅
    private String  description;//店铺简介
    private Integer  store_recommend;//是否推荐
    private Integer store_credit;	//店铺信用
    private double  praise_rate;	//店铺好评率
    private double  store_desccredit;	//描述相符度
    private double  store_servicecredit;	//服务态度分数
    private double store_deliverycredit;	//发货速度分数
    private Integer  store_collect;	//店铺收藏数量
    private Integer goods_num; //店铺商品数量
    private Long new_num;    //最新上架商品数量
    private Long hot_num;    //热卖商品数量
    private Integer platform_num;    //平台代理商品数量
    private Long recommend_num;  //推荐商品数量
    private Integer favorited;  //是否关注
    private List<ApiGoods> goodsList = new ArrayList<>();   //部分商品列表，用于店铺列表的展示

    public ApiStore() {
    }

    public Integer getStore_id() {
        return store_id;
    }

    public void setStore_id(Integer store_id) {
        this.store_id = store_id;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public Integer getStore_level() {
        return store_level;
    }

    public void setStore_level(Integer store_level) {
        this.store_level = store_level;
    }

    public String getStore_logo() {
        return store_logo;
    }

    public void setStore_logo(String store_logo) {
        this.store_logo = store_logo;
    }

    public String getStore_banner() {
        return store_banner;
    }

    public void setStore_banner(String store_banner) {
        this.store_banner = store_banner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStore_recommend() {
        return store_recommend;
    }

    public void setStore_recommend(Integer store_recommend) {
        this.store_recommend = store_recommend;
    }

    public Integer getStore_credit() {
        return store_credit;
    }

    public void setStore_credit(Integer store_credit) {
        this.store_credit = store_credit;
    }

    public double getPraise_rate() {
        return praise_rate;
    }

    public void setPraise_rate(double praise_rate) {
        this.praise_rate = praise_rate;
    }

    public double getStore_desccredit() {
        return store_desccredit;
    }

    public void setStore_desccredit(double store_desccredit) {
        this.store_desccredit = store_desccredit;
    }

    public double getStore_servicecredit() {
        return store_servicecredit;
    }

    public void setStore_servicecredit(double store_servicecredit) {
        this.store_servicecredit = store_servicecredit;
    }

    public double getStore_deliverycredit() {
        return store_deliverycredit;
    }

    public void setStore_deliverycredit(double store_deliverycredit) {
        this.store_deliverycredit = store_deliverycredit;
    }

    public Integer getStore_collect() {
        return store_collect;
    }

    public void setStore_collect(Integer store_collect) {
        this.store_collect = store_collect;
    }

    public Integer getGoods_num() {
        return goods_num;
    }

    public void setGoods_num(Integer goods_num) {
        this.goods_num = goods_num;
    }

    public Long getNew_num() {
        return new_num;
    }

    public void setNew_num(Long new_num) {
        this.new_num = new_num;
    }

    public Long getHot_num() {
        return hot_num;
    }

    public void setHot_num(Long hot_num) {
        this.hot_num = hot_num;
    }

    public Long getRecommend_num() {
        return recommend_num;
    }

    public void setRecommend_num(Long recommend_num) {
        this.recommend_num = recommend_num;
    }

    public Integer getFavorited() {
        return favorited;
    }

    public void setFavorited(Integer favorited) {
        this.favorited = favorited;
    }

    public List<ApiGoods> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<ApiGoods> goodsList) {
        this.goodsList = goodsList;
    }

	public Integer getPlatform_num() {
		return platform_num;
	}

	public void setPlatform_num(Integer platform_num) {
		this.platform_num = platform_num;
	}
    
}
