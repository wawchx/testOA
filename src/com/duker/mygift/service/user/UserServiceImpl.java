/*
 * @(#)UserServiceImpl.java 2010-1-15
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.service.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Restrictions;

import com.duker.mygift.constant.DList;
import com.duker.mygift.dao.GenericHibernateDao;
import com.duker.mygift.exception.CommonLogicException;
import com.duker.mygift.model.Role;
import com.duker.mygift.model.UserInfo;
import com.duker.mygift.vo.CriteriaProperty;
import com.duker.mygift.vo.PagedList;
import com.duker.mygift.vo.SortOrder;

/**
 * <pre>
 * 用户服务接口实现,用户名不区分大小写
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2010-1-15
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class UserServiceImpl implements UserService {

	/**
	 * Hibernate通用dao
	 */
	private GenericHibernateDao dao;

	/**
	 * 权限管理接口
	 */
	private RightService rightService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.UserService#userExist(java.lang.String)
	 */
	@Override
	public boolean userExist(String userName) {
		userName = userName.toLowerCase().trim();
		Long count = dao.uniqueResult(
				"select count(*) from UserInfo where userName=?", userName);

		return count != null && count > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.duker.mygift.service.user.UserService#addUser(java.lang.String,
	 * com.duker.mygift.model.UserInfo)
	 */
	@Override
	public UserInfo addUser(String operationUserName, UserInfo user)
			throws CommonLogicException {
		String userName = user.getUsername();
		userName = userName.toLowerCase().trim();
		UserInfo a = dao.findById(UserInfo.class, userName);
		if (a != null) {
			throw new CommonLogicException("用户用户名已经存在");
		}
		Role role = user.getRole();
		if (role == null || StringUtils.isBlank(role.getRoleId())) {
			throw new CommonLogicException("您没有给用户指定角色，如果没有角色请先创建角色");
		}
		if (!"admin".equals(operationUserName)) {
			// 查找授权用户所拥有的角色
			List<String> roleIds = rightService.findRoleIds(operationUserName);
			// 检查授权用户是否有操作该用户的权限
			if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
				throw new CommonLogicException("您没有权限给用户指定该角色");
			}
		}
		user.setUsername(userName);
		user.setPassword(user.getPassword());

		user.setState(DList.UserInfo.State.VALID);
		dao.save(user);

		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.UserService#updateUser(java.lang.String,
	 * com.duker.mygift.model.UserInfo)
	 */
	@Override
	public UserInfo updateUser(String operationUserName, UserInfo user)
			throws CommonLogicException {
		String userName = user.getUsername();
		UserInfo a = dao.findById(UserInfo.class, userName);
		if (a == null) {
			throw new CommonLogicException("找不到要修改的用户");
		}
		Role role = a.getRole();
		if (role == null || StringUtils.isBlank(role.getRoleId())) {
			throw new CommonLogicException("您没有权限修改该用户的资料");
		}
		if (!"admin".equals(operationUserName)) {
			// 查找授权用户所拥有的角色
			List<String> roleIds = rightService.findRoleIds(operationUserName);
			// 检查授权用户是否有操作该用户的权限
			if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
				throw new CommonLogicException("您没有权限修改该用户的资料");
			}

			role = user.getRole();
			if (role != null) {
				// 检查授权用户是否有操作该角色的权限
				if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
					throw new CommonLogicException("您没有权限将用户指定为该角色");
				}
			}
		}

		a.setUsername(userName);
		a.setPassword(user.getPassword());
		a.setRole(user.getRole());

		dao.update(a);

		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.UserService#updateUser(com.duker.mygift
	 * .model.UserInfo)
	 */
	@Override
	public UserInfo updateUser(UserInfo user) {
		String userName = user.getUsername();
		UserInfo a = dao.findById(UserInfo.class, userName);
		if (a == null) {
			throw new CommonLogicException("找不到要修改的用户");
		}
		a.setUsername(userName);
		a.setPassword(user.getPassword());
		dao.update(a);

		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.UserService#modifyPwd(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public UserInfo modifyPwd(String userName, String oldPwd, String newPwd)
			throws CommonLogicException {
		if (StringUtils.equals(oldPwd, newPwd)) {
			throw new CommonLogicException("旧密码与新密码不能相同");
		}
		UserInfo a = dao.findById(UserInfo.class, userName);
		if (a == null) {
			throw new CommonLogicException("用户名或密码不正确");
		}
		String pwdKey = a.getPassword();
		if (!oldPwd.equals(pwdKey)) {
			throw new CommonLogicException("输入的旧密码不正确");
		}
		a.setPassword(newPwd);

		dao.update(a);

		return a;
	}

	/**
	 * 查找用户并检查操作用户对其是否有权限
	 * 
	 * @param operationUserName
	 *            操作用户,超级用户传空
	 * @param userName
	 *            用户
	 * @return 用户对象，如果为空表示没有权限
	 */
	private UserInfo findUser(String operationUserName, String userName) {
		UserInfo a = dao.findById(UserInfo.class, userName);
		if (a == null) {
			throw new CommonLogicException("用户不存在");
		}

		if (!"admin".equals(operationUserName)) {
			Role role = a.getRole();
			if (role == null) {
				return null;
			}
			// 查找授权用户所拥有的角色
			List<String> roleIds = rightService.findRoleIds(operationUserName);
			// 检查授权用户是否有操作该用户的权限
			if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
				return null;
			}
		}

		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.duker.mygift.service.user.UserService#restPwd(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public UserInfo restPwd(String operationUserName, String userName,
			String newPwd) throws CommonLogicException {
		UserInfo a = findUser(operationUserName, userName);
		if (a == null) {
			throw new CommonLogicException("您没有权限重置该用户的密码");
		}

		a.setPassword(newPwd);

		dao.update(a);

		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.duker.mygift.service.user.UserService#login(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public boolean login(String userName, String pwd, String loginIp) {
		userName = userName.toLowerCase().trim();
		Conjunction c = Restrictions.conjunction();
		c.add(Restrictions.idEq(userName));
		c.add(Restrictions.eq("state", DList.UserInfo.State.VALID));
		List<CriteriaProperty> properties = new ArrayList<CriteriaProperty>(2);
		properties.add(new CriteriaProperty("role", FetchMode.JOIN));
		UserInfo a = dao.uniqueResultByCriterion(UserInfo.class, c, properties);
		if (a == null) {
			return false;
		}

		if (!pwd.equals(a.getPassword())) {
			return false;
		}

		dao.update(a);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.UserService#updateState(java.lang.String,
	 * java.lang.String, java.lang.Integer)
	 */
	@Override
	public void updateState(String operationUserName, String userName,
			Integer state) throws CommonLogicException {
		if (!DList.UserInfo.State.VALID.equals(state)
				&& !DList.UserInfo.State.INVALID.equals(state)) {
			throw new CommonLogicException("要修改的状态不存在");
		}

		UserInfo a = findUser(operationUserName, userName);
		if (a == null) {
			throw new CommonLogicException("您没有权限修改该用户的状态");
		}

		if (!"admin".equals(operationUserName)) {
			Role role = a.getRole();
			if (role == null) {
				throw new CommonLogicException("您没有权限修改该用户的状态");
			}
			// 查找授权用户所拥有的角色
			List<String> roleIds = rightService.findRoleIds(operationUserName);
			// 检查授权用户是否有操作该用户的权限
			if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
				throw new CommonLogicException("您没有权限修改该用户的状态");
			}
		}
		a.setState(state);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.UserService#updateState(java.lang.String,
	 * java.util.List, java.lang.Integer)
	 */
	@Override
	public void updateState(String operationUserName, List<String> userNames,
			Integer state) throws CommonLogicException {
		if (!DList.UserInfo.State.VALID.equals(state)
				&& !DList.UserInfo.State.INVALID.equals(state)) {
			throw new CommonLogicException("要修改的状态不存在");
		}

		List<String> roleIds = null;
		if (!"admin".equals(operationUserName)) {
			// 查找授权用户所拥有的角色
			roleIds = rightService.findRoleIds(operationUserName);
		}

		for (String userName : userNames) {
			UserInfo a = dao.findById(UserInfo.class, userName);

			if (a == null) {
				throw new CommonLogicException("用户不存在");
			}

			if (roleIds != null) {
				Role role = a.getRole();
				if (role == null) {
					throw new CommonLogicException("您没有权限修改该用户的状态");
				}
				// 检查授权用户是否有操作该用户的权限
				if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
					throw new CommonLogicException("您没有权限修改该用户的状态");
				}
			}
			a.setState(state);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.duker.mygift.service.user.UserService#findUser(java.lang.String)
	 */
	@Override
	public UserInfo findUser(String userName) {
		return dao.findById(UserInfo.class, userName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.UserService#findUsers(java.lang.String,
	 * com.duker.mygift.model.UserInfo, int, int, java.util.List)
	 */
	@Override
	public PagedList<UserInfo> findUsers(String operationUserName,
			UserInfo condition, int pageNo, int pageSize, List<SortOrder> orders) {
		List<Object> values = new LinkedList<Object>();
		StringBuilder hql = new StringBuilder("from UserInfo a");
		String separator = " where ";
		if (condition != null) {
			String userName = condition.getUsername();
			if (StringUtils.isNotBlank(userName)) {
				userName = userName.trim();
				hql.append(separator);
				hql.append("a.username like ?");
				values.add("%" + userName.toLowerCase().trim() + "%");
				separator = " and ";
			}

			String nickName = condition.getNickname();
			if (StringUtils.isNotBlank(nickName)) {
				userName = userName.trim();
				hql.append(separator);
				hql.append("a.nickname like ?");
				values.add("%" + nickName.toLowerCase().trim() + "%");
				separator = " and ";
			}

			Integer state = condition.getState();
			if (state != null && state != 0) {
				hql.append(separator);
				hql.append("a.state = ?");
				values.add(state);
				separator = " and ";
			}
		}

		if (orders != null) {
			for (SortOrder o : orders) {
				o.setPrefix("a.");
			}
		}

		return dao.page(hql.toString(), pageNo, pageSize, orders,
				values.toArray());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.UserService#deleteUser(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void deleteUser(String operationUserName, String userName) {
		UserInfo user = dao.findById(UserInfo.class, userName);
		if (user == null) {
			throw new CommonLogicException("用户不存在");
		}

		if ("admin".equals(userName)) {
			throw new CommonLogicException("超级用户不能删除");
		}
		dao.delete(user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.UserService#deleteUsers(java.lang.String,
	 * java.util.List)
	 */
	@Override
	public void deleteUsers(String operationUserName, List<String> userNames) {
		for (String userName : userNames) {
			UserInfo user = dao.findById(UserInfo.class, userName);
			if (user == null) {
				throw new CommonLogicException("用户不存在");
			}
			if ("admin".equals(userName)) {
				throw new CommonLogicException("超级用户不能删除");
			}
			Role role = user.getRole();
			if (role == null) {
				throw new CommonLogicException("您没有权限删除该用户");
			}
			// 查找授权用户所拥有的角色
			List<String> roleIds = rightService.findRoleIds(operationUserName);
			// 检查授权用户是否有操作该用户的权限
			if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
				throw new CommonLogicException("您没有权限删除该用户");
			}
			dao.delete(user);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.duker.mygift.service.user.UserService#randomPwd(int)
	 */
	@Override
	public String randomPwd(int length) {
		char[] chars = { '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
				'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q',
				'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C',
				'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q',
				'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
		return RandomStringUtils.random(length, chars);
	}

	public void setDao(GenericHibernateDao dao) {
		this.dao = dao;
	}

	public void setRightService(RightService rightService) {
		this.rightService = rightService;
	}

}
