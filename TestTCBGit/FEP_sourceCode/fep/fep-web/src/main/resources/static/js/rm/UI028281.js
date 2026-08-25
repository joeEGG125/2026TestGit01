var formId = "form-validator";

$(document).ready(function () {
    initDatePicker('txDate');
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });

    // 按下執行按鈕
    $('#btnConfirm').click(function () {
        var form = {
            txDate: $('.txDate').val(),
        }
        if ($('#queryData').val() === "1") {
            if (isTableColumnChecked('dataList', '至少勾選表格中的一筆資料')) {
                var data = getTableColumnCheckedData('dataList');
                doAjax(data, "/rm/UI_028281/executeClick", false, true, function (resp) {
                    if ('undefined' !== typeof resp) {
                        showSuccessCmnAlert(resp.message, function () {
                            // 執行成功重新查詢主頁資料
                            if (resp.result) {
                                doFormSubmit('/rm/UI_028281/queryClick', form);
                            }
                        });
                    }
                });
            }
        } else {
            showMessage("WARNING", "請先做查詢再執行")
        }
    });

    //按下全部執行按鈕
    $('#btnConfirmAll').click(function () {
        var form = {
            txDate: $('.txDate').val(),
        }
        if ($('#queryData').val() === "1") {
            var txDate = form.txDate;
            doAjax(txDate, "/rm/UI_028281/executeBtn_Click", false, true, function (resp) {
                if ('undefined' !== typeof resp) {
                    showSuccessCmnAlert(resp.message, function () {
                        // 執行成功重新查詢主頁資料

                        if (resp.result) {
                            doFormSubmit('/rm/UI_028281/queryClick', form);
                        }
                    });
                }
            });
        } else {
            showMessage("WARNING", "請先做查詢再執行")
        }
    });
})