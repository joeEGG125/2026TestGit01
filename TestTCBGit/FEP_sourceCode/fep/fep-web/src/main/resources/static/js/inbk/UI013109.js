var formId = "form-validator";

$(document).ready(function() {
    // 按下確認按鈕
    $('#btnConfirm').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
})
// 文本框改變事件
function apidChange() {
    var apId = document.getElementById("apId").value;
    var options = $('#curDdl option');
    if (apId ==="1600"){
        for (var i=0;i<options.length;i++)
        {
            if(options[i].value === "000-TWD") {
                options[i].selected = true;
            }
        }
    }else {
        for (var j=0;j<options.length;j++) {
            if (options[j].value === "0") {
                options[j].selected = true;
            }
        }
    }
}