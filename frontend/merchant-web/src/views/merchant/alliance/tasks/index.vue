<template>
  <div class="app-container alliance-task-page">
    <div class="page-heading">
      <div>
        <h2>{{ isPublicPool ? "联盟公众池" : "定向订单" }}</h2>
        <p>{{ isPublicPool ? "查看当前有效联盟内公开发布、可由本商户承接的订单" : "处理联盟成员指定给本商户的协助订单" }}</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="refresh">刷新</el-button>
    </div>

    <el-alert v-if="allianceLoaded && !alliance" title="当前商户尚未加入有效租商联盟" type="warning" :closable="false" show-icon class="alliance-alert" />

    <div class="summary-grid">
      <div class="summary-item"><span>{{ isPublicPool ? "可承接订单" : "待我处理" }}</span><strong class="warning-value">{{ isPublicPool ? stats.publicPoolCount || 0 : stats.pendingCount || 0 }}</strong></div>
      <div class="summary-item"><span>{{ isPublicPool ? "今日新增" : "履约中" }}</span><strong>{{ isPublicPool ? stats.todayNewCount || 0 : stats.fulfillmentCount || 0 }}</strong></div>
      <div class="summary-item"><span>{{ isPublicPool ? "我的承接中" : "已完成" }}</span><strong class="success-value">{{ isPublicPool ? stats.myAcceptedCount || 0 : stats.completedCount || 0 }}</strong></div>
    </div>

    <div v-if="!isPublicPool" class="status-tabs">
      <button v-for="tab in directTabs" :key="tab.label" type="button" :class="{ active: query.status === tab.value }" @click="changeStatus(tab.value)">
        {{ tab.label }}<span v-if="tab.value === RENTAL_CODE.ALLIANCE_ORDER_STATUS.PENDING && stats.pendingCount">{{ stats.pendingCount }}</span>
      </button>
    </div>

    <el-form :inline="true" :model="query" class="search-form" @submit.prevent>
      <el-form-item :label="isPublicPool ? '套餐 / 型号' : '订单编码 / 套餐'">
        <el-input v-model="query.keyword" clearable :placeholder="isPublicPool ? '输入套餐名、型号或订单编码' : '输入订单编码、套餐名或型号'" @keyup.enter="search" />
      </el-form-item>
      <el-form-item v-if="isPublicPool" label="履约城市">
        <el-input v-model="query.deliveryCity" clearable placeholder="输入配送城市" @keyup.enter="search" />
      </el-form-item>
      <el-form-item v-if="isPublicPool" label="租期开始">
        <el-date-picker v-model="query.leaseStartDate" type="date" value-format="YYYY-MM-DD" placeholder="选择起租日期" />
      </el-form-item>
      <el-form-item><el-button type="primary" :icon="Search" @click="search">查询</el-button><el-button :icon="Refresh" @click="reset">重置</el-button></el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border row-key="allianceOrderId">
      <el-table-column label="订单编码" prop="orderNo" min-width="165" />
      <el-table-column label="发起商户" prop="merchantName" min-width="145" show-overflow-tooltip />
      <el-table-column label="套餐 / 型号" min-width="205" show-overflow-tooltip>
        <template #default="{ row }">
          <strong>{{ row.packageName || snapshot(row).productName || snapshot(row).modelName || "协助订单" }}</strong>
          <span v-if="snapshot(row).modelName" class="secondary-text">{{ snapshot(row).modelName }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="isPublicPool" label="配送城市" min-width="120">
        <template #default="{ row }">{{ snapshot(row).deliveryCity || "未提供" }}</template>
      </el-table-column>
      <el-table-column label="租期" min-width="190"><template #default="{ row }">{{ leaseText(row) }}</template></el-table-column>
      <el-table-column label="结算价" width="125" align="right"><template #default="{ row }">{{ money(row.settlementPrice) }}</template></el-table-column>
      <el-table-column label="订单创建时间" prop="orderCreateTime" min-width="165" />
      <el-table-column label="转单时间" prop="transferTime" min-width="165" />
      <el-table-column v-if="!isPublicPool" label="收货区域" min-width="130"><template #default="{ row }">{{ snapshot(row).deliveryCity || "已脱敏" }}</template></el-table-column>
      <el-table-column v-if="!isPublicPool" label="状态" width="105" align="center"><template #default="{ row }"><el-tag :type="statusType(row.allianceOrderStatus)">{{ statusText(row.allianceOrderStatus) }}</el-tag></template></el-table-column>
      <el-table-column v-else label="发起方" width="145"><template #default="{ row }">{{ row.merchantName || "联盟商户" }}</template></el-table-column>
      <el-table-column label="操作" width="145" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="openDetail(row)">详情</el-button>
          <el-button v-if="canAccept(row)" link type="success" @click="openAccept(row)">{{ isPublicPool ? "承接" : "接受" }}</el-button>
          <el-button v-if="!isPublicPool && Number(row.allianceOrderStatus) === RENTAL_CODE.ALLIANCE_ORDER_STATUS.PENDING" link type="danger" @click="reject(row)">拒绝</el-button>
        </template>
      </el-table-column>
      <template #empty><el-empty :description="isPublicPool ? '当前没有可承接的联盟订单' : '暂无发给本商户的定向订单'" :image-size="86" /></template>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="loadTasks" />

    <el-dialog v-model="acceptVisible" :title="isPublicPool ? '承接联盟订单' : '接受协助订单'" width="500px" append-to-body>
      <template v-if="selectedTask">
        <div class="accept-summary">
          <strong>{{ selectedTask.orderNo }}</strong>
          <span>{{ selectedTask.packageName || snapshot(selectedTask).productName || "协助订单" }}</span>
          <span>{{ leaseText(selectedTask) }} · {{ money(selectedTask.settlementPrice) }}</span>
        </div>
        <el-form label-width="105px">
          <el-form-item label="履约仓库" required>
            <el-select v-model="acceptForm.warehouseId" filterable placeholder="请选择本商户履约仓库" style="width: 100%">
              <el-option v-for="item in warehouses" :key="warehouseId(item)" :label="warehouseLabel(item)" :value="warehouseId(item)" />
            </el-select>
          </el-form-item>
        </el-form>
        <el-alert title="确认后会在本商户创建子订单并按所选仓库校验套餐履约库存；客户资料继续按履约权限控制。" type="info" :closable="false" />
      </template>
      <template #footer><el-button @click="acceptVisible = false">取消</el-button><el-button type="primary" :loading="actionLoading" @click="confirmAccept">确认承接</el-button></template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="协助订单详情" size="min(600px, 94vw)" destroy-on-close>
      <template v-if="selectedTask">
        <el-descriptions :column="1" border label-width="120">
          <el-descriptions-item label="订单编码">{{ selectedTask.orderNo || "-" }}</el-descriptions-item>
          <el-descriptions-item label="发起商户">{{ selectedTask.merchantName || "联盟商户" }}</el-descriptions-item>
          <el-descriptions-item label="套餐">{{ selectedTask.packageName || snapshot(selectedTask).productName || "-" }}</el-descriptions-item>
          <el-descriptions-item label="主型号">{{ snapshot(selectedTask).modelName || "-" }}</el-descriptions-item>
          <el-descriptions-item label="租期">{{ leaseText(selectedTask) }}</el-descriptions-item>
          <el-descriptions-item label="结算价">{{ money(selectedTask.settlementPrice) }}</el-descriptions-item>
          <el-descriptions-item label="订单创建时间">{{ selectedTask.orderCreateTime || "-" }}</el-descriptions-item>
          <el-descriptions-item label="转单时间">{{ selectedTask.transferTime || "-" }}</el-descriptions-item>
          <el-descriptions-item label="配送城市">{{ snapshot(selectedTask).deliveryCity || "已脱敏" }}</el-descriptions-item>
          <el-descriptions-item v-if="!isPublicPool" label="状态"><el-tag :type="statusType(selectedTask.allianceOrderStatus)">{{ statusText(selectedTask.allianceOrderStatus) }}</el-tag></el-descriptions-item>
        </el-descriptions>
        <el-alert class="detail-note" title="承接前不展示客户姓名、电话和详细地址；接受后客户资料仅在允许的履约环节查看。" type="info" :closable="false" />
      </template>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button><el-button v-if="selectedTask && canAccept(selectedTask)" type="primary" @click="detailVisible = false; openAccept(selectedTask)">承接订单</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Refresh, Search } from "@element-plus/icons-vue";
