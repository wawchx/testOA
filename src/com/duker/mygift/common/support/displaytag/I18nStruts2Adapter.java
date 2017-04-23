/*
 * @(#)I18nStruts2Adapter.java 2009-7-16
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.support.displaytag;

import java.util.Iterator;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.displaytag.Messages;
import org.displaytag.localization.I18nResourceProvider;
import org.displaytag.localization.I18nWebworkAdapter;
import org.displaytag.localization.LocaleResolver;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * <pre>
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2009-7-16
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class I18nStruts2Adapter implements I18nResourceProvider, LocaleResolver {

	/**
	 * prefix/suffix for missing entries.
	 */
	public static final String UNDEFINED_KEY = "???"; //$NON-NLS-1$

	/**
	 * logger.
	 */
	private static Log log = LogFactory.getLog(I18nWebworkAdapter.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.displaytag.localization.LocaleResolver#resolveLocale(javax.servlet.http.HttpServletRequest)
	 */
	public Locale resolveLocale(HttpServletRequest request) {
		return ActionContext.getContext().getLocale();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.displaytag.localization.I18nResourceProvider#getResource(java.lang.String,
	 *      java.lang.String, javax.servlet.jsp.tagext.Tag,
	 *      javax.servlet.jsp.PageContext)
	 */
	public String getResource(String resourceKey, String defaultValue, Tag tag,
			PageContext context) {
		// if resourceKey isn't defined either, use defaultValue
		String key = (resourceKey != null) ? resourceKey : defaultValue;

		String message = null;
		ValueStack stack = ActionContext.getContext().getValueStack();
		Iterator<?> iterator = stack.getRoot().iterator();

		while (iterator.hasNext()) {
			Object o = iterator.next();

			if (o instanceof TextProvider) {
				TextProvider tp = (TextProvider) o;
				message = tp.getText(key, key);

				break;
			}
		}

		// if user explicitely added a titleKey we guess this is an error
		if (message == null && resourceKey != null) {
			log.debug(Messages
					.getString("Localization.missingkey", resourceKey)); //$NON-NLS-1$
			message = UNDEFINED_KEY + resourceKey + UNDEFINED_KEY;
		}

		return message;
	}

}
