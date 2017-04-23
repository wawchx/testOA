/*
 * @(#)QuartzJobProxy.java 2009-6-23
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.support.quartz;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;

import com.duker.mygift.common.BeanObjectFactory;

/**
 * <pre>
 * 与spring集成的可序列化的quartz job代理
 * 通过job data map中配置的org.quartz.job.beanName,org.quartz.job.executeMethod取得
 * 要执行的任务的bean id/name,execute method.通过org.quartz.job.persistentProps获取要
 * 持久化的任务状态属性,在任务执行完之后把这些属性持久化到数据库中,系统下次重启后这些属性会被
 * 重新设置到任务bean上
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2009-6-23
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@SuppressWarnings("unchecked")
public class QuartzJobProxy implements Job, Serializable {

	/**
	 * 序列化版本号
	 */
	private static final long serialVersionUID = -2422339110885088312L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		try {
			CachedIntrospectionResults.acceptClassLoader(Thread.currentThread()
					.getContextClassLoader());

			JobDataMap map = context.getJobDetail().getJobDataMap();
			Object obj = map.get("org.quartz.job.executeMethod");
			if (!(obj instanceof String)) {
				return;
			}

			String[] ss = ((String) obj).split("\\s*,\\s*");
			for (String s : ss) {
				String[] sss = s.split("\\.");
				if (sss.length < 2) {
					continue;
				}
				try {
					String beanName = sss[0];
					String executeMethod = sss[1];
					Object bean = BeanObjectFactory.getBean(beanName);
					Object p = map.get("org.quartz.job.persistentProps");

					Map<String, Object> propertyValues = null;

					if (p instanceof Map) {
						propertyValues = (Map<String, Object>) p;

						if (!propertyValues.isEmpty()) {
							BeanWrapper bw = PropertyAccessorFactory
									.forBeanPropertyAccess(bean);

							MutablePropertyValues pvs = new MutablePropertyValues();
							pvs.addPropertyValues(propertyValues);
							bw.setPropertyValues(pvs, true);
						}
					}

					// 得到目标类, 如果是aop代理的类,则得到原始类
					Class<?> cls = AopUtils.getTargetClass(bean);
					Method m = cls.getMethod(executeMethod);
					context.setResult(m.invoke(bean));

					Iterator<Object> it = null;

					if (p instanceof List) {
						propertyValues = new HashMap<String, Object>();
						it = ((List<Object>) p).iterator();
					}
					else if (p instanceof Map) {
						it = ((Map<Object, Object>) p).keySet().iterator();
					}

					if (it == null || propertyValues == null) {
						continue;
					}

					String propName = null;
					Object propValue = null;

					while (it.hasNext()) {
						propName = it.next().toString();
						propValue = getProperty(bean, propName);

						if (propValue instanceof Serializable) {
							propertyValues.put(propName, propValue);
						}
					}

					if (!propertyValues.isEmpty()) {
						map.put("org.quartz.job.persistentProps",
								propertyValues);
					}
					else {
						map.remove("org.quartz.job.persistentProps");
					}
				}
				catch (Exception e) {
				}
			}
		}
		catch (Exception ex) {
			throw new JobExecutionException(ex);
		}
		finally {
			CachedIntrospectionResults.clearClassLoader(Thread.currentThread()
					.getContextClassLoader());
			Introspector.flushCaches();
		}
	}

	/**
	 * 取得对象的属性值
	 * 
	 * @param bean
	 *            pojo
	 * @param name
	 *            属性名
	 * @return 属性值
	 */
	public static Object getProperty(Object bean, String name) {
		try {
			return PropertyUtils.getProperty(bean, name);
		}
		catch (Exception ex) {
			return null;
		}
	}

}
