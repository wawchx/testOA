/*
 * @(#)RefreshServlet.java 2009-11-26
 * 
 * ��Ϣ��˹���ϵͳ
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
 * ��¼��Ϻ������ϴη��ʵ�ҳ��
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2009-11-26
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class RefreshServlet extends HttpServlet {

	/**
	 * ���л��汾��
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
