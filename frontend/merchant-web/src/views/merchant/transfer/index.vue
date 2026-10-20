<template>
  <div class="app-container warehouse-transfer-page">
    <div class="page-heading">
      <div>
        <h2>{{ isAutomatic ? "自动仓调" : "智能仓调" }}</h2>
        <p>{{ isAutomatic ? "按目标仓库和可用库存排序推荐来源；转仓仍由你逐件确认" : "根据当前仓库可用库存选择来源设备并转至目标仓库" }}</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadInventory">刷新库存</el-button>
    </div>

    <el-alert v-if="isAutomatic" class="policy-alert" title="自动仓调当前提供来源仓排序建议，不会后台自动扣减库存；确认具体设备后将立即完成转仓。" type="info" :closable="false" show-icon />
    <el-alert v-else class="policy-alert" title="当前转仓能力按逐件设备立即变更仓库；已关联订单或非可用状态的设备不能转仓。" type="info" :closable="false" show-icon />

    <div class="summary-grid">
      <div class="summary-item"><span>来源仓商品</span><strong>{{ total }}</strong></div>
      <div class="summary-item"><span>本页可用数量</span><strong>{{ currentAvailable }}</strong></div>
      <div class="summary-item"><span>目标仓库</span><strong class="summary-name">{{ selectedWarehouseName || "未选择" }}</strong></div>
    </div>

    <el-form :inline="true" :model="query" class="search-form" @submit.prevent>
      <el-form-item label="目标仓库" required>
        <el-select v-model="query.targetWarehouseId" clearable filterable placeholder="请选择目标仓库" @change="handleTargetWarehouseChange">
          <el-option v-for="item in warehouses" :key="warehouseId(item)" :label="warehouseLabel(item)" :value="warehouseId(item)" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源仓库">
        <el-select v-model="query.sourceWarehouseId" clearable filterable placeholder="全部来源仓" @change="handleSourceWarehouseChange">
          <el-option v-for="item in sourceWarehouseOptions" :key="warehouseId(item)" :label="warehouseLabel(item)" :value="warehouseId(item)" />
        </el-select>
      </el-form-item>
      <el-form-item label="商品 / 型号">
        <el-input v-model="query.keyword" clearable placeholder="输入商品名称或型号" @keyup.enter="search" />
      </el-form-item>
      <el-form-item><el-button type="primary" :icon="Search" @click="search">查询</el-button><el-button :icon="Refresh" @click="reset">重置</el-button></el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="recommendedRows" border row-key="inventoryId">
      <el-table-column v-if="isAutomatic" label="建议顺序" width="105" align="center"><template #default="{ $index }"><el-tag :type="$index === 0 ? 'success' : 'info'">{{ $index === 0 ? "优先推荐" : `候选 ${$index + 1}` }}</el-tag></template></el-table-column>
      <el-table-column label="商品" min-width="220"><template #default="{ row }"><strong>{{ row.productName || "未命名商品" }}</strong><span class="secondary-text">{{ row.modelName || row.productCode || "-" }}</span></template></el-table-column>
      <el-table-column label="来源仓库" prop="warehouseName" min-width="155" />
      <el-table-column label="可用数量" prop="availableQuantity" width="110" align="center" />
      <el-table-column label="转入目标" min-width="165"><template #default>{{ selectedWarehouseName || "请先选择目标仓库" }}</template></el-table-column>
      <el-table-column v-if="isAutomatic" label="推荐依据" min-width="190"><template #default="{ row }">{{ recommendationText(row) }}</template></el-table-column>
      <el-table-column v-else label="商品编码" prop="productCode" min-width="150" />
      <el-table-column label="操作" width="140" fixed="right" align="center"><template #default="{ row }"><el-button link type="primary" :disabled="!query.targetWarehouseId || Number(row.warehouseId) === Number(query.targetWarehouseId)" @click="openUnitPicker(row)">选择设备</el-button></template></el-table-column>
      <template #empty><el-empty description="没有可调拨的可用库存" :image-size="86" /></template>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="loadInventory" />

    <el-dialog v-model="unitPickerVisible" title="选择转仓设备" width="820px" append-to-body>
      <div v-if="selectedInventory" class="selected-inventory">
        <span><small>商品</small>{{ selectedInventory.productName || selectedInventory.modelName }}</span>
        <span><small>来源仓库</small>{{ selectedInventory.warehouseName }}</span>
        <span><small>目标仓库</small>{{ selectedWarehouseName || "-" }}</span>
      </div>
      <el-alert v-if="unitLoadMessage" :title="unitLoadMessage" type="warning" :closable="false" show-icon class="unit-alert" />
      <el-table v-loading="unitLoading" :data="transferableUnits" border highlight-current-row @current-change="selectUnit">
      <el-table-column width="54" align="center"><template #default="{ row }"><el-radio v-model="selectedUnitId" :value="row.unitId" /></template></el-table-column>
        <el-table-column label="序列号" min-width="180"><template #default="{ row }">{{ row.serialNumber || "-" }}</template></el-table-column>
        <el-table-column label="库存编码" prop="unitCode" min-width="175" />
        <el-table-column label="产品" prop="productName" min-width="180" />
        <el-table-column label="状态" width="100" align="center"><template #default="{ row }"><el-tag type="success">{{ unitStatusText(row.unitStatus) }}</el-tag></template></el-table-column>
      </el-table>
      <pagination v-show="unitTotal > 0" :total="unitTotal" v-model:page="unitQuery.pageNum" v-model:limit="unitQuery.pageSize" @pagination="loadUnits" />
      <template #footer>
        <el-button @click="unitPickerVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedUnitId" :loading="transferring" @click="confirmTransfer">确认转仓</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Refresh, Search } from "@element-plus/icons-vue";
