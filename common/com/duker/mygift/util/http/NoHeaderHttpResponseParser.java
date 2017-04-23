/*
 * @(#)NoHeaderHttpResponseParser.java Jan 24, 2014
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.util.http;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.config.MessageConstraints;
import org.apache.http.impl.io.DefaultHttpResponseParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.message.LineParser;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: Jan 24, 2014
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class NoHeaderHttpResponseParser extends DefaultHttpResponseParser {

	private final HttpResponseFactory responseFactory;

	public NoHeaderHttpResponseParser(SessionInputBuffer buffer,
			LineParser lineParser, HttpResponseFactory responseFactory,
			MessageConstraints constraints) {
		super(buffer, lineParser, responseFactory, constraints);
		this.responseFactory = responseFactory;
	}

	public HttpResponse parse() throws IOException, HttpException {
		StatusLine statusline = new BasicStatusLine(new ProtocolVersion("HTTP",
				1, 1), 200, "OK");
		return this.responseFactory.newHttpResponse(statusline, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.impl.conn.DefaultHttpResponseParser#parseHead(org.apache
	 * .http.io.SessionInputBuffer)
	 */
	protected HttpResponse parseHead(SessionInputBuffer sessionBuffer)
			throws IOException, HttpException {
		StatusLine statusline = new BasicStatusLine(new ProtocolVersion("HTTP",
				1, 1), 200, "OK");
		return this.responseFactory.newHttpResponse(statusline, null);
	}

}
