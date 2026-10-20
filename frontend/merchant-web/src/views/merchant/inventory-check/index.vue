<template>
  <div class="app-container inventory-check-page">
    <div class="page-heading">
      <div>
        <h2>{{ isScan ? "扫码盘点" : "手工盘点" }}</h2>
        <p>{{ isScan ? "扫描设备序列号或库存编码，自动核对所属仓库和设备状态" : "记录账面可用数量快照，再录入实盘数量并复核差异" }}</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">
        {{ isScan ? "新建扫码盘点" : "新建手工盘点" }}
      </el-button>
    </div>

    <el-row :gutter="12" class="summary-row">
      <el-col :xs="12" :sm="6" :md="6">
        <div class="summary-item">
          <span>进行中</span>
          <strong>{{ summary.processingCount || 0 }}</strong>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6">
        <div class="summary-item warning">
          <span>待复核差异</span>
          <strong>{{ summary.pendingReviewCount || 0 }}</strong>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6">
        <div class="summary-item success">
          <span>今日已完成</span>
          <strong>{{ summary.completedTodayCount || 0 }}</strong>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6">
        <div class="summary-item warning">
          <span>待处理异常</span>
          <strong>{{ summary.abnormalCount || 0 }}</strong>
        </div>
      </el-col>
    </el-row>

    <el-form v-if="showSearch" :inline="true" :model="query" class="search-form" @submit.prevent>
      <el-form-item label="盘点仓库">
        <el-select v-model="query.warehouseId" clearable filterable placeholder="全部仓库" @change="loadData">
          <el-option v-for="item in warehouses" :key="warehouseId(item)" :label="warehouseLabel(item)" :value="warehouseId(item)" />
        </el-select>
      </el-form-item>
      <el-form-item label="盘点状态">
        <el-select v-model="query.status" clearable placeholder="全部状态" @change="loadData">
          <el-option label="盘点中" :value="RENTAL_CODE.STOCKTAKING_STATUS.PROCESSING" />
          <el-option label="待复核" :value="RENTAL_CODE.STOCKTAKING_STATUS.REVIEW_REQUIRED" />
          <el-option label="已完成" :value="RENTAL_CODE.STOCKTAKING_STATUS.COMPLETED" />
          <el-option label="已关闭" :value="RENTAL_CODE.STOCKTAKING_STATUS.CLOSED" />
        </el-select>
      </el-form-item>
      <el-form-item label="盘点日期">
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="handleDateRangeChange" />
      </el-form-item>
      <el-form-item label="盘点单号">
        <el-input v-model="query.keyword" clearable placeholder="输入盘点单号" @keyup.enter="loadData" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button :icon="Refresh" @click="loadData">刷新</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadData" />
    </el-row>

    <el-card v-if="isScan && activeScan" class="scan-workspace" shadow="never">
      <template #header>
        <div class="workspace-heading">
          <div>
            <strong>{{ activeScan.stocktakingNo }}</strong>
            <span>{{ activeScan.warehouseName }} · {{ dateText(activeScan.stocktakingDate) }}</span>
          </div>
          <div class="workspace-actions">
            <el-tag :type="statusType(activeScan.status)">{{ statusText(activeScan.status) }}</el-tag>
            <el-button v-if="Number(activeScan.status) === RENTAL_CODE.STOCKTAKING_STATUS.PROCESSING" @click="scanPaused = !scanPaused">{{ scanPaused ? "继续扫码" : "暂停扫码" }}</el-button>
            <el-button v-if="Number(activeScan.status) === RENTAL_CODE.STOCKTAKING_STATUS.PROCESSING" type="success" plain @click="completeScan">结束盘点</el-button>
            <el-button @click="activeScan = null">收起</el-button>
          </div>
        </div>
      </template>
      <el-row :gutter="12" class="scan-stats">
        <el-col :xs="12" :sm="6"><span>已识别</span><strong>{{ activeItems.length }}</strong></el-col>
        <el-col :xs="12" :sm="6"><span>核对正常</span><strong>{{ normalScanCount }}</strong></el-col>
        <el-col :xs="12" :sm="6"><span>待处理异常</span><strong class="text-warning">{{ abnormalScanCount }}</strong></el-col>
        <el-col :xs="12" :sm="6"><span>本次重复扫描</span><strong>{{ duplicateScanCount }}</strong></el-col>
      </el-row>
      <el-form v-if="Number(activeScan.status) === RENTAL_CODE.STOCKTAKING_STATUS.PROCESSING" :model="scanForm" @submit.prevent>
        <el-form-item label="设备序列号 / 库存编码" class="scan-form-item">
          <el-input ref="scanInputRef" v-model="scanForm.code" clearable size="large" :disabled="scanPaused" placeholder="扫码后按回车确认" @keyup.enter.prevent="submitScan">
            <template #append><el-button :loading="scanLoading" :disabled="scanPaused" :icon="Search" @click="submitScan">确认识别</el-button></template>
          </el-input>
        </el-form-item>
      </el-form>
      <el-alert v-if="scanPaused" title="扫码已暂停，继续扫码后可恢复录入。" type="info" :closable="false" show-icon />
      <el-alert v-if="latestScan" :title="latestScan.message" :type="latestScan.duplicate ? 'warning' : Number(latestScan.scanResult) === RENTAL_CODE.STOCKTAKING_SCAN_RESULT.ABNORMAL ? 'error' : 'success'" :closable="false" show-icon />
      <el-table v-loading="detailLoading" :data="activeItems" border class="scan-results">
        <el-table-column label="识别内容" prop="scanCode" min-width="180" />
        <el-table-column label="产品" min-width="190">
          <template #default="{ row }">{{ row.productName || row.modelName || "未识别设备" }}</template>
        </el-table-column>
        <el-table-column label="设备状态" width="120">
          <template #default="{ row }">{{ inventoryStatusText(row.systemStatus) }}</template>
        </el-table-column>
          <el-table-column label="盘点结果" width="110" align="center">
          <template #default="{ row }"><el-tag :type="Number(row.scanResult) === RENTAL_CODE.STOCKTAKING_SCAN_RESULT.ABNORMAL ? 'danger' : 'success'">{{ Number(row.scanResult) === RENTAL_CODE.STOCKTAKING_SCAN_RESULT.ABNORMAL ? "异常" : "正常" }}</el-tag></template>
        </el-table-column>
        <el-table-column label="说明" prop="remark" min-width="180" show-overflow-tooltip />
        <el-table-column label="扫描时间" prop="createTime" width="170" />
      </el-table>
      <el-empty v-if="!detailLoading && !activeItems.length" description="尚未扫描设备" :image-size="64" />
      <p class="workspace-note">异常项会保留在盘点记录中；结束盘点仅归档本次结果，不会自动更改库存或设备状态。</p>
    </el-card>

    <el-table v-loading="loading" :data="rows" border row-key="stocktakingId">
      <el-table-column label="盘点单号" prop="stocktakingNo" min-width="170" />
      <el-table-column label="仓库" prop="warehouseName" min-width="130" />
      <el-table-column label="方式" width="115" align="center"><template #default="{ row }">{{ Number(row.mode) === RENTAL_CODE.STOCKTAKING_MODE_CODE.SCAN ? "扫码盘点" : "手工盘点" }}</template></el-table-column>
      <el-table-column label="盘点日期" prop="stocktakingDate" width="125" />
      <el-table-column label="盘点范围" prop="scopeLabel" min-width="160" show-overflow-tooltip />
      <el-table-column label="进度" min-width="180">
        <template #default="{ row }">
          <span>{{ row.completedCount || 0 }} / {{ row.itemCount || 0 }}</span>
          <el-progress :percentage="progress(row)" :stroke-width="6" :show-text="false" />
        </template>
      </el-table-column>
      <el-table-column label="差异 / 异常" width="120" align="center">
        <template #default="{ row }"><el-tag :type="Number(row.abnormalCount) ? 'warning' : 'success'">{{ Number(row.abnormalCount) ? `${row.abnormalCount} 项` : "无差异" }}</el-tag></template>
      </el-table-column>
      <el-table-column label="状态" width="105" align="center"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
      <el-table-column label="创建时间" prop="createTime" min-width="165" />
      <el-table-column label="操作" width="205" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openRow(row)">{{ isScan && Number(row.status) === RENTAL_CODE.STOCKTAKING_STATUS.PROCESSING ? "继续盘点" : "查看详情" }}</el-button>
          <el-button v-if="Number(row.status) === RENTAL_CODE.STOCKTAKING_STATUS.REVIEW_REQUIRED" link type="success" @click="reviewRow(row)">复核完成</el-button>
          <el-button v-if="Number(row.status) === RENTAL_CODE.STOCKTAKING_STATUS.PROCESSING || Number(row.status) === RENTAL_CODE.STOCKTAKING_STATUS.REVIEW_REQUIRED" link type="warning" @click="closeRow(row)">关闭</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="loadList" />

    <el-drawer v-model="manualDetailVisible" title="手工盘点详情" size="min(920px, 92vw)" destroy-on-close>
      <template v-if="manualDetail">
        <div class="detail-overview">
          <span><small>盘点单号</small>{{ manualDetail.stocktaking.stocktakingNo }}</span>
          <span><small>仓库</small>{{ manualDetail.stocktaking.warehouseName }}</span>
          <span><small>盘点日期</small>{{ dateText(manualDetail.stocktaking.stocktakingDate) }}</span>
          <span><small>状态</small><el-tag :type="statusType(manualDetail.stocktaking.status)">{{ statusText(manualDetail.stocktaking.status) }}</el-tag></span>
        </div>
        <el-alert title="账面可用数量为创建盘点时的快照；保存差异不会自动修改库存。" type="info" :closable="false" show-icon />
        <el-table v-loading="detailLoading" :data="manualDetail.items" border class="manual-items">
          <el-table-column label="产品" min-width="230"><template #default="{ row }"><strong>{{ row.productName || "未命名产品" }}</strong><span v-if="row.modelName" class="secondary-text">{{ row.modelName }}</span></template></el-table-column>
          <el-table-column label="账面可用" prop="bookQuantity" width="115" align="center" />
          <el-table-column label="实盘数量" width="185" align="center"><template #default="{ row }"><el-input-number v-if="Number(manualDetail.stocktaking.status) === RENTAL_CODE.STOCKTAKING_STATUS.PROCESSING" v-model="row.editCount" :min="0" :precision="0" controls-position="right" /><span v-else>{{ row.countedQuantity ?? "-" }}</span></template></el-table-column>
          <el-table-column label="差异" width="100" align="center"><template #default="{ row }"><el-tag v-if="row.differenceQuantity != null" :type="Number(row.differenceQuantity) === 0 ? 'success' : 'warning'">{{ Number(row.differenceQuantity) > 0 ? `+${row.differenceQuantity}` : row.differenceQuantity }}</el-tag><span v-else>-</span></template></el-table-column>
          <el-table-column label="操作" width="95" align="center"><template #default="{ row }"><el-button v-if="Number(manualDetail.stocktaking.status) === RENTAL_CODE.STOCKTAKING_STATUS.PROCESSING" link type="primary" :loading="savingItemId === row.itemId" @click="saveCount(row)">保存</el-button><span v-else>已记录</span></template></el-table-column>
        </el-table>
        <el-empty v-if="!detailLoading && !manualDetail.items.length" description="此仓库当前没有可盘点的库存" :image-size="70" />
        <div class="drawer-actions">
          <el-button v-if="Number(manualDetail.stocktaking.status) === RENTAL_CODE.STOCKTAKING_STATUS.PROCESSING" type="success" :disabled="manualDetail.items.some(item => item.countedQuantity == null)" @click="completeManual">完成盘点</el-button>
          <el-button v-if="Number(manualDetail.stocktaking.status) === RENTAL_CODE.STOCKTAKING_STATUS.REVIEW_REQUIRED" type="success" @click="reviewRow(manualDetail.stocktaking)">确认差异复核</el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="createVisible" :title="isScan ? '新建扫码盘点' : '新建手工盘点'" width="520px" append-to-body>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="盘点仓库" prop="warehouseId">
          <el-select v-model="createForm.warehouseId" filterable placeholder="请选择仓库" style="width: 100%" @change="handleCreateScopeChange">
            <el-option v-for="item in warehouses" :key="warehouseId(item)" :label="warehouseLabel(item)" :value="warehouseId(item)" />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点日期" prop="stocktakingDate"><el-date-picker v-model="createForm.stocktakingDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" /></el-form-item>
        <el-form-item label="盘点范围" prop="scope">
          <el-select v-model="createForm.scope" style="width: 100%" @change="handleCreateScopeChange">
            <el-option label="全仓可用库存" :value="RENTAL_CODE.STOCKTAKING_SCOPE.ALL" />
            <el-option label="指定商品分类" :value="RENTAL_CODE.STOCKTAKING_SCOPE.CLASS" />
            <el-option label="指定型号" :value="RENTAL_CODE.STOCKTAKING_SCOPE.MODEL" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="createForm.scope === RENTAL_CODE.STOCKTAKING_SCOPE.CLASS" label="商品分类" prop="classId">
          <el-select v-model="createForm.classId" filterable :loading="scopeLoading" placeholder="选择当前仓有库存的分类" style="width: 100%">
            <el-option v-for="item in scopeOptions" :key="item.optionId" :label="item.optionLabel" :value="item.optionId" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="createForm.scope === RENTAL_CODE.STOCKTAKING_SCOPE.MODEL" label="商品型号" prop="modelId">
          <el-select v-model="createForm.modelId" filterable :loading="scopeLoading" placeholder="选择当前仓有库存的型号" style="width: 100%">
            <el-option v-for="item in scopeOptions" :key="item.optionId" :label="item.optionLabel" :value="item.optionId" />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点说明"><el-input v-model="createForm.remark" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <el-alert :title="isScan ? '创建后可连续扫描设备；仅记录核对结果，不直接变更库存。' : '创建时记录当前仓库的账面可用数量快照，后续录入实盘数量。'" type="info" :closable="false" />
      <template #footer><el-button @click="createVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="submitCreate">创建盘点</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, Refresh, Search } from "@element-plus/icons-vue";
