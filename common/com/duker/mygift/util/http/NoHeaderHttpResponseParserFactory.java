/*
 * @(#)NoHeaderHttpResponseParserFactory.java Jan 24, 2014
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.util.http;

import org.apache.http.HttpResponse;
import org.apache.http.config.MessageConstraints;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.io.DefaultHttpResponseParserFactory;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicLineParser;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Jan 24, 2014
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class NoHeaderHttpResponseParserFactory extends
		DefaultHttpResponseParserFactory {

	public static final NoHeaderHttpResponseParserFactory INSTANCE = new NoHeaderHttpResponseParserFactory();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.impl.conn.DefaultHttpResponseParserFactory#create(org
	 * .apache.http.io.SessionInputBuffer,
	 * org.apache.http.config.MessageConstraints)
	 */
	public HttpMessageParser<HttpResponse> create(SessionInputBuffer buffer,
			MessageConstraints constraints) {
		return new NoHeaderHttpResponseParser(buffer, BasicLineParser.INSTANCE,
				DefaultHttpResponseFactory.INSTANCE, constraints);
	}

}
