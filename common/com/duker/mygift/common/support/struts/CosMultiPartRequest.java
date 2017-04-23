/*
 * @(#)CosMultiPartRequest.java Feb 14, 2012
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.support.struts;

import java.io.File;
import java.io.IOException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.StrutsConstants;
import org.apache.struts2.dispatcher.multipart.MultiPartRequest;

import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Feb 14, 2012
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class CosMultiPartRequest implements MultiPartRequest {

	private static final Logger LOG = LoggerFactory
			.getLogger(MultiPartRequest.class);

	private static final String UID = new UID().toString().replace(':', '_')
			.replace('-', '_');

	/**
	 * Counter used in unique identifier generation.
	 */
	private static int counter = 0;

	protected Map<String, List<CosFileItem>> files = new HashMap<String, List<CosFileItem>>();

	protected Map<String, List<String>> params = new HashMap<String, List<String>>();

	protected List<String> errors = new LinkedList<String>();

	protected int maxSize;

	@Inject(StrutsConstants.STRUTS_MULTIPART_MAXSIZE)
	public void setMaxSize(String maxSize) {
		long s = Long.parseLong(maxSize);
		if (s > Integer.MAX_VALUE) {
			this.maxSize = Integer.MAX_VALUE;
		}
		else {
			this.maxSize = (int) s;
		}
	}

	/**
	 * Returns an identifier that is unique within the class loader used to load
	 * this class, but does not have random-like apearance.
	 * 
	 * @return A String with the non-random looking instance identifier.
	 */
	private static String getUniqueId() {
		final int limit = 100000000;
		int current;
		synchronized (CosMultiPartRequest.class) {
			current = counter++;
		}
		String id = Integer.toString(current);

		// If you manage to get more than 100 million of ids, you'll
		// start getting ids longer than 8 characters.
		if (current < limit) {
			id = ("00000000" + id).substring(id.length());
		}

		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.struts2.dispatcher.multipart.MultiPartRequest#parse(javax.
	 * servlet.http.HttpServletRequest, java.lang.String)
	 */
	public void parse(HttpServletRequest request, String saveDir)
			throws IOException {
		MultipartParser mp = new MultipartParser(request, this.maxSize);
		String charset = request.getCharacterEncoding();
		mp.setEncoding(charset);
		Part part;
		if (saveDir == null) {
			saveDir = System.getProperty("java.io.tmpdir");
		}
		StringBuilder sb = new StringBuilder();
		while ((part = mp.readNextPart()) != null) {
			if (part.isFile()) {
				LOG.debug("Item is a file upload", new String[0]);
				FilePart filePart = (FilePart) part;
				String fileName = filePart.getFileName();
				String name = filePart.getName();
				// Skip file uploads that don't have a file name - meaning that
				// no file was selected.
				if ((fileName == null) || (fileName.trim().length() < 1)) {
					LOG.debug("No file has been uploaded for the field: "
							+ name);
					continue;
				}

				List<CosFileItem> values = this.files.get(name);
				if (values == null) {
					values = new LinkedList<CosFileItem>();
					this.files.put(name, values);
				}
				sb.setLength(0);
				sb.append("upload_");
				sb.append(UID);
				sb.append("_");
				sb.append(getUniqueId());
				sb.append(".tmp");

				File file = new File(saveDir, sb.toString());
				filePart.writeTo(file);
				CosFileItem fileItem = new CosFileItem();
				fileItem.setFile(file);
				fileItem.setContentType(filePart.getContentType());
				fileItem.setFileName(fileName);
				values.add(fileItem);

				continue;
			}
			if (part.isParam()) {
				LOG.debug("Item is a normal form field");
				ParamPart paramPart = (ParamPart) part;
				String name = paramPart.getName();
				List<String> values = this.params.get(name);
				if (values == null) {
					values = new LinkedList<String>();
					this.params.put(name, values);
				}

				if (charset != null)
					values.add(paramPart.getStringValue(charset));
				else {
					values.add(paramPart.getStringValue());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.apache.struts2.dispatcher.multipart.MultiPartRequest#
	 * getFileParameterNames()
	 */
	public Enumeration<String> getFileParameterNames() {
		return Collections.enumeration(files.keySet());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.struts2.dispatcher.multipart.MultiPartRequest#getContentType
	 * (java.lang.String)
	 */
	public String[] getContentType(String fieldName) {
		List<CosFileItem> items = files.get(fieldName);

		if (items == null) {
			return null;
		}

		List<String> contentTypes = new ArrayList<String>(items.size());
		for (CosFileItem fileItem : items) {
			contentTypes.add(fileItem.getContentType());
		}

		return contentTypes.toArray(new String[contentTypes.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.struts2.dispatcher.multipart.MultiPartRequest#getFile(java
	 * .lang.String)
	 */
	public File[] getFile(String fieldName) {
		List<CosFileItem> items = this.files.get(fieldName);

		if (items == null) {
			return null;
		}

		List<File> fileList = new ArrayList<File>(items.size());
		for (CosFileItem fileItem : items) {
			fileList.add(fileItem.getFile());
		}

		return fileList.toArray(new File[fileList.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.struts2.dispatcher.multipart.MultiPartRequest#getFileNames
	 * (java.lang.String)
	 */
	public String[] getFileNames(String fieldName) {
		List<CosFileItem> items = this.files.get(fieldName);

		if (items == null) {
			return null;
		}

		List<String> fileNames = new ArrayList<String>(items.size());
		for (CosFileItem fileItem : items) {
			fileNames.add(getCanonicalName(fileItem.getFileName()));
		}

		return fileNames.toArray(new String[fileNames.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.struts2.dispatcher.multipart.MultiPartRequest#getFilesystemName
	 * (java.lang.String)
	 */
	public String[] getFilesystemName(String fieldName) {
		List<CosFileItem> items = this.files.get(fieldName);

		if (items == null) {
			return null;
		}

		List<String> fileNames = new ArrayList<String>(items.size());
		for (CosFileItem fileItem : items) {
			fileNames.add(fileItem.getFile().getName());
		}

		return fileNames.toArray(new String[fileNames.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.struts2.dispatcher.multipart.MultiPartRequest#getParameter
	 * (java.lang.String)
	 */
	public String getParameter(String name) {
		List<String> vs = this.params.get(name);
		if (vs != null && vs.size() > 0) {
			return vs.get(0);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.struts2.dispatcher.multipart.MultiPartRequest#getParameterNames
	 * ()
	 */
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(this.params.keySet());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.struts2.dispatcher.multipart.MultiPartRequest#getParameterValues
	 * (java.lang.String)
	 */
	public String[] getParameterValues(String name) {
		List<String> vs = this.params.get(name);
		if (vs != null && vs.size() > 0) {
			return vs.toArray(new String[vs.size()]);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.struts2.dispatcher.multipart.MultiPartRequest#getErrors()
	 */
	public List<String> getErrors() {
		return errors;
	}

	/**
	 * Returns the canonical name of the given file.
	 * 
	 * @param filename
	 *            the given file
	 * @return the canonical name of the given file
	 */
	private String getCanonicalName(String filename) {
		int forwardSlash = filename.lastIndexOf("/");
		int backwardSlash = filename.lastIndexOf("\\");
		if (forwardSlash != -1 && forwardSlash > backwardSlash) {
			filename = filename.substring(forwardSlash + 1, filename.length());
		}
		else if (backwardSlash != -1 && backwardSlash >= forwardSlash) {
			filename = filename.substring(backwardSlash + 1, filename.length());
		}

		return filename;
	}

	public void cleanUp() {
		for (List<CosFileItem> items : files.values()) {
			for (CosFileItem item : items) {
				item.getFile().delete();
			}
		}
	}
}
