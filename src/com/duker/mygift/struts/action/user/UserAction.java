/*
 * @(#)UserAction.java Jan 24, 2010
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.struts.action.user;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;

import com.duker.mygift.common.util.ServletUtil;
import com.duker.mygift.constant.CList;
import com.duker.mygift.constant.DList;
import com.duker.mygift.exception.CommonLogicException;
import com.duker.mygift.model.Menu;
import com.duker.mygift.model.UserInfo;
import com.duker.mygift.service.user.RightService;
import com.duker.mygift.service.user.UserService;
import com.duker.mygift.struts.action.PaginatedAction;
import com.duker.mygift.vo.PagedList;
import com.duker.mygift.vo.SortOrder;
import com.duker.mygift.vo.Tree;
import com.opensymphony.xwork2.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork2.validator.annotations.RegexFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

/**
 * <pre>
 * �û�����
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
public class UserAction extends PaginatedAction {

	/**
	 * �û�
	 */
	private UserInfo user;

	/**
	 * �û��û���
	 */
	private String userName;

	/**
	 * �û��û����б�
	 */
	private List<String> userNames;

	/**
	 * ����
	 */
	private String pwd;

	/**
	 * ������
	 */
	private String oldPwd;

	/**
	 * ��֤��
	 */
	private String securityCode;

	/**
	 * ״̬,0��Ч 1��Ч
	 */
	private Integer state;

	/**
	 * IP ��ַ
	 */
	private String ip;

	/**
	 * �����û� �û���
	 */
	private String operationUserName;

	/**
	 * �û�����
	 */
	private UserService userService;

	/**
	 * Ȩ�޷���
	 */
	private RightService rightService;

	/**
	 * ��¼��֤
	 * 
	 * @return ��ת����ֵ
	 */
	@Validations(requiredStrings = {
			@RequiredStringValidator(message = "�û�������Ϊ��", fieldName = "userName", trim = true),
			@RequiredStringValidator(message = "���벻��Ϊ��", fieldName = "pwd", trim = true),
			@RequiredStringValidator(message = "��֤�벻��Ϊ��", fieldName = "securityCode", trim = true) })
	public String login() throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpSession session = request.getSession();
		// session�б������֤��
		String rv = ServletUtil.getValue(session,
				CList.User.Session.SECURITY_CODE, String.class);

		session.removeAttribute(CList.User.Session.SECURITY_CODE);
		if (rv == null) {
			throw new CommonLogicException("��֤��ʧЧ������������֤�롣");
		}

		if (!rv.equalsIgnoreCase(securityCode)) {
			throw new CommonLogicException("��֤�����");
		}

		// ��ʼ��¼��֤
		String ip = ServletUtil.getRealIp(request);
		UserInfo a = userService.findUser(userName);
		if (a == null) {
			throw new CommonLogicException("��¼ʧ��,�û������������");
		}
		if (DList.UserInfo.State.LOCKED.equals(a.getState())) {
			throw new CommonLogicException("���˻����ڳ���δʹ�ö����������������ϼ��û���ϵ��");
		}
		if (!DList.UserInfo.State.VALID.equals(a.getState())) {
			throw new CommonLogicException("���˻��ѱ����ã��������ϼ��û���ϵ��");
		}

		boolean ret = userService.login(userName, pwd, ip);

		if (!ret) {
			throw new CommonLogicException("��¼ʧ��,�û������������");
		}

		UserInfo user = new UserInfo();
		user.setUsername(a.getUsername());
		user.setPassword(a.getPassword());
		user.setNickname(a.getNickname());
		user.setState(a.getState());

		// ����session
		session.setAttribute(CList.User.Session.USER_NAME, userName);
		session.setAttribute(CList.User.Session.USER_INFO, user);

		// ���ɲ˵�
		List<String> urls = rightService.findUrls(userName);
		session.setAttribute(CList.User.Session.URLS, urls);
		Tree<Menu> rootMenu;
		List<Tree<Menu>> menuList = rightService.findMenus(userName);
		if (menuList == null) {
			rootMenu = null;
		}
		int size = menuList.size();
		if (size > 15) {
			Menu otherMenu = new Menu();
			otherMenu.setMenuId("-999");
			otherMenu.setMenuName("��������");
			List<Tree<Menu>> children = new ArrayList<Tree<Menu>>(
					menuList.subList(15, size));
			Tree<Menu> otherTree = new Tree<Menu>(otherMenu, null, children);
			menuList = new ArrayList<Tree<Menu>>(menuList.subList(0, 15));
			menuList.add(otherTree);
		}

		rootMenu = new Tree<Menu>(null, null, menuList);

		session.setAttribute(CList.User.Session.MENU, rootMenu);

		// ���ɲ˵�����
		Map<String, String> navigation = rightService.findNavigation(userName);
		session.setAttribute(CList.User.Session.NAVIGATION, navigation);

		return SUCCESS;
	}

	/**
	 * �˳���¼
	 * 
	 * @return ��ת����ֵ
	 */
	public String logout() throws Exception {
		// ���session
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpSession session = request.getSession();

		session.removeAttribute(CList.User.Session.USER_NAME);
		session.removeAttribute(CList.User.Session.USER_INFO);
		session.removeAttribute(CList.User.Session.URLS);
		session.removeAttribute(CList.User.Session.MENU);

		return SUCCESS;
	}

	/**
	 * �����û���Ϣ
	 * 
	 * @return ��ת����ֵ
	 */
	public String findUser() {
		user = userService.findUser(userName);

		return SUCCESS;
	}

	/**
	 * �����û��б�
	 * 
	 * @return ��ת����ֵ
	 */
	public String findUsers() {
		List<SortOrder> orders = new LinkedList<SortOrder>();
		if (StringUtils.isBlank(sort)) {
			orders.add(SortOrder.asc("username"));
		}
		else {
			orders.add(new SortOrder(sort, asc));
		}
		PagedList<UserInfo> pList = userService.findUsers(operationUserName,
				user, pageNo - 1, pageSize, orders);
		paginate(pList);

		return SUCCESS;
	}

	/**
	 * ����û�
	 * 
	 * @return ��ת����ֵ
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "�û�������Ϊ��", fieldName = "user.username", trim = true) }, stringLengthFields = {
			@StringLengthFieldValidator(message = "�û���������3��20λ", fieldName = "user.username", minLength = "3", maxLength = "20"),
			@StringLengthFieldValidator(message = "���������6��20λ", fieldName = "user.pwd", minLength = "6", maxLength = "20") }, regexFields = { @RegexFieldValidator(message = "���������ASCII�ַ�", fieldName = "user.pwd", regex = "[\\x00-\\xFF]+") })
	public String addUser() {
		pwd = user.getPassword();
		if (StringUtils.isBlank(pwd)) {
			pwd = userService.randomPwd(6);
			user.setPassword(pwd);
		}
		if (StringUtils.isBlank(operationUserName)) {
			operationUserName = "admin";
		}
		user = userService.addUser(operationUserName, user);

		userName = user.getUsername();
		StringBuilder sb = new StringBuilder();
		sb.append("����û�");
		sb.append(userName);
		sb.append("�ɹ������μ����룺");
		sb.append(pwd);
		addActionMessage(sb.toString());

		return RESULTINFO;
	}

	/**
	 * �޸�����
	 * 
	 * @return ��ת����ֵ
	 */
	@Validations(requiredStrings = {
			@RequiredStringValidator(message = "�û�������Ϊ��", fieldName = "userName", trim = true),
			@RequiredStringValidator(message = "�����벻��Ϊ��", fieldName = "oldPwd", trim = true),
			@RequiredStringValidator(message = "���벻��Ϊ��", fieldName = "pwd", trim = true) }, stringLengthFields = { @StringLengthFieldValidator(message = "���������6��20λ", fieldName = "pwd", minLength = "6", maxLength = "20") }, regexFields = { @RegexFieldValidator(message = "���������ASCII�ַ�", fieldName = "pwd", regex = "[\\x00-\\xFF]+") })
	public String modifyPwd() throws Exception {
		user = userService.modifyPwd(userName, oldPwd, pwd);
		addActionMessage("�޸�����ɹ�,�����µ�¼");
		logout();

		return RESULTINFO;
	}

	/**
	 * ��������
	 * 
	 * @return ��ת����ֵ
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "�û�������Ϊ��", fieldName = "userName", trim = true) })
	public String resetPwd() {
		pwd = userService.randomPwd(6);
		user = userService.restPwd(operationUserName, userName, pwd);

		StringBuilder sb = new StringBuilder();

		sb.append("�û�");
		sb.append(userName);
		sb.append("���뱻����Ϊ��");
		sb.append(pwd);
		sb.append("�����μ�");
		addActionMessage(sb.toString());

		return RESULTINFO;
	}

	/**
	 * �����û���Ϣ
	 * 
	 * @return ��ת����ֵ
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "�û�������Ϊ��", fieldName = "user.username", trim = true) })
	public String updateUser() {
		user = userService.updateUser(operationUserName, user);
		addActionMessage("�޸��û���Ϣ�ɹ�");

		return RESULTINFO;
	}

	/**
	 * �����û���Ϣ
	 * 
	 * @return ��ת����ֵ
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "�û�������Ϊ��", fieldName = "user.username", trim = true) })
	public String updateUserInfo() {
		user = userService.updateUser(user);
		addActionMessage("�޸��û���Ϣ�ɹ�");

		return RESULTINFO;
	}

	/**
	 * �����û�״̬
	 * 
	 * @return ��ת����
	 */
	@Validations(fieldExpressions = { @FieldExpressionValidator(message = "�û��û�������Ϊ��", fieldName = "userName", expression = "userName != null || userNames != null") }, requiredFields = { @RequiredFieldValidator(message = "״̬����Ϊ��", fieldName = "state") })
	public String changeUserState() {
		if (userNames != null && !userNames.isEmpty()) {
			userService.updateState(operationUserName, userNames, state);
			StringBuilder sb = new StringBuilder();
			String seperator = "";
			for (String name : userNames) {
				sb.append(seperator);
				sb.append(name);
				seperator = ",";
			}
		}
		else if (StringUtils.isNotBlank(userName)) {
			userService.updateState(operationUserName, userName, state);
		}

		return SUCCESS;
	}

	/**
	 * ɾ���û�
	 * 
	 * @return ��ת����
	 */
	@Validations(requiredFields = { @RequiredFieldValidator(message = "�û��û�������Ϊ��", fieldName = "userName") })
	public String deleteUser() {
		userService.deleteUser(operationUserName, userName);

		return SUCCESS;
	}

	/**
	 * ��ȡ���û����б�
	 * 
	 * @return ��ת����ֵ
	 */
	public String findSubUsers() {
		PagedList<UserInfo> pList = userService.findUsers(operationUserName,
				null, pageNo - 1, 200, null);
		if (pList == null) {
			retCode = 1;
			retMsg = "û�пɹ�������û�";
			return ERROR;
		}
		List<UserInfo> users = pList.getList();
		userNames = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		for (UserInfo user : users) {
			sb.append(user.getUsername());
			userNames.add(sb.toString());
			sb.setLength(0);
		}
		return SUCCESS;
	}

	public UserInfo getUser() {
		return user;
	}

	public void setUser(UserInfo user) {
		this.user = user;
	}

	public String getOldPwd() {
		return oldPwd;
	}

	public void setOldPwd(String oldPwd) {
		this.oldPwd = oldPwd;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getSecurityCode() {
		return securityCode;
	}

	public void setSecurityCode(String securityCode) {
		this.securityCode = securityCode;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<String> getUserNames() {
		return userNames;
	}

	public void setUserNames(List<String> userNames) {
		this.userNames = userNames;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getOperationUserName() {
		return operationUserName;
	}

	public void setOperationUserName(String operationUserName) {
		this.operationUserName = operationUserName;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setRightService(RightService rightService) {
		this.rightService = rightService;
	}

}
