var formId = "form-validator";

$(document).ready(function() {
   initDatePicker('txdate');
    // 按下查詢按鈕
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
            "txdate":{
                required: true
            },
 			"ioflag": {
                min:0
            }
        },
        messages:{
            "txdate":{
                required: "必須有資料"
            },
 			"ioflag": {
                min: "必須選取"
       		}
		}
    }));

})