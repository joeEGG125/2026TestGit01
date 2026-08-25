var formId = "form-validator";
$(document).ready(function() {
	
//	$('#binNo').focus();
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});
	
	//按下更新按鈕
	$('.btnUpdate').click(function () {
		//#btnIMS
	    var checkDataArray = [];
	    //抓取當頁所有的checkbox
	    var checkArray = $("input[name^='msgctlDataCheck']");
		const cbsProc = this.id === 'btnIMS' ? "Y" : 'N';
		const procName = cbsProc === 'Y' ? 'IMS' : 'FEP';
	    var i = 0
	    for(const e of checkArray){
	        //當頁的checkbox中有選取的列
	        const isChecked = $(e).prop("checked");
	        if(isChecked){
				const rowMsgid = $(e).val(); 
				var jsonData={
					msgctlMsgid:rowMsgid,
					uiProcType:cbsProc
				}
			    //var data = jsonStringToObj(jsonData);
	            checkDataArray.push(jsonData);
	        }
			i++;
	    }
	    if (checkDataArray.length === 0){
	        showInfoCmnAlert('至少勾選表格中的一筆數據');
	    }else {
	        showCmnConfirmDialog('共選取'+checkDataArray.length+'筆，是否確認更新交易處理方式為' + procName + '?', function() { 
	            doAjax(checkDataArray, "/dbmaintain/UI_070070/updateCbsProc", false, true, function (resp) {
					if ('undefined' !== typeof resp) {
						if (resp.result) {
							doFormSubmit('/currentPageAjax', resp, false);
						}
					}
	            });
	        });
	    }
	});
	
	$("#uiTxType1").change(function(){
		let utype1 = this.value;
		changeSelect('uiTxType2',this.value);
	});
	
})

function checkAll(obj){
	var checkArray = $("input[name^='msgctlDataCheck']");
	for(const e of checkArray){
		$(e).prop("checked", obj.checked);
	}
}

function changeCheckAll(obj){
	var checkAll = $("#allCheckBox")[0];
	var checkArray = $("input[name^='msgctlDataCheck']");
	var i = 0;
	if(obj.checked == true){
		if(checkAll.checked != true){
			for(const e of checkArray){
				if($(e).prop("checked") == true){
					i++;
				}
			}
			if(checkArray.length == i){
				checkAll.checked = true;
			}
		}
	}else{
		if(checkAll.checked == true){
			checkAll.checked = false;
		}
	}
	
}

function changeSelect(selId2, selValue1){
	if(selValue1 == ''){
		$("#" + selId2 + " option").remove();
		$.each(options2, function(i){
			// 2025-02-03 modified for 【Client Potential XSS】
			// $("#" + selId2).append($("<option value='" + options2[i].value + "'>" + options2[i].text + "</option>"));
			$("#" + selId2).append($("<option value='" + filterXSS(options2[i].value) + "'>" + filterXSS(options2[i].text) + "</option>"));
		});
	}else{
		$.each(maps, function(key, val){
			if(selValue1 == key){
				$("#" + selId2 + " option").remove();
				let seled = maps[key];
				$.each(options2, function(i){
					if(options2[i].value == ''){
						// 2025-02-03 modified for 【Client Potential XSS】
						// $("#" + selId2).append($("<option value='" + options2[i].value + "'>" + options2[i].text + "</option>"));
						$("#" + selId2).append($("<option value='" + filterXSS(options2[i].value) + "'>" + filterXSS(options2[i].text) + "</option>"));
					}else if($.inArray(options2[i].value, seled) >= 0){
						// 2025-02-03 modified for 【Client Potential XSS】
						// $("#" + selId2).append($("<option value='" + options2[i].value + "'>" + options2[i].text + "</option>"));
						$("#" + selId2).append($("<option value='" + filterXSS(options2[i].value) + "'>" + filterXSS(options2[i].text) + "</option>"));
					}
				});
			}
		});
	}
}