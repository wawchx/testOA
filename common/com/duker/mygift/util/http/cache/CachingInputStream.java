/*
 * @(#)CachingInputStream.java May 15, 2011
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.util.http.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.protocol.HTTP;

import com.duker.mygift.constant.CList;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: May 15, 2011
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
class CachingInputStream extends InputStream {

	private InputStream in;

	private Map<String, Set<String>> headers;

	private File headerFile;

	private OutputStream cachingStream;

	private File bodyFile;

	private boolean cachingComplete = false;

	public CachingInputStream(InputStream in, File bodyFile,
			Map<String, Set<String>> headers, File headerFile) {
		this.in = in;
		this.headers = headers;
		this.headerFile = headerFile;
		this.bodyFile = bodyFile;
	}

	private void createStreamIfNecessary() throws Exception {
		if (cachingStream == null) {
			bodyFile.getParentFile().mkdirs();
			cachingStream = new FileOutputStream(bodyFile);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		try {
			int b = in.read();
			try {
				if (b == -1) {
					cachingComplete();
				}
				else {
					createStreamIfNecessary();
					cachingStream.write(b);
				}
			}
			catch (Exception e) {
			}

			return b;
		}
		catch (IOException e) {
			cachingInterrupt();

			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] b) throws IOException {
		try {
			int num = in.read(b);
			try {
				if (num == -1) {
					cachingComplete();
				}
				else {
					createStreamIfNecessary();
					cachingStream.write(b, 0, num);
				}
			}
			catch (Exception e) {
			}

			return num;
		}
		catch (IOException e) {
			cachingInterrupt();

			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		try {
			int num = in.read(b, off, len);
			try {
				if (num == -1) {
					cachingComplete();
				}
				else {
					createStreamIfNecessary();
					cachingStream.write(b, off, num);
				}
			}
			catch (Exception e) {
			}

			return num;
		}
		catch (IOException e) {
			cachingInterrupt();

			throw e;
		}
	}

	/**
	 * 缓存结束
	 */
	protected void cachingComplete() throws Exception {
		if (cachingStream == null) {
			return;
		}
		cachingStream.flush();
		cachingStream.close();
		cachingStream = null;
		Set<String> vs = new HashSet<String>(1);
		vs.add(Long.toString(bodyFile.length()));
		headers.put(HTTP.CONTENT_LEN, vs);

		OutputStream out = null;
		try {
			headerFile.getParentFile().mkdirs();
			out = new FileOutputStream(headerFile);
			for (Entry<String, Set<String>> entry : headers.entrySet()) {
				String name = entry.getKey();
				for (String value : entry.getValue()) {
					if (StringUtils.isBlank(value)) {
						continue;
					}
					out.write(name.getBytes());
					out.write(':');
					out.write(value.getBytes());
					out.write(CList.LINE_SEPARATOR.getBytes());
				}
			}
			cachingComplete = true;
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

	/**
	 * 缓存中断,清除缓存文件
	 */
	protected void cachingInterrupt() {
		if (cachingStream == null) {
			return;
		}
		FileUtils.deleteQuietly(headerFile);
		try {
			cachingStream.close();
			cachingStream = null;
			FileUtils.deleteQuietly(bodyFile);
		}
		catch (Exception e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	public long skip(long n) throws IOException {
		try {
			byte[] bs = new byte[1];
			long ret = 0;
			while (read(bs) != -1) {
				ret++;
			}
			return ret;
		}
		catch (IOException e) {
			cachingInterrupt();

			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		try {
			return in.available();
		}
		catch (IOException e) {
			cachingInterrupt();

			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		try {
			in.close();
		}
		finally {
			if (cachingStream != null) {
				try {
					cachingStream.close();
					cachingStream = null;
					if (!cachingComplete) {
						FileUtils.deleteQuietly(headerFile);
						FileUtils.deleteQuietly(bodyFile);
					}
				}
				catch (Exception e) {
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		close();
	}

}
