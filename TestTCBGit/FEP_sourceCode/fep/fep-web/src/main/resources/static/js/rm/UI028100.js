var formId = "form-validator";

$(document).ready(function() {
	var rm = new Vue({
		el: '#rm',
		data: {
			txDate: "",
			oTxDate:"",
			oFepNo:"",
			oEjno:"",
			actno:"",
			amt:"",
			oRmSno:"",
			oFiscSno:"",
			oSenderBank:"",
			receiverBank:"",
			inName:"",
			outName:"",
			memo:"",
		},
		mounted:function (){
			this.$nextTick(function(){
				//調用需要執行的方法
				doAjax("", "/rm/UI_028100/pageLoad", false, true, function(resp) {
					if ('undefined' !== typeof resp) {
						$('#txDate').val(resp.txDate);
						showMessage(resp.messageType, resp.message);
					}
				});

			})
		},
	});

	$('#btnQuery').on('click', function() {
		if (doValidateForm(formId)) {
			var jsonData = {
				txDate: $("#txDate").val(),
				stan: $("#stan").val(),
				fepNo: $("#fepNo").val(),
				ejfno: $("#ejfno").val(),
				fiscSno: $("#fiscSno").val(),
				senderBank: $("#senderBank").val(),
				rmSno: $("#rmSno").val(),
			};
			doAjax(jsonData, "/rm/UI_028100/queryClick", false, true, function(resp) {
				if ('undefined' !== typeof resp) {
					rm.oTxDate = resp.oTxDate;
					rm.oFepNo = resp.oFepNo;
					rm.oEjno = resp.oEjno;
					rm.actno = resp.actno;
					rm.amt = resp.amt;
					rm.oRmSno = resp.oRmSno;
					rm.oFiscSno = resp.oFiscSno;
					rm.oSenderBank = resp.oSenderBank;
					rm.receiverBank = resp.receiverBank;
					rm.inName = resp.inName;
					rm.outName = resp.outName;
					rm.memo = resp.memo;
					showMessage(resp.messageType, resp.message);
				}
			});
		}
	});

	$('#btnConfirm').on('click', function() {
		if (doValidateForm(formId)) {
			doAjax("", "/rm/UI_028100/executeClick", false, true, function(resp) {
				if ('undefined' !== typeof resp) {
					showMessage(resp.messageType, resp.message);
				}
			});
		}
	});


	$('#' + formId).validate(getValidFormOptinal({
		rules: {
			stan:{
				number:true,
			},
			fepNo: {
				number: true,
			},
			ejfno: {
				number: true,
			},

			fiscSno: {
				required: true,
				number: true,
			},
			senderBank: {
				required: true,
			},
			rmSno: {
				required: true,
				number: true,
			},
		},
		messages:{
			stan:{
				number:"請輸入整數",
			},
			fepNo: {
				number: "請輸入整數",
			},
			ejfno: {
				number: "請輸入整數",
			},

			fiscSno: {
				required: "必須有值",
				number: "請輸入整數",
			},
			senderBank: {
				required: "必須有值",
			},
			rmSno: {
				required: "必須有值",
				number: "請輸入整數",
			},
		}
	}));


})

