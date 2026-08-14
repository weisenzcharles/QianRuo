<template>
  <div class="app-container">
    <el-alert title="协议正文发布后不可覆盖修改；发布新版本会自动下线同业务键的旧版本，商户确认记录保留版本号和正文哈希。" type="info" :closable="false" />
    <el-button type="primary" icon="Plus" class="add-button" @click="openCreate" v-hasPermi="['platform:protocol:add']">新增协议草稿</el-button>
    <el-table v-loading="loading" :data="rows" border class="table">
      <el-table-column prop="protocolKey" label="业务键" width="180" />
      <el-table-column prop="protocolTitle" label="协议标题" min-width="220" />
      <el-table-column prop="versionNo" label="版本" width="90" />
      <el-table-column prop="protocolStatus" label="状态" width="110"><template #default="scope"><el-tag :type="statusType(scope.row.protocolStatus)">{{ statusLabel(scope.row.protocolStatus) }}</el-tag></template></el-table-column>
      <el-table-column prop="contentHash" label="正文哈希" min-width="300" show-overflow-tooltip />
      <el-table-column label="操作" width="220" fixed="right"><template #default="scope"><el-button link type="primary" icon="Edit" :disabled="scope.row.protocolStatus === 'published'" @click="openEdit(scope.row)" v-hasPermi="['platform:protocol:edit']">编辑</el-button><el-button link type="success" icon="Promotion" :disabled="scope.row.protocolStatus === 'published'" @click="publish(scope.row)" v-hasPermi="['platform:protocol:publish']">发布</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="dialog" :title="editing ? '修改协议草稿' : '新增协议草稿'" width="760px" append-to-body>
      <el-form :model="form" label-width="100px">
        <el-form-item label="协议 ID" v-if="editing"><el-input v-model="form.protocolId" disabled /></el-form-item>
        <el-form-item label="业务键" required><el-input v-model="form.protocolKey" :disabled="editing" placeholder="例如 merchant_service_agreement" /></el-form-item>
        <el-form-item label="协议标题" required><el-input v-model="form.protocolTitle" maxlength="200" /></el-form-item>
        <el-form-item label="协议正文" required><el-input v-model="form.protocolContent" type="textarea" :rows="14" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存草稿</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addPlatformProtocol, listPlatformProtocols, publishPlatformProtocol, updatePlatformProtocol } from '@/api/rental/protocol'

const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)
const editing = ref(false)
const form = reactive({ protocolId: undefined, protocolKey: '', protocolTitle: '', protocolContent: '', remark: '' })

/** 加载平台协议全部版本。 */
function load() {
  loading.value = true
  listPlatformProtocols().then(response => { rows.value = response.data || [] }).finally(() => { loading.value = false })
}

/** 打开新增协议草稿表单。 */
function openCreate() {
  editing.value = false
  Object.assign(form, { protocolId: undefined, protocolKey: '', protocolTitle: '', protocolContent: '', remark: '' })
  dialog.value = true
}

/** 打开协议草稿编辑表单。 */
function openEdit(row) {
  editing.value = true
  Object.assign(form, row)
  dialog.value = true
}

/** 保存协议草稿，正文哈希由服务端统一计算。 */
function save() {
  if (!form.protocolKey.trim() || !form.protocolTitle.trim() || !form.protocolContent.trim()) return ElMessage.warning('请完整填写协议键、标题和正文')
  saving.value = true
  const action = editing.value ? updatePlatformProtocol : addPlatformProtocol
  action({ ...form }).then(() => { ElMessage.success('草稿保存成功'); dialog.value = false; load() }).finally(() => { saving.value = false })
}

/** 发布协议版本并触发旧版本自动下线。 */
function publish(row) {
  ElMessageBox.confirm(`确认发布《${row.protocolTitle}》v${row.versionNo}吗？`, '发布确认', { type: 'warning' }).then(() => publishPlatformProtocol(row.protocolId)).then(() => { ElMessage.success('协议发布成功'); load() })
}

/** 将协议状态转换为页面显示文本。 */
function statusLabel(value) {
  return { draft: '草稿', published: '已发布', retired: '已下线' }[value] || value || '-'
}

/** 根据协议状态返回 Element Plus 标签颜色。 */
function statusType(value) {
  return { draft: 'info', published: 'success', retired: 'warning' }[value] || 'info'
}

load()
</script>

<style scoped>
.add-button { margin-top: 16px; }
.table { margin-top: 16px; }
</style>
