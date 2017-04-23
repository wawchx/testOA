/*
 * @(#)UserAction.java Jan 24, 2010
 * 
 * 信息审核管理系统
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
 * 用户管理
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
public class UserAction extends PaginatedAction {

	/**
	 * 用户
	 */
	private UserInfo user;

	/**
	 * 用户用户名
	 */
	private String userName;

	/**
	 * 用户用户名列表
	 */
	private List<String> userNames;

	/**
	 * 密码
	 */
	private String pwd;

	/**
	 * 旧密码
	 */
	private String oldPwd;

	/**
	 * 验证码
	 */
	private String securityCode;

	/**
	 * 状态,0有效 1无效
	 */
	private Integer state;

	/**
	 * IP 地址
	 */
	private String ip;

	/**
	 * 操作用户 用户名
	 */
	private String operationUserName;

	/**
	 * 用户服务
	 */
	private UserService userService;

	/**
	 * 权限服务
	 */
	private RightService rightService;

	/**
	 * 登录验证
	 * 
	 * @return 跳转控制值
	 */
	@Validations(requiredStrings = {
			@RequiredStringValidator(message = "用户名不能为空", fieldName = "userName", trim = true),
			@RequiredStringValidator(message = "密码不能为空", fieldName = "pwd", trim = true),
			@RequiredStringValidator(message = "验证码不能为空", fieldName = "securityCode", trim = true) })
	public String login() throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpSession session = request.getSession();
		// session中保存的验证码
		String rv = ServletUtil.getValue(session,
				CList.User.Session.SECURITY_CODE, String.class);

		session.removeAttribute(CList.User.Session.SECURITY_CODE);
		if (rv == null) {
			throw new CommonLogicException("验证码失效，请点击更换验证码。");
		}

		if (!rv.equalsIgnoreCase(securityCode)) {
			throw new CommonLogicException("验证码错误");
		}

		// 开始登录验证
		String ip = ServletUtil.getRealIp(request);
		UserInfo a = userService.findUser(userName);
		if (a == null) {
			throw new CommonLogicException("登录失败,用户名或密码错误");
		}
		if (DList.UserInfo.State.LOCKED.equals(a.getState())) {
			throw new CommonLogicException("您账户由于长期未使用而被锁定，请与您上级用户联系！");
		}
		if (!DList.UserInfo.State.VALID.equals(a.getState())) {
			throw new CommonLogicException("您账户已被禁用，请与您上级用户联系！");
		}

		boolean ret = userService.login(userName, pwd, ip);

		if (!ret) {
			throw new CommonLogicException("登录失败,用户名或密码错误");
		}

		UserInfo user = new UserInfo();
		user.setUsername(a.getUsername());
		user.setPassword(a.getPassword());
		user.setNickname(a.getNickname());
		user.setState(a.getState());

		// 生成session
		session.setAttribute(CList.User.Session.USER_NAME, userName);
		session.setAttribute(CList.User.Session.USER_INFO, user);

		// 生成菜单
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
			otherMenu.setMenuName("其他管理");
			List<Tree<Menu>> children = new ArrayList<Tree<Menu>>(
					menuList.subList(15, size));
			Tree<Menu> otherTree = new Tree<Menu>(otherMenu, null, children);
			menuList = new ArrayList<Tree<Menu>>(menuList.subList(0, 15));
			menuList.add(otherTree);
		}

		rootMenu = new Tree<Menu>(null, null, menuList);

		session.setAttribute(CList.User.Session.MENU, rootMenu);

		// 生成菜单导航
		Map<String, String> navigation = rightService.findNavigation(userName);
		session.setAttribute(CList.User.Session.NAVIGATION, navigation);

		return SUCCESS;
	}

	/**
	 * 退出登录
	 * 
	 * @return 跳转控制值
	 */
	public String logout() throws Exception {
		// 清空session
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpSession session = request.getSession();

		session.removeAttribute(CList.User.Session.USER_NAME);
		session.removeAttribute(CList.User.Session.USER_INFO);
		session.removeAttribute(CList.User.Session.URLS);
		session.removeAttribute(CList.User.Session.MENU);

		return SUCCESS;
	}

	/**
	 * 查找用户信息
	 * 
	 * @return 跳转控制值
	 */
	public String findUser() {
		user = userService.findUser(userName);

		return SUCCESS;
	}

	/**
	 * 查找用户列表
	 * 
	 * @return 跳转控制值
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
	 * 添加用户
	 * 
	 * @return 跳转控制值
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "用户名不能为空", fieldName = "user.username", trim = true) }, stringLengthFields = {
			@StringLengthFieldValidator(message = "用户名必须在3到20位", fieldName = "user.username", minLength = "3", maxLength = "20"),
			@StringLengthFieldValidator(message = "密码必须在6到20位", fieldName = "user.pwd", minLength = "6", maxLength = "20") }, regexFields = { @RegexFieldValidator(message = "密码必须是ASCII字符", fieldName = "user.pwd", regex = "[\\x00-\\xFF]+") })
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
		sb.append("添加用户");
		sb.append(userName);
		sb.append("成功，请牢记密码：");
		sb.append(pwd);
		addActionMessage(sb.toString());

		return RESULTINFO;
	}

	/**
	 * 修改密码
	 * 
	 * @return 跳转控制值
	 */
	@Validations(requiredStrings = {
			@RequiredStringValidator(message = "用户名不能为空", fieldName = "userName", trim = true),
			@RequiredStringValidator(message = "旧密码不能为空", fieldName = "oldPwd", trim = true),
			@RequiredStringValidator(message = "密码不能为空", fieldName = "pwd", trim = true) }, stringLengthFields = { @StringLengthFieldValidator(message = "密码必须在6到20位", fieldName = "pwd", minLength = "6", maxLength = "20") }, regexFields = { @RegexFieldValidator(message = "密码必须是ASCII字符", fieldName = "pwd", regex = "[\\x00-\\xFF]+") })
	public String modifyPwd() throws Exception {
		user = userService.modifyPwd(userName, oldPwd, pwd);
		addActionMessage("修改密码成功,请重新登录");
		logout();

		return RESULTINFO;
	}

	/**
	 * 重置密码
	 * 
	 * @return 跳转控制值
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "用户名不能为空", fieldName = "userName", trim = true) })
	public String resetPwd() {
		pwd = userService.randomPwd(6);
		user = userService.restPwd(operationUserName, userName, pwd);

		StringBuilder sb = new StringBuilder();

		sb.append("用户");
		sb.append(userName);
		sb.append("密码被重置为：");
		sb.append(pwd);
		sb.append("，请牢记");
		addActionMessage(sb.toString());

		return RESULTINFO;
	}

	/**
	 * 更新用户信息
	 * 
	 * @return 跳转控制值
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "用户名不能为空", fieldName = "user.username", trim = true) })
	public String updateUser() {
		user = userService.updateUser(operationUserName, user);
		addActionMessage("修改用户信息成功");

		return RESULTINFO;
	}

	/**
	 * 更新用户信息
	 * 
	 * @return 跳转控制值
	 */
	@Validations(requiredStrings = { @RequiredStringValidator(message = "用户名不能为空", fieldName = "user.username", trim = true) })
	public String updateUserInfo() {
		user = userService.updateUser(user);
		addActionMessage("修改用户信息成功");

		return RESULTINFO;
	}

	/**
	 * 更改用户状态
	 * 
	 * @return 跳转控制
	 */
	@Validations(fieldExpressions = { @FieldExpressionValidator(message = "用户用户名不能为空", fieldName = "userName", expression = "userName != null || userNames != null") }, requiredFields = { @RequiredFieldValidator(message = "状态不能为空", fieldName = "state") })
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
	 * 删除用户
	 * 
	 * @return 跳转控制
	 */
	@Validations(requiredFields = { @RequiredFieldValidator(message = "用户用户名不能为空", fieldName = "userName") })
	public String deleteUser() {
		userService.deleteUser(operationUserName, userName);

		return SUCCESS;
	}

	/**
	 * 获取子用户名列表
	 * 
	 * @return 跳转控制值
	 */
	public String findSubUsers() {
		PagedList<UserInfo> pList = userService.findUsers(operationUserName,
				null, pageNo - 1, 200, null);
		if (pList == null) {
			retCode = 1;
			retMsg = "没有可管理的子用户";
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
