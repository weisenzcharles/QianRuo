<template>
  <div class="app-container inventory-forecast-page">
    <header class="page-heading">
      <div>
        <p class="page-kicker">INVENTORY FORECAST</p>
        <h2>库存预测</h2>
        <p class="page-description">查看每日库存预测快照、可接单数量和风险说明，提前安排备货与调货。</p>
      </div>
      <el-button v-hasPermi="['merchant:inventory:forecast:refresh']" :icon="Refresh" :loading="refreshing" @click="refreshForecast">
        刷新预测
      </el-button>
    </header>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="仓库">
          <el-select v-model="query.warehouseId" clearable filterable placeholder="全部可访问仓库" @change="load">
            <el-option v-for="item in warehouses" :key="warehouseId(item)" :label="warehouseLabel(item)" :value="warehouseId(item)" />
          </el-select>
        </el-form-item>
        <el-form-item label="展示天数">
          <el-select v-model="query.days" @change="load">
            <el-option v-for="days in dayOptions" :key="days" :label="`${days} 天`" :value="days" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="query.brand" clearable filterable placeholder="全部品牌" :disabled="query.mode === 'package'" @change="changeBrand">
            <el-option v-for="brand in brands" :key="brand" :label="brand" :value="brand" />
          </el-select>
        </el-form-item>
        <el-form-item label="型号">
          <el-select v-model="query.modelId" clearable filterable placeholder="全部型号" :disabled="query.mode === 'package'" @change="load">
            <el-option v-for="item in models" :key="item.modelId" :label="item.modelName" :value="item.modelId" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="forecast-toolbar">
      <div>
        <h3>未来 {{ query.days }} 天预测</h3>
        <p>每天独立生成快照；单元格显示预计可用库存和可接单数量。</p>
      </div>
      <el-radio-group v-model="query.mode" @change="changeMode">
        <el-radio-button label="model">按型号</el-radio-button>
        <el-radio-button label="package">按套餐</el-radio-button>
      </el-radio-group>
    </div>

    <el-alert title="预测结果来自最近一次快照刷新；需要立即核对时可手工刷新。" type="info" :closable="false" show-icon class="snapshot-tip" />

    <el-table v-loading="loading" :data="displayRows" border class="forecast-table">
      <el-table-column :label="query.mode === 'model' ? '型号' : '套餐名称'" prop="itemName" min-width="220" fixed="left" show-overflow-tooltip />
      <el-table-column v-if="query.mode === 'model'" label="品牌" prop="brand" width="140" fixed="left" show-overflow-tooltip />
      <el-table-column v-if="query.mode === 'package'" label="套餐编码" prop="itemCode" width="180" fixed="left" show-overflow-tooltip />
      <el-table-column label="当前总库存" prop="totalQuantity" width="120" align="center" fixed="left" />
      <el-table-column v-for="date in forecastDates" :key="date" :label="formatDate(date)" min-width="132" align="center">
        <template #default="{ row }">
          <div v-if="row.forecast[date]" class="forecast-cell">
            <strong>{{ row.forecast[date].availableQuantity }}</strong>
            <span>可用 / {{ row.forecast[date].orderableQuantity }} 可接单</span>
            <el-tag size="small" effect="plain" :type="riskTagType(row.forecast[date].riskLevel)">
              {{ row.forecast[date].riskLevelName }}
            </el-tag>
          </div>
          <span v-else class="muted-text">暂无快照</span>
        </template>
      </el-table-column>
      <template #empty><el-empty description="暂无库存预测快照，请先刷新预测" /></template>
    </el-table>
  </div>
</template>

