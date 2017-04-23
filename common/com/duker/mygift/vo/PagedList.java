/*
 * @(#)PagedList.java May 25, 2009
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.vo;

import java.io.Serializable;
import java.util.List;

/**
 * <pre>
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: May 25, 2009
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class PagedList<T> implements Serializable {

	/**
	 * 序列化版本号
	 */
	private static final long serialVersionUID = -5396518799352102881L;

	/**
	 * 当前页数据
	 */
	private List<T> list;

	/**
	 * 总记录数
	 */
	private int resultSize;

	public PagedList() {
	}

	public PagedList(List<T> list, int resultSize) {
		this.list = list;
		this.resultSize = resultSize;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public int getResultSize() {
		return resultSize;
	}

	public void setResultSize(int resultSize) {
		this.resultSize = resultSize;
	}

}
