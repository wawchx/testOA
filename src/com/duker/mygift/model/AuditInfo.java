package com.duker.mygift.model;

import java.util.Date;

public class AuditInfo {

	// ��˱��
	private Integer auditId;

	// ����ļ���
	private String name;

	// �ļ�·��
	private String attachment;

	// ���״̬
	private Integer status;

	// ����ʱ��
	private Date createtime;

	// ��ע��Ϣ
	private String des;

	public Integer getAuditId() {
		return auditId;
	}

	public void setAuditId(Integer auditId) {
		this.auditId = auditId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

}
