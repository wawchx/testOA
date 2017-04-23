/*
 * @(#)ConfigUtil.java Dec 4, 2013
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: Dec 4, 2013
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class ConfigUtil {

	private Properties props = new Properties();

	private static final ConfigUtil instance = new ConfigUtil();

	private ConfigUtil() {
		InputStream in = null;

		try {
			in = Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream("sys.properties");
			props.load(in);
		}
		catch (Exception ex) {
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (Exception ex) {
				}
			}
		}
	}

	public static String getProperty(String key) {
		return instance.props.getProperty(key);
	}

}
