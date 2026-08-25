var formId = "form-validator";

$(document).ready(function() {
    initDatePicker('beginDate');
    initDatePicker('endDate');
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // Grid中第一列查詢按鈕
    $('.a-inquiry').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/inbk/UI_019060/queryApidtl', form);
    });

    // 建立表單驗證
    var validatorOption = getValidFormOptinal({
        rules:{
            "beginDate":{
                required: true
            },
            "endDate": {
                required: true,
            }
        },
    });
    validatorOption = addAndGetDateLessEqualValidator(formId, 'beginDate', 'endDate', '結束日期不可小於開始日期', validatorOption)
    $('#' + formId).validate(validatorOption);
})