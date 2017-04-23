package com.duker.mygift.service.audit;

import java.io.File;
import java.util.List;

import com.duker.mygift.model.AuditInfo;
import com.duker.mygift.vo.PagedList;
import com.duker.mygift.vo.SortOrder;

/**
 * �����Ϣ������
 */
public interface AuditService {

	// ����ϴ�����ļ���Ϣ
	void addAudit(File file, String fileFileName, AuditInfo audit);

	// ��ȡ�����Ϣ�б�
	PagedList<AuditInfo> findAuditList(int pageNo, int pageSize, String name,
			Integer status, List<SortOrder> orders);

	// ɾ��һ�������Ϣ
	void deleteAuditInfo(Integer auditId);

	// �����Ϣ
	void auditInfo(Integer auditId, Integer status);

}
