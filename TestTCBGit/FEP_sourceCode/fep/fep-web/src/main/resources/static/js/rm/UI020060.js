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
    //按下清除按鈕跳轉UI020061_B書面
    $('#btnClear1').click(function () {
        var form = {
            allbankBkno:"",
            allbankBrno:""
        }
        doFormSubmit('/rm/UI_020060/index',form);
    });
    // Grid中第一列按明細查詢按鈕
    $('.btnDetail').click(function () {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/rm/UI_020060/showDetial', form);
    });

})