/*
 * @(#)OpenSessionInterceptor.java Apr 25, 2009
 * 
 * ��Ϣ��˹���ϵͳ
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
 * ����org.springframework.orm.hibernate4.support.OpenSessionInViewFilterʵ�ֵ�OpenSession������
 * 
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: Apr 25, 2009
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
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
	 * ����ҵ�����󱣳�OpenSessionInViewģʽ
	 * 
	 * @param joinPoint
	 *            ���ӵ����
	 * @return �����ط������صĽ��
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
