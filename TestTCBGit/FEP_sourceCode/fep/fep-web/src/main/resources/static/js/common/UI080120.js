var formId = "form-nThreads";

$(document).ready(function () {
    // 建立表單驗證
    var validatorOption = getValidFormOptinal({
        rules: {
            nThreads: {
                required: true,
                digits: true,
            },
        }
    });
    // $('.' + formId).validate(validatorOption);
    $('.' + formId).each(function () {
        $(this).validate(validatorOption);
    });
});
// 按下查詢按鈕
$('#btnQuery').click(function () {
    var form = {};
    doFormSubmit('/common/UI_080120/doQuery', form);
});
// 按下每個查詢按鈕
$('.btn-get-nThreads').click(function () {
    doAction($(this), 'GET');
});
// 按下每個設置按鈕
$('.btn-set-nThreads').click(function () {
    doAction($(this), 'SET');
});
// 按下每個重置按鈕
$('.btn-reset-nThreads').click(function () {
    doAction($(this), 'RESET');
});

// 執行動作
function doAction(btn, action) {
    var form = btn.parent().parent().find('.panel-detail').find('.form-nThreads');
    doClearValidtFormObj(form);
    if (action === 'SET' && !doValidateFormObj(form)) {
        return;
    }
    var data = getFormObjData(form);
    data['action'] = action;
    doAjax(data, "/common/UI_080120/doAction", false, true, function (resp) {
        if ('undefined' !== typeof resp) {
            if ('undefined' !== typeof resp.data && resp.data != null) {
                var eventExecutorData = resp.data;
                form.find('.executorIdleCount').text(eventExecutorData.executorIdleCount);
                form.find('.executorActiveCount').text(eventExecutorData.executorActiveCount);
                form.find('.executorSetupCount').val(eventExecutorData.executorSetupCount);
                form.find('.executorConfigurationCount').text(eventExecutorData.executorConfigurationCount);
            }
            var btnPanel = form.parent().parent().find('.panel-button');
            btnPanel.find('.btn-set-nThreads').attr("disabled", resp.offline);
            btnPanel.find('.btn-reset-nThreads').attr("disabled", resp.offline);
            showMessage(resp.messageType, resp.message);
        }
    });
}