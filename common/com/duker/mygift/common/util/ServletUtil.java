/*
 * @(#)ServletUtil.java 2009-11-24
 * 
 * ��Ϣ��˹���ϵͳ
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
 * servletȡֵ������
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2009-11-24
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
@SuppressWarnings("unchecked")
public class ServletUtil {

	/**
	 * ��session��ȡֵ
	 * 
	 * @param session
	 *            �Ự
	 * @param key
	 *            �Ự��ֵ
	 * @param cls
	 *            �Ựֵ����
	 * @return �Ự�е�ֵ
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
	 * ��request��ȡֵ
	 * 
	 * @param request
	 *            ����
	 * @param key
	 *            request��ֵ
	 * @param cls
	 *            requestֵ����
	 * @return request�е�ֵ
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
	 * ȡ�÷����ߵ���ʵip
	 * 
	 * @param request
	 *            ����
	 * @return ip��ַ
	 */
	public static String getRealIp(HttpServletRequest request) {
		// ȡ�ô�cdnת�������ʵip
		String ip = request.getHeader(CList.HTTP_X_FORWARDED_FOR);
		if (StringUtils.isBlank(ip)) {
			// ȡ�ôӴ���ת�������ʵip
			ip = request.getHeader(CList.PROXY_CLIENT_IP);
			if (StringUtils.isBlank(ip)) {
				// ȡ�ôӴ���ת�������ʵip
				ip = request.getHeader(CList.WL_PROXY_CLIENT_IP);
				if (StringUtils.isBlank(ip)) {
					// û�д�cdn������ת������,ȡnginxת�������ʵip
					ip = request.getHeader(CList.X_REDIRECT_IP);
					if (StringUtils.isBlank(ip)) {
						// û�д�cdn������ת������,ȡnginxת�������ʵip
						ip = request.getHeader(CList.X_REAL_IP);
					}
				}
			}
		}

		if (StringUtils.isBlank(ip)) {
			// �û�ֱ�ӷ��ʵ���ʵip
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
	 * ȡ�÷����ߵ�User-Agent�����滻�ƶ��ʲ�User-AgentΪfirefox��
	 * 
	 * @param request
	 *            ����
	 * @return User-Agent
	 */
	public static String getUserAgent(HttpServletRequest request) {
		String ua = request.getHeader("User-Agent");

		return ua;
	}

	/**
	 * �ҳ��û����ʵĵ�ַ
	 * 
	 * @param request
	 *            ����
	 * @param response
	 *            ��Ӧ
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
	 * ȡ�÷�������ַ������scheme,host,port
	 * 
	 * @param request
	 *            http����
	 * @return ��������ַ
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
