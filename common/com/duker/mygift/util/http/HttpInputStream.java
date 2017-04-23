/*
 * @(#)HttpInputStream.java Oct 12, 2013
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.util.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.methods.CloseableHttpResponse;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: Oct 12, 2013
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class HttpInputStream extends BufferedInputStream {

	private CloseableHttpResponse httpResponse;

	public HttpInputStream(InputStream in, int size,
			CloseableHttpResponse httpResponse) {
		super(in, size);
		this.httpResponse = httpResponse;
	}

	public HttpInputStream(InputStream in, CloseableHttpResponse httpResponse) {
		super(in);
		this.httpResponse = httpResponse;
	}

	public void close() throws IOException {
		httpResponse.close();
	}

}
