var formValidatorId = "form-validator";
var formDetailedId = "form-detailed";


$(document).ready(function() {
    var fundlog = new Vue({
        el: '#fundlogV',
        data: {
            txDate:"",
            ejfno:"",
            fgseqnotxt1:"",
            fgAmt:"",
            stan:"",
            atmWorkStatus:"",
            tlrID:"",
            supID:"",
            queryFlag:"False",
        },
    });

    var btnV = new Vue({
        el: '#btnV',
        methods: {
            btnQueryClick:function(){
                if(doValidateForm(formValidatorId)){
                    var jsonData = {
                        fgSeqno: $("#fgSeqnoTxt").val(),
                    };
                    doAjax(jsonData, "/inbk/UI_019313/getFundlog", false, true, function(resp) {
                        if(resp.result === "Success"){
                            fundlog.txDate = resp.fundlog.fundlogTxDate;
                            fundlog.ejfno = resp.fundlog.fundlogEjfno;
                            fundlog.fgseqnotxt1 = resp.fundlog.fundlogFgSeqno;
                            fundlog.fgAmt = resp.fgAmt;
                            fundlog.stan = resp.fundlog.fundlogStan;
                            if(resp.fundlog.fundlogStatus === "Y" || resp.fundlog.fundlogStatus === "P" || resp.fundlog.fundlogStatus === "N"){
                                fundlog.atmWorkStatus = resp.fundlog.fundlogStatus;
                            }else{
                                fundlog.atmWorkStatus = "";
                            }
                            fundlog.tlrID = resp.tlrid;
                            fundlog.supID = resp.fundlog.fundlogSupno;
                            fundlog.queryFlag = "True";
                        }else{
                            btnV.clearDetailed();
                            showMessage(resp.messageType, resp.message);
                        }
                    });
                }
            },
            insertBtnClick:function(){
                if(doValidateForm(formDetailedId)){
                    var jsonData = {
                        fgAmt: fundlog.fgAmt,
                    };
                    doAjax(jsonData, "/inbk/UI_019313/insertFundlog", false, true, function(resp) {
                        if(resp.result === "Success"){
                            $("#fgSeqnoTxt").val(resp.fundlog.fundlogFgSeqno);
                            fundlog.txDate = resp.fundlog.fundlogTxDate;
                            fundlog.fgAmt = resp.fgAmt;
                            fundlog.stan = resp.fundlog.fundlogStan;
                            if(resp.fundlog.fundlogStatus === "Y" || resp.fundlog.fundlogStatus === "P" || resp.fundlog.fundlogStatus === "N"){
                                fundlog.atmWorkStatus = resp.fundlog.fundlogStatus;
                            }else{
                                fundlog.atmWorkStatus = "";
                            }

                            fundlog.tlrID = resp.tlrid;
                            fundlog.supID = resp.fundlog.fundlogSupno;
                            fundlog.queryFlag = "False";
                        }else{
                            btnV.clearDetailed();
                        }
                        showMessage(resp.messageType, resp.message);
                    });
                }
            },
            updateBtnClick:function(){
                if(fundlog.queryFlag === "True"){
                    if(doValidateForm(formDetailedId)){
                        var jsonData = {
                            fgSeqno: $("#fgSeqnoTxt").val(),
                            fgAmt: fundlog.fgAmt,
                            txDateTxt:fundlog.txDate,
                            ejfnoTxt:fundlog.ejfno,
                            selectedValue:fundlog.atmWorkStatus,
                            tlridTxt:fundlog.tlrID,
                        };
                        doAjax(jsonData, "/inbk/UI_019313/updateFundlog", false, true, function(resp) {
                            if(resp.result === "Success"){
                                $("#fgSeqnoTxt").val(fundlog.fgseqnotxt1);
                            }
                            showMessage(resp.messageType, resp.message);
                        });
                    }
                }else{
                    showMessage("DANGER", "請先查詢,再傳送交易處理結果!");
                }
            },
            delBtnClick:function(){
                if(fundlog.queryFlag === "True"){
                    if(doValidateForm(formDetailedId)){
                        var jsonData = {
                            fgAmt: fundlog.fgAmt,
                            txDateTxt:fundlog.txDate,
                            ejfnoTxt:fundlog.ejfno,
                            selectedValue:fundlog.atmWorkStatus,
                            tlridTxt:fundlog.tlrID,
                        };
                        doAjax(jsonData, "/inbk/UI_019313/delFundlog", false, true, function(resp) {
                            if(resp.result === "Success"){
                                $("#fgSeqnoTxt").val("");
                                btnV.clearDetailed();
                                fundlog.queryFlag = "False";
                            }
                            showMessage(resp.messageType, resp.message);
                        });
                    }
                }else{
                    showMessage("DANGER", "請先查詢,再傳送交易處理結果!");
                }
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
