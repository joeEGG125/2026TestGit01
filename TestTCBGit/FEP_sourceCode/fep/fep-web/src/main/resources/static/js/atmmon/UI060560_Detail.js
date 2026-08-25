var formId = "form-validator";

$(document).ready(function() {
	// 按下查詢按鈕
	var btnQuery = $('#btnQuery');
	if (btnQuery.length > 0) {
		btnQuery.click(function() {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		});
	}

	//按下上一頁按鈕
//    $('#btnPrevPage').click(function () {
//        var form = {
//        }
//        doFormSubmit('/atmmon/UI_060560/Main', form);
//    });

	var btn = document.getElementById('btnUcdidHistory');
    if (!btn) return;
    btn.addEventListener('click', function(){
        var form = document.getElementById('form-validator1');
        if (!form) return;

        showLoading(true);
        showProcessingMessage(true);

        form.submit();
    });
})