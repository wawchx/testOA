/*
 * @(#)PBECryptoUtil.java 2008-6-27
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.common.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Hex;

/**
 * <pre>
 * PBE���ڿ������Կ�ӽ��ܹ���
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2008-6-27
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class PBECryptoUtil {

	/**
	 * PBE Key
	 */
	private PBEKeySpec pbeKeySpec;

	/**
	 * PBE Parameter
	 */
	private PBEParameterSpec pbeParamSpec;

	/**
	 * Secret Key Factory
	 */
	private SecretKeyFactory keyFactory;

	/**
	 * Secret Key
	 */
	private SecretKey pbeKey;

	/**
	 * Cipher
	 */
	private Cipher pbeCipher;

	/**
	 * PBE��Կ
	 */
	private static String PASSWORD;

	/**
	 * �����㷨
	 */
	private static final String ALGORITHM_PBEWITHMD5andDES = "PBEWITHMD5andDES";

	public PBECryptoUtil() {
		try {
			keyFactory = SecretKeyFactory
					.getInstance(ALGORITHM_PBEWITHMD5andDES);
			pbeCipher = Cipher.getInstance(ALGORITHM_PBEWITHMD5andDES);
		}
		catch (Exception ex) {
		}
	}

	public PBECryptoUtil(char[] password, byte[] salt, int count)
			throws Exception {
		this();
		setParams(password, salt, count);
	}

	/**
	 * ���ü��ܲ���
	 * 
	 * @param password
	 *            ����
	 * @param salt
	 *            ��
	 * @param count
	 *            ��������
	 * @throws Exception
	 */
	public void setParams(char[] password, byte[] salt, int count)
			throws Exception {
		pbeKeySpec = new PBEKeySpec(password);
		pbeParamSpec = new PBEParameterSpec(salt, count);
		pbeKey = keyFactory.generateSecret(pbeKeySpec);
	}

	/**
	 * ����
	 * 
	 * @param datas
	 *            ԭʼ����
	 * @return byte[] ���ܺ��ֽ���
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] datas) throws Exception {
		pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

		return pbeCipher.doFinal(datas);
	}

	/**
	 * ����
	 * 
	 * @param datas
	 *            ԭʼ����
	 * @return byte[] ���ܺ��ֽ���
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] datas) throws Exception {
		pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);

		return pbeCipher.doFinal(datas);
	}

	/**
	 * ת������
	 * 
	 * @param data
	 *            Ҫ��/���ܵ�����
	 * @return String
	 */
	public static String cryptoPassword(String data) {
		byte[] salt = null;
		byte[] bytes = null;

		try {
			if (PASSWORD == null) {
				String userName = (String) System.getProperties().get(
						"user.name");
				String appRoot = getAppPath();

				PASSWORD = "www.51ssm.com";

				if (userName != null) {
					PASSWORD = userName;
				}

				if (appRoot != null) {
					PASSWORD += appRoot;
				}

				PASSWORD = URLEncoder.encode(PASSWORD, "utf-8");
			}

			salt = new byte[] { 0x11, 0x22, 0x4F, 0x58, (byte) 0x88, 0x10,
					0x40, 0x38 };
			PBECryptoUtil util = new PBECryptoUtil();

			util.setParams(PASSWORD.toCharArray(), salt, 2);

			if (data.startsWith("*")) {
				data = data.substring(1);
				bytes = util.decrypt(Hex.decodeHex(data.toCharArray()));
				data = new String(bytes, "utf-8");
			}
			else {
				bytes = util.encrypt(data.getBytes("utf-8"));
				data = "*" + new String(Hex.encodeHex(bytes));
			}
		}
		catch (Exception ex) {
		}

		return data;
	}

	/**
	 * ȡ�ð�װ·��
	 * 
	 * @return String ��װ·��
	 */
	public static String getAppPath() throws Exception {
		String className = PBECryptoUtil.class.getName();
		int index = -1;

		className = className.replace('.', '/') + ".class";
		URL url = Thread.currentThread().getContextClassLoader().getResource(
				className);
		String appPath = URLDecoder.decode(url.getPath(), "utf-8");
		appPath = appPath.replace(File.separatorChar, '/');

		if (url.getProtocol().equals("jar")) {
			// jar���Ŀ¼
			index = appPath.indexOf("jar!/");

			if (index != -1) {
				appPath = appPath.substring(0, index);
			}

			index = appPath.lastIndexOf('/');

			if (index != -1) {
				appPath = appPath.substring(0, index);
			}
		}
		else {
			// classpath��Ŀ¼
			index = appPath.indexOf(className);

			if (index != -1) {
				appPath = appPath.substring(0, index - 1);
			}
		}

		return appPath;
	}

	public static void main(String[] args) {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(System.in));

			while (true) {
				System.out.println("Enter a password to encrypt/decrypt:");
				String str = br.readLine();

				if (str != null) {
					if (str.equals("exit")) {
						break;
					}

					String value = PBECryptoUtil.cryptoPassword(str);

					System.out.println("Password converted:");
					System.out.println(value);
				}
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		finally {
			if (br != null) {
				try {
					br.close();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
