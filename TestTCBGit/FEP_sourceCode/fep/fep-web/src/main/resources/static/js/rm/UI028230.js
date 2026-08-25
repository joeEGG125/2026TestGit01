$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
			var form = {
		        "": "",
		    };
			doFormSubmit('/rm/UI_028230/queryClick', form);
    });

  // Grid中第一列查詢按鈕
    $('.btn-inquiry').click(function() {
		var value = $(this).attr("value");
    	var form = jsonStringToObj(value);
    	doFormSubmit('/rm/UI_028230/inquiryDetail', form);
    });
})