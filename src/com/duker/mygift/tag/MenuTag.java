/*
 * @(#)MenuTag.java 2010-2-2
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.tag;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.duker.mygift.common.util.ServletUtil;
import com.duker.mygift.constant.CList;
import com.duker.mygift.model.Menu;
import com.duker.mygift.vo.Tree;

/**
 * <pre>
 * 用户菜单标签
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2010-2-2
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@SuppressWarnings("unchecked")
public class MenuTag {

	/**
	 * 生成用户菜单
	 * 
	 * @param request
	 *            http请求
	 * @return 菜单html
	 */
	public static String getMenu(HttpServletRequest request) {
		Tree<Menu> menu = ServletUtil.getValue(request.getSession(),
				CList.User.Session.MENU, Tree.class);

		return getMenu(menu, request.getContextPath());
	}

	/**
	 * 递归取出每个结点的菜单
	 * 
	 * @param menu
	 *            菜单结点
	 * @param contextPath
	 * @return 菜单html
	 */
	public static String getMenu(Tree<Menu> menu, String contextPath) {
		if (menu == null || !menu.hasChild()) {
			return "";
		}

		StringBuilder menuString = new StringBuilder();
		StringBuilder childString = new StringBuilder();
		List<Tree<Menu>> children = menu.getChildren();
		menuString.append("<div menu='true' ");
		Menu m = menu.getElement();
		if (m != null) {
			String menuId = m.getMenuId();
			if (StringUtils.isNotBlank(menuId)) {
				menuString.append("id='");
				menuString.append(menuId);
				menuString.append("_");
				menuString.append(m.hashCode());
				menuString.append("' ");
			}
		}
		menuString.append("class='menu'><ul menu='true'>");

		for (Tree<Menu> child : children) {
			m = child.getElement();
			if (m == null) {
				continue;
			}
			String menuName = m.getMenuName();
			if (child.hasChild()) {
				menuString.append("<li menu='true' child='");
				menuString.append(m.getMenuId());
				menuString.append("_");
				menuString.append(m.hashCode());
				menuString.append("'>");
				menuString.append(menuName);
				menuString.append("</li>");
				childString.append(getMenu(child, contextPath));
			}
			else {
				String url = m.getUrl();
				if (StringUtils.isNotBlank(url)) {
					String target = m.getTarget();
					menuString.append("<li menu='true'><a menu='true' href='");
					if (!url.startsWith("http://")) {
						menuString.append(contextPath);
					}
					menuString.append(url);
					menuString.append("'");
					if (StringUtils.isNotBlank(target)) {
						menuString.append(" target='");
						menuString.append(target);
						menuString.append("'");
					}
					menuString.append(">");
					menuString.append(menuName);
					menuString.append("</a></li>");
				}
			}
		}
		menuString.append("</ul></div>");
		menuString.append(childString);

		return menuString.toString();
	}
}
