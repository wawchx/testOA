<%@page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%@taglib prefix="my" uri="/my-tags"%>
<%@taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
	<head>
		<title>信息审核管理系统</title>
		<meta name="keywords" content="信息审核管理系统,首页" />
		<meta name="description" content="首页" />
		<%@include file="/inc/commonheader.jsp"%>
		<style type="text/css">
			.allocate-title {
				font-size: 18px;
				color: red;
			}
			
			.allocate-title.bottom {
				margin-top: 15px;
			}
		</style>
	</head>
	<body>
		<%@include file="/inc/header.jsp"%>
		<div class="body">
			<%@include file="/inc/left.jsp"%>
			<!-- 表单的内容区域  -->
			<%-- <div class="right">
				<div class="right">
					<div class="allocate-title bottom">
						未处理故障信息
					</div>
					<display:table name="noDealResults" id="row" class="mysheet" sort="list" pagesize="15">
						<display:column title="序号">${(pageNo-1)*pageSize+row_rowNum}</display:column>
							<display:column title="仪器名称" property="machineInfo.name" sortable="true" sortProperty="machineInfo.name" />
							<display:column title="保修人员" property="workerInfo.name" sortable="true" sortProperty="workerInfo.name" />
							<display:column property="createTime" title="报修时间" sortable="true" sortProperty="createTime" format="{0,date,yyyy-MM-dd}"/>
							<display:column property="repaireTime" title="故障时间" sortable="true" sortProperty="repaireTime" format="{0,date,yyyy-MM-dd}"/>
							<display:column title="故障报修地点" property="address" sortable="true" sortProperty="address" />
							<display:column title="备注" property="des" sortable="true" sortProperty="des" headerClass="last" class="last"/>
					</display:table>
					<div class="allocate-title bottom">
						预处理故障报修信息
					</div>
					<display:table name="dealingResults" id="row" class="mysheet" sort="list" pagesize="15">
						<display:column title="序号">${(pageNo-1)*pageSize+row_rowNum}</display:column>
							<display:column title="仪器名称" property="machineInfo.name" sortable="true" sortProperty="machineInfo.name" />
							<display:column title="仪器名称" property="machineInfo.name" sortable="true" sortProperty="machineInfo.name" />
						<display:column title="保修人员" property="workerInfo.name" sortable="true" sortProperty="workerInfo.name" />
						<display:column property="dealingTime" title="故障预处理时间" sortable="true" sortProperty="dealingTime" format="{0,date,yyyy-MM-dd HH:mm:ss}"/>
						<display:column title="故障预处理方式">${c_repairInfo_dealingMethod[row.dealingMethod]}</display:column>
						<display:column title="故障预处理过程" property="dealingProcess" sortable="true" sortProperty="dealingProcess" />
						<display:column title="故障预处理结果" headerClass="last" class="last">${c_repairInfo_dealingResult[row.dealingResult]}</display:column>
					</display:table>
				</div>
			</div> --%>
		</div>
	</body>
</html>
