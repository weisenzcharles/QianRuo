<template>
  <div class="app-container inventory-alert-page">
    <header class="page-heading">
      <div>
        <p class="page-kicker">INVENTORY ALERT</p>
        <h2>库存告警</h2>
        <p class="page-description">设置低库存阈值并跟踪需要处理的告警事件</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="refresh">刷新</el-button>
    </header>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="section-header"><strong>告警规则</strong><el-button type="primary" :icon="Plus" @click="openRule()">新增规则</el-button></div>
      </template>
      <el-table v-loading="loading" :data="rules" border row-key="ruleId">
        <el-table-column label="适用仓库" prop="warehouseName" min-width="150" />
        <el-table-column label="适用商品" prop="productName" min-width="180" show-overflow-tooltip />
        <el-table-column label="告警条件" min-width="180">
          <template #default="{ row }">{{ row.thresholdTypeLabel }} ≤ {{ row.thresholdValue }}{{ Number(row.thresholdType) === THRESHOLD_TYPE.PERCENT ? '%' : ' 件' }}</template>
        </el-table-column>
        <el-table-column label="通知" prop="notifyEnabledLabel" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center"><template #default="{ row }"><el-tag :type="Number(row.ruleStatus) === RULE_STATUS.ENABLED ? 'success' : 'info'">{{ row.ruleStatusLabel }}</el-tag></template></el-table-column>
        <el-table-column label="更新时间" prop="updateTime" width="170" />
        <el-table-column label="操作" fixed="right" width="150" align="center">
          <template #default="{ row }"><el-button link type="primary" @click="openRule(row)">编辑</el-button><el-button link type="danger" @click="removeRule(row)">删除</el-button></template>
        </el-table-column>
        <template #empty><el-empty description="暂无告警规则" /></template>
      </el-table>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="section-header"><strong>告警事件</strong><el-form :inline="true" :model="eventQuery" @submit.prevent><el-form-item label="状态"><el-select v-model="eventQuery.alertStatus" clearable placeholder="全部状态" style="width: 140px" @change="loadEvents"><el-option v-for="item in eventStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-form></div>
      </template>
      <el-table v-loading="eventLoading" :data="events" border row-key="alertId">
        <el-table-column label="仓库" prop="warehouseName" min-width="140" />
        <el-table-column label="商品" prop="productName" min-width="180" show-overflow-tooltip />
        <el-table-column label="当前库存" width="120" align="center"><template #default="{ row }">{{ row.snapshotAvailableQuantity }} / {{ row.snapshotTotalQuantity }}</template></el-table-column>
        <el-table-column label="告警说明" prop="alertMessage" min-width="260" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center"><template #default="{ row }"><el-tag :type="eventTagType(row)">{{ row.alertStatusLabel }}</el-tag></template></el-table-column>
        <el-table-column label="最近触发" prop="lastTriggeredTime" width="170" />
        <el-table-column label="操作" width="110" align="center"><template #default="{ row }"><el-button v-if="Number(row.alertStatus) === ALERT_STATUS.PENDING" link type="primary" @click="acknowledge(row)">确认</el-button><span v-else class="muted-text">-</span></template></el-table-column>
        <template #empty><el-empty description="暂无告警事件" /></template>
      </el-table>
    </el-card>

    <el-dialog v-model="ruleVisible" :title="ruleForm.ruleId ? '编辑告警规则' : '新增告警规则'" width="520px" append-to-body destroy-on-close>
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="ruleRules" label-width="100px">
        <el-form-item label="适用仓库"><el-select v-model="ruleForm.warehouseId" clearable filterable placeholder="全部可访问仓库" style="width: 100%"><el-option v-for="item in warehouses" :key="warehouseId(item)" :label="warehouseLabel(item)" :value="warehouseId(item)" /></el-select></el-form-item>
        <el-form-item label="适用商品"><el-select v-model="ruleForm.productId" clearable filterable placeholder="全部商品" style="width: 100%"><el-option v-for="item in products" :key="productId(item)" :label="productLabel(item)" :value="productId(item)" /></el-select></el-form-item>
        <el-form-item label="阈值类型" prop="thresholdType"><el-radio-group v-model="ruleForm.thresholdType"><el-radio-button :value="THRESHOLD_TYPE.PERCENT">可用库存百分比</el-radio-button><el-radio-button :value="THRESHOLD_TYPE.QUANTITY">可用库存数量</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="阈值" prop="thresholdValue"><el-input-number v-model="ruleForm.thresholdValue" :min="0" :max="ruleForm.thresholdType === THRESHOLD_TYPE.PERCENT ? 100 : 999999" :precision="ruleForm.thresholdType === THRESHOLD_TYPE.PERCENT ? 2 : 0" controls-position="right" style="width: 220px" /><span class="unit-text">{{ ruleForm.thresholdType === THRESHOLD_TYPE.PERCENT ? '%' : '件' }}</span></el-form-item>
        <el-form-item label="通知方式"><el-switch v-model="ruleForm.notifyEnabled" inline-prompt active-text="通知" inactive-text="静默" /></el-form-item>
        <el-form-item label="规则状态"><el-switch v-model="ruleForm.ruleStatus" :active-value="RULE_STATUS.ENABLED" :inactive-value="RULE_STATUS.DISABLED" inline-prompt active-text="启用" inactive-text="停用" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="ruleVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveRule">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { listMerchantInventoryOptions } from '@/api/merchant/inventory'
