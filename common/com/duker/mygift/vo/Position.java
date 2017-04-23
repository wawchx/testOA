/*
 * @(#)Position.java 2008-11-27
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.vo;

/**
 * 
 * <pre>
 * 页面跳转位置
 * 
 * @author caocs
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2008-11-27
 * 修改人 :  caocs
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class Position {

	/**
	 * url类型0:url地址 1:JavaScript代码
	 */
	private int type;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 跳转地址
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