<script setup>
import { computed, onActivated, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { listMerchantInventoryOptions } from '@/api/merchant/inventory'
import { listMerchantInventoryForecast, listMerchantWarehouses, refreshMerchantInventoryForecast } from '@/api/merchant/rentalBusiness'

const dayOptions = [3, 5, 7, 15, 30]
const loading = ref(false)
const refreshing = ref(false)
const warehouses = ref([])
const allOptions = ref([])
const rows = ref([])
const query = reactive({ mode: 'model', warehouseId: undefined, brand: '', modelId: undefined, days: 30 })
let hasBeenActivated = false

const brands = computed(() => [...new Set(allOptions.value.map((item) => item?.brand).filter(Boolean))])
const models = computed(() => {
  const seen = new Set()
  return allOptions.value
    .filter((item) => !query.brand || item?.brand === query.brand)
    .filter((item) => item?.modelId && !seen.has(item.modelId) && seen.add(item.modelId))
    .map((item) => ({ modelId: item.modelId, modelName: item.modelName || item.productName }))
})
const forecastDates = computed(() => [...new Set(rows.value.map((row) => dateKey(row.snapshotDate)))].filter(Boolean).sort())
const displayRows = computed(() => {
  const grouped = new Map()
  rows.value.forEach((row) => {
    const key = [row.forecastType, row.itemCode, row.itemName, row.brand].filter(Boolean).join(':')
    if (!grouped.has(key)) {
      grouped.set(key, { itemName: row.itemName, itemCode: row.itemCode, brand: row.brand, totalQuantity: row.totalQuantity, forecast: {} })
    }
    grouped.get(key).forecast[dateKey(row.snapshotDate)] = row
  })
  return [...grouped.values()]
})

function unwrap(response) { return response?.data ?? response }
function rowsOf(response) { const data = unwrap(response); return Array.isArray(data) ? data : data?.rows || [] }
function errorText(error) { return error?.msg || error?.message || '操作失败，请稍后重试' }
function dateKey(value) { return value ? String(value).slice(0, 10) : '' }
function formatDate(value) { const key = dateKey(value); return key ? key.slice(5).replace('-', '/') : '-' }
function warehouseId(item) { return item?.warehouseId ?? item?.id }
function warehouseLabel(item) {
  const name = item?.warehouseName ?? item?.name ?? '仓库'
  const code = item?.warehouseCode ?? item?.code
  return code ? `${name}（${code}）` : name
}
function riskTagType(level) { return ({ 0: 'success', 1: 'warning', 2: 'danger' })[Number(level)] || 'info' }
function changeBrand() {
  if (!models.value.some((item) => Number(item.modelId) === Number(query.modelId))) query.modelId = undefined
  load()
}
function changeMode() {
  if (query.mode === 'package') { query.brand = ''; query.modelId = undefined }
  load()
}
async function loadOptions() {
  const [warehouseResponse, optionResponse] = await Promise.all([
    listMerchantWarehouses({ pageNum: 1, pageSize: 1000, status: 0 }),
    listMerchantInventoryOptions({}),
  ])
  warehouses.value = rowsOf(warehouseResponse)
  allOptions.value = rowsOf(optionResponse)
}
async function load() {
  loading.value = true
  try {
    rows.value = rowsOf(await listMerchantInventoryForecast({ ...query }))
  } catch (error) {
    rows.value = []
    ElMessage.error(errorText(error))
  } finally {
    loading.value = false
  }
}
async function refreshForecast() {
  refreshing.value = true
  try {
    await refreshMerchantInventoryForecast()
    ElMessage.success('库存预测已刷新')
    await load()
  } catch (error) {
    ElMessage.error(errorText(error))
  } finally {
    refreshing.value = false
  }
}
async function loadPage() {
  try {
    await Promise.all([loadOptions(), load()])
  } catch (error) {
    ElMessage.error(errorText(error))
  }
}

onMounted(loadPage)
onActivated(() => {
  if (hasBeenActivated) loadPage()
  hasBeenActivated = true
})
</script>

<style scoped>
.inventory-forecast-page { width: 100%; min-height: 100%; }
.page-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.page-kicker { margin: 0 0 4px; color: var(--el-color-primary); font-size: 12px; font-weight: 700; letter-spacing: 0.12em; }
.page-heading h2 { margin: 0; font-size: 24px; font-weight: 650; }
.page-description { margin: 7px 0 0; color: var(--el-text-color-secondary); font-size: 13px; }
.filter-card { margin-bottom: 18px; }
.filter-card :deep(.el-form-item) { margin-bottom: 0; }
.filter-card :deep(.el-select) { width: 180px; }
.forecast-toolbar { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 12px; }
.forecast-toolbar h3 { margin: 0; font-size: 18px; font-weight: 600; }
.forecast-toolbar p { margin: 6px 0 0; color: var(--el-text-color-secondary); font-size: 13px; }
.snapshot-tip { margin-bottom: 12px; }
.forecast-table { width: 100%; }
.forecast-cell { display: flex; flex-direction: column; align-items: center; gap: 3px; min-height: 62px; justify-content: center; }
.forecast-cell strong { color: var(--el-text-color-primary); font-size: 17px; line-height: 1; }
.forecast-cell span { color: var(--el-text-color-secondary); font-size: 12px; white-space: nowrap; }
.muted-text { color: var(--el-text-color-secondary); }
@media (max-width: 900px) {
  .page-heading, .forecast-toolbar { flex-direction: column; }
  .filter-card :deep(.el-select) { width: 100%; }
}
</style>
