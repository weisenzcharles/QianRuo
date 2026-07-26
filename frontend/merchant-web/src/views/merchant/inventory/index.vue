<template>
  <stage-one-panel
    kicker="Inventory"
    title="仓库库存"
    :items="items"
  />
</template>

<script setup>
import StageOnePanel from '../components/StageOnePanel.vue'

const items = [
  { title: '仓库', desc: '第 2 阶段实现仓库资料、员工仓库权限和归还仓配置。' },
  { title: '库存流水', desc: '预留库存预占、出入库、调货和盘点流水。' },
  { title: '库存快照', desc: '预留在库空闲、物流库存、预计库存和告警。' }
]
</script>
