<template>
    <div class="container">
        <el-container style="height: 100%">
            <el-header><Header/></el-header>
            <el-container>
                <el-aside width="200px"><Aside/></el-aside>
                <el-main>
                    <router-view></router-view>
                </el-main>
            </el-container>
            <el-footer><Footer/></el-footer>
        </el-container>
    </div>
</template>

<script>
import Header from "./Header.vue";
import Aside from "./Aside.vue";
import Main from "./Main.vue";
import Footer from "./Footer.vue";

export default {
    name: 'Index',
    components:{Header,Aside,Main,Footer},
    props: {
        msg: String
    }
}
</script>
<style>

.el-header, .el-footer {
    background-color: #B3C0D1;
    color: #333;
    text-align: center;
    line-height: 60px;
}

.el-aside {
    background-color: #D3DCE6;
    color: #333;
    text-align: center;
    line-height: 200px;
}

.el-main {
    background-color: #E9EEF3;
    color: #333;
    text-align: center;

}

body > .el-container {
    margin-bottom: 40px;
}

.container{
    position: absolute;
    top:0;
    left: 0;
    width: 100%;
    height:100%;
}
</style>