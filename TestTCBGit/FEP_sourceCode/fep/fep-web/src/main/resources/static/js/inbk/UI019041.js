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
    $('#btnConfirm').click(function () {
        var jsonData = {
            fwdrstTxDate: document.getElementById("fwdrstTxDate").innerHTML
        };
        //失敗筆數
        var failTimes = document.getElementById('FailTimes').innerHTML;
        showConfirmDialog('myConfirm', '', function () {
            if (failTimes > 0) {
                doAjax(jsonData, "/inbk/UI_019041/queryBatchByName", false, true, function (resp) {
                    if ('undefined' !== typeof resp) {
                        showMessage(resp.messageType, resp.message);
                    }
                });
            } else {
                showWarningCmnAlert('查無資料');
            }
        });

    });
})