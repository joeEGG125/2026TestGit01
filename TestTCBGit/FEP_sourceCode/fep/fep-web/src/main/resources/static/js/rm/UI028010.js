var formId = "form-validator";

$(document).ready(function () {
    // 按下執行按鈕
    $('#btnExecute').click(function () {
        if (doValidateForm(formId)) {
            var from = {
                receiverBank: document.getElementById("receiverBank").value,
                chnmemo: document.getElementById("chnmemo").value,
                engmemo: document.getElementById("engmemo").value
            };
            doAjax(from, "/rm/UI_028010/insertMsgout", false, true, function (resp) {
                if ('undefined' !== typeof resp) {
                    showMessage(resp.messageType, resp.message);
                }
            });
        }
    });
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            "receiverBank": {
                required: true,
                rangelength: [7, 7]
            },
            "chnmemo": {
                required: true,
            }
        },
        messages: {
            "receiverBank": {
                required: "必須有資料",
                rangelength: "請輸入7位數"
            },
            "chnmemo": {
                required: "必須有資料",
            }
        }
    }));
})