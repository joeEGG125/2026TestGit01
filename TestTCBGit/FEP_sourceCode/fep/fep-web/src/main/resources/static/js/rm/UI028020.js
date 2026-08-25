var formId = "form-validator";

$(document).ready(function() {
    // 按下查詢按鈕
    $('#btnQuery').click(function() {
        var from = {
            inqFlag: document.getElementById("inqFlag").value,
            sno:  document.getElementById("sno").value,
            orgKind: document.getElementById("orgKind").value,
        }
        doAjax(from, "/rm/UI_028020/queryClick", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showMessage(resp.messageType, resp.message);
                document.getElementById("sno").value = resp.data.sno;
                document.getElementById("rc").value = resp.data.rc;
                var options = $('#status option');
                for (var i=0;i<options.length;i++)
                {
                       if(options[i].value === resp.data.status) {
                           options[i].selected = true;
                    }
                }
                document.getElementById("resolve").value = resp.data.resolve;
            }
        });
    });
//    // 建立表單驗證
//    $('#' + formId).validate(getValidFormOptinal({
//        rules: {
//            receiverBank: {
//                required: true,
//            },
//            chnmemo: {
//                required: true,
//            },
//        }
//    }));
})