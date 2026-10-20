<template>
  <el-dialog v-model="dialogVisible" title="操作日志详情" width="min(860px, 94vw)" append-to-body>
    <el-skeleton v-if="loading" :rows="8" animated />
    <template v-else>
      <el-descriptions :column="2" border label-width="120">
        <el-descriptions-item label="操作时间">{{ display(form.operationTime) }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ display(form.username) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ display(form.operatorName) }}</el-descriptions-item>
        <el-descriptions-item label="业务模块">{{ display(form.businessModule) }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <dict-tag :options="MERCHANT_AUDIT_TYPE_OPTIONS" :value="form.operationType" />
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ display(form.description) }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ display(form.ipAddress) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="form.status === 0 ? 'success' : 'danger'" size="small">
            {{ form.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <section v-if="changeRows.length" class="change-section">
        <div class="change-section-header">
          <div>
            <h3>变更内容</h3>
            <span class="change-section-subtitle">按字段对比变更前后的数据</span>
          </div>
          <div class="change-actions">
            <el-tag type="danger" effect="light">变更前</el-tag>
            <el-icon><Right /></el-icon>
            <el-tag type="success" effect="light">变更后</el-tag>
          </div>
        </div>

        <el-table :data="changeRows" border stripe class="change-table">
          <el-table-column label="变更字段" prop="label" width="220" show-overflow-tooltip>
            <template #default="scope">
              <div class="field-cell">
                <span>{{ scope.row.label }}</span>
                <el-tag v-if="scope.row.changed" type="warning" size="small">已变更</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column min-width="260">
            <template #header>
              <div class="change-column-header">
                <span>变更前</span>
                <el-button
                  :icon="CopyDocument"
                  link
                  type="primary"
                  title="复制变更前内容"
                  @click="copyText(form.beforeContent)"
                >复制</el-button>
              </div>
            </template>
            <template #default="scope">
              <span class="change-value before-value">{{ scope.row.before }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="260">
            <template #header>
              <div class="change-column-header">
                <span>变更后</span>
                <el-button
                  :icon="CopyDocument"
                  link
                  type="primary"
                  title="复制变更后内容"
                  @click="copyText(form.afterContent)"
                >复制</el-button>
              </div>
            </template>
            <template #default="scope">
              <span class="change-value after-value">{{ scope.row.after }}</span>
            </template>
          </el-table-column>
        </el-table>

      </section>

      <el-alert v-if="form.status !== 0" :title="display(form.errorMessage)" type="error" :closable="false" show-icon />
    </template>
  </el-dialog>
</template>

<script setup>
import { CopyDocument, Right } from '@element-plus/icons-vue'
import { getMerchantBusinessAudit, getMerchantOperLogFieldLabels } from '@/api/merchant/operlog'
import { MERCHANT_AUDIT_TYPE_OPTIONS } from '@/constants/options'

const props = defineProps({
  visible: { type: Boolean, default: false },
  auditId: { type: [Number, String], default: null }
})
const emit = defineEmits(['update:visible'])
const loading = ref(false)
const form = ref({})
const fieldLabels = ref({})
let fieldLabelsLoaded = false

const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

/** 将脱敏快照转成可逐字段比较的行，兼容对象、数组和历史纯文本。 */
const changeRows = computed(() => {
  const before = flattenSnapshot(parseSnapshot(form.value.beforeContent))
  const after = flattenSnapshot(parseSnapshot(form.value.afterContent))
  const beforeMap = new Map(before.map(item => [item.path, item.value]))
  const afterMap = new Map(after.map(item => [item.path, item.value]))
  const paths = [...new Set([...before.map(item => item.path), ...after.map(item => item.path)])]

  return paths.map(path => {
    const beforeValue = beforeMap.get(path)
    const afterValue = afterMap.get(path)
    return {
      label: fieldLabel(path),
      before: formatChangeValue(beforeValue),
      after: formatChangeValue(afterValue),
      changed: !sameValue(beforeValue, afterValue)
    }
  }).filter(row => row.changed)
})

/** 在详情弹窗打开时加载服务端脱敏后的前后变更快照。 */
watch(() => [props.visible, props.auditId], async ([visible, auditId]) => {
  if (!visible || !auditId) return
  loading.value = true
  try {
    const [, response] = await Promise.all([
      loadFieldLabels(),
      getMerchantBusinessAudit(auditId)
    ])
    form.value = response.data || {}
  } finally {
    loading.value = false
  }
}, { immediate: true })

/** 从平台字典加载字段中文名称；失败时保留通用字段名，避免详情页无法打开。 */
async function loadFieldLabels() {
  if (fieldLabelsLoaded) return
  try {
    const response = await getMerchantOperLogFieldLabels()
    fieldLabels.value = response.data || {}
  } catch {
    fieldLabels.value = {}
  } finally {
    fieldLabelsLoaded = true
  }
}

/** 将空值转换为统一的详情占位符。 */
function display(value) {
  return value === undefined || value === null || value === '' ? '-' : value
}

/** 解析服务端快照，兼容 JSON 对象和历史纯文本。 */
function parseSnapshot(value) {
  if (value === undefined || value === null || value === '') return {}
  if (typeof value !== 'string') return value
  try {
    return JSON.parse(value)
  } catch {
    return { 内容: value }
  }
}

/** 展开嵌套对象，技术主键不展示给商户用户。 */
function flattenSnapshot(value, prefix = '', rows = []) {
  if (Array.isArray(value)) {
    if (!value.length) rows.push({ path: prefix || '内容', value: '[]' })
    value.forEach((item, index) => flattenSnapshot(item, `${prefix}[${index + 1}]`, rows))
    return rows
  }

  if (value && typeof value === 'object') {
    const entries = Object.entries(value).filter(([key]) => !isHiddenDisplayField(key))
    if (!entries.length && prefix) rows.push({ path: prefix, value: '{}' })
    entries.forEach(([key, item]) => flattenSnapshot(item, prefix ? `${prefix}.${key}` : key, rows))
    return rows
  }

  rows.push({ path: prefix || '内容', value })
  return rows
}

/** 技术关联 ID 只用于内部关联，不在商户操作日志中展示。 */
function isTechnicalIdentifier(key) {
  return key === 'id' || /(?:Id|ID|Ids|IDs|_id|_ids|_ID|_IDs)$/.test(key)
}

/** 版本号只用于内部并发控制，不在商户变更日志中展示。 */
function isHiddenDisplayField(key) {
  return isTechnicalIdentifier(key) || key.toLowerCase() === 'version'
}

function fieldLabel(path) {
  return path.split('.').map(part => {
    const match = part.match(/^(.*)\[(\d+)\]$/)
    const key = match ? match[1] : part
    const index = match ? `（第${match[2]}项）` : ''
    const label = fieldLabels.value[key] || fallbackFieldLabel(key)
    return `${label}${index}`
  }).join(' / ')
}

/** 字典暂不可用时保留可读的字段名，不在前端复制完整业务映射。 */
function fallbackFieldLabel(key) {
  return key.replace(/([a-z])([A-Z])/g, '$1 $2').replace(/_/g, ' ')
}

function formatChangeValue(value) {
  if (value === undefined) return '未设置'
  if (value === null || value === '') return '空'
  if (value === true) return '是'
  if (value === false) return '否'
  return String(value)
}

function sameValue(before, after) {
  return before === after || (before !== undefined && after !== undefined && String(before) === String(after))
}

/** 复制详情中的脱敏快照。 */
async function copyText(value) {
  try {
    const text = typeof value === 'string' ? value : JSON.stringify(value, null, 2) || ''
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败，请检查浏览器剪贴板权限')
  }
}
</script>

<style lang="scss" scoped>
.change-section {
  margin-top: 20px;
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-bg-color);

  .change-section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 16px;

    h3 {
      margin: 0;
      font-size: 16px;
      line-height: 24px;
    }

    .change-section-subtitle {
      display: block;
      margin-top: 4px;
      color: var(--el-text-color-secondary);
      font-size: 12px;
    }
  }

  .change-actions,
  .change-column-header,
  .field-cell {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .change-column-header {
    justify-content: space-between;
  }

  .change-table {
    :deep(.el-table__cell) {
      vertical-align: top;
    }

    .change-value {
      display: block;
      min-height: 24px;
      padding: 4px 8px;
      border-radius: 4px;
      white-space: pre-wrap;
      overflow-wrap: anywhere;
    }

    .before-value {
      color: var(--el-color-danger);
      background: var(--el-color-danger-light-9);
    }

    .after-value {
      color: var(--el-color-success-dark-2);
      background: var(--el-color-success-light-9);
    }
  }
}

.el-alert {
  margin-top: 20px;
}
</style>
