/*
 * @(#)HttpFetchUtil.java Feb 24, 2011
 * 
 * ��Ϣ��˹���ϵͳ
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
 * �޸İ汾: 0.9
 * �޸�����: Feb 24, 2011
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class HttpFetchUtil {

	/**
	 * ����֤��http proxy
	 */
	public HttpProxy httpProxy;

	/**
	 * ����������
	 */
	public ProxyTask proxyTask;

	/**
	 * �Ƿ�ʹ�������X-Forwarded-For����ͷ
	 */
	public boolean useringXForwardedFor = false;

	/**
	 * �Ƿ�������ͷ��ʹ��Referer
	 */
	private boolean usingReferer = true;

	/**
	 * �Ƿ�ά�ֻỰ
	 */
	private boolean keepSession = true;

	/**
	 * ��Ӧ�Ƿ�û��ͷ
	 */
	private boolean noHeader = false;

	/**
	 * ����ͷ��Я����Referer
	 */
	private String referer = null;

	/**
	 * �Ƿ���302/301��ת
	 */
	public static final String HANDLE_REDIRECTS = "handle-redirects";

	/**
	 * �Ƿ��������
	 */
	public static final String ABORT_REQUEST = "abort-request";

	/**
	 * ƥ��xmlͷ
	 */
	private Pattern xmlPattern = Pattern
			.compile("^<(.+)\\s+xmlns\\s*=\\s*[^>]*>(.*)$");

	/**
	 * ƥ��xml�������
	 */
	private static final Pattern ELEMENT_CONTENT_PATTERN = Pattern
			.compile("<\\w+[^>]*>(.*)</\\w+>");

	/**
	 * ƥ��xml�еĿ����ַ���bom
	 */
	private static final Pattern CONTROL_CHARACTER_PATTERN = Pattern
			.compile("[\\p{Cntrl}&&[^\r\n]]|\\uFEFF");

	/**
	 * cookie�洢��
	 */
	private static final CookieStore PERSISTENT_COOKIE_STORE = new PersistentCookieStore();

	/**
	 * ��ʱ��
	 */
	private TimeWatcher timeWatcher = new TimeWatcher(0);

	/**
	 * ץȡʧ�ܺ����Դ���
	 */
	private int retryCount = 1;

	/**
	 * �Ƿ����ö��߳����ӳ�
	 */
	private boolean multiThread = false;

	/**
	 * ������Ӹ���
	 */
	private int maxTotalConnections = 3000;

	/**
	 * ÿ���������������
	 */
	private int maxPerRoute = 20;

	/**
	 * ʧ�ܺ�ȴ�ʱ��
	 */
	private long errorWaitTime = 2000;

	/**
	 * ���Դ���
	 */
	private int retry = 0;

	/**
	 * �Ƿ�رջ���
	 */
	private boolean noCache = false;

	/**
	 * �Ƿ������ɻ����ļ�
	 */
	private boolean cacheFirst = false;

	/**
	 * ����socket���ݰ�֮�䳬ʱʱ��,��λ����
	 */
	private int soTimeout = 90000;

	/**
	 * ���ӳ�ʱʱ��,��λ����
	 */
	private int connectionTimeout = 120000;

	/**
	 * HttpClient
	 */
	private CloseableHttpClient client;

	/**
	 * cookie�洢λ��
	 */
	private CookieStore cookieStore;

	/**
	 * ����������
	 */
	private HttpCacheInterceptor cacheInterceptor;

	/**
	 * �Ƿ��Զ�����401��¼
	 */
	private boolean authenticationEnabled = true;

	/**
	 * cookie����
	 */
	private String cookieSpecs = CookieSpecs.DEFAULT;

	/**
	 * ��������������
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
	 * ��ǰ�����״̬
	 */
	private ThreadLocal<Integer> currentStatusCode = new ThreadLocal<Integer>();

	/**
	 * ��ǰ�����Content-Type
	 */
	private ThreadLocal<String> contentType = new ThreadLocal<String>();

	/**
	 * 301/302��ת��ĵ�ַ
	 */
	private ThreadLocal<String> location = new ThreadLocal<String>();

	/**
	 * ��Ӧͷ
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
	 * ��ӹ̶�cookie�Ա��סһЩ��֤��Ϣ
	 * 
	 * @param cookie
	 *            ��֤��ص�cookie
	 */
	public void addCookie(Cookie cookie) {
		if (cookieStore != null) {
			cookieStore.addCookie(cookie);
		}
	}

	/**
	 * ��ȡcookie�б�
	 * 
	 * @return cookie�б�
	 */
	public List<Cookie> getCookies() {
		return cookieStore == null ? null : cookieStore.getCookies();
	}

	/**
	 * �������cookie
	 */
	public void clearCookies() {
		if (cookieStore != null) {
			cookieStore.clear();
		}
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @return httpץȡ����ʵ��
	 */
	public static HttpFetchUtil createHttpFetchUtil() {
		return createHttpFetchUtil(0, 5, true, true, false, 30, 2, false);
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @return httpץȡ����ʵ��
	 */
	public static HttpFetchUtil createHttpFetchUtil(boolean multiThread) {
		return createHttpFetchUtil(0, 5, true, true, multiThread, 30, 2, false);
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @param noCache
	 *            �Ƿ�رջ���
	 * @return httpץȡ����ʵ��
	 */
	public static HttpFetchUtil createHttpFetchUtil(boolean multiThread,
			boolean noCache) {
		return createHttpFetchUtil(0, 5, true, true, multiThread, 30, 2,
				noCache);
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @return httpץȡ����ʵ��
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
	 * ����ץȡ����ʵ��
	 * 
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @return httpץȡ����ʵ��
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
	 * ����ץȡ����ʵ��
	 * 
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @param noHeader
	 *            ��Ӧ�Ƿ�û��ͷ
	 * @return httpץȡ����ʵ��
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
	 * ����ץȡ����ʵ��
	 * 
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param soTimeout
	 *            ����socket���ݰ�֮�䳬ʱʱ��,��λ����
	 * @param connectionTimeout
	 *            ���ӳ�ʱʱ��,��λ����
	 * @return httpץȡ����ʵ��
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
	 * ����ץȡ����ʵ��
	 * 
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param soTimeout
	 *            ����socket���ݰ�֮�䳬ʱʱ��,��λ����
	 * @param connectionTimeout
	 *            ���ӳ�ʱʱ��,��λ����
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @return httpץȡ����ʵ��
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
	 * ����ץȡ����ʵ��
	 * 
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param soTimeout
	 *            ����socket���ݰ�֮�䳬ʱʱ��,��λ����
	 * @param connectionTimeout
	 *            ���ӳ�ʱʱ��,��λ����
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @param noCache
	 *            �Ƿ�رջ���
	 * @return httpץȡ����ʵ��
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
	 * ����ץȡ����ʵ��
	 * 
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param soTimeout
	 *            ����socket���ݰ�֮�䳬ʱʱ��,��λ����
	 * @param connectionTimeout
	 *            ���ӳ�ʱʱ��,��λ����
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @param noCache
	 *            �Ƿ�رջ���
	 * @param noHeader
	 *            ��Ӧ�Ƿ�û��ͷ
	 * @return httpץȡ����ʵ��
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
	 * ����ץȡ����ʵ��
	 * 
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @return httpץȡ����ʵ��
	 */
	public static HttpFetchUtil createHttpFetchUtil(int retryCount) {
		return createHttpFetchUtil(0, retryCount, true, true, false, 30, 2,
				false);
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @return httpץȡ����ʵ��
	 */
	public static HttpFetchUtil createHttpFetchUtil(int retryCount,
			boolean multiThread) {
		return createHttpFetchUtil(0, retryCount, true, true, multiThread, 30,
				2, false);
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @param cookieFile
	 *            cookie�洢λ��
	 * @return httpץȡ����ʵ��
	 */
	public static HttpFetchUtil createHttpFetchUtil(int retryCount,
			boolean multiThread, String cookieFile) {
		return createHttpFetchUtil(0, retryCount, true, true, multiThread, 30,
				2, false, new PersistentCookieStore(cookieFile));
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @param cookieStore
	 *            cookie�洢��
	 * @return httpץȡ����ʵ��
	 */
	public static HttpFetchUtil createHttpFetchUtil(int retryCount,
			boolean multiThread, CookieStore cookieStore) {
		return createHttpFetchUtil(0, retryCount, true, true, multiThread, 30,
				2, false, cookieStore);
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @param noCache
	 *            �Ƿ�رջ���
	 * @return httpץȡ����ʵ��
	 */
	public static HttpFetchUtil createHttpFetchUtil(int retryCount,
			boolean multiThread, boolean noCache) {
		return createHttpFetchUtil(0, retryCount, true, true, multiThread, 30,
				2, noCache);
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @param minFetchInterval
	 *            ÿ��ץȡ��Сʱ����
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @return httpץȡ����ʵ��
	 */
	public static HttpFetchUtil createHttpFetchUtil(long minFetchInterval,
			int retryCount) {
		return createHttpFetchUtil(minFetchInterval, retryCount, true, true,
				false, 30, 2, false);
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @param minFetchInterval
	 *            ÿ��ץȡ��Сʱ����
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param noCache
	 *            �Ƿ�رջ���
	 * @return httpץȡ����ʵ��
	 */
	public static HttpFetchUtil createHttpFetchUtil(long minFetchInterval,
			int retryCount, boolean noCache) {
		return createHttpFetchUtil(minFetchInterval, retryCount, true, true,
				false, 30, 2, noCache);
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @param minFetchInterval
	 *            ÿ��ץȡ��Сʱ����
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @param noCache
	 *            �Ƿ�رջ���
	 * @return httpץȡ����ʵ��
	 */
	public static HttpFetchUtil createHttpFetchUtil(long minFetchInterval,
			int retryCount, boolean multiThread, boolean noCache) {
		return createHttpFetchUtil(minFetchInterval, retryCount, true, true,
				multiThread, 30, 2, noCache);
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @param minFetchInterval
	 *            ÿ��ץȡ��Сʱ����
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param usingReferer
	 *            ����ͷ�Ƿ�Я��referer
	 * @param keepSession
	 *            �Ƿ񱣳ֻỰ
	 * @param noCache
	 *            �Ƿ�رջ���
	 * @return httpץȡ����ʵ��
	 */
	public static HttpFetchUtil createHttpFetchUtil(long minFetchInterval,
			int retryCount, boolean usingReferer, boolean keepSession,
			boolean noCache) {
		return createHttpFetchUtil(minFetchInterval, retryCount, usingReferer,
				keepSession, false, 30, 2, noCache);
	}

	/**
	 * ����ץȡ����ʵ��
	 * 
	 * @param minFetchInterval
	 *            ÿ��ץȡ��Сʱ����
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param usingReferer
	 *            ����ͷ�Ƿ�Я��referer
	 * @param keepSession
	 *            �Ƿ񱣳ֻỰ
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @param maxTotalConnections
	 *            ������Ӹ���
	 * @param maxPerRoute
	 *            ÿ���������������
	 * @param noCache
	 *            �Ƿ�رջ���
	 * @return httpץȡ����ʵ��
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
	 * ����ץȡ����ʵ��
	 * 
	 * @param minFetchInterval
	 *            ÿ��ץȡ��Сʱ����
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param usingReferer
	 *            ����ͷ�Ƿ�Я��referer
	 * @param keepSession
	 *            �Ƿ񱣳ֻỰ
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @param maxTotalConnections
	 *            ������Ӹ���
	 * @param maxPerRoute
	 *            ÿ���������������
	 * @param noCache
	 *            �Ƿ�رջ���
	 * @param cookieStore
	 *            cookie�洢��
	 * @return httpץȡ����ʵ��
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
	 * ����ץȡ����ʵ��
	 * 
	 * @param minFetchInterval
	 *            ÿ��ץȡ��Сʱ����
	 * @param retryCount
	 *            ʧ�ܺ����Դ���
	 * @param usingReferer
	 *            ����ͷ�Ƿ�Я��referer
	 * @param keepSession
	 *            �Ƿ񱣳ֻỰ
	 * @param multiThread
	 *            �Ƿ����ö��߳����ӳ�
	 * @param maxTotalConnections
	 *            ������Ӹ���
	 * @param maxPerRoute
	 *            ÿ���������������
	 * @param noCache
	 *            �Ƿ�رջ���
	 * @param noHeader
	 *            ��Ӧ�Ƿ�û��ͷ
	 * @return httpץȡ����ʵ��
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
	 * ��ʼ��HttpClient
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
	 * �ͷ����ӳ�����
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
	 * �ͷ����ӳ�����
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
	 * ʹ��getץȡxml����
	 * 
	 * @param url
	 *            ��ַ
	 * @return ����xml�ļ�
	 */
	public Document getXmlDoc(String url) throws Exception {
		SAXReader reader = new SAXReader();
		return getXmlDoc(url, null, reader);
	}

	/**
	 * ʹ��getץȡxml����
	 * 
	 * @param url
	 *            ��ַ
	 * @param reader
	 *            SAXReader
	 * @return ����xml�ļ�
	 */
	public Document getXmlDoc(String url, SAXReader reader) throws Exception {
		return getXmlDoc(url, null, reader);
	}

	/**
	 * ʹ��get��ʽץȡxml����
	 * 
	 * @param url
	 *            ��ַ
	 * @param headers
	 *            ����ͷ
	 * @return ����xml�ļ�
	 */
	public Document getXmlDoc(String url, Map<String, String> headers)
			throws Exception {
		SAXReader reader = new SAXReader();
		return getXmlDoc(url, headers, reader);
	}

	/**
	 * ʹ��get��ʽץȡxml����
	 * 
	 * @param url
	 *            ��ַ
	 * @param headers
	 *            ����ͷ
	 * @param reader
	 *            SAXReader
	 * @return ����xml�ļ�
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ʹ��postץȡxml����
	 * 
	 * @param url
	 *            ��ַ
	 * @param params
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @return ����xml�ļ�
	 */
	public Document getXmlDoc(String url, Map<String, Object> params,
			String postCharset) throws Exception {
		SAXReader reader = new SAXReader();
		return getXmlDoc(url, params, postCharset, null, reader);
	}

	/**
	 * ʹ��postץȡxml����
	 * 
	 * @param url
	 *            ��ַ
	 * @param params
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param reader
	 *            SAXReader
	 * @return ����xml�ļ�
	 */
	public Document getXmlDoc(String url, Map<String, Object> params,
			String postCharset, SAXReader reader) throws Exception {
		return getXmlDoc(url, params, postCharset, null, reader);
	}

	/**
	 * ʹ��post��ʽץȡxml����
	 * 
	 * @param url
	 *            ��ַ
	 * @param params
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param headers
	 *            ����ͷ
	 * @return ����xml�ļ�
	 */
	public Document getXmlDoc(String url, Map<String, Object> params,
			String postCharset, Map<String, String> headers) throws Exception {
		SAXReader reader = new SAXReader();
		return getXmlDoc(url, params, postCharset, headers, reader);
	}

	/**
	 * ʹ��post��ʽץȡxml����
	 * 
	 * @param url
	 *            ��ַ
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param headers
	 *            ����ͷ
	 * @param reader
	 *            SAXReader
	 * @return ����xml�ļ�
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ������ҳ����
	 * 
	 * @param htmlContent
	 *            ��ҳ����
	 * @return dom��
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
	 * ʹ��get��ʽץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @return dom��
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
	 * ʹ��get��ʽץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param headers
	 *            ����ͷ
	 * @return dom��
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
	 * ʹ��post��ʽץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param params
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @return dom��
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
	 * ʹ��post��ʽץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param params
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param headers
	 *            ����ͷ
	 * @return dom��
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
	 * ����http get����
	 * 
	 * @param url
	 *            url��ַ
	 * @return http��Ӧ
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url) throws Exception {
		return sendHttpRequest(url, true, null);
	}

	/**
	 * ����http get����
	 * 
	 * @param url
	 *            url��ַ
	 * @param headers
	 *            ����ͷ
	 * @return http��Ӧ
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url,
			Map<String, String> headers) throws Exception {
		return sendHttpRequest(url, true, headers);
	}

	/**
	 * ����http get����
	 * 
	 * @param url
	 *            url��ַ
	 * @param handleRedirects
	 *            �Ƿ���301/302��ת
	 * @return http��Ӧ
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url,
			boolean handleRedirects) throws Exception {
		return sendHttpRequest(url, handleRedirects, null);
	}

	/**
	 * ����http get����
	 * 
	 * @param url
	 *            url��ַ
	 * @param handleRedirects
	 *            �Ƿ���301/302��ת
	 * @param headers
	 *            ����ͷ
	 * @return http��Ӧ
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
	 * ����http post����
	 * 
	 * @param url
	 *            url��ַ
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @return http��Ӧ
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url,
			Map<String, Object> parameters, String postCharset)
			throws Exception {
		return sendHttpRequest(url, parameters, postCharset, true, null);
	}

	/**
	 * ����http post����
	 * 
	 * @param url
	 *            url��ַ
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param headers
	 *            ����ͷ
	 * @return http��Ӧ
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url,
			Map<String, Object> parameters, String postCharset,
			Map<String, String> headers) throws Exception {
		return sendHttpRequest(url, parameters, postCharset, true, headers);
	}

	/**
	 * ����http post����
	 * 
	 * @param url
	 *            url��ַ
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param handleRedirects
	 *            �Ƿ���301/302��ת
	 * @return http��Ӧ
	 * @throws Exception
	 */
	public CloseableHttpResponse sendHttpRequest(String url,
			Map<String, Object> parameters, String postCharset,
			boolean handleRedirects) throws Exception {
		return sendHttpRequest(url, parameters, postCharset, handleRedirects,
				null);
	}

	/**
	 * ����http post����
	 * 
	 * @param url
	 *            url��ַ
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param handleRedirects
	 *            �Ƿ���301/302��ת
	 * @param headers
	 *            ����ͷ
	 * @return http��Ӧ
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
	 * ʹ��get��ʽȡ��301��302��ת��ַ
	 * 
	 * @param url
	 *            url��ַ
	 * @return ��ת��ַ
	 * @throws Exception
	 */
	public String getRedirectLocation(String url) throws Exception {
		return getRedirectLocation(url, null);
	}

	/**
	 * ʹ��get��ʽȡ��301��302��ת��ַ
	 * 
	 * @param url
	 *            url��ַ
	 * @param headers
	 *            ����ͷ
	 * @return ��ת��ַ
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
						throw new Exception("����http����ʧ��" + statusLine);
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
	 * ʹ��post��ʽȡ��301��302��ת��ַ
	 * 
	 * @param url
	 *            url��ַ
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @return ��ת��ַ
	 * @throws Exception
	 */
	public String getRedirectLocation(String url,
			Map<String, Object> parameters, String postCharset)
			throws Exception {
		return getRedirectLocation(url, parameters, postCharset, null);
	}

	/**
	 * ʹ��post��ʽȡ��301��302��ת��ַ
	 * 
	 * @param url
	 *            url��ַ
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param headers
	 *            ����ͷ
	 * @return ��ת��ַ
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
						throw new Exception("����http����ʧ��" + statusLine);
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
	 * ʹ��get��ʽץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @return http������
	 * @throws Exception
	 */
	public InputStream getInputStreamFromUrl(String url) throws Exception {
		return getInputStreamFromUrl(url, null);
	}

	/**
	 * ʹ��get��ʽץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param headers
	 *            ����ͷ
	 * @return http������
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ʹ��post��ʽץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @return http������
	 * @throws Exception
	 */
	public InputStream getInputStreamFromUrl(String url,
			Map<String, Object> parameters, String postCharset)
			throws Exception {
		return getInputStreamFromUrl(url, parameters, postCharset, null);
	}

	/**
	 * ʹ��post��ʽץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param headers
	 *            ����ͷ
	 * @return http������
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ʹ��get��ʽץȡ��ҳ���ݻ�ȡReader��
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            http��Ӧ�ַ���
	 * @return http������
	 * @throws Exception
	 */
	public Reader getReaderFromUrl(String url, String defaultResponseCharset)
			throws Exception {
		return getReaderFromUrl(url, defaultResponseCharset, null);
	}

	/**
	 * ʹ��get��ʽץȡ��ҳ���ݻ�ȡReader��
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            http��Ӧ�ַ���
	 * @param headers
	 *            ����ͷ
	 * @return http������
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ʹ��post��ʽץȡ��ҳ���ݻ�ȡReader��
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @return http������
	 * @throws Exception
	 */
	public Reader getReaderFromUrl(String url, String defaultResponseCharset,
			Map<String, Object> parameters, String postCharset)
			throws Exception {
		return getReaderFromUrl(url, defaultResponseCharset, parameters,
				postCharset, null);
	}

	/**
	 * ʹ��post��ʽץȡ��ҳ���ݻ�ȡReader��
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param headers
	 *            ����ͷ
	 * @return http������
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ʹ��get��ʽץȡ��ҳ���ݻ�ȡLineNumberReader��
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            http��Ӧ�ַ���
	 * @return http������
	 * @throws Exception
	 */
	public LineNumberReader getLineReaderFromUrl(String url,
			String defaultResponseCharset) throws Exception {
		return getLineReaderFromUrl(url, defaultResponseCharset, null);
	}

	/**
	 * ʹ��get��ʽץȡ��ҳ���ݻ�ȡLineNumberReader��
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            http��Ӧ�ַ���
	 * @param headers
	 *            ����ͷ
	 * @return http������
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ���contentType�Ƿ�Ϊ�ı�
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
	 * ʹ��post��ʽץȡ��ҳ���ݻ�ȡLineNumberReader��
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @return http������
	 * @throws Exception
	 */
	public LineNumberReader getLineReaderFromUrl(String url,
			String defaultResponseCharset, Map<String, Object> parameters,
			String postCharset) throws Exception {
		return getLineReaderFromUrl(url, defaultResponseCharset, parameters,
				postCharset, null);
	}

	/**
	 * ʹ��post��ʽץȡ��ҳ���ݻ�ȡLineNumberReader��
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param headers
	 *            ����ͷ
	 * @return http������
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ʹ��get��ʽ����ץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            http��Ӧ�ַ���
	 * @return ��ҳ����
	 * @throws Exception
	 */
	public List<String> getLineStringFromUrl(String url,
			String defaultResponseCharset) throws Exception {
		return getLineStringFromUrl(url, defaultResponseCharset, null);
	}

	/**
	 * ʹ��get��ʽ����ץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            http��Ӧ�ַ���
	 * @param headers
	 *            ����ͷ
	 * @return ��ҳ����
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ʹ��post��ʽ����ץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @return ��ҳ����
	 * @throws Exception
	 */
	public List<String> getLineStringFromUrl(String url,
			String defaultResponseCharset, Map<String, Object> parameters,
			String postCharset) throws Exception {
		return getLineStringFromUrl(url, defaultResponseCharset, parameters,
				postCharset, null);
	}

	/**
	 * ʹ��post��ʽ����ץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param headers
	 *            ����ͷ
	 * @return ��ҳ����
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ʹ��get��ʽץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @return html����
	 * @throws Exception
	 */
	public String getStringFromUrl(String url, String defaultResponseCharset)
			throws Exception {
		return getStringFromUrl(url, defaultResponseCharset, null);
	}

	/**
	 * ʹ��get��ʽץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param headers
	 *            ����ͷ
	 * @return html����
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ʹ��post��ʽץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @return html����
	 * @throws Exception
	 */
	public String getStringFromUrl(String url, String defaultResponseCharset,
			Map<String, Object> parameters, String postCharset)
			throws Exception {
		return getStringFromUrl(url, defaultResponseCharset, parameters,
				postCharset, null);
	}

	/**
	 * ʹ��post��ʽץȡ��ҳ����
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param headers
	 *            ����ͷ
	 * @return html����
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ʹ��get��ʽץȡhtml/xml��ҳ����,��ȥ��namespace
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @return ���˺��html����
	 * @throws Exception
	 */
	public String getXmlStringFromUrl(String url, String defaultResponseCharset)
			throws Exception {
		return getXmlStringFromUrl(url, defaultResponseCharset, null);
	}

	/**
	 * ʹ��get��ʽץȡhtml/xml��ҳ����,��ȥ��namespace
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param headers
	 *            ����ͷ
	 * @return ���˺��html����
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ʹ��post��ʽץȡhtml/xml��ҳ����,��ȥ��namespace
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @return ���˺��html����
	 * @throws Exception
	 */
	public String getXmlStringFromUrl(String url,
			String defaultResponseCharset, Map<String, Object> parameters,
			String postCharset) throws Exception {
		return getXmlStringFromUrl(url, defaultResponseCharset, parameters,
				postCharset, null);
	}

	/**
	 * ʹ��post��ʽץȡhtml/xml��ҳ����,��ȥ��namespace
	 * 
	 * @param url
	 *            url��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param headers
	 *            ����ͷ
	 * @return ���˺��html����
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
						throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ʹ�ô���postץȡ��ҳ����
	 * 
	 * @param url
	 *            ץȡ��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param proxyIp
	 *            http����ip
	 * @param proxyPort
	 *            http����˿�
	 * @return http������
	 * @throws Exception
	 */
	public String getStringFromUrl(String url, String defaultResponseCharset,
			Map<String, Object> parameters, String postCharset, String proxyIp,
			int proxyPort) throws Exception {
		return getStringFromUrl(url, defaultResponseCharset, parameters,
				postCharset, proxyIp, proxyPort, null);
	}

	/**
	 * ʹ�ô���postץȡ��ҳ����
	 * 
	 * @param url
	 *            ץȡ��ַ
	 * @param defaultResponseCharset
	 *            httpĬ����Ӧ�ַ���
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param proxyIp
	 *            http����ip
	 * @param proxyPort
	 *            http����˿�
	 * @param headers
	 *            ����ͷ
	 * @return http������
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
				throw new Exception("ץȡ�ļ�ʧ��" + statusLine);
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
	 * ���url�����ָ���
	 * 
	 * @param url
	 *            url��ַ
	 * @return �ָ���
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
	 * ��ȡhttp��ַ������
	 * 
	 * @param url
	 *            http��ַ
	 * @return ����
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
	 * ȡ���Ե�ַ
	 * 
	 * @param currentUrl
	 *            ��ǰurl��ַ
	 * @param nextUrl
	 *            Ҫ������һ��url
	 * @return ����Ҫ���ʵ�url�ľ��Ե�ַ
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
	 * �޸������xml����
	 * 
	 * @param content
	 *            xml����
	 * @return �޸����xml����
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
	 * ���˵������ַ�
	 * 
	 * @param content
	 *            string����
	 * @return ���˺��string����
	 */
	public static String stripControlCharacters(String content) {
		return CONTROL_CHARACTER_PATTERN.matcher(content).replaceAll("");
	}

	/**
	 * ���˵�xml�е������ַ�
	 * 
	 * @param content
	 *            xml����
	 * @return ���˺��xml����
	 */
	public static String stripInvalidXMLCharacters(String content) {
		content = CONTROL_CHARACTER_PATTERN.matcher(content).replaceAll("");
		// ȥ��ifeng�ӿ���xml�����ظ���bug,http://v.ifeng.com/video_info_new/e/e2/653f2c55-2c42-4fd8-94ce-5fff42bc3ee2.xml
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
	 * ����HttpGet����
	 * 
	 * @param url
	 *            url��ַ
	 * @param headers
	 *            ����ͷ
	 * @param handleRedirects
	 *            �Ƿ�֧����ת
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
	 * ����HttpPost����
	 * 
	 * @param url
	 *            url��ַ
	 * @param parameters
	 *            post����
	 * @param postCharset
	 *            post�����ַ���
	 * @param headers
	 *            ����ͷ
	 * @param handleRedirects
	 *            �Ƿ�֧����ת
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
			// 0:��ͨ���� 1:multipart���� 2:�Զ�������
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
