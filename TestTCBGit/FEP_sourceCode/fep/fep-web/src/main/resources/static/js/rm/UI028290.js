var formId = "form-validator";

$(document).ready(function() {
    // 按下執行按鈕
    $('#btnConfirm').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
})