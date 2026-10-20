<template>
  <div class="app-container">
    <el-alert title="协议正文发布后不可覆盖修改；发布新版本会自动下线同业务键的旧版本，商户确认记录保留版本号和正文哈希。" type="info" :closable="false" />
    <el-button type="primary" icon="Plus" class="add-button" @click="openCreate" v-hasPermi="['platform:protocol:add']">新增协议草稿</el-button>
    <el-table v-loading="loading" :data="rows" border class="table">
      <el-table-column prop="protocolKey" label="业务键" width="180" />
      <el-table-column prop="protocolTitle" label="协议标题" min-width="220" />
      <el-table-column prop="versionNo" label="版本" width="90" />
      <el-table-column label="适用入口" min-width="180"><template #default="scope"><el-tag v-for="label in scopeLabels(scope.row)" :key="label" class="scope-tag">{{ label }}</el-tag></template></el-table-column>
      <el-table-column label="生效时间" width="180"><template #default="scope">{{ scope.row.effectiveTime || '-' }}</template></el-table-column>
      <el-table-column prop="protocolStatus" label="状态" width="110"><template #default="scope"><el-tag :type="statusType(scope.row)">{{ statusLabel(scope.row) }}</el-tag></template></el-table-column>
      <el-table-column prop="contentHash" label="正文哈希" min-width="300" show-overflow-tooltip />
      <el-table-column label="操作" width="220" fixed="right"><template #default="scope"><el-button link type="primary" icon="Edit" :disabled="scope.row.protocolStatus === RENTAL_CODE.PROTOCOL_STATUS.PUBLISHED" @click="openEdit(scope.row)" v-hasPermi="['platform:protocol:edit']">编辑</el-button><el-button link type="success" icon="Promotion" :disabled="scope.row.protocolStatus === RENTAL_CODE.PROTOCOL_STATUS.PUBLISHED" @click="openPublish(scope.row)" v-hasPermi="['platform:protocol:publish']">发布</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="dialog" :title="editing ? '修改协议草稿' : '新增协议草稿'" width="760px" append-to-body>
      <el-form :model="form" label-width="100px">
        <el-form-item label="协议编号" v-if="editing"><el-input v-model="form.protocolId" disabled /></el-form-item>
        <el-form-item label="业务键" required><el-input v-model="form.protocolKey" :disabled="editing" placeholder="例如 merchant_service_agreement" /></el-form-item>
        <el-form-item label="协议标题" required><el-input v-model="form.protocolTitle" maxlength="200" /></el-form-item>
        <el-form-item label="适用入口" required><el-checkbox-group v-model="form.scopeTypes"><el-checkbox v-for="item in scopeOptions" :key="item.value" :label="item.value">{{ item.label }}</el-checkbox></el-checkbox-group></el-form-item>
        <el-form-item label="协议正文" required><el-input v-model="form.protocolContent" type="textarea" :rows="14" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存草稿</el-button></template>
    </el-dialog>
    <el-dialog v-model="publishDialog" title="发布协议" width="520px" append-to-body>
      <el-form label-width="110px">
        <el-form-item label="协议版本"><span>{{ publishingRow?.protocolTitle }} v{{ publishingRow?.versionNo }}</span></el-form-item>
        <el-form-item label="生效时间"><el-date-picker v-model="publishEffectiveTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="留空立即生效" clearable /></el-form-item>
        <el-alert title="定时生效前，商户端不会展示该版本，也不能提前确认。" type="info" :closable="false" />
      </el-form>
      <template #footer><el-button @click="publishDialog = false">取消</el-button><el-button type="primary" :loading="publishing" @click="submitPublish">确认发布</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addPlatformProtocol, listPlatformProtocols, publishPlatformProtocol, updatePlatformProtocol } from '@/api/rental/protocol'
import { RENTAL_CODE } from '@/utils/rental/options'

const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)
const editing = ref(false)
const form = reactive({ protocolId: undefined, protocolKey: '', protocolTitle: '', protocolContent: '', scopeTypes: [1], remark: '' })
const scopeOptions = [
  { label: '商户网页端', value: 1 },
  { label: '访客网页端', value: 2 },
  { label: '小程序', value: 3 }
]
const publishDialog = ref(false)
const publishing = ref(false)
const publishingRow = ref(null)
const publishEffectiveTime = ref('')

