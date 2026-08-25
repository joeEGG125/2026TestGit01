var formId = "form-validator";

//上一筆資料
$("#btnPrev").bind('click', function () {
    var form = {
        "": "",
    };
    doFormSubmit("/atmmon/UI_060620_B/inquiryPrev", form)
})
//下一筆資料
$("#btnNext").bind('click', function () {
    var form = {
        "": "",
    };
    doFormSubmit("/atmmon/UI_060620_B/inquiryNext", form)
})

//更新對照檔
$(document).ready(function () {
    checkItem('fepNotifyMail_Customize');
    checkItem('fepNotifyPhone_Customize');
    // 更新對照檔
    $('#btnUpdate').click(function () {
        var chbNotify = 0;
        if ($('#chbNotify').prop('checked')) {
            chbNotify = 1;
        }
        var jsonData = {
            chbNotify: chbNotify,
            description: $("#description").val(),
            remark: $("#remark").val(),
            responsible: $("#responsible").val(),
            notifyMail: $("#notifyMail").val(),
            action: $("#action").val(),
            msgPattern: $("#msgPattern").val(),
            msgkbNotify: {
                fepNotifyMail_APD: $("#fepNotifyMail_APD").prop('checked'),
                fepNotifyMail_SYS: $("#fepNotifyMail_SYS").prop('checked'),
                fepNotifyMail_Customize: $(".fepNotifyMail_Customize").prop('checked'),
                msgkbNotifymail: $(".msgkbNotifymail").val(),
                fepNotifyPhone_APD: $("#fepNotifyPhone_APD").prop('checked'),
                fepNotifyPhone_SYS: $("#fepNotifyPhone_SYS").prop('checked'),
                fepNotifyPhone_Customize: $(".fepNotifyPhone_Customize").prop('checked'),
                msgkbNotifyphone: $(".msgkbNotifyphone").val(),
            },
        };
        doAjax(jsonData, "/atmmon/UI_060620_B/updateDetail", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showMessage(resp.messageType, resp.message);
            }
        });
    });
})

function checkItem(item) {
    var checked = $("." + item).prop("checked");
    if (item === 'fepNotifyMail_Customize') {
        $(".msgkbNotifymail").prop("disabled", !checked);
    } else if (item === 'fepNotifyPhone_Customize') {
        $(".msgkbNotifyphone").prop("disabled", !checked);
    }
}