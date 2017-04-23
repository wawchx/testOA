/*
 * @(#)UserService.java 2009-11-24
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.service.user;

import java.util.List;

import com.duker.mygift.exception.CommonLogicException;
import com.duker.mygift.model.UserInfo;
import com.duker.mygift.vo.PagedList;
import com.duker.mygift.vo.SortOrder;

/**
 * <pre>
 * 用户服务接口
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
public interface UserService {

	/**
	 * 用户是否存在
	 * 
	 * @param userName
	 *            用户名
	 * @return true存在 false不存在
	 */
	boolean userExist(String userName);

	/**
	 * 添加用户
	 * 
	 * @param operationUserName
	 *            操作用户
	 * @param user
	 *            用户对象
	 * @return 保存后的用户对象
	 */
	UserInfo addUser(String operationUserName, UserInfo user)
			throws CommonLogicException;

	/**
	 * 更新用户信息,不包括密码、登录信息、状态等
	 * 
	 * @param operationUserName
	 *            操作用户,超级用户或者更新自己时传空值
	 * @param user
	 *            用户对象
	 * @return 更新后的用户对象
	 */
	UserInfo updateUser(String operationUserName, UserInfo user)
			throws CommonLogicException;

	/**
	 * 更新用户基本信息,不包括密码、登录信息、状态、类型、权限、sp等
	 * 
	 * @param user
	 *            用户对象
	 * @return 更新后的用户对象
	 */
	UserInfo updateUser(UserInfo user) throws CommonLogicException;

	/**
	 * 修改用户密码
	 * 
	 * @param userName
	 *            用户名
	 * @param oldPwd
	 *            旧密码
	 * @param newPwd
	 *            新密码
	 * @return 更新后的用户对象
	 */
	UserInfo modifyPwd(String userName, String oldPwd, String newPwd)
			throws CommonLogicException;

	/**
	 * 重置用户密码
	 * 
	 * @param operationUserName
	 *            操作用户
	 * @param userName
	 *            用户名
	 * @param newPwd
	 *            新密码
	 * @return 更新后的用户对象
	 */
	UserInfo restPwd(String operationUserName, String userName, String newPwd)
			throws CommonLogicException;

	/**
	 * 用户登录
	 * 
	 * @param userName
	 *            用户名
	 * @param pwd
	 *            密码
	 * @param loginIp
	 *            登录ip地址
	 */
	boolean login(String userName, String pwd, String loginIp);

	/**
	 * 更新用户状态
	 * 
	 * @param operationUserName
	 *            操作用户
	 * @param userName
	 *            用户名
	 * @param state
	 *            有效性
	 */
	void updateState(String operationUserName, String userName, Integer state)
			throws CommonLogicException;

	/**
	 * 更新用户状态
	 * 
	 * @param operationUserName
	 *            操作用户
	 * @param userNames
	 *            用户名列表
	 * @param state
	 *            有效性
	 */
	void updateState(String operationUserName, List<String> userNames,
			Integer state) throws CommonLogicException;

	/**
	 * 通过用户名查找用户
	 * 
	 * @param userName
	 *            用户名
	 * @return 用户
	 */
	UserInfo findUser(String userName);

	/**
	 * 
	 * 通过条件模糊查找用户列表
	 * 
	 * @param operationUserName
	 *            操作用户
	 * @param condition
	 *            查询条件,支持用户名、昵称、真实姓名、手机、电话、email、qq、用户类型、用户状态
	 * @param pageNo
	 *            第几页， 从0开始
	 * @param pageSize
	 *            每页几条记录
	 * @param orders
	 *            排序方式
	 * @return 用户列表
	 */
	PagedList<UserInfo> findUsers(String operationUserName, UserInfo condition,
			int pageNo, int pageSize, List<SortOrder> orders);

	/**
	 * 删除用户
	 * 
	 * @param operationUserName
	 *            操作用户
	 * @param userName
	 *            用户名
	 */
	void deleteUser(String operationUserName, String userName)
			throws CommonLogicException;

	/**
	 * 批量删除用户
	 * 
	 * @param operationUserName
	 *            操作用户
	 * @param userNames
	 *            用户名列表
	 */
	void deleteUsers(String operationUserName, List<String> userNames)
			throws CommonLogicException;

	/**
	 * 生成随机密码
	 * 
	 * @param length
	 *            密码长度
	 * @return 随机密码
	 */
	String randomPwd(int length);

}
