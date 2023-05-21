import { createApp } from 'vue'
import App from './App.vue'
import Main from "./components/Main.vue";
import ElementPlus from 'element-plus'
import router from "./router";
import 'element-plus/dist/index.css'
import AMapLoader from '@amap/amap-jsapi-loader';

const app = createApp(App);
app.use(ElementPlus);

app.use(router)

app.mount('#app')

window._AMapSecurityConfig = {
    securityJsCode:'27c8047c8a3782a2439d565b41972591',          //  申请的安全密钥
}
AMapLoader.load({
    "key": "6a1dffbc1897731b1cb56383912128ad",              // 申请好的Web端开发者Key，首次调用 load 时必填
    "version": "1.4.15",   // 指定要加载的 JS API 的版本，缺省时默认为 1.4.15
    "plugins": [
        'AMap.PlaceSearch',
    ],           // 需要使用的的插件列表，如比例尺'AMap.Scale'等
    "AMapUI": {                                         // 是否加载 AMapUI，缺省不加载
        "version": '1.0',                               // AMapUI 缺省 1.1
        "plugins": [
            'overlay/SimpleMarker',//SimpleMarker
            'overlay/SimpleInfoWindow',//SimpleInfoWindow
        ],                                   // 需要加载的 AMapUI ui插件
    },
}).then((AMap)=>{
    // AMap = new AMap.Map('container');
}).catch(e => {
    console.log(e);
})

