var formId = "form-validator";

$(document).ready(function() {
	$('#btnConfirm').on('click', function() {
		if (doValidateForm(formId)) {
			var jsonData = {
				bkno: $("#bkno").val(),
				confiRm: $("#confiRm").val(),
				txkind: $("#txkind").val()
			};
			doAjax(jsonData, "/rm/UI_028030/executeClick", false, true, function(resp) {
				if ('undefined' !== typeof resp) {
					showMessage(resp.messageType, resp.message);
				}
			});
		}
	});
	$('#' + formId).validate(getValidFormOptinal({
		rules: {
			bkno: {
				required: true,
				number: true,
			},
		},
		messages:{
			bkno: {
				required: "收信行總行代號未輸入",
				number: "請輸入數字",
			},
		}
	}));
})

