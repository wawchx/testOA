<%@page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>操作提示--信息审核管理系统</title>
		<link href="/css/common.css" rel="stylesheet" type="text/css" />
		<link href="/css/prompt.css" rel="stylesheet" type="text/css" />
		<s:if test="!retCode">
		<script type="text/javascript">
			<s:if test="#attr.refresh">
			if("function" == typeof parent.refresh){
				parent.refresh();
			}
			</s:if>
			if("function" == typeof parent.promptCallback){
				parent.promptCallback();
			}
			function close(){
				<s:if test="not #attr.notClose">
					if(parent.ymPrompt && "function" == typeof parent.ymPrompt.close){
						window.setTimeout(function (){
							parent.ymPrompt.close();
						}, 3000);
					}
				</s:if>
			}
		</script>
		</s:if>
	</head>

	<body onload="close()">
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
	</body>
</html>
