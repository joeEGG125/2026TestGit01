var formId = "form-validator";

$(document).ready(function() {
    initDatePicker('datetime');
    initDatePicker('datetimeo');
    initTimePicker('stime');
    initTimePicker('etime');
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // 建立表單驗證
    var validatorOption = getValidFormOptinal({
        rules: {
            datetime: {
                required: true,
                dateISO: true,
            },
            datetimeo: {
                dateISO: true,
            },
        }
    })
    validatorOption = addAndGetDateLessEqualValidator(formId,'stime','etime','交易時間起不可大於交易時間訖',validatorOption)
    $('#' + formId).validate(validatorOption);
})