var formId = "form-validator";

$(document).ready(function () {
    initDatePicker('datetime');
    initDatePicker('datetimeo');
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });

    $('#btnDownload').click(function () {
        var formData = getFormData(formId);
        doAjaxDownload(formData, '/inbk/UI_019270/doDownload');
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
    $('#' + formId).validate(validatorOption);
})