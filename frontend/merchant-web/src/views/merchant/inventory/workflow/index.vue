<template>
  <div class="app-container merchant-list-page">
    <el-alert title="预占只冻结资源池可用量，释放或过期会恢复可接单容量，消耗才会扣减真实库存；所有写操作必须携带幂等键。" type="info" :closable="false" />
    <el-row :gutter="16" class="workflow-grid">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>创建库存预占</template>
          <el-form :model="reserveForm" label-width="110px">
            <el-form-item label="套餐 ID" required><el-input-number v-model="reserveForm.packageId" :min="1" /></el-form-item>
            <el-form-item label="仓库 ID" required><el-input-number v-model="reserveForm.warehouseId" :min="1" /></el-form-item>
            <el-form-item label="数量" required><el-input-number v-model="reserveForm.quantity" :min="1" /></el-form-item>
            <el-form-item label="订单 ID"><el-input-number v-model="reserveForm.orderId" :min="1" /></el-form-item>
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
            <el-form-item label="预占 ID" required><el-input-number v-model="actionForm.reservationId" :min="1" /><el-button class="query-button" icon="Search" @click="loadReservation">查询</el-button></el-form-item>
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
        <el-form-item label="库存单元 ID"><el-input-number v-model="transitionForm.unitId" :min="1" /></el-form-item>
        <el-form-item label="或单元编码"><el-input v-model="transitionForm.unitCode" /></el-form-item>
        <el-form-item label="SN ID"><el-input-number v-model="transitionForm.snId" :min="1" /></el-form-item>
        <el-form-item label="或 SN 编码"><el-input v-model="transitionForm.snCode" /></el-form-item>
        <el-form-item label="商品 ID"><el-input-number v-model="transitionForm.productId" :min="1" /></el-form-item>
        <el-form-item label="仓库 ID"><el-input-number v-model="transitionForm.warehouseId" :min="1" /></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="transitionForm.changeQty" :min="1" /></el-form-item>
        <el-form-item label="原状态"><el-select v-model="transitionForm.fromStatus"><el-option v-for="item in states" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        <el-form-item label="目标状态"><el-select v-model="transitionForm.toStatus"><el-option v-for="item in states" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        <el-form-item label="幂等键"><el-input v-model="transitionForm.idempotencyKey" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="transitionForm.reason" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="saving" @click="transition">提交状态迁移</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card v-if="reservation" shadow="never" class="result-card">
      <template #header>最近预占结果</template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="预占 ID">{{ reservation.reservationId }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag>{{ reservation.reservationStatus }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="数量">{{ reservation.quantity }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  consumeInventoryReservation,
  expireInventoryReservation,
  getInventoryReservation,
  releaseInventoryReservation,
  reserveInventory,
  transitionInventory
} from '@/api/merchant/inventoryWorkflow'

const saving = ref(false)
const reservation = ref(null)
const states = ['available', 'reserved', 'rented', 'repairing', 'lost', 'retired']
const reserveForm = reactive({ packageId: undefined, warehouseId: undefined, quantity: 1, orderId: undefined, idempotencyKey: '', reason: '' })
const actionForm = reactive({ reservationId: undefined, action: 'release', idempotencyKey: '', reason: '' })
const transitionForm = reactive({ unitId: undefined, unitCode: '', snId: undefined, snCode: '', productId: undefined, warehouseId: undefined, changeQty: 1, fromStatus: 'available', toStatus: 'reserved', idempotencyKey: '', reason: '' })

/** 创建套餐库存预占并展示后端返回的预占单。 */
function reserve() {
  if (!reserveForm.packageId || !reserveForm.warehouseId || !reserveForm.quantity || !reserveForm.idempotencyKey.trim()) return ElMessage.warning('请完整填写预占参数')
  saving.value = true
  reserveInventory({ ...reserveForm }).then(response => { reservation.value = response.data; ElMessage.success('库存预占成功') }).finally(() => { saving.value = false })
}

/** 执行释放、消耗或过期动作，并刷新预占状态。 */
function changeReservation() {
  if (!actionForm.reservationId || !actionForm.idempotencyKey.trim()) return ElMessage.warning('请填写预占 ID 和幂等键')
  saving.value = true
  const data = { idempotencyKey: actionForm.idempotencyKey, reason: actionForm.reason }
  const actions = { release: releaseInventoryReservation, consume: consumeInventoryReservation, expire: expireInventoryReservation }
  actions[actionForm.action](actionForm.reservationId, data).then(response => { reservation.value = response.data; ElMessage.success('预占状态已更新') }).finally(() => { saving.value = false })
}

/** 提交数量库存或逐件库存状态迁移。 */
function transition() {
  if (!transitionForm.idempotencyKey.trim() || !transitionForm.warehouseId) return ElMessage.warning('请填写仓库 ID 和幂等键')
  saving.value = true
  transitionInventory({ ...transitionForm }).then(() => ElMessage.success('库存状态迁移成功')).finally(() => { saving.value = false })
}

/** 根据输入的预占 ID 查询最近结果，供页面刷新后继续操作。 */
function loadReservation() {
  if (!actionForm.reservationId) return
  getInventoryReservation(actionForm.reservationId).then(response => { reservation.value = response.data })
}

</script>

<style scoped>
.workflow-grid { margin-top: 16px; }
.transition-card, .result-card { margin-top: 16px; }
.query-button { margin-left: 8px; }
</style>
