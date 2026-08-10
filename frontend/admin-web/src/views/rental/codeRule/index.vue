<template>
  <div class="app-container">
    <el-alert
      title="编码长度表示最终编码总长度，系统按前缀加数字序号自动生成；当前序号只读且不能重置。"
      type="info"
      :closable="false"
      class="rule-notice"
    />

    <el-table v-loading="loading" :data="rules" border>
      <el-table-column prop="codeName" label="编码类型" min-width="180" />
      <el-table-column prop="codeType" label="类型键" min-width="150" />
      <el-table-column label="前缀" width="220">
        <template #default="scope">
          <el-input v-model="scope.row.prefix" maxlength="32" :disabled="!scope.row.editing" />
        </template>
      </el-table-column>
      <el-table-column label="总长度" width="180">
        <template #default="scope">
          <el-input-number
            v-model="scope.row.codeLength"
            :min="2"
            :max="64"
            :disabled="!scope.row.editing"
            controls-position="right"
          />
        </template>
      </el-table-column>
      <el-table-column label="当前序号" prop="currentValue" width="140" />
      <el-table-column label="状态" width="120">
        <template #default="scope">
          <el-switch v-model="scope.row.status" :active-value="0" :inactive-value="1" :disabled="!scope.row.editing" />
        </template>
      </el-table-column>
      <el-table-column label="示例" min-width="180">
        <template #default="scope">{{ preview(scope.row) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="scope">
          <el-button v-if="!scope.row.editing" link type="primary" icon="Edit" @click="startEdit(scope.row)">编辑</el-button>
          <template v-else>
            <el-button link type="primary" icon="Check" :loading="scope.row.saving" @click="save(scope.row)">保存</el-button>
            <el-button link icon="Close" @click="cancelEdit(scope.row)">取消</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup name="CodeRule">
import { ElMessage } from 'element-plus'
import { listCodeRules, updateCodeRule } from '@/api/rental/codeRule'

const rules = ref([])
const loading = ref(false)

/**
 * 加载平台统一编码规则，并为每行建立独立的编辑快照。
 */
function loadRules() {
  loading.value = true
  listCodeRules().then(response => {
    rules.value = (response.data || []).map(item => ({ ...item, editing: false, saving: false, snapshot: null }))
  }).finally(() => {
    loading.value = false
  })
}

/**
 * 进入当前编码规则编辑状态，并保存取消编辑所需的原始快照。
 *
 * @param {object} row 当前编码规则
 */
function startEdit(row) {
  row.snapshot = { prefix: row.prefix, codeLength: row.codeLength, status: row.status, remark: row.remark }
  row.editing = true
}

/**
 * 放弃当前行修改并恢复服务端返回的原始配置。
 *
 * @param {object} row 当前编码规则
 */
function cancelEdit(row) {
  if (row.snapshot) Object.assign(row, row.snapshot)
  row.editing = false
  row.snapshot = null
}

/**
 * 保存当前编码规则；当前序号不从浏览器提交，始终由平台服务维护。
 *
 * @param {object} row 当前编码规则
 */
function save(row) {
  row.saving = true
  updateCodeRule(row.codeType, {
    prefix: row.prefix,
    codeLength: row.codeLength,
    status: row.status,
    remark: row.remark
  }).then(response => {
    Object.assign(row, response.data || {})
    row.editing = false
    row.snapshot = null
    ElMessage.success('编码规则保存成功')
  }).finally(() => {
    row.saving = false
  })
}

/**
 * 根据当前规则展示下一个编码示例，帮助管理员确认总长度含义。
 *
 * @param {object} row 当前编码规则
 * @returns {string} 编码示例
 */
function preview(row) {
  const prefix = String(row.prefix || '').toUpperCase()
  const width = Math.max(1, Number(row.codeLength || 0) - prefix.length)
  return prefix + String(Number(row.currentValue || 0) + 1).padStart(width, '0')
}

loadRules()
</script>

<style scoped>
.rule-notice { margin-bottom: 16px; }
</style>
