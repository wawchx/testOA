/*
 * @(#)RSACryptoUtil.java 2009-9-8
 * 
 * 信息审核管理系统
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
 * RSA 加解密和签名工具（多线程安全）
 * 
 * @author zhangwei
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
public final class RSACryptoUtil {

	/**
	 * RSA 公钥
	 */
	private PublicKey publicKey = null;

	/**
	 * RSA 私钥
	 */
	private PrivateKey privateKey = null;

	/**
	 * 构造 RSA 密钥加解密工具
	 * 
	 * @param x509EncodedPublicKeyBytes
	 *            X509 编码的 RSA 公钥字节串
	 * @param pkcs8EncodedPrivateKeyBytes
	 *            PKCS8 编码的 RSA 私钥字节串
	 * @throws Exception
	 */
	public RSACryptoUtil(byte[] x509EncodedPublicKeyBytes,
			byte[] pkcs8EncodedPrivateKeyBytes) throws Exception {
		this.initKey(x509EncodedPublicKeyBytes, pkcs8EncodedPrivateKeyBytes);
	}

	/**
	 * 构造 RSA 密钥加解密工具
	 * 
	 * @param base16PublicKeyString
	 *            X509 编码的 RSA 公钥字节串的 BASE 16 编码字符串
	 * @param base16PrivateKeyString
	 *            PKCS8 编码的 RSA 私钥字节串的 BASE 16 编码字符串
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
	 * 使用私钥加密数据
	 * 
	 * @param plainBytes
	 *            明文字节串
	 * @return 密文字节串
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] plainBytes) throws Exception {
		if (privateKey == null) {
			throw new Exception("没有配置私钥");
		}

		Cipher cipher = javax.crypto.Cipher
				.getInstance("RSA/NONE/PKCS1Padding");
		cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, privateKey);
		return cipher.doFinal(plainBytes);
	}

	/**
	 * 使用私钥加密明文字符串
	 * 
	 * @param plainString
	 *            明文字符串
	 * @return BASE16 编码的密文字符串
	 * @throws Exception
	 */
	public String encrypt(String plainString) throws Exception {
		byte[] cipherBytes = this.encrypt(plainString.getBytes());
		return new String(Hex.encodeHex(cipherBytes));
	}

	/**
	 * 使用公钥解密数据
	 * 
	 * @param cipherBytes
	 *            密文字节串
	 * @return 明文字节串
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] cipherBytes) throws Exception {
		if (publicKey == null) {
			throw new Exception("没有配置公钥");
		}
		Cipher cipher = javax.crypto.Cipher
				.getInstance("RSA/NONE/PKCS1Padding");
		cipher.init(javax.crypto.Cipher.DECRYPT_MODE, publicKey);
		return cipher.doFinal(cipherBytes);
	}

	/**
	 * 使用公钥解密密文字符串
	 * 
	 * @param base16CipherString
	 *            BASE16 编码的待解密的密文字符串
	 * @return 明文字节串
	 * @throws Exception
	 */
	public byte[] decrypt(String base16CipherString) throws Exception {
		byte[] cipherBytes = Hex.decodeHex(base16CipherString.toCharArray());
		return this.decrypt(cipherBytes);
	}

	/**
	 * 使用私钥签名源数据字节串
	 * 
	 * @param source
	 *            待签名的原文字节串
	 * @return 签名字节串
	 * @throws Exception
	 */
	public byte[] sign(byte[] source) throws Exception {
		if (privateKey == null) {
			throw new Exception("没有配置私钥");
		}
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initSign(privateKey);
		signature.update(source);
		return signature.sign();
	}

	/**
	 * 使用私钥签名源数据字符串
	 * 
	 * @param source
	 *            待签名的原文字符串
	 * @return BASE16 编码的签名字符串
	 * @throws Exception
	 */
	public String sign(String source) throws Exception {
		byte[] signature = this.sign(source.getBytes());
		return new String(Hex.encodeHex(signature));
	}

	/**
	 * 使用公钥验证签名的有效性
	 * 
	 * @param source
	 *            原文字节串
	 * @param signatureBytes
	 *            待验证的签名字节串
	 * @return true-签名正确，false-签名不正确
	 * @throws Exception
	 */
	public boolean verify(byte[] source, byte[] signatureBytes)
			throws Exception {
		if (publicKey == null) {
			throw new Exception("没有配置公钥");
		}
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initVerify(publicKey);
		signature.update(source);
		return signature.verify(signatureBytes);
	}

	/**
	 * 使用私钥验证签名
	 * 
	 * @param source
	 *            源字符串
	 * @param base16SignatureString
	 *            待验证的 BASE16 编码的签名字符串
	 * @return true-签名正确，false-签名不正确
	 * @throws Exception
	 */
	public boolean verify(String source, String base16SignatureString)
			throws Exception {
		return this.verify(source.getBytes(), Hex
				.decodeHex(base16SignatureString.toCharArray()));
	}

	/**
	 * 使用私钥加密数据
	 * 
	 * @param pkcs8EncodedPrivateKeyBytes
	 *            PKCS8 编码的 RSA 私钥字节串
	 * @param plainBytes
	 *            待加密的明文字节串
	 * @return 密文字节串
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] pkcs8EncodedPrivateKeyBytes,
			byte[] plainBytes) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				pkcs8EncodedPrivateKeyBytes);
		return rsaCryptoUtil.encrypt(plainBytes);
	}

	/**
	 * 使用私钥加密数据
	 * 
	 * @param pkcs8EncodedPrivateKeyBytes
	 *            PKCS8 编码的 RSA 私钥字节串
	 * @param plainString
	 *            待加密的明文字符串
	 * @return 密文字节串
	 * @throws Exception
	 */
	public static String encrypt(byte[] pkcs8EncodedPrivateKeyBytes,
			String plainString) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				pkcs8EncodedPrivateKeyBytes);
		return rsaCryptoUtil.encrypt(plainString);
	}

	/**
	 * 使用私钥加密明文字符串
	 * 
	 * @param base16PrivateKeyString
	 *            PKCS8 编码的 RSA 私钥字节串的 BASE 16 编码字符串
	 * @param plainBytes
	 *            待加密的明文字节串
	 * @return BASE16 编码的密文字符串
	 * @throws Exception
	 */
	public static byte[] encrypt(String base16PrivateKeyString,
			byte[] plainBytes) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				base16PrivateKeyString);
		return rsaCryptoUtil.encrypt(plainBytes);
	}

	/**
	 * 使用私钥加密明文字符串
	 * 
	 * @param base16PrivateKeyString
	 *            PKCS8 编码的 RSA 私钥字节串的 BASE 16 编码字符串
	 * @param plainString
	 *            待加密的明文字符串
	 * @return BASE16 编码的密文字符串
	 * @throws Exception
	 */
	public static String encrypt(String base16PrivateKeyString,
			String plainString) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				base16PrivateKeyString);
		return rsaCryptoUtil.encrypt(plainString);
	}

	/**
	 * 使用公钥解密密文字节串
	 * 
	 * @param x509EncodedPublicKeyBytes
	 *            X509 编码的 RSA 公钥字节串
	 * @param cipherBytes
	 *            待解密的密文字节串
	 * @return 明文字节串
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] x509EncodedPublicKeyBytes,
			byte[] cipherBytes) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(
				x509EncodedPublicKeyBytes, null);
		return rsaCryptoUtil.decrypt(cipherBytes);
	}

	/**
	 * 使用公钥解密密文字符串
	 * 
	 * @param x509EncodedPublicKeyBytes
	 *            X509 编码的 RSA 公钥字节串
	 * @param base16CipherString
	 *            BASE16 编码的待解密的密文字符串
	 * @return 明文字节串
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] x509EncodedPublicKeyBytes,
			String base16CipherString) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(
				x509EncodedPublicKeyBytes, null);
		return rsaCryptoUtil.decrypt(base16CipherString);
	}

	/**
	 * 使用公钥解密密文字节串
	 * 
	 * @param base16PublicKeyString
	 *            X509 编码的 RSA 公钥字节串的 BASE 16 编码字符串
	 * @param cipherBytes
	 *            待解密的密文字节串
	 * @return 明文字节串
	 * @throws Exception
	 */
	public static byte[] decrypt(String base16PublicKeyString,
			byte[] cipherBytes) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(base16PublicKeyString,
				null);
		return rsaCryptoUtil.decrypt(cipherBytes);
	}

	/**
	 * 使用公钥解密密文字符串
	 * 
	 * @param base16PublicKeyString
	 *            X509 编码的 RSA 公钥字节串的 BASE 16 编码字符串
	 * @param base16CipherString
	 *            BASE16 编码的待解密的密文字符串
	 * @return 明文字节串
	 * @throws Exception
	 */
	public static byte[] decrypt(String base16PublicKeyString,
			String base16CipherString) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(base16PublicKeyString,
				null);
		return rsaCryptoUtil.decrypt(base16CipherString);
	}

	/**
	 * 使用私钥签名源数据字节串
	 * 
	 * @param pkcs8EncodedPrivateKeyBytes
	 *            PKCS8 编码的 RSA 私钥字节串
	 * @param source
	 *            待签名的原文字节串
	 * @return 签名字节串
	 * @throws Exception
	 */
	public static byte[] sign(byte[] pkcs8EncodedPrivateKeyBytes, byte[] source)
			throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				pkcs8EncodedPrivateKeyBytes);
		return rsaCryptoUtil.sign(source);
	}

	/**
	 * 使用私钥签名源数据字符串
	 * 
	 * @param pkcs8EncodedPrivateKeyBytes
	 *            PKCS8 编码的 RSA 私钥字节串
	 * @param source
	 *            待签名的原文字符串
	 * @return BASE16 编码的签名字符串
	 * @throws Exception
	 */
	public static String sign(byte[] pkcs8EncodedPrivateKeyBytes, String source)
			throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				pkcs8EncodedPrivateKeyBytes);
		return rsaCryptoUtil.sign(source);
	}

	/**
	 * 使用私钥签名源数据字节串
	 * 
	 * @param base16PrivateKeyString
	 *            PKCS8 编码的 RSA 私钥字节串的 BASE 16 编码字符串
	 * @param source
	 *            待签名的原文字节串
	 * @return 签名字节串
	 * @throws Exception
	 */
	public static byte[] sign(String base16PrivateKeyString, byte[] source)
			throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				base16PrivateKeyString);
		return rsaCryptoUtil.sign(source);
	}

	/**
	 * 使用私钥签名源数据字符串
	 * 
	 * @param base16PrivateKeyString
	 *            PKCS8 编码的 RSA 私钥字节串的 BASE 16 编码字符串
	 * @param source
	 *            待签名的原文字符串
	 * @return BASE16 编码的签名字符串
	 * @throws Exception
	 */
	public static String sign(String base16PrivateKeyString, String source)
			throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(null,
				base16PrivateKeyString);
		return rsaCryptoUtil.sign(source);
	}

	/**
	 * 使用公钥验证签名的有效性
	 * 
	 * @param x509EncodedPublicKeyBytes
	 *            X509 编码的 RSA 公钥字节串
	 * @param source
	 *            原文字节串
	 * @param signatureBytes
	 *            签名字节串
	 * @return true-签名正确，false-签名不正确
	 * @throws Exception
	 */
	public static boolean verify(byte[] x509EncodedPublicKeyBytes,
			byte[] source, byte[] signatureBytes) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(
				x509EncodedPublicKeyBytes, null);
		return rsaCryptoUtil.verify(source, signatureBytes);
	}

	/**
	 * 使用私钥验证签名
	 * 
	 * @param x509EncodedPublicKeyBytes
	 *            X509 编码的 RSA 公钥字节串
	 * @param source
	 *            原文字符串
	 * @param base16SignatureString
	 *            待验证的 BASE16 编码的签名字符串
	 * @return true-签名正确，false-签名不正确
	 * @throws Exception
	 */
	public static boolean verify(byte[] x509EncodedPublicKeyBytes,
			String source, String base16SignatureString) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(
				x509EncodedPublicKeyBytes, null);
		return rsaCryptoUtil.verify(source, base16SignatureString);
	}

	/**
	 * 使用公钥验证签名的有效性
	 * 
	 * @param base16PublicKeyString
	 *            X509 编码的 RSA 公钥字节串的 BASE 16 编码字符串
	 * @param source
	 *            原文字节串
	 * @param signatureBytes
	 *            签名字节串
	 * @return true-签名正确，false-签名不正确
	 * @throws Exception
	 */
	public static boolean verify(String base16PublicKeyString, byte[] source,
			byte[] signatureBytes) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(base16PublicKeyString,
				null);
		return rsaCryptoUtil.verify(source, signatureBytes);
	}

	/**
	 * 使用私钥验证签名
	 * 
	 * @param base16PublicKeyString
	 *            X509 编码的 RSA 公钥字节串的 BASE 16 编码字符串
	 * @param source
	 *            原文字符串
	 * @param base16SignatureString
	 *            待验证的 BASE16 编码的签名字符串
	 * @return true-签名正确，false-签名不正确
	 * @throws Exception
	 */
	public static boolean verify(String base16PublicKeyString, String source,
			String base16SignatureString) throws Exception {
		RSACryptoUtil rsaCryptoUtil = new RSACryptoUtil(base16PublicKeyString,
				null);
		return rsaCryptoUtil.verify(source, base16SignatureString);
	}

	/**
	 * 初始化 RSA 公钥和私钥
	 * 
	 * @param x509EncodedPublicKeyBytes
	 *            X509 编码的 RSA 公钥字节串
	 * @param pkcs8EncodedPrivateKeyBytes
	 *            PKCS8 编码的 RSA 私钥字节串
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
