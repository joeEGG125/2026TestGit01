var formId = "form-validator";
$(document).ready(function() {
	// Grid中交易別超連結
	$('.feptxnTbsdyFisc').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/atmmon/UI_060610/syscomDetail', form);
	});	
});