<template>
  <div class="app-container merchant-publish-page">
    <div class="page-heading publish-heading">
      <div>
        <span class="page-eyebrow">PRODUCT PUBLISH</span>
        <h2>发布租赁商品</h2>
        <p>选择商户已开通的产品分类，完善商品信息后提交发布。</p>
      </div>
      <el-button link type="primary" icon="ArrowLeft" @click="backToProducts">返回我的商品</el-button>
    </div>

    <el-steps :active="activeStep" finish-status="success" align-center class="publish-steps">
      <el-step title="选择产品分类" description="选择商品所属的经营分类" />
      <el-step title="确认商品信息" description="填写商品和库存配置" />
      <el-step title="提交发布" description="完成商品发布" />
    </el-steps>

    <section v-if="activeStep === 0" class="publish-section">
      <el-alert type="info" :closable="false" show-icon class="publish-notice">
        <template #title>
          <strong>发布须知</strong>
        </template>
        <ol>
          <li>请选择与商品实际用途相符的产品分类，分类会影响商品展示和后续业务统计。</li>
          <li>找不到对应分类、品牌或型号时，请先联系平台管理员维护基础资料。</li>
        </ol>
        <el-button link type="primary" @click="openClassSettings">前往经营分类设置</el-button>
      </el-alert>

      <div class="category-toolbar">
        <div class="category-search">
          <el-input
            v-model="searchKeyword"
            clearable
            size="large"
            placeholder="请输入商品名称或分类关键词搜索"
            @keyup.enter="selectFirstSearchResult"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
            <template #append><el-button icon="Search" @click="selectFirstSearchResult">搜索</el-button></template>
          </el-input>
        </div>
        <el-radio-group v-model="form.productType" class="product-type-group" @change="handleProductTypeChange">
          <el-radio :value="RENTAL_CODE.PRODUCT_TYPE.DEVICE">设备商品</el-radio>
          <el-radio :value="RENTAL_CODE.PRODUCT_TYPE.ACCESSORY">配件商品</el-radio>
        </el-radio-group>
      </div>

      <div v-if="recentClasses.length" class="recent-section">
        <div class="section-title-row compact">
          <div>
            <h3>最近使用的分类</h3>
            <span>快速进入常用发布分类</span>
          </div>
          <el-button link type="info" @click="clearRecentClasses">清空记录</el-button>
        </div>
        <div class="recent-class-list">
          <button v-for="item in recentClasses" :key="item.classId" type="button" class="recent-class-item" @click="selectRecentClass(item)">
            {{ item.pathLabel }}
          </button>
        </div>
      </div>

      <el-card v-loading="optionLoading" shadow="never" class="category-card">
        <div v-if="searchKeyword.trim()" class="search-result-panel">
          <div class="subsection-title">分类搜索结果</div>
          <button
            v-for="item in searchResults"
            :key="item.classId"
            type="button"
            class="search-result-item"
            @click="selectSearchClass(item)"
          >
            <span>{{ item.pathLabel }}</span>
            <small>{{ item.classCode || '未设置分类编码' }}</small>
          </button>
          <el-empty v-if="searchResults.length === 0" :image-size="64" description="没有匹配的经营分类" />
        </div>

        <div v-else class="category-browser">
          <div v-for="column in categoryColumns" :key="column.key" class="category-column">
            <div class="column-search">
              <el-input
                v-model="columnSearchKeywords[column.key]"
                clearable
                size="large"
                :placeholder="`搜索${column.title}`"
              >
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
            </div>
            <div class="column-options">
              <template v-if="column.kind === 'category'">
                <button
                  v-for="item in column.items"
                  :key="item.classId"
                  type="button"
                  class="category-option"
                  :class="{ 'is-active': Number(selectedPath[column.index]) === Number(item.classId), 'is-selected': Number(selectedClassId) === Number(item.classId) }"
                  @click="selectClass(item, column.index)"
                >
                  <span class="category-option-name">{{ item.className }}</span>
                  <el-icon v-if="column.key === 'level1' && item.children?.length"><ArrowRight /></el-icon>
                  <el-icon v-if="Number(selectedClassId) === Number(item.classId)" class="selected-icon"><Check /></el-icon>
                </button>
              </template>
              <template v-else-if="column.kind === 'brand'">
                <button
                  v-for="item in column.items"
                  :key="item.brand"
                  type="button"
                  class="category-option"
                  :class="{ 'is-active': selectedBrand === item.brand }"
                  @click="selectBrand(item)"
                >
                  <span class="category-option-name">{{ item.brand }}</span>
                  <small class="option-count">{{ item.modelCount }} 个型号</small>
                </button>
              </template>
              <template v-else>
                <button
                  v-for="item in column.items"
                  :key="item.modelId"
                  type="button"
                  class="category-option"
                  :class="{ 'is-active': selectedBrand === item.brand, 'is-selected': Number(form.modelId) === Number(item.modelId) }"
                  @click="selectModel(item)"
                >
                  <span class="category-option-name">
                    <strong>{{ item.modelName }}</strong>
                  </span>
                  <el-icon v-if="Number(form.modelId) === Number(item.modelId)" class="selected-icon"><Check /></el-icon>
                </button>
              </template>
              <el-empty v-if="column.items.length === 0 && column.emptyText" :image-size="56" :description="column.emptyText" />
            </div>
          </div>
        </div>

        <div class="brand-model-apply-hint">
          <span>未找到需要品牌/型号，</span>
          <el-button link type="primary" @click="openBrandModelApplyDialog">点击申请</el-button>
        </div>

      </el-card>
    </section>

    <section v-else-if="activeStep === 1" class="publish-section">
      <div class="section-title-row">
        <div>
          <h3>确认商品信息</h3>
          <span>商品编码由系统统一生成，保存后不可修改</span>
        </div>
        <el-tag type="primary" effect="plain">{{ selectedClassLabel }}</el-tag>
      </div>

      <el-alert type="info" :closable="false" show-icon class="publish-notice">
        <template #title>商品信息会用于商品列表、库存和订单关联，请确认分类与标准型号匹配。</template>
      </el-alert>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="publish-form">
        <el-card shadow="never" class="form-card">
          <template #header><div class="form-card-title">基础信息</div></template>
          <el-row :gutter="24">
            <el-col :xs="24" :sm="12">
              <el-form-item label="商品名称" prop="productName">
                <el-input v-model="form.productName" maxlength="128" show-word-limit placeholder="请输入商品名称" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="产品分类">
                <el-input :model-value="selectedClassLabel" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="商品类型" prop="productType">
                <el-radio-group v-model="form.productType" @change="handleProductTypeChange">
                  <el-radio :value="RENTAL_CODE.PRODUCT_TYPE.DEVICE">设备商品</el-radio>
                  <el-radio :value="RENTAL_CODE.PRODUCT_TYPE.ACCESSORY">配件商品</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="标准型号" prop="modelId">
                <el-select v-model="form.modelId" clearable filterable class="field-control" placeholder="请选择标准型号" @change="handleModelChange">
                  <el-option v-for="item in filteredModels" :key="item.modelId" :label="modelOptionLabel(item)" :value="item.modelId" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <el-card shadow="never" class="form-card">
          <template #header><div class="form-card-title">规格与库存</div></template>
          <el-row :gutter="24">
            <el-col :xs="24" :sm="12">
              <el-form-item label="品牌">
                <el-input v-model="form.brand" maxlength="64" placeholder="选择标准型号后自动带出" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="商品型号">
                <el-input v-model="form.modelName" maxlength="128" placeholder="选择标准型号后自动带出" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="规格">
                <el-input v-model="form.memorySpec" maxlength="64" placeholder="例如：128G" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="库存模式" prop="stockMode">
                <el-select v-model="form.stockMode" class="field-control" placeholder="请选择库存模式">
                  <el-option v-for="item in stockModeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <el-card shadow="never" class="form-card">
          <template #header><div class="form-card-title">价格与状态</div></template>
          <el-row :gutter="24">
            <el-col :xs="24" :sm="12">
              <el-form-item label="采购成本">
                <el-input-number v-model="form.purchaseCost" :min="0" :precision="2" :step="0.01" controls-position="right" class="field-control" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="销售价格">
                <el-input-number v-model="form.salePrice" :min="0" :precision="2" :step="0.01" controls-position="right" class="field-control" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="商品状态" prop="status">
                <el-switch v-model="form.status" :active-value="NORMAL_DISABLE_CODE.NORMAL" :inactive-value="NORMAL_DISABLE_CODE.DISABLED" active-text="启用" inactive-text="停用" inline-prompt />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注">
                <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入商品备注" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>
      </el-form>
    </section>

    <section v-else class="publish-success-section">
      <el-result icon="success" title="商品发布成功" sub-title="商品已经保存到当前商户的商品列表，可以继续维护库存或发布其他商品。">
        <template #extra>
          <div class="success-summary">
            <div><span>商品名称</span><strong>{{ publishedProduct.productName || form.productName }}</strong></div>
            <div><span>商品编码</span><strong>{{ publishedProduct.productCode || '系统已生成' }}</strong></div>
            <div><span>产品分类</span><strong>{{ selectedClassLabel }}</strong></div>
          </div>
          <div class="success-actions">
            <el-button type="primary" @click="backToProducts">查看我的商品</el-button>
            <el-button @click="startAnotherPublish">继续发布</el-button>
          </div>
        </template>
      </el-result>
    </section>

    <el-dialog
      v-model="brandModelApplyVisible"
      title="品牌型号申请"
      width="760px"
      append-to-body
      destroy-on-close
      class="brand-model-apply-dialog"
    >
      <el-form ref="brandModelApplyFormRef" :model="brandModelApplyForm" :rules="brandModelApplyRules" label-width="96px" class="apply-form">
        <el-form-item label="所属分类" prop="classPath">
          <el-cascader
            v-model="brandModelApplyForm.classPath"
            :options="classTree"
            :props="classCascaderProps"
            :show-all-levels="true"
            clearable
            filterable
            placeholder="请选择所属分类"
            class="apply-field"
          />
        </el-form-item>
        <el-form-item label="型号" prop="modelName">
          <el-input
            v-model="brandModelApplyForm.modelName"
            clearable
            maxlength="128"
            show-word-limit
            placeholder="请输入型号"
          />
          <div class="apply-field-tip">1、仅有中文型号填写中文名称，仅有英文型号填写英文名称，优先填写中文名称；2、纯阿拉伯数字直接填写。</div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="brandModelApplyForm.remark"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请输入备注"
          />
          <div class="apply-field-tip">请填写所申请品牌型号的说明。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="brandModelApplyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBrandModelApply">确定</el-button>
      </template>
    </el-dialog>

    <div v-if="activeStep < 2" class="publish-footer">
      <div v-if="activeStep === 0" class="footer-selection">
        <span class="selected-label">已选分类</span>
        <el-tag v-if="selectedClassLabel" type="primary" effect="plain" class="selected-class-value">{{ selectedClassLabel }}</el-tag>
        <span v-else class="placeholder-text">请选择分类</span>
      </div>
      <div class="footer-actions">
        <el-button v-if="activeStep === 1" icon="ArrowLeft" @click="activeStep = 0">上一步</el-button>
        <el-button v-if="activeStep === 0" type="primary" :disabled="!selectedClassId" icon="ArrowRight" @click="goToProductForm">
          确认分类，下一步
        </el-button>
        <el-button v-else v-hasPermi="['merchant:product:add']" type="primary" :loading="saving" icon="Check" @click="submitProduct">
          提交发布
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowRight, Check, Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { addMerchantProduct } from '@/api/merchant/rentalBusiness'
import { listBrandModelOptions } from '@/api/merchant/brandModel'
import { listBusinessProductClasses, listSelectedProductClassIds } from '@/api/merchant/productClass'
import { NORMAL_DISABLE_CODE } from '@/constants/options'
import { RENTAL_CODE } from '@/utils/rental/options'

