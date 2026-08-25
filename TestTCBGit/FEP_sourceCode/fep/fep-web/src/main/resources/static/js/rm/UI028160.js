var formId = "form-validator";

$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        showLoading(true);
        showProcessingMessage(true);
        $('#' + formId).submit();
    });

    $('#kind').on('change', function() {
        var form = {
            kind: $("#kind").val(),
            brno: $("#brno").val(),
        };
        doFormSubmit('/rm/UI_028160/selectChange', form);
    });
})