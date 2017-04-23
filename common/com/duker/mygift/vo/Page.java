/*
 * Page.java
 * 
 * Copyright @ 信息审核管理系统
 * 
 */
package com.duker.mygift.vo;

import java.io.Serializable;

/**
 * <pre>
 * 分页
 * 
 * @author wangzh
 * @version 1.0
 * 
 * 修改版本: 1.0
 * 修改日期：2008-5-5
 * 修改人 : wangzh
 * 修改说明：形成初始版本
 * </pre>
 */
public class Page implements Serializable {

	/**
	 * 序列化版本号
	 */
	private static final long serialVersionUID = -4899453280523144735L;

	/**
	 * 默认页面大小
	 */
	public static final int DEFAULT_PAGESIZE = 10;

	/**
	 * 总记录数, 可写
	 */
	private int resultCount;

	/**
	 * 总页数
	 */
	private int pageCount;

	/**
	 * 当前页码, 初始化时传入
	 */
	private int pageNo;

	/**
	 * 每页记录数, 初始化时传入
	 */
	private int pageSize;

	public Page() {
		this(0, DEFAULT_PAGESIZE);
	}

	public Page(int pageNo, int pageSize) {
		this.pageNo = (pageNo < 0) ? 0 : pageNo;
		this.pageSize = (pageSize <= 0) ? DEFAULT_PAGESIZE : pageSize;
	}

	// 计算总页数
	private void calculatePageCount() {
		pageCount = (resultCount + pageSize - 1) / pageSize;
		if (pageCount <= 0) {
			if (resultCount > 0) {
				pageCount = 1;
			}
			else {
				pageCount = 0;
			}
		}
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = (pageNo < 0) ? 0 : pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = (pageSize <= 0) ? DEFAULT_PAGESIZE : pageSize;
		calculatePageCount();
	}

	public int getResultCount() {
		return resultCount;
	}

	public void setResultCount(int resultCount) {
		this.resultCount = resultCount < 0 ? 0 : resultCount;
		calculatePageCount();
	}

	public int getPageCount() {
		return pageCount;
	}

	public int getFirstResult() {
		return pageNo * pageSize;
	}

	public int getCurrentPageSize() {
		if (resultCount == 0) {
			return 0;
		}

		int lastIndex = pageCount - 1;

		if (pageNo < lastIndex) {
			return pageSize;
		}
		else if (pageNo == lastIndex) {
			return resultCount - (pageNo * pageSize);
		}
		else {
			return 0;
		}
	}

	public boolean isFirstPage() {
		return pageNo == 0;
	}

	public boolean isLastPage() {
		return pageNo == pageCount;
	}
}
