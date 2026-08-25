var formId = "form-validator";

$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });

    // Grid中第一列查詢按鈕
    $('.btn-inquiry').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        showCmnConfirmDialog('確定執行?', function() {
            doFormSubmit('/inbk/UI_019141/resultGrdvRowCommand', form);
        }, function() {

        });
    });

    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules:{
            "txTroutActno":{
                required: true
            },
            "txTrinBkno": {
                required: true,
            },
            "txTrinActno":{
                required: true
            },
            "txtTxAmt":{
                required: true
            }
        },
    }));
})