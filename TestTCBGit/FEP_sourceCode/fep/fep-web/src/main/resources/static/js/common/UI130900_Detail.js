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
        doFormSubmit('/common/UI_130900/showDetail', form);
    });
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
			reportDep_depNo: {
				required: true,
			},
            reportDep_depName: {
                required: true,
            },
			reportDep_type: {
			    required: true,
			}
        },
        messages: {
			reportDep_depNo: {
			    required: "必須輸入資料",
			},
            reportDep_depName: {
                required: "必須輸入資料",
            },
			reportDep_type: {
			    required: "必須選擇單位"
			}
        }
    }));
})