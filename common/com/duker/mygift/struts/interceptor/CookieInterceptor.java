/*
 * @(#)CookieInterceptor.java 2009-6-16
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.struts.interceptor;

import java.util.Collections;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.util.TextParseUtil;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

/**
 * <pre>
 * 仿照org.apache.struts2.interceptor.CookieInterceptor写的cookie拦截器
 * 除了拦截cookie中的值外把javax.servlet.http.Cookie本身也拦截到action上
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2009-6-16
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class CookieInterceptor extends AbstractInterceptor {

	/**
	 * 序列化版本号
	 */
	private static final long serialVersionUID = 5769116830678147513L;


	private static final Logger LOG = LoggerFactory
			.getLogger(CookieInterceptor.class);

	private Set<?> cookiesNameSet = Collections.EMPTY_SET;

	private Set<?> cookiesValueSet = Collections.EMPTY_SET;

	/**
	 * Set the <code>cookiesName</code> which if matche will allow the cookie
	 * to be injected into action, could be comma-separated string.
	 * 
	 * @param cookiesName
	 */
	public void setCookiesName(String cookiesName) {
		if (cookiesName != null)
			this.cookiesNameSet = TextParseUtil
					.commaDelimitedStringToSet(cookiesName);
	}

	/**
	 * Set the <code>cookiesValue</code> which if matched (together with
	 * matching cookiesName) will caused the cookie to be injected into action,
	 * could be comma-separated string.
	 * 
	 * @param cookiesValue
	 */
	public void setCookiesValue(String cookiesValue) {
		if (cookiesValue != null)
			this.cookiesValueSet = TextParseUtil
					.commaDelimitedStringToSet(cookiesValue);
	}

	public String intercept(ActionInvocation invocation) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start interception");
		}

		final ValueStack stack = ActionContext.getContext().getValueStack();
		HttpServletRequest request = ServletActionContext.getRequest();

		// contains selected cookies
		Cookie cookies[] = request.getCookies();

		if (cookies != null) {
			if (cookiesNameSet.contains("*")) {
				for (int i = 0; i < cookies.length; i++) {
					if (LOG.isDebugEnabled()) {
						LOG
								.debug("contains cookie name [*] in configured cookies name set, cookie with name ["
										+ cookies[i].getName()
										+ "] with value ["
										+ cookies[i].getValue()
										+ "] will be injected");
					}

					populateCookieValueIntoStack(cookies[i], stack);
				}

			}
			else {
				for (int i = 0; i < cookies.length; i++) {
					if (cookiesNameSet.contains(cookies[i].getName())) {
						populateCookieValueIntoStack(cookies[i], stack);
					}
				}
			}

		}

		return invocation.invoke();
	}

	/**
	 * Hook that populate cookie value into value stack (hence the action) if
	 * the criteria is satisfied (if the cookie value matches with those
	 * configured).
	 * 
	 * @param cookie
	 * @param stack
	 */
	protected void populateCookieValueIntoStack(Cookie cookie, ValueStack stack) {
		String name = cookie.getName();
		String value = cookie.getValue();

		if (cookiesValueSet.isEmpty() || cookiesValueSet.contains("*")) {
			// If the interceptor is configured to accept any cookie value
			// OR
			// no cookiesValue is defined, so as long as the cookie name match
			// we'll inject it into Struts' action
			if (LOG.isDebugEnabled()) {
				if (cookiesValueSet.isEmpty()) {
					LOG
							.debug("no cookie value is configured, cookie with name ["
									+ name
									+ "] with value ["
									+ value
									+ "] will be injected");
				}
				else if (cookiesValueSet.contains("*")) {
					LOG
							.debug("interceptor is configured to accept any value, cookie with name ["
									+ name
									+ "] with value ["
									+ value
									+ "] will be injected");
				}
			}

			stack.setValue(name, value);
			stack.setValue(name + "Cookie", cookie);
		}
		else {
			// if cookiesValues is specified, the cookie's value must match
			// before we
			// inject them into Struts' action
			if (cookiesValueSet.contains(value)) {
				if (LOG.isDebugEnabled()) {
					LOG
							.debug("both configured cookie name and value matched, cookie ["
									+ name
									+ "] with value ["
									+ value
									+ "] will be injected");
				}

				stack.setValue(name, value);
				stack.setValue(name + "Cookie", cookie);
			}
		}
	}

}
