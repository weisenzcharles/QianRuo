<template>
  <stage-one-panel
    kicker="Merchant Dashboard"
    title="商家工作台"
    :items="items"
  />
</template>

<script setup>
import StageOnePanel from '../components/StageOnePanel.vue'

const items = [
  { title: '今日待发', desc: '承接后续订单 MVP 的待发货、待备货和异常提醒。' },
  { title: '今日归还', desc: '承接客户归还、到库验机和售后处理入口。' },
  { title: '经营概览', desc: '预留营收、租用中、库存占用和工作提醒指标。' }
]
</script>
