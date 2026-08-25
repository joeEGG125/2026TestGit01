var formId = "form-validator";

$(document).ready(function() {
    initDateTimePicker('sdatetime','YYYY/MM/DD');
    initDateTimePicker('edatetime','YYYY/MM/DD');
    initTimePicker('stime');
    initTimePicker('etime');
    // 按下確認按鈕
    $('#btnConfirm').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // 建立表單驗證
    var validatorOption = getValidFormOptinal({
        rules: {
            sdatetime: {
                required: true,
                dateISO: true,
            },
            edatetime: {
                dateISO: true,
                dateISO: true,
            },
        }
    })
    validatorOption = addAndGetDateLessEqualValidator(formId,'stime','etime','交易時間起不可大於交易時間訖',validatorOption)
    $('#' + formId).validate(validatorOption);
})