//checkbox联合选择,支持全选、全部取消
function UnionCheck(options) {
	//初始化参数
	this.check_all = options["check_all"];
	this.check_one = options["check_one"];
	this.valid_tip = options["valid_tip"];
	this.invalid_tip = options["invalid_tip"];
	this.tip = options["tip"] ? options["tip"] : "tip";
	this.title = options["title"] ? options["title"] : "改变状态失败";
	//绑定checkbox的点击事件
	this.bind = function(options) {
		if(options){
			var url = options["url"];
			var all_params_name = options["all_params_name"];
			var one_params_name = options["one_params_name"];
			var value_name = options["value_name"];
			var checked_value = options["checked_value"];
			var unchecked_value = options["unchecked_value"];
		}
		var c = this;
		$("#" + this.check_all).click(function() {
			var checkbox = $(this)[0];
			if(options){
				var value = checkbox.checked ? checked_value : unchecked_value;
				var checkboxs = $("[name=" + c.check_one + "]:enabled");
				var all_params = [];
				var params = {};
				params[all_params_name] = all_params;
				params[value_name] = value;
				for ( var i = 0; i < checkboxs.length; i++) {
					if (checkbox.checked != checkboxs[i].checked) {
						all_params[all_params.length] = checkboxs[i].value;
					}
				}
				if (all_params.length > 0) {
					c.checkAll(checkbox, url, params);
				}
			}else{
				var checkboxs = $("[name=" + c.check_one + "]:enabled");
				for ( var i = 0; i < checkboxs.length; i++) {
					if (checkboxs[i].checked != checkbox.checked) {
						if (c.tip != "notip") {
							var val = checkboxs[i].value;
							if (checkbox.checked) {
								$("#" + c.tip + "_" + val).html(c.valid_tip);
							} else {
								$("#" + c.tip + "_" + val).html(c.invalid_tip);
							}
						}
						checkboxs[i].checked = checkbox.checked;
					}
				}
				if (checkbox.checked) {
					$(checkbox).prop("indeterminate", false);
					$(checkbox).prop("checked", true);
				} else {
					$(checkbox).prop("indeterminate", false);
					$(checkbox).prop("checked", false);
				}
			}
		});
		$("input[name=" + this.check_one + "]").click(function() {
			if(options){
				var checkbox = $(this)[0];
				var value = checkbox.checked ? checked_value : unchecked_value;
				var params = {};
				params[one_params_name] = checkbox.value;
				params[value_name] = value;
				c.checkOne(checkbox, url, params);
			}else{
				c.checkState();
			}
		});
	};
	//检查勾选状态
	this.checkState = function() {
		var checked = true;
		var unchecked = true;
		var checkboxs = $("[name=" + this.check_one + "]:enabled");
		for ( var i = 0; i < checkboxs.length; i++) {
			if (checkboxs[i].checked) {
				unchecked = false;
			} else {
				checked = false;
			}
		}
		if (checked) {
			$("#" + this.check_all).prop("indeterminate", false);
			$("#" + this.check_all).prop("checked", true);
		} else {
			if (unchecked) {
				$("#" + this.check_all).prop("indeterminate", false);
				$("#" + this.check_all).prop("checked", false);
			} else {
				$("#" + this.check_all).prop("indeterminate", true);
				$("#" + this.check_all).prop("checked", false);
			}
		}
	};

	//全选或取消全选
	this.checkAll = function(checkbox, url, params) {
		var checkboxs = $("[name=" + this.check_one + "]:enabled");
		var c = this;
		$.getJSON(url, params, function(json) {
			if (!json || !json.retCode) {
				for ( var i = 0; i < checkboxs.length; i++) {
					if (checkboxs[i].checked != checkbox.checked) {
						if (c.tip != "notip") {
							var val = checkboxs[i].value;
							if (checkbox.checked) {
								$("#" + c.tip + "_" + val).html(c.valid_tip);
							} else {
								$("#" + c.tip + "_" + val).html(c.invalid_tip);
							}
						}
						checkboxs[i].checked = checkbox.checked;
					}
				}
				if (checkbox.checked) {
					$(checkbox).prop("indeterminate", false);
					$(checkbox).prop("checked", true);
				} else {
					$(checkbox).prop("indeterminate", false);
					$(checkbox).prop("checked", false);
				}
			} else {
				checkbox.checked = !checkbox.checked;
				notice.prompt(c.title + "：" + json.retMsg);
			}
		});
	};

	//选中一个
	this.checkOne = function(checkbox, url, params) {
		var c = this;
		$.getJSON(url, params,
				function(json) {
					if (!json || !json.retCode) {
						if (c.tip != "notip") {
							if (checkbox.checked) {
								$("#" + c.tip + "_" + checkbox.value).html(
										c.valid_tip);
							} else {
								$("#" + c.tip + "_" + checkbox.value).html(
										c.invalid_tip);
							}
						}
						c.checkState();
					} else {
						checkbox.checked = !checkbox.checked;
						notice.prompt(c.title + "：" + json.retMsg);
					}
				});
	};
}
