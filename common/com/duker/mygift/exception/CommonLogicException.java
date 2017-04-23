/*
 * @(#)CommonLogicException.java Aug 25, 2009
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.exception;

/**
 * <pre>
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: Aug 25, 2009
 * 修改人 :  lijingrui
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@SuppressWarnings("serial")
public class CommonLogicException extends RuntimeException {

	/**
	 * 成功
	 */
	public static final int SUCCESS = 0;

	/**
	 * 其他错误
	 */
	public static final int OTHER_ERROR = -9999;

	/**
	 * 数据库错误
	 */
	public static final int DB_ERROR = -1;

	/**
	 * 数据检验失败，空数据
	 */
	public static final int NULL_DATA = -2;

	/**
	 * 数据检验失败，非法数据
	 * 
	 */
	public static final int INVAILD_DATA = -3;

	/**
	 * 错误码
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
