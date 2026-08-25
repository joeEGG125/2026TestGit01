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

    // Grid中交易別超連結
    $('.atmAtmNo').click(function () {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/atmmon/UI_060010_Q/resultGrdvRowCommand', form);
    });

    $('#btnDownload').click(function () {
        var formData = getFormData(formId);
        doAjaxDownload(formData, '/atmmon/UI_060010_Q/doDownload');
    });


    //按下更新按鈕
    $('#btnUpdate').click(function () {
        var checkDataArray = [];
        //抓取當頁所有的checkbox
        var checkArray = $("input[name^='atmFepConnectionCheck']");
        var i = 0
        for (const e of checkArray) {
            //當頁的checkbox中有選取的列
            const isChecked = $(e).prop("checked");
            if (isChecked) {
                //抓取有checked這一列select(是否連線至fep)欄位的值
                const rowSelect = $("select[name='" + i + e.name + "']").val();
                //抓取有checked這一列ATMNO欄位的值
                const rowAtmNo = $("a[name='" + i + "atmAtmNo']").text();
                //抓取有checked這一列ATMIP欄位的值
                const rowAtmIp = $("input[name='" + i + "atmIp']").val();
                var jsonData = {
                    atmAtmNoTxt: rowAtmNo,
                    atmFepConnection: rowSelect,
                    atmIpTxt: rowAtmIp
                }
                //var data = jsonStringToObj(jsonData);
                checkDataArray.push(jsonData);
            }
            i++;
        }
        if (checkDataArray.length === 0) {
            showInfoCmnAlert('至少勾選表格中的一筆數據');
        } else {
            showCmnConfirmDialog('共選取' + checkDataArray.length + '筆，是否確認更新資料?', function () {
                doAjax(checkDataArray, "/atmmon/UI_060010_Q/updateFepConnect", false, true, function (resp) {
                    if ('undefined' !== typeof resp) {
                        if (resp.result) {
                            doFormSubmit('/currentPageAjax', resp, false);
                        }
                    }
                });
            });
        }
    });

});

function openFepConnect() {
    //抓取當頁所有的checkbox
    var checkArray = $("input[name^='atmFepConnectionCheck']");
    var i = 0
    for (const e of checkArray) {
        //當頁的checkbox中有選取的列
        const isChecked = $(e).prop("checked");
        if (isChecked) {
            $("select[name='" + i + e.name + "']").prop('disabled', false);
            $("input[name='" + i + "atmIp']").prop('disabled', false);
        } else {
            $("select[name='" + i + e.name + "']").prop('disabled', true);
            $("input[name='" + i + "atmIp']").prop('disabled', true);
        }
        i++;
    }
}