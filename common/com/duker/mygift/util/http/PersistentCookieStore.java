/*
 * @(#)PersistentCookieStore.java Sep 9, 2012
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.util.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.http.annotation.GuardedBy;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieIdentityComparator;

import com.duker.mygift.util.ConfigUtil;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Sep 9, 2012
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@ThreadSafe
@SuppressWarnings("unchecked")
public class PersistentCookieStore implements CookieStore, Serializable {

	@GuardedBy("this")
	private TreeSet<Cookie> cookies;

	private File cookieFile;

	public PersistentCookieStore() {
		this.cookieFile = new File(ConfigUtil
				.getProperty("http.cookieFilePath"), "cookie");
		this.readCookies();
	}

	public PersistentCookieStore(String fileName) {
		fileName = fileName.replace(':', '_');
		this.cookieFile = new File(ConfigUtil
				.getProperty("http.cookieFilePath"), fileName);
		this.cookieFile.getParentFile().mkdirs();
		this.readCookies();
	}

	private void readCookies() {
		if (this.cookieFile.exists()) {
			ObjectInputStream oin = null;
			try {
				oin = new ObjectInputStream(
						new FileInputStream(this.cookieFile));
				this.cookies = (TreeSet<Cookie>) oin.readObject();
			}
			catch (Exception e) {
			}
			finally {
				if (oin != null) {
					try {
						oin.close();
					}
					catch (Exception e) {
					}
				}
			}
			if (this.cookies == null) {
				this.cookies = new TreeSet<Cookie>(
						new CookieIdentityComparator());
			}
		}
		else {
			this.cookies = new TreeSet<Cookie>(new CookieIdentityComparator());
		}
	}

	private void writeCookies() {
		this.cookieFile.getParentFile().mkdirs();
		ObjectOutputStream out = null;

		try {
			out = new ObjectOutputStream(new FileOutputStream(this.cookieFile));
			out.writeObject(this.cookies);
		}
		catch (Exception e) {
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (Exception e) {
				}
			}
		}
	}

	public boolean contains(Cookie cookie) {
		return cookies.contains(cookie) && !cookie.isExpired(new Date());
	}

	/**
	 * Adds an {@link Cookie HTTP cookie}, replacing any existing equivalent
	 * cookies. If the given cookie has already expired it will not be added,
	 * but existing values will still be removed.
	 * 
	 * @param cookie
	 *            the {@link Cookie cookie} to be added
	 * 
	 * @see #addCookies(Cookie[])
	 * 
	 */
	public synchronized void addCookie(Cookie cookie) {
		if (cookie != null) {
			// first remove any old cookie that is equivalent
			cookies.remove(cookie);
			if (!cookie.isExpired(new Date())) {
				cookies.add(cookie);
			}
			this.writeCookies();
		}
	}

	/**
	 * Adds an array of {@link Cookie HTTP cookies}. Cookies are added
	 * individually and in the given array order. If any of the given cookies
	 * has already expired it will not be added, but existing values will still
	 * be removed.
	 * 
	 * @param cookies
	 *            the {@link Cookie cookies} to be added
	 * 
	 * @see #addCookie(Cookie)
	 * 
	 */
	public synchronized void addCookies(Cookie[] cookies) {
		if (cookies != null) {
			for (Cookie cooky : cookies) {
				this.addCookie(cooky);
			}
		}
	}

	/**
	 * Returns an immutable array of {@link Cookie cookies} that this HTTP state
	 * currently contains.
	 * 
	 * @return an array of {@link Cookie cookies}.
	 */
	public synchronized List<Cookie> getCookies() {
		// create defensive copy so it won't be concurrently modified
		return new ArrayList<Cookie>(cookies);
	}

	/**
	 * Removes all of {@link Cookie cookies} in this HTTP state that have
	 * expired by the specified {@link java.util.Date date}.
	 * 
	 * @return true if any cookies were purged.
	 * 
	 * @see Cookie#isExpired(Date)
	 */
	public synchronized boolean clearExpired(final Date date) {
		if (date == null) {
			return false;
		}
		boolean removed = false;
		for (Iterator<Cookie> it = cookies.iterator(); it.hasNext();) {
			if (it.next().isExpired(date)) {
				it.remove();
				removed = true;
			}
		}
		this.writeCookies();
		return removed;
	}

	/**
	 * Clears all cookies.
	 */
	public synchronized void clear() {
		cookies.clear();
		this.writeCookies();
	}

	@Override
	public synchronized String toString() {
		return cookies.toString();
	}

}