import { listMerchantWarehouses } from "@/api/merchant/rentalBusiness";
import { RENTAL_CODE } from "@/utils/rental/options";
import {
  closeMerchantStocktaking,
  completeMerchantStocktaking,
  countMerchantStocktakingItem,
  createMerchantStocktaking,
  getMerchantStocktaking,
  getMerchantStocktakingSummary,
  listMerchantStocktakingScopeOptions,
  listMerchantStocktakings,
  reviewMerchantStocktaking,
  scanMerchantStocktaking
} from "@/api/merchant/stocktaking";

const route = useRoute();
const isScan = computed(() => route.path.endsWith("/scan"));
const loading = ref(false);
const detailLoading = ref(false);
const submitting = ref(false);
const scanLoading = ref(false);
const scopeLoading = ref(false);
const showSearch = ref(true);
const savingItemId = ref(null);
const total = ref(0);
const rows = ref([]);
const warehouses = ref([]);
const scopeOptions = ref([]);
const summary = ref({});
const activeScan = ref(null);
const activeItems = ref([]);
const scanPaused = ref(false);
const duplicateScanCount = ref(0);
const latestScan = ref(null);
const manualDetail = ref(null);
const manualDetailVisible = ref(false);
const createVisible = ref(false);
const dateRange = ref([]);
const createFormRef = ref();
const scanInputRef = ref();
const query = reactive({ pageNum: 1, pageSize: 10, warehouseId: undefined, status: undefined, beginDate: undefined, endDate: undefined, keyword: "" });
const scanForm = reactive({ code: "" });
const createForm = reactive({ warehouseId: undefined, stocktakingDate: localDate(), scope: RENTAL_CODE.STOCKTAKING_SCOPE.ALL, classId: undefined, modelId: undefined, remark: "" });
const normalScanCount = computed(() => activeItems.value.filter(item => Number(item.scanResult) === RENTAL_CODE.STOCKTAKING_SCAN_RESULT.NORMAL).length);
const abnormalScanCount = computed(() => activeItems.value.filter(item => Number(item.scanResult) === RENTAL_CODE.STOCKTAKING_SCAN_RESULT.ABNORMAL).length);
const createRules = computed(() => ({
  warehouseId: [{ required: true, message: "请选择盘点仓库", trigger: "change" }],
  stocktakingDate: [{ required: true, message: "请选择盘点日期", trigger: "change" }],
  classId: createForm.scope === RENTAL_CODE.STOCKTAKING_SCOPE.CLASS ? [{ required: true, message: "请选择商品分类", trigger: "change" }] : [],
  modelId: createForm.scope === RENTAL_CODE.STOCKTAKING_SCOPE.MODEL ? [{ required: true, message: "请选择商品型号", trigger: "change" }] : []
}));