const RECENT_CLASS_KEY = 'merchant-product-publish-recent-classes'

const router = useRouter()
const activeStep = ref(0)
const saving = ref(false)
const optionLoading = ref(false)
const formRef = ref(null)
const searchKeyword = ref('')
const classTree = ref([])
const modelOptions = ref([])
const selectedPath = ref([])
const selectedClassId = ref(undefined)
const selectedBrand = ref('')
const columnSearchKeywords = reactive({ level1: '', level2: '', brand: '', model: '' })
const brandModelApplyVisible = ref(false)
const brandModelApplyFormRef = ref(null)
const brandModelApplyForm = reactive({ classPath: [], modelName: '', remark: '' })
const classCascaderProps = {
  value: 'classId',
  label: 'className',
  children: 'children',
  checkStrictly: true,
  emitPath: true
}
const recentClassRecords = ref([])
const publishedProduct = ref({})

const stockModeOptions = [
  { label: '逐件', value: RENTAL_CODE.STOCK_MODE.SERIAL },
  { label: '数量', value: RENTAL_CODE.STOCK_MODE.QUANTITY }
]

const form = reactive(createEmptyForm())

const rules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  productType: [{ required: true, message: '请选择商品类型', trigger: 'change' }],
  modelId: [{ validator: validateModel, trigger: 'change' }],
  stockMode: [{ required: true, message: '请选择库存模式', trigger: 'change' }],
  status: [{ required: true, message: '请选择商品状态', trigger: 'change' }]
}

