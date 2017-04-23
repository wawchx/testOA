/*
 * @(#)XmlWebApplicationContextInitializer.java Dec 25, 2013
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.common.support.spring;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: Dec 25, 2013
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class XmlWebApplicationContextInitializer implements
		ApplicationContextInitializer<XmlWebApplicationContext> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.context.ApplicationContextInitializer#initialize(
	 * org.springframework.context.ConfigurableApplicationContext)
	 */
	public void initialize(XmlWebApplicationContext applicationContext) {
		applicationContext.setAllowBeanDefinitionOverriding(false);
	}

}
