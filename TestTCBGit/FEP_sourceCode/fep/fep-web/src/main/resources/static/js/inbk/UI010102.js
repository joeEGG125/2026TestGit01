$(document).ready(function() {
	$("#btnExecute").bind('click', function() {
		clearMessage();
		var jsonData = {
			keyId: $("#selectKeyId").val()
		};
		doAjax(jsonData, "/inbk/UI_010102/execute", false, true, function(resp) {
			if ('undefined' !== typeof resp) {
				showMessage(resp.messageType, resp.message);
			}
		});
	});
})