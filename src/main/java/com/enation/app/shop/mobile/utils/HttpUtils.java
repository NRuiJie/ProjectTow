package com.enation.app.shop.mobile.utils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Dawei
 * Date: 14-7-16
 * Time: 上午11:11
 * To change this template use File | Settings | File Templates.
 */
public class HttpUtils {

    /**
     * 获取指定网址的源码
     *
     * @param url
     * @return
     */
    public static String get(String url) {
        return invoke(HttpClients.createDefault(), new HttpGet(url));
    }

    /**
     * Post提交数据
     *
     * @param url
     * @param data
     * @param headerMap
     * @return
     */
    public static String post(String url, String data, Map<String, String> headerMap) {
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setEntity(new StringEntity(data, Charset.forName("utf-8")));
        } catch (Exception ex) {
        }
        //添加头
        if(headerMap != null && headerMap.size() > 0){
            for(String key : headerMap.keySet()){
                httpPost.addHeader(key, headerMap.get(key));
            }
        }
        return invoke(HttpClients.createDefault(), httpPost);
    }

    /**
     * Post提交数据
     *
     * @param url
     * @param dataMap
     * @param headerMap
     * @return
     */
    public static String post(String url, Map<String, String> dataMap, Map<String, String> headerMap) {
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> postParams = new ArrayList<NameValuePair>();
        for (String key : dataMap.keySet()) {
            postParams.add(new BasicNameValuePair(key, dataMap.get(key)));
        }
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(postParams, Charset.forName("utf-8")));
        } catch (Exception ex) {
        }
        //添加头
        if(headerMap != null && headerMap.size() > 0){
            for(String key : headerMap.keySet()){
                httpPost.addHeader(key, headerMap.get(key));
            }
        }
        return invoke(HttpClients.createDefault(), httpPost);
    }

    private static String invoke(CloseableHttpClient httpClient, HttpRequestBase httpRequest) {
        String content = "";
        try {
            CloseableHttpResponse response = httpClient.execute(httpRequest);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    byte[] bytes = EntityUtils.toByteArray(entity);
                    if (bytes != null) {
                        String charset = "utf-8";
                        content = new String(bytes, Charset.forName(charset));
                    }
                }
            } catch (Exception ex) {
                return "";
            } finally {
                if (response != null) {
                    try {
                        response.close();
                    } catch (Exception ex) {
                    }
                }
            }
        } catch (Exception ex) {

        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception ex) {
                }
            }
        }
        return content;
    }

}
