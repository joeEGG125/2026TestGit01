var vm = new Vue({
    el: '#UI080040',
    data: {
        title: {},
        list1:[

        ],
        list2:[

        ],
        checked:false,
        checkModel:[]
    },
    watch:{
        checkModel(){
            if(vm.checkModel.length==vm.list2.length){
                vm.checked=true;
            }else{
                vm.checked=false;
            }
        }
    },
    methods:{
        checkAll(){
            if(vm.checked){
                vm.checkModel=[];
            }else{
                vm.list2.forEach((item)=>{
                    if(vm.checkModel.indexOf(item.id)==-1){
                        vm.checkModel.push(item.id)
                    }
                })
            }
        }
    },
});

function fuzzyScreen() {
    vm.list2 = vm.list1;
    if($('#pNo').val()){
        vm.list2 = vm.list2.filter(
            item => item.pNo.indexOf($('#pNo').val()) > -1
        )
    }
    if(vm.list2.length > 0){
        if($('#name').val()){
            vm.list2 = vm.list2.filter(
                item => item.name.indexOf($('#name').val()) > -1
            )
        }
    }

    if(vm.list2.length > 0){
        if($('#startDate').val()){
            vm.list2 = vm.list2.filter(
                item => item.startDate.indexOf($('#startDate').val()) > -1
            )
        }
    }

    if(vm.list2.length > 0){
        if($('#endDate').val()){
            vm.list2 = vm.list2.filter(
                item => item.endDate.indexOf($('#endDate').val()) > -1
            )
        }
    }
}

function clickSort(msg,name) {
    $('img').attr("src",ctx+"/images/sort/sort-default.png");
    if(msg.className === 'desc'){
        msg.lastChild.src = ctx+"/images/sort/sort-asc.png";
        msg.className = "asc";
        vm.list2 = vm.list2.reverse();
    }else{
        msg.lastChild.src = ctx+"/images/sort/sort-desc.png";
        msg.className = "desc";
        methodSorting("desc",name)
    }
}

function methodSorting(type,name){
    if(name === 'pNo'){
        vm.list2 = vm.list2.sort(function (a,b) {
            return  b.pNo-a.pNo;
        })
    }else if(name === 'name'){
        vm.list2 = vm.list2.sort(function (a,b) {
            return  b.name.localeCompare(a.name);
        })
    }else if(name === 'startDate'){
        vm.list2 = vm.list2.sort(function (a,b) {
            let aTimeString = a.startDate
            let bTimeString = b.startDate
            aTimeString = aTimeString.replace(/-/g,'/')
            bTimeString = bTimeString.replace(/-/g,'/')
            let aTime = new Date(aTimeString).getTime()
            let bTime = new Date(bTimeString).getTime()
            return bTime - aTime;
        })
    }else{
        vm.list2 = vm.list2.sort(function (a,b) {
            let aTimeString = a.endDate
            let bTimeString = b.endDate
            aTimeString = aTimeString.replace(/-/g,'/')
            bTimeString = bTimeString.replace(/-/g,'/')
            let aTime = new Date(aTimeString).getTime()
            let bTime = new Date(bTimeString).getTime()
            return bTime - aTime;
        })
    }
}