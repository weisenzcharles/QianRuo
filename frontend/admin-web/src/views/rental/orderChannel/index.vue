<template>
  <div class="app-container">
    <el-alert title="平台端只维护租商联盟的联盟订单渠道，商户端只能选择使用，不能修改平台渠道定义。" type="info" :closable="false" />
    <el-form :inline="true" :model="query" class="search-form">
      <el-form-item label="渠道名称"><el-input v-model="query.keyword" clearable placeholder="请输入渠道名称或编码" @keyup.enter="load" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="query.status" clearable placeholder="全部" @change="load"><el-option label="启用" :value="NORMAL_DISABLE_CODE.NORMAL" /><el-option label="停用" :value="NORMAL_DISABLE_CODE.DISABLED" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="load">查询</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-button type="primary" icon="Plus" @click="openCreate" v-hasPermi="['platform:orderChannel:add']">新增联盟渠道</el-button>
    <el-table v-loading="loading" :data="rows" border class="table">
      <el-table-column prop="channelCode" label="渠道编码" min-width="180" />
      <el-table-column prop="channelName" label="渠道名称" min-width="180" />
      <el-table-column prop="status" label="状态" width="100"><template #default="scope"><el-tag :type="scope.row.status === NORMAL_DISABLE_CODE.NORMAL ? 'success' : 'info'">{{ scope.row.status === NORMAL_DISABLE_CODE.NORMAL ? '启用' : '停用' }}</el-tag></template></el-table-column>
      <el-table-column prop="sortNo" label="排序" width="90" />
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="scope"><el-button link type="primary" icon="Edit" @click="openEdit(scope.row)" v-hasPermi="['platform:orderChannel:edit']">编辑</el-button><el-button link type="danger" icon="Delete" @click="removeRow(scope.row)" v-hasPermi="['platform:orderChannel:remove']">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialog" :title="editing ? '修改联盟渠道' : '新增联盟渠道'" width="520px" append-to-body>
      <el-form :model="form" label-width="100px">
        <el-form-item v-if="editing" label="渠道编码"><el-input v-model="form.channelCode" disabled placeholder="保存后由系统生成" /></el-form-item>
        <el-form-item label="渠道名称" required><el-input v-model="form.channelName" maxlength="128" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="NORMAL_DISABLE_CODE.NORMAL" :inactive-value="NORMAL_DISABLE_CODE.DISABLED" active-text="启用" inactive-text="停用" inline-prompt /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortNo" :min="0" :max="9999" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.reason" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addPlatformOrderChannel, deletePlatformOrderChannel, listPlatformOrderChannels, updatePlatformOrderChannel } from '@/api/rental/orderChannel'
import { RENTAL_CODE } from '@/utils/rental/options'
import { NORMAL_DISABLE_CODE } from '@/utils/stateCode'

const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)
const editing = ref(false)
const query = reactive({ keyword: '', status: undefined })
const form = reactive({ channelId: undefined, channelName: '', channelCode: '', status: NORMAL_DISABLE_CODE.NORMAL, sortNo: 0, reason: '' })

/** 加载平台联盟订单渠道。 */
function load() {
  loading.value = true
  listPlatformOrderChannels(query).then(response => { rows.value = response.data || [] }).finally(() => { loading.value = false })
}

/** 重置平台渠道查询条件。 */
function resetQuery() {
  query.keyword = ''
  query.status = undefined
  load()
}

/** 打开新增联盟渠道表单。 */
function openCreate() {
  editing.value = false
  Object.assign(form, { channelId: undefined, channelName: '', channelCode: '', status: NORMAL_DISABLE_CODE.NORMAL, sortNo: 0, reason: '' })
  dialog.value = true
}

/** 打开联盟渠道编辑表单。 */
function openEdit(row) {
  editing.value = true
  Object.assign(form, row, { reason: row.remark || '' })
  dialog.value = true
}

/** 保存平台联盟渠道。 */
function save() {
  if (!form.channelName.trim()) return ElMessage.warning('请输入渠道名称')
  saving.value = true
  const action = editing.value ? updatePlatformOrderChannel : addPlatformOrderChannel
  action({ ...form, channelType: RENTAL_CODE.ORDER_CHANNEL_TYPE.ALLIANCE }).then(() => { ElMessage.success('保存成功'); dialog.value = false; load() }).finally(() => { saving.value = false })
}

/** 删除平台联盟渠道，仍被业务引用时由后端拒绝。 */
function removeRow(row) {
  ElMessageBox.confirm(`确认删除“${row.channelName}”吗？`, '操作确认', { type: 'warning' }).then(() => deletePlatformOrderChannel(row.channelId)).then(() => { ElMessage.success('删除成功'); load() })
}

load()
</script>

<style scoped>
.search-form { margin-top: 16px; }
.table { margin-top: 16px; }
</style>
