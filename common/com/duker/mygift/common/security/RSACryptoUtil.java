/*
 * @(#)RSACryptoUtil.java 2009-9-8
 * 
 * ��Ϣ��˹���ϵͳ
 */
package com.duker.mygift.common.security;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Hex;

/**
 * <pre>
 * RSA �ӽ��ܺ�ǩ�����ߣ����̰߳�ȫ��
 * 
 * @author zhangwei
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
public final class RSACryptoUtil {

	/**
	 * RSA ��Կ
	 */
	private PublicKey publicKey = null;

	/**
	 * RSA ˽Կ
	 */
	private PrivateKey privateKey = null;

	/**
	 * ���� RSA ��Կ�ӽ��ܹ���
	 * 
	 * @param x509EncodedPublicKeyBytes
	 *            X509 ����� RSA ��Կ�ֽڴ�
	 * @param pkcs8EncodedPrivateKeyBytes
	 *            PKCS8 ����� RSA ˽Կ�ֽڴ�
	 * @throws Exception
	 */
	public RSACryptoUtil(byte[] x509EncodedPublicKeyBytes,
			byte[] pkcs8EncodedPrivateKeyBytes) throws Exception {
		this.initKey(x509EncodedPublicKeyBytes, pkcs8EncodedPrivateKeyBytes);
	}

	/**
	 * ���� RSA ��Կ�ӽ��ܹ���
	 * 
	 * @param base16PublicKeyString
	 *            X509 ����� RSA ��Կ�ֽڴ��� BASE 16 �����ַ���
	 * @param base16PrivateKeyString
	 *            PKCS8 ����� RSA ˽Կ�ֽڴ��� BASE 16 �����ַ���
	 * @throws Exception
	 */
	public RSACryptoUtil(String base16PublicKeyString,
			String base16PrivateKeyString) throws Exception {
		byte[] x509EncodedPublicKeyBytes = null;
		if ((base16PublicKeyString != null)
				&& (!base16PublicKeyString.equals(""))) {
			x509EncodedPublicKeyBytes = Hex.decodeHex(base16PublicKeyString
					.toCharArray());
		}

		byte[] pkcs8EncodedPrivateKeyBytes = null;
		if ((base16PrivateKeyString != null)
				&& (!base16PrivateKeyString.equals(""))) {
			pkcs8EncodedPrivateKeyBytes = Hex.decodeHex(base16PrivateKeyString
					.toCharArray());
		}

		this.initKey(x509EncodedPublicKeyBytes, pkcs8EncodedPrivateKeyBytes);
	}

	/**
	 * ʹ��˽Կ��������
	 * 
	 * @param plainBytes
	 *            �����ֽڴ�
	 * @return �����ֽڴ�
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] plainBytes) throws Exception {
		if (privateKey == null) {
			throw new Exception("û������˽Կ");
		}

		Cipher cipher = javax.crypto.Cipher
				.getInstance("RSA/NONE/PKCS1Padding");
		cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, privateKey);
		return cipher.doFinal(plainBytes);
	}

	/**
	 * ʹ��˽Կ���������ַ���
	 * 
	 * @param plainString
	 *            �����ַ���
	 * @return BASE16 ����������ַ���
	 * @throws Exception
	 */
	public String encrypt(String plainString) throws Exception {
		byte[] cipherBytes = this.encrypt(plainString.getBytes());
		return new String(Hex.encodeHex(cipherBytes));
	}

	/**
	 * ʹ�ù�Կ��������
	 * 
	 * @param cipherBytes
	 *            �����ֽڴ�
	 * @return �����ֽڴ�
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] cipherBytes) throws Exception {
		if (publicKey == null) {
			throw new Exception("û�����ù�Կ");
		}
		Cipher cipher = javax.crypto.Cipher
				.getInstance("RSA/NONE/PKCS1Padding");
		cipher.init(javax.crypto.Cipher.DECRYPT_MODE, publicKey);
		return cipher.doFinal(cipherBytes);
	}

	/**
	 * ʹ�ù�Կ���������ַ���
	 * 
	 * @param base16CipherString
	 *            BASE16 ����Ĵ����ܵ������ַ���
	 * @return �����ֽڴ�
	 * @throws Exception
	 */
	public byte[] decrypt(String base16CipherString) throws Exception {
		byte[] cipherBytes = Hex.decodeHex(base16CipherString.toCharArray());
		return this.decrypt(cipherBytes);
	}

	/**
	 * ʹ��˽Կǩ��Դ�����ֽڴ�
	 * 
	 * @param source
	 *            ��ǩ����ԭ���ֽڴ�
	 * @return ǩ���ֽڴ�
	 * @throws Exception
	 */
	public byte[] sign(byte[] source) throws Exception {
		if (privateKey == null) {
			throw new Exception("û������˽Կ");
		}
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initSign(privateKey);
		signature.update(source);
		return signature.sign();
	}

	/**
	 * ʹ��˽Կǩ��Դ�����ַ���
	 * 
	 * @param source
	 *            ��ǩ����ԭ���ַ���
	 * @return BASE16 �����ǩ���ַ���
	 * @throws Exception
	 */
	public String sign(String source) throws Exception {
		byte[] signature = this.sign(source.getBytes());
		return new String(Hex.encodeHex(signature));
	}

	/**
	 * ʹ�ù�Կ��֤ǩ������Ч��
	 * 
	 * @param source
	 *            ԭ���ֽڴ�
	 * @param signatureBytes
	 *            ����֤��ǩ���ֽڴ�
	 * @return true-ǩ����ȷ��false-ǩ������ȷ
	 * @throws Exception
	 */
	public boolean verify(byte[] source, byte[] signatureBytes)
			throws Exception {
		if (publicKey == null) {
			throw new Exception("û�����ù�Կ");
		}
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initVerify(publicKey);
		signature.update(source);
		return signature.verify(signatureBytes);
	}

	/**
	 * ʹ��˽Կ��֤ǩ��
	 * 
	 * @param source
	 *            Դ�ַ���
	 * @param base16SignatureString
	 *            ����֤�� BASE16 �����ǩ���ַ���
	 * @return true-ǩ����ȷ��false-ǩ������ȷ
	 * @throws Exception
	 */
	public boolean verify(String source, String base16SignatureString)
			throws Exception {
		return this.verify(source.getBytes(), Hex
				.decodeHex(base16SignatureString.toCharArray()));
	}

	/**
	 * ʹ��˽Կ��������
	 * 
	 * @param pkcs8EncodedPrivateKeyBytes
	 *            PKCS8 ����� RSA ˽Կ�ֽڴ�
	 * @param plainBytes
	 *            �����ܵ������ֽڴ�
	 * @return �����ֽڴ�
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] pkcs8EncodedPrivateKeyBytes,
			byte[] plainBytes) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				pkcs8EncodedPrivateKeyBytes);
		return rsaCryptoUtil.encrypt(plainBytes);
	}

	/**
	 * ʹ��˽Կ��������
	 * 
	 * @param pkcs8EncodedPrivateKeyBytes
	 *            PKCS8 ����� RSA ˽Կ�ֽڴ�
	 * @param plainString
	 *            �����ܵ������ַ���
	 * @return �����ֽڴ�
	 * @throws Exception
	 */
	public static String encrypt(byte[] pkcs8EncodedPrivateKeyBytes,
			String plainString) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				pkcs8EncodedPrivateKeyBytes);
		return rsaCryptoUtil.encrypt(plainString);
	}

	/**
	 * ʹ��˽Կ���������ַ���
	 * 
	 * @param base16PrivateKeyString
	 *            PKCS8 ����� RSA ˽Կ�ֽڴ��� BASE 16 �����ַ���
	 * @param plainBytes
	 *            �����ܵ������ֽڴ�
	 * @return BASE16 ����������ַ���
	 * @throws Exception
	 */
	public static byte[] encrypt(String base16PrivateKeyString,
			byte[] plainBytes) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				base16PrivateKeyString);
		return rsaCryptoUtil.encrypt(plainBytes);
	}

	/**
	 * ʹ��˽Կ���������ַ���
	 * 
	 * @param base16PrivateKeyString
	 *            PKCS8 ����� RSA ˽Կ�ֽڴ��� BASE 16 �����ַ���
	 * @param plainString
	 *            �����ܵ������ַ���
	 * @return BASE16 ����������ַ���
	 * @throws Exception
	 */
	public static String encrypt(String base16PrivateKeyString,
			String plainString) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				base16PrivateKeyString);
		return rsaCryptoUtil.encrypt(plainString);
	}

	/**
	 * ʹ�ù�Կ���������ֽڴ�
	 * 
	 * @param x509EncodedPublicKeyBytes
	 *            X509 ����� RSA ��Կ�ֽڴ�
	 * @param cipherBytes
	 *            �����ܵ������ֽڴ�
	 * @return �����ֽڴ�
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] x509EncodedPublicKeyBytes,
			byte[] cipherBytes) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(
				x509EncodedPublicKeyBytes, null);
		return rsaCryptoUtil.decrypt(cipherBytes);
	}

	/**
	 * ʹ�ù�Կ���������ַ���
	 * 
	 * @param x509EncodedPublicKeyBytes
	 *            X509 ����� RSA ��Կ�ֽڴ�
	 * @param base16CipherString
	 *            BASE16 ����Ĵ����ܵ������ַ���
	 * @return �����ֽڴ�
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] x509EncodedPublicKeyBytes,
			String base16CipherString) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(
				x509EncodedPublicKeyBytes, null);
		return rsaCryptoUtil.decrypt(base16CipherString);
	}

	/**
	 * ʹ�ù�Կ���������ֽڴ�
	 * 
	 * @param base16PublicKeyString
	 *            X509 ����� RSA ��Կ�ֽڴ��� BASE 16 �����ַ���
	 * @param cipherBytes
	 *            �����ܵ������ֽڴ�
	 * @return �����ֽڴ�
	 * @throws Exception
	 */
	public static byte[] decrypt(String base16PublicKeyString,
			byte[] cipherBytes) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(base16PublicKeyString,
				null);
		return rsaCryptoUtil.decrypt(cipherBytes);
	}

	/**
	 * ʹ�ù�Կ���������ַ���
	 * 
	 * @param base16PublicKeyString
	 *            X509 ����� RSA ��Կ�ֽڴ��� BASE 16 �����ַ���
	 * @param base16CipherString
	 *            BASE16 ����Ĵ����ܵ������ַ���
	 * @return �����ֽڴ�
	 * @throws Exception
	 */
	public static byte[] decrypt(String base16PublicKeyString,
			String base16CipherString) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(base16PublicKeyString,
				null);
		return rsaCryptoUtil.decrypt(base16CipherString);
	}

	/**
	 * ʹ��˽Կǩ��Դ�����ֽڴ�
	 * 
	 * @param pkcs8EncodedPrivateKeyBytes
	 *            PKCS8 ����� RSA ˽Կ�ֽڴ�
	 * @param source
	 *            ��ǩ����ԭ���ֽڴ�
	 * @return ǩ���ֽڴ�
	 * @throws Exception
	 */
	public static byte[] sign(byte[] pkcs8EncodedPrivateKeyBytes, byte[] source)
			throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				pkcs8EncodedPrivateKeyBytes);
		return rsaCryptoUtil.sign(source);
	}

	/**
	 * ʹ��˽Կǩ��Դ�����ַ���
	 * 
	 * @param pkcs8EncodedPrivateKeyBytes
	 *            PKCS8 ����� RSA ˽Կ�ֽڴ�
	 * @param source
	 *            ��ǩ����ԭ���ַ���
	 * @return BASE16 �����ǩ���ַ���
	 * @throws Exception
	 */
	public static String sign(byte[] pkcs8EncodedPrivateKeyBytes, String source)
			throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				pkcs8EncodedPrivateKeyBytes);
		return rsaCryptoUtil.sign(source);
	}

	/**
	 * ʹ��˽Կǩ��Դ�����ֽڴ�
	 * 
	 * @param base16PrivateKeyString
	 *            PKCS8 ����� RSA ˽Կ�ֽڴ��� BASE 16 �����ַ���
	 * @param source
	 *            ��ǩ����ԭ���ֽڴ�
	 * @return ǩ���ֽڴ�
	 * @throws Exception
	 */
	public static byte[] sign(String base16PrivateKeyString, byte[] source)
			throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				base16PrivateKeyString);
		return rsaCryptoUtil.sign(source);
	}

	/**
	 * ʹ��˽Կǩ��Դ�����ַ���
	 * 
	 * @param base16PrivateKeyString
	 *            PKCS8 ����� RSA ˽Կ�ֽڴ��� BASE 16 �����ַ���
	 * @param source
	 *            ��ǩ����ԭ���ַ���
	 * @return BASE16 �����ǩ���ַ���
	 * @throws Exception
	 */
	public static String sign(String base16PrivateKeyString, String source)
			throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				base16PrivateKeyString);
		return rsaCryptoUtil.sign(source);
	}

	/**
	 * ʹ�ù�Կ��֤ǩ������Ч��
	 * 
	 * @param x509EncodedPublicKeyBytes
	 *            X509 ����� RSA ��Կ�ֽڴ�
	 * @param source
	 *            ԭ���ֽڴ�
	 * @param signatureBytes
	 *            ǩ���ֽڴ�
	 * @return true-ǩ����ȷ��false-ǩ������ȷ
	 * @throws Exception
	 */
	public static boolean verify(byte[] x509EncodedPublicKeyBytes,
			byte[] source, byte[] signatureBytes) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(
				x509EncodedPublicKeyBytes, null);
		return rsaCryptoUtil.verify(source, signatureBytes);
	}

	/**
	 * ʹ��˽Կ��֤ǩ��
	 * 
	 * @param x509EncodedPublicKeyBytes
	 *            X509 ����� RSA ��Կ�ֽڴ�
	 * @param source
	 *            ԭ���ַ���
	 * @param base16SignatureString
	 *            ����֤�� BASE16 �����ǩ���ַ���
	 * @return true-ǩ����ȷ��false-ǩ������ȷ
	 * @throws Exception
	 */
	public static boolean verify(byte[] x509EncodedPublicKeyBytes,
			String source, String base16SignatureString) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(
				x509EncodedPublicKeyBytes, null);
		return rsaCryptoUtil.verify(source, base16SignatureString);
	}

	/**
	 * ʹ�ù�Կ��֤ǩ������Ч��
	 * 
	 * @param base16PublicKeyString
	 *            X509 ����� RSA ��Կ�ֽڴ��� BASE 16 �����ַ���
	 * @param source
	 *            ԭ���ֽڴ�
	 * @param signatureBytes
	 *            ǩ���ֽڴ�
	 * @return true-ǩ����ȷ��false-ǩ������ȷ
	 * @throws Exception
	 */
	public static boolean verify(String base16PublicKeyString, byte[] source,
			byte[] signatureBytes) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(base16PublicKeyString,
				null);
		return rsaCryptoUtil.verify(source, signatureBytes);
	}

	/**
	 * ʹ��˽Կ��֤ǩ��
	 * 
	 * @param base16PublicKeyString
	 *            X509 ����� RSA ��Կ�ֽڴ��� BASE 16 �����ַ���
	 * @param source
	 *            ԭ���ַ���
	 * @param base16SignatureString
	 *            ����֤�� BASE16 �����ǩ���ַ���
	 * @return true-ǩ����ȷ��false-ǩ������ȷ
	 * @throws Exception
	 */
	public static boolean verify(String base16PublicKeyString, String source,
			String base16SignatureString) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(base16PublicKeyString,
				null);
		return rsaCryptoUtil.verify(source, base16SignatureString);
	}

	/**
	 * ��ʼ�� RSA ��Կ��˽Կ
	 * 
	 * @param x509EncodedPublicKeyBytes
	 *            X509 ����� RSA ��Կ�ֽڴ�
	 * @param pkcs8EncodedPrivateKeyBytes
	 *            PKCS8 ����� RSA ˽Կ�ֽڴ�
	 * @throws Exception
	 */
	private void initKey(byte[] x509EncodedPublicKeyBytes,
			byte[] pkcs8EncodedPrivateKeyBytes) throws Exception {
		if ((x509EncodedPublicKeyBytes != null)
				&& (x509EncodedPublicKeyBytes.length > 0)) {
			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
					x509EncodedPublicKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
		}

		if ((pkcs8EncodedPrivateKeyBytes != null)
				&& (pkcs8EncodedPrivateKeyBytes.length > 0)) {
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
					pkcs8EncodedPrivateKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
		}
	}
}
