var formId = "form-validator";
$(document).ready(function () {
    initDateTimePicker('transactDate', 'YYYY-MM-DD');
    initDateTimePicker('feptxnTxTimeBegin', 'HH:mm:ss');
    initDateTimePicker('feptxnTxTimeEnd', 'HH:mm:ss');
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        if (!validateTimeRange()) {
            return; // 交易時間起訖驗證，不通過不送出表單
        }

        if (confirm('確定要以現有條件搜尋FEPLOG?')) {
            if (doValidateForm(formId)) {
                showLoading(true);
                showProcessingMessage(true);
                $('#' + formId).submit();
            }
        } else {
            alert('取消搜尋');
        }
    });
    // Grid中交易別超連結
    $('.ej').click(function () {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/atmmon/UI_060610/bindGridDetail', form);
    });
    // 下載Log
    $('#btnDownload').click(function () {
        var formData = getFormData(formId);
        doAjaxDownload(formData, '/atmmon/UI_060610/download');
    });
    $('#server').change(function () {
        var form = {
            server: $('#server').val(),
        }
        doAjax(form, "/atmmon/UI_060610/getSelectLog", false, true, function (resp) {
            $('#logType option').remove();
            $.each(resp.logType, function (index, value) {
                // 2024-11-06 Richard modified for 【Client Potential XSS】
                $('#logType').append("<option value='" + filterXSS(value.value) + "'>" + filterXSS(value.text) + "</option>");
            });
            $('#channelDownload option').remove();
            $.each(resp.channelDownload, function (index, value) {
                $('#channelDownload').append("<option value='" + filterXSS(value.value) + "'>" + filterXSS(value.text) + "</option>");
            });
        });
    });
});

//交易時間起訖驗證
function validateTimeRange() {
    var beginStr = $('#feptxnTxTimeBegin').val();
    var endStr = $('#feptxnTxTimeEnd').val();
    if (!beginStr || beginStr.trim() === "" || !endStr || endStr.trim() === "") {
        alert("『交易時間起訖』為必填欄位");
        return false;
    }

    var baseDate = "1970/01/01 ";
    var beginTime = new Date(baseDate + beginStr);
    var endTime = new Date(baseDate + endStr);
    if (isNaN(beginTime.getTime()) || isNaN(endTime.getTime())) {
        alert("時間格式不正確，請重新輸入");
        return false;
    }
    var timeDiff = endTime.getTime() - beginTime.getTime();

    if (timeDiff < 0) {
        alert("結束時間不能早於開始時間");
        return false;
    }
    if (timeDiff > 3600000) {
        alert("交易時間起訖的查詢範圍不能超過 1 小時");
        return false;
    }
    return true;
}

function changeTime() {
    var form = {
        transactDate: $('#transactDate').val(),
        server: $('#server').val(),
        logType: $('#logType').val(),
        feptxnTxTimeBegin: $('#feptxnTxTimeBegin').val(),
        feptxnTxTimeEnd: $('#feptxnTxTimeEnd').val(),
    }
    doAjax(form, "/atmmon/UI_060610/getLogNames", false, true, function (resp) {
        if (resp==null){
            return;
        }
        if ('undefined' !== typeof resp) {
            $('#selectGZLog option').remove();
            $('#selectGZLog').append("<option value=''>全部</option>");
            $.each(resp, function (index, value) {
                $('#selectGZLog').append("<option value='" + filterXSS(value) + "'>" + filterXSS(value) + "</option>");
            });
        }
    });
}