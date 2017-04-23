/*
 * @(#)CachingEntity.java Mar 18, 2012
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.util.http.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Mar 18, 2012
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
class CachingEntity implements HttpEntity {

	private final HttpEntity backend;

	private final CachingInputStream cachingStream;

	public CachingEntity(HttpEntity backend, File bodyFile,
			Map<String, Set<String>> headers, File headerFile)
			throws IOException {
		this.backend = backend;
		this.cachingStream = new CachingInputStream(backend.getContent(),
				bodyFile, headers, headerFile);
	}

	public Header getContentType() {
		return backend.getContentType();
	}

	public Header getContentEncoding() {
		return backend.getContentEncoding();
	}

	public boolean isChunked() {
		return backend.isChunked();
	}

	public boolean isRepeatable() {
		return backend.isRepeatable();
	}

	public long getContentLength() {
		return backend.getContentLength();
	}

	public InputStream getContent() throws IOException {
		return cachingStream;
	}

	public void writeTo(final OutputStream outstream) throws IOException {
		if (outstream == null) {
			throw new IllegalArgumentException("Output stream may not be null");
		}
		try {
			int l;
			byte[] tmp = new byte[2048];
			while ((l = cachingStream.read(tmp)) != -1) {
				outstream.write(tmp, 0, l);
			}
		}
		finally {
			cachingStream.close();
		}
	}

	public boolean isStreaming() {
		return backend.isStreaming();
	}

	public void consumeContent() throws IOException {
		cachingStream.close();
	}
}
