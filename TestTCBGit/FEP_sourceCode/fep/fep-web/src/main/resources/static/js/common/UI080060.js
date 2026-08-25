var formId = "form-validator";

$(document).ready(function() {

    $('#btnComit').on('click', function() {
        if (doValidateForm(formId)) {
            var jsonData = {
                logonId: $("#logonId").val(),
                oldSscod: $("#oldSscod").val(),
                newSscod: $("#newSscod").val(),
                confimSscod: $("#confimSscod").val()
            };
            doAjax(jsonData, "/common/UI_080060/btnConfirm", false, true, function(resp) {
                if ('undefined' !== typeof resp) {
                    showMessage(resp.messageType, resp.message);
                }
            });
        }
    });


    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            oldSscod: {
                required: true
            },
            newSscod: {
                required: true,
                minlength:5
            },
            confimSscod: {
                required: true,
                minlength:5,
                equalTo: "#newSscod"
            },
        },
        messages:{
            oldSscod: {
                required: "必須有資料",
            },
            newSscod: {
                minlength: "密碼長度應大於5",
                required: "必須有資料"
            },
            confimSscod: {
                required: "必須有資料",
                minlength: "密碼長度應大於5",
                equalTo: "新密碼與新密碼確認必須相同"
            },
        }
    }));
})