/*
 * @(#)ImageHepler.java Aug 30, 2009
 * 
 * ��Ϣ��˹���ϵͳ
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
 * ͼ�����š����ˮӡ�����֧࣬��PNG��GIF��JPG��ʽ��ͼ��
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: Aug 30, 2009
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class ImageHepler {

	/**
	 * �Ƿ���GIFͼ��
	 * 
	 * @param fileName
	 *            �ļ���
	 * @return true�ǣ�false����
	 */
	public static boolean isGif(String fileName) throws Exception {
		return "GIF".equals(getImageFormat(fileName));
	}

	/**
	 * �Ƿ���GIFͼ��
	 * 
	 * @param file
	 *            ͼ���ļ�
	 * @return true�ǣ�false����
	 */
	public static boolean isGif(File file) throws Exception {
		return "GIF".equals(getImageFormat(file));
	}

	/**
	 * �Ƿ���PNGͼ��
	 * 
	 * @param fileName
	 *            �ļ���
	 * @return true�ǣ�false����
	 */
	public static boolean isPng(String fileName) throws Exception {
		return "PNG".equals(getImageFormat(fileName));
	}

	/**
	 * �Ƿ���PNGͼ��
	 * 
	 * @param file
	 *            ͼ���ļ�
	 * @return true�ǣ�false����
	 */
	public static boolean isPng(File file) throws Exception {
		return "PNG".equals(getImageFormat(file));
	}

	/**
	 * ��ȡͼ���ļ����ļ���ʽ
	 * 
	 * @param fileName
	 *            �ļ���
	 * @return ͼ���ļ���ʽPNG, GIF, �����PNG, GIFͼ��������ô����unsupported
	 */
	public static String getImageFormat(String fileName) throws Exception {
		return getImageFormat(new File(fileName));
	}

	/**
	 * ��ȡͼ���ļ����ļ���ʽ
	 * 
	 * @param file
	 *            ͼ���ļ�
	 * @return ͼ���ļ���ʽPNG, GIF, JPEG
	 */
	public static String getImageFormat(File file) throws Exception {
		ImageInfo info = Imaging.getImageInfo(file);

		return info.getFormat().getExtension();
	}

	/**
	 * ��ȡ�ļ��ߴ�
	 * 
	 * @param fileName
	 *            �ļ���
	 * @return �ļ��ߴ�
	 */
	public static Dimension getImageSize(String fileName) throws Exception {
		return Imaging.getImageSize(new File(fileName));
	}

	/**
	 * ��ȡ�ļ��ߴ�
	 * 
	 * @param file
	 *            �ļ�
	 * @return �ļ��ߴ�
	 */
	public static Dimension getImageSize(File file) throws Exception {
		return Imaging.getImageSize(file);
	}

	/**
	 * ��ȡ�ļ��ߴ�
	 * 
	 * @param in
	 *            �ļ�������
	 * @return �ļ��ߴ�
	 */
	public static Dimension getImageSize(InputStream in) throws Exception {
		return Imaging.getImageSize(in, null);
	}

	/**
	 * ����Gifͼ��
	 * 
	 * @param source
	 *            Դ�ļ���ַ
	 * @param dest
	 *            Ŀ���ļ���ַ
	 * @param width
	 *            �������
	 * @param height
	 *            �����߶�
	 * @throws Exception
	 *             �쳣
	 */
	public static void resizeGIF(String source, String dest, int width,
			int height) throws Exception {
		resizeGIF(new File(source), new File(dest), width, height);
	}

	/**
	 * ����Gifͼ��
	 * 
	 * @param source
	 *            Դ�ļ�
	 * @param dest
	 *            Ŀ���ļ�
	 * @param width
	 *            �������
	 * @param height
	 *            �����߶�
	 * @throws Exception
	 *             �쳣
	 */
	public static void resizeGIF(File source, File dest, int width, int height)
			throws Exception {
		InputStream in = null;
		OutputStream out = null;

		try {
			// ����һ��GifDecoder
			GifDecoder gifDecoder = new GifDecoder();
			in = new FileInputStream(source);

			if (GifDecoder.STATUS_FORMAT_ERROR == gifDecoder.read(in)) {
				// ����read���������ļ���������ʱ���׳��쳣��
				throw new Exception("��ȡ�ļ�" + source + "����");
			}

			// ��̬���������
			AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
			out = new FileOutputStream(dest);
			animatedGifEncoder.start(out);
			// ����ѭ����ʾ�Ĵ���
			animatedGifEncoder.setRepeat(gifDecoder.getLoopCount());
			animatedGifEncoder.setQuality(19);

			// ȡ��֡�ĸ���
			int frameCount = gifDecoder.getFrameCount();
			for (int i = 0; i < frameCount; i++) {
				// ȡ��֡
				BufferedImage sourceFrame = gifDecoder.getFrame(i);
				// ����
				BufferedImage targetFrame = resizeImage(sourceFrame, width,
						height);
				animatedGifEncoder.setTransparent(sourceFrame.createGraphics()
						.getColor());
				// ����ÿ֡��ʾ���ʱ��
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
	 * ����GIF,JPEG,PNGͼ���ļ�
	 * 
	 * @param source
	 *            Դ�ļ���ַ
	 * @param dest
	 *            Ŀ���ļ���ַ
	 * @param width
	 *            Ŀ����
	 * @param height
	 *            Ŀ��߶�
	 * @throws Exception
	 *             �쳣��Ϣ
	 */
	public static void resizeImage(String source, String dest, int width,
			int height) throws Exception {
		resizeImage(new File(source), new File(dest), width, height);
	}

	/**
	 * ����GIF,JPEG,PNGͼ���ļ�
	 * 
	 * @param source
	 *            Դ�ļ�
	 * @param dest
	 *            Ŀ���ļ�
	 * @param width
	 *            Ŀ����
	 * @param height
	 *            Ŀ��߶�
	 * @throws Exception
	 *             �쳣��Ϣ
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
	 * ����GIF,JPEG,PNGͼ���ļ�
	 * 
	 * @param source
	 *            Դ�ļ�
	 * @param dest
	 *            Ŀ���ļ�
	 * @param ratio
	 *            ���ű���,С��1
	 * @throws Exception
	 *             �쳣��Ϣ
	 */
	public static void resizeImage(String source, String dest, double ratio)
			throws Exception {
		resizeImage(new File(source), new File(dest), ratio);
	}

	/**
	 * ����GIF,JPEG,PNGͼ���ļ�
	 * 
	 * @param source
	 *            Դ�ļ�
	 * @param dest
	 *            Ŀ���ļ�
	 * @param ratio
	 *            ���ű���,С��1
	 * @throws Exception
	 *             �쳣��Ϣ
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
	 * ����Ƿ���adobe���
	 * 
	 * @param file
	 *            ͼƬ�ļ�
	 * @return �Ƿ���adobe���
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
	 * ת��adobe��ɫģʽ
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
	 * ת��ycckģʽΪcmykģʽ
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
	 * ת��cmykͼƬΪrgbͼƬ
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
	 * ת��CMYK��ɫģʽͼƬΪRGBģʽ
	 * 
	 * @param file
	 *            CMYKģʽͼƬ
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
	 * ʵ��ͼ��ĵȱ����Ž�ȡ��֧��PNG��JPGͼ��
	 * 
	 * @param srcImage
	 *            ԭʼͼ��
	 * @param width
	 *            ���ź�Ŀ��
	 * @param height
	 *            ���ź�ĸ߶�
	 * @return ���ź��ͼ��
	 */
	private static BufferedImage resizeImage(BufferedImage srcImage, int width,
			int height) {
		// ԭͼ�Ĵ�С
		int sw = srcImage.getWidth();
		int sh = srcImage.getHeight();

		if (sw <= width && sh <= height) {
			// ���ԭͼ��Ĵ�СС��Ҫ���ŵ�ͼ���С��ֱ�ӽ�Ҫ���ŵ�ͼ���ƹ�ȥ
			return srcImage;
		}
		// ��ȡλ��
		int x = 0;
		int y = 0;
		// ��ԭͼ�������ź�Ĵ�С
		int w = sw;
		int h = sh;

		if (sh * width > height * sw) {
			// �Ը߶Ƚ��н�ȡ
			w = sw;
			h = (sw * height) / width;
			y = (sh - h) / 2;
		}
		else {
			// �Կ�Ƚ��н�ȡ
			h = sh;
			w = (sh * width) / height;
			x = (sw - w) / 2;
		}

		// ���и�
		ImageFilter filter = new CropImageFilter(x, y, w, h);
		Image img = Toolkit.getDefaultToolkit().createImage(
				new FilteredImageSource(srcImage.getSource(), filter));
		// ������
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
	 * ��Gifͼ������ˮӡ
	 * 
	 * @param source
	 *            Դ�ļ���ַ
	 * @param dest
	 *            Ŀ���ļ���ַ
	 * @param text
	 *            ˮӡ����
	 * @throws Exception
	 *             �쳣
	 */
	public static void createGifWatermark(String source, String dest,
			String text) throws Exception {
		createGifWatermark(new File(source), new File(dest), text);
	}

	/**
	 * ��Gifͼ������ˮӡ
	 * 
	 * @param source
	 *            Դ�ļ�
	 * @param dest
	 *            Ŀ���ļ�
	 * @param text
	 *            ˮӡ����
	 * @throws Exception
	 *             �쳣
	 */
	public static void createGifWatermark(File source, File dest, String text)
			throws Exception {
		InputStream in = null;
		OutputStream out = null;

		try {
			// ����һ��GifDecoder
			GifDecoder gifDecoder = new GifDecoder();
			in = new FileInputStream(source);

			if (GifDecoder.STATUS_FORMAT_ERROR == gifDecoder.read(in)) {
				// ����read���������ļ���������ʱ���׳��쳣��
				throw new Exception("��ȡ�ļ�" + source + "����");
			}

			// ��̬���������
			AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
			out = new FileOutputStream(dest);
			animatedGifEncoder.start(out);
			// ����ѭ����ʾ�Ĵ���
			animatedGifEncoder.setRepeat(gifDecoder.getLoopCount());
			animatedGifEncoder.setQuality(19);

			// ȡ��֡�ĸ���
			int frameCount = gifDecoder.getFrameCount();
			for (int i = 0; i < frameCount; i++) {
				// ȡ��֡
				BufferedImage sourceFrame = gifDecoder.getFrame(i);
				// ��ˮӡ
				BufferedImage targetFrame = createWatermark(sourceFrame, text);
				animatedGifEncoder.setTransparent(sourceFrame.createGraphics()
						.getColor());
				// ����ÿ֡��ʾ���ʱ��
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
	 * ��GIF,JPEG,PNGͼ������ˮӡ
	 * 
	 * @param source
	 *            Դ�ļ���ַ
	 * @param dest
	 *            Ŀ���ļ���ַ
	 * @param text
	 *            ˮӡ����
	 * @throws Exception
	 *             �쳣
	 */
	public static void createWatermark(String source, String dest, String text)
			throws Exception {
		createWatermark(new File(source), new File(dest), text);
	}

	/**
	 * ��GIF,JPEG,PNGͼ������ˮӡ
	 * 
	 * @param source
	 *            Դ�ļ�
	 * @param dest
	 *            Ŀ���ļ�
	 * @param text
	 *            ˮӡ����
	 * @throws Exception
	 *             �쳣
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
	 * ��ͼ������ˮӡ��֧��PNG,JPGͼ��
	 * 
	 * @param source
	 *            Դ�ļ�
	 * @param text
	 *            ˮӡ����
	 * @throws Exception
	 *             �쳣
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

		// ȡ��ͼ��������
		Graphics2D g = source.createGraphics();
		// ȥ�����(�����õ���������ʱ��,����־��)
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.red);
		g.setFont(new Font(null, Font.HANGING_BASELINE, fontSize));
		// ��ָ��������������
		g.drawString(text, fontSizeX, fontSizeY);
		g.dispose();

		return source;
	}

}
