/**
 * 初始化日期選擇元件
 *
 * @param id
 * @param defaultDate
 */
function initDatePicker(id, defaultDate) {
    initDateTimePicker(id, 'YYYY-MM-DD', defaultDate);
}

/**
 * 初始化時間選擇元件
 *
 * @param id
 * @param defaultDate
 */
function initTimePicker(id, defaultDate) {
    initDateTimePicker(id, 'HH:mm:ss', defaultDate);
}

/**
 * 初始化日期時間選擇元件
 *
 * @param id
 * @param format
 * @param defaultDate
 */
function initDateTimePicker(id, format, defaultDate) {
    var opt = getDateTimePickerOpt(format, defaultDate);
    $('#' + id).datetimepicker(opt);
}

/**
 * 獲取日期時間選擇元件option
 *
 * @param id
 * @param defaultDate
 */
function getDateTimePickerOpt(format, defaultDate) {
    var opt = {
        // debug: true, // 放開這個屬性會Pop的Div不會消失便於debug
        format: format,
        dayViewHeaderFormat: 'YYYY MMMM',
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
    };
    return opt;
}

/**
 * Bootstrap時間選擇控件設置日期時間
 *
 * @param picker
 * @param datetime
 */
function setDateTimePicker(id, datetime) {
    $('#' + id).datetimepicker('_setValue', moment(datetime));
}

/**
 * 勾選表格中某一列每個checkbox觸發的事件
 *
 * @param prefixName
 * @param callback
 */
function doCheckInTableColumn(prefixName, callback) {
    var checkName = prefixName + 'Check';
    var checkAllName = prefixName + 'CheckAll';
    var isCheckedAll = $("input[name='" + checkName + "']").length == $("input[name='" + checkName + "']:checked").length;
    var checkAllObj = $("input[name='" + checkAllName + "']");
    if (checkAllObj.length > 0) {
        checkAllObj.prop("checked", isCheckedAll);
    }
    if ('undefined' !== typeof callback) {
        callback();
    }
}

/**
 * 勾選表格中表頭的checkbox觸發的事件
 *
 * @param checkObj
 * @param prefixName
 * @param callback
 */
function doCheckAllInTableHeader(checkObj, prefixName, callback) {
    var checkName = prefixName + 'Check';
    var checkAllName = prefixName + 'CheckAll';
    var isCheckedAll = checkObj.checked;
    $("input[name='" + checkAllName + "']").prop("checked", isCheckedAll);
    var checkObj = $("input[name='" + checkName + "']");
    if (checkObj.length > 0) {
        checkObj.prop("checked", isCheckedAll);
    }
    if ('undefined' !== typeof callback) {
        callback();
    }
}

/**
 * 是否勾選表格的資料
 *
 * @param prefixName
 * @param alertMsg
 * @returns {Boolean}
 */
function isTableColumnChecked(prefixName, alertMsg) {
    var checkArray = $("input[name='" + prefixName + "Check']:checked");
    if (checkArray.length == 0) {
        if (!stringIsBlank(alertMsg)) {
            showDangerCmnAlert(alertMsg);
        }
        return false;
    }
    return true;
}

/**
 * 獲取勾選表格的資料, 回傳一個JsonArray對象
 *
 * @param prefixName
 * @returns JsonArray
 */
function getTableColumnCheckedData(prefixName) {
    // 取出勾選的json對象, 存入一個數組中
    var checkDataArray = [];
    var checkArray = $("input[name='" + prefixName + "Check']:checked");
    checkArray.each(function () {
        var data = jsonStringToObj($(this).val());
        checkDataArray.push(data);
    });
    return checkDataArray;
}

/**
 * 移除form中元件的disabled屬性, 并得到所有移除掉disable屬性的元件array
 *
 * @param formId
 * @returns JsonArray
 */
function removeAttrDisabled(formId) {
    return $("#" + formId).find(':disabled').removeAttr('disabled');
}

/**
 * 設置disabled屬性
 *
 * @param formId
 */
function addAttrDisabled(disabledField) {
    if (disabledField) {
        disabledField.prop('disabled', 'true');
    }
}

/**
 * Bruce add 即時將金額加入逗號(三個為一組)
 *
 * @param id
 */
function changeCur(id) {
	//1清除 "數字" 和 "," 以外的字元。2清除第1碼是0
	var integerDigits = $('#'+id).val().replace(/[^0-9,]/g,"").replace(/^0{1,}/g,"");
	// 將整數的部分切割成陣列
	integerDigits = integerDigits.replaceAll(',', '').split("");
	// 用來存放3個位數的陣列
	var threeDigits = [];
	// 當數字足夠，從後面取出三個位數，轉成字串塞回 threeDigits
	while (integerDigits.length > 3) {
		threeDigits.unshift(integerDigits.splice(integerDigits.length - 3, 3).join(""));
	}
	threeDigits.unshift(integerDigits.join(""));//陣列用空白連接
	$('#'+id).val(threeDigits.join(','));
}