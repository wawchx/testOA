/*
 * @(#)BeanObjectFactory.java 2008-3-27
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

/**
 * <pre>
 * ���ڴ�Springȡ��һ��Beanʵ��
 * ��ʱ������Ҫ����ʵ�����ȡ��һ��Bean�����������������ʾ����AcclicationContext,�ᵼ�´����Spring���.
 * ������ȡ��һ���������Spring���,�Ӷ���ʹ�����Spring���̫��
 * 
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: 2008-8-21
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
@SuppressWarnings("unchecked")
public class BeanObjectFactory {

	/**
	 * Spring������
	 */
	private static ApplicationContext context;

	/**
	 * ��־����
	 */
	private static final Log log = LogFactory.getLog(BeanObjectFactory.class);

	/**
	 * @return the context
	 */
	public static ApplicationContext getContext() {
		return context;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public static void setContext(ApplicationContext context) {
		BeanObjectFactory.context = context;
	}

	/**
	 * ��spring�л��һ��bean
	 * 
	 * @param <T>
	 *            bean����
	 * @param beanName
	 *            bean����
	 * @return T bean����
	 */
	public static <T> T getBean(String beanName) {
		Assert.notNull(context, "BeanObjectFactoryδ��ʼ��");
		Assert.hasText(beanName, "����BeanNameΪ��");

		return (T) context.getBean(beanName);
	}

	/**
	 * springӦ�ùرշ���
	 * 
	 */
	public static void destroy() {
		log.info("destroying all spring objects...");

		try {
			if (context instanceof ConfigurableApplicationContext) {
				ConfigurableApplicationContext appContext = (ConfigurableApplicationContext) context;

				appContext.close();
				log.info("...all spring objects have been destroyed");
				// Log4jWebConfigurer.shutdownLogging();
			}
			else {
				log.info("cannot destroyed, not using a ConfigurableApplicationContext, try to destroy the objects manually.");
			}
		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
}
