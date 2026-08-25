var formId = "form-validator";

$(document).ready(function() {
	
	if($('input[name=rbtn_ATMNo]:checked').val() != '1') {
		$('#aTMNoTxt').attr('disabled', true);
	}
    
	$('#btnQuery').on('click', function() {
		
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
            $('#'+formId).submit();
     	}
    });
	
	$('#' + formId).validate(getValidFormOptinal({
		rules: {
            aTMNoTxt: {
                maxlength:8
            },
        },
        messages:{
            aTMNoTxt: {
				maxlength: "最大長度為8"
            },
        }
    }));
});

function rbtn_ATMNo_SelectedChanged(value){
		if(value == 0){
			$('#aTMNoTxt').attr('disabled', true);
			$('#aTMNoTxt').val('');
		}else{
			$('#aTMNoTxt').attr('disabled', false);
		}
};
