/**
 * 表單驗證
 *
 * @param formId
 */
function doValidateForm(formId) {
    var validatorResult = true;
    var validatorForm = $('#' + formId);
    if (validatorForm.length > 0) {
        var validator = validatorForm.validate();
        if (!jQuery.isEmptyObject(validator.settings.rules)) {
            validatorResult = validatorForm.valid()
        }
    }
    return validatorResult;
}

/**
 * 表單驗證
 *
 * @param validatorForm
 */
function doValidateFormObj(validatorForm) {
    var validatorResult = true;
    if (validatorForm.length > 0) {
        var validator = validatorForm.validate();
        if (!jQuery.isEmptyObject(validator.settings.rules)) {
            validatorResult = validatorForm.valid()
        }
    }
    return validatorResult;
}

/**
 * 清除表單驗證
 *
 * @param formId
 */
function doClearValidtForm(formId) {
    var validatorForm = $('#' + formId);
    if (validatorForm.length > 0) {
        $('input[data-toggle="popover"]')
            .removeClass('is-invalid')
            .removeAttr('data-toggle')
            .removeAttr('data-placement')
            .removeAttr('data-template')
            .removeAttr('data-content')
            .removeAttr('data-original-title')
            .removeAttr('title')
            .removeAttr('aria-describedby')
            .removeAttr('aria-invalid')
            .removeAttr('data-html');
        $('.panel-popover-error').remove();
    }
}

/**
 * 清除表單驗證
 *
 * @param validatorForm
 */
function doClearValidtFormObj(validatorForm) {
    if (validatorForm.length > 0) {
        $('input[data-toggle="popover"]')
            .removeClass('is-invalid')
            .removeAttr('data-toggle')
            .removeAttr('data-placement')
            .removeAttr('data-template')
            .removeAttr('data-content')
            .removeAttr('data-original-title')
            .removeAttr('title')
            .removeAttr('aria-describedby')
            .removeAttr('aria-invalid')
            .removeAttr('data-html');
        $('.panel-popover-error').remove();
    }
}

/**
 * 顯示隱藏PopoverError
 *
 * @param display
 */
function doDisplayRemovePopoverError(display) {
    var popoverError = $('.panel-popover-error');
    if (popoverError.length > 0) {
        if (display) {
            popoverError.show();
        } else {
            popoverError.hide();
        }
    }
}

/**
 * 獲取表單驗證的選項資料
 *
 * @param formId
 */
function getValidFormOptinal(option) {
    var result = {
        ignore: "",
        errorElement: 'span',
        errorPlacement: function (error, element) {
        },
        highlight: function (element, errorClass, validClass) {
            var displayNone = $(".card-condition").hasClass('collapsed-card');
            if (displayNone) {
                $('.btn-condition-collapse').trigger('click');
            }
            $(element).addClass('is-invalid');
            $(element).attr('data-toggle', 'popover')
                .attr('data-placement', 'right')
                .attr('data-template', '<div class="popover panel-popover-error" role="tooltip"><div class="arrow"></div><div class="popover-body"></div></div>')
                .attr('data-content', this.errorMap[$(element).attr('name')] + '<span class="popover-close popover-close-' + $(element).attr('name') + '" aria-hidden="true">×</span>')
                .attr('data-html', true);
            $(element).popover('show');
            $('.popover-close-' + encodeURIComponent($(element).attr('name'))).click(function () {
                $(element).popover('hide');
            });
        },
        unhighlight: function (element, errorClass, validClass) {
            $(element).removeClass('is-invalid');
            $(element).removeAttr('data-toggle')
                .removeAttr('data-placement')
                .removeAttr('data-template')
                .removeAttr('data-content')
                .removeAttr('data-original-title')
                .removeAttr('title')
                .removeAttr('aria-describedby')
                .removeAttr('aria-invalid')
                .removeAttr('data-html');
            $(element).popover('dispose');
        }
    };
    return $.extend(true, option, result);
}

/**
 * 增加日期時間起訖檢核, 即檢核起始日期時間不能大於終止日期時間
 *
 * @param formId 表單ID
 * @param beginDateId 日期時間起元件ID
 * @param endDateId 日期時間訖元件ID
 * @param message 錯誤訊息
 * @param originalValidatorOption 原始的表單驗證option
 */
function addAndGetDateLessEqualValidator(formId, beginDateId, endDateId, message, originalValidatorOption) {
    var ruleName = 'dateLessEqual';
    $.validator.addMethod(ruleName, function (value, element, param) {
        var beginDate = value;
        var endDate = $(param)[0].value;
        if ($(param).length > 1) {
            endDate = $(param)[1].value;
        }
        if (stringIsBlank(beginDate) && stringIsBlank(endDate)) {
            return true;
        }
        return (beginDate <= endDate);
    });
    var dateLessEqualOption = {
        rules: {},
        messages: {}
    };
    dateLessEqualOption.rules[beginDateId] = {};
    dateLessEqualOption.rules[beginDateId][ruleName] = "#" + formId + " #" + endDateId;
    dateLessEqualOption.messages[beginDateId] = {};
    dateLessEqualOption.messages[beginDateId][ruleName] = message;
    if (originalValidatorOption) {
        return $.extend(true, dateLessEqualOption, originalValidatorOption);
    }
    return dateLessEqualOption;
}

/**
 * 增加IP地址檢核
 *
 * @param inputId 輸入的IP地址文本框ID
 * @param message 錯誤訊息
 * @param originalValidatorOption 原始的表單驗證option
 */
function addCheckIPValidator(inputId, message, originalValidatorOption) {
    var ruleName = 'checkIP';
    $.validator.addMethod(ruleName, function (value, element, param) {
        if (stringIsBlank(value)) {
            return true;
        }
        return matchIp(value);
    });
    var checkIPOption = {
        rules: {},
        messages: {}
    };
    checkIPOption.rules[inputId] = {};
    checkIPOption.rules[inputId][ruleName] = true;
    checkIPOption.messages[inputId] = {};
    checkIPOption.messages[inputId][ruleName] = message;
    if (originalValidatorOption) {
        return $.extend(true, checkIPOption, originalValidatorOption);
    }
    return checkIPOption;
}

/**
 * 增加Decimal檢核
 *
 * @param inputId
 * @param message
 * @param originalValidatorOption
 * @returns {{messages: {}, rules: {}}|*}
 */
function addCheckDecimalValidator(inputId, message, originalValidatorOption) {
    var fixNumberFormat = $('#' + inputId).attr('FixNumberFormat');
    if (stringIsBlank(fixNumberFormat)) {
        return;
    }
    var tmp = fixNumberFormat.split(",");
    if (tmp.length <= 1) {
        return;
    }
    var ruleName = 'checkDecimal' + tmp[0] + tmp[1];
    $.validator.addMethod(ruleName, function (value, element, param) {
        if (stringIsBlank(value)) {
            return true;
        }
        return matchDecimal(value, tmp[0], tmp[1]);
    });
    var checkDecimalOption = {
        rules: {},
        messages: {}
    };
    checkDecimalOption.rules[inputId] = {};
    checkDecimalOption.rules[inputId][ruleName] = true;
    checkDecimalOption.messages[inputId] = {};
    checkDecimalOption.messages[inputId][ruleName] = message;
    var required = $('#' + inputId).attr('RequireFieldCheck');
    if (required.toUpperCase() === 'TRUE') {
        checkDecimalOption.rules[inputId]['required'] = true;
    }
    if (originalValidatorOption) {
        return $.extend(true, checkDecimalOption, originalValidatorOption);
    }
    return checkDecimalOption;
}