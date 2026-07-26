<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="操作地址" prop="operIp">
        <el-input v-model="queryParams.operIp" placeholder="请输入操作地址" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="系统模块" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入系统模块" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="操作人员" prop="operName">
        <el-input v-model="queryParams.operName" placeholder="请输入操作人员" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="businessType">
        <el-select v-model="queryParams.businessType" placeholder="操作类型" clearable style="width: 220px">
          <el-option v-for="option in OPER_TYPE_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="操作状态" clearable style="width: 220px">
          <el-option v-for="option in OPER_STATUS_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间" style="width: 340px">
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
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['merchant:operlog:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="operlogList" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="日志编号" align="center" prop="operId" width="100" />
      <el-table-column label="系统模块" align="center" prop="title" min-width="130" show-overflow-tooltip />
      <el-table-column label="操作类型" align="center" prop="businessType" width="110">
        <template #default="scope">
          <dict-tag :options="OPER_TYPE_OPTIONS" :value="scope.row.businessType" />
        </template>
      </el-table-column>
      <el-table-column label="请求方式" align="center" prop="requestMethod" width="100" />
      <el-table-column label="操作人员" align="center" prop="operName" min-width="120" show-overflow-tooltip sortable="custom" />
      <el-table-column label="操作地址" align="center" prop="operIp" width="140" show-overflow-tooltip />
      <el-table-column label="操作状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="OPER_STATUS_OPTIONS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作日期" align="center" prop="operTime" width="180" sortable="custom">
        <template #default="scope">{{ parseTime(scope.row.operTime) }}</template>
      </el-table-column>
      <el-table-column label="消耗时间" align="center" prop="costTime" width="110" sortable="custom">
        <template #default="scope">{{ scope.row.costTime }}毫秒</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="90" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="showDetail(scope.row)">详细</el-button>
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

    <operlog-detail v-model:visible="detailVisible" :row="detailRow" />
  </div>
</template>

<script setup name="MerchantOperlog">
import OperlogDetail from './detail.vue'
import { listMerchantOperLog } from '@/api/merchant/operlog'
import { OPER_STATUS_OPTIONS, OPER_TYPE_OPTIONS } from '@/constants/options'

const { proxy } = getCurrentInstance()
const queryRef = ref()
const loading = ref(false)
const showSearch = ref(true)
const operlogList = ref([])
const total = ref(0)
const dateRange = ref([])
const detailVisible = ref(false)
const detailRow = ref({})
const defaultSort = { prop: 'operTime', order: 'descending' }

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  operIp: undefined,
  title: undefined,
  operName: undefined,
  businessType: undefined,
  status: undefined
})

async function getList() {
  loading.value = true
  try {
    const response = await listMerchantOperLog(proxy.addDateRange(queryParams, dateRange.value))
    operlogList.value = response.rows
    total.value = response.total
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  queryRef.value?.resetFields()
  handleQuery()
}

function handleSortChange(column) {
  queryParams.orderByColumn = column.prop
  queryParams.isAsc = column.order
  getList()
}

function showDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

function handleExport() {
  proxy.download('/merchant/operlog/export', {
    ...proxy.addDateRange({ ...queryParams }, dateRange.value)
  }, `merchant_operlog_${Date.now()}.xlsx`)
}

getList()
</script>
