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

	//Grid中 來源通道 超連結
	$('.msgfileChannel').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/atmmon/UI_060630/bindGridDetail', form);
	});	
});