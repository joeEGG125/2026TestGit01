var formId = "form-validator";

$(document).ready(function() {
    initDatePicker('cbspendTxDate');


    // 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});

	$('#btnModify').click(function () {
        var data = getFormData(formId);
        doAjax(data, "/dbmaintain/UI_070050/doAllModify", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
				if (resp.result) {
					doFormSubmit('/currentPageAjax', resp, false);
				}
            }
        });
    });
	
	 // Grid中修改按鈕
    $('.btn-modify').click(function () {
        var tr = $(this).parent().parent();
        var cbspendSubsys = tr.children().eq(16).text();
        var cbspendZone = tr.children().eq(17).text();
        var cbspendTbsdy = tr.children().eq(15).text();
        var cbspendTxDate =tr.children().eq(1).text();
        var cbspendEjfno = tr.children().eq(4).text();
        var cbspendResendCnt = tr.find(".txt-cbspendResendCnt").val();
        var data = {"cbspendSubsys": cbspendSubsys,"cbspendZone": cbspendZone,
        "cbspendTbsdy": cbspendTbsdy,"cbspendTxDate": cbspendTxDate,"cbspendEjfno": cbspendEjfno,
        "cbspendResendCnt": cbspendResendCnt};
        doAjax(data, "/dbmaintain/UI_070050/doModify", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showSuccessCmnAlert(resp.message, function () {
                });
            }
        });
    });
})