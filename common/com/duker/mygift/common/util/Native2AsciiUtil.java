/*
 * @(#)Native2AsciiUtil.java 2011-1-10
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.util;

/**
 * <pre>
 * native2ascii.exe Java code implementation.
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2011-1-10
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class Native2AsciiUtil {

	/**
	 * prefix of ascii string of native character
	 */
	private static String PREFIX = "\\u";

	/**
	 * Native to ascii string. It's same as execut native2ascii.exe.
	 * 
	 * @param str
	 *            native string
	 * @return ascii string
	 */
	public static String native2Ascii(String str) {
		StringBuilder sb = new StringBuilder();
		int length = str.length();
		for (int i = 0; i < length; i++) {
			sb.append(char2Ascii(str.charAt(i)));
		}

		return sb.toString();
	}

	/**
	 * Native character to ascii string.
	 * 
	 * @param c
	 *            native character
	 * @return ascii string
	 */
	private static String char2Ascii(int c) {
		return PREFIX + String.format("%04x", c);
	}

	/**
	 * Ascii to native string. It's same as execut native2ascii.exe -reverse.
	 * 
	 * @param str
	 *            ascii string
	 * @return native string
	 */
	public static String ascii2Native(String str) {
		StringBuilder sb = new StringBuilder();
		int begin = 0;
		int index = str.indexOf(PREFIX);
		while (index != -1) {
			sb.append(str.substring(begin, index));
			sb.append(ascii2Char(str.substring(index, index + 6)));
			begin = index + 6;
			index = str.indexOf(PREFIX, begin);
		}
		sb.append(str.substring(begin));

		return sb.toString();
	}

	/**
	 * Ascii to native character.
	 * 
	 * @param str
	 *            ascii string
	 * @return native character
	 */
	private static char ascii2Char(String str) {
		if (str.length() != 6) {
			throw new IllegalArgumentException(
					"Ascii string of a native character must be 6 character.");
		}
		if (!PREFIX.equals(str.substring(0, 2))) {
			throw new IllegalArgumentException(
					"Ascii string of a native character must start with \"\\u\".");
		}
		return (char) (Integer.parseInt(str.substring(2, 6), 16));
	}
}
