
//\u53bb\u9664\u5de6\u8fb9\u7684\u7a7a\u683c\uff0c\u5305\u62ec\u5168\u89d2\u7a7a\u683c
function LTrim(str) {
	return str.replace(/(^[\s|\u3000]*)/g, "");
}

//\u53bb\u9664\u53f3\u8fb9\u7684\u7a7a\u683c\uff0c\u5305\u62ec\u5168\u89d2\u7a7a\u683c
function RTrim(str) {
	return str.replace(/([\s|\u3000]*$)/g, "");
}

//\u53bb\u9664\u524d\u540e\u7a7a\u683c\uff0c\u5305\u62ec\u5168\u89d2\u7a7a\u683c
function Trim(str) {
	return str.replace(/(^[\s|\u3000]*)|([\s|\u3000]*$)/g, "");
}
String.prototype.trim = function () {
	// \u7528\u6b63\u5219\u8868\u8fbe\u5f0f\u5c06\u524d\u540e\u7a7a\u683c
	// \u7528\u7a7a\u5b57\u7b26\u4e32\u66ff\u4ee3\u3002
	return this.replace(/(^\s*)|(\s*$)/g, "");
};
String.prototype.JLen = function () {
	//\u7528\u6b63\u5219\u8868\u8fbe\u5f0f\u7b97\u957f\u5ea6\uff0c\u4e2d\u6587\u5168\u89d2\u5360\u4e24\u4f4d
	return this.replace(/[^\x00-\xff]/g, "AA").length;
};

