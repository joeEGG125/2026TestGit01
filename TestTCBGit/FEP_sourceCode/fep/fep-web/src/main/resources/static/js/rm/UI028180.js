var formId = "form-validator";

$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        if($("#original").val()==="1"){
            if($("#batchNo").val().trim()===""){
                showMessage("DANGER","必須輸入批號");
                return;
            }
        }
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // 按下執行按鈕
    $('#btnExecute').click(function() {
        var form = {
            brno:$('#brno').val(),
            txAmt:$('#txAmt').val(),
            fepNo:$('#fepNo').val(),
            original:$('#original').val(),
            batchNo:$('#batchNo').val(),
        }
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            doFormSubmit('/rm/UI_028180/executeClick', form);
        }
    });

    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            brno: {
                required: true,
            },
            txAmt: {
                required: true,
            },
            fepNo: {
                required: true,
            },
        },
        messages: {
            brno: {
                required: "必須選取",
            },
            txAmt: {
                required: "必須有資料",
                number: "請輸入數字",
            },
            fepNo: {
                required: "必須有資料",
                number: "請輸入數字",
            }
        }
    }));
})
$("#original").change(function() {
    var original = $("#original").val();
    if(original === "1") {
        $("#batchNo").removeAttr("readOnly");
    } else {
        $("#batchNo").val("")
        $("#batchNo").attr("readOnly","readOnly");
    }
});