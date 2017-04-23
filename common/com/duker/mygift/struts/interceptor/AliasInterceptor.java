/*
 * @(#)AliasInterceptor.java 2008-6-11
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.struts.interceptor;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.reflection.ReflectionContextState;

/**
 * <pre>
 * 该类在xwork的<code>com.opensymphony.xwork2.interceptor.AliasInterceptor</code>上做了修改.
 * <li>
 * 把转换参数放在了拦截器&lt;interceptor-ref&gt;&lt;interceptor-ref&gt;下面
 * </li>
 * <li>
 * 调转敢转换顺序#{ 'alias' : 'name' },alias转换后的参数名,name要取的表达式
 * </li>
 * 
 * &lt;u&gt;Example code:&lt;/u&gt;
 * 
 * &lt;!-- START SNIPPET: example --&gt;
 * &lt;action name=&quot;someAction&quot; class=&quot;com.examples.SomeAction&quot;&gt;
 *     &lt;interceptor-ref name=&quot;alias&quot;&gt;
 *     	&lt;!-- The value for the bar parameter will be applied as if it were named foo --&gt;
 *     	&lt;param name=&quot;aliases&quot;&gt;#{ 'foo' : 'bar' }&lt;/param&gt;
 *     &lt;/interceptor-ref&gt;
 *     &lt;interceptor-ref name=&quot;basicStack&quot;/&gt;
 *     &lt;result name=&quot;success&quot;&gt;good_result.ftl&lt;/result&gt;
 * &lt;/action&gt;
 * &lt;!-- END SNIPPET: example --&gt;
 * 
 * @author wangzh
 * 
 * @version
 * 
 * 修改版本:
 * 修改日期: 2008-6-11
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 
 * </pre>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AliasInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = 146618216301962007L;

	private static final Log log = LogFactory.getLog(AliasInterceptor.class);

	protected String aliases;

	public String intercept(ActionInvocation invocation) throws Exception {

		ActionContext ac = invocation.getInvocationContext();
		ValueStack stack = ac.getValueStack();
		Object obj = stack.findValue(aliases);

		if (obj instanceof Map) {
			// override
			Map aliaseMap = (Map) obj;
			Map contextParameters = ac.getParameters();
			Iterator itr = aliaseMap.entrySet().iterator();
			Map contextMap = ac.getContextMap();
			try {
				while (itr.hasNext()) {
					Map.Entry entry = (Map.Entry) itr.next();
					String name = entry.getValue().toString();
					String alias = (String) entry.getKey();
					ReflectionContextState.setCreatingNullObjects(contextMap, false);
					ReflectionContextState.setDenyMethodExecution(contextMap, false);
					ReflectionContextState.setReportingConversionErrors(contextMap,
							false);
					Object value = stack.findValue(name);
					ReflectionContextState.setCreatingNullObjects(contextMap, true);
					ReflectionContextState.setDenyMethodExecution(contextMap, true);
					ReflectionContextState.setReportingConversionErrors(contextMap,
							true);
					if (null == value) {
						// workaround
						if (null != contextParameters) {
							value = contextParameters.get(name);
							if (value == null) {
								Iterator<Entry> it = contextParameters
										.entrySet().iterator();
								while (it.hasNext()) {
									Entry e = it.next();
									String k = e.getKey().toString();
									Object v = e.getValue();
									if (k.startsWith(name)) {
										stack
												.setValue(alias
														+ k.substring(name
																.length()), v);
									}
								}
							}
						}
					}
					if (null != value) {
						stack.setValue(alias, value);
					}
				}
			}
			finally {
				ReflectionContextState.setCreatingNullObjects(contextMap, false);
				ReflectionContextState.setDenyMethodExecution(contextMap, false);
				ReflectionContextState
						.setReportingConversionErrors(contextMap, false);
			}

		}
		else {
			log.debug("invalid alias expression:" + aliases);
		}

		return invocation.invoke();
	}

	public String getAliases() {
		return aliases;
	}

	public void setAliases(String aliases) {
		this.aliases = aliases;
	}

}
