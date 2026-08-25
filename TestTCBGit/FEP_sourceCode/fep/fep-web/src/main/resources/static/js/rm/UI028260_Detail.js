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
    //按下上一頁按鈕
    $('#btnPrevPage1').click(function () {
        var form = {
        }
        doFormSubmit('/rm/UI_028260/index', form);
    });
})