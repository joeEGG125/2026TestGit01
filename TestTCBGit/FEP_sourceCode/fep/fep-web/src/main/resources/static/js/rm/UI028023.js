var formId = "form-validator";

$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        var from = {
            orgPcodeDdl: document.getElementById("orgPcodeDdl").value,
        };
        doAjax(from, "/rm/UI_028023/queryClick", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showMessage(resp.messageType, resp.message);
                document.getElementById("oOrgPcode").value = resp.data.oOrgPcode;
                document.getElementById("rmPendingCnt").value = resp.data.rmPendingCnt;
                document.getElementById("rmPendingAmt").value = resp.data.rmPendingAmt;
            }
        });
    });
})