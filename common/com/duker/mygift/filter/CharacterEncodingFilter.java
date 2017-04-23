/*
 * @(#)CharacterEncodingFilter.java Apr 26, 2009
 * 
 * 信息审核管理系统
 */ 

package com.duker.mygift.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.ClassUtils;
import org.springframework.web.filter.OncePerRequestFilter;


/**
 * <pre>
 * 编码集过滤器
 * 
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Apr 26, 2009
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class CharacterEncodingFilter extends OncePerRequestFilter {

	// Determine whether the Servlet 2.4 HttpServletResponse.setCharacterEncoding(String)
	// method is available, for use in the "doFilterInternal" implementation.
	private final static boolean responseSetCharacterEncodingAvailable = ClassUtils.hasMethod(
			HttpServletResponse.class, "setCharacterEncoding", new Class[] {String.class});


	private String encoding;

	private boolean forceEncoding = false;


	/**
	 * Set the encoding to use for requests. This encoding will be passed into a
	 * {@link javax.servlet.http.HttpServletRequest#setCharacterEncoding} call.
	 * <p>Whether this encoding will override existing request encodings
	 * (and whether it will be applied as default response encoding as well)
	 * depends on the {@link #setForceEncoding "forceEncoding"} flag.
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Set whether the configured {@link #setEncoding encoding} of this filter
	 * is supposed to override existing request and response encodings.
	 * <p>Default is "false", i.e. do not modify the encoding if
	 * {@link javax.servlet.http.HttpServletRequest#getCharacterEncoding()}
	 * returns a non-null value. Switch this to "true" to enforce the specified
	 * encoding in any case, applying it as default response encoding as well.
	 * <p>Note that the response encoding will only be set on Servlet 2.4+
	 * containers, since Servlet 2.3 did not provide a facility for setting
	 * a default response encoding.
	 */
	public void setForceEncoding(boolean forceEncoding) {
		this.forceEncoding = forceEncoding;
	}


	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (this.encoding != null && (this.forceEncoding || request.getCharacterEncoding() == null)) {
			request.setCharacterEncoding(this.encoding);
			//固化编码集
			request.getParameter("userName");
			if (this.forceEncoding && responseSetCharacterEncodingAvailable) {
				response.setCharacterEncoding(this.encoding);
			}
		}
		filterChain.doFilter(request, response);
	}

}
