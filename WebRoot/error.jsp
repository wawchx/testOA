<%@page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%@taglib prefix="my" uri="/my-tags"%>
<!DOCTYPE html>
<html>
	<head>
		<title>操作失败--信息审核管理系统</title>
		<meta name="keywords" content="信息审核管理系统,操作提示" />
		<meta name="description" content="操作提示" />
		<%@include file="/inc/commonheader.jsp"%>
		<style type="text/css">
			textarea{
				line-height: 20px;
				width: 1000px;
				height: 500px;
				overflow: scroll;
				background-color: #FFFFFF;
				border: 1px solid #7F9DB9;
			}
		</style>
	</head>

	<body>
		<%@include file="/inc/header.jsp"%>
		<div class="body">
			<%@include file="/inc/left.jsp"%>
			<div class="right">
				<div class="resultmsgbox">
					<div class="actionError">
						服务器内部错误<br>
						发生错误页面${requestScope["javax.servlet.error.request_uri"]}<br>
						错误类型${requestScope["javax.servlet.error.exception_type"]}<br>
						<textarea readonly="readonly">${my:printStackTrace(pageContext.request, pageContext.out)}</textarea>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>
