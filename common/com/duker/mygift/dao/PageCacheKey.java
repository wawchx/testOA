/*
 * @(#)PageCacheKey.java Dec 4, 2014
 * 
 * 信息审核管理系统
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
 * 修改版本: 0.9
 * 修改日期: Dec 4, 2014
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
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