import { acceptAllianceTask, getAlliance, getAllianceTaskSummary, listAllianceTasks, rejectAllianceTask } from "@/api/merchant/alliance";
import { listMerchantWarehouses } from "@/api/merchant/rentalBusiness";
import { RENTAL_CODE } from "@/utils/rental/options";

const route = useRoute();
const isPublicPool = computed(() => route.path.endsWith("/public-pool"));
const loading = ref(false);
const actionLoading = ref(false);
const total = ref(0);
const rows = ref([]);
const warehouses = ref([]);
const alliance = ref(null);
const allianceLoaded = ref(false);
const stats = ref({});
const selectedTask = ref(null);
const acceptVisible = ref(false);
const detailVisible = ref(false);
const query = reactive({ pageNum: 1, pageSize: 10, status: undefined, keyword: "", deliveryCity: "", leaseStartDate: undefined });
const acceptForm = reactive({ warehouseId: undefined });
const directTabs = [
  { label: "全部", value: undefined },
  { label: "待处理", value: RENTAL_CODE.ALLIANCE_ORDER_STATUS.PENDING },
  { label: "履约中", value: RENTAL_CODE.ALLIANCE_ORDER_STATUS.IN_FULFILLMENT },
  { label: "已完成", value: RENTAL_CODE.ALLIANCE_ORDER_STATUS.COMPLETED }
];

