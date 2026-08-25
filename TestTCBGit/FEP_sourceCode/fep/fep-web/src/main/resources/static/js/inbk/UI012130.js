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
//    $('#BtnComit').click(function() {
//        console.log("123132");
////    	doFormSubmit('/inbk/UI_012130/inquiryDetail', form);
//        if (doValidateForm(formId)) {
//    		showLoading(true);
//    		showProcessingMessage(true);
//    		$('#' + formId).submit();
//    	}
//    });
    $("#BtnComit").bind('click', function() {
        var jsonData = {
    			prcresultddl: $("#inbkpendPrcResult").val(),
    			oritxdatetxt: $("#inbkpendOriTxDate").text(),
    			oritbsdyfisctxt: $("#inbkpendOriTbsdyFisc").text(),
    			pcodetxt: $("#inbkpendOriPcode").text(),
    			txatmtxt: $("#inbkpendTxAmt").text(),
                troutbknotxt: $("#inbkpendTroutbkno").text(),
                troutactnotxt: $("#inbkpendTroutActno").text(),
                trinbknotxt: $("#inbkpendTrinBkno").text(),
                trinactnotxr: $("#inbkpendTrinActno").text(),
                cardnotxt: $("#inbkpendMajorActno").text(),
                trinactnoactualtxt: $("#inbkpendTrinActnoActual").text(),
                atmnotxt: $("#inbkpendAtmno").text(),
                oribknotxt: $("#inbkpendOriBkno").text(),
                oristantxt: $("#inbkpendOriStan").text(),
                queryok: $("#QueryOK").text(),
    	};
    	doAjax(jsonData, "/inbk/UI_012130/inquiryDetail", false, true, function(resp) {
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

