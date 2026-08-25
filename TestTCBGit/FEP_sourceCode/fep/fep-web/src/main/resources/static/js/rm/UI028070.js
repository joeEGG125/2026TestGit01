var formId = "form-validator";

$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });

    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            bknoRfv: {
                required: true,
            },
        },
        messages: {
            bknoRfv: {
                required: "必須有資料",
            }
        }
    }));
})