function unwrap(response) { return response?.data ?? response; }
function errorText(error) { return error?.msg || error?.message || "操作失败，请稍后重试"; }
function warehouseId(item) { return item?.warehouseId ?? item?.id; }
function warehouseLabel(item) { const name = item?.warehouseName ?? item?.name ?? "仓库"; const code = item?.warehouseCode ?? item?.code; return code ? `${name}（${code}）` : name; }
function snapshot(row) {
  try { return JSON.parse(row?.maskedSnapshotJson || "{}"); }
  catch { return {}; }
}
function leaseText(row) { return row?.leaseStartDate && row?.leaseEndDate ? `${row.leaseStartDate} 至 ${row.leaseEndDate}` : "未设置"; }
function money(value) { return value === null || value === undefined || value === "" ? "-" : `¥ ${Number(value).toFixed(2)}`; }
function statusText(value) { return ({ [RENTAL_CODE.ALLIANCE_ORDER_STATUS.PENDING]: "待接受", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.ACCEPTED]: "已接受", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.REJECTED]: "已拒绝", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.CANCELLED]: "已取消", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.EXPIRED]: "已过期", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.PUBLIC_POOL]: "公众池中", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.IN_FULFILLMENT]: "履约中", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.COMPLETED]: "已完成", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.EXCEPTION]: "异常" })[Number(value)] || "未知状态"; }
function statusType(value) { return ({ [RENTAL_CODE.ALLIANCE_ORDER_STATUS.PENDING]: "warning", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.ACCEPTED]: "primary", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.REJECTED]: "danger", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.CANCELLED]: "info", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.EXPIRED]: "info", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.PUBLIC_POOL]: "warning", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.IN_FULFILLMENT]: "primary", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.COMPLETED]: "success", [RENTAL_CODE.ALLIANCE_ORDER_STATUS.EXCEPTION]: "danger" })[Number(value)] || "info"; }
function canAccept(row) { return isPublicPool.value ? Number(row.allianceOrderStatus) === RENTAL_CODE.ALLIANCE_ORDER_STATUS.PUBLIC_POOL : Number(row.allianceOrderStatus) === RENTAL_CODE.ALLIANCE_ORDER_STATUS.PENDING; }
async function loadAlliance() {
  try {
    const result = unwrap(await getAlliance());
    alliance.value = result?.alliance || null;
    allianceLoaded.value = true;
  } catch (error) { allianceLoaded.value = true; ElMessage.error(errorText(error)); }
}
async function loadWarehouses() {
  try {
    const result = unwrap(await listMerchantWarehouses({ pageNum: 1, pageSize: 1000, status: 0 }));
    warehouses.value = result?.rows || result || [];
  } catch (error) { ElMessage.error(errorText(error)); }
}
async function loadTasks() {
  loading.value = true;
  try {
    const params = { ...query, view: isPublicPool.value ? "public_pool" : "direct" };
    if (isPublicPool.value) params.status = RENTAL_CODE.ALLIANCE_ORDER_STATUS.PUBLIC_POOL;
    else {
      delete params.deliveryCity;
      delete params.leaseStartDate;
    }
    const result = unwrap(await listAllianceTasks(params));
    rows.value = result?.rows || [];
    total.value = Number(result?.total || 0);
  } catch (error) { ElMessage.error(errorText(error)); }
  finally { loading.value = false; }
}
async function loadSummary() {
  try { stats.value = unwrap(await getAllianceTaskSummary({ view: isPublicPool.value ? "public_pool" : "direct" })) || {}; }
  catch (error) { ElMessage.error(errorText(error)); }
}
async function refresh() { await Promise.all([loadTasks(), loadSummary()]); }
function search() { query.pageNum = 1; loadTasks(); }
function reset() { Object.assign(query, { pageNum: 1, pageSize: 10, status: undefined, keyword: "", deliveryCity: "", leaseStartDate: undefined }); loadTasks(); }
function changeStatus(status) { query.status = status; query.pageNum = 1; loadTasks(); }
function openDetail(row) { selectedTask.value = row; detailVisible.value = true; }
async function openAccept(row) {
  if (!warehouses.value.length) await loadWarehouses();
  selectedTask.value = row;
  acceptForm.warehouseId = undefined;
  acceptVisible.value = true;
}
async function confirmAccept() {
  if (!acceptForm.warehouseId) return ElMessage.warning("请选择履约仓库");
  actionLoading.value = true;
  try {
    await acceptAllianceTask(selectedTask.value.allianceOrderId, { warehouseId: acceptForm.warehouseId });
    acceptVisible.value = false;
    ElMessage.success("协助订单已承接");
    await refresh();
  } catch (error) { ElMessage.error(errorText(error)); }
  finally { actionLoading.value = false; }
}
async function reject(row) {
  try {
    await ElMessageBox.confirm(`确定拒绝订单 ${row.orderNo || ""} 吗？`, "拒绝协助订单", { type: "warning" });
    await rejectAllianceTask(row.allianceOrderId);
    ElMessage.success("订单已拒绝");
    await refresh();
  } catch (error) { if (error !== "cancel" && error !== "close") ElMessage.error(errorText(error)); }
}
watch(() => route.path, () => { query.pageNum = 1; query.status = undefined; selectedTask.value = null; refresh(); });
onMounted(async () => { await Promise.all([loadAlliance(), loadWarehouses()]); await refresh(); });
</script>

