/*
 * @(#)ActionErrorInterceptor.java 2008-12-8
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.struts.interceptor;

import org.apache.commons.lang3.StringEscapeUtils;

import com.duker.mygift.exception.CommonLogicException;
import com.duker.mygift.struts.action.BaseAction;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.ExceptionHolder;
import com.opensymphony.xwork2.interceptor.ExceptionMappingInterceptor;

/**
 * <pre>
 * ActionSupport���쳣������,���쳣��Ϣ���뵽ActionErrors����
 * �쳣��Ϣ��<code>Exception</code>.LocalizedMessage��ȡ�����LocalizedMessageΪ������Ĭ��ֵ&quot;δ֪����&quot;
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2008-12-8
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
@SuppressWarnings("serial")
public class ActionErrorInterceptor extends ExceptionMappingInterceptor {

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.opensymphony.xwork2.interceptor.ExceptionMappingInterceptor#
	 * publishException(com.opensymphony.xwork2.ActionInvocation,
	 * com.opensymphony.xwork2.interceptor.ExceptionHolder)
	 */
	@Override
	protected void publishException(ActionInvocation invocation,
			ExceptionHolder exceptionHolder) {
		Object obj = invocation.getAction();

		if (obj instanceof BaseAction) {
			BaseAction action = (BaseAction) obj;
			Exception ex = exceptionHolder.getException();

			if (ex instanceof CommonLogicException) {
				CommonLogicException e = (CommonLogicException) ex;
				String msg = e.getMessage();
				if (msg != null) {
					msg = StringEscapeUtils.escapeHtml4(msg);
					msg = msg.replaceAll("\r(\n)?", "<br>");
					msg = msg.replaceAll(" ", "&nbsp;");
				}

				action.setRetCode(e.getErrorCode());
				action.setRetMsg(msg);
				action.addActionError(msg);
			}
			else {
				action.setRetCode(1);
				action.setRetMsg("ϵͳ����");
				action.addActionError("ϵͳ����,���Ժ�����");
			}

		}

		super.publishException(invocation, exceptionHolder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.xwork2.interceptor.ExceptionMappingInterceptor#handleLogging
	 * (java.lang.Exception)
	 */
	@Override
	protected void handleLogging(Exception e) {
		if (!(e instanceof CommonLogicException)) {
			super.handleLogging(e);
		}
	}

}
