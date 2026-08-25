var formId = "form-validator";

$(document).ready(function() {
	var rm = new Vue({
		el: '#rm',
		data: {
			rmout:{},
		},
	});

	$('#btnQuery').on('click', function() {
		var jsonData = {
			kind: $("#kind").val(),
			uiItem: $("#uiItem").val(),
			owpriority: $("#owpriority").val(),
		};
		doAjax(jsonData, "/rm/UI_028130/queryClick", false, true, function(resp) {
			if ('undefined' !== typeof resp) {
				rm.rmout = resp.rmout;
				showMessage(resp.messageType, resp.message);
			}
		});
	});

	$('#btnConfirm').on('click', function() {
		var kind = $("#kind").val();
		if(kind === "3"){
			if (doValidateForm(formId)) {
				var jsonData = {
					kind: $("#kind").val(),
					uiItem: $("#uiItem").val(),
					owpriority: $("#owpriority").val(),
				};
				doAjax(jsonData, "/rm/UI_028130/executeClick", false, true, function(resp) {
					if ('undefined' !== typeof resp) {
						rm.rmout = resp.rmout;
						showMessage(resp.messageType, resp.message);
					}
				});
			}
		} else {
			var jsonData = {
				kind: $("#kind").val(),
				uiItem: $("#uiItem").val(),
				owpriority: $("#owpriority").val(),
			};
			doAjax(jsonData, "/rm/UI_028130/executeClick", false, true, function(resp) {
				if ('undefined' !== typeof resp) {
					rm.rmout = resp.rmout;
					showMessage(resp.messageType, resp.message);
				}
			});
		}
	});

	$('#kind').on('change', function() {
		if($('#kind').val() === "5"){
			$('#owpriority').val("0");
			$('#owpriority').attr("disabled",true);
		} else {
			$('#owpriority').attr("disabled",false);
		}
	});


	$('#' + formId).validate(getValidFormOptinal({
		rules: {
			uiItem: {
				required: true,
				number: true,
			},
		},
		messages:{
			uiItem: {
				required: "必須有值",
				number: "請輸入整數",
			},
		}
	}));

})

