<%@page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
	<head>
		<title>操作提示--信息审核管理系统</title>
		<meta name="keywords" content="信息审核管理系统,操作提示" />
		<meta name="description" content="操作提示" />
		<%@include file="/inc/commonheader.jsp"%>
		<link href="/prompt/skin/qq/ymPrompt.css" rel="stylesheet" type="text/css" />
		<script src="/prompt/ymPrompt.js" type="text/javascript"></script>
	</head>

	<body>
		<%@include file="/inc/header.jsp"%>
		<div class="body">
			<%@include file="/inc/left.jsp"%>
			<div class="right">
				<div class="resultmsgbox">
					<div class="actionMessage">
						<s:iterator value="#attr.actionMessages">
							<s:property escapeHtml="false"/>
							<br/>
						</s:iterator>
					</div>
					<div class="actionError">
						<s:iterator value="#attr.actionErrors">
							<s:property escapeHtml="false"/>
							<br/>
						</s:iterator>
						<s:iterator value="#attr.fieldErrors">
							<s:property value="value[0]" escapeHtml="false"/>
							<br/>
						</s:iterator>
					</div>
				</div>
				<div class="resultcmdbox">
					<s:iterator value="#attr.positions">
						<s:if test="type eq 0"><input type="button" class="nbutton_middle" style="margin-right: 5px;" onclick="window.location='<s:url value="%{url}" includeParams="none" encode="false" escapeAmp="false" />'" value="${name}" /></s:if>
						<s:else><input type="button" class="nbutton_middle" style="margin-right: 5px;" onclick="${url}" value="${name}" /></s:else>
					</s:iterator>
				</div>
			</div>
		</div>
	</body>
</html>
