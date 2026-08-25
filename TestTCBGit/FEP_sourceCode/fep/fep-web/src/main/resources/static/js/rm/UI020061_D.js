var formId = "form-validator";

$(document).ready(function () {
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // 按下清除按鈕
    $('#btnClear1').click(function () {
        window.location.reload()
    });
})