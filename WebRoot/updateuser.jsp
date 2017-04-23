<%@page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="my" uri="/my-tags"%>
<!DOCTYPE html>
<html>
	<head>
		<title>修改资料--信息审核管理系统</title>
		<meta name="keywords" content="信息审核管理系统,修改资料" />
		<meta name="description" content="修改资料" />
		<%@include file="/inc/commonheader.jsp"%>
		<script src="/js/validator.js" type="text/javascript"></script>
	</head>

	<body>
		<%@include file="/inc/header.jsp"%>
		<div class="body">
			<%@include file="/inc/left.jsp"%>
			<div class="right">
				<form method="post" action="/updateuser.action" onsubmit="return Validator.Validate(this,3);">
					<!-- 表单的头区域  -->
					<div class="formheader">
						<span>修改资料</span>
					</div>
					<!-- 表单的内容区域  -->
					<div class="formbody">
						<table border="0" cellpadding="0px" cellspacing="5px">
							<tr>
								<td class="onetitle">
									用户名称
								</td>
								<td>
									${user.username}
								</td>
							</tr>
							<tr>
								<td class="onetitle">
									用户工号
								</td>
								<td>
									<input class="text" type="text" name="user.workno" value="${user.workno}">
								</td>
							</tr>
							<tr>
								<td class="onetitle">
									密码
								</td>
								<td>
									<input class="text" type="text" name="user.password" value="${user.password}">
								</td>
							</tr>
							<tr>
								<td colspan="2" class="blueline"></td>
							</tr>
							<tr>
								<td></td>
								<td class="button">
									<input type="hidden" name="user.username" value="${user.username}" />
									<input type="submit" class="nbutton" value="提交" />
									<input style="margin-left : 15px;" type="reset" class="nbutton" value="重置" />
								</td>
							</tr>
						</table>
					</div>
				</form>
			</div>
		</div>
	</body>
</html>
