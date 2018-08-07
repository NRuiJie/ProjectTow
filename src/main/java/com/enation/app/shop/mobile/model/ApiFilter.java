package com.enation.app.shop.mobile.model;

import java.io.Serializable;
import java.util.List;

public class ApiFilter implements Serializable {

	private String name;
	private List<ApiFilterValue> valueList;
	private String type;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ApiFilterValue> getValueList() {
		return valueList;
	}
	public void setValueList(List<ApiFilterValue> valueList) {
		this.valueList = valueList;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
	
}
