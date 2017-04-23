var notice = null;
var mousePos = {
	x : 0,
	y : 0
};
document.write('<script type="text/javascript" src="/js/jquery.bgiframe.js"></script>');
function registerButton() {
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
}
function registerSheet() {
	$("table[class=mysheet] tr").mouseover(function() {
		if (!$(this).is("[_color]")) {
			$(this).attr("_color", $(this).css("backgroundColor"));
		}
		$(this).css("backgroundColor", "lightskyblue");
	});
	$("table[class=mysheet] tr").mouseout(function() {
		color = $(this).attr("_color");
		$(this).css("backgroundColor", color);
	});
}
function displayInputFocus(){
	if (!$(this).is("[_color]")) {
		$(this).attr("_color", $(this).css("color"));
	}
}
function displayInputBlur(){
	var oldValue = $(this).attr("_value");
	var newValue = $(this).val();
	$(this).attr("value", newValue);
	if (oldValue != newValue) {
		var form = $("#" + form_id);
		form.empty();
		form.attr("action", $(this).attr("_action"));
		$(this).clone().appendTo(form);
		form.submit();
		var onsubmit = $(this).attr("_onsubmit");
		if(onsubmit){
			eval(onsubmit);
		}
	} else {
		$(this).css("color", $(this).attr("_color"));
	}
}
function displayInputKeyup(e){
	var oldValue = $(this).attr("_value");
	var newValue = $(this).val();
	if (e.which == 13) {
		$(this).attr("value", newValue);
		var form = $("#" + form_id);
		form.empty();
		form.attr("action", $(this).attr("_action"));
		$(this).clone().appendTo(form);
		form.submit();
		var onsubmit = $(this).attr("_onsubmit");
		if(onsubmit){
			eval(onsubmit);
		}
	} else {
		if (oldValue != newValue) {
			$(this).css("color", "red");
		} else {
			$(this).css("color", $(this).attr("_color"));
		}
	}
}
var form_id = "form_" + new Date().getTime();
function bindDisplayInput() {
	var inputs = $(".display_input");
	if (inputs.length <= 0) {
		return;
	}
	if($("#"+form_id).length<=0){
		$("body")
				.append(
						"<form id='"
								+ form_id
								+ "' method='get' style='display:none' onsubmit='return Validator.Validate(this, 4)'></form>");
	}
	inputs.attr("autocomplete", "off");
	inputs.unbind("focus",displayInputFocus);
	inputs.bind("focus",displayInputFocus);
	inputs.unbind("blur",displayInputBlur);
	inputs.bind("blur",displayInputBlur);
	inputs.unbind("keyup",displayInputKeyup);
	inputs.bind("keyup",displayInputKeyup);
	var options ={
		dataType: "json",
		beforeSubmit: function (arr, form, options) {
			return Validator.Validate(form[0],4);
		},
		success: function (data, statusText,xhr, form) {
			if(data){
				if(!data.retCode){
					var action = form.attr("action");
					var input = $("input[_action='"+action+"']");
					input.css("color", input.attr("_color"));
					input.attr("_value", input.val());
					input.change();
					if(typeof updatePartSuccess == "function"){
						updatePartSuccess();
					}
					notice.prompt("修改成功");
				}else{
					notice.prompt("修改失败：" + data.retMsg);
				}
			}else{
				notice.prompt("修改失败：服务器没有反应");
			}
		},
		error: function() {
			notice.prompt("修改失败");
		}
	};
	$("#"+form_id).ajaxForm(options);
}
function displaySelectChange(){
	var form = $("#" + form_id);
	form.empty();
	form.attr("action", $(this).attr("_action"));
	form.append("<input name='"+this.name+"' value='"+this.value+"'>");
	form.submit();
	var onsubmit = $(this).attr("_onsubmit");
	if(onsubmit){
		eval(onsubmit);
	}
}
function bindDisplaySelect() {
	var selects = $(".display_select");
	if (selects.length <= 0) {
		return;
	}
	if($("#"+form_id).length<=0){
		$("body")
				.append(
						"<form id='"
								+ form_id
								+ "' method='get' style='display:none' onsubmit='return Validator.Validate(this, 4)'></form>");
	}
	selects.attr("autocomplete", "off");
	selects.unbind("change",displaySelectChange);
	selects.bind("change",displaySelectChange);
	var options ={
		dataType: "json",
		beforeSubmit: function (arr, form, options) {
			return Validator.Validate(form[0],4);
		},
		success: function (data, statusText, xhr, form) {
			if(data){
				if(!data.retCode){
					notice.prompt("修改成功");
				}else{
					notice.prompt("修改失败：" + data.retMsg);
				}
			}else{
				notice.prompt("修改失败：服务器没有反应");
			}
		},
		error: function() {
			notice.prompt("修改失败");
		}
	};
	$("#"+form_id).ajaxForm(options);
}
$(document).ready(function() {
	notice = new Notice();
	registerButton();
	registerSheet();
	bindDisplayInput();
	bindDisplaySelect();
	$(window).bind("keydown", null, function(event) {
		var keyCode = event.which;
		if (!keyCode) {
			keyCode = event.keyCode;
		}
		if (keyCode == 13 && "function" == typeof enter) {
			enter();
		}
	});
	
	$(".queryblock form").each(function(index, form){
		if(form.onsubmit){
			form.oldonsubmit = form.onsubmit;
			form.onsubmit = null;
		}
		form.onsubmit = function (){
			if(this.oldonsubmit){
				var ret=this.oldonsubmit();
				if(!ret) {
					return false;
				}
			}
			if ($(".g_waitInfo").length > 0) {
				return;
			}
			var waitInfo = $('<div class="g_waitInfo"><span>数据正在加载，请稍后...</span></div>');
			var offset = $(this).offset();
			var top = offset.top + $(this).height() + 2;
			var left = offset.left;
			waitInfo.css({
				top : top,
				left : left
			});
			$("body").append(waitInfo);
		}
	});
});
$(document).click(function(e) {
	mousePos = {
		x : e.pageX,
		y : e.pageY
	};
	var elements = $("div[not_auto_disapear_index]");
	for ( var i = 0; i < elements.length; i++) {
		updateNoticePos($(elements[i]));
	}
});
$(document).mousemove(function(e) {
	mousePos = {
		x : e.pageX,
		y : e.pageY
	};
});

