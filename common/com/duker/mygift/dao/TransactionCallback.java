/*
 * @(#)TransactionCallback.java 2010-5-24
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.dao;

/**
 * <pre>
 * 事务执行回调函数
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
public interface TransactionCallback<T> {

	/**
	 * 执行一段带事务的代码
	 * 
	 * @return 返回结果
	 */
	T doTransaction() throws Throwable;

}
