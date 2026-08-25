var formId = "form-validator";

$(document).ready(function () {
    initDatePicker('tradingDate');
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
})