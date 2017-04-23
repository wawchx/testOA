<%@page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@include file="/inc/footer.jsp"%>
<div class="header">
	<div class="logo"></div>
	<div class="sysoperation">
		<s:if test="@com.duker.mygift.tag.ELUtil@isNotNull(#session[@com.duker.mygift.constant.CList$User$Session@USER_NAME])">
		<input type="button" class="nbutton_modifyinfo" style="margin-top: 40px;" onclick="window.location='/finduser.action'" />
		<input type="button" class="nbutton_modifypwd" style="margin-top: 40px;" onclick="window.location='/modifypwd.jsp'" />
		<input type="button" class="nbutton_logout" style="margin-top: 40px;" onclick="window.location='/logout.action'" />
		</s:if>
		<s:else>
		<input type="button" class="nbutton_login" style="margin-top: 40px;margin-right: 25px;" onclick="window.location='/login.jsp'" />
		</s:else>
	</div>
	<div class="info">
		<s:set var="navigation_urls" value="#session[@com.duker.mygift.constant.CList$User$Session@NAVIGATION]"></s:set>
		<span>
		您好
		<s:if test="@com.duker.mygift.tag.ELUtil@isNotNull(#session[@com.duker.mygift.constant.CList$User$Session@USER_INFO].username)"><s:property value="#session[@com.duker.mygift.constant.CList$User$Session@USER_INFO].username" escapeHtml="false"/></s:if>
		<s:if test="@com.duker.mygift.tag.ELUtil@isNotNull(#session[@com.duker.mygift.constant.CList$User$Session@USER_INFO].nickname)">&nbsp;&nbsp;昵称:<s:property value="#session[@com.duker.mygift.constant.CList$User$Session@USER_INFO].nickname" escapeHtml="false"/></s:if>
		<s:else>游客</s:else>
		</span><a href="/" style="margin-left: 5px;">首页</a><s:if test="#navigation_urls[#request['javax.servlet.forward.servlet_path']]!=null">--><s:property value="#navigation_urls[#request['javax.servlet.forward.servlet_path']]" /></s:if>	<s:elseif test="#navigation_urls[@org.apache.struts2.ServletActionContext@getRequest().getServletPath()]!=null">--><s:property value="#navigation_urls[@org.apache.struts2.ServletActionContext@getRequest().getServletPath()]" /></s:elseif>
	</div>
</div>