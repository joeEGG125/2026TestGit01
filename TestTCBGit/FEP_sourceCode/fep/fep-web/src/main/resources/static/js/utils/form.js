/**
 * 驗證form表單並提交
 *
 * @param formId
 */
function doFormValidateAndSubmit(formId) {
    if (doValidateForm(formId)) {
        removeAttrDisabled(formId);
        showLoading(true);
        showProcessingMessage(true);
        $('#' + formId).submit();
    }
}

/**
 * 組建form表單提交資料
 *
 * @param url
 * @param form
 * @param showProcessing
 */
function doFormSubmit(url, form, showProcessing) {
    $form = $('<form id="form-submit" method="post"></form>');
    setCsrfToForm($form);
    $.each(form, function (key, value) {
        $form.append('<input type="hidden" name="' + key + '"/>');
    });
    $form.appendTo('body');
    $.each(form, function (key, value) {
        $("#form-submit input[name='" + key + "']").val(value);
    });
    $("#form-submit").attr("action", ctx + url);
    showLoading(true);
    if ((typeof showProcessingMessage !== 'undefined') && showProcessing) {
        showProcessingMessage(true);
    }
    $("#form-submit").submit();
    $("#form-submit").remove();
}

/**
 * 組建form表單提交資料
 *
 * @param url
 * @param form
 * @param showProcessing
 */
function doFormSubmitErrHandle(url, form, showProcessing) {
    $form = $('<form id="form-submit-errhandle" method="post" target="submiterrhandle"></form>');
    setCsrfToForm($form);
    $.each(form, function (key, value) {
        $form.append('<input type="hidden" name="' + key + '"/>');
    });
    $form.appendTo('body');
    $.each(form, function (key, value) {
        $("#form-submit-errhandle input[name='" + key + "']").val(value);
    });
    $("#form-submit-errhandle").attr("action", ctx + url);
    showLoading(true);
    if ((typeof showProcessingMessage !== 'undefined') && showProcessing) {
        showProcessingMessage(true);
    }
    $("#form-submit-errhandle").submit();
    $("#form-submit-errhandle").remove();
}

/**
 * 組建form表單ajax提交資料
 *
 * @param url
 * @param form
 * @param showProcessing
 */
function doAjaxFormSubmit(url, form, isAsync, successFunc, showProcessing) {
    $form = $('<form id="form-submit-ajax" method="post"></form>');
    setCsrfToForm($form);
    $.each(form, function (key, value) {
        $form.append('<input type="hidden" name="' + key + '"/>');
    });
    $form.appendTo('body');
    $.each(form, function (key, value) {
        $("#form-submit-ajax input[name='" + key + "']").val(value);
    });
    $("#form-submit-ajax").attr("action", ctx + url);
    doAjaxFormById("form-submit-ajax", url, isAsync, showProcessing, successFunc);
    $("#form-submit-ajax").remove();
}

/**
 * 獲取表單
 *
 * @param formId
 * @returns formData
 */
function getFormData(formId) {
    return getFormObjData($("#" + formId));
}

/**
 * 獲取表單
 *
 * @param form
 * @returns formData
 */
function getFormObjData(form) {
    var disabledField = form.find('input:disabled,select:disabled').removeAttr('disabled');
    var formData = {};
    var formDataArray = form.serializeArray();
    $.each(formDataArray, function () {
        if (this.name in formData) {
            var oldValue = formData[this.name];
            if (typeof oldValue === 'string') {
                var newValue = [oldValue, this.value];
                formData[this.name] = newValue;
            } else {
                oldValue.push(this.value);
                formData[this.name] = oldValue;
            }
        } else {
            formData[this.name] = this.value || '';
        }
    });
    disabledField.prop('disabled', 'true');
    return formData;
}

/**
 * 在提交form之前, 塞入csrf token
 */
function setCsrfToForm(form) {
    var token = filterXSS($("meta[name='_csrf']").attr("content"));
    var header = filterXSS($("meta[name='_csrf_header']").attr("content"));
    form.append('<input type="hidden" name="' + "_csrf" + '" value="' + token + '"/>');
}

/**
 * 在提交form之前, 塞入csrf token
 */
function setCsrfToFormData(formData) {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    formData["_csrf"] = token;
}