var formId = "form-validator";

$(document).ready(function() {
    // 按下確認按鈕
    $('#btnComit').click(function() {        
    	showLoading(true);
        showProcessingMessage(true);
        $('#' + formId).submit();
    });

})