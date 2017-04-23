/*
 * @(#)DateConverter.java 2008-6-16
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.struts.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import ognl.DefaultTypeConverter;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.XWorkException;

/**
 * <pre>
 * 仿照<code>XWorkBasicConverter</code>写的一个日期类型转换类,
 * 实现<code>java.lang.String</code>与<code>java.util.Date</code>相互转换,
 * 	dates - uses the SHORT or RFC3339 format (<code>yyyy-MM-dd'T'HH:mm:ss</code>) or custom format(<code>yyyyMMddHHmmss</code>) for the Locale associated with the current request.
 * 	其中Date转换为String的时候只能转换为custom format(<code>yyyyMMddHHmmss</code>.)
 * 
 * @author wangzh
 * 
 * @version
 * 
 * 修改版本: 1.0
 * 修改日期: 2008-6-16
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DateConverter extends DefaultTypeConverter {

	private static final String MILLISECOND_FORMAT = ".SSS";

	/*
	 * (non-Javadoc)
	 * 
	 * @see ognl.DefaultTypeConverter#convertValue(java.util.Map,
	 *      java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object convertValue(Map context, Object value, Class toType) {
		Object result = null;
		if (value == null || toType.isAssignableFrom(value.getClass())) {
			// no need to convert at all, right?
			return value;
		}
		if (toType == String.class) {
			result = doConvertToString(context, value);
		}
		else if (Date.class.isAssignableFrom(toType)) {
			result = doConvertToDate(context, value, toType);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ognl.DefaultTypeConverter#convertValue(java.util.Map,
	 *      java.lang.Object, java.lang.reflect.Member, java.lang.String,
	 *      java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object convertValue(Map context, Object target, Member member,
			String propertyName, Object value, Class toType) {
		return convertValue(context, value, toType);
	}

	protected Object doConvertToDate(Map context, Object value, Class toType) {
		if (value == null) {
			return null;
		}

		Date result = null;

		if (value instanceof String && ((String) value).length() > 0) {
			String sa = (String) value;
			Locale locale = getLocale(context);

			DateFormat df = null;
			if (java.sql.Time.class == toType) {
				df = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
			}
			else if (java.sql.Timestamp.class == toType) {
				Date check = null;
				SimpleDateFormat dtfmt = (SimpleDateFormat) DateFormat
						.getDateTimeInstance(DateFormat.SHORT,
								DateFormat.MEDIUM, locale);
				SimpleDateFormat fullfmt = new SimpleDateFormat(dtfmt
						.toPattern()
						+ MILLISECOND_FORMAT, locale);

				SimpleDateFormat dfmt = (SimpleDateFormat) DateFormat
						.getDateInstance(DateFormat.SHORT, locale);

				SimpleDateFormat dfCustom = new SimpleDateFormat(
						"yyyyMMddHHmmss");

				SimpleDateFormat[] fmts = { fullfmt, dtfmt, dfmt, dfCustom };
				for (int i = 0; i < fmts.length; i++) {
					try {
						check = fmts[i].parse(sa);
						df = fmts[i];
						if (check != null) {
							break;
						}
					}
					catch (ParseException ignore) {
					}
				}
			}
			else if (java.util.Date.class == toType) {
				Date check = null;
				SimpleDateFormat d1 = (SimpleDateFormat) DateFormat
						.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG,
								locale);
				SimpleDateFormat d2 = (SimpleDateFormat) DateFormat
						.getDateTimeInstance(DateFormat.SHORT,
								DateFormat.MEDIUM, locale);
				SimpleDateFormat d3 = (SimpleDateFormat) DateFormat
						.getDateTimeInstance(DateFormat.SHORT,
								DateFormat.SHORT, locale);
				SimpleDateFormat rfc3399 = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss");
				SimpleDateFormat dfCustom = new SimpleDateFormat(
						"yyyyMMddHHmmss");
				SimpleDateFormat d4 = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat d5 = new SimpleDateFormat("yyyy-MM");
				// added RFC 3339 date format (XW-473), custom date format
				SimpleDateFormat[] dfs = { d1, d2, d3, rfc3399, dfCustom, d4,
						d5 };
				for (int i = 0; i < dfs.length; i++) {
					try {
						check = dfs[i].parse(sa);
						df = dfs[i];
						if (check != null) {
							break;
						}
					}
					catch (ParseException ignore) {
					}
				}
			}
			// final fallback for dates without time
			if (df == null) {
				df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			}
			try {
				// let's use strict parsing (XW-341)
				df.setLenient(false);
				result = df.parse(sa);
				if (Date.class != toType) {
					try {
						Constructor constructor = toType
								.getConstructor(new Class[] { long.class });
						return constructor.newInstance(new Object[] { result
								.getTime() });
					}
					catch (Exception e) {
						throw new XWorkException("Couldn't create class "
								+ toType + " using default (long) constructor",
								e);
					}
				}
			}
			catch (ParseException e) {
				throw new XWorkException("Could not parse date", e);
			}
		}
		else if (value.getClass().isArray()) {
			if (Array.getLength(value) == 1) {
				return doConvertToDate(context, Array.get(value, 0), toType);
			}
		}
		else if (Date.class.isAssignableFrom(value.getClass())) {
			result = (Date) value;
		}
		return result;
	}

	protected String doConvertToString(Map context, Object value) {
		String result = null;
		if (value instanceof Date) {
			DateFormat df = null;
			if (value instanceof java.sql.Time) {
				df = DateFormat.getTimeInstance(DateFormat.MEDIUM,
						getLocale(context));
			}
			else {
				df = new SimpleDateFormat("yyyyMMddHHmmss");
			}
			result = df.format(value);
		}
		return result;
	}

	private Locale getLocale(Map context) {
		if (context == null) {
			return Locale.getDefault();
		}

		Locale locale = (Locale) context.get(ActionContext.LOCALE);

		if (locale == null) {
			locale = Locale.getDefault();
		}

		return locale;
	}
}
