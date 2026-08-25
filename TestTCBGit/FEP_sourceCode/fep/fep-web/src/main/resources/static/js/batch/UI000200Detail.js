var formId = "form-validator";

$(document).ready(function () {
    // 按下變更保存按鈕
    $('#btnChange').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });

    // 按下放棄變更按鈕
    $('#btnClear1').click(function () {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/batch/UI_000200/showDetail', form);
    });
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            task_Name: {
                required: true,
            },
            task_Description: {
                required: true,
            },
            task_Timeout: {
                required: true,
                number:true,
            },
        },
        messages: {
            task_Name: {
                required: "必須輸入資料",
            },
            task_Description: {
                required: "必須輸入資料",
            },
            task_Timeout: {
                required: "必須輸入資料",
                number: "必須輸入數字",
            }
        }
    }));
})