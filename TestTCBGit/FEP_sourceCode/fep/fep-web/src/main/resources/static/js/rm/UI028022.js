var formId = "form-validator";

$(document).ready(function () {
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        var from = {
            orgFiscno: document.getElementById("orgFiscno").value,
            orgPcode: document.getElementById("orgPcode").value,
        };
        doAjax(from, "/rm/UI_028022/queryClick", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showMessage(resp.messageType, resp.message);
                document.getElementById("oOrgTxamt").value = resp.data.oOrgTxamt;
                document.getElementById("oOrgFiscno").value = resp.data.oOrgFiscno
                document.getElementById("oOrgRmsno").value = resp.data.oOrgRmsno
                document.getElementById("oOrgReceiverBank").value = resp.data.oOrgReceiverBank
                document.getElementById("oMsg").value = resp.data.oMsg
                document.getElementById("oOrgPcode").value = resp.data.oOrgPcode
                document.getElementById("oOrgStan").value = resp.data.oOrgStan
                document.getElementById("oOrgTxDatetime").value = resp.data.oOrgTxDatetime
            }
        });
    });
})