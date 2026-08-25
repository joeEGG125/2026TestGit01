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
	// 按下新增按鈕
	$('#btnInsert').click(function() {
		showLoading(true);
		showProcessingMessage(true);
		var form = {
			'actionType':'I'
		}
		doFormSubmit('/dbmaintain/UI_070060/insertClick', form);
	});
	//Grid中 來源通道 超連結
	$('.msgfileChannel').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/dbmaintain/UI_070060/bindGridDetail', form);
	});	
});