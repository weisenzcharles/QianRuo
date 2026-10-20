<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch">
      <el-form-item label="分类名称" prop="className">
        <el-input
          v-model="queryParams.className"
          placeholder="请输入分类名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="分类状态" clearable style="width: 180px">
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
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
          @click="handleAdd()"
          v-hasPermi="['platform:class:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadClassTree" />
    </el-row>

    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="classTree"
      row-key="classId"
      :default-expand-all="expandAll"
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="className" label="分类名称" min-width="260" />
      <el-table-column prop="classCode" label="分类编码" width="180" />
      <el-table-column prop="classLevel" label="层级" align="center" width="90" />
      <el-table-column prop="sortNo" label="排序" align="center" width="90" />
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === NORMAL_DISABLE_CODE.NORMAL ? 'success' : 'info'">
            {{ scope.row.status === NORMAL_DISABLE_CODE.NORMAL ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" align="center" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="210" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['platform:class:edit']"
          >修改</el-button>
          <el-button
            link
            type="primary"
            icon="Plus"
            @click="handleAdd(scope.row)"
            v-hasPermi="['platform:class:add']"
          >新增</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['platform:class:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogOpen" :title="dialogTitle" width="620px" append-to-body @closed="resetForm">
      <el-form ref="classFormRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="上级分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ value: 'classId', label: 'className', children: 'children' }"
            value-key="classId"
            check-strictly
            clearable
            placeholder="请选择上级分类"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col v-if="form.classId" :span="12">
            <el-form-item label="分类编码">
              <el-input v-model="form.classCode" maxlength="64" disabled placeholder="保存后由系统自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类名称" prop="className">
              <el-input v-model="form.className" maxlength="128" placeholder="请输入分类名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="sortNo">
              <el-input-number v-model="form.sortNo" :min="0" :max="9999" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch v-model="form.status" :active-value="NORMAL_DISABLE_CODE.NORMAL" :inactive-value="NORMAL_DISABLE_CODE.DISABLED" active-text="正常" inactive-text="停用" inline-prompt />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ProductClass">
import {
  addProductClass,
  deleteProductClass,
  getProductClass,
  listProductClass,
  updateProductClass
} from '@/api/rental/productClass'
import { NORMAL_DISABLE_CODE } from '@/utils/stateCode'

const { proxy } = getCurrentInstance()
const classTree = ref([])
const parentOptions = ref([])
const loading = ref(false)
const showSearch = ref(true)
const refreshTable = ref(true)
const expandAll = ref(true)
const dialogOpen = ref(false)
const dialogTitle = ref('')
const statusOptions = [
  { label: '正常', value: NORMAL_DISABLE_CODE.NORMAL },
  { label: '停用', value: NORMAL_DISABLE_CODE.DISABLED }
]

const data = reactive({
  queryParams: {
    className: undefined,
    status: undefined
  },
  form: {},
  rules: {
    parentId: [{ required: true, message: '上级分类不能为空', trigger: 'change' }],
    className: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
    sortNo: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }],
    status: [{ required: true, message: '分类状态不能为空', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/**
 * 查询平台产品分类树并刷新表格数据。
 */
function loadClassTree() {
  loading.value = true
  return listProductClass(queryParams.value).then(response => {
    classTree.value = response.data || []
  }).finally(() => {
    loading.value = false
  })
}

/**
 * 重置新增或修改表单为稳定默认值。
 */
function resetForm() {
  form.value = {
    classId: undefined,
    parentId: 0,
    classCode: undefined,
    className: undefined,
    sortNo: 0,
    status: NORMAL_DISABLE_CODE.NORMAL,
    remark: undefined
  }
  proxy.resetForm('classFormRef')
}

/**
 * 生成包含虚拟根节点的上级分类选择树，并排除当前节点及其全部后代。
 *
 * @param {number|string|undefined} excludedId 编辑时需要排除的当前分类编号
 */
function prepareParentOptions(excludedId) {
  const cloneAndFilter = nodes => (nodes || [])
    .filter(node => node.classId !== excludedId)
    .map(node => ({ ...node, children: cloneAndFilter(node.children) }))
  parentOptions.value = [{
    classId: 0,
    className: '顶级分类',
    children: cloneAndFilter(classTree.value)
  }]
}

/**
 * 根据当前筛选条件重新查询分类树。
 */
function handleQuery() {
  loadClassTree()
}

/**
 * 清空查询条件并重新加载全部分类。
 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

/**
 * 打开新增分类对话框，可将当前行作为默认上级分类。
 *
 * @param {object|undefined} row 当前分类行
 */
function handleAdd(row) {
  resetForm()
  loadClassTree().then(() => {
    prepareParentOptions()
    form.value.parentId = row?.classId || 0
    dialogTitle.value = '新增产品分类'
    dialogOpen.value = true
  })
}

/**
 * 加载指定分类详情并打开修改对话框。
 *
 * @param {object} row 当前分类行
 */
function handleUpdate(row) {
  resetForm()
  Promise.all([loadClassTree(), getProductClass(row.classId)]).then(([, response]) => {
    prepareParentOptions(row.classId)
    form.value = { ...response.data, status: Number(response.data?.status ?? NORMAL_DISABLE_CODE.NORMAL) }
    dialogTitle.value = '修改产品分类'
    dialogOpen.value = true
  })
}

/**
 * 确认后软删除指定分类并刷新列表。
 *
 * @param {object} row 当前分类行
 */
function handleDelete(row) {
  proxy.$modal.confirm(`是否确认删除产品分类“${row.className}”？`).then(() => {
    return deleteProductClass(row.classId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    loadClassTree()
  }).catch(() => {})
}

/**
 * 校验表单后新增或修改产品分类。
 */
function submitForm() {
  proxy.$refs.classFormRef.validate(valid => {
    if (!valid) {
      return
    }
    const request = form.value.classId ? updateProductClass(form.value) : addProductClass(form.value)
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.classId ? '修改成功' : '新增成功')
      dialogOpen.value = false
      loadClassTree()
    })
  })
}

/**
 * 关闭编辑对话框并清理表单状态。
 */
function cancel() {
  dialogOpen.value = false
}

/**
 * 切换分类树的全部展开或全部折叠状态。
 */
function toggleExpandAll() {
  refreshTable.value = false
  expandAll.value = !expandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

loadClassTree()
</script>
