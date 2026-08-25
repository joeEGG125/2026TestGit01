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
    $("#BtnComit").bind('click', function() {
        var jsonData = {
                tradingDate: $("#trading").val(),
    			bkno: $("#bkno").val(),
    			stan: $("#stan").val(),
    			queryflagtxt: $("#QueryFlagTxt").text(),
    	};
    	doAjax(jsonData, "/inbk/UI_012290/inquiryDetail", false, true, function(resp) {
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
    		bkno: {
            	required: true,
            },
            stan: {
            	required: true,
            },
    	}
    }));
})

