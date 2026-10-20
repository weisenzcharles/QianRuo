<template>
  <div class="app-container platform-after-sales-page">
    <PageHeading
      kicker="AFTER-SALES GOVERNANCE"
      title="售后治理"
      description="统一查看和处理所有商户售后案件，完整保留事件与证据记录"
    >
      <template #aside><el-button :icon="Refresh" :loading="loading" @click="getList">刷新</el-button></template>
    </PageHeading>

    <el-form ref="queryRef" :model="queryParams" :inline="true" class="filter-form" @submit.prevent>
      <el-form-item label="案件状态" prop="caseStatus">
        <el-select v-model="queryParams.caseStatus" clearable placeholder="全部状态" style="width: 130px">
          <el-option v-for="item in caseStatuses" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="案件/订单" prop="keyword">
        <el-input v-model.trim="queryParams.keyword" clearable placeholder="案件编号、订单编码或问题描述" style="width: 250px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="商户" prop="merchantKeyword">
        <el-input v-model.trim="queryParams.merchantKeyword" clearable placeholder="商户名称或编码" style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="RefreshRight" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="售后单号" prop="caseNo" width="220" show-overflow-tooltip />
      <el-table-column label="订单编码" prop="orderNo" width="180" show-overflow-tooltip />
      <el-table-column label="商户" prop="merchantName" min-width="170" show-overflow-tooltip />
      <el-table-column label="售后类型" prop="caseTypeName" width="100" />
      <el-table-column label="状态" prop="caseStatusName" width="100" align="center">
        <template #default="{ row }"><el-tag :type="statusMeta(row.caseStatus).type">{{ row.caseStatusName || statusMeta(row.caseStatus).label }}</el-tag></template>
      </el-table-column>
      <el-table-column label="问题描述" prop="problemDesc" min-width="220" show-overflow-tooltip />
      <el-table-column label="赔付金额" prop="compensationAmount" width="110" align="right">
        <template #default="{ row }">{{ row.compensationAmount == null ? '-' : `¥${row.compensationAmount}` }}</template>
      </el-table-column>
      <el-table-column label="平台工单" prop="platformTicketNo" width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ row.platformTicketNo || '-' }}</template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="175" />
      <el-table-column label="操作" width="130" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button v-if="Number(row.caseStatus) === 0" v-hasPermi="['platform:afterSales:process']" link type="primary" @click="openProcess(row)">处理</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    <el-empty v-if="!loading && rows.length === 0" description="暂无售后案件" />

    <el-drawer v-model="detailVisible" title="售后案件详情" size="760px" destroy-on-close>
      <template v-if="detail?.afterSales">
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="售后单号">{{ detail.afterSales.caseNo }}</el-descriptions-item>
          <el-descriptions-item label="订单编码">{{ detail.afterSales.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="商户">{{ detail.afterSales.merchantName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="售后类型">{{ detail.afterSales.caseTypeName }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="statusMeta(detail.afterSales.caseStatus).type">{{ detail.afterSales.caseStatusName }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="是否联系客户">{{ Number(detail.afterSales.customerContacted) === 1 ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="赔付金额">{{ detail.afterSales.compensationAmount == null ? '-' : `¥${detail.afterSales.compensationAmount}` }}</el-descriptions-item>
          <el-descriptions-item label="平台工单">{{ detail.afterSales.platformTicketNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.afterSales.createTime }}</el-descriptions-item>
          <el-descriptions-item label="问题描述" :span="2">{{ detail.afterSales.problemDesc || '-' }}</el-descriptions-item>
          <el-descriptions-item label="丢失物品" :span="2"><pre class="json-value">{{ detail.afterSales.lostItemsJson || '-' }}</pre></el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.afterSales.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">处理时间线</el-divider>
        <el-timeline v-if="detail.events?.length">
          <el-timeline-item v-for="(event, index) in detail.events" :key="`${event.createTime}-${index}`" :timestamp="event.createTime" placement="top">
            <strong>{{ event.eventTypeName }}</strong><span class="timeline-operator">{{ event.operatorName || '系统' }}</span>
            <p>{{ event.content }}</p>
            <small v-if="event.toStatusName">状态：{{ event.fromStatusName || '新建' }} → {{ event.toStatusName }}</small>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无处理记录" />
        <el-divider content-position="left">证据记录</el-divider>
        <el-form
          v-if="Number(detail.afterSales.caseStatus) === 0"
          v-hasPermi="['platform:afterSales:process']"
          :model="evidenceForm"
          label-width="90px"
          class="evidence-form">
          <el-form-item label="证据类型" required>
            <el-select v-model="evidenceForm.evidenceType" style="width: 180px">
              <el-option v-for="item in evidenceTypes" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="证据文件" required>
            <FileUpload
              v-model="evidenceForm.fileUrl"
              business-type="after-sales"
              :limit="1"
              :file-size="20"
              :file-type="['jpg', 'jpeg', 'png', 'mp4', 'pdf', 'doc', 'docx']"
              :is-show-tip="false" />
          </el-form-item>
          <el-form-item label="证据说明">
            <el-input v-model.trim="evidenceForm.description" maxlength="1000" show-word-limit placeholder="补充说明证据内容" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="evidenceSaving" @click="submitEvidence">保存证据</el-button>
          </el-form-item>
        </el-form>
        <el-table v-if="detail.evidence?.length" :data="detail.evidence" border size="small">
          <el-table-column label="类型" prop="evidenceTypeName" width="90" />
          <el-table-column label="文件" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <el-link v-if="row.fileUrl" :href="row.fileUrl" target="_blank" type="primary">
                {{ row.fileName || row.fileUrl }}
              </el-link>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="说明" prop="description" min-width="180" show-overflow-tooltip />
          <el-table-column label="上传人" prop="operatorName" width="110" />
          <el-table-column label="时间" prop="createTime" width="175" />
        </el-table>
        <el-empty v-else description="暂无证据记录" />
      </template>
      <el-skeleton v-else :rows="10" animated />
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="detail?.afterSales && Number(detail.afterSales.caseStatus) === 0" v-hasPermi="['platform:afterSales:process']" type="primary" @click="openProcess(detail.afterSales)">追加处理</el-button>
      </template>
    </el-drawer>

    <el-dialog v-model="processVisible" title="处理售后案件" width="600px" append-to-body destroy-on-close>
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="90px">
        <el-form-item label="售后单号"><span>{{ currentCase?.caseNo || '-' }}</span></el-form-item>
        <el-form-item label="更新状态" prop="toStatus">
          <el-select v-model="processForm.toStatus" style="width: 100%">
            <el-option v-for="item in caseStatuses" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明" prop="content"><el-input v-model.trim="processForm.content" type="textarea" :rows="5" maxlength="2000" show-word-limit placeholder="请填写联系客户、核损、赔付或完结说明" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="processVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitProcess">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="PlatformAfterSales">
import { getCurrentInstance, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, RefreshRight, Search } from '@element-plus/icons-vue'
import PageHeading from '@/components/PageHeading.vue'
import { appendPlatformAfterSalesEvent, appendPlatformAfterSalesEvidence, getPlatformAfterSales, listPlatformAfterSales } from '@/api/rental/afterSales'

const route = useRoute()
const { proxy } = getCurrentInstance()
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const rows = ref([])
const detail = ref(null)
const currentCase = ref(null)
const detailVisible = ref(false)
const processVisible = ref(false)
const evidenceSaving = ref(false)
const processFormRef = ref()
const queryParams = reactive({ pageNum: 1, pageSize: 10, caseStatus: undefined, keyword: route.query.caseNo ? String(route.query.caseNo) : undefined, merchantKeyword: undefined })
const processForm = reactive({ toStatus: 0, content: '' })
const evidenceForm = reactive({ evidenceType: 1, fileUrl: '', description: '' })
const caseStatuses = [{ value: 0, label: '处理中' }, { value: 1, label: '已完成' }, { value: 2, label: '已关闭' }]
const evidenceTypes = [{ value: 1, label: '图片' }, { value: 2, label: '视频' }, { value: 3, label: '文件' }, { value: 9, label: '其他' }]
const processRules = { toStatus: [{ required: true, message: '请选择案件状态', trigger: 'change' }], content: [{ required: true, message: '请输入处理说明', trigger: 'blur' }] }

function statusMeta(value) { return ({ 0: { label: '处理中', type: 'warning' }, 1: { label: '已完成', type: 'success' }, 2: { label: '已关闭', type: 'info' } })[Number(value)] || { label: '未知状态', type: 'info' } }
function getList() { loading.value = true; listPlatformAfterSales({ ...queryParams }).then(response => { rows.value = response.rows || []; total.value = Number(response.total || 0) }).finally(() => { loading.value = false }) }
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function openDetail(row) {
  detailVisible.value = true
  detail.value = null
  resetEvidenceForm()
  getPlatformAfterSales(row.caseId).then(response => { detail.value = response.data || null })
}
function openProcess(row) { currentCase.value = row; Object.assign(processForm, { toStatus: Number(row.caseStatus ?? 0), content: '' }); processVisible.value = true }
async function submitProcess() { if (!await processFormRef.value.validate().catch(() => false)) return; saving.value = true; try { await appendPlatformAfterSalesEvent(currentCase.value.caseId, { ...processForm }); ElMessage.success('售后处理已记录'); processVisible.value = false; getList(); if (detailVisible.value && detail.value?.afterSales?.caseId === currentCase.value.caseId) openDetail(currentCase.value) } finally { saving.value = false } }
function resetEvidenceForm() { Object.assign(evidenceForm, { evidenceType: 1, fileUrl: '', description: '' }) }
async function submitEvidence() {
  if (!detail.value?.afterSales || !evidenceForm.fileUrl) {
    ElMessage.warning('请先上传证据文件')
    return
  }
  evidenceSaving.value = true
  try {
    await appendPlatformAfterSalesEvidence(detail.value.afterSales.caseId, { ...evidenceForm })
    ElMessage.success('售后证据已保存')
    openDetail(detail.value.afterSales)
  } finally {
    evidenceSaving.value = false
  }
}

getList()
</script>

<style scoped>
.filter-form { margin-bottom: 12px; }
.timeline-operator { margin-left: 12px; color: var(--el-text-color-secondary); }
.el-timeline p { margin: 8px 0 4px; white-space: pre-wrap; }
.json-value { margin: 0; white-space: pre-wrap; word-break: break-all; font: inherit; }
.evidence-form { padding: 0 4px 8px; }
</style>
