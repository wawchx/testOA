/*
 * Page.java
 * 
 * Copyright @ ��Ϣ��˹���ϵͳ
 * 
 */
package com.duker.mygift.vo;

import java.io.Serializable;

/**
 * <pre>
 * ��ҳ
 * 
 * @author wangzh
 * @version 1.0
 * 
 * �޸İ汾: 1.0
 * �޸����ڣ�2008-5-5
 * �޸��� : wangzh
 * �޸�˵�����γɳ�ʼ�汾
 * </pre>
 */
public class Page implements Serializable {

	/**
	 * ���л��汾��
	 */
	private static final long serialVersionUID = -4899453280523144735L;

	/**
	 * Ĭ��ҳ���С
	 */
	public static final int DEFAULT_PAGESIZE = 10;

	/**
	 * �ܼ�¼��, ��д
	 */
	private int resultCount;

	/**
	 * ��ҳ��
	 */
	private int pageCount;

	/**
	 * ��ǰҳ��, ��ʼ��ʱ����
	 */
	private int pageNo;

	/**
	 * ÿҳ��¼��, ��ʼ��ʱ����
	 */
	private int pageSize;

	public Page() {
		this(0, DEFAULT_PAGESIZE);
	}

	public Page(int pageNo, int pageSize) {
		this.pageNo = (pageNo < 0) ? 0 : pageNo;
		this.pageSize = (pageSize <= 0) ? DEFAULT_PAGESIZE : pageSize;
	}

	// ������ҳ��
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