function unwrap(response) { return response?.data ?? response; }
function responseRows(response) { const data = unwrap(response); return data?.rows || data || []; }
function errorText(error) { return error?.msg || error?.message || "操作失败，请稍后重试"; }
function localDate() { const now = new Date(); return new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 10); }
function warehouseId(item) { return item?.warehouseId ?? item?.id; }
function warehouseLabel(item) { return [item?.warehouseName ?? item?.name, item?.warehouseCode ?? item?.code].filter(Boolean).join("（") + (item?.warehouseCode || item?.code ? "）" : ""); }
function dateText(value) { return value ? String(value).slice(0, 10) : "-"; }
function statusText(value) { return ({ [RENTAL_CODE.STOCKTAKING_STATUS.PROCESSING]: "盘点中", [RENTAL_CODE.STOCKTAKING_STATUS.COMPLETED]: "已完成", [RENTAL_CODE.STOCKTAKING_STATUS.CLOSED]: "已关闭", [RENTAL_CODE.STOCKTAKING_STATUS.REVIEW_REQUIRED]: "待复核" })[Number(value)] || "未知状态"; }
function statusType(value) { return ({ [RENTAL_CODE.STOCKTAKING_STATUS.PROCESSING]: "primary", [RENTAL_CODE.STOCKTAKING_STATUS.COMPLETED]: "success", [RENTAL_CODE.STOCKTAKING_STATUS.CLOSED]: "info", [RENTAL_CODE.STOCKTAKING_STATUS.REVIEW_REQUIRED]: "warning" })[Number(value)] || "info"; }
function progress(row) { const totalCount = Number(row.itemCount || 0); return totalCount ? Math.min(100, Math.round(Number(row.completedCount || 0) * 100 / totalCount)) : 0; }
function inventoryStatusText(value) { return ({ [RENTAL_CODE.INVENTORY_STATUS.AVAILABLE]: "可用", [RENTAL_CODE.INVENTORY_STATUS.RESERVED]: "已预占", [RENTAL_CODE.INVENTORY_STATUS.RENTED]: "租赁中", [RENTAL_CODE.INVENTORY_STATUS.REPAIRING]: "维修中", [RENTAL_CODE.INVENTORY_STATUS.LOST]: "遗失", [RENTAL_CODE.INVENTORY_STATUS.RETIRED]: "已报废" })[Number(value)] || "未识别"; }
function searchMode() { return isScan.value ? RENTAL_CODE.STOCKTAKING_MODE.SCAN : RENTAL_CODE.STOCKTAKING_MODE.MANUAL; }

