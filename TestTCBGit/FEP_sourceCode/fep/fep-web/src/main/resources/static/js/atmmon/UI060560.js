var formId = "form-validator";

$(document).ready(function() {
	initDatePicker('feptxnTbsdyFisc');
	initDatePicker('feptxnTxDate');
	initTimePicker('feptxnTxTimeBegin');
	initTimePicker('feptxnTxTimeEnd');
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
		    //$('#btnDownload').prop('disabled', false); //無資料不啟用下載按鈕
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});
	// 按下下載按鈕
    $('#btnDownload').click(function() {
		var totalCount = parseInt($(this).data('total'), 10) || 0;
		if (totalCount > 5000) {
			alert('查詢結果超過5000筆，請縮小查詢範圍!!');
			return false; // 中斷執行，不進行下載
		}
		
    	var form = $('#' + formId);
    	var action = form.attr('action');
    	form.attr('action', $(this).data('url'));
    	form[0].submit();
    	form.attr('action', action);
    });
	// Grid中第一列查詢按鈕
	$('.a-inquiry').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/atmmon/UI_060560/inquiryDetail', form);
	});
	// 建立表單驗證
	var validatorOption = getValidFormOptinal({
		rules: {
			feptxnTbsdyFisc: {
				dateISO: true,
			},
			feptxnTxDate: {
			    required: true,
				dateISO: true,
			},
			feptxnEjfno: {
				digits: true
			},
			feptxnTraceEjfno: {
				digits: true
			},
			feptxnTxAmt: {
				digits: true
			},
		}
	});
	validatorOption = addAndGetDateLessEqualValidator(formId, 'feptxnTxTimeBegin', 'feptxnTxTimeEnd', '交易時間起不可大於交易時間訖', validatorOption);
	$('#' + formId).validate(validatorOption);
})