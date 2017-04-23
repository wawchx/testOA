/*
 * @(#)XmlWebApplicationContextInitializer.java Dec 25, 2013
 * 
 * 信息审核管理系统
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
 * 修改版本: 0.9
 * 修改日期: Dec 25, 2013
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
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