const brandModelApplyRules = {
  classPath: [{ required: true, message: '所属分类不能为空', trigger: 'change' }],
  modelName: [
    { required: true, message: '型号不能为空', trigger: 'blur' },
    { max: 128, message: '型号长度不能超过128个字符', trigger: 'blur' }
  ],
  remark: [{ max: 500, message: '备注长度不能超过500个字符', trigger: 'blur' }]
}

const categoryColumns = computed(() => {
  const selectedRoot = findClassNode(classTree.value, selectedPath.value[0])
  const selectedSecond = selectedRoot?.children?.find(item => Number(item.classId) === Number(selectedPath.value[1]))
  const modelScope = selectedSecond || selectedRoot
  const scopedModels = modelOptions.value.filter(item => isModelInCategoryScope(item, modelScope))
  const brands = Array.from(scopedModels.reduce((result, item) => {
    const brand = String(item.brand || '').trim()
    if (!brand) {
      return result
    }
    const current = result.get(brand) || { brand, modelCount: 0 }
    current.modelCount += 1
    result.set(brand, current)
    return result
  }, new Map()).values())
  const selectedBrandModels = selectedBrand.value
    ? scopedModels.filter(item => String(item.brand || '').trim() === selectedBrand.value)
    : []

  return [
    {
      key: 'level1',
      title: '一级分类',
      kind: 'category',
      index: 0,
      items: filterItems(classTree.value, columnSearchKeywords.level1, ['className', 'classCode']),
      emptyText: '暂无一级分类'
    },
    {
      key: 'level2',
      title: '二级分类',
      kind: 'category',
      index: 1,
      items: filterItems(selectedRoot?.children || [], columnSearchKeywords.level2, ['className', 'classCode']),
      emptyText: selectedRoot ? '暂无二级分类' : ''
    },
    {
      key: 'brand',
      title: '品牌',
      kind: 'brand',
      items: filterItems(brands, columnSearchKeywords.brand, ['brand']),
      emptyText: modelScope ? '暂无可选品牌' : ''
    },
    {
      key: 'model',
      title: '型号',
      kind: 'model',
      items: filterItems(selectedBrandModels, columnSearchKeywords.model, ['modelName', 'memorySpec']),
      emptyText: selectedBrand.value ? '暂无可选型号' : ''
    }
  ]
})

