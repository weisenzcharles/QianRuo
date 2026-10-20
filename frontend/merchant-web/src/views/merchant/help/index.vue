<template>
  <stage-one-panel
    kicker="Help"
    title="帮助中心"
    :items="items"
  />
</template>

<script setup>
import StageOnePanel from '../components/StageOnePanel.vue'

const items = [
  { title: '操作指引', desc: '查看商户商品、库存、订单和账号管理说明。' },
  { title: '业务规则', desc: '查看套餐和订单渠道的业务校验规则。' },
  { title: '问题反馈', desc: '预留问题反馈、处理进度和平台联系入口。' }
]
</script>
