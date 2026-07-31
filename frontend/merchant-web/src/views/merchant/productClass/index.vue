<template>
  <div class="app-container merchant-class-page">
    <div class="page-toolbar">
      <div>
        <h2>经营类目</h2>
        <span class="selection-count">已选择 {{ selectedCount }} 项</span>
      </div>
      <el-button
        type="primary"
        icon="Check"
        :loading="saving"
        @click="saveSelections"
        v-hasPermi="['merchant:class:edit']"
      >保存</el-button>
    </div>

    <el-skeleton :loading="loading" animated :rows="8">
      <template #default>
        <el-empty v-if="classOptions.length === 0" description="暂无可选经营类目" />
        <el-tree
          v-else
          ref="classTreeRef"
          class="class-tree"
          :data="classOptions"
          node-key="classId"
          show-checkbox
          check-strictly
          default-expand-all
          :props="{ label: 'className', children: 'children' }"
          @check="syncSelectedCount"
        >
          <template #default="{ data }">
            <span class="class-node">
              <span>{{ data.className }}</span>
              <small>{{ data.classCode }}</small>
            </span>
          </template>
        </el-tree>
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
const loading = ref(false)
const saving = ref(false)

/**
 * 并行加载平台有效类目和当前商户已选主键，并在树渲染后准确回显勾选状态。
 */
function loadSelections() {
  loading.value = true
  Promise.all([listProductClassOptions(), listSelectedProductClassIds()]).then(([optionResponse, selectedResponse]) => {
    classOptions.value = optionResponse.data || []
    const selectedIds = selectedResponse.data || []
    selectedCount.value = selectedIds.length
    nextTick(() => {
      classTreeRef.value?.setCheckedKeys(selectedIds, false)
    })
  }).finally(() => {
    loading.value = false
  })
}

/**
 * 根据树组件当前勾选状态同步已选择数量。
 */
function syncSelectedCount() {
  selectedCount.value = classTreeRef.value?.getCheckedKeys(false).length || 0
}

/**
 * 保存当前商户经营类目，归属信息只由后端登录上下文确定。
 */
function saveSelections() {
  const classIds = classTreeRef.value?.getCheckedKeys(false) || []
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
