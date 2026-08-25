// 按下查詢按鈕
$('#btnQuery').click(function () {
    var form = {};
    doFormSubmit('/common/UI_080110/doQuery', form);
});
// 按下每個查詢按鈕
$('.btn-get-concurrency').click(function () {
    doAction($(this), 'GET');
});
// 按下每個設置按鈕
$('.btn-set-concurrency').click(function () {
    doAction($(this), 'SET');
});
// 按下每個重置按鈕
$('.btn-reset-concurrency').click(function () {
    doAction($(this), 'RESET');
});

// 執行動作
function doAction(btn, action) {
    var form = btn.parent().parent().find('.panel-detail').find('.form-concurrency');
    var data = getFormObjData(form);
    data['action'] = action;
    doAjax(data, "/common/UI_080110/doAction", false, true, function (resp) {
        if ('undefined' !== typeof resp) {
            if ('undefined' !== typeof resp.data && resp.data != null) {
                var concurrency = resp.data;
                form.find('.idleInvokerCount').text(concurrency.idleInvokerCount);
                form.find('.activeInvokerCount').text(concurrency.activeInvokerCount);
                form.find('.currentConcurrency').val(concurrency.currentConcurrency);
                form.find('.initConcurrency').text(concurrency.initConcurrency);
            }
            var btnPanel = form.parent().parent().find('.panel-button');
            btnPanel.find('.btn-set-concurrency').attr("disabled", resp.offline);
            btnPanel.find('.btn-reset-concurrency').attr("disabled", resp.offline);
            showMessage(resp.messageType, resp.message);
        }
    });
}