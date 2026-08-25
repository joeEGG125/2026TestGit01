var vm = new Vue({
    el: '#UI080080',
    data: {
        list1:[
            {id:1,name:'sadfsadf',qunzu:'asdas',fhdh:'asdas',shangcidengluriqi:'sdf',shangcidenglushijian:'sdafsad',shangcixiugairen:'asdasdasdf',shangcixiugai:'asdas'},
            {id:2,name:'sadfsadf',qunzu:'asdas',fhdh:'asdas',shangcidengluriqi:'sdf',shangcidenglushijian:'sdafsad',shangcixiugairen:'asdasdasdf',shangcixiugai:'asdas'},
            {id:3,name:'sadfsadf',qunzu:'asdas',fhdh:'asdas',shangcidengluriqi:'sdf',shangcidenglushijian:'sdafsad',shangcixiugairen:'asdasdasdf',shangcixiugai:'asdas'},
            {id:4,name:'sadfsadf',qunzu:'asdas',fhdh:'asdas',shangcidengluriqi:'sdf',shangcidenglushijian:'sdafsad',shangcixiugairen:'asdasdasdf',shangcixiugai:'asdas'},
            {id:5,name:'sadfsadf',qunzu:'asdas',fhdh:'asdas',shangcidengluriqi:'sdf',shangcidenglushijian:'sdafsad',shangcixiugairen:'asdasdasdf',shangcixiugai:'asdas'},
            {id:6,name:'sadfsadf',qunzu:'asdas',fhdh:'asdas',shangcidengluriqi:'sdf',shangcidenglushijian:'sdafsad',shangcixiugairen:'asdasdasdf',shangcixiugai:'asdas'},
            {id:7,name:'sadfsadf',qunzu:'asdas',fhdh:'asdas',shangcidengluriqi:'sdf',shangcidenglushijian:'sdafsad',shangcixiugairen:'asdasdasdf',shangcixiugai:'asdas'},
            {id:8,name:'sadfsadf',qunzu:'asdas',fhdh:'asdas',shangcidengluriqi:'sdf',shangcidenglushijian:'sdafsad',shangcixiugairen:'asdasdasdf',shangcixiugai:'asdas'},
            {id:9,name:'sadfsadf',qunzu:'asdas',fhdh:'asdas',shangcidengluriqi:'sdf',shangcidenglushijian:'sdafsad',shangcixiugairen:'asdasdasdf',shangcixiugai:'asdas'},
            {id:10,name:'sadfsadf',qunzu:'asdas',fhdh:'asdas',shangcidengluriqi:'sdf',shangcidenglushijian:'sdafsad',shangcixiugairen:'asdasdasdf',shangcixiugai:'asdas'},
        ],
        list1cover:[

        ],
    },
    methods: {
        indexs:function () {
            if(this.list1.length !== 0){
                this.list1cover.length=0;
                this.List1Cover();
            }

        },
        List1Cover:function () {
            var sice = 10- this.list1.length;
            for (var i = 0;i< sice;i++){
                this.list1cover.push({id:'',name:''});
            }
        },
    },
    mounted(){
        this.indexs();
    },
});