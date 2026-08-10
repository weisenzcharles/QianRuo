<template>
  <div class="app-container business-crud-page">
    <div class="page-heading">
      <div>
        <h2>{{ config.title }}</h2>
        <p>{{ config.description }}</p>
      </div>
      <el-button v-if="config.editable" type="primary" icon="Plus" @click="openCreate">新增</el-button>
      <el-button v-if="props.resource === 'inventory'" type="primary" icon="Edit" @click="openInventoryAdjust">调整库存</el-button>
    </div>

    <el-form :inline="true" :model="query" class="search-form" @submit.prevent>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="编码、名称或型号" @keyup.enter="load" />
      </el-form-item>
      <el-form-item v-if="config.queryProductType" label="商品类型">
        <el-select v-model="query.productType" clearable placeholder="全部" @change="load">
          <el-option label="设备" value="device" />
          <el-option label="配件" value="accessory" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="config.queryStatus" label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" @change="load">
          <el-option label="启用" :value="0" />
          <el-option label="停用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="load">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" row-key="id" border>
      <el-table-column v-for="column in config.columns" :key="column.key" :label="column.label" :prop="column.key" :min-width="column.width || 120">
        <template #default="scope">
          <el-tag v-if="column.tag" :type="tagType(scope.row[column.key])">{{ displayValue(column, scope.row[column.key]) }}</el-tag>
          <span v-else>{{ displayValue(column, scope.row[column.key]) }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="config.editable || config.canRemove" label="操作" fixed="right" width="170">
        <template #default="scope">
          <el-button v-if="config.editable" link type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-button v-if="config.editable || config.canRemove" link type="danger" @click="removeRow(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />

    <el-dialog v-model="dialogVisible" :title="formTitle" width="720px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="120px">
        <el-row :gutter="16">
          <el-col v-for="field in config.fields" :key="field.key" :span="field.span || 12">
            <el-form-item :label="field.label" :required="field.required">
              <el-select v-if="field.type === 'select'" v-model="form[field.key]" clearable :placeholder="field.placeholder || `请选择${field.label}`" class="field-control">
                <el-option v-for="option in field.options" :key="String(option.value)" :label="option.label" :value="option.value" />
              </el-select>
              <el-input-number v-else-if="field.type === 'number'" v-model="form[field.key]" :min="field.min" :max="field.max" controls-position="right" class="field-control" />
              <el-input v-else-if="field.type === 'textarea'" v-model="form[field.key]" type="textarea" :rows="field.rows || 4" :placeholder="field.placeholder" />
              <el-input v-else v-model="form[field.key]" :disabled="field.systemGenerated" :placeholder="field.placeholder || `请输入${field.label}`" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-alert v-if="config.packageHelp && !form.components" title="套餐组件格式：JSON 数组，填写 poolId、componentRole、requiredQty" type="info" :closable="false" />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="inventoryDialogVisible" title="调整数量库存" width="560px" append-to-body>
      <el-form :model="inventoryForm" label-width="110px">
        <el-form-item label="商品 ID" required><el-input-number v-model="inventoryForm.productId" :min="1" class="field-control" /></el-form-item>
        <el-form-item label="仓库 ID" required><el-input-number v-model="inventoryForm.warehouseId" :min="1" class="field-control" /></el-form-item>
        <el-form-item label="变更数量" required><el-input-number v-model="inventoryForm.changeQty" controls-position="right" class="field-control" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="inventoryForm.reason" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inventoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitInventory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addMerchantAccessory,
  addMerchantPackage,
  addMerchantPool,
  addMerchantProduct,
  addMerchantSn,
  addMerchantWarehouse,
  adjustMerchantInventory,
  deleteMerchantAccessory,
  deleteMerchantInventory,
  deleteMerchantPackage,
  deleteMerchantPool,
  deleteMerchantProduct,
  deleteMerchantSn,
  deleteMerchantWarehouse,
  listMerchantAccessories,
  listMerchantInventory,
  listMerchantPackages,
  listMerchantPools,
  listMerchantProducts,
  listMerchantSns,
  listMerchantWarehouses,
  updateMerchantAccessory,
  updateMerchantPackage,
  updateMerchantPool,
  updateMerchantProduct,
  updateMerchantSn,
  updateMerchantWarehouse
} from '@/api/merchant/rentalBusiness'

const props = defineProps({
  resource: { type: String, required: true }
})

