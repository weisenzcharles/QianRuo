<template>
  <div class="app-container merchant-list-page merchant-brand-model-page">
    <div class="page-toolbar">
      <div>
        <h2>常用品牌型号</h2>
        <span class="selection-count">已选择 {{ selectedIds.length }} 项，仅展示当前商户经营类目内的标准型号</span>
      </div>
    </div>

    <el-alert v-if="options.length === 0 && !loading" title="暂无可选型号，请先在经营类目中选择业务范围，或等待平台维护标准型号。" type="info" :closable="false" show-icon />
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" icon="Check" :loading="saving" @click="saveSelections" v-hasPermi="['merchant:brandModel:edit']">保存</el-button></el-col>
      <right-toolbar :search="false" @queryTable="loadOptions" />
    </el-row>
    <el-table ref="modelTableRef" v-loading="loading" :data="options" row-key="modelId" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" reserve-selection />
      <el-table-column label="产品类目" prop="className" min-width="160" show-overflow-tooltip />
      <el-table-column label="品牌" prop="brand" width="150" />
      <el-table-column label="型号" prop="modelName" min-width="180" />
      <el-table-column label="内存/规格" prop="memorySpec" min-width="150" show-overflow-tooltip />
      <el-table-column label="选择状态" width="100" align="center">
        <template #default="scope"><el-tag v-if="scope.row.selected" type="success">常用</el-tag><span v-else class="muted">未选择</span></template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup name="MerchantBrandModel">
import { listBrandModelOptions, listSelectedBrandModelIds, saveSelectedBrandModels } from '@/api/merchant/brandModel'

const { proxy } = getCurrentInstance()
const modelTableRef = ref()
const options = ref([])
const selectedIds = ref([])
const loading = ref(false)
const saving = ref(false)

/** 加载当前商户可见标准型号和常用选择状态。 */
function loadOptions() {
  loading.value = true
  Promise.all([listBrandModelOptions(), listSelectedBrandModelIds()]).then(([optionResponse, selectedResponse]) => {
    options.value = optionResponse.data || []
    const selected = new Set((selectedResponse.data || []).map(modelId => String(modelId)))
    selectedIds.value = options.value.filter(item => selected.has(String(item.modelId))).map(item => item.modelId)
    nextTick(() => {
      options.value.forEach(item => {
        if (selected.has(String(item.modelId))) {
          modelTableRef.value?.toggleRowSelection(item, true)
        }
      })
    })
  }).finally(() => { loading.value = false })
}

/** 同步表格勾选状态到待保存的标准型号主键集合。 */
function handleSelectionChange(rows) {
  selectedIds.value = rows.map(row => row.modelId)
}

/** 保存当前商户常用型号，服务端会再次校验类目和型号归属。 */
function saveSelections() {
  saving.value = true
  saveSelectedBrandModels(selectedIds.value).then(() => {
    proxy.$modal.msgSuccess('常用品牌型号保存成功')
    loadOptions()
  }).finally(() => { saving.value = false })
}

loadOptions()
</script>

<style scoped>
.merchant-brand-model-page { max-width: 1280px; }
.page-toolbar { display: flex; align-items: center; justify-content: space-between; min-height: 56px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--el-border-color-light); }
.page-toolbar h2 { margin: 0; font-size: 20px; font-weight: 600; letter-spacing: 0; }
.selection-count { display: block; margin-top: 6px; color: var(--el-text-color-secondary); font-size: 13px; }
.muted { color: var(--el-text-color-placeholder); }
</style>
