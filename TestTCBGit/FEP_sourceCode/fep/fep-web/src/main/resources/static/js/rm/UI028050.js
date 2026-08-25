var formId = "form-validator";
var formDetailedId = "form-detailed";

$(document).ready(function() {
	var rm = new Vue({
		el: '#rm',
		data: {
			senderSeqRv:"",
			receiverSeqRv:"",
			newSenderSeqRv:"",
			newReceiverSeqRv:"",
		},
		mounted:function (){
			this.$nextTick(function(){
				//調用需要執行的方法
				doAjax("", "/rm/UI_028050/pageLoad", false, true, function(resp) {
					if ('undefined' !== typeof resp) {
						showMessage(resp.messageType, resp.message);
					}
				});

			})
		},
	});

	$('#btnQuery').on('click', function() {
		if (doValidateForm(formDetailedId)) {
			var jsonData = {
				kind: $("#kind").val(),
				bkno: $("#bkno").val(),
				senderSeqRv: rm.senderSeqRv,
				receiverSeqRv: rm.receiverSeqRv,
				newSenderSeqRv: rm.newSenderSeqRv,
				newReceiverSeqRv: rm.newReceiverSeqRv,
			};
			doAjax(jsonData, "/rm/UI_028050/queryClick", false, true, function(resp) {
				if ('undefined' !== typeof resp) {
					rm.senderSeqRv = resp.senderSeqRv;
					rm.receiverSeqRv = resp.receiverSeqRv;
					showMessage(resp.messageType, resp.message);
				}
			});
		}
	});

	$('#btnConfirm').on('click', function() {
		if (doValidateForm(formDetailedId)) {
			if (doValidateForm(formId)) {
				var jsonData = {
					kind: $("#kind").val(),
					bkno: $("#bkno").val(),
					senderSeqRv: rm.senderSeqRv,
					receiverSeqRv: rm.receiverSeqRv,
					newSenderSeqRv: rm.newSenderSeqRv,
					newReceiverSeqRv: rm.newReceiverSeqRv,
				};
				doAjax(jsonData, "/rm/UI_028050/executeClick", false, true, function(resp) {
					if ('undefined' !== typeof resp) {
						rm.senderSeqRv = resp.senderSeqRv;
						rm.receiverSeqRv = resp.receiverSeqRv;
						rm.newSenderSeqRv = resp.newSenderSeqRv;
						rm.newReceiverSeqRv = resp.newReceiverSeqRv;
						showMessage(resp.messageType, resp.message);
					}
				});
			}
		}
	});


	$('#' + formId).validate(getValidFormOptinal({
		rules: {
			bkno:{
				required:true,
				rangelength:[3,3],
			},
			newSenderSeq: {
				number: true,
			},
			newReceiverSeq: {
				number: true,
			},
		},
		messages:{
			bkno:{
				required:"必須選取",
				rangelength:"請輸入三位數整數",
			},
			newSenderSeq: {
				number: "請輸入小於七位的整數",
			},
			newReceiverSeq: {
				number: "請輸入小於七位的整數",
			},
		}
	}));

	$('#' + formDetailedId).validate(getValidFormOptinal({
		rules: {
			bkno:{
				required:true,
				rangelength:[3,3],
			},
		},
		messages:{
			bkno:{
				required:"必須選取",
				rangelength:"請輸入三位數整數",
			},
		}
	}));
})

