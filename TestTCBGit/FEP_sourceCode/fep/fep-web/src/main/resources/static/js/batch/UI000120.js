var formId = "form-validator";

$(document).ready(function () {
    $('.a-inquiry').click(function () {
        var data = {};
        data['historyLogfile'] = $("#historyLogfile").val();
        doAjax(data, "/batch/UI_000120/doViewLogContent", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                doStopMyInterval();
                if (resp.result) {
                    $("#historyLogcontent").html(resp.data)
                    showInfoAlert('historyLogcontentAlert', '', doStartMyInterval);
                } else {
                    showAlert('', resp.messageType, resp.message, doStartMyInterval);
                }
            }
        });
    });
    $(".btnReturn").click(function () {
        var value = $(this).attr("value");
        var data = jsonStringToObj(value);
        doAjax(data, "/batch/UI_000120/doReturn", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                doStopMyInterval();
                showAlert('', resp.messageType, resp.message, doStartMyInterval);
            }
        });
    });
    $(".btnSkip").click(function () {
        var value = $(this).attr("value");
        var data = jsonStringToObj(value);
        doAjax(data, "/batch/UI_000120/doSkip", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                doStopMyInterval();
                showAlert('', resp.messageType, resp.message, doStartMyInterval);
            }
        });
    });
})

function doStartMyInterval() {
    if ('undefined' !== typeof setupMyIntervalFunc) {
        setupMyIntervalFunc();
    }
}

function doStopMyInterval() {
    if ('undefined' !== typeof clearMyInterval) {
        clearMyInterval();
    }
}