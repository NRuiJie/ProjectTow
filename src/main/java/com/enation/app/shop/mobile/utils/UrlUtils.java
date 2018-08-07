package com.enation.app.shop.mobile.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrlUtils {

	/**
	 * 将url中的参数整理成map返回
	 * @param url
	 * @return
	 */
	public static Map<String, List<String>> getQueryParams(String url) {
        try {
            Map<String, List<String>> params = new HashMap<String, List<String>>();
            String[] urlParts = url.split("\\?");
            if (urlParts.length > 1) {
                String query = urlParts[1];
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], "UTF-8");
                    String value = "";
                    if (pair.length > 1) {
                        value = URLDecoder.decode(pair[1], "UTF-8");
                    }

                    List<String> values = params.get(key);
                    if (values == null) {
                        values = new ArrayList<String>();
                        params.put(key, values);
                    }
                    values.add(value);
                }
            }

            return params;
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }
	
	/**
	 * 获取url中一个参数值
	 * @param url
	 * @param name
	 * @return
	 */
	public static String getParameter(String url, String name){
		Map<String, List<String>> paramMap = getQueryParams(url);
		if(paramMap.size() == 0 || !paramMap.containsKey(name))
			return null;
		List<String> valueList = paramMap.get(name);
		if(valueList == null || valueList.size() == 0)
			return null;
		return valueList.get(0);
	}
	
	
}
