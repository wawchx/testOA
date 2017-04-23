/*
 * @(#)PaginatedListImpl.java 2009-6-25
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.support.displaytag;

import java.util.List;

import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;

/**
 * <pre>
 * displaytag分页
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
public class PaginatedListImpl implements PaginatedList {

	/**
	 * the current partial list
	 */
	private List<?> list;

	/**
	 * the page number of the partial list (starts from 1)
	 */
	private int pageNumber = 1;

	/**
	 * the number of objects per page. Unless this page is the last one the
	 * partial list should thus have a size equal to the result of this method
	 */
	private int objectsPerPage = 14;

	/**
	 * the size of the full list
	 */
	private int fullListSize = 0;

	/**
	 * the sort criterion used to externally sort the full list
	 */
	private String sortCriterion;

	/**
	 * the sort direction used to externally sort the full list
	 */
	private SortOrderEnum sortDirection;

	/**
	 * an ID for the search used to get the list. It may be null. Such an ID can
	 * be necessary if the full list is cached, in a way or another (in the
	 * session, in the business tier, or anywhere else), to be able to retrieve
	 * the full list from the cache
	 */
	private String searchId;

	public PaginatedListImpl() {
	}

	public PaginatedListImpl(List<?> list, int pageNumber, int objectsPerPage,
			int fullListSize, String sortCriterion) {
		this.list = list;
		this.pageNumber = pageNumber;
		this.objectsPerPage = objectsPerPage;
		this.fullListSize = fullListSize;
		this.sortCriterion = sortCriterion;
	}

	public PaginatedListImpl(List<?> list, int pageNumber, int objectsPerPage,
			int fullListSize, String sortCriterion, SortOrderEnum sortDirection) {
		this.list = list;
		this.pageNumber = pageNumber;
		this.objectsPerPage = objectsPerPage;
		this.fullListSize = fullListSize;
		this.sortCriterion = sortCriterion;
		this.sortDirection = sortDirection;
	}

	public PaginatedListImpl(List<?> list, int pageNumber, int objectsPerPage,
			int fullListSize, String sortCriterion,
			SortOrderEnum sortDirection, String searchId) {
		this.list = list;
		this.pageNumber = pageNumber;
		this.objectsPerPage = objectsPerPage;
		this.fullListSize = fullListSize;
		this.sortCriterion = sortCriterion;
		this.sortDirection = sortDirection;
		this.searchId = searchId;
	}

	public int getFullListSize() {
		return fullListSize;
	}

	public void setFullListSize(int fullListSize) {
		this.fullListSize = fullListSize;
	}

	public List<?> getList() {
		return list;
	}

	public void setList(List<?> list) {
		this.list = list;
	}

	public int getObjectsPerPage() {
		return objectsPerPage;
	}

	public void setObjectsPerPage(int objectsPerPage) {
		this.objectsPerPage = objectsPerPage;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public String getSearchId() {
		return searchId;
	}

	public void setSearchId(String searchId) {
		this.searchId = searchId;
	}

	public String getSortCriterion() {
		return sortCriterion;
	}

	public void setSortCriterion(String sortCriterion) {
		this.sortCriterion = sortCriterion;
	}

	public SortOrderEnum getSortDirection() {
		return sortDirection;
	}

	public void setSortDirection(SortOrderEnum sortDirection) {
		this.sortDirection = sortDirection;
	}

}
