var formId = "form-validator";

$(document).ready(function() {
	initDateTimePicker('txtBSDAYS_YEAR', 'YYYY');
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});
	
	// 按下新增按鈕
	$('#btnInsert').click(function() {
		showLoading(true);
		showProcessingMessage(true);
		var form = {
		}
		doFormSubmit('/dbmaintain/UI_070020/insertClick', form);
	});
	
	// 建立表單驗證
	$('#' + formId).validate(getValidFormOptinal({
		rules: {
			year: {
				required: true,
			}
		}
	}));
	var lblBSDAYS_ZONE_CODE = $('#lblBSDAYS_ZONE_CODE').val();
	// 增加並實作日期cell的click事件
	addChooseCalendarDateEvent(function(chooseDate) {
		showLoading(true);
		showProcessingMessage(true);
		var value="{\"BSDAYS_ZONE_CODEDdl\":\""+lblBSDAYS_ZONE_CODE+"\",\"txtBSDAYS_DATE\":\""+chooseDate+"\"}";
		var form = jsonStringToObj(value);
		
		doFormSubmit('/dbmaintain/UI_070020/updateClick', form);
	});
	
})