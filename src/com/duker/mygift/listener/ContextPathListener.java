/*
 * @(#)ContextPathListener.java 2009-11-12
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * <pre>
 * 替换页面文件中的${ctx}为web上下文
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2009-11-12
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class ContextPathListener implements ServletContextListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @seejavax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
	 * ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.ServletContextListener#contextInitialized(javax.servlet
	 * .ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent event) {
		BufferedReader reader = null;
		OutputStreamWriter writer = null;

		try {
			ServletContext sc = event.getServletContext();
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();
			File root = new File(sc.getRealPath("/"));
			File pages = new File(
					new File(classLoader.getResource("/").toURI()),
					"page_index");
			Map<String, Long> pageIndex = new LinkedHashMap<String, Long>();

			if (pages.isFile()) {
				reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(pages), "utf-8"));
				String str;
				while ((str = reader.readLine()) != null) {
					String[] ss = str.split("=");

					if (ss != null && ss.length >= 2) {
						try {
							pageIndex.put(ss[0], Long.parseLong(ss[1]));
						}
						catch (Exception ex) {
						}
					}
				}
			}

			profilePage(root, pageIndex, sc);

			writer = new OutputStreamWriter(new FileOutputStream(pages),
					"utf-8");
			Set<Entry<String, Long>> entrySet = pageIndex.entrySet();

			for (Entry<String, Long> entry : entrySet) {
				writer.write(entry.getKey());
				writer.write("=");
				writer.write(Long.toString(entry.getValue()));
				writer.write(System.getProperty("line.separator"));
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (Exception ex) {
				}
			}
			if (writer != null) {
				try {
					writer.close();
				}
				catch (Exception ex) {
				}
			}
		}
	}

	/**
	 * 分析页面
	 * 
	 * @param file
	 *            页面文件
	 * @param pageIndex
	 *            页面修改时间索引
	 * @param sc
	 *            servlet上下文
	 * @throws Exception
	 */
	private void profilePage(File file, Map<String, Long> pageIndex,
			ServletContext sc) throws Exception {
		if (file.isDirectory()) {
			File[] fs = file.listFiles();
			if (fs != null) {
				for (File f : fs) {
					profilePage(f, pageIndex, sc);
				}
			}
		}
		else if (file.isFile() && file.canRead() && file.canWrite()) {
			String key = file.getCanonicalPath();
			Long value = pageIndex.get(key);
			long lastModified = file.lastModified();

			if (key.endsWith(".css") || key.endsWith(".html")
					|| key.endsWith(".htm") || key.endsWith(".jsp")
					|| key.endsWith(".js")) {
				if (value == null || value < lastModified) {
					FileInputStream in = null;
					FileOutputStream out = null;

					try {
						in = new FileInputStream(file);
						byte[] bs = new byte[in.available()];
						in.read(bs);
						String content = new String(bs, "utf-8");
						content = content.replaceAll("\\$\\{ctx\\}", sc
								.getContextPath());
						out = new FileOutputStream(file);
						out.write(content.getBytes("utf-8"));
					}
					catch (Exception ex) {
					}
					finally {
						if (in != null) {
							try {
								in.close();
							}
							catch (Exception ex) {
							}
						}
						if (out != null) {
							try {
								out.close();
							}
							catch (Exception ex) {
							}
						}
					}

				}
				pageIndex.put(key, file.lastModified());
			}
		}

	}

}