const flattenedClasses = computed(() => flattenClasses(classTree.value))

const searchResults = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return []
  }
  return flattenedClasses.value.filter(item => [item.pathLabel, item.className, item.classCode].some(value => String(value || '').toLowerCase().includes(keyword))).slice(0, 50)
})

const recentClasses = computed(() => {
  const available = new Map(flattenedClasses.value.map(item => [Number(item.classId), item]))
  return recentClassRecords.value.map(item => available.get(Number(item.classId)) || item).filter(item => item?.classId).slice(0, 8)
})

const selectedClassLabel = computed(() => {
  const item = flattenedClasses.value.find(item => Number(item.classId) === Number(selectedClassId.value))
  return item?.pathLabel || ''
})

const filteredModels = computed(() => {
  if (!form.classId) {
    return modelOptions.value
  }
  return modelOptions.value.filter(item => Number(item.classId) === Number(form.classId))
})

function createEmptyForm() {
  return {
    productName: '',
    classId: undefined,
    modelId: undefined,
    productType: RENTAL_CODE.PRODUCT_TYPE.DEVICE,
    stockMode: RENTAL_CODE.STOCK_MODE.SERIAL,
    brand: '',
    modelName: '',
    memorySpec: '',
    purchaseCost: undefined,
    salePrice: undefined,
    status: NORMAL_DISABLE_CODE.NORMAL,
    remark: ''
  }
}

