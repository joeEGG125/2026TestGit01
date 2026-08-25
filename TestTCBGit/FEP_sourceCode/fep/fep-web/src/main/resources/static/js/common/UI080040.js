var treeMainSetting = {
    edit: {
        drag: {
            autoExpandTrigger: true,
            prev: dropPrev,
            inner: dropInner,
            next: dropNext,
        },
        enable: true,
        showRemoveBtn: false,
        showRenameBtn: false
    },
    data: {
        simpleData: {
            enable: true
        }
    },
    showLine: true,
    callback: {
        onClick: onClick,
        onRightClick: OnRightClick, //合庫不需要此功能先點掉
        beforeDrag: beforeDrag,
        beforeDrop: beforeDrop,
        beforeDragOpen: beforeDragOpen,

    }
};

var treeMainNodes =[
    { id:"root", pId:-1, name:"root", open:true},
    { id:1, pId:"root", name:"父節點1","nodeList":[11,21,31]},
    { id:2, pId:"root", name:"父節點2","nodeList":[12,22,32]},
    { id:3, pId:"root", name:"父節點3","nodeList":[221,222]}
];

var roleForm = "roleForm";

var userForm = "userForm";

$('#' + roleForm).validate(getValidFormOptinal({
    rules: {
        roleNo: {
            required: true,
        },
        roleName: {
            required: true,
        },
    },
    messages:{
        roleNo:{
            required: "必須有資料",
        },
        roleName:{
            required: "必須有資料",
        },
    }
}));

$('#' + userForm).validate(getValidFormOptinal({
    rules: {
        userNo: {
            required: true,
        },
        userName: {
            required: true,
        },
    },
    messages:{
        userNo:{
            required: "必須有資料",
        },
        userName:{
            required: "必須有資料",
        },
    }
}));

$("#fullCollection").click(function (){var izTree = $.fn.zTree.getZTreeObj("treeMain");
    var nodes = izTree.transformToArray(izTree.getNodes());
      for(var i=0;i<nodes.length;i++){
          if(nodes[i].level == 0){
              //根節點展開
              izTree.expandNode(nodes[i],true,false,false)
          }else{
              izTree.expandNode(nodes[i],false,true,false)
          }
      }
  });

$("#expandAll").click(function (){
    var izTree = $.fn.zTree.getZTreeObj("treeMain");
    izTree.expandAll(true);
});

//角色確認
$("#roleConfirmClick").click(function () {
    var nodes = zTree.getSelectedNodes();
    if(doValidateForm(roleForm)){
        if($('#roleDeleteClick').attr('disabled') === 'disabled'){
            var jsonData = {
                pid:nodes[0].id,
                no: $("#roleNo").val(),
                name:$("#roleName").val(),
                effectdate:$("#roleStartDate").val(),
                expireddate:$("#roleEndDate").val()
            };
            doAjax(jsonData, "/common/UI_080040/insert", false, true, function(resp) {
                if("SUCCESS" === resp.messageType){
                    InitializationData(resp.dataList);
                    showWarningCmnAlert(resp.message);
                    $("#role").hide();
                }else{
                    showWarningCmnAlert(resp.message);
                }
            });
        } else {
            var jsonData = {
                pid:nodes[0].pId,
                id:nodes[0].id,
                no: $("#roleNo").val(),
                name:$("#roleName").val(),
                effectdate:$("#roleStartDate").val(),
                expireddate:$("#roleEndDate").val()
            };
            doAjax(jsonData, "/common/UI_080040/updateRole", false, true, function(resp) {
                if("SUCCESS" === resp.messageType){
                    if(resp.dataList!=null){
                        InitializationData(resp.dataList);
                        nodes[0].no = $("#roleNo").val();
                        nodes[0].name = $("#roleName").val()+'('+$("#roleNo").val()+')';
                        nodes[0].rolename = $("#roleName").val();
                        nodes[0].groupStartDate = $("#roleStartDate").val();
                        nodes[0].groupEndDate = $("#roleEndDate").val();
                        zTree.updateNode(nodes[0]);

                        showWarningCmnAlert(resp.message);

                        $("#role").hide();
                    }
                }else{
                    showWarningCmnAlert(resp.message);
                }
            });
        }
    }
});

//使用者確認
$("#userConfirmClick").click(function () {
    var nodes = zTree.getSelectedNodes();
    if(doValidateForm(userForm)){
        var jsonData = {
            pid:nodes[0].pId,
            id:nodes[0].id,
            type:nodes[0].type,
            no: $("#userNo").val(),
            name:$("#userName").val(),
            empid:$("#empId").val(),
            usermail:$("#userEmail").val(),
            effectdate:$("#userStartDate").val(),
            expireddate:$("#userEndDate").val()
        };
        doAjax(jsonData, "/common/UI_080040/updateUser", false, true, function(resp) {
            if("SUCCESS" === resp.messageType){
                InitializationData(resp.dataList);
                showWarningCmnAlert(resp.message);
                $("#user").hide();
            }else{
                showWarningCmnAlert(resp.message);
            }
        });
    }
});

