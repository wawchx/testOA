/*
 * @(#)CriteriaProperty.java 2010-3-25
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.vo;

import java.io.Serializable;

import org.hibernate.FetchMode;
import org.hibernate.sql.JoinType;

/**
 * <pre>
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2010-3-25
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class CriteriaProperty implements Serializable {

	/**
	 * 序列化版本号
	 */
	private static final long serialVersionUID = -3743172192377283486L;

	/**
	 * 点分法表示的属性名
	 */
	private String prop;

	/**
	 * 别名
	 */
	private String alias;

	/**
	 * <pre>
	 * 连接类型,取值范围JoinType.NONE/JoinType.INNER_JOIN/JoinType.LEFT_OUTER_JOIN/JoinType.RIGHT_OUTER_JOIN/JoinType.FULL_JOIN
	 * </pre>
	 */
	private JoinType joinType;

	/**
	 * <pre>
	 * 抓取模式,取值范围FetchMode.DEFAULT/FetchMode.JOIN/FetchMode.SELECT
	 * </pre>
	 */
	private FetchMode fetchMode;

	public CriteriaProperty(String prop, FetchMode fetchMode) {
		this.prop = prop;
		this.fetchMode = fetchMode;
	}

	public CriteriaProperty(String prop, String alias) {
		this.prop = prop;
		this.alias = alias;
	}

	public CriteriaProperty(String prop, String alias, JoinType joinType) {
		this.prop = prop;
		this.alias = alias;
		this.joinType = joinType;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public FetchMode getFetchMode() {
		return fetchMode;
	}

	public void setFetchMode(FetchMode fetchMode) {
		this.fetchMode = fetchMode;
	}

	public JoinType getJoinType() {
		return joinType;
	}

	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}

	public String getProp() {
		return prop;
	}

	public void setProp(String prop) {
		this.prop = prop;
	}

}
