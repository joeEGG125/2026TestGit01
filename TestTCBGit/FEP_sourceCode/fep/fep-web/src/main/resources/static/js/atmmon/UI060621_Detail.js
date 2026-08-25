var formId = "form-validator";
$(document).ready(function () {
    checkItem('fepNotifyMail_Customize');
    checkItem('fepNotifyPhone_Customize');
    // 按下更新按鈕
    $('#btnUpdate').click(function () {
        doFormValidateAndSubmit(formId);
    });
});

function checkItem(item) {
    var checked = $("." + item).prop("checked");
    if (item === 'fepNotifyMail_Customize') {
        $(".msgkbNotifymail").prop("disabled", !checked);
    } else if (item === 'fepNotifyPhone_Customize') {
        $(".msgkbNotifyphone").prop("disabled", !checked);
    }
}