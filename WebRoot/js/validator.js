/*-------------------------------------------------------------------------------------------
   文件名称: Validator.js
   文件作用: 表单验证
   程序设计: wangzh
   创建日期: 2008-11-28
   修改日期: 2010-04-15
   修改记录：1.除Require校验器外,输入框在没有值的情况下不进行校验
 -------------------------------------------------------------------------------------------*/
// JavaScript Document

 Validator = {
	Require : /.+/,
	Email : /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/,
	Year: /^[1-2][0-9]{3}$/,
	Phone : /^(\d{2,3}\-)?(0\d{2,3}-)?[1-9]\d{6,7}(\-\d{1,4})?$/,
	Mobile : /^(\d{2,3}\-)?1\d{10}$/,
	Url : /^http:\/\/[A-Za-z0-9]+\.[A-Za-z0-9]+[\/=\?%\-&_~`@[\]\':+!]*([^<>\"\"])*$/,
	IdCard : "this.IsIdCard(value)",
	Currency : /^\d+(\.\d+)?$/,
	Number : /^-?\d+(\.\d+)?$/,
	Zip : /^[1-9]\d{5}$/,
	QQ : /^[1-9]\d{4,8}$/,
	Integer : /^-?\d+$/,
	Uint : /^[1-9]\d*$/,
	DoubleRe : /^-?\d+(\.\d+)?$/,
	Double : "this.LimitDoubleFun(value)",
	English : /^[A-Za-z]+$/,
	Chinese :  /^[\u0391-\uFFE5]+$/,
	Username : /^[a-z]\w{3,}$/i,
	UnSafe : /^(([A-Z]*|[a-z]*|\d*|[-_\~!@#\$%\^&\*\.\(\)\[\]\{\}<>\?\\\/\'\"]*)|.{0,5})$|\s/,
	IsSafe : function(str){return !this.UnSafe.test(str);},
	SafeString : "this.IsSafe(value)",
	Filter : "this.DoFilter(value, getAttribute('accept'))",
	Limit : "this.limit(value.length, getAttribute('min'), getAttribute('max'))",
	LimitB : "this.limit(this.LenB(value), getAttribute('min'), getAttribute('max'))",
	Date : "this.NoRequire(value,getAttribute('require')) || this.IsDate(value, getAttribute('format'))",
	Repeat : "value == document.getElementsByName(getAttribute('to'))[0].value",
	Range : "this.between(value, getAttribute('min'), getAttribute('max'))",
	Compare : "this.compare(value, getAttribute('operator'), getAttribute('to'))",
	Custom : "this.Exec(value, getAttribute('regexp'))",
	Group : "this.MustChecked(getAttribute('name'), getAttribute('min'), getAttribute('max'))",
	ErrorItem : [document.forms[0]],
	ErrorMessage : ["校验失败：\t\t\t\t"],
	Validate : function(theForm, mode){
		var obj = theForm || event.srcElement;
		var count = obj.elements.length;
		this.ErrorMessage.length = 1;
		this.ErrorItem.length = 1;
		this.ErrorItem[0] = obj;
		
		this.removeEvent(obj, "reset", this.ClearAllState);
		for(var i=0;i<count;i++){
			with(obj.elements[i]){
				this.ClearState(obj.elements[i]);
				if(getAttribute("trim") == "true"){
					try{
						value = value.trim();
					}catch(e){
					}
				}
				if(getAttribute("require") != null && getAttribute("require").toLowerCase() == "true" && value == ""){
					this.AddError(i, getAttribute("title")+"不能为空。");
					continue;
				}
				
				// 没有值的不用校验
				if(value == "") continue;
						
				var _dataType = getAttribute("dataType");
				if(typeof(_dataType) == "object" || typeof(this[_dataType]) == "undefined")  continue;
				
				switch(_dataType){
					case "IdCard" :
					case "Date" :
					case "Repeat" :
					case "Range" :
					case "Compare" :
					case "Custom" :
					case "Group" :
					case "Limit" :
					case "LimitB" :
					case "SafeString" :
					case "Filter" :
					case "Double" :
						if(!eval(this[_dataType]))	{
							this.AddError(i, getAttribute("msg"));
						}
						break;
					default :
						if(!this[_dataType].test(value)){
							this.AddError(i, getAttribute("msg"));
						}
						break;
				}
			}
		}
		if(this.ErrorMessage.length > 1){
			this.addEvent(obj, "reset", this.ClearAllState);
			mode = mode || 1;
			var errCount = this.ErrorItem.length;
			switch(mode){
			case 2 :
				for(var i=1;i<errCount;i++)
					this.ErrorItem[i].style.color = "red";
				// 可能控件不可见，抛弃异常
				try{
					this.ErrorItem[1].focus();
				}catch(e){}
				break;
			case 1 :
				for(var i=1;i<errCount;i++)
					this.ErrorItem[i].style.color = "red";
				alert(this.ErrorMessage.join("\n"));
				// 可能控件不可见，抛弃异常
				try{
					this.ErrorItem[1].focus();
				}catch(e){}
				break;
			case 3 :
				for(var i=1;i<errCount;i++){
				try{
					var span = document.createElement("SPAN");
					span.name = "__ErrorMessagePanel";
					span.style.color = "red";
					this.ErrorItem[i].parentNode.appendChild(span);
					span.innerHTML = this.ErrorMessage[i].replace(/\d+:/," ");
					}
					catch(e){alert(e.description);}
				}
				// 可能控件不可见，抛弃异常
				try{
					this.ErrorItem[1].focus();
				}catch(e){}
				break;
			case 4 :
				for(var i=1;i<errCount;i++)
					this.ErrorItem[i].style.color = "red";
				notice.prompt(this.ErrorMessage.join("\n"));
				// 可能控件不可见，抛弃异常
				try{
					this.ErrorItem[1].focus();
				}catch(e){}
				break;
			default :
				alert(this.ErrorMessage.join("\n"));
				break;
			}
			return false;
		}
		return true;
	},
	// 检查单个控件
	ValidateSingle : function(theForm, mode, element){
		var obj = element || event.srcElement;
		var errorLength = 0;	// 错误数，检查单个控件一般不提供消息提示。
		this.ErrorItem[0] = theForm || this.ErrorItem[0];
		
		this.removeEvent(theForm, "reset", this.ClearAllState);
		with(obj){
			if(getAttribute("trim") == "true"){
				try{
					value = value.trim();
				}catch(e){
				}
			}
			if(getAttribute("require") != null && getAttribute("require").toLowerCase() == "true" && value == ""){
				errorLength = 1;
				this.ErrorItem[1] = obj;
				this.ClearState(obj);
			}else{
				// 没有值的不用校验
				if(value == "") return true;
				var _dataType = getAttribute("dataType");
				if(typeof(_dataType) == "object" || typeof(this[_dataType]) == "undefined") return true;
				this.ClearState(obj);
				switch(_dataType){
					case "IdCard" :
					case "Date" :
					case "Repeat" :
					case "Range" :
					case "Compare" :
					case "Custom" :
					case "Group" :
					case "Limit" :
					case "LimitB" :
					case "SafeString" :
					case "Filter" :
					case "Double" :
						if(!eval(this[_dataType]))	{
							errorLength = 1;
							this.ErrorItem[1] = obj;
						}
						break;
					default :
						if(!this[_dataType].test(value)){
							errorLength = 1;
							this.ErrorItem[1] = obj;
						}
						break;
				}
			}
		}

		if(errorLength > 0){
			this.addEvent(theForm, "reset", this.ClearAllState);
			mode = mode || 1;
			switch(mode){
			case 2 :
				try{
					this.ErrorItem[1].style.color = "red";
					this.ErrorItem[1].focus();
				}catch(e){}
				break;
			case 1 :
				try{
					this.ErrorItem[1].style.color = "red";
					this.ErrorItem[1].focus();
					alert(this.ErrorMessage.join("\n"));
				}catch(e){}
				break;
			case 3 :
				try{
					var span = document.createElement("SPAN");
					span.name = "__ErrorMessagePanel";
					span.style.color = "red";
					this.ErrorItem[1].parentNode.appendChild(span);
					span.innerHTML = this.ErrorMessage[0].replace(/\d+:/," ");
					this.ErrorItem[1].focus();
				}catch(e){alert(e.description);}
				break;
			default :
				alert(this.ErrorMessage.join("\n"));
				break;
			}
			return false;
		}
		return true;
	},
	autoAddMsg : function(){
		var obj = document.forms[0];
		var count = obj.elements.length;
		for(var i=0;i<count;i++){
			with(obj.elements[i]){
				if(getAttribute("msg") == null || getAttribute("msg") == ""){
					var _dataType = getAttribute("dataType");
					switch(_dataType){
						case "Integer":
							setAttribute("msg",getAttribute("title")+"必须是整数。");
							break;
						case "Currency":
							setAttribute("msg",getAttribute("title")+"必须是货币。");
							break;
						case "Double":
							setAttribute("msg",getAttribute("title")+"必须是数值。且整数部分不得长于14位，小数部分不得长于6位。");
							break;
						case "Date":
							setAttribute("msg",getAttribute("title")+"必须符合日期格式,例：2007-01-02");
							break;
						case "Email":
							setAttribute("msg",getAttribute("title")+"必须是Email。");
							break;
						case "Phone":
							setAttribute("msg",getAttribute("title")+"必须是电话号码。");
							break;
						case "Mobile":
							setAttribute("msg",getAttribute("title")+"必须是手机号码。");
							break;
						case "Url":
							setAttribute("msg",getAttribute("title")+"必须是URL。");
							break;
						case "Zip":
							setAttribute("msg",getAttribute("title")+"必须是邮政编码。");
							break;
						case "Chinese":
							setAttribute("msg",getAttribute("title")+"必须是中文。");
							break;
						case "English":
							setAttribute("msg",getAttribute("title")+"必须是英文。");
							break;
						case "Limit" :
							setAttribute("msg",getAttribute("title")+"必须在"+getAttribute("max")+"个字之内。（中文为1个字）");
							break;
						case "LimitB" :
							setAttribute("msg",getAttribute("title")+"必须在"+getAttribute("max")+"个字节之内。（中文为2个字节）");
							break;
						case "IdCard" :
						case "Date" :
						case "Repeat" :
						case "Range" :
						case "Compare" :
						case "Custom" :
						case "Group" :
						case "SafeString" :
						case "Filter" :
						default :
							setAttribute("msg",getAttribute("title")+"不能为空或者格式不正确。");
							break;
					}
				}
			}
		}
	},
	// 校验数据源相关控件
	ValidateOnDsName : function(theForm, mode,dsName){
		// dci_form_dataland.js
		if(!ValidateXmlCurRow(dsName)) return true;
		var obj = theForm || event.srcElement;
		var count = obj.elements.length;
		this.ErrorMessage.length = 1;
		this.ErrorItem.length = 1;
		this.ErrorItem[0] = obj;
		
		this.removeEvent(obj, "reset", this.ClearAllState);
		for(var i=0;i<count;i++){
			if(obj.elements[i].datasource == dsName){
				with(obj.elements[i]){
					if(getAttribute("trim") == "true"){
						try{
							value = value.trim();
						}catch(e){
						}
					}
					if(getAttribute("require") != null && getAttribute("require").toLowerCase() == "true" && value == ""){
						this.AddError(i, getAttribute("title")+"不能为空。");
						continue;
					}
					
					// 没有值的不用校验
					if(value == "") continue;
									
					var _dataType = getAttribute("dataType");
					if(typeof(_dataType) == "object" || typeof(this[_dataType]) == "undefined")  continue;
					this.ClearState(obj.elements[i]);
					switch(_dataType){
						case "IdCard" :
						case "Date" :
						case "Repeat" :
						case "Range" :
						case "Compare" :
						case "Custom" :
						case "Group" :
						case "Limit" :
						case "LimitB" :
						case "SafeString" :
						case "Filter" :
						case "Double" :
							if(!eval(this[_dataType]))	{
								this.AddError(i, getAttribute("msg"));
							}
							break;
						default :
							if(!this[_dataType].test(value)){
								this.AddError(i, getAttribute("msg"));
							}
							break;
					}
				}
			}
		}
		if(this.ErrorMessage.length > 1){
			this.addEvent(obj, "reset", this.ClearAllState);
			mode = mode || 1;
			var errCount = this.ErrorItem.length;
			switch(mode){
			case 2 :
				for(var i=1;i<errCount;i++)
					this.ErrorItem[i].style.color = "red";
				// 可能控件不可见，抛弃异常
				try{
					this.ErrorItem[1].focus();
				}catch(e){}
				break;
			case 1 :
				for(var i=1;i<errCount;i++)
					this.ErrorItem[i].style.color = "red";
				alert(this.ErrorMessage.join("\n"));
				// 可能控件不可见，抛弃异常
				try{
					this.ErrorItem[1].focus();
				}catch(e){}
				break;
			case 3 :
				for(var i=1;i<errCount;i++){
				try{
					var span = document.createElement("SPAN");
					span.name = "__ErrorMessagePanel";
					span.style.color = "red";
					this.ErrorItem[i].parentNode.appendChild(span);
					span.innerHTML = this.ErrorMessage[i].replace(/\d+:/," ");
					}
					catch(e){alert(e.description);}
				}
				// 可能控件不可见，抛弃异常
				try{
					this.ErrorItem[1].focus();
				}catch(e){}
				break;
			default :
				alert(this.ErrorMessage.join("\n"));
				break;
			}
			return false;
		}
		return true;
	},
	limit : function(len, min, max){
		min = min || 0;
		max = max || Number.MAX_VALUE;
		return min <= len && len <= max;
	},
	between : function(v, min, max){
		var reg = /^-?\d+(\.\d+)?$/;
		if(reg.test(min) && reg.test(max)){
			if(!reg.test(v)) return false;
			min = new Number(min);
			max = new Number(max);
			v = new Number(v);
			return v <= max && v >= min;
		}else{
			return v <= max && v >= min;
		}
	},
	LenB : function(str){
		return str.replace(/[^\x00-\xff]/g,"**").length;
	},
	ClearAllState : function(){
		for(var i=1;i<Validator.ErrorItem.length;i++){
			with(Validator.ErrorItem[i]){
				if(style.color == "red")
					style.color = "";
				var lastNode = parentNode.childNodes[parentNode.childNodes.length-1];
				if(lastNode.name == "__ErrorMessagePanel")
					parentNode.removeChild(lastNode);
			}
		}
	},
	ClearState : function(elem){
		with(elem){
			if(style.color == "red")
				style.color = "";
			var lastNode = parentNode.childNodes[parentNode.childNodes.length-1];
			if(lastNode.name == "__ErrorMessagePanel")
				parentNode.removeChild(lastNode);
		}
	},
	AddError : function(index, str){
		this.ErrorItem[this.ErrorItem.length] = this.ErrorItem[0].elements[index];
		this.ErrorMessage[this.ErrorMessage.length] = this.ErrorMessage.length + ":" + str;
	},
	Exec : function(op, reg){
		return new RegExp(reg,"g").test(op);
	},
	compare : function(op1,operator,op2){
		switch (operator) {
			case "NotEqual":
				return (op1 != op2);
			case "GreaterThan":
				return (op1 > op2);
			case "GreaterThanEqual":
				return (op1 >= op2);
			case "LessThan":
				return (op1 < op2);
			case "LessThanEqual":
				return (op1 <= op2);
			default:
				return (op1 == op2);
		}
	},
	MustChecked : function(name, min, max){
		var groups = document.getElementsByName(name);
		var hasChecked = 0;
		min = min || 1;
		max = max || groups.length;
		for(var i=groups.length-1;i>=0;i--)
			if(groups[i].checked) hasChecked++;
		return min <= hasChecked && hasChecked <= max;
	},
	DoFilter : function(input, filter){
		return new RegExp("^.+\.(" + filter.split(/\s*,\s*/).join("|")+ ")$", "gi").test(input);
	},
	IsIdCard : function(number){
		var date, Ai;
		var verify = "10x98765432";
		var Wi = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
		var area = ['','','','','','','','','','','','北京','天津','河北','山西','内蒙古','','','','','','辽宁','吉林','黑龙江','','','','','','','','上海','江苏','浙江','安微','福建','江西','山东','','','','河南','湖北','湖南','广东','广西','海南','','','','重庆','四川','贵州','云南','西藏','','','','','','','陕西','甘肃','青海','宁夏','新疆','','','','','','台湾','','','','','','','','','','香港','澳门','','','','','','','','','国外'];
		var re = number.match(/^(\d{2})\d{4}(((\d{2})(\d{2})(\d{2})(\d{3}))|((\d{4})(\d{2})(\d{2})(\d{3}[x\d])))$/i);
		if(re == null) return false;
		if(re[1] >= area.length || area[re[1]] == "") return false;
		if(re[2].length == 12){
			Ai = number.substr(0, 17);
			date = [re[9], re[10], re[11]].join("-");
		}
		else{
			Ai = number.substr(0, 6) + "19" + number.substr(6);
			date = ["19" + re[4], re[5], re[6]].join("-");
		}
		if(!this.IsDate(date, "ymd")) return false;
		var sum = 0;
		for(var i = 0;i<=16;i++){
			sum += Ai.charAt(i) * Wi[i];
		}
		Ai +=  verify.charAt(sum%11);
		return (number.length ==15 || number.length == 18 && number == Ai);
	},
	IsDate : function(op, formatString){
		formatString = formatString || "ymd";
		var m, year, month, day, hours = 0, minutes = 0, seconds = 0;
		switch(formatString){
			case "ymd" :
				m = op.match(new RegExp("^(\\d{4})(\\-(\\d{2})(\\-(\\d{2}))?)?$"));
				if(m == null ) return false;
				day = m[5]?m[5]:1;
				month = m[3]?parseInt(m[3]):1;
				year =  parseInt(m[1], 10);
				break;
			case "full" :
				m = op.match(new RegExp("^(\\d{4})(\\-(\\d{2})(\\-(\\d{2})(\\s{1}(\\d{2}):(\\d{2})(:(\\d{2}))?)?)?)?$"));
				if(m == null ) return false;
				day = m[5]?m[5]:1;
				month = m[3]?parseInt(m[3]):1;
				year =  parseInt(m[1], 10);
				hours = m[7]?m[7]:0;
				minutes = m[8]?m[8]:0;
				seconds = m[10]?m[10]:0;
				break;
			case "yyyymmdd" :
				m = op.match(new RegExp("^(\\d{4})\\-(\\d{2})\\-(\\d{2})$"));
				if(m == null ) return false;
				day = m[3]?m[3]:1;
				month = m[2]?parseInt(m[2]):1;
				year =  parseInt(m[1], 10);
				break;
			case "dmy" :
				m = op.match(new RegExp("^(\\d{2})\\-(\\d{2})\\-(\\d{4})$"));
				if(m == null ) return false;
				day = m[1];
				month = parseInt(m[2]);
				year = parseInt(m[3], 10);
				break;
			default :
				break;
		}
		month = month==0 ?12:month;
		var date = new Date(year, month-1, day, hours, minutes, seconds);
        return (typeof(date) == "object" && year == date.getFullYear() && month == (date.getMonth()+1) && day == date.getDate() && hours == date.getHours() && minutes == date.getMinutes() && seconds == date.getSeconds());
	},
	NoRequire : function(value,strReq){
		if(value == "" && (strReq==null || strReq=="" || strReq.toLowerCase()=="false")) return true;
	},
	LimitDoubleFun : function(value){
		if(this.DoubleRe.test(value)){
			var str = ""+value;
			var pos = str.indexOf(".");
			if(pos==-1){
				if(str.length>14) return false;
			}else{
				// 整数部分长度限制
				var str1 = str.substring(0,pos);
				if(str1.length>14) return false;
				// 小数部分长度限制
				var str2 = str.substring(pos+1,str.length);
				if(str2.length>6) return false;
			}
			return true;
		}
		return false;
	},
	addEvent : function (obj, eventName, fun) {
		//为对象追加事件
		if (obj.addEventListener) {
			obj.addEventListener(eventName, fun, false);
		} else if (obj.attachEvent) {
			obj.attachEvent("on" + eventName, fun);
		}
	},
	removeEvent : function (obj, eventName, fun) {
		//解除为对象追加的事件
		if (obj.removeEventListener) {
			obj.removeEventListener(eventName, fun, false);
		} else if (obj.detachEvent) {
			obj.detachEvent("on" + eventName, fun);
		}
	}
 }
 
String.prototype.trim = function() {
	// 用正则表达式将前后空格
	// 用空字符串替代。
	return this.replace(/(^\s*)|(\s*$)/g, "");
}