const statusOptions = [{ label: '启用', value: 0 }, { label: '停用', value: 1 }]
const enabledOptions = [{ label: '启用', value: 1 }, { label: '停用', value: 0 }]
const booleanOptions = [{ label: '是', value: 1 }, { label: '否', value: 0 }]
const resourceOptions = [{ label: '设备', value: 'device' }, { label: '配件', value: 'accessory' }, { label: '耗材', value: 'consumable' }]
const productTypeOptions = [{ label: '设备', value: 'device' }, { label: '配件', value: 'accessory' }]
const stockModeOptions = [{ label: '逐件/SN', value: 'serial' }, { label: '数量', value: 'quantity' }]
const assetStatusOptions = ['available', 'reserved', 'rented', 'repairing', 'lost', 'retired'].map(value => ({ label: value, value }))

const configs = {
  warehouse: {
    title: '仓库管理', description: '维护商户仓库，所有操作受当前用户仓库权限约束。', editable: true, queryStatus: true,
    columns: [{ key: 'code', label: '仓库编码' }, { key: 'name', label: '仓库名称' }, { key: 'city', label: '城市' }, { key: 'contactName', label: '负责人' }, { key: 'status', label: '状态', tag: true }],
    fields: [{ key: 'code', label: '仓库编码', systemGenerated: true, placeholder: '保存后由系统自动生成' }, { key: 'name', label: '仓库名称', required: true }, { key: 'contactName', label: '负责人' }, { key: 'contactPhone', label: '联系电话' }, { key: 'city', label: '城市' }, { key: 'address', label: '详细地址', span: 24 }, { key: 'status', label: '状态', type: 'select', options: statusOptions }, { key: 'remark', label: '备注', type: 'textarea', span: 24 }]
  },
  pool: {
    title: '库存资源池', description: '用资源池统一约束可替代库存，避免套餐切换时重复统计。', editable: true, queryStatus: true,
    columns: [{ key: 'poolCode', label: '资源池编码' }, { key: 'poolName', label: '资源池名称' }, { key: 'resourceType', label: '资源类型' }, { key: 'compatibleModelIds', label: '兼容型号' }, { key: 'status', label: '状态', tag: true }],
    fields: [{ key: 'poolCode', label: '资源池编码', systemGenerated: true, placeholder: '保存后由系统自动生成' }, { key: 'poolName', label: '资源池名称', required: true }, { key: 'resourceType', label: '资源类型', type: 'select', required: true, options: resourceOptions }, { key: 'substitutableFlag', label: '允许替代', type: 'select', options: booleanOptions }, { key: 'compatibleModelIds', label: '兼容型号 ID', placeholder: '多个 ID 用逗号分隔', span: 24 }, { key: 'status', label: '状态', type: 'select', options: statusOptions }, { key: 'remark', label: '备注', type: 'textarea', span: 24 }]
  },
  product: {
    title: '商品管理', description: '商品是库存和套餐的基础 SKU，商品类型与库存模式有严格约束。', editable: true, queryStatus: true, queryProductType: true,
    columns: [{ key: 'productCode', label: '商品编码' }, { key: 'productName', label: '商品名称', width: 180 }, { key: 'productType', label: '类型' }, { key: 'stockMode', label: '库存模式' }, { key: 'brand', label: '品牌' }, { key: 'modelName', label: '型号' }, { key: 'status', label: '状态', tag: true }],
    fields: [{ key: 'productCode', label: '商品编码', systemGenerated: true, placeholder: '保存后由系统自动生成' }, { key: 'productName', label: '商品名称', required: true }, { key: 'classId', label: '类目 ID', type: 'number', required: true, min: 1 }, { key: 'modelId', label: '型号 ID', type: 'number', min: 1 }, { key: 'poolId', label: '资源池 ID', type: 'number', required: true, min: 1 }, { key: 'productType', label: '商品类型', type: 'select', required: true, options: productTypeOptions }, { key: 'stockMode', label: '库存模式', type: 'select', required: true, options: stockModeOptions }, { key: 'brand', label: '品牌' }, { key: 'modelName', label: '型号' }, { key: 'memorySpec', label: '规格' }, { key: 'salePrice', label: '销售价', type: 'number', min: 0 }, { key: 'status', label: '状态', type: 'select', options: statusOptions }, { key: 'remark', label: '备注', type: 'textarea', span: 24 }]
  },
  sn: {
    title: 'SN 管理', description: '逐件商品通过 SN 管理生命周期；已租用或预占的 SN 不允许直接删除。', editable: true,
    columns: [{ key: 'snCode', label: 'SN 编码', width: 180 }, { key: 'productName', label: '商品' }, { key: 'warehouseName', label: '仓库' }, { key: 'assetStatus', label: '状态', tag: true }, { key: 'conditionLevel', label: '成色' }],
    fields: [{ key: 'productId', label: '商品 ID', type: 'number', required: true, min: 1 }, { key: 'warehouseId', label: '仓库 ID', type: 'number', required: true, min: 1 }, { key: 'snCode', label: 'SN 编码', required: true }, { key: 'barcode', label: '条码' }, { key: 'assetStatus', label: '资产状态', type: 'select', options: assetStatusOptions }, { key: 'conditionLevel', label: '成色' }, { key: 'remark', label: '备注', type: 'textarea', span: 24 }]
  },
  accessory: {
    title: '配件实例', description: '逐件配件单独建实例；数量型通用配件请在库存管理中调整总量。', editable: true,
    columns: [{ key: 'itemCode', label: '配件编码', width: 180 }, { key: 'productName', label: '商品' }, { key: 'warehouseName', label: '仓库' }, { key: 'itemStatus', label: '状态', tag: true }],
    fields: [{ key: 'productId', label: '商品 ID', type: 'number', required: true, min: 1 }, { key: 'warehouseId', label: '仓库 ID', type: 'number', required: true, min: 1 }, { key: 'itemCode', label: '配件编码', systemGenerated: true, placeholder: '保存后由系统自动生成' }, { key: 'itemStatus', label: '状态', type: 'select', options: assetStatusOptions }, { key: 'remark', label: '备注', type: 'textarea', span: 24 }]
  },
  package: {
    title: '套餐管理', description: '套餐按版本化 BOM 保存，组件按资源池校验，可在不重复统计的前提下切换组合。', editable: true, packageHelp: true,
    columns: [{ key: 'packageCode', label: '套餐编码' }, { key: 'packageName', label: '套餐名称', width: 180 }, { key: 'platformCode', label: '订单渠道' }, { key: 'versionNo', label: '当前版本' }, { key: 'enabled', label: '状态', tag: true }],
    fields: [{ key: 'packageCode', label: '套餐编码', systemGenerated: true, placeholder: '保存后由系统自动生成' }, { key: 'packageName', label: '套餐名称', required: true }, { key: 'platformCode', label: '订单渠道' }, { key: 'enabled', label: '启用状态', type: 'select', options: enabledOptions }, { key: 'sortNo', label: '排序', type: 'number', min: 0 }, { key: 'components', label: 'BOM 组件 JSON', type: 'textarea', required: true, span: 24, rows: 6 }, { key: 'remark', label: '备注', type: 'textarea', span: 24 }]
  },
  inventory: {
    title: '库存管理', description: '数量库存通过余额和流水维护，逐件库存请进入 SN 或配件实例管理。', editable: false, canRemove: true,
    columns: [{ key: 'productCode', label: '商品编码' }, { key: 'productName', label: '商品名称', width: 180 }, { key: 'warehouseName', label: '仓库' }, { key: 'stockMode', label: '库存模式' }, { key: 'onHandQty', label: '在库数量' }, { key: 'reservedQty', label: '已预占' }, { key: 'availableQty', label: '可用数量' }]
  }
}

