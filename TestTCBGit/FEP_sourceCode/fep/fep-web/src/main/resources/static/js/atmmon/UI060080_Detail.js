var formId = "form-validator";
$(document).ready(function () {
    // 按下[變更儲存]按鈕
    $('#btnChange').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            alarm_no: {
                required: true,
            },
            alarm_names: {
                required: true,
            },
            alarm_notify_times: {
				digits:true,
				max:255
            }
        },
        messages: {
            alarm_no: {
                required: "請輸入數字或英文字母",
            },
            alarm_names: {
                required: "必須輸入資料",
            },
            alarm_notify_times: {
				digits:"請輸入0~255之間的整數",
				max:"請輸入0~255之間的整數"
            }
        }
    }));
})