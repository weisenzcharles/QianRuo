<template>
  <div class="app-container merchant-list-page">
    <el-alert title="平台联盟渠道可直接使用，商户自定义渠道由当前商户维护；套餐和订单转移都会记录不可变历史。" type="info" :closable="false" />
    <el-form v-show="showSearch" :inline="true" :model="query" @submit.prevent>
      <el-form-item label="渠道名称">
        <el-input v-model="query.keyword" clearable placeholder="请输入渠道名称或编码" @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" @change="load">
          <el-option label="启用" :value="NORMAL_DISABLE_CODE.NORMAL" />
          <el-option label="停用" :value="NORMAL_DISABLE_CODE.DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="load">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" icon="Plus" @click="openCreate">新增自定义渠道</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Switch" @click="transferDialog = true">渠道转移</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="load" />
    </el-row>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="channelCode" label="渠道编码" min-width="180" />
      <el-table-column prop="channelName" label="渠道名称" min-width="180" />
      <el-table-column prop="channelType" label="类型" width="120">
        <template #default="scope">{{ scope.row.channelType === RENTAL_CODE.ORDER_CHANNEL_TYPE.ALLIANCE ? '平台联盟' : '商户自定义' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope"><el-tag :type="scope.row.status === NORMAL_DISABLE_CODE.NORMAL ? 'success' : 'info'">{{ scope.row.status === NORMAL_DISABLE_CODE.NORMAL ? '启用' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="sortNo" label="排序" width="90" />
      <el-table-column label="操作" width="230" fixed="right" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="openDetail(scope.row)">详情</el-button>
          <template v-if="scope.row.scopeType === RENTAL_CODE.ORDER_CHANNEL_SCOPE.MERCHANT">
            <el-button link type="primary" icon="Edit" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="removeRow(scope.row)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="渠道详情" width="620px" append-to-body>
      <el-descriptions v-if="detailRow" :column="2" border label-width="120">
        <el-descriptions-item label="渠道名称">{{ detailRow.channelName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="渠道编码">{{ detailRow.channelCode || "-" }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detailRow.channelType === RENTAL_CODE.ORDER_CHANNEL_TYPE.ALLIANCE ? "平台联盟" : "商户自定义" }}</el-descriptions-item>
        <el-descriptions-item label="维护方">{{ detailRow.scopeType === RENTAL_CODE.ORDER_CHANNEL_SCOPE.MERCHANT ? "当前商户" : "平台" }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailRow.status === NORMAL_DISABLE_CODE.NORMAL ? "启用" : "停用" }}</el-descriptions-item>
        <el-descriptions-item label="排序">{{ detailRow.sortNo ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailRow.createTime || "-" }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailRow.remark || "-" }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="formDialog" :title="editing ? '修改自定义渠道' : '新增自定义渠道'" width="520px" append-to-body>
      <el-form :model="form" label-width="100px">
        <el-form-item v-if="editing" label="渠道编码"><el-input v-model="form.channelCode" disabled placeholder="保存后由系统生成" /></el-form-item>
        <el-form-item label="渠道名称" required><el-input v-model="form.channelName" maxlength="128" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="NORMAL_DISABLE_CODE.NORMAL" :inactive-value="NORMAL_DISABLE_CODE.DISABLED" active-text="启用" inactive-text="停用" inline-prompt /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortNo" :min="0" :max="9999" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.reason" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="formDialog = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveChannel">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="transferDialog" title="套餐或订单渠道转移" width="620px" append-to-body>
      <el-tabs v-model="transferType">
        <el-tab-pane label="套餐渠道" name="package">
          <el-form :model="packageTransfer" label-width="120px">
            <el-form-item label="套餐编号" required><el-input-number v-model="packageTransfer.packageId" :min="1" /></el-form-item>
            <el-form-item label="目标渠道" required><el-select v-model="packageTransfer.toChannelId" placeholder="请选择" class="wide"><el-option v-for="item in activeChannels" :key="item.channelId" :label="`${item.channelName}（${item.channelCode}）`" :value="item.channelId" /></el-select></el-form-item>
            <el-form-item label="幂等键" required><el-input v-model="packageTransfer.idempotencyKey" placeholder="每次业务操作使用唯一键" /></el-form-item>
            <el-form-item label="原因"><el-input v-model="packageTransfer.reason" type="textarea" :rows="2" /></el-form-item>
          </el-form>
          <el-button type="primary" :loading="saving" @click="submitPackageTransfer">转移套餐渠道</el-button>
        </el-tab-pane>
        <el-tab-pane label="订单渠道" name="orders">
          <el-form :model="orderTransfer" label-width="120px">
            <el-form-item label="订单号" required><el-input v-model="orderTransfer.orderIdsText" placeholder="多个编号使用逗号分隔" /></el-form-item>
            <el-form-item label="目标渠道" required><el-select v-model="orderTransfer.toChannelId" placeholder="请选择" class="wide"><el-option v-for="item in activeChannels" :key="item.channelId" :label="`${item.channelName}（${item.channelCode}）`" :value="item.channelId" /></el-select></el-form-item>
            <el-form-item label="幂等键" required><el-input v-model="orderTransfer.idempotencyKey" placeholder="本批次使用唯一键" /></el-form-item>
            <el-form-item label="原因"><el-input v-model="orderTransfer.reason" type="textarea" :rows="2" /></el-form-item>
          </el-form>
          <el-button type="primary" :loading="saving" @click="submitOrderTransfer">转移订单渠道</el-button>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addMerchantOrderChannel,
  deleteMerchantOrderChannel,
  listMerchantOrderChannels,
  transferOrderChannels,
  transferPackageChannel,
  updateMerchantOrderChannel
} from '@/api/merchant/orderChannel'
import { RENTAL_CODE } from '@/utils/rental/options'
import { NORMAL_DISABLE_CODE } from '@/constants/options'

