$(document).ready(function () {
    var monitor = new Vue({
        el: '#monitor',
        data: {
            updateTime: "",
            stopNotification: "false",
            enableAutoRestart: "true",
            showPid: "false",
            systemStatusList: [{
                sysHostname: '-',
                sysServiceIP: '-',
                sysCpu: '-',
                sysRam: '-',
                sysUserport: '-',
                sysFEPDB: '',
                sysEMSDB: '',
                sysENCDB: '',
                sysENCLOGDB: '',
                sysFEPHIS: '',
            }],
            ifepTXNList: [{
                source: '',
                count: '',
                tocount: '',
                timeoutcount:'',
                timeouttotalcount:'',
            }],
            serviceStopList: [],
            serviceStartList: [],
            serviceFlag: "true",

            serverNetworkStatusList: [],

            clientNetworkStopList: [],
            clientNetworkStartList: [],
            clientNetworkFlag: "true",

            diskSpaceList: [],

            msMQStopList: [],
            msMQStartList: [],
            msMQFlag: "true",

            ibmMQStartStatusList: [],
            ibmMQStopStatusList: [],
            ibmMqFlag: "true",

            iisHostServiceStatusList: [],
        },
        methods: {
            checkboxStatus: function (obj) {
                if (obj === 'serviceFlag') {
                    if ($("#serviceFlag").checked) {//判斷全選按鈕的狀態是不是選中的
                        $("#serviceFlag").checked = false;
                    } else {
                        $("#serviceFlag").checked = true;
                    }
                    if (monitor.serviceFlag === 'true') {
                        monitor.serviceFlag = 'false';
                    } else {
                        monitor.serviceFlag = 'true';
                    }
                } else if (obj === 'clientNetworkFlag') {
                    if ($("#clientNetworkFlag").checked) {//判斷全選按鈕的狀態是不是選中的
                        $("#clientNetworkFlag").checked = false;
                    } else {
                        $("#clientNetworkFlag").checked = true;
                    }
                    if (monitor.clientNetworkFlag === 'true') {
                        monitor.clientNetworkFlag = 'false';
                    } else {
                        monitor.clientNetworkFlag = 'true';
                    }
                } else if (obj === 'msMQFlag') {
                    if ($("#msMQFlag").checked) {//判斷全選按鈕的狀態是不是選中的
                        $("#msMQFlag").checked = false;
                    } else {
                        $("#msMQFlag").checked = true;
                    }
                    if (monitor.msMQFlag === 'true') {
                        monitor.msMQFlag = 'false';
                    } else {
                        monitor.msMQFlag = 'true';
                    }
                } else if (obj === 'ibmMqFlag') {
                    if ($("#ibmMqFlag").checked) {//判斷全選按鈕的狀態是不是選中的
                        $("#ibmMqFlag").checked = false;
                    } else {
                        $("#ibmMqFlag").checked = true;
                    }
                    if (monitor.ibmMqFlag === 'true') {
                        monitor.ibmMqFlag = 'false';
                    } else {
                        monitor.ibmMqFlag = 'true';
                    }
                }
            },
            doStopNotification: function (val) {
                var jsonData = {
                    stopNotification: val,
                };
                doAjax(jsonData, "/common/UI_080100/doStopNotification", false, true, function (resp) {
                    if ('undefined' !== typeof resp) {
                        showMessage(resp.messageType, resp.message);
                    }
                });
            },
            doEnableAutoRestart: function (val) {
                var jsonData = {
                    enableAutoRestart: val,
                };
                doAjax(jsonData, "/common/UI_080100/doEnableAutoRestart", false, true, function (resp) {
                    if ('undefined' !== typeof resp) {
                        showMessage(resp.messageType, resp.message);
                    }
                });
            },
        },
        mounted: function () {
            this.$nextTick(function () {
                //調用需要執行的方法
                bindGrid("one");
            })
        },
    });
    function bindGrid(obj) {
        doAjax("", "/common/UI_080100/inquiryMain", false, true, function (resp) {
            if ('undefined' !== typeof resp) {
                showMessage(resp.messageType, resp.message);

                var data = resp.data;
                monitor.reNewTime = data.reNewTime;
                if (obj === "one" && resp.result) {
                    startScheduledRefresh();
                }
                monitor.updateTime = new Date().format("yyyy/MM/dd") + " " + checkHour(new Date().getHours()) + " " + new Date().format("hh:mm:ss");
                monitor.stopNotification = data.stopNotification;
                monitor.enableAutoRestart = data.enableAutoRestart;
                monitor.systemStatusList = data.systemStatusList;
                monitor.showPid = data.showPid;
                if (data.serviceStatusList != null) {
                    monitor.serviceStopList = data.serviceStatusList.filter(item => item.serviceState === '未知' || item.serviceState === '停止');
                    monitor.serviceStartList = data.serviceStatusList.filter(item => item.serviceState === '正常');
                } else {
                    monitor.serviceStopList = [];
                    monitor.serviceStartList = [];
                }

                if (data.serverNetworkStatusList != null) {
                    monitor.serverNetworkStatusList = data.serverNetworkStatusList;
                } else {
                    monitor.serverNetworkStatusList = [];
                }

                if (data.clientNetworkStatusList != null) {
                    monitor.clientNetworkStopList = data.clientNetworkStatusList.filter(item => item.state === 'DisConnect' || item.state === 'Unknown');
                    monitor.clientNetworkStartList = data.clientNetworkStatusList.filter(item => item.state === 'Connect');
                } else {
                    monitor.clientNetworkStopList = [];
                    monitor.clientNetworkStartList = [];
                }

                if (data.diskSpaceList != null) {
                    monitor.diskSpaceList = data.diskSpaceList;
                } else {
                    monitor.diskSpaceList = [];
                }

                if (data.msMQStatusList != null) {
                    monitor.msMQStopList = data.msMQStatusList.filter(item => item.useJournal === '否');
                    monitor.msMQStartList = data.msMQStatusList.filter(item => item.useJournal === '是');
                } else {
                    monitor.msMQStopList = [];
                    monitor.msMQStartList = [];
                }

                if (data.ibmMQStatusList != null) {
                    monitor.ibmMQStopStatusList = data.ibmMQStatusList.filter(item => item.status === '未知' || item.status === '停止');
                    monitor.ibmMQStartStatusList = data.ibmMQStatusList.filter(item => item.status === '正常');

                } else {
                    monitor.ibmMQStarStatusList = [];
                    monitor.ibmMQStopStatusList = [];
                }

                if (data.iisHostServiceStatusList != null) {
                    monitor.iisHostServiceStatusList = data.iisHostServiceStatusList;
                } else {
                    monitor.iisHostServiceStatusList = [];
                }
                if (data.ifepTXNList != null) {
                    monitor.ifepTXNList = data.ifepTXNList;
                }
                else{
                    monitor.ifepTXNList =[];
                }
            }
        });
    }

    function startScheduledRefresh() {
        var t1 = window.setInterval(bindGrid, monitor.reNewTime);
    }

    // 按下查詢按鈕
    $('#btnQuery').click(function () {
        bindGrid();
    });

    Date.prototype.format = function (fmt) {
        var o = {
            "M+": this.getMonth() + 1,                 //月份
            "d+": this.getDate(),                    //日
            "h+": this.getHours(),                   //小時
            "m+": this.getMinutes(),                 //分
            "s+": this.getSeconds(),                 //秒
            "q+": Math.floor((this.getMonth() + 3) / 3), //季度
            "S": this.getMilliseconds()             //毫秒
        };
        if (/(y+)/.test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        }
        for (var k in o) {
            if (new RegExp("(" + k + ")").test(fmt)) {
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
            }
        }
        return fmt;
    }

    function checkHour(hour) {
        if (hour > 12) {
            return "下午";
        } else {
            return "上午";
        }
    }
})





