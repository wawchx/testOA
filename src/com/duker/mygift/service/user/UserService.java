/*
 * @(#)UserService.java 2009-11-24
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.service.user;

import java.util.List;

import com.duker.mygift.exception.CommonLogicException;
import com.duker.mygift.model.UserInfo;
import com.duker.mygift.vo.PagedList;
import com.duker.mygift.vo.SortOrder;

/**
 * <pre>
 * �û�����ӿ�
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
public interface UserService {

	/**
	 * �û��Ƿ����
	 * 
	 * @param userName
	 *            �û���
	 * @return true���� false������
	 */
	boolean userExist(String userName);

	/**
	 * ����û�
	 * 
	 * @param operationUserName
	 *            �����û�
	 * @param user
	 *            �û�����
	 * @return �������û�����
	 */
	UserInfo addUser(String operationUserName, UserInfo user)
			throws CommonLogicException;

	/**
	 * �����û���Ϣ,���������롢��¼��Ϣ��״̬��
	 * 
	 * @param operationUserName
	 *            �����û�,�����û����߸����Լ�ʱ����ֵ
	 * @param user
	 *            �û�����
	 * @return ���º���û�����
	 */
	UserInfo updateUser(String operationUserName, UserInfo user)
			throws CommonLogicException;

	/**
	 * �����û�������Ϣ,���������롢��¼��Ϣ��״̬�����͡�Ȩ�ޡ�sp��
	 * 
	 * @param user
	 *            �û�����
	 * @return ���º���û�����
	 */
	UserInfo updateUser(UserInfo user) throws CommonLogicException;

	/**
	 * �޸��û�����
	 * 
	 * @param userName
	 *            �û���
	 * @param oldPwd
	 *            ������
	 * @param newPwd
	 *            ������
	 * @return ���º���û�����
	 */
	UserInfo modifyPwd(String userName, String oldPwd, String newPwd)
			throws CommonLogicException;

	/**
	 * �����û�����
	 * 
	 * @param operationUserName
	 *            �����û�
	 * @param userName
	 *            �û���
	 * @param newPwd
	 *            ������
	 * @return ���º���û�����
	 */
	UserInfo restPwd(String operationUserName, String userName, String newPwd)
			throws CommonLogicException;

	/**
	 * �û���¼
	 * 
	 * @param userName
	 *            �û���
	 * @param pwd
	 *            ����
	 * @param loginIp
	 *            ��¼ip��ַ
	 */
	boolean login(String userName, String pwd, String loginIp);

	/**
	 * �����û�״̬
	 * 
	 * @param operationUserName
	 *            �����û�
	 * @param userName
	 *            �û���
	 * @param state
	 *            ��Ч��
	 */
	void updateState(String operationUserName, String userName, Integer state)
			throws CommonLogicException;

	/**
	 * �����û�״̬
	 * 
	 * @param operationUserName
	 *            �����û�
	 * @param userNames
	 *            �û����б�
	 * @param state
	 *            ��Ч��
	 */
	void updateState(String operationUserName, List<String> userNames,
			Integer state) throws CommonLogicException;

	/**
	 * ͨ���û��������û�
	 * 
	 * @param userName
	 *            �û���
	 * @return �û�
	 */
	UserInfo findUser(String userName);

	/**
	 * 
	 * ͨ������ģ�������û��б�
	 * 
	 * @param operationUserName
	 *            �����û�
	 * @param condition
	 *            ��ѯ����,֧���û������ǳơ���ʵ�������ֻ����绰��email��qq���û����͡��û�״̬
	 * @param pageNo
	 *            �ڼ�ҳ�� ��0��ʼ
	 * @param pageSize
	 *            ÿҳ������¼
	 * @param orders
	 *            ����ʽ
	 * @return �û��б�
	 */
	PagedList<UserInfo> findUsers(String operationUserName, UserInfo condition,
			int pageNo, int pageSize, List<SortOrder> orders);

	/**
	 * ɾ���û�
	 * 
	 * @param operationUserName
	 *            �����û�
	 * @param userName
	 *            �û���
	 */
	void deleteUser(String operationUserName, String userName)
			throws CommonLogicException;

	/**
	 * ����ɾ���û�
	 * 
	 * @param operationUserName
	 *            �����û�
	 * @param userNames
	 *            �û����б�
	 */
	void deleteUsers(String operationUserName, List<String> userNames)
			throws CommonLogicException;

	/**
	 * �����������
	 * 
	 * @param length
	 *            ���볤��
	 * @return �������
	 */
	String randomPwd(int length);

}
