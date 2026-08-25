var formId = "form-validator";

$(document).ready(function() {
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
                    tradingDate: $("#trading").val(),
                    pcode: $("#pcode").val(),
                    oribknotxt: $("#bkno").val(),
                    oristantxt: $("#stan").val(),
        			txatmtxt: $("#txatmtxt").text(),
        			troutactnotxt: $("#troutActno").text(),
        			troutbknotxt: $("#troutbknotxt").text(),
        			ecinstructionddl: $("#ecinstructionddl").val(),
        			oridatetimetxt: $("#oridatetime").text(),
        			merchantid: $("#merchantid").text(),
        			atmno: $("#atmno").text(),
        			queryok: $("#QueryOK").text(),
        	};
        	doAjax(jsonData, "/inbk/UI_012140/inquiryDetail", false, true, function(resp) {
        		if ('undefined' !== typeof resp) {
        			showMessage(resp.messageType, resp.message);
        		}
        	});
        });
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
    	rules: {
    		tradingDate: {
    			required: true,
    			dateISO: true,
    		},
    		oribknotxt: {
            	required: true,
            },
            oristantxt: {
            	required: true,
            },
    	}
    }));
})