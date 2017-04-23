<%@page language="java" pageEncoding="utf-8"
	contentType="text/html; charset=utf-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="my" uri="/my-tags"%>
<%@taglib prefix="display" uri="http://displaytag.sf.net"%>
<!DOCTYPE html>
<html>
	<head>
		<title>用户列表--信息审核管理系统</title>
		<meta name="keywords" content="信息审核管理系统,用户列表" />
		<meta name="description" content="用户列表" />
		<%@include file="/inc/commonheader.jsp"%>
		<link href="/prompt/skin/qq/ymPrompt.css" rel="stylesheet" type="text/css" />
		<script src="/prompt/ymPrompt.js" type="text/javascript"></script>
		<script src="/js/checkbox.js" type="text/javascript"></script>
		<script type="text/javascript">
			var uc = new UnionCheck({check_all: "check_all", check_one: "check_user", valid_tip: "有效", invalid_tip: "无效"});
			$(document).ready(function () {
				uc.bind({
					url: "/user/changeuserstate.action",
					all_params_name: "userNames", 
					one_params_name: "userName", 
					value_name: "state", 
					checked_value: <s:property value="@com.duker.mygift.constant.DList$UserInfo$State@VALID"/>,
					unchecked_value: <s:property value="@com.duker.mygift.constant.DList$UserInfo$State@INVALID"/>});
				uc.checkState();
				
				// 清空查询条件
				$("#btn_clear").click(function(){
					$("#query_form input").filter("*[type=text],*[type=hidden],*[type=file]").val("");
					$("#query_form select").each(function(index, domEle){
						domEle.selectedIndex = 0;
					});
					$("#query_form input[name='user.role.roleName']").val("点击选择");
				});
			});
			
			function deleteUser(username){
				if(confirm("确定删除此用户吗？")) {
					var params = {"userName":username};
					var retCode = -1;
					var retMsg = "服务器没有响应";
					$.ajax({type:"post", url:"/user/deleteuser.action",
								dataType:"json", data:params, async:false, success:function (json) {
						if(json){
							retCode = json.retCode;
							retMsg = json.retMsg;
						}
					}});
					
					if(!retCode){
						notice.prompt("删除成功");
						refresh();
					}else{
						notice.prompt("删除失败："+retMsg);
					}
				}
			}
			function openRoleTree(username,roleId){
				if(!username) return;
				grantusername = username;
				ymPrompt.win("/user/roletree.jsp?roleId="+roleId+"&callback=addUserRole&clearCallback=delUserRole",500,550,"选择角色",null,null,null,true)
			}
			function addUserRole(roleId){
				if(!grantusername || !roleId) return;
				var params = {"userName":grantusername, roleId:roleId};
				var retCode = -1;
				var retMsg = "服务器没有响应";
				$.ajax({type:"get", url:"/user/adduserrole.action",
							dataType:"json", data:params, async:false, success:function (json) {
					if(json){
						retCode = json.retCode;
						retMsg = json.retMsg;
					}
				}});
				
				if(!retCode){
					notice.prompt("分配成功");
					refresh();
				}else{
					notice.prompt("分配失败："+retMsg);
				}
			}
			function delUserRole(){
				if(!grantusername) return;
				var params = {"userName":grantusername};
				var retCode = -1;
				var retMsg = "服务器没有响应";
				$.ajax({type:"get", url:"/user/deluserrole.action",
							dataType:"json", data:params, async:false, success:function (json) {
					if(json){
						retCode = json.retCode;
						retMsg = json.retMsg;
					}
				}});
				
				if(!retCode){
					notice.prompt("取消分配成功");
					refresh();
				}else{
					notice.prompt("取消分配失败："+retMsg);
				}
			}
			function resetpwd(username){
				if(confirm("确定重置该用户的密码吗？")){
					ymPrompt.win("/user/resetpwd.action?userName="+username,350,300,"重置密码",null,null,null,true)
				}
			}
			function selectRole(){
				var url = "/user/roletree.jsp?roleId="+$("#roleId").val()+"&callback=addRole&clearCallback=delRole&empty=1";
				ymPrompt.win(url,500,550,"选择角色",null,null,null,true)
			}
			function addRole(roleId, roleName){
				$("#roleId").val(roleId);
				$("#roleName").val(roleName);
			}
			function delRole(){
				$("#roleId").val("");
				$("#roleName").val("点击选择");
			}
		</script>
	</head>

	<body>
		<%@include file="/inc/header.jsp"%>
		<div class="body">
			<%@include file="/inc/left.jsp"%>
			<div class="right">
				<!-- 表单的头区域  -->
				<div class="queryheader">
					<span>用户列表</span>
				</div>
				<!-- 表单的内容区域  -->
				<div class="querysheet">
					${my:getConstMap(pageContext.request, "user_state")}
					<display:table name="resultList" id="row" class="mysheet" requestURI="/user/index.action"
						sort="external" pagesize="15" partialList="true" size="resultSize">
						<display:column title="序号">${(pageNo-1)*pageSize+row_rowNum}</display:column>
						<display:column property="username" title="用户名称" sortable="true" sortProperty="username" />
						<display:column property="password" title="用户密码" sortable="true" sortProperty="password" />
						<display:column property="nickname" title="用户昵称" sortable="true" sortProperty="nickname" maxLength="20"/>
						<display:column property="workname" title="公司名称" sortable="true" sortProperty="workname" />
						<display:column property="address" title="公司地址" sortable="true" sortProperty="address" />
						<display:column property="phone" title="联系方式" sortable="true" sortProperty="phone" />
						<display:column title="角色">
							<s:if test="#attr.row.username neq 'admin'">
								<s:if test="#attr.row.role.roleName != null">
									<a href="javascript:openRoleTree('${row.username}','${row.role.roleId}')">${row.role.roleName}</a>
								</s:if>
								<s:else>
									<a href="javascript:openRoleTree('${row.username}','${row.role.roleId}')">分配角色</a>
								</s:else>
							</s:if>
						</display:column>
						<s:if test="#attr.row.username eq 'admin'">
							<display:column title="状态">${c_user_state[row.state]}</display:column>
						</s:if>
						<s:else>
							<display:column title="状态<input type='checkbox' id='check_all' autocomplete='off'/>">
								<span id="tip_${row.username}">${c_user_state[row.state]}</span>
								<input type="checkbox" name="check_user" value="${row.username}" <s:if test="#attr.row.state eq @com.duker.mygift.constant.DList$UserInfo$State@VALID">checked</s:if> autocomplete="off"/>
							</display:column>
						</s:else>
						<display:column title="操作" headerClass="last" class="last">
							<a href="javascript:resetpwd('${row.username}')">重置密码</a>
							<s:if test="#attr.row.username neq 'admin'">
								<a href="/user/finduser.action?userName=${row.username}">修改</a>
								<a href="javascript:deleteUser('${row.username}')">删除</a>
							</s:if>
						</display:column>
					</display:table>
					<div class="operationblock">
						<input class="nbutton" type="button" value="添加用户" onclick="window.location='/user/adduser.jsp';"/>
					</div>
					<div id="actionmsg"></div>
				</div>
				<div class="queryblock">
					<form id="query_form" action="/user/index.action" method="get">
						<table>
							<tr>
								<td class="querytitle">
									用户名称
								</td>
								<td class="querycontent">
									<input class="text" type="text" name="user.username" value="${user.username}"/>
								</td>
								<td class="querytitle">
									用户昵称
								</td>
								<td class="querycontent">
									<input class="text" type="text" name="user.nickname" value="${user.nickname}"/>
								</td>
								<td class="querytitle">
									用户状态
								</td>
								<td class="querycontent">
									<s:select cssClass="select" name="user.state" list="@com.duker.mygift.tag.ELUtil@getConstMap('user_state')" />
								</td>
							</tr>
							<tr>
								<td></td>
								<td colspan="5" class="querybutton">
									<input type="submit" class="nbutton" value="查询"/>
									<input id="btn_clear" type="button" class="nbutton" value="清空"/>
								</td>
							</tr>
						</table>
					</form>
				</div>
			</div>
		</div>
	</body>
</html>