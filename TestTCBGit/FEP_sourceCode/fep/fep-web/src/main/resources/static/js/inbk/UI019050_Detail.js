var formId = "form-validator";

$(document).ready(function() {
    // Grid中第一列按序號查詢按鈕
    $('.a-inquiry').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/inbk/UI_019050/inquiryFeptxnList', form);
    });
})