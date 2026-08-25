var formId = "form-validator";

$(document).ready(function() {
	var rm = new Vue({
		el: '#rm',
		data: {
			senderSeq:"",
			receiverSeq:"",
			newSenderSeq:"",
			newReceiverSeq:"",
		},
		mounted:function (){
			this.$nextTick(function(){
				//調用需要執行的方法
				doAjax("", "/rm/UI_028040/pageLoad", false, true, function(resp) {
					if ('undefined' !== typeof resp) {
						rm.senderSeq = resp.senderSeq;
						rm.receiverSeq = resp.receiverSeq;
					}
				});

			})
		},
	});

	$('#btnQuery').on('click', function() {
		var jsonData = {
			kind: $("#kind").val(),
			txKind: $("#txKind").val(),
			senderSeq: rm.senderSeq,
			receiverSeq: rm.receiverSeq,
			newSenderSeq: rm.newSenderSeq,
			newReceiverSeq: rm.newReceiverSeq,
		};
		doAjax(jsonData, "/rm/UI_028040/queryClick", false, true, function(resp) {
			if ('undefined' !== typeof resp) {
				$("#kind").val(resp.kind)
				rm.senderSeq = resp.senderSeq;
				rm.receiverSeq = resp.receiverSeq;
				showMessage(resp.response, resp.response);
			}
		});
	});

	$('#btnConfirm').on('click', function() {
		if (doValidateForm(formId)) {
			var jsonData = {
				kind: $("#kind").val(),
				txKind: $("#txKind").val(),
				senderSeq: rm.senderSeq,
				receiverSeq: rm.receiverSeq,
				newSenderSeq: rm.newSenderSeq,
				newReceiverSeq: rm.newReceiverSeq,
			};
			doAjax(jsonData, "/rm/UI_028040/executeClick", false, true, function(resp) {
				if ('undefined' !== typeof resp) {
					rm.senderSeq = resp.senderSeq;
					rm.receiverSeq = resp.receiverSeq;
					rm.newSenderSeq = resp.newSenderSeq;
					rm.newReceiverSeq = resp.newReceiverSeq;
					showMessage(resp.messageType, resp.message);
				}
			});
		}
	});


	$('#' + formId).validate(getValidFormOptinal({
		rules: {
			newSenderSeq: {
				number: true,
			},
			newReceiverSeq: {
				number: true,
			},
		},
		messages:{
			newSenderSeq: {
				number: "請輸入小於七位的整數",
			},
			newReceiverSeq: {
				number: "請輸入小於七位的整數",
			},
		}
	}));
})

