/*
 * @(#)RightService.java 2009-11-24
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.service.user;

import java.util.List;
import java.util.Map;

import com.duker.mygift.exception.CommonLogicException;
import com.duker.mygift.model.Menu;
import com.duker.mygift.model.Role;
import com.duker.mygift.vo.Tree;

/**
 * <pre>
 * Ȩ�޹���ӿڡ��ýӿڰ�����ɫ��ά�����˵���Ȩ����ķ���
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2009-11-24
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public interface RightService {

	/**
	 * ��ɫ���Ƿ����
	 * 
	 * @param roleName
	 *            ��ɫ��
	 * @return �Ƿ����
	 */
	boolean roleExist(String roleName);

	/**
	 * ���ӽ�ɫ
	 * 
	 * @param userName
	 *            �û��û���
	 * @param role
	 *            ��ɫʵ��
	 * @return �����Ľ�ɫʵ��
	 */
	Role addRole(String userName, Role role) throws CommonLogicException;

	/**
	 * �޸Ľ�ɫ����Ϣ
	 * 
	 * @param userName
	 *            �û��û���
	 * @param role
	 *            ��ɫ
	 * @return ���º�Ľ�ɫʵ��
	 */
	Role updateRole(String userName, Role role) throws CommonLogicException;

	/**
	 * ɾ����ɫ
	 * 
	 * @param userName
	 *            �û��û���
	 * @param roleId
	 *            ��ɫid
	 */
	void deleteRole(String userName, String roleId) throws CommonLogicException;

	/**
	 * ����ɾ����ɫ
	 * 
	 * @param userName
	 *            �û��û���
	 * @param roleIds
	 *            ��ɫid�б�
	 */
	void deleteRoles(String userName, List<String> roleIds)
			throws CommonLogicException;

	/**
	 * ��ȡ��ɫ��ϸ��Ϣ
	 * 
	 * @param roleId
	 *            ��ɫid
	 * @return ��ɫ��ϸ��Ϣ
	 */
	Role findRole(String roleId);

	/**
	 * ��ȡ�û��û���������¼��Ľ�ɫ(����������)
	 * 
	 * @param userName
	 *            �û��û���
	 * @return ��ɫ��
	 */
	List<Tree<Role>> findRoles(String userName);

	/**
	 * ��ȡ�û��û���������¼��Ľ�ɫ��id(����������)
	 * 
	 * @param userName
	 *            �û��û���
	 * @return ��ɫid�б�
	 */
	List<String> findRoleIds(String userName);

	/**
	 * ��ȡ�û��û���������¼��Ľ�ɫ��id
	 * 
	 * @param userName
	 *            �û��û���
	 * @param containSelf
	 *            �Ƿ��������
	 * @return ��ɫid�б�
	 */
	List<String> findRoleIds(String userName, boolean containSelf);

	/**
	 * �����û�����ɫ
	 * 
	 * @param userUserName
	 *            �û��û���
	 * @param grantUserName
	 *            �¼��û��û���
	 * @param roleId
	 *            ��ɫid
	 */
	void addUserRole(String userUserName, String grantUserName, String roleId)
			throws CommonLogicException;

	/**
	 * ȡ���û�����ɫ����
	 * 
	 * @param userUserName
	 *            �û��û���
	 * @param grantUserName
	 *            �¼��û��û���
	 */
	void delUserRole(String userUserName, String grantUserName)
			throws CommonLogicException;

	/**
	 * ������ɫ��Ȩ����
	 * 
	 * @param userName
	 *            �û��û���
	 * @param roleId
	 *            ��ɫid
	 * @param rightId
	 *            Ȩ����id
	 */
	void addRoleRight(String userName, String roleId, String rightId)
			throws CommonLogicException;

	/**
	 * ������ɫ��Ȩ����
	 * 
	 * @param userName
	 *            �û��û���
	 * @param roleId
	 *            ��ɫid
	 * @param rightIds
	 *            Ȩ����id�б�
	 */
	void addRoleRight(String userName, String roleId, List<String> rightIds)
			throws CommonLogicException;

	/**
	 * ��ȡ�û�ӵ�е�url�б�
	 * 
	 * @param userName
	 *            �û��û���
	 * @return url�б�
	 */
	List<String> findUrls(String userName);

	/**
	 * ��ȡͬȨ�����µĲ˵�
	 * 
	 * @param menuId
	 *            �˵�id
	 * @return ͬȨ�����µĲ˵�id�б�
	 */
	List<String> findMenuIds(String menuId);

	/**
	 * ��ȡȨ�����µĲ˵�
	 * 
	 * @param roleId
	 *            Ȩ����id
	 * @return Ȩ�����µĲ˵�id�б�
	 */
	List<String> findMenuIdsByRole(String roleId);

	/**
	 * ��ȡ�û���ӵ�еĲ˵���
	 * 
	 * @param userName
	 *            �û��û���
	 * @return �˵���
	 */
	List<Tree<Menu>> findMenus(String userName);

	/**
	 * ��ȡ�û��˵�������ַ
	 * 
	 * @param userName
	 *            �û��û���
	 * @return ������ַ
	 */
	Map<String, String> findNavigation(String userName);

	/**
	 * �����˵�˳��
	 * 
	 * @param menuId
	 *            �˵�id
	 * @param type
	 *            �ƶ�����
	 * @return �˵�����
	 */
	List<Menu> updateMenuOrder(String menuId, Integer type);

}
