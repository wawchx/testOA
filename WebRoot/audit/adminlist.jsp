<%@page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%@taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@taglib prefix="my" uri="/my-tags"%>
<!DOCTYPE html>
<html>
	<head>
		<title>审核信息列表--信息审核管理系统</title>
		<meta name="keywords" content="审核信息列表" />
		<meta name="description" content="审核信息列表" />
		<%@include file="/inc/commonheader.jsp"%>
		<script src="/js/jquery.form.js" type="text/javascript"></script>
		<script type="text/javascript">
			$(document).ready(function(){
				$("#btn_clear").click(function(){
					$("#query_form input").filter("*[type=text],*[type=hidden],*[type=file]").val("");
					$("#query_form select").each(function(index, domEle){
						domEle.selectedIndex = 0;
					});
				});
			});
			
			function auditinfo(auditid,status){
				if(confirm("确定审核该信息吗？！")) {
					var params = {"auditId":auditid,"status":status};
					var retCode = -1;
					var retMsg = "服务器没有响应";
					$.ajax({type:"get", url:"/audit/auditinfo.action",
								dataType:"json", data:params, async:false, success:function (json) {
						if(json){
							retCode = json.retCode;
							retMsg = json.retMsg;
						}
					}});
					
					if(!retCode){
						notice.prompt("删除成功");
						refresh();
					}else{
						notice.prompt("删除失败："+retMsg);
					}
				}
			}
		</script>
	</head>
	<body>
		<%@include file="/inc/header.jsp"%>
		<div class="body">
			<%@include file="/inc/left.jsp"%>
			<div class="right">
				<!-- 表单的头区域  -->
				<div class="queryheader">
					<span>审核信息列表</span>
				</div>
				<!-- 表单的内容区域  -->
				<div class="querysheet">
					${my:getConstMap(pageContext.request, "auditInfo_status")}
					<display:table name="resultList" id="row" class="mysheet" requestURI="/audit/findauditlist.action" sort="external" pagesize="15" partialList="true" size="resultSize">
						<display:column title="序号">${(pageNo-1)*pageSize+row_rowNum}</display:column>
						<display:column title="文件名称" property="name" sortable="true" sortProperty="name" />
						<display:column property="createtime" title="创建时间" sortable="true" sortProperty="createtime" format="{0,date,yyyy-MM-dd HH:mm:ss}"/>
						<display:column title="审核状态">${c_auditInfo_status[row.status]}</display:column>
						<display:column title="备注" property="des" sortable="true" sortProperty="des" maxLength="20" />
						<display:column title="文件">
							<a href="/file/${row.attachment}">下载</a>
						</display:column>
					</display:table>
					<div class="operationblock">
						<input class="nbutton_middle" type="button" value="审核信息上传" onclick="window.location='/audit/addaudit.jsp';"/>
					</div>
					<div id="actionmsg"></div>
				</div>
				<div class="queryblock">
					<form id="query_form" action="/audit/findauditlist.action" method="get">
						<table>
							<tr>
								<td class="querytitle">
									文件名称
								</td>
								<td class="querycontent">
									<input class="text" name="name" type="text" placeholder="请输入" title="文件名称" value="${name}" />
								</td>
							</tr>
							<tr>
								<td colspan="2" class="querybutton">
									<input type="submit" class="nbutton" value="查询"/>
									<input id="btn_clear" type="button" class="nbutton" value="清空"/>
								</td>
							</tr>
						</table>
					</form>
				</div>
			</div>
		</div>
	</body>
</html>