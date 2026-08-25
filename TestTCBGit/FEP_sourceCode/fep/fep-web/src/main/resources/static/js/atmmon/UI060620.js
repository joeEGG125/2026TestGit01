var formId = "form-validator";
var donwloading = false;

$(document).ready(function () {
    initDatePicker('dtTransactDate');
    initDateTimePicker('txTransactTimeBEG', 'HH:mm');
    initDateTimePicker('txTransactTimeEND', 'HH:mm');
    // 按下儲存按鈕
    $('#btnReserve').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    $('#btnDownload').click(function () {
        clearInterval(timerId);
        donwloading = true;
        var formData = getFormData(formId);
        doAjaxDownload(formData, '/atmmon/UI_060620/download', function () {
            donwloading = false;
            timerId = setInterval(myrefresh, form.time * 1000);
        });
    });
    // Grid中第一列查詢按鈕
    $('.a-start').click(function () {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/atmmon/UI_060620_A/inquiryDetail', form);
    });

    // Grid中最後一列查詢按鈕
    $('.a-end').click(function () {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/atmmon/UI_060620_B/inquiryDetail', form);
    });
})

// Ajax實現定時刷新頁面
function myrefresh() {
    if (doValidateForm(formId)) {
        showLoading(true);
        showProcessingMessage(true);
        $('#' + formId).submit();
    }
}