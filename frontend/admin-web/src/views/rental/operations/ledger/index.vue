<template>
  <div class="app-container operations-ledger-page">
    <PageHeading
      kicker="OPERATIONS LEDGER"
      title="运维台账"
      description="追踪数据库备份、日志包和平台容量变化，避免用页面状态替代真实运维结果"
    >
      <template #aside>
        <el-button :icon="Refresh" :loading="loading" @click="refreshAll">刷新</el-button>
      </template>
    </PageHeading>

    <el-row :gutter="16" class="summary-grid">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="summary-card">
          <span class="summary-label">最近成功备份</span>
          <strong class="summary-value">{{ summary.latestSuccessfulBackup?.backupTypeName || '暂无记录' }}</strong>
          <span class="summary-help">{{ summary.latestSuccessfulBackup?.completedTime || '请登记真实备份结果' }}</span>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="summary-card">
          <span class="summary-label">备份成功</span>
          <strong class="summary-value success-text">{{ summary.backupSuccessCount || 0 }} 次</strong>
          <span class="summary-help">累计登记成功的备份记录</span>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="summary-card">
          <span class="summary-label">备份失败</span>
          <strong class="summary-value danger-text">{{ summary.backupFailureCount || 0 }} 次</strong>
          <span class="summary-help">需要补充处理说明的记录</span>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="summary-card">
          <span class="summary-label">已用 / 可用容量</span>
          <strong class="summary-value">{{ formatBytes(summary.latestUsedBytes) }} / {{ formatBytes(summary.latestAvailableBytes) }}</strong>
          <span class="summary-help">{{ summary.latestMeasuredTime || '暂无容量快照' }}</span>
        </el-card>
      </el-col>
    </el-row>

    <el-alert
      class="page-alert"
      title="每日数据库备份默认保留7天，月度日志包默认保留31天；备份文件由部署脚本或运维任务生成，本页只登记结果、失败原因和容量快照。"
      type="info"
      :closable="false"
      show-icon
    />

    <el-card shadow="never" class="ledger-card">
      <el-tabs v-model="activeTab" @tab-change="loadActiveTab">
        <el-tab-pane label="备份记录" name="backups">
          <el-form ref="backupQueryRef" :model="backupQuery" :inline="true" class="filter-form" @submit.prevent>
            <el-form-item label="备份类型" prop="backupType">
              <el-select v-model="backupQuery.backupType" clearable placeholder="全部类型" style="width: 160px">
                <el-option v-for="item in backupTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="backupStatus">
              <el-select v-model="backupQuery.backupStatus" clearable placeholder="全部状态" style="width: 130px">
                <el-option v-for="item in backupStatuses" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词" prop="keyword">
              <el-input v-model.trim="backupQuery.keyword" clearable placeholder="备份编码、对象键或文件名" style="width: 260px" @keyup.enter="handleBackupQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleBackupQuery">搜索</el-button>
              <el-button :icon="RefreshRight" @click="resetBackupQuery">重置</el-button>
            </el-form-item>
            <el-form-item class="toolbar-item">
              <el-button v-hasPermi="['platform:operations:backup:add']" type="primary" :icon="Plus" @click="openBackupDialog">登记备份结果</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="loadingBackups" :data="backupRows" border>
            <el-table-column label="备份编码" prop="backupNo" width="190" />
            <el-table-column label="备份类型" prop="backupTypeName" width="150" />
            <el-table-column label="状态" prop="backupStatusName" width="100" align="center">
              <template #default="{ row }"><el-tag :type="backupStatusType(row.backupStatus)" effect="plain">{{ row.backupStatusName }}</el-tag></template>
            </el-table-column>
            <el-table-column label="备份对象" prop="backupKey" min-width="230" show-overflow-tooltip />
            <el-table-column label="文件大小" prop="fileSizeBytes" width="115" align="right">
              <template #default="{ row }">{{ formatBytes(row.fileSizeBytes) }}</template>
            </el-table-column>
            <el-table-column label="开始时间" prop="startedTime" width="170" />
            <el-table-column label="完成时间" prop="completedTime" width="170" />
            <el-table-column label="保留至" prop="retentionUntil" width="170" />
            <el-table-column label="失败原因" prop="failureReason" min-width="180" show-overflow-tooltip />
          </el-table>
          <pagination v-show="backupTotal > 0" v-model:page="backupQuery.pageNum" v-model:limit="backupQuery.pageSize" :total="backupTotal" @pagination="loadBackups" />
          <el-empty v-if="!loadingBackups && backupRows.length === 0" description="暂无备份记录" />
        </el-tab-pane>

        <el-tab-pane label="容量流水" name="capacity">
          <el-form ref="capacityQueryRef" :model="capacityQuery" :inline="true" class="filter-form" @submit.prevent>
            <el-form-item label="容量范围" prop="scopeType">
              <el-select v-model="capacityQuery.scopeType" clearable placeholder="全部范围" style="width: 150px">
                <el-option v-for="item in scopeTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词" prop="keyword">
              <el-input v-model.trim="capacityQuery.keyword" clearable placeholder="范围名称、业务键或来源" style="width: 260px" @keyup.enter="handleCapacityQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleCapacityQuery">搜索</el-button>
              <el-button :icon="RefreshRight" @click="resetCapacityQuery">重置</el-button>
            </el-form-item>
            <el-form-item class="toolbar-item">
              <el-button v-hasPermi="['platform:operations:capacity:add']" type="primary" :icon="Plus" @click="openCapacityDialog">登记容量快照</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="loadingCapacity" :data="capacityRows" border>
            <el-table-column label="容量范围" prop="scopeTypeName" width="110" />
            <el-table-column label="范围名称" prop="scopeName" min-width="180" show-overflow-tooltip />
            <el-table-column label="总容量" prop="capacityBytes" width="125" align="right"><template #default="{ row }">{{ formatBytes(row.capacityBytes) }}</template></el-table-column>
            <el-table-column label="已使用" prop="usedBytes" width="125" align="right"><template #default="{ row }">{{ formatBytes(row.usedBytes) }}</template></el-table-column>
            <el-table-column label="可用" prop="availableBytes" width="125" align="right"><template #default="{ row }">{{ formatBytes(row.availableBytes) }}</template></el-table-column>
            <el-table-column label="变化" prop="changeTypeName" width="110" align="center">
              <template #default="{ row }"><span :class="changeClass(row.changeType)">{{ row.changeTypeName }}</span></template>
            </el-table-column>
            <el-table-column label="已用变化" prop="changedBytes" width="125" align="right"><template #default="{ row }">{{ signedBytes(row.changedBytes) }}</template></el-table-column>
            <el-table-column label="来源" prop="snapshotSource" width="140" show-overflow-tooltip />
            <el-table-column label="测量时间" prop="measuredTime" width="170" />
          </el-table>
          <pagination v-show="capacityTotal > 0" v-model:page="capacityQuery.pageNum" v-model:limit="capacityQuery.pageSize" :total="capacityTotal" @pagination="loadCapacity" />
          <el-empty v-if="!loadingCapacity && capacityRows.length === 0" description="暂无容量流水" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="backupVisible" title="登记备份结果" width="680px" append-to-body destroy-on-close>
      <el-form ref="backupFormRef" :model="backupForm" :rules="backupRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="备份类型" prop="backupType">
              <el-select v-model="backupForm.backupType" style="width: 100%">
                <el-option v-for="item in backupTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备份状态" prop="backupStatus">
              <el-select v-model="backupForm.backupStatus" style="width: 100%">
                <el-option v-for="item in backupStatuses" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="对象键 / 路径" prop="backupKey"><el-input v-model.trim="backupForm.backupKey" maxlength="255" placeholder="例如：backups/2026-09-28/ry_rental.sql.gz" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="文件名称"><el-input v-model.trim="backupForm.fileName" maxlength="255" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="文件大小"><el-input-number v-model="backupForm.fileSizeBytes" :min="0" :controls="false" style="width: 100%" /><span class="unit-help">字节</span></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="开始时间" prop="startedTime"><el-date-picker v-model="backupForm.startedTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="完成时间"><el-date-picker v-model="backupForm.completedTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="保留至"><el-date-picker v-model="backupForm.retentionUntil" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="失败原因" prop="failureReason"><el-input v-model.trim="backupForm.failureReason" type="textarea" :rows="3" maxlength="1000" show-word-limit placeholder="失败时填写处理原因；成功时可留空" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="backupVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitBackup">确定登记</el-button></template>
    </el-dialog>

    <el-dialog v-model="capacityVisible" title="登记容量快照" width="620px" append-to-body destroy-on-close>
      <el-form ref="capacityFormRef" :model="capacityForm" :rules="capacityRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="容量范围" prop="scopeType"><el-select v-model="capacityForm.scopeType" style="width: 100%"><el-option v-for="item in scopeTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来源" prop="snapshotSource"><el-input v-model.trim="capacityForm.snapshotSource" maxlength="100" placeholder="例如：对象存储监控" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="范围业务键" prop="scopeKey"><el-input v-model.trim="capacityForm.scopeKey" maxlength="100" placeholder="例如：ry-rental-db" /></el-form-item>
        <el-form-item label="范围名称" prop="scopeName"><el-input v-model.trim="capacityForm.scopeName" maxlength="200" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="总容量" prop="capacityBytes"><el-input-number v-model="capacityForm.capacityBytes" :min="0" :controls="false" style="width: 100%" /><span class="unit-help">字节</span></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="已使用" prop="usedBytes"><el-input-number v-model="capacityForm.usedBytes" :min="0" :controls="false" style="width: 100%" /><span class="unit-help">字节</span></el-form-item></el-col>
        </el-row>
        <el-form-item label="可用容量"><span class="calculated-value">{{ formatBytes(availableBytes) }}</span><span class="unit-help">由总容量减去已使用自动计算</span></el-form-item>
        <el-form-item label="测量时间" prop="measuredTime"><el-date-picker v-model="capacityForm.measuredTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="capacityVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitCapacity">确定登记</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="PlatformOperationsLedger">
