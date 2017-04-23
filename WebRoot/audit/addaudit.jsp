<%@page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
	<head>
		<title>审核信息录入--信息审核管理系统</title>
		<meta name="keywords" content="资源信息审核管理系统,审核信息录入" />
		<meta name="description" content="审核信息录入" />
		<%@include file="/inc/commonheader.jsp"%>
		<script src="/js/validator.js" type="text/javascript"></script>
		<script type="text/javascript">
			$(document).ready(function(){
				
				$("#btn_clear").click(function(){
					$("#query_form input").filter("*[type=text],*[type=hidden],*[type=file]").val("");
					$("#query_form select").each(function(index, domEle){
						domEle.selectedIndex = 0;
					});
				});
			});
		</script>
	</head>
	<body>
		<%@include file="/inc/header.jsp"%>
		<div class="body">
			<%@include file="/inc/left.jsp"%>
			<div class="right">
				<form method="post" action="/audit/addaudit.action" enctype="multipart/form-data" onsubmit="return Validator.Validate(this,3);" >
					<!-- 表单的头区域  -->
					<div class="formheader">
						<span>审核信息录入</span>
					</div>
					<!-- 表单的内容区域  -->
					<div class="formbody">
						<table border="0" cellpadding="0px" cellspacing="5px">
							<tr>
								<td class="onetitle">
									审核文档
								</td>
								<td>
									<input type="file" name="file" require="true" title="审核文档" accept="doc,pdf" msg="格式，支持word, pdf格式" /><span class="tooltip">(支持word, pdf格式)</span>
								</td>
							</tr>
							<tr>
								<td class="onetitle">
									文档名称
								</td>
								<td>
									<input class="text" type="text" name="audit.name" title="文档名称" value="${audit.name}" msg="文档名称"/>
								</td>
							</tr>
							<tr>
								<td class="onetitle">
									备注
								</td>
								<td>
									<input class="text" type="text" name="audit.des" title="备注" value="${audit.des}" msg="备注"/>
								</td>
							</tr>
							<tr>
								<td colspan="2" class="blueline"></td>
							</tr>
							<tr>
								<td id="actionmsg" colspan="2">
									<s:actionerror/>
									<s:fielderror />
								</td>
							</tr>
							<tr>
								<td></td>
								<td class="button">
									<input type="submit" class="nbutton" value="提交" />
									<input style="margin-left : 15px;" type="reset" class="nbutton" value="重置" onclick="window.setTimeout(init, 100);"/>
								</td>
							</tr>
						</table>
					</div>
				</form>
			</div>
		</div>
	</body>
</html>