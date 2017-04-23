<%@page language="java" pageEncoding="utf-8"
	contentType="text/html; charset=utf-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>修改角色--信息审核管理系统</title>
		<link href="/css/common.css" rel="stylesheet" type="text/css" />
		<link href="/css/prompt.css" rel="stylesheet" type="text/css" />
		<script src="/js/validator.js" type="text/javascript"></script>
		<script src="/js/jquery.js" type="text/javascript"></script>
		<script src="/js/jquery.form.js" type="text/javascript"></script>
		<script src="/js/common.js" type="text/javascript"></script>
		<script type="text/javascript">
			$(document).ready(function(){
				var options = {
					dataType: "json",
					beforeSubmit: function (arr, form, options) {
						return Validator.Validate(form[0],3);
					},
					success: function (data, statusText) {
						if(data){
							if(!data.retCode){
								parent.updateRoleSuccess("${param.roleId}", $("#roleName").val(), $("#description").val());
								parent.ymPrompt.close();
								parent.notice.prompt("修改成功");
							}else{
								notice.prompt("修改失败：" + data.retMsg);
							}
						}else{
							notice.prompt("修改失败：服务器没有反应");
						}
					},
					error: function() {
						notice.prompt("修改失败");
					}
				};
				$('#update_role').ajaxForm(options);
			});
		</script>
	</head>

	<body>
		<div class="querysheet">
			<form action="/user/updaterole.action" method="get" id="update_role">
				<table class="mysheet1">
					<tr class="odd">
						<td colspan="2">
							修改角色
						</td>
					</tr>
					<tr class="even">
						<td>角色名</td>
						<td class="last">
							<input type="hidden" name="role.roleId" value="${param.roleId}"/>
							<input class="text" type="text" id="roleName" name="role.roleName" require="true" trim="true" title="角色名" maxlength="20" value="${role.roleName}"/>
							<span class="errortip">*</span><span class="tooltip">(最大20个字符)</span>
						</td>
					</tr>
					<tr class="odd">
						<td>角色描述</td>
						<td class="last">
							<textarea class="textarea" style="width:200px;height:150px;" name="role.description" dataType="Limit" min="0" max="500">${role.description}</textarea>
							<span class="tooltip">(最大500个字符)</span>
						</td>
					</tr>
					<tr class="even">
						<td></td>
						<td class="last">
							<input class="nbutton" type="submit" value="提交" />
						</td>
					</tr>
				</table>
			</form>
		</div>
		<div id="actionmsg"></div>
	</body>
</html>