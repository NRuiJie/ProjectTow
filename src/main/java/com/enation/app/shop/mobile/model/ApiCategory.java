package com.enation.app.shop.mobile.model;

public class ApiCategory {

	//分类id
	private Integer cat_id;
	//分类名称
	private String cat_name;
	//分类id组合
	private String cat_path;

	public Integer getCat_id() {
		return cat_id;
	}

	public void setCat_id(Integer cat_id) {
		this.cat_id = cat_id;
	}

	public String getCat_name() {
		return cat_name;
	}

	public void setCat_name(String cat_name) {
		this.cat_name = cat_name;
	}

	public String getCat_path() {
		return cat_path;
	}

	public void setCat_path(String cat_path) {
		this.cat_path = cat_path;
	}

}
