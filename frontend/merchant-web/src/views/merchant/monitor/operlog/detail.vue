<template>
  <el-dialog v-model="dialogVisible" title="操作日志详细" width="min(780px, 94vw)" append-to-body>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="日志编号">{{ display(form.operId) }}</el-descriptions-item>
      <el-descriptions-item label="用户ID">{{ display(form.operatorUserId) }}</el-descriptions-item>
      <el-descriptions-item label="操作模块">{{ display(form.title) }}</el-descriptions-item>
      <el-descriptions-item label="操作类型">{{ typeLabel }}</el-descriptions-item>
      <el-descriptions-item label="操作人员">{{ display(form.operName) }}</el-descriptions-item>
      <el-descriptions-item label="操作状态">
        <el-tag :type="form.status === 0 ? 'success' : 'danger'" size="small">
          {{ form.status === 0 ? '正常' : '异常' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="请求方式">{{ display(form.requestMethod) }}</el-descriptions-item>
      <el-descriptions-item label="操作地址">{{ display(form.operIp) }}</el-descriptions-item>
      <el-descriptions-item label="请求地址" :span="2">{{ display(form.operUrl) }}</el-descriptions-item>
      <el-descriptions-item label="操作方法" :span="2">{{ display(form.method) }}</el-descriptions-item>
      <el-descriptions-item label="操作时间">{{ display(form.operTime) }}</el-descriptions-item>
      <el-descriptions-item label="消耗时间">{{ form.costTime ?? 0 }} 毫秒</el-descriptions-item>
    </el-descriptions>

    <section class="payload-section">
      <header>
        <span>请求参数</span>
        <el-button :icon="CopyDocument" text @click="copyText(form.operParam)" title="复制请求参数" />
      </header>
      <pre>{{ formatJson(form.operParam) }}</pre>
    </section>

    <section class="payload-section">
      <header>
        <span>返回参数</span>
        <el-button :icon="CopyDocument" text @click="copyText(form.jsonResult)" title="复制返回参数" />
      </header>
      <pre>{{ formatJson(form.jsonResult) }}</pre>
    </section>

    <el-alert v-if="form.status !== 0" :title="display(form.errorMsg)" type="error" :closable="false" show-icon />
  </el-dialog>
</template>

<script setup>
import { CopyDocument } from '@element-plus/icons-vue'
import { OPER_TYPE_OPTIONS } from '@/constants/options'

const props = defineProps({
  visible: { type: Boolean, default: false },
  row: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})
const form = computed(() => props.row || {})
const typeLabel = computed(() => OPER_TYPE_OPTIONS.find(option => option.value === form.value.businessType)?.label || '-')

function display(value) {
  return value === undefined || value === null || value === '' ? '-' : value
}

function formatJson(value) {
  if (!value) return '（无数据）'
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

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
    max-height: 220px;
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