<style scoped>
.page-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.page-heading h2 { margin: 0; font-size: 20px; font-weight: 600; }
.page-heading p { margin: 6px 0 0; color: var(--el-text-color-secondary); }
.alliance-alert { margin-bottom: 16px; }
.summary-grid { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 12px; margin-bottom: 18px; }
.summary-item { display: flex; min-height: 76px; align-items: center; justify-content: space-between; padding: 12px 16px; border: 1px solid var(--el-border-color-light); border-radius: 4px; background: var(--el-bg-color); }
.summary-item span { color: var(--el-text-color-secondary); }
.summary-item strong { color: var(--el-color-primary); font-size: 22px; }
.summary-item .warning-value { color: var(--el-color-warning); }
.summary-item .success-value { color: var(--el-color-success); }
.status-tabs { display: flex; gap: 20px; margin-bottom: 14px; border-bottom: 1px solid var(--el-border-color-light); }
.status-tabs button { position: relative; padding: 10px 1px; border: 0; background: none; color: var(--el-text-color-secondary); cursor: pointer; }
.status-tabs button.active { color: var(--el-color-primary); }
.status-tabs button.active::after { position: absolute; right: 0; bottom: -1px; left: 0; height: 2px; background: var(--el-color-primary); content: ""; }
.status-tabs span { margin-left: 5px; color: var(--el-color-warning); }
.search-form { margin-bottom: 12px; }
.secondary-text { display: block; margin-top: 3px; color: var(--el-text-color-secondary); font-size: 12px; font-weight: 400; }
.accept-summary { display: flex; flex-direction: column; gap: 6px; margin-bottom: 20px; padding: 12px 14px; border: 1px solid var(--el-border-color-light); border-radius: 4px; }
.accept-summary span { color: var(--el-text-color-secondary); }
.detail-note { margin-top: 16px; }
@media (max-width: 720px) { .page-heading { flex-direction: column; } .summary-grid { grid-template-columns: 1fr; } }
</style>
