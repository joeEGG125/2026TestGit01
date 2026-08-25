var formId = "form-validator";

$(document).ready(function() {
    initDatePicker('lblStDate');
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    $('#lblZone').change(function () {
        var lblZone = $("#lblZone").val();
        var lblStDate = $("input[name='lblStDate']").val();
        var sysCode = $("#sysCode").val();
        var acBranchCode = $("#acBranchCode").val();
        var drCrSide = $("#drCrSide").val();
        var acCode = $("#acCode").val();
        var subAcCode = $("#subAcCode").val();
        var dtlAcCode = $("#dtlAcCode").val();
        var deptCode = $("#deptCode").val();
        var txAmt = $("#txAmt").val();
        var brapTxType = $("#brapTxType").val();
        var brapCur = $("#brapCur").val();
        var form = jsonStringToObj("{" +
            "\"lblZone\":\""+lblZone+
            "\",\"lblStDate\":\""+lblStDate+
            "\",\"sysCode\":\""+sysCode+
            "\",\"acBranchCode\":\""+acBranchCode+
            "\",\"drCrSide\":\""+drCrSide+
            "\",\"acCode\":\""+acCode+
            "\",\"subAcCode\":\""+subAcCode+
            "\",\"dtlAcCode\":\""+dtlAcCode+
            "\",\"deptCode\":\""+deptCode+
            "\",\"txAmt\":\""+txAmt+
            "\",\"brapTxType\":\""+brapTxType+
            "\",\"brapCur\":\""+brapCur+
            "\"}");
        doFormSubmit('/inbk/UI_019401/selectOptionList', form);
    })
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            lblStDate: {
                required: true,
                dateISO: true,
            },
        }
    }));
})