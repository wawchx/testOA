/*
 * @(#)RightService.java 2009-11-24
 * 
 * 信息审核管理系统
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
 * 权限管理接口。该接口包括角色的维护、菜单与权限组的分配
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2009-11-24
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public interface RightService {

	/**
	 * 角色名是否存在
	 * 
	 * @param roleName
	 *            角色名
	 * @return 是否存在
	 */
	boolean roleExist(String roleName);

	/**
	 * 增加角色
	 * 
	 * @param userName
	 *            用户用户名
	 * @param role
	 *            角色实例
	 * @return 保存后的角色实例
	 */
	Role addRole(String userName, Role role) throws CommonLogicException;

	/**
	 * 修改角色的信息
	 * 
	 * @param userName
	 *            用户用户名
	 * @param role
	 *            角色
	 * @return 更新后的角色实例
	 */
	Role updateRole(String userName, Role role) throws CommonLogicException;

	/**
	 * 删除角色
	 * 
	 * @param userName
	 *            用户用户名
	 * @param roleId
	 *            角色id
	 */
	void deleteRole(String userName, String roleId) throws CommonLogicException;

	/**
	 * 批量删除角色
	 * 
	 * @param userName
	 *            用户用户名
	 * @param roleIds
	 *            角色id列表
	 */
	void deleteRoles(String userName, List<String> roleIds)
			throws CommonLogicException;

	/**
	 * 获取角色详细信息
	 * 
	 * @param roleId
	 *            角色id
	 * @return 角色详细信息
	 */
	Role findRole(String roleId);

	/**
	 * 获取用户用户所管理的下级的角色(不包含自身)
	 * 
	 * @param userName
	 *            用户用户名
	 * @return 角色树
	 */
	List<Tree<Role>> findRoles(String userName);

	/**
	 * 获取用户用户所管理的下级的角色的id(不包含自身)
	 * 
	 * @param userName
	 *            用户用户名
	 * @return 角色id列表
	 */
	List<String> findRoleIds(String userName);

	/**
	 * 获取用户用户所管理的下级的角色的id
	 * 
	 * @param userName
	 *            用户用户名
	 * @param containSelf
	 *            是否包含自身
	 * @return 角色id列表
	 */
	List<String> findRoleIds(String userName, boolean containSelf);

	/**
	 * 关联用户、角色
	 * 
	 * @param userUserName
	 *            用户用户名
	 * @param grantUserName
	 *            下级用户用户名
	 * @param roleId
	 *            角色id
	 */
	void addUserRole(String userUserName, String grantUserName, String roleId)
			throws CommonLogicException;

	/**
	 * 取消用户、角色关联
	 * 
	 * @param userUserName
	 *            用户用户名
	 * @param grantUserName
	 *            下级用户用户名
	 */
	void delUserRole(String userUserName, String grantUserName)
			throws CommonLogicException;

	/**
	 * 关联角色、权限组
	 * 
	 * @param userName
	 *            用户用户名
	 * @param roleId
	 *            角色id
	 * @param rightId
	 *            权限组id
	 */
	void addRoleRight(String userName, String roleId, String rightId)
			throws CommonLogicException;

	/**
	 * 关联角色、权限组
	 * 
	 * @param userName
	 *            用户用户名
	 * @param roleId
	 *            角色id
	 * @param rightIds
	 *            权限组id列表
	 */
	void addRoleRight(String userName, String roleId, List<String> rightIds)
			throws CommonLogicException;

	/**
	 * 获取用户拥有的url列表
	 * 
	 * @param userName
	 *            用户用户名
	 * @return url列表
	 */
	List<String> findUrls(String userName);

	/**
	 * 获取同权限组下的菜单
	 * 
	 * @param menuId
	 *            菜单id
	 * @return 同权限组下的菜单id列表
	 */
	List<String> findMenuIds(String menuId);

	/**
	 * 获取权限组下的菜单
	 * 
	 * @param roleId
	 *            权限组id
	 * @return 权限组下的菜单id列表
	 */
	List<String> findMenuIdsByRole(String roleId);

	/**
	 * 获取用户所拥有的菜单树
	 * 
	 * @param userName
	 *            用户用户名
	 * @return 菜单树
	 */
	List<Tree<Menu>> findMenus(String userName);

	/**
	 * 获取用户菜单导航地址
	 * 
	 * @param userName
	 *            用户用户名
	 * @return 导航地址
	 */
	Map<String, String> findNavigation(String userName);

	/**
	 * 调整菜单顺序
	 * 
	 * @param menuId
	 *            菜单id
	 * @param type
	 *            移动类型
	 * @return 菜单集合
	 */
	List<Menu> updateMenuOrder(String menuId, Integer type);

}
