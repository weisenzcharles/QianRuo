<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="租户ID" prop="tenantId">
            <el-input
               v-model="queryParams.tenantId"
               placeholder="请输入租户ID"
               clearable
               style="width: 160px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="商户编码" prop="memberCode">
            <el-input
               v-model="queryParams.memberCode"
               placeholder="请输入商户编码"
               clearable
               style="width: 180px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="主体名称" prop="legalSubjectName">
            <el-input
               v-model="queryParams.legalSubjectName"
               placeholder="请输入主体名称"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="商户类型" prop="memberType">
            <el-select v-model="queryParams.memberType" placeholder="商户类型" clearable style="width: 160px">
               <el-option
                  v-for="item in rentalMemberTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="认证状态" prop="authStatus">
            <el-select v-model="queryParams.authStatus" placeholder="认证状态" clearable style="width: 160px">
               <el-option
                  v-for="item in rentalAuthStatusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="商户状态" clearable style="width: 140px">
               <el-option
                  v-for="dict in sys_normal_disable"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button
               type="primary"
               plain
               icon="Plus"
               @click="handleAdd"
               v-hasPermi="['rental:member:add']"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['rental:member:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['rental:member:remove']"
            >删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-hasPermi="['rental:member:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="memberList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="商户ID" align="center" prop="memberId" width="90" />
         <el-table-column label="租户ID" align="center" prop="tenantId" width="90" />
         <el-table-column label="商户编码" align="center" prop="memberCode" min-width="140" />
         <el-table-column label="主体/昵称" min-width="220" show-overflow-tooltip>
            <template #default="scope">
               <div class="member-name">
                  <span>{{ scope.row.legalSubjectName || '-' }}</span>
                  <small>{{ scope.row.nickname || '-' }}</small>
               </div>
            </template>
         </el-table-column>
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
         <el-table-column label="会员等级" align="center" prop="vipLevel" width="100">
            <template #default="scope">
               <el-tag :type="findRentalOption(rentalVipLevelOptions, scope.row.vipLevel).tagType">
                  {{ findRentalOption(rentalVipLevelOptions, scope.row.vipLevel).label }}
               </el-tag>
            </template>
         </el-table-column>
         <el-table-column label="联系人" align="center" prop="contactName" width="120" />
         <el-table-column label="联系电话" align="center" prop="contactPhone" width="140" />
         <el-table-column label="存储额度" align="center" prop="storageQuotaMb" width="110">
            <template #default="scope">
               <span>{{ formatQuota(scope.row.storageQuotaMb) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="对外接单" align="center" prop="externalAcceptEnabled" width="100">
            <template #default="scope">
               <el-tag :type="findRentalOption(rentalYesNoOptions, scope.row.externalAcceptEnabled).tagType">
                  {{ findRentalOption(rentalYesNoOptions, scope.row.externalAcceptEnabled).label }}
               </el-tag>
            </template>
         </el-table-column>
         <el-table-column label="状态" align="center" prop="status" width="90">
            <template #default="scope">
               <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" width="180" align="center" fixed="right" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['rental:member:query']">详情</el-button>
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['rental:member:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['rental:member:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />

      <el-dialog :title="title" v-model="open" width="720px" append-to-body>
         <el-form ref="memberRef" :model="form" :rules="rules" label-width="110px">
            <el-row :gutter="16">
               <el-col :span="12">
                  <el-form-item label="租户ID" prop="tenantId">
                     <el-input-number v-model="form.tenantId" controls-position="right" :min="1" style="width: 100%" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="商户编码" prop="memberCode">
                     <el-input v-model="form.memberCode" placeholder="请输入商户编码" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="商户类型" prop="memberType">
                     <el-select v-model="form.memberType" placeholder="请选择商户类型" style="width: 100%">
                        <el-option
                           v-for="item in rentalMemberTypeOptions"
                           :key="item.value"
                           :label="item.label"
                           :value="item.value"
                        />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="认证状态" prop="authStatus">
                     <el-select v-model="form.authStatus" placeholder="请选择认证状态" style="width: 100%">
                        <el-option
                           v-for="item in rentalAuthStatusOptions"
                           :key="item.value"
                           :label="item.label"
                           :value="item.value"
                        />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="主体名称" prop="legalSubjectName">
                     <el-input v-model="form.legalSubjectName" placeholder="请输入主体名称" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="主体编号" prop="licenseNo">
                     <el-input v-model="form.licenseNo" placeholder="营业执照/主体编号" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="商户昵称" prop="nickname">
                     <el-input v-model="form.nickname" placeholder="请输入商户昵称" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="联系人" prop="contactName">
                     <el-input v-model="form.contactName" placeholder="请输入联系人" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="联系电话" prop="contactPhone">
                     <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="会员等级" prop="vipLevel">
                     <el-select v-model="form.vipLevel" placeholder="请选择会员等级" style="width: 100%">
                        <el-option
                           v-for="item in rentalVipLevelOptions"
                           :key="item.value"
                           :label="item.label"
                           :value="item.value"
                        />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="存储额度MB" prop="storageQuotaMb">
                     <el-input-number v-model="form.storageQuotaMb" controls-position="right" :min="0" style="width: 100%" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="对外接单" prop="externalAcceptEnabled">
                     <el-switch
                        v-model="form.externalAcceptEnabled"
                        :active-value="1"
                        :inactive-value="0"
                        active-text="允许"
                        inactive-text="禁止"
                     />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="商户状态" prop="status">
                     <el-radio-group v-model="form.status">
                        <el-radio
                           v-for="dict in sys_normal_disable"
                           :key="dict.value"
                           :value="dict.value"
                        >{{ dict.label }}</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                     <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
                  </el-form-item>
               </el-col>
            </el-row>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>

      <el-drawer v-model="detailOpen" title="商户详情" size="520px">
         <el-descriptions v-if="detail" :column="1" border>
            <el-descriptions-item label="商户ID">{{ detail.memberId }}</el-descriptions-item>
            <el-descriptions-item label="租户ID">{{ detail.tenantId }}</el-descriptions-item>
            <el-descriptions-item label="商户编码">{{ detail.memberCode }}</el-descriptions-item>
            <el-descriptions-item label="商户类型">{{ getRentalOptionLabel(rentalMemberTypeOptions, detail.memberType) }}</el-descriptions-item>
            <el-descriptions-item label="认证状态">{{ getRentalOptionLabel(rentalAuthStatusOptions, detail.authStatus) }}</el-descriptions-item>
            <el-descriptions-item label="主体名称">{{ detail.legalSubjectName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="主体编号">{{ detail.licenseNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商户昵称">{{ detail.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detail.contactName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.contactPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="会员等级">{{ getRentalOptionLabel(rentalVipLevelOptions, detail.vipLevel) }}</el-descriptions-item>
            <el-descriptions-item label="存储额度">{{ formatQuota(detail.storageQuotaMb) }}</el-descriptions-item>
            <el-descriptions-item label="对外接单">{{ getRentalOptionLabel(rentalYesNoOptions, detail.externalAcceptEnabled) }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
         </el-descriptions>
      </el-drawer>
   </div>
</template>

<script setup name="RentalMember">
import { addMember, delMember, getMember, listMember, updateMember } from '@/api/rental/member'
import {
  findRentalOption,
  getRentalOptionLabel,
  rentalAuthStatusOptions,
  rentalMemberTypeOptions,
  rentalVipLevelOptions,
  rentalYesNoOptions
} from '@/utils/rental/options'

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict('sys_normal_disable')

const memberList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const detail = ref(null)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    tenantId: undefined,
    memberCode: undefined,
    memberType: undefined,
    authStatus: undefined,
    legalSubjectName: undefined,
    status: undefined
  },
  rules: {
    tenantId: [{ required: true, message: '租户ID不能为空', trigger: 'blur' }],
    memberCode: [{ required: true, message: '商户编码不能为空', trigger: 'blur' }],
    memberType: [{ required: true, message: '商户类型不能为空', trigger: 'change' }],
    authStatus: [{ required: true, message: '认证状态不能为空', trigger: 'change' }],
    vipLevel: [{ required: true, message: '会员等级不能为空', trigger: 'change' }],
    storageQuotaMb: [{ required: true, message: '存储额度不能为空', trigger: 'blur' }],
    externalAcceptEnabled: [{ required: true, message: '对外接单状态不能为空', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listMember(queryParams.value).then(response => {
    memberList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    memberId: undefined,
    tenantId: 1,
    memberCode: undefined,
    memberType: 'personal',
    authStatus: 'unverified',
    legalSubjectName: undefined,
    licenseNo: undefined,
    nickname: undefined,
    contactName: undefined,
    contactPhone: undefined,
    vipLevel: 'free',
    storageQuotaMb: 1024,
    externalAcceptEnabled: 0,
    status: '0',
    remark: undefined
  }
  proxy.resetForm('memberRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.memberId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增商户'
}

function handleUpdate(row) {
  reset()
  const memberId = row.memberId || ids.value
  getMember(memberId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改商户'
  })
}

function handleDetail(row) {
  getMember(row.memberId).then(response => {
    detail.value = response.data
    detailOpen.value = true
  })
}

function submitForm() {
  proxy.$refs.memberRef.validate(valid => {
    if (valid) {
      if (form.value.memberId !== undefined) {
        updateMember(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addMember(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const memberIds = row.memberId || ids.value
  proxy.$modal.confirm('是否确认删除商户编号为"' + memberIds + '"的数据项？').then(function() {
    return delMember(memberIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('system/rental/member/export', {
    ...queryParams.value
  }, `rental_member_${new Date().getTime()}.xlsx`)
}

function formatQuota(value) {
  if (value === undefined || value === null) {
    return '-'
  }
  if (value >= 1024) {
    return `${Number(value / 1024).toFixed(1)} GB`
  }
  return `${value} MB`
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
</style>
