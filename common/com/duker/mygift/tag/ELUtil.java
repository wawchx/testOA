/*
 * @(#)ELUtil.java 2009-7-31
 * 
 * 信息审核管理系统
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
 * 修改版本: 0.9
 * 修改日期: 2009-7-31
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class ELUtil {

	/**
	 * 常量Map
	 */
	private static final Map<String, Map<Object, String>> CONST_MAP = new HashMap<String, Map<Object, String>>();

	static {
		// user_state
		Map<Object, String> map = new LinkedHashMap<Object, String>();
		map.put(0, "所有");
		map.put(DList.UserInfo.State.VALID, "有效");
		map.put(DList.UserInfo.State.INVALID, "无效");
		map.put(DList.UserInfo.State.LOCKED, "锁定");
		CONST_MAP.put("user_state", map);

		map = new LinkedHashMap<Object, String>();
		map.put(0, "所有");
		map.put(DList.AuditInfo.status.going, "已上报");
		map.put(DList.AuditInfo.status.notthrough, "不合格");
		map.put(DList.AuditInfo.status.goingthrough, "继续上报");
		map.put(DList.AuditInfo.status.through, "已通过");
		CONST_MAP.put("auditInfo_status", map);
	}

	/**
	 * 返回一个常量map给s:select,s:radio使用
	 * 
	 * @param key
	 *            常量key
	 * @return map值
	 */
	public static Map<Object, String> getConstMap(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}

		return CONST_MAP.get(key);
	}

	/**
	 * 返回一个常量map给s:select,s:radio使用,并且设置到request attribute里面
	 * 
	 * @param request
	 *            http请求
	 * @param key
	 *            常量key
	 */
	public static void getConstMap(HttpServletRequest request, String key) {
		Map<Object, String> map = getConstMap(key);
		if (map != null && !map.isEmpty()) {
			request.setAttribute("c_" + key, map);
		}
	}

	/**
	 * 是否为超级用户登陆
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
	 * 是否不是超级用户登陆
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
	 * 判断字符串是否为空
	 * 
	 * @param str
	 *            字符串
	 * @return 是否为空
	 */
	public static boolean isNull(String str) {
		return str == null || StringUtils.isBlank(str)
				|| "null".equalsIgnoreCase(str);
	}

	/**
	 * 判断字符串是否不为空
	 * 
	 * @param str
	 *            字符串
	 * @return 是否不为空
	 */
	public static boolean isNotNull(String str) {
		return str != null && !StringUtils.isBlank(str)
				&& !"null".equalsIgnoreCase(str);
	}

	/**
	 * 连接两个字符串
	 * 
	 * @param str1
	 *            字符串
	 * @param str2
	 *            字符串
	 * @return 字符串
	 */
	public static String strcat(String str1, String str2) {
		return str1 + str2;
	}

	/**
	 * 截取并填充字符串
	 * 
	 * @param input
	 *            要截取的串
	 * @param beginIndex
	 *            开始位置
	 * @param endIndex
	 *            结束位置
	 * @param fill
	 *            填充串
	 * @return 截取后串
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
	 * 截取目标串之后的字符
	 * 
	 * @param input
	 *            输入串
	 * @param substring
	 *            目标串
	 * @return 截取后的字符串
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
	 * 截取目标串之前的字符
	 * 
	 * @param input
	 *            输入串
	 * @param substring
	 *            目标串
	 * @return 截取后的字符串
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
	 * 把java对象生成json字符串
	 * 
	 * @param object
	 *            java对象
	 * @param excludes
	 *            不序列化的属性,逗号分割的正则表达式
	 * @param includes
	 *            要序列化的属性,逗号分割的正则表达式
	 * @return json字符串
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
	 * 剔除html标签
	 * 
	 * @param content
	 *            要剔除的内容
	 * @return 剔除后的内容
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
	 * encodeURI 方法返回一个编码的 URI。如果您将编码结果传递给
	 * decodeURIComponent，那么将返回初始的字符串。encodeURI 方法不会对下列字符进行编码：":"、"/"、";"、"?" 和
	 * "&"。请使用 encodeURIComponent 方法对这些字符进行编码。
	 * 
	 * @param url
	 *            url地址
	 * @param response
	 *            http响应
	 * @return 编码后的url
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
	 * encodeURIComponent 方法返回一个已编码的 URI。如果您将编码结果传递给
	 * decodeURIComponent，那么将返回初始的字符串。因为 encodeURIComponent
	 * 方法对所有的字符编码，请注意，如果该字符串代表一个路径，例如
	 * /folder1/folder2/default.html，其中的斜杠也将被编码。这样一来，当该编码结果被作为请求发送到 web
	 * 服务器时将是无效的。如果字符串中包含不止一个 URI 组件，请使用 encodeURI 方法进行编码。
	 * 
	 * @param url
	 *            url地址
	 * @param response
	 *            http响应
	 * @return 编码后的url
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
	 * URIComponent 是一个完整的 URI 的一部分。
	 * 
	 * @param url
	 *            url地址
	 * @param response
	 *            http响应
	 * @return 编码后的url
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
	 * 取得文件名的后缀
	 * 
	 * @param fileName
	 *            文件名
	 * @return 文件名的后缀
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
	 * 打印异常到页面
	 * 
	 * @param request
	 *            http请求
	 * @param writer
	 *            jsp页面输出流
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
	 * 格式化金额
	 * 
	 * @param cents
	 *            分
	 * @param unit
	 *            单位,元,可为空
	 * @return 元,精确到小数点后两位
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
	 * 转义弹出窗口所需要的字符串，先替换回车换行为<br>
	 * 再escapeHtml4、escapeEcmaScript
	 * 
	 * @param content
	 *            转义前的字符串
	 * @return 转义后的字符串
	 */
	public static String escapeYmPrompt(String content) {
		return StringEscapeUtils.escapeEcmaScript(StringEscapeUtils
				.escapeHtml4(content.replaceAll("[\r\n]+", "<br>")));
	}
}