import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, RefreshRight, Search } from '@element-plus/icons-vue'
import PageHeading from '@/components/PageHeading.vue'
import {
  getPlatformOperationsLedgerSummary,
  listPlatformBackups,
  listPlatformCapacity,
  registerPlatformBackup,
  registerPlatformCapacity
} from '@/api/rental/operations'

const { proxy } = getCurrentInstance()
const activeTab = ref('backups')
const loading = ref(false)
const loadingBackups = ref(false)
const loadingCapacity = ref(false)
const saving = ref(false)
const backupTotal = ref(0)
const capacityTotal = ref(0)
const backupRows = ref([])
const capacityRows = ref([])
const backupVisible = ref(false)
const capacityVisible = ref(false)
const backupFormRef = ref()
const capacityFormRef = ref()
const summary = reactive({ latestSuccessfulBackup: null, backupSuccessCount: 0, backupFailureCount: 0, latestUsedBytes: 0, latestAvailableBytes: 0, latestMeasuredTime: '' })
const backupQueryRef = ref()
const capacityQueryRef = ref()
const backupQuery = reactive({ pageNum: 1, pageSize: 10, backupType: undefined, backupStatus: undefined, keyword: undefined })
const capacityQuery = reactive({ pageNum: 1, pageSize: 10, scopeType: undefined, keyword: undefined })
const backupForm = reactive({ backupType: 1, backupStatus: 1, backupKey: '', fileName: '', fileSizeBytes: 0, startedTime: '', completedTime: '', retentionUntil: '', failureReason: '' })
const capacityForm = reactive({ scopeType: 1, scopeKey: '', scopeName: '', capacityBytes: 0, usedBytes: 0, snapshotSource: '', measuredTime: '' })
const backupTypes = [{ value: 1, label: '数据库每日备份' }, { value: 2, label: '月度日志包' }, { value: 3, label: '对象存储副本' }]
const backupStatuses = [{ value: 0, label: '执行中' }, { value: 1, label: '成功' }, { value: 2, label: '失败' }, { value: 3, label: '已过期' }]
const scopeTypes = [{ value: 1, label: '数据库' }, { value: 2, label: '对象存储' }, { value: 3, label: '文件存储' }]
const backupRules = { backupType: [{ required: true, message: '请选择备份类型', trigger: 'change' }], backupStatus: [{ required: true, message: '请选择备份状态', trigger: 'change' }], backupKey: [{ required: true, message: '请输入备份对象键或路径', trigger: 'blur' }], startedTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }], failureReason: [{ validator: (_rule, value, callback) => { if (backupForm.backupStatus === 2 && !value?.trim()) callback(new Error('备份失败时必须填写失败原因')); else callback() }, trigger: 'blur' }] }
const capacityRules = { scopeType: [{ required: true, message: '请选择容量范围', trigger: 'change' }], scopeKey: [{ required: true, message: '请输入范围业务键', trigger: 'blur' }], scopeName: [{ required: true, message: '请输入范围名称', trigger: 'blur' }], capacityBytes: [{ required: true, message: '请输入总容量', trigger: 'blur' }], usedBytes: [{ required: true, message: '请输入已使用容量', trigger: 'blur' }], snapshotSource: [{ required: true, message: '请输入快照来源', trigger: 'blur' }], measuredTime: [{ required: true, message: '请选择测量时间', trigger: 'change' }] }
const availableBytes = computed(() => Math.max(Number(capacityForm.capacityBytes || 0) - Number(capacityForm.usedBytes || 0), 0))

