var formId = "form-validator";
$(document).ready(function() {
	$('#alarm_no').focus();
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
				doAjax(data, "/atmmon/UI_060080/deleteList", false, true, function(resp) {
					if ('undefined' !== typeof resp) {
						if (resp.result) {
							doFormSubmit('/currentPageAjax', resp, false);
						}
					}
				});
			});
		}
	});

    //按下新增按鈕
    $('#btnInsert').click(function () {
        var form = {
            btnType:"I",
        }
        doFormSubmit('/atmmon/UI_060080/showDetail', form);
    });
	
	// Grid中第二列修改按鈕
    $('.btn-inquiry').click(function() {
        var value = $(this).attr("value");
        var form =   {
            btnType:"E",
            alarm_no:value,
        }
        doFormSubmit('/atmmon/UI_060080/showDetail', form);
    });
})