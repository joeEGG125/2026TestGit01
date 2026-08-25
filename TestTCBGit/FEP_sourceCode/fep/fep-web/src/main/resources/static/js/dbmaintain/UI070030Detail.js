var formId = "form-validator";

$(document).ready(function () {
    initDatePicker('INBKPARM_EFFECT_DATE');
    
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
    var validatorOption = getValidFormOptinal({
        rules: {
            INBKPARM_APID: {
                required: true,
            },
            INBKPARM_PCODE: {
                required: true,
            },
            INBKPARM_EFFECT_DATE: {
                required: true,
            },
            INBKPARM_RANGE_FROM: {
                required: true,
            },
            INBKPARM_RANGE_TO: {
                required: true,
            },
            INBKPARM_FEE_MBR_DR: {
                required: true,
            },
            INBKPARM_FEE_MBR_CR: {
                required: true,
            },
            INBKPARM_FEE_ASS_DR: {
                required: true,
            },
            INBKPARM_FEE_ASS_CR: {
                required: true,
            },
            INBKPARM_FEE_CUSTPAY: {
                required: true,
            },
            INBKPARM_FEE_MIN: {
                required: true,
            }
        },
        messages: {
            INBKPARM_APID: {
                required: "必須輸入資料",
            },
            INBKPARM_PCODE: {
                required: "必須輸入資料",
            },
            INBKPARM_EFFECT_DATE: {
                required: "必須輸入資料",
            },
            INBKPARM_RANGE_FROM: {
                required: "必須輸入資料",
            },
            INBKPARM_RANGE_TO: {
                required: "必須輸入資料",
            },
            INBKPARM_FEE_MBR_DR: {
                required: "必須輸入資料",
            },
            INBKPARM_FEE_MBR_CR: {
                required: "必須輸入資料",
            },
            INBKPARM_FEE_ASS_DR: {
                required: "必須輸入資料",
            },
            INBKPARM_FEE_ASS_CR: {
                required: "必須輸入資料",
            },
            INBKPARM_FEE_CUSTPAY: {
                required: "必須輸入資料",
            },
            INBKPARM_FEE_MIN: {
                required: "必須輸入資料",
            }
        }
    });
    validatorOption = addCheckDecimalValidator('INBKPARM_RANGE_FROM', '輸入格式錯誤,應輸入decimal(9,2)', validatorOption);
    validatorOption = addCheckDecimalValidator('INBKPARM_RANGE_TO', '輸入格式錯誤,應輸入decimal(9,2)', validatorOption);
    validatorOption = addCheckDecimalValidator('INBKPARM_FEE_MBR_DR', '輸入格式錯誤,應輸入decimal(7,4)', validatorOption);
    validatorOption = addCheckDecimalValidator('INBKPARM_FEE_MBR_CR', '輸入格式錯誤,應輸入decimal(7,4)', validatorOption);
    validatorOption = addCheckDecimalValidator('INBKPARM_FEE_ASS_DR', '輸入格式錯誤,應輸入decimal(7,4)', validatorOption);
    validatorOption = addCheckDecimalValidator('INBKPARM_FEE_ASS_CR', '輸入格式錯誤,應輸入decimal(7,4)', validatorOption);
    validatorOption = addCheckDecimalValidator('INBKPARM_FEE_CUSTPAY', '輸入格式錯誤,應輸入decimal(7,4)', validatorOption);
    validatorOption = addCheckDecimalValidator('INBKPARM_FEE_MIN', '輸入格式錯誤,應輸入decimal(7,2)', validatorOption);
    $('#' + formId).validate(validatorOption);

})