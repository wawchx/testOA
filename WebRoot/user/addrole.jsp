<%@page language="java" pageEncoding="utf-8"
	contentType="text/html; charset=utf-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>添加角色--信息审核管理系统</title>
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
								parent.addRoleSuccess("${param.roleId}", data.role.roleId, $("#roleName").val(), $("#description").val());
								parent.ymPrompt.close();
								parent.notice.prompt("添加成功");
							}else{
								notice.prompt("添加失败：" + data.retMsg);
							}
						}else{
							notice.prompt("添加失败：服务器没有反应");
						}
					},
					error: function() {
						notice.prompt("添加失败");
					}
				};
				$('#add_role').ajaxForm(options);
			});
		</script>
	</head>

	<body>
		<div class="querysheet">
			<form action="/user/addrole.action" method="get" id="add_role">
				<table class="mysheet1">
					<tr class="odd">
						<td colspan="2" class="last">
							<s:if test="#parameters.roleName != null && @com.duker.mygift.tag.ELUtil@isNotNull(#parameters.roleName[0])">
								给<span style="color: red">${param.roleName}</span>角色添加下级角色
							</s:if>
							<s:else>
								添加角色
							</s:else>
						</td>
					</tr>
					<tr class="even">
						<td>角色名</td>
						<td class="last">
							<input type="hidden" name="role.parentRoleId" value="${param.roleId}"/>
							<input class="text" type="text" id="roleName" name="role.roleName" require="true" trim="true" title="角色名" maxlength="20"/>
							<span class="errortip">*</span><span class="tooltip">(最大20个字符)</span>
						</td>
					</tr>
					<tr class="odd">
						<td>角色描述</td>
						<td class="last">
							<textarea class="textarea" style="width:200px;height:150px;" id="description" name="role.description" dataType="Limit" min="0" max="500"></textarea>
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