var formId = "form-validator";

$(document).ready(function () {
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    //按下刪除按鈕
    $('#btnDelete').click(function () {
        var checked = document.getElementsByClassName('checked');
        var checkboxStat = 0;
        for (var i = 0; i < checked.length; i++) {
            if (checked[i].checked){
                checkboxStat = 1;
            }
        }
        if (checkboxStat===0){
            showInfoCmnAlert('至少勾選表格中的一筆資料');
        }else {
            showCmnConfirmDialog('確認刪除資料?', function() {
                for (var i = 0; i < checked.length; i++) {
                    if (checked[i].checked){
                        checkboxStat = 1;
                        var dForm = jsonStringToObj(checked[i].value);
                        doAjax(dForm, "/rm/UI_020061_B/btnDelete", false, true, function (resp) {
                            if ('undefined' !== typeof resp) {
                                var form = {
                                    allbankBkno:$('#allbankBkno').val(),
                                    allbankBrno:$('#allbankBrno').val(),
                                }
                                doFormSubmit('/rm/UI_020061_B/index',form);
                            }
                        });
                    }
                }
            });
        }
    });
    //按下新增按鈕
    $('#btnInsert').click(function () {
        var form = {
            btnType:"insert",
        }
        doFormSubmit('/rm/UI_020061_B/showDetial', form);
    });
    // 按下清除按鈕
    $('#btnClear1').click(function () {
        $('#allbankBkno').val("");
        $('#allbankBrno').val("");
    });
    // Grid中第二列按明細查詢按鈕
    $('.btnDetail').click(function () {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        form.allbankSetCloseTime =
        doFormSubmit('/rm/UI_020061_B/showDetial', form);
    });
})