const apiMap = {
  warehouse: { list: listMerchantWarehouses, add: addMerchantWarehouse, update: updateMerchantWarehouse, remove: deleteMerchantWarehouse },
  pool: { list: listMerchantPools, add: addMerchantPool, update: updateMerchantPool, remove: deleteMerchantPool },
  product: { list: listMerchantProducts, add: addMerchantProduct, update: updateMerchantProduct, remove: deleteMerchantProduct },
  sn: { list: listMerchantSns, add: addMerchantSn, update: updateMerchantSn, remove: deleteMerchantSn },
  accessory: { list: listMerchantAccessories, add: addMerchantAccessory, update: updateMerchantAccessory, remove: deleteMerchantAccessory },
  package: { list: listMerchantPackages, add: addMerchantPackage, update: updateMerchantPackage, remove: deleteMerchantPackage },
  inventory: { list: listMerchantInventory, remove: row => deleteMerchantInventory(row.productId, row.warehouseId) }
}

const config = computed(() => configs[props.resource])
const api = computed(() => apiMap[props.resource])
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const inventoryDialogVisible = ref(false)
const editing = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: undefined, productType: undefined })
const form = reactive({})
const inventoryForm = reactive({ productId: undefined, warehouseId: undefined, changeQty: undefined, reason: '' })
const formTitle = computed(() => `${editing.value ? '修改' : '新增'}${config.value.title}`)

