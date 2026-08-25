var formId = "form-validator";

$(document).ready(function() {
    initDatePicker('lblStDate');
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
            lblStDate: {
                required: true,
                dateISO: true,
            },
        }
    }));
})