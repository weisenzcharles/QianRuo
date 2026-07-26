<template>
  <stage-one-panel
    kicker="Order Center"
    title="订单中心"
    :items="items"
  />
</template>

<script setup>
import StageOnePanel from '../components/StageOnePanel.vue'

const items = [
  { title: '录单', desc: '第 3 阶段实现订单创建、复制解析和状态机。' },
  { title: '发货', desc: '预留 SN 绑定、发货视频、快递单号和面单打印。' },
  { title: '归还验机', desc: '预留归还入库、验机图片、售后和完结动作。' }
]
</script>