import { listMerchantInventory, listMerchantInventoryUnits, transferMerchantInventoryUnit } from "@/api/merchant/inventory";
import { listMerchantWarehouses } from "@/api/merchant/rentalBusiness";
import { RENTAL_CODE } from "@/utils/rental/options";

const route = useRoute();
const isAutomatic = computed(() => route.path.endsWith("/automatic"));
const loading = ref(false);
const unitLoading = ref(false);
const transferring = ref(false);
const rows = ref([]);
const total = ref(0);
const warehouses = ref([]);
const selectedInventory = ref(null);
const transferableUnits = ref([]);
const unitTotal = ref(0);
const unitLoadMessage = ref("");
const selectedUnitId = ref(null);
const unitPickerVisible = ref(false);
const query = reactive({ pageNum: 1, pageSize: 10, targetWarehouseId: undefined, sourceWarehouseId: undefined, keyword: "" });
const unitQuery = reactive({ pageNum: 1, pageSize: 10 });

function unwrap(response) { return response?.data ?? response; }
function rowsOf(response) { const data = unwrap(response); return data?.rows || data || []; }
function errorText(error) { return error?.msg || error?.message || "操作失败，请稍后重试"; }
function warehouseId(item) { return item?.warehouseId ?? item?.id; }
function warehouseLabel(item) { const name = item?.warehouseName ?? item?.name ?? "仓库"; const code = item?.warehouseCode ?? item?.code; return code ? `${name}（${code}）` : name; }
function unitStatusText(value) { return Number(value) === RENTAL_CODE.INVENTORY_STATUS.AVAILABLE ? "可用" : "不可转仓"; }
const sourceWarehouseOptions = computed(() => warehouses.value.filter(item => Number(warehouseId(item)) !== Number(query.targetWarehouseId)));
const selectedWarehouseName = computed(() => warehouses.value.find(item => Number(warehouseId(item)) === Number(query.targetWarehouseId))?.warehouseName || warehouses.value.find(item => Number(warehouseId(item)) === Number(query.targetWarehouseId))?.name || "");
const recommendedRows = computed(() => {
  const result = rows.value.filter(row => Number(row.availableQuantity) > 0 && Number(row.warehouseId) !== Number(query.targetWarehouseId));
  if (isAutomatic.value) result.sort((a, b) => Number(b.availableQuantity) - Number(a.availableQuantity));
  return result;
});
const currentAvailable = computed(() => recommendedRows.value.reduce((sum, row) => sum + Number(row.availableQuantity || 0), 0));

