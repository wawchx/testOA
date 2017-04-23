/*
 * @(#)NoCacheFilter.java Dec 12, 2009
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * <pre>
 * 不缓存http响应存过滤器
 * 
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Dec 12, 2009
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class NoCacheFilter extends OncePerRequestFilter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		response.setHeader("pragma", "no-cache");
		response.setHeader("cache-control", "no-cache");
		filterChain.doFilter(request, response);
	}

}
