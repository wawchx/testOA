package com.duker.mygift.service.audit;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.duker.mygift.constant.DList;
import com.duker.mygift.dao.GenericHibernateDao;
import com.duker.mygift.model.AuditInfo;
import com.duker.mygift.utils.ZipFileUtil;
import com.duker.mygift.vo.PagedList;
import com.duker.mygift.vo.SortOrder;

/**
 * <pre>
 * 仪器故障实现类
 * </pre>
 */
public class AuditServiceImpl implements AuditService {

	/**
	 * Hibernate通用dao
	 */
	private GenericHibernateDao dao;

	private final File uploadPath;

	public AuditServiceImpl() {
		String tomcatPath = System.getProperty("user.dir");
		tomcatPath = tomcatPath.replace("bin", "webapps");
		tomcatPath += "\\ROOT\\file\\";
		uploadPath = new File(tomcatPath);
	}

	@Override
	public void addAudit(File file, String fileFileName, AuditInfo audit) {
		String suffix = null;
		if (file != null && StringUtils.isNotBlank(fileFileName)) {
			int idx = fileFileName.lastIndexOf('.');
			if (idx != -1) {
				suffix = fileFileName.substring(idx);
			}
			else {
				suffix = ".doc";
			}
			// 上传文件
			try {
				fileFileName = System.currentTimeMillis() + "";
				String uploadUrl = getImgUploadedUrl(fileFileName, suffix, file);
				audit.setAttachment(uploadUrl.substring(
						uploadUrl.lastIndexOf("/") + 1, uploadUrl.length()));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		audit.setCreatetime(new Date());
		audit.setStatus(DList.AuditInfo.status.going);
		dao.save(audit);
	}

	/**
	 * 获取上传文件路径
	 * 
	 * @param uploadFileName
	 *            文件名
	 * @param suffix
	 *            用户名
	 * @param file
	 *            文件
	 * 
	 * @return String 服务器地址
	 * @throws Exception
	 */
	private String getImgUploadedUrl(String uploadFileName, String suffix,
			File file) throws Exception {

		StringBuilder sb = new StringBuilder();
		String fileName = sb.append("/").append(uploadPath.getName())
				.append("/").append(uploadFileName.replace(suffix, ""))
				.append(suffix).toString();
		// 保存到文件服务器
		File newFile = new File(uploadPath.getParent(), fileName);
		FileUtils.copyFile(file, newFile);
		sb = new StringBuilder();
		File[] files = new File[1];
		String serverPath = sb.append(uploadPath).append("/")
				.append(newFile.getName()).toString();
		File serverFile = new File(serverPath);
		files[0] = serverFile;
		fileName = serverPath.substring(0, serverPath.lastIndexOf("."))
				+ ".zip";
		ZipFileUtil.compressFiles2Zip(files, fileName);
		if (serverFile.exists()) {
			serverFile.delete();
		}
		return fileName;
	}

	@Override
	public PagedList<AuditInfo> findAuditList(int pageNo, int pageSize,
			String name, Integer status, List<SortOrder> orders) {
		List<Object> values = new LinkedList<Object>();
		StringBuilder hql = new StringBuilder("from AuditInfo");
		String separator = " where ";

		if (StringUtils.isNotBlank(name)) {
			name = name.trim();
			hql.append(separator);
			hql.append("name like ?");
			values.add("%" + name + "%");
			separator = " and ";
		}
		if (status != null && status != 0) {
			hql.append(separator);
			hql.append("status=?");
			values.add(status);
			separator = " and ";
		}
		return dao.page(hql.toString(), pageNo, pageSize, orders,
				values.toArray());
	}

	@Override
	public void deleteAuditInfo(Integer auditId) {
		AuditInfo auditInfo = dao.findById(AuditInfo.class, auditId);
		File file = new File(uploadPath.getParent(), auditInfo.getAttachment());
		if (file.exists()) {
			file.delete();
		}

		dao.delete(auditInfo);
	}

	@Override
	public void auditInfo(Integer auditId, Integer status) {
		AuditInfo auditInfo = dao.findById(AuditInfo.class, auditId);
		auditInfo.setStatus(status);
		dao.update(auditInfo);
	}

	public void setDao(GenericHibernateDao dao) {
		this.dao = dao;
	}

}
