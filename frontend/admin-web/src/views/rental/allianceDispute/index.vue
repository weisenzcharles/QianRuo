<template>
  <div class="app-container">
    <el-form ref="queryRef" v-show="showSearch" :model="queryParams" :inline="true">
      <el-form-item label="纠纷状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="全部" @change="handleQuery">
          <el-option v-for="item in disputeStatuses" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" /></el-row>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="订单编码" prop="orderNo" width="180" align="center" />
      <el-table-column label="发起商户" prop="initiatorMerchantName" min-width="160" show-overflow-tooltip />
      <el-table-column label="被申请商户" prop="respondentMerchantName" min-width="160" show-overflow-tooltip />
      <el-table-column label="纠纷类型" width="120" align="center"><template #default="scope">{{ disputeTypeText(scope.row.disputeType) }}</template></el-table-column>
      <el-table-column label="申请赔付金额" prop="claimAmount" width="140" align="right" />
      <el-table-column label="状态" width="110" align="center"><template #default="scope"><el-tag :type="statusMeta(scope.row.disputeStatus).type">{{ statusMeta(scope.row.disputeStatus).label }}</el-tag></template></el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
      <el-table-column label="操作" width="100" fixed="right" align="center"><template #default="scope"><el-button link type="primary" @click="openDetail(scope.row)">处理</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog v-model="detailVisible" title="联盟纠纷处理" width="820px">
      <el-descriptions v-if="detail" :column="2" border label-width="120">
        <el-descriptions-item label="订单编码">{{ detail.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="纠纷类型">{{ disputeTypeText(detail.disputeType) }}</el-descriptions-item>
        <el-descriptions-item label="发起商户">{{ detail.initiatorMerchantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="被申请商户">{{ detail.respondentMerchantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMeta(detail.disputeStatus).label }}</el-descriptions-item>
        <el-descriptions-item label="申请赔付金额">{{ detail.claimAmount }}</el-descriptions-item>
        <el-descriptions-item label="纠纷说明" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">证据</el-divider>
      <el-table :data="detail?.evidence || []" border size="small">
        <el-table-column label="证据类型" width="110"><template #default="scope">{{ evidenceTypeText(scope.row.evidenceType) }}</template></el-table-column>
        <el-table-column label="内容" prop="content" min-width="280"><template #default="scope">{{ scope.row.content || scope.row.fileName || scope.row.fileUrl || '-' }}</template></el-table-column>
        <el-table-column label="提交时间" prop="createTime" width="180" />
      </el-table>
      <el-divider content-position="left">提交调解方案</el-divider>
      <el-form :model="mediationForm" label-width="110px">
        <el-form-item label="赔付金额"><el-input-number v-model="mediationForm.proposalAmount" :min="0" :precision="2" controls-position="right" /></el-form-item>
        <el-form-item label="调解方案"><el-input v-model="mediationForm.proposalContent" type="textarea" maxlength="2000" show-word-limit :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="detailVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="submitMediation">提交方案</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="PlatformAllianceDispute">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllianceDisputeDetail, listAllianceDisputes, proposeAllianceMediation } from '@/api/rental/allianceOrder'
import { ALLIANCE_DISPUTE_STATUS, ALLIANCE_DISPUTE_TYPE, ALLIANCE_EVIDENCE_TYPE } from '@/utils/rental/alliance'

const disputeStatuses = [
  { value: ALLIANCE_DISPUTE_STATUS.PENDING, label: '待处理' },
  { value: ALLIANCE_DISPUTE_STATUS.EVIDENCE, label: '举证中' },
  { value: ALLIANCE_DISPUTE_STATUS.MEDIATING, label: '调解中' },
  { value: ALLIANCE_DISPUTE_STATUS.CONFIRMING, label: '待确认' },
  { value: ALLIANCE_DISPUTE_STATUS.RESOLVED, label: '已解决' },
  { value: ALLIANCE_DISPUTE_STATUS.REJECTED, label: '已驳回' },
  { value: ALLIANCE_DISPUTE_STATUS.CLOSED, label: '已关闭' },
]
const disputeTypes = {
  [ALLIANCE_DISPUTE_TYPE.DAMAGE]: '损坏',
  [ALLIANCE_DISPUTE_TYPE.LOSS]: '丢失',
  [ALLIANCE_DISPUTE_TYPE.DELAYED_FULFILLMENT]: '延迟履约',
  [ALLIANCE_DISPUTE_TYPE.FEE]: '费用',
  [ALLIANCE_DISPUTE_TYPE.OTHER]: '其他',
}
const statusTypes = {
  [ALLIANCE_DISPUTE_STATUS.PENDING]: { label: '待处理', type: 'warning' },
  [ALLIANCE_DISPUTE_STATUS.EVIDENCE]: { label: '举证中', type: 'primary' },
  [ALLIANCE_DISPUTE_STATUS.MEDIATING]: { label: '调解中', type: 'warning' },
  [ALLIANCE_DISPUTE_STATUS.CONFIRMING]: { label: '待确认', type: 'warning' },
  [ALLIANCE_DISPUTE_STATUS.RESOLVED]: { label: '已解决', type: 'success' },
  [ALLIANCE_DISPUTE_STATUS.REJECTED]: { label: '已驳回', type: 'danger' },
  [ALLIANCE_DISPUTE_STATUS.CLOSED]: { label: '已关闭', type: 'info' },
}
const evidenceTypes = {
  [ALLIANCE_EVIDENCE_TYPE.IMAGE]: '图片',
  [ALLIANCE_EVIDENCE_TYPE.VIDEO]: '视频',
  [ALLIANCE_EVIDENCE_TYPE.FILE]: '文件',
  [ALLIANCE_EVIDENCE_TYPE.TEXT]: '文字',
}
const loading = ref(false)
const submitting = ref(false)
const showSearch = ref(true)
const rows = ref([])
const total = ref(0)
const queryParams = reactive({ status: undefined, pageNum: 1, pageSize: 10 })
const detailVisible = ref(false)
const detail = ref(null)
const mediationForm = reactive({ disputeId: null, proposalAmount: 0, proposalContent: '' })

function statusMeta(value) { return statusTypes[Number(value)] || { label: '未知状态', type: 'info' } }
function disputeTypeText(value) { return disputeTypes[Number(value)] || '未知' }
function evidenceTypeText(value) { return evidenceTypes[Number(value)] || '未知' }
function getList() {
  loading.value = true
  listAllianceDisputes(queryParams).then(response => { rows.value = response.rows || []; total.value = Number(response.total || 0) }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryParams.status = undefined; handleQuery() }
function openDetail(row) {
  getAllianceDisputeDetail(row.disputeId).then(response => {
    detail.value = response.data || null
    Object.assign(mediationForm, { disputeId: row.disputeId, proposalAmount: 0, proposalContent: '' })
    detailVisible.value = true
  })
}
function submitMediation() {
  if (!mediationForm.proposalContent.trim()) return ElMessage.warning('请填写调解方案')
  submitting.value = true
  proposeAllianceMediation({ ...mediationForm }).then(() => { detailVisible.value = false; getList(); ElMessage.success('调解方案已提交') }).finally(() => { submitting.value = false })
}
getList()
</script>
