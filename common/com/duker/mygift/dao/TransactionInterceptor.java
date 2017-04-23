/*
 * @(#)TransactionInterceptor.java 2010-5-24
 * 
 * 信息审核管理系统
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
 * 事务事务拦截器
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2010-5-24
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class TransactionInterceptor {

	/** Delegate used to create, commit and rollback transactions */
	private PlatformTransactionManager transactionManager;

	/**
	 * <pre>
	 * 根据需要生成一个事务
	 * propagationBehavior取TransactionInfo.PROPAGATION_REQUIRED
	 * isolationLevel取TransactionInfo.ISOLATION_DEFAULT
	 * readOnly=false
	 * 任何异常都回滚
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
		// 绑定到线程变量
		txInfo.bindToThread();
	}

	/**
	 * <pre>
	 * 根据需要生成一个事务
	 * propagationBehavior取TransactionInfo.PROPAGATION_REQUIRED
	 * isolationLevel取TransactionInfo.ISOLATION_DEFAULT
	 * 任何异常都回滚
	 * @param readOnly 是否是只读事务
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
		// 绑定到线程变量
		txInfo.bindToThread();
	}

	/**
	 * <pre>
	 * 根据需要生成一个事务
	 * propagationBehavior取TransactionInfo.PROPAGATION_REQUIRED
	 * isolationLevel取TransactionInfo.ISOLATION_DEFAULT
	 * @param readOnly 是否是只读事务
	 * @param rollbackList 需要回滚的异常列表
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
		// 绑定到线程变量
		txInfo.bindToThread();
	}

	/**
	 * 根据需要生成一个事务
	 * 
	 * @param propagationBehavior
	 *            事务传播行为,取值TransactionInfo.PROPAGATION_XXX
	 * @param isolationLevel
	 *            事务隔离级别,取值TransactionInfo.ISOLATION_XXX
	 * @param timeout
	 *            事务超时时间
	 * @param readOnly
	 *            是否是只读事务
	 * @param rollbackList
	 *            需要回滚的异常列表
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
		// 绑定到线程变量
		txInfo.bindToThread();
	}

	/**
	 * 发生异常时执行,根据异常判断是否需要回滚
	 * 
	 * @param ex
	 *            异常
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
	 * 正常执行完毕
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
	 * 执行一段带事务的代码
	 * 
	 * propagationBehavior取TransactionInfo.PROPAGATION_REQUIRED
	 * isolationLevel取TransactionInfo.ISOLATION_DEFAULT
	 * readOnly=false
	 * 任何异常都回滚
	 * 
	 * @param <T>
	 *            返回结果类型
	 * @param callBack
	 *            回调函数
	 * @return 执行结果
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
	 * 执行一段带事务的代码
	 * 
	 * propagationBehavior取TransactionInfo.PROPAGATION_REQUIRED
	 * isolationLevel取TransactionInfo.ISOLATION_DEFAULT
	 * 任何异常都回滚
	 * 
	 * @param <T>
	 *            返回结果类型
	 * @param readOnly
	 *            是否是只读事务
	 * @param callBack
	 *            回调函数
	 * @return 执行结果
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
	 * 执行一段带事务的代码
	 * 
	 * propagationBehavior取TransactionInfo.PROPAGATION_REQUIRED
	 * isolationLevel取TransactionInfo.ISOLATION_DEFAULT
	 * 
	 * @param <T>
	 *            返回结果类型
	 * @param readOnly
	 *            是否是只读事务
	 * @param rollbackList
	 *            需要回滚的异常列表
	 * @param callBack
	 *            回调函数
	 * @return 执行结果
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
	 * 执行一段带事务的代码
	 * 
	 * @param <T>
	 *            返回结果类型
	 * @param propagationBehavior
	 *            事务传播行为,取值TransactionInfo.PROPAGATION_XXX
	 * @param isolationLevel
	 *            事务隔离级别,取值TransactionInfo.ISOLATION_XXX
	 * @param timeout
	 *            事务超时时间
	 * @param readOnly
	 *            是否是只读事务
	 * @param rollbackList
	 *            需要回滚的异常列表
	 * @param callBack
	 *            回调函数
	 * @return 执行结果
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
