var formId = "form-validator";

$(document).ready(function() {
    // 按下確認按鈕
    $('#btnComit').click(function() {
        if (doValidateForm(formId)) {
            if ($("#targetHost").val()===$("#sourceHost").val()){
                showInfoCmnAlert('[來源主機]與[目的主機]不能為同一台！！！');
                return;
            }
            showCmnConfirmDialog('你確定要把批次執行主機從[來源主機]切換到[目的主機]嗎?', function () {
                $('#' + formId).submit();
            });
        }
    });

    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            sourceHost: {
                required:true,
            },
            targetHost: {
                required:true,
            },
        },
    }));
})