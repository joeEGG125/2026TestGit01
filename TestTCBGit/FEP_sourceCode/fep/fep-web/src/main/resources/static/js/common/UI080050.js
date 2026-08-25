var treeMainSetting = {
    data: {
        simpleData: {
            enable: true
        }
    },
    callback: {
        beforeCheck:true,
        onClick: onClick,

    }
};

var treeViceSetting = {
    check: {
        enable: true,
        autoCheckTrigger: true,
        chkboxType:{"Y":"ps","N":"ps"}
    },
    data: {
        simpleData: {
            enable: true
        }
    }
};

var treeMainNodes =[
    { id:"root", pId:-1, name:"root", open:true},
    { id:1, pId:"root", name:"父節點1","nodeList":[11,21,31]},
    { id:2, pId:"root", name:"父節點2","nodeList":[12,22,32]},
    { id:3, pId:"root", name:"父節點3","nodeList":[221,222]}
];

var treeViceNodes =[
    { id:"root", pId:-1, name:"root", open:true},
    { id:1, pId:"root", name:"隨意勾選 1", open:true, checked:false},
    { id:11, pId:1, name:"隨意勾選 1-1-1"},
    { id:12, pId:1, name:"隨意勾選 1-1-2"},
    { id:2, pId:"root", name:"隨意勾選 1-2", open:true},
    { id:21, pId:2, name:"隨意勾選 1-2-1"},
    { id:22, pId:2, name:"隨意勾選 1-2-2"},
    { id:3, pId:"root", name:"隨意勾選 2", open:true},
    { id:31, pId:3, name:"隨意勾選 2-1"},
    { id:32, pId:3, name:"隨意勾選 2-2"},
    { id:33, pId:3, name:"隨意勾選 2-2-1"},
    { id:34, pId:3, name:"隨意勾選 2-2-2"},
];

$(document).ready(function(){
    doAjax("", "/common/UI_080050/select", false, true, function(resp) {
        //console.log(resp);
        InitializationData(resp.roleInfoList,resp.groupInfoList);
        initialExpansion();
    });

});

function onClick(event, treeId, treeNode) {
    var vice = $.fn.zTree.getZTreeObj("treeVice");
    vice.checkAllNodes(false);
    if('undefined' !== typeof (treeNode.resourceList)){
        for (var i = 0; i < treeNode.resourceList.length; i++) {
            var objNode = vice.getNodeByParam("id",treeNode.resourceList[i]);
            if(objNode !== null){
                objNode.checked = true;
                vice.updateNode(objNode);

                objNode.getParentNode().checked = true;
                vice.updateNode(objNode.getParentNode());
            }
        }
    }
}


$('#updateOrder').click(function () {
    var main = $.fn.zTree.getZTreeObj("treeMain");
    var mainNode = main.getSelectedNodes();
    var vice = $.fn.zTree.getZTreeObj("treeVice");
    var checkedNodes = vice.getCheckedNodes(true);
    var jsonData = {
        roleId: mainNode[0].id,
        groupList: null,
        resourceList: null
    };
    var groups = [];
    var resources = [];
    for (var i = 0; i < checkedNodes.length; i++) {
        if(checkedNodes[i].type === 'G'){
            groups.push(checkedNodes[i].id);
        }else if(checkedNodes[i].type === 'R'){
            resources.push(checkedNodes[i].id);
        }
    }


    jsonData.groupList = groups;
    jsonData.resourceList = resources;
    doAjax(jsonData, "/common/UI_080050/updateRole", false, true, function(resp) {
        if("SUCCESS" === resp.messageType){
        }
        showWarningCmnAlert(resp.message);
    });
    doAjax("", "/common/UI_080050/select", false, true, function(resp) {
        InitializationData(resp.roleInfoList,resp.groupInfoList);
    });
})

function InitializationData(roleList, groupList){
    //角色
    var role = [{
        id:"root",
        pId:-1,
        name:"root",
        open:true,
        // "iconOpen":ctx+"/images/tree/root_open.png",
        // "iconClose":ctx+"/images/tree/root_close.png",
    }];
    for(var i = 0; i<roleList.length; i++){
        var node = {
            "id":roleList[i].roleid,
            "pId":"root",
            "name":roleList[i].rolename,
            "no":roleList[i].roleno,
            "resourceList":roleList[i].resourceids,
            "icon":ctx+"/images/tree/group.png"
            };
        role.push(node);
    }
    $.fn.zTree.init($("#treeMain"), treeMainSetting, role);

    //群組/功能
    var group = [];
    for(var i = 0; i<groupList.length; i++){
        if(groupList[i].type === "T"){
            var node = {
                "id":groupList[i].id,
                "pId":groupList[i].pid,
                "name":groupList[i].name,
                // "iconOpen":ctx+"/images/tree/root_open.png",
                // "iconClose":ctx+"/images/tree/root_close.png",
                "type":groupList[i].type,
                "open":true};
            group.push(node);
        }else if(groupList[i].type === "G"){
            var node = {
                "id":groupList[i].id,
                "pId":groupList[i].pid,
                "name":groupList[i].name,
                "no":groupList[i].no,
                // "icon":ctx+"/images/tree/1.png",
                "type":groupList[i].type,
                "open":false};
            group.push(node);
        }else{
            var node = {
                "id":groupList[i].id,
                "pId":groupList[i].pid,
                "name":groupList[i].name,
                "no":groupList[i].no,
                "type":groupList[i].type,
                // "icon":ctx+"/images/tree/2.png"
                };
            group.push(node);
        }
    }
    $.fn.zTree.init($("#treeVice "), treeViceSetting, group);

}

function initialExpansion(){
    var treeVice = $.fn.zTree.getZTreeObj("treeVice");
    treeVice.expandAll(true);
    var nodes = treeVice.transformToArray(treeVice.getNodes());
    for(var i=0;i<nodes.length;i++){
        if(nodes[i].level == 0){
            treeVice.expandNode(nodes[i],true,false,false)
        }else{
            treeVice.expandNode(nodes[i],false,true,false)
        }
    }
}
