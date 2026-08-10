<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch">
      <el-form-item label="品牌" prop="brand">
        <el-input v-model="queryParams.brand" placeholder="请输入品牌" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="型号" prop="modelName">
        <el-input v-model="queryParams.modelName" placeholder="请输入型号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类目" prop="classId">
        <el-tree-select v-model="queryParams.classId" :data="classOptions" :props="treeProps" check-strictly clearable filterable placeholder="请选择类目" style="width: 220px" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="型号状态" clearable style="width: 140px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['platform:brandModel:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="modelList">
      <el-table-column label="型号ID" prop="modelId" width="90" align="center" />
      <el-table-column label="产品类目" prop="className" min-width="150" show-overflow-tooltip />
      <el-table-column label="品牌" prop="brand" width="140" />
      <el-table-column label="型号" prop="modelName" min-width="180" />
      <el-table-column label="内存/规格" prop="memorySpec" min-width="130" show-overflow-tooltip />
      <el-table-column label="官方标准" prop="officialFlag" width="100" align="center">
        <template #default="scope">{{ scope.row.officialFlag === '1' ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['platform:brandModel:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['platform:brandModel:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog v-model="dialogOpen" :title="dialogTitle" width="620px" append-to-body>
      <el-form ref="modelFormRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="产品类目" prop="classId">
          <el-tree-select v-model="form.classId" :data="classOptions" :props="treeProps" check-strictly filterable placeholder="请选择产品类目" style="width: 100%" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="品牌" prop="brand"><el-input v-model="form.brand" maxlength="64" placeholder="请输入品牌" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="型号" prop="modelName"><el-input v-model="form.modelName" maxlength="128" placeholder="请输入型号" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="内存/规格" prop="memorySpec"><el-input v-model="form.memorySpec" maxlength="64" placeholder="可选" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="官方标准" prop="officialFlag"><el-radio-group v-model="form.officialFlag"><el-radio value="1">是</el-radio><el-radio value="0">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确定</el-button><el-button @click="cancel">取消</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="BrandModel">
import { listProductClass } from '@/api/rental/productClass'
import { addBrandModel, deleteBrandModel, getBrandModel, listBrandModels, updateBrandModel } from '@/api/rental/brandModel'

const { proxy } = getCurrentInstance()
const modelList = ref([])
const classOptions = ref([])
const total = ref(0)
const loading = ref(false)
const showSearch = ref(true)
const dialogOpen = ref(false)
const dialogTitle = ref('')
const treeProps = { value: 'classId', label: 'className', children: 'children' }
const queryParams = reactive({ pageNum: 1, pageSize: 10, brand: undefined, modelName: undefined, classId: undefined, status: undefined })
const form = ref({})
const rules = {
  classId: [{ required: true, message: '产品类目不能为空', trigger: 'change' }],
  brand: [{ required: true, message: '品牌不能为空', trigger: 'blur' }],
  modelName: [{ required: true, message: '型号不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
}

/** 查询平台标准品牌型号列表。 */
function getList() {
  loading.value = true
  listBrandModels(queryParams).then(response => {
    modelList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => { loading.value = false })
}

/** 加载正常产品类目，作为型号归属选择。 */
function loadClassOptions() {
  listProductClass({ status: '0' }).then(response => { classOptions.value = response.data || [] })
}

/** 重置查询条件并回到第一页。 */
function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.pageNum = 1
  getList()
}

/** 根据当前查询条件刷新标准型号列表。 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 打开新增标准型号表单。 */
function handleAdd() {
  form.value = { classId: undefined, brand: undefined, modelName: undefined, memorySpec: undefined, officialFlag: '1', status: '0', remark: undefined }
  dialogTitle.value = '新增标准品牌型号'
  dialogOpen.value = true
}

/** 加载指定标准型号详情并打开编辑表单。 */
function handleUpdate(row) {
  getBrandModel(row.modelId).then(response => {
    form.value = response.data
    dialogTitle.value = '修改标准品牌型号'
    dialogOpen.value = true
  })
}

/** 确认后软删除未被引用的标准型号。 */
function handleDelete(row) {
  proxy.$modal.confirm(`是否确认删除标准型号“${row.brand} ${row.modelName}”？`).then(() => deleteBrandModel(row.modelId)).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

/** 校验表单后新增或修改标准型号。 */
function submitForm() {
  proxy.$refs.modelFormRef.validate(valid => {
    if (!valid) return
    const request = form.value.modelId ? updateBrandModel(form.value) : addBrandModel(form.value)
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.modelId ? '修改成功' : '新增成功')
      dialogOpen.value = false
      getList()
    })
  })
}

/** 关闭标准型号编辑对话框。 */
function cancel() {
  dialogOpen.value = false
  proxy.resetForm('modelFormRef')
}

loadClassOptions()
getList()
</script>
