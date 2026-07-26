<template>
  <stage-one-panel
    kicker="Logistics"
    title="物流归还"
    :items="items"
  />
</template>

<script setup>
import StageOnePanel from '../components/StageOnePanel.vue'

const items = [
  { title: '快递下单', desc: '预留承运商配置、下单和面单打印。' },
  { title: '路由同步', desc: '预留物流轨迹、异常提醒和时效检查。' },
  { title: '客户归还', desc: '预留客户归还下单和归还仓锁定。' }
]
</script>
