/*
 * @(#)CommonLogicException.java Aug 25, 2009
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.exception;

/**
 * <pre>
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: Aug 25, 2009
 * �޸��� :  lijingrui
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
@SuppressWarnings("serial")
public class CommonLogicException extends RuntimeException {

	/**
	 * �ɹ�
	 */
	public static final int SUCCESS = 0;

	/**
	 * ��������
	 */
	public static final int OTHER_ERROR = -9999;

	/**
	 * ���ݿ����
	 */
	public static final int DB_ERROR = -1;

	/**
	 * ���ݼ���ʧ�ܣ�������
	 */
	public static final int NULL_DATA = -2;

	/**
	 * ���ݼ���ʧ�ܣ��Ƿ�����
	 * 
	 */
	public static final int INVAILD_DATA = -3;

	/**
	 * ������
	 */
	private int errorCode = OTHER_ERROR;

	public CommonLogicException() {
	}

	public CommonLogicException(int errorCode) {
		this.errorCode = errorCode;
	}

	public CommonLogicException(String message) {
		super(message);
	}

	public CommonLogicException(Throwable cause) {
		super(cause);
	}

	public CommonLogicException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public CommonLogicException(int errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	public CommonLogicException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommonLogicException(int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#fillInStackTrace()
	 */
	@Override
	public Throwable fillInStackTrace() {
		if (getCause() != null) {
			return super.fillInStackTrace();
		}
		return this;
	}

}
