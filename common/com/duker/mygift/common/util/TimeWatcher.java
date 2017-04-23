/*
 * @(#)TimeWatcher.java Dec 20, 2009
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.util;

/**
 * <pre>
 * 计时器
 * 
 * @author wangzh
 * 
 * @version 1.0
 * 
 * 修改版本: 1.0
 * 修改日期: Dec 20, 2009
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class TimeWatcher {

	/**
	 * 时间限制
	 */
	private long timeLimit = 0;

	/**
	 * 当前计时时间
	 */
	private long currentTime = 0;

	public TimeWatcher() {
	}

	public TimeWatcher(long timeLimit) {
		this.timeLimit = timeLimit;
	}

	/**
	 * 开始计时
	 */
	public void watch() {
		if (timeLimit > 0) {
			long elapsed = System.currentTimeMillis() - currentTime;

			if (elapsed < timeLimit) {
				try {
					Thread.sleep(timeLimit - elapsed);
				}
				catch (Exception ex) {
				}
			}
		}
		currentTime = System.currentTimeMillis();
	}

	/**
	 * 开始计时
	 * 
	 * @param timeLimit
	 *            时间限制
	 */
	public void watch(long timeLimit) {
		if (timeLimit > 0) {
			long elapsed = System.currentTimeMillis() - currentTime;

			if (elapsed < timeLimit) {
				try {
					Thread.sleep(timeLimit - elapsed);
				}
				catch (Exception ex) {
				}
			}
		}
		currentTime = System.currentTimeMillis();
	}

	public long getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(long timeLimit) {
		this.timeLimit = timeLimit;
	}

}
