/*
 * @(#)CriteriaProperty.java 2010-3-25
 * 
 * ��Ϣ��˹���ϵͳ
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
 * �޸İ汾: 0.9
 * �޸�����: 2010-3-25
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class CriteriaProperty implements Serializable {

	/**
	 * ���л��汾��
	 */
	private static final long serialVersionUID = -3743172192377283486L;

	/**
	 * ��ַ���ʾ��������
	 */
	private String prop;

	/**
	 * ����
	 */
	private String alias;

	/**
	 * <pre>
	 * ��������,ȡֵ��ΧJoinType.NONE/JoinType.INNER_JOIN/JoinType.LEFT_OUTER_JOIN/JoinType.RIGHT_OUTER_JOIN/JoinType.FULL_JOIN
	 * </pre>
	 */
	private JoinType joinType;

	/**
	 * <pre>
	 * ץȡģʽ,ȡֵ��ΧFetchMode.DEFAULT/FetchMode.JOIN/FetchMode.SELECT
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
