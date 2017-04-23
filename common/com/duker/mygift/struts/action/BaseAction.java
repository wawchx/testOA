/*
 * @(#)BaseAction.java Apr 30, 2009
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.struts.action;

import com.opensymphony.xwork2.ActionSupport;

/**
 * <pre>
 * 
 * Action基类
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: Apr 30, 2009
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@SuppressWarnings("serial")
public abstract class BaseAction extends ActionSupport {

	/**
	 * 跳转控制字符串
	 */
	public static final String RESULTINFO = "resultInfo";

	/**
	 * 响应码 0成功 其他失败
	 */
	protected int retCode = 0;

	/**
	 * 错误信息
	 */
	protected String retMsg;

	public int getRetCode() {
		return retCode;
	}

	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}

	public String getRetMsg() {
		return retMsg;
	}

	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}

}
