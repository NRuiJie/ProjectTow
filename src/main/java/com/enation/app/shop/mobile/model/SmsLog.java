package com.enation.app.shop.mobile.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class SmsLog {
	
	public static Hashtable<String, Object> cache = new Hashtable<>();
	
	private String mobile;

    private String mobileCode;

    private List<Long> timeList = new ArrayList<>();

    public SmsLog() {}

    public SmsLog(String mobile, String mobileCode) {
        this.mobile = mobile;
        this.mobileCode = mobileCode;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobileCode() {
        return mobileCode;
    }

    public void setMobileCode(String mobileCode) {
        this.mobileCode = mobileCode;
    }

    public List<Long> getTimeList() {
        return timeList;
    }

    public void setTimeList(List<Long> timeList) {
        this.timeList = timeList;
    }
}
