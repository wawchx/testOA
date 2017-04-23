/*
 * @(#)RandomGraphic.java 2010-8-24
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.common.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * <pre>
 *  ����������ֻ���ĸ������ͼ��ʽ��ʾ�������˹�ʶ��ʹ�������ʶ�� ��Сϵͳ�������Զ������Ŀ����ԡ�
 *  ���ɵ��ַ�ͼ����ɫ����ɫ����ɣ����ֻ���ĸ��ֱ����λ����һ����Χ��Ҳ������ģ����ٱ������Զ�ʶ��ļ��ʡ�
 *  �����ߵ���ɫ�ɺ졢�ȡ��ơ��̡������塢�������϶��ɡ�
 *  �������ֵ�0��1��2�׺���ĸ��o��l,z������ʹ��������ʶ����˲������������ֺ���ĸ�Ļ�ϴ���
 *  ���ɵĴ���ĸͳһ��Сд��������󳤶�Ϊ16��
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
public class RandomGraphic {

	/**
	 * Ҫ���ɵ��ַ��������ɹ��������õ�
	 */
	private int charCount = 0;

	/**
	 * ����������
	 */
	private int lineCount = 10;

	/**
	 * ͼƬ��ȣ���λΪ����
	 */
	private int width = 90;

	/**
	 * ͼƬ�߶ȣ���λΪ����
	 */
	private int height = 30;

	/**
	 * �����С����λpt
	 */
	private int fontSize = 22;

	/**
	 * ����
	 */
	private String fontName = "Verdana";

	/**
	 * ������ɫ����
	 */
	private int backgroundIndex = 0;

	/**
	 * ˮƽ������ʼλ��
	 */
	private int offsetX = 5;

	/**
	 * ��ֱ������ʼλ��
	 */
	private int offsetY = 5;

	/**
	 * ����ַ�������
	 */
	private static final int MAX_CHAR_COUNT = 16;

	/**
	 * �������߸���
	 */
	private static final int MAX_LINE_COUNT = 100;

	/**
	 * ��ɫ���飬������ɫʹ��
	 */
	private static final Color[] BACKGROUND_COLORS = { Color.WHITE,
			new Color(253, 253, 197) };

	/**
	 * ��ɫ���飬�����ִ�ʱ���ѡ��һ��
	 */
	private static final Color[] CHAR_COLORS = { Color.BLUE,
			new Color(0, 0, 139), new Color(0, 139, 139), Color.GREEN,
			new Color(0, 100, 0), Color.RED, new Color(139, 0, 0),
			new Color(235, 37, 86), new Color(64, 115, 220),
			new Color(34, 128, 8), new Color(105, 59, 9) };

	/**
	 * ��ɫ���飬���Ƹ�����ʱ���ѡ��һ��
	 */
	private static final Color[] LINE_COLORS = { Color.RED, Color.ORANGE,
			Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA };

	/**
	 * ����ͼ��ĸ�ʽ������JPEG��ʽ,����Ϊ�ļ�ʱ��չ��Ϊ.jpg�� �����ҳ��ʱ��Ҫ����MIME type Ϊimage/jpeg
	 */
	public final static String GRAPHIC_JPEG = "JPEG";

	/**
	 * ����ͼ��ĸ�ʽ������PNG��ʽ,����Ϊ�ļ�ʱ��չ��Ϊ.png�� �����ҳ��ʱ��Ҫ����MIME type Ϊimage/png
	 */
	public final static String GRAPHIC_PNG = "PNG";

	protected RandomGraphic() {
	}

	/**
	 * �ù���������������
	 * 
	 * @param charCount
	 *            �ַ�����
	 */
	protected RandomGraphic(int charCount) {
		if (charCount > 0 && charCount <= MAX_CHAR_COUNT) {
			this.charCount = charCount;
		}
	}

	/**
	 * �ù���������������
	 * 
	 * @param charCount
	 *            �ַ�����
	 * @param lineCount
	 *            ����������
	 */
	protected RandomGraphic(int charCount, int lineCount) {
		if (charCount > 0 && charCount <= MAX_CHAR_COUNT) {
			this.charCount = charCount;
		}
		if (lineCount > 0 && lineCount <= MAX_LINE_COUNT) {
			this.lineCount = lineCount;
		}
	}

	/**
	 * �ù���������������
	 * 
	 * @param charCount
	 *            �ַ�����
	 * @param lineCount
	 *            ����������
	 * @param width
	 *            ͼƬ���
	 * @param height
	 *            ͼƬ�߶�
	 * @param fontSize
	 *            �����С
	 */
	protected RandomGraphic(int charCount, int lineCount, int width,
			int height, int fontSize) {
		if (charCount > 0 && charCount <= MAX_CHAR_COUNT) {
			this.charCount = charCount;
		}
		if (lineCount > 0 && lineCount <= MAX_LINE_COUNT) {
			this.lineCount = lineCount;
		}
		if (width > 0) {
			this.width = width;
		}
		if (height > 0) {
			this.height = height;
		}
		if (fontSize > 0) {
			this.fontSize = fontSize;
		}
	}

	/**
	 * �ù���������������
	 * 
	 * @param charCount
	 *            �ַ�����
	 * @param lineCount
	 *            ����������
	 * @param width
	 *            ͼƬ���
	 * @param height
	 *            ͼƬ�߶�
	 * @param fontSize
	 *            �����С
	 * @param fontName
	 *            ������
	 */
	protected RandomGraphic(int charCount, int lineCount, int width,
			int height, int fontSize, String fontName) {
		if (charCount > 0 && charCount <= MAX_CHAR_COUNT) {
			this.charCount = charCount;
		}
		if (lineCount > 0 && lineCount <= MAX_LINE_COUNT) {
			this.lineCount = lineCount;
		}
		if (width > 0) {
			this.width = width;
		}
		if (height > 0) {
			this.height = height;
		}
		if (fontSize > 0) {
			this.fontSize = fontSize;
		}
		if (StringUtils.isNotBlank(fontName)) {
			this.fontName = fontName;
		}
	}

	/**
	 * �ù���������������
	 * 
	 * @param charCount
	 *            �ַ�����
	 * @param lineCount
	 *            ����������
	 * @param width
	 *            ͼƬ���
	 * @param height
	 *            ͼƬ�߶�
	 * @param fontSize
	 *            �����С
	 * @param fontName
	 *            ������
	 * @param backgroundIndex
	 *            ������ɫ����
	 * @param offsetX
	 *            ˮƽ������ʼλ��
	 * @param offsetY
	 *            ��ֱ������ʼλ��
	 */
	protected RandomGraphic(int charCount, int lineCount, int width,
			int height, int fontSize, String fontName, int backgroundIndex,
			int offsetX, int offsetY) {
		if (charCount > 0 && charCount <= MAX_CHAR_COUNT) {
			this.charCount = charCount;
		}
		if (lineCount > 0 && lineCount <= MAX_LINE_COUNT) {
			this.lineCount = lineCount;
		}
		if (width > 0) {
			this.width = width;
		}
		if (height > 0) {
			this.height = height;
		}
		if (fontSize > 0) {
			this.fontSize = fontSize;
		}
		if (StringUtils.isNotBlank(fontName)) {
			this.fontName = fontName;
		}
		this.backgroundIndex = backgroundIndex;
		if (offsetX > 0) {
			this.offsetX = offsetX;
		}
		if (offsetY > 0) {
			this.offsetY = offsetY;
		}
	}

	/**
	 * ��������Ĺ�������
	 * 
	 * @param charCount
	 *            Ҫ���ɵ��ַ�������������1��16֮��
	 * @return ����RandomGraphic����ʵ��
	 */
	public static RandomGraphic createInstance(int charCount) {
		return new RandomGraphic(charCount);
	}

	/**
	 * ��������Ĺ�������
	 * 
	 * @param charCount
	 *            Ҫ���ɵ��ַ�������������1��16֮��
	 * @param lineCount
	 *            Ҫ���ɵĸ���������
	 * @return ����RandomGraphic����ʵ��
	 */
	public static RandomGraphic createInstance(int charCount, int lineCount) {
		return new RandomGraphic(charCount, lineCount);
	}

	/**
	 * ��������Ĺ�������
	 * 
	 * @param charCount
	 *            Ҫ���ɵ��ַ�������������1��16֮��
	 * @param lineCount
	 *            Ҫ���ɵĸ���������
	 * @param width
	 *            ͼƬ���
	 * @param height
	 *            ͼƬ�߶�
	 * @param fontSize
	 *            �����С
	 * @return ����RandomGraphic����ʵ��
	 */
	public static RandomGraphic createInstance(int charCount, int lineCount,
			int width, int height, int fontSize) {
		return new RandomGraphic(charCount, lineCount, width, height, fontSize);
	}

	/**
	 * ��������Ĺ�������
	 * 
	 * @param charCount
	 *            Ҫ���ɵ��ַ�������������1��16֮��
	 * @param lineCount
	 *            Ҫ���ɵĸ���������
	 * @param width
	 *            ͼƬ���
	 * @param height
	 *            ͼƬ�߶�
	 * @param fontSize
	 *            �����С
	 * @param fontName
	 *            ������
	 * @return ����RandomGraphic����ʵ��
	 */
	public static RandomGraphic createInstance(int charCount, int lineCount,
			int width, int height, int fontSize, String fontName) {
		return new RandomGraphic(charCount, lineCount, width, height, fontSize,
				fontName);
	}

	/**
	 * ��������Ĺ�������
	 * 
	 * @param charCount
	 *            Ҫ���ɵ��ַ�������������1��16֮��
	 * @param lineCount
	 *            Ҫ���ɵĸ���������
	 * @param width
	 *            ͼƬ���
	 * @param height
	 *            ͼƬ�߶�
	 * @param fontSize
	 *            �����С
	 * @param fontName
	 *            ������
	 * @param backgroundIndex
	 *            ������ɫ����
	 * @param offsetX
	 *            ˮƽ������ʼλ��
	 * @param offsetY
	 *            ��ֱ������ʼλ��
	 * @return ����RandomGraphic����ʵ��
	 */
	public static RandomGraphic createInstance(int charCount, int lineCount,
			int width, int height, int fontSize, String fontName,
			int backgroundIndex, int offsetX, int offsetY) {
		return new RandomGraphic(charCount, lineCount, width, height, fontSize,
				fontName, backgroundIndex, offsetX, offsetY);
	}

	/**
	 * �������������
	 * 
	 * @return �������ֵ
	 */
	public String createNumber() {
		// ������ɵĴ���ֵ
		String charValue = RandomStringUtils.randomNumeric(charCount);

		return charValue;
	}

	/**
	 * ������ĸ�����
	 * 
	 * @return �������ֵ
	 */
	public String createAlpha() {
		// ������ɵĴ���ֵ
		String charValue = RandomStringUtils.randomAlphabetic(charCount)
				.toLowerCase();

		return charValue;
	}

	/**
	 * ������ĸ�����������
	 * 
	 * @return �������ֵ
	 */
	public String createAlphaNumber() {
		// ������ɵĴ���ֵ
		String charValue = RandomStringUtils.randomAlphanumeric(charCount)
				.toLowerCase();

		return charValue;
	}

	/**
	 * ��ͼ��ʽ�����ַ��������ƽ���������out��
	 * 
	 * @param charValue
	 *            Ҫ���Ƶ��ַ���
	 * @param graphicFormat
	 *            �������ɵ�ͼ���ʽ��ֵΪGRAPHIC_JPEG��GRAPHIC_PNG
	 * @param out
	 *            ͼ���������
	 * @return ������ɵĴ���ֵ
	 * @throws IOException
	 */
	public String draw(String charValue, String graphicFormat, OutputStream out)
			throws IOException {
		// �����ڴ�ͼ����
		BufferedImage bi = null;
		Graphics2D g = null;
		if (GRAPHIC_PNG.equals(graphicFormat)
				&& (backgroundIndex < 0 || backgroundIndex >= BACKGROUND_COLORS.length)) {
			GraphicsConfiguration config = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDefaultConfiguration();
			bi = config.createCompatibleImage(width, height,
					Transparency.TRANSLUCENT);
			g = bi.createGraphics();
		}
		if (bi == null) {
			if (backgroundIndex < 0
					|| backgroundIndex >= BACKGROUND_COLORS.length) {
				backgroundIndex = 0;
			}
			bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			g = bi.createGraphics();
			// ���ñ���ɫ
			Color backColor = BACKGROUND_COLORS[backgroundIndex];
			g.setColor(backColor);
			g.fillRect(0, 0, width, height);
		}
		g.setStroke(new BasicStroke(1));

		// �������lineCount�������ߣ�ʹͼ���е���֤�벻�ױ���������̽�⵽
		// ���������
		Random random = new Random();

		for (int i = 0; i < lineCount; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int xl = random.nextInt(3);
			int yl = random.nextInt(3);
			Color color = LINE_COLORS[randomInt(random, 0, LINE_COLORS.length)];
			g.setColor(color);
			g.drawLine(x, y, x + xl, y + yl);
		}

		// ����font
		g.setFont(new Font(fontName, Font.ITALIC, fontSize));

		int fontWidth = width / charCount;
		// ����charValue,ÿ���ַ���ɫ���
		for (int i = 0; i < charCount; i++) {
			String c = charValue.substring(i, i + 1);
			Color color = CHAR_COLORS[randomInt(random, 0, CHAR_COLORS.length)];
			g.setColor(color);
			int xpos = offsetX + i * fontWidth;
			// ��ֱ���������
			int ypos = randomInt(random, offsetY + height / 2, height);
			g.drawString(c, xpos, ypos);
		}

		g.dispose();
		bi.flush();
		// �������
		ImageIO.write(bi, graphicFormat, out);

		return charValue;
	}

	/**
	 * ����[from,to)֮���һ���������
	 * 
	 * @param random
	 *            �����������
	 * @param from
	 *            ��ʼֵ
	 * @param to
	 *            ����ֵ
	 * @return [from,to)֮���һ���������
	 */
	protected int randomInt(Random random, int from, int to) {
		// Random r = new Random();
		return from + random.nextInt(to - from);
	}

	/**
	 * ������Χ��������ɫ
	 * 
	 * @param fc
	 *            ��ɫ��ΧСֵ
	 * @param bc
	 *            ��ɫ��Χ��ֵ
	 * @return Color ��ɫֵ
	 */
	public Color getRandColor(int fc, int bc) {
		Random random = new Random();

		if (fc > 255) {
			fc = 255;
		}

		if (bc > 255) {
			bc = 255;
		}

		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);

		return new Color(r, g, b);
	}
}
