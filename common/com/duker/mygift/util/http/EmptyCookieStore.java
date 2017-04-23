/*
 * @(#)EmptyCookieStore.java Feb 19, 2014
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.util.http;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: Feb 19, 2014
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class EmptyCookieStore implements CookieStore, Serializable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.client.CookieStore#addCookie(org.apache.http.cookie.Cookie
	 * )
	 */
	public void addCookie(Cookie arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.http.client.CookieStore#clear()
	 */
	public void clear() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.http.client.CookieStore#clearExpired(java.util.Date)
	 */
	public boolean clearExpired(Date arg0) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.http.client.CookieStore#getCookies()
	 */
	public List<Cookie> getCookies() {
		return new LinkedList<Cookie>();
	}

}