async function loadWarehouses() {
  try { warehouses.value = rowsOf(await listMerchantWarehouses({ pageNum: 1, pageSize: 1000, status: 0 })); }
  catch (error) { ElMessage.error(errorText(error)); }
}
async function loadInventory() {
  loading.value = true;
  try {
    const response = await listMerchantInventory({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      warehouseId: query.sourceWarehouseId || undefined,
      excludeWarehouseId: query.targetWarehouseId || undefined,
      unitStatus: RENTAL_CODE.INVENTORY_STATUS.AVAILABLE
    });
    const data = unwrap(response);
    rows.value = data?.rows || [];
    total.value = Number(data?.total || 0);
  } catch (error) { ElMessage.error(errorText(error)); }
  finally { loading.value = false; }
}
function search() { query.pageNum = 1; loadInventory(); }
function reset() { Object.assign(query, { pageNum: 1, pageSize: 10, targetWarehouseId: undefined, sourceWarehouseId: undefined, keyword: "" }); loadInventory(); }
function handleTargetWarehouseChange() {
  if (Number(query.sourceWarehouseId) === Number(query.targetWarehouseId)) query.sourceWarehouseId = undefined;
  query.pageNum = 1;
  loadInventory();
}
function handleSourceWarehouseChange() { query.pageNum = 1; loadInventory(); }
function recommendationText(row) { return `可用 ${Number(row.availableQuantity || 0)} 件，优先从可用量较多的来源仓选取`; }
async function openUnitPicker(row) {
  if (!query.targetWarehouseId) return ElMessage.warning("请先选择目标仓库");
  if (Number(row.warehouseId) === Number(query.targetWarehouseId)) return ElMessage.warning("目标仓库不能与来源仓相同");
  selectedInventory.value = row;
  selectedUnitId.value = null;
  unitLoadMessage.value = "";
  Object.assign(unitQuery, { pageNum: 1, pageSize: 10 });
  unitPickerVisible.value = true;
  await loadUnits();
}
async function loadUnits() {
  if (!selectedInventory.value) return;
  unitLoading.value = true;
  try {
    const response = await listMerchantInventoryUnits(selectedInventory.value.inventoryId, {
      ...unitQuery,
      warehouseId: selectedInventory.value.warehouseId,
      unitStatus: RENTAL_CODE.INVENTORY_STATUS.AVAILABLE,
      transferableOnly: true
    });
    const data = unwrap(response);
    transferableUnits.value = data?.rows || [];
    unitTotal.value = Number(data?.total || 0);
    if (!transferableUnits.value.length) unitLoadMessage.value = "该库存没有未关联订单的可用逐件设备。";
  } catch (error) { unitLoadMessage.value = errorText(error); transferableUnits.value = []; }
  finally { unitLoading.value = false; }
}
function selectUnit(row) { selectedUnitId.value = row?.unitId ?? null; }
async function confirmTransfer() {
  if (!selectedUnitId.value || !query.targetWarehouseId) return;
  try {
    await ElMessageBox.confirm(`将所选设备从“${selectedInventory.value.warehouseName}”立即转入“${selectedWarehouseName.value}”？`, "确认转仓", { type: "warning" });
  } catch (error) { if (error === "cancel" || error === "close") return; throw error; }
  transferring.value = true;
  try {
    await transferMerchantInventoryUnit({ unitId: selectedUnitId.value, targetWarehouseId: query.targetWarehouseId });
    ElMessage.success("设备已转仓");
    unitPickerVisible.value = false;
    await loadInventory();
  } catch (error) { ElMessage.error(errorText(error)); }
  finally { transferring.value = false; }
}
watch(() => route.path, () => { query.pageNum = 1; loadInventory(); });
onMounted(async () => { await loadWarehouses(); await loadInventory(); });
</script>

<style scoped>
.page-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 14px; }
.page-heading h2 { margin: 0; font-size: 20px; font-weight: 600; }
.page-heading p { margin: 6px 0 0; color: var(--el-text-color-secondary); }
.policy-alert { margin-bottom: 15px; }
.summary-grid { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 12px; margin-bottom: 16px; }
.summary-item { display: flex; min-height: 74px; align-items: center; justify-content: space-between; padding: 12px 16px; border: 1px solid var(--el-border-color-light); border-radius: 4px; }
.summary-item span { color: var(--el-text-color-secondary); }
.summary-item strong { color: var(--el-color-primary); font-size: 20px; }
.summary-name { max-width: 65%; overflow: hidden; color: var(--el-text-color-primary) !important; font-size: 15px !important; text-overflow: ellipsis; white-space: nowrap; }
.search-form { margin-bottom: 12px; }
.secondary-text { display: block; margin-top: 3px; color: var(--el-text-color-secondary); font-size: 12px; }
.selected-inventory { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 12px; margin-bottom: 15px; }
.selected-inventory span { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.selected-inventory small { color: var(--el-text-color-secondary); }
.unit-alert { margin-bottom: 12px; }
@media (max-width: 720px) { .page-heading { flex-direction: column; } .summary-grid { grid-template-columns: 1fr; } .selected-inventory { grid-template-columns: 1fr; } }
</style>
