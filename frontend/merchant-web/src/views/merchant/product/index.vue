<template>
  <stage-one-panel
    kicker="Product"
    title="商品资料"
    :items="items"
  />
</template>

<script setup>
import StageOnePanel from '../components/StageOnePanel.vue'

const items = [
  { title: '品牌型号', desc: '维护平台标准型号和商家常用型号。' },
  { title: '商品/SN', desc: '维护商品、SN、配件、二维码和条码。' },
  { title: '套餐', desc: '维护录单快捷套餐和适用渠道配置。' }
]
</script>
