/*
 * @(#)StreamInterceptor.java Jul 1, 2011
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.struts.interceptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Jul 1, 2011
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class StreamInterceptor extends AbstractInterceptor {

	/**
	 * 序列化版本号
	 */
	private static final long serialVersionUID = -4061302694419936140L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.xwork2.interceptor.AbstractInterceptor#intercept(com
	 * .opensymphony.xwork2.ActionInvocation)
	 */
	public String intercept(ActionInvocation invocation) throws Exception {
		ActionContext ac = invocation.getInvocationContext();
		HttpServletRequest request = (HttpServletRequest) ac
				.get(ServletActionContext.HTTP_REQUEST);
		String contentType = request.getContentType();
		InputStream is = null;
		try {
			if (contentType != null
					&& contentType.indexOf("application/octet-stream") != -1) {
				// String fileName = request.getHeader("X-File-Name");
				is = request.getInputStream();
				Map<String, Object> params = ac.getParameters();
				// params.put("fileName", fileName);
				params.put("stream", is);
			}
			else if (request instanceof MultiPartRequestWrapper) {
				MultiPartRequestWrapper multiWrapper = (MultiPartRequestWrapper) request;
				File[] files = multiWrapper.getFiles("file");
				if (files != null && files.length > 0) {
					is = new FileInputStream(files[0]);
					String[] fileNames = multiWrapper.getFileNames("file");
					Map<String, Object> params = ac.getParameters();
					params.put("fileName", fileNames[0]);
					params.put("stream", is);
				}
			}

			return invocation.invoke();
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (Exception e) {
				}
			}
		}
	}
}
