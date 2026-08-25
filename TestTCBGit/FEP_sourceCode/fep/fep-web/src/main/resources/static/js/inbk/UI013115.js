var formId = "form-validator";

$(document).ready(function() {
	$('#bankNoTxt').attr('disabled', true);
    $("rqvBankNo").hide();
    
	$('#btnConfirm').on('click', function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
            $('#'+formId).submit();
     	}
    });
	
	$('#' + formId).validate(getValidFormOptinal({
		rules: {
            bankNoTxt: {
                required: true,
                maxlength:3
            },
            aTMNoTxt: {
                required: true
            },
        },
        messages:{
            bankNoTxt: {
				maxlength: "最大長度為3",
                required: "銀行代號不能為空"
            },
            aTMNoTxt: {
                required: "ATM代號不能為空"
            },
        }
    }));
});

function BankRbl_SelectedIndexChanged(){
		document.getElementById("bankNoTxt").value="";
		
		if(document.getElementById("bankRbl").value==1) {
            $('#bankNoTxt').attr('disabled', true);
            $("rqvBankNo").hide();
            
		}else {
            $('#bankNoTxt').attr('disabled', false);
            $("rqvBankNo").show();
		}
		
	};
