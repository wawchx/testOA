/*
 * @(#)DESedeCryptoUtil.java 2009-9-8
 * 
 * ��Ϣ��˹���ϵͳ
 */
package com.duker.mygift.common.security;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

/**
 * <pre>
 * DES �ӽ��ܹ��ߣ����̰߳�ȫ��
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2009-9-8
 * �޸��� :  zhangwei
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public final class DESedeCryptoUtil {

	private Key key;

	private String algorithm = "DESede";

	/**
	 * ����ӽ��ܹ���ʵ��
	 * 
	 * @param keyBytes
	 *            ��Կ�ֽڴ�(24�ֽ�)
	 * @throws Exception
	 */
	public DESedeCryptoUtil(byte[] keyBytes) throws Exception {
		this(keyBytes, "DESede");
	}

	/**
	 * ����ӽ��ܹ���ʵ��
	 * 
	 * @param keyBytes
	 *            ��Կ�ֽڴ�(24 �ֽ�)
	 * @param algorithm
	 *            �ӽ����㷨
	 * @throws Exception
	 */
	public DESedeCryptoUtil(byte[] keyBytes, String algorithm) throws Exception {
		this.initKey(keyBytes, algorithm);
	}

	/**
	 * ����ӽ��ܹ���ʵ��
	 * 
	 * @param base16KeyString
	 *            BASE16 �������Կ�ַ���
	 * @throws Exception
	 */
	public DESedeCryptoUtil(String base16KeyString) throws Exception {
		this(base16KeyString, "DESede");
	}

	/**
	 * ����ӽ��ܹ���ʵ��
	 * 
	 * @param base16KeyString
	 *            BASE16 �������Կ�ַ���
	 * @param algorithm
	 *            �ӽ����㷨
	 * @throws Exception
	 */
	public DESedeCryptoUtil(String base16KeyString, String algorithm)
			throws Exception {
		byte[] keyBytes = Hex.decodeHex(base16KeyString.toCharArray());
		this.initKey(keyBytes, algorithm);
	}

	/**
	 * ���������ֽڴ�
	 * 
	 * @param plainBytes
	 *            �����ܵ������ֽڴ�
	 * @return ���ܺ�������ֽڴ�
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] plainBytes) throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(plainBytes);
	}

	/**
	 * ���������ַ���
	 * 
	 * @param plainString
	 *            �����ܵ������ֽڴ�
	 * @return ���ܺ�������ֽڴ��� BASE16 �����ַ���
	 * @throws Exception
	 */
	public String encrypt(String plainString) throws Exception {
		byte[] cipherBytes = this.encrypt(plainString.getBytes());
		return new String(Hex.encodeHex(cipherBytes));
	}

	/**
	 * ���������ֽڴ�
	 * 
	 * @param cipherBytes
	 *            �����ܵ������ֽڴ�
	 * @return �����ֽڴ�
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] cipherBytes) throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(cipherBytes);
	}

	/**
	 * ��Կ���������ַ���
	 * 
	 * @param base16CipherString
	 *            �����ܵ������ֽڴ� BASE16 ������ַ���
	 * @return �����ַ���
	 * @throws Exception
	 */
	public byte[] decrypt(String base16CipherString) throws Exception {
		return this.decrypt(Hex.decodeHex(base16CipherString.toCharArray()));
	}

	/**
	 * ���������ֽڴ�
	 * 
	 * @param keyBytes
	 *            DES ��Կ�ֽڴ�(24 �ֽ�)
	 * @param plainBytes
	 *            �����ܵ������ֽڴ�
	 * @return ���ܺ�������ֽڴ�
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] keyBytes, byte[] plainBytes)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(keyBytes);
		return desEDECryptoUtil.encrypt(plainBytes);
	}

	/**
	 * ���������ֽڴ�
	 * 
	 * @param keyBytes
	 *            DES ��Կ�ֽڴ�(24 �ֽ�)
	 * @param plainString
	 *            �����ܵ������ֽڴ�
	 * @return ���ܺ�������ֽڴ�
	 * @throws Exception
	 */
	public static String encrypt(byte[] keyBytes, String plainString)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(keyBytes);
		return desEDECryptoUtil.encrypt(plainString);
	}

	/**
	 * ���������ַ���
	 * 
	 * @param base16KeyString
	 *            BASE16 ����� DES ��Կ�ַ���
	 * @param plainBytes
	 *            �����ܵ������ֽڴ�
	 * @return ���ܺ�������ֽڴ��� BASE16 �����ַ���
	 * @throws Exception
	 */
	public static byte[] encrypt(String base16KeyString, byte[] plainBytes)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(
				base16KeyString);
		return desEDECryptoUtil.encrypt(plainBytes);
	}

	/**
	 * ���������ַ���
	 * 
	 * @param base16KeyString
	 *            BASE16 ����� DES ��Կ�ַ���
	 * @param plainString
	 *            �����ܵ������ֽڴ�
	 * @return ���ܺ�������ֽڴ��� BASE16 �����ַ���
	 * @throws Exception
	 */
	public static String encrypt(String base16KeyString, String plainString)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(
				base16KeyString);
		return desEDECryptoUtil.encrypt(plainString);
	}

	/**
	 * ���������ֽڴ�
	 * 
	 * @param keyBytes
	 *            DES ��Կ�ֽڴ�(24 �ֽ�)
	 * @param cipherBytes
	 *            �����ܵ������ֽڴ�
	 * @return �����ֽڴ�
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] keyBytes, byte[] cipherBytes)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(keyBytes);
		return desEDECryptoUtil.decrypt(cipherBytes);
	}

	/**
	 * ���������ֽڴ�
	 * 
	 * @param keyBytes
	 *            DES ��Կ�ֽڴ�(24 �ֽ�)
	 * @param base16CipherString
	 *            �����ܵ������ֽڴ� BASE16 ������ַ���
	 * @return �����ֽڴ�
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] keyBytes, String base16CipherString)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(keyBytes);
		return desEDECryptoUtil.decrypt(base16CipherString);
	}

	/**
	 * ���������ַ���
	 * 
	 * @param base16KeyString
	 *            BASE16 ����� DES ��Կ�ַ���
	 * @param cipherBytes
	 *            �����ܵ������ֽڴ�
	 * @return �����ַ���
	 * @throws Exception
	 */
	public static byte[] decrypt(String base16KeyString, byte[] cipherBytes)
			throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(
				base16KeyString);
		return desEDECryptoUtil.decrypt(cipherBytes);
	}

	/**
	 * ���������ַ���
	 * 
	 * @param base16KeyString
	 *            BASE16 ����� DES ��Կ�ַ���
	 * @param base16CipherString
	 *            �����ܵ������ֽڴ� BASE16 ������ַ���
	 * @return �����ַ���
	 * @throws Exception
	 */
	public static byte[] decrypt(String base16KeyString,
			String base16CipherString) throws Exception {
		DESedeCryptoUtil desEDECryptoUtil = new DESedeCryptoUtil(
				base16KeyString);
		return desEDECryptoUtil.decrypt(base16CipherString);
	}

	/**
	 * ��ʼ����Կ
	 * 
	 * @param keyBytes
	 *            ��Կ�ֽڴ�(24 �ֽ�)
	 * @param algorithm
	 *            �ӽ����㷨
	 * @throws Exception
	 */
	private void initKey(byte[] keyBytes, String algorithm) throws Exception {
		this.key = new SecretKeySpec(keyBytes, algorithm);
		this.algorithm = algorithm;
	}

}
