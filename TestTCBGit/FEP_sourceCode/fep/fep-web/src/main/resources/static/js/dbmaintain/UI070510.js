var formId = "form-validator";
$(document).ready(function() {
	
//	$('#binNo').focus();
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});
	
	$('#' + formId).validate(getValidFormOptinal({
		rules: {
            txtBinNo: {
                maxlength:6
            },
            txtBinOrg: {
                maxlength:6
            },
        },
        messages:{
            txtBinNo: {
				maxlength: "最大長度為6"
            },
            txtBinOrg: {
				maxlength: "最大長度為6"
            },
        }
    }));
	
	// 按下刪除按鈕
	$('#btnDelete').click(function() {
		if (isTableColumnChecked('demo', '至少勾選表格中的一筆資料')) {
			showConfirmDialog('cmnConfirm', '請確認刪除嗎？', function() {
				var data = getTableColumnCheckedData('demo');
				doAjax(data, "/dbmaintain/UI_070510/deleteList", false, true, function(resp) {
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
    $('#btnInsert').click(function() {
		doFormSubmit('/dbmaintain/UI_070510/insertPage', "");
	});
	
})