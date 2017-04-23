/*
 * @(#)EmptyAuthenticationStrategy.java May 24, 2012
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.util.http;

import java.util.Map;
import java.util.Queue;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthOption;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: May 24, 2012
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class EmptyAuthenticationStrategy implements AuthenticationStrategy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.client.AuthenticationStrategy#authFailed(org.apache.http
	 * .HttpHost, org.apache.http.auth.AuthScheme,
	 * org.apache.http.protocol.HttpContext)
	 */
	public void authFailed(HttpHost authhost, AuthScheme authScheme,
			HttpContext context) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.client.AuthenticationStrategy#authSucceeded(org.apache
	 * .http.HttpHost, org.apache.http.auth.AuthScheme,
	 * org.apache.http.protocol.HttpContext)
	 */
	public void authSucceeded(HttpHost authhost, AuthScheme authScheme,
			HttpContext context) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.client.AuthenticationStrategy#getChallenges(org.apache
	 * .http.HttpHost, org.apache.http.HttpResponse,
	 * org.apache.http.protocol.HttpContext)
	 */
	public Map<String, Header> getChallenges(HttpHost authhost,
			HttpResponse response, HttpContext context)
			throws MalformedChallengeException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.client.AuthenticationStrategy#isAuthenticationRequested
	 * (org.apache.http.HttpHost, org.apache.http.HttpResponse,
	 * org.apache.http.protocol.HttpContext)
	 */
	public boolean isAuthenticationRequested(HttpHost authhost,
			HttpResponse response, HttpContext context) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.http.client.AuthenticationStrategy#select(java.util.Map,
	 * org.apache.http.HttpHost, org.apache.http.HttpResponse,
	 * org.apache.http.protocol.HttpContext)
	 */
	public Queue<AuthOption> select(Map<String, Header> challenges,
			HttpHost authhost, HttpResponse response, HttpContext context)
			throws MalformedChallengeException {
		return null;
	}

}
