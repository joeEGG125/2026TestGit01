var formId = "form-validator";

$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
       var from = {
           orgpcodeddl: document.getElementById("orgpcodeddl").value,
       }
       doAjax(from, "/rm/UI_028021/queryClick", false, true, function (resp) {
           if ('undefined' !== typeof resp) {
               showMessage(resp.messageType, resp.message);
               document.getElementById("orgtxamt").value = resp.data.orgtxamt;
               document.getElementById("orgfiscno").value = resp.data.orgfiscno;
               document.getElementById("orgrmsno").value = resp.data.orgrmsno;
               document.getElementById("orgreceiverbank").value = resp.data.orgreceiverbank;
               document.getElementById("orgpcode").value = resp.data.orgpcode;
               document.getElementById("orgstan").value = resp.data.orgstan;
           }
       });
    });
})