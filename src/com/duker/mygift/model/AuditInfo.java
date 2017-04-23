package com.duker.mygift.model;

import java.util.Date;

public class AuditInfo {

	// 审核编号
	private Integer auditId;

	// 审核文件名
	private String name;

	// 文件路径
	private String attachment;

	// 审核状态
	private Integer status;

	// 创建时间
	private Date createtime;

	// 备注信息
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