async function loadOptions() {
  optionLoading.value = true
  try {
    const [classResponse, selectedResponse, modelResponse] = await Promise.all([
      listBusinessProductClasses(),
      listSelectedProductClassIds(),
      listBrandModelOptions()
    ])
    classTree.value = responseData(classResponse) || []
    const selectedClassIds = new Set((responseData(selectedResponse) || []).map(Number))
    if (!selectedClassIds.size) {
      classTree.value = []
    }
    modelOptions.value = responseData(modelResponse) || []
    loadRecentClasses()
  } finally {
    optionLoading.value = false
  }
}

function flattenClasses(nodes, parents = [], result = []) {
  nodes.forEach(node => {
    const pathIds = parents.map(item => item.classId).concat(node.classId)
    const pathNames = parents.map(item => item.className).concat(node.className)
    const current = {
      classId: node.classId,
      className: node.className,
      classCode: node.classCode,
      pathIds,
      pathLabel: pathNames.join(' / '),
      children: node.children || []
    }
    result.push(current)
    flattenClasses(node.children || [], parents.concat(current), result)
  })
  return result
}

function findClassNode(nodes, classId) {
  if (classId === undefined || classId === null || classId === '') {
    return undefined
  }
  for (const node of nodes) {
    if (Number(node.classId) === Number(classId)) {
      return node
    }
    const child = findClassNode(node.children || [], classId)
    if (child) {
      return child
    }
  }
  return undefined
}

function isModelInCategoryScope(model, scopeNode) {
  if (!scopeNode) {
    return false
  }
  return Boolean(findClassNode([scopeNode], model.classId))
}

function filterItems(items, keyword, fields) {
  const normalizedKeyword = String(keyword || '').trim().toLowerCase()
  if (!normalizedKeyword) {
    return items
  }
  return items.filter(item => fields.some(field => String(item[field] || '').toLowerCase().includes(normalizedKeyword)))
}

function selectClass(item, columnIndex) {
  selectedPath.value = columnIndex === 0
    ? [item.classId]
    : [selectedPath.value[0], item.classId]
  selectedClassId.value = item.classId
  form.classId = item.classId
  selectedBrand.value = ''
  columnSearchKeywords.brand = ''
  columnSearchKeywords.model = ''
  if (form.modelId && !filteredModels.value.some(model => Number(model.modelId) === Number(form.modelId))) {
    clearModelSnapshot()
  }
}

