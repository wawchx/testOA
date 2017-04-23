/*
 * @(#)RandImageServlet.java 2010-8-24
 * 
 * ��Ϣ��˹���ϵͳ
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
 * ���������֤��ͼƬservlet����
 * 
 * @author wangzh
 * 
 * @version 1.0.0
 * 
 * �޸İ汾: 1.0.0
 * �޸�����: 2010-8-24
 * �޸��� :  wangzh
 * �޸�˵��: �ع�����
 * ������ ��
 * </pre>
 */
public class RandImageServlet extends HttpServlet {

	/**
	 * ��־����
	 */
	private static final Log log = LogFactory.getLog(RandImageServlet.class);

	/**
	 * ���ͼ��������
	 */
	private RandomGraphic randomGraphic;

	/**
	 * ����session�е�key
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
			// ���������
			String v = randomGraphic.createNumber();
			HttpSession session = req.getSession();
			// ���ַ�����ֵ������session�У����ں��û��ֹ��������֤��Ƚ�
			session.setAttribute(sessionKey, v);
			// �����������Ϊͼ�񣬸�ʽΪjpeg
			res.setContentType("image/png");
			// �������������Ӧ�ͻ��˶�����������
			OutputStream out = res.getOutputStream();
			randomGraphic.draw(v, RandomGraphic.GRAPHIC_PNG, out);
			out.flush();
		}
		catch (Exception ex) {
			log.warn("������֤��ʧ��", ex);
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
