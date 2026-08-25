var formId = "form-validator";

$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        showLoading(true);
        showProcessingMessage(true);
        $('#' + formId).submit();
    });
    // Grid中第一列查詢按鈕
    $('.a-inquiry').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/batch/UI_000100/queryDetails', form);
    });
    // 按下新增跳轉到新增頁面
    $('#btnInsert').click(function() {
        var form = {
            "":"",
        }
        doFormSubmit('/batch/UI_000100/queryDetails', form);
    });
    // 按下刪除刪除Grid中選中的資料
    $('#btnDelete').click(function() {
        let checked = document.getElementsByName('delChecks');
        let checkboxStat = 0;
        for (let i = 0; i < checked.length; i++) {
            if (checked[i].checked){
                checkboxStat = 1;
            }
        }
        if (checkboxStat===0){
            showInfoCmnAlert('至少勾選表格中的一筆數據');
        }else {
            showCmnConfirmDialog('確認刪除資料?', function (){
                showLoading(true);
                showProcessingMessage(true);
                $('#idForm').submit();
            });
        }
    });
    // 按下重建排程
    $('#btnQuartz').click(function() {
        var jsonData = {};
        showCmnConfirmDialog('確定要重建所有排程資料嗎?', function() {
            doAjax(jsonData, "/batch/UI_000100/makeQuartz", false, true, function (resp) {
                if ('undefined' !== typeof resp) {
                    showMessage(resp.messageType, resp.message);
                }
            });
        });
    });
    $('#btnChangeHost').click(function () {
        var form = {
            "":"",
        }
        doFormSubmit('/batch/UI_000100/btnChangeHost', form);
    });
})