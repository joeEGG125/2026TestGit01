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
    // 按下清除按鈕
    $('#btnClear1').click(function () {
        window.location.reload()
    });
    // Grid中第一列按修改按鈕
    $('.btnUpdate').click(function () {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/rm/UI_020061_C/resultGrdv_RowCommand', form);
    });
    //下拉框改變事件 市縣鄉區
    $("#countyDDL").change(function() {
        var form = {
            allbankBkno:$('#allbankBkno').val(),
            allbankBrno:$('#allbankBrno').val(),
            countyDDL:$('#countyDDL').val(),
            regionDDL:$('#regionDDL').val(),
        }
        doFormSubmit('/rm/UI_020061_C_Detail/countyDDL_SelectedIndexChanged', form);
    });
})