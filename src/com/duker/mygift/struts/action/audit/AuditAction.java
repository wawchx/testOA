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
 * �������ϲ�����Ϣ
 */
public class AuditAction extends PaginatedAction {

	// ��˷�����
	private AuditService auditService;

	// �����Ϣ
	private AuditInfo audit;

	// ��˱��
	private Integer auditId;

	private File file;

	// ��Դ����
	private String fileFileName;

	// ����
	private String name;

	// ����״̬
	private Integer status;

	/**
	 * ����ϴ�����ļ���Ϣ
	 * 
	 */
	public String addAudit() throws Exception {
		auditService.addAudit(file, fileFileName, audit);

		return SUCCESS;
	}

	/**
	 * �༭��Ա��ȡ�����Ϣ�б�
	 * 
	 * @return ��ת����ֵ
	 */
	public String findEditList() throws Exception {
		status = DList.AuditInfo.status.going;
		findAuditInfoList();

		return SUCCESS;
	}

	/**
	 * ������Ա��ȡ�����Ϣ�б�
	 * 
	 * @return ��ת����ֵ
	 */
	public String findDepartList() throws Exception {
		status = DList.AuditInfo.status.going;
		findAuditInfoList();

		return SUCCESS;
	}

	/**
	 * ������Ա��ȡ�����Ϣ�б�
	 * 
	 * @return ��ת����ֵ
	 */
	public String findManagerList() throws Exception {
		status = DList.AuditInfo.status.goingthrough;
		findAuditInfoList();

		return SUCCESS;
	}

	/**
	 * ��������Ա��ȡ�����Ϣ�б�
	 * 
	 * @return ��ת����ֵ
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
	 * ɾ��һ�������Ϣ
	 * 
	 */
	public String deleteAuditInfo() throws Exception {

		auditService.deleteAuditInfo(auditId);

		return SUCCESS;
	}

	/**
	 * �����Ϣ
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
