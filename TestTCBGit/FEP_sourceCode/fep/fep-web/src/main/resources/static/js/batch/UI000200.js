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
                        doAjax(dForm, "/batch/UI_000200/btnDelete", false, true, function (resp) {
                            if ('undefined' !== typeof resp) {
                                var form = {
                                    task_Name:"",
                                }
                                doFormSubmit('/batch/UI_000200/queryClick',form);
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
        doFormSubmit('/batch/UI_000200/showDetail', form);
    });

    // Grid中第二列修改按鈕
    $('.btn-inquiry').click(function() {
        var value = $(this).attr("value");
        var form =   {
            btnType:"update",
            task_Id:value,
        }
        doFormSubmit('/batch/UI_000200/showDetail', form);
    });
})