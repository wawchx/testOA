/*
 * @(#)ProxyTask.java Aug 31, 2014
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.util.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

import com.duker.mygift.util.ConfigUtil;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Aug 31, 2014
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@SuppressWarnings("unchecked")
public class ProxyTask implements Serializable {

	public String method = "GET";

	public String checkUrl;

	public Map<String, String> headers;

	public Map<String, Object> parameters;

	public String defaultResponseCharset = "utf-8";

	public String postCharset = "utf-8";

	public String key;

	public boolean usingReferer = false;

	public boolean keepSession = false;

	public boolean noHeader = false;

	public int checkTimeout = 10000;

	private transient ProxyChecker proxyChecker;

	private transient File storeFile;

	private transient File proxyFile;

	private transient File cookieFile;

	private transient boolean initialized = false;

	private transient boolean checking = false;

	private transient Timer timer = new Timer(true);

	public static final Random RANDOM = new Random();

	private static final Log log = LogFactory.getLog(ProxyTask.class);

	/**
	 * 生成代理检测任务
	 */
	public static ProxyTask createProxyTask(String key,
			ProxyChecker proxyChecker) throws Exception {
		key = key.replace(':', '_');
		String proxyPath = ConfigUtil.getProperty("http.proxyFilePath");
		File storeFile = new File(proxyPath, "task_" + key);
		ProxyTask pt = null;

		if (storeFile.exists()) {
			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(new FileInputStream(storeFile));
				pt = (ProxyTask) in.readObject();
			}
			catch (Exception ex) {
			}
			finally {
				if (in != null) {
					try {
						in.close();
					}
					catch (Exception e) {
					}
				}
			}
		}
		if (pt == null) {
			pt = new ProxyTask();
			pt.key = key;
		}
		pt.timer = new Timer(true);
		pt.initialized = false;
		pt.proxyChecker = proxyChecker;
		pt.checking = false;
		pt.cookieFile = new File(ConfigUtil.getProperty("http.cookieFilePath"),
				key);
		pt.storeFile = storeFile;
		pt.proxyFile = new File(proxyPath, "proxy_" + key + ".txt");

		return pt;
	}

	/**
	 * 重新设置key
	 */
	public void setKey(String key) {
		this.key = key;
		this.cookieFile = new File(
				ConfigUtil.getProperty("http.cookieFilePath"), key.replace(':',
						'_'));
	}

	/**
	 * 初始化检测任务
	 */
	public void init() throws Exception {
		if (StringUtils.isBlank(this.checkUrl)) {
			return;
		}
		if (initialized) {
			return;
		}

		final File proxyAllFile = new File(
				ConfigUtil.getProperty("http.proxyFilePath"), "proxy_all.txt");
		initialized = true;
		timer.schedule(new TimerTask() {

			private int checkProxyPeriod = 300 * 1000;

			public void run() {
				if (checking) {
					return;
				}
				checking = true;
				try {
					long now = System.currentTimeMillis();
					if (proxyFile.length() == 0
							|| now - proxyFile.lastModified() >= checkProxyPeriod) {
						log.info("开始检测地址:" + checkUrl);
						Set<HttpProxy> newProxies = new LinkedHashSet<HttpProxy>(
								200);
						if (proxyAllFile.exists()) {
							List<String> lines = FileUtils
									.readLines(proxyAllFile);
							Collections.shuffle(lines);
							for (String line : lines) {
								try {
									String[] ss = line.split(":");
									HttpProxy httpProxy = new HttpProxy(ss[0],
											Integer.parseInt(ss[1]));
									newProxies.add(httpProxy);
									if (newProxies.size() >= 200) {
										break;
									}
								}
								catch (Exception e) {
								}
							}
						}
						log.info("开始检测新获取的" + newProxies.size() + "个代理:"
								+ checkUrl);
						newProxies = checkProxy(newProxies);
						log.info("检测新获取代理完成，有" + newProxies.size() + "个可用代理:"
								+ checkUrl);

						Set<HttpProxy> oldProxies = new LinkedHashSet<HttpProxy>();
						if (proxyFile.exists()) {
							List<String> lines = FileUtils.readLines(proxyFile);
							for (String line : lines) {
								try {
									String[] ss = line.split(":");
									HttpProxy httpProxy = new HttpProxy(ss[0],
											Integer.parseInt(ss[1]));
									if (newProxies.contains(httpProxy)) {
										continue;
									}
									oldProxies.add(httpProxy);
								}
								catch (Exception e) {
								}
							}
						}

						log.info("开始检测" + oldProxies.size() + "个旧代理:"
								+ checkUrl);
						oldProxies = checkProxy(oldProxies);
						log.info("检测旧代理完成，有" + oldProxies.size() + "个旧代理可以使用:"
								+ checkUrl);
						newProxies.addAll(oldProxies);
						FileUtils.writeLines(proxyFile, newProxies);

						log.info("检测地址:" + checkUrl + "完成,有"
								+ newProxies.size() + "个代理可用");
					}
				}
				catch (Exception e) {
					log.error("检测代理失败", e);
				}
				finally {
					checking = false;
				}
			}
		}, 1000, 5000);
	}

	/**
	 * 停止代理检测
	 */
	public void destroy() {
		timer.cancel();
		timer.purge();
		timer = new Timer(true);
		initialized = false;
	}

	/**
	 * 检测代理是否可用
	 */
	private Set<HttpProxy> checkProxy(Set<HttpProxy> proxies) throws Exception {
		Set<HttpProxy> newProxies = new LinkedHashSet<HttpProxy>();
		if (proxies == null || proxies.isEmpty()) {
			return newProxies;
		}
		HttpFetchUtil selectHttpFetchUtil = HttpFetchUtil
				.createFastHttpFetchUtil(1, checkTimeout, checkTimeout, false,
						true, noHeader);
		if (keepSession) {
			selectHttpFetchUtil.setKeepSession(true);
			CookieStore cookieStore = new BasicCookieStore();
			if (cookieFile.exists()) {
				ObjectInputStream oin = null;
				try {
					oin = new ObjectInputStream(new FileInputStream(
							this.cookieFile));
					TreeSet<Cookie> cookies = (TreeSet<Cookie>) oin
							.readObject();
					for (Cookie cookie : cookies) {
						cookieStore.addCookie(cookie);
					}
				}
				catch (Exception e) {
				}
				finally {
					if (oin != null) {
						try {
							oin.close();
						}
						catch (Exception e) {
						}
					}
				}
			}
			selectHttpFetchUtil.setCookieStore(cookieStore);
		}
		else {
			selectHttpFetchUtil.setKeepSession(false);
		}
		selectHttpFetchUtil.setUsingReferer(usingReferer);

		try {
			for (HttpProxy proxy : proxies) {
				Socket socket = new Socket();
				try {
					InetSocketAddress addr = new InetSocketAddress(proxy.ip,
							proxy.port);
					socket.connect(addr, 5000);
				}
				catch (Exception e) {
					// 连接失败，代理不可用
					ProxyManager.addBadProxy(proxy);
					continue;
				}
				finally {
					try {
						socket.close();
					}
					catch (Exception e) {
					}
				}

				selectHttpFetchUtil.httpProxy = proxy;
				long start = System.currentTimeMillis();
				try {
					// log.info("开始检测代理" + proxy.ip + ":" + proxy.port);
					String content;
					if ("GET".equals(method)) {
						content = selectHttpFetchUtil.getStringFromUrl(
								checkUrl, defaultResponseCharset, headers);
					}
					else {
						content = selectHttpFetchUtil.getStringFromUrl(
								checkUrl, defaultResponseCharset, parameters,
								postCharset, headers);
					}
					Integer status = selectHttpFetchUtil.getStatusCode();
					if (!proxyChecker.check(status, content)) {
						// log.info("检测代理" + proxy.ip + ":" + proxy.port +
						// "未通过");
						continue;
					}

					long time = System.currentTimeMillis() - start;
					StringBuilder strLog = new StringBuilder();
					strLog.append("使用");
					strLog.append(proxy.ip);
					strLog.append(":");
					strLog.append(proxy.port);
					strLog.append("访问");
					strLog.append(checkUrl);
					strLog.append("用时");
					strLog.append(time);
					strLog.append("毫秒");

					log.info(strLog);

					newProxies.add(proxy);
				}
				catch (Exception ex) {
					// log.info("代理" + proxy.ip + ":" + proxy.port + "无法访问");
					continue;
				}
			}
		}
		finally {
			selectHttpFetchUtil.close();
		}

		return newProxies;
	}

	/**
	 * 添加检测参数
	 */
	public void addCheckParameters(String method, String checkUrl,
			Map<String, String> headers, Map<String, Object> parameters,
			String defaultResponseCharset, String postCharset) throws Exception {
		this.method = method;
		this.checkUrl = checkUrl;
		this.headers = headers;
		this.parameters = parameters;
		this.defaultResponseCharset = defaultResponseCharset;
		this.postCharset = postCharset;
		this.init();
		this.storeTask();
	}

	/**
	 * 保存任务
	 */
	private void storeTask() {
		ObjectOutputStream out = null;

		try {
			out = new ObjectOutputStream(new FileOutputStream(storeFile));
			out.writeObject(this);
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
	 * 随机获取一个有用代理
	 */
	public HttpProxy selectRandomProxy() {
		try {
			List<String> lines = FileUtils.readLines(proxyFile);
			if (lines.isEmpty()) {
				return null;
			}
			double d = RANDOM.nextDouble();
			int i = (int) (d * (lines.size() - 1));
			String line = lines.get(i);

			String[] ss = line.split(":");
			HttpProxy httpProxy = new HttpProxy(ss[0], Integer.parseInt(ss[1]));

			return httpProxy;
		}
		catch (Exception e) {
			return null;
		}
	}

	public static String randomXForwardedFor() {
		StringBuilder sb = new StringBuilder();
		double d = RANDOM.nextFloat();
		int i = (int) ((255 - 192) * d + 192);
		sb.append(i);
		sb.append('.');

		d = RANDOM.nextFloat();
		i = (int) (255 * d);
		sb.append(i);
		sb.append('.');

		d = RANDOM.nextFloat();
		i = (int) (255 * d);
		sb.append(i);
		sb.append('.');

		d = RANDOM.nextFloat();
		i = (int) (255 * d);
		sb.append(i);

		return sb.toString();
	}
}
