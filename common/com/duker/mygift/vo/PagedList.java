/*
 * @(#)PagedList.java May 25, 2009
 * 
 * ��Ϣ��˹���ϵͳ
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
 * �޸İ汾: 0.9
 * �޸�����: May 25, 2009
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class PagedList<T> implements Serializable {

	/**
	 * ���л��汾��
	 */
	private static final long serialVersionUID = -5396518799352102881L;

	/**
	 * ��ǰҳ����
	 */
	private List<T> list;

	/**
	 * �ܼ�¼��
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
