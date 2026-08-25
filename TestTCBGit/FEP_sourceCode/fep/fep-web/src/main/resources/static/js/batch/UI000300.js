var formId = "form-validator";
$(document).ready(function () {
	
	// Grid中的超連結(檢視)
    $('.btn-inquiry').click(function() {
		var value = filterXSS($(this).attr("value"));
		$("#historyLogcontent").html(value);
		showInfoAlert('myAlert', '', '');
    });
 	
	initDateTimePicker1('batchStartDate','YYYY-MM-DD');
    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
});

/**
 * 初始化日期時間選擇元件
 * 
 * @param id
 * @param format
 * @param defaultDate
 */
function initDateTimePicker1(id, format, defaultDate) {
	$('#' + id).datetimepicker({
		format: format,
		locale: 'zh-tw',
		allowInputToggle: true,
		useStrict: true,
		toolbarPlacement: 'bottom',
		defaultDate: defaultDate,
		buttons: {
			showToday: true,
			showClear: true,
			showClose: true
		},
		icons: {
			today: 'fa fa-calendar-check'
		},
		tooltips: {
			today: '跳轉到當前日期/時刻',
			clear: '清除',
			close: '關閉面板',
			selectMonth: '選擇月份',
			prevMonth: '上一月',
			nextMonth: '下一月',
			selectYear: '選擇年',
			prevYear: '上一年',
			nextYear: '下一年',
			selectDecade: '選擇十年內',
			prevDecade: '上一個十年',
			nextDecade: '下一個十年',
			prevCentury: '上一個世紀',
			nextCentury: '下一個世紀',
			incrementHour: '增加時',
			pickHour: '選擇時',
			decrementHour: '減少時',
			incrementMinute: '增加分',
			pickMinute: '選擇分',
			decrementMinute: '減少分',
			incrementSecond: '增加秒',
			pickSecond: '選擇秒',
			decrementSecond: '減少秒'
		}
	}); 
}   