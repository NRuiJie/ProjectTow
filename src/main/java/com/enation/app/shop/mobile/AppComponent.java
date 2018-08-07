package com.enation.app.shop.mobile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.framework.component.IComponent;
import com.enation.framework.database.IDaoSupport;

@Component
public class AppComponent implements IComponent {

	@Autowired
	private IDaoSupport daoSupport;

	@Override
	public void install() {
		// 添加ThemeUri映射
		daoSupport
				.execute("INSERT INTO es_themeuri(uri,path,deleteflag,pagename,point,sitemaptype,keywords,description,httpcache) "
						+ "VALUES('/mobile/goods-(\\d+).html', '/mobile/goods.html', '0', '商品详情', '0', '0', '', '', '0')");
		daoSupport
				.execute("INSERT INTO es_themeuri(uri,path,deleteflag,pagename,point,sitemaptype,keywords,description,httpcache) "
						+ "VALUES('/mobile/goodsattr-(\\d+).html', '/mobile/goodsattr.html', '0', '商品属性', '0', '0', '', '', '0')");


		// 添加广告位及演示广告

		// 添加App商品标签：热品推荐等

	}

	@Override
	public void unInstall() {
		daoSupport.execute("DELETE FROM es_themeuri WHERE path=?",
				"/mobile/goods.html");
		daoSupport.execute("DELETE FROM es_themeuri WHERE path=?",
				"/mobile/goodsattr.html");
	}

}
