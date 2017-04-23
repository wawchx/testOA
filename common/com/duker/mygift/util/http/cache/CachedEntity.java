/*
 * @(#)CachedEntity.java Mar 19, 2012
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.util.http.cache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Mar 19, 2012
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class CachedEntity implements HttpEntity {

	private final Map<String, Set<String>> cachedHeaders;

	private final File bodyFile;

	public CachedEntity(Map<String, Set<String>> cachedHeaders, File bodyFile) {
		this.cachedHeaders = cachedHeaders;
		this.bodyFile = bodyFile;
	}

	public Header getContentType() {
		Set<String> vs = cachedHeaders.get(HTTP.CONTENT_TYPE);
		return new BasicHeader(HTTP.CONTENT_TYPE, vs.iterator().next());
	}

	public Header getContentEncoding() {
		Set<String> vs = cachedHeaders.get(HTTP.CONTENT_ENCODING);
		return new BasicHeader(HTTP.CONTENT_ENCODING, vs.iterator().next());
	}

	public boolean isChunked() {
		return false;
	}

	public boolean isRepeatable() {
		return true;
	}

	public long getContentLength() {
		return bodyFile.length();
	}

	public InputStream getContent() throws IOException {
		return new BufferedInputStream(new FileInputStream(bodyFile));
	}

	public void writeTo(final OutputStream outstream) throws IOException {
		if (outstream == null) {
			throw new IllegalArgumentException("Output stream may not be null");
		}
		InputStream instream = new FileInputStream(bodyFile);
		try {
			int l;
			byte[] tmp = new byte[2048];
			while ((l = instream.read(tmp)) != -1) {
				outstream.write(tmp, 0, l);
			}
		}
		finally {
			instream.close();
		}
	}

	public boolean isStreaming() {
		return false;
	}

	public void consumeContent() throws IOException {
	}

}
