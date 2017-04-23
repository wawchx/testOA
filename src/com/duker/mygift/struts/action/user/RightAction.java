/*
 * @(#)RightAction.java Jan 24, 2010
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.struts.action.user;

import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;

import com.duker.mygift.model.Menu;
import com.duker.mygift.model.RightGroup;
import com.duker.mygift.model.Role;
import com.duker.mygift.service.user.RightService;
import com.duker.mygift.struts.action.PaginatedAction;
import com.duker.mygift.vo.Tree;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

/**
 * <pre>
 * 权限管理
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
public class RightAction extends PaginatedAction {

	/**
	 * 角色
	 */
	private Role role;

	/**
	 * 用户用户名
	 */
	private String userName;

	/**
	 * 操作用户用户名
	 */
	private String operationUserName;

	/**
	 * 用户类型
	 */
	private Integer userType;

	/**
	 * 角色id
	 */
	private String roleId;

	/**
	 * 菜单id
	 */
	private String menuId;

	/**
	 * 菜单id列表
	 */
	private List<String> menuIds;

	/**
	 * 权限组id列表
	 */
	private List<String> rightIds;

	/**
	 * 树是否默认展开
	 */
	private boolean treeOpen = false;

	/**
	 * 权限服务
	 */
	private RightService rightService;

	/**
	 * 菜单集合
	 */
	private List<Menu> menus;

	/**
	 * 操作 0置顶 1上移 2下移 3置底
	 */
	private Integer type;

	/**
	 * 查找角色
	 * 
	 * @return 跳转控制值
	 */
	public String findRole() {
		role = rightService.findRole(roleId);

		return SUCCESS;
	}

	/**
	 * 创建角色树
	 * 
	 * @return 跳转控制
	 */
	public String createRoleTree() {
		List<Tree<Role>> trees = rightService.findRoles(operationUserName);

		if (trees != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version='1.0' encoding='utf-8'?>");
			try {
				boolean oldTreeOpen = treeOpen;
				sb.append("<tree id='0'>");
				getRoleTree(trees, sb);
				sb.append("</tree>");
				treeOpen = oldTreeOpen;
			}
			catch (Exception ex) {
				sb.setLength(0);
				sb.append("<?xml version='1.0' encoding='utf-8'?>");
				sb.append("<xml>获取分类树出错</xml>");
			}

			try {
				HttpServletResponse response = ServletActionContext
						.getResponse();
				String xml = sb.toString();
				response.setContentType("text/xml");
				OutputStream out = response.getOutputStream();
				out.write(xml.getBytes("utf-8"));
			}
			catch (Exception ex) {
			}
		}

		return NONE;
	}

	/**
	 * 生成树
	 * 
	 * @param trees
	 *            树对象
	 * @param sb
	 *            字符串
	 */
	private void getRoleTree(List<Tree<Role>> trees, StringBuilder sb) {
		for (Tree<Role> tree : trees) {
			Role element = tree.getElement();
			String roleId = element.getRoleId();
			String roleName = element.getRoleName();

			// 节点名称添加tip描述
			String desc = element.getDescription();
			if (desc == null) {
				desc = "";
			}

			sb.append("<item text='");
			sb.append(roleName);
			sb.append("' id='r_");
			sb.append(roleId);

			// 节点名称添加tip描述
			sb.append("' tooltip='");
			sb.append(desc);

			if (treeOpen) {
				sb.append("' open='1' call='1' select='1'>");
				treeOpen = false;
			}
			else {
				sb.append("'>");
			}

			List<Tree<Role>> subTrees = tree.getChildren();

			if (subTrees != null && !subTrees.isEmpty()) {
				getRoleTree(subTrees, sb);
			}

			sb.append("</item>");
		}
	}

	/**
	 * 添加角色
	 * 
	 * @return 跳转控制值
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "角色名不能为空", fieldName = "role.roleName", trim = true) })
	public String addRole() {
		role = rightService.addRole(operationUserName, role);

		return SUCCESS;
	}

	/**
	 * 更新角色信息
	 * 
	 * @return 跳转控制值
	 */
	@Validations(requiredStrings = {
			@RequiredStringValidator(message = "角色id不能为空", fieldName = "role.roleId", trim = true),
			@RequiredStringValidator(message = "角色名不能为空", fieldName = "role.roleName", trim = true) })
	public String updateRole() {
		role = rightService.updateRole(operationUserName, role);

		return SUCCESS;
	}

	/**
	 * 删除角色
	 * 
	 * @return 跳转控制
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "角色id不能为空", fieldName = "roleId", trim = true) })
	public String deleteRole() {
		rightService.deleteRole(operationUserName, roleId);

		return SUCCESS;
	}

	/**
	 * 根据权限组id或者菜单id查找它所属权限组的菜单id
	 * 
	 * @return 跳转控制值
	 */
	public String findMenuIds() {
		if (StringUtils.isNotBlank(roleId)) {
			menuIds = rightService.findMenuIdsByRole(roleId);
		}
		else if (StringUtils.isNotBlank(menuId)) {
			menuIds = rightService.findMenuIds(menuId);
		}

		return SUCCESS;
	}

	/**
	 * 创建菜单树
	 * 
	 * @return 跳转控制
	 */
	public String createMenuTree() {
		List<Tree<Menu>> trees = rightService.findMenus(operationUserName);

		if (trees != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version='1.0' encoding='utf-8'?>");
			try {
				boolean oldTreeOpen = treeOpen;
				sb.append("<tree id='0'>");
				getMenuTree(trees, sb);
				sb.append("</tree>");
				treeOpen = oldTreeOpen;
			}
			catch (Exception ex) {
				sb.setLength(0);
				sb.append("<?xml version='1.0' encoding='utf-8'?>");
				sb.append("<xml>获取分类树出错</xml>");
			}

			try {
				HttpServletResponse response = ServletActionContext
						.getResponse();
				String xml = sb.toString();
				response.setContentType("text/xml");
				OutputStream out = response.getOutputStream();
				out.write(xml.getBytes("utf-8"));
			}
			catch (Exception ex) {
			}
		}

		return NONE;
	}

	/**
	 * 生成树
	 * 
	 * @param trees
	 *            树对象
	 * @param sb
	 *            字符串
	 */
	private void getMenuTree(List<Tree<Menu>> trees, StringBuilder sb) {
		for (Tree<Menu> tree : trees) {
			Menu element = tree.getElement();
			String menuId = element.getMenuId();
			String menuName = element.getMenuName();

			sb.append("<item text='");
			sb.append(menuName);
			sb.append("' id='m_");
			sb.append(menuId);
			sb.append("'");
			if (treeOpen) {
				sb.append(" open='1' call='1' select='1'");
				treeOpen = false;
			}
			RightGroup right = element.getRight();
			if (right != null) {
				sb.append("><userdata name='rightId'>");
				sb.append(right.getRightId());
				sb.append("</userdata>");
			}
			else {
				if (!tree.hasChild()) {
					sb.append(" checked='1' disabled='1'");
				}
				sb.append("><userdata name='rightId'></userdata>");
			}

			sb.append("<userdata name='menuIndex'>");
			sb.append(element.getMenuIndex());
			sb.append("</userdata>");

			List<Tree<Menu>> subTrees = tree.getChildren();

			if (subTrees != null && !subTrees.isEmpty()) {
				getMenuTree(subTrees, sb);
			}

			sb.append("</item>");
		}
	}

	/**
	 * 给下级用户授权角色
	 * 
	 * @return 跳转控制
	 */
	@Validations(requiredStrings = {
			@RequiredStringValidator(message = "请先选择要授权的用户", fieldName = "userName", trim = true),
			@RequiredStringValidator(message = "请先选择角色", fieldName = "roleId", trim = true) })
	public String addUserRole() throws Exception {
		rightService.addUserRole(operationUserName, userName, roleId);

		return SUCCESS;
	}

	/**
	 * 取消用户授权的角色
	 * 
	 * @return 跳转控制
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "请先选择要取消授权的用户", fieldName = "userName", trim = true) })
	public String delUserRole() throws Exception {
		rightService.delUserRole(operationUserName, userName);

		return SUCCESS;
	}

	/**
	 * 给角色分配权限组
	 * 
	 * @return 跳转控制
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "请先选择要授权的角色", fieldName = "roleId", trim = true) })
	public String addRoleRight() throws Exception {
		rightService.addRoleRight(operationUserName, roleId, rightIds);

		return SUCCESS;
	}

	/**
	 * 更新菜单显示顺序
	 * 
	 * @return 跳转控制
	 */
	@Validations(requiredFields = {
			@RequiredFieldValidator(message = "分类id不能为空", fieldName = "menuId"),
			@RequiredFieldValidator(message = "操作类型不能为空", fieldName = "type") })
	public String updateMenuOrder() {
		menus = rightService.updateMenuOrder(menuId, type);

		return SUCCESS;
	}

	public String getMenuId() {
		return menuId;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

	public List<String> getMenuIds() {
		return menuIds;
	}

	public void setMenuIds(List<String> menuIds) {
		this.menuIds = menuIds;
	}

	public String getOperationUserName() {
		return operationUserName;
	}

	public void setOperationUserName(String operationUserName) {
		this.operationUserName = operationUserName;
	}

	public List<String> getRightIds() {
		return rightIds;
	}

	public void setRightIds(List<String> rightIds) {
		this.rightIds = rightIds;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public boolean isTreeOpen() {
		return treeOpen;
	}

	public void setTreeOpen(boolean treeOpen) {
		this.treeOpen = treeOpen;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	public void setRightService(RightService rightService) {
		this.rightService = rightService;
	}

	public List<Menu> getMenus() {
		return menus;
	}

	public void setMenus(List<Menu> menus) {
		this.menus = menus;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
