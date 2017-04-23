/*
 * @(#)InitializationListener.java 2008-3-27
 * 
 * ��Ϣ��˹���ϵͳ
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
 * ϵͳ��ʼ��������,���spring��log4j�ĳ�ʼ��
 * 
 * @author wangzh
 * 
 * @version 1.0
 * 
 * �޸İ汾: 1.0
 * �޸�����: 2008-3-27
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������:
 * </pre>
 */
public class InitializationListener implements ServletContextListener {

	/**
	 * spring��ʼ��������
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
		log.info("ϵͳ��ʼ����ʼ");

		spring.contextInitialized(event);
		// �õ�Spring��Context,��������������ʼ��BeanObjectFactory
		WebApplicationContext context = WebApplicationContextUtils
				.getRequiredWebApplicationContext(event.getServletContext());
		BeanObjectFactory.setContext(context);
		log.info("ϵͳ��ʼ�����");
	}

}
