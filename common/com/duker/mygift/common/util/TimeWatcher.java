/*
 * @(#)TimeWatcher.java Dec 20, 2009
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.common.util;

/**
 * <pre>
 * ��ʱ��
 * 
 * @author wangzh
 * 
 * @version 1.0
 * 
 * �޸İ汾: 1.0
 * �޸�����: Dec 20, 2009
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class TimeWatcher {

	/**
	 * ʱ������
	 */
	private long timeLimit = 0;

	/**
	 * ��ǰ��ʱʱ��
	 */
	private long currentTime = 0;

	public TimeWatcher() {
	}

	public TimeWatcher(long timeLimit) {
		this.timeLimit = timeLimit;
	}

	/**
	 * ��ʼ��ʱ
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
	 * ��ʼ��ʱ
	 * 
	 * @param timeLimit
	 *            ʱ������
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