import { listMerchantWarehouses } from '@/api/merchant/rentalBusiness'
import { acknowledgeInventoryAlert, deleteInventoryAlertRule, listInventoryAlertEvents, listInventoryAlertRules, saveInventoryAlertRule } from '@/api/merchant/inventoryGovernance'

const THRESHOLD_TYPE = Object.freeze({ PERCENT: 1, QUANTITY: 2 })
const RULE_STATUS = Object.freeze({ ENABLED: 0, DISABLED: 1 })
const ALERT_STATUS = Object.freeze({ PENDING: 0, ACKNOWLEDGED: 1, RESOLVED: 2 })
const loading = ref(false)
const eventLoading = ref(false)
const saving = ref(false)
const rules = ref([])
const events = ref([])
const warehouses = ref([])
const products = ref([])
const ruleVisible = ref(false)
const ruleFormRef = ref()
const ruleForm = reactive({ ruleId: undefined, warehouseId: undefined, productId: undefined, thresholdType: THRESHOLD_TYPE.PERCENT, thresholdValue: 20, notifyEnabled: true, ruleStatus: RULE_STATUS.ENABLED })
const eventQuery = reactive({ alertStatus: undefined })
const eventStatusOptions = [{ value: ALERT_STATUS.PENDING, label: '待处理' }, { value: ALERT_STATUS.ACKNOWLEDGED, label: '已确认' }, { value: ALERT_STATUS.RESOLVED, label: '已恢复' }]
const ruleRules = { thresholdType: [{ required: true, message: '请选择阈值类型', trigger: 'change' }], thresholdValue: [{ required: true, message: '请输入阈值', trigger: 'change' }] }

function unwrap(response) { return response?.data ?? response }
function rowsOf(response) { const data = unwrap(response); return Array.isArray(data) ? data : data?.rows || [] }
function errorText(error) { return error?.msg || error?.message || '操作失败，请稍后重试' }
function warehouseId(item) { return item?.warehouseId ?? item?.id }
function warehouseLabel(item) { const name = item?.warehouseName ?? item?.name ?? '仓库'; const code = item?.warehouseCode ?? item?.code; return code ? `${name}（${code}）` : name }
function productId(item) { return item?.productId ?? item?.id }
function productLabel(item) { return [item?.productName ?? item?.name, item?.productCode ?? item?.code].filter(Boolean).join(' / ') }
function eventTagType(row) { return ({ 0: 'danger', 1: 'warning', 2: 'success' })[Number(row.alertStatus)] || 'info' }
async function loadRules() { const result = unwrap(await listInventoryAlertRules()); rules.value = rowsOf(result) }
async function loadEvents() { eventLoading.value = true; try { events.value = rowsOf(unwrap(await listInventoryAlertEvents(eventQuery))) } catch (error) { ElMessage.error(errorText(error)) } finally { eventLoading.value = false } }
async function loadOptions() { const [warehouseResult, productResult] = await Promise.all([listMerchantWarehouses({ pageNum: 1, pageSize: 1000, status: 0 }), listMerchantInventoryOptions({})]); warehouses.value = rowsOf(warehouseResult); products.value = rowsOf(productResult) }
async function refresh() { loading.value = true; try { await Promise.all([loadRules(), loadEvents(), loadOptions()]) } catch (error) { ElMessage.error(errorText(error)) } finally { loading.value = false } }
function openRule(row) { Object.assign(ruleForm, row ? { ruleId: row.ruleId, warehouseId: row.warehouseId, productId: row.productId, thresholdType: Number(row.thresholdType), thresholdValue: Number(row.thresholdValue), notifyEnabled: Number(row.notifyEnabled) === 1, ruleStatus: Number(row.ruleStatus) } : { ruleId: undefined, warehouseId: undefined, productId: undefined, thresholdType: THRESHOLD_TYPE.PERCENT, thresholdValue: 20, notifyEnabled: true, ruleStatus: RULE_STATUS.ENABLED }); ruleVisible.value = true }
async function saveRule() { const valid = await ruleFormRef.value?.validate().catch(() => false); if (!valid) return; saving.value = true; try { await saveInventoryAlertRule({ ...ruleForm, notifyEnabled: ruleForm.notifyEnabled }); ElMessage.success('告警规则已保存'); ruleVisible.value = false; await refresh() } catch (error) { ElMessage.error(errorText(error)) } finally { saving.value = false } }
async function removeRule(row) { try { await ElMessageBox.confirm('删除后将不再产生新的告警事件，是否继续？', '删除确认', { type: 'warning' }); await deleteInventoryAlertRule(row.ruleId); ElMessage.success('告警规则已删除'); await refresh() } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(errorText(error)) } }
async function acknowledge(row) { try { await acknowledgeInventoryAlert(row.alertId); ElMessage.success('告警已确认'); await loadEvents() } catch (error) { ElMessage.error(errorText(error)) } }
onMounted(refresh)
</script>

<style scoped>
.inventory-alert-page { min-height: 100%; }
.page-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.page-kicker { margin: 0 0 4px; color: var(--el-color-primary); font-size: 12px; font-weight: 700; letter-spacing: 1px; }
.page-heading h2 { margin: 0; }
.page-description { margin: 6px 0 0; color: var(--el-text-color-secondary); }
.section-card { margin-bottom: 16px; }
.section-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.section-header :deep(.el-form-item) { margin-bottom: 0; }
.unit-text { margin-left: 8px; color: var(--el-text-color-secondary); }
.muted-text { color: var(--el-text-color-secondary); }
</style>
