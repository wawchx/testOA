/*
 * @(#)ProxyChecker.java Mar 3, 2014
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.util.http;

import java.io.Serializable;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: Mar 3, 2014
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public interface ProxyChecker extends Serializable {

	/**
	 * �����ô���ȡ���Ľ���Ƿ���ȷ
	 * 
	 * @param status
	 *            http��Ӧ״̬
	 * @param content
	 *            �ô���ȡ��������
	 * @return �����Ƿ���ȷ
	 */
	boolean check(Integer status, String content);
}
