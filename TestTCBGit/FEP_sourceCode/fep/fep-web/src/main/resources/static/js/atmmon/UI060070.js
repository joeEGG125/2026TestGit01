var formId = "form-validator";

$(document).ready(function() {
    $('#btnQuery').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules:{
            "sysconfName":{
				maxlength:30
			}
        },
    }));
    
    // Grid中第一列查詢按鈕
	$('.a-inquiry').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		
		doFormSubmit('/atmmon/UI_060070/inquiryDetail', form);
	});
	
    
})