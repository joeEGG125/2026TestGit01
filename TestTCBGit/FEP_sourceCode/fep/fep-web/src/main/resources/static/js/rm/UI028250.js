$(document).ready(function() {
    // 按下執行按鈕
    $('#btnExecute').click(function() {
        var form = {
            programID: $("#programID").val(),
            PRGSTAT_FLAG: $('input:radio[name="PRGSTAT_FLAG"]:checked').val(),
        };
        doFormSubmit('/rm/UI_028250/executeClick',form);
    });
})