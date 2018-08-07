package com.enation.app.shop.mobile.model;

import java.io.Serializable;

public class ApiFilterValue implements Serializable {

	private String name;
	private String value;
	
	public ApiFilterValue() {
	}
	
	public ApiFilterValue(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
	
}
