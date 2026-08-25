var formValidatorId = "form-validator";
var formDetailedId = "form-detailed";


$(document).ready(function() {
    var fundlog = new Vue({
        el: '#fundlogM',
        data: {
            txDate:"",
            fgAmt:"",
            stan:"",
            atmWorkStatus:"",
            tlrID:"",
            supID:"",
            queryFlag:"False",
        },
    });

    var btnV = new Vue({
        el: '#btnM',
        methods: {
            btnQueryClick:function(){
                if(doValidateForm(formValidatorId)){
                    var jsonData = {
                        fgSeqno: $("#fgSeqnoTxt").val(),
                    };

                    doAjax(jsonData, "/inbk/UI_015313/inquiryMain", false, true, function(resp) {
                        if(resp.result === "Success"){
                            fundlog.txDate = resp.txDateTxt;
                            fundlog.fgAmt = resp.fgAmt;
                            fundlog.stan = resp.stanTxt;
                            fundlog.atmWorkStatus = resp.selectedValue;
                            fundlog.tlrID = resp.tlridTxt;
                            fundlog.supID = resp.supIDTxt;
                            fundlog.queryFlag = resp.queryFlagTxt;
                        }
                        else{
                            btnV.clearDetailed();
                            showMessage(resp.messageType, resp.message);
                        }
                    });
                }
            },
            btnConfirm:function(){
                var jsonData = {
                    fgSeqno: $("#fgSeqnoTxt").val(),
                    txDateTxt: $("#txDate").val(),
                    fgAmt: $("#fgAmt").val(),
                    stanTxt: $("#stan").val(),
                    selectedValue: $("#atmWorkStatus").val(),
                    tlridTxt: $("#tlrID").val(),
                    supIDTxt: $("#supID").val(),
                    queryFlagTxt: $("#QueryFlag").val(),
                };
                doAjax(jsonData, "/inbk/UI_015313/inquiryDetail", false, true, function(resp) {
                	if ('undefined' !== typeof resp) {
                		showMessage(resp.messageType, resp.message);
                	}
                });
            },
            btnClearClick:function(){
                // 按下清除按鈕事件, 重定向當前頁面
                var btnClear = $('#btnClear');
                if (btnClear.length > 0) {
                    doFormSubmit('/clear', {}, false);
                }
            },
            clearDetailed:function(){
                fundlog.txDate = "";
                fundlog.fgAmt = "";
                fundlog.stan = "";
                fundlog.atmWorkStatus = "";
                fundlog.tlrID = "";
                fundlog.supID = "";
            },
        },
    });
    $('#' + formValidatorId).validate(getValidFormOptinal({
        rules: {
            fgSeqnoTxt: {
                required: true,
            },
        }
    }));
    $('#' + formDetailedId).validate(getValidFormOptinal({
        rules: {
            fgAmt: {
                required: true,
                number:true,
                min:0.01,
            },
        },
        messages:{
            fgAmt:{
                required: "金額未輸入",
                number:"金額格式錯誤",
                min:"金額必須大於0"
            }
        }
    }));
})
