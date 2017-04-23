<%@page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>角色树--信息审核管理系统</title>
		<link href="/css/common.css" rel="stylesheet" type="text/css" />
		<link href="/js/dhtmlxtree/dhtmlxtree.css" rel="stylesheet" type="text/css" />
		<script type="text/javascript" src="/js/jquery.js"></script>
		<script type="text/javascript" src="/js/common.js"></script>
		<script type="text/javascript" src="/js/dhtmlxtree/dhtmlxcommon.js"></script>
		<script type="text/javascript" src="/js/dhtmlxtree/dhtmlxtree.js"></script>
		<script type="text/javaScript">
			var tree = null;
			<s:if test="#parameters.empty eq null">
			var notEmpty = true;
			</s:if>
			<s:else>
			var notEmpty = false;
			</s:else>
			<s:if test="#parameters.callback eq null">
			var callback = parent.select;
			</s:if>
			<s:else>
			var callback = parent.${param.callback};
			</s:else>
			<s:if test="#parameters.clearCallback eq null">
			var clearCallback = parent.clearSelect;
			</s:if>
			<s:else>
			var clearCallback = parent.${param.clearCallback};
			</s:else>
			$(document).ready(function () {
				// 初始化树
				tree=new dhtmlXTreeObject("role_tree", "100%", "100%", 0);
				tree.setSkin('dhx_skyblue');
				tree.setImagePath("/js/dhtmlxtree/imgs/csh_books/");
				tree.loadXML("/user/roletree.action", function(){
					<s:if test="#parameters.roleId neq null">
					tree.selectItem('r_${param.roleId}');
					tree.openItem('r_${param.roleId}');
					</s:if>
				});
			});
			
			function submit(){
				var id = tree.getSelectedItemId();
				if(notEmpty && !id){
					notice.prompt("请选择角色");
					return;
				}
				if("function" == typeof callback && id){
					var value = tree.getSelectedItemText();
					callback(id.substr(2), value);
				}
				if(parent.ymPrompt){
					parent.ymPrompt.close();
				}
			}
			function reset(){
				if("function" == typeof clearCallback){
					clearCallback();
				}
				if(parent.ymPrompt){
					parent.ymPrompt.close();
				}
			}
		</script>
	</head>
	<body style="height: auto;">
		<div style="margin-top: 20px;margin-left: 40px;">
			<div id="role_tree"></div>
			<div style="margin-top: 5px;">
				<input class="nbutton" type="button" onclick="submit()" value="确定"/>
				<input class="nbutton" type="button" onclick="reset()" value="清除所选"/>
			</div>
			<div id="actionmsg" style="color: blue;"></div>
		</div>
	</body>
</html>