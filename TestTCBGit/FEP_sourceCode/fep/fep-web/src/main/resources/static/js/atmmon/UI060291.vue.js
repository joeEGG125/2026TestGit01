var vm = new Vue({
    el: '#UI060291',
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
