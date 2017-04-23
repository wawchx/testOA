/*
 * @(#)CryptoPropertyPlaceholderConfigurer.java 2008-6-30
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.common.support.spring;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

/**
 * <pre>
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2008-6-30
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class CryptoPropertyPlaceholderConfigurer extends
		PropertySourcesPlaceholderConfigurer {

	/**
	 * ��Դ�ļ�
	 */
	protected Resource[] locations;

	/**
	 * �Ƿ�����Ҳ������ļ�
	 */
	protected boolean ignoreResourceNotFound = false;

	/**
	 * �����ļ���ȡ����
	 */
	protected PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();

	/**
	 * ���������ļ���ȡ����
	 */
	protected CryptoPropertiesPersister cryptoPropertiesPersister = new CryptoPropertiesPersister();

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.springframework.core.io.support.PropertiesLoaderSupport#
	 * setIgnoreResourceNotFound(boolean)
	 */
	@Override
	public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
		this.ignoreResourceNotFound = ignoreResourceNotFound;
		super.setIgnoreResourceNotFound(ignoreResourceNotFound);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.springframework.core.io.support.PropertiesLoaderSupport#
	 * setPropertiesPersister(org.springframework.util.PropertiesPersister)
	 */
	@Override
	public void setPropertiesPersister(PropertiesPersister propertiesPersister) {
		this.propertiesPersister = (propertiesPersister != null ? propertiesPersister
				: new DefaultPropertiesPersister());
		super.setPropertiesPersister(propertiesPersister);
	}

	/**
	 * @param cryptoPropertiesPersister
	 *            the cryptoPropertiesPersister to set
	 */
	public void setCryptoPropertiesPersister(
			CryptoPropertiesPersister cryptoPropertiesPersister) {
		this.cryptoPropertiesPersister = cryptoPropertiesPersister;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.core.io.support.PropertiesLoaderSupport#setLocation
	 * (org.springframework.core.io.Resource)
	 */
	@Override
	public void setLocation(Resource location) {
		this.locations = new Resource[] { location };
		super.setLocation(location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.core.io.support.PropertiesLoaderSupport#setLocations
	 * (org.springframework.core.io.Resource[])
	 */
	@Override
	public void setLocations(Resource... locations) {
		this.locations = (locations != null ? locations.clone() : null);
		super.setLocations(this.locations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.core.io.support.PropertiesLoaderSupport#loadProperties
	 * (java.util.Properties)
	 */
	@Override
	protected void loadProperties(Properties props) throws IOException {
		if (this.locations == null) {
			return;
		}

		for (Resource location : this.locations) {
			if (logger.isInfoEnabled()) {
				logger.info("Loading properties file from " + location);
			}

			InputStream is = null;
			try {
				String filename = location.getFilename();
				if (filename != null && filename.endsWith(".xml")) {
					is = location.getInputStream();
					this.propertiesPersister.loadFromXml(props, is);
				}
				else {
					this.cryptoPropertiesPersister.load(props, location);
				}
			}
			catch (IOException ex) {
				if (this.ignoreResourceNotFound) {
					if (logger.isWarnEnabled()) {
						logger.warn("Could not load properties from "
								+ location, ex);
					}
				}
				else {
					throw ex;
				}
			}
			finally {
				if (is != null) {
					try {
						is.close();
					}
					catch (Exception ex) {
					}
				}
			}
		}
	}

}
