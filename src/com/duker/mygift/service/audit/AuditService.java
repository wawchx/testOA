package com.duker.mygift.service.audit;

import java.io.File;
import java.util.List;

import com.duker.mygift.model.AuditInfo;
import com.duker.mygift.vo.PagedList;
import com.duker.mygift.vo.SortOrder;

/**
 * 审核信息服务类
 */
public interface AuditService {

	// 添加上传审核文件信息
	void addAudit(File file, String fileFileName, AuditInfo audit);

	// 获取审核信息列表
	PagedList<AuditInfo> findAuditList(int pageNo, int pageSize, String name,
			Integer status, List<SortOrder> orders);

	// 删除一个审核信息
	void deleteAuditInfo(Integer auditId);

	// 审核信息
	void auditInfo(Integer auditId, Integer status);

}
