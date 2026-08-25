var batchForm = "batchForm";

$('#' + batchForm).validate(getValidFormOptinal({
    rules: {
        batchName: {
            required: true,
        },
    },
    messages: {
        batchName: {
            required: "必須有值",
        },
    }
}));

var vm = new Vue({
    el: '#UI000100',
    data: {
        items: {
            schedule: false,
            singletime: false,
            checkbusinessdate: false,
            denyconcurrentexec: false,
            enable: false,
        },
        batch: {},
        //星期
        mwChk: [],
        //周
        wmChk: [],
        //月
        mChk: [],
        mList: [],
        //日
        mdChk: [],
        mdList: [],
        groupChk: [],
        groupList: [],
        subsys: [],
        radioType: "",
        detail: "",
        tasks: [],
        taskList: [],
        currentIndex: -1,
        tskId: -1,
        insertTaskId: -1,
        insertJobsContinueOnFail: 0,
        messageType: "info",
        message: "",
        jobsContinueOnFails: [],
        detailJobsContinueOnFail: -1,
        notify: {},
    },
    methods: {
        mCheckAll(status) {
            if (status === "false") {
                vm.mChk = [];
            } else {
                vm.mList.forEach((item) => {
                    if (vm.mChk.indexOf(item.value) == -1) {
                        vm.mChk.push(item.value)
                    }
                })
            }
        },
        mdCheckAll(status) {
            if (status === "false") {
                vm.mdChk = [];
            } else {
                vm.mdList.forEach((item) => {
                    if (vm.mdChk.indexOf(item.value) == -1) {
                        vm.mdChk.push(item.value)
                    }
                })
            }
        },
        groupCheckAll(status) {
            if (status === "false") {
                vm.groupChk = [];
            } else {
                vm.groupList.forEach((item) => {
                    if (vm.groupChk.indexOf(item.value) == -1) {
                        vm.groupChk.push(item.value)
                    }
                })
            }
        },
        clertData() {
            vm.items = {
                schedule: false,
                singletime: false,
                checkbusinessdate: false,
                denyconcurrentexec: false,
                enable: false,
            };
            vm.batch = {};
            //星期
            vm.mwChk = [];
            //周
            vm.wmChk = [];
            //月
            vm.mChk = [];
            vm.mList = [];
            //日
            vm.mdChk = [];
            vm.mdList = [];
            vm.groupChk = [];
            vm.groupList = [];
            vm.subsys = [];
            vm.tasks = [];
            vm.taskList = [];
            vm.radioType = "";
            vm.detail = "";
            vm.currentIndex = -1;
            vm.tskId = -1;
            vm.insertTaskId = -1;
            vm.insertJobsContinueOnFail = 0;
            vm.messageType = "info";
            vm.message = "";
            vm.jobsContinueOnFails = [];
            vm.detailJobsContinueOnFail = -1;
            vm.notify = {};
        },
        startTick() {
            doAjax("", "/batch/UI_000100/details", false, true, function (resp) {
                vm.clertData();
                vm.detail = resp.detail;
                vm.subsys = resp.subsys;
                vm.groupList = resp.startGroup;
                vm.notify = resp.notify;
                $(".batchSchedule").hide();
                $('.changeTask').hide();
                if (vm.detail === 'update') {
                    $('.changeTask').show();
                    if (resp.date !== null) {
                        $('#batchDate').val(resp.date);
                        $('#batchTime').val(resp.time);
                    } else {
                        $('#batchDate').val(new Date().getFullYear() + "-" + ("0" + (new Date().getMonth() + 1)).slice(-2) + "-" + new Date().getDate());
                        $('#batchTime').val(("0" + new Date().getHours()).slice(-2) + ":" + ("0" + new Date().getMinutes()).slice(-2));
                    }
                    initDatePicker('batchDate');
                    initDateTimePicker('batchTime', 'HH:mm');
                    vm.mdList = resp.mdList;
                    vm.mList = resp.mList;
                    if (resp.batch.batchScheduleMonths !== null) {
                        vm.mChk = resp.batch.batchScheduleMonths.split(',');
                    }
                    if (resp.batch.batchScheduleWeekdays !== null) {
                        vm.mwChk = resp.batch.batchScheduleWeekdays.split(',');
                    }
                    if (resp.batch.batchScheduleWhickweeks !== null) {
                        vm.wmChk = resp.batch.batchScheduleWhickweeks.split(',');
                    }
                    if (resp.batch.batchScheduleMonthdays !== null) {
                        vm.mdChk = resp.batch.batchScheduleMonthdays.split(',');
                    }
                    vm.batch = resp.batch;
                    if (stringIsBlank(vm.batch.batchScheduleType)) {
                        vm.batch.batchScheduleType = "D";
                    } else if (vm.batch.batchScheduleType === 'O') {
                        vm.batch.batchScheduleType = 'M'
                    }
                    if (resp.batch.batchStartgroup !== null) {
                        vm.groupChk = resp.batch.batchStartgroup.split(',');
                    }
                    vm.radioType = resp.radioType;
                    vm.tasks = resp.tasks;
                    vm.taskList = resp.taskList;
                    vm.jobsContinueOnFails = resp.jobsContinueOnFails;
                    if (vm.batch.batchSchedule === 1) {
                        $(".batchSchedule").show();
                        vm.items.schedule = !vm.items.schedule;
                    }
                    if (vm.batch.batchSingletime === 1) {
                        vm.items.singletime = !vm.items.singletime;
                    }
                    if (vm.batch.batchCheckbusinessdate === 1) {
                        vm.items.checkbusinessdate = !vm.items.checkbusinessdate;
                    }
                    if (vm.batch.batchDenyconcurrentexec === 1) {
                        vm.items.denyconcurrentexec = !vm.items.denyconcurrentexec;
                    }
                    if (vm.batch.batchEnable === 1) {
                        vm.items.enable = !vm.items.enable;
                    }
                    if (vm.batch.batchExecuteHostName === null) {
                        vm.batch.batchExecuteHostName = " ";
                    }
                    $(".batchNotifymail").prop("disabled", !vm.notify.fepNotifyMail_Customize);
                    $(".batchNotifyphone").prop("disabled", !vm.notify.fepNotifyPhone_Customize);
                    batchExecuteHostNameOnChange(vm.batch.batchExecuteHostName);
                    if (stringIsBlank(vm.batch.batchDailyRepetitionType)) {
                        vm.batch.batchDailyRepetitionType = "D";
                    }
                    batchDailyRepetitionTypeOnClick(vm.batch.batchDailyRepetitionType)
                } else {
                    vm.batch.batchScheduleType = null;
                    vm.batch.batchNotifytype = 0;
                    vm.batch.batchEnable = 0;
                    vm.batch.batchSchedule = 0;
                    vm.batch.batchSingletime = 0;
                    vm.items.checkbusinessdate = !vm.items.checkbusinessdate;
                    vm.batch.batchCheckbusinessdate = 1;
                    vm.batch.batchDenyconcurrentexec = 0;
                    vm.batch.batchSubsys = '0';
                    vm.batch.batchZone = '   ';
                    vm.batch.batchExecuteHostName = " ";
                    vm.batch.batchDailyRepetitionType = "D";
                }
            });
        },
        taskDetails: function (status) {
            // 如果之前有點一筆資料的明細按鈕, 則要先將該筆沒有存儲的資料設定還原
            this.taskRestore();
            vm.currentIndex = status;
            vm.tskId = vm.tasks[status].JOBTASK_TASKID;
            vm.detailJobsContinueOnFail = vm.tasks[status].JOBS_CONTINUEONFAIL;
        },
        taskStore(i) {
            var jsonData = {
                sender: -1,
                jobId: vm.tasks[i].JOBS_JOBID,
                tskId: vm.tasks[i].JOBTASK_TASKID,
                jobsContinueOnFail: vm.tasks[i].JOBS_CONTINUEONFAIL,
            };
            doAjax(jsonData, "/batch/UI_000100/saveTaskClick", false, true, function (resp) {
                window.scrollTo(0, 0);
                if ('undefined' !== typeof resp && resp !== null) {
                    vm.currentIndex = -1;
                    if (Array.isArray(resp.data)) {
                        vm.tasks = resp.data;
                    }
                    showMessage(resp.messageType, resp.message);
                }
            });
        },
        taskCancel(status) {
            vm.tasks[status].JOBTASK_TASKID = vm.tskId;
            vm.tasks[status].JOBS_CONTINUEONFAIL = vm.detailJobsContinueOnFail;
            vm.tskId = -1;
            vm.currentIndex = -1;
            vm.detailJobsContinueOnFail = -1;
        },
        taskDel(status) {
            // 如果之前有點一筆資料的明細按鈕, 則要先將該筆沒有存儲的資料設定還原
            this.taskRestore();
            var jsonData = {
                jobId: vm.tasks[status].JOBS_JOBID,
            };
            doAjax(jsonData, "/batch/UI_000100/delTaskClick", false, true, function (resp) {
                window.scrollTo(0, 0);
                if ('undefined' !== typeof resp && resp !== null) {
                    if (Array.isArray(resp.data)) {
                        vm.tasks = resp.data;
                    }
                    showMessage(resp.messageType, resp.message);
                }
            });
        },
        taskInsert() {
            // 如果之前有點一筆資料的明細按鈕, 則要先將該筆沒有存儲的資料設定還原
            this.taskRestore();
            if (vm.insertTaskId === -1) {
                showPopover('insertTaskId', '必須輸入資料');
                return;
            }
            var jsonData = {
                sender: vm.tasks.length,
                jobId: "",
                tskId: vm.insertTaskId,
                jobsContinueOnFail: vm.insertJobsContinueOnFail,
            };
            doAjax(jsonData, "/batch/UI_000100/insertTaskClick", false, true, function (resp) {
                window.scrollTo(0, 0);
                if ('undefined' !== typeof resp && resp !== null) {
                    vm.insertTaskId = -1;
                    vm.insertJobsContinueOnFail = 0;
                    if (Array.isArray(resp.data)) {
                        vm.tasks = resp.data;
                    }
                    showMessage(resp.messageType, resp.message);
                }
            });
        },
        changeTaskOrder() {
            // 如果之前有點一筆資料的明細按鈕, 則要先將該筆沒有存儲的資料設定還原
            this.taskRestore();
            if (vm.tasks.length > 1) {
                clearMessage();
                for (let i = 0; i < vm.tasks.length; i++) {
                    if (vm.tasks[i].JOBS_SEQ === "") {
                        showPopover('' + vm.tasks[i].JOBS_JOBID, '必須輸入資料');
                        return;
                    }
                }
                var jobsSeqs = Array.from(vm.tasks, ({JOBS_SEQ}) => JOBS_SEQ);
                var jobsJobIDs = Array.from(vm.tasks, ({JOBS_JOBID}) => JOBS_JOBID);
                var taskIDs = Array.from(vm.tasks, ({TASK_ID}) => TASK_ID);
                var jobsContinueOnFails = Array.from(vm.tasks, ({JOBS_CONTINUEONFAIL}) => JOBS_CONTINUEONFAIL);
                var jsonData = {
                    sender: vm.tasks.length,
                    jobsSeq: jobsSeqs.toString(),
                    jobsJobID: jobsJobIDs.toString(),
                    taskID: taskIDs.toString(),
                    jobsContinueOnFail: jobsContinueOnFails.toString(),
                };
                doAjax(jsonData, "/batch/UI_000100/changeTaskOrder", false, true, function (resp) {
                    window.scrollTo(0, 0);
                    if ('undefined' !== typeof resp) {
                        if (Array.isArray(resp.data)) {
                            vm.tasks = resp.data;
                        }
                        showMessage(resp.messageType, resp.message);
                    }
                });
            } else {
                window.scrollTo(0, 0);
                showDangerMessage("批次Task清單至少有兩筆");
            }
        },
        taskRestore() {
            // 如果之前有點一筆資料的明細按鈕, 則要先將該筆沒有存儲的資料設定還原
            if (vm.currentIndex !== -1 && vm.tskId !== -1) {
                this.taskCancel(vm.currentIndex);
            }
        },
    },
    // mounted: function () {
    //     this.$nextTick(function () {
    //         vm.startTick();
    //     })
    // },
});

