/*
 * @(#)RefreshServlet.java 2009-11-26
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

/**
 * <pre>
 * 登录完毕后跳回上次访问的页面
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2009-11-26
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class RefreshServlet extends HttpServlet {

	/**
	 * 序列化版本号
	 */
	private static final long serialVersionUID = -7820842104777731569L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String lastPage = request.getParameter("lastPage");

		if (StringUtils.isBlank(lastPage)) {
			lastPage = request.getContextPath();
			if (lastPage == null || "".equals(lastPage)) {
				lastPage = "index.jsp";
			}
		}

		response.sendRedirect(lastPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
