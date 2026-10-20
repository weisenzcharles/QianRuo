<template>
  <el-table :data="rows" border size="small" class="ledger-table">
    <el-table-column label="流水号" prop="ledgerNo" min-width="170" show-overflow-tooltip>
      <template #default="{ row }">
        <el-link type="primary" :underline="false" @click="$emit('detail', row)">{{ row.ledgerNo || '-' }}</el-link>
      </template>
    </el-table-column>
    <el-table-column label="资金账户" prop="accountTypeName" width="145" show-overflow-tooltip />
    <el-table-column label="业务类型" prop="bizTypeName" min-width="130" show-overflow-tooltip />
    <el-table-column label="方向" prop="directionName" width="84" align="center">
      <template #default="{ row }"><el-tag size="small" :type="directionType(row.direction)">{{ row.directionName || '-' }}</el-tag></template>
    </el-table-column>
    <el-table-column label="金额" prop="amount" width="125" align="right"><template #default="{ row }">{{ money(row.amount) }}</template></el-table-column>
    <el-table-column v-if="!compact" label="余额" prop="balanceAfter" width="125" align="right"><template #default="{ row }">{{ money(row.balanceAfter) }}</template></el-table-column>
    <el-table-column v-if="showReference" label="关联类型" prop="refType" width="100" align="center" />
    <el-table-column v-if="!compact" label="发票" prop="invoiceStatusName" width="95" align="center" />
    <el-table-column label="状态" prop="ledgerStatusName" width="90" align="center" />
    <el-table-column label="发生时间" prop="createTime" min-width="165" />
  </el-table>
</template>

<script setup>
defineProps({ rows: { type: Array, default: () => [] }, compact: Boolean, showReference: Boolean })
defineEmits(['detail'])

function money(value) {
  return `¥ ${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function directionType(direction) {
  return { 1: 'success', 2: 'danger', 3: 'warning', 4: 'info', 5: 'info' }[direction] || 'info'
}
</script>

<style scoped>
.ledger-table :deep(.el-link) { vertical-align: middle; }
</style>
