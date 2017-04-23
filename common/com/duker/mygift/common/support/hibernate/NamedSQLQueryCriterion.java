/*
 * @(#)NamedSQLQueryCriterion.java 2010-1-13
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.support.hibernate;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.Type;

/**
 * <pre>
 * 命名查询约束
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2010-1-13
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class NamedSQLQueryCriterion implements Criterion {

	private final String namedSql;

	private final TypedValue[] typedValues;

	private String alias;

	private String property;

	public NamedSQLQueryCriterion(String namedSql) {
		this.namedSql = namedSql;
		this.typedValues = new TypedValue[0];
	}

	public NamedSQLQueryCriterion(String namedSql, String property) {
		this.namedSql = namedSql;
		this.typedValues = new TypedValue[0];
		this.property = property;
	}

	public NamedSQLQueryCriterion(String namedSql, String alias, String property) {
		this.namedSql = namedSql;
		this.typedValues = new TypedValue[0];
		if (StringUtils.isNotBlank(alias)) {
			this.alias = alias + ".";
		}
		this.property = property;
	}

	public NamedSQLQueryCriterion(String namedSql, Object value, Type type) {
		this.namedSql = namedSql;
		typedValues = new TypedValue[] { new TypedValue(type, value) };
	}

	public NamedSQLQueryCriterion(String namedSql, Object value, Type type,
			String alias) {
		this.namedSql = namedSql;
		typedValues = new TypedValue[] { new TypedValue(type, value) };
		if (StringUtils.isNotBlank(alias)) {
			this.alias = alias + ".";
		}
	}

	public NamedSQLQueryCriterion(String namedSql, Object value, Type type,
			String alias, String property) {
		this.namedSql = namedSql;
		typedValues = new TypedValue[] { new TypedValue(type, value) };
		if (StringUtils.isNotBlank(alias)) {
			this.alias = alias + ".";
		}
		if (StringUtils.isNotBlank(property)) {
			this.property = property.toUpperCase();
		}
	}

	public NamedSQLQueryCriterion(String namedSql, Object[] values, Type[] types) {
		this.namedSql = namedSql;
		typedValues = new TypedValue[values.length];
		for (int i = 0; i < typedValues.length; i++) {
			typedValues[i] = new TypedValue(types[i], values[i]);
		}
	}

	public NamedSQLQueryCriterion(String namedSql, Object[] values,
			Type[] types, String alias) {
		this.namedSql = namedSql;
		typedValues = new TypedValue[values.length];
		for (int i = 0; i < typedValues.length; i++) {
			typedValues[i] = new TypedValue(types[i], values[i]);
		}
		if (StringUtils.isNotBlank(alias)) {
			this.alias = alias + ".";
		}
	}

	public NamedSQLQueryCriterion(String namedSql, Object[] values,
			Type[] types, String alias, String property) {
		this.namedSql = namedSql;
		typedValues = new TypedValue[values.length];
		for (int i = 0; i < typedValues.length; i++) {
			typedValues[i] = new TypedValue(types[i], values[i]);
		}
		if (StringUtils.isNotBlank(alias)) {
			this.alias = alias + ".";
		}
		if (StringUtils.isNotBlank(property)) {
			this.property = property.toUpperCase();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hibernate.criterion.Criterion#toSqlString(org.hibernate.Criteria,
	 * org.hibernate.criterion.CriteriaQuery)
	 */
	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
			throws HibernateException {
		String sql = criteriaQuery.getFactory().getNamedSQLQuery(namedSql)
				.getQueryString();
		if (StringUtils.isBlank(alias)) {
			sql = StringHelper.replace(sql, "{alias}", criteriaQuery
					.getSQLAlias(criteria));
		}
		else {
			sql = StringHelper.replace(sql, "{alias}", criteriaQuery
					.getSQLAlias(criteria, alias));
		}

		if (StringUtils.isNotBlank(property)) {
			sql = StringHelper.replace(sql, "{property}", property);
		}

		return sql;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hibernate.criterion.Criterion#getTypedValues(org.hibernate.Criteria,
	 * org.hibernate.criterion.CriteriaQuery)
	 */
	public TypedValue[] getTypedValues(Criteria criteria,
			CriteriaQuery criteriaQuery) throws HibernateException {
		return typedValues;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return namedSql;
	}
}
