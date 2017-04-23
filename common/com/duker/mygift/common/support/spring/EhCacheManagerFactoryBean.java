/*
 * @(#)EhCacheManagerFactoryBean.java Feb 27, 2012
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.support.spring;

import java.io.IOException;
import java.io.InputStream;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Feb 27, 2012
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class EhCacheManagerFactoryBean implements FactoryBean<CacheManager>,
		InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Resource configLocation;

	private boolean shared = false;

	private String cacheManagerName;

	private CacheManager cacheManager;

	/**
	 * Set the location of the EHCache config file. A typical value is
	 * "/WEB-INF/ehcache.xml".
	 * <p>
	 * Default is "ehcache.xml" in the root of the class path, or if not found,
	 * "ehcache-failsafe.xml" in the EHCache jar (default EHCache
	 * initialization).
	 * 
	 * @see net.sf.ehcache.CacheManager#create(java.io.InputStream)
	 * @see net.sf.ehcache.CacheManager#CacheManager(java.io.InputStream)
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set whether the EHCache CacheManager should be shared (as a singleton at
	 * the VM level) or independent (typically local within the application).
	 * Default is "false", creating an independent instance.
	 * 
	 * @see net.sf.ehcache.CacheManager#create()
	 * @see net.sf.ehcache.CacheManager#CacheManager()
	 */
	public void setShared(boolean shared) {
		this.shared = shared;
	}

	/**
	 * Set the name of the EHCache CacheManager (if a specific name is desired).
	 * 
	 * @see net.sf.ehcache.CacheManager#setName(String)
	 */
	public void setCacheManagerName(String cacheManagerName) {
		this.cacheManagerName = cacheManagerName;
	}

	public void afterPropertiesSet() throws IOException, CacheException {
		logger.info("Initializing EHCache CacheManager");
		if (this.configLocation != null) {
			InputStream is = this.configLocation.getInputStream();
			try {
				Configuration configuration = ConfigurationFactory.parseConfiguration(is);
				if (this.cacheManagerName != null) {
					configuration.setName(this.cacheManagerName);
				}
				this.cacheManager = (this.shared ? CacheManager.create(configuration) : new CacheManager(configuration));
			}
			finally {
				is.close();
			}
		}
		else {
			Configuration configuration = ConfigurationFactory.parseConfiguration();
			if (this.cacheManagerName != null) {
				configuration.setName(this.cacheManagerName);
			}
			this.cacheManager = (this.shared ? CacheManager.create(configuration) : new CacheManager(configuration));
		}
	}

	public CacheManager getObject() {
		return this.cacheManager;
	}

	public Class<? extends CacheManager> getObjectType() {
		return (this.cacheManager != null ? this.cacheManager.getClass() : CacheManager.class);
	}

	public boolean isSingleton() {
		return true;
	}

	public void destroy() {
		logger.info("Shutting down EHCache CacheManager");
		this.cacheManager.shutdown();
	}

}
