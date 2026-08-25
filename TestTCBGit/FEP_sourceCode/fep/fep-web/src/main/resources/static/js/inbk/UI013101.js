var formId = "form-validator";

$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnConfirm').click(function() {
        var from = {
            apidddl: document.getElementById("apidddl").value,
            curlbl: document.getElementById("curlbl").value,
        }
        doAjax(from, "/inbk/UI_013101/confirmClick", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showMessage(resp.messageType, resp.message);
            }
        });
    });
})

 $("#apidddl").change(function() {
  var apiddl = document.getElementById("apidddl").value;

     if( apiddl === "1600") {
          $("#curlbl").val("001")
//          document.getElementById("curlbl").innerHtml = "001-USD";
     } else {
          $("#curlbl").val("000")
//          document.getElementById("curlbl").innerText = "";
     }
 });