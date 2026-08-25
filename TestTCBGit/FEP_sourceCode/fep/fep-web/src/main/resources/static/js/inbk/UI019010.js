var formId = "form-validator";

$(document).ready(function() {
	var confirmAction = $('#' + formId).attr('action');
	var queryAction = confirmAction.replace('/confirm', '/query');

	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		showLoading(true);
		showProcessingMessage(true);

		$('#' + formId).attr('action', queryAction);
		$('#' + formId).submit();
	});

    // 按下確認變更按鈕
     $('#btnConfirm').click(function() {
    	if (doValidateForm(formId)) {
    		showLoading(true);
    		showProcessingMessage(true);
			$('#' + formId).attr('action', confirmAction);
    		$('#' + formId).submit();
    	}
    });
})