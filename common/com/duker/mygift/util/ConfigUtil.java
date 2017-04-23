/*
 * @(#)ConfigUtil.java Dec 4, 2013
 * 
 * 信息审核管理系统
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
 * 修改版本: 0.9
 * 修改日期: Dec 4, 2013
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
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
