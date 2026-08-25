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
			"sysconfSubsysno":{
				maxlength:30,
				required: true
			},
            "sysconfName":{
				maxlength:30,
				required: true
			},
			"sysconfType":{
				maxlength:10
			},
			"sysconfValue":{
				maxlength:120
			},
			"sysconfRemark":{
				maxlength:100
			}
        },
        messages: {
			sysconfSubsysno: {
                required: "必須選取",
            },
            sysconfName: {
                required: "必須有資料",
            },
		}
    }));
    
	
    
})