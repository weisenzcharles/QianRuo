<template>
  <el-dialog v-model="dialogVisible" title="操作日志详情" width="min(860px, 94vw)" append-to-body>
    <el-skeleton v-if="loading" :rows="8" animated />
    <template v-else>
      <el-descriptions :column="2" border>
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
        <el-descriptions-item label="对象类型">{{ display(form.targetType) }}</el-descriptions-item>
        <el-descriptions-item label="对象编号">{{ display(form.targetId) }}</el-descriptions-item>
        <el-descriptions-item label="对象名称" :span="2">{{ display(form.targetName) }}</el-descriptions-item>
      </el-descriptions>

      <section class="payload-section">
        <header>
          <span>变更前</span>
          <el-button :icon="CopyDocument" text title="复制变更前内容" @click="copyText(form.beforeContent)" />
        </header>
        <pre>{{ formatJson(form.beforeContent) }}</pre>
      </section>

      <section class="payload-section">
        <header>
          <span>变更后</span>
          <el-button :icon="CopyDocument" text title="复制变更后内容" @click="copyText(form.afterContent)" />
        </header>
        <pre>{{ formatJson(form.afterContent) }}</pre>
      </section>

      <el-alert v-if="form.status !== 0" :title="display(form.errorMessage)" type="error" :closable="false" show-icon />
    </template>
  </el-dialog>
</template>

<script setup>
import { CopyDocument } from '@element-plus/icons-vue'
import { getMerchantBusinessAudit } from '@/api/merchant/operlog'
import { MERCHANT_AUDIT_TYPE_OPTIONS } from '@/constants/options'

const props = defineProps({
  visible: { type: Boolean, default: false },
  auditId: { type: [Number, String], default: null }
})
const emit = defineEmits(['update:visible'])
const loading = ref(false)
const form = ref({})

const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

/** 在详情弹窗打开时加载服务端脱敏后的前后变更快照。 */
watch(() => [props.visible, props.auditId], async ([visible, auditId]) => {
  if (!visible || !auditId) return
  loading.value = true
  try {
    const response = await getMerchantBusinessAudit(auditId)
    form.value = response.data || {}
  } finally {
    loading.value = false
  }
}, { immediate: true })

/** 将空值转换为统一的详情占位符。 */
function display(value) {
  return value === undefined || value === null || value === '' ? '-' : value
}

/** 格式化 JSON 快照，兼容历史异常文本和空快照。 */
function formatJson(value) {
  if (!value) return '（无数据）'
  try {
    return JSON.stringify(typeof value === 'string' ? JSON.parse(value) : value, null, 2)
  } catch {
    return value
  }
}

/** 复制详情中的脱敏快照。 */
async function copyText(value) {
  try {
    await navigator.clipboard.writeText(formatJson(value))
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败，请检查浏览器剪贴板权限')
  }
}
</script>

<style lang="scss" scoped>
.payload-section {
  margin-top: 20px;

  header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 36px;
    font-weight: 600;
  }

  pre {
    max-height: 260px;
    margin: 0;
    padding: 12px;
    overflow: auto;
    border: 1px solid var(--el-border-color-light);
    border-radius: 4px;
    background: var(--el-fill-color-light);
    white-space: pre-wrap;
    overflow-wrap: anywhere;
  }
}

.el-alert {
  margin-top: 20px;
}
</style>
