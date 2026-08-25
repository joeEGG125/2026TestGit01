var formId = "form-validator";

function initTimePicker(id, defaultDate) {
    initDateTimePicker(id, 'HH:mm', defaultDate);
}

$(document).ready(function() {
    initDatePicker('logTimeBegin', new Date());
    initDatePicker('logTimeEnd', new Date());
    initDateTimePicker('logTimeBeginTime', 'HH:mm');
    initDateTimePicker('logTimeEndTime', 'HH:mm');

    // Grid中第一列查詢按鈕
    $('.a-inquiry').click(function() {
        if($(this).attr("data-audit") != '') {
            var value = $(this).attr("value");
            var form = jsonStringToObj(value);
            doFormSubmit('/common/UI_080070/inquiryDetail', form);
        }else {
            alert('無欄位輸入資料');
        }
    });

    $('#btnQuery').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });

    $('#btnApply').click(function() {
        let form = {
            btnType: 'insert'
        }
        doFormSubmit('/common/UI_080070/showDetail', form);
    });

    $('.btn-inquiry').click(function() {
        let value = $(this).attr("value");
        let form = jsonStringToObj(value);
        doFormSubmit('/common/UI_080070/showDetail', form);
    });

    // 建立表單驗證
    $.validator.addMethod("logTime", function(value, element) {
        // 獲取日期和時間
        let logTimeBegin = $('#logTimeBegin input').val();
        let logTimeBeginTime = $('#logTimeBeginTime input').val();
        let logTimeEnd = $('#logTimeEnd input').val();
        let logTimeEndTime = $('#logTimeEndTime input').val();

        // 組合日期和時間
        let beginDateTime = new Date(logTimeBegin + ' ' + logTimeBeginTime);
        let endDateTime = new Date(logTimeEnd + ' ' + logTimeEndTime);

        // 比較日期時間
        return (beginDateTime.getTime() <= endDateTime.getTime())
    }, "起始時間不能大於結束時間");

    var validatorOption = getValidFormOptinal({
        rules: {
            logTimeBegin: {
                required: true
            },
            logTimeEnd: {
                required: true
            },
            logTimeBeginTime : {
                logTime : true
            },
            logTimeEndTime : {
                logTime : true
            },
        },
        messages: {
            logTimeBegin: {
                required: "請輸入年月"
            },
            logTimeEnd: {
                required: "請輸入年月"
            },
            logTimeBeginTime : {
                logTime : "起始時間不能大於結束時間"
            },
            logTimeEndTime : {
                logTime : "起始時間不能大於結束時間"
            },
        }
    });
    validatorOption = addAndGetDateLessEqualValidator(formId, 'logTimeBegin', 'logTimeEnd', '起日不能大於訖日', validatorOption);
    // validatorOption = addAndGetDateLessEqualValidator(formId, 'logTimeBeginTime', 'logTimeEndTime', '起始時間不能大於結束時間', validatorOption);
    $('#' + formId).validate(validatorOption);

})