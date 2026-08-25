var formId = "form-validator";

$(document).ready(function() {
    initDatePicker('dtTxDate');
    initDatePicker('dtOrgDate');
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            dtTxDate: {
                required: true,
                dateISO: true,
            },
            dtOrgDate: {
                required: true,
                dateISO: true,
            },
            ddlOriginal: {
                required: true,
            },
            tbFepNo: {
                required: true,
            },
            tbTxAmt: {
                required: true,
            },
            tbBrno: {
                required: true,
            },
            tbOrgFepNo: {
                required: true,
            },
        }
    }));

})