function selectSearchClass(item) {
  selectedPath.value = item.pathIds
  selectedClassId.value = item.classId
  form.classId = item.classId
  searchKeyword.value = ''
  selectedBrand.value = ''
  columnSearchKeywords.brand = ''
  columnSearchKeywords.model = ''
  if (form.modelId && !filteredModels.value.some(model => Number(model.modelId) === Number(form.modelId))) {
    clearModelSnapshot()
  }
}

function selectRecentClass(item) {
  const classRecord = flattenedClasses.value.find(option => Number(option.classId) === Number(item.classId)) || item
  selectedPath.value = classRecord.pathIds || [classRecord.classId]
  selectedClassId.value = classRecord.classId
  form.classId = classRecord.classId
  searchKeyword.value = ''
  selectedBrand.value = ''
  columnSearchKeywords.brand = ''
  columnSearchKeywords.model = ''
}

function selectBrand(item) {
  selectedBrand.value = item.brand
  columnSearchKeywords.model = ''
}

function selectModel(item) {
  const classRecord = flattenedClasses.value.find(option => Number(option.classId) === Number(item.classId))
  if (classRecord) {
    selectedPath.value = classRecord.pathIds
    selectedClassId.value = classRecord.classId
    form.classId = classRecord.classId
  }
  selectedBrand.value = item.brand || ''
  form.modelId = item.modelId
  handleModelChange(item.modelId)
}

function selectFirstSearchResult() {
  if (searchResults.value.length) {
    selectSearchClass(searchResults.value[0])
  }
}

function openBrandModelApplyDialog() {
  brandModelApplyForm.classPath = selectedPath.value.length ? [...selectedPath.value] : []
  brandModelApplyForm.modelName = ''
  brandModelApplyForm.remark = ''
  brandModelApplyVisible.value = true
}

function submitBrandModelApply() {
  brandModelApplyFormRef.value?.validate(valid => {
    if (!valid) {
      return
    }
    ElMessage.warning('品牌型号申请接口尚未接入，请联系平台管理员录入')
  })
}

function goToProductForm() {
  if (!selectedClassId.value) {
    ElMessage.warning('请选择商品发布分类')
    return
  }
  form.classId = selectedClassId.value
  activeStep.value = 1
}

function handleProductTypeChange(productType) {
  form.stockMode = productType === RENTAL_CODE.PRODUCT_TYPE.DEVICE
    ? RENTAL_CODE.STOCK_MODE.SERIAL
    : RENTAL_CODE.STOCK_MODE.QUANTITY
}

function handleModelChange(modelId) {
  const model = modelOptions.value.find(item => Number(item.modelId) === Number(modelId))
  if (!model) {
    return
  }
  form.brand = model.brand || ''
  form.modelName = model.modelName || ''
  form.memorySpec = model.memorySpec || ''
}

function validateModel(_rule, value, callback) {
  if (form.productType === RENTAL_CODE.PRODUCT_TYPE.DEVICE && !value) {
    callback(new Error('设备商品必须选择标准型号'))
    return
  }
  callback()
}

async function submitProduct() {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }
  saving.value = true
  try {
    const response = await addMerchantProduct({ ...form })
    if (response && response.code !== undefined && response.code !== 200) {
      return
    }
    publishedProduct.value = responseData(response) || { ...form }
    saveRecentClass()
    activeStep.value = 2
    ElMessage.success('商品发布成功')
  } finally {
    saving.value = false
  }
}

function saveRecentClass() {
  const current = flattenedClasses.value.find(item => Number(item.classId) === Number(selectedClassId.value))
  if (!current) {
    return
  }
  const record = {
    classId: current.classId,
    className: current.className,
    classCode: current.classCode,
    pathIds: current.pathIds,
    pathLabel: current.pathLabel
  }
  const records = [record, ...recentClassRecords.value.filter(item => Number(item.classId) !== Number(current.classId))].slice(0, 8)
  recentClassRecords.value = records
  localStorage.setItem(RECENT_CLASS_KEY, JSON.stringify(records))
}

