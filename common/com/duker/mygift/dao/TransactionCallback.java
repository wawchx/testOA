/*
 * @(#)TransactionCallback.java 2010-5-24
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.dao;

/**
 * <pre>
 * ����ִ�лص�����
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2010-5-24
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public interface TransactionCallback<T> {

	/**
	 * ִ��һ�δ�����Ĵ���
	 * 
	 * @return ���ؽ��
	 */
	T doTransaction() throws Throwable;

}
