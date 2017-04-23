/*
 * @(#)PreResultInterceptor.java 2009-5-4
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.struts.interceptor;

import static org.springframework.context.ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.interceptor.PreResultListener;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import com.opensymphony.xwork2.util.reflection.ReflectionContextState;

/**
 * <pre>
 * PreResultInterceptor
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2009-5-4
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class PreResultInterceptor extends AbstractInterceptor {

	/**
	 * ���л��汾��
	 */
	private static final long serialVersionUID = 2587426261413346019L;

	/**
	 * ��־����
	 */
	protected static final Log log = LogFactory
			.getLog(PreResultInterceptor.class);

	/**
	 * struts2���󹤳���
	 */
	protected ObjectFactory objectFactory;

	/**
	 * ��ջ������
	 */
	protected ValueStackFactory valueStackFactory;

	/**
	 * listeners,��",; \t\n"�ָ�
	 */
	protected String listeners;

	/**
	 * �����б�
	 */
	protected String params;

	public void setListeners(String listeners) {
		this.listeners = listeners;
	}

	public void setParams(String params) {
		this.params = params;
	}

	@Inject
	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Inject
	public void setValueStackFactory(ValueStackFactory valueStackFactory) {
		this.valueStackFactory = valueStackFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.xwork2.interceptor.AbstractInterceptor#intercept(com.opensymphony.xwork2.ActionInvocation)
	 */
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		ActionContext ctx = invocation.getInvocationContext();
		String[] listenerArray = StringUtils.tokenizeToStringArray(listeners,
				CONFIG_LOCATION_DELIMITERS);
		ValueStack stack = invocation.getInvocationContext().getValueStack();
		Object obj = stack.findValue(params);
		Map map = null;
		ValueStack newStack = null;

		if (obj instanceof Map) {
			map = (Map) obj;
			newStack = valueStackFactory.createValueStack();
			Map<String, Object> context = newStack.getContext();
			ReflectionContextState.setCreatingNullObjects(context, true);
			ReflectionContextState.setDenyMethodExecution(context, true);
			ReflectionContextState.setReportingConversionErrors(context, true);

			// keep locale from original context
			context.put(ActionContext.LOCALE, stack.getContext().get(
					ActionContext.LOCALE));
		}

		for (int i = 0; i < listenerArray.length; i++) {
			try {
				Object listener = objectFactory.buildBean(listenerArray[i], ctx
						.getContextMap(), true);

				if (listener instanceof PreResultListener) {
					invocation
							.addPreResultListener((PreResultListener) listener);

					if (map != null && newStack != null) {
						Set<Entry> entrySet = map.entrySet();
						newStack.push(listener);

						for (Entry entry : entrySet) {
							newStack.setValue(entry.getKey().toString(), entry
									.getValue());
						}

						newStack.pop();
					}
				}
			}
			catch (Throwable t) {
				log.error("add PreResultListener[" + listenerArray[i]
						+ "]error!", t);
			}
		}

		return invocation.invoke();
	}

}