async function loadWarehouses() {
  try {
    warehouses.value = responseRows(await listMerchantWarehouses({ pageNum: 1, pageSize: 1000, status: 0 }));
  } catch (error) {
    ElMessage.error(errorText(error));
  }
}
async function loadSummary() {
  try { summary.value = unwrap(await getMerchantStocktakingSummary({ warehouseId: query.warehouseId })) || {}; }
  catch (error) { ElMessage.error(errorText(error)); }
}
async function loadList() {
  loading.value = true;
  try {
    const response = await listMerchantStocktakings({ ...query, mode: searchMode() });
    const data = unwrap(response);
    rows.value = data?.rows || [];
    total.value = Number(data?.total || 0);
  } catch (error) {
    ElMessage.error(errorText(error));
  } finally { loading.value = false; }
}
async function loadData() {
  query.pageNum = 1;
  await Promise.all([loadList(), loadSummary()]);
}
function resetQuery() {
  Object.assign(query, { pageNum: 1, pageSize: 10, warehouseId: undefined, status: undefined, beginDate: undefined, endDate: undefined, keyword: "" });
  dateRange.value = [];
  loadData();
}
function handleDateRangeChange(value) {
  query.beginDate = value?.[0];
  query.endDate = value?.[1];
  loadData();
}
function openCreate() {
  scopeOptions.value = [];
  Object.assign(createForm, { warehouseId: warehouses.value.length ? warehouseId(warehouses.value[0]) : undefined, stocktakingDate: localDate(), scope: RENTAL_CODE.STOCKTAKING_SCOPE.ALL, classId: undefined, modelId: undefined, remark: "" });
  createVisible.value = true;
}
async function handleCreateScopeChange() {
  createForm.classId = undefined;
  createForm.modelId = undefined;
  await loadScopeOptions();
}
async function loadScopeOptions() {
  if (!createForm.warehouseId || createForm.scope === RENTAL_CODE.STOCKTAKING_SCOPE.ALL) {
    scopeOptions.value = [];
    return;
  }
  scopeLoading.value = true;
  try {
    scopeOptions.value = responseRows(await listMerchantStocktakingScopeOptions({ warehouseId: createForm.warehouseId, scope: createForm.scope }));
  } catch (error) {
    scopeOptions.value = [];
    ElMessage.error(errorText(error));
  } finally { scopeLoading.value = false; }
}
async function submitCreate() {
  if (!createFormRef.value) return;
  const valid = await createFormRef.value.validate().catch(() => false);
  if (!valid) return;
  submitting.value = true;
  try {
    const result = unwrap(await createMerchantStocktaking({ ...createForm, mode: searchMode() }));
    createVisible.value = false;
    ElMessage.success("盘点单已创建");
    await loadData();
    if (isScan.value && result?.stocktakingId) await openScanSession({ stocktakingId: result.stocktakingId });
  } catch (error) { ElMessage.error(errorText(error)); }
  finally { submitting.value = false; }
}
async function openRow(row) {
  if (isScan.value) return openScanSession(row);
  detailLoading.value = true;
  manualDetailVisible.value = true;
  try {
    manualDetail.value = unwrap(await getMerchantStocktaking(row.stocktakingId));
    manualDetail.value.items = (manualDetail.value.items || []).map(item => ({ ...item, editCount: item.countedQuantity ?? 0 }));
  } catch (error) { ElMessage.error(errorText(error)); }
  finally { detailLoading.value = false; }
}
async function openScanSession(row) {
  detailLoading.value = true;
  latestScan.value = null;
  scanPaused.value = false;
  duplicateScanCount.value = 0;
  scanForm.code = "";
  try {
    const data = unwrap(await getMerchantStocktaking(row.stocktakingId));
    activeScan.value = data.stocktaking;
    activeItems.value = data.items || [];
    await nextTick();
    scanInputRef.value?.focus?.();
  } catch (error) { ElMessage.error(errorText(error)); }
  finally { detailLoading.value = false; }
}
async function saveCount(row) {
  savingItemId.value = row.itemId;
  try {
    await countMerchantStocktakingItem(manualDetail.value.stocktaking.stocktakingId, row.itemId, row.editCount);
    await openRow({ stocktakingId: manualDetail.value.stocktaking.stocktakingId });
    await loadData();
    ElMessage.success("实盘数量已保存");
  } catch (error) { ElMessage.error(errorText(error)); }
  finally { savingItemId.value = null; }
}
async function submitScan() {
  if (!activeScan.value || scanPaused.value || !scanForm.code.trim() || scanLoading.value) return;
  scanLoading.value = true;
  try {
    latestScan.value = unwrap(await scanMerchantStocktaking(activeScan.value.stocktakingId, scanForm.code.trim()));
    if (latestScan.value?.duplicate) duplicateScanCount.value += 1;
    scanForm.code = "";
    await reloadActiveScan();
    await nextTick();
    scanInputRef.value?.focus?.();
  } catch (error) { ElMessage.error(errorText(error)); }
  finally { scanLoading.value = false; }
}
async function reloadActiveScan() {
  if (!activeScan.value) return;
  const data = unwrap(await getMerchantStocktaking(activeScan.value.stocktakingId));
  activeScan.value = data.stocktaking;
  activeItems.value = data.items || [];
  await loadData();
}
async function completeManual() {
  try {
    await ElMessageBox.confirm("完成后将锁定盘点结果，不会自动调整库存。", "完成盘点", { type: "warning" });
    await completeMerchantStocktaking(manualDetail.value.stocktaking.stocktakingId);
    manualDetailVisible.value = false;
    ElMessage.success("盘点已完成");
    await loadData();
  } catch (error) { if (error !== "cancel" && error !== "close") ElMessage.error(errorText(error)); }
}
async function completeScan() {
  try {
    await ElMessageBox.confirm("结束后将归档扫码结果；异常项会保留，不会自动调整库存。", "结束盘点", { type: "warning" });
    await completeMerchantStocktaking(activeScan.value.stocktakingId);
    await reloadActiveScan();
    ElMessage.success("扫码盘点已结束");
  } catch (error) { if (error !== "cancel" && error !== "close") ElMessage.error(errorText(error)); }
}
async function closeRow(row) {
  try {
    await ElMessageBox.confirm("关闭后不能继续录入本次盘点数据。", "关闭盘点单", { type: "warning" });
    await closeMerchantStocktaking(row.stocktakingId);
    ElMessage.success("盘点单已关闭");
    await loadData();
  } catch (error) { if (error !== "cancel" && error !== "close") ElMessage.error(errorText(error)); }
}
async function reviewRow(row) {
  try {
    await ElMessageBox.confirm("确认已核对本次盘点差异？确认后将归档盘点结果，不会自动调整库存。", "差异复核", { type: "warning" });
    await reviewMerchantStocktaking(row.stocktakingId);
    manualDetailVisible.value = false;
    ElMessage.success("差异已复核，盘点结果已归档");
    await loadData();
  } catch (error) { if (error !== "cancel" && error !== "close") ElMessage.error(errorText(error)); }
}
watch(() => route.path, () => { activeScan.value = null; loadData(); });
onMounted(async () => { await loadWarehouses(); await loadData(); });
</script>

