var formId = "form-validator";

$(document).ready(function() {
    initDatePicker('clearDate');
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
            "apId":{
                required: true,
                rangelength:[4,4]
            }
        },
        messages:{
            "apId":{
                required: "跨行業務代號不能為空",
                rangelength:"格式錯誤"
            }
        }
    }));

})