function loadRecentClasses() {
  try {
    recentClassRecords.value = JSON.parse(localStorage.getItem(RECENT_CLASS_KEY) || '[]')
  } catch {
    recentClassRecords.value = []
  }
}

function clearRecentClasses() {
  recentClassRecords.value = []
  localStorage.removeItem(RECENT_CLASS_KEY)
}

function clearModelSnapshot() {
  form.modelId = undefined
  form.brand = ''
  form.modelName = ''
  form.memorySpec = ''
}

function startAnotherPublish() {
  Object.assign(form, createEmptyForm())
  selectedPath.value = []
  selectedClassId.value = undefined
  selectedBrand.value = ''
  Object.assign(columnSearchKeywords, { level1: '', level2: '', brand: '', model: '' })
  searchKeyword.value = ''
  publishedProduct.value = {}
  activeStep.value = 0
}

function openClassSettings() {
  router.push('/merchant/product/class')
}

function backToProducts() {
  router.push('/merchant/product/my')
}

function modelOptionLabel(model) {
  return [model.brand, model.modelName, model.memorySpec].filter(Boolean).join(' / ')
}

function responseRows(response) {
  const body = response?.data && !Array.isArray(response.data) ? response.data : response
  return Array.isArray(body) ? body : body?.rows || []
}

function responseData(response) {
  return response?.data !== undefined ? response.data : response
}

onMounted(loadOptions)
</script>

<style scoped>
.merchant-publish-page {
  min-height: calc(100vh - 84px);
  padding-bottom: 92px;
  background: var(--el-bg-color-page);
}

.publish-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.page-eyebrow {
  display: block;
  margin-bottom: 4px;
  color: var(--el-color-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 2px;
}

.publish-heading h2 {
  margin: 0;
}

.publish-heading p {
  margin: 8px 0 0;
  color: var(--el-text-color-secondary);
}

.publish-steps {
  max-width: 1120px;
  margin: 0 auto 24px;
  padding: 20px 12px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
}

.publish-section,
.publish-success-section {
  max-width: 1280px;
  margin: 0 auto;
}

.publish-section {
  --category-row-height: var(--el-component-size-large);
}

.publish-notice {
  margin-bottom: 24px;
}

.publish-notice ol {
  margin: 10px 0 8px;
  padding-left: 20px;
  color: var(--el-text-color-regular);
  line-height: 1.9;
}

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin: 0 0 16px;
}

.section-title-row.compact {
  margin-bottom: 12px;
}

.section-title-row h3 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 18px;
}

.section-title-row span {
  display: block;
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.product-type-group {
  flex-shrink: 0;
}

.category-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 0 0 20px;
}

.category-card,
.form-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.category-search {
  flex: 1;
  min-width: 0;
  width: 100%;
  max-width: none;
  height: var(--category-row-height);
  margin: 0;
}

.category-search :deep(.el-input),
.category-search :deep(.el-input__wrapper),
.category-search :deep(.el-input-group__append) {
  height: 100%;
  box-sizing: border-box;
}

.category-browser {
  display: grid;
  grid-template-columns: repeat(4, minmax(180px, 1fr));
  min-height: 460px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  overflow: hidden;
}

.category-column {
  display: flex;
  flex-direction: column;
  min-width: 0;
  border-right: 1px solid var(--el-border-color-lighter);
}

.category-column:last-child {
  border-right: 0;
}

.column-search {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  height: calc(var(--category-row-height) + 16px);
  padding: 8px;
  position: sticky;
  top: 0;
  z-index: 2;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  box-sizing: border-box;
}

.column-search :deep(.el-input),
.column-search :deep(.el-input__wrapper) {
  height: 100%;
  box-sizing: border-box;
}

