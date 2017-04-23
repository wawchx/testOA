/*
 * @(#)ContentTypeException.java Oct 25, 2013
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
 * �޸�����: Oct 25, 2013
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
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
