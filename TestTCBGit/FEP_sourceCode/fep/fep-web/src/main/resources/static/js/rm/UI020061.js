var formId = "form-validator";

$(document).ready(function () {
    // 按下查詢按鈕
    $('#btnExecute').click(function () {
        var excuteTypeDdl = document.getElementById("excuteTypeDdl").value
        var value = $(this).attr("value");
        var form = {
            allbankBkno:value.allbankBkno,
            allbankBrno:value.allbankBrno,
            countyDDL:value.countyDDL,
            regionDDL:value.regionDDL,
            excuteTypeDdlSelectedIndexChanged:"1",
            fileUpload:"",
            fileUpload1:"",
        }
        if (excuteTypeDdl==="1") {
            doFormSubmit('/rm/UI_020061_A/index',form);
        }else if (excuteTypeDdl==="2") {
            doFormSubmit('/rm/UI_020061_B/index',form);
        }else if (excuteTypeDdl==="3") {
            doFormSubmit('/rm/UI_020061_C/index',form);
        }else{
            doFormSubmit('/rm/UI_020061_D/index',form);
        }
    });
})