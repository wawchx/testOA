/*
 * @(#)PaginatedAction.java 2009-6-25
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.struts.action;

import java.util.List;

import org.displaytag.properties.SortOrderEnum;

import com.duker.mygift.common.support.displaytag.PaginatedListImpl;
import com.duker.mygift.vo.PagedList;

/**
 * <pre>
 * ��ҳaction����
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2009-6-25
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
@SuppressWarnings("serial")
public abstract class PaginatedAction extends BaseAction {

	/**
	 * ����ʽ
	 */
	protected boolean asc = true;

	/**
	 * ������
	 */
	protected String sort;

	/**
	 * ��ǰҳ
	 */
	protected int pageNo = 1;

	/**
	 * ÿҳ��С,�̶�
	 */
	protected int pageSize = 15;

	/**
	 * ��ǰҳ��С
	 */
	protected int curPageSize;

	/**
	 * �ܽ����
	 */
	protected int resultSize;

	/**
	 * ��ҳ��
	 */
	protected int pageCount;

	/**
	 * ����б�
	 */
	protected PaginatedListImpl resultList;

	/**
	 * ҳ��html
	 */
	protected String pagingBanner;

	/**
	 * ҳ�����
	 */
	protected int groupCount = 9;

	/**
	 * js��ҳServerPageʵ������
	 */
	protected String pager;

	/**
	 * ҳ��д�뷽��
	 */
	protected String writePagefunc;

	/**
	 * ��ҳ
	 * 
	 * @param <T>
	 *            ����
	 * @param list
	 *            ��ǰҳ�б�
	 * @param resultSize
	 *            �ܼ�¼��
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
	 * ��ҳ
	 * 
	 * @param <T>
	 *            ����
	 * @param pList
	 *            ��ҳ����
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