$(document).ready(function () {
    vm.startTick();
})

function check(value) {
    if (value === "mw") {
        $('.week').attr("disabled", false);
        $('.day').attr("disabled", true);
    } else {
        $('.week').attr("disabled", true);
        $('.day').attr("disabled", false);
    }
}

function checkItem(item) {
    if (item === 'batchSchedule') {
        vm.items.schedule = !vm.items.schedule;
        if (vm.batch.batchSchedule === 1) {
            $(".batchSchedule").hide();
            vm.batch.batchSchedule = 0;
        } else {
            if (vm.detail === 'update') {
                $(".batchSchedule").show();
            }
            vm.batch.batchSchedule = 1;
        }
    } else if (item === 'batchSingletime') {
        vm.items.singletime = !vm.items.singletime;
        if (vm.batch.batchSingletime === 1) {
            vm.batch.batchSingletime = 0;
        } else {
            vm.batch.batchSingletime = 1;
        }
    } else if (item === 'batchCheckbusinessdate') {
        vm.items.checkbusinessdate = !vm.items.checkbusinessdate;
        if (vm.batch.batchCheckbusinessdate === 1) {
            vm.batch.batchCheckbusinessdate = 0;
        } else {
            vm.batch.batchCheckbusinessdate = 1;
        }
    } else if (item === 'batchDenyconcurrentexec') {
        vm.items.denyconcurrentexec = !vm.items.denyconcurrentexec;
        if (vm.batch.batchDenyconcurrentexec === 1) {
            vm.batch.batchDenyconcurrentexec = 0;
        } else {
            vm.batch.batchDenyconcurrentexec = 1;
        }
    } else if (item === 'batchEnable') {
        vm.items.enable = !vm.items.enable;
        if (vm.batch.batchEnable === 1) {
            vm.batch.batchEnable = 0;
        } else {
            vm.batch.batchEnable = 1;
        }
    } else if (item === 'fepNotifyMail_APD') {
        vm.notify.fepNotifyMail_APD = !vm.notify.fepNotifyMail_APD;
    } else if (item === 'fepNotifyMail_SYS') {
        vm.notify.fepNotifyMail_SYS = !vm.notify.fepNotifyMail_SYS;
    } else if (item === 'fepNotifyMail_Customize') {
        vm.notify.fepNotifyMail_Customize = !vm.notify.fepNotifyMail_Customize;
        $(".batchNotifymail").prop("disabled", !vm.notify.fepNotifyMail_Customize);
    } else if (item === 'fepNotifyPhone_APD') {
        vm.notify.fepNotifyPhone_APD = !vm.notify.fepNotifyPhone_APD;
    } else if (item === 'fepNotifyPhone_SYS') {
        vm.notify.fepNotifyPhone_SYS = !vm.notify.fepNotifyPhone_SYS;
    } else if (item === 'fepNotifyPhone_Customize') {
        vm.notify.fepNotifyPhone_Customize = !vm.notify.fepNotifyPhone_Customize;
        $(".batchNotifyphone").prop("disabled", !vm.notify.fepNotifyPhone_Customize);
    }
}

