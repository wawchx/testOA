/*
 * @(#)QuartzJobProxy.java 2009-6-23
 * 
 * ��Ϣ��˹���ϵͳ
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
 * ��spring���ɵĿ����л���quartz job����
 * ͨ��job data map�����õ�org.quartz.job.beanName,org.quartz.job.executeMethodȡ��
 * Ҫִ�е������bean id/name,execute method.ͨ��org.quartz.job.persistentProps��ȡҪ
 * �־û�������״̬����,������ִ����֮�����Щ���Գ־û������ݿ���,ϵͳ�´���������Щ���Իᱻ
 * �������õ�����bean��
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2009-6-23
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@SuppressWarnings("unchecked")
public class QuartzJobProxy implements Job, Serializable {

	/**
	 * ���л��汾��
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

					// �õ�Ŀ����, �����aop�������,��õ�ԭʼ��
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
	 * ȡ�ö��������ֵ
	 * 
	 * @param bean
	 *            pojo
	 * @param name
	 *            ������
	 * @return ����ֵ
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