$(document).ready(function(){
    $("#user").hide();
    $("#role").hide();
    doAjax("", "/common/UI_080040/select", false, true, function(resp) {
        var data = [];
        if(resp.dataList!=null){
            InitializationData(resp.dataList);
        }
    });

});

function InitializationData(dataList){
    var data = [];
    for(var i = 0; i<dataList.length; i++){
        if(dataList[i].type === "T"){
            var node = {
                "id":dataList[i].id,
                "pId":dataList[i].pid,
                "name":dataList[i].name,
                "type":dataList[i].type,
                "open":true,
                // "iconOpen":ctx+"/images/tree/root_open.png",
                // "iconClose":ctx+"/images/tree/root_close.png",
            };
            data.push(node);
        }
        else if(dataList[i].type === "R"){
            var node = {
                "id":dataList[i].id,
                "pId":dataList[i].pid,
                "name":dataList[i].name+'('+dataList[i].no+')',
                "rolename":dataList[i].name,
                "no":dataList[i].no,
                "roleStartDate":dataList[i].effectdate,
                "roleEndDate":dataList[i].expireddate,
                "icon":ctx+"/images/tree/group.png",
                "type":dataList[i].type,
                "open":false,
                "dropRoot":false
            };
            data.push(node);
        }
        else{
            var node = {
                "id":dataList[i].id,
                "pId":dataList[i].pid,
                "name":dataList[i].name+'('+dataList[i].no+')',
                "username":dataList[i].name,
                "no":dataList[i].no,
                "userStartDate":dataList[i].effectdate,
                "userEndDate":dataList[i].expireddate,
                "userEmail":dataList[i].emailaddress,
                "empId":dataList[i].empid,
                "icon":ctx+"/images/tree/single.png",
                "type":dataList[i].type,
                "dropRoot":false,
                "dropInner":false
            };
            data.push(node);
        }
    }
    zNodes  = data;
    $.fn.zTree.init($("#treeMain"), treeMainSetting, zNodes);
    zTree = $.fn.zTree.getZTreeObj("treeMain");
    rMenu = $("#rMenu");
}

function onClick(event, treeId, treeNode) {

    if(treeNode.type === "T"){
        $("#user").hide();
        $("#role").hide();
    }else if(treeNode.type === "R"){
        $("#user").hide();
        $("#roleNo").val(treeNode.no);
        $("#roleName").val(treeNode.rolename);
        $("#roleStartDate").val(treeNode.roleStartDate);
        $("#roleEndDate").val(treeNode.roleEndDate);
        $('#roleDeleteClick').attr('disabled',false);
        initDatePicker('roleStartDate');
        initDatePicker('roleEndDate');
        $("#role").show();
        $('#roleID').html('目前角色:'+treeNode.name);
    }else{
        $("#role").hide();
        $("#userNo").val(treeNode.no);
        $("#userName").val(treeNode.username);
        $("#empId").val(treeNode.empId);
        $("#userEmail").val(treeNode.userEmail);
        $("#userStartDate").val(treeNode.userStartDate);
        $("#userEndDate").val(treeNode.userEndDate);
        initDatePicker('userStartDate');
        initDatePicker('userEndDate');
        $("#user").show();
        $('#userID').html('目前使用者:'+treeNode.name);
    }

}

function dropPrev(treeId, nodes, targetNode) {
    var pNode = targetNode.getParentNode();
    if (pNode && pNode.dropInner === false) {
        return false;
    } else {
        for (var i=0,l=curDragNodes.length; i<l; i++) {
            var curPNode = curDragNodes[i].getParentNode();
            if (curPNode && curPNode !== targetNode.getParentNode() && curPNode.childOuter === false) {
                return false;
            }
        }
    }
    return true;
}

function dropNext(treeId, nodes, targetNode) {
    var pNode = targetNode.getParentNode();
    if (pNode && pNode.dropInner === false) {
        return false;
    } else {
        for (var i=0,l=curDragNodes.length; i<l; i++) {
            var curPNode = curDragNodes[i].getParentNode();
            if (curPNode && curPNode !== targetNode.getParentNode() && curPNode.childOuter === false) {
                return false;
            }
        }
    }
    return true;
}

function beforeDrag(treeId, treeNodes) {
    className = (className === "dark" ? "":"dark");
    for (var i=0,l=treeNodes.length; i<l; i++) {
        if (treeNodes[i].drag === false) {
            curDragNodes = null;
            return false;
        } else if (treeNodes[i].parentTId && treeNodes[i].getParentNode().childDrag === false) {
            curDragNodes = null;
            return false;
        }
    }
    curDragNodes = treeNodes;
    return true;
}

