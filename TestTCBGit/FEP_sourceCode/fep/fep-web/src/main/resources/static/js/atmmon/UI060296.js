var zNodes = [];
var treeName = 'msgctlTree';
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
    let izTree = $.fn.zTree.getZTreeObj(treeName);
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

var zTree;
$('#tabId3-tab').click(function (){
    let data = {checkList: []};
    doAjax(data, "/atmmon/UI_060296/select", false, true, function(resp) {
        if(resp.treeData!=null){
            InitializationData(resp.treeData);
        }
    });
})

function InitializationData(dataList){
    let data = [];
    for (let i=0;i<dataList.length;i++){
        if ( dataList[i].treeLevel == 1){
            let node = {
                "id":dataList[i].txType1,
                "pId":-1,
                "name":dataList[i].txType1Name,
                "type":dataList[i].type,
                "treeLevel": dataList[i].treeLevel,
                "chkDisabled":true,
                "checked": dataList[i].checked,
                "open":true
            }
            data.push(node);
        }else if ( dataList[i].treeLevel == 2){
            let node = {
                "id":dataList[i].txType1+"_"+dataList[i].txType2,
                "pId":dataList[i].txType1,
                "resourceUrl":dataList[i].txType1,
                "name":dataList[i].txType2+" "+dataList[i].txType2Name,
                "type":dataList[i].txType1,
                "treeLevel": dataList[i].treeLevel,
                "checked": dataList[i].checked,
                "chkDisabled":true,
                "open":true,
                "dropRoot":false,
            }
            data.push(node);
        }else{
            let node = {
                "id":dataList[i].msgctl[0].msgctlMsgid,
                "pId":dataList[i].txType1+"_"+dataList[i].txType2,
                "resourceUrl":dataList[i].txType2,
                "name":dataList[i].msgctl[0].msgctlMsgid+dataList[i].msgctl[0].msgctlMsgName,
                "type":dataList[i].txType1,
                "treeLevel": dataList[i].treeLevel,
                "checked": dataList[i].checked,
                "chkDisabled":true,
                "open":false,
                "dropRoot":false,
                "dropInner":false
            }
            data.push(node);
        }
    }
    zNodes = data;
    $.fn.zTree.init($('#'+treeName), setting, zNodes);
    zTree = $.fn.zTree.getZTreeObj(treeName);
}