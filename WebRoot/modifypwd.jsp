<%@page language="java" pageEncoding="utf-8"
	contentType="text/html; charset=utf-8"%>
<!DOCTYPE html>
<html>
	<head>
		<title>修改密码--信息审核管理系统</title>
		<meta name="keywords" content="信息审核管理系统,修改密码" />
		<meta name="description" content="修改密码" />
		<%@include file="/inc/commonheader.jsp"%>
		<script src="/js/validator.js" type="text/javascript"></script>
	</head>

	<body>
		<%@include file="/inc/header.jsp"%>
		<div class="body">
			<%@include file="/inc/left.jsp"%>
			<div class="right">
				<form method="post" action="/modifypwd.action" onsubmit="return Validator.Validate(this,3);">
					<!-- 表单的头区域  -->
					<div class="formheader">
						<span>修改密码</span>
					</div>
					<!-- 表单的内容区域  -->
					<div class="formbody">
						<table border="0" cellpadding="0px" cellspacing="5px">
							<tr>
								<td class="onetitle">
									旧密码
								</td>
								<td>
									<input class="text" type="password" name="oldPwd" require="true" title="旧密码" value="">
									<span class="errortip">*</span>
								</td>
							</tr>
							<tr>
								<td class="onetitle">
									新密码
								</td>
								<td>
									<input class="text" id="pwd" type="password" name="pwd" require="true" dataType="Custom" regexp="[\x00-\xFF]{6,20}" msg="密码必须是6-20位ASCII字符" title="密码" maxlength="20">
									<span class="errortip">*</span><span class="tooltip">(密码必须是6-20位字母、数字、特殊字符)</span>
								</td>
							</tr>
							<tr>
								<td class="onetitle">
									确认密码
								</td>
								<td>
									<input class="text" type="password" dataType="Repeat" to="pwd" msg="两次输入的密码不一致" title="确认密码" maxlength="20">
									<span class="errortip">*</span><span class="tooltip">(必须与新密码一致)</span>
								</td>
							</tr>
							<s:if test="#attr.errorMsg != null">
							<tr>
								<td colspan="2">
									<div id="actionmsg" style="height: 20px;color: red;">${errorMsg}</div>
								</td>
							</tr>
							</s:if>
							<tr>
								<td colspan="2" class="blueline"></td>
							</tr>
							<tr>
								<td></td>
								<td class="button">
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
