var formId = "form-validator";

$(document).ready(function () {
    initTimePicker('allbankSetCloseTime');
    if ($('#btnType').val()==="insert"){
        $('#allbankBkno').removeAttr("readonly");
        $('#allbankBrno').removeAttr("readonly");
    }
    // 按下變更儲存按鈕
    $('#btnChange').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // 按下放棄變更儲存按鈕
    $('#btnDisChange').click(function () {
        window.location.reload()
    });
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules:{
            "allbankBkno":{
                required: true,
            },
            "allbankBrno":{
                required: true,
            },
            "allbankBrnoChkcode":{
                required: true,
            },
            "allbankUnitBank":{
                required: true,
            }
        },
        messages:{
            "allbankBkno":{
                required: "必須有資料",
            },
            "allbankBrno":{
                required: "必須有資料",
            },
            "allbankBrnoChkcode":{
                required: "必須有資料",
            },
            "allbankUnitBank":{
                required: "必須有資料",
            }
        }
    }));
})
// 文本框改變事件
function brnoChange() {
    var form = {
        allbankBkno:$('#allbankBkno').val(),
        allbankBrno:$('#allbankBrno').val(),
        allbankType:$('#allbankType').val()
    }
    doAjax(form, "/rm/UI_020061_B/setALLBANK_BRNO_CHKCODE", false, true, function (resp) {
        if ('undefined' !== typeof resp) {
            $('#allbankBrnoChkcode').val(resp.data.allbankBrnoChkcode)
        }
    });
}
