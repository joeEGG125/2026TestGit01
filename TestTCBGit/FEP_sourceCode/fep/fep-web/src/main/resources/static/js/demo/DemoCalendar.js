var formId = "form-validator";

$(document).ready(function() {
	initDateTimePicker('chooseYear', 'YYYY');
	initDatePicker('activeCalendar', '2022-07-13');
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});
	$('#btnChangeCalendar').click(function() {
		if (doValidateForm(formId)) {
			var chooseYear = $('#chooseYear').val();
			setCalendarYear(chooseYear);
		}
	});
	$('#btnActiveCalendar').click(function() {
		var activeCalendar = $('#activeCalendar').val();
		setCalendarDateActive(activeCalendar);
	});
	$('#btnClearActiveCalendar').click(function() {
		var activeCalendar = $('#activeCalendar').val();
		clearCalendarDateActive(activeCalendar);
	});
	$('#btnClearAllActiveCalendar').click(function() {
		clearAllCalendarDateActive();
	});
	// 建立表單驗證
	$('#' + formId).validate(getValidFormOptinal({
		rules: {
			year: {
				required: true,
			}
		}
	}));
	// 增加並實作日期cell的click事件
	addChooseCalendarDateEvent(function(chooseDate) {
		showInfoCmnAlert(chooseDate);
	});
})