var formId = "form-validator";

$(document).ready(function() {
	initDatePicker('tradingDate');
	$("input[name='radioOption']").click(function() {
		clearMessage();
		doClearValidtForm(formId);
		$('#pcode').attr("disabled", true);
		$('#bkno').attr("disabled", true);
		$('#stan').attr("disabled", true);
		$('#ejno').attr("disabled", true);
		var value = $(this).val();
		switch (value) {
			case 'PCODE':
				$('#pcode').attr("disabled", false);
				break;
			case 'STAN':
				$('#bkno').attr("disabled", false);
				$('#stan').attr("disabled", false);
				break;
			case 'EJNO':
				$('#ejno').attr("disabled", false);
				break;
		}
	});
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});
	// Grid中第一列查詢按鈕
	$('.btn-inquiry').click(function() {
		var value = $(this).val();
		var form = jsonStringToObj(value);
		doFormSubmit('/inbk/UI_019020/inquiryDetail', form);
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
			ejno: {
				required: true,
				digits: true
			},
		}
	}));
})