var formId = "form-validator";

$(document).ready(function() {
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});
	// Grid中第一列執行按鈕
	$('.btnStartBatch').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		showConfirmDialog('cmnConfirm', '是否啟動此批次？', function() {
			// Grid中第一列執行按鈕
			doFormSubmit('/batch/UI_000110/btnExecute', form);
		});
	});
	// Grid中第八列時間 onclick
	$('.a-inquiry').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/batch/UI_000120/btnTime', form);
	});
})