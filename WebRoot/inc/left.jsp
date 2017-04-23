<%@page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%@taglib prefix="my" uri="/my-tags"%>
<script type="text/javascript">
	var header_height;
	var info_height;
	var body_height = ${empty param.height ? 737 : param.height};
	var left_width;
	var right_width = ${empty param.width ? 1139 : param.width};
	$(document).ready(function () {
		resize();
		var menu = new Menu();
		menu.width = $("#menu_container").width();
		menu.xmlString = "${my:getMenu(pageContext.request)}";
		menu.init();
		menu.showByElement(document.getElementById("menu_container"), "left");
		$(window).bind("resize", null, function () {
			resize();
		});
	});
	function resize(){
		var client_width=document.body.clientWidth;
		var client_height=document.body.clientHeight;
		var elements = $("div[not_auto_disapear_index],div[auto_disapear_index]");
		for ( var i = 0; i < elements.length; i++) {
			updateNoticePos($(elements[i]));
		}
		header_height=$(".header").prop("offsetHeight");
		header_height=!header_height||isNaN(header_height)?0:header_height;
		header_height+=getMargin(".header", "marginTop")+getMargin(".header", "marginBottom");
		
		var height = body_height + header_height;
		if(height < client_height){
			height = client_height;
			body_height=height - header_height;
		}
		$(".left").css("marginTop", -3);
		$(".left").height(body_height + 3);
		$(".right").height(body_height);
		$(".body").height(body_height);
		$(document.body).height(height);
		
		left_width=$(".left").prop("offsetWidth");
		left_width+=getMargin(".left", "marginLeft") + getMargin(".left", "marginRight");
		
		var width = right_width + left_width;
		if(width < client_width){
			width = client_width;
			right_width = width - left_width;
		}
		
		$(".right").width(right_width);
		$(document.body).width(width);
		$(".footer").css("top", height - 20);
		if(typeof comet != "undefined"){
			comet.initialize();
		}
	}
	function getMargin(selector, marginType){
		var margin = $(selector).css(marginType);
		margin = margin ? parseInt(margin) : 0;
		margin = isNaN(margin) ? 0 : margin;
		return margin;
	}
</script>
<div id="menu_container" class="left">
</div>
