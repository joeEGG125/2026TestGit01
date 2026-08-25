var formId = "form-validator";

$(document).ready(function() {
	doChange("3000");
	$('#btnConfirm').on('click', function() {
		clearMessage();
		if (doValidateForm(formId)) {
			var jsonData = {
				noticeidd: $("#NOTICE_IDDdl").val(),
				noticetext: $("#NOTICE_DATAText").val(),
				textnotxt: $("#TextNoTxt").val(),
				idtext: $("#IDTxt").val(),
				resulttxt: $("#ResultTxt").val(),
				banktxt: $('#BankIDLbl')[0].innerText
			};
			doAjax(jsonData, "/inbk/UI_013100/execute", false, true, function(resp) {
				if ('undefined' !== typeof resp) {
					showMessage(resp.messageType, resp.message);
				}
			});
		}
	});
	$('#' + formId).validate(getValidFormOptinal({
		rules: {
			textnotxt: {
				required: true,
			},
			idtext: {
				required: true,
			},
		}
	}));
})

function doChange(value) {
	clearMessage();
	doClearValidtForm(formId);
	$("#tr3000 input[type=text]").val("");
	$("#tr4102 input[type=text]").val("");
	$("#tr3000 input[type=text]").attr("disabled", true);
	$("#tr4102 input[type=text]").attr("disabled", true);
	$("#tr4102 select").attr("disabled", true);
	$('#div4102 select option:first').prop('selected', 'selected');
	var id = "tr" + value;
	$("#" + id + " input[type=text]").attr("disabled", false);
	$("#" + id + " select").attr("disabled", false);
}