const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const showSearch = ref(true)
const formDialog = ref(false)
const detailVisible = ref(false)
const detailRow = ref(null)
const transferDialog = ref(false)
const editing = ref(false)
const transferType = ref('package')
const query = reactive({ keyword: '', status: undefined })
const form = reactive({ channelId: undefined, channelCode: '', channelName: '', status: NORMAL_DISABLE_CODE.NORMAL, sortNo: 0, reason: '' })
const packageTransfer = reactive({ packageId: undefined, toChannelId: undefined, idempotencyKey: '', reason: '' })
const orderTransfer = reactive({ orderIdsText: '', toChannelId: undefined, idempotencyKey: '', reason: '' })
const activeChannels = computed(() => rows.value.filter(item => item.status === NORMAL_DISABLE_CODE.NORMAL))

/** 加载平台联盟渠道和当前商户自定义渠道。 */
function load() {
  loading.value = true
  listMerchantOrderChannels(query).then(response => { rows.value = response.rows || response.data?.rows || [] }).finally(() => { loading.value = false })
}

/** 清空渠道查询条件。 */
function resetQuery() {
  query.keyword = ''
  query.status = undefined
  load()
}

/** 打开新增自定义渠道表单。 */
function openCreate() {
  editing.value = false
  Object.assign(form, { channelId: undefined, channelCode: '', channelName: '', status: NORMAL_DISABLE_CODE.NORMAL, sortNo: 0, reason: '' })
  formDialog.value = true
}

/** 打开自定义渠道编辑表单。 */
function openEdit(row) {
  editing.value = true
  Object.assign(form, row, { reason: row.remark || '' })
  formDialog.value = true
}

/** 查看当前渠道详情。 */
function openDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

/** 保存新增或修改的商户自定义渠道。 */
function saveChannel() {
  if (!form.channelName.trim()) return ElMessage.warning('请输入渠道名称')
  saving.value = true
  const action = editing.value ? updateMerchantOrderChannel : addMerchantOrderChannel
  action({ ...form }).then(() => { ElMessage.success('保存成功'); formDialog.value = false; load() }).finally(() => { saving.value = false })
}

/** 删除自定义渠道，后端会拒绝仍有业务引用的渠道。 */
function removeRow(row) {
  ElMessageBox.confirm(`确认删除渠道“${row.channelName}”吗？`, '操作确认', { type: 'warning' }).then(() => deleteMerchantOrderChannel(row.channelId)).then(() => { ElMessage.success('删除成功'); load() })
}

/** 提交套餐默认渠道转移。 */
function submitPackageTransfer() {
  if (!packageTransfer.packageId || !packageTransfer.toChannelId || !packageTransfer.idempotencyKey.trim()) return ElMessage.warning('请完整填写套餐、目标渠道和幂等键')
  saving.value = true
  transferPackageChannel({ ...packageTransfer }).then(() => { ElMessage.success('套餐渠道转移成功'); transferDialog.value = false }).finally(() => { saving.value = false })
}

/** 提交批量订单渠道转移。 */
function submitOrderTransfer() {
  const orderIds = orderTransfer.orderIdsText.split(',').map(item => Number(item.trim())).filter(item => Number.isInteger(item) && item > 0)
  if (!orderIds.length || !orderTransfer.toChannelId || !orderTransfer.idempotencyKey.trim()) return ElMessage.warning('请完整填写订单、目标渠道和幂等键')
  saving.value = true
  transferOrderChannels({ ...orderTransfer, orderIds }).then(response => { ElMessage.success(`已转移 ${response.data || 0} 个订单`); transferDialog.value = false }).finally(() => { saving.value = false })
}

load()
</script>

<style scoped>
.wide { width: 100%; }
.muted { color: var(--el-text-color-secondary); }
</style>
