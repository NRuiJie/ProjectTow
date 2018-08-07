package com.enation.app.shop.mobile.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateUtils {

	/**
	 * 是否是手机号码
	 * @param mobile
	 * @return
	 */
	public static boolean isMobile(String mobile) {
		boolean flag = false;
		try {
			Pattern p = Pattern
					.compile("^((13[0-9])|(14[0-9])|(15[^4,\\D])|(16[0-9])|(17[0-9])|(18[0-9])|(19[0-9]))\\d{8}$");
			Matcher m = p.matcher(mobile);
			flag = m.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
}
