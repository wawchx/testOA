/*
 * @(#)ActionErrorInterceptor.java 2008-12-8
 * 
 * 信息审核管理系统
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
 * ActionSupport类异常拦截器,把异常信息加入到ActionErrors里面
 * 异常信息从<code>Exception</code>.LocalizedMessage中取，如果LocalizedMessage为空则填默认值&quot;未知错误&quot;
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2008-12-8
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
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
				action.setRetMsg("系统错误");
				action.addActionError("系统错误,请稍后重试");
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