/** 加载当前资源分页数据。 */
function load() {
  loading.value = true
  api.value.list(query).then(response => {
    rows.value = response.rows || response.data?.rows || response.data || []
    total.value = response.total || response.data?.total || 0
  }).finally(() => { loading.value = false })
}

/** 清空查询条件并重新加载列表。 */
function resetQuery() {
  query.pageNum = 1
  query.keyword = ''
  query.status = undefined
  query.productType = undefined
  load()
}

/** 打开新增表单并填充资源默认值。 */
function openCreate() {
  editing.value = false
  Object.keys(form).forEach(key => delete form[key])
  config.value.fields.forEach(field => { form[field.key] = field.key === 'components' ? '[]' : undefined })
  if (props.resource === 'pool') form.substitutableFlag = 1
  if (props.resource === 'sn' || props.resource === 'accessory') form.assetStatus = 'available'
  dialogVisible.value = true
}

/** 打开编辑表单并复制当前行，防止表格数据被表单双向修改。 */
function openEdit(row) {
  editing.value = true
  Object.keys(form).forEach(key => delete form[key])
  form.id = row.id
  config.value.fields.forEach(field => { form[field.key] = row[field.key] })
  if (props.resource === 'package') form.components = JSON.stringify(row.components || [], null, 2)
  dialogVisible.value = true
}

/** 提交新增或修改命令，并转换兼容型号和套餐 BOM 的输入格式。 */
function submit() {
  const data = { ...form }
  data.id = editing.value ? form.id : undefined
  if (props.resource === 'pool') data.compatibleModelIds = parseIds(data.compatibleModelIds)
  if (props.resource === 'package') {
    try { data.components = JSON.parse(data.components || '[]') } catch (error) { ElMessage.error('BOM 组件 JSON 格式不正确'); return }
  }
  saving.value = true
  const action = editing.value ? api.value.update : api.value.add
  action(data).then(() => { ElMessage.success('保存成功'); dialogVisible.value = false; load() }).finally(() => { saving.value = false })
}

/** 删除当前行，后端仍会校验引用关系并执行软删除。 */
function removeRow(row) {
  const removeAction = () => props.resource === 'inventory' ? api.value.remove(row) : api.value.remove(row.id)
  ElMessageBox.confirm(`确认删除“${row.name || row.productName || row.packageName || row.code || row.itemCode || row.snCode || row.productCode}”吗？`, '操作确认', { type: 'warning' }).then(removeAction).then(() => { ElMessage.success('删除成功'); load() })
}

/** 打开数量库存调整表单。 */
function openInventoryAdjust() {
  Object.assign(inventoryForm, { productId: undefined, warehouseId: undefined, changeQty: undefined, reason: '' })
  inventoryDialogVisible.value = true
}

/** 提交数量库存调整并写入流水。 */
function submitInventory() {
  saving.value = true
  adjustMerchantInventory(inventoryForm).then(() => { ElMessage.success('库存调整成功'); inventoryDialogVisible.value = false; load() }).finally(() => { saving.value = false })
}

/** 转换逗号分隔的主键输入。 */
function parseIds(value) {
  if (Array.isArray(value)) return value
  return String(value || '').split(',').map(item => Number(item.trim())).filter(item => Number.isInteger(item) && item > 0)
}

/** 将字典值转换成页面可读文本。 */
function displayValue(column, value) {
  if (value === undefined || value === null || value === '') return '-'
  if (column.key === 'status') return Number(value) === 0 ? '启用' : '停用'
  if (column.key === 'enabled') return Number(value) === 1 ? '启用' : '停用'
  if (column.key === 'compatibleModelIds' && Array.isArray(value)) return value.join(', ')
  return value
}

/** 根据状态值返回 Element Plus 标签颜色。 */
function tagType(value) {
  return Number(value) === 1 || ['rented', 'repairing', 'lost', 'retired'].includes(value) ? 'danger' : 'success'
}

load()
</script>

<style scoped>
.business-crud-page { min-height: calc(100vh - 84px); }
.page-heading { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 18px; }
.page-heading h2 { margin: 0; font-size: 20px; font-weight: 600; }
.page-heading p { margin: 8px 0 0; color: var(--el-text-color-secondary); }
.search-form { padding: 16px; margin-bottom: 16px; background: var(--el-fill-color-light); }
.field-control { width: 100%; }
</style>
