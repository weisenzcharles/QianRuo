<template>
  <div class="app-container platform-alert-page">
    <PageHeading
      kicker="SYSTEM ALERTS"
      title="系统告警"
      description="集中查看调度任务、外部接口、支付和存储异常，并保留确认与解除记录"
    >
      <template #aside>
        <el-button :icon="Refresh" :loading="loading" @click="getList">刷新</el-button>
      </template>
    </PageHeading>

    <el-form ref="queryRef" :model="queryParams" :inline="true" class="filter-form" @submit.prevent>
      <el-form-item label="告警类型" prop="alertType">
        <el-select v-model="queryParams.alertType" clearable placeholder="全部类型" style="width: 130px">
          <el-option v-for="item in alertTypes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="告警等级" prop="alertLevel">
        <el-select v-model="queryParams.alertLevel" clearable placeholder="全部等级" style="width: 120px">
          <el-option v-for="item in alertLevels" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="事件状态" prop="eventStatus">
        <el-select v-model="queryParams.eventStatus" clearable placeholder="全部状态" style="width: 120px">
          <el-option v-for="item in eventStatuses" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词" prop="keyword">
        <el-input v-model.trim="queryParams.keyword" clearable placeholder="标题、详情或来源" style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="RefreshRight" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      class="page-alert"
      title="当前已接入调度任务失败日志；支付、短信、存储和外部接口可以复用同一告警事件模型继续接入。"
      type="info"
      :closable="false"
      show-icon
    />

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="告警类型" prop="alertTypeName" width="110" />
      <el-table-column label="等级" prop="alertLevelName" width="95" align="center">
        <template #default="{ row }"><el-tag :type="levelType(row.alertLevel)">{{ row.alertLevelName }}</el-tag></template>
      </el-table-column>
      <el-table-column label="标题" prop="title" min-width="220" show-overflow-tooltip />
      <el-table-column label="详情" prop="detail" min-width="300" show-overflow-tooltip />
      <el-table-column label="来源" prop="sourceKey" min-width="180" show-overflow-tooltip />
      <el-table-column label="发生次数" prop="occurrenceCount" width="95" align="right" />
      <el-table-column label="状态" prop="eventStatusName" width="100" align="center">
        <template #default="{ row }"><el-tag effect="plain" :type="statusType(row.eventStatus)">{{ row.eventStatusName }}</el-tag></template>
      </el-table-column>
      <el-table-column label="最近发现" prop="lastSeenTime" width="170" />
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            v-if="Number(row.eventStatus) === 0"
            v-hasPermi="['platform:operations:alert:ack']"
            link
            type="primary"
            :loading="workingId === row.alertEventId"
            @click="acknowledge(row)"
          >确认已知</el-button>
          <el-button
            v-if="Number(row.eventStatus) !== 2"
            v-hasPermi="['platform:operations:alert:resolve']"
            link
            type="warning"
            @click="openResolve(row)"
          >解除</el-button>
          <span v-if="Number(row.eventStatus) === 2" class="resolved-text">已解除</span>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    <el-empty v-if="!loading && rows.length === 0" description="暂无运维告警" />

    <el-dialog v-model="resolveVisible" title="解除运维告警" width="520px" append-to-body destroy-on-close>
      <el-form ref="resolveFormRef" :model="resolveForm" :rules="resolveRules" label-width="90px">
        <el-form-item label="告警标题"><span>{{ currentRow?.title || '-' }}</span></el-form-item>
        <el-form-item label="解除说明" prop="remark">
          <el-input v-model="resolveForm.remark" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入解除原因或处理结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="resolve">确定解除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PlatformOperationsAlerts">
import { getCurrentInstance, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, RefreshRight, Search } from '@element-plus/icons-vue'
import PageHeading from '@/components/PageHeading.vue'
import { acknowledgePlatformAlert, listPlatformAlertEvents, resolvePlatformAlert } from '@/api/rental/operations'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const saving = ref(false)
const workingId = ref(null)
const total = ref(0)
const rows = ref([])
const currentRow = ref(null)
const resolveVisible = ref(false)
const resolveFormRef = ref()
const queryParams = reactive({ pageNum: 1, pageSize: 10, alertType: undefined, alertLevel: undefined, eventStatus: undefined, keyword: undefined })
const resolveForm = reactive({ remark: '' })
const alertTypes = [{ value: 1, label: '调度任务' }, { value: 2, label: '外部接口' }, { value: 3, label: '支付' }, { value: 4, label: '存储' }, { value: 5, label: '系统' }]
const alertLevels = [{ value: 1, label: '提示' }, { value: 2, label: '预警' }, { value: 3, label: '高风险' }]
const eventStatuses = [{ value: 0, label: '待处理' }, { value: 1, label: '已确认' }, { value: 2, label: '已解除' }]
const resolveRules = { remark: [{ required: true, message: '请输入解除说明', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listPlatformAlertEvents({ ...queryParams }).then(response => {
    rows.value = response.rows || []
    total.value = Number(response.total || 0)
  }).finally(() => { loading.value = false })
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

async function acknowledge(row) {
  if (!row?.alertEventId || workingId.value) return
  workingId.value = row.alertEventId
  try {
    await acknowledgePlatformAlert(row.alertEventId)
    ElMessage.success('告警已确认')
    getList()
  } finally {
    workingId.value = null
  }
}

function openResolve(row) {
  currentRow.value = row
  resolveForm.remark = ''
  resolveVisible.value = true
}

async function resolve() {
  if (!await resolveFormRef.value.validate().catch(() => false)) return
  saving.value = true
  try {
    await resolvePlatformAlert(currentRow.value.alertEventId, { remark: resolveForm.remark })
    ElMessage.success('告警已解除')
    resolveVisible.value = false
    getList()
  } finally {
    saving.value = false
  }
}

function levelType(value) { return ({ 1: 'info', 2: 'warning', 3: 'danger' })[Number(value)] || 'info' }
function statusType(value) { return ({ 0: 'warning', 1: 'primary', 2: 'success' })[Number(value)] || 'info' }

getList()
</script>

<style scoped>
.filter-form { margin-bottom: 12px; }
.page-alert { margin-bottom: 16px; }
.resolved-text { color: var(--el-text-color-secondary); font-size: 13px; }
</style>
