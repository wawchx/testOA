/*
 * @(#)CryptoPropertiesPersister.java 2008-8-11
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.support.spring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;

import com.duker.mygift.common.security.PBECryptoUtil;

/**
 * <pre>
 * 读取properties文件并对cryptoProperties指定的属性进行加/解密处理(PBE算法)
 * cryptoProperties指定的属性值以*开头为密文,load的过程进行了解密;非以*开头的表示明文
 * load的过程中会把加密会的值写入到properties文件中
 * 
 * @author wangzh
 * 
 * @version 1.0
 * 
 * 修改版本: 1.0
 * 修改日期: 2008-8-11
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class CryptoPropertiesPersister {

	/**
	 * 要加密的属性列表key值
	 */
	private static final String CRYPTO_PROPERTIES = "cryptoProperties";

	/**
	 * 属性列表分割符
	 */
	private static final String ROPERTIES_DELIMITERS = "[,; \t]";

	/**
	 * 从属性文件中读入属性
	 * 
	 * @param props
	 *            属性
	 * @param source
	 *            文件
	 * @return Properties 属性
	 * @throws IOException
	 */
	public Properties load(Properties props, Resource source)
			throws IOException {
		return load(props, source.getFile());
	}

	public Properties load(Properties props, File file) throws IOException {
		String needCrypto = null;
		List<String> needCryptoList = null;
		Properties encryptedProps = null;
		Iterator<Entry<Object, Object>> it = null;
		Entry<Object, Object> entry = null;
		String propertyName = null;
		String propertyValue = null;

		if (props == null) {
			props = new Properties();
		}

		InputStream is = new FileInputStream(file);

		try {
			props.load(is);
		}
		finally {
			try {
				is.close();
			}
			catch (Exception ex) {
			}
		}
		needCrypto = (String) props.remove(CRYPTO_PROPERTIES);
		needCryptoList = splitToStringList(needCrypto, ROPERTIES_DELIMITERS);

		if (needCryptoList == null || needCryptoList.size() <= 0) {
			return props;
		}

		encryptedProps = new Properties();
		it = props.entrySet().iterator();

		while (it.hasNext()) {
			entry = it.next();
			propertyName = (String) entry.getKey();
			propertyValue = (String) entry.getValue();

			if (needCryptoList.contains(propertyName)) {
				convertPropertyValue(propertyName, propertyValue, props,
						encryptedProps);
			}
		}

		if (encryptedProps.size() > 0 && file.canWrite()
				&& !file.getName().endsWith(".xml")) {
			updateProperties(file, props, encryptedProps);
		}

		return props;
	}

	/**
	 * 更新属性文件
	 * 
	 * @param file
	 *            属性文件
	 * @param props
	 *            要更新的属性
	 * @param encryptedProps
	 *            加密后的属性
	 */
	protected void updateProperties(File file, Properties props,
			Properties encryptedProps) {
		BufferedReader reader = null;
		OutputStreamWriter writer = null;
		InputStreamReader inputReader = null;
		String line = null;
		String content = "";
		Matcher matcher = null;
		Iterator<Entry<Object, Object>> it = null;
		Entry<Object, Object> en = null;
		String key = null;
		String value = null;
		String regex = null;
		int end;
		String replace = null;

		try {
			inputReader = new InputStreamReader(new FileInputStream(file));
			reader = new BufferedReader(inputReader);
			line = reader.readLine();

			while (line != null) {
				matcher = Pattern.compile("[ \t\f]*#").matcher(line);

				if (!matcher.lookingAt()) {
					it = encryptedProps.entrySet().iterator();

					while (it.hasNext()) {
						en = it.next();
						key = (String) en.getKey();
						value = (String) en.getValue();
						regex = "[ \t\f]*" + key + "[ \t\f=]+";
						regex = regex.replaceAll("[.]", "\\\\.");
						matcher = Pattern.compile(regex).matcher(line);

						if (matcher.lookingAt()) {
							end = matcher.end();
							// "key="部分
							replace = line.substring(0, end);
							// "value"部分
							replace += value;
							line = replace;
							break;
						}
					}
				}

				content += line + System.getProperty("line.separator");
				line = reader.readLine();
			}

			writer = new OutputStreamWriter(new FileOutputStream(file),
					inputReader.getEncoding());
			writer.write(content);
		}
		catch (Throwable ex) {
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (Exception ex) {
				}
			}

			if (writer != null) {
				try {
					writer.close();
				}
				catch (Exception ex) {
				}
			}
		}
	}

	/**
	 * 转换属性值
	 * 
	 * @param key
	 *            关键字
	 * @param value
	 *            值
	 * @param props
	 *            转换前的属性
	 * @param encryptedProps
	 *            转换后的属性
	 */
	protected void convertPropertyValue(String key, String value,
			Properties props, Properties encryptedProps) {
		String convertedValue = PBECryptoUtil.cryptoPassword(value);

		if (!value.startsWith("*")) {
			encryptedProps.put(key, convertedValue);
		}
		else {
			props.put(key, convertedValue);
		}
	}

	/**
	 * 解析以特殊字符分割的字符串为list
	 * 
	 * @param str
	 *            要解析的串
	 * @param delimiters
	 *            分割符
	 * @return list
	 */
	protected List<String> splitToStringList(String str, String delimiters) {
		String[] splits = null;
		List<String> tokens = null;

		if (str == null) {
			return null;
		}

		splits = str.split(delimiters);

		if (splits == null || splits.length <= 0) {
			return null;
		}

		tokens = new LinkedList<String>();

		for (String token : splits) {
			token = token.trim();

			if (token.length() > 0) {
				tokens.add(token);
			}
		}

		return tokens;
	}

}
