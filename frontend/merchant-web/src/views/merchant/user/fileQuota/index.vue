<template>
  <div class="app-container merchant-list-page">
    <el-card shadow="never">
      <template #header>文件配额</template>
      <el-progress :percentage="usagePercent" :status="usagePercent >= 90 ? 'exception' : undefined" />
      <el-descriptions :column="3" label-width="120" class="quota-summary">
        <el-descriptions-item label="总配额">{{ formatBytes(quota.quotaBytes) }}</el-descriptions-item>
        <el-descriptions-item label="已使用">{{ formatBytes(quota.usedBytes) }}</el-descriptions-item>
        <el-descriptions-item label="可用">{{ formatBytes(quota.availableBytes) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-card shadow="never" class="ledger-card">
      <template #header>文件操作审计</template>
      <el-row :gutter="10" class="mb8">
        <right-toolbar :search="false" @queryTable="load" />
      </el-row>
      <el-table v-loading="loading" :data="ledger" border>
        <el-table-column prop="fileName" label="文件名" min-width="220" show-overflow-tooltip />
        <el-table-column prop="changeType" label="操作" width="110" />
        <el-table-column prop="refType" label="业务资源" width="130" />
        <el-table-column prop="fileSize" label="文件大小" width="120"><template #default="scope">{{ formatBytes(scope.row.fileSize) }}</template></el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="190" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { getMerchantFileQuota, listMerchantFileQuotaLedger } from '@/api/merchant/fileQuota'

const loading = ref(false)
const quota = ref({ quotaBytes: 0, usedBytes: 0, availableBytes: 0 })
const ledger = ref([])
const usagePercent = computed(() => quota.value.quotaBytes ? Math.min(100, Math.round(quota.value.usedBytes * 100 / quota.value.quotaBytes)) : 0)

/** 将字节数转换成便于阅读的文件容量。 */
function formatBytes(value) {
  const bytes = Number(value || 0)
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`
  return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} GB`
}

/** 加载配额账户和文件审计流水。 */
function load() {
  loading.value = true
  Promise.all([getMerchantFileQuota(), listMerchantFileQuotaLedger()]).then(([quotaResponse, ledgerResponse]) => {
    quota.value = quotaResponse.data || quota.value
    ledger.value = ledgerResponse.data || []
  }).finally(() => { loading.value = false })
}

load()
</script>

<style scoped>
.quota-summary { margin-top: 20px; }
.ledger-card { margin-top: 16px; }
</style>
