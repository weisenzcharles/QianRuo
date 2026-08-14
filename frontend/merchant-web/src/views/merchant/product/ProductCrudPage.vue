<template>
  <div class="app-container merchant-list-page merchant-product-page">
    <div class="page-heading">
      <div>
        <h2>{{ createOnly ? '发布商品' : '商品管理' }}</h2>
        <p>{{ createOnly ? '创建当前商户可经营的商品 SKU。' : '维护当前商户的商品 SKU，编码由系统统一生成。' }}</p>
      </div>
    </div>

    <el-alert
      v-if="!optionLoading && classOptions.length === 0"
      type="warning"
      :closable="false"
      class="option-alert"
    >
      <template #title>
        <div class="option-alert-content">
          <span>请先在经营类目中选择可经营的产品类目</span>
          <el-button v-hasPermi="['merchant:class:list']" link type="primary" @click="openClassSettings">
            前往设置
          </el-button>
        </div>
      </template>
    </el-alert>

    <el-form v-if="!createOnly" v-show="showSearch" :inline="true" :model="query" @submit.prevent>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="商品编码、名称、品牌或型号"
          @keyup.enter="loadProducts"
        />
      </el-form-item>
      <el-form-item label="经营类目">
        <el-select v-model="query.classId" clearable filterable placeholder="全部类目" @change="loadProducts">
          <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="商品类型">
        <el-select v-model="query.productType" clearable placeholder="全部" @change="loadProducts">
          <el-option v-for="item in productTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="库存模式">
        <el-select v-model="query.stockMode" clearable placeholder="全部" @change="loadProducts">
          <el-option v-for="item in stockModeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" @change="loadProducts">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="loadProducts">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row v-if="!createOnly" :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button v-hasPermi="['merchant:product:add']" type="primary" icon="Plus" @click="openCreate">新增商品</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadProducts" />
    </el-row>

    <template v-if="!createOnly">
      <el-table v-loading="loading" :data="rows" row-key="id" border @row-dblclick="openDetail">
      <el-table-column prop="productCode" label="商品编码" min-width="150" show-overflow-tooltip />
      <el-table-column prop="productName" label="商品名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="经营类目" min-width="150" show-overflow-tooltip>
        <template #default="scope">
          {{ classLabel(scope.row.classId) }}
        </template>
      </el-table-column>
      <el-table-column label="标准型号" min-width="180" show-overflow-tooltip>
        <template #default="scope">
          {{ modelLabel(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column prop="poolName" label="资源池" min-width="160" show-overflow-tooltip />
      <el-table-column label="商品类型" width="100">
        <template #default="scope">
          {{ productTypeLabel(scope.row.productType) }}
        </template>
      </el-table-column>
      <el-table-column label="库存模式" width="110">
        <template #default="scope">
          {{ stockModeLabel(scope.row.stockMode) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="scope">
          <el-tag :type="Number(scope.row.status) === 0 ? 'success' : 'info'">
            {{ Number(scope.row.status) === 0 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="210">
        <template #default="scope">
          <el-button v-hasPermi="['merchant:product:query']" link type="primary" @click="openDetail(scope.row)">
            详情
          </el-button>
          <el-button v-hasPermi="['merchant:product:edit']" link type="primary" @click="openEdit(scope.row)">
            编辑
          </el-button>
          <el-button v-hasPermi="['merchant:product:remove']" link type="danger" @click="removeProduct(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        v-model:page="query.pageNum"
        v-model:limit="query.pageSize"
        :total="total"
        @pagination="loadProducts"
      />
    </template>

    <el-drawer v-model="detailVisible" title="商品详情" size="520px" destroy-on-close>
      <el-skeleton v-if="detailLoading" :rows="8" animated />
      <el-descriptions v-else :column="1" border>
        <el-descriptions-item label="商品编码">{{ detail.productCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ detail.productName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经营类目">{{ classLabel(detail.classId) }}</el-descriptions-item>
        <el-descriptions-item label="标准型号">{{ modelLabel(detail) }}</el-descriptions-item>
        <el-descriptions-item label="资源池">{{ detail.poolName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商品类型">{{ productTypeLabel(detail.productType) }}</el-descriptions-item>
        <el-descriptions-item label="库存模式">{{ stockModeLabel(detail.stockMode) }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ detail.brand || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商品型号">{{ detail.modelName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ detail.memorySpec || '-' }}</el-descriptions-item>
        <el-descriptions-item label="采购成本">{{ money(detail.purchaseCost) }}</el-descriptions-item>
        <el-descriptions-item label="销售价格">{{ money(detail.salePrice) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ Number(detail.status) === 0 ? '启用' : '停用' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <el-dialog v-model="formVisible" :title="editing ? '修改商品' : (createOnly ? '发布商品' : '新增商品')" width="760px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="112px">
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="商品编码">
              <el-input :model-value="form.productCode || '保存后由系统生成'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品名称" prop="productName">
              <el-input v-model="form.productName" maxlength="128" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经营类目" prop="classId">
              <el-select v-model="form.classId" filterable placeholder="请选择经营类目" class="field-control" @change="handleClassChange">
                <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准型号" prop="modelId">
              <el-select v-model="form.modelId" clearable filterable placeholder="请选择标准型号" class="field-control" @change="handleModelChange">
                <el-option v-for="item in filteredModels" :key="item.modelId" :label="modelOptionLabel(item)" :value="item.modelId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品类型" prop="productType">
              <el-select v-model="form.productType" placeholder="请选择商品类型" class="field-control" @change="handleProductTypeChange">
                <el-option v-for="item in productTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存模式" prop="stockMode">
              <el-select v-model="form.stockMode" placeholder="请选择库存模式" class="field-control">
                <el-option v-for="item in stockModeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资源池" prop="poolId">
              <el-select v-model="form.poolId" filterable placeholder="请选择资源池" class="field-control">
                <el-option v-for="item in filteredPools" :key="item.id" :label="poolOptionLabel(item)" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" maxlength="64" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品型号">
              <el-input v-model="form.modelName" maxlength="128" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格">
              <el-input v-model="form.memorySpec" maxlength="64" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购成本">
              <el-input-number v-model="form.purchaseCost" :min="0" :precision="2" :step="0.01" controls-position="right" class="field-control" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售价格">
              <el-input-number v-model="form.salePrice" :min="0" :precision="2" :step="0.01" controls-position="right" class="field-control" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" class="field-control">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button v-hasPermi="[editing ? 'merchant:product:edit' : 'merchant:product:add']" type="primary" :loading="saving" @click="submitForm">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  addMerchantProduct,
  deleteMerchantProduct,
  getMerchantProduct,
  listMerchantProducts,
  listMerchantPools,
  updateMerchantProduct
} from '@/api/merchant/rentalBusiness'
import { listBrandModelOptions } from '@/api/merchant/brandModel'
import { listBusinessProductClasses, listSelectedProductClassIds } from '@/api/merchant/productClass'

const props = defineProps({
  createOnly: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()

const statusOptions = [{ label: '启用', value: 0 }, { label: '停用', value: 1 }]
const productTypeOptions = [{ label: '设备', value: 'device' }, { label: '配件', value: 'accessory' }]
const stockModeOptions = [{ label: '逐件/SN', value: 'serial' }, { label: '数量', value: 'quantity' }]
const formRef = ref(null)
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const showSearch = ref(true)
const saving = ref(false)
const optionLoading = ref(false)
const formVisible = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const editing = ref(false)
const classTree = ref([])
const selectedClassIds = ref(new Set())
const modelOptions = ref([])
const poolOptions = ref([])
const detail = ref({})
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', classId: undefined, productType: undefined, stockMode: undefined, status: undefined })
const form = reactive(createEmptyForm())

const rules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  classId: [{ required: true, message: '请选择经营类目', trigger: 'change' }],
  productType: [{ required: true, message: '请选择商品类型', trigger: 'change' }],
  stockMode: [{ required: true, message: '请选择库存模式', trigger: 'change' }],
  poolId: [{ required: true, message: '请选择资源池', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const classOptions = computed(() => flattenClasses(classTree.value, [], selectedClassIds.value))

const filteredModels = computed(() => {
  if (!form.classId) {
    return modelOptions.value
  }
  return modelOptions.value.filter(item => Number(item.classId) === Number(form.classId))
})

const filteredPools = computed(() => {
  if (!form.productType) {
    return poolOptions.value
  }
  const resourceTypes = form.productType === 'device' ? ['device'] : ['accessory', 'consumable']
  return poolOptions.value.filter(item => resourceTypes.includes(item.resourceType))
})

/** 创建商品表单的默认值，避免新增和编辑复用上一次表单状态。 */
function createEmptyForm() {
  return {
    id: undefined,
    productCode: '',
    productName: '',
    classId: undefined,
    modelId: undefined,
    poolId: undefined,
    productType: 'device',
    stockMode: 'serial',
    brand: '',
    modelName: '',
    memorySpec: '',
    purchaseCost: undefined,
    salePrice: undefined,
    status: 0,
    remark: ''
  }
}

/** 加载类目、标准型号和启用资源池，为商品表单提供可信选项。 */
async function loadOptions() {
  optionLoading.value = true
  try {
    const [classResponse, selectedResponse, modelResponse, poolResponse] = await Promise.all([
      listBusinessProductClasses(),
      listSelectedProductClassIds(),
      listBrandModelOptions(),
      listMerchantPools({ pageNum: 1, pageSize: 500, status: 0 })
    ])
    classTree.value = responseData(classResponse) || []
    selectedClassIds.value = new Set((responseData(selectedResponse) || []).map(Number))
    modelOptions.value = responseData(modelResponse) || []
    poolOptions.value = responseRows(poolResponse)
  } finally {
    optionLoading.value = false
  }
}

/** 加载当前商户商品分页列表，并保留后端返回的总数。 */
async function loadProducts() {
  loading.value = true
  try {
    const response = await listMerchantProducts(query)
    rows.value = responseRows(response)
    total.value = responseTotal(response, rows.value.length)
  } finally {
    loading.value = false
  }
}

/** 重置商品筛选条件并回到第一页。 */
function resetQuery() {
  Object.assign(query, { pageNum: 1, keyword: '', classId: undefined, productType: undefined, stockMode: undefined, status: undefined })
  loadProducts()
}

/**
 * 跳转到当前商户经营类目设置页面，类目保存后可返回商品页面继续发布流程。
 */
function openClassSettings() {
  router.push('/merchant/product/class')
}

/** 打开新增商品表单并清空历史编辑值。 */
function openCreate() {
  editing.value = false
  Object.assign(form, createEmptyForm())
  formVisible.value = true
}

/** 读取商品详情后打开编辑表单，确保编辑数据来自商户归属接口。 */
async function openEdit(row) {
  editing.value = true
  const response = await getMerchantProduct(row.id)
  Object.assign(form, createEmptyForm(), responseData(response) || row)
  formVisible.value = true
}

/** 读取商品详情并打开右侧详情抽屉。 */
async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const response = await getMerchantProduct(row.id)
    detail.value = responseData(response) || row
  } finally {
    detailLoading.value = false
  }
}

/** 提交商品新增或修改请求，并在提交前执行前端表单校验。 */
async function submitForm() {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }
  if (form.productType === 'device' && !form.modelId) {
    ElMessage.warning('设备商品必须选择标准型号')
    return
  }
  saving.value = true
  try {
    const payload = { ...form }
    if (!editing.value) {
      delete payload.id
      delete payload.productCode
    }
    const response = editing.value ? await updateMerchantProduct(payload) : await addMerchantProduct(payload)
    if (response && response.code !== undefined && response.code !== 200) {
      return
    }
    ElMessage.success(editing.value ? '商品修改成功' : '商品新增成功')
    formVisible.value = false
    if (!props.createOnly) {
      await loadProducts()
    }
  } finally {
    saving.value = false
  }
}

/** 删除当前商品，后端会检查库存、套餐和订单引用后执行软删除。 */
function removeProduct(row) {
  ElMessageBox.confirm(`确认删除商品“${row.productName || row.productCode}”吗？`, '操作确认', { type: 'warning' })
    .then(() => deleteMerchantProduct(row.id))
    .then(() => {
      ElMessage.success('商品已删除')
      loadProducts()
    })
    .catch(() => undefined)
}

/** 切换类目时清理不再匹配的标准型号，避免提交跨类目关系。 */
function handleClassChange() {
  if (form.modelId && !filteredModels.value.some(item => Number(item.modelId) === Number(form.modelId))) {
    form.modelId = undefined
    form.brand = ''
    form.modelName = ''
    form.memorySpec = ''
  }
}

/** 选择标准型号后自动补齐商品的品牌、型号和规格快照。 */
function handleModelChange(modelId) {
  const model = modelOptions.value.find(item => Number(item.modelId) === Number(modelId))
  if (!model) {
    return
  }
  form.brand = model.brand || form.brand
  form.modelName = model.modelName || form.modelName
  form.memorySpec = model.memorySpec || form.memorySpec
}

/** 切换商品类型时清理不兼容资源池，并给新增商品设置合理库存默认值。 */
function handleProductTypeChange(productType) {
  if (!editing.value) {
    form.stockMode = productType === 'device' ? 'serial' : 'quantity'
  }
  if (form.poolId && !filteredPools.value.some(item => Number(item.id) === Number(form.poolId))) {
    form.poolId = undefined
  }
}

/** 将平台类目树转换为带层级路径的下拉选项，只允许商户已选择的类目。 */
function flattenClasses(nodes, parents, selectedIds, result = []) {
  nodes.forEach(node => {
    const path = parents.concat(node.className || [])
    if (selectedIds.has(Number(node.classId))) {
      result.push({ value: node.classId, label: path.join(' / ') })
    }
    flattenClasses(node.children || [], path, selectedIds, result)
  })
  return result
}

/** 从标准型号对象生成商品表单中的可读选项名称。 */
function modelOptionLabel(model) {
  return [model.brand, model.modelName, model.memorySpec].filter(Boolean).join(' / ')
}

/** 从资源池对象生成包含编码的可读选项名称。 */
function poolOptionLabel(pool) {
  return [pool.poolName, pool.poolCode].filter(Boolean).join(' / ')
}

/** 根据类目主键返回列表和详情中的类目名称。 */
function classLabel(classId) {
  const option = classOptions.value.find(item => Number(item.value) === Number(classId))
  return option ? option.label : classId || '-'
}

/** 组合标准型号的品牌、型号和规格展示文本。 */
function modelLabel(product) {
  return [product.brand, product.modelName, product.memorySpec].filter(Boolean).join(' / ') || '-'
}

/** 将商品类型编码转换为中文名称。 */
function productTypeLabel(value) {
  const option = productTypeOptions.find(item => item.value === value)
  return option ? option.label : value || '-'
}

/** 将库存模式编码转换为中文名称。 */
function stockModeLabel(value) {
  const option = stockModeOptions.find(item => item.value === value)
  return option ? option.label : value || '-'
}

/** 将金额格式化为两位小数，空值保持未设置状态。 */
function money(value) {
  return value === undefined || value === null || value === '' ? '-' : Number(value).toFixed(2)
}

/** 兼容分页接口和 Axios 包装响应，统一提取行数据。 */
function responseRows(response) {
  const body = response?.data && !Array.isArray(response.data) ? response.data : response
  if (Array.isArray(body)) {
    return body
  }
  return body?.rows || []
}

/** 兼容普通 AjaxResult 和分页响应，统一提取业务数据。 */
function responseData(response) {
  return response?.data !== undefined ? response.data : response
}

/** 兼容不同分页响应结构，统一读取总记录数。 */
function responseTotal(response, fallback) {
  const body = response?.data && !Array.isArray(response.data) ? response.data : response
  return body?.total === undefined ? fallback : body.total
}

onMounted(async () => {
  await loadOptions()
  if (props.createOnly) {
    openCreate()
  } else {
    await loadProducts()
  }
})
</script>

<style scoped>
.merchant-product-page { min-height: calc(100vh - 84px); }
.option-alert { margin-bottom: 16px; }
.option-alert-content { display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 16px; }
.field-control { width: 100%; }
</style>
