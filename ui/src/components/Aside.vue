<template>
    <el-menu background-color="#545c64" text-color="#ffffff"
             active-text-color="#ffd04b" class="el-menu-vertical-demo"
             router>
        <el-menu-item index="/index/menu1"><el-icon><Position /></el-icon>Search</el-menu-item>
        <el-menu-item index="/index/menu2"><el-icon><ChatLineSquare /></el-icon>Information</el-menu-item>
        <el-menu-item index="/index/menu3"><el-icon><Setting /></el-icon>Setting</el-menu-item>
    </el-menu>
</template>

<script setup>
import { Position,ChatLineSquare,Setting } from "@element-plus/icons-vue";
</script>

<style scoped>

</style>