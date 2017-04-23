<%@page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%@page import="com.duker.mygift.constant.CList"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%
	if (session.getAttribute(CList.User.Session.USER_INFO) != null) {
		response.sendRedirect(request.getContextPath());
		return;
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<title>信息审核管理系统</title>
		<meta name="keywords" content="信息审核管理系统,登录" />
		<meta name="description" content="登录页页" />
		<link href="/css/common.css" rel="stylesheet" type="text/css" />
		<link href="/css/login.css" rel="stylesheet" type="text/css" />
		<script src="/js/jquery.js" type="text/javascript"></script>
		<!--[if lte IE 6]>
		<script src="/js/iepngfix_tilebg.js" type="text/javascript"></script> 
		<style type="text/css">
			.login .line span {
				font-weight: normal;
			}
			.loginbutton {
				margin-top: 44px;
			}
			.loginblock { behavior: url(/css/iepngfix.htc) }
		</style>
		<![endif]-->
		<script type="text/javascript">
			function check(){
				var userName = $.trim($("#userName").val());
				if (!userName){
					prompt("请输入用户名！");
					$("#userName").focus();
					return  false;
				}
				var pwd = $.trim($("#pwd").val());
				if (!pwd){
					prompt("请输入密码！");
					$("#pwd").focus();
					return  false;
				}
				var securitycode = $.trim($("#securitycode").val());
				if (!securitycode){
					prompt("请输入验证码！");
					$("#securitycode").focus();
					return  false;
				}
				return true;
			}

			function prompt(msg){
				if(msg){
					$("#actionmsg").css("color", "red");
					$("#actionmsg").html(msg);
				}else{
					$("#actionmsg").css("color", "blue");
					$("#actionmsg").html("");
				}
			}
			function init(){
				$("#loginTbl").height(document.body.clientHeight);
				$(document.body).css("visibility", "visible");
				window.setTimeout(function(){
					var userName = $("#userName").val();
					if(!userName){
						$("#userName").focus();
					}else{
						var pwd = $("#pwd").val();
						if(!pwd){
							$("#pwd").focus();
						}else{
							var securityCode = $("#securityCode").val();
							if(!securityCode){
								$("#securityCode").focus();
							}
						}
					}
				}, 1000);
			}
			$(document).ready(function(){
				init();
				$("input[class*=button]").bind("mouseover", null, function(e) {
					$(this).attr("class", "m" + $(this).attr("class").substr(1));
				});
				$("input[class*=button]").bind("mouseout", null, function(e) {
					$(this).attr("class", "n" + $(this).attr("class").substr(1));
				});
				$("input[class*=button]").bind("mousedown", null, function(e) {
					$(this).attr("class", "o" + $(this).attr("class").substr(1));
				});
				$("input[class*=button]").bind("mouseup", null, function(e) {
					$(this).attr("class", "n" + $(this).attr("class").substr(1));
				});
				$("input[type=hidden]").css("display", "inline");
			});
			$(window).bind("resize", null, init);
		</script>
	</head>
	<body style="visibility: hidden;">
		<form action="/login.action" method="post" onsubmit="return check();">
			<table id="loginTbl" class="loginouter">
				<tr><td>
				<div class="loginblock">
					<div class="login">
						<div class="line">
							<span>用户名：</span><input type="text" class="userName" id="userName" name="userName" value="admin" onchange="prompt('')"/>
						</div>
						<div class="line">
							<span>密　码：</span><input type="password" class="pwd" id="pwd" name="pwd" value="1" onchange="prompt('')"/>
						</div>
						<div class="line">
							<span>验证码：</span><input type="text" class="securitycode" maxlength="4" id="securitycode" name="securityCode" autocomplete="off" onchange="prompt('')"/><img class="randimage" onclick="this.src='/randimage?t='+ Math.random()" alt="点击更换验证码" src="/randimage" />
						</div>
						<div class="loginbutton">
							<input class="nbutton_login" type="submit" value="" style="border: 0px;" />
							<input type="hidden" name="lastPage" value="${lastPage}" />
						</div>
					</div>
					<div id="copyright" class="copyright">
						<div id="actionmsg" style="height: 20px;color: red;">
							<s:if test="#attr.loginMsg != null">
								${loginMsg} &nbsp;
							</s:if>
							<s:if test="actionErrors != null && actionErrors.size > 0" >
								<s:iterator value="actionErrors" status="st">
									<s:if test="#st.index lt 2">
										<s:property escapeHtml="false"/>&nbsp;
									</s:if>
								</s:iterator>
							</s:if>
							<s:if test="fieldErrors != null && fieldErrors.size > 0">
								<s:iterator value="fieldErrors" status="st">
									<s:if test="#st.index lt 2">
										<s:property value="value[0]" escapeHtml="false"/>&nbsp;
									</s:if>
								</s:iterator>
							</s:if>
						</div>
						<div class="mygift">Copyright &copy; 2009-2015 信息审核管理系统</div>
						<div class="mygift">推荐使用浏览器IE 6.0+或者Firefox 2.0+，推荐使用分辨率1280*1024</div>
					</div>
				</div>
				</td></tr>
			</table>
		</form>
	</body>
</html>
