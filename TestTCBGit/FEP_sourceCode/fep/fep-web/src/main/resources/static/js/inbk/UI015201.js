var formId = "form-validator";

$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        var lblTxDate = $("#lblTxDate").html();
        var lblBankNo = $("#lblBankNo").html();
        var form = jsonStringToObj("{\"lblTxDate\":\""+lblTxDate+"\",\"lblBankNo\":\""+lblBankNo+"\"}");
        doFormSubmit('/inbk/UI_015201/getClrtotalDetailed', form);
    });
})