var formId = "form-validator";

$(document).ready(function () {

    // 按下變更保存按鈕
    $('#btnChange').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            removeAttrDisabled(formId);
            $('#' + formId).submit();
        }
    });


    // 建立表單驗證
	$('#' + formId).validate(getValidFormOptinal({
        rules: {
            binNo: {
                required: true,
            },
            binBkno: {
                required: true,
            }
        },
        messages: {
            binNo: {
                required: "CREDIT CARD BIN不能為空",
            },
            binBkno: {
                required: "發卡組織不能為空",
            }
        }
    }));
})