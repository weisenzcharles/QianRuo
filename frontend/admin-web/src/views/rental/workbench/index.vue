<template>
  <div class="app-container platform-workbench-page">
    <PageHeading
      kicker="OPERATIONS WORKBENCH"
      title="通用工作台"
      description="集中查看订单履约、售后纠纷、物流治理、库存告警和平台审核事项"
    >
      <template #aside>
        <span v-if="overview.generatedAt" class="generated-time">更新于 {{ overview.generatedAt }}</span>
        <el-button :icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
      </template>
    </PageHeading>

    <el-row v-loading="loading" :gutter="16" class="summary-grid">
      <el-col v-for="item in summaryCards" :key="item.key" :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value" :class="item.tone">{{ item.value }}</div>
          <div class="summary-help">{{ item.help }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-alert
      v-if="overview.executionAvailable === false"
      class="page-alert"
      title="租赁业务服务暂不可用，订单履约、售后、物流和库存待办暂未计入汇总。"
      type="warning"
      :closable="false"
      show-icon
    />

    <el-card shadow="never" class="workbench-panel">
      <template #header>
        <div class="panel-heading">
          <span>待办事项</span>
          <span class="panel-note">按优先级和进入时间排序，最多展示 50 条</span>
        </div>
      </template>
      <el-table v-loading="loading" :data="overview.tasks || []" border>
        <el-table-column label="类型" prop="taskTypeName" width="110" />
        <el-table-column label="优先级" prop="priorityName" width="100" align="center">
          <template #default="{ row }"><el-tag :type="priorityType(row.priority)">{{ row.priorityName }}</el-tag></template>
        </el-table-column>
        <el-table-column label="事项" prop="subject" min-width="190" show-overflow-tooltip />
        <el-table-column label="商户" prop="merchantName" min-width="150" show-overflow-tooltip />
        <el-table-column label="说明" prop="detail" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">{{ formatExpressCompanyText(row.detail) }}</template>
        </el-table-column>
        <el-table-column label="状态" prop="taskStatusName" width="110" align="center">
          <template #default="{ row }"><el-tag effect="plain" :type="statusType(row.taskStatus)">{{ row.taskStatusName }}</el-tag></template>
        </el-table-column>
        <el-table-column label="进入时间" prop="createTime" width="175" />
        <el-table-column label="操作" width="90" fixed="right" align="center">
          <template #default="{ row }"><el-button link type="primary" @click="openTarget(row)">查看</el-button></template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !(overview.tasks || []).length" description="暂无待办事项" />
    </el-card>

    <el-alert
      class="page-alert"
      title="工作台只聚合当前业务待办，不改变风险、工单或提现自身的处理状态；具体处理请进入对应业务页面。"
      type="info"
      :closable="false"
      show-icon
    />
  </div>
</template>

<script setup name="PlatformWorkbench">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import PageHeading from '@/components/PageHeading.vue'
import { getPlatformWorkbenchOverview } from '@/api/rental/workbench'
import { formatExpressCompanyText } from '@/utils/rental/expressCompany'

const router = useRouter()
const loading = ref(false)
const overview = reactive({ generatedAt: '', totalTaskCount: 0, riskTaskCount: 0, ticketTaskCount: 0, withdrawalTaskCount: 0, invoiceTaskCount: 0, marketReportTaskCount: 0, fulfillmentTaskCount: 0, afterSalesTaskCount: 0, logisticsTaskCount: 0, inventoryAlertTaskCount: 0, executionAvailable: null, pendingWithdrawalAmount: 0, tasks: [] })
const summaryCards = computed(() => [
  { key: 'total', label: '全部待办', value: `${overview.totalTaskCount || 0} 项`, help: '需要平台继续跟进的事项', tone: 'blue' },
  { key: 'fulfillment', label: '订单履约', value: `${overview.fulfillmentTaskCount || 0} 项`, help: '待发货、归还逾期等履约事项', tone: 'warning' },
  { key: 'afterSales', label: '售后纠纷', value: `${overview.afterSalesTaskCount || 0} 项`, help: '等待平台处理的售后案件', tone: 'danger' },
  { key: 'logistics', label: '物流异常', value: `${overview.logisticsTaskCount || 0} 项`, help: '未签收且需要关注的异常运单', tone: 'danger' },
  { key: 'inventoryAlert', label: '库存告警', value: `${overview.inventoryAlertTaskCount || 0} 项`, help: '尚未确认的库存告警事件', tone: 'warning' },
  { key: 'risk', label: '运营风险', value: `${overview.riskTaskCount || 0} 项`, help: '尚未解除的风险事件', tone: 'danger' },
  { key: 'ticket', label: '客服工单', value: `${overview.ticketTaskCount || 0} 项`, help: '尚未关闭的客服工单', tone: 'warning' },
  { key: 'withdrawal', label: '待审提现', value: `${overview.withdrawalTaskCount || 0} 笔`, help: `待审核金额 ${money(overview.pendingWithdrawalAmount)}`, tone: 'green' },
  { key: 'invoice', label: '发票处理', value: `${overview.invoiceTaskCount || 0} 笔`, help: '待审核或待开具的发票申请', tone: 'warning' },
  { key: 'marketReport', label: '市场举报', value: `${overview.marketReportTaskCount || 0} 项`, help: '等待平台处理的举报', tone: 'danger' }
])

async function loadData() {
  loading.value = true
  try {
    Object.assign(overview, (await getPlatformWorkbenchOverview()).data || {})
  } catch (error) {
    ElMessage.error(error?.message || '工作台加载失败')
  } finally {
    loading.value = false
  }
}

function openTarget(row) {
  if (!row?.targetPath) return
  router.push(row.targetPath).catch(() => ElMessage.error('目标页面打开失败'))
}

function priorityType(value) {
  return ({ 1: 'info', 2: 'warning', 3: 'danger' })[Number(value)] || 'info'
}

function statusType(value) {
  return ({ 0: 'warning', 1: 'primary', 2: 'info' })[Number(value)] || 'info'
}

function money(value) {
  return `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

loadData()
</script>

<style scoped>
.generated-time { margin-right: 12px; color: var(--el-text-color-secondary); font-size: 13px; }
.summary-grid { margin-bottom: 16px; }
.summary-card { min-height: 126px; }
.summary-label { color: var(--el-text-color-secondary); font-size: 14px; }
.summary-value { margin-top: 10px; font-size: 26px; font-weight: 600; line-height: 1.2; }
.summary-help { margin-top: 10px; color: var(--el-text-color-secondary); font-size: 12px; }
.summary-value.blue { color: var(--el-color-primary); }
.summary-value.green { color: var(--el-color-success); }
.summary-value.warning { color: var(--el-color-warning); }
.summary-value.danger { color: var(--el-color-danger); }
.workbench-panel { margin-bottom: 16px; }
.panel-heading { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.panel-note { color: var(--el-text-color-secondary); font-size: 12px; font-weight: 400; }
.page-alert { margin-top: 16px; }
</style>
