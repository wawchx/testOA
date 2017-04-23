/*
 * @(#)JsonUtil.java May 1, 2011
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.common.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: May 1, 2011
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class JsonUtil {

	/**
	 * jackson json��������
	 */
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/**
	 * ƥ�������±�
	 */
	private static final Pattern ARRAY_PATTERN = Pattern
			.compile("\\[(\\d+)\\]");

	static {
		OBJECT_MAPPER.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
				true);
	}

	/**
	 * @param <T>
	 *            ��ȡ����java��������
	 * @param src
	 *            ������json����
	 * @param valueType
	 *            ��ȡ����java��������
	 * @return ��json�ж����Ķ���
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readValue(byte[],
	 *      java.lang.Class)
	 */
	public static <T> T readValue(byte[] src, Class<T> valueType)
			throws Exception {
		return OBJECT_MAPPER.readValue(src, valueType);
	}

	/**
	 * @param <T>
	 *            ��ȡ����java��������
	 * @param src
	 *            ������json����
	 * @param offset
	 *            ��ʼλ��
	 * @param len
	 *            ����
	 * @param valueType
	 *            ��ȡ����java��������
	 * @return ��json�ж����Ķ���
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readValue(byte[], int,
	 *      int, java.lang.Class)
	 */
	public static <T> T readValue(byte[] src, int offset, int len,
			Class<T> valueType) throws Exception {
		return OBJECT_MAPPER.readValue(src, offset, len, valueType);
	}

	/**
	 * @param <T>
	 *            ��ȡ����java��������
	 * @param src
	 *            �ļ�����
	 * @param valueType
	 *            ��ȡ����java��������
	 * @return ��json�ж����Ķ���
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readValue(java.io.File,
	 *      java.lang.Class)
	 */
	public static <T> T readValue(File src, Class<T> valueType)
			throws Exception {
		return OBJECT_MAPPER.readValue(src, valueType);
	}

	/**
	 * @param <T>
	 *            ��ȡ����java��������
	 * @param src
	 *            �ֽ�������
	 * @param valueType
	 *            ��ȡ����java��������
	 * @return ��json�ж����Ķ���
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readValue(java.io.InputStream,
	 *      java.lang.Class)
	 */
	public static <T> T readValue(InputStream src, Class<T> valueType)
			throws Exception {
		return OBJECT_MAPPER.readValue(src, valueType);
	}

	/**
	 * @param <T>
	 *            ��ȡ����java��������
	 * @param jp
	 *            JsonParser
	 * @param valueType
	 *            ��ȡ����java��������
	 * @return ��json�ж����Ķ���
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readValues(com.fasterxml.jackson.core.JsonParser,
	 *      java.lang.Class)
	 */
	public static <T> T readValue(JsonParser jp, Class<T> valueType)
			throws Exception {
		return OBJECT_MAPPER.readValue(jp, valueType);
	}

	/**
	 * @param <T>
	 *            ��ȡ����java��������
	 * @param src
	 *            �ַ�������
	 * @param valueType
	 *            ��ȡ����java��������
	 * @return ��json�ж����Ķ���
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readValue(java.io.Reader,
	 *      java.lang.Class)
	 */
	public static <T> T readValue(Reader src, Class<T> valueType)
			throws Exception {
		return OBJECT_MAPPER.readValue(src, valueType);
	}

	/**
	 * @param <T>
	 *            ��ȡ����java��������
	 * @param content
	 *            �ַ�����
	 * @param valueType
	 *            ��ȡ����java��������
	 * @return ��json�ж����Ķ���
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readValue(java.lang.String
	 *      , java.lang.Class)
	 */
	public static <T> T readValue(String content, Class<T> valueType)
			throws Exception {
		return OBJECT_MAPPER.readValue(content, valueType);
	}

	/**
	 * @param <T>
	 *            ��ȡ����java��������
	 * @param src
	 *            json���ݵ�ַ
	 * @param valueType
	 *            ��ȡ����java��������
	 * @return ��json�ж����Ķ���
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readValue(java.net.URL,
	 *      java.lang.Class)
	 */
	public static <T> T readValue(URL src, Class<T> valueType) throws Exception {
		return OBJECT_MAPPER.readValue(src, valueType);
	}

	/**
	 * 
	 * @param resultFile
	 *            json�ļ�
	 * @param value
	 *            Ҫת��Ϊjson�Ķ���
	 * @throws Exception
	 * @see com.fasterxml.jackson.databind.ObjectMapper#writeValue(java.io.File,
	 *      java.lang.Object)
	 */
	public static void writeValue(File resultFile, Object value)
			throws Exception {
		OBJECT_MAPPER.writeValue(resultFile, value);
	}

	/**
	 * @param out
	 *            jsonд����
	 * @param value
	 *            Ҫת��Ϊjson�Ķ���
	 * @throws Exception
	 * @see com.fasterxml.jackson.databind.ObjectMapper#writeValue(java.io.OutputStream,
	 *      java.lang.Object)
	 */
	public static void writeValue(OutputStream out, Object value)
			throws Exception {
		OBJECT_MAPPER.writeValue(out, value);
	}

	/**
	 * @param w
	 *            jsonд���ַ���
	 * @param value
	 *            Ҫת��Ϊjson�Ķ���
	 * @throws Exception
	 * @see com.fasterxml.jackson.databind.ObjectMapper#writeValue(java.io.Writer,
	 *      java.lang.Object)
	 */
	public static void writeValue(Writer w, Object value) throws Exception {
		OBJECT_MAPPER.writeValue(w, value);
	}

	/**
	 * @param value
	 *            Ҫת��Ϊjson�Ķ���
	 * @return json���ֽ�����
	 * @throws Exception
	 * @see com.fasterxml.jackson.databind.ObjectMapper#writeValueAsBytes(java.lang.Object)
	 */
	public static byte[] writeValueAsBytes(Object value) throws Exception {
		return OBJECT_MAPPER.writeValueAsBytes(value);
	}

	/**
	 * @param value
	 *            Ҫת��Ϊjson�Ķ���
	 * @return json��
	 * @throws Exception
	 * @see com.fasterxml.jackson.databind.ObjectMapper#writeValueAsString(java.lang.Object)
	 */
	public static String writeValueAsString(Object value) throws Exception {
		return OBJECT_MAPPER.writeValueAsString(value);
	}

	/**
	 * @param in
	 *            �ֽ�������
	 * @return JsonNode
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readTree(java.io.InputStream
	 *      )
	 */
	public static JsonNode readTree(InputStream in) throws Exception {
		return OBJECT_MAPPER.readTree(in);
	}

	/**
	 * @param r
	 *            �ַ�������
	 * @return JsonNode
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readTree(java.io.Reader)
	 */
	public static JsonNode readTree(Reader r) throws Exception {
		return OBJECT_MAPPER.readTree(r);
	}

	/**
	 * @param content
	 *            json����
	 * @return JsonNode
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readTree(java.lang.String
	 *      )
	 */
	public static JsonNode readTree(String content) throws Exception {
		return OBJECT_MAPPER.readTree(content);
	}

	/**
	 * @param file
	 *            json����
	 * @return JsonNode
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readTree(java.io.File )
	 */
	public static JsonNode readTree(File file) throws Exception {
		return OBJECT_MAPPER.readTree(file);
	}

	/**
	 * @param source
	 *            json����
	 * @return JsonNode
	 * @see com.fasterxml.jackson.databind.ObjectMapper#readTree(java.net.URL )
	 */
	public static JsonNode readTree(URL source) throws Exception {
		return OBJECT_MAPPER.readTree(source);
	}

	/**
	 * ʹ��jpathѡ��JsonNode
	 * 
	 * @param data
	 *            json����
	 * @param jpath
	 *            ��ַ���ʾ��jpath,����:WeatherForecast[0].Date
	 * @return JsonNode�б�
	 */
	public static List<JsonNode> selectNodes(String data, String jpath)
			throws Exception {
		JsonNode root = OBJECT_MAPPER.readTree(data);
		return selectNodes(root, jpath);
	}

	/**
	 * ʹ��jpathѡ��JsonNode
	 * 
	 * @param root
	 *            json���ݸ����
	 * @param jpath
	 *            ��ַ���ʾ��jpath,����:WeatherForecast[0].Date
	 * @return JsonNode�б�
	 */
	public static List<JsonNode> selectNodes(JsonNode root, String jpath)
			throws Exception {
		if (jpath == null || jpath.length() == 0) {
			List<JsonNode> ret = new ArrayList<JsonNode>(root.size());
			Iterator<JsonNode> it = root.iterator();
			while (it.hasNext()) {
				ret.add(it.next());
			}

			return ret;
		}
		List<JsonNode> lastNodes = new LinkedList<JsonNode>();
		if (root.isArray()) {
			Iterator<JsonNode> it = root.iterator();
			while (it.hasNext()) {
				lastNodes.add(it.next());
			}
		}
		else {
			lastNodes.add(root);
		}
		if (!jpath.endsWith(".")) {
			jpath = jpath + ".";
		}
		int idx = jpath.indexOf('.');
		while (idx != 0) {
			String prop = jpath.substring(0, idx);
			List<JsonNode> ret = new LinkedList<JsonNode>();
			Matcher m = ARRAY_PATTERN.matcher(prop);
			if (m.find()) {
				// ��������ȡ��N��
				int start = m.start();
				int index = Integer.parseInt(m.group(1));
				if (index < 0) {
					return ret;
				}
				if (start == 0) {
					// ֱ����[xxx]����ʽ��ͷ
					if (index < lastNodes.size()) {
						ret.add(lastNodes.get(index));
					}
				}
				else {
					prop = prop.substring(0, start);
					for (JsonNode ln : lastNodes) {
						JsonNode n = ln.get(prop);
						if (n == null || !n.isArray()) {
							continue;
						}
						if (index < n.size()) {
							ret.add(n.get(index));
						}
					}
				}
			}
			else {
				for (JsonNode ln : lastNodes) {
					JsonNode n = ln.get(prop);
					if (n == null) {
						continue;
					}
					if (n.isArray()) {
						Iterator<JsonNode> it = n.iterator();
						while (it.hasNext()) {
							ret.add(it.next());
						}
					}
					else {
						ret.add(n);
					}
				}
			}

			if (idx == jpath.length() - 1) {
				return ret;
			}
			lastNodes = ret;
			if (ret.isEmpty()) {
				return ret;
			}

			jpath = jpath.substring(idx + 1);
			idx = jpath.indexOf('.');
		}

		return null;
	}

	/**
	 * ʹ��jpathѡ��JsonNode
	 * 
	 * @param data
	 *            json����
	 * @param jpath
	 *            ��ַ���ʾ��jpath,����:WeatherForecast[0].Date
	 * @return JsonNode
	 */
	public static JsonNode selectSingleNode(String data, String jpath)
			throws Exception {
		List<JsonNode> ns = selectNodes(data, jpath);
		if (ns != null && !ns.isEmpty()) {
			return ns.get(0);
		}

		return null;
	}

	/**
	 * ʹ��jpathѡ��JsonNode
	 * 
	 * @param root
	 *            json���ݸ����
	 * @param jpath
	 *            ��ַ���ʾ��jpath,����:WeatherForecast[0].Date
	 * @return JsonNode
	 */
	public static JsonNode selectSingleNode(JsonNode root, String jpath)
			throws Exception {
		List<JsonNode> ns = selectNodes(root, jpath);
		if (ns != null && !ns.isEmpty()) {
			return ns.get(0);
		}

		return null;
	}

}