/** 加载平台协议全部版本。 */
function load() {
  loading.value = true
  listPlatformProtocols().then(response => { rows.value = response.data || [] }).finally(() => { loading.value = false })
}

/** 打开新增协议草稿表单。 */
function openCreate() {
  editing.value = false
  Object.assign(form, { protocolId: undefined, protocolKey: '', protocolTitle: '', protocolContent: '', scopeTypes: [1], remark: '' })
  dialog.value = true
}

/** 打开协议草稿编辑表单。 */
function openEdit(row) {
  editing.value = true
  Object.assign(form, {
    protocolId: row.protocolId,
    protocolKey: row.protocolKey,
    protocolTitle: row.protocolTitle,
    protocolContent: row.protocolContent || '',
    scopeTypes: Array.isArray(row.scopeTypes) && row.scopeTypes.length ? [...row.scopeTypes] : [1],
    remark: row.remark || ''
  })
  dialog.value = true
}

/** 保存协议草稿，正文哈希由服务端统一计算。 */
function save() {
  if (!form.protocolKey.trim() || !form.protocolTitle.trim() || !form.protocolContent.trim()) return ElMessage.warning('请完整填写协议键、标题和正文')
  saving.value = true
  const action = editing.value ? updatePlatformProtocol : addPlatformProtocol
  action({ ...form }).then(() => { ElMessage.success('草稿保存成功'); dialog.value = false; load() }).finally(() => { saving.value = false })
}

/** 打开发布设置，支持立即生效或定时生效。 */
function openPublish(row) {
  publishingRow.value = row
  publishEffectiveTime.value = ''
  publishDialog.value = true
}

/** 发布协议版本并触发旧版本自动下线。 */
function submitPublish() {
  if (!publishingRow.value) return
  ElMessageBox.confirm(`确认发布《${publishingRow.value.protocolTitle}》v${publishingRow.value.versionNo}吗？`, '发布确认', { type: 'warning' }).then(() => {
    publishing.value = true
    return publishPlatformProtocol(publishingRow.value.protocolId, { effectiveTime: publishEffectiveTime.value || undefined })
  }).then(() => {
    ElMessage.success(publishEffectiveTime.value ? '协议已发布，将按设定时间生效' : '协议已立即生效')
    publishDialog.value = false
    load()
  }).finally(() => { publishing.value = false })
}

/** 将协议状态转换为页面显示文本。 */
function statusLabel(row) {
  if (isScheduled(row)) return '待生效'
  return {
    [RENTAL_CODE.PROTOCOL_STATUS.DRAFT]: '草稿',
    [RENTAL_CODE.PROTOCOL_STATUS.PUBLISHED]: '已发布',
    [RENTAL_CODE.PROTOCOL_STATUS.RETIRED]: '已下线'
  }[Number(row.protocolStatus)] || '-'
}

/** 根据协议状态返回 Element Plus 标签颜色。 */
function statusType(row) {
  if (isScheduled(row)) return 'warning'
  return {
    [RENTAL_CODE.PROTOCOL_STATUS.DRAFT]: 'info',
    [RENTAL_CODE.PROTOCOL_STATUS.PUBLISHED]: 'success',
    [RENTAL_CODE.PROTOCOL_STATUS.RETIRED]: 'warning'
  }[Number(row.protocolStatus)] || 'info'
}

/** 判断协议是否已经配置未来生效时间。 */
function isScheduled(row) {
  if (Number(row.protocolStatus) !== RENTAL_CODE.PROTOCOL_STATUS.PUBLISHED || !row.effectiveTime) return false
  return new Date(row.effectiveTime.replace(' ', 'T')).getTime() > Date.now()
}

/** 兼容历史接口返回的入口编码，页面只展示中文。 */
function scopeLabels(row) {
  if (Array.isArray(row.scopeLabels) && row.scopeLabels.length) return row.scopeLabels
  const labels = { 1: '商户网页端', 2: '访客网页端', 3: '小程序' }
  return (row.scopeTypes || []).map(item => labels[item]).filter(Boolean)
}

load()
</script>

<style scoped>
.add-button { margin-top: 16px; }
.table { margin-top: 16px; }
.scope-tag { margin-right: 4px; }
</style>
