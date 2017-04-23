/**************************************************
 * * 弹出菜单
 * * 2009-1-7
 ***************************************************/
var Menu = function() {
	this.width = 180;
	/*菜单宽度*/
	this.wraper = null;
	/*最外层对象*/
	this.currItem = null;
	/*当前被操作菜单项*/
	this.xml = null;
	/*xml文件*/
	this.xmlString = null;
	/*xml串*/
	this.mainNode = null;
	/*主菜单*/
	this.childNodes = [];
	/*所有菜单*/
	this.iTimerID = null;
	/*关闭菜单定时器*/
	this.moveCount = 0;
	/*移动次数*/
};
/*@注册事件*/
Menu.prototype.regEvent = function() {
	var self = this;
	/*单击文档关闭*/
	$(document).click(function(e) {
		var isMenu = $(e.target).attr("menu");
		if(isMenu){
			return;
		}
		if (self.iTimerID != null) {
			window.clearTimeout(self.iTimerID);
			self.iTimerID = null;
		}
		self.closeChildren(self.wraper);
	});
};
/*@设定属性*/
Menu.prototype.setAttribute = function() {
	/*设定菜单属性*/
	for ( var i = 0; i < this.childNodes.length; i++) {
		this.setMenuAttribute(this.childNodes[i]);
	}
	/*设定菜单项属性*/
	var li = $(this.wraper).find("li");
	for ( var i = 0; i < li.length; i++) {
		this.setItemAttribute(li[i]);
	}
};
/*@设定菜单属性*/
Menu.prototype.setMenuAttribute = function(menu) {
	menu.style.display = "none";
	menu.style.width = this.width + "px";
};
/*@设定菜单项属性*/
Menu.prototype.setItemAttribute = function(item) {
	var self = this;
	if (item.getAttribute("enabled") != "false") {
		$(item).mouseover(function() {
			self.mouseover($(this)[0]);
		});
		$(item).mouseout(function() {
			self.mouseout($(this)[0]);
		});
		if (item.getAttribute("child") != null) {
			item.innerHTML = "<span menu=\"true\" class=\"prevMark\">" + item.innerHTML
					+ "</span><img menu=\"true\" class=\"mark\" src=\"/img/menu_off.png\" />";
		}
	} else {
		item.style.color = "#D7CFBE";
	}
	item.className = "out";
};
/*@鼠标经过*/
Menu.prototype.mouseover = function(o) {
	this.moveCount++;
	if (this.iTimerID != null) {
		window.clearTimeout(this.iTimerID);
		this.iTimerID = null;
	}
	this.closeChildren(o.parentNode);
	o.className = "over";
	var childId = o.getAttribute("child");
	if (childId != null) {
		this.currItem = o;
		this.showChildren(o, childId);
	}
};
/*@鼠标离开*/
Menu.prototype.mouseout = function(o) {
	this.moveCount--;
	if (o != this.currItem) {
		o.className = "out";
		this.currItem = null;
	}
	var self = this;
	if (this.iTimerID == null) {
		this.iTimerID = window.setTimeout(function() {
			if (self.wraper && self.moveCount == 0) {
				self.closeChildren(self.wraper);
			}
		}, 500);
	}
};
/*@显示子菜单*/
Menu.prototype.showChildren = function(o, id) {
	var pos = getElementPos(o);
	var n = document.getElementById(id);
	$(o).find(".mark").attr("src", "/img/menu_on.png");
	if (n) {
		n.style.top = pos.y + "px";
		n.style.left = (pos.x + this.width) + "px";
		n.style.display = "";
	}
};
/*@关闭子菜单*/
Menu.prototype.closeChildren = function(parentNode) {
	$(parentNode).find(".mark").attr("src", "/img/menu_off.png");
	var li = parentNode.getElementsByTagName("li");
	var n;
	for ( var i = 0; i < li.length; i++) {
		li[i].className = "out";
		var childId = li[i].getAttribute("child");
		if (childId != null) {
			n = document.getElementById(childId);
			if (n) {
				this.closeChildren(n);
				n.style.display = "none";
			}
		}
	}
};
/*@显示主菜单*/
Menu.prototype.showByElement = function(e, align) {
	var pos = getElementPos(e);
	if (align == "left") {
		pos.y = pos.y;
	}
	if (align == "right") {
		pos.x = pos.x;
	}
	this.mainNode.style.top = pos.y + "px";
	this.mainNode.style.left = pos.x + "px";
	//this.mainNode.style.display = "inline";
	$(this.mainNode).fadeIn(200);
	var first = $(this.mainNode).children("ul")[0];
	first.className = "first";
};
/*@加载数据文件*/
Menu.prototype.loadXml = function() {
	if (this.xml) {
		var self = this;
		$.ajax( {
			url : this.xml,
			success : function(text) {
				self.xmlString = text;
				self.resolve();
			}
		});
	} else {
		this.resolve();
	}
};
/*@解析数据文件*/
Menu.prototype.resolve = function() {
	var menuId = "menu_" + Menu.getGuid();
	var xml = this.xmlString;
	var div = document.createElement("div");
	div.id = menuId;
	div.innerHTML = xml;
	document.body.appendChild(div);
	var wraper = $("#" + menuId);
	var children = wraper.children("div");
	this.wraper = wraper[0];
	this.childNodes = children;
	this.mainNode = this.childNodes[0];
	children.bgiframe();
};
/*@获取guid*/
Menu.getGuid = function() {
	var guid = new Date().getTime().toString() + "_";
	for ( var i = 1; i <= 32; i++) {
		var n = Math.floor(Math.random() * 16).toString(16);
		guid += n;
	}
	return guid;
};
/*@初始化程序*/
Menu.prototype.init = function() {
	/*加载xml*/
	this.loadXml();
	/*注册事件*/
	this.regEvent();
	/*设定属性*/
	this.setAttribute();
};

/*
** <summary>
** 获取对象位置
** </summary>
** <param name="element">对象</param>
*/
function getElementPos(element) {
	var offsetTop = element.offsetTop;
	var offsetLeft = element.offsetLeft;

	while (element = element.offsetParent) {
		offsetTop += element.offsetTop;
		offsetLeft += element.offsetLeft;
	}

	return {
		x : offsetLeft,
		y : offsetTop
	};
};