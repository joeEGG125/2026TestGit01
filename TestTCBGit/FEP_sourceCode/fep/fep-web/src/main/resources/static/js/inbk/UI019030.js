var formId = "form-validator";
//var tradingDate = "";
$(document).ready(function() {
    tradingDate = $('#tradingDate').val();
    initDatePicker('tradingDate');

     // 按下查詢按鈕
     $('#BtnQuery').click(function() {
     	if (doValidateForm(formId)) {
     		showLoading(true);
     		showProcessingMessage(true);
     		$('#' + formId).submit();
     	}
     });
     // 按下確認按鈕
     $("#BtnComit").bind('click', function() {
         var jsonData = {
     			tradingDate: $("#tradingDate").val(),
     			ejnotxt: $("#ejnotxt").val(),
     			queryok: $("#QueryOK").text(),
     			feptxnPcode: $("#pcodetxt").text(),
     			feptxnTbsdyFisc: $("#feptxnTbsdyFisctxt").text(),
     			rcrbl: $('input:radio.rcrbl:checked').val(),
     			atmrctxt: $("#redio").text(),
     			msgid: $("#MSGID").text(),
     	};
     	console.log($(".rcrbl").val());
     	doAjax(jsonData, "/inbk/UI_019030/excure", false, true, function(resp) {
     		if ('undefined' !== typeof resp) {
     			showMessage(resp.messageType, resp.message);
     		}
     	});
     });

     // 按下RC=4001
      $('.rcrbl').click(function() {
        if($(this).val()==1){
           $('#redio').text("")
        }
        if($(this).val()==2){
           $('#redio').text("0501")
        }
        if($(this).val()==3){
           $('#redio').text("0601")
        }
     });


     $('#' + formId).validate(getValidFormOptinal({
     		rules: {
     		    tradingDate: {
            		required: true,
            		dateISO: true,
            	},
     		    ejnotxt: {
                	required: true,
                	digits: true
                },
            }
     }));
})