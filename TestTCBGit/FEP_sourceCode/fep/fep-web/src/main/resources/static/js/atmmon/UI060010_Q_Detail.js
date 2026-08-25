var formId = "form-validator";
$(document).ready(function() {
    //明細頁儲存功能
    $('#btnChangeSave').click(function() {
        doFormValidateAndSubmit(formId);
    });
});