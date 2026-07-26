<template>
  <stage-one-panel
    kicker="Merchant Settings"
    title="商家设置"
    :items="items"
  />
</template>

<script setup>
import StageOnePanel from '../components/StageOnePanel.vue'

const items = [
  { title: '主体资料', desc: '维护商户主体、认证状态和联系人。' },
  { title: '员工权限', desc: '预留商家员工、角色和仓库范围。' },
  { title: '敏感权限', desc: '预留脱敏、明文查看和导出授权。' }
]
</script>
