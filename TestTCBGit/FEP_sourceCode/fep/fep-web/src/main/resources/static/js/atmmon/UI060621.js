var formId = "form-validator";
$(document).ready(function() {
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});

	$('.errorcode').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/atmmon/UI_060621/bindGridDetail', form);
	});	
	
	$('#btnDelete').click(function() {
			if (isTableColumnChecked("db", '至少勾選表格中的一筆資料')) {
				showConfirmDialog('cmnConfirm', '請確認刪除嗎？', function() {
				var data = getTableColumnCheckedData("db");
				doAjax(data, "/atmmon/UI_060621/btnDelete", false, true, function(resp) {
					if ('undefined' !== typeof resp) {
						showSuccessCmnAlert(resp.message, function() {
							// 刪除成功重新查詢主頁資料
							if (resp.result) {
								doFormSubmit('/currentPageAjax', resp, false);
							}
						});
					}
				});
				});
			}
		});
});