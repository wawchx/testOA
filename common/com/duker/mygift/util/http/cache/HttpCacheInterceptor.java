/*
 * @(#)HttpCacheInterceptor.java Sep 13, 2013
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.util.http.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import com.duker.mygift.constant.CList;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Sep 13, 2013
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class HttpCacheInterceptor implements HttpRequestInterceptor,
		HttpResponseInterceptor {

	public static final String GET_METHOD = "GET";

	public static final String LAST_MODIFIED = "Last-Modified";

	public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

	public static final String IF_NONE_MATCH = "If-None-Match";

	public static final String PRAGMA = "Pragma";

	public static final String ETAG = "ETag";

	public static final String CACHE_CONTROL = "Cache-Control";

	public static final String CACHE_CONTROL_NO_STORE = "no-store";

	public static final String CACHE_CONTROL_NO_CACHE = "no-cache";

	public static final String CACHE_REQUEST = HttpContext.RESERVED_PREFIX
			+ ".cache.request";

	/**
	 * 缓存存储位置
	 */
	private static final String CACHE_LOCATION;

	private boolean cacheFirst = false;

	static {
		String path = System.getProperty("java.io.tmpdir");
		File dir = new File(path, "cache");
		dir.mkdir();
		CACHE_LOCATION = dir.getPath();
	}

	public boolean isCacheFirst() {
		return cacheFirst;
	}

	public void setCacheFirst(boolean cacheFirst) {
		this.cacheFirst = cacheFirst;
	}

	/**
	 * 获取缓存文件
	 * 
	 * @param uri
	 *            uri地址
	 * @return 缓存文件
	 */
	private String getCacheFile(URI uri) {
		try {
			String host = uri.getHost();
			StringBuilder u = new StringBuilder(host);
			String path = uri.getPath();
			if (StringUtils.isNotBlank(path)) {
				int idx = path.lastIndexOf('/');
				if (idx != -1) {
					u.append(path.substring(0, idx));
				}
			}
			u.append("/");
			u.append(DigestUtils.md5Hex(uri.toString().getBytes("utf-8")));

			return u.toString();
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * 读取缓存响应头
	 * 
	 * @param cacheFile
	 *            缓存响应文件
	 * @return 缓存响应头
	 */
	private Map<String, Set<String>> readCachedHeaders(String cacheFile) {
		Map<String, Set<String>> headers = new LinkedHashMap<String, Set<String>>();
		BufferedReader reader = null;
		try {
			File file = new File(CACHE_LOCATION, cacheFile + ".header");
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				int idx = line.indexOf(':');
				if (idx == -1) {
					continue;
				}
				String name = line.substring(0, idx);
				String value = line.substring(idx + 1);
				Set<String> values = headers.get(name);
				if (values == null) {
					values = new LinkedHashSet<String>(1);
					headers.put(name, values);
				}
				values.add(value);
			}
		}
		catch (Exception e) {
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (Exception e) {
				}
			}
		}

		return headers;
	}

	/**
	 * 本次请求是否可以使用缓存
	 * 
	 * @param request
	 *            请求
	 * @return 是否可使用缓存
	 */
	private boolean isServableFromCache(HttpRequest request) {
		RequestLine requestLine = request.getRequestLine();
		ProtocolVersion pv = requestLine.getProtocolVersion();
		if (HttpVersion.HTTP_1_1.compareToVersion(pv) != 0) {
			return false;
		}

		if (!GET_METHOD.equals(requestLine.getMethod())) {
			return false;
		}

		if (request.getHeaders(PRAGMA).length > 0) {
			return false;
		}

		Header[] cacheControlHeaders = request.getHeaders(CACHE_CONTROL);
		for (Header cacheControl : cacheControlHeaders) {
			for (HeaderElement cacheControlElement : cacheControl.getElements()) {
				String name = cacheControlElement.getName();
				if (CACHE_CONTROL_NO_STORE.equalsIgnoreCase(name)) {
					return false;
				}

				if (CACHE_CONTROL_NO_CACHE.equalsIgnoreCase(name)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * 响应是否可以被缓存
	 * 
	 * @param response
	 *            响应
	 * @return 是否可被缓存
	 */
	private boolean isResponseCacheable(HttpResponse response) {
		if (response.getHeaders(PRAGMA).length > 0) {
			return false;
		}

		Header[] cacheControlHeaders = response.getHeaders(CACHE_CONTROL);
		for (Header cacheControl : cacheControlHeaders) {
			for (HeaderElement cacheControlElement : cacheControl.getElements()) {
				String name = cacheControlElement.getName();
				if (CACHE_CONTROL_NO_STORE.equalsIgnoreCase(name)) {
					return false;
				}

				if (CACHE_CONTROL_NO_CACHE.equalsIgnoreCase(name)) {
					return false;
				}
			}
		}
		if (!response.containsHeader(LAST_MODIFIED)
				&& !response.containsHeader(IF_NONE_MATCH)) {
			return false;
		}

		switch (response.getStatusLine().getStatusCode()) {
			case HttpStatus.SC_OK:
			case HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION:
			case HttpStatus.SC_MULTIPLE_CHOICES:
			case HttpStatus.SC_MOVED_PERMANENTLY:
			case HttpStatus.SC_GONE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * 处理后端响应
	 * 
	 * @param cacheFile
	 *            缓存文件位置
	 * @param cachedHeaders
	 *            上次缓存的响应头
	 * @param backendResponse
	 *            后端响应
	 */
	private void handleBackendResponse(String cacheFile,
			Map<String, Set<String>> cachedHeaders, HttpResponse backendResponse)
			throws IOException {
		int statusCode = backendResponse.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
			backendResponse.setStatusCode(HttpStatus.SC_OK);
			backendResponse.setEntity(new CachedEntity(cachedHeaders, new File(
					CACHE_LOCATION, cacheFile + ".body")));
		}
		else if (isResponseCacheable(backendResponse)) {
			HttpEntity backendEntity = backendResponse.getEntity();
			Header[] allHeaders = backendResponse.getAllHeaders();
			Map<String, Set<String>> headers = new LinkedHashMap<String, Set<String>>(
					allHeaders.length);
			for (Header header : allHeaders) {
				String name = header.getName();
				Set<String> values = headers.get(name);
				if (values == null) {
					values = new LinkedHashSet<String>(1);
					headers.put(name, values);
				}
				values.add(header.getValue());
			}
			File headerFile = new File(CACHE_LOCATION, cacheFile + ".header");
			File bodyFile = new File(CACHE_LOCATION, cacheFile + ".body");
			if (cacheFirst) {
				// 先生成缓存文件
				headerFile.getParentFile().mkdirs();
				bodyFile.getParentFile().mkdirs();
				InputStream in = null;
				OutputStream out = null;

				try {
					in = backendEntity.getContent();
					out = new FileOutputStream(bodyFile);
					IOUtils.copyLarge(in, out);
				}
				catch (Exception ex) {
					if (out != null) {
						try {
							out.close();
						}
						catch (Exception e) {
						}
						out = null;
					}
					bodyFile.delete();
					throw new IOException("生成缓存文件" + bodyFile.getPath() + "失败",
							ex);
				}
				finally {
					if (in != null) {
						try {
							in.close();
						}
						catch (Exception e) {
						}
					}
					if (out != null) {
						try {
							out.close();
						}
						catch (Exception e) {
						}
					}
				}
				Set<String> vs = new HashSet<String>(1);
				vs.add(Long.toString(bodyFile.length()));
				headers.put(HTTP.CONTENT_LEN, vs);
				try {
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
				}
				catch (Exception ex) {
					if (out != null) {
						try {
							out.close();
						}
						catch (Exception e) {
						}
						out = null;
					}
					headerFile.delete();
					bodyFile.delete();
					throw new IOException("生成缓存文件" + headerFile.getPath()
							+ "失败", ex);
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

				backendResponse.setEntity(new CachedEntity(headers, bodyFile));
			}
			else {
				backendResponse.setEntity(new CachingEntity(backendEntity,
						bodyFile, headers, headerFile));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.HttpRequestInterceptor#process(org.apache.http.HttpRequest
	 * , org.apache.http.protocol.HttpContext)
	 */
	public void process(HttpRequest request, HttpContext context)
			throws HttpException, IOException {
		if (request instanceof HttpRequestWrapper) {
			request = ((HttpRequestWrapper) request).getOriginal();
		}
		if (request instanceof HttpUriRequest) {
			context.setAttribute(CACHE_REQUEST, request);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.http.HttpResponseInterceptor#process(org.apache.http.HttpResponse
	 * , org.apache.http.protocol.HttpContext)
	 */
	public void process(HttpResponse response, HttpContext context)
			throws HttpException, IOException {
		HttpUriRequest request = (HttpUriRequest) context
				.getAttribute(CACHE_REQUEST);
		if (request == null || !isServableFromCache(request)) {
			return;
		}

		String cacheFile = getCacheFile(request.getURI());
		if (cacheFile == null) {
			return;
		}

		request.removeHeaders(IF_MODIFIED_SINCE);
		request.removeHeaders(IF_NONE_MATCH);
		File body = new File(CACHE_LOCATION, cacheFile + ".body");
		Map<String, Set<String>> headers = null;
		if (body.exists()) {
			// 缓存响应体必须存在
			headers = readCachedHeaders(cacheFile);
			Set<String> values = headers.get(HTTP.CONTENT_LEN);
			if (values != null && !values.isEmpty()) {
				// 判断缓存响应体是否完整
				String value = values.iterator().next();
				try {
					long oldLength = Long.parseLong(value);
					if (oldLength > 0 && body.length() == oldLength) {
						// 携带缓存过期标志
						values = headers.get(LAST_MODIFIED);
						if (values != null && !values.isEmpty()) {
							request.addHeader(IF_MODIFIED_SINCE, values
									.iterator().next());
						}
						values = headers.get(ETAG);
						if (values != null && !values.isEmpty()) {
							request.addHeader(IF_NONE_MATCH, values.iterator()
									.next());
						}
					}
				}
				catch (Exception e) {
				}
			}
		}

		handleBackendResponse(cacheFile, headers, response);
	}

}
