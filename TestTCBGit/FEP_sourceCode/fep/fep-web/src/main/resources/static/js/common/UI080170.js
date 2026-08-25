var formId = "form-validator";

// 按下查詢按鈕
$('#btnQuery').click(function () {
    var form = {};
    doFormSubmit('/common/UI_080170/doQuery', form);
});
// 按下變更狀態按鈕
$('#btnChangeLineStatus').click(function () {
    // 2026-01-27 Richard modified 如果有選擇停止FEPAP腳位, 則show confirm dialog
    var severStopChecked = $("input:radio.radio-server-stop:checked");
    if (severStopChecked.length > 0) {
        showCmnConfirmDialog('停止FEPAP腳位將會停止往此AP腳位對應的所有IMS腳位傳送交易,你確定嗎?', function () {
            doChangeLineStatus();
        });
    } else {
        doChangeLineStatus();
    }
});

// 變更狀態
function doChangeLineStatus() {
    var form = $('#' + formId);
    if (form.length > 0) {
        showLoading(true);
        showProcessingMessage(true);
        form.submit();
    } else {
        showDangerMessage("未查詢到CBSGW資料無法變更狀態");
    }
}