<%@tag pageEncoding="utf-8" body-content="empty"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@attribute name="className" type="java.lang.String"%>
<%@attribute name="style" type="java.lang.String"%>
<%@attribute name="href" type="java.lang.String"%>
<%@attribute name="onclick" type="java.lang.String"%>
<%@attribute name="icon" type="java.lang.String"%>
<%@attribute name="iconStyle" type="java.lang.String"%>
<%@attribute name="text" type="java.lang.String"%>
<a class="<s:property value="#attr.className" escape="false" />"<s:if test="#attr.href neq null"> href="<s:property value="#attr.href" escapeHtml="true" />"</s:if><s:else> href="javascript:void(0)" onclick="<s:property value="#attr.onclick" escapeHtml="true" />"</s:else> style="color: #000;text-decoration: none;display: inline-block;margin: 2px; <s:property value="#attr.style" escape="false" />">
	<img style="display: inline-block;margin-left: -2px;margin-right: 2px; border: 0;vertical-align: middle;<s:property value="#attr.iconStyle" escape="false" />" src="/img/icon/<s:property value="#attr.icon" escapeHtml="true" />" /><span style="display: inline-block;"><s:property value="#attr.text" escapeHtml="true" /></span>
</a>