async function refreshAll() {
  loading.value = true
  try { await Promise.all([loadSummary(), loadBackups(), loadCapacity()]) } finally { loading.value = false }
}

async function loadSummary() {
  const response = await getPlatformOperationsLedgerSummary()
  Object.assign(summary, response.data || {})
}

async function loadBackups() {
  loadingBackups.value = true
  try { const response = await listPlatformBackups({ ...backupQuery }); backupRows.value = response.rows || []; backupTotal.value = Number(response.total || 0) } finally { loadingBackups.value = false }
}

async function loadCapacity() {
  loadingCapacity.value = true
  try { const response = await listPlatformCapacity({ ...capacityQuery }); capacityRows.value = response.rows || []; capacityTotal.value = Number(response.total || 0) } finally { loadingCapacity.value = false }
}

function loadActiveTab() { activeTab.value === 'backups' ? loadBackups() : loadCapacity() }
function handleBackupQuery() { backupQuery.pageNum = 1; loadBackups() }
function handleCapacityQuery() { capacityQuery.pageNum = 1; loadCapacity() }
function resetBackupQuery() { proxy.resetForm('backupQueryRef'); handleBackupQuery() }
function resetCapacityQuery() { proxy.resetForm('capacityQueryRef'); handleCapacityQuery() }

