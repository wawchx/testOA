/*
 * @(#)HttpReader.java Oct 12, 2013
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

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
public class HttpReader extends BufferedReader {

	private CloseableHttpResponse httpResponse;

	public HttpReader(Reader in, int sz, CloseableHttpResponse httpResponse) {
		super(in, sz);
		this.httpResponse = httpResponse;
	}

	public HttpReader(Reader in, CloseableHttpResponse httpResponse) {
		super(in);
		this.httpResponse = httpResponse;
	}

	public void close() throws IOException {
		httpResponse.close();
	}

}
