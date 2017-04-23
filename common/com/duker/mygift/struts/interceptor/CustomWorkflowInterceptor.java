/*
 * @(#)CustomWorkflowInterceptor.java 2010-3-31
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.struts.interceptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.duker.mygift.struts.action.BaseAction;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.DefaultWorkflowInterceptor;

/**
 * <pre>
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2010-3-31
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class CustomWorkflowInterceptor extends DefaultWorkflowInterceptor {

	/**
	 * ���л��汾��
	 */
	private static final long serialVersionUID = 6656226045021956603L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.xwork2.interceptor.DefaultWorkflowInterceptor#doIntercept(com.opensymphony.xwork2.ActionInvocation)
	 */
	@Override
	protected String doIntercept(ActionInvocation invocation) throws Exception {
		Object action = invocation.getAction();

		if (action instanceof BaseAction) {
			BaseAction baseAction = (BaseAction) action;

			if (baseAction.hasErrors()) {
				Collection<String> actionErrors = baseAction.getActionErrors();
				Map<String, List<String>> fieldErrors = baseAction
						.getFieldErrors();
				StringBuilder retMsg = new StringBuilder();
				if (actionErrors != null && !actionErrors.isEmpty()) {
					retMsg.append(actionErrors.toString());
				}
				if (fieldErrors != null && !fieldErrors.isEmpty()) {
					retMsg.append(fieldErrors.toString());
				}
				baseAction.setRetCode(1);
				baseAction.setRetMsg(retMsg.toString());
			}
		}

		return super.doIntercept(invocation);
	}

}
