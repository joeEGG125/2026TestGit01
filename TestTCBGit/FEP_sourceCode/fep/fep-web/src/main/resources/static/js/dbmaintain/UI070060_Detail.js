var formId = "form-validator";
$(document).ready(function () {
    checkItem('fepNotifyMail_Customize');
    checkItem('fepNotifyPhone_Customize');
    // 按下儲存按鈕
    $('#btnSaveBtn').click(function () {
        doFormValidateAndSubmit(formId);
    });
});

function checkItem(item) {
    var checked = $("." + item).prop("checked");
    if (item === 'fepNotifyMail_Customize') {
        $(".msgfileResponsible").prop("disabled", !checked);
    } else if (item === 'fepNotifyPhone_Customize') {
        $(".msgfileNotifyphone").prop("disabled", !checked);
    }
}