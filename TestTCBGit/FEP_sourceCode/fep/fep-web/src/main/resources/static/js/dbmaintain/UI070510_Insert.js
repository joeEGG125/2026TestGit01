var formId = "form-validator";
$(document).ready(function() {
	

	$('#btnInsert').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
            $('#'+formId).submit();
     	}
	});
	
	$('#btnPrevPage').click(function() {
		var value = '{\'focusInsert\':\'' + $('#focusInsert').val()+'\'}';
		doFormSubmit('/dbmaintain/UI_070510/prevPage', value);
	});
	
	$('#' + formId).validate(getValidFormOptinal({
		rules: {
            txtBinNoInsert: {
                required: true,
                maxlength:6
            },
            txtBinOrgInsert: {
                required: true,
                maxlength:6
            },
        },
        messages:{
            txtBinNoInsert: {
				maxlength: "最大長度為6",
                required:  "CREDIT CARD BIN不能為空"
            },
            txtBinOrgInsert: {
				maxlength: "最大長度為6",
                required: "發卡組織不能為空"
            },
        }
    }));
	
})