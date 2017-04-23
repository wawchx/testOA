/*
 * @(#)ELUtil.java 2009-7-31
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.tag;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.JSONUtil;

import com.duker.mygift.constant.CList;
import com.duker.mygift.constant.DList;

/**
 * <pre>
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2009-7-31
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class ELUtil {

	/**
	 * ����Map
	 */
	private static final Map<String, Map<Object, String>> CONST_MAP = new HashMap<String, Map<Object, String>>();

	static {
		// user_state
		Map<Object, String> map = new LinkedHashMap<Object, String>();
		map.put(0, "����");
		map.put(DList.UserInfo.State.VALID, "��Ч");
		map.put(DList.UserInfo.State.INVALID, "��Ч");
		map.put(DList.UserInfo.State.LOCKED, "����");
		CONST_MAP.put("user_state", map);

		map = new LinkedHashMap<Object, String>();
		map.put(0, "����");
		map.put(DList.AuditInfo.status.going, "���ϱ�");
		map.put(DList.AuditInfo.status.notthrough, "���ϸ�");
		map.put(DList.AuditInfo.status.goingthrough, "�����ϱ�");
		map.put(DList.AuditInfo.status.through, "��ͨ��");
		CONST_MAP.put("auditInfo_status", map);
	}

	/**
	 * ����һ������map��s:select,s:radioʹ��
	 * 
	 * @param key
	 *            ����key
	 * @return mapֵ
	 */
	public static Map<Object, String> getConstMap(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}

		return CONST_MAP.get(key);
	}

	/**
	 * ����һ������map��s:select,s:radioʹ��,�������õ�request attribute����
	 * 
	 * @param request
	 *            http����
	 * @param key
	 *            ����key
	 */
	public static void getConstMap(HttpServletRequest request, String key) {
		Map<Object, String> map = getConstMap(key);
		if (map != null && !map.isEmpty()) {
			request.setAttribute("c_" + key, map);
		}
	}

	/**
	 * �Ƿ�Ϊ�����û���½
	 * 
	 * @return true/false
	 */
	public static boolean isSuperUser() {
		HttpSession session = ServletActionContext.getRequest().getSession();
		String userName = (String) session
				.getAttribute(CList.User.Session.USER_NAME);

		return "user".equals(userName);
	}

	/**
	 * �Ƿ��ǳ����û���½
	 * 
	 * @return true/false
	 */
	public static boolean isNotSuperUser() {
		HttpSession session = ServletActionContext.getRequest().getSession();
		String userName = (String) session
				.getAttribute(CList.User.Session.USER_NAME);

		return !"user".equals(userName);
	}

	/**
	 * �ж��ַ����Ƿ�Ϊ��
	 * 
	 * @param str
	 *            �ַ���
	 * @return �Ƿ�Ϊ��
	 */
	public static boolean isNull(String str) {
		return str == null || StringUtils.isBlank(str)
				|| "null".equalsIgnoreCase(str);
	}

	/**
	 * �ж��ַ����Ƿ�Ϊ��
	 * 
	 * @param str
	 *            �ַ���
	 * @return �Ƿ�Ϊ��
	 */
	public static boolean isNotNull(String str) {
		return str != null && !StringUtils.isBlank(str)
				&& !"null".equalsIgnoreCase(str);
	}

	/**
	 * ���������ַ���
	 * 
	 * @param str1
	 *            �ַ���
	 * @param str2
	 *            �ַ���
	 * @return �ַ���
	 */
	public static String strcat(String str1, String str2) {
		return str1 + str2;
	}

	/**
	 * ��ȡ������ַ���
	 * 
	 * @param input
	 *            Ҫ��ȡ�Ĵ�
	 * @param beginIndex
	 *            ��ʼλ��
	 * @param endIndex
	 *            ����λ��
	 * @param fill
	 *            ��䴮
	 * @return ��ȡ��
	 */
	public static String substring(String input, int beginIndex, int endIndex,
			String fill) {
		if (input == null) {
			return "";
		}

		// input = escapeHTML(input);
		input = input.trim();

		int length = input.length();

		if (beginIndex >= length) {
			return "";
		}

		if (beginIndex < 0) {
			beginIndex = 0;
		}

		if (endIndex < 0 || endIndex > length) {
			endIndex = length;
		}

		if (endIndex < beginIndex) {
			return "";
		}

		if (endIndex < length && fill != null) {
			return input.substring(beginIndex, endIndex) + fill;
		}

		return input.substring(beginIndex, endIndex);
	}

	/**
	 * ��ȡĿ�괮֮����ַ�
	 * 
	 * @param input
	 *            ���봮
	 * @param substring
	 *            Ŀ�괮
	 * @return ��ȡ����ַ���
	 */
	public static String substringAfter(String input, String substring) {
		if (input == null) {
			input = "";
		}
		if (input.length() == 0) {
			return "";
		}
		if (substring == null) {
			substring = "";
		}
		if (substring.length() == 0) {
			return input;
		}

		int index = input.indexOf(substring);
		if (index == -1) {
			return input;
		}
		return input.substring(index + substring.length());
	}

	/**
	 * ��ȡĿ�괮֮ǰ���ַ�
	 * 
	 * @param input
	 *            ���봮
	 * @param substring
	 *            Ŀ�괮
	 * @return ��ȡ����ַ���
	 */
	public static String substringBefore(String input, String substring) {
		if (input == null) {
			input = "";
		}
		if (input.length() == 0) {
			return "";
		}
		if (substring == null) {
			substring = "";
		}
		if (substring.length() == 0) {
			return "";
		}

		int index = input.indexOf(substring);
		if (index == -1) {
			return input;
		}
		return input.substring(0, index);
	}

	/**
	 * ��java��������json�ַ���
	 * 
	 * @param object
	 *            java����
	 * @param excludes
	 *            �����л�������,���ŷָ��������ʽ
	 * @param includes
	 *            Ҫ���л�������,���ŷָ��������ʽ
	 * @return json�ַ���
	 */
	public static String toJSON(Object object, String excludes, String includes) {
		try {
			List<Pattern> excludeProperties = null;
			List<Pattern> includeProperties = null;

			Set<String> excludePatterns = JSONUtil.asSet(excludes);

			if (excludePatterns != null) {
				excludeProperties = new ArrayList<Pattern>(
						excludePatterns.size());
				for (String pattern : excludePatterns) {
					excludeProperties.add(Pattern.compile(pattern));
				}
			}

			Set<String> includePatterns = JSONUtil.asSet(includes);

			if (includePatterns != null) {
				includeProperties = new ArrayList<Pattern>(
						includePatterns.size());
				Map<String, String> existingPatterns = new HashMap<String, String>();

				for (String pattern : includePatterns) {
					// Compile a pattern for each *unique* "level" of the object
					// hierarchy specified in the regex.
					String[] patternPieces = pattern.split("\\\\\\.");
					String patternExpr = "";

					for (String patternPiece : patternPieces) {
						if (patternExpr.length() > 0) {
							patternExpr += "\\.";
						}
						patternExpr += patternPiece;

						// Check for duplicate patterns so that there is no
						// overlap.
						if (!existingPatterns.containsKey(patternExpr)) {
							existingPatterns.put(patternExpr, patternExpr);

							// Add a pattern that does not have the indexed
							// property matching (ie. list\[\d+\] becomes list).
							if (patternPiece.endsWith("\\]")) {
								includeProperties.add(Pattern
										.compile(patternExpr
												.substring(0, patternPiece
														.lastIndexOf("\\["))));
							}

							includeProperties.add(Pattern.compile(patternExpr));
						}
					}
				}
			}

			return JSONUtil.serialize(object, excludeProperties,
					includeProperties, true, true);
		}
		catch (Exception ex) {
		}

		return "";
	}

	/**
	 * �޳�html��ǩ
	 * 
	 * @param content
	 *            Ҫ�޳�������
	 * @return �޳��������
	 */
	public static String escapeHTML(String content) {
		if (content == null) {
			return null;
		}

		content = StringEscapeUtils.escapeHtml4(content.replaceAll("[\r\n]+",
				"<br>"));

		return content;
	}

	/**
	 * encodeURI ��������һ������� URI������������������ݸ�
	 * decodeURIComponent����ô�����س�ʼ���ַ�����encodeURI ��������������ַ����б��룺":"��"/"��";"��"?" ��
	 * "&"����ʹ�� encodeURIComponent ��������Щ�ַ����б��롣
	 * 
	 * @param url
	 *            url��ַ
	 * @param response
	 *            http��Ӧ
	 * @return ������url
	 */
	public static String encodeURI(String url, HttpServletResponse response) {
		String enc = response.getCharacterEncoding();
		if (StringUtils.isBlank(enc)) {
			enc = "utf-8";
		}
		try {
			url = URLEncoder.encode(url, enc);
			url = url.replace(URLEncoder.encode(":", "utf-8"), ":");
			url = url.replace(URLEncoder.encode("/", "utf-8"), "/");
			url = url.replace(URLEncoder.encode(";", "utf-8"), ";");
			url = url.replace(URLEncoder.encode("?", "utf-8"), "?");
			url = url.replace(URLEncoder.encode("&", "utf-8"), "&");
			url = url.replace("+", "%20");
		}
		catch (Exception e) {
		}

		return url;
	}

	/**
	 * encodeURIComponent ��������һ���ѱ���� URI������������������ݸ�
	 * decodeURIComponent����ô�����س�ʼ���ַ�������Ϊ encodeURIComponent
	 * ���������е��ַ����룬��ע�⣬������ַ�������һ��·��������
	 * /folder1/folder2/default.html�����е�б��Ҳ�������롣����һ�������ñ���������Ϊ�����͵� web
	 * ������ʱ������Ч�ġ�����ַ����а�����ֹһ�� URI �������ʹ�� encodeURI �������б��롣
	 * 
	 * @param url
	 *            url��ַ
	 * @param response
	 *            http��Ӧ
	 * @return ������url
	 */
	public static String encodeURIComponent(String url,
			HttpServletResponse response) {
		String enc = response.getCharacterEncoding();
		if (StringUtils.isBlank(enc)) {
			enc = "utf-8";
		}
		try {
			url = URLEncoder.encode(url, enc);
			url = url.replace("+", "%20");
		}
		catch (Exception e) {
		}

		return url;
	}

	/**
	 * URIComponent ��һ�������� URI ��һ���֡�
	 * 
	 * @param url
	 *            url��ַ
	 * @param response
	 *            http��Ӧ
	 * @return ������url
	 */
	public static String decodeURIComponent(String url,
			HttpServletResponse response) {
		String enc = response.getCharacterEncoding();
		if (StringUtils.isBlank(enc)) {
			enc = "utf-8";
		}
		try {
			url = URLDecoder.decode(url, enc);
		}
		catch (Exception e) {
		}

		return url;
	}

	/**
	 * ȡ���ļ����ĺ�׺
	 * 
	 * @param fileName
	 *            �ļ���
	 * @return �ļ����ĺ�׺
	 */
	public static String getSuffix(String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return "";
		}
		int idx = fileName.lastIndexOf('.');
		if (idx != -1) {
			return fileName.substring(idx);
		}

		return "";
	}

	/**
	 * ��ӡ�쳣��ҳ��
	 * 
	 * @param request
	 *            http����
	 * @param writer
	 *            jspҳ�������
	 */
	public static void printStackTrace(HttpServletRequest request,
			JspWriter writer) {
		Object obj = request.getAttribute("javax.servlet.error.exception");
		if (obj instanceof Exception) {
			Exception ex = (Exception) obj;
			ex.printStackTrace(new PrintWriter(writer));
		}
	}

	/**
	 * ��ʽ�����
	 * 
	 * @param cents
	 *            ��
	 * @param unit
	 *            ��λ,Ԫ,��Ϊ��
	 * @return Ԫ,��ȷ��С�������λ
	 */
	public static String formatMoney(Long cents, String unit) {
		if (cents == null) {
			return null;
		}
		BigDecimal bd = new BigDecimal(cents);
		if (StringUtils.isNotBlank(unit)) {
			return bd.movePointLeft(2).toString() + unit;
		}
		return bd.movePointLeft(2).toString();
	}

	/**
	 * ת�嵯����������Ҫ���ַ��������滻�س�����Ϊ<br>
	 * ��escapeHtml4��escapeEcmaScript
	 * 
	 * @param content
	 *            ת��ǰ���ַ���
	 * @return ת�����ַ���
	 */
	public static String escapeYmPrompt(String content) {
		return StringEscapeUtils.escapeEcmaScript(StringEscapeUtils
				.escapeHtml4(content.replaceAll("[\r\n]+", "<br>")));
	}
}
