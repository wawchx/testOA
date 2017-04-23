/*
 * @(#)RightServiceImpl.java 2010-1-15
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.service.user;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.duker.mygift.constant.DList;
import com.duker.mygift.dao.GenericHibernateDao;
import com.duker.mygift.exception.CommonLogicException;
import com.duker.mygift.model.Menu;
import com.duker.mygift.model.RightGroup;
import com.duker.mygift.model.Role;
import com.duker.mygift.model.UserInfo;
import com.duker.mygift.vo.Tree;

/**
 * <pre>
 * Ȩ�޹���ӿ�ʵ��
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
public class RightServiceImpl implements RightService {

	/**
	 * Hibernateͨ��dao
	 */
	private GenericHibernateDao dao;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#roleExist(java.lang.String)
	 */
	@Override
	public boolean roleExist(String roleName) {
		Long count = dao.uniqueResult(
				"select count(*) from Role where roleName=?", roleName);

		return count != null && count > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.duker.mygift.service.user.RightService#addRole(java.lang.String,
	 * com.duker.mygift.model.Role)
	 */
	@Override
	public Role addRole(String userName, Role role) throws CommonLogicException {
		if (roleExist(role.getRoleName().trim())) {
			throw new CommonLogicException("��ɫ�Ѿ�����");
		}

		if (!"admin".equals(userName)) {
			String parentRoleId = role.getParentRoleId();
			if (StringUtils.isBlank(parentRoleId)) {
				throw new CommonLogicException("��û��Ȩ�޴����ý�ɫ");
			}
			// ������Ȩ�û���ӵ�еĽ�ɫ
			List<String> roleIds = findRoleIds(userName, true);
			// �����Ȩ�û��Ƿ��в����ý�ɫ��Ȩ��
			if (Collections.binarySearch(roleIds, parentRoleId) < 0) {
				throw new CommonLogicException("��û��Ȩ�޴����ý�ɫ");
			}
		}

		String maxId = dao.uniqueResult("select max(roleId) from Role");
		if (StringUtils.isBlank(maxId)) {
			maxId = "R000001";
		}
		else {
			Pattern p = Pattern.compile("\\d+");
			Matcher m = p.matcher(maxId);
			if (m.find()) {
				int i = Integer.parseInt(m.group()) + 1;
				maxId = String.format("R%06d", i);
			}
		}

		role.setRoleId(maxId);
		role.setRights(null);
		role.setUsers(null);
		dao.save(role);

		return role;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#updateRole(java.lang.String,
	 * com.duker.mygift.model.Role)
	 */
	@Override
	public Role updateRole(String userName, Role role)
			throws CommonLogicException {
		String roleId = role.getRoleId();
		Role r = dao.findById(Role.class, roleId);
		if (r == null) {
			return null;
		}
		if (!"admin".equals(userName)) {
			// ������Ȩ�û���ӵ�еĽ�ɫ
			List<String> roleIds = findRoleIds(userName);
			// �����Ȩ�û��Ƿ��в����ý�ɫ��Ȩ��
			if (Collections.binarySearch(roleIds, roleId) < 0) {
				throw new CommonLogicException("��û��Ȩ���޸ĸý�ɫ");
			}
		}
		r.setRoleName(role.getRoleName());
		r.setDescription(role.getDescription());
		dao.update(r);

		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#deleteRole(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void deleteRole(String userName, String roleId) {
		Role role = dao.findById(Role.class, roleId);
		if (role == null) {
			return;
		}
		if (!"admin".equals(userName)) {
			// ������Ȩ�û���ӵ�еĽ�ɫ
			List<String> roleIds = findRoleIds(userName);
			// �����Ȩ�û��Ƿ��в����ý�ɫ��Ȩ��
			if (Collections.binarySearch(roleIds, roleId) < 0) {
				throw new CommonLogicException("��û��Ȩ��ɾ���ý�ɫ");
			}
		}
		Set<UserInfo> users = role.getUsers();
		if (users != null && !users.isEmpty()) {
			throw new CommonLogicException("�ý�ɫ���Ѿ��������û�������ɾ��");
		}
		Long count = dao.uniqueResult(
				"select count(*) from Role where parentRoleId=?", roleId);
		if (count != null && count > 0) {
			throw new CommonLogicException("�ý�ɫ���¼���ɫ������ɾ��");
		}
		dao.delete(role);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#deleteRoles(java.lang.String,
	 * java.util.List)
	 */
	@Override
	public void deleteRoles(String userName, List<String> roleIds) {
		for (String roleId : roleIds) {
			deleteRole(userName, roleId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#findRole(java.lang.String)
	 */
	@Override
	public Role findRole(String roleId) {
		return dao.findById(Role.class, roleId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#findRoles(java.lang.String)
	 */
	@Override
	public List<Tree<Role>> findRoles(String userName) {
		List<Role> rs = null;
		if ("admin".equals(userName)) {
			rs = dao.find("from Role order by roleId");
		}
		else {
			String hql = "select r from UserInfo a join a.role r where a.username=? order by r.roleId";
			Role r = dao.uniqueResult(hql, userName);
			if (r == null) {
				return null;
			}
			rs = new LinkedList<Role>();
			rs.add(r);
			Map<String, List<Role>> childrenMap = findAllSubRoleMap();
			findAllSubRole(r, rs, childrenMap);
		}

		Map<String, Tree<Role>> roleIndex = new HashMap<String, Tree<Role>>();
		Tree<Role> parentTree = null;
		Tree<Role> tree = null;
		String parentId = null;
		String roleId = null;

		for (Role r : rs) {
			parentId = r.getParentRoleId();
			roleId = r.getRoleId();
			tree = roleIndex.get(roleId);
			if (parentId == null || "-1".equals(parentId)) {
				if (tree == null) {
					tree = new Tree<Role>(r, null, new LinkedList<Tree<Role>>());
				}
				else {
					tree.setElement(r);
				}
				roleIndex.put(roleId, tree);
			}
			else {
				parentTree = roleIndex.get(parentId);
				if (parentTree == null) {
					parentTree = new Tree<Role>(null, null,
							new LinkedList<Tree<Role>>());
					roleIndex.put(parentId, parentTree);
				}
				if (tree == null) {
					tree = new Tree<Role>(r, parentTree,
							new LinkedList<Tree<Role>>());
					roleIndex.put(roleId, tree);
				}
				else {
					tree.setParent(parentTree);
					tree.setElement(r);
				}
			}
		}

		List<Tree<Role>> rootRoles = new LinkedList<Tree<Role>>();
		Set<Entry<String, Tree<Role>>> entrySet = roleIndex.entrySet();
		for (Entry<String, Tree<Role>> entry : entrySet) {
			Tree<Role> value = entry.getValue();
			Role r = value.getElement();
			if (r == null) {
				return value.getChildren();
			}
			parentId = r.getParentRoleId();
			if (parentId == null || "-1".equals(parentId)
					|| !roleIndex.containsKey(parentId)) {
				rootRoles.add(value);
			}
		}

		return rootRoles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#findRoleIds(java.lang.String)
	 */
	@Override
	public List<String> findRoleIds(String userName) {
		return findRoleIds(userName, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#findRoleIds(java.lang.String,
	 * boolean)
	 */
	@Override
	public List<String> findRoleIds(String userName, boolean containSelf) {
		List<String> roleIds = new LinkedList<String>();
		String hql = "select r.roleId from UserInfo a join a.role r where a.username=?";
		String roleId = dao.uniqueResult(hql, userName);

		if (roleId == null) {
			return roleIds;
		}

		Map<String, List<String>> childrenMap = findAllSubRoleIdMap();
		findAllSubRole(roleId, roleIds, childrenMap);
		if (containSelf) {
			roleIds.add(roleId);
		}
		Collections.sort(roleIds);

		return roleIds;
	}

	/**
	 * ��������ɫ���ӽ�ɫ�б�Ķ�Ӧ��ϵ��
	 * 
	 * @return ��ɫ���ӽ�ɫ�б�
	 */
	private Map<String, List<Role>> findAllSubRoleMap() {
		List<Role> rs = dao.find("from Role order by roleId");
		Map<String, List<Role>> childrenMap = new HashMap<String, List<Role>>();
		for (Role r : rs) {
			String parentId = r.getParentRoleId();
			if (parentId == null) {
				continue;
			}
			List<Role> children = childrenMap.get(parentId);
			if (children == null) {
				children = new LinkedList<Role>();
				childrenMap.put(parentId, children);
			}
			children.add(r);
		}

		return childrenMap;
	}

	/**
	 * ��������ɫid���ӽ�ɫid�б�Ķ�Ӧ��ϵ��
	 * 
	 * @return ��ɫid���ӽ�ɫid�б�
	 */
	private Map<String, List<String>> findAllSubRoleIdMap() {
		String hql = "select new Role(roleId,parentRoleId) from Role order by roleId";
		List<Role> rs = dao.find(hql);
		Map<String, List<String>> childrenMap = new HashMap<String, List<String>>();
		for (Role r : rs) {
			String parentId = r.getParentRoleId();
			if (parentId == null) {
				continue;
			}
			List<String> children = childrenMap.get(parentId);
			if (children == null) {
				children = new LinkedList<String>();
				childrenMap.put(parentId, children);
			}
			children.add(r.getRoleId());
		}

		return childrenMap;
	}

	/**
	 * ���ҽ�ɫ�������ӽ�ɫ(����������)
	 * 
	 * @param role
	 *            ��ɫ
	 * @param roles
	 *            �ӽ�ɫ�б�
	 * @param childrenMap
	 *            ���и���ɫ���ӽ�ɫ�Ĺ�ϵ��
	 */
	private void findAllSubRole(Role role, List<Role> roles,
			Map<String, List<Role>> childrenMap) {
		List<Role> children = childrenMap.get(role.getRoleId());
		if (children == null || children.isEmpty()) {
			return;
		}
		roles.addAll(children);
		for (Role child : children) {
			findAllSubRole(child, roles, childrenMap);
		}
	}

	/**
	 * ���ҽ�ɫid�������ӽ�ɫid(����������)
	 * 
	 * @param roleId
	 *            ��ɫid
	 * @param roleIds
	 *            �ӽ�ɫid�б�
	 * @param childrenMap
	 *            ���и���ɫ���ӽ�ɫ�Ĺ�ϵ��
	 */
	private void findAllSubRole(String roleId, List<String> roleIds,
			Map<String, List<String>> childrenMap) {
		List<String> children = childrenMap.get(roleId);
		if (children == null || children.isEmpty()) {
			return;
		}
		roleIds.addAll(children);
		for (String child : children) {
			findAllSubRole(child, roleIds, childrenMap);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#addUserRole(java.lang.String ,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void addUserRole(String userUserName, String grantUserName,
			String roleId) {
		UserInfo grantUser = dao.findById(UserInfo.class, grantUserName);
		if (grantUser == null) {
			return;
		}

		Role addRole = dao.findById(Role.class, roleId);
		List<String> roleIds = null;
		if (!"admin".equals(userUserName)) {
			// ������Ȩ�û���ӵ�еĽ�ɫ
			roleIds = findRoleIds(userUserName);

			// �����Ȩ�û��Ƿ��в���ԭ�н�ɫ��Ȩ��
			Role role = grantUser.getRole();
			if (role == null) {
				throw new CommonLogicException("��û��Ȩ�޸��û���Ȩ��ɫ");
			}
			if (Collections.binarySearch(roleIds, role.getRoleId()) < 0) {
				throw new CommonLogicException("��û��Ȩ�޸��û���Ȩ��ɫ");
			}

			if (addRole != null) {
				if (StringUtils.isNotBlank(userUserName)) {
					// �����Ȩ�û��Ƿ��в����ý�ɫ��Ȩ��
					if (Collections.binarySearch(roleIds, roleId) < 0) {
						throw new CommonLogicException("��û��Ȩ�޸��û���Ȩ�ý�ɫ");
					}
				}
			}
		}

		grantUser.setRole(addRole);
		dao.update(grantUser);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#delUserRole(java.lang.String ,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void delUserRole(String userUserName, String grantUserName) {
		UserInfo grantUser = dao.findById(UserInfo.class, grantUserName);
		if (grantUser == null) {
			return;
		}
		Role role = grantUser.getRole();
		if (role != null) {
			String roleId = role.getRoleId();
			if (!"admin".equals(userUserName)) {
				// ������Ȩ�û���ӵ�еĽ�ɫ
				List<String> roleIds = findRoleIds(userUserName);
				// �����Ȩ�û��Ƿ��в����ý�ɫ��Ȩ��
				if (Collections.binarySearch(roleIds, roleId) < 0) {
					throw new CommonLogicException("��û��Ȩ��ȡ���û��Ľ�ɫ");
				}
			}
		}

		grantUser.setRole(null);
		dao.update(grantUser);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#addRoleRight(java.lang.String
	 * , java.lang.String, java.lang.String)
	 */
	@Override
	public void addRoleRight(String userName, String roleId, String rightId) {
		Role role = dao.findById(Role.class, roleId);
		if (role == null) {
			return;
		}
		RightGroup right = dao.findById(RightGroup.class, rightId);
		if (right == null) {
			return;
		}
		if (!"admin".equals(userName)) {
			// ������Ȩ�û���ӵ�еĽ�ɫ
			List<String> roleIds = findRoleIds(userName);
			// �����Ȩ�û��Ƿ��в����ý�ɫ��Ȩ��
			if (Collections.binarySearch(roleIds, roleId) < 0) {
				throw new CommonLogicException("��û��Ȩ�޸��ý�ɫ����Ȩ��");
			}
		}

		String parentRoleId = role.getParentRoleId();
		if (StringUtils.isNotBlank(parentRoleId)) {
			// ���Ҹ���ɫ��ӵ�е�Ȩ����
			List<String> rightIds = findRightIds(parentRoleId);
			// �������Ȩ��ɫ��Ȩ���Ƿ���ڸ���ɫ��Ȩ��
			if (Collections.binarySearch(rightIds, rightId) < 0) {
				throw new CommonLogicException("�ý�ɫ��Ȩ�޲��ܴ��ڸ���ɫ��Ȩ��");
			}
		}

		role.getRights().add(right);
		dao.update(role);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#addRoleUrl(java.lang.String,
	 * java.lang.String, java.util.List)
	 */
	@Override
	public void addRoleRight(String userName, String roleId,
			List<String> rightIds) {
		Role role = dao.findById(Role.class, roleId);
		if (role == null) {
			return;
		}
		if (!"admin".equals(userName)) {
			// ������Ȩ�û���ӵ�еĽ�ɫ
			List<String> roleIds = findRoleIds(userName);
			// �����Ȩ�û��Ƿ��в����ý�ɫ��Ȩ��
			if (Collections.binarySearch(roleIds, roleId) < 0) {
				throw new CommonLogicException("��û��Ȩ�޸��ý�ɫ����Ȩ��");
			}
		}
		if (rightIds != null && !rightIds.isEmpty()) {
			String parentRoleId = role.getParentRoleId();
			if (StringUtils.isNotBlank(parentRoleId)) {
				// ���Ҹ���ɫ��ӵ�е�Ȩ����
				List<String> rids = findRightIds(parentRoleId);
				for (String rightId : rightIds) {
					// �������Ȩ��ɫ��Ȩ���Ƿ���ڸ���ɫ��Ȩ��
					if (Collections.binarySearch(rids, rightId) < 0) {
						throw new CommonLogicException("�ý�ɫ��Ȩ�޲��ܴ��ڸ���ɫ��Ȩ��");
					}
				}
			}
			Set<RightGroup> rights = new LinkedHashSet<RightGroup>(
					rightIds.size());
			for (String rightId : rightIds) {
				RightGroup right = dao.findById(RightGroup.class, rightId);
				if (right != null) {
					rights.add(right);
				}
			}

			// ��ɫȨ�ޱ�ȡ�����ӽ�ɫҲ�ᱻ����ȡ��
			Set<RightGroup> rs = role.getRights();
			rs.removeAll(rights);
			if (!rs.isEmpty()) {
				Set<Role> subRoles = role.getChildrenRoles();
				for (Role subRole : subRoles) {
					subRole.getRights().removeAll(rs);
				}
			}

			role.setRights(rights);
		}
		else {
			role.setRights(null);
			Set<Role> subRoles = role.getChildrenRoles();
			for (Role subRole : subRoles) {
				subRole.setRights(null);
			}
		}
		dao.update(role);
	}

	/**
	 * ��ȡ��ɫ��ӵ�е�Ȩ����id
	 * 
	 * @param roleId
	 *            ��ɫid
	 * @return Ȩ����id�б�
	 */
	private List<String> findRightIds(String roleId) {
		String hql = "select rs.rightId from Role r join r.rights rs where r.roleId=? order by rs.rightId";

		return dao.find(hql, roleId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#findUrls(java.lang.String)
	 */
	@Override
	public List<String> findUrls(String userName) {
		if ("admin".equals(userName)) {
			String hql = "select us from RightGroup r join r.urls us order by us";

			return dao.find(hql);
		}
		String hql = "select us from UserInfo a join a.role.rights rs join rs.urls us where a.username=? order by us";

		return dao.find(hql, userName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#findMenuIds(java.lang.String)
	 */
	@Override
	public List<String> findMenuIds(String menuId) {
		RightGroup right = dao.uniqueResult(
				"select m.right from Menu m where m.menuId=?", menuId);

		if (right == null) {
			return null;
		}

		return dao.find("select m.menuId from Menu m where m.right=?", right);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#findMenuIdsByRole(java.lang.
	 * String)
	 */
	@Override
	public List<String> findMenuIdsByRole(String roleId) {
		String hql = "select m.menuId from Menu m, Role role join role.rights rs where m.right=rs and role.roleId=?";

		return dao.find(hql, roleId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.duker.mygift.service.user.RightService#findMenus(java.lang.String)
	 */
	@Override
	public List<Tree<Menu>> findMenus(String userName) {
		List<Menu> ms = null;
		if ("admin".equals(userName)) {
			String hql = "from Menu order by parentId, menuIndex desc";
			ms = dao.find(hql);
		}
		else {
			String hql = "select m from Menu m, UserInfo a join a.role.rights rs where m.right=rs and a.username=? order by m.parentId, m.menuIndex desc";
			ms = dao.find(hql, userName);
			List<Menu> ms1 = dao
					.find("select m from Menu m where m.right=null");
			ms.addAll(ms1);
			Collections.sort(ms, new MenuComparator());
		}

		Map<String, Tree<Menu>> menuIndex = new HashMap<String, Tree<Menu>>();
		List<Tree<Menu>> rootMenus = new LinkedList<Tree<Menu>>();
		Tree<Menu> parentTree = null;
		Tree<Menu> tree = null;
		String parentId = null;
		String menuId = null;

		for (Menu m : ms) {
			parentId = m.getParentId();
			menuId = m.getMenuId();
			tree = menuIndex.get(menuId);
			if (parentId == null || "-1".equals(parentId)) {
				if (tree == null) {
					tree = new Tree<Menu>(m, null, new LinkedList<Tree<Menu>>());
				}
				else {
					tree.setElement(m);
				}
				rootMenus.add(tree);
				menuIndex.put(menuId, tree);
			}
			else {
				parentTree = menuIndex.get(parentId);
				if (parentTree == null) {
					parentTree = new Tree<Menu>(null, null,
							new LinkedList<Tree<Menu>>());
					menuIndex.put(parentId, parentTree);
				}
				if (tree == null) {
					tree = new Tree<Menu>(m, parentTree,
							new LinkedList<Tree<Menu>>());
					menuIndex.put(menuId, tree);
				}
				else {
					tree.setParent(parentTree);
					tree.setElement(m);
				}
			}
		}

		Iterator<Tree<Menu>> it = rootMenus.iterator();
		while (it.hasNext()) {
			if (!checkNullNode(it.next())) {
				it.remove();
			}
		}

		return rootMenus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.duker.mygift.service.user.RightService#findNavigation(java.lang.
	 * String)
	 */
	@Override
	public Map<String, String> findNavigation(String userName) {
		List<Menu> ms = null;
		if ("admin".equals(userName)) {
			String hql = "from Menu where url is not null";
			ms = dao.find(hql);
		}
		else {
			String hql = "select m from Menu m, UserInfo a join a.role.rights rs where m.right=rs and a.username=? and m.url is not null";
			ms = dao.find(hql, userName);
			List<Menu> ms1 = dao
					.find("select m from Menu m where m.right=null and m.url is not null");
			ms.addAll(ms1);
		}
		Map<String, String> navigation = new HashMap<String, String>(ms.size());
		for (Menu m : ms) {
			navigation.put(m.getUrl(), m.toString());
		}

		return navigation;
	}

	/**
	 * �ж�������Ƿ�Ϊ�ս��
	 * 
	 * @param tree
	 *            ��
	 * @return �Ƿ�Ϊ�ս��
	 */
	private boolean checkNullNode(Tree<Menu> tree) {
		List<Tree<Menu>> children = tree.getChildren();
		if (children == null || children.isEmpty()) {
			Menu m = tree.getElement();
			if (m == null || StringUtils.isBlank(m.getUrl())) {
				return false;
			}
		}
		else {
			Iterator<Tree<Menu>> it = children.iterator();
			while (it.hasNext()) {
				if (!checkNullNode(it.next())) {
					it.remove();
				}
			}

			if (children.isEmpty()) {
				Menu m = tree.getElement();
				if (m == null || StringUtils.isBlank(m.getUrl())) {
					return false;
				}
			}
		}

		return true;
	}

	static private class MenuComparator implements Comparator<Menu> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Menu m1, Menu m2) {
			String parentId1 = m1.getParentId();
			String parentId2 = m2.getParentId();
			parentId1 = parentId1 == null ? "" : parentId1;
			parentId2 = parentId2 == null ? "" : parentId2;

			int ret = parentId1.compareTo(parentId2);
			if (ret == 0) {
				Long menuIndex1 = m1.getMenuIndex();
				Long menuIndex2 = m2.getMenuIndex();
				menuIndex1 = menuIndex1 == null ? Long.MIN_VALUE : menuIndex1;
				menuIndex2 = menuIndex2 == null ? Long.MIN_VALUE : menuIndex2;

				ret = menuIndex2.compareTo(menuIndex1);
				if (ret == 0) {
					ret = m1.getMenuId().compareTo(m2.getMenuId());
				}
			}

			return ret;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.duker.mygift.service.user.RightService#updateMenuOrder(java.lang
	 * .String , java.lang.Integer)
	 */
	@Override
	public List<Menu> updateMenuOrder(String menuId, Integer type) {
		Menu menu = dao.findById(Menu.class, menuId);
		if (menu == null) {
			throw new CommonLogicException("�ƶ��Ĳ˵�������");
		}
		Long menuIndex = menu.getMenuIndex();
		String parentId = menu.getParentId();
		if (DList.OperationType.TOP.equals(type)) {
			String hql = "select max(menuIndex) from Menu where parentId=?";
			Long order = dao.uniqueResult(hql, parentId);
			if (order == null) {
				throw new CommonLogicException("����ȷѡ��˵�");
			}
			if (order.equals(menuIndex)) {
				throw new CommonLogicException("�Ѿ��ö�");
			}

			menu.setMenuIndex(order + 1);
			dao.update(menu);
		}
		else if (DList.OperationType.UP.equals(type)) {
			String hql = "from Menu where parentId=? and menuIndex>? order by menuIndex";
			Menu p = dao.findTop(hql, 1, null, parentId, menuIndex);

			if (p != null) {
				Long order = p.getMenuIndex();
				p.setMenuIndex(menuIndex);
				menu.setMenuIndex(order);

				dao.update(p);
				dao.update(menu);
			}
			else {
				throw new CommonLogicException("�Ѿ��ö������������ƶ�");
			}
		}
		else if (DList.OperationType.DOWN.equals(type)) {
			String hql = "from Menu where parentId=? and menuIndex<? order by menuIndex desc";
			Menu p = dao.findTop(hql, 1, null, parentId, menuIndex);

			if (p != null) {
				Long order = p.getMenuIndex();
				p.setMenuIndex(menuIndex);
				menu.setMenuIndex(order);

				dao.update(p);
				dao.update(menu);
			}
			else {
				throw new CommonLogicException("�Ѿ��õף����������ƶ�");
			}
		}
		else if (DList.OperationType.BOTTOM.equals(type)) {
			String hql = "select min(menuIndex) from Menu where parentId=?";
			Long order = dao.uniqueResult(hql, parentId);

			if (order == null) {
				throw new CommonLogicException("����ȷѡ��˵�");
			}
			if (order.equals(menuIndex)) {
				throw new CommonLogicException("�Ѿ��õ�");
			}

			menu.setMenuIndex(order - 1);
			dao.update(menu);
		}
		else {
			throw new CommonLogicException("�������ͷǷ�");
		}

		String hql = "from Menu where parentId=? order by menuIndex";

		return dao.find(hql, parentId);
	}

	public void setDao(GenericHibernateDao dao) {
		this.dao = dao;
	}

}
