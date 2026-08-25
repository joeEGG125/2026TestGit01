var formId = "form-validator";

$(document).ready(function() {
	// Grid中第一列查詢按鈕
	$('.a-inquiry').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/atmmon/UI_060610_A/inquiryFeplogDetail', form);
	});
	$('#btnDownload').click(function () {
		donwloading = true;
		var feptxnEjfno = $('#feptxnEjfno').val();
		var ejfnO1 = $('#ejfnO1').val();
		var ejfnO2 = $('#ejfnO2').val();
		var ejfnO3 = $('#ejfnO3').val();
		var ejfnO4 = $('#ejfnO4').val();
		var feptxnTraceEjfno = $('#feptxnTraceEjfno').val();
		var feptxnTxDate = $('#feptxnTxDate').val();
		var formData = jsonStringToObj("{\"feptxnEjfno\":\""+feptxnEjfno+"\",\"ejfnO1\":\""+ejfnO1+"\",\"ejfnO2\":\""+ejfnO2
		+"\",\"ejfnO3\":\""+ejfnO3+"\",\"ejfnO4\":\""+ejfnO4+"\",\"feptxnTraceEjfno\":\""+feptxnTraceEjfno+"\",\"feptxnTxDate\":\""+feptxnTxDate +"\"}");

		doAjaxDownload(formData, '/atmmon/UI_060550/download', function () {
			donwloading = false;
		});
	});
})