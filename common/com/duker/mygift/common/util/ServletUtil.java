/*
 * @(#)ServletUtil.java 2009-11-24
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.util;

import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.duker.mygift.constant.CList;

/**
 * <pre>
 * servlet取值工具类
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2009-11-24
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@SuppressWarnings("unchecked")
public class ServletUtil {

	/**
	 * 从session中取值
	 * 
	 * @param session
	 *            会话
	 * @param key
	 *            会话键值
	 * @param cls
	 *            会话值类型
	 * @return 会话中的值
	 */
	public static <T> T getValue(HttpSession session, String key, Class<T> cls) {
		Object obj = session.getAttribute(key);

		if (obj == null) {
			return null;
		}

		if (cls.isAssignableFrom(obj.getClass())) {
			return (T) obj;
		}

		return null;
	}

	/**
	 * 从request中取值
	 * 
	 * @param request
	 *            请求
	 * @param key
	 *            request键值
	 * @param cls
	 *            request值类型
	 * @return request中的值
	 */
	public static <T> T getValue(HttpServletRequest request, String key,
			Class<T> cls) {
		Object obj = request.getAttribute(key);

		if (obj == null) {
			return null;
		}

		if (cls.isAssignableFrom(obj.getClass())) {
			return (T) obj;
		}

		return null;
	}

	/**
	 * 取得访问者的真实ip
	 * 
	 * @param request
	 *            请求
	 * @return ip地址
	 */
	public static String getRealIp(HttpServletRequest request) {
		// 取得从cdn转发后的真实ip
		String ip = request.getHeader(CList.HTTP_X_FORWARDED_FOR);
		if (StringUtils.isBlank(ip)) {
			// 取得从代理转发后的真实ip
			ip = request.getHeader(CList.PROXY_CLIENT_IP);
			if (StringUtils.isBlank(ip)) {
				// 取得从代理转发后的真实ip
				ip = request.getHeader(CList.WL_PROXY_CLIENT_IP);
				if (StringUtils.isBlank(ip)) {
					// 没有从cdn、代理转发过来,取nginx转发后的真实ip
					ip = request.getHeader(CList.X_REDIRECT_IP);
					if (StringUtils.isBlank(ip)) {
						// 没有从cdn、代理转发过来,取nginx转发后的真实ip
						ip = request.getHeader(CList.X_REAL_IP);
					}
				}
			}
		}

		if (StringUtils.isBlank(ip)) {
			// 用户直接访问的真实ip
			ip = request.getRemoteAddr();
		}
		else {
			for (String i : ip.split(",")) {
				if (!"unknown".equalsIgnoreCase(i)) {
					return i;
				}
			}
		}

		return ip;
	}

	/**
	 * 取得访问者的User-Agent，并替换移动资产User-Agent为firefox的
	 * 
	 * @param request
	 *            请求
	 * @return User-Agent
	 */
	public static String getUserAgent(HttpServletRequest request) {
		String ua = request.getHeader("User-Agent");

		return ua;
	}

	/**
	 * 找出用户访问的地址
	 * 
	 * @param request
	 *            请求
	 * @param response
	 *            响应
	 * @throws ServletException
	 */
	public static String findLastPage(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		StringBuilder lastPage = new StringBuilder();
		Map<String, Object> params = request.getParameterMap();

		lastPage.append(request.getContextPath());
		lastPage.append(request.getServletPath());

		if (params != null) {
			Entry<String, Object> entry = null;
			String key = null;
			Object value = null;
			Iterator<Entry<String, Object>> it = params.entrySet().iterator();
			String separator = "?";
			String charset = request.getCharacterEncoding();

			if (charset == null) {
				charset = "utf-8";
			}

			try {
				while (it.hasNext()) {
					entry = it.next();
					key = entry.getKey();
					value = entry.getValue();

					if (key == null || value == null) {
						continue;
					}

					if (value.getClass().isArray()) {
						for (int i = 0; i < Array.getLength(value); i++) {
							Object v = Array.get(value, i);

							if (v == null) {
								continue;
							}

							lastPage.append(separator);
							lastPage.append(key);
							lastPage.append("=");
							lastPage.append(URLEncoder.encode(v.toString(),
									charset));
							separator = "&";
						}
					}
				}
			}
			catch (Exception ex) {
				throw new ServletException(ex);
			}
		}

		return lastPage.toString();
	}

	/**
	 * 取得服务器地址，包括scheme,host,port
	 * 
	 * @param request
	 *            http请求
	 * @return 服务器地址
	 */
	public static String getSchemeHostAndPort(HttpServletRequest request) {
		StringBuffer sb = request.getRequestURL();
		int idx = sb.indexOf("//");
		idx = sb.indexOf("/", idx + 2);
		if (idx != -1) {
			return sb.substring(0, idx);
		}
		return sb.toString();
	}
}
