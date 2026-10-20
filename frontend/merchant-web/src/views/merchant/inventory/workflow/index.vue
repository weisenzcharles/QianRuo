<template>
  <div class="app-container merchant-list-page">
    <el-alert title="预占会冻结可用库存，释放或过期会恢复可接单容量，消耗才会扣减真实库存；所有写操作必须携带幂等键。" type="info" :closable="false" />
    <el-row :gutter="16" class="workflow-grid">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>创建库存预占</template>
          <el-form :model="reserveForm" label-width="110px">
            <el-form-item label="套餐" required><el-select v-model="reserveForm.packageId" filterable clearable placeholder="请选择套餐" class="full-width"><el-option v-for="item in packageOptions" :key="item.packageId" :label="`${item.packageName}（${item.packageCode}）`" :value="item.packageId" /></el-select></el-form-item>
            <el-form-item label="仓库" required><el-select v-model="reserveForm.warehouseId" filterable clearable placeholder="请选择仓库" class="full-width"><el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="`${item.warehouseName}（${item.warehouseCode}）`" :value="item.warehouseId" /></el-select></el-form-item>
            <el-form-item label="数量" required><el-input-number v-model="reserveForm.quantity" :min="1" /></el-form-item>
            <el-form-item label="订单" required><el-select v-model="reserveForm.orderId" filterable clearable placeholder="请选择订单" class="full-width"><el-option v-for="item in orderOptions" :key="item.orderId" :label="item.orderNo" :value="item.orderId" /></el-select></el-form-item>
            <el-form-item label="幂等键" required><el-input v-model="reserveForm.idempotencyKey" /></el-form-item>
            <el-form-item label="原因"><el-input v-model="reserveForm.reason" type="textarea" :rows="2" /></el-form-item>
          </el-form>
          <el-button type="primary" :loading="saving" @click="reserve">创建预占</el-button>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>预占生命周期</template>
          <el-form :model="actionForm" label-width="110px">
            <el-form-item label="预占单号" required><el-input v-model="actionForm.reservationNo" clearable placeholder="请输入预占单号" @keyup.enter="loadReservation" /><el-button class="query-button" icon="Search" @click="loadReservation">查询</el-button></el-form-item>
            <el-form-item label="动作"><el-radio-group v-model="actionForm.action"><el-radio value="release">释放</el-radio><el-radio value="consume">消耗</el-radio><el-radio value="expire">过期</el-radio></el-radio-group></el-form-item>
            <el-form-item label="幂等键" required><el-input v-model="actionForm.idempotencyKey" /></el-form-item>
            <el-form-item label="原因"><el-input v-model="actionForm.reason" type="textarea" :rows="2" /></el-form-item>
          </el-form>
          <el-button type="warning" :loading="saving" @click="changeReservation">执行生命周期动作</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-card shadow="never" class="transition-card">
      <template #header>库存状态迁移</template>
      <el-form :model="transitionForm" :inline="true">
        <el-form-item label="库存编码" required><el-input v-model="transitionForm.unitCode" clearable placeholder="请输入库存单元编码" /></el-form-item>
        <el-form-item label="原状态"><el-select v-model="transitionForm.fromStatus"><el-option v-for="item in inventoryStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="目标状态"><el-select v-model="transitionForm.toStatus"><el-option v-for="item in inventoryStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="幂等键"><el-input v-model="transitionForm.idempotencyKey" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="transitionForm.reason" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="saving" @click="transition">提交状态迁移</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card v-if="reservation" shadow="never" class="result-card">
      <template #header>最近预占结果</template>
      <el-descriptions :column="3" border label-width="120">
        <el-descriptions-item label="预占单号">{{ reservation.reservationNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag>{{ reservationStatusLabel(reservation.reservationStatus) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="数量">{{ reservation.quantity }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getInventoryReservationByNo,
  releaseInventoryReservationByNo,
  reserveInventory,
  transitionInventory,
  consumeInventoryReservationByNo,
  expireInventoryReservationByNo
} from '@/api/merchant/inventoryWorkflow'
import { listMerchantOrders } from '@/api/merchant/order'
import { listMerchantPackages, listMerchantWarehouses } from '@/api/merchant/rentalBusiness'
import { RENTAL_CODE } from '@/utils/rental/options'

const saving = ref(false)
const reservation = ref(null)
const packageOptions = ref([])
const warehouseOptions = ref([])
const orderOptions = ref([])
const inventoryStatusOptions = [
  { value: RENTAL_CODE.INVENTORY_STATUS.AVAILABLE, label: '可用' },
  { value: RENTAL_CODE.INVENTORY_STATUS.RESERVED, label: '已预占' },
  { value: RENTAL_CODE.INVENTORY_STATUS.RENTED, label: '租赁中' },
  { value: RENTAL_CODE.INVENTORY_STATUS.REPAIRING, label: '维修中' },
  { value: RENTAL_CODE.INVENTORY_STATUS.LOST, label: '遗失' },
  { value: RENTAL_CODE.INVENTORY_STATUS.RETIRED, label: '已报废' }
]
const reservationStatusOptions = [
  { value: RENTAL_CODE.RESERVATION_STATUS.RESERVED, label: '已预占' },
  { value: RENTAL_CODE.RESERVATION_STATUS.LOCKED, label: '已锁定' },
  { value: RENTAL_CODE.RESERVATION_STATUS.RELEASED, label: '已释放' },
  { value: RENTAL_CODE.RESERVATION_STATUS.CONSUMED, label: '已消耗' },
  { value: RENTAL_CODE.RESERVATION_STATUS.EXPIRED, label: '已过期' }
]
const reserveForm = reactive({ packageId: undefined, warehouseId: undefined, quantity: 1, orderId: undefined, idempotencyKey: '', reason: '' })
const actionForm = reactive({ reservationNo: '', action: 'release', idempotencyKey: '', reason: '' })
const transitionForm = reactive({
  unitCode: '',
  fromStatus: RENTAL_CODE.INVENTORY_STATUS.AVAILABLE,
  toStatus: RENTAL_CODE.INVENTORY_STATUS.RESERVED,
  idempotencyKey: '',
  reason: ''
})

function reservationStatusLabel(value) {
  return reservationStatusOptions.find(item => item.value === value)?.label || '-'
}

/** 创建套餐库存预占并展示后端返回的预占单。 */
function reserve() {
  if (!reserveForm.packageId || !reserveForm.warehouseId || !reserveForm.orderId || !reserveForm.quantity || !reserveForm.idempotencyKey.trim()) return ElMessage.warning('请完整填写预占参数')
  saving.value = true
  reserveInventory({ ...reserveForm }).then(response => { reservation.value = response.data; ElMessage.success('库存预占成功') }).finally(() => { saving.value = false })
}

/** 执行释放、消耗或过期动作，并刷新预占状态。 */
function changeReservation() {
  if (!actionForm.reservationNo.trim() || !actionForm.idempotencyKey.trim()) return ElMessage.warning('请填写预占单号和幂等键')
  saving.value = true
  const data = { idempotencyKey: actionForm.idempotencyKey, reason: actionForm.reason }
  const actions = { release: releaseInventoryReservationByNo, consume: consumeInventoryReservationByNo, expire: expireInventoryReservationByNo }
  actions[actionForm.action](actionForm.reservationNo.trim(), data).then(response => { reservation.value = response.data; ElMessage.success('预占状态已更新') }).finally(() => { saving.value = false })
}

/** 提交数量库存或逐件库存状态迁移。 */
function transition() {
  if (!transitionForm.unitCode.trim() || !transitionForm.idempotencyKey.trim()) return ElMessage.warning('请填写库存编码和幂等键')
  saving.value = true
  transitionInventory({ ...transitionForm }).then(() => ElMessage.success('库存状态迁移成功')).finally(() => { saving.value = false })
}

/** 根据输入的预占编号查询最近结果，供页面刷新后继续操作。 */
function loadReservation() {
  if (!actionForm.reservationNo.trim()) return
  getInventoryReservationByNo(actionForm.reservationNo.trim()).then(response => { reservation.value = response.data })
}

function responseRows(response) {
  const data = response?.data ?? response
  if (Array.isArray(data)) return data
  return Array.isArray(data?.rows) ? data.rows : []
}

async function loadOptions() {
  const [packages, warehouses, orders] = await Promise.all([
    listMerchantPackages({ status: 0, pageNum: 1, pageSize: 1000 }),
    listMerchantWarehouses({ status: 0, pageNum: 1, pageSize: 1000 }),
    listMerchantOrders({ pageNum: 1, pageSize: 1000 })
  ])
  packageOptions.value = responseRows(packages)
  warehouseOptions.value = responseRows(warehouses)
  orderOptions.value = responseRows(orders)
}

onMounted(() => { loadOptions() })

</script>

<style scoped>
.workflow-grid { margin-top: 16px; }
.transition-card, .result-card { margin-top: 16px; }
.query-button { margin-left: 8px; }
.full-width { width: 100%; }
</style>
