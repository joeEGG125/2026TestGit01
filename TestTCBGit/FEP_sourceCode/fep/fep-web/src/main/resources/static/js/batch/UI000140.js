var formId = "form-validator";

$(document).ready(function () {
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    $('.btn-resume').click(function () {
        operateFunc($(this), 'resume');
    });
    $('.btn-pause').click(function () {
        operateFunc($(this), 'pause');
    });
})

var operateFunc = function (btn, action) {
    var data = {"batchExecuteHostName": btn.attr("batch-execute-hostname"), "batchId": btn.attr("value"), "action": action};
    doAjax(data, "/batch/UI_000140/doOperate", false, true, function (resp) {
        if ('undefined' !== typeof resp) {
            showMessage(resp.messageType, resp.message);
            var batchScheduler = resp.data;
            var btnResume = $('#btnResume-' + batchScheduler.hostName + '-' + batchScheduler.batchId);
            var btnPause = $('#btnPause-' + batchScheduler.hostName + '-' + batchScheduler.batchId);
            if (batchScheduler.triggerState === 'PAUSED') {
                btnResume.removeClass('disabled').addClass('btn-resume').on('click', function () {
                    operateFunc($(this), 'resume');
                });
                btnPause.removeClass('btn-pause').addClass('disabled').unbind("click");
            } else if (batchScheduler.triggerState === 'NORMAL') {
                btnResume.removeClass('btn-resume').addClass('disabled').unbind("click");
                btnPause.removeClass('disabled').addClass('btn-pause').on('click', function () {
                    operateFunc($(this), 'pause');
                });
            } else {
                btnResume.removeClass('btn-resume').addClass('disabled').unbind("click");
                btnPause.removeClass('btn-pause').addClass('disabled').unbind("click");
            }
            var spanTriggerState = $('#spanTriggerState-' + batchScheduler.hostName + '-' + batchScheduler.batchId);
            switch (batchScheduler.triggerState) {
                case "NONE":
                    spanTriggerState.html('未知');
                    break;
                case "NORMAL":
                    spanTriggerState.html('正常');
                    break;
                case "PAUSED":
                    spanTriggerState.html('暫停');
                    break;
                case "COMPLETE":
                    spanTriggerState.html('完成');
                    break;
                case "ERROR":
                    spanTriggerState.html('錯誤');
                    break;
                case "BLOCKED":
                    spanTriggerState.html('阻塞');
                    break;
            }
            $('#tdNextruntime-' + batchScheduler.hostName + '-' + batchScheduler.batchId).html(moment(batchScheduler.nextFireTime).format('YYYY/MM/DD HH:mm:ss'))
        }
    });
}