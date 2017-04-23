/*
 * @(#)HttpInputStream.java Oct 12, 2013
 * 
 * 信息审核管理系统
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
 * 修改版本: 0.9
 * 修改日期: Oct 12, 2013
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
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
