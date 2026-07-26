<template>
  <stage-one-panel
    kicker="Market"
    title="周转市场"
    :items="items"
  />
</template>

<script setup>
import StageOnePanel from '../components/StageOnePanel.vue'

const items = [
  { title: '发布库存', desc: '预留标准化发帖和全下拉发布。' },
  { title: '筛选关注', desc: '预留型号、城市、数量和关注订阅。' },
  { title: '举报风控', desc: '预留举报保证金、处罚和违规追溯。' }
]
</script>
