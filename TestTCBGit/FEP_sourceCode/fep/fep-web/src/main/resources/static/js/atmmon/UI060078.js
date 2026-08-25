var formId = "form-validator";
$(document).ready(function() {
	initDatePicker('sysstatTbsdyFisc');
	initDatePicker('zoneZoneTbsdy');
	//$('#zoneZoneTbsdy').focus();
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});
	// Grid中交易別超連結
	$('.atmcTxCode').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/atmmon/UI_060078/bindGridDetail', form);
	});	
	// 按下清除鈕
	$('#btnClear').click(function(){
		$('#txCode').val("");
		$('#cur').val("");
		$('#atmNo').val("");
	});
})