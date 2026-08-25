var formId = "form-validator";

$(document).ready(function () {
	 initDatePicker('txdate');
    // 按下執行按鈕
    $('#btnExecute').click(function () {
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
            }
        },
        messages:{
            "txdate":{
                required: "必須有資料"
            } 			
		}
    }));
})