/*
 * @(#)OpenSessionInterceptor.java Apr 25, 2009
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.support.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * <pre>
 * 参照org.springframework.orm.hibernate4.support.OpenSessionInViewFilter实现的OpenSession拦截器
 * 
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Apr 25, 2009
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class OpenSessionInterceptor {

	private SessionFactory sessionFactory;

	private static final Log logger = LogFactory
			.getLog(OpenSessionInterceptor.class);

	/**
	 * Set the SessionFactory bean
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Return the SessionFactory bean
	 */
	protected SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	/**
	 * 拦截业务请求保持OpenSessionInView模式
	 * 
	 * @param joinPoint
	 *            连接点对象
	 * @return 被拦截方法返回的结果
	 * @throws Throwable
	 */
	public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
		boolean participate = false;

		if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
			// Do not modify the Session: just set the participate flag.
			participate = true;
		}
		else {
			logger.debug("Opening Hibernate Session in OpenSessionInterceptor");
			Session session = openSession(sessionFactory);
			TransactionSynchronizationManager.bindResource(sessionFactory,
					new SessionHolder(session));
		}

		try {
			return joinPoint.proceed();
		}
		finally {
			if (!participate) {
				SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
						.unbindResource(sessionFactory);
				logger
						.debug("Closing Hibernate Session in OpenSessionInterceptor");
				SessionFactoryUtils.closeSession(sessionHolder.getSession());
			}
		}
	}

	/**
	 * Open a Session for the SessionFactory that this filter uses.
	 * <p>
	 * The default implementation delegates to the
	 * <code>SessionFactory.openSession</code> method and sets the
	 * <code>Session</code>'s flush mode to "MANUAL".
	 * 
	 * @param sessionFactory
	 *            the SessionFactory that this filter uses
	 * @return the Session to use
	 * @throws DataAccessResourceFailureException
	 *             if the Session could not be created
	 * @see org.hibernate.FlushMode#MANUAL
	 */
	protected Session openSession(SessionFactory sessionFactory)
			throws DataAccessResourceFailureException {
		try {
			Session session = sessionFactory.openSession();
			session.setFlushMode(FlushMode.MANUAL);
			return session;
		}
		catch (HibernateException ex) {
			throw new DataAccessResourceFailureException(
					"Could not open Hibernate Session", ex);
		}
	}

}
