/*
 * @(#)CList.java Aug 14, 2009
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.constant;

/**
 * <pre>
 * H3业务管理系统的常量值
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: Aug 14, 2009
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public interface CList {

	/**
	 * 换行符
	 */
	String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

	/**
	 * 最小汉字
	 */
	char MIN_CHARACTER = 0x4E00;

	/**
	 * 最大汉字
	 */
	char MAX_CHARACTER = 0x9FA5;

	/**
	 * ie6请求头
	 */
	String USER_AGENT_IE6 = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)";

	/**
	 * ie7请求头
	 */
	String USER_AGENT_IE7 = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)";

	/**
	 * ie8请求头
	 */
	String USER_AGENT_IE8 = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)";

	/**
	 * ie10请求头
	 */
	String USER_AGENT_IE9 = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)";

	/**
	 * firefox请求头
	 */
	String USER_AGENT_FIREFOX = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0";

	/**
	 * chrome请求头
	 */
	String USER_AGENT_CHROME = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.26 Safari/537.11";

	/**
	 * 360浏览器请求头
	 */
	String USER_AGENT_360_PHONE = "Mozilla/5.0 (Linux; U; Android 4.2.2; zh-cn; GT-I9152 Build/JDQ39) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30; 360browser(securitypay,securityinstalled); 360(android,uppayplugin); 360 Aphone Browser (5.4.0)";

	/**
	 * nginx下载回调
	 */
	String X_ACCEL_REDIRECT = "X-Accel-Redirect";

	/**
	 * nginx下载回调字符集
	 */
	String X_ACCEL_CHARSET = "X-Accel-Charset";

	/**
	 * apache下载回调
	 */
	String X_SENDFILE = "X-Sendfile";

	/**
	 * 取nginx转发后的真实ip
	 */
	String X_REAL_IP = "X-Real-IP";

	/**
	 * 取nginx转发后的真实ip
	 */
	String X_REDIRECT_IP = "X-Redirect-IP";

	/**
	 * 取cdn转发后的真实ip
	 */
	String HTTP_X_FORWARDED_FOR = "X-Forwarded-For";

	/**
	 * 取代理转发后的真实ip
	 */
	String PROXY_CLIENT_IP = "Proxy-Client-IP";

	/**
	 * 取代理转发后的真实ip
	 */
	String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";

	/**
	 * 管理系统常量
	 */
	interface User {

		/**
		 * session常量
		 */
		interface Session {

			/**
			 * 登录用户名
			 */
			String USER_NAME = "login.userName";

			/**
			 * 用户信息
			 */
			String USER_INFO = "login.user";

			/**
			 * 验证码
			 */
			String SECURITY_CODE = "rv";

			/**
			 * url地址
			 */
			String URLS = "login.urls";

			/**
			 * 菜单
			 */
			String MENU = "login.menu";

			/**
			 * 导航地址
			 */
			String NAVIGATION = "login.navigation";

		}

	}
}