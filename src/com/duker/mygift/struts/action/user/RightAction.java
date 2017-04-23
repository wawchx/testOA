/*
 * @(#)RightAction.java Jan 24, 2010
 * 
 * ��Ϣ��˹���ϵͳ
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
 * Ȩ�޹���
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
public class RightAction extends PaginatedAction {

	/**
	 * ��ɫ
	 */
	private Role role;

	/**
	 * �û��û���
	 */
	private String userName;

	/**
	 * �����û��û���
	 */
	private String operationUserName;

	/**
	 * �û�����
	 */
	private Integer userType;

	/**
	 * ��ɫid
	 */
	private String roleId;

	/**
	 * �˵�id
	 */
	private String menuId;

	/**
	 * �˵�id�б�
	 */
	private List<String> menuIds;

	/**
	 * Ȩ����id�б�
	 */
	private List<String> rightIds;

	/**
	 * ���Ƿ�Ĭ��չ��
	 */
	private boolean treeOpen = false;

	/**
	 * Ȩ�޷���
	 */
	private RightService rightService;

	/**
	 * �˵�����
	 */
	private List<Menu> menus;

	/**
	 * ���� 0�ö� 1���� 2���� 3�õ�
	 */
	private Integer type;

	/**
	 * ���ҽ�ɫ
	 * 
	 * @return ��ת����ֵ
	 */
	public String findRole() {
		role = rightService.findRole(roleId);

		return SUCCESS;
	}

	/**
	 * ������ɫ��
	 * 
	 * @return ��ת����
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
				sb.append("<xml>��ȡ����������</xml>");
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
	 * ������
	 * 
	 * @param trees
	 *            ������
	 * @param sb
	 *            �ַ���
	 */
	private void getRoleTree(List<Tree<Role>> trees, StringBuilder sb) {
		for (Tree<Role> tree : trees) {
			Role element = tree.getElement();
			String roleId = element.getRoleId();
			String roleName = element.getRoleName();

			// �ڵ��������tip����
			String desc = element.getDescription();
			if (desc == null) {
				desc = "";
			}

			sb.append("<item text='");
			sb.append(roleName);
			sb.append("' id='r_");
			sb.append(roleId);

			// �ڵ��������tip����
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
	 * ��ӽ�ɫ
	 * 
	 * @return ��ת����ֵ
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "��ɫ������Ϊ��", fieldName = "role.roleName", trim = true) })
	public String addRole() {
		role = rightService.addRole(operationUserName, role);

		return SUCCESS;
	}

	/**
	 * ���½�ɫ��Ϣ
	 * 
	 * @return ��ת����ֵ
	 */
	@Validations(requiredStrings = {
			@RequiredStringValidator(message = "��ɫid����Ϊ��", fieldName = "role.roleId", trim = true),
			@RequiredStringValidator(message = "��ɫ������Ϊ��", fieldName = "role.roleName", trim = true) })
	public String updateRole() {
		role = rightService.updateRole(operationUserName, role);

		return SUCCESS;
	}

	/**
	 * ɾ����ɫ
	 * 
	 * @return ��ת����
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "��ɫid����Ϊ��", fieldName = "roleId", trim = true) })
	public String deleteRole() {
		rightService.deleteRole(operationUserName, roleId);

		return SUCCESS;
	}

	/**
	 * ����Ȩ����id���߲˵�id����������Ȩ����Ĳ˵�id
	 * 
	 * @return ��ת����ֵ
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
	 * �����˵���
	 * 
	 * @return ��ת����
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
				sb.append("<xml>��ȡ����������</xml>");
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
	 * ������
	 * 
	 * @param trees
	 *            ������
	 * @param sb
	 *            �ַ���
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
	 * ���¼��û���Ȩ��ɫ
	 * 
	 * @return ��ת����
	 */
	@Validations(requiredStrings = {
			@RequiredStringValidator(message = "����ѡ��Ҫ��Ȩ���û�", fieldName = "userName", trim = true),
			@RequiredStringValidator(message = "����ѡ���ɫ", fieldName = "roleId", trim = true) })
	public String addUserRole() throws Exception {
		rightService.addUserRole(operationUserName, userName, roleId);

		return SUCCESS;
	}

	/**
	 * ȡ���û���Ȩ�Ľ�ɫ
	 * 
	 * @return ��ת����
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "����ѡ��Ҫȡ����Ȩ���û�", fieldName = "userName", trim = true) })
	public String delUserRole() throws Exception {
		rightService.delUserRole(operationUserName, userName);

		return SUCCESS;
	}

	/**
	 * ����ɫ����Ȩ����
	 * 
	 * @return ��ת����
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "����ѡ��Ҫ��Ȩ�Ľ�ɫ", fieldName = "roleId", trim = true) })
	public String addRoleRight() throws Exception {
		rightService.addRoleRight(operationUserName, roleId, rightIds);

		return SUCCESS;
	}

	/**
	 * ���²˵���ʾ˳��
	 * 
	 * @return ��ת����
	 */
	@Validations(requiredFields = {
			@RequiredFieldValidator(message = "����id����Ϊ��", fieldName = "menuId"),
			@RequiredFieldValidator(message = "�������Ͳ���Ϊ��", fieldName = "type") })
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
