/*
 * @(#)OpenSessionInViewFilter.java Apr 1, 2012
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.filter;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Apr 1, 2012
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class OpenSessionInViewFilter extends
		org.springframework.orm.hibernate4.support.OpenSessionInViewFilter {

	/**
	 * 不需要过滤的url
	 */
	private Set<String> notFilters = new HashSet<String>(0);

	public void setNotFilterList(String notFilterList) {
		if (notFilterList != null) {
			String[] urls = notFilterList.split("\\s*,\\s*");
			for (String url : urls) {
				notFilters.add(url);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.filter.OncePerRequestFilter#shouldNotFilter(javax
	 * .servlet.http.HttpServletRequest)
	 */
	protected boolean shouldNotFilter(HttpServletRequest request)
			throws ServletException {
		String url = request.getServletPath();

		return notFilters.contains(url);
	}
}
