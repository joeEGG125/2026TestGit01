var formId = "form-validator";
var detailFormId = "form-validator-detail";

$(document).ready(function() {
	// 按下新增按鈕
	$('#btnAdd').click(function() {
		var form = {};
		doFormSubmit('/common/UI_080010/insertDetail', form);
	});
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		showLoading(true);
		showProcessingMessage(true);
		$('#' + formId).submit();
	});
	// 按下刪除按鈕
	$('#btnDelete').click(function() {
		if (isTableColumnChecked('demo', '至少勾選表格中的一筆資料')) {
			showConfirmDialog('cmnConfirm', '請確認刪除嗎？', function() {
				var data = getTableColumnCheckedData('demo');
				doAjax(data, "/common/UI_080010/deleteList", false, true, function(resp) {
					if ('undefined' !== typeof resp) {
						if (resp.result) {
							doFormSubmit('/currentPageAjax', resp, false);
						}
					}
				});
			});
		}
	});
	// Grid中第一列查詢按鈕
	$('.btn-inquiry').click(function() {
		var value = $(this).val();
		var form = jsonStringToObj(value);
		doFormSubmit('/common/UI_080010/inquiryDetail', form);
	});
	// Grid中第一列修改按鈕
	$('.btn-modify').click(function() {
		var value = $(this).val();
		var form = jsonStringToObj(value);
		form.logonIdQ = $('#logonIdQ').val();
		form.userNameQ = $('#userNameQ').val();
		doFormSubmit('/common/UI_080010/inquiryDetail', form);
	});
	// Grid中第一列刪除按鈕
	$('.btn-delete').click(function() {
		var value = $(this).val();
		showConfirmDialog('cmnConfirm', '請確認刪除嗎？', function() {
			var form = jsonStringToObj(value);
			doFormSubmit('/common/UI_080010/deleteDetail', form);
		});
	});
	// Grid中第一列解鎖按鈕
	$('.btn-unlock').click(function() {
		var value = $(this).val();
		showConfirmDialog('cmnConfirm', '請確認解鎖嗎？', function() {
			var form = jsonStringToObj(value);
			doFormSubmit('/common/UI_080010/unlock', form);
		});
	});
	// Grid中第一列重置密碼按鈕
	$('.btn-restsscode').click(function() {
		var value = $(this).val();
		showConfirmDialog('cmnConfirm', '請確認要重置密碼嗎？', function() {
			var form = jsonStringToObj(value);
			doFormSubmit('/common/UI_080010/restsscode', form);
		});
	});
})