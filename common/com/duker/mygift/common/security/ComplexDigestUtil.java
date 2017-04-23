/*
 * @(#)ComplexDigestUtil.java 2009-11-24
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.common.security;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * <pre>
 * md5,sha�����㷨,��ֹ����
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2009-11-24
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class ComplexDigestUtil {

	/**
	 * ���md5,��ֹ����
	 * 
	 * @param data
	 *            byte[] Ҫ���ܵ��ֽ���
	 * @return byte[] ���ܺ���ֽ���
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
	 * ���md5,��ֹ����
	 * 
	 * @param data
	 *            byte[] Ҫ���ܵ��ֽ���
	 * @return String ���ܺ��MD5��
	 */
	public static String multiMD5Hex(byte[] data) {
		return new String(Hex.encodeHex(multiMD5(data)));
	}

	/**
	 * ���sha,��ֹ����
	 * 
	 * @param data
	 *            byte[] Ҫ���ܵ��ֽ���
	 * @return byte[] ���ܺ���ֽ���
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
	 * ���sha,��ֹ����
	 * 
	 * @param data
	 *            byte[] Ҫ���ܵ��ֽ���
	 * @return String ���ܺ��sha��
	 */
	public static String multiShaHex(byte[] data) {
		return new String(Hex.encodeHex(multiSha(data)));
	}
}
