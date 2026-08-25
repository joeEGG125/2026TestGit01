var formId = "form-validator";

$(document).ready(function () {
    // 按下執行按鈕
    $('#btnExecute').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    $("#countyDDL").change(function() {
        var form = {
            countyDDL:$('#countyDDL').val(),
            regionDDL:$('#regionDDL').val(),
            elseTb:$('#elseTb').val(),
            flagDDL:$('#flagDDL').val(),
        }
        doFormSubmit('/rm/UI_020062/countyDDL_SelectedIndexChanged', form);
    });
})