$("#btnUpdate").bind('click', function () {
    clearMessage();
    if (doValidateForm(batchForm)) {
        var jobid = 0;
        if (vm.tasks.length > 0) {
            jobid = vm.tasks[0].JOBS_JOBID;
        }
        var jsonData = {
            jobId: jobid,
            batch: vm.batch,
            radioType: vm.radioType,
            groupChk: vm.groupChk.toString(),
            mdChk: vm.mdChk.toString(),
            mChk: vm.mChk.toString(),
            wmChk: vm.wmChk.toString(),
            mwChk: vm.mwChk.toString(),
            dateTime: $('#batchDate').val() + " " + $('#batchTime').val(),
            notify: vm.notify,
        };
        doAjax(jsonData, "/batch/UI_000100/saveClick", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                window.scrollTo(0, 0);
                showMessage(resp.messageType, resp.message);
                // if(resp.message === '新增成功'){
                //     vm.startTick();
                // }
                // 2024-04-16 處理成功則返回上一頁
                if (resp.result) {
                    doFormSubmit('/prevPageAjax', resp, false);
                }
            }
        });
    }
});

$("#btnWaiver").bind('click', function () {
    clearMessage();
    vm.startTick();
});

$('.jobsSeq').change(function () {
    for (let i = 0; i < vm.tasks.length; i++) {
        if (vm.tasks[i].JOBS_SEQ === "") {
            showPopover('' + vm.tasks[i].JOBS_JOBID, '必須輸入資料');
            return;
        } else {
            hidePopover('' + vm.tasks[i].JOBS_JOBID);
            break;
        }
    }
})

