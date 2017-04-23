/*
 * @(#)PageCache.java Dec 4, 2014
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.dao;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

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
public class PageCache {

	private Ehcache cache;

	private String name;

	public PageCache() {
	}

	public void init() {
		cache = CacheManager.create().getCache(name);
	}

	public void put(Integer resultCount, String hql, Object... values) {
		PageCacheKey key = new PageCacheKey(hql, values);
		Element element = new Element(key.toString(), resultCount);
		cache.put(element);
	}

	public Integer get(String hql, Object... values) {
		PageCacheKey key = new PageCacheKey(hql, values);
		Element element = cache.get(key.toString());
		if (element != null) {
			return (Integer) element.getObjectValue();
		}

		return null;
	}

	public void remove(String hql, Object... values) {
		PageCacheKey key = new PageCacheKey(hql, values);
		cache.remove(key.toString());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
