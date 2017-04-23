/*
 * @(#)BeanObjectFactory.java 2008-3-27
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

/**
 * <pre>
 * 用于从Spring取得一个Bean实例
 * 有时代码需要根据实际情况取得一个Bean但是如果代码里面显示引用AcclicationContext,会导致代码和Spring耦合.
 * 这样争取在一个类里面和Spring耦合,从而不使程序和Spring耦合太紧
 * 
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: 2008-8-21
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@SuppressWarnings("unchecked")
public class BeanObjectFactory {

	/**
	 * Spring上下文
	 */
	private static ApplicationContext context;

	/**
	 * 日志对象
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
	 * 从spring中获得一个bean
	 * 
	 * @param <T>
	 *            bean类型
	 * @param beanName
	 *            bean名称
	 * @return T bean类型
	 */
	public static <T> T getBean(String beanName) {
		Assert.notNull(context, "BeanObjectFactory未初始化");
		Assert.hasText(beanName, "传入BeanName为空");

		return (T) context.getBean(beanName);
	}

	/**
	 * spring应用关闭方法
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
