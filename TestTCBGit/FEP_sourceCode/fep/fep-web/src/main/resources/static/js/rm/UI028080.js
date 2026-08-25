var formId = "form-validator";

$(document).ready(function () {
    initDatePicker('tradingDate');

    $('#btnQuery').click(function () {
        // console.log("123");
        // var value = $(this).val();
        // console.log(value);
        // var form = jsonStringToObj(value);
        // doFormSubmit('/rm/UI_028080/queryClick', form);
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
})