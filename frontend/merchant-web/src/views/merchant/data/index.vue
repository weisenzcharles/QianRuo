<template>
  <stage-one-panel
    kicker="Data"
    title="经营数据"
    :items="items"
  />
</template>

<script setup>
import StageOnePanel from '../components/StageOnePanel.vue'

const items = [
  { title: '订单概览', desc: '查看订单数量、状态分布和待处理事项。' },
  { title: '库存概览', desc: '查看设备、SN、配件和套餐的库存占用情况。' },
  { title: '经营趋势', desc: '预留营业额、出租率和履约率统计入口。' }
]
</script>
