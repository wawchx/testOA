/*
 * @(#)MenuTag.java 2010-2-2
 * 
 * ��Ϣ��˹���ϵͳ
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
 * �û��˵���ǩ
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2010-2-2
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
@SuppressWarnings("unchecked")
public class MenuTag {

	/**
	 * �����û��˵�
	 * 
	 * @param request
	 *            http����
	 * @return �˵�html
	 */
	public static String getMenu(HttpServletRequest request) {
		Tree<Menu> menu = ServletUtil.getValue(request.getSession(),
				CList.User.Session.MENU, Tree.class);

		return getMenu(menu, request.getContextPath());
	}

	/**
	 * �ݹ�ȡ��ÿ�����Ĳ˵�
	 * 
	 * @param menu
	 *            �˵����
	 * @param contextPath
	 * @return �˵�html
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
