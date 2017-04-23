/*
 * @(#)ProxyChecker.java Mar 3, 2014
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.util.http;

import java.io.Serializable;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Mar 3, 2014
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public interface ProxyChecker extends Serializable {

	/**
	 * 检验用代理取到的结果是否正确
	 * 
	 * @param status
	 *            http响应状态
	 * @param content
	 *            用代理取到的内容
	 * @return 内容是否正确
	 */
	boolean check(Integer status, String content);
}
