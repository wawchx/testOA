/*
 * @(#)ContentTypeException.java Oct 25, 2013
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
 * 修改日期: Oct 25, 2013
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class ContentTypeException extends CommonLogicException {

	public ContentTypeException() {
		super("empty content type");
	}

	public ContentTypeException(String contentType) {
		super(contentType);
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
