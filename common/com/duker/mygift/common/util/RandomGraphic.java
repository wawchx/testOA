/*
 * @(#)RandomGraphic.java 2010-8-24
 * 
 * 信息审核管理系统
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
 *  生成随机数字或字母串，以图像方式显示，用于人工识别，使程序很难识别。 减小系统被程序自动攻击的可能性。
 *  生成的字符图形颜色由蓝色组而成，数字或字母垂直方向位置在一定范围内也是随机的，减少被程序自动识别的几率。
 *  干扰线的颜色由红、橙、黄、绿、蓝、靛、紫随机组合而成。
 *  由于数字的0，1，2易和字母的o，l,z混淆，使人眼难以识别，因此不建议生成数字和字母的混合串。
 *  生成的串字母统一用小写，串的最大长度为16。
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
public class RandomGraphic {

	/**
	 * 要生成的字符个数，由工厂方法得到
	 */
	private int charCount = 0;

	/**
	 * 干扰线条数
	 */
	private int lineCount = 10;

	/**
	 * 图片宽度，单位为像素
	 */
	private int width = 90;

	/**
	 * 图片高度，单位为像素
	 */
	private int height = 30;

	/**
	 * 字体大小，单位pt
	 */
	private int fontSize = 22;

	/**
	 * 字体
	 */
	private String fontName = "Verdana";

	/**
	 * 背景颜色索引
	 */
	private int backgroundIndex = 0;

	/**
	 * 水平方向起始位置
	 */
	private int offsetX = 5;

	/**
	 * 垂直方向起始位置
	 */
	private int offsetY = 5;

	/**
	 * 最大字符串个数
	 */
	private static final int MAX_CHAR_COUNT = 16;

	/**
	 * 最大干扰线个数
	 */
	private static final int MAX_LINE_COUNT = 100;

	/**
	 * 颜色数组，背景颜色使用
	 */
	private static final Color[] BACKGROUND_COLORS = { Color.WHITE,
			new Color(253, 253, 197) };

	/**
	 * 颜色数组，绘制字串时随机选择一个
	 */
	private static final Color[] CHAR_COLORS = { Color.BLUE,
			new Color(0, 0, 139), new Color(0, 139, 139), Color.GREEN,
			new Color(0, 100, 0), Color.RED, new Color(139, 0, 0),
			new Color(235, 37, 86), new Color(64, 115, 220),
			new Color(34, 128, 8), new Color(105, 59, 9) };

	/**
	 * 颜色数组，绘制干扰线时随机选择一个
	 */
	private static final Color[] LINE_COLORS = { Color.RED, Color.ORANGE,
			Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA };

	/**
	 * 生成图像的格式常量，JPEG格式,生成为文件时扩展名为.jpg； 输出到页面时需要设置MIME type 为image/jpeg
	 */
	public final static String GRAPHIC_JPEG = "JPEG";

	/**
	 * 生成图像的格式常量，PNG格式,生成为文件时扩展名为.png； 输出到页面时需要设置MIME type 为image/png
	 */
	public final static String GRAPHIC_PNG = "PNG";

	protected RandomGraphic() {
	}

	/**
	 * 用工厂方法创建对象
	 * 
	 * @param charCount
	 *            字符个数
	 */
	protected RandomGraphic(int charCount) {
		if (charCount > 0 && charCount <= MAX_CHAR_COUNT) {
			this.charCount = charCount;
		}
	}

	/**
	 * 用工厂方法创建对象
	 * 
	 * @param charCount
	 *            字符个数
	 * @param lineCount
	 *            干扰线条数
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
	 * 用工厂方法创建对象
	 * 
	 * @param charCount
	 *            字符个数
	 * @param lineCount
	 *            干扰线条数
	 * @param width
	 *            图片宽度
	 * @param height
	 *            图片高度
	 * @param fontSize
	 *            字体大小
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
	 * 用工厂方法创建对象
	 * 
	 * @param charCount
	 *            字符个数
	 * @param lineCount
	 *            干扰线条数
	 * @param width
	 *            图片宽度
	 * @param height
	 *            图片高度
	 * @param fontSize
	 *            字体大小
	 * @param fontName
	 *            字体名
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
	 * 用工厂方法创建对象
	 * 
	 * @param charCount
	 *            字符个数
	 * @param lineCount
	 *            干扰线条数
	 * @param width
	 *            图片宽度
	 * @param height
	 *            图片高度
	 * @param fontSize
	 *            字体大小
	 * @param fontName
	 *            字体名
	 * @param backgroundIndex
	 *            背景颜色索引
	 * @param offsetX
	 *            水平方向起始位置
	 * @param offsetY
	 *            垂直方向起始位置
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
	 * 创建对象的工厂方法
	 * 
	 * @param charCount
	 *            要生成的字符个数，个数在1到16之间
	 * @return 返回RandomGraphic对象实例
	 */
	public static RandomGraphic createInstance(int charCount) {
		return new RandomGraphic(charCount);
	}

	/**
	 * 创建对象的工厂方法
	 * 
	 * @param charCount
	 *            要生成的字符个数，个数在1到16之间
	 * @param lineCount
	 *            要生成的干扰线条数
	 * @return 返回RandomGraphic对象实例
	 */
	public static RandomGraphic createInstance(int charCount, int lineCount) {
		return new RandomGraphic(charCount, lineCount);
	}

	/**
	 * 创建对象的工厂方法
	 * 
	 * @param charCount
	 *            要生成的字符个数，个数在1到16之间
	 * @param lineCount
	 *            要生成的干扰线条数
	 * @param width
	 *            图片宽度
	 * @param height
	 *            图片高度
	 * @param fontSize
	 *            字体大小
	 * @return 返回RandomGraphic对象实例
	 */
	public static RandomGraphic createInstance(int charCount, int lineCount,
			int width, int height, int fontSize) {
		return new RandomGraphic(charCount, lineCount, width, height, fontSize);
	}

	/**
	 * 创建对象的工厂方法
	 * 
	 * @param charCount
	 *            要生成的字符个数，个数在1到16之间
	 * @param lineCount
	 *            要生成的干扰线条数
	 * @param width
	 *            图片宽度
	 * @param height
	 *            图片高度
	 * @param fontSize
	 *            字体大小
	 * @param fontName
	 *            字体名
	 * @return 返回RandomGraphic对象实例
	 */
	public static RandomGraphic createInstance(int charCount, int lineCount,
			int width, int height, int fontSize, String fontName) {
		return new RandomGraphic(charCount, lineCount, width, height, fontSize,
				fontName);
	}

	/**
	 * 创建对象的工厂方法
	 * 
	 * @param charCount
	 *            要生成的字符个数，个数在1到16之间
	 * @param lineCount
	 *            要生成的干扰线条数
	 * @param width
	 *            图片宽度
	 * @param height
	 *            图片高度
	 * @param fontSize
	 *            字体大小
	 * @param fontName
	 *            字体名
	 * @param backgroundIndex
	 *            背景颜色索引
	 * @param offsetX
	 *            水平方向起始位置
	 * @param offsetY
	 *            垂直方向起始位置
	 * @return 返回RandomGraphic对象实例
	 */
	public static RandomGraphic createInstance(int charCount, int lineCount,
			int width, int height, int fontSize, String fontName,
			int backgroundIndex, int offsetX, int offsetY) {
		return new RandomGraphic(charCount, lineCount, width, height, fontSize,
				fontName, backgroundIndex, offsetX, offsetY);
	}

	/**
	 * 生成数字随机串
	 * 
	 * @return 随机串的值
	 */
	public String createNumber() {
		// 随机生成的串的值
		String charValue = RandomStringUtils.randomNumeric(charCount);

		return charValue;
	}

	/**
	 * 生成字母随机串
	 * 
	 * @return 随机串的值
	 */
	public String createAlpha() {
		// 随机生成的串的值
		String charValue = RandomStringUtils.randomAlphabetic(charCount)
				.toLowerCase();

		return charValue;
	}

	/**
	 * 生成字母、数字随机串
	 * 
	 * @return 随机串的值
	 */
	public String createAlphaNumber() {
		// 随机生成的串的值
		String charValue = RandomStringUtils.randomAlphanumeric(charCount)
				.toLowerCase();

		return charValue;
	}

	/**
	 * 以图像方式绘制字符串，绘制结果输出到流out中
	 * 
	 * @param charValue
	 *            要绘制的字符串
	 * @param graphicFormat
	 *            设置生成的图像格式，值为GRAPHIC_JPEG或GRAPHIC_PNG
	 * @param out
	 *            图像结果输出流
	 * @return 随机生成的串的值
	 * @throws IOException
	 */
	public String draw(String charValue, String graphicFormat, OutputStream out)
			throws IOException {
		// 创建内存图像区
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
			// 设置背景色
			Color backColor = BACKGROUND_COLORS[backgroundIndex];
			g.setColor(backColor);
			g.fillRect(0, 0, width, height);
		}
		g.setStroke(new BasicStroke(1));

		// 随机产生lineCount条干扰线，使图象中的认证码不易被其它程序探测到
		// 生成随机类
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

		// 设置font
		g.setFont(new Font(fontName, Font.ITALIC, fontSize));

		int fontWidth = width / charCount;
		// 绘制charValue,每个字符颜色随机
		for (int i = 0; i < charCount; i++) {
			String c = charValue.substring(i, i + 1);
			Color color = CHAR_COLORS[randomInt(random, 0, CHAR_COLORS.length)];
			g.setColor(color);
			int xpos = offsetX + i * fontWidth;
			// 垂直方向上随机
			int ypos = randomInt(random, offsetY + height / 2, height);
			g.drawString(c, xpos, ypos);
		}

		g.dispose();
		bi.flush();
		// 输出到流
		ImageIO.write(bi, graphicFormat, out);

		return charValue;
	}

	/**
	 * 返回[from,to)之间的一个随机整数
	 * 
	 * @param random
	 *            随机数生成器
	 * @param from
	 *            起始值
	 * @param to
	 *            结束值
	 * @return [from,to)之间的一个随机整数
	 */
	protected int randomInt(Random random, int from, int to) {
		// Random r = new Random();
		return from + random.nextInt(to - from);
	}

	/**
	 * 给定范围获得随机颜色
	 * 
	 * @param fc
	 *            颜色范围小值
	 * @param bc
	 *            颜色范围大值
	 * @return Color 颜色值
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
