/*
 * @(#)ProxyManager.java Mar 2, 2014
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.util.http;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ognl.Ognl;
import ognl.OgnlContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import com.duker.mygift.constant.CList;
import com.duker.mygift.util.ConfigUtil;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: Mar 2, 2014
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
@SuppressWarnings("unchecked")
public class ProxyManager {

	private boolean checking = false;

	private Timer timer = new Timer(true);

	private static final Log log = LogFactory.getLog(ProxyManager.class);

	public static void main(String[] args) throws Exception {
		new ProxyManager().loadProxies();
	}

	/**
	 * ���������������
	 */
	public void init() throws Exception {
		String proxyPath = ConfigUtil.getProperty("http.proxyFilePath");
		final File proxyFile = new File(proxyPath, "proxy_all.txt");
		final File badProxyFile = new File(proxyPath, "proxy_bad.txt");

		timer.schedule(new TimerTask() {

			private int checkProxyPeriod = 3600 * 1000;

			public void run() {
				if (checking) {
					return;
				}
				checking = true;
				try {
					long now = System.currentTimeMillis();
					if (now - proxyFile.lastModified() >= checkProxyPeriod) {
						log.info("��ʼ��ȡ���д���");
						Set<HttpProxy> proxies = loadProxies();
						Set<HttpProxy> badProxies;
						if (badProxyFile.exists()) {
							List<String> lines = FileUtils
									.readLines(badProxyFile);
							badProxies = new HashSet<HttpProxy>(lines.size());
							for (String line : lines) {
								try {
									String[] ss = line.split(":");
									HttpProxy httpProxy = new HttpProxy(ss[0],
											Integer.parseInt(ss[1]));
									badProxies.add(httpProxy);
								}
								catch (Exception e) {
								}
							}
						}
						else {
							badProxies = new HashSet<HttpProxy>(0);
						}
						proxies.removeAll(badProxies);
						FileUtils.writeLines(proxyFile, proxies);
						log.info("��ȡ��" + proxies.size() + "������");
					}
				}
				catch (Exception e) {
					log.error("������ʧ��", e);
				}
				finally {
					checking = false;
				}
			}
		}, 1000, 5000);

	}

	/**
	 * ֹͣ�������
	 */
	public void destroy() {
		timer.cancel();
		timer.purge();
		timer = new Timer(true);
	}

	/**
	 * ���治���ô���
	 * 
	 * @param proxy
	 *            �����ô���
	 */
	public static void addBadProxy(HttpProxy proxy) throws Exception {
		String proxyPath = ConfigUtil.getProperty("http.proxyFilePath");
		File badProxyFile = new File(proxyPath, "proxy_bad.txt");
		List<String> lines;
		synchronized (ProxyManager.class) {
			if (badProxyFile.exists()) {
				lines = FileUtils.readLines(badProxyFile);
			}
			else {
				lines = new ArrayList<String>(1);
			}

			Set<String> badProxies = new LinkedHashSet<String>(lines);
			String p = proxy.toString();
			if (badProxies.contains(p)) {
				return;
			}

			lines.add(p);
			FileUtils.writeLines(badProxyFile, lines);
		}
	}

	/**
	 * �������д���
	 */
	protected Set<HttpProxy> loadProxies() throws Exception {
		Set<HttpProxy> newProxies = new LinkedHashSet<HttpProxy>(200);
		List<HttpProxy> proxied1 = load56adsProxies();
		List<HttpProxy> proxied2 = loadXiciProxies();
		List<HttpProxy> proxied3 = loadPachongProxies();
		newProxies.addAll(proxied1);
		newProxies.addAll(proxied2);
		newProxies.addAll(proxied3);

		return newProxies;
	}

	/**
	 * ��www.56ads.cn��ȡ�����б�
	 */
	protected List<HttpProxy> load56adsProxies() throws Exception {
		log.info("��ʼ��www.56ads.cn��ȡ�����б�");
		String url = "http://www.56ads.com/proxyip/";
		HttpFetchUtil httpFetchUtil = HttpFetchUtil.createFastHttpFetchUtil(1,
				50000, 50000, false, true);
		ProxyTask proxyTask = ProxyTask.createProxyTask("56ads",
				new ProxyChecker() {

					public boolean check(Integer status, String content) {
						if (StringUtils.isBlank(content)
								|| (!content.contains("listbox"))) {
							return false;
						}

						return true;
					}
				});
		proxyTask.checkTimeout = 30000;
		proxyTask.addCheckParameters("GET", url, null, null, "gbk", "gbk");
		proxyTask.init();
		httpFetchUtil.proxyTask = proxyTask;
		List<HttpProxy> proxies = new LinkedList<HttpProxy>();
		try {
			String listPath = "//DIV[@class='listbox']/UL/LI/A[@href]";
			Document doc = httpFetchUtil.getHtmlDoc(url, "gbk");
			List<Element> es = doc.selectNodes(listPath);
			String listUrl = null;
			for (Element e : es) {
				listUrl = e.attributeValue("href");
				if (StringUtils.isNotBlank(listUrl)) {
					break;
				}
			}
			if (StringUtils.isBlank(listUrl)) {
				log.error("û�д�" + url + "ƥ�䵽�����б��ַ��");
				return proxies;
			}

			url = listUrl;
			String ipListPath1 = "//DIV[@class='viewbox']/DIV[@class='content']/P";
			String ipListPath2 = "//DIV[@class='viewbox']/DIV[@class='content']";
			String nextPagePath = "//DIV[@class='dede_pages']/UL/LI/A[contains(text(), '��һҳ') and @href!='#']";
			do {
				doc = httpFetchUtil.getHtmlDoc(url, "gbk");
				String content = doc.valueOf(ipListPath1);
				if (StringUtils.isBlank(content)) {
					content = doc.valueOf(ipListPath2);
				}
				if (StringUtils.isBlank(content)) {
					log.error("û�д�" + url + "ƥ�䵽�����б��������ƥ���߼���"
							+ CList.LINE_SEPARATOR + doc.asXML());
					break;
				}
				content = content.trim();
				String[] ss = content.split("[\r\n]+");
				for (String s : ss) {
					s = s.trim();
					int idx = s.indexOf('@');
					if (idx == -1) {
						continue;
					}
					s = s.substring(0, idx);
					String[] ipPort = s.split(":");
					if (ipPort.length < 2 || !StringUtils.isNumeric(ipPort[1])) {
						continue;
					}
					proxies.add(new HttpProxy(ipPort[0], Integer
							.parseInt(ipPort[1])));
				}
				Element nextPageE = (Element) doc
						.selectSingleNode(nextPagePath);
				if (nextPageE == null) {
					break;
				}
				url = HttpFetchUtil.findAbsoluteUrl(url,
						nextPageE.attributeValue("href"));
			} while (true);

			log.info("��www.56ads.com��ȡ��" + proxies.size() + "������");
		}
		catch (Exception ex) {
			StringBuilder strLog = new StringBuilder();
			strLog.append("��");
			strLog.append(url);
			strLog.append("ѡ��http�����쳣");
			log.error(strLog, ex);
		}
		finally {
			httpFetchUtil.close();
		}

		return proxies;
	}

	/**
	 * ��www.xici.net.co��ȡ�����б�
	 */
	protected List<HttpProxy> loadXiciProxies() throws Exception {
		log.info("��ʼ��www.xici.net.co��ȡ�����б�");
		String url = "http://www.xici.net.co/nn";
		HttpFetchUtil httpFetchUtil = HttpFetchUtil.createFastHttpFetchUtil(1,
				50000, 50000, false, true);
		ProxyTask proxyTask = ProxyTask.createProxyTask("xici",
				new ProxyChecker() {

					public boolean check(Integer status, String content) {
						if (StringUtils.isBlank(content)
								|| (!content.contains("ip_list"))) {
							return false;
						}

						return true;
					}
				});
		proxyTask.checkTimeout = 30000;
		proxyTask.addCheckParameters("GET", url, null, null, "utf-8", "utf-8");
		proxyTask.init();
		httpFetchUtil.proxyTask = proxyTask;
		List<HttpProxy> proxies = new LinkedList<HttpProxy>();
		try {
			String ipListPath = "//TABLE[@id='ip_list']/TBODY/TR[contains(TD[6]/text(), 'HTTP')]";
			String nextPagePath = "//DIV[@class='pagination']/A[contains(text(), '��һҳ')]";
			String ipPath = "TD[2]";
			String portPath = "TD[3]";
			int i = 0;
			do {
				Document doc = httpFetchUtil.getHtmlDoc(url, "utf-8");
				List<Element> es = doc.selectNodes(ipListPath);
				if (es.isEmpty()) {
					log.error("û�д�" + url + "ƥ�䵽�����б��������ƥ���߼���");
					return proxies;
				}
				for (Element e : es) {
					String ip = e.valueOf(ipPath);
					String port = e.valueOf(portPath);
					if (StringUtils.isBlank(ip) || !StringUtils.isNumeric(port)) {
						continue;
					}
					proxies.add(new HttpProxy(ip, Integer.parseInt(port)));
				}

				Element nextPageE = (Element) doc
						.selectSingleNode(nextPagePath);
				if (nextPageE == null) {
					break;
				}
				if (i++ >= 10) {
					break;
				}
				url = HttpFetchUtil.findAbsoluteUrl(url,
						nextPageE.attributeValue("href"));
			} while (true);

			if (proxies.isEmpty()) {
				log.error("��" + url + "ƥ�䵽�����б�Ϊ�գ��������ƥ���߼���");
			}
			else {
				log.info("��www.xici.net.co��ȡ��" + proxies.size() + "������");
			}
		}
		catch (Exception ex) {
			StringBuilder strLog = new StringBuilder();
			strLog.append("��");
			strLog.append(url);
			strLog.append("ѡ��http�����쳣");
			log.error(strLog, ex);
		}
		finally {
			httpFetchUtil.close();
		}

		return proxies;
	}

	/**
	 * ��pachong.org��ȡ�����б�
	 */
	protected List<HttpProxy> loadPachongProxies() throws Exception {
		log.info("��ʼ��pachong.org��ȡ�����б�");
		String url = "http://pachong.org/area/short/name/cn.html";
		HttpFetchUtil httpFetchUtil = HttpFetchUtil.createFastHttpFetchUtil(1,
				50000, 50000, false, true);
		ProxyTask proxyTask = ProxyTask.createProxyTask("pachong",
				new ProxyChecker() {

					public boolean check(Integer status, String content) {
						if (StringUtils.isBlank(content)
								|| (!content.contains("class=\"tb\""))) {
							return false;
						}

						return true;
					}
				});
		proxyTask.checkTimeout = 30000;
		proxyTask.addCheckParameters("GET", url, null, null, "utf-8", "utf-8");
		proxyTask.init();
		httpFetchUtil.proxyTask = proxyTask;
		List<HttpProxy> proxies = new LinkedList<HttpProxy>();
		try {
			String ipPortListPath = "//TABLE[@class='tb']/TBODY/TR[contains(TD/A/text(), '�й�')]";
			String ipPath = "TD[2]";
			String portPath = "TD[3]";
			Document doc = httpFetchUtil.getHtmlDoc(url, "utf-8");
			List<Element> es = doc.selectNodes(ipPortListPath);
			if (es.isEmpty()) {
				log.error("û�д�" + url + "ƥ�䵽�����б��������ƥ���߼���");
				return proxies;
			}
			Object expr = null;
			OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);
			Map<String, Long> root = new HashMap<String, Long>();
			String s = doc
					.valueOf("//HEAD/SCRIPT[contains(text(), 'var')]/text()");
			if (StringUtils.isNotBlank(s)) {
				try {
					s = s.replaceAll("var\\s+", "");
					s = s.replace(';', ',');
					if (s.endsWith(",")) {
						s = s.substring(0, s.length() - 1);
					}
					Ognl.setRoot(context, root);
					expr = Ognl.parseExpression(s);
					Ognl.getValue(expr, context, root);
				}
				catch (Exception e1) {
				}
			}
			Pattern p = Pattern
					.compile("document\\.write\\s*\\(\\s*(.+)\\s*\\)\\s*");
			for (Element e : es) {
				String ip = e.valueOf(ipPath);
				String port = e.valueOf(portPath);
				if (StringUtils.isBlank(ip) || StringUtils.isBlank(port)) {
					continue;
				}
				Matcher m = p.matcher(port);
				if (m.find()) {
					try {
						port = m.group(1);
						Object newExpr = Ognl.parseExpression(port);
						Integer o = (Integer) Ognl.getValue(newExpr, context,
								root);
						proxies.add(new HttpProxy(ip, o));
					}
					catch (Exception e1) {
					}
				}
				else if (!StringUtils.isNumeric(port)) {
					continue;
				}
				else {
					proxies.add(new HttpProxy(ip, Integer.parseInt(port)));
				}
			}

			if (proxies.isEmpty()) {
				log.error("��" + url + "ƥ�䵽�����б�Ϊ�գ��������ƥ���߼���");
			}
			else {
				log.info("��pachong.org��ȡ��" + proxies.size() + "������");
			}
		}
		catch (Exception ex) {
			StringBuilder strLog = new StringBuilder();
			strLog.append("��");
			strLog.append(url);
			strLog.append("ѡ��http�����쳣");
			log.error(strLog, ex);
		}
		finally {
			httpFetchUtil.close();
		}

		return proxies;
	}
}
