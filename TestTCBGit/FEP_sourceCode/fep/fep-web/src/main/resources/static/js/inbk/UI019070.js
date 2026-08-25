var formId = "form-validator";

$(document).ready(function() {
	initDatePicker('dttxDATE');
	initDatePicker('dttxDATEe');
    $('#btnQuery').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    
    // Grid中第一列查詢按鈕
	$('.a-inquiry').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/inbk/UI_019070/inquiryDetail', form);
	});
    
})