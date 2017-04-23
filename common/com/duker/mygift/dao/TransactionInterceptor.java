/*
 * @(#)TransactionInterceptor.java 2010-5-24
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;

/**
 * <pre>
 * ��������������
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2010-5-24
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class TransactionInterceptor {

	/** Delegate used to create, commit and rollback transactions */
	private PlatformTransactionManager transactionManager;

	/**
	 * <pre>
	 * ������Ҫ����һ������
	 * propagationBehaviorȡTransactionInfo.PROPAGATION_REQUIRED
	 * isolationLevelȡTransactionInfo.ISOLATION_DEFAULT
	 * readOnly=false
	 * �κ��쳣���ع�
	 * </pre>
	 */
	public void createTransactionIfNecessary() {
		List<RollbackRuleAttribute> rollbackRules = new ArrayList<RollbackRuleAttribute>(
				1);
		rollbackRules.add(new RollbackRuleAttribute(Throwable.class));
		RuleBasedTransactionAttribute txAttr = new RuleBasedTransactionAttribute();
		txAttr.setRollbackRules(rollbackRules);

		TransactionStatus status = null;
		if (transactionManager != null) {
			status = transactionManager.getTransaction(txAttr);
		}
		TransactionInfo txInfo = new TransactionInfo(txAttr);
		txInfo.newTransactionStatus(status);
		// �󶨵��̱߳���
		txInfo.bindToThread();
	}

	/**
	 * <pre>
	 * ������Ҫ����һ������
	 * propagationBehaviorȡTransactionInfo.PROPAGATION_REQUIRED
	 * isolationLevelȡTransactionInfo.ISOLATION_DEFAULT
	 * �κ��쳣���ع�
	 * @param readOnly �Ƿ���ֻ������
	 * </pre>
	 */
	public void createTransactionIfNecessary(boolean readOnly) {
		List<RollbackRuleAttribute> rollbackRules = new ArrayList<RollbackRuleAttribute>(
				1);
		rollbackRules.add(new RollbackRuleAttribute(Throwable.class));
		RuleBasedTransactionAttribute txAttr = new RuleBasedTransactionAttribute();
		txAttr.setRollbackRules(rollbackRules);
		txAttr.setReadOnly(readOnly);

		TransactionStatus status = null;
		if (transactionManager != null) {
			status = transactionManager.getTransaction(txAttr);
		}
		TransactionInfo txInfo = new TransactionInfo(txAttr);
		txInfo.newTransactionStatus(status);
		// �󶨵��̱߳���
		txInfo.bindToThread();
	}

	/**
	 * <pre>
	 * ������Ҫ����һ������
	 * propagationBehaviorȡTransactionInfo.PROPAGATION_REQUIRED
	 * isolationLevelȡTransactionInfo.ISOLATION_DEFAULT
	 * @param readOnly �Ƿ���ֻ������
	 * @param rollbackList ��Ҫ�ع����쳣�б�
	 * </pre>
	 */
	public void createTransactionIfNecessary(boolean readOnly,
			List<Throwable> rollbackList) {
		List<RollbackRuleAttribute> rollbackRules = new ArrayList<RollbackRuleAttribute>();
		if (rollbackList != null && !rollbackList.isEmpty()) {
			for (Throwable ex : rollbackList) {
				rollbackRules.add(new RollbackRuleAttribute(ex.getClass()));
			}
		}
		else {
			rollbackRules.add(new RollbackRuleAttribute(Throwable.class));
		}
		RuleBasedTransactionAttribute txAttr = new RuleBasedTransactionAttribute();
		txAttr.setRollbackRules(rollbackRules);
		txAttr.setReadOnly(readOnly);

		TransactionStatus status = null;
		if (transactionManager != null) {
			status = transactionManager.getTransaction(txAttr);
		}
		TransactionInfo txInfo = new TransactionInfo(txAttr);
		txInfo.newTransactionStatus(status);
		// �󶨵��̱߳���
		txInfo.bindToThread();
	}

	/**
	 * ������Ҫ����һ������
	 * 
	 * @param propagationBehavior
	 *            ���񴫲���Ϊ,ȡֵTransactionInfo.PROPAGATION_XXX
	 * @param isolationLevel
	 *            ������뼶��,ȡֵTransactionInfo.ISOLATION_XXX
	 * @param timeout
	 *            ����ʱʱ��
	 * @param readOnly
	 *            �Ƿ���ֻ������
	 * @param rollbackList
	 *            ��Ҫ�ع����쳣�б�
	 */
	public void createTransactionIfNecessary(int propagationBehavior,
			int isolationLevel, int timeout, boolean readOnly,
			List<Throwable> rollbackList) {
		List<RollbackRuleAttribute> rollbackRules = new ArrayList<RollbackRuleAttribute>();
		if (rollbackList != null && !rollbackList.isEmpty()) {
			for (Throwable ex : rollbackList) {
				rollbackRules.add(new RollbackRuleAttribute(ex.getClass()));
			}
		}
		else {
			rollbackRules.add(new RollbackRuleAttribute(Throwable.class));
		}

		RuleBasedTransactionAttribute txAttr = new RuleBasedTransactionAttribute(
				propagationBehavior, rollbackRules);
		txAttr.setIsolationLevel(isolationLevel);
		txAttr.setTimeout(timeout);
		txAttr.setReadOnly(readOnly);

		TransactionStatus status = null;
		if (transactionManager != null) {
			status = transactionManager.getTransaction(txAttr);
		}
		TransactionInfo txInfo = new TransactionInfo(txAttr);
		txInfo.newTransactionStatus(status);
		// �󶨵��̱߳���
		txInfo.bindToThread();
	}

	/**
	 * �����쳣ʱִ��,�����쳣�ж��Ƿ���Ҫ�ع�
	 * 
	 * @param ex
	 *            �쳣
	 */
	public void completeTransactionAfterThrowing(Throwable ex) {
		if (transactionManager == null) {
			return;
		}
		TransactionInfo txInfo = TransactionInfo.getCurrentTransactionInfo();
		if (txInfo == null) {
			return;
		}
		txInfo.restoreThreadLocalStatus();
		if (!txInfo.hasTransaction()) {
			return;
		}
		if (txInfo.getTransactionAttribute().rollbackOn(ex)) {
			try {
				transactionManager.rollback(txInfo.getTransactionStatus());
			}
			catch (TransactionSystemException ex2) {
				ex2.initApplicationException(ex);
				throw ex2;
			}
			catch (RuntimeException ex2) {
				throw ex2;
			}
			catch (Error err) {
				throw err;
			}
		}
		else {
			// We don't roll back on this exception.
			// Will still roll back if TransactionStatus.isRollbackOnly() is
			// true.
			try {
				transactionManager.commit(txInfo.getTransactionStatus());
			}
			catch (TransactionSystemException ex2) {
				ex2.initApplicationException(ex);
				throw ex2;
			}
			catch (RuntimeException ex2) {
				throw ex2;
			}
			catch (Error err) {
				throw err;
			}
		}
	}

	/**
	 * ����ִ�����
	 */
	public void commitTransactionAfterReturning() {
		if (transactionManager == null) {
			return;
		}
		TransactionInfo txInfo = TransactionInfo.getCurrentTransactionInfo();
		if (txInfo == null) {
			return;
		}
		txInfo.restoreThreadLocalStatus();
		if (!txInfo.hasTransaction()) {
			return;
		}
		transactionManager.commit(txInfo.getTransactionStatus());
	}

	/**
	 * <pre>
	 * ִ��һ�δ�����Ĵ���
	 * 
	 * propagationBehaviorȡTransactionInfo.PROPAGATION_REQUIRED
	 * isolationLevelȡTransactionInfo.ISOLATION_DEFAULT
	 * readOnly=false
	 * �κ��쳣���ع�
	 * 
	 * @param <T>
	 *            ���ؽ������
	 * @param callBack
	 *            �ص�����
	 * @return ִ�н��
	 * @throws Throwable
	 * </pre>
	 */
	public <T> T doTransaction(TransactionCallback<T> callBack)
			throws Throwable {
		if (callBack == null) {
			return null;
		}
		createTransactionIfNecessary();
		T result = null;

		try {
			result = callBack.doTransaction();
		}
		catch (Throwable ex) {
			completeTransactionAfterThrowing(ex);

			throw ex;
		}
		commitTransactionAfterReturning();

		return result;
	}

	/**
	 * <pre>
	 * ִ��һ�δ�����Ĵ���
	 * 
	 * propagationBehaviorȡTransactionInfo.PROPAGATION_REQUIRED
	 * isolationLevelȡTransactionInfo.ISOLATION_DEFAULT
	 * �κ��쳣���ع�
	 * 
	 * @param <T>
	 *            ���ؽ������
	 * @param readOnly
	 *            �Ƿ���ֻ������
	 * @param callBack
	 *            �ص�����
	 * @return ִ�н��
	 * @throws Throwable
	 * </pre>
	 */
	public <T> T doTransaction(boolean readOnly, TransactionCallback<T> callBack)
			throws Throwable {
		if (callBack == null) {
			return null;
		}
		createTransactionIfNecessary(readOnly);
		T result = null;

		try {
			result = callBack.doTransaction();
		}
		catch (Throwable ex) {
			completeTransactionAfterThrowing(ex);

			throw ex;
		}
		commitTransactionAfterReturning();

		return result;
	}

	/**
	 * <pre>
	 * ִ��һ�δ�����Ĵ���
	 * 
	 * propagationBehaviorȡTransactionInfo.PROPAGATION_REQUIRED
	 * isolationLevelȡTransactionInfo.ISOLATION_DEFAULT
	 * 
	 * @param <T>
	 *            ���ؽ������
	 * @param readOnly
	 *            �Ƿ���ֻ������
	 * @param rollbackList
	 *            ��Ҫ�ع����쳣�б�
	 * @param callBack
	 *            �ص�����
	 * @return ִ�н��
	 * @throws Throwable
	 * </pre>
	 */
	public <T> T doTransaction(boolean readOnly, List<Throwable> rollbackList,
			TransactionCallback<T> callBack) throws Throwable {
		if (callBack == null) {
			return null;
		}
		createTransactionIfNecessary(readOnly, rollbackList);
		T result = null;

		try {
			result = callBack.doTransaction();
		}
		catch (Throwable ex) {
			completeTransactionAfterThrowing(ex);

			throw ex;
		}
		commitTransactionAfterReturning();

		return result;
	}

	/**
	 * ִ��һ�δ�����Ĵ���
	 * 
	 * @param <T>
	 *            ���ؽ������
	 * @param propagationBehavior
	 *            ���񴫲���Ϊ,ȡֵTransactionInfo.PROPAGATION_XXX
	 * @param isolationLevel
	 *            ������뼶��,ȡֵTransactionInfo.ISOLATION_XXX
	 * @param timeout
	 *            ����ʱʱ��
	 * @param readOnly
	 *            �Ƿ���ֻ������
	 * @param rollbackList
	 *            ��Ҫ�ع����쳣�б�
	 * @param callBack
	 *            �ص�����
	 * @return ִ�н��
	 * @throws Throwable
	 */
	public <T> T doTransaction(int propagationBehavior, int isolationLevel,
			int timeout, boolean readOnly, List<Throwable> rollbackList,
			TransactionCallback<T> callBack) throws Throwable {
		if (callBack == null) {
			return null;
		}
		createTransactionIfNecessary(propagationBehavior, isolationLevel,
				timeout, readOnly, rollbackList);
		T result = null;

		try {
			result = callBack.doTransaction();
		}
		catch (Throwable ex) {
			completeTransactionAfterThrowing(ex);

			throw ex;
		}
		commitTransactionAfterReturning();

		return result;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

}
