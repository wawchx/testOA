/*
 * @(#)UserServiceImpl.java 2010-1-15
 * 
 * ��Ϣ��˹���ϵͳ
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
 * �û�����ӿ�ʵ��,�û��������ִ�Сд
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2010-1-15
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class UserServiceImpl implements UserService {

	/**
	 * Hibernateͨ��dao
	 */
	private GenericHibernateDao dao;

	/**
	 * Ȩ�޹���ӿ�
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
			throw new CommonLogicException("�û��û����Ѿ�����");
		}
		Role role = user.getRole();
		if (role == null || StringUtils.isBlank(role.getRoleId())) {
			throw new CommonLogicException("��û�и��û�ָ����ɫ�����û�н�ɫ���ȴ�����ɫ");
		}
		if (!"admin".equals(operationUserName)) {
			// ������Ȩ�û���ӵ�еĽ�ɫ
			List<String> roleIds = rightService.findRoleIds(operationUserName);
			// �����Ȩ�û��Ƿ��в������û���Ȩ��
			if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
				throw new CommonLogicException("��û��Ȩ�޸��û�ָ���ý�ɫ");
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
			throw new CommonLogicException("�Ҳ���Ҫ�޸ĵ��û�");
		}
		Role role = a.getRole();
		if (role == null || StringUtils.isBlank(role.getRoleId())) {
			throw new CommonLogicException("��û��Ȩ���޸ĸ��û�������");
		}
		if (!"admin".equals(operationUserName)) {
			// ������Ȩ�û���ӵ�еĽ�ɫ
			List<String> roleIds = rightService.findRoleIds(operationUserName);
			// �����Ȩ�û��Ƿ��в������û���Ȩ��
			if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
				throw new CommonLogicException("��û��Ȩ���޸ĸ��û�������");
			}

			role = user.getRole();
			if (role != null) {
				// �����Ȩ�û��Ƿ��в����ý�ɫ��Ȩ��
				if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
					throw new CommonLogicException("��û��Ȩ�޽��û�ָ��Ϊ�ý�ɫ");
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
			throw new CommonLogicException("�Ҳ���Ҫ�޸ĵ��û�");
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
			throw new CommonLogicException("�������������벻����ͬ");
		}
		UserInfo a = dao.findById(UserInfo.class, userName);
		if (a == null) {
			throw new CommonLogicException("�û��������벻��ȷ");
		}
		String pwdKey = a.getPassword();
		if (!oldPwd.equals(pwdKey)) {
			throw new CommonLogicException("����ľ����벻��ȷ");
		}
		a.setPassword(newPwd);

		dao.update(a);

		return a;
	}

	/**
	 * �����û����������û������Ƿ���Ȩ��
	 * 
	 * @param operationUserName
	 *            �����û�,�����û�����
	 * @param userName
	 *            �û�
	 * @return �û��������Ϊ�ձ�ʾû��Ȩ��
	 */
	private UserInfo findUser(String operationUserName, String userName) {
		UserInfo a = dao.findById(UserInfo.class, userName);
		if (a == null) {
			throw new CommonLogicException("�û�������");
		}

		if (!"admin".equals(operationUserName)) {
			Role role = a.getRole();
			if (role == null) {
				return null;
			}
			// ������Ȩ�û���ӵ�еĽ�ɫ
			List<String> roleIds = rightService.findRoleIds(operationUserName);
			// �����Ȩ�û��Ƿ��в������û���Ȩ��
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
			throw new CommonLogicException("��û��Ȩ�����ø��û�������");
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
			throw new CommonLogicException("Ҫ�޸ĵ�״̬������");
		}

		UserInfo a = findUser(operationUserName, userName);
		if (a == null) {
			throw new CommonLogicException("��û��Ȩ���޸ĸ��û���״̬");
		}

		if (!"admin".equals(operationUserName)) {
			Role role = a.getRole();
			if (role == null) {
				throw new CommonLogicException("��û��Ȩ���޸ĸ��û���״̬");
			}
			// ������Ȩ�û���ӵ�еĽ�ɫ
			List<String> roleIds = rightService.findRoleIds(operationUserName);
			// �����Ȩ�û��Ƿ��в������û���Ȩ��
			if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
				throw new CommonLogicException("��û��Ȩ���޸ĸ��û���״̬");
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
			throw new CommonLogicException("Ҫ�޸ĵ�״̬������");
		}

		List<String> roleIds = null;
		if (!"admin".equals(operationUserName)) {
			// ������Ȩ�û���ӵ�еĽ�ɫ
			roleIds = rightService.findRoleIds(operationUserName);
		}

		for (String userName : userNames) {
			UserInfo a = dao.findById(UserInfo.class, userName);

			if (a == null) {
				throw new CommonLogicException("�û�������");
			}

			if (roleIds != null) {
				Role role = a.getRole();
				if (role == null) {
					throw new CommonLogicException("��û��Ȩ���޸ĸ��û���״̬");
				}
				// �����Ȩ�û��Ƿ��в������û���Ȩ��
				if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
					throw new CommonLogicException("��û��Ȩ���޸ĸ��û���״̬");
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
			throw new CommonLogicException("�û�������");
		}

		if ("admin".equals(userName)) {
			throw new CommonLogicException("�����û�����ɾ��");
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
				throw new CommonLogicException("�û�������");
			}
			if ("admin".equals(userName)) {
				throw new CommonLogicException("�����û�����ɾ��");
			}
			Role role = user.getRole();
			if (role == null) {
				throw new CommonLogicException("��û��Ȩ��ɾ�����û�");
			}
			// ������Ȩ�û���ӵ�еĽ�ɫ
			List<String> roleIds = rightService.findRoleIds(operationUserName);
			// �����Ȩ�û��Ƿ��в������û���Ȩ��
			if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
				throw new CommonLogicException("��û��Ȩ��ɾ�����û�");
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
