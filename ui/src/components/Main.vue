<template>
    <div>
        <el-icon :size="50" color="#3375b9">
            <UserFilled />
        </el-icon>
    </div>
    <div>
        <el-button type="primary" plain :icon="Search">Search</el-button>
        <el-link href="https://www.baidu.com" target="_blank">默认链接</el-link>
    </div>
</template>

<script setup lang='ts'>
    import { UserFilled,Search } from "@element-plus/icons-vue";
</script>
<style scoped>
.el-icon-user{
    font-size: 30px;
    color: green;
}
</style>