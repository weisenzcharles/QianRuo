<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="公告标题" prop="noticeTitle">
        <el-input
          v-model="queryParams.noticeTitle"
          placeholder="请输入公告标题"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="公告类型" prop="noticeType">
        <el-select v-model="queryParams.noticeType" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="item in noticeTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="发布范围" prop="targetScope">
        <el-select v-model="queryParams.targetScope" placeholder="全部" clearable style="width: 160px">
          <el-option label="全部商户" :value="MERCHANT_NOTICE_SCOPE_CODE.ALL_MERCHANTS" />
          <el-option label="指定商户" :value="MERCHANT_NOTICE_SCOPE_CODE.SPECIFIED_MERCHANTS" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="dict in sys_notice_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['platform:merchantNotice:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" v-hasPermi="['platform:merchantNotice:edit']" @click="handleUpdate()">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['platform:merchantNotice:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="noticeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="公告标题" prop="noticeTitle" min-width="220" show-overflow-tooltip />
      <el-table-column label="公告类型" prop="noticeType" width="110" align="center">
        <template #default="scope">
          <dict-tag :options="sys_notice_type" :value="scope.row.noticeType" />
        </template>
      </el-table-column>
      <el-table-column label="发布范围" min-width="240" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.targetScope === MERCHANT_NOTICE_SCOPE_CODE.ALL_MERCHANTS ? '全部商户' : (scope.row.targetMemberNames || '未配置指定商户') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template #default="scope">
          <dict-tag :options="sys_notice_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="发布时间" prop="createTime" width="180" align="center" />
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" v-hasPermi="['platform:merchantNotice:edit']" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Delete" v-hasPermi="['platform:merchantNotice:remove']" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <el-dialog v-model="open" :title="title" width="780px" append-to-body>
      <el-form ref="noticeRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="公告标题" prop="noticeTitle">
              <el-input v-model="form.noticeTitle" placeholder="请输入公告标题" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公告类型" prop="noticeType">
              <el-select v-model="form.noticeType" placeholder="请选择公告类型" style="width: 100%">
                <el-option v-for="item in noticeTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="发布范围" prop="targetScope">
              <el-radio-group v-model="form.targetScope" @change="handleScopeChange">
                <el-radio :value="MERCHANT_NOTICE_SCOPE_CODE.ALL_MERCHANTS">全部商户</el-radio>
                <el-radio :value="MERCHANT_NOTICE_SCOPE_CODE.SPECIFIED_MERCHANTS">指定商户</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col v-if="form.targetScope === MERCHANT_NOTICE_SCOPE_CODE.SPECIFIED_MERCHANTS" :span="24">
            <el-form-item label="接收商户" prop="targetMemberIds">
              <el-select
                v-model="form.targetMemberIds"
                multiple
                filterable
                clearable
                collapse-tags
                collapse-tags-tooltip
                placeholder="请选择接收商户"
                style="width: 100%"
              >
                <el-option
                  v-for="item in memberOptions"
                  :key="item.memberId"
                  :label="item.memberName || item.merchantName"
                  :value="item.memberId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-switch
                v-model="form.status"
                :active-value="NORMAL_DISABLE_CODE.NORMAL"
                :inactive-value="NORMAL_DISABLE_CODE.DISABLED"
                active-text="正常"
                inactive-text="停用"
                inline-prompt
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="公告内容">
              <editor v-model="form.noticeContent" :min-height="220" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" maxlength="500" show-word-limit :rows="3" placeholder="请输入备注" />
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
  </div>
</template>

<script setup name="MerchantNotice">
import {
  addMerchantNotice,
  deleteMerchantNotice,
  getMerchantNotice,
  listMerchantNotice,
  listMerchantNoticeMembers,
  updateMerchantNotice
} from '@/api/rental/merchantNotice'
import { NORMAL_DISABLE_CODE, SYSTEM_CODE } from '@/utils/stateCode'

const { proxy } = getCurrentInstance()
const { sys_notice_status, sys_notice_type } = proxy.useDict('sys_notice_status', 'sys_notice_type')
const MERCHANT_NOTICE_SCOPE_CODE = SYSTEM_CODE.MERCHANT_NOTICE_SCOPE

const noticeTypeOptions = [
  { label: '通知', value: SYSTEM_CODE.NOTICE_TYPE.NOTIFICATION },
  { label: '公告', value: SYSTEM_CODE.NOTICE_TYPE.ANNOUNCEMENT }
]
const noticeList = ref([])
const memberOptions = ref([])
const open = ref(false)
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
    noticeTitle: undefined,
    noticeType: undefined,
    targetScope: undefined,
    status: undefined
  },
  rules: {
    noticeTitle: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
    noticeType: [{ required: true, message: '公告类型不能为空', trigger: 'change' }],
    targetScope: [{ required: true, message: '发布范围不能为空', trigger: 'change' }],
    targetMemberIds: [{
      validator: (rule, value, callback) => {
        if (data.form.targetScope === MERCHANT_NOTICE_SCOPE_CODE.SPECIFIED_MERCHANTS && (!value || value.length === 0)) {
          callback(new Error('请选择接收商户'))
          return
        }
        callback()
      },
      trigger: 'change'
    }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listMerchantNotice(queryParams.value)
    .then(response => {
      noticeList.value = response.rows || []
      total.value = response.total || 0
    })
    .finally(() => {
      loading.value = false
    })
}

function loadMemberOptions() {
  return listMerchantNoticeMembers().then(response => {
    memberOptions.value = Array.isArray(response.data) ? response.data : []
  })
}

function reset() {
  form.value = {
    merchantNoticeId: undefined,
    noticeTitle: undefined,
    noticeType: SYSTEM_CODE.NOTICE_TYPE.ANNOUNCEMENT,
    noticeContent: '',
    targetScope: MERCHANT_NOTICE_SCOPE_CODE.ALL_MERCHANTS,
    targetMemberIds: [],
    status: NORMAL_DISABLE_CODE.NORMAL,
    remark: ''
  }
  proxy.resetForm('noticeRef')
}

function cancel() {
  open.value = false
  reset()
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
  ids.value = selection.map(item => item.merchantNoticeId)
  single.value = selection.length !== 1
  multiple.value = selection.length === 0
}

function handleAdd() {
  reset()
  loadMemberOptions()
  title.value = '添加商户公告'
  open.value = true
}

function handleUpdate(row) {
  reset()
  const merchantNoticeId = row?.merchantNoticeId || ids.value[0]
  Promise.all([getMerchantNotice(merchantNoticeId), loadMemberOptions()]).then(([response]) => {
    form.value = {
      ...form.value,
      ...response.data,
      targetMemberIds: response.data.targetMemberIds || []
    }
    title.value = '修改商户公告'
    open.value = true
  })
}

function handleScopeChange(value) {
  if (value === MERCHANT_NOTICE_SCOPE_CODE.ALL_MERCHANTS) {
    form.value.targetMemberIds = []
  }
}

function submitForm() {
  proxy.$refs.noticeRef.validate(valid => {
    if (!valid) {
      return
    }
    const action = form.value.merchantNoticeId ? updateMerchantNotice : addMerchantNotice
    action(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.merchantNoticeId ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const merchantNoticeIds = row?.merchantNoticeId || ids.value
  proxy.$modal.confirm('是否确认删除选中的商户公告？').then(() => deleteMerchantNotice(merchantNoticeIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>
