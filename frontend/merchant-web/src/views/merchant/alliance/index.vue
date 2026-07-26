<template>
  <stage-one-panel
    kicker="Alliance"
    title="联盟协作"
    :items="items"
  />
</template>

<script setup>
import StageOnePanel from '../components/StageOnePanel.vue'

const items = [
  { title: '联盟关系', desc: '预留盟主、成员和协作规则。' },
  { title: '共享库存', desc: '预留共享 SN 库存和承接上限。' },
  { title: '协助订单', desc: '预留转派、拒绝、公众池和结算记账。' }
]
</script>
