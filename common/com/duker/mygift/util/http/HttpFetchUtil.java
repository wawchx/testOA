/*
 * @(#)HttpFetchUtil.java Feb 24, 2011
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.util.http;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.xerces.impl.Constants;
import org.cyberneko.html.HTMLScanner;
import org.cyberneko.html.parsers.DOMParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.DOMReader;
import org.dom4j.io.SAXReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.duker.mygift.common.util.TimeWatcher;
import com.duker.mygift.constant.CList;
import com.duker.mygift.exception.CommonLogicException;
import com.duker.mygift.exception.ContentTypeException;
import com.duker.mygift.util.http.cache.HttpCacheInterceptor;

/**
 * <pre>
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: Feb 24, 2011
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class HttpFetchUtil {

	/**
	 * 带认证的http proxy
	 */
	public HttpProxy httpProxy;

	/**
	 * 代理检测任务
	 */
	public ProxyTask proxyTask;

	/**
	 * 是否使用随机的X-Forwarded-For请求头
	 */
	public boolean useringXForwardedFor = false;

	/**
	 * 是否在请求头中使用Referer
	 */
	private boolean usingReferer = true;

	/**
	 * 是否维持会话
	 */
	private boolean keepSession = true;

	/**
	 * 响应是否没有头
	 */
	private boolean noHeader = false;

	/**
	 * 请求头中携带的Referer
	 */
	private String referer = null;

	/**
	 * 是否处理302/301跳转
	 */
	public static final String HANDLE_REDIRECTS = "handle-redirects";

	/**
	 * 是否忽略请求
	 */
	public static final String ABORT_REQUEST = "abort-request";

	/**
	 * 匹配xml头
	 */
	private Pattern xmlPattern = Pattern
			.compile("^<(.+)\\s+xmlns\\s*=\\s*[^>]*>(.*)$");

	/**
	 * 匹配xml结点内容
	 */
	private static final Pattern ELEMENT_CONTENT_PATTERN = Pattern
			.compile("<\\w+[^>]*>(.*)</\\w+>");

	/**
	 * 匹配xml中的控制字符与bom
	 */
	private static final Pattern CONTROL_CHARACTER_PATTERN = Pattern
			.compile("[\\p{Cntrl}&&[^\r\n]]|\\uFEFF");

	/**
	 * cookie存储器
	 */
	private static final CookieStore PERSISTENT_COOKIE_STORE = new PersistentCookieStore();

	/**
	 * 计时器
	 */
	private TimeWatcher timeWatcher = new TimeWatcher(0);

	/**
	 * 抓取失败后重试次数
	 */
	private int retryCount = 1;

	/**
	 * 是否启用多线程连接池
	 */
	private boolean multiThread = false;

	/**
	 * 最大连接个数
	 */
	private int maxTotalConnections = 3000;

	/**
	 * 每个主机最大连接数
	 */
	private int maxPerRoute = 20;

	/**
	 * 失败后等待时间
	 */
	private long errorWaitTime = 2000;

	/**
	 * 重试次数
	 */
	private int retry = 0;

	/**
	 * 是否关闭缓存
	 */
	private boolean noCache = false;

	/**
	 * 是否先生成缓存文件
	 */
	private boolean cacheFirst = false;

	/**
	 * 两次socket数据包之间超时时间,单位毫秒
	 */
	private int soTimeout = 90000;

	/**
	 * 连接超时时间,单位毫秒
	 */
	private int connectionTimeout = 120000;

	/**
	 * HttpClient
	 */
	private CloseableHttpClient client;

	/**
	 * cookie存储位置
	 */
	private CookieStore cookieStore;

	/**
	 * 缓存拦截器
	 */
	private HttpCacheInterceptor cacheInterceptor;

	/**
	 * 是否自动处理401登录
	 */
	private boolean authenticationEnabled = true;

	/**
	 * cookie策略
	 */
	private String cookieSpecs = CookieSpecs.DEFAULT;

	/**
	 * 请求上下文配置
	 */
	private ThreadLocal<HttpClientContext> context = new ThreadLocal<HttpClientContext>() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		protected HttpClientContext initialValue() {
			return HttpClientContext.create();
		}
	};

	/**
	 * 当前请求的状态
	 */
	private ThreadLocal<Integer> currentStatusCode = new ThreadLocal<Integer>();

	/**
	 * 当前请求的Content-Type
	 */
	private ThreadLocal<String> contentType = new ThreadLocal<String>();

	/**
	 * 301/302跳转后的地址
	 */
	private ThreadLocal<String> location = new ThreadLocal<String>();

	/**
	 * 响应头
	 */
	private ThreadLocal<Header[]> headers = new ThreadLocal<Header[]>();

	private HttpFetchUtil(boolean multiThread, int retryCount,
			int maxTotalConnections, int maxPerRoute, boolean noCache,
			boolean noHeader) {
		this.multiThread = multiThread;
		this.retryCount = retryCount;
		this.maxTotalConnections = maxTotalConnections;
		this.maxPerRoute = maxPerRoute;
		this.noCache = noCache;
		this.noHeader = noHeader;
		initHttpClient(PERSISTENT_COOKIE_STORE);
	}

	private HttpFetchUtil(boolean multiThread, int retryCount,
			int maxTotalConnections, int maxPerRoute, boolean noCache,
			boolean noHeader, CookieStore cookieStore) {
		this.multiThread = multiThread;
		this.retryCount = retryCount;
		this.maxTotalConnections = maxTotalConnections;
		this.maxPerRoute = maxPerRoute;
		this.noCache = noCache;
		this.noHeader = noHeader;
		initHttpClient(cookieStore);
	}

	/**
	 * 添加固定cookie以便记住一些认证信息
	 * 
	 * @param cookie
	 *            认证相关的cookie
	 */
	public void addCookie(Cookie cookie) {
		if (cookieStore != null) {
			cookieStore.addCookie(cookie);
		}
	}

	/**
	 * 获取cookie列表
	 * 
	 * @return cookie列表
	 */
	public List<Cookie> getCookies() {
		return cookieStore == null ? null : cookieStore.getCookies();
	}

	/**
	 * 清除所有cookie
	 */
	public void clearCookies() {
		if (cookieStore != null) {
			cookieStore.clear();
		}
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil() {
		return createHttpFetchUtil(0, 5, true, true, false, 30, 2, false);
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(boolean multiThread) {
		return createHttpFetchUtil(0, 5, true, true, multiThread, 30, 2, false);
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @param noCache
	 *            是否关闭缓存
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(boolean multiThread,
			boolean noCache) {
		return createHttpFetchUtil(0, 5, true, true, multiThread, 30, 2,
				noCache);
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param retryCount
	 *            失败后重试次数
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createFastHttpFetchUtil(int retryCount) {
		HttpFetchUtil httpFetchUtil = new HttpFetchUtil(false, retryCount, 30,
				2, false, false, null);
		httpFetchUtil.setMinFetchInterval(0);
		httpFetchUtil.setUsingReferer(false);
		httpFetchUtil.setKeepSession(false);
		httpFetchUtil.setSoTimeout(40000);
		httpFetchUtil.setConnectionTimeout(40000);

		return httpFetchUtil;
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param retryCount
	 *            失败后重试次数
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createFastHttpFetchUtil(int retryCount,
			boolean multiThread) {
		HttpFetchUtil httpFetchUtil = new HttpFetchUtil(multiThread,
				retryCount, 30, 2, false, false, null);
		httpFetchUtil.setMinFetchInterval(0);
		httpFetchUtil.setUsingReferer(false);
		httpFetchUtil.setKeepSession(false);
		httpFetchUtil.setSoTimeout(40000);
		httpFetchUtil.setConnectionTimeout(40000);

		return httpFetchUtil;
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param retryCount
	 *            失败后重试次数
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @param noHeader
	 *            响应是否没有头
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createFastHttpFetchUtil(int retryCount,
			boolean multiThread, boolean noHeader) {
		HttpFetchUtil httpFetchUtil = new HttpFetchUtil(multiThread,
				retryCount, 30, 2, false, noHeader, null);
		httpFetchUtil.setMinFetchInterval(0);
		httpFetchUtil.setUsingReferer(true);
		httpFetchUtil.setKeepSession(false);
		httpFetchUtil.setSoTimeout(60000);
		httpFetchUtil.setConnectionTimeout(60000);

		return httpFetchUtil;
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param retryCount
	 *            失败后重试次数
	 * @param soTimeout
	 *            两次socket数据包之间超时时间,单位毫秒
	 * @param connectionTimeout
	 *            连接超时时间,单位毫秒
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createFastHttpFetchUtil(int retryCount,
			int soTimeout, int connectionTimeout) {
		HttpFetchUtil httpFetchUtil = new HttpFetchUtil(false, retryCount, 30,
				2, false, false, null);
		httpFetchUtil.setMinFetchInterval(0);
		httpFetchUtil.setUsingReferer(false);
		httpFetchUtil.setKeepSession(false);
		httpFetchUtil.setSoTimeout(soTimeout);
		httpFetchUtil.setConnectionTimeout(connectionTimeout);

		return httpFetchUtil;
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param retryCount
	 *            失败后重试次数
	 * @param soTimeout
	 *            两次socket数据包之间超时时间,单位毫秒
	 * @param connectionTimeout
	 *            连接超时时间,单位毫秒
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createFastHttpFetchUtil(int retryCount,
			int soTimeout, int connectionTimeout, boolean multiThread) {
		HttpFetchUtil httpFetchUtil = createHttpFetchUtil(0, retryCount, false,
				false, multiThread, 30, 2, false, null);
		httpFetchUtil.setSoTimeout(soTimeout);
		httpFetchUtil.setConnectionTimeout(connectionTimeout);

		return httpFetchUtil;
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param retryCount
	 *            失败后重试次数
	 * @param soTimeout
	 *            两次socket数据包之间超时时间,单位毫秒
	 * @param connectionTimeout
	 *            连接超时时间,单位毫秒
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @param noCache
	 *            是否关闭缓存
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createFastHttpFetchUtil(int retryCount,
			int soTimeout, int connectionTimeout, boolean multiThread,
			boolean noCache) {
		HttpFetchUtil httpFetchUtil = createHttpFetchUtil(0, retryCount, true,
				true, multiThread, 30, 2, noCache, null);
		httpFetchUtil.setSoTimeout(soTimeout);
		httpFetchUtil.setConnectionTimeout(connectionTimeout);

		return httpFetchUtil;
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param retryCount
	 *            失败后重试次数
	 * @param soTimeout
	 *            两次socket数据包之间超时时间,单位毫秒
	 * @param connectionTimeout
	 *            连接超时时间,单位毫秒
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @param noCache
	 *            是否关闭缓存
	 * @param noHeader
	 *            响应是否没有头
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createFastHttpFetchUtil(int retryCount,
			int soTimeout, int connectionTimeout, boolean multiThread,
			boolean noCache, boolean noHeader) {
		HttpFetchUtil httpFetchUtil = createHttpFetchUtil(0, retryCount, true,
				true, multiThread, 30, 2, noCache, noHeader);
		httpFetchUtil.setSoTimeout(soTimeout);
		httpFetchUtil.setConnectionTimeout(connectionTimeout);

		return httpFetchUtil;
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param retryCount
	 *            失败后重试次数
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(int retryCount) {
		return createHttpFetchUtil(0, retryCount, true, true, false, 30, 2,
				false);
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param retryCount
	 *            失败后重试次数
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(int retryCount,
			boolean multiThread) {
		return createHttpFetchUtil(0, retryCount, true, true, multiThread, 30,
				2, false);
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param retryCount
	 *            失败后重试次数
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @param cookieFile
	 *            cookie存储位置
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(int retryCount,
			boolean multiThread, String cookieFile) {
		return createHttpFetchUtil(0, retryCount, true, true, multiThread, 30,
				2, false, new PersistentCookieStore(cookieFile));
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param retryCount
	 *            失败后重试次数
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @param cookieStore
	 *            cookie存储器
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(int retryCount,
			boolean multiThread, CookieStore cookieStore) {
		return createHttpFetchUtil(0, retryCount, true, true, multiThread, 30,
				2, false, cookieStore);
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param retryCount
	 *            失败后重试次数
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @param noCache
	 *            是否关闭缓存
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(int retryCount,
			boolean multiThread, boolean noCache) {
		return createHttpFetchUtil(0, retryCount, true, true, multiThread, 30,
				2, noCache);
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param minFetchInterval
	 *            每次抓取最小时间间隔
	 * @param retryCount
	 *            失败后重试次数
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(long minFetchInterval,
			int retryCount) {
		return createHttpFetchUtil(minFetchInterval, retryCount, true, true,
				false, 30, 2, false);
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param minFetchInterval
	 *            每次抓取最小时间间隔
	 * @param retryCount
	 *            失败后重试次数
	 * @param noCache
	 *            是否关闭缓存
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(long minFetchInterval,
			int retryCount, boolean noCache) {
		return createHttpFetchUtil(minFetchInterval, retryCount, true, true,
				false, 30, 2, noCache);
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param minFetchInterval
	 *            每次抓取最小时间间隔
	 * @param retryCount
	 *            失败后重试次数
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @param noCache
	 *            是否关闭缓存
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(long minFetchInterval,
			int retryCount, boolean multiThread, boolean noCache) {
		return createHttpFetchUtil(minFetchInterval, retryCount, true, true,
				multiThread, 30, 2, noCache);
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param minFetchInterval
	 *            每次抓取最小时间间隔
	 * @param retryCount
	 *            失败后重试次数
	 * @param usingReferer
	 *            请求头是否携带referer
	 * @param keepSession
	 *            是否保持会话
	 * @param noCache
	 *            是否关闭缓存
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(long minFetchInterval,
			int retryCount, boolean usingReferer, boolean keepSession,
			boolean noCache) {
		return createHttpFetchUtil(minFetchInterval, retryCount, usingReferer,
				keepSession, false, 30, 2, noCache);
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param minFetchInterval
	 *            每次抓取最小时间间隔
	 * @param retryCount
	 *            失败后重试次数
	 * @param usingReferer
	 *            请求头是否携带referer
	 * @param keepSession
	 *            是否保持会话
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @param maxTotalConnections
	 *            最大连接个数
	 * @param maxPerRoute
	 *            每个主机最大连接数
	 * @param noCache
	 *            是否关闭缓存
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(long minFetchInterval,
			int retryCount, boolean usingReferer, boolean keepSession,
			boolean multiThread, int maxTotalConnections, int maxPerRoute,
			boolean noCache) {
		HttpFetchUtil httpFetchUtil = new HttpFetchUtil(multiThread,
				retryCount, maxTotalConnections, maxPerRoute, noCache, false);
		httpFetchUtil.setMinFetchInterval(minFetchInterval);
		httpFetchUtil.setUsingReferer(usingReferer);
		httpFetchUtil.setKeepSession(keepSession);

		return httpFetchUtil;
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param minFetchInterval
	 *            每次抓取最小时间间隔
	 * @param retryCount
	 *            失败后重试次数
	 * @param usingReferer
	 *            请求头是否携带referer
	 * @param keepSession
	 *            是否保持会话
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @param maxTotalConnections
	 *            最大连接个数
	 * @param maxPerRoute
	 *            每个主机最大连接数
	 * @param noCache
	 *            是否关闭缓存
	 * @param cookieStore
	 *            cookie存储器
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(long minFetchInterval,
			int retryCount, boolean usingReferer, boolean keepSession,
			boolean multiThread, int maxTotalConnections, int maxPerRoute,
			boolean noCache, CookieStore cookieStore) {
		HttpFetchUtil httpFetchUtil = new HttpFetchUtil(multiThread,
				retryCount, maxTotalConnections, maxPerRoute, noCache, false,
				cookieStore);
		httpFetchUtil.setMinFetchInterval(minFetchInterval);
		httpFetchUtil.setUsingReferer(usingReferer);
		httpFetchUtil.setKeepSession(keepSession);

		return httpFetchUtil;
	}

	/**
	 * 生成抓取工具实例
	 * 
	 * @param minFetchInterval
	 *            每次抓取最小时间间隔
	 * @param retryCount
	 *            失败后重试次数
	 * @param usingReferer
	 *            请求头是否携带referer
	 * @param keepSession
	 *            是否保持会话
	 * @param multiThread
	 *            是否启用多线程连接池
	 * @param maxTotalConnections
	 *            最大连接个数
	 * @param maxPerRoute
	 *            每个主机最大连接数
	 * @param noCache
	 *            是否关闭缓存
	 * @param noHeader
	 *            响应是否没有头
	 * @return http抓取工具实例
	 */
	public static HttpFetchUtil createHttpFetchUtil(long minFetchInterval,
			int retryCount, boolean usingReferer, boolean keepSession,
			boolean multiThread, int maxTotalConnections, int maxPerRoute,
			boolean noCache, boolean noHeader) {
		HttpFetchUtil httpFetchUtil = new HttpFetchUtil(multiThread,
				retryCount, maxTotalConnections, maxPerRoute, noCache, noHeader);
		httpFetchUtil.setMinFetchInterval(minFetchInterval);
		httpFetchUtil.setUsingReferer(usingReferer);
		httpFetchUtil.setKeepSession(keepSession);

		return httpFetchUtil;
	}

	/**
	 * 初始化HttpClient
	 */
	private void initHttpClient(CookieStore cookieStore) {
		HttpClientBuilder builder = HttpClientBuilder.create();
		if (multiThread) {
			PoolingHttpClientConnectionManager pcm;
			if (noHeader) {
				pcm = new PoolingHttpClientConnectionManager(
						new ManagedHttpClientConnectionFactory(
								NoHeaderHttpResponseParserFactory.INSTANCE));
			}
			else {
				pcm = new PoolingHttpClientConnectionManager(10,
						TimeUnit.SECONDS);
			}
			pcm.setMaxTotal(maxTotalConnections);
			pcm.setDefaultMaxPerRoute(maxPerRoute);
			builder.setConnectionManager(pcm);
		}
		else {
			BasicHttpClientConnectionManager bm;
			if (noHeader) {
				Lookup<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
						.<ConnectionSocketFactory> create()
						.register("http",
								PlainConnectionSocketFactory.getSocketFactory())
						.register("https",
								SSLConnectionSocketFactory.getSocketFactory())
						.build();
				bm = new BasicHttpClientConnectionManager(
						socketFactoryRegistry,
						new ManagedHttpClientConnectionFactory(
								NoHeaderHttpResponseParserFactory.INSTANCE));
			}
			else {
				bm = new BasicHttpClientConnectionManager();
			}

			builder.setConnectionManager(bm);
		}

		this.cookieStore = cookieStore;
		builder.setDefaultCookieStore(new EmptyCookieStore());
		builder.setRedirectStrategy(new LaxRedirectStrategy());
		builder.setRetryHandler(new DefaultHttpRequestRetryHandler(retryCount,
				true));
		if (!noCache) {
			cacheInterceptor = new HttpCacheInterceptor();
			cacheInterceptor.setCacheFirst(cacheFirst);
			builder
					.addInterceptorLast((HttpResponseInterceptor) cacheInterceptor);
			builder
					.addInterceptorLast((HttpRequestInterceptor) cacheInterceptor);
		}
		client = builder.build();
	}

	/**
	 * 释放连接池连接
	 */
	public void close() {
		if (client != null) {
			try {
				client.close();
			}
			catch (Exception ex) {
			}
		}
	}

	/**
	 * 释放连接池连接
	 */
	public void releaseConnection() {
		CloseableHttpClient client = this.client;
		initHttpClient(cookieStore);
		if (client != null) {
			try {
				client.close();
			}
			catch (Exception ex) {
			}
		}
	}

	/**
	 * 使用get抓取xml数据
	 * 
	 * @param url
	 *            地址
	 * @return 数据xml文件
	 */
	public Document getXmlDoc(String url) throws Exception {
		SAXReader reader = new SAXReader();
		return getXmlDoc(url, null, reader);
	}

	/**
	 * 使用get抓取xml数据
	 * 
	 * @param url
	 *            地址
	 * @param reader
	 *            SAXReader
	 * @return 数据xml文件
	 */
	public Document getXmlDoc(String url, SAXReader reader) throws Exception {
		return getXmlDoc(url, null, reader);
	}

	/**
	 * 使用get方式抓取xml数据
	 * 
	 * @param url
	 *            地址
	 * @param headers
	 *            请求头
	 * @return 数据xml文件
	 */
	public Document getXmlDoc(String url, Map<String, String> headers)
			throws Exception {
		SAXReader reader = new SAXReader();
		return getXmlDoc(url, headers, reader);
	}

	/**
	 * 使用get方式抓取xml数据
	 * 
	 * @param url
	 *            地址
	 * @param headers
	 *            请求头
	 * @param reader
	 *            SAXReader
	 * @return 数据xml文件
	 */
	public Document getXmlDoc(String url, Map<String, String> headers,
			SAXReader reader) throws Exception {
		CloseableHttpResponse response = null;
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		HttpClientContext context = this.context.get();

		try {
			retry = 0;
			while (retry < retryCount) {
				try {
					HttpUriRequest get = createHttpGet(url, headers,
							handleRedirects);
					timeWatcher.watch();
					response = client.execute(get, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}

					return reader.read(in);
				}
				catch (CommonLogicException ex) {
					throw ex;
				}
				catch (DocumentException ex) {
					ErrorHandler errorHandler = reader.getErrorHandler();
					if (errorHandler != null) {
						errorHandler.fatalError(new SAXParseException(ex
								.getMessage(), null, ex));
					}
					String content = getStringFromUrl(url, "utf-8", headers);
					content = HttpFetchUtil.stripInvalidXMLCharacters(content);
					return reader.read(new StringReader(content));
				}
				catch (Exception ex) {
					ErrorHandler errorHandler = reader.getErrorHandler();
					if (errorHandler != null) {
						errorHandler.fatalError(new SAXParseException(ex
								.getMessage(), null, ex));
					}
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
			}
		}
		catch (Exception ex) {
			if (response != null) {
				try {
					response.close();
					response = null;
				}
				catch (Exception ex1) {
				}
			}
			releaseConnection();
			watchForError();

			throw ex;
		}
		finally {
			if (response != null) {
				try {
					response.close();
				}
				catch (Exception ex) {
				}
			}
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用post抓取xml数据
	 * 
	 * @param url
	 *            地址
	 * @param params
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @return 数据xml文件
	 */
	public Document getXmlDoc(String url, Map<String, Object> params,
			String postCharset) throws Exception {
		SAXReader reader = new SAXReader();
		return getXmlDoc(url, params, postCharset, null, reader);
	}

	/**
	 * 使用post抓取xml数据
	 * 
	 * @param url
	 *            地址
	 * @param params
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param reader
	 *            SAXReader
	 * @return 数据xml文件
	 */
	public Document getXmlDoc(String url, Map<String, Object> params,
			String postCharset, SAXReader reader) throws Exception {
		return getXmlDoc(url, params, postCharset, null, reader);
	}

	/**
	 * 使用post方式抓取xml数据
	 * 
	 * @param url
	 *            地址
	 * @param params
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param headers
	 *            请求头
	 * @return 数据xml文件
	 */
	public Document getXmlDoc(String url, Map<String, Object> params,
			String postCharset, Map<String, String> headers) throws Exception {
		SAXReader reader = new SAXReader();
		return getXmlDoc(url, params, postCharset, headers, reader);
	}

	/**
	 * 使用post方式抓取xml数据
	 * 
	 * @param url
	 *            地址
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param headers
	 *            请求头
	 * @param reader
	 *            SAXReader
	 * @return 数据xml文件
	 */
	public Document getXmlDoc(String url, Map<String, Object> parameters,
			String postCharset, Map<String, String> headers, SAXReader reader)
			throws Exception {
		CloseableHttpResponse response = null;
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		HttpClientContext context = this.context.get();

		try {
			retry = 0;
			while (retry < retryCount) {
				try {
					HttpUriRequest post = createHttpPost(url, parameters,
							postCharset, headers, handleRedirects);
					timeWatcher.watch();
					response = client.execute(post, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}

					return reader.read(in);
				}
				catch (CommonLogicException ex) {
					throw ex;
				}
				catch (DocumentException ex) {
					ErrorHandler errorHandler = reader.getErrorHandler();
					if (errorHandler != null) {
						errorHandler.fatalError(new SAXParseException(ex
								.getMessage(), null, ex));
					}
					String content = getStringFromUrl(url, "utf-8", parameters,
							postCharset, headers);
					content = HttpFetchUtil.stripInvalidXMLCharacters(content);
					return reader.read(new StringReader(content));
				}
				catch (Exception ex) {
					ErrorHandler errorHandler = reader.getErrorHandler();
					if (errorHandler != null) {
						errorHandler.fatalError(new SAXParseException(ex
								.getMessage(), null, ex));
					}
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
			}
		}
		catch (Exception ex) {
			if (response != null) {
				try {
					response.close();
					response = null;
				}
				catch (Exception ex1) {
				}
			}
			releaseConnection();
			watchForError();

			throw ex;
		}
		finally {
			if (response != null) {
				try {
					response.close();
				}
				catch (Exception ex) {
				}
			}
			this.context.remove();
		}

		return null;
	}

	/**
	 * 解析网页内容
	 * 
	 * @param htmlContent
	 *            网页内容
	 * @return dom树
	 * @throws Exception
	 */
	public Document getHtmlDoc(String htmlContent) throws Exception {
		if (StringUtils.isBlank(htmlContent)) {
			return null;
		}

		DOMParser parser = new DOMParser();
		parser
				.setFeature(
						"http://cyberneko.org/html/features/scanner/fix-mswindows-refs",
						true);
		parser.setFeature(HTMLScanner.NOTIFY_CHAR_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_XML_BUILTIN_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_HTML_BUILTIN_REFS, true);
		parser.setFeature(Constants.SAX_FEATURE_PREFIX
				+ Constants.NAMESPACES_FEATURE, false);
		parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_IFRAME, true);
		parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_TAGS, true);

		parser.parse(new InputSource(new StringReader(htmlContent)));
		org.w3c.dom.Document w3cDoc = parser.getDocument();
		DOMReader domReader = new DOMReader();

		return domReader.read(w3cDoc);
	}

	/**
	 * 使用get方式抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @return dom树
	 * @throws Exception
	 */
	public Document getHtmlDoc(String url, String defaultResponseCharset)
			throws Exception {
		String content = getStringFromUrl(url, defaultResponseCharset, null);
		if (content == null) {
			return null;
		}
		DOMParser parser = new DOMParser();
		parser.setFeature(HTMLScanner.FIX_MSWINDOWS_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_CHAR_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_XML_BUILTIN_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_HTML_BUILTIN_REFS, true);
		parser.setProperty(
				"http://cyberneko.org/html/properties/default-encoding",
				defaultResponseCharset);
		parser.setFeature(Constants.SAX_FEATURE_PREFIX
				+ Constants.NAMESPACES_FEATURE, false);
		parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_IFRAME, true);
		parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_TAGS, true);
		parser.parse(new InputSource(new StringReader(content)));

		org.w3c.dom.Document w3cDoc = parser.getDocument();
		DOMReader domReader = new DOMReader();

		return domReader.read(w3cDoc);
	}

	/**
	 * 使用get方式抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param headers
	 *            请求头
	 * @return dom树
	 * @throws Exception
	 */
	public Document getHtmlDoc(String url, String defaultResponseCharset,
			Map<String, String> headers) throws Exception {
		String content = getStringFromUrl(url, defaultResponseCharset, headers);
		if (content == null) {
			return null;
		}
		DOMParser parser = new DOMParser();
		parser.setFeature(HTMLScanner.FIX_MSWINDOWS_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_CHAR_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_XML_BUILTIN_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_HTML_BUILTIN_REFS, true);
		parser.setProperty(
				"http://cyberneko.org/html/properties/default-encoding",
				defaultResponseCharset);
		parser.setFeature(Constants.SAX_FEATURE_PREFIX
				+ Constants.NAMESPACES_FEATURE, false);
		parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_IFRAME, true);
		parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_TAGS, true);

		parser.parse(new InputSource(new StringReader(content)));

		org.w3c.dom.Document w3cDoc = parser.getDocument();
		DOMReader domReader = new DOMReader();

		return domReader.read(w3cDoc);
	}

	/**
	 * 使用post方式抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param params
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @return dom树
	 * @throws Exception
	 */
	public Document getHtmlDoc(String url, String defaultResponseCharset,
			Map<String, Object> params, String postCharset) throws Exception {
		String content = getStringFromUrl(url, defaultResponseCharset, params,
				postCharset, null);
		if (content == null) {
			return null;
		}
		DOMParser parser = new DOMParser();
		parser.setFeature(HTMLScanner.FIX_MSWINDOWS_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_CHAR_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_XML_BUILTIN_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_HTML_BUILTIN_REFS, true);
		parser.setProperty(
				"http://cyberneko.org/html/properties/default-encoding",
				defaultResponseCharset);
		parser.setFeature(Constants.SAX_FEATURE_PREFIX
				+ Constants.NAMESPACES_FEATURE, false);
		parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_IFRAME, true);
		parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_TAGS, true);

		parser.parse(new InputSource(new StringReader(content)));

		org.w3c.dom.Document w3cDoc = parser.getDocument();
		DOMReader domReader = new DOMReader();

		return domReader.read(w3cDoc);
	}

	/**
	 * 使用post方式抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param params
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param headers
	 *            请求头
	 * @return dom树
	 * @throws Exception
	 */
	public Document getHtmlDoc(String url, String defaultResponseCharset,
			Map<String, Object> params, String postCharset,
			Map<String, String> headers) throws Exception {
		String content = getStringFromUrl(url, defaultResponseCharset, params,
				postCharset, headers);
		if (content == null) {
			return null;
		}
		DOMParser parser = new DOMParser();
		parser.setFeature(HTMLScanner.FIX_MSWINDOWS_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_CHAR_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_XML_BUILTIN_REFS, true);
		parser.setFeature(HTMLScanner.NOTIFY_HTML_BUILTIN_REFS, true);
		parser.setProperty(
				"http://cyberneko.org/html/properties/default-encoding",
				defaultResponseCharset);
		parser.setFeature(Constants.SAX_FEATURE_PREFIX
				+ Constants.NAMESPACES_FEATURE, false);
		parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_IFRAME, true);
		parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_TAGS, true);

		parser.parse(new InputSource(new StringReader(content)));

		org.w3c.dom.Document w3cDoc = parser.getDocument();
		DOMReader domReader = new DOMReader();

		return domReader.read(w3cDoc);
	}

	/**
	 * 发送http get请求
	 * 
	 * @param url
	 *            url地址
	 * @return http响应
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url) throws Exception {
		return sendHttpRequest(url, true, null);
	}

	/**
	 * 发送http get请求
	 * 
	 * @param url
	 *            url地址
	 * @param headers
	 *            请求头
	 * @return http响应
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url,
			Map<String, String> headers) throws Exception {
		return sendHttpRequest(url, true, headers);
	}

	/**
	 * 发送http get请求
	 * 
	 * @param url
	 *            url地址
	 * @param handleRedirects
	 *            是否处理301/302跳转
	 * @return http响应
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url,
			boolean handleRedirects) throws Exception {
		return sendHttpRequest(url, handleRedirects, null);
	}

	/**
	 * 发送http get请求
	 * 
	 * @param url
	 *            url地址
	 * @param handleRedirects
	 *            是否处理301/302跳转
	 * @param headers
	 *            请求头
	 * @return http响应
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url,
			boolean handleRedirects, Map<String, String> headers)
			throws Exception {
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest get = createHttpGet(url, headers,
							handleRedirects);
					timeWatcher.watch();
					response = client.execute(get, context);
					if (headers != null && headers.containsKey(ABORT_REQUEST)) {
						get.abort();
					}

					return response;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 发送http post请求
	 * 
	 * @param url
	 *            url地址
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @return http响应
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url,
			Map<String, Object> parameters, String postCharset)
			throws Exception {
		return sendHttpRequest(url, parameters, postCharset, true, null);
	}

	/**
	 * 发送http post请求
	 * 
	 * @param url
	 *            url地址
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param headers
	 *            请求头
	 * @return http响应
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url,
			Map<String, Object> parameters, String postCharset,
			Map<String, String> headers) throws Exception {
		return sendHttpRequest(url, parameters, postCharset, true, headers);
	}

	/**
	 * 发送http post请求
	 * 
	 * @param url
	 *            url地址
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param handleRedirects
	 *            是否处理301/302跳转
	 * @return http响应
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url,
			Map<String, Object> parameters, String postCharset,
			boolean handleRedirects) throws Exception {
		return sendHttpRequest(url, parameters, postCharset, handleRedirects,
				null);
	}

	/**
	 * 发送http post请求
	 * 
	 * @param url
	 *            url地址
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param handleRedirects
	 *            是否处理301/302跳转
	 * @param headers
	 *            请求头
	 * @return http响应
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url,
			Map<String, Object> parameters, String postCharset,
			boolean handleRedirects, Map<String, String> headers)
			throws Exception {
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest post = createHttpPost(url, parameters,
							postCharset, headers, handleRedirects);
					timeWatcher.watch();
					response = client.execute(post, context);
					if (headers != null && headers.containsKey(ABORT_REQUEST)) {
						post.abort();
					}

					return response;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用get方式取得301、302跳转地址
	 * 
	 * @param url
	 *            url地址
	 * @return 跳转地址
	 * @throws Exception
	 */
	public String getRedirectLocation(String url) throws Exception {
		return getRedirectLocation(url, null);
	}

	/**
	 * 使用get方式取得301、302跳转地址
	 * 
	 * @param url
	 *            url地址
	 * @param headers
	 *            请求头
	 * @return 跳转地址
	 * @throws Exception
	 */
	public String getRedirectLocation(String url, Map<String, String> headers)
			throws Exception {
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest get = createHttpGet(url, headers, false);
					timeWatcher.watch();
					response = client.execute(get, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY
							|| statusCode == HttpStatus.SC_MOVED_PERMANENTLY
							|| statusCode == HttpStatus.SC_TEMPORARY_REDIRECT
							|| statusCode == HttpStatus.SC_SEE_OTHER) {
						return this.location.get();
					}
					else if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (statusCode != HttpStatus.SC_OK
							&& statusCode != HttpStatus.SC_PARTIAL_CONTENT) {
						throw new Exception("发送http请求失败" + statusLine);
					}
					else {
						return null;
					}
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
				finally {
					if (response != null) {
						try {
							response.close();
						}
						catch (Exception ex) {
						}
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用post方式取得301、302跳转地址
	 * 
	 * @param url
	 *            url地址
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @return 跳转地址
	 * @throws Exception
	 */
	public String getRedirectLocation(String url,
			Map<String, Object> parameters, String postCharset)
			throws Exception {
		return getRedirectLocation(url, parameters, postCharset, null);
	}

	/**
	 * 使用post方式取得301、302跳转地址
	 * 
	 * @param url
	 *            url地址
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param headers
	 *            请求头
	 * @return 跳转地址
	 * @throws Exception
	 */
	public String getRedirectLocation(String url,
			Map<String, Object> parameters, String postCharset,
			Map<String, String> headers) throws Exception {
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest post = createHttpPost(url, parameters,
							postCharset, headers, false);
					timeWatcher.watch();
					response = client.execute(post, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY
							|| statusCode == HttpStatus.SC_MOVED_PERMANENTLY
							|| statusCode == HttpStatus.SC_TEMPORARY_REDIRECT
							|| statusCode == HttpStatus.SC_SEE_OTHER) {
						return this.location.get();
					}
					else if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (statusCode != HttpStatus.SC_OK
							&& statusCode != HttpStatus.SC_PARTIAL_CONTENT) {
						throw new Exception("发送http请求失败" + statusLine);
					}
					else {
						return null;
					}
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
				finally {
					if (response != null) {
						try {
							response.close();
						}
						catch (Exception ex) {
						}
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用get方式抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @return http网络流
	 * @throws Exception
	 */
	public InputStream getInputStreamFromUrl(String url) throws Exception {
		return getInputStreamFromUrl(url, null);
	}

	/**
	 * 使用get方式抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param headers
	 *            请求头
	 * @return http网络流
	 * @throws Exception
	 */
	public InputStream getInputStreamFromUrl(String url,
			Map<String, String> headers) throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest get = createHttpGet(url, headers,
							handleRedirects);
					timeWatcher.watch();
					response = client.execute(get, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}

					return new HttpInputStream(in, response);
				}
				catch (CommonLogicException ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					throw ex;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用post方式抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @return http网络流
	 * @throws Exception
	 */
	public InputStream getInputStreamFromUrl(String url,
			Map<String, Object> parameters, String postCharset)
			throws Exception {
		return getInputStreamFromUrl(url, parameters, postCharset, null);
	}

	/**
	 * 使用post方式抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param headers
	 *            请求头
	 * @return http网络流
	 * @throws Exception
	 */
	public InputStream getInputStreamFromUrl(String url,
			Map<String, Object> parameters, String postCharset,
			Map<String, String> headers) throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest post = createHttpPost(url, parameters,
							postCharset, headers, handleRedirects);
					timeWatcher.watch();
					response = client.execute(post, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}

					return new HttpInputStream(in, response);
				}
				catch (CommonLogicException ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					throw ex;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用get方式抓取网页内容获取Reader流
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http响应字符集
	 * @return http网络流
	 * @throws Exception
	 */
	public Reader getReaderFromUrl(String url, String defaultResponseCharset)
			throws Exception {
		return getReaderFromUrl(url, defaultResponseCharset, null);
	}

	/**
	 * 使用get方式抓取网页内容获取Reader流
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http响应字符集
	 * @param headers
	 *            请求头
	 * @return http网络流
	 * @throws Exception
	 */
	public Reader getReaderFromUrl(String url, String defaultResponseCharset,
			Map<String, String> headers) throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest get = createHttpGet(url, headers,
							handleRedirects);
					timeWatcher.watch();
					response = client.execute(get, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}
					ContentType contentType = ContentType.get(entity);
					Charset charset = null;
					if (contentType != null) {
						charset = contentType.getCharset();
					}
					if (charset == null) {
						charset = Charset.forName(defaultResponseCharset);
					}

					return new HttpReader(new InputStreamReader(in, charset),
							response);
				}
				catch (CommonLogicException ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					throw ex;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用post方式抓取网页内容获取Reader流
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @return http网络流
	 * @throws Exception
	 */
	public Reader getReaderFromUrl(String url, String defaultResponseCharset,
			Map<String, Object> parameters, String postCharset)
			throws Exception {
		return getReaderFromUrl(url, defaultResponseCharset, parameters,
				postCharset, null);
	}

	/**
	 * 使用post方式抓取网页内容获取Reader流
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param headers
	 *            请求头
	 * @return http网络流
	 * @throws Exception
	 */
	public Reader getReaderFromUrl(String url, String defaultResponseCharset,
			Map<String, Object> parameters, String postCharset,
			Map<String, String> headers) throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest post = createHttpPost(url, parameters,
							postCharset, headers, handleRedirects);
					timeWatcher.watch();
					response = client.execute(post, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}
					ContentType contentType = ContentType.get(entity);
					Charset charset = null;
					if (contentType != null) {
						charset = contentType.getCharset();
					}
					if (charset == null) {
						charset = Charset.forName(defaultResponseCharset);
					}

					return new HttpReader(new InputStreamReader(in, charset),
							response);
				}
				catch (CommonLogicException ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					throw ex;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用get方式抓取网页内容获取LineNumberReader流
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http响应字符集
	 * @return http网络流
	 * @throws Exception
	 */
	public LineNumberReader getLineReaderFromUrl(String url,
			String defaultResponseCharset) throws Exception {
		return getLineReaderFromUrl(url, defaultResponseCharset, null);
	}

	/**
	 * 使用get方式抓取网页内容获取LineNumberReader流
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http响应字符集
	 * @param headers
	 *            请求头
	 * @return http网络流
	 * @throws Exception
	 */
	public LineNumberReader getLineReaderFromUrl(String url,
			String defaultResponseCharset, Map<String, String> headers)
			throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest get = createHttpGet(url, headers,
							handleRedirects);
					timeWatcher.watch();
					response = client.execute(get, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}
					ContentType contentType = ContentType.get(entity);
					Charset charset = null;
					if (contentType != null) {
						checkTextContentType(entity.getContentLength(), contentType);
						charset = contentType.getCharset();
					}
					if (charset == null) {
						charset = Charset.forName(defaultResponseCharset);
					}

					return new HttpLineNumberReader(new InputStreamReader(in,
							charset), response);
				}
				catch (CommonLogicException ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					throw ex;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/*
	 * 检查contentType是否为文本
	 */
	private void checkTextContentType(long contentLength, ContentType contentType) {
		if (contentLength > 0 && contentLength < 5 * 1024 * 1024) {
			return;
		}
		String mimeType = contentType.getMimeType();
		if (!mimeType.contains("text") && !mimeType.contains("json")
				&& !mimeType.contains("javascript")
				&& !mimeType.contains("xml")
				&& !StringUtils.containsIgnoreCase(mimeType, "mpegurl")) {
			throw new ContentTypeException(mimeType);
		}
	}

	/**
	 * 使用post方式抓取网页内容获取LineNumberReader流
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @return http网络流
	 * @throws Exception
	 */
	public LineNumberReader getLineReaderFromUrl(String url,
			String defaultResponseCharset, Map<String, Object> parameters,
			String postCharset) throws Exception {
		return getLineReaderFromUrl(url, defaultResponseCharset, parameters,
				postCharset, null);
	}

	/**
	 * 使用post方式抓取网页内容获取LineNumberReader流
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param headers
	 *            请求头
	 * @return http网络流
	 * @throws Exception
	 */
	public LineNumberReader getLineReaderFromUrl(String url,
			String defaultResponseCharset, Map<String, Object> parameters,
			String postCharset, Map<String, String> headers) throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest post = createHttpPost(url, parameters,
							postCharset, headers, handleRedirects);
					timeWatcher.watch();
					response = client.execute(post, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}
					ContentType contentType = ContentType.get(entity);
					Charset charset = null;
					if (contentType != null) {
						checkTextContentType(entity.getContentLength(), contentType);
						charset = contentType.getCharset();
					}
					if (charset == null) {
						charset = Charset.forName(defaultResponseCharset);
					}

					return new HttpLineNumberReader(new InputStreamReader(in,
							charset), response);
				}
				catch (CommonLogicException ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					throw ex;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用get方式按行抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http响应字符集
	 * @return 网页内容
	 * @throws Exception
	 */
	public List<String> getLineStringFromUrl(String url,
			String defaultResponseCharset) throws Exception {
		return getLineStringFromUrl(url, defaultResponseCharset, null);
	}

	/**
	 * 使用get方式按行抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http响应字符集
	 * @param headers
	 *            请求头
	 * @return 网页内容
	 * @throws Exception
	 */
	public List<String> getLineStringFromUrl(String url,
			String defaultResponseCharset, Map<String, String> headers)
			throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		List<String> result = new ArrayList<String>();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest get = createHttpGet(url, headers,
							handleRedirects);
					timeWatcher.watch();
					response = client.execute(get, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}
					ContentType contentType = ContentType.get(entity);
					Charset charset = null;
					if (contentType != null) {
						checkTextContentType(entity.getContentLength(), contentType);
						charset = contentType.getCharset();
					}
					if (charset == null) {
						charset = Charset.forName(defaultResponseCharset);
					}

					LineNumberReader reader = new HttpLineNumberReader(
							new InputStreamReader(in, charset), response);
					String line = null;
					while ((line = reader.readLine()) != null) {
						result.add(line);
					}

					return result;
				}
				catch (CommonLogicException ex) {
					throw ex;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
				finally {
					if (response != null) {
						try {
							response.close();
						}
						catch (Exception ex) {
						}
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return result;
	}

	/**
	 * 使用post方式按行抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @return 网页内容
	 * @throws Exception
	 */
	public List<String> getLineStringFromUrl(String url,
			String defaultResponseCharset, Map<String, Object> parameters,
			String postCharset) throws Exception {
		return getLineStringFromUrl(url, defaultResponseCharset, parameters,
				postCharset, null);
	}

	/**
	 * 使用post方式按行抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param headers
	 *            请求头
	 * @return 网页内容
	 * @throws Exception
	 */
	public List<String> getLineStringFromUrl(String url,
			String defaultResponseCharset, Map<String, Object> parameters,
			String postCharset, Map<String, String> headers) throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		List<String> result = new ArrayList<String>();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest post = createHttpPost(url, parameters,
							postCharset, headers, handleRedirects);
					timeWatcher.watch();
					response = client.execute(post, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}
					ContentType contentType = ContentType.get(entity);
					Charset charset = null;
					if (contentType != null) {
						checkTextContentType(entity.getContentLength(), contentType);
						charset = contentType.getCharset();
					}
					if (charset == null) {
						charset = Charset.forName(defaultResponseCharset);
					}

					LineNumberReader reader = new HttpLineNumberReader(
							new InputStreamReader(in, charset), response);
					String line = null;
					while ((line = reader.readLine()) != null) {
						result.add(line);
					}

					return result;
				}
				catch (CommonLogicException ex) {
					throw ex;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
				finally {
					if (response != null) {
						try {
							response.close();
						}
						catch (Exception ex) {
						}
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return result;
	}

	/**
	 * 使用get方式抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @return html代码
	 * @throws Exception
	 */
	public String getStringFromUrl(String url, String defaultResponseCharset)
			throws Exception {
		return getStringFromUrl(url, defaultResponseCharset, null);
	}

	/**
	 * 使用get方式抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param headers
	 *            请求头
	 * @return html代码
	 * @throws Exception
	 */
	public String getStringFromUrl(String url, String defaultResponseCharset,
			Map<String, String> headers) throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest get = createHttpGet(url, headers,
							handleRedirects);
					timeWatcher.watch();
					response = client.execute(get, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}
					ContentType contentType = ContentType.get(entity);
					Charset charset = null;
					if (contentType != null) {
						checkTextContentType(entity.getContentLength(), contentType);
						charset = contentType.getCharset();
					}
					if (charset == null) {
						charset = Charset.forName(defaultResponseCharset);
					}

					return IOUtils.toString(in, charset);
				}
				catch (CommonLogicException ex) {
					throw ex;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
				finally {
					if (response != null) {
						try {
							response.close();
						}
						catch (Exception ex) {
						}
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用post方式抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @return html代码
	 * @throws Exception
	 */
	public String getStringFromUrl(String url, String defaultResponseCharset,
			Map<String, Object> parameters, String postCharset)
			throws Exception {
		return getStringFromUrl(url, defaultResponseCharset, parameters,
				postCharset, null);
	}

	/**
	 * 使用post方式抓取网页内容
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param headers
	 *            请求头
	 * @return html代码
	 * @throws Exception
	 */
	public String getStringFromUrl(String url, String defaultResponseCharset,
			Map<String, Object> parameters, String postCharset,
			Map<String, String> headers) throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest post = createHttpPost(url, parameters,
							postCharset, headers, handleRedirects);
					timeWatcher.watch();
					response = client.execute(post, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}
					ContentType contentType = ContentType.get(entity);
					Charset charset = null;
					if (contentType != null) {
						checkTextContentType(entity.getContentLength(), contentType);
						charset = contentType.getCharset();
					}
					if (charset == null) {
						charset = Charset.forName(defaultResponseCharset);
					}

					return IOUtils.toString(in, charset);
				}
				catch (CommonLogicException ex) {
					throw ex;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
				finally {
					if (response != null) {
						try {
							response.close();
						}
						catch (Exception ex) {
						}
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用get方式抓取html/xml网页内容,并去掉namespace
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @return 过滤后的html代码
	 * @throws Exception
	 */
	public String getXmlStringFromUrl(String url, String defaultResponseCharset)
			throws Exception {
		return getXmlStringFromUrl(url, defaultResponseCharset, null);
	}

	/**
	 * 使用get方式抓取html/xml网页内容,并去掉namespace
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param headers
	 *            请求头
	 * @return 过滤后的html代码
	 * @throws Exception
	 */
	public String getXmlStringFromUrl(String url,
			String defaultResponseCharset, Map<String, String> headers)
			throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest get = createHttpGet(url, headers,
							handleRedirects);
					timeWatcher.watch();
					response = client.execute(get, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}
					ContentType contentType = ContentType.get(entity);
					Charset charset = null;
					if (contentType != null) {
						checkTextContentType(entity.getContentLength(), contentType);
						charset = contentType.getCharset();
					}
					if (charset == null) {
						charset = Charset.forName(defaultResponseCharset);
					}

					LineNumberReader reader = new LineNumberReader(
							new InputStreamReader(in, charset));
					StringBuilder content = new StringBuilder();
					Matcher m = null;
					String line = null;
					while ((line = reader.readLine()) != null) {
						line = line.trim();
						if (StringUtils.isBlank(line)) {
							continue;
						}

						if (m == null) {
							m = xmlPattern.matcher(line);
							if (m.matches()) {
								line = new StringBuilder("<")
										.append(m.group(1)).append(">").append(
												m.group(2)).toString();
							}
							else {
								m = null;
							}
						}
						content.append(line);
						content.append("\n");
					}

					return content.toString();
				}
				catch (CommonLogicException ex) {
					throw ex;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
				finally {
					if (response != null) {
						try {
							response.close();
						}
						catch (Exception ex) {
						}
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用post方式抓取html/xml网页内容,并去掉namespace
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @return 过滤后的html代码
	 * @throws Exception
	 */
	public String getXmlStringFromUrl(String url,
			String defaultResponseCharset, Map<String, Object> parameters,
			String postCharset) throws Exception {
		return getXmlStringFromUrl(url, defaultResponseCharset, parameters,
				postCharset, null);
	}

	/**
	 * 使用post方式抓取html/xml网页内容,并去掉namespace
	 * 
	 * @param url
	 *            url地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param headers
	 *            请求头
	 * @return 过滤后的html代码
	 * @throws Exception
	 */
	public String getXmlStringFromUrl(String url,
			String defaultResponseCharset, Map<String, Object> parameters,
			String postCharset, Map<String, String> headers) throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		CloseableHttpResponse response = null;
		HttpClientContext context = this.context.get();
		retry = 0;
		try {
			while (retry < retryCount) {
				try {
					HttpUriRequest post = createHttpPost(url, parameters,
							postCharset, headers, handleRedirects);
					timeWatcher.watch();
					response = client.execute(post, context);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					this.currentStatusCode.set(statusCode);
					Header header = response.getFirstHeader("Content-Type");
					this.contentType.set(header != null ? header.getValue()
							: null);
					this.headers.set(response.getAllHeaders());
					header = response.getFirstHeader("Location");
					this.location
							.set(header != null ? header.getValue() : null);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						return null;
					}
					else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
							|| statusCode == HttpStatus.SC_BAD_GATEWAY
							|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
							|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
							|| statusCode == HttpStatus.SC_FORBIDDEN) {
						throw new Exception(statusLine.toString());
					}
					else if (!handleRedirects
							&& (statusCode != HttpStatus.SC_OK
									&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
						throw new CommonLogicException(statusCode, statusLine
								.toString());
					}
					else if (in == null) {
						throw new Exception("抓取文件失败" + statusLine);
					}
					ContentType contentType = ContentType.get(entity);
					Charset charset = null;
					if (contentType != null) {
						checkTextContentType(entity.getContentLength(), contentType);
						charset = contentType.getCharset();
					}
					if (charset == null) {
						charset = Charset.forName(defaultResponseCharset);
					}

					LineNumberReader reader = new LineNumberReader(
							new InputStreamReader(in, charset));
					StringBuilder content = new StringBuilder();
					Matcher m = null;
					String line = null;
					while ((line = reader.readLine()) != null) {
						line = line.trim();
						if (StringUtils.isBlank(line)) {
							continue;
						}

						if (m == null) {
							m = xmlPattern.matcher(line);
							if (m.matches()) {
								line = new StringBuilder("<")
										.append(m.group(1)).append(">").append(
												m.group(2)).toString();
							}
							else {
								m = null;
							}
						}
						content.append(line);
						content.append("\n");
					}

					return content.toString();
				}
				catch (CommonLogicException ex) {
					throw ex;
				}
				catch (Exception ex) {
					if (response != null) {
						try {
							response.close();
							response = null;
						}
						catch (Exception ex1) {
						}
					}
					releaseConnection();
					watchForError();
					if (retry >= retryCount) {
						throw ex;
					}
				}
				finally {
					if (response != null) {
						try {
							response.close();
						}
						catch (Exception ex) {
						}
					}
				}
			}
		}
		finally {
			this.context.remove();
		}

		return null;
	}

	/**
	 * 使用代理post抓取网页内容
	 * 
	 * @param url
	 *            抓取地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param proxyIp
	 *            http代理ip
	 * @param proxyPort
	 *            http代理端口
	 * @return http网络流
	 * @throws Exception
	 */
	public String getStringFromUrl(String url, String defaultResponseCharset,
			Map<String, Object> parameters, String postCharset, String proxyIp,
			int proxyPort) throws Exception {
		return getStringFromUrl(url, defaultResponseCharset, parameters,
				postCharset, proxyIp, proxyPort, null);
	}

	/**
	 * 使用代理post抓取网页内容
	 * 
	 * @param url
	 *            抓取地址
	 * @param defaultResponseCharset
	 *            http默认响应字符集
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param proxyIp
	 *            http代理ip
	 * @param proxyPort
	 *            http代理端口
	 * @param headers
	 *            请求头
	 * @return http网络流
	 * @throws Exception
	 */
	public String getStringFromUrl(String url, String defaultResponseCharset,
			Map<String, Object> parameters, String postCharset, String proxyIp,
			int proxyPort, Map<String, String> headers) throws Exception {
		boolean handleRedirects = headers != null
				&& headers.containsKey(HANDLE_REDIRECTS);
		HttpUriRequest post = createHttpPost(url, parameters, postCharset,
				headers, handleRedirects);
		HttpClientContext context = this.context.get();
		timeWatcher.watch();
		CloseableHttpResponse response = client.execute(post, context);

		try {
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			this.currentStatusCode.set(statusCode);
			Header header = response.getFirstHeader("Content-Type");
			this.contentType.set(header != null ? header.getValue() : null);
			this.headers.set(response.getAllHeaders());
			header = response.getFirstHeader("Location");
			this.location.set(header != null ? header.getValue() : null);
			HttpEntity entity = response.getEntity();
			if (statusCode == HttpStatus.SC_NOT_FOUND) {
				return "";
			}
			else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
					|| statusCode == HttpStatus.SC_BAD_GATEWAY
					|| statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE
					|| statusCode == HttpStatus.SC_GATEWAY_TIMEOUT
					|| statusCode == HttpStatus.SC_FORBIDDEN) {
				throw new Exception(statusLine.toString());
			}
			else if (!handleRedirects
					&& (statusCode != HttpStatus.SC_OK
							&& statusCode != HttpStatus.SC_PARTIAL_CONTENT && statusCode != HttpStatus.SC_NOT_MODIFIED)) {
				throw new CommonLogicException(statusCode, statusLine.toString());
			}
			else if (entity == null) {
				throw new Exception("抓取文件失败" + statusLine);
			}
			InputStream in = entity.getContent();
			ContentType contentType = ContentType.get(entity);
			Charset charset = null;
			if (contentType != null) {
				checkTextContentType(entity.getContentLength(), contentType);
				charset = contentType.getCharset();
			}
			if (charset == null) {
				charset = Charset.forName(defaultResponseCharset);
			}

			return IOUtils.toString(in, charset);
		}
		catch (Exception ex) {
			releaseConnection();
			timeWatcher.watch(errorWaitTime);

			throw ex;
		}
		finally {
			try {
				response.close();
			}
			catch (Exception ex) {
			}
			this.context.remove();
		}
	}

	/**
	 * 检测url参数分隔符
	 * 
	 * @param url
	 *            url地址
	 * @return 分隔符
	 */
	public static String detectUrlParamSeparator(String url) {
		String separator = null;

		if (url.contains("?")) {
			char c = url.charAt(url.length() - 1);
			if (c == '?' || c == '&') {
				separator = "";
			}
			else {
				separator = "&";
			}
		}
		else {
			separator = "?";
		}

		return separator;
	}

	/**
	 * 获取http地址的域名
	 * 
	 * @param url
	 *            http地址
	 * @return 域名
	 */
	public static String findDomainName(String url) {
		int start = url.lastIndexOf("://");
		int end = 0;
		if (start != -1) {
			start += 3;
			end = url.indexOf('/', start);
		}
		else {
			start = 0;
			end = url.indexOf('/');
		}

		if (end != -1) {
			return url.substring(start, end);
		}
		return url.substring(start);
	}

	/**
	 * 取绝对地址
	 * 
	 * @param currentUrl
	 *            当前url地址
	 * @param nextUrl
	 *            要访问下一个url
	 * @return 返回要访问的url的绝对地址
	 */
	public static String findAbsoluteUrl(String currentUrl, String nextUrl) {
		nextUrl = nextUrl.trim();
		if (nextUrl.indexOf("://") != -1) {
			int idx = nextUrl.lastIndexOf("http://");
			if (idx != -1) {
				return nextUrl.substring(idx);
			}

			return nextUrl;
		}
		if (nextUrl.startsWith("/")) {
			int idx = currentUrl.indexOf("://");
			if (idx != -1) {
				idx = currentUrl.indexOf('/', idx + 3);
			}
			else {
				idx = currentUrl.indexOf('/');
			}
			if (idx != -1) {
				return currentUrl.substring(0, idx) + nextUrl;
			}
			return currentUrl + nextUrl;
		}
		else if (nextUrl.startsWith("./")) {
			nextUrl = nextUrl.substring(1);
		}
		else if (nextUrl.startsWith("#")) {
			return currentUrl + nextUrl;
		}
		else if (nextUrl.startsWith("?")) {
			int idx = currentUrl.lastIndexOf('?');
			if (idx != -1) {
				return currentUrl.substring(0, idx) + nextUrl;
			}
			return currentUrl + nextUrl;
		}
		else {
			nextUrl = "/" + nextUrl;
		}

		int idx = currentUrl.lastIndexOf('/');
		if (idx == -1) {
			return currentUrl + nextUrl;
		}

		int idx1 = currentUrl.indexOf("://");
		if (idx1 != -1 && idx == idx1 + 2) {
			return currentUrl + nextUrl;
		}
		return currentUrl.substring(0, idx) + nextUrl;
	}

	/**
	 * 修复破损的xml内容
	 * 
	 * @param content
	 *            xml内容
	 * @return 修复后的xml内容
	 */
	public static String repairXmlContent(String content) {
		if (content.charAt(0) != '<') {
			content = content.substring(1);
		}
		content = CONTROL_CHARACTER_PATTERN.matcher(content).replaceAll("");
		StringBuilder xml = new StringBuilder();
		Matcher m = ELEMENT_CONTENT_PATTERN.matcher(content);
		int end = 0;
		while (m.find(end)) {
			int s = m.start(1);
			int e = m.end(1);
			xml.append(content.substring(end, s));
			String g = m.group(1);
			if (StringUtils.isNotBlank(g)) {
				xml.append("<![CDATA[");
				xml.append(g.trim());
				xml.append("]]>");
			}
			end = e;
		}
		xml.append(content.substring(end));

		return xml.toString();
	}

	/**
	 * 过滤掉控制字符
	 * 
	 * @param content
	 *            string内容
	 * @return 过滤后的string内容
	 */
	public static String stripControlCharacters(String content) {
		return CONTROL_CHARACTER_PATTERN.matcher(content).replaceAll("");
	}

	/**
	 * 过滤掉xml中的特殊字符
	 * 
	 * @param content
	 *            xml内容
	 * @return 过滤后的xml内容
	 */
	public static String stripInvalidXMLCharacters(String content) {
		content = CONTROL_CHARACTER_PATTERN.matcher(content).replaceAll("");
		// 去除ifeng接口中xml内容重复的bug,http://v.ifeng.com/video_info_new/e/e2/653f2c55-2c42-4fd8-94ce-5fff42bc3ee2.xml
		int idx = content.indexOf("<?xml");
		if (idx > 0) {
			content = content.substring(idx);
		}
		idx = content.indexOf("<?xml", 6);
		if (idx != -1) {
			content = content.substring(0, idx);
		}

		return CONTROL_CHARACTER_PATTERN.matcher(content).replaceAll("");
	}

	/**
	 * 生成HttpGet请求
	 * 
	 * @param url
	 *            url地址
	 * @param headers
	 *            请求头
	 * @param handleRedirects
	 *            是否支持跳转
	 * @return HttpGet
	 */
	private HttpUriRequest createHttpGet(String url,
			Map<String, String> headers, boolean handleRedirects) {
		HttpClientContext context = this.context.get();
		RequestConfig.Builder configBuilder = RequestConfig.custom();
		configBuilder.setConnectTimeout(connectionTimeout);
		configBuilder.setSocketTimeout(soTimeout);
		configBuilder.setConnectionRequestTimeout(connectionTimeout);
		configBuilder.setMaxRedirects(5);
		configBuilder.setCookieSpec(cookieSpecs);

		if (keepSession && cookieStore != null) {
			context.setCookieStore(cookieStore);
		}
		if (proxyTask != null) {
			httpProxy = proxyTask.selectRandomProxy();
		}
		if (httpProxy != null) {
			HttpHost proxy = new HttpHost(httpProxy.ip, httpProxy.port);
			configBuilder.setProxy(proxy);
			if (StringUtils.isNotBlank(httpProxy.user)) {
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(AuthScope.ANY,
						new UsernamePasswordCredentials(httpProxy.user,
								httpProxy.password));
				context.setCredentialsProvider(credsProvider);
			}
		}
		if (handleRedirects) {
			configBuilder.setRedirectsEnabled(true);
			configBuilder.setRelativeRedirectsAllowed(true);
			configBuilder.setMaxRedirects(8);
		}
		else {
			configBuilder.setRedirectsEnabled(false);
		}
		configBuilder.setAuthenticationEnabled(authenticationEnabled);
		RequestBuilder reqBuilder = RequestBuilder.get();
		reqBuilder.setConfig(configBuilder.build());
		reqBuilder.setUri(url.trim());

		HttpUriRequest get = reqBuilder.build();
		String host = get.getURI().getHost();
		get.setHeader("User-Agent", CList.USER_AGENT_FIREFOX);
		get.setHeader("Accept", "*/*");
		get.setHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
		get.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		get.setHeader("Connection", "keep-alive");
		if (usingReferer) {
			if (StringUtils.isBlank(referer)) {
				get.setHeader("Referer", "http://" + host);
			}
			else {
				get.setHeader("Referer", referer);
			}
		}
		if (useringXForwardedFor) {
			get.setHeader(CList.HTTP_X_FORWARDED_FOR, ProxyTask
					.randomXForwardedFor());
		}
		if (headers != null && !headers.isEmpty()) {
			for (Entry<String, String> entry : headers.entrySet()) {
				String name = entry.getKey();
				if (StringUtils.isBlank(name) || HANDLE_REDIRECTS.equals(name)
						|| ABORT_REQUEST.equals(name)) {
					continue;
				}
				String value = entry.getValue();
				if (StringUtils.isBlank(value)) {
					get.removeHeaders(name);
				}
				else {
					get.setHeader(name, value);
				}
			}
		}

		return get;
	}

	/**
	 * 生成HttpPost请求
	 * 
	 * @param url
	 *            url地址
	 * @param parameters
	 *            post参数
	 * @param postCharset
	 *            post请求字符集
	 * @param headers
	 *            请求头
	 * @param handleRedirects
	 *            是否支持跳转
	 * @return HttpPost
	 */
	private HttpUriRequest createHttpPost(String url,
			Map<String, Object> parameters, String postCharset,
			Map<String, String> headers, boolean handleRedirects)
			throws Exception {
		HttpClientContext context = this.context.get();
		RequestConfig.Builder configBuilder = RequestConfig.custom();
		configBuilder.setConnectTimeout(connectionTimeout);
		configBuilder.setSocketTimeout(soTimeout);
		configBuilder.setConnectionRequestTimeout(connectionTimeout);
		configBuilder.setMaxRedirects(5);
		configBuilder.setCookieSpec(cookieSpecs);

		if (keepSession && cookieStore != null) {
			context.setCookieStore(cookieStore);
		}
		if (proxyTask != null) {
			httpProxy = proxyTask.selectRandomProxy();
		}
		if (httpProxy != null) {
			HttpHost proxy = new HttpHost(httpProxy.ip, httpProxy.port);
			configBuilder.setProxy(proxy);
			if (StringUtils.isNotBlank(httpProxy.user)) {
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(AuthScope.ANY,
						new UsernamePasswordCredentials(httpProxy.user,
								httpProxy.password));
				context.setCredentialsProvider(credsProvider);
			}
		}
		if (handleRedirects) {
			configBuilder.setRedirectsEnabled(true);
			configBuilder.setRelativeRedirectsAllowed(true);
			configBuilder.setMaxRedirects(8);
		}
		else {
			configBuilder.setRedirectsEnabled(false);
		}
		configBuilder.setAuthenticationEnabled(authenticationEnabled);
		RequestBuilder reqBuilder = RequestBuilder.post();
		reqBuilder.setConfig(configBuilder.build());
		reqBuilder.setUri(url.trim());

		reqBuilder.setHeader("User-Agent", CList.USER_AGENT_FIREFOX);
		reqBuilder.setHeader("Accept", "*/*");
		reqBuilder.setHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
		reqBuilder.setHeader("Accept-Language",
				"zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		reqBuilder.setHeader("Connection", "keep-alive");

		if (useringXForwardedFor) {
			reqBuilder.setHeader(CList.HTTP_X_FORWARDED_FOR, ProxyTask
					.randomXForwardedFor());
		}

		if (headers != null && !headers.isEmpty()) {
			for (Entry<String, String> entry : headers.entrySet()) {
				String name = entry.getKey();
				if (StringUtils.isBlank(name) || HANDLE_REDIRECTS.equals(name)
						|| ABORT_REQUEST.equals(name)) {
					continue;
				}
				String value = entry.getValue();
				if (StringUtils.isBlank(value)) {
					reqBuilder.removeHeaders(name);
				}
				else {
					reqBuilder.setHeader(name, value);
				}
			}
		}

		if (parameters != null && !parameters.isEmpty()) {
			// 0:普通类型 1:multipart类型 2:自定义类型
			int bodyType = 0;
			for (Object value : parameters.values()) {
				if (value instanceof File || value instanceof ContentBody) {
					bodyType = 1;
					break;
				}
				else if (value.getClass().isArray()) {
					for (int i = 0; i < Array.getLength(value); i++) {
						Object v = Array.get(value, i);
						if (v instanceof File || v instanceof ContentBody) {
							bodyType = 1;
							break;
						}
					}
					if (bodyType == 1) {
						break;
					}
				}
				else if (value instanceof HttpEntity) {
					bodyType = 2;
					break;
				}
			}

			if (bodyType == 1) {
				MultipartEntityBuilder meBuilder = MultipartEntityBuilder
						.create();

				for (Entry<String, Object> entry : parameters.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					if (key == null || value == null) {
						continue;
					}

					if (value instanceof File) {
						File f = (File) value;
						meBuilder.addBinaryBody(key, f,
								ContentType.APPLICATION_OCTET_STREAM, f
										.getName());
					}
					else if (value instanceof ContentBody) {
						meBuilder.addPart(key, (ContentBody) value);
					}
					else if (value.getClass().isArray()) {
						for (int i = 0; i < Array.getLength(value); i++) {
							Object v = Array.get(value, i);

							if (v == null) {
								continue;
							}

							if (v instanceof File) {
								File f = (File) v;
								meBuilder.addBinaryBody(key, f,
										ContentType.APPLICATION_OCTET_STREAM, f
												.getName());
							}
							else if (v instanceof ContentBody) {
								meBuilder.addPart(key, (ContentBody) v);
							}
							else {
								meBuilder.addTextBody(key, v.toString(),
										ContentType.create("text/plain",
												Charset.forName(postCharset)));
							}
						}
					}
					else {
						meBuilder.addTextBody(key, value.toString(),
								ContentType.create("text/plain", Charset
										.forName(postCharset)));
					}
				}

				reqBuilder.setEntity(meBuilder.build());
			}
			else if (bodyType == 2) {
				for (Entry<String, Object> entry : parameters.entrySet()) {
					Object value = entry.getValue();
					if (value == null) {
						continue;
					}
					if (value instanceof HttpEntity) {
						reqBuilder.setEntity((HttpEntity) value);
					}
				}
			}
			else {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>(
						parameters.size());
				for (Entry<String, Object> entry : parameters.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					if (key == null || value == null) {
						continue;
					}

					if (value.getClass().isArray()) {
						for (int i = 0; i < Array.getLength(value); i++) {
							Object v = Array.get(value, i);

							if (v == null) {
								continue;
							}

							nvps.add(new BasicNameValuePair(key, v.toString()));
						}
					}
					else {
						nvps.add(new BasicNameValuePair(key, value.toString()));
					}
				}
				if (StringUtils.isBlank(postCharset)) {
					postCharset = "utf-8";
				}
				try {
					reqBuilder.setEntity(new UrlEncodedFormEntity(nvps,
							postCharset));
				}
				catch (Exception ex) {
				}
			}
		}

		HttpUriRequest post = reqBuilder.build();
		String host = post.getURI().getHost();
		if (usingReferer) {
			if (StringUtils.isBlank(referer)) {
				post.setHeader("Referer", "http://" + host);
			}
			else {
				post.setHeader("Referer", referer);
			}
		}

		return post;
	}

	/**
	 * 
	 * @see net.mtkan.util.TimeWatcher#watch()
	 */
	public void watch(long minFetchInterval) {
		timeWatcher.watch(minFetchInterval);
	}

	/**
	 * 
	 * @see net.mtkan.util.TimeWatcher#watch()
	 */
	public void watch() {
		timeWatcher.watch();
	}

	public void watchForError() {
		if (retry < 30) {
			timeWatcher.watch(++retry * errorWaitTime);
		}
		else {
			++retry;
			timeWatcher.watch(30 * errorWaitTime);
		}
	}

	public boolean isKeepSession() {
		return keepSession;
	}

	public void setKeepSession(boolean keepSession) {
		this.keepSession = keepSession;
	}

	public boolean isUsingReferer() {
		return usingReferer;
	}

	public void setUsingReferer(boolean usingReferer) {
		this.usingReferer = usingReferer;
	}

	public long getMinFetchInterval() {
		return timeWatcher.getTimeLimit();
	}

	public void setMinFetchInterval(long minFetchInterval) {
		timeWatcher.setTimeLimit(minFetchInterval);
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		if (retryCount <= 0) {
			retryCount = 1;
		}
		this.retryCount = retryCount;
	}

	public int getRetry() {
		return retry;
	}

	public boolean isMultiThread() {
		return multiThread;
	}

	public void setMultiThread(boolean multiThread) {
		this.multiThread = multiThread;
	}

	public int getMaxTotalConnections() {
		return maxTotalConnections;
	}

	public void setMaxTotalConnections(int maxTotalConnections) {
		this.maxTotalConnections = maxTotalConnections;
	}

	public int getMaxPerRoute() {
		return maxPerRoute;
	}

	public void setMaxPerRoute(int maxPerRoute) {
		this.maxPerRoute = maxPerRoute;
	}

	public long getErrorWaitTime() {
		return errorWaitTime;
	}

	public void setErrorWaitTime(long errorWaitTime) {
		this.errorWaitTime = errorWaitTime;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public boolean isNoCache() {
		return noCache;
	}

	public void setNoCache(boolean noCache) {
		if (this.noCache != noCache) {
			this.noCache = noCache;
			releaseConnection();
		}
	}

	public boolean isCacheFirst() {
		return cacheFirst;
	}

	public void setCacheFirst(boolean cacheFirst) {
		if (this.cacheFirst != cacheFirst) {
			this.cacheFirst = cacheFirst;
			releaseConnection();
		}
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public Integer getStatusCode() {
		return this.currentStatusCode.get();
	}

	public String getLocation() {
		return this.location.get();
	}

	public String getContentType() {
		return this.contentType.get();
	}

	public Header[] getHeaders() {
		return this.headers.get();
	}

	public boolean isAuthenticationEnabled() {
		return authenticationEnabled;
	}

	public void setAuthenticationEnabled(boolean authenticationEnabled) {
		this.authenticationEnabled = authenticationEnabled;
	}

	public CookieStore getCookieStore() {
		return cookieStore;
	}

	public void setCookieStore(CookieStore cookieStore) {
		this.keepSession = cookieStore != null;
		this.cookieStore = cookieStore;
	}

	public void setContext(HttpClientContext context) {
		this.context.set(context);
	}

	public String getCookieSpecs() {
		return cookieSpecs;
	}

	public void setCookieSpecs(String cookieSpecs) {
		this.cookieSpecs = cookieSpecs;
	}

}
