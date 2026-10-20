<template>
  <div class="app-container platform-operations-page">
    <PageHeading
      kicker="OPERATIONS CENTER"
      title="经营分析与运营风控"
      description="查看平台订单、库存、财务和工单的周期指标及风险事项"
    >
      <template #aside>
        <el-button :icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
      </template>
    </PageHeading>

    <el-card shadow="never" class="filter-panel">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="统计周期">
          <el-select v-model="days" class="days-select" @change="loadData">
            <el-option v-for="item in dayOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <span class="date-range">{{ overview.beginDate || '-' }} 至 {{ overview.endDate || '-' }}</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row v-loading="loading" :gutter="16" class="summary-grid">
      <el-col v-for="item in summaryCards" :key="item.key" :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value" :class="item.tone">{{ item.value }}</div>
          <div class="summary-help">{{ item.help }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-grid">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="operations-panel">
          <template #header>
            <div class="panel-heading"><span>订单经营趋势</span><span class="panel-note">按订单创建日期统计</span></div>
          </template>
          <el-table v-loading="loading" :data="overview.trends || []" border height="420">
            <el-table-column label="日期" prop="statDate" width="125" />
            <el-table-column label="订单数" prop="orderCount" width="100" align="right" />
            <el-table-column label="租金收入" prop="rentAmount" min-width="130" align="right">
              <template #default="{ row }">{{ money(row.rentAmount) }}</template>
            </el-table-column>
            <el-table-column label="已完成" prop="completedOrderCount" width="100" align="right" />
            <el-table-column label="逾期" prop="overdueOrderCount" width="100" align="right">
              <template #default="{ row }"><span :class="{ 'danger-text': Number(row.overdueOrderCount) > 0 }">{{ row.overdueOrderCount || 0 }}</span></template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && !(overview.trends || []).length" description="暂无订单趋势数据" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="operations-panel risk-panel">
          <template #header>
            <div class="panel-heading"><span>运营风险事项</span><span class="panel-note">规则型实时检查</span></div>
          </template>
          <div v-if="overview.risks?.length" class="risk-list">
            <div v-for="(risk, index) in overview.risks" :key="`${risk.riskType}-${risk.subject}-${index}`" class="risk-item" :class="{ 'risk-item--selected': Number(risk.riskEventId) === selectedRiskId }">
              <div class="risk-item__top">
                <el-tag :type="riskTagType(risk.riskLevel)" size="small">{{ risk.riskLevelName || riskLevelName(risk.riskLevel) }}</el-tag>
                <el-tag v-if="risk.eventStatusName" :type="risk.eventStatus === 1 ? 'info' : 'warning'" size="small" effect="plain">
                  {{ risk.eventStatusName }}
                </el-tag>
                <span class="risk-type">{{ risk.riskTypeName || riskTypeName(risk.riskType) }}</span>
                <strong>{{ risk.subject }}</strong>
                <span v-if="risk.count" class="risk-count">{{ risk.count }}</span>
              </div>
              <div class="risk-detail">{{ risk.detail }}</div>
              <div class="risk-item__bottom">
                <span v-if="risk.lastSeenTime" class="risk-time">最近发现：{{ risk.lastSeenTime }}</span>
                <el-button
                  v-if="risk.riskEventId && Number(risk.eventStatus) === 0"
                  link
                  type="warning"
                  size="small"
                  :loading="acknowledgingRiskId === risk.riskEventId"
                  @click="acknowledgeRisk(risk)"
                >确认已知</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无风险事项" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="analytics-grid">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="operations-panel">
          <template #header>
            <div class="panel-heading"><span>收货城市分布</span><span class="panel-note">按订单收件地址统计</span></div>
          </template>
          <el-table v-loading="loading" :data="overview.cityStats || []" border height="360">
            <el-table-column label="城市" prop="cityName" min-width="150" show-overflow-tooltip />
            <el-table-column label="订单数" prop="orderCount" width="90" align="right" />
            <el-table-column label="租金" prop="rentAmount" width="120" align="right">
              <template #default="{ row }">{{ money(row.rentAmount) }}</template>
            </el-table-column>
            <el-table-column label="已完成" prop="completedOrderCount" width="90" align="right" />
            <el-table-column label="逾期" prop="overdueOrderCount" width="80" align="right">
              <template #default="{ row }"><span :class="{ 'danger-text': Number(row.overdueOrderCount) > 0 }">{{ row.overdueOrderCount || 0 }}</span></template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && !(overview.cityStats || []).length" description="暂无城市分布数据" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="operations-panel">
          <template #header>
            <div class="panel-heading"><span>商品型号热度</span><span class="panel-note">按订单明细统计</span></div>
          </template>
          <el-table v-loading="loading" :data="overview.modelStats || []" border height="360">
            <el-table-column label="型号" prop="modelName" min-width="180" show-overflow-tooltip />
            <el-table-column label="订单数" prop="orderCount" width="90" align="right" />
            <el-table-column label="数量" prop="quantity" width="80" align="right" />
            <el-table-column label="已完成" prop="completedOrderCount" width="90" align="right" />
            <el-table-column label="逾期" prop="overdueOrderCount" width="80" align="right">
              <template #default="{ row }"><span :class="{ 'danger-text': Number(row.overdueOrderCount) > 0 }">{{ row.overdueOrderCount || 0 }}</span></template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && !(overview.modelStats || []).length" description="暂无商品型号数据" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="operations-panel merchant-panel">
      <template #header>
        <div class="panel-heading"><span>商户经营排行</span><span class="panel-note">按周期订单数降序</span></div>
      </template>
      <el-table v-loading="loading" :data="overview.merchants || []" border>
        <el-table-column label="商户名称" prop="merchantName" min-width="180" show-overflow-tooltip />
        <el-table-column label="商户编码" prop="memberCode" width="150" />
        <el-table-column label="订单数" prop="orderCount" width="100" align="right" />
        <el-table-column label="周期租金" prop="rentAmount" width="135" align="right"><template #default="{ row }">{{ money(row.rentAmount) }}</template></el-table-column>
        <el-table-column label="履约中" prop="activeOrderCount" width="100" align="right" />
        <el-table-column label="已完成" prop="completedOrderCount" width="100" align="right" />
        <el-table-column label="逾期" prop="overdueOrderCount" width="90" align="right"><template #default="{ row }"><span :class="{ 'danger-text': Number(row.overdueOrderCount) > 0 }">{{ row.overdueOrderCount || 0 }}</span></template></el-table-column>
        <el-table-column label="库存总量" prop="totalInventoryQuantity" width="100" align="right" />
        <el-table-column label="可用库存" prop="availableInventoryQuantity" width="100" align="right" />
      </el-table>
      <el-empty v-if="!loading && !(overview.merchants || []).length" description="暂无商户经营数据" />
    </el-card>

    <el-alert class="page-alert" title="城市与型号报表基于订单收件人历史快照和订单明细汇总；未解析地址或缺少型号的订单会归入对应的未解析分组。" type="info" :closable="false" show-icon />
  </div>
</template>

<script setup name="PlatformOperationsAnalytics">
import { computed, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageHeading from '@/components/PageHeading.vue'
import { acknowledgePlatformRisk, getPlatformOperationsOverview } from '@/api/rental/operations'

const loading = ref(false)
const acknowledgingRiskId = ref(null)
const route = useRoute()
const selectedRiskId = computed(() => Number(route.query.riskEventId || 0))
const days = ref(30)
const overview = reactive({ beginDate: '', endDate: '', summary: {}, trends: [], merchants: [], cityStats: [], modelStats: [], risks: [] })
const dayOptions = [
  { value: 7, label: '近 7 天' },
  { value: 15, label: '近 15 天' },
  { value: 30, label: '近 30 天' },
  { value: 90, label: '近 90 天' }
]

const summaryCards = computed(() => {
  const summary = overview.summary || {}
  return [
    { key: 'orders', label: '周期订单', value: `${summary.orderCount || 0} 单`, help: `统计周期内创建的订单`, tone: 'blue' },
    { key: 'rent', label: '周期租金', value: money(summary.rentAmount), help: '统计周期内订单租金', tone: 'green' },
    { key: 'active', label: '履约中订单', value: `${summary.activeOrderCount || 0} 单`, help: '当前仍在履约流程中的订单', tone: 'blue' },
    { key: 'overdue', label: '逾期订单', value: `${summary.overdueOrderCount || 0} 单`, help: '需要运营跟进的逾期订单', tone: 'danger' },
    { key: 'inventory', label: '库存总量', value: `${summary.totalInventoryQuantity || 0} 件`, help: '有效商户库存合计', tone: 'blue' },
    { key: 'available', label: '可用库存', value: `${summary.availableInventoryQuantity || 0} 件`, help: '当前可用于接单的库存', tone: 'green' },
    { key: 'riskMerchants', label: '低库存商户', value: `${summary.lowInventoryMerchantCount || 0} 家`, help: '可用库存不超过总量10%的商户', tone: 'warning' },
    { key: 'finance', label: '财务异常', value: `${summary.abnormalLedgerCount || 0} 条`, help: '异常财务流水数量', tone: 'danger' }
  ]
})

async function loadData() {
  loading.value = true
  try {
    const response = await getPlatformOperationsOverview({ days: days.value })
    Object.assign(overview, response.data || { beginDate: '', endDate: '', summary: {}, trends: [], merchants: [], cityStats: [], modelStats: [], risks: [] })
  } catch (error) {
    ElMessage.error(error?.message || '经营分析加载失败')
  } finally {
    loading.value = false
  }
}

async function acknowledgeRisk(risk) {
  if (!risk?.riskEventId) return
  acknowledgingRiskId.value = risk.riskEventId
  try {
    await acknowledgePlatformRisk(risk.riskEventId)
    ElMessage.success('风险已确认')
    await loadData()
  } catch (error) {
    ElMessage.error(error?.message || '风险确认失败')
  } finally {
    acknowledgingRiskId.value = null
  }
}

function money(value) {
  const amount = Number(value || 0)
  return `¥${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function riskTagType(level) {
  return { 0: 'success', 1: 'info', 2: 'warning', 3: 'danger' }[Number(level)] || 'info'
}

function riskLevelName(level) {
  return { 0: '正常', 1: '提示', 2: '预警', 3: '高风险' }[Number(level)] || '未知等级'
}

function riskTypeName(type) {
  return { 1: '库存', 2: '订单', 3: '财务', 4: '工单' }[Number(type)] || '其他'
}

loadData()
</script>

<style scoped>
.filter-panel { margin-bottom: 16px; }
.filter-panel :deep(.el-form-item) { margin-bottom: 0; }
.days-select { width: 140px; }
.date-range { color: var(--el-text-color-secondary); line-height: 32px; }
.summary-grid { margin-bottom: 16px; }
.summary-card { min-height: 126px; }
.summary-label { color: var(--el-text-color-secondary); font-size: 14px; }
.summary-value { margin-top: 10px; font-size: 26px; font-weight: 600; line-height: 1.2; }
.summary-help { margin-top: 10px; color: var(--el-text-color-secondary); font-size: 12px; }
.summary-value.blue { color: var(--el-color-primary); }
.summary-value.green { color: var(--el-color-success); }
.summary-value.warning { color: var(--el-color-warning); }
.summary-value.danger, .danger-text { color: var(--el-color-danger); }
.content-grid { margin-bottom: 16px; }
.analytics-grid { margin-bottom: 16px; }
.operations-panel { height: 100%; }
.panel-heading { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.panel-note { color: var(--el-text-color-secondary); font-size: 12px; font-weight: 400; }
.risk-list { max-height: 420px; overflow-y: auto; }
.risk-item { padding: 12px 0; border-bottom: 1px solid var(--el-border-color-lighter); }
.risk-item--selected { margin: 0 -12px; padding-left: 12px; padding-right: 12px; background: var(--el-fill-color-light); border-radius: 4px; }
.risk-item:first-child { padding-top: 0; }
.risk-item:last-child { padding-bottom: 0; border-bottom: 0; }
.risk-item__top { display: flex; align-items: center; gap: 8px; min-width: 0; }
.risk-type { color: var(--el-text-color-secondary); font-size: 13px; }
.risk-item__top strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.risk-count { margin-left: auto; color: var(--el-text-color-primary); font-weight: 600; }
.risk-detail { margin-top: 8px; color: var(--el-text-color-secondary); font-size: 13px; line-height: 1.5; }
.risk-item__bottom { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-top: 6px; min-height: 24px; }
.risk-time { color: var(--el-text-color-secondary); font-size: 12px; }
.merchant-panel { margin-bottom: 16px; }
.page-alert { margin-top: 16px; }
</style>
