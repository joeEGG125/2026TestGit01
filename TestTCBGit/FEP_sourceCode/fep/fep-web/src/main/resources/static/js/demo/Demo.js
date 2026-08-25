var formId = "form-validator";

$(document).ready(function () {
    initDatePicker('tradingDate');
    initTimePicker('tradingTime');
    $("input[name='radioOption']").click(function () {
        clearMessage();
        doClearValidtForm(formId);
        $('#pcode').attr("disabled", true);
        $('#bkno').attr("disabled", true);
        $('#stan').attr("disabled", true);
        $('#ejno').attr("disabled", true);
        var value = $(this).val();
        switch (value) {
            case 'PCODE':
                $('#pcode').attr("disabled", false);
                break;
            case 'STAN':
                $('#bkno').attr("disabled", false);
                $('#stan').attr("disabled", false);
                break;
            case 'EJNO':
                $('#ejno').attr("disabled", false);
                break;
        }
    });
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // Grid中查詢按鈕
    $('.btn-inquiry').click(function () {
        var value = $(this).val();
        var form = jsonStringToObj(value);
        doFormSubmit('/demo/Demo/inquiryDetail', form);
    });
    // 建立表單驗證
    var validatorOption = getValidFormOptinal({
        rules: {
            tradingDate: {
                required: true,
                dateISO: true,
            },
            bkno: {
                required: true,
            },
            stan: {
                required: true,
            },
            ejno: {
                required: true,
                digits: true
            },
            txtIp: {
                required: true,
            }
        }
    });
    validatorOption = addCheckIPValidator('txtIp', '請輸入正確的IP地址', validatorOption);
    validatorOption = addCheckDecimalValidator('txtDecimal', '輸入格式錯誤,應輸入decimal(7,4)', validatorOption);
    validatorOption = addCheckDecimalValidator('txtDecimal2', '輸入格式錯誤,應輸入decimal(7,2)', validatorOption);
    $('#' + formId).validate(validatorOption);
    $('#btnCmnConfirm').click(function () {
        showCmnConfirmDialog('確認?', function () {
            showSuccessCmnAlert('你按下了確認按鈕');
        }, function () {
            showWarningCmnAlert('你按下了取消按鈕');
        });
    });
    $('#btnConfirm').click(function () {
        showConfirmDialog('myConfirm', '', function () {
            showSuccessCmnAlert('你按下了確認按鈕');
        }, function () {
            showWarningCmnAlert('你按下了取消按鈕');
        });
    });
    $('#btnAlert').click(function () {
        showInfoAlert('myAlert', '', function () {
            showWarningCmnAlert('你按下了關閉按鈕');
        });
    });
    $('#btnDelete').click(function () {
        if (isTableColumnChecked('demo', '至少勾選表格中的一筆資料')) {
            var data = getTableColumnCheckedData('demo');
            doAjax(data, "/demo/Demo/doDelete", false, true, function (resp) {
                if ('undefined' !== typeof resp) {
                    showSuccessCmnAlert(resp.message, function () {
                        // 刪除成功重新查詢主頁資料
                        if (resp.result) {
                            doFormSubmit('/currentPageAjax', resp, false);
                        }
                    });
                }
            });
        }
    });
    $('#btnShowPopover').click(function () {
        showPopover('tradingDate', '日期是不是選錯了!!!');
        showPopover('bkno', '不可以是空白哦!!!');
        showPopover('stan', 'Cannot be empty!!!');
    });
    $('#btnHidePopover').click(function () {
        hidePopover('bkno');
        hidePopover('stan');
    });
    $('#btnSftp').click(function () {
        var data = {};
        doAjax(data, "/demo/Demo/doSftp", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showSuccessCmnAlert(resp.message, function () {

                });
            }
        });
    });
    $('#btn500Post').click(function () {
        var form = {'tradingDate': '2020-03-01'};
        doFormSubmit('/demo/Demo/500_post', form);
    });
    $('#btn500Ajax').click(function () {
        var data = {};
        doAjax(data, "/demo/Demo/500_ajax", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showSuccessCmnAlert(resp.message, function () {
                });
            }
        });
    });
    $('#btn404').click(function () {
        location.href = ctx + '/123/123';
    });
    $('#btnClearUser').click(function () {
        var data = {};
        doAjax(data, "/demo/Demo/clearUser", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showSuccessCmnAlert(resp.message, function () {
                });
            }
        });
    });
    // Grid中修改按鈕
    $('.btn-modify').click(function () {
        var tr = $(this).parent().parent();
        var feptxnEjfno = tr.find(".txt-feptxnEjfno").val();
        var feptxnReqRc = tr.children().eq(8).text();
        var data = {"feptxnEjfno": feptxnEjfno, "feptxnReqRc": feptxnReqRc};
        doAjax(data, "/demo/Demo/doModify", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showSuccessCmnAlert(resp.message, function () {
                });
            }
        });
    });
    $('#btnGetFormData').click(function () {
        var formData = getFormData(formId);
        showInfoCmnAlert(jsonObjToString(formData));
    });
    $('#btnTxTest').click(function () {
        var data = {};
        doAjax(data, "/demo/Demo/doTxTest", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showCmnAlert(resp.messageType, resp.message, function () {
                });
            }
        });
    });
})