//按下重設批次狀態按鈕
$('#btnRestBatchStatus').bind('click', function () {
    doAjax({}, '/batch/UI_000100/doRestBatchStatus', false, true, function (resp) {
        if ('undefined' !== typeof resp) {
            showMessage(resp.messageType, resp.message);
        }
    });
});

// 執行主機change事件
function batchExecuteHostNameOnChange(value) {
    vm.batch.batchExecuteHostName = value;
    var disable = stringIsBlank(value);
    $(".batchDenyconcurrentexec").prop("disabled", disable);
    if (disable) {
        vm.items.denyconcurrentexec = false;
        vm.batch.batchDenyconcurrentexec = 0;
    }
}

//按下重設批次狀態按鈕
$('#btnQuartz').bind('click', function () {
    var jsonData = {};
    showCmnConfirmDialog('確定要重建排程資料嗎?', function() {
        doAjax(jsonData, "/batch/UI_000100/makeQuartzForBatch", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showMessage(resp.messageType, resp.message);
            }
        });
    });
});

// 按下每日設定中的Radio
function batchDailyRepetitionTypeOnClick(value) {
    $('.batchScheduleDayinterval').attr("disabled", value !== "D");
    $('.batchScheduleRepetitioninteral').attr("disabled", value !== "T");
    $('.batchScheduleRepetitioninduration').attr("disabled", value !== "T");
}