/*
 * @(#)CryptoPropertiesPersister.java 2008-8-11
 * 
 * ��Ϣ��˹���ϵͳ
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
 * ��ȡproperties�ļ�����cryptoPropertiesָ�������Խ��м�/���ܴ���(PBE�㷨)
 * cryptoPropertiesָ��������ֵ��*��ͷΪ����,load�Ĺ��̽����˽���;����*��ͷ�ı�ʾ����
 * load�Ĺ����л�Ѽ��ܻ��ֵд�뵽properties�ļ���
 * 
 * @author wangzh
 * 
 * @version 1.0
 * 
 * �޸İ汾: 1.0
 * �޸�����: 2008-8-11
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class CryptoPropertiesPersister {

	/**
	 * Ҫ���ܵ������б�keyֵ
	 */
	private static final String CRYPTO_PROPERTIES = "cryptoProperties";

	/**
	 * �����б�ָ��
	 */
	private static final String ROPERTIES_DELIMITERS = "[,; \t]";

	/**
	 * �������ļ��ж�������
	 * 
	 * @param props
	 *            ����
	 * @param source
	 *            �ļ�
	 * @return Properties ����
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
	 * ���������ļ�
	 * 
	 * @param file
	 *            �����ļ�
	 * @param props
	 *            Ҫ���µ�����
	 * @param encryptedProps
	 *            ���ܺ������
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
							// "key="����
							replace = line.substring(0, end);
							// "value"����
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
	 * ת������ֵ
	 * 
	 * @param key
	 *            �ؼ���
	 * @param value
	 *            ֵ
	 * @param props
	 *            ת��ǰ������
	 * @param encryptedProps
	 *            ת���������
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
	 * �����������ַ��ָ���ַ���Ϊlist
	 * 
	 * @param str
	 *            Ҫ�����Ĵ�
	 * @param delimiters
	 *            �ָ��
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
