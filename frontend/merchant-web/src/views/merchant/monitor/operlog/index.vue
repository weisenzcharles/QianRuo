<template>
  <div class="app-container merchant-list-page">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="76px">
      <el-form-item label="业务模块" prop="businessModule">
        <el-select v-model="queryParams.businessModule" placeholder="请选择模块" clearable style="width: 180px">
          <el-option v-for="module in moduleOptions" :key="module" :label="module" :value="module" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作类型" prop="operationType">
        <el-select v-model="queryParams.operationType" placeholder="请选择操作类型" clearable style="width: 180px">
          <el-option v-for="option in MERCHANT_AUDIT_TYPE_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作人" prop="operatorName">
        <el-select v-model="queryParams.operatorName" placeholder="请选择操作人" clearable style="width: 180px">
          <el-option v-for="operator in operatorOptions" :key="operator" :label="operator" :value="operator" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="queryParams.description" placeholder="请输入描述" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="IP 地址" prop="ipAddress">
        <el-input v-model="queryParams.ipAddress" placeholder="请输入 IP 地址" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="操作时间" style="width: 350px">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="auditList" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="操作时间" align="center" prop="operationTime" width="180" sortable="custom">
        <template #default="scope">{{ parseTime(scope.row.operationTime) }}</template>
      </el-table-column>
      <el-table-column label="用户名" align="center" prop="username" min-width="140" show-overflow-tooltip sortable="custom" />
      <el-table-column label="操作人" align="center" prop="operatorName" min-width="120" show-overflow-tooltip sortable="custom" />
      <el-table-column label="模块" align="center" prop="businessModule" min-width="130" show-overflow-tooltip>
        <template #default="scope">
          <el-tag type="primary" effect="light">{{ scope.row.businessModule || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作类型" align="center" prop="operationType" width="110">
        <template #default="scope">
          <dict-tag :options="MERCHANT_AUDIT_TYPE_OPTIONS" :value="scope.row.operationType" />
        </template>
      </el-table-column>
      <el-table-column label="描述" align="left" prop="description" min-width="280" show-overflow-tooltip />
      <el-table-column label="IP地址" align="center" prop="ipAddress" width="150" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="90" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="showDetail(scope.row)">详情</el-button>
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

    <operlog-detail v-model:visible="detailVisible" :audit-id="detailId" />
  </div>
</template>

<script setup name="MerchantOperlog">
import OperlogDetail from './detail.vue'
import { getMerchantOperLogOptions, listMerchantOperLog } from '@/api/merchant/operlog'
import { MERCHANT_AUDIT_TYPE_OPTIONS } from '@/constants/options'

const { proxy } = getCurrentInstance()
const queryRef = ref()
const loading = ref(false)
const showSearch = ref(true)
const auditList = ref([])
const moduleOptions = ref([])
const operatorOptions = ref([])
const total = ref(0)
const dateRange = ref([])
const detailVisible = ref(false)
const detailId = ref(null)
const defaultSort = { prop: 'operationTime', order: 'descending' }

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  businessModule: undefined,
  operationType: undefined,
  username: undefined,
  operatorName: undefined,
  description: undefined,
  ipAddress: undefined
})

/** 查询当前商户的业务审计摘要列表。 */
async function getList() {
  loading.value = true
  try {
    const response = await listMerchantOperLog(proxy.addDateRange(queryParams, dateRange.value))
    auditList.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

/** 执行审计条件查询并回到第一页。 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 清空筛选条件并刷新审计列表。 */
function resetQuery() {
  dateRange.value = []
  queryRef.value?.resetFields()
  handleQuery()
}

/** 按操作时间或操作人切换服务端排序。 */
function handleSortChange(column) {
  queryParams.orderByColumn = column.prop
  queryParams.isAsc = column.order
  getList()
}

/** 打开审计详情并由详情组件加载前后快照。 */
function showDetail(row) {
  detailId.value = row.auditId
  detailVisible.value = true
}

/** 查询当前商户日志中可用的模块和操作人筛选项。 */
async function loadOptions() {
  try {
    const response = await getMerchantOperLogOptions()
    moduleOptions.value = response.data?.businessModules || []
    operatorOptions.value = response.data?.operatorNames || []
  } catch (error) {
    moduleOptions.value = []
    operatorOptions.value = []
  }
}

loadOptions()
getList()
</script>
