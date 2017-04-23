/*
 * @(#)DESedeCryptoUtil.java 2009-9-8
 * 
 * 信息审核管理系统
 */
package com.duker.mygift.common.security;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

/**
 * <pre>
 * DES 加解密工具（多线程安全）
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2009-9-8
 * 修改人 :  zhangwei
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public final class DESedeCryptoUtil {

	private Key key;

	private String algorithm = "DESede";

	/**
	 * 构造加解密工具实例
	 * 
	 * @param keyBytes
	 *            密钥字节串(24字节)
	 * @throws Exception
	 */
	public DESedeCryptoUtil(byte[] keyBytes) throws Exception {
		this(keyBytes, "DESede");
	}

	/**
	 * 构造加解密工具实例
	 * 
	 * @param keyBytes
	 *            密钥字节串(24 字节)
	 * @param algorithm
	 *            加解密算法
	 * @throws Exception
	 */
	public DESedeCryptoUtil(byte[] keyBytes, String algorithm) throws Exception {
		this.initKey(keyBytes, algorithm);
	}

	/**
	 * 构造加解密工具实例
	 * 
	 * @param base16KeyString
	 *            BASE16 编码的密钥字符串
	 * @throws Exception
	 */
	public DESedeCryptoUtil(String base16KeyString) throws Exception {
		this(base16KeyString, "DESede");
	}

	/**
	 * 构造加解密工具实例
	 * 
	 * @param base16KeyString
	 *            BASE16 编码的密钥字符串
	 * @param algorithm
	 *            加解密算法
	 * @throws Exception
	 */
	public DESedeCryptoUtil(String base16KeyString, String algorithm)
			throws Exception {
		byte[] keyBytes = Hex.decodeHex(base16KeyString.toCharArray());
		this.initKey(keyBytes, algorithm);
	}

	/**
	 * 加密明文字节串
	 * 
	 * @param plainBytes
	 *            待加密的明文字节串
	 * @return 加密后的密文字节串
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] plainBytes) throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(plainBytes);
	}

	/**
	 * 加密明文字符串
	 * 
	 * @param plainString
	 *            待加密的明文字节串
	 * @return 加密后的密文字节串的 BASE16 编码字符串
	 * @throws Exception
	 */
	public String encrypt(String plainString) throws Exception {
		byte[] cipherBytes = this.encrypt(plainString.getBytes());
		return new String(Hex.encodeHex(cipherBytes));
	}

	/**
	 * 解密密文字节串
	 * 
	 * @param cipherBytes
	 *            待解密的密文字节串
	 * @return 明文字节串
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] cipherBytes) throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(cipherBytes);
	}

	/**
	 * 密钥解密密文字符串
	 * 
	 * @param base16CipherString
	 *            待解密的密文字节串 BASE16 编码的字符串
	 * @return 明文字符串
	 * @throws Exception
	 */
	public byte[] decrypt(String base16CipherString) throws Exception {
		return this.decrypt(Hex.decodeHex(base16CipherString.toCharArray()));
	}

	/**
	 * 加密明文字节串
	 * 
	 * @param keyBytes
	 *            DES 密钥字节串(24 字节)
	 * @param plainBytes
	 *            待加密的明文字节串
	 * @return 加密后的密文字节串
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] keyBytes, byte[] plainBytes)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(keyBytes);
		return desEDECryptoUtil.encrypt(plainBytes);
	}

	/**
	 * 加密明文字节串
	 * 
	 * @param keyBytes
	 *            DES 密钥字节串(24 字节)
	 * @param plainString
	 *            待加密的明文字节串
	 * @return 加密后的密文字节串
	 * @throws Exception
	 */
	public static String encrypt(byte[] keyBytes, String plainString)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(keyBytes);
		return desEDECryptoUtil.encrypt(plainString);
	}

	/**
	 * 加密明文字符串
	 * 
	 * @param base16KeyString
	 *            BASE16 编码的 DES 密钥字符串
	 * @param plainBytes
	 *            待加密的明文字节串
	 * @return 加密后的密文字节串的 BASE16 编码字符串
	 * @throws Exception
	 */
	public static byte[] encrypt(String base16KeyString, byte[] plainBytes)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(
				base16KeyString);
		return desEDECryptoUtil.encrypt(plainBytes);
	}

	/**
	 * 加密明文字符串
	 * 
	 * @param base16KeyString
	 *            BASE16 编码的 DES 密钥字符串
	 * @param plainString
	 *            待加密的明文字节串
	 * @return 加密后的密文字节串的 BASE16 编码字符串
	 * @throws Exception
	 */
	public static String encrypt(String base16KeyString, String plainString)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(
				base16KeyString);
		return desEDECryptoUtil.encrypt(plainString);
	}

	/**
	 * 解密密文字节串
	 * 
	 * @param keyBytes
	 *            DES 密钥字节串(24 字节)
	 * @param cipherBytes
	 *            待解密的密文字节串
	 * @return 明文字节串
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] keyBytes, byte[] cipherBytes)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(keyBytes);
		return desEDECryptoUtil.decrypt(cipherBytes);
	}

	/**
	 * 解密密文字节串
	 * 
	 * @param keyBytes
	 *            DES 密钥字节串(24 字节)
	 * @param base16CipherString
	 *            待解密的密文字节串 BASE16 编码的字符串
	 * @return 明文字节串
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] keyBytes, String base16CipherString)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(keyBytes);
		return desEDECryptoUtil.decrypt(base16CipherString);
	}

	/**
	 * 解密密文字符串
	 * 
	 * @param base16KeyString
	 *            BASE16 编码的 DES 密钥字符串
	 * @param cipherBytes
	 *            待解密的密文字节串
	 * @return 明文字符串
	 * @throws Exception
	 */
	public static byte[] decrypt(String base16KeyString, byte[] cipherBytes)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(
				base16KeyString);
		return desEDECryptoUtil.decrypt(cipherBytes);
	}

	/**
	 * 解密密文字符串
	 * 
	 * @param base16KeyString
	 *            BASE16 编码的 DES 密钥字符串
	 * @param base16CipherString
	 *            待解密的密文字节串 BASE16 编码的字符串
	 * @return 明文字符串
	 * @throws Exception
	 */
	public static byte[] decrypt(String base16KeyString,
			String base16CipherString) throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(
				base16KeyString);
		return desEDECryptoUtil.decrypt(base16CipherString);
	}

	/**
	 * 初始化密钥
	 * 
	 * @param keyBytes
	 *            密钥字节串(24 字节)
	 * @param algorithm
	 *            加解密算法
	 * @throws Exception
	 */
	private void initKey(byte[] keyBytes, String algorithm) throws Exception {
		this.key = new SecretKeySpec(keyBytes, algorithm);
		this.algorithm = algorithm;
	}

}
