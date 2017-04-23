/*
 * @(#)Position.java 2008-11-27
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.vo;

/**
 * 
 * <pre>
 * ҳ����תλ��
 * 
 * @author caocs
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2008-11-27
 * �޸��� :  caocs
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class Position {

	/**
	 * url����0:url��ַ 1:JavaScript����
	 */
	private int type;

	/**
	 * ����
	 */
	private String name;

	/**
	 * ��ת��ַ
	 */
	private String url;

	public Position() {
	}

	public Position(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public Position(int type, String name, String url) {
		super();
		this.type = type;
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
