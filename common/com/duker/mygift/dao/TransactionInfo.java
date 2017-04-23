/*
 * @(#)TransactionInfo.java May 23, 2010
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.dao;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * <pre>
 * 保持事务信息
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: May 23, 2010
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class TransactionInfo {

	/**
	 * Support a current transaction; create a new one if none exists. Analogous
	 * to the EJB transaction attribute of the same name.
	 * <p>
	 * This is typically the default setting of a transaction definition, and
	 * typically defines a transaction synchronization scope.
	 */
	public static final int PROPAGATION_REQUIRED = TransactionDefinition.PROPAGATION_REQUIRED;

	/**
	 * Support a current transaction; execute non-transactionally if none
	 * exists. Analogous to the EJB transaction attribute of the same name.
	 * <p>
	 * <b>NOTE:</b> For transaction managers with transaction synchronization,
	 * <code>PROPAGATION_SUPPORTS</code> is slightly different from no
	 * transaction at all, as it defines a transaction scope that
	 * synchronization might apply to. As a consequence, the same resources (a
	 * JDBC <code>Connection</code>, a Hibernate <code>Session</code>, etc) will
	 * be shared for the entire specified scope. Note that the exact behavior
	 * depends on the actual synchronization configuration of the transaction
	 * manager!
	 * <p>
	 * In general, use <code>PROPAGATION_SUPPORTS</code> with care! In
	 * particular, do not rely on <code>PROPAGATION_REQUIRED</code> or
	 * <code>PROPAGATION_REQUIRES_NEW</code> <i>within</i> a
	 * <code>PROPAGATION_SUPPORTS</code> scope (which may lead to
	 * synchronization conflicts at runtime). If such nesting is unavoidable,
	 * make sure to configure your transaction manager appropriately (typically
	 * switching to "synchronization on actual transaction").
	 * 
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setTransactionSynchronization(int)
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#SYNCHRONIZATION_ON_ACTUAL_TRANSACTION
	 */
	public static final int PROPAGATION_SUPPORTS = TransactionDefinition.PROPAGATION_SUPPORTS;

	/**
	 * Support a current transaction; throw an exception if no current
	 * transaction exists. Analogous to the EJB transaction attribute of the
	 * same name.
	 * <p>
	 * Note that transaction synchronization within a
	 * <code>PROPAGATION_MANDATORY</code> scope will always be driven by the
	 * surrounding transaction.
	 */
	public static final int PROPAGATION_MANDATORY = TransactionDefinition.PROPAGATION_MANDATORY;

	/**
	 * Create a new transaction, suspending the current transaction if one
	 * exists. Analogous to the EJB transaction attribute of the same name.
	 * <p>
	 * <b>NOTE:</b> Actual transaction suspension will not work out-of-the-box
	 * on all transaction managers. This in particular applies to
	 * {@link org.springframework.transaction.jta.JtaTransactionManager}, which
	 * requires the <code>javax.transaction.TransactionManager</code> to be made
	 * available it to it (which is server-specific in standard J2EE).
	 * <p>
	 * A <code>PROPAGATION_REQUIRES_NEW</code> scope always defines its own
	 * transaction synchronizations. Existing synchronizations will be suspended
	 * and resumed appropriately.
	 * 
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager(javax.transaction.TransactionManager)
	 */
	public static final int PROPAGATION_REQUIRES_NEW = TransactionDefinition.PROPAGATION_REQUIRES_NEW;

	/**
	 * Do not support a current transaction; rather always execute
	 * non-transactionally. Analogous to the EJB transaction attribute of the
	 * same name.
	 * <p>
	 * <b>NOTE:</b> Actual transaction suspension will not work out-of-the-box
	 * on all transaction managers. This in particular applies to
	 * {@link org.springframework.transaction.jta.JtaTransactionManager}, which
	 * requires the <code>javax.transaction.TransactionManager</code> to be made
	 * available it to it (which is server-specific in standard J2EE).
	 * <p>
	 * Note that transaction synchronization is <i>not</i> available within a
	 * <code>PROPAGATION_NOT_SUPPORTED</code> scope. Existing synchronizations
	 * will be suspended and resumed appropriately.
	 * 
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager(javax.transaction.TransactionManager)
	 */
	public static final int PROPAGATION_NOT_SUPPORTED = TransactionDefinition.PROPAGATION_NOT_SUPPORTED;

	/**
	 * Do not support a current transaction; throw an exception if a current
	 * transaction exists. Analogous to the EJB transaction attribute of the
	 * same name.
	 * <p>
	 * Note that transaction synchronization is <i>not</i> available within a
	 * <code>PROPAGATION_NEVER</code> scope.
	 */
	public static final int PROPAGATION_NEVER = TransactionDefinition.PROPAGATION_NEVER;

	/**
	 * Execute within a nested transaction if a current transaction exists,
	 * behave like {@link #PROPAGATION_REQUIRED} else. There is no analogous
	 * feature in EJB.
	 * <p>
	 * <b>NOTE:</b> Actual creation of a nested transaction will only work on
	 * specific transaction managers. Out of the box, this only applies to the
	 * JDBC
	 * {@link org.springframework.jdbc.datasource.DataSourceTransactionManager}
	 * when working on a JDBC 3.0 driver. Some JTA providers might support
	 * nested transactions as well.
	 * 
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
	 */
	public static final int PROPAGATION_NESTED = TransactionDefinition.PROPAGATION_NESTED;

	/**
	 * Use the default isolation level of the underlying datastore. All other
	 * levels correspond to the JDBC isolation levels.
	 * 
	 * @see java.sql.Connection
	 */
	public static final int ISOLATION_DEFAULT = TransactionDefinition.ISOLATION_DEFAULT;

	/**
	 * Indicates that dirty reads, non-repeatable reads and phantom reads can
	 * occur.
	 * <p>
	 * This level allows a row changed by one transaction to be read by another
	 * transaction before any changes in that row have been committed (a "dirty
	 * read"). If any of the changes are rolled back, the second transaction
	 * will have retrieved an invalid row.
	 * 
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 */
	public static final int ISOLATION_READ_UNCOMMITTED = TransactionDefinition.ISOLATION_READ_UNCOMMITTED;

	/**
	 * Indicates that dirty reads are prevented; non-repeatable reads and
	 * phantom reads can occur.
	 * <p>
	 * This level only prohibits a transaction from reading a row with
	 * uncommitted changes in it.
	 * 
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 */
	public static final int ISOLATION_READ_COMMITTED = TransactionDefinition.ISOLATION_READ_COMMITTED;

	/**
	 * Indicates that dirty reads and non-repeatable reads are prevented;
	 * phantom reads can occur.
	 * <p>
	 * This level prohibits a transaction from reading a row with uncommitted
	 * changes in it, and it also prohibits the situation where one transaction
	 * reads a row, a second transaction alters the row, and the first
	 * transaction rereads the row, getting different values the second time (a
	 * "non-repeatable read").
	 * 
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 */
	public static final int ISOLATION_REPEATABLE_READ = TransactionDefinition.ISOLATION_REPEATABLE_READ;

	/**
	 * Indicates that dirty reads, non-repeatable reads and phantom reads are
	 * prevented.
	 * <p>
	 * This level includes the prohibitions in
	 * {@link #ISOLATION_REPEATABLE_READ} and further prohibits the situation
	 * where one transaction reads all rows that satisfy a <code>WHERE</code>
	 * condition, a second transaction inserts a row that satisfies that
	 * <code>WHERE</code> condition, and the first transaction rereads for the
	 * same condition, retrieving the additional "phantom" row in the second
	 * read.
	 * 
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 */
	public static final int ISOLATION_SERIALIZABLE = TransactionDefinition.ISOLATION_SERIALIZABLE;

	/**
	 * Use the default timeout of the underlying transaction system, or none if
	 * timeouts are not supported.
	 */
	public static final int TIMEOUT_DEFAULT = TransactionDefinition.TIMEOUT_DEFAULT;

	/**
	 * 保存事务到当前线程
	 */
	private static final ThreadLocal<TransactionInfo> transactionInfoHolder = new ThreadLocal<TransactionInfo>() {

		private final String name = "Current manual-driven transaction";

		public String toString() {
			return this.name;
		}
	};

	private final TransactionAttribute transactionAttribute;

	private TransactionStatus transactionStatus;

	private TransactionInfo oldTransactionInfo;

	public static TransactionInfo getCurrentTransactionInfo() {
		return transactionInfoHolder.get();
	}

	public TransactionInfo(TransactionAttribute transactionAttribute) {
		this.transactionAttribute = transactionAttribute;
	}

	public TransactionAttribute getTransactionAttribute() {
		return this.transactionAttribute;
	}

	public void newTransactionStatus(TransactionStatus status) {
		this.transactionStatus = status;
	}

	public TransactionStatus getTransactionStatus() {
		return this.transactionStatus;
	}

	/**
	 * Return whether a transaction was created by this aspect, or whether we
	 * just have a placeholder to keep ThreadLocal stack integrity.
	 */
	public boolean hasTransaction() {
		return (this.transactionStatus != null);
	}

	public void bindToThread() {
		// Expose current TransactionStatus, preserving any existing
		// TransactionStatus
		// for restoration after this transaction is complete.
		this.oldTransactionInfo =  transactionInfoHolder.get();
		transactionInfoHolder.set(this);
	}

	public void restoreThreadLocalStatus() {
		// Use stack to restore old transaction TransactionInfo.
		// Will be null if none was set.
		transactionInfoHolder.set(this.oldTransactionInfo);
	}

	public String toString() {
		return this.transactionAttribute.toString();
	}

}
