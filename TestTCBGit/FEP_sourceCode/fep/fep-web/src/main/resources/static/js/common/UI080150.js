var formUploadId = "form-upload";
$(document).ready(function() {
	//上傳檔案
	$('#btnUpload').click(function(){
		if($('#kmsFile')[0].files[0]){
			$('#' + formUploadId).submit();
		}else{
			showDangerMessage('請選擇要上傳的基碼檔!');
		}
	});
	
	$('#kmsFile').change(function(){
		clearMessage();//清除訊息
	});
	
});