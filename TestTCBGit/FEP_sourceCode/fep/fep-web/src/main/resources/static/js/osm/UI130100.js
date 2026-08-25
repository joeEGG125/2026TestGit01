var formId = "form-validator";
$(document).ready(function() {
	$('#alarm_no').focus();
	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		showLoading(true);
		showProcessingMessage(true);
		$('#' + formId).submit();
	});
	
	// 按下刪除按鈕
	$('#btnDelete').click(function() {
		if (isTableColumnChecked('demo', '至少勾選表格中的一筆資料')) {
			showConfirmDialog('cmnConfirm', '請確認刪除嗎？', function() {
				var data = getTableColumnCheckedData('demo');
				doAjax(data, "/osm/UI_130100/deleteList", false, true, function(resp) {
					if ('undefined' !== typeof resp) {
						if (resp.result) {
							doFormSubmit('/currentPageAjax', resp, false);
						}
					}
				});
			});
		}
	});

    //按下新增按鈕
    $('#btnInsert').click(function () {
        var form = {
            btnType:"insert",
        }
        doFormSubmit('/osm/UI_130100/showDetail', form);
    });
	
	// Grid中第二列修改按鈕
    $('.a-inquiry').click(function() {
    		var inputtext = $(this).attr("field");
 		    var now = new Date();
		    var day = (now.getMonth() + 1).toString();
		    if(day.length==1) {
		    	day = "0" + day;
		    }	    
		    var date = now.getFullYear().toString()  + day;	  
		    var showdd = inputtext.substring(4,6);	
		    var showdmonth = inputtext.substring(0,4);
		    if(now.getFullYear().toString() == showdmonth) {
	          if(Number(date)-Number(inputtext) == 1) {
				var value = $(this).attr("value");
				var form = jsonStringToObj(value);
				doFormSubmit('/osm/UI_130100/showDetail', form);  	
	          }else {
				showPopover('tbxTX_MM', '只能維護上個月的資料');
			  }
	        }	
	           

    });
    
})