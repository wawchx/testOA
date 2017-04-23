/*
 * @(#)RandImageServlet.java 2010-8-24
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.duker.mygift.common.util.RandomGraphic;

/**
 * <pre>
 * 随机生成验证码图片servlet请求
 * 
 * @author wangzh
 * 
 * @version 1.0.0
 * 
 * 修改版本: 1.0.0
 * 修改日期: 2010-8-24
 * 修改人 :  wangzh
 * 修改说明: 重构代码
 * 复审人 ：
 * </pre>
 */
public class RandImageServlet extends HttpServlet {

	/**
	 * 日志对象
	 */
	private static final Log log = LogFactory.getLog(RandImageServlet.class);

	/**
	 * 随机图像生成器
	 */
	private RandomGraphic randomGraphic;

	/**
	 * 放入session中的key
	 */
	private String sessionKey = "rv";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		try {
			// 生成随机串
			String v = randomGraphic.createNumber();
			HttpSession session = req.getSession();
			// 将字符串的值保留在session中，便于和用户手工输入的验证码比较
			session.setAttribute(sessionKey, v);
			// 设置输出内容为图像，格式为jpeg
			res.setContentType("image/png");
			// 将内容输出到响应客户端对象的输出流中
			OutputStream out = res.getOutputStream();
			randomGraphic.draw(v, RandomGraphic.GRAPHIC_PNG, out);
			out.flush();
		}
		catch (Exception ex) {
			log.warn("生成验证码失败", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException {
		String charCountStr = getInitParameter("charCount");
		String lineCountStr = getInitParameter("lineCount");
		String widthStr = getInitParameter("width");
		String heightStr = getInitParameter("height");
		String fontSizeStr = getInitParameter("fontSize");
		String fontName = getInitParameter("fontName");
		String backgroundIndexStr = getInitParameter("backgroundIndex");
		String offsetXStr = getInitParameter("offsetX");
		String offsetYStr = getInitParameter("offsetY");
		sessionKey = getInitParameter("sessionKey");

		int charCount = 4;
		int lineCount = 0;
		int width = 90;
		int height = 30;
		int fontSize = 22;
		int backgroundIndex = 0;
		int offsetX = 5;
		int offsetY = 5;

		if (StringUtils.isNotBlank(charCountStr)) {
			charCount = Integer.parseInt(charCountStr.trim());
		}
		if (StringUtils.isNotBlank(lineCountStr)) {
			lineCount = Integer.parseInt(lineCountStr.trim());
		}
		if (StringUtils.isNotBlank(widthStr)) {
			width = Integer.parseInt(widthStr.trim());
		}
		if (StringUtils.isNotBlank(heightStr)) {
			height = Integer.parseInt(heightStr.trim());
		}
		if (StringUtils.isNotBlank(fontSizeStr)) {
			fontSize = Integer.parseInt(fontSizeStr.trim());
		}
		if (StringUtils.isBlank(fontName)) {
			fontName = "Verdana";
		}
		if (StringUtils.isNotBlank(backgroundIndexStr)) {
			backgroundIndex = Integer.parseInt(backgroundIndexStr.trim());
		}
		if (StringUtils.isNotBlank(offsetXStr)) {
			offsetX = Integer.parseInt(offsetXStr.trim());
		}
		if (StringUtils.isNotBlank(offsetYStr)) {
			offsetY = Integer.parseInt(offsetYStr.trim());
		}
		if (StringUtils.isBlank(sessionKey)) {
			sessionKey = "rv";
		}
		randomGraphic = RandomGraphic.createInstance(charCount, lineCount,
				width, height, fontSize, fontName, backgroundIndex, offsetX,
				offsetY);
	}

}
