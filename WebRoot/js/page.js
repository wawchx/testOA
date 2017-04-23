//利用scroll翻页
function ScrollPage(id) {
	//初始化参数
	this.id = id;
	this.pageNo = 1;
	this.pageCount = 0;
	this.height = 0;
	//初始化方法
	this.start = function() {
		this.height = $("#" + this.id).height();
		var scrollHeight = document.getElementById(this.id).scrollHeight;
		this.pageCount = parseInt((scrollHeight + this.height - 1)
				/ this.height);
		this.pageCount = isNaN(this.pageCount) ? 0 : this.pageCount;
		this.turnPage(1);
	};

	//向前翻一页
	this.pageUp = function() {
		if (this.pageNo <= 1) {
			return;
		}
		this.turnPage(--(this.pageNo));
	};

	//向后翻一页
	this.pageDown = function() {
		if (this.pageNo >= this.pageCount) {
			return;
		}
		this.turnPage(++(this.pageNo));
	};

	//翻到第一页
	this.firstPage = function() {
		this.turnPage(1);
	};

	//翻到最后一页
	this.lastPage = function() {
		this.turnPage(this.pageCount);
	};

	//转到第n页
	this.turnPage = function(p) {
		if (this.pageCount < 1) {
			return;
		}
		if (!p || p < 1) {
			p = 1;
		}
		if (p > this.pageCount) {
			p = this.pageCount;
		}
		this.pageNo = p;
		$("#" + this.id).scrollTop((this.pageNo - 1) * this.height);
		if (this.pageNo == 1) {
			//到达第一页
			if ("function" == typeof this.disenablePageUp) {
				this.disenablePageUp();
			}
		} else {
			//离开第一页
			if ("function" == typeof this.enablePageUp) {
				this.enablePageUp();
			}
		}
		if (this.pageNo == this.pageCount) {
			//到达最后一页
			if ("function" == typeof this.disenablePageDown) {
				this.disenablePageDown();
			}
		} else {
			//离开最后一页
			if ("function" == typeof this.enablePageDown) {
				this.enablePageDown();
			}
		}
	};
}

//纯js分页
function Page(id) {
	//初始化参数
	this.id = id;
	this.pageNo = 1;
	this.pageCount = 0;
	this.data = [];
	this.map = [];
	//初始化方法
	this.start = function() {
		if (!this.data && this.data.length < 1) {
			return;
		}
		var s_html = "";
		var sub_index = 0;
		var height = $("#" + this.id).height();
		$("#" + this.id).height("auto");
		for ( var i = 0; i < this.data.length; i++) {
			var html = this.appendData(sub_index++, i);
			var h = $("#" + this.id).height();
			if (h < height) {
				s_html += html;
				this.map[this.pageCount] = s_html;
			} else {
				sub_index = 0;
				this.pageCount++;
				s_html = this.appendData(sub_index++, i);
				this.map[this.pageCount] = s_html;
				$("#" + this.id).html(s_html);
			}
		}
		this.data = null;
		this.pageCount++;
		$("#" + this.id).height(height);
		this.turnPage(this.pageNo);
	};

	//向前翻一页
	this.pageUp = function() {
		if (this.pageNo <= 1) {
			return;
		}
		this.turnPage(--(this.pageNo));
	};

	//向后翻一页
	this.pageDown = function() {
		if (this.pageNo >= this.pageCount) {
			return;
		}
		this.turnPage(++(this.pageNo));
	};

	//翻到第一页
	this.firstPage = function() {
		this.turnPage(1);
	};

	//翻到最后一页
	this.lastPage = function() {
		this.turnPage(this.pageCount);
	};

	//转到第n页
	this.turnPage = function(p) {
		if (this.pageCount < 1) {
			return;
		}
		if (!p || p < 1) {
			p = 1;
		}
		if (p > this.pageCount) {
			p = this.pageCount;
		}
		this.pageNo = p;
		$("#" + this.id).html(this.map[this.pageNo - 1]);
		if (this.pageNo == 1) {
			//到达第一页
			if ("function" == typeof this.disenablePageUp) {
				this.disenablePageUp();
			}
		} else {
			//离开第一页
			if ("function" == typeof this.enablePageUp) {
				this.enablePageUp();
			}
		}
		if (this.pageNo == this.pageCount) {
			//到达最后一页
			if ("function" == typeof this.disenablePageDown) {
				this.disenablePageDown();
			}
		} else {
			//离开最后一页
			if ("function" == typeof this.enablePageDown) {
				this.enablePageDown();
			}
		}
		//更改页面内容
		if ("function" == typeof this.writePage) {
			this.writePage();
		}
	};
}

//后台分页
function ServerPage(action, pageCount, pageParameter, pageNo, writePage) {
	this.action = action;
	this.pageParameter = pageParameter;
	this.pageNo = pageNo;
	this.pageCount = pageCount;
	this.writePage = writePage;
	//向前翻一页
	this.pageUp = function() {
		this.turnPage(--(this.pageNo));
	};

	//向后翻一页
	this.pageDown = function() {
		this.turnPage(++(this.pageNo));
	};
	this.turnPage = function(p) {
		if (!this.action) {
			return;
		}
		if (!p || p < 1) {
			p = 1;
		}
		if (!this.pageCount) {
			return;
		}
		this.writePage("正在加载...");
		if (p > this.pageCount) {
			p = this.pageCount;
		}
		this.pageNo = p;
		var params = {};
		params[this.pageParameter] = this.pageNo;
		//ajax分页
		var writePage = this.writePage;
		$.ajax( {
			url : this.action,
			data : params,
			dataType : "text",
			cache: false,
			success : function(data) {
				writePage(data);
			}
		});
	};
	this.sort = function(url){
		this.action = url;
		this.writePage("正在加载...");
		//ajax分页
		var writePage = this.writePage;
		$.ajax( {
			url : this.action,
			dataType : "text",
			cache: false,
			success : function(data) {
				writePage(data);
			}
		});
	};
	this.refresh = function(){
		this.turnPage(this.pageNo);
	};
	this.ajaxForm = function(form) {
		var writePage = this.writePage;
		var options = {
			dataType : 'text',
			cache: false,
			beforeSubmit : function(arr, form, options) {
				var ret = Validator.Validate(form[0], 3);
				if(ret){
					writePage("正在加载...");
				}
				return ret;
			},
			success : function(data, statusText) {
				writePage(data);
			},
			error : function() {
				notice.prompt('加载页面失败');
			}
		};
		if (form) {
			form.ajaxForm(options);
		} else {
			$('form[page_form]').ajaxForm(options);
		}
	};
}
