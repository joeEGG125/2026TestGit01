var formId = "form-validator";

$(document).ready(function() {
    initDatePicker('tradingDate');
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        if (doValidateForm(formId)) {
           showLoading(true);
           showProcessingMessage(true);
           $('#' + formId).submit();
        }
    });

    // Grid中第一列查詢按鈕
    $('.btn-inquiry').click(function() {
    	var value = $(this).attr("value");
    	var form = jsonStringToObj(value);
    	form.remtypeddl = document.getElementById("remtypeddl").value;
        form.ioflag = document.getElementById("ioflag").value;
    	doFormSubmit('/rm/UI_028110/inquiryDetail', form);
    });
})