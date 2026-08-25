var formId = "form-validator";

$(document).ready(function() {
    // 按下確認按鈕
    $('#btnDelete').click(function() {
        if (isTableColumnChecked('dataList', '至少勾選表格中的一筆資料')) {
            var data = getTableColumnCheckedData('dataList');
            doAjax(data, "/rm/UI_028190/deleteClick", false, true, function(resp) {
                if ('undefined' !== typeof resp) {
                    showSuccessCmnAlert(resp.message, function() {
                        // 刪除成功重新查詢主頁資料
                        if (resp.result) {
                            window.location.reload();
                        }
                    });
                }
            });
        }
    });

})