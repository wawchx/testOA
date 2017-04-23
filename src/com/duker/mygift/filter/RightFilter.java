/*
 * @(#)RightFilter.java Jan 24, 2010
 * 
 * 雷悍网络后台管理系统
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
 * 权限过滤器
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: Jan 24, 2010
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@SuppressWarnings("unchecked")
public class RightFilter extends OncePerRequestFilter {

	/**
	 * 不需要过滤的url
	 */
	private final Set<String> notFilters = new HashSet<String>(0);

	/**
	 * 需要登录过滤的url
	 */
	private final Set<String> needLoginFilters = new HashSet<String>(0);

	/**
	 * 日志对象
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
				promptTimout("请先登录！", request, response);

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
					// 有权限
					filterChain.doFilter(request, response);
				}
			} else {
				prompt("尊敬的用户，您无权使用此项功能！", request, response);
			}
		} catch (ServletException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("内部错误", ex);
			prompt("内部错误", request, response);
		}
	}

	/**
	 * 用户未登录，跳转到登录页面
	 * 
	 * @param msg
	 *            消息内容
	 * @param request
	 *            请求
	 * @param response
	 *            响应
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
	 * 不具有访问权限，跳转到指定页面
	 * 
	 * @param msg
	 *            消息内容
	 * @param request
	 *            请求
	 * @param response
	 *            响应
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
			p.setName("返回");
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
