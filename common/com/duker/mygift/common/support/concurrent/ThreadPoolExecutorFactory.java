/*
 * @(#)ThreadPoolExecutorFactory.java 2008-4-28
 * 
 * Copy Right@ 广东普信科技有限公司
 */

package com.duker.mygift.common.support.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <pre>
 * ThreadPoolExecutor工厂类
 * 大部分代码是从ThreadPoolExecutorFactoryBean中改写的
 * 
 * @author wangzh
 * 
 * @version
 * 
 * 修改版本: 1.0
 * 修改日期: 2008-4-28
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人:
 * </pre>
 */
public class ThreadPoolExecutorFactory implements
		FactoryBean<ThreadPoolExecutor>, InitializingBean, DisposableBean {

	private final Object poolSizeMonitor = new Object();

	private int corePoolSize = 1;

	private int maxPoolSize = Integer.MAX_VALUE;

	private int keepAliveSeconds = 60;

	private int queueCapacity = Integer.MAX_VALUE;

	private ThreadFactory threadFactory = Executors.defaultThreadFactory();

	private ThreadPoolExecutor executor;

	private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

	/**
	 * Set the ThreadPoolExecutor's core pool size. Default is 1.
	 * <p>
	 * <b>This setting can be modified at runtime, for example through JMX.</b>
	 */
	public void setCorePoolSize(int corePoolSize) {
		synchronized (this.poolSizeMonitor) {
			this.corePoolSize = corePoolSize;
		}
	}

	/**
	 * Return the ThreadPoolExecutor's core pool size.
	 */
	public int getCorePoolSize() {
		synchronized (this.poolSizeMonitor) {
			return this.corePoolSize;
		}
	}

	/**
	 * Set the ThreadPoolExecutor's maximum pool size. Default is
	 * <code>Integer.MAX_VALUE</code>.
	 * <p>
	 * <b>This setting can be modified at runtime, for example through JMX.</b>
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		synchronized (this.poolSizeMonitor) {
			this.maxPoolSize = maxPoolSize;
		}
	}

	/**
	 * Return the ThreadPoolExecutor's maximum pool size.
	 */
	public int getMaxPoolSize() {
		synchronized (this.poolSizeMonitor) {
			return this.maxPoolSize;
		}
	}

	/**
	 * Set the ThreadPoolExecutor's keep-alive seconds. Default is 60.
	 * <p>
	 * <b>This setting can be modified at runtime, for example through JMX.</b>
	 */
	public void setKeepAliveSeconds(int keepAliveSeconds) {
		synchronized (this.poolSizeMonitor) {
			this.keepAliveSeconds = keepAliveSeconds;
		}
	}

	/**
	 * Return the ThreadPoolExecutor's keep-alive seconds.
	 */
	public int getKeepAliveSeconds() {
		synchronized (this.poolSizeMonitor) {
			return this.keepAliveSeconds;
		}
	}

	/**
	 * Set the capacity for the ThreadPoolExecutor's BlockingQueue. Default is
	 * <code>Integer.MAX_VALUE</code>.
	 * <p>
	 * Any positive value will lead to a LinkedBlockingQueue instance; any other
	 * value will lead to a SynchronousQueue instance.
	 * 
	 * @see java.util.concurrent.LinkedBlockingQueue
	 * @see java.util.concurrent.SynchronousQueue
	 */
	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	/**
	 * Get the capacity for the ThreadPoolExecutor's BlockingQueue.
	 * 
	 * @see java.util.concurrent.LinkedBlockingQueue
	 * @see java.util.concurrent.SynchronousQueue
	 */
	public int getQueueCapacity() {
		return this.queueCapacity;
	}

	/**
	 * Set the ThreadFactory to use for the ThreadPoolExecutor's thread pool.
	 * Default is the ThreadPoolExecutor's default thread factory.
	 * 
	 * @see java.util.concurrent.Executors#defaultThreadFactory()
	 */
	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = (threadFactory != null ? threadFactory : Executors
				.defaultThreadFactory());
	}

	/**
	 * Set the RejectedExecutionHandler to use for the ThreadPoolExecutor.
	 * Default is the ThreadPoolExecutor's default abort policy.
	 * 
	 * @see java.util.concurrent.ThreadPoolExecutor.AbortPolicy
	 */
	public void setRejectedExecutionHandler(
			RejectedExecutionHandler rejectedExecutionHandler) {
		this.rejectedExecutionHandler = (rejectedExecutionHandler != null ? rejectedExecutionHandler
				: new ThreadPoolExecutor.AbortPolicy());
	}

	/**
	 * Create the BlockingQueue to use for the ThreadPoolExecutor.
	 * <p>
	 * A LinkedBlockingQueue instance will be created for a positive capacity
	 * value; a SynchronousQueue else.
	 * 
	 * @param queueCapacity
	 *            the specified queue capacity
	 * @return the BlockingQueue instance
	 * @see java.util.concurrent.LinkedBlockingQueue
	 * @see java.util.concurrent.SynchronousQueue
	 */
	protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
		if (queueCapacity > 0) {
			return new LinkedBlockingQueue<Runnable>(queueCapacity);
		}
		return new SynchronousQueue<Runnable>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public ThreadPoolExecutor getObject() throws Exception {
		return executor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class<ThreadPoolExecutor> getObjectType() {
		return ThreadPoolExecutor.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		BlockingQueue<Runnable> queue = createQueue(this.queueCapacity);
		executor = new ThreadPoolExecutor(this.corePoolSize, this.maxPoolSize,
				this.keepAliveSeconds, TimeUnit.SECONDS, queue,
				this.threadFactory, this.rejectedExecutionHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
		if (executor != null) {
			executor.shutdown();
		}
	}

}
