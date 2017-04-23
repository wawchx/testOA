/*
 * @(#)ComplexDigestUtil.java 2009-11-24
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.security;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * <pre>
 * md5,sha变形算法,防止暴破
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2009-11-24
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class ComplexDigestUtil {

	/**
	 * 多次md5,防止暴破
	 * 
	 * @param data
	 *            byte[] 要加密的字节码
	 * @return byte[] 加密后的字节码
	 */
	public static byte[] multiMD5(byte[] data) {
		byte[] res = DigestUtils.md5(data);
		int headLength = res.length / 2;
		byte[] head = new byte[headLength];

		if (res.length > headLength) {
			byte[] tail = new byte[res.length - headLength];

			System.arraycopy(res, 0, head, 0, headLength);
			System.arraycopy(res, headLength, tail, 0, tail.length);
			head = DigestUtils.md5(head);
			tail = DigestUtils.md5(tail);
			res = new byte[head.length + tail.length];
			System.arraycopy(head, 0, res, 0, head.length);
			System.arraycopy(tail, 0, res, head.length, tail.length);
		}

		res = DigestUtils.md5(res);

		return res;
	}

	/**
	 * 多次md5,防止暴破
	 * 
	 * @param data
	 *            byte[] 要加密的字节码
	 * @return String 加密后的MD5串
	 */
	public static String multiMD5Hex(byte[] data) {
		return new String(Hex.encodeHex(multiMD5(data)));
	}

	/**
	 * 多次sha,防止暴破
	 * 
	 * @param data
	 *            byte[] 要加密的字节码
	 * @return byte[] 加密后的字节码
	 */
	public static byte[] multiSha(byte[] data) {
		byte[] res = DigestUtils.sha1(data);
		int headLength = res.length / 2;
		byte[] head = new byte[headLength];

		if (res.length > headLength) {
			byte[] tail = new byte[res.length - headLength];

			System.arraycopy(res, 0, head, 0, headLength);
			System.arraycopy(res, headLength, tail, 0, tail.length);
			head = DigestUtils.sha1(head);
			tail = DigestUtils.sha1(tail);
			res = new byte[head.length + tail.length];
			System.arraycopy(head, 0, res, 0, head.length);
			System.arraycopy(tail, 0, res, head.length, tail.length);
		}

		res = DigestUtils.sha1(res);

		return res;
	}

	/**
	 * 多次sha,防止暴破
	 * 
	 * @param data
	 *            byte[] 要加密的字节码
	 * @return String 加密后的sha串
	 */
	public static String multiShaHex(byte[] data) {
		return new String(Hex.encodeHex(multiSha(data)));
	}
}
