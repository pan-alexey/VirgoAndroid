//initialize lork storage;
//Lockr.prefix = 'kubaninstrument_';

const localIP = "http://37.46.132.69:8080/api/"
const internetIP = "http://192.168.22.101:8084/"
const serverTimeout = 1500;
//need savee acivity;


//-------------------------------------------------------------//
detectResult = function(qrcode){
    if (typeof app == "undefined") return;
    app.qrScan(qrcode);
};

//-------------------------------------------------------------//
let $ajax = function(option){
    let $option = option;
    $.ajax({    
        method: "POST",
        url: localIP+$option.url,
        crossDomain: true,
        data: $option.data,
        dataType: 'json',
        timeout: serverTimeout,
        success: function (data) {
            $option.success(data); 
        },
        error: function (error) {
            $.ajax({    
                method: "POST",
                url: internetIP+$option.url,
                crossDomain: true,
                data: $option.data,
                dataType: 'json',
                timeout: serverTimeout,
                success: function (data) {
                    $option.success(data); 
                },
                error: function (error) {
                    $option.error(error); 
                }
            });
        }
    });
};
//-------------------------------------------------------------//

if( typeof Lockr.get('history') == "undefined" ){
    //Lockr.sadd("history", "-");
} 

//-------------------------------------------------------------//
let app = new Vue({
    el: '#vueApp',
    data: {
        activity: 'activity-preload',
        activityСhangeCouponEnter:'',
        history : 0,
        historyArray : [],
        user: {
            login: Lockr.get('login'),
            password: Lockr.get('password'),
            name: Lockr.get('name'),
        },
        activityAuth : {
            login : null,
            password : null,
            error : null,
        },
        activityQR : {
            couponClass : "",
            couponAction : "",
            status : '',
            coupon : "******",
            check : "",
            fio : "",
            phone : "",
            info : "",
            registeAvalible : false,
            unregisteAvalible : false,
        },
        activityСhange : {
            couponClass : "",
            couponAction : "",
            status : '',
            coupon : "******",
            check : "",
            fio : "",
            phone : "",
            info : "",
            registeAvalible : false,
            unregisteAvalible : false,
        }
    },
    methods: {
        //-------------  login -------------//
        login: function () {
            $this = this;
            let _data = {
                'login' : this.activityAuth.login,
                'password' : this.activityAuth.password,
            }
            $ajax({
                url : "login.php",
                data : _data,
                success : function(data){
                    if(data.auth == "success"){
                        //Lockr.set('auth', data.auth);
                        Lockr.set('login', data.login);
                        Lockr.set('password', _data.password);
                        Lockr.set('name', data.name);
                        $this.enter();
                        return;
                    } 
                    $this.activityAuth.error = 'Неверный Логин/Пароль';
                    return;
                },
                error : function(){
                    $this.activityAuth.error = 'Ошибка соединения';
                    return;
                }
            });
        }
        //------------/  login -------------//



        //-------------  open_qr -------------//
        ,open_qr: function () {
            this.activity = 'activity-qr';
            if( !Lockr.get('login') ) {this.enter();return;}
            if (typeof _API !== "undefined") {_API.openScanner();}
        }
        //------------/  open_qr -------------//
        //-------------  qrScan -------------//
        ,qrScan: function (qrcode) {
            $this = this;
            if( !Lockr.get('login') ) {this.enter();return;}
            if(this.activity != 'activity-qr'){return;}
            if (typeof _API !== "undefined") {_API.playBeep();}
            this.activityQR.couponClass = "";
            this.activityQR.status = '';
            $this.activityQR.check =  "";
            $this.activityQR.fio =  "";
            $this.activityQR.phone =  "";
            $this.activityQR.info =  "";
            if(!qr[qrcode]) { 
                this.activityQR.couponClass='error'; 
                this.activityQR.coupon = 'ОШИБКА'; 
                this.activityQR.status = 'Купона не существует';
                return;
            }

            let cuponId = qrcode.substr(0,6);
            this.activityQR.coupon = cuponId;

            // 1 Проверяем купон на сервере.
            let _data = {};
            _data['login'] = Lockr.get('login');
            _data['password'] = Lockr.get('password');
            _data['name'] = Lockr.get('name');
            _data['id_coupon'] = cuponId;


            //Промис запроса чека
            var promise = new Promise(function(resolve,reject) {
                $ajax({
                    url : "get.php",
                    data : _data,
                    success : function(data){
                        if(data.auth != "success"){$this.exit(); return;}
                        $this.activityQR.couponClass=data.class; 
                        $this.activityQR.check = data.check_info;
                        $this.activityQR.fio = data.fio;
                        $this.activityQR.phone = data.phone;
                        $this.activityQR.couponAction = data.action;
                        $this.activityQR.info = data.info;
                        resolve(data);
                        return;
                    },
                    error : function(){
                        $this.activityQR.status = 'Ошибка соединения';
                        return;
                    }
                });
            });
            //-------------------------------------------------------------//
            promise.then( function(_result){
                return new Promise(function(resolve,reject) {
                    if(_result.action=="зарегистрировать" && $this.activityQR.couponClass!="error"){
                       $this.qrScanRegister();
                    }
                })
            });
        }
        //-------------  qrScanRegister -------------//
        ,qrScanRegister: function(){
            let _data = {};
            let $this = this;
            _data['login'] = Lockr.get('login');
            _data['password'] = Lockr.get('password');
            _data['name'] = Lockr.get('name');
            _data['id_coupon'] = this.activityQR.coupon;

            Lockr.sadd("history",  _data['id_coupon'] );

            $ajax({
                url : "register.php",
                data : _data,
                success : function(data){
                    $this.activityQR.couponClass = "success"; 
                    $this.activityQR.couponAction = "отменить"; 
                    return;
                },
                error : function(){
                    $this.activityQR.status = 'Ошибка соединения';
                    return;
                }
            });
        }

        //-------------  qrScanUnregister -------------//
        ,qrScanUnregister: function(){
            let _data = {};
            let $this = this;
            _data['login'] = Lockr.get('login');
            _data['password'] = Lockr.get('password');
            _data['name'] = Lockr.get('name');
            _data['id_coupon'] = this.activityQR.coupon;
            
            Lockr.sadd("history",  -_data['id_coupon'] );

            let promise = new Promise(function(resolve,reject) {
                $ajax({
                    url : "unregister.php",
                    data : _data,
                    success : function(data){
                        if(data.auth != "success"){$this.exit(); return;}

                        console.log( data );
                        resolve(data);
                        return;
                    },
                    error : function(){
                        $this.activityQR.status = 'Ошибка соединения';
                        return;
                    }
                });
            }).then( function(_result){
                return new Promise(function(resolve,reject) {
                $ajax({
                        url : "get.php",
                        data : _data,
                        success : function(data){
                            if(data.auth != "success"){$this.exit(); return;}
                            $this.activityQR.couponClass=data.class; 
                            $this.activityQR.check = data.check_info;
                            $this.activityQR.fio = data.fio;
                            $this.activityQR.phone = data.phone;
                            $this.activityQR.couponAction = data.action;
                            $this.activityQR.info = data.info;
                            resolve(data);
                            return;
                        },
                        error : function(){
                            $this.activityQR.status = 'Ошибка соединения';
                            return;
                        }
                    });
                });
            });
        //-------------  qrScanUnregister -------------//
        }




        //-------------  open_change -------------//
        ,open_change: function () {
            if( !Lockr.get('login') ) {this.enter();return;}
            if (typeof _API !== "undefined") {_API.closeScanner();}
            this.activity = 'activity-change';

        }
        //------------/  open_change -------------//


        //-------------  open_change -------------//
        ,open_call: function () {
            if( !Lockr.get('login') ) {this.enter();return;}
            if (typeof _API !== "undefined") {_API.closeScanner();}
            this.activity = 'activity-call';

        }
        //------------/  open_change -------------//

        //-------------  open_change -------------//
        ,open_history: function () {
            if( !Lockr.get('login') ) {this.enter();return;}
            if (typeof _API !== "undefined") {_API.closeScanner();}
            this.activity = 'activity-history';

            var historyArray = Lockr.smembers("history");
            historyArray.reverse();
                
            this.historyArray = Lockr.smembers("history");
            this.historyArray = [
                1	,
                2	,
                3	,
                4	,
                5	,
                6	,
                7	,
                8	,
                9	,
                -10	,
                11	,
                12	,
                13	,
                14	,
                15	,
                16	,
                17	,
                18	,
                19	,
                20	,
                21	,
                22	,
                23	,
                24	,
                25	,
                26	,
                27	,
                28	,
                29	,
                30	,
                31	,
                32	,
                33	,
                34	,
                35	,
                36	,
                37	,
                38	,
                39	,
                40	,
                41	,
                42	,
                43	,
                44	,
                45	,
                46	,
                47	,
                48	,
                49	,
                50	,
                51	,
                52	,
                53	,
                54	,
                55	,
                56	,
                57	,
                58	,
                59	,
                60	,
                61	,
                62	,
                63	,
                64	,
                65	,
                66	,
                67	,
                68	,
                69	,
                70	,
                71	,
                72	,
                73	,
                74	,
                75	,
                76	,
                77	,
                78	,
                79	,
                80	,
                81	,
                82	,
                83	,
                84	,
                85	,
                86	,
                87	,
                88	,
                89	,
                90	,
                91	,
                92	,
                93	,
                94	,
                95	,
                96	,
                97	,
                98	,
                99	,
                100	,
                101	,
                102	,
                103	,
                104	,
                105	,
                106	,
                107	,
                108	,
                109	,
                110	,
                111	,
                112	,
                113	,
                114	,
                115	,
                116	,
                117	,
                118	,
                119	,
                120	,
                121	,
                122	,
                123	,
                124	,
                125	,
                126	,
                127	,
                128	,
                129	,
                130	,
                131	,
                132	,
                133	,
                134	,
                135	,
                136	,
                137	,
                138	,
                139	,
                140	,
                141	,
                142	,
                143	,
                144	,
                145	,
                146	,
                147	,
                148	,
                149	,
                150	,
                151	,
                152	,
                153	,
                154	,
                155	,
                156	,
                157	,
                158	,
                159	,
                160	,
                161	,
                162	,
                163	,
                164	,
                165	,
                166	,
                167	,
                168	,
                169	,
                170	,
                171	,
                172	,
                173	,
                174	,
                175	,
                176	,
                177	,
                178	,
                179	,
                180	,
                181	,
                182	,
                183	,
                184	,
                185	,
                186	,
                187	,
                188	,
                189	,
                190	,
                191	,
                192	,
                193	,
                194	,
                195	,
                196	,
                197	,
                198	,
                199	,
                200	,
                201	,
                202	,
                203	,
                204	,
                205	,
                206	,
                207	,
                208	,
                209	,
                210	,
                211	,
                212	,
                213	,
                214	,
                215	,
                216	,
                217	,
                218	,
                219	,
                220	,
                221	,
                222	,
                223	,
                224	,
                225	,
                226	,
                227	,
                228	,
                229	,
                230	,
                231	,
                232	,
                233	,
                234	,
                235	,
                236	,
                237	,
                238	,
                239	,
                240	,
                241	,
                242	,
                243	,
                244	,
                245	,
                246	,
                247	,
                248	,
                249	,
                250	,
                251	,
                252	,
                253	,
                254	,
                255	,
                256	,
                257	,
                258	,
                259	,
                260	,
                261	,
                262	,
                263	,
                264	,
                265	,
                266	,
                267	,
                268	,
                269	,
                270	,
                271	,
                272	,
                273	,
                274	,
                275	,
                276	,
                277	,
                278	,
                279	,
                280	,
                281	,
                282	,
                283	,
                284	,
                285	,
                286	,
                287	,
                288	,
                289	,
                290	,
                291	,
                292	,
                293	,
                294	,
                295	,
                296	,
                297	,
                298	,
                299	,
                300	,
                301	,
                302	,
                303	,
                304	,
                305	,
                306	,
                307	,
                308	,
                309	,
                310	,
                311	,
                312	,
                313	,
                314	,
                315	,
                

            ]
        }
        //------------/  open_change -------------//

        //-------------  open_change -------------//
        ,open_setting: function () {
            if( !Lockr.get('login') ) {return;}
            if (typeof _API !== "undefined") {_API.closeScanner();}
            this.activity = 'activity-setting';

            let history = Lockr.smembers("history");
            console.log(history);
            console.log(history.length);
            this.history = history.length;

        }
        //------------/  open_change -------------//

        //-------------  enter -------------//
        ,enter: function () {
            if( Lockr.get('login') ){
                this.user.login = Lockr.get('login');
                this.user.password = Lockr.get('password');
                this.user.name = Lockr.get('name');
                this.open_qr();
                return;           
            }
            this.activity = 'activity-auth';
            // in this swich case to open old activity
        }
        //------------/  enter -------------//



        //-------------  exit -------------//
        ,exit: function () {
            Lockr.rm('login');
            Lockr.rm('password');
            Lockr.rm('name');

            this.activityQR = {
                couponClass : "",
                couponAction : "",
                status : '',
                coupon : "******",
                check : "",
                fio : "",
                phone : "",
                info : "",
                registeAvalible : false,
                unregisteAvalible : false,
            };

            this.activityСhange = {
                couponEnter : "",
                couponClass : "",
                status : '',
                coupon : "******",
                check : "",
                fio : "",
                phone : "",
                info : "",
                registeAvalible : false,
                unregisteAvalible : false,
            };

            if (typeof _API !== "undefined") {_API.closeScanner();}
            this.enter();
        }
        //------------/  exit -------------//
        ,changeNumb : function(result){
            let _data = {};
            let $this = this;
            $this.activityСhange.status = '';

            _data['login'] = Lockr.get('login');
            _data['password'] = Lockr.get('password');
            _data['name'] = Lockr.get('name');
            _data['id_coupon'] = result;
            $ajax({
                url : "get.php",
                data : _data,
                success : function(data){
                    if(data.auth != "success"){$this.exit(); return;}
                    $this.activityСhange.couponClass=data.class; 
                    $this.activityСhange.coupon = data.id_coupon; 
                    $this.activityСhange.check = data.check_info;
                    $this.activityСhange.fio = data.fio;
                    $this.activityСhange.phone = data.phone;
                    $this.activityСhange.couponAction = data.action;
                    $this.activityСhange.info = data.info;
                    console.log(data);
                    return;
                },
                error : function(){
                    //$this.activityQR.status = 'Ошибка соединения';
                    return;
                }
            });
        }
        ,activityСhangeRegister : function(result){
            let _data = {};
            let $this = this;
            _data['login'] = Lockr.get('login');
            _data['password'] = Lockr.get('password');
            _data['name'] = Lockr.get('name');
            _data['id_coupon'] = this.activityСhange.coupon;

            Lockr.sadd("history",  _data['id_coupon'] );

            $ajax({
                url : "register.php",
                data : _data,
                success : function(data){
                    $this.activityСhange.couponClass = "success"; 
                    $this.activityСhange.couponAction = "отменить"; 
                    return;
                },
                error : function(){
                    $this.activityСhange.status = 'Ошибка соединения';
                    return;
                }
            });
        }
        ,activityСhangeUnregister : function(){
            let _data = {};
            let $this = this;
            _data['login'] = Lockr.get('login');
            _data['password'] = Lockr.get('password');
            _data['name'] = Lockr.get('name');
            _data['id_coupon'] = this.activityСhange.coupon;
            
            Lockr.sadd("history",  -_data['id_coupon'] );

            let promise = new Promise(function(resolve,reject) {
                $ajax({
                    url : "unregister.php",
                    data : _data,
                    success : function(data){
                        if(data.auth != "success"){$this.exit(); return;}
                        console.log( data );
                        resolve(data);
                        return;
                    },
                    error : function(){
                        //$this.activityСhange.status = 'Ошибка соединения';
                        return;
                    }
                });
            }).then( function(_result){
                return new Promise(function(resolve,reject) {
                    _data['login'] = Lockr.get('login');
                    _data['password'] = Lockr.get('password');
                    _data['name'] = Lockr.get('name');
                    _data['id_coupon'] = $this.activityСhange.coupon;
                    $ajax({
                        url : "get.php",
                        data : _data,
                        success : function(data){
                            if(data.auth != "success"){$this.exit(); return;}
                            $this.activityСhange.couponClass=data.class; 
                            $this.activityСhange.coupon = data.id_coupon; 
                            $this.activityСhange.check = data.check_info;
                            $this.activityСhange.fio = data.fio;
                            $this.activityСhange.phone = data.phone;
                            $this.activityСhange.couponAction = data.action;
                            $this.activityСhange.info = data.info;
                            console.log(data);
                            return;
                        },
                        error : function(){
                            //$this.activityQR.status = 'Ошибка соединения';
                            return;
                        }
                    });
                });
            });
        }







        ,exportData: function() {
            let _data = {};
            _data['login'] = Lockr.get('login');
            _data['password'] = Lockr.get('password');
            _data['name'] = Lockr.get('name');
            _data['history'] = Lockr.smembers("history");
            $ajax({
                url : "export.php",
                data : _data,
                success : function(data){
                    if(data.auth != "success"){$this.exit(); return;}
                    $this.activityСhange.couponClass=data.class; 
                    $this.activityСhange.coupon = data.id_coupon; 
                    $this.activityСhange.check = data.check_info;
                    $this.activityСhange.fio = data.fio;
                    $this.activityСhange.phone = data.phone;
                    $this.activityСhange.couponAction = data.action;
                    $this.activityСhange.info = data.info;
                    console.log(data);
                    return;
                },
                error : function(){
                    //$this.activityQR.status = 'Ошибка соединения';
                    return;
                }
            });

        }


        //activityСhangeRegister
        //activityСhangeUnregister
        ,isNumber: function(evt) {
            evt = (evt) ? evt : window.event;
            var charCode = (evt.which) ? evt.which : evt.keyCode;
            if ((charCode > 31 && (charCode < 48 || charCode > 57)) && charCode !== 46) {
              evt.preventDefault();
            } else {
              return true;
            }
        }
    },
    watch: {
        activityСhangeCouponEnter :  _.throttle( function(value){this.changeNumb(value);} ,800)
   
    },

    mounted: function () { 
        //if (typeof _API !== "undefined") {_API.closeScanner();}
        this.enter();
    }
})
//-------------------------------------------------------------//




// Lockr.smembers("wat");



















/* 

_API.openScanner()
*/

// new Vue({
//     el: '#body',
//     data: {
//       input: '# hello'

//       activity: 'preload',
//       selectMenu : "",
//       activityAuthText : 'error'
//     },
//     computed: {
//       compiledMarkdown: function () {
//         return marked(this.input, { sanitize: true })
//       }
//     },
//     methods: {
//       update: _.debounce(function (e) {
//         this.input = e.target.value
//       }, 300)
//     }
// });