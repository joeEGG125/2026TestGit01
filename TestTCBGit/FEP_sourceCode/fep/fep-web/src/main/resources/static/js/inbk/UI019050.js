var formId = "form-validator";

$(document).ready(function() {
    initDatePicker('txdate');
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // Grid中第一列按序號查詢按鈕
    $('.a-inquiry').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/inbk/UI_019050/ShowDetail', form);
    });
    // Grid中第一列按序號查詢按鈕
    $('.a-inquiry1').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/inbk/UI_019050/inquiryFeplogList', form);
    });
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            txdate: {
                required: true,
                dateISO: true,
            },
        }
    }));
})