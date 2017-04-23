/*
 * @(#)SortOrder.java Jun 25, 2009
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.vo;

import java.sql.Types;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * <pre>
 * 排序方式
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: Jun 25, 2009
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@SuppressWarnings("serial")
public class SortOrder extends Order {

	private boolean ascending;

	private boolean ignoreCase;

	private String propertyName;

	private String orderByclause;

	private String prefix;

	private boolean nullsLast = true;

	public SortOrder(String propertyName, boolean ascending) {
		super(null, false);
		this.propertyName = propertyName;
		this.ascending = ascending;
	}

	public SortOrder(String orderByclause) {
		super(null, false);
		this.orderByclause = orderByclause;
	}

	public SortOrder ignoreCase() {
		ignoreCase = true;
		return this;
	}

	public SortOrder setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	public SortOrder setNullsLast(boolean nullsLast) {
		this.nullsLast = nullsLast;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (orderByclause != null) {
			if (prefix == null) {
				return orderByclause;
			}

			StringBuilder fragment = new StringBuilder();
			String[] os = orderByclause.split("\\s*,\\s*");
			for (String o : os) {
				fragment.append(prefix);
				fragment.append(o);
				fragment.append(", ");
			}
			int len = fragment.length();
			if (len > 0) {
				fragment.delete(len - 2, len);
			}

			return fragment.toString();
		}

		StringBuilder fragment = new StringBuilder();
		if (prefix != null) {
			fragment.append(prefix);
		}
		fragment.append(propertyName);
		if (!ascending) {
			fragment.append(" desc");
			if (nullsLast) {
				fragment.append(" nulls last");
			}
		}

		return fragment.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernate.criterion.Order#toSqlString(org.hibernate.Criteria,
	 * org.hibernate.criterion.CriteriaQuery)
	 */
	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
			throws HibernateException {
		StringBuilder fragment = new StringBuilder();
		if (orderByclause != null) {
			String[] os = orderByclause.split("\\s*,\\s*");
			for (String o : os) {
				int idx = o.indexOf(' ');
				String propertyName;
				String dir;
				if (idx == -1) {
					propertyName = o;
					dir = "";
				}
				else {
					propertyName = o.substring(0, idx);
					dir = o.substring(idx);
				}
				String[] columns = criteriaQuery.getColumnsUsingProjection(
						criteria, propertyName);
				for (int i = 0; i < columns.length; i++) {
					fragment.append(columns[i]);
					fragment.append(dir);
					fragment.append(", ");
				}
			}
		}
		else {
			String[] columns = criteriaQuery.getColumnsUsingProjection(
					criteria, propertyName);
			Type type = criteriaQuery.getTypeUsingProjection(criteria,
					propertyName);
			SessionFactoryImplementor factory = criteriaQuery.getFactory();
			int[] types = type.sqlTypes(factory);
			for (int i = 0; i < columns.length; i++) {
				boolean lower = ignoreCase && types[i] == Types.VARCHAR;
				if (lower) {
					fragment
							.append(factory.getDialect().getLowercaseFunction());
					fragment.append('(');
				}
				fragment.append(columns[i]);
				if (lower) {
					fragment.append(')');
				}
				if (!ascending) {
					fragment.append(" desc");
					if (nullsLast) {
						fragment.append(" nulls last");
					}
				}
				fragment.append(", ");
			}
		}

		int len = fragment.length();
		if (len > 0) {
			fragment.delete(len - 2, len);
		}

		return fragment.toString();
	}

	public static SortOrder asc(String propertyName) {
		return new SortOrder(propertyName, true);
	}

	public static SortOrder desc(String propertyName) {
		return new SortOrder(propertyName, false);
	}

	public static SortOrder order(String orderByclause) {
		return new SortOrder(orderByclause);
	}

}
