var formId = "form-validator";

$(document).ready(function () {
    initDatePicker('feptxnTbsdyFisc');
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            feptxnTbsdyFisc: {
                required: true,
                dateISO: true,
            },
        }
    }));

    // 操作按鈕事件
    $('.table').on('click', '.action-btn', function (e) {
        e.preventDefault();

        var action = $(this).data('action'); // 按鈕行為
        var seqNo = $(this).data('seq');     // 交易序號
        var txTime = $(this).data('time');   // UI交易時間

        // 確認提示
        var actionText = $(this).text();
        if (!confirm("確定要對交易序號 [" + seqNo + "] 執行「" + actionText + "」嗎？")) {
            return;
        }

        showLoading(true);
        if (typeof showProcessingMessage === "function") {
            showProcessingMessage(true);
        }
        // AJAX 呼叫後端 API
        var actionUrl = $('#processActionUrl').val();
        var csrfToken = $("input[name='_csrf']").val();
        $.ajax({
            type: "POST",
            url: actionUrl,
            data: {
                action: action,
                seqNo: seqNo,
                txTime: txTime,
                _csrf: csrfToken
            },
            success: function (response) {
                showLoading(false);
                if (response.success) {
                    alert(actionText + " 處理成功！");
                    // 重新查詢更新畫面
                    $('#btnQuery').click();
                } else {
                    alert("處理失敗：" + response.message);
                }
            },
            error: function (xhr, status, error) {
                showLoading(false);
                alert("系統發生異常：" + error);
            }
        });
    });
});