<style scoped>
.page-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.page-heading h2 { margin: 0; font-size: 20px; font-weight: 600; }
.page-heading p { margin: 6px 0 0; color: var(--el-text-color-secondary); }
.summary-row { margin-bottom: 16px; }
.summary-item { display: flex; min-height: 76px; align-items: center; justify-content: space-between; padding: 12px 16px; border: 1px solid var(--el-border-color-light); border-radius: 4px; background: var(--el-bg-color); }
.summary-item span { color: var(--el-text-color-secondary); }
.summary-item strong { color: var(--el-color-primary); font-size: 22px; font-weight: 600; }
.summary-item.warning strong { color: var(--el-color-warning); }
.summary-item.success strong { color: var(--el-color-success); }
.search-form { margin-bottom: 12px; }
.scan-workspace { margin-bottom: 18px; }
.workspace-heading,.workspace-actions { display: flex; align-items: center; gap: 12px; }
.workspace-heading { justify-content: space-between; }
.workspace-heading strong { margin-right: 14px; }
.workspace-heading span { color: var(--el-text-color-secondary); }
.scan-stats { margin-bottom: 15px; padding: 12px 0; border-bottom: 1px solid var(--el-border-color-lighter); }
.scan-stats > div { display: flex; align-items: center; justify-content: space-between; padding: 4px 14px; }
.scan-stats > div + div { border-left: 1px solid var(--el-border-color-lighter); }
.scan-stats span { color: var(--el-text-color-secondary); }
.scan-stats strong { font-size: 20px; }
.text-warning { color: var(--el-color-warning); }
.scan-form-item { max-width: 760px; }
.scan-results { margin-top: 14px; }
.workspace-note,.secondary-text { display: block; margin-top: 4px; color: var(--el-text-color-secondary); font-size: 12px; }
.detail-overview { display: grid; grid-template-columns: repeat(4,minmax(0,1fr)); gap: 12px; margin-bottom: 18px; }
.detail-overview > span { display: flex; min-width: 0; flex-direction: column; gap: 5px; }
.detail-overview small { color: var(--el-text-color-secondary); }
.manual-items { margin-top: 16px; }
.drawer-actions { display: flex; justify-content: flex-end; margin-top: 18px; }
@media (max-width: 720px) { .page-heading { flex-direction: column; } .workspace-heading { align-items: flex-start; flex-direction: column; } .detail-overview { grid-template-columns: repeat(2,minmax(0,1fr)); } .scan-stats > div { padding: 4px 2px; } }
</style>