function beforeDrop(treeId, treeNodes, targetNode, moveType, isCopy) {
    var moveNode = treeNodes[0];

    if(moveNode.pId == 0 && targetNode.pId > 0  && (moveType === "next" || moveType === "prev")){
        showWarningCmnAlert("本系統功能群組下不能包含其他功能群組, 請重新操作!");
        return false
    }
    return !(targetNode == null || (moveType != "inner" && !targetNode.parentTId));
}

function dropInner(treeId, nodes, targetNode) {
    if (targetNode && targetNode.dropInner === false) {
        return false;
    } else {
        for (var i=0,l=curDragNodes.length; i<l; i++) {
            if (!targetNode && curDragNodes[i].dropRoot === false) {
                return false;
            } else if (curDragNodes[i].parentTId && curDragNodes[i].getParentNode() !== targetNode && curDragNodes[i].getParentNode().childOuter === false) {
                return false;
            }
        }
    }
    return true;
}

function OnRightClick(event, treeId, treeNode) {
    if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {

    } else if (treeNode && !treeNode.noR) {
        zTree.selectNode(treeNode);
        showRMenu(treeNode.id, treeNode.pId, treeNode.type, event.clientX, event.clientY);
    }
}

function showRMenu(id, pId, type, x, y) {
    $("#rMenu ul").show();
     if(type === 'T'){
         //新增角色
         $("#m_add").show();
         //選取/取消選取功能
         $("#m_unCheck").hide();
     }else{
         if(type === 'R' ){
             //新增角色
             $("#m_add").hide();
             //選取/取消選取功能
             $("#m_unCheck").show();
         }else{
             //新增角色
             $("#m_add").hide();
             //選取/取消選取功能
             $("#m_unCheck").hide();
         }
     }
     y += document.body.scrollTop;
     x += document.body.scrollLeft;
     rMenu.css({"top":y+"px", "left":x+"px", "visibility":"visible"});

     $("body").bind("mousedown", onBodyMouseDown);
 }

function hideRMenu() {
    if (rMenu) rMenu.css({"visibility": "hidden"});
    $("body").unbind("mousedown", onBodyMouseDown);
}

function onBodyMouseDown(event){
    if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length>0)) {
        rMenu.css({"visibility" : "hidden"});
    }
}

var log, className = "dark", curDragNodes, autoExpandNode;
function beforeDragOpen(treeId, treeNode) {
    autoExpandNode = treeNode;
    return true;
}

//新增角色
function addRole() {
    hideRMenu();
    $('#roleDeleteClick').attr('disabled',true);
    $("#user").hide();
    $("#roleNo").val("");
    $("#roleName").val("");
    // $("#roleStartDate").val(new Date().getFullYear() + "-" + (new Date().getMonth() + 1) + "-" + new Date().getDate());
    // $("#roleEndDate").val("2039-12-31");
    $("#roleStartDate").val($("#roleStartDate").attr('data-defaultEffectDate'));
    $("#roleEndDate").val($("#roleEndDate").attr('data-defaultExpiredDate'));
    initDatePicker('roleStartDate');
    initDatePicker('roleEndDate');
    $("#role").show();
    $('#roleID').html('新增角色');
}

//刪除
function deleteClick(){
    var nodes = zTree.getSelectedNodes();
    if (nodes && nodes.length>0) {
        var jsonData = {
            pid:nodes[0].pId,
            id:nodes[0].id
        };
        showCmnConfirmDialog('確定要刪除所選的資料嗎?', function() {
            doAjax(jsonData, "/common/UI_080040/deleteRole", false, true, function(resp) {
                if("SUCCESS" === resp.messageType){
                    if(resp.dataList!=null){
                        InitializationData(resp.dataList);
                    }
                    $("#role").hide();
                    $("#user").hide();
                    showWarningCmnAlert(resp.message);
                }else{
                    showWarningCmnAlert(resp.message);
                }
            });
        }, function() {

        });
    }
}

function checkTreeNode(){
    $('#pNo').val('');
    $('#name').val('');
    $('#startDate').val('');
    $('#endDate').val('');
    vm.checked = false;
    var nodes = zTree.getSelectedNodes();
    vm.title = {id: nodes[0].id, name:nodes[0].name}
    doAjax({id: nodes[0].id}, "/common/UI_080040/checkTreeNode", false, true, function(resp) {
        vm.list2 = resp.allList;
        vm.list1 = resp.allList;
        vm.checkModel = [];
        for(var i = 0; i<resp.selectList.length; i++){
            vm.checkModel.push(resp.selectList[i].childid);
        }

        if(vm.checkModel.length==vm.list2.length){
            vm.checked=true;
        }
    });
    showConfirmDialog('myConfirm', "", function() {
        var jsonData = {
            pid: nodes[0].id,
            ids: vm.checkModel
        };
        doAjax(jsonData, "/common/UI_080040/queryClick", false, true, function(resp) {
            if("SUCCESS" === resp.messageType){
                InitializationData(resp.dataList);
                showWarningCmnAlert(resp.message);
            }else{
                showWarningCmnAlert(resp.message);
            }
        });
    }, function() {

    });
}