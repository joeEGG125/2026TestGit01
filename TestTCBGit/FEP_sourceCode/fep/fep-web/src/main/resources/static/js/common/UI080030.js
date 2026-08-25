    var groupForm = "groupForm";
    var resourceForm = "resourceForm";

    var zNodes = [
        { "id":"root", "pId":-1, "name":"root",open:true},
        { "id":"parent1","pNo":"01", "pId":"root", "name":"展開、折疊 自定義圖標不同","groupStartDate":"2021-12-17","groupEndDate":"2021-12-17",open:false,dropRoot:false},
        { "id":"11","pNo":"011", "pId":"parent1", "name":"葉子節點1","resourceUrl":"parent1","resourceStartDate":"2021-12-14","resourceEndDate":"2021-12-14",dropRoot:false,dropInner:false},
        { "id":"12","pNo":"012", "pId":"parent1", "name":"葉子節點2","resourceUrl":"parent1","resourceStartDate":"2021-12-14","resourceEndDate":"2021-12-14",dropRoot:false,dropInner:false},
        { "id":"13","pNo":"013", "pId":"parent1", "name":"葉子節點3","resourceUrl":"parent1","resourceStartDate":"2021-12-14","resourceEndDate":"2021-12-14",dropRoot:false,dropInner:false,},

        { "id":"parent2","pNo":"02", "pId":"root", "name":"展開、折疊 自定義圖標相同","groupStartDate":"2021-12-14","groupEndDate":"2021-12-14",open:false,dropRoot:false},
        { "id":"21","pNo":"021", "pId":"parent2", "name":"葉子節點1","resourceUrl":"parent2","resourceStartDate":"2021-12-14","resourceEndDate":"2021-12-14",dropRoot:false,dropInner:false},
        { "id":"22","pNo":"022", "pId":"parent2", "name":"葉子節點2","resourceUrl":"parent2","resourceStartDate":"2021-12-14","resourceEndDate":"2021-12-14",dropRoot:false,dropInner:false},
        { "id":"23","pNo":"023", "pId":"parent2", "name":"葉子節點3","resourceUrl":"parent2","resourceStartDate":"2021-12-14","resourceEndDate":"2021-12-14",dropRoot:false,dropInner:false},
        { "id":"parent3","pNO":"03", "pId":"root", "name":"不使用自定義圖標","groupStartDate":"2021-12-12","groupEndDate":"2021-12-12",open:false,dropRoot:false},
        { "id":"31","pNo":"031", "pId":"parent3", "name":"葉子節點1","resourceUrl":"parent3","resourceStartDate":"2021-12-14","resourceEndDate":"2021-12-14",dropRoot:false,dropInner:false},
        { "id":"32","pNo":"032", "pId":"parent3", "name":"葉子節點2","resourceUrl":"parent3","resourceStartDate":"2021-12-14","resourceEndDate":"2021-12-14",dropRoot:false,dropInner:false},
        { "id":"33","pNo":"033", "pId":"parent3", "name":"葉子節點3","resourceUrl":"parent3","resourceStartDate":"2021-12-14","resourceEndDate":"2021-12-14",dropRoot:false,dropInner:false}
    ];

    $('#' + groupForm).validate(getValidFormOptinal({
        rules: {
            groupNo: {
                required: true,
            },
            groupName: {
                required: true,
            },
        },
        messages:{
            groupNo:{
                required: "必須有資料",
            },
            groupName:{
                required: "必須有資料",
            },
        }
    }));

    $('#' + resourceForm).validate(getValidFormOptinal({
    rules: {
        resourceNo: {
            required: true,
        },
        resourceName: {
            required: true,
        },
    },
    messages:{
        resourceNo:{
            required: "必須有資料",
        },
        resourceName:{
            required: "必須有資料",
        },
    }
}));

    var setting = {
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
            onRightClick: OnRightClick,
            beforeDrag: beforeDrag,
            beforeDrop: beforeDrop,
            beforeDragOpen: beforeDragOpen,

        }
    };

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
//        else if(moveNode.pId == 0 && targetNode.pId == 0  && moveType === "inner"){
//            showWarningCmnAlert("本系統功能群組下不能包含其他功能群組, 請重新操作!");
//            return false
//        }
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
            //showRMenu2(treeNode.id, treeNode.pId, event.clientX, event.clientY);
        }
    }

    function showRMenu(id, pId, type, x, y) {
        $("#rMenu ul").show();
        if(type === 'T'){
            //新增功能
            $("#m_check").hide();
            //新增群組
            $("#m_add").show();
            //新增功能
            $("#m_unCheck").hide();
        }else{
            if(type === 'G' ){
                //新增群組
                $("#m_add").hide();
                //新增功能
                $("#m_check").show();
                //新增功能
                $("#m_unCheck").show();
            }else{
                //新增群組
                $("#m_add").hide();
                //新增功能
                $("#m_check").hide();
                //新增功能
                $("#m_unCheck").hide();
            }
        }
        y += document.body.scrollTop;
        x += document.body.scrollLeft;
        rMenu.css({"top":y+"px", "left":x+"px", "visibility":"visible"});

        $("body").bind("mousedown", onBodyMouseDown);
    }

    function showRMenu2(type, pId, x, y) {
        $("#rMenu ul").show();
        if(type === 0){
            //新增功能
            $("#m_check").hide();
            //新增群組
            $("#m_add").show();
        }else{
            if(pId === 0 ){
                //新增群組
                $("#m_add").hide();
                //新增功能
                $("#m_check").show();
            }else{
                //新增群組
                $("#m_add").hide();
                //新增功能
                $("#m_check").hide();
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

    //增加父節點
    function addGroupNode() {
        hideRMenu();
        $('#groupDeleteClick').attr('disabled',true)
        $("#resource").hide();
        $("#groupNo").val("");
        $("#groupName").val("");
        // $("#groupStartDate").val(new Date().getFullYear() + "-" + (new Date().getMonth() + 1) + "-" + new Date().getDate());
        // $("#groupEndDate").val("2039-12-31");
        $("#groupStartDate").val($("#groupStartDate").attr('data-defaultEffectDate'));
        $("#groupEndDate").val($("#groupEndDate").attr('data-defaultExpiredDate'));
        initDatePicker('groupStartDate');
        initDatePicker('groupEndDate');
        $("#group").show();
    }

    //增加子節點
    function addResourceNode(checked) {
        hideRMenu();
        $('#resourceDeleteClick').attr('disabled',true)
        $("#group").hide();
        $("#resourceNo").val("");
        $("#resourceName").val("");
        $("#resourceUrl").val("");
        // $("#resourceStartDate").val(new Date().getFullYear() + "-" + (new Date().getMonth() + 1) + "-" + new Date().getDate());
        // $("#resourceEndDate").val("2039-12-31");
        $("#resourceStartDate").val($("#resourceStartDate").attr('data-defaultEffectDate'));
        $("#resourceEndDate").val($("#resourceEndDate").attr('data-defaultExpiredDate'));
        initDatePicker('resourceStartDate');
        initDatePicker('resourceEndDate');
        $("#resource").show();
    }

    //群組確認
    $("#groupConfirmClick").click(function () {
        var nodes = zTree.getSelectedNodes();
        if(doValidateForm(groupForm)){
            if($('#groupDeleteClick').attr('disabled') === 'disabled'){
                var jsonData = {
                    pid:nodes[0].id,
                    no: $("#groupNo").val(),
                    name:$("#groupName").val(),
                    effectdate:$("#groupStartDate").val(),
                    expireddate:$("#groupEndDate").val(),
                    type:'G'
                };
                doAjax(jsonData, "/common/UI_080030/insert", false, true, function(resp) {
                    if("SUCCESS" === resp.messageType){
                        InitializationData(resp.dataList);
                        showWarningCmnAlert(resp.message);
                        $("#group").hide();
                    }else{
                        showWarningCmnAlert(resp.message);
                    }
                });
            } else {
                var jsonData = {
                    pid:nodes[0].pId,
                    id:nodes[0].id,
                    type:nodes[0].type,
                    no: $("#groupNo").val(),
                    name:$("#groupName").val(),
                    effectdate:$("#groupStartDate").val(),
                    expireddate:$("#groupEndDate").val()
                };
                doAjax(jsonData, "/common/UI_080030/update", false, true, function(resp) {
                    if("SUCCESS" === resp.messageType){
                        if(resp.dataList!=null){
                            InitializationData(resp.dataList);
                            nodes[0].no = $("#groupNo").val();
                            nodes[0].name = $("#groupName").val();
                            nodes[0].groupStartDate = $("#groupStartDate").val();
                            nodes[0].groupEndDate = $("#groupEndDate").val();
                            zTree.updateNode(nodes[0]);

                            showWarningCmnAlert(resp.message);

                            $("#group").hide();
                        }
                    }else{
                        showWarningCmnAlert(resp.message);
                    }
                });
            }
        }

    })

    //功能確認
    $("#resourceConfirmClick").click(function () {
        var nodes = zTree.getSelectedNodes();
        if(doValidateForm(resourceForm)){

            if($('#resourceDeleteClick').attr('disabled') === 'disabled'){
                var jsonData = {
                    pid:nodes[0].pId,
                    id:nodes[0].id,
                    type:nodes[0].type,
                    no: $("#resourceNo").val(),
                    name:$("#resourceName").val(),
                    resourceUrl:$("#resourceUrl").val(),
                    effectdate:$("#resourceStartDate").val(),
                    expireddate:$("#resourceEndDate").val(),
                    type:'R'
                };
                doAjax(jsonData, "/common/UI_080030/insert", false, true, function(resp) {
                    if("SUCCESS" === resp.messageType){
                        InitializationData(resp.dataList);
                        showWarningCmnAlert(resp.message);
                         $("#resource").hide();
                    }else{
                        showWarningCmnAlert(resp.message);
                    }
                });

            } else {
                var jsonData = {
                    pid:nodes[0].pId,
                    id:nodes[0].id,
                    type:nodes[0].type,
                    no: $("#resourceNo").val(),
                    name:$("#resourceName").val(),
                    resourceUrl:$("#resourceUrl").val(),
                    effectdate:$("#resourceStartDate").val(),
                    expireddate:$("#resourceEndDate").val()
                };
                doAjax(jsonData, "/common/UI_080030/update", false, true, function(resp) {
                    if("SUCCESS" === resp.messageType){
                        InitializationData(resp.dataList);
                        showWarningCmnAlert(resp.message);
                        $("#resource").hide();
                    }else{
                        showWarningCmnAlert(resp.message);
                    }
                });
            }
        }
    })

    var groupmembers =[];
    var groups =[];
    //更新順序
    $('#updateOrder').click(function (){
        var treeobj = $.fn.zTree.getZTreeObj("treeDemo");
        var node = treeobj.getNodes();
        var nodes = treeobj.transformToArray(node);
        groupmembers =[];
        groups =[];

        getTreeData(node[0]);

//        if('undefined' !== typeof node[0].children){
//            for (var i=0; i<node[0].children.length; i++) {
//                var groupNode = node[0].children[i];
//                var group = {
//                    groupid:groupNode.id,
//                    type:groupNode.type,
//                    locationno:i+1
//                }
//                groups.push(group);
//                if('undefined' !== typeof groupNode.children){
//                    for (var j=0; j<groupNode.children.length; j++) {
//                        var resourceNode = groupNode.children[j];
//                        var groupmember = {
//                            groupid:groupNode.id,
//                            childid:resourceNode.id,
//                            type:groupNode.type,
//                            locationno:j+1
//                        };
//                        groupmembers.push(groupmember);
//                    }
//                }
//            }
//        }
//        console.log(groups);
//        console.log(groupmembers);
        var jsonData = {
            groupList:groups,
            groupmembersList:groupmembers
        };
        doAjax(jsonData, "/common/UI_080030/updateOrder", false, true, function(resp) {
            if("SUCCESS" === resp.messageType){
                InitializationData(resp.dataList);
            }
            showWarningCmnAlert(resp.message);
        });

    })

    function getTreeData(nodeData){
        if('undefined' !== typeof nodeData.children){
            for (var i=0; i<nodeData.children.length; i++) {
                var childNode = nodeData.children[i];
                if(nodeData.type === 'T' && childNode.type === 'G'){
                    var group = {
                        groupid:childNode.id,
                        locationno:i+1
                    }
                    groups.push(group);
                } else if(nodeData.type === 'G' && childNode.type === 'R') {
                    var groupmember = {
                        groupid:nodeData.id,
                        childid:childNode.id,
                        childtype:childNode.type,
                        locationno:i+1
                    }
                    groupmembers.push(groupmember);
                } else if(nodeData.type === 'G' && childNode.type === 'G') {
                    var group = {
                        groupid:childNode.id,
                        locationno:i+1
                    }
                    groups.push(group);
                    var groupmember = {
                        groupid:nodeData.id,
                        childid:childNode.id,
                        childtype:childNode.type,
                        locationno:i+1
                    }
                    groupmembers.push(groupmember);
                }
                getTreeData(childNode);
            }
        }
    }

    $("#fullCollection").click(function (){var izTree = $.fn.zTree.getZTreeObj("treeDemo");
        var nodes = izTree.transformToArray(izTree.getNodes());
        for(var i=0;i<nodes.length;i++){
            if(nodes[i].level == 0){
                //根節點展開
                izTree.expandNode(nodes[i],true,false,false)
            }else{
                izTree.expandNode(nodes[i],false,true,false)
            }
        }
    })

    $("#expandAll").click(function (){
        var izTree = $.fn.zTree.getZTreeObj("treeDemo");
        izTree.expandAll(true);
    })

    var zTree, rMenu;
    $(document).ready(function(){
        $("#group").hide();
        $("#resource").hide();
        doAjax("", "/common/UI_080030/select", false, true, function(resp) {
            var data = [];
            if(resp.dataList!=null){
                InitializationData(resp.dataList);
            }
        });

    });

    //刪除
    function deleteClick(){
        var nodes = zTree.getSelectedNodes();

        if (nodes && nodes.length>0) {
            var jsonData = {
                pid:nodes[0].pId,
                id:nodes[0].id
            };
            showCmnConfirmDialog('確定要刪除所選的資料嗎?', function() {
                doAjax(jsonData, "/common/UI_080030/delete", false, true, function(resp) {
                    if("SUCCESS" === resp.messageType){
                        if(resp.dataList!=null){
                            InitializationData(resp.dataList);
                        }
                        $("#group").hide();
                        $("#resource").hide();
                        showWarningCmnAlert(resp.message);
                    }else{
                        showWarningCmnAlert(resp.message);
                    }
                });
            }, function() {

            });
        }
    }

    function onClick(event, treeId, treeNode, clickFlag) {
        $('#resourceDeleteClick').attr('disabled',false)
        $('#groupDeleteClick').attr('disabled',false)
        if(treeNode.name === "root"){
            $("#group").hide();
            $("#resource").hide();
        }else if(treeNode.pId === 0){
            $("#resource").hide();
            $("#groupNo").val(treeNode.no);
            $("#groupName").val(treeNode.name);
            $("#groupStartDate").val(treeNode.groupStartDate);
            $("#groupEndDate").val(treeNode.groupEndDate);
            initDatePicker('groupStartDate');
            initDatePicker('groupEndDate');
            $("#group").show();
        }else{
            $("#group").hide();
            $("#resourceNo").val(treeNode.no);
            $("#resourceName").val(treeNode.name);
            $("#resourceUrl").val(treeNode.resourceUrl);
            $("#resourceStartDate").val(treeNode.resourceStartDate);
            $("#resourceEndDate").val(treeNode.resourceEndDate);
            initDatePicker('resourceStartDate');
            initDatePicker('resourceEndDate');
            $("#resource").show();
        }
    }

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
            else if(dataList[i].type === "G"){
                var node = {
                    "id":dataList[i].id,
                    "pId":dataList[i].pid,
                    "name":dataList[i].name,
                    "no":dataList[i].no,
                    "groupStartDate":dataList[i].effectdate,
                    "groupEndDate":dataList[i].expireddate,
                    // "icon":ctx+"/images/tree/1.png",
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
                    "name":dataList[i].name,
                    "no":dataList[i].no,
                    "resourceStartDate":dataList[i].effectdate,
                    "resourceEndDate":dataList[i].expireddate,
                    "resourceUrl":dataList[i].url,
                    // "icon":ctx+"/images/tree/2.png",
                    "type":dataList[i].type,
                    "dropRoot":false,
                    "dropInner":false
                };
                data.push(node);
            }
        }
        zNodes  = data;
        $.fn.zTree.init($("#treeDemo"), setting, zNodes);
        zTree = $.fn.zTree.getZTreeObj("treeDemo");
        rMenu = $("#rMenu");
    }

    function InitializationData2(dataList){
        var data = [];
        for(var i = 0; i<dataList.length; i++){
            if(dataList[i].name === "root"){
                var node = {
                    "id":dataList[i].id,
                    "pId":dataList[i].pid,
                    "name":dataList[i].name,
                    "open":true};
                data.push(node);
            }
            else if(dataList[i].pid === 0){
                var node = {
                    "id":dataList[i].id,
                    "pId":dataList[i].pid,
                    "name":dataList[i].name,
                    "no":dataList[i].no,
                    "groupStartDate":dataList[i].effectdate,
                    "groupEndDate":dataList[i].expireddate,
                    // "icon":ctx+"/images/tree/1.png",
                    "open":false,
                    "dropRoot":false
                };
                data.push(node);
            } else{
                var node = {
                    "id":dataList[i].id,
                    "pId":dataList[i].pid,
                    "name":dataList[i].name,
                    "no":dataList[i].no,
                    "resourceStartDate":dataList[i].effectdate,
                    "resourceEndDate":dataList[i].expireddate,
                    "resourceUrl":dataList[i].url,
                    // "icon":ctx+"/images/tree/2.png",
                    "dropRoot":false,
                    "dropInner":false
                };
                data.push(node);
            }
        }
        zNodes  = data;
        $.fn.zTree.init($("#treeDemo"), setting, zNodes);
        zTree = $.fn.zTree.getZTreeObj("treeDemo");
        rMenu = $("#rMenu");
    }

    function checkTreeNode(){
        $('#pNo').val('');
        $('#name').val('');
        $('#url').val('');
        $('#startDate').val('');
        $('#endDate').val('');
        vm.checked = false;
        var nodes = zTree.getSelectedNodes();
        vm.title = {id: nodes[0].id, name:nodes[0].name}
        doAjax({id: nodes[0].id}, "/common/UI_080030/checkTreeNode", false, true, function(resp) {
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
            doAjax(jsonData, "/common/UI_080030/queryClick", false, true, function(resp) {
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