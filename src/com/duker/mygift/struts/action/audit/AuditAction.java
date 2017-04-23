package com.duker.mygift.struts.action.audit;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.duker.mygift.constant.DList;
import com.duker.mygift.model.AuditInfo;
import com.duker.mygift.service.audit.AuditService;
import com.duker.mygift.struts.action.PaginatedAction;
import com.duker.mygift.vo.PagedList;
import com.duker.mygift.vo.SortOrder;

/**
 * 仪器故障操作信息
 */
public class AuditAction extends PaginatedAction {

	// 审核服务类
	private AuditService auditService;

	// 审核信息
	private AuditInfo audit;

	// 审核编号
	private Integer auditId;

	private File file;

	// 资源名称
	private String fileFileName;

	// 名称
	private String name;

	// 仪器状态
	private Integer status;

	/**
	 * 添加上传审核文件信息
	 * 
	 */
	public String addAudit() throws Exception {
		auditService.addAudit(file, fileFileName, audit);

		return SUCCESS;
	}

	/**
	 * 编辑人员获取审核信息列表
	 * 
	 * @return 跳转控制值
	 */
	public String findEditList() throws Exception {
		status = DList.AuditInfo.status.going;
		findAuditInfoList();

		return SUCCESS;
	}

	/**
	 * 部门人员获取审核信息列表
	 * 
	 * @return 跳转控制值
	 */
	public String findDepartList() throws Exception {
		status = DList.AuditInfo.status.going;
		findAuditInfoList();

		return SUCCESS;
	}

	/**
	 * 管理人员获取审核信息列表
	 * 
	 * @return 跳转控制值
	 */
	public String findManagerList() throws Exception {
		status = DList.AuditInfo.status.goingthrough;
		findAuditInfoList();

		return SUCCESS;
	}

	/**
	 * 超级管理员获取审核信息列表
	 * 
	 * @return 跳转控制值
	 */
	public String findAdminList() throws Exception {
		status = DList.AuditInfo.status.through;
		findAuditInfoList();

		return SUCCESS;
	}

	public String findAuditInfoList() throws Exception {
		List<SortOrder> orders = new LinkedList<SortOrder>();
		if (StringUtils.isBlank(sort)) {
			orders.add(SortOrder.asc("auditId"));
		}
		else {
			orders.add(new SortOrder(sort, asc));
		}
		PagedList<AuditInfo> pList = auditService.findAuditList(pageNo - 1,
				pageSize, name, status, orders);
		paginate(pList);

		return SUCCESS;
	}

	/**
	 * 删除一个审核信息
	 * 
	 */
	public String deleteAuditInfo() throws Exception {

		auditService.deleteAuditInfo(auditId);

		return SUCCESS;
	}

	/**
	 * 审核信息
	 * 
	 */
	public String auditInfo() throws Exception {

		auditService.auditInfo(auditId, status);

		return SUCCESS;
	}

	public AuditService getAuditService() {
		return auditService;
	}

	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public AuditInfo getAudit() {
		return audit;
	}

	public void setAudit(AuditInfo audit) {
		this.audit = audit;
	}

	public Integer getAuditId() {
		return auditId;
	}

	public void setAuditId(Integer auditId) {
		this.auditId = auditId;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileFileName() {
		return fileFileName;
	}

	public void setFileFileName(String fileFileName) {
		this.fileFileName = fileFileName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}
