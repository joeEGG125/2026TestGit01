// 按下查詢按鈕
$('#btnQuery').click(function () {
    var form = {};
    doFormSubmit('/common/UI_080500/doQuery', form);
});
// 按下線路切換中的確認按鈕
$('.btn-confirm-channel').click(function () {
    var form = $("#form-channel");
    var data = getFormObjData(form);
    data["name"] = $(this).val();
    data["target"] = "channel";
    // 若有不同FISCGW的主要及備援線路同時勾選的情況, 比如FISCGW1勾主要, FISCGW2勾備援, 或FISCGW1勾備援, FISCGW2勾主要
    if (data.hasOwnProperty("primary") && data.hasOwnProperty("secondary") && !Array.isArray(data["primary"]) && !Array.isArray(data["secondary"]) && data["primary"] !== data["secondary"]) {
        showDangerMessage('不可同時在不同主機啟用同一種線路!');
        return;
    }
    var primaryChk = $('#primary' + data["name"]);
    var secondaryChk = $('#secondary' + data["name"]);
    var confirmMessage = '';
    if (primaryChk.prop('checked') && !secondaryChk.prop('checked')) {
        confirmMessage = '你確定要將' + data["name"] + '的線路切換至Primary腳位嗎?';
    } else if (!primaryChk.prop('checked') && secondaryChk.prop('checked')) {
        confirmMessage = '你確定要將' + data["name"] + '的線路切換至Secondary腳位嗎?';
    } else if (primaryChk.prop('checked') && secondaryChk.prop('checked')) {
        confirmMessage = '你確定要將' + data["name"] + '同時啟用Primary及Secondary腳位嗎?';
    } else if (!primaryChk.prop('checked') && !secondaryChk.prop('checked')) {
        confirmMessage = '你確定要將' + data["name"] + '同時停用Primary及Secondary腳位嗎?';
    }
    showCmnConfirmDialog(confirmMessage, function () {
        doAction(data);
    });
});
// 按下服務切換中的確認按鈕
$('.btn-confirm-service').click(function () {
    var form = $(this).parent().parent().parent().parent().parent();
    var data = getFormObjData(form);
    data["target"] = "service";
    var confirmMessage = '';
    if (data["action"] === 'start') {
        confirmMessage = '你確定要啟動' + data["name"] + '服務嗎?';
    } else if (data["action"] === 'stop') {
        confirmMessage = '你確定要停止' + data["name"] + '服務嗎?';
    }
    showCmnConfirmDialog(confirmMessage, function () {
        doAction(data);
    });
});

// 執行動作
function doAction(data) {
    doFormSubmit('/common/UI_080500/doAction', data);
}