var formId = "form-validator";

$(document).ready(function() {
    //按下查詢按鈕
    $('#btnQuery').click(function() {
        var date = new Date();

        var year = date.getFullYear();
        var month = date.getMonth() + 1;
        var day = date.getDate();
        if (month >= 1 && month <= 9) {
            month = "0" + month;
        }
        if (day >= 0 && day <= 9) {
            day = "0" + day;
        }
        var newDate = year+"-"+month+"-"+day;
        var form = {
            queryTypeDdl: "1",
            datetime:newDate,
        };
        doFormSubmit('/rm/UI_028200/ctrlQueryClick', form);
    });

    //按下UI_028200_1_Detail執行按鈕
    $('#btnConfirm').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // Grid中第一列查詢按鈕
    $('.btnDetail').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/rm/UI_028200_1/execute', form);
    });
    //按下上一頁按鈕
    $('#btnPrevPage1').click(function () {
        var date = new Date();
        var seperator1 = "-";
        var year = date.getFullYear();
        var month = date.getMonth() + 1;
        var strDate = date.getDate();
        if (month >= 1 && month <= 9) {
            month = "0" + month;
        }
        if (strDate >= 0 && strDate <= 9) {
            strDate = "0" + strDate;
        }
        var currentdate = year + seperator1 + month + seperator1 + strDate;
        var form = {
            datetime:  currentdate,
        }
        doFormSubmit('/rm/UI_028200/index', form);
    });

    $('#btnPrevPage2').click(function () {
        var date = new Date();

        var year = date.getFullYear();
        var month = date.getMonth() + 1;
        var day = date.getDate();
        if (month >= 1 && month <= 9) {
            month = "0" + month;
        }
        if (day >= 0 && day <= 9) {
            day = "0" + day;
        }
        var newDate = year+"-"+month+"-"+day;
        var form = {
            queryTypeDdl: "1",
            datetime:newDate,
        }
        doFormSubmit('/rm/UI_028200/ctrlQueryClick', form);
    });
})