$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        var form = {
            queryType: "all",
        };
        doFormSubmit('/rm/UI_028200_2/queryClick', form);
    });

    // Grid中第一列查詢按鈕
    $('.btnDetail').click(function() {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/rm/UI_028200_2_1/queryClick', form);
    });
})