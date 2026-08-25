var formId = "form-validator";
$(document).ready(function () {
    initDatePicker('batchExecuteDate');
    // 建立表單驗證
    var validatorOption = getValidFormOptinal({
        rules: {
            batchExecuteDate: {
                required: true,
                dateISO: true,
            }
        }
    });
    $('#' + formId).validate(validatorOption);
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // Grid中應執行時間 onclick
    $('.a-inquiry').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/batch/UI_000120/btnTime', form);
    });
});