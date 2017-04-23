/*
 * @(#)HttpProxy.java Nov 26, 2013
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.util.http;

import java.io.Serializable;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Nov 26, 2013
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class HttpProxy implements Comparable<HttpProxy>, Serializable {

	public String user;

	public String password;

	public String ip;

	public int port;

	public HttpProxy(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public HttpProxy(String ip, int port, String user, String password) {
		this.ip = ip;
		this.port = port;
		this.user = user;
		this.password = password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof HttpProxy)) {
			return false;
		}
		HttpProxy other = (HttpProxy) obj;
		if (ip == null) {
			if (other.ip != null) {
				return false;
			}
		}
		else if (!ip.equals(other.ip)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(HttpProxy o) {
		int ipc = ip.compareTo(o.ip);
		if (ipc == 0) {
			return port - o.port;
		}
		return ipc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return ip + ":" + port;
	}

}
