var formId = "form-validator";

$(document).ready(function() {
    initDatePicker('datetime');
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
        rules: {
            datetime: {
                required: true,
                dateISO: true,
            },
        }
    }));
})