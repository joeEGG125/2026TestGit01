var formId = "form-validator";
$(document).ready(function () {
    // 按下[變更儲存]按鈕
    $('#btnChange').click(function () {
	    if($('#atmfeeTxMm').val() != '') {
		    var now = new Date();
		    var day = (now.getMonth() + 1).toString();
		    var inputtext = $('#atmfeeTxMm').val();
		    alertmonth = '';
    	    if(day==1) {
      			alertmonth = 12;
      		}else {
      			alertmonth = "0" + (Number(day)-1);
      		}
		    if(day.length==1) {
		    	day = "0" + day;
		    }	    
		    var date = now.getFullYear().toString()  + day;	  
		    var showdd = inputtext.substring(4,6);	
		    var showdmonth = inputtext.substring(0,4);
		    if(now.getFullYear().toString() == showdmonth) {
	          if(Number(date)-Number(inputtext) != 1) {
					showPopover('atmfeeTxMm', '目前只能維護'+alertmonth+'月的資料');
					return;	  	
	          }
	        }else {
					showPopover('atmfeeTxMm', '目前只能維護'+alertmonth+'月的資料');
					return;	  	
	        }		    

	    }
	    
	    var atmfeeFee = $('#atmfeeFee').val();
		if (atmfeeFee.indexOf('.') >= 0) {
			var spl = atmfeeFee.split('.');
			  if(spl[0].length > 3 || spl[1].length > 4 || spl[1].length ==0) {
						showPopover('atmfeeFee', '手續費格式錯誤');
						return;	
			  }
		}else {
			 if(atmfeeFee.length > 3) {
					showPopover('atmfeeFee', '手續費格式錯誤');
					return;	
		  }
		}
	    
	    
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // 按下[放棄變更]按鈕
    $('#btnClear1').click(function () {
        var value = $(this).attr("value");
        var form = jsonStringToObj(value);
        doFormSubmit('/osm/UI_130100/showDetail', form);
    });

    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            atmfeeSeqNo: {
                required: true
            },
            atmfeeTxMm: {
                required: true,
				rangelength:[6,6]
            },
			atmfeeName: {
				required: true
			},
			atmfeeCur: {
				required: true
			},
			atmfeeFee: {
				required: true,
				number:true
			},
			atmfeeFiscFlag: {
				required: true
			},
			atmfeePcode: {
				minlength:4
			}
        },
        messages: {
            atmfeeSeqNo: {
                required: "請輸入手續費序號"
            },
            atmfeeTxMm: {
                required: "請輸入年月",
				rangelength: $.validator.format("年月格式錯誤")
            },
			atmfeeName: {
                required: "請輸入手續費名稱"
            },
			atmfeeCur: {
                required: "請選擇幣別"
            },
			atmfeeFee: {
                required: "手續費必須輸入",
				number:"手續費須為數字"
            },
			atmfeeFiscFlag: {
                required: "請選擇跨行記號"
            },
			atmfeePcode: {
                minlength: "欄位「財金PCODE」少於4碼"
            }
        }
    }));
})