var formId = "form-validator-detail";

$(document).ready(function() {
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            logonid: {
                required: true,
            },
            username: {
                required: true,
            },
        },
        messages:{
            logonid:{
                required: "必須輸入",
            },
            username:{
                required: "必須輸入",
            },
        }
    }));
    initDatePicker('effectdate');
    initDatePicker('expireddate');
    // 按下確認按鈕
    $('#btnSave').click(function() {
        if(doValidateForm(formId)){
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
})