function Notice(id, timeout) {
	this.id = id ? id : "actionmsg";
	this.defaultTimeout = timeout ? timeout : 1500;
	var element = $("#" + this.id);
	this.defaultMessage = element[0] ? $.trim(element.html()) : "";
	this.defaultColor = element[0] ? element.css("color") : "";
	this.index = 0;
	/**
	 * 打印提示信息
	 * @param String message 消息内容
	 * @param int timeout 自动消失时间,单位毫秒,-1表示永不消息
	 * @param String color 消息显示颜色
	 * @param int bindIndex 不自动消失的消息需要绑定一个索引,之后再调用clear进行清除
	 */
	this.prompt = function(message, timeout, color, bindIndex) {
		var element = $("#" + this.id);
		var defaultMessage = this.defaultMessage;
		var defaultColor = this.defaultColor;
		if (element[0]) {
			element=$("<div></div>");
			$("body").append(element);
			if (message) {
				element.css("display", "none");
				element.css("float", "left");
				element.css("position", "absolute");
				element.css("fontSize", "20px");
				element.css("wordWrap", "break-word");
				element.css("wordBreak", "break-all");
				element.html(message);
				element.css("color", color ? color : "red");
				var w = element.width();
				if (w >= 800) {
					var h = element.height();
					element.empty();
					var html = "<div class='dialog4Left' dialog='true'></div>"
							+ "<div class='dialog4Center' dialog='true'><span dialog='true' style='float:left;margin-left:-30px;padding-top:25px;width:"
							+ (w / 4) + "px;height:" + (h * 4) + "px;'>"
							+ message + "</span></div>"
							+ "<div class='dialog4Right' dialog='true'></div>";
					element.append(html);
				} else if (w >= 600) {
					var h = element.height();
					element.empty();
					var html = "<div class='dialog3Left' dialog='true'></div>"
							+ "<div class='dialog3Center' dialog='true'><span dialog='true' style='float:left;margin-left:-30px;padding-top:25px;width:"
							+ (w / 3) + "px;height:" + (h * 3) + "px;'>"
							+ message + "</span></div>"
							+ "<div class='dialog3Right' dialog='true'></div>";
					element.append(html);
				} else if (w >= 400) {
					var h = element.height();
					element.empty();
					var html = "<div class='dialog2Left' dialog='true'></div>"
							+ "<div class='dialog2Center' dialog='true'><span dialog='true' style='float:left;margin-left:-30px;padding-top:25px;width:"
							+ (w / 2) + "px;height:" + (h * 2) + "px;'>"
							+ message + "</span></div>"
							+ "<div class='dialog2Right' dialog='true'></div>";
					element.append(html);
				} else {
					var h = element.height();
					element.empty();
					var html = "<div class='dialogLeft' dialog='true'></div>"
							+ "<div class='dialogCenter' dialog='true'><span dialog='true' style='float:left;padding-top:20px;'>" + message
							+ "</span></div>"
							+ "<div class='dialogRight' dialog='true'></div>";
					element.append(html);
				}
				element.bgiframe();
				updateNoticePos(element);
				element.css("display", "");
				if (!timeout) {
					var count = message.length;
					count = count < 7 ? 7 : count;
					timeout = this.defaultTimeout * (count/7);
				}
				if (timeout != -1) {
					var func = function (e) {
						mousePos = {
							x : e.pageX,
							y : e.pageY
						};
						var dialog = $(e.target).attr("dialog");
						if (!dialog) {
							element.fadeOut(500, function() {
								$(this).remove();
							});
							$(document).unbind("click", func);
						}
					}
					element.attr("auto_disapear_index", this.index++);
					element.mouseover(function(e) {
						$(this).attr("not_hide", "true");
					});
					window.setTimeout(function() {
						var notHide = element.attr("not_hide");
						if (notHide) {
							return;
						}
						$(document).unbind("click", func);
						element.remove();
						}, timeout);

					window.setTimeout(function() {$(document).click(func);},500);
				} else {
					element.attr("not_auto_disapear_index", bindIndex);
				}
			} else {
				element.html(defaultMessage);
				element.css("color", defaultColor);
			}
			return element;
		} else {
			alert(message ? message : this.defaultMessage);
		}
	};
	/**
	 * 清除第bindIndex个绑定的消息
	 * @param int bindIndex
	 */
	this.clear = function(bindIndex) {
		$("div[not_auto_disapear_index=" + bindIndex + "]").remove();
		;
	}
}

function updateNoticePos(element) {
	var width = document.documentElement.clientWidth;
	var height = document.documentElement.clientHeight;
	var w = element.width();
	var h = element.height();
	var x = mousePos.x - w;
	var y = mousePos.y - h;
	if (x + w > width) {
		element.css("left", width - w);
	} else {
		if (x < 0) {
			element.css("left", 0);
		} else {
			element.css("left", x);
		}
	}
	if (y + h > height) {
		element.css("top", height - h);
	} else {
		if (y < 0) {
			element.css("top", 0);
		} else {
			element.css("top", y);
		}
	}
}
function refresh() {
	window.location.reload();
}

function clickToCopyOK(msg){
	notice.prompt(msg);
}
