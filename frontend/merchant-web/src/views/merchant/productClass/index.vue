<template>
  <div class="app-container merchant-list-page merchant-class-page">
    <el-form v-show="showSearch" :model="queryParams" :inline="true" @submit.prevent>
      <el-form-item label="类目名称">
        <el-input v-model="queryParams.keyword" clearable placeholder="请输入类目名称" style="width: 220px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <div class="page-toolbar">
      <div>
        <h2>经营类目</h2>
        <span class="selection-count">已选择 {{ selectedCount }} 项</span>
      </div>
    </div>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          icon="Check"
          :loading="saving"
          @click="saveSelections"
          v-hasPermi="['merchant:class:edit']"
        >保存</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" :search="true" @queryTable="loadSelections" />
    </el-row>

    <el-skeleton :loading="loading" animated :rows="8">
      <template #default>
        <el-empty v-if="classOptions.length === 0" description="暂无可选经营类目" />
        <el-table v-else v-loading="loading" :data="filteredOptions" row-key="classId" border default-expand-all :tree-props="{ children: 'children' }">
          <el-table-column label="选择" width="80" align="center">
            <template #default="{ row }"><el-checkbox :model-value="selectedIds.includes(row.classId)" @change="toggleSelection(row.classId, $event)" /></template>
          </el-table-column>
          <el-table-column prop="className" label="类目名称" min-width="280" />
          <el-table-column prop="classCode" label="类目编码" width="190" />
          <el-table-column prop="classLevel" label="层级" width="90" align="center" />
          <el-table-column prop="sortNo" label="排序" width="90" align="center" />
        </el-table>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup name="MerchantProductClass">
import {
  listProductClassOptions,
  listSelectedProductClassIds,
  saveSelectedProductClasses
} from '@/api/merchant/productClass'

const { proxy } = getCurrentInstance()
const classTreeRef = ref(null)
const classOptions = ref([])
const selectedCount = ref(0)
const selectedIds = ref([])
const loading = ref(false)
const saving = ref(false)
const showSearch = ref(true)
const queryParams = reactive({ keyword: '' })
const filteredOptions = computed(() => filterTree(classOptions.value, queryParams.keyword))

/**
 * 并行加载平台有效类目和当前商户已选主键，并在树渲染后准确回显勾选状态。
 */
function loadSelections() {
  loading.value = true
  Promise.all([listProductClassOptions(), listSelectedProductClassIds()]).then(([optionResponse, selectedResponse]) => {
    classOptions.value = optionResponse.data || []
    const responseSelectedIds = selectedResponse.data || []
    selectedIds.value = responseSelectedIds
  }).finally(() => {
    loading.value = false
  })
}

/**
 * 根据树组件当前勾选状态同步已选择数量。
 */
function syncSelectedCount() {
  selectedCount.value = selectedIds.value.length
}

/** 切换单个经营类目的选中状态，父子类目保持独立可选。 */
function toggleSelection(classId, checked) {
  const ids = new Set(selectedIds.value)
  checked ? ids.add(classId) : ids.delete(classId)
  selectedIds.value = Array.from(ids)
  syncSelectedCount()
}

/** 按类目名称过滤树数据，同时保留命中的父级路径。 */
function filterTree(nodes, keyword) {
  if (!keyword) return nodes
  return nodes.reduce((result, node) => {
    const children = filterTree(node.children || [], keyword)
    if (node.className?.includes(keyword) || children.length) result.push({ ...node, children })
    return result
  }, [])
}

/** 执行类目名称筛选。 */
function handleQuery() {}

/** 清空类目名称筛选条件。 */
function resetQuery() { queryParams.keyword = '' }

/**
 * 保存当前商户经营类目，归属信息只由后端登录上下文确定。
 */
function saveSelections() {
  const classIds = selectedIds.value
  saving.value = true
  saveSelectedProductClasses(classIds).then(() => {
    proxy.$modal.msgSuccess('经营类目保存成功')
    selectedCount.value = classIds.length
  }).finally(() => {
    saving.value = false
  })
}

loadSelections()
</script>

<style scoped>
.merchant-class-page {
  max-width: 1080px;
}

.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 56px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.page-toolbar h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0;
}

.selection-count {
  display: block;
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.class-tree {
  min-height: 360px;
  padding: 8px 0;
}

.class-tree :deep(.el-tree-node__content) {
  height: 40px;
}

.class-node {
  display: inline-flex;
  align-items: baseline;
  gap: 12px;
  min-width: 0;
}

.class-node small {
  color: var(--el-text-color-placeholder);
}
</style>
