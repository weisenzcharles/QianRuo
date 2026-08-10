<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch">
      <el-form-item label="商户编码" prop="memberCode">
        <el-input v-model="queryParams.memberCode" placeholder="请输入商户编码" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="店铺名称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="请输入店铺名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="企业名称" prop="legalSubjectName">
        <el-input v-model="queryParams.legalSubjectName" placeholder="请输入企业名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="商户类型" prop="memberType">
        <el-select v-model="queryParams.memberType" placeholder="商户类型" clearable style="width: 150px">
          <el-option v-for="item in rentalMemberTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="认证状态" prop="authStatus">
        <el-select v-model="queryParams.authStatus" placeholder="认证状态" clearable style="width: 150px">
          <el-option v-for="item in rentalAuthStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="冻结状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="冻结状态" clearable style="width: 140px">
          <el-option label="未冻结" value="0" />
          <el-option label="已冻结" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['platform:member:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['platform:member:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['platform:member:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['platform:member:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="memberList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="商户ID" align="center" prop="memberId" width="90" />
      <el-table-column label="租户ID" align="center" prop="tenantId" width="90" />
      <el-table-column label="商户编码" align="center" prop="memberCode" min-width="140" />
      <el-table-column label="店铺/企业" min-width="220" show-overflow-tooltip>
        <template #default="scope">
          <div class="member-name">
            <span>{{ scope.row.nickname || '-' }}</span>
            <small>{{ scope.row.legalSubjectName || '-' }}</small>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="主账号" align="center" prop="mainAccount" width="140" show-overflow-tooltip />
      <el-table-column label="类型" align="center" prop="memberType" width="90">
        <template #default="scope">
          <el-tag :type="findRentalOption(rentalMemberTypeOptions, scope.row.memberType).tagType">
            {{ findRentalOption(rentalMemberTypeOptions, scope.row.memberType).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="认证" align="center" prop="authStatus" width="100">
        <template #default="scope">
          <el-tag :type="findRentalOption(rentalAuthStatusOptions, scope.row.authStatus).tagType">
            {{ findRentalOption(rentalAuthStatusOptions, scope.row.authStatus).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="联系人" align="center" prop="contactName" width="110" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" width="140" />
      <el-table-column label="会员等级" align="center" prop="vipLevel" width="100">
        <template #default="scope">
          <el-tag :type="findRentalOption(rentalVipLevelOptions, scope.row.vipLevel).tagType">
            {{ findRentalOption(rentalVipLevelOptions, scope.row.vipLevel).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="冻结状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === '1' ? 'danger' : 'success'">{{ scope.row.status === '1' ? '已冻结' : '未冻结' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="250" align="center" fixed="right" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['platform:member:query']">详情</el-button>
          <el-button v-if="scope.row.authStatus === 'pending'" link type="success" icon="Checked" @click="handleAudit(scope.row)" v-hasPermi="['platform:member:audit']">审核</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['platform:member:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['platform:member:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="min(1040px, 96vw)" top="4vh" append-to-body destroy-on-close>
      <member-editor ref="memberEditorRef" v-model="form" />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <member-detail-drawer v-model="detailOpen" :member="detail" />

    <el-dialog title="审核商户申请" v-model="auditOpen" width="520px" append-to-body destroy-on-close>
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="92px">
        <el-form-item label="店铺名称">
          <el-input :model-value="auditTarget?.nickname" disabled />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input :model-value="auditTarget?.contactPhone" disabled />
        </el-form-item>
        <el-form-item label="审核结果" prop="authStatus">
          <el-radio-group v-model="auditForm.authStatus">
            <el-radio value="verified">审核通过</el-radio>
            <el-radio value="rejected">审核驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="会员类型" prop="membershipType">
          <el-select v-model="auditForm.membershipType" style="width: 100%" placeholder="请选择审核通过后的会员类型">
            <el-option v-for="item in rentalMembershipTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="接单资格" prop="qualificationCodes">
          <el-checkbox-group v-model="auditForm.qualificationCodes">
            <el-checkbox v-for="item in rentalQualificationOptions" :key="item.value" :label="item.value">{{ item.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditRemark">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="驳回时必须填写具体原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditOpen = false">取消</el-button>
        <el-button type="primary" :loading="auditing" @click="submitAudit">确认审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RentalMember">
import { addMember, auditMember, delMember, getCertificationHistory, getMember, getMemberQualifications, getMembershipHistory, getQualificationHistory, listMember, updateMember } from '@/api/rental/member'
import MemberDetailDrawer from './components/MemberDetailDrawer.vue'
import MemberEditor from './components/MemberEditor.vue'
import { findRentalOption, rentalAuthStatusOptions, rentalMemberTypeOptions, rentalMembershipTypeOptions, rentalQualificationOptions, rentalVipLevelOptions } from '@/utils/rental/options'

const { proxy } = getCurrentInstance()
const memberEditorRef = ref()
const memberList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const detail = ref(null)
const auditOpen = ref(false)
const auditing = ref(false)
const auditTarget = ref(null)
const auditFormRef = ref()
const auditForm = reactive({ authStatus: 'verified', auditRemark: '', membershipType: '', qualificationCodes: [] })
const auditRules = {
  authStatus: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
  auditRemark: [{ validator: validateAuditRemark, trigger: 'blur' }]
}
const loading = ref(true)
const submitting = ref(false)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const form = ref(defaultForm())
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  memberCode: undefined,
  nickname: undefined,
  memberType: undefined,
  authStatus: undefined,
  legalSubjectName: undefined,
  status: undefined
})

/** 加载平台商户分页列表，并在请求结束后恢复表格加载状态。 */
async function getList() {
  loading.value = true
  try {
    const response = await listMember(queryParams)
    memberList.value = response.rows
    total.value = response.total
  } finally {
    loading.value = false
  }
}

/**
 * 创建平台商户编辑表单默认值，新增商户统一从待审核状态开始。
 *
 * @returns {object} 商户表单默认值
 */
function defaultForm() {
  return {
    memberId: undefined,
    memberCode: undefined,
    mainAccount: undefined,
    memberType: 'personal',
    membershipType: 'personal_member',
    authStatus: 'pending',
    legalSubjectName: undefined,
    licenseNo: undefined,
    nickname: undefined,
    contactName: undefined,
    contactPhone: undefined,
    servicePhone: undefined,
    contactEmail: undefined,
    alipayRealName: undefined,
    alipayAccount: undefined,
    storeLogoUrl: undefined,
    storeDescription: undefined,
    registeredCapital: undefined,
    businessScope: undefined,
    licenseAddress: undefined,
    licenseValidFrom: undefined,
    licenseValidTo: undefined,
    licenseLongTerm: 0,
    licenseImageUrl: undefined,
    legalRepresentativeName: undefined,
    legalRepresentativeIdNo: undefined,
    legalIdFrontUrl: undefined,
    legalIdBackUrl: undefined,
    signingDate: undefined,
    contractValidFrom: undefined,
    contractValidTo: undefined,
    contractFileUrl: undefined,
    auditTime: undefined,
    auditRemark: undefined,
    freezeTime: undefined,
    freezeReason: undefined,
    vipLevel: 'free',
    storageQuotaMb: 1024,
    externalAcceptEnabled: 0,
    status: '0',
    remark: undefined
  }
}

/** 重置商户编辑数据和子表单校验状态。 */
function reset() {
  form.value = defaultForm()
  nextTick(() => memberEditorRef.value?.resetFields())
}

/** 关闭商户编辑弹窗并清理未提交的表单数据。 */
function cancel() {
  open.value = false
  reset()
}

/** 从第一页重新执行当前商户筛选条件。 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 清空商户查询条件并重新加载第一页。 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

/**
 * 同步表格选中商户主键及批量操作按钮状态。
 *
 * @param {Array<object>} selection 当前选中的商户行
 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.memberId)
  single.value = selection.length !== 1
  multiple.value = selection.length === 0
}

/** 打开新增商户弹窗，并使用服务端认可的待审核默认状态。 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '新增商户'
}

/**
 * 加载选中商户详情并打开资料编辑弹窗。
 *
 * @param {object} row 当前商户行，未传入时使用表格单选结果
 */
async function handleUpdate(row = {}) {
  reset()
  const memberId = row.memberId ?? ids.value[0]
  const response = await getMember(memberId)
  form.value = { ...defaultForm(), ...response.data }
  open.value = true
  title.value = '修改商户'
}

/**
 * 加载商户完整资料并打开只读详情抽屉。
 *
 * @param {object} row 当前商户行
 */
async function handleDetail(row) {
  const [memberResponse, certificationResponse, qualificationResponse, membershipHistoryResponse, qualificationHistoryResponse] = await Promise.all([
    getMember(row.memberId),
    getCertificationHistory(row.memberId),
    getMemberQualifications(row.memberId),
    getMembershipHistory(row.memberId),
    getQualificationHistory(row.memberId)
  ])
  detail.value = {
    ...memberResponse.data,
    certificationHistory: certificationResponse.data || [],
    qualifications: qualificationResponse.data || [],
    membershipHistory: membershipHistoryResponse.data || [],
    qualificationHistory: qualificationHistoryResponse.data || []
  }
  detailOpen.value = true
}

/** 校验并新增或修改商户资料，成功后关闭弹窗并刷新列表。 */
async function submitForm() {
  const valid = await memberEditorRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (form.value.memberId !== undefined) {
      await updateMember(form.value)
      proxy.$modal.msgSuccess('修改成功')
    } else {
      await addMember(form.value)
      proxy.$modal.msgSuccess('新增成功')
    }
    open.value = false
    await getList()
  } finally {
    submitting.value = false
  }
}

/**
 * 确认后删除当前商户或表格批量选中的商户记录。
 *
 * @param {object} row 当前商户行，未传入时使用批量选中结果
 */
function handleDelete(row = {}) {
  const memberIds = row.memberId ?? ids.value
  proxy.$modal.confirm(`是否确认删除商户编号为"${memberIds}"的数据项？`).then(() => delMember(memberIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 使用当前筛选条件导出平台商户列表。 */
function handleExport() {
  proxy.download('/admin/rental/member/export', { ...queryParams }, `rental_member_${Date.now()}.xlsx`)
}

/**
 * 初始化审核表单并打开审核弹窗，只允许待审核商户进入该流程。
 *
 * @param {object} row 待审核商户行
 */
async function handleAudit(row) {
  auditTarget.value = row
  auditForm.authStatus = 'verified'
  auditForm.auditRemark = ''
  auditForm.membershipType = row.membershipType || (row.memberType === 'enterprise' ? 'ordinary_enterprise_member' : 'personal_member')
  const response = await getMemberQualifications(row.memberId)
  auditForm.qualificationCodes = (response.data || [])
    .filter(item => item.qualificationStatus === 'enabled')
    .map(item => item.qualificationCode)
  auditOpen.value = true
  nextTick(() => auditFormRef.value?.clearValidate())
}

/**
 * 校验并提交独立审核命令，审核通过后提示管理员账号已同步开通。
 */
async function submitAudit() {
  const valid = await auditFormRef.value.validate().catch(() => false)
  if (!valid) return
  auditing.value = true
  try {
    await auditMember(auditTarget.value.memberId, { ...auditForm })
    proxy.$modal.msgSuccess(auditForm.authStatus === 'verified' ? '审核通过，商户管理员账号已开通' : '审核已驳回')
    auditOpen.value = false
    await getList()
  } finally {
    auditing.value = false
  }
}

/**
 * 校验审核意见：通过时可选，驳回时必须填写可操作的具体原因。
 *
 * @param {object} rule Element Plus 校验规则
 * @param {string} value 审核意见
 * @param {Function} callback 校验完成回调
 */
function validateAuditRemark(rule, value, callback) {
  if (auditForm.authStatus === 'rejected' && !value?.trim()) {
    callback(new Error('审核驳回时必须填写原因'))
    return
  }
  callback()
}

getList()
</script>

<style scoped>
.member-name {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.member-name small {
  color: #909399;
}

:deep(.el-dialog__body) {
  max-height: calc(100vh - 190px);
  overflow-y: auto;
}
</style>
