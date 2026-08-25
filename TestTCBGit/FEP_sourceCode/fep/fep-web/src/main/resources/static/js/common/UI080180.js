var formId = "form-validator";

// 按下查詢按鈕
$('#btnQuery').click(function () {
    var form = {};
    doFormSubmit('/common/UI_080180/doQuery', form);
});
// 按下變更狀態按鈕
$('#btnChangeLineStatus').click(function () {
    var form = $('#' + formId);
    if (form.length > 0) {
        showLoading(true);
        showProcessingMessage(true);
        form.submit();
    } else {
        showDangerMessage("未查詢到IMSGW資料無法變更狀態");
    }
});