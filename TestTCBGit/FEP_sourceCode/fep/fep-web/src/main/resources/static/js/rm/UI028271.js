var formId = "form-validator";

$(document).ready(function() {
	var rm = new Vue({
		el: '#rm',
		data: {
			amlFlag:"",
		},
		mounted:function (){
			this.$nextTick(function(){
				//調用需要執行的方法
				doAjax("", "/rm/UI_028271/pageLoad", false, true, function(resp) {
					if ('undefined' !== typeof resp) {
						$("#amlFlag").val(resp.amlFlag)
					}
				});

			})
		},
	});

	$('#btnConfirm').on('click', function() {
		var jsonData = {
			qryAmlFlag: $("#qryAmlFlag").val(),
			amlFlag: $("#amlFlag").val(),
		};
		doAjax(jsonData, "/rm/UI_028271/executeClick", false, true, function(resp) {
			if ('undefined' !== typeof resp) {
				$("#amlFlag").val(resp.amlFlag)
				showMessage(resp.messageType, resp.message);
			}
		});
	});

})

