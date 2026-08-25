var formId = "form-validator";

$(document).ready(function() {
	// 按下查詢按鈕
//	var btnQuery = $('#btnQuery');
//	if (btnQuery.length > 0) {
//		btnQuery.click(function() {
//			showLoading(true);
//			showProcessingMessage(true);
//			$('#' + formId).submit();
//		});
//	}


	$('#btnQuery').click(function() {
	    var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        showLoading(true);
        showProcessingMessage(true);
        doFormSubmit('/rm/UI_028110/inquiryLog', form);
    });

    $('#btnQuery1').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        showLoading(true);
        showProcessingMessage(true);
        doFormSubmit('/rm/UI_028110/inquiryLog1', form);
    });

    $('#btnQuery2').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        showLoading(true);
        showProcessingMessage(true);
        doFormSubmit('/rm/UI_028110/inquiryLog2', form);
    });

    $('#btnQuery3').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        showLoading(true);
        showProcessingMessage(true);
        doFormSubmit('/rm/UI_028110/inquiryLog3', form);
    });
})