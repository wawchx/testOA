/*
 * @(#)CList.java Aug 14, 2009
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.constant;

/**
 * <pre>
 * H3ҵ�����ϵͳ�ĳ���ֵ
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: Aug 14, 2009
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public interface CList {

	/**
	 * ���з�
	 */
	String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

	/**
	 * ��С����
	 */
	char MIN_CHARACTER = 0x4E00;

	/**
	 * �����
	 */
	char MAX_CHARACTER = 0x9FA5;

	/**
	 * ie6����ͷ
	 */
	String USER_AGENT_IE6 = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)";

	/**
	 * ie7����ͷ
	 */
	String USER_AGENT_IE7 = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)";

	/**
	 * ie8����ͷ
	 */
	String USER_AGENT_IE8 = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)";

	/**
	 * ie10����ͷ
	 */
	String USER_AGENT_IE9 = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)";

	/**
	 * firefox����ͷ
	 */
	String USER_AGENT_FIREFOX = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0";

	/**
	 * chrome����ͷ
	 */
	String USER_AGENT_CHROME = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.26 Safari/537.11";

	/**
	 * 360���������ͷ
	 */
	String USER_AGENT_360_PHONE = "Mozilla/5.0 (Linux; U; Android 4.2.2; zh-cn; GT-I9152 Build/JDQ39) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30; 360browser(securitypay,securityinstalled); 360(android,uppayplugin); 360 Aphone Browser (5.4.0)";

	/**
	 * nginx���ػص�
	 */
	String X_ACCEL_REDIRECT = "X-Accel-Redirect";

	/**
	 * nginx���ػص��ַ���
	 */
	String X_ACCEL_CHARSET = "X-Accel-Charset";

	/**
	 * apache���ػص�
	 */
	String X_SENDFILE = "X-Sendfile";

	/**
	 * ȡnginxת�������ʵip
	 */
	String X_REAL_IP = "X-Real-IP";

	/**
	 * ȡnginxת�������ʵip
	 */
	String X_REDIRECT_IP = "X-Redirect-IP";

	/**
	 * ȡcdnת�������ʵip
	 */
	String HTTP_X_FORWARDED_FOR = "X-Forwarded-For";

	/**
	 * ȡ����ת�������ʵip
	 */
	String PROXY_CLIENT_IP = "Proxy-Client-IP";

	/**
	 * ȡ����ת�������ʵip
	 */
	String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";

	/**
	 * ����ϵͳ����
	 */
	interface User {

		/**
		 * session����
		 */
		interface Session {

			/**
			 * ��¼�û���
			 */
			String USER_NAME = "login.userName";

			/**
			 * �û���Ϣ
			 */
			String USER_INFO = "login.user";

			/**
			 * ��֤��
			 */
			String SECURITY_CODE = "rv";

			/**
			 * url��ַ
			 */
			String URLS = "login.urls";

			/**
			 * �˵�
			 */
			String MENU = "login.menu";

			/**
			 * ������ַ
			 */
			String NAVIGATION = "login.navigation";

		}

	}
}