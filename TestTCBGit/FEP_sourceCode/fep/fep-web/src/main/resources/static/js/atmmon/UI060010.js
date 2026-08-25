var formId = "form-validator";
var formUploadId = "form-upload";
$(document).ready(function() {
    // 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});
	// Grid中交易別超連結
	$('.atmAtmNo').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/atmmon/UI_060010/resultGrdvRowCommand', form);
	});
	//上傳檔案
	$('#btnUpload').click(function(){
		$('#' + formUploadId).submit();
	});
});