var zNodes1 = [];
var zNodes2 = [];
var zNodes3 = [];
var zNodes4 = [];
var treeNames = new Array('msgctlTree1','msgctlTree2','msgctlTree3','msgctlTree4')
var zTree1;
var zTree2;
var zTree3;
var zTree4;
var initOpen = true;
var sysstatArray = new Array('sysstatCbs','sysstatFcs','sysstatCredit','sysstatMtp','sysstatTwmp');
var setting = {
    edit: {
        drag: {
            autoExpandTrigger: true,
            // prev: dropPrev,
            // inner: dropInner,
            // next: dropNext,
        },
        enable: false,
        showRemoveBtn: false,
        showRenameBtn: false
    },
    data: {
        simpleData: {
            enable: true
        }
    },
    check: {
      enable:true,
      chkStyle: "checkbox"
    },
    showLine: true,
    callback: {

    }
};

function openNode(level){

    for (let x in treeNames){
        let izTree = $.fn.zTree.getZTreeObj(treeNames[x]);
        let nodes = izTree.transformToArray(izTree.getNodes());
        izTree.expandAll(false);
        for(let i=0;i<nodes.length;i++){
            if (level == 3){
                izTree.expandAll(true);
            }else if (level == 2 ){
                if (nodes[i].treeLevel == 1 ){
                    izTree.expandNode(nodes[i],true,false, true);
                }
            }else if (level == 1 ){
                izTree.expandAll(false);
            }
        }
    }

}

//開啟交易類別
$('#btnLevel1').click(function (){
    openNode(1);
});
//開啟業務分類
$('#btnLevel2').click(function (){
    openNode(2);

});
//開啟交易控制
$('#btnLevel3').click(function (){
    openNode(3);
});

function selectData(){
    let data = {checkList: []};
    doAjax( data, "/atmmon/UI_060291/select", false, true, function(resp) {
        if(resp.treeData!=null){
            initializationData(resp.treeData);
            if (initOpen){
                $('#btnLevel2').trigger('click');
                initOpen = false;
            }
        }
    });
}
$(document).ready(function(){

    selectData();

    $('#btnConfirm').click(function() {
        let data = [];
        for (let x in treeNames){
            let tree = $.fn.zTree.getZTreeObj(treeNames[x]);
            let nodes = tree.getCheckedNodes();
            for (let i in nodes){
                data.push(nodes[i]);
            }
        }

        let channels = [];
        $('input[name=channelData]').each(function (){
            let channel = {
                channelChannelno: this.id,
                channelEnable: (this.checked)?1:0
            }
            channels.push(channel);
        });

        let sysstat = {
            sysstatCbs: $('#sysstatCbs').prop('checked')?1:0,
            sysstatFcs: $('#sysstatFcs').prop('checked')?1:0,
            sysstatCredit: $('#sysstatCredit').prop('checked')?1:0,
            sysstatMtp: $('#sysstatMtp').prop('checked')?1:0,
            sysstatTwmp: $('#sysstatTwmp').prop('checked')?1:0,
			sysstatFido: $('#sysstatFido').prop('checked')?1:0
        }

        let formData = {
            treeData: data,
            channelList: channels,
            sysstat : sysstat
        }
        doAjax(formData, "/atmmon/UI_060291/confirm", false, true, function (resp) {
            console.log(resp);
        });
    });


    $('#channelAll').click(function() {
        $('input[name=channelData]').each(function(){
            this.checked = true;
        })
    });
    $('#channelNone').click(function() {
        $('input[name=channelData]').each(function(){
            this.checked = false;
        })
    });
    $('#sysstatAll').click(function() {
        for (let i in sysstatArray){
            $('#'+sysstatArray[i]).prop('checked', true);
        }
    });
    $('#sysstatNone').click(function() {
        for (let i in sysstatArray){
            $('#'+sysstatArray[i]).prop('checked', false);
        }
    });


});

function initializationData(dataList){
    let data = [];
    for (let i=0;i<dataList.length;i++){
        if ( dataList[i].treeLevel == 1){
            let node = {
                "id":dataList[i].txType1,
                "pId":-1,
                "name":dataList[i].txType1Name,
                "type":dataList[i].type,
                "treeLevel": dataList[i].treeLevel,
                "checked": dataList[i].checked,
                "type1": dataList[i].txType1,
                "open":true
            }
            data.push(node);
        }else if ( dataList[i].treeLevel == 2){
            let node = {
                "id":dataList[i].txType1+"_"+dataList[i].txType2,
                "pId":dataList[i].txType1,
                "resourceUrl":dataList[i].txType1,
                "name":dataList[i].txType2Name,
                "type":dataList[i].txType1,
                "treeLevel": dataList[i].treeLevel,
                "checked": dataList[i].checked,
                "type1": dataList[i].txType1,
                "open":true,
                "dropRoot":false,
            }
            data.push(node);
        }else{
            let node = {
                "id":dataList[i].msgctl[0].msgctlMsgid,
                "pId":dataList[i].txType1+"_"+dataList[i].txType2,
                "resourceUrl":dataList[i].txType2,
                "name":dataList[i].msgctl[0].msgctlMsgid+' '+dataList[i].msgctl[0].msgctlMsgName,
                "type":dataList[i].txType1,
                "treeLevel": dataList[i].treeLevel,
                "checked": dataList[i].checked,
                "type1": dataList[i].txType1,
                "open":false,
                "dropRoot":false,
                "dropInner":false
            }
            data.push(node);
        }
    }


    for (let x in data){
        if (data[x].type1 == 1){
            zNodes1.push(data[x]);
        }else if (data[x].type1 == 2){
            zNodes2.push(data[x]);
        }else if (data[x].type1 == 3){
            zNodes3.push(data[x]);
        }else if (data[x].type1 == 4){
            zNodes4.push(data[x]);
        }
    }

    $.fn.zTree.init($('#'+treeNames[0]), setting, zNodes1);
    zTree1 = $.fn.zTree.getZTreeObj(treeNames[0]);
    zTree1.updateNode();
    $.fn.zTree.init($('#'+treeNames[1]), setting, zNodes2);
    zTree2 = $.fn.zTree.getZTreeObj(treeNames[1]);
    zTree2.updateNode();
    $.fn.zTree.init($('#'+treeNames[2]), setting, zNodes3);
    zTree3 = $.fn.zTree.getZTreeObj(treeNames[2]);
    zTree3.updateNode();
    $.fn.zTree.init($('#'+treeNames[3]), setting, zNodes4);
    zTree4 = $.fn.zTree.getZTreeObj(treeNames[3]);
    zTree4.updateNode();

}