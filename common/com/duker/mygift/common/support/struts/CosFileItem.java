/*
 * @(#)CosFileItem.java Feb 14, 2012
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.common.support.struts;

import java.io.File;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: Feb 14, 2012
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class CosFileItem {

	private File file;

	private String fileName;

	private String contentType;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
