/*
 * @(#)PageCacheKey.java Dec 4, 2014
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.dao;

import java.io.Serializable;
import java.util.Arrays;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: Dec 4, 2014
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class PageCacheKey implements Serializable {

	public String hql;

	public Object[] values;

	public PageCacheKey() {
	}

	public PageCacheKey(String hql, Object[] values) {
		this.hql = hql;
		this.values = values;
	}

	@Override
	public String toString() {
		return "PageCacheKey [hql=" + hql + ", values="
				+ Arrays.toString(values) + "]";
	}

}