function openBackupDialog() { Object.assign(backupForm, { backupType: 1, backupStatus: 1, backupKey: '', fileName: '', fileSizeBytes: 0, startedTime: '', completedTime: '', retentionUntil: '', failureReason: '' }); backupVisible.value = true }
function openCapacityDialog() { Object.assign(capacityForm, { scopeType: 1, scopeKey: '', scopeName: '', capacityBytes: 0, usedBytes: 0, snapshotSource: '', measuredTime: '' }); capacityVisible.value = true }

async function submitBackup() {
  const valid = await backupFormRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try { await registerPlatformBackup({ ...backupForm }); ElMessage.success('备份记录已登记'); backupVisible.value = false; await Promise.all([loadSummary(), loadBackups()]) } finally { saving.value = false }
}

async function submitCapacity() {
  const valid = await capacityFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (Number(capacityForm.usedBytes) > Number(capacityForm.capacityBytes)) { ElMessage.warning('已使用容量不能大于总容量'); return }
  saving.value = true
  try { await registerPlatformCapacity({ ...capacityForm, availableBytes: availableBytes.value }); ElMessage.success('容量快照已登记'); capacityVisible.value = false; await Promise.all([loadSummary(), loadCapacity()]) } finally { saving.value = false }
}

function formatBytes(value) {
  const bytes = Number(value || 0)
  if (bytes < 1024) return `${bytes} B`
  const units = ['KB', 'MB', 'GB', 'TB', 'PB']
  let amount = bytes
  let index = -1
  while (amount >= 1024 && index < units.length - 1) { amount /= 1024; index += 1 }
  return `${amount.toFixed(amount >= 10 ? 1 : 2)} ${units[index]}`
}

function signedBytes(value) { const bytes = Number(value || 0); return `${bytes > 0 ? '+' : ''}${formatBytes(bytes)}` }
function backupStatusType(status) { return { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[Number(status)] || 'info' }
function changeClass(type) { return { 2: 'danger-text', 3: 'success-text' }[Number(type)] || '' }

onMounted(refreshAll)
</script>

<style scoped>
.summary-grid { margin-bottom: 16px; }
.summary-card { min-height: 126px; display: flex; flex-direction: column; justify-content: space-between; }
.summary-label, .summary-help { color: var(--el-text-color-secondary); font-size: 13px; }
.summary-value { display: block; margin: 12px 0 8px; color: var(--el-text-color-primary); font-size: 22px; line-height: 1.2; }
.success-text { color: var(--el-color-success); }
.danger-text { color: var(--el-color-danger); }
.page-alert { margin-bottom: 16px; }
.ledger-card { min-height: 520px; }
.filter-form { display: flex; flex-wrap: wrap; align-items: center; }
.filter-form :deep(.el-form-item) { margin-bottom: 12px; }
.toolbar-item { margin-left: auto; }
.unit-help { margin-left: 8px; color: var(--el-text-color-secondary); font-size: 12px; }
.calculated-value { color: var(--el-color-primary); font-weight: 600; }
</style>
