<template>
        <div class="m-4">
            <p>Top-k中的k值设定</p>
            <el-input-number v-model="num" :min="1" :max="10" @change="handleChange" />
        </div>
        <div class="m-4">
            <p>检索方法设定</p>
            <el-cascader
                    v-model="value"
                    :options="options2"

            />
        </div>
    <div class="flex justify-space-between mb-4 flex-wrap gap-4">
        <el-button
                v-for="button in buttons"
                :key="button.text"
                :type="button.type"
                text
                bg
        >{{ button.text }}</el-button
        >
    </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

const num = ref(1)
const handleChange = (value: number) => {
    console.log(value)
}

const value = ref([])

const options2 = [
    {
        value: 'bsp',
        label: 'BSP',
    },
    {
        value: 'spp',
        label: 'SPP',

    },
    {
        value: 'sp',
        label: 'SP',
    },
]
const buttons = [
    { type: 'primary', text: 'set' },
] as const
</script>

<style scoped>

</style>