.column-search :deep(.el-input__wrapper) {
  min-height: 0;
}

.column-options {
  flex: 1;
  min-height: 340px;
  overflow-y: auto;
  padding: 8px;
}

.category-option,
.search-result-item,
.recent-class-item {
  border: 0;
  font: inherit;
  cursor: pointer;
}

.category-option {
  display: flex;
  align-items: center;
  width: 100%;
  height: var(--category-row-height);
  min-height: var(--category-row-height);
  padding: 8px 10px;
  color: var(--el-text-color-regular);
  text-align: left;
  background: transparent;
  border-radius: 4px;
  box-sizing: border-box;
}

.category-option:hover,
.category-option.is-active {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.category-option.is-selected {
  font-weight: 600;
}

.category-option-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-option-name strong {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-option-name {
  line-height: 1.35;
}

.option-count {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 400;
}

.option-count {
  flex-shrink: 0;
  margin-left: 8px;
}

.selected-icon {
  margin-left: 4px;
  color: var(--el-color-primary);
}

.brand-model-apply-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.apply-form {
  padding: 12px 28px 4px 8px;
}

.apply-field {
  width: 100%;
}

.apply-field-tip {
  width: 100%;
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.brand-model-apply-dialog :deep(.el-dialog__body) {
  padding-top: 18px;
  padding-bottom: 12px;
}

.selected-class-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 48px;
  margin-top: 18px;
  padding: 10px 14px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-fill-color-extra-light);
}

.selected-label {
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
}

.placeholder-text {
  color: var(--el-text-color-placeholder);
}

.search-result-panel {
  min-height: 390px;
  padding: 4px 8px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
}

.subsection-title {
  padding: 12px 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.search-result-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 12px 8px;
  color: var(--el-text-color-regular);
  text-align: left;
  background: transparent;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.search-result-item:hover {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.search-result-item small {
  color: var(--el-text-color-placeholder);
}

.recent-section {
  margin: 0 0 20px;
}

.recent-class-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.recent-class-item {
  padding: 10px 14px;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
}

.recent-class-item:hover {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-5);
}

.form-card-title {
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 600;
}

.field-control {
  width: 100%;
}

.publish-success-section {
  padding: 24px 0 48px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
}

.success-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  max-width: 760px;
  margin: 0 auto 24px;
  padding: 16px;
  text-align: left;
  background: var(--el-fill-color-light);
  border-radius: 6px;
}

.success-summary div {
  min-width: 0;
}

.success-summary span,
.success-summary strong {
  display: block;
}

.success-summary span {
  margin-bottom: 6px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.success-summary strong {
  overflow: hidden;
  color: var(--el-text-color-primary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.publish-footer {
  position: sticky;
  bottom: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  max-width: 1280px;
  margin: 24px auto 0;
  padding: 16px 0;
  background: color-mix(in srgb, var(--el-bg-color-page) 94%, transparent);
  border-top: 1px solid var(--el-border-color-light);
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.04);
}

.footer-selection {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.selected-class-value {
  max-width: min(720px, 70vw);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.footer-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 12px;
}

@media (max-width: 1000px) {
  .category-browser {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }

  .category-column:nth-child(2) {
    border-right: 0;
  }

  .category-column:nth-child(n + 3) {
    border-top: 1px solid var(--el-border-color-lighter);
  }

  .success-summary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .publish-heading,
  .section-title-row,
  .category-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .category-search,
  .product-type-group {
    width: 100%;
  }

  .category-browser {
    grid-template-columns: 1fr;
  }

  .category-column {
    border-right: 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .category-column:last-child {
    border-bottom: 0;
  }

  .publish-footer {
    align-items: stretch;
    flex-direction: column;
    padding: 12px 0;
  }

  .footer-selection,
  .footer-actions {
    width: 100%;
  }

  .footer-actions {
    justify-content: flex-end;
  }
}
</style>
