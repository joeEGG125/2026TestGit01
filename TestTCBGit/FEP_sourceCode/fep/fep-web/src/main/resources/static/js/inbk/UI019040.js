var formId = "form-validator";

$(document).ready(function() {
	initDatePicker('fwdrstTxDate');
	$("input[name='radioOption']").click(function() {
		clearMessage();
		var value = $(this).val();
		switch (value) {
			case 'ORDER':
				$("#fwdtxnTxId").removeAttr('disabled');
				break;
			default:
				$("#fwdtxnTxId").attr('disabled', 'disabled');
				break;
		}
		$('#btnQuery').trigger('click');
	});
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});
	// 建立表單驗證
	$('#' + formId).validate(getValidFormOptinal({
		rules: {
			fwdrstTxDate: {
				required: true,
				dateISO: true,
			},
			fwdtxnTxId: {
				required: true,
			},
		}
	}));
})