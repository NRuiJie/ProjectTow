package com.enation.app.shop.mobile.payment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.StringUtil;

public class WeixinUtil {

	
	/**
	 * 进行sha1签名
	 * @param params
	 * @return
	 */
	public static String sha1Sign(Map params  ) {
		 
		String url = createLinkString(params);
		String sign =  Sha1.encode(url);
	 
		return sign;
	}

	
	
	/**
	 * 生成签名
	 * 
	 * @param params
	 *            参数map
	 * @param key
	 *            支付key(API密钥)
	 * @return
	 */
	public static String createSign(Map params, String key) {
		 
		String url = createLinkString(params);
		url = url + "&key=" + key;
		String sign = DigestUtils.md5Hex(url).toUpperCase();
		return sign;
	}

	public static String mapToXml(Map map) {
		Document document = DocumentHelper.createDocument();
	 
		Element nodeElement = document.addElement("xml");
		for (Object obj : map.keySet()) {
			Element keyElement = nodeElement.addElement(String.valueOf(obj));
			String text = String.valueOf(map.get(obj));
			if (!"total_fee".equals(obj)) {
				keyElement.addCDATA(text);
			} else {
				keyElement.setText(text);
			}

		}
		return doc2String(document);
	}
	
	/**
	 * 将一个xml转为map
	 * @param document
	 * @return
	 */
	public static Map xmlToMap(Document document)
	{
		Map map = new HashMap();
		
		Element rootEl = document.getRootElement();
		List<Element> elList =rootEl.elements();
		for (Element el : elList) {
			String name =el.getName();
			String text= el.getText();
			map.put(name,text);
		}
		return map;
	}
	public static String doc2String(Document document) {
		String s = "";
		try {
			// 使用输出流来进行转化
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// 使用UTF-8编码
		 
			OutputFormat format = new OutputFormat("   ", true, "UTF-8");
			XMLWriter writer = new XMLWriter(out, format);
			writer.write(document);
			s = out.toString("UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return s;
	}

	public static Document post(String wsPart, String doc_str) {

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(wsPart);
			HttpEntity requestEntity = new StringEntity(doc_str,"UTF-8");
			
			httppost.setEntity(requestEntity);
			httppost.setHeader(HTTP.CONTENT_ENCODING, HTTP.UTF_8);
			HttpResponse httpresponse = httpclient.execute(httppost);
			HttpEntity rentity = httpresponse.getEntity();
			InputStream in = rentity.getContent();
			// String res= StringUtil.inputStream2String(in);
			// System.out.println(res);
			if (in != null) {
				SAXReader reader = new SAXReader();
				Document doc = reader.read(in);
				return doc;
			} else {
				throw new RuntimeException("post uri[" + wsPart
						+ "]发生错误:[stream 返回Null]");
			}
		} catch (Throwable e) {

			throw new RuntimeException("post uri [" + wsPart + "]发生错误", e);

		}
	}
	
	
	public static  String httpget(String uri) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(uri);
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity, "utf-8");
		 
			return content;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		return null;
	}
	

	public static String createLinkString(Map<String, String> params) {

		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);

		String prestr = "";

		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);
			
			if("sign".equals(key)){
				continue;
			}
			
			if ("".equals(prestr)) {// 拼接时，不包括最后一个&字符
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr +"&" +key + "=" + value ;
			}
		}
		return prestr;
	}
	
	
	public static   String getWholeUrl( ){
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String servername =request.getServerName();
		String path  = request.getServletPath();
		int port = request.getServerPort();
		
		String portstr="";
		if(port!=80){
			portstr=":"+port;
		}
		String contextPath = request.getContextPath();
		if(contextPath.equals("/")){
			contextPath="";
		}
		
		if("/".equals(path)){
			path="/index.html";
		}
 
		
		String url  = "http://"+servername+path;
		String queryString = (request).getQueryString();
		
		if(!StringUtil.isEmpty(queryString)){
			url=url+"?"+queryString;
		}
		
		return url;
		
	}
	
	public static void main(String[] args) {
		Map map = new HashMap();
		map.put("a", "1");
		map.put("b", "2");
		
		String xml =mapToXml(map);
		System.out.println(xml);
		
	}
}
