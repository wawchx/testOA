/*
 * @(#)ImageHepler.java Aug 30, 2009
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.bytesource.ByteSource;
import org.apache.commons.imaging.common.bytesource.ByteSourceFile;
import org.apache.commons.imaging.formats.jpeg.JpegImageParser;
import org.apache.commons.imaging.formats.jpeg.segments.App14Segment;
import org.apache.commons.imaging.formats.jpeg.segments.Segment;
import org.apache.commons.io.FileUtils;

import com.duker.mygift.common.util.gif.AnimatedGifEncoder;
import com.duker.mygift.common.util.gif.GifDecoder;

/**
 * <pre>
 * 图像缩放、添加水印工具类，支持PNG、GIF、JPG格式的图像
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: Aug 30, 2009
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class ImageHepler {

	/**
	 * 是否是GIF图像
	 * 
	 * @param fileName
	 *            文件名
	 * @return true是，false不是
	 */
	public static boolean isGif(String fileName) throws Exception {
		return "GIF".equals(getImageFormat(fileName));
	}

	/**
	 * 是否是GIF图像
	 * 
	 * @param file
	 *            图像文件
	 * @return true是，false不是
	 */
	public static boolean isGif(File file) throws Exception {
		return "GIF".equals(getImageFormat(file));
	}

	/**
	 * 是否是PNG图像
	 * 
	 * @param fileName
	 *            文件名
	 * @return true是，false不是
	 */
	public static boolean isPng(String fileName) throws Exception {
		return "PNG".equals(getImageFormat(fileName));
	}

	/**
	 * 是否是PNG图像
	 * 
	 * @param file
	 *            图像文件
	 * @return true是，false不是
	 */
	public static boolean isPng(File file) throws Exception {
		return "PNG".equals(getImageFormat(file));
	}

	/**
	 * 获取图像文件的文件格式
	 * 
	 * @param fileName
	 *            文件名
	 * @return 图像文件格式PNG, GIF, 如果非PNG, GIF图像类型那么返回unsupported
	 */
	public static String getImageFormat(String fileName) throws Exception {
		return getImageFormat(new File(fileName));
	}

	/**
	 * 获取图像文件的文件格式
	 * 
	 * @param file
	 *            图像文件
	 * @return 图像文件格式PNG, GIF, JPEG
	 */
	public static String getImageFormat(File file) throws Exception {
		ImageInfo info = Imaging.getImageInfo(file);

		return info.getFormat().getExtension();
	}

	/**
	 * 获取文件尺寸
	 * 
	 * @param fileName
	 *            文件名
	 * @return 文件尺寸
	 */
	public static Dimension getImageSize(String fileName) throws Exception {
		return Imaging.getImageSize(new File(fileName));
	}

	/**
	 * 获取文件尺寸
	 * 
	 * @param file
	 *            文件
	 * @return 文件尺寸
	 */
	public static Dimension getImageSize(File file) throws Exception {
		return Imaging.getImageSize(file);
	}

	/**
	 * 获取文件尺寸
	 * 
	 * @param in
	 *            文件输入流
	 * @return 文件尺寸
	 */
	public static Dimension getImageSize(InputStream in) throws Exception {
		return Imaging.getImageSize(in, null);
	}

	/**
	 * 缩放Gif图像
	 * 
	 * @param source
	 *            源文件地址
	 * @param dest
	 *            目标文件地址
	 * @param width
	 *            期望宽度
	 * @param height
	 *            期望高度
	 * @throws Exception
	 *             异常
	 */
	public static void resizeGIF(String source, String dest, int width,
			int height) throws Exception {
		resizeGIF(new File(source), new File(dest), width, height);
	}

	/**
	 * 缩放Gif图像
	 * 
	 * @param source
	 *            源文件
	 * @param dest
	 *            目标文件
	 * @param width
	 *            期望宽度
	 * @param height
	 *            期望高度
	 * @throws Exception
	 *             异常
	 */
	public static void resizeGIF(File source, File dest, int width, int height)
			throws Exception {
		InputStream in = null;
		OutputStream out = null;

		try {
			// 生成一个GifDecoder
			GifDecoder gifDecoder = new GifDecoder();
			in = new FileInputStream(source);

			if (GifDecoder.STATUS_FORMAT_ERROR == gifDecoder.read(in)) {
				// 调用read方法读入文件流，出错时，抛出异常。
				throw new Exception("读取文件" + source + "出错");
			}

			// 动态画面编码器
			AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
			out = new FileOutputStream(dest);
			animatedGifEncoder.start(out);
			// 设置循环显示的次数
			animatedGifEncoder.setRepeat(gifDecoder.getLoopCount());
			animatedGifEncoder.setQuality(19);

			// 取得帧的个数
			int frameCount = gifDecoder.getFrameCount();
			for (int i = 0; i < frameCount; i++) {
				// 取得帧
				BufferedImage sourceFrame = gifDecoder.getFrame(i);
				// 缩放
				BufferedImage targetFrame = resizeImage(sourceFrame, width,
						height);
				animatedGifEncoder.setTransparent(sourceFrame.createGraphics()
						.getColor());
				// 设置每帧显示间隔时间
				animatedGifEncoder.setDelay(gifDecoder.getDelay(i));
				animatedGifEncoder.addFrame(targetFrame);
			}

			animatedGifEncoder.finish();
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

	/**
	 * 缩放GIF,JPEG,PNG图像文件
	 * 
	 * @param source
	 *            源文件地址
	 * @param dest
	 *            目标文件地址
	 * @param width
	 *            目标宽度
	 * @param height
	 *            目标高度
	 * @throws Exception
	 *             异常信息
	 */
	public static void resizeImage(String source, String dest, int width,
			int height) throws Exception {
		resizeImage(new File(source), new File(dest), width, height);
	}

	/**
	 * 缩放GIF,JPEG,PNG图像文件
	 * 
	 * @param source
	 *            源文件
	 * @param dest
	 *            目标文件
	 * @param width
	 *            目标宽度
	 * @param height
	 *            目标高度
	 * @throws Exception
	 *             异常信息
	 */
	public static void resizeImage(File source, File dest, int width, int height)
			throws Exception {
		ImageInfo info = Imaging.getImageInfo(source);
		int sw = info.getWidth();
		int sh = info.getHeight();
		if (sw <= width && sh <= height) {
			FileUtils.copyFile(source, dest);

			return;
		}

		String formatName = info.getFormat().getExtension();
		if ("GIF".equals(formatName)) {
			resizeGIF(source, dest, width, height);
		}
		else {
			BufferedImage srcImage;
			int colorType = info.getColorType();
			if (ImageInfo.COLOR_TYPE_YCCK == colorType
					|| ImageInfo.COLOR_TYPE_CMYK == colorType) {
				// CMYK
				srcImage = createJPEG4(source, colorType);
			}
			else {
				srcImage = ImageIO.read(source);
			}

			BufferedImage destImage = resizeImage(srcImage, width, height);
			ImageIO.write(destImage, formatName, dest);
		}
	}

	/**
	 * 缩放GIF,JPEG,PNG图像文件
	 * 
	 * @param source
	 *            源文件
	 * @param dest
	 *            目标文件
	 * @param ratio
	 *            缩放比例,小于1
	 * @throws Exception
	 *             异常信息
	 */
	public static void resizeImage(String source, String dest, double ratio)
			throws Exception {
		resizeImage(new File(source), new File(dest), ratio);
	}

	/**
	 * 缩放GIF,JPEG,PNG图像文件
	 * 
	 * @param source
	 *            源文件
	 * @param dest
	 *            目标文件
	 * @param ratio
	 *            缩放比例,小于1
	 * @throws Exception
	 *             异常信息
	 */
	public static void resizeImage(File source, File dest, double ratio)
			throws Exception {
		if (ratio >= 1 || ratio < 0) {
			FileUtils.copyFile(source, dest);

			return;
		}

		ImageInfo info = Imaging.getImageInfo(source);
		int width = (int) (info.getWidth() * ratio);
		int height = (int) (info.getHeight() * ratio);

		String formatName = info.getFormat().getExtension();
		if ("GIF".equals(formatName)) {
			resizeGIF(source, dest, width, height);
		}
		else {
			BufferedImage srcImage;
			int colorType = info.getColorType();
			if (ImageInfo.COLOR_TYPE_YCCK == colorType
					|| ImageInfo.COLOR_TYPE_CMYK == colorType) {
				// CMYK
				srcImage = createJPEG4(source, colorType);
			}
			else {
				srcImage = ImageIO.read(source);
			}

			BufferedImage destImage = resizeImage(srcImage, width, height);
			ImageIO.write(destImage, formatName, dest);
		}
	}

	/**
	 * 检查是否有adobe标记
	 * 
	 * @param file
	 *            图片文件
	 * @return 是否有adobe标记
	 * @throws Exception
	 */
	private static boolean checkAdobeMarker(File file) throws Exception {
		JpegImageParser parser = new JpegImageParser();
		ByteSource byteSource = new ByteSourceFile(file);
		List<Segment> segments = parser.readSegments(byteSource,
				new int[] { 0xffee }, true);
		if (segments != null && segments.size() >= 1) {
			App14Segment app14Segment = (App14Segment) segments.get(0);
			byte[] data = app14Segment.bytes;
			if (data.length >= 12 && data[0] == 'A' && data[1] == 'd'
					&& data[2] == 'o' && data[3] == 'b' && data[4] == 'e') {
				return true;
			}
		}

		return false;
	}

	/*
	 * 转换adobe颜色模式
	 */
	private static void convertInvertedColors(WritableRaster raster) {
		int height = raster.getHeight();
		int width = raster.getWidth();
		int stride = width * 4;
		int[] pixelRow = new int[stride];
		for (int h = 0; h < height; h++) {
			raster.getPixels(0, h, width, 1, pixelRow);
			for (int x = 0; x < stride; x++) {
				pixelRow[x] = 255 - pixelRow[x];
			}
			raster.setPixels(0, h, width, 1, pixelRow);
		}
	}

	/*
	 * 转换ycck模式为cmyk模式
	 */
	private static void convertYcckToCmyk(WritableRaster raster) {
		int height = raster.getHeight();
		int width = raster.getWidth();
		int stride = width * 4;
		int[] pixelRow = new int[stride];
		for (int h = 0; h < height; h++) {
			raster.getPixels(0, h, width, 1, pixelRow);

			for (int x = 0; x < stride; x += 4) {
				int y = pixelRow[x];
				int cb = pixelRow[x + 1];
				int cr = pixelRow[x + 2];

				int c = (int) (y + 1.402 * cr - 178.956);
				int m = (int) (y - 0.34414 * cb - 0.71414 * cr + 135.95984);
				y = (int) (y + 1.772 * cb - 226.316);

				if (c < 0)
					c = 0;
				else if (c > 255)
					c = 255;
				if (m < 0)
					m = 0;
				else if (m > 255)
					m = 255;
				if (y < 0)
					y = 0;
				else if (y > 255)
					y = 255;

				pixelRow[x] = 255 - c;
				pixelRow[x + 1] = 255 - m;
				pixelRow[x + 2] = 255 - y;
			}

			raster.setPixels(0, h, width, 1, pixelRow);
		}
	}

	/*
	 * 转换cmyk图片为rgb图片
	 */
	private static BufferedImage convertCmykToRgb(Raster cmykRaster,
			ICC_Profile cmykProfile) throws IOException {
		if (cmykProfile == null) {
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();
			InputStream in = classLoader
					.getResourceAsStream("ISOcoated_v2_300_eci.icc");
			try {
				cmykProfile = ICC_Profile.getInstance(in);
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
		ICC_ColorSpace cmykCS = new ICC_ColorSpace(cmykProfile);
		BufferedImage rgbImage = new BufferedImage(cmykRaster.getWidth(),
				cmykRaster.getHeight(), BufferedImage.TYPE_INT_RGB);
		WritableRaster rgbRaster = rgbImage.getRaster();
		ColorSpace rgbCS = rgbImage.getColorModel().getColorSpace();
		ColorConvertOp cmykToRgb = new ColorConvertOp(cmykCS, rgbCS, null);
		cmykToRgb.filter(cmykRaster, rgbRaster);

		return rgbImage;
	}

	/**
	 * 转换CMYK颜色模式图片为RGB模式
	 * 
	 * @param file
	 *            CMYK模式图片
	 * @return BufferedImage
	 */
	private static BufferedImage createJPEG4(File file, int colorType)
			throws Exception {
		ImageInputStream input = ImageIO.createImageInputStream(file);
		Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
		if (readers == null || !readers.hasNext()) {
			throw new Exception("No ImageReaders found");
		}

		ImageReader reader = readers.next();
		reader.setInput(input);
		WritableRaster raster = (WritableRaster) reader.readRaster(0, null);
		if (colorType == ImageInfo.COLOR_TYPE_YCCK) {
			convertYcckToCmyk(raster);
		}

		boolean hasAdobeMarker = checkAdobeMarker(file);
		if (hasAdobeMarker) {
			convertInvertedColors(raster);
		}

		ICC_Profile profile = Imaging.getICCProfile(file);
		return convertCmykToRgb(raster, profile);
	}

	/**
	 * 实现图像的等比缩放截取，支持PNG、JPG图像
	 * 
	 * @param srcImage
	 *            原始图像
	 * @param width
	 *            缩放后的宽度
	 * @param height
	 *            缩放后的高度
	 * @return 缩放后的图像
	 */
	private static BufferedImage resizeImage(BufferedImage srcImage, int width,
			int height) {
		// 原图的大小
		int sw = srcImage.getWidth();
		int sh = srcImage.getHeight();

		if (sw <= width && sh <= height) {
			// 如果原图像的大小小于要缩放的图像大小，直接将要缩放的图像复制过去
			return srcImage;
		}
		// 截取位置
		int x = 0;
		int y = 0;
		// 对原图进行缩放后的大小
		int w = sw;
		int h = sh;

		if (sh * width > height * sw) {
			// 对高度进行截取
			w = sw;
			h = (sw * height) / width;
			y = (sh - h) / 2;
		}
		else {
			// 对宽度进行截取
			h = sh;
			w = (sh * width) / height;
			x = (sw - w) / 2;
		}

		// 先切割
		ImageFilter filter = new CropImageFilter(x, y, w, h);
		Image img = Toolkit.getDefaultToolkit().createImage(
				new FilteredImageSource(srcImage.getSource(), filter));
		// 再缩放
		img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);

		BufferedImage destImage = null;
		int type = srcImage.getType();
		if (type == BufferedImage.TYPE_CUSTOM) {
			// handmade
			ColorModel cm = srcImage.getColorModel();
			WritableRaster raster = cm.createCompatibleWritableRaster(width,
					height);
			boolean alphaPremultiplied = cm.isAlphaPremultiplied();
			destImage = new BufferedImage(cm, raster, alphaPremultiplied, null);
		}
		else {
			destImage = new BufferedImage(width, height, type);
		}

		Graphics g = destImage.getGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();

		return destImage;
	}

	/**
	 * 给Gif图像生成水印
	 * 
	 * @param source
	 *            源文件地址
	 * @param dest
	 *            目标文件地址
	 * @param text
	 *            水印内容
	 * @throws Exception
	 *             异常
	 */
	public static void createGifWatermark(String source, String dest,
			String text) throws Exception {
		createGifWatermark(new File(source), new File(dest), text);
	}

	/**
	 * 给Gif图像生成水印
	 * 
	 * @param source
	 *            源文件
	 * @param dest
	 *            目标文件
	 * @param text
	 *            水印内容
	 * @throws Exception
	 *             异常
	 */
	public static void createGifWatermark(File source, File dest, String text)
			throws Exception {
		InputStream in = null;
		OutputStream out = null;

		try {
			// 生成一个GifDecoder
			GifDecoder gifDecoder = new GifDecoder();
			in = new FileInputStream(source);

			if (GifDecoder.STATUS_FORMAT_ERROR == gifDecoder.read(in)) {
				// 调用read方法读入文件流，出错时，抛出异常。
				throw new Exception("读取文件" + source + "出错");
			}

			// 动态画面编码器
			AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
			out = new FileOutputStream(dest);
			animatedGifEncoder.start(out);
			// 设置循环显示的次数
			animatedGifEncoder.setRepeat(gifDecoder.getLoopCount());
			animatedGifEncoder.setQuality(19);

			// 取得帧的个数
			int frameCount = gifDecoder.getFrameCount();
			for (int i = 0; i < frameCount; i++) {
				// 取得帧
				BufferedImage sourceFrame = gifDecoder.getFrame(i);
				// 加水印
				BufferedImage targetFrame = createWatermark(sourceFrame, text);
				animatedGifEncoder.setTransparent(sourceFrame.createGraphics()
						.getColor());
				// 设置每帧显示间隔时间
				animatedGifEncoder.setDelay(gifDecoder.getDelay(i));
				animatedGifEncoder.addFrame(targetFrame);
			}

			animatedGifEncoder.finish();
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

	/**
	 * 给GIF,JPEG,PNG图像生成水印
	 * 
	 * @param source
	 *            源文件地址
	 * @param dest
	 *            目标文件地址
	 * @param text
	 *            水印内容
	 * @throws Exception
	 *             异常
	 */
	public static void createWatermark(String source, String dest, String text)
			throws Exception {
		createWatermark(new File(source), new File(dest), text);
	}

	/**
	 * 给GIF,JPEG,PNG图像生成水印
	 * 
	 * @param source
	 *            源文件
	 * @param dest
	 *            目标文件
	 * @param text
	 *            水印内容
	 * @throws Exception
	 *             异常
	 */
	public static void createWatermark(File source, File dest, String text)
			throws Exception {
		ImageInfo info = Imaging.getImageInfo(source);
		int sw = info.getWidth();
		int sh = info.getHeight();
		if (sw < 70 || sh < 10) {
			FileUtils.copyFile(source, dest);

			return;
		}

		String formatName = info.getFormat().getExtension();
		if ("GIF".equals(formatName)) {
			createGifWatermark(source, dest, text);
		}
		else {
			BufferedImage srcImage;
			int colorType = info.getColorType();
			if (ImageInfo.COLOR_TYPE_YCCK == colorType
					|| ImageInfo.COLOR_TYPE_CMYK == colorType) {
				// CMYK
				srcImage = createJPEG4(source, colorType);
			}
			else {
				srcImage = ImageIO.read(source);
			}

			BufferedImage destImage = createWatermark(srcImage, text);
			ImageIO.write(destImage, formatName, dest);
		}
	}

	/**
	 * 给图像生成水印，支持PNG,JPG图像
	 * 
	 * @param source
	 *            源文件
	 * @param text
	 *            水印内容
	 * @throws Exception
	 *             异常
	 */
	private static BufferedImage createWatermark(BufferedImage source,
			String text) throws Exception {
		int width = source.getWidth();
		int height = source.getHeight();

		int fontSizeX = width / 45;
		int fontSizeY = height / 15;
		int fontSize = fontSizeX < fontSizeY ? fontSizeX : fontSizeY;
		fontSize = fontSize < 12 ? 12 : fontSize;

		int chinese = 0;
		int length = text.length();
		for (int i = 0; i < length; i++) {
			char chr = text.charAt(i);
			if (chr <= 0x9FA5 && chr >= 0x4E00) {
				chinese++;
			}
		}

		fontSizeX = width - fontSize * ((length - chinese) * 3 + chinese * 6)
				/ 5;
		fontSizeY = height - fontSize;
		fontSizeX = fontSizeX < 0 ? 0 : fontSizeX;
		fontSizeY = fontSizeY < 0 ? 0 : fontSizeY;

		// 取得图形上下文
		Graphics2D g = source.createGraphics();
		// 去除锯齿(当设置的字体过大的时候,会出现锯齿)
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.red);
		g.setFont(new Font(null, Font.HANGING_BASELINE, fontSize));
		// 在指定坐标除添加文字
		g.drawString(text, fontSizeX, fontSizeY);
		g.dispose();

		return source;
	}

}
