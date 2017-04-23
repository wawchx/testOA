/*
 * @(#)InitializationListener.java 2008-3-27
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.support.spring;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.duker.mygift.common.BeanObjectFactory;

/**
 * <pre>
 * 系统初始化监听器,完成spring和log4j的初始化
 * 
 * @author wangzh
 * 
 * @version 1.0
 * 
 * 修改版本: 1.0
 * 修改日期: 2008-3-27
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人:
 * </pre>
 */
public class InitializationListener implements ServletContextListener {

	/**
	 * spring初始化监听器
	 */
	private ContextLoaderListener spring = new ContextLoaderListener();

	/*
	 * (non-Javadoc)
	 * 
	 * @seejavax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
	 * ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent event) {
		spring.contextDestroyed(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.ServletContextListener#contextInitialized(javax.servlet
	 * .ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent event) {
		Log log = LogFactory.getLog(InitializationListener.class);
		log.info("系统初始化开始");

		spring.contextInitialized(event);
		// 得到Spring的Context,并且用这个对象初始化BeanObjectFactory
		WebApplicationContext context = WebApplicationContextUtils
				.getRequiredWebApplicationContext(event.getServletContext());
		BeanObjectFactory.setContext(context);
		log.info("系统初始化完成");
	}

}
