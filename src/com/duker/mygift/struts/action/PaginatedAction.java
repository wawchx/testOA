/*
 * @(#)PaginatedAction.java 2009-6-25
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.struts.action;

import java.util.List;

import org.displaytag.properties.SortOrderEnum;

import com.duker.mygift.common.support.displaytag.PaginatedListImpl;
import com.duker.mygift.vo.PagedList;

/**
 * <pre>
 * 分页action基类
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2009-6-25
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@SuppressWarnings("serial")
public abstract class PaginatedAction extends BaseAction {

	/**
	 * 排序方式
	 */
	protected boolean asc = true;

	/**
	 * 排序列
	 */
	protected String sort;

	/**
	 * 当前页
	 */
	protected int pageNo = 1;

	/**
	 * 每页大小,固定
	 */
	protected int pageSize = 15;

	/**
	 * 当前页大小
	 */
	protected int curPageSize;

	/**
	 * 总结果数
	 */
	protected int resultSize;

	/**
	 * 总页数
	 */
	protected int pageCount;

	/**
	 * 结果列表
	 */
	protected PaginatedListImpl resultList;

	/**
	 * 页码html
	 */
	protected String pagingBanner;

	/**
	 * 页码分组
	 */
	protected int groupCount = 9;

	/**
	 * js分页ServerPage实例对象
	 */
	protected String pager;

	/**
	 * 页面写入方法
	 */
	protected String writePagefunc;

	/**
	 * 分页
	 * 
	 * @param <T>
	 *            内容
	 * @param list
	 *            当前页列表
	 * @param resultSize
	 *            总记录数
	 */
	protected <T> void paginate(List<T> list, int resultSize) {
		this.curPageSize = (list != null ? list.size() : 0);
		this.resultSize = resultSize;
		this.resultSize = this.resultSize < 0 ? 0 : this.resultSize;
		this.pageCount = (this.resultSize + this.pageSize - 1) / this.pageSize;
		this.pageCount = this.pageCount < 0 ? 0 : this.pageCount;
		this.pageNo = this.pageNo < 1 ? 1 : this.pageNo;
		this.pageNo = this.pageNo > this.pageCount ? this.pageCount
				: this.pageNo;
		this.resultList = new PaginatedListImpl(list, this.pageNo,
				this.pageSize, this.resultSize, sort,
				asc ? SortOrderEnum.ASCENDING : SortOrderEnum.DESCENDING);
	}

	/**
	 * 分页
	 * 
	 * @param <T>
	 *            内容
	 * @param pList
	 *            分页对象
	 */
	protected <T> void paginate(PagedList<T> pList) {
		if (pList == null) {
			return;
		}
		List<T> list = pList.getList();
		this.curPageSize = (list != null ? list.size() : 0);
		this.resultSize = pList.getResultSize();
		this.resultSize = this.resultSize < 0 ? 0 : this.resultSize;
		this.pageCount = (this.resultSize + this.pageSize - 1) / this.pageSize;
		this.pageCount = this.pageCount < 0 ? 0 : this.pageCount;
		this.pageNo = this.pageNo < 1 ? 1 : this.pageNo;
		this.pageNo = this.pageNo > this.pageCount ? this.pageCount
				: this.pageNo;
		this.resultList = new PaginatedListImpl(list, this.pageNo,
				this.pageSize, this.resultSize, sort,
				asc ? SortOrderEnum.ASCENDING : SortOrderEnum.DESCENDING);
	}

	public int getCurPageSize() {
		return curPageSize;
	}

	public void setCurPageSize(int curPageSize) {
		this.curPageSize = curPageSize;
	}

	public boolean isAsc() {
		return asc;
	}

	public void setAsc(boolean asc) {
		this.asc = asc;
	}

	public void setGroupCount(int groupCount) {
		this.groupCount = groupCount;
	}

	public int getPageCount() {
		return pageCount;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getPagingBanner() {
		return pagingBanner;
	}

	public PaginatedListImpl getResultList() {
		return resultList;
	}

	public int getResultSize() {
		return resultSize;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getPager() {
		return pager;
	}

	public void setPager(String pager) {
		this.pager = pager;
	}

	public String getWritePagefunc() {
		return writePagefunc;
	}

	public void setWritePagefunc(String writePagefunc) {
		this.writePagefunc = writePagefunc;
	}

}
