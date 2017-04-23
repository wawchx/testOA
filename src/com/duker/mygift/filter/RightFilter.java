/*
 * @(#)RightFilter.java Jan 24, 2010
 * 
 * �׺������̨����ϵͳ
 */

package com.duker.mygift.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import com.duker.mygift.common.util.ServletUtil;
import com.duker.mygift.constant.CList;
import com.duker.mygift.vo.Position;

/**
 * <pre>
 * Ȩ�޹�����
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: Jan 24, 2010
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
@SuppressWarnings("unchecked")
public class RightFilter extends OncePerRequestFilter {

	/**
	 * ����Ҫ���˵�url
	 */
	private final Set<String> notFilters = new HashSet<String>(0);

	/**
	 * ��Ҫ��¼���˵�url
	 */
	private final Set<String> needLoginFilters = new HashSet<String>(0);

	/**
	 * ��־����
	 */
	private static final Log log = LogFactory.getLog(RightFilter.class);

	public void setNotFilterList(String notFilterList) {
		if (notFilterList != null) {
			String[] urls = notFilterList.split("\\s*,\\s*");
			for (String url : urls) {
				notFilters.add(url);
			}
		}
	}

	public void setNeedLoginFilterList(String needLoginFilterList) {
		if (needLoginFilterList != null) {
			String[] urls = needLoginFilterList.split("\\s*,\\s*");
			for (String url : urls) {
				needLoginFilters.add(url);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(
	 * javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			HttpSession session = request.getSession();
			String userName = ServletUtil.getValue(session,
					CList.User.Session.USER_NAME, String.class);

			if (StringUtils.isBlank(userName)) {
				promptTimout("���ȵ�¼��", request, response);

				return;
			}

			String url = request.getServletPath();
			if (needLoginFilters.contains(url)) {
				filterChain.doFilter(request, response);

				return;
			}

			List<String> urls = ServletUtil.getValue(session,
					CList.User.Session.URLS, List.class);
			boolean hasRight = "admin".equals(userName);
			if (!hasRight && urls != null && !urls.isEmpty()) {
				for (String u : urls) {
					if (url.startsWith(u)) {
						hasRight = true;
					}
				}
			}

			if (hasRight) {
				if (request.isSecure()) {
					String uri = request.getRequestURL().toString();
					int idx = uri.indexOf('/', 8);
					String u = uri.substring(0, idx).replace("https", "http")
							+ request.getContextPath();
					response.sendRedirect(u);
				} else {
					// ��Ȩ��
					filterChain.doFilter(request, response);
				}
			} else {
				prompt("�𾴵��û�������Ȩʹ�ô���ܣ�", request, response);
			}
		} catch (ServletException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("�ڲ�����", ex);
			prompt("�ڲ�����", request, response);
		}
	}

	/**
	 * �û�δ��¼����ת����¼ҳ��
	 * 
	 * @param msg
	 *            ��Ϣ����
	 * @param request
	 *            ����
	 * @param response
	 *            ��Ӧ
	 * @throws ServletException
	 */
	private void promptTimout(String msg, HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		RequestDispatcher dispatcher = request
				.getRequestDispatcher("/login.jsp");

		if (dispatcher != null) {
			request.setAttribute("lastPage",
					ServletUtil.findLastPage(request, response));
			request.setAttribute("loginMsg", msg);

			try {
				dispatcher.forward(request, response);
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}

	/**
	 * �����з���Ȩ�ޣ���ת��ָ��ҳ��
	 * 
	 * @param msg
	 *            ��Ϣ����
	 * @param request
	 *            ����
	 * @param response
	 *            ��Ӧ
	 * @throws ServletException
	 */
	private void prompt(String msg, HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		RequestDispatcher dispatcher = request
				.getRequestDispatcher("/resultinfo.jsp");

		if (dispatcher != null) {
			List<String> msgs = new ArrayList<String>(1);
			msgs.add(msg);
			List<Position> positions = new ArrayList<Position>(1);
			Position p = new Position();

			p.setType(1);
			p.setName("����");
			p.setUrl("history.back()");
			positions.add(p);

			request.setAttribute("actionMessages", msgs);
			request.setAttribute("positions", positions);

			try {
				dispatcher.forward(request, response);
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.filter.GenericFilterBean#initFilterBean()
	 */
	@Override
	protected void initFilterBean() throws ServletException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.filter.OncePerRequestFilter#shouldNotFilter(javax
	 * .servlet.http.HttpServletRequest)
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request)
			throws ServletException {
		String url = request.getServletPath();
		if (notFilters.contains(url)) {
			return true;
		}

		return false;
	}

}
