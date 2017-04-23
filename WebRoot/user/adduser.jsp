<%@page language="java" pageEncoding="utf-8"
	contentType="text/html; charset=utf-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
	<head>
		<title>添加用户--信息审核管理系统</title>
		<meta name="keywords" content="信息审核管理系统,添加用户" />
		<meta name="description" content="添加用户" />
		<%@include file="/inc/commonheader.jsp"%>
		<link href="/prompt/skin/qq/ymPrompt.css" rel="stylesheet" type="text/css" />
		<script src="/prompt/ymPrompt.js" type="text/javascript"></script>
		<script src="/js/validator.js" type="text/javascript"></script>
		<script type="text/javascript">
			function openRoleTree(){
				var url = "/user/roletree.jsp?roleId="+$("#roleId").val()+"&callback=addRole&clearCallback=delRole";
				<s:if test="@com.duker.mygift.tag.ELUtil@isSuperUser()">
					url+="&empty=1";
				</s:if>
				ymPrompt.win(url,500,550,"选择角色",null,null,null,true)
			}
			function addRole(roleId, roleName){
				$("#roleId").val(roleId);
				$("#roleName").val(roleName);
				if(roleId == "R000002"){
					$("#tr_workname").show();
					$("#tr_address").show();
					$("#tr_phone").show();
				}else {
					$("#tr_workname").hide();
					$("#tr_address").hide();
					$("#tr_phone").hide();
				}
			}
			function delRole(){
				$("#roleId").val("");
				$("#roleName").val("点击选择");
			}
			$(document).ready(init);
			function init(){
				delRole();
			}
		</script>
	</head>

	<body>
		<%@include file="/inc/header.jsp"%>
		<div class="body">
			<%@include file="/inc/left.jsp"%>
			<div class="right">
				<form method="post" action="/user/adduser.action" onsubmit="return Validator.Validate(this,3);">
					<!-- 表单的头区域  -->
					<div class="formheader">
						<span>添加用户</span>
					</div>
					<!-- 表单的内容区域  -->
					<div class="formbody">
						<table border="0" cellpadding="0px" cellspacing="5px">
							<tr>
								<td class="onetitle">
									角色
								</td>
								<td>
									<input class="text readonlytext" id="roleName" type="text" readonly="readonly" autocomplete="off" onclick="openRoleTree()" value="${empty user.role?"点击选择":user.role.roleName}"/>
									<span class="errortip">*</span>
									<a href="javascript:delRole()">清除所选</a>
									<input type="hidden" id="roleId" name="user.role.roleId" require="true" title="角色" value="${user.role.roleId}"/>
								</td>
							</tr>
							<tr>
								<td class="onetitle">
									用户名称
								</td>
								<td>
									<input class="text" type="text" name="user.username" require="true" trim="true"  title="用户名" value="${user.username}" msg="用户名必须是3-20位的字母、数字或下划线">
									<span class="errortip">*</span><span class="tooltip">(用户名)</span>
								</td>
							</tr>
							<tr>
								<td class="onetitle">
									用户昵称
								</td>
								<td>
									<input class="text" type="text" name="user.nickname" value="${user.nickname}">
								</td>
							</tr>
							<tr id="tr_workname">
								<td class="onetitle">
									公司名称
								</td>
								<td>
									<input class="text" type="text" name="user.workname" value="${user.workname}">
								</td>
							</tr>
							<tr id="tr_phone">
								<td class="onetitle">
									联系方式
								</td>
								<td>
									<input class="text" type="text" name="user.phone" value="${user.phone}">
								</td>
							</tr>
							<tr id="tr_address">
								<td class="onetitle">
									供货商地址
								</td>
								<td>
									<input class="text" type="text" name="user.address" value="${user.address}">
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