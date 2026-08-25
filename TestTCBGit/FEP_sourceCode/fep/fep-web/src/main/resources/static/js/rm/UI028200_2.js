$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        var form = {
            queryType: "all",
        };
        doFormSubmit('/rm/UI_028200_2/queryAllClick', form);
    });

    // Grid中第一列查詢按鈕
    $('.btnDetail').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/rm/UI_028200_2/queryClick', form);
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
})