$('.btn-download').click(function () {
    var data = {'path': $(this).val()};
    doAjaxDownload(data, '/demo/DemoDownload/download');
});
$('#btnClearUser').click(function () {
    var data = {};
    doAjax(data, "/demo/Demo/clearUser", false, true, function (resp) {
        if ('undefined' !== typeof resp) {
            showSuccessCmnAlert(resp.message, function () {
            });
        }
    });
});