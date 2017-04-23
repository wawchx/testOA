/*
 * @(#)BaseAction.java Apr 30, 2009
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.struts.action;

import com.opensymphony.xwork2.ActionSupport;

/**
 * <pre>
 * 
 * Action����
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: Apr 30, 2009
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
@SuppressWarnings("serial")
public abstract class BaseAction extends ActionSupport {

	/**
	 * ��ת�����ַ���
	 */
	public static final String RESULTINFO = "resultInfo";

	/**
	 * ��Ӧ�� 0�ɹ� ����ʧ��
	 */
	protected int retCode = 0;

	/**
	 * ������Ϣ
	 */
	protected String retMsg;

	public int getRetCode() {
		return retCode;
	}

	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}

	public String getRetMsg() {
		return retMsg;
	}

	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}

}
