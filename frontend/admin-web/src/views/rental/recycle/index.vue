<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="对象类型" prop="objectType"><el-select v-model="queryParams.objectType" clearable placeholder="全部类型" style="width: 160px"><el-option v-for="item in objectTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
      <el-form-item label="处理状态" prop="restoreStatus"><el-select v-model="queryParams.restoreStatus" clearable placeholder="全部状态" style="width: 130px"><el-option v-for="item in restoreStatuses" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
      <el-form-item label="关键词" prop="keyword"><el-input v-model.trim="queryParams.keyword" clearable placeholder="对象名称或编码" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="rows">
      <el-table-column label="对象名称" prop="objectName" min-width="200" show-overflow-tooltip />
      <el-table-column label="对象编码" prop="objectCode" width="180" show-overflow-tooltip />
      <el-table-column label="对象类型" width="130"><template #default="{ row }">{{ objectTypeLabel(row.objectType) }}</template></el-table-column>
      <el-table-column label="所属商户" prop="memberName" min-width="160" show-overflow-tooltip />
      <el-table-column label="删除时间" prop="deletedTime" width="175" />
      <el-table-column label="有效期至" prop="expiresTime" width="175" />
      <el-table-column label="状态" width="105" align="center"><template #default="{ row }"><el-tag :type="restoreMeta(row.restoreStatus).type">{{ row.restoreStatusName || restoreMeta(row.restoreStatus).label }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="130" fixed="right" align="center">
        <template #default="{ row }"><el-button link type="primary" @click="openSnapshot(row)">查看快照</el-button><el-button v-if="row.restoreStatus === 0" link type="success" @click="restore(row)" v-hasPermi="['platform:recycle:restore']">恢复</el-button></template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    <el-empty v-if="!loading && rows.length === 0" description="暂无回收站记录" />
    <el-dialog v-model="snapshotVisible" title="回收快照" width="720px" append-to-body destroy-on-close>
      <el-descriptions v-if="selected" :column="2" border label-width="120">
        <el-descriptions-item label="对象名称">{{ selected.objectName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="对象编码">{{ selected.objectCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="对象类型">{{ objectTypeLabel(selected.objectType) }}</el-descriptions-item>
        <el-descriptions-item label="所属商户">{{ selected.memberName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="删除时间">{{ selected.deletedTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="有效期至">{{ selected.expiresTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <pre v-if="selected" class="snapshot-content">{{ formatSnapshot(selected.snapshotJson) }}</pre>
      <template #footer><el-button @click="snapshotVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="PlatformRecycle">
import { listRecycleRecords, restoreRecycleRecord } from '@/api/rental/governance'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const total = ref(0)
const rows = ref([])
const selected = ref(null)
const snapshotVisible = ref(false)
const queryParams = reactive({ pageNum: 1, pageSize: 10, objectType: undefined, restoreStatus: undefined, keyword: undefined })
const objectTypes = [{ value: 'knowledge_article', label: '知识文章' }, { value: 'merchant_notice', label: '商户通知' }]
const restoreStatuses = [{ value: 0, label: '待恢复' }, { value: 1, label: '已恢复' }, { value: 2, label: '已清理' }]
function objectTypeLabel(value) { return objectTypes.find(item => item.value === value)?.label || '其他对象' }
function restoreMeta(value) { return ({ 0: { label: '待恢复', type: 'warning' }, 1: { label: '已恢复', type: 'success' }, 2: { label: '已清理', type: 'info' } })[Number(value)] || { label: '未知状态', type: 'info' } }
function getList() { loading.value = true; listRecycleRecords({ ...queryParams }).then(response => { rows.value = response.rows || []; total.value = Number(response.total || 0) }).finally(() => { loading.value = false }) }
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function openSnapshot(row) { selected.value = row; snapshotVisible.value = true }
async function restore(row) { await proxy.$modal.confirm(`确认恢复“${row.objectName || row.objectCode}”吗？`); await restoreRecycleRecord(row.recycleId); ElMessage.success('恢复成功'); getList() }
function formatSnapshot(value) { if (!value) return '暂无快照内容'; try { return JSON.stringify(JSON.parse(value), null, 2) } catch { return value } }
getList()
</script>

<style scoped>
.snapshot-content { max-height: 420px; overflow: auto; padding: 16px; margin: 16px 0 0; color: var(--el-text-color-regular); background: var(--el-fill-color-light); border-radius: 4px; white-space: pre-wrap; word-break: break-all; }
</style>
