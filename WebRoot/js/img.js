//等比例缩放图片
function drawImg(img, boxWidth, boxHeight) {
	if (!img.src) {
		return;
	}
	var imgWidth = $(img).width();
	var imgHeight = $(img).height();
	//比较imgBox的长宽比与img的长宽比大小
	if ((imgWidth * boxHeight) > (boxWidth * imgHeight)) {
		// 对高度进行缩放
		$(img).width(boxWidth);
		$(img).height(Math.floor((boxWidth * imgHeight) / imgWidth));
	} else {
		// 对宽度进行缩放
		$(img).width(Math.floor((boxHeight * imgWidth) / imgHeight));
		$(img).height(boxHeight);
	}
	$(img).css("visibility", "visible");
}

