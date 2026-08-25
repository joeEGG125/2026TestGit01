var formId = "form-validator";

$(document).ready(function () {
    // 按下執行按鈕
    $('#btnExecute').click(function () {
        var fileUpload = $('#fileUpload').val();
        var fileUpload2 = $('#fileUpload2').val();
        if (null === fileUpload || "" === fileUpload) {
            showMessage("INFO", "必須上傳'財金檔案'");
            return;
        }
        if (null === fileUpload2 || "" === fileUpload2) {
            showMessage("INFO", "必須上傳'轉參加證券匯款銀行代號檔'");
            return;
        }
        $('#excuteTypeDdlSelectedIndexChanged').val("1")
        showLoading(true);
        showProcessingMessage(true);
        $('#' + formId).submit();
    });
    //按下清除按鈕
    $('#btnClear1').click(function () {
        var form = {
            allbankBkno: "",
            allbankBrno: ""
        }
        doFormSubmit('/rm/UI_020061_A/index', form);
    });
    //按下上一頁按鈕
    $('#btnPrevPage1').click(function () {
        var form = {
            allbankBkno: "",
            allbankBrno: ""
        }
        doFormSubmit('/rm/UI_020061/index', form);
    });
    //下拉框改變事件 市縣鄉區
    $("#excuteTypeDdlSelectedIndexChanged").change(function () {
        var form = {
            excuteTypeDdlSelectedIndexChanged: $('#excuteTypeDdlSelectedIndexChanged').val(),
            fileUpload: $('#fileUpload').val(),
            fileUpload2: $('#fileUpload2').val(),
        }
        if (form.excuteTypeDdlSelectedIndexChanged === "1") {
            doFormSubmit('/rm/UI_020061_A/index', form);
        } else {
            doFormSubmit('/rm/UI_020061_A/getLog', form);
        }
    });
})
$('.btnDetail').click(function () {
    var value = $(this).attr("value");
    var form = {
        excuteTypeDdlSelectedIndexChanged: $('#excuteTypeDdlSelectedIndexChanged').val(),
        fileUpload: $('#fileUpload').val(),
        fileUpload2: $('#fileUpload2').val(),
        logUrl: value,
    }
    doFormSubmit('/rm/UI_020061_A_Detail/getLogDetail', form);
});