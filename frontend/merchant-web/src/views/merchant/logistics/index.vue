<template>
  <div class="app-container logistics-governance-page">
    <PageHeading
      kicker="LOGISTICS GOVERNANCE"
      title="物流治理"
      description="统一查看物流状态、异常信息和供应商查询快照">
      <template #aside>
        <div class="heading-actions">
          <div class="heading-summary">
            <span>当前页</span>
            <strong>{{ rows.length }}</strong>
            <span>条物流记录</span>
          </div>
          <el-button :icon="Refresh" :loading="loading" @click="loadList">
            刷新
          </el-button>
        </div>
      </template>
    </PageHeading>

    <el-row :gutter="12" class="summary-grid">
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-card__label">物流记录</div>
          <el-statistic :value="rows.length" />
          <div class="summary-card__note">当前页记录数</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card summary-card--primary">
          <div class="summary-card__label">运输中</div>
          <el-statistic :value="inTransitCount" />
          <div class="summary-card__note">需要持续关注的物流</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card summary-card--danger">
          <div class="summary-card__label">异常记录</div>
          <el-statistic :value="abnormalCount" />
          <div class="summary-card__note">包含异常状态或异常标记</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card summary-card--warning">
          <div class="summary-card__label">待查询</div>
          <el-statistic :value="pendingQueryCount" />
          <div class="summary-card__note">尚未取得供应商快照</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="filter-card">
      <div class="filter-card__header">
        <div>
          <h3>筛选条件</h3>
          <span class="filter-card__subtitle">按物流方向、状态和异常记录快速定位</span>
        </div>
        <el-tag effect="plain">已加载 {{ rows.length }} 条</el-tag>
      </div>
      <el-form :model="queryParams" :inline="true" @submit.prevent>
        <el-form-item label="订单或单号">
          <el-input
            v-model.trim="queryParams.keyword"
            clearable
            placeholder="订单编码、快递公司或单号"
            class="keyword-input"
            @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="物流方向">
          <el-select
            v-model="queryParams.direction"
            clearable
            placeholder="全部方向"
            class="select-input">
            <el-option
              v-for="item in directionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流状态">
          <el-select
            v-model="queryParams.routeStatus"
            clearable
            placeholder="全部状态"
            class="select-input">
            <el-option
              v-for="item in routeStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="queryParams.abnormalOnly">仅看异常</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">
            查询
          </el-button>
          <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-card__header">
        <div>
          <h3>物流治理台账</h3>
          <span class="table-card__subtitle">查看承运商、物流单号、查询状态和异常说明</span>
        </div>
        <div class="table-card__tools">
          <span class="table-total">共 {{ total }} 条</span>
          <el-button text :icon="Refresh" :loading="loading" @click="loadList">
            刷新
          </el-button>
        </div>
      </div>
      <el-table
        v-loading="loading"
        :data="rows"
        border
        stripe
        :row-key="rowKey">
        <el-table-column
          label="订单编码"
          prop="orderNo"
          min-width="170"
          show-overflow-tooltip />
        <el-table-column label="方向" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="directionType(row.direction)" effect="plain">
              {{ directionLabel(row.directionName || row.direction) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="快递公司"
          prop="carrier"
          min-width="130"
          show-overflow-tooltip>
          <template #default="{ row }">
            {{ getExpressCompanyName(row.carrier) || "-" }}
          </template>
        </el-table-column>
        <el-table-column
          label="快递单号"
          prop="trackingNo"
          min-width="160"
          show-overflow-tooltip />
        <el-table-column
          label="关联仓库"
          prop="warehouseName"
          min-width="130"
          show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.warehouseName || "未设置" }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row)" effect="plain">
              {{ row.routeStatusName || "待处理" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近节点" min-width="170">
          <template #default="{ row }">
            {{ row.lastRouteTime || "暂无轨迹" }}
          </template>
        </el-table-column>
        <el-table-column label="最近查询" min-width="170">
          <template #default="{ row }">
            {{ row.routeQueryTime || "尚未查询" }}
          </template>
        </el-table-column>
        <el-table-column label="异常说明" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.routeMessage || "-" }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="openTrack(row)">
              轨迹
            </el-button>
            <el-button
              link
              type="warning"
              :icon="Refresh"
              :loading="refreshingKey === refreshKey(row)"
              @click="refreshRow(row)">
              刷新
            </el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无物流记录" /></template>
      </el-table>
      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="loadList" />
    </el-card>

    <el-drawer
      v-model="trackVisible"
      title="物流轨迹"
      size="620px"
      destroy-on-close>
      <template v-if="track">
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="订单编码">
            {{ track.orderNo || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="物流方向">
            {{ directionLabel(track.direction) }}
          </el-descriptions-item>
          <el-descriptions-item label="快递公司">
            {{ getExpressCompanyName(track.carrier) || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="快递单号">
            {{ track.trackingNo || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag
              :type="track.available ? 'success' : 'warning'"
              effect="plain">
              {{ track.status || "待处理" }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="查询结果">
            {{ track.message || "-" }}
          </el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">路线节点</el-divider>
        <el-timeline v-if="track.events?.length">
          <el-timeline-item
            v-for="(item, index) in track.events"
            :key="`${item.time || 'event'}-${index}`"
            :timestamp="item.time"
            placement="top">
            <strong>{{ item.status || "运输节点" }}</strong>
            <p>{{ item.description || "-" }}</p>
            <small v-if="item.location">{{ item.location }}</small>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无路线节点" />
      </template>
      <el-skeleton v-else :rows="8" animated />
      <template #footer>
        <el-button @click="trackVisible = false">关闭</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="MerchantLogisticsGovernance">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { Refresh, RefreshLeft, Search, View } from "@element-plus/icons-vue";
import PageHeading from "@/components/PageHeading.vue";
import {
  getMerchantLogisticsTrack,
  listMerchantLogistics,
  refreshMerchantLogistics,
} from "@/api/merchant/logistics";
import { getExpressCompanyName } from "@/utils/rental/expressCompany";

const DIRECTION = Object.freeze({ SHIP: 1, RETURN: 2, TRANSFER: 3 });
const ROUTE_STATUS = Object.freeze({
  PENDING: 0,
  IN_TRANSIT: 1,
  DELIVERED: 2,
  ABNORMAL: 3,
  CLOSED: 4,
});
const directionOptions = [
  { value: DIRECTION.SHIP, label: "发货" },
  { value: DIRECTION.RETURN, label: "归还" },
  { value: DIRECTION.TRANSFER, label: "调货" },
];
const routeStatusOptions = [
  { value: ROUTE_STATUS.PENDING, label: "待处理" },
  { value: ROUTE_STATUS.IN_TRANSIT, label: "运输中" },
  { value: ROUTE_STATUS.DELIVERED, label: "已签收" },
  { value: ROUTE_STATUS.ABNORMAL, label: "异常" },
  { value: ROUTE_STATUS.CLOSED, label: "已关闭" },
];
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const refreshingKey = ref("");
const trackVisible = ref(false);
const track = ref(null);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  direction: undefined,
  routeStatus: undefined,
  abnormalOnly: false,
});
const inTransitCount = computed(
  () =>
    rows.value.filter(
      (row) => Number(row.routeStatus) === ROUTE_STATUS.IN_TRANSIT,
    ).length,
);
const abnormalCount = computed(
  () =>
    rows.value.filter(
      (row) =>
        Number(row.abnormalFlag) === 1 ||
        Number(row.routeStatus) === ROUTE_STATUS.ABNORMAL,
    ).length,
);
const pendingQueryCount = computed(
  () => rows.value.filter((row) => !row.routeQueryTime).length,
);

function unwrap(response) {
  return response?.data ?? response;
}
function pageData(response) {
  const data = unwrap(response);
  return {
    rows: Array.isArray(data) ? data : data?.rows || [],
    total: Number(data?.total || 0),
  };
}
function errorText(error) {
  return error?.msg || error?.message || "操作失败，请稍后重试";
}
function directionLabel(value) {
  const option = directionOptions.find(
    (item) => String(item.value) === String(value),
  );
  return option?.label || (typeof value === "string" && value ? value : "-");
}
function directionType(value) {
  return { 1: "primary", 2: "success", 3: "warning" }[Number(value)] || "info";
}
function statusType(row) {
  if (
    Number(row?.abnormalFlag) === 1 ||
    Number(row?.routeStatus) === ROUTE_STATUS.ABNORMAL
  )
    return "danger";
  return (
    { 0: "info", 1: "primary", 2: "success", 3: "danger", 4: "info" }[
      Number(row?.routeStatus)
    ] || "info"
  );
}
function refreshKey(row) {
  return `${row.orderNo || ""}:${row.direction || ""}`;
}
function rowKey(row) {
  return row.logisticsId || refreshKey(row);
}
function loadList() {
  loading.value = true;
  listMerchantLogistics(queryParams)
    .then((response) => {
      const page = pageData(response);
      rows.value = page.rows;
      total.value = page.total;
    })
    .catch((error) => ElMessage.error(errorText(error)))
    .finally(() => {
      loading.value = false;
    });
}
function handleQuery() {
  queryParams.pageNum = 1;
  loadList();
}
function resetQuery() {
  Object.assign(queryParams, {
    keyword: "",
    direction: undefined,
    routeStatus: undefined,
    abnormalOnly: false,
    pageNum: 1,
  });
  loadList();
}
async function openTrack(row) {
  trackVisible.value = true;
  track.value = null;
  try {
    track.value = unwrap(
      await getMerchantLogisticsTrack(row.orderNo, row.direction),
    );
  } catch (error) {
    trackVisible.value = false;
    ElMessage.error(errorText(error));
  }
}
async function refreshRow(row) {
  const key = refreshKey(row);
  refreshingKey.value = key;
  try {
    track.value = unwrap(
      await refreshMerchantLogistics(row.orderNo, row.direction),
    );
    trackVisible.value = true;
    ElMessage.success("物流轨迹已刷新");
    loadList();
  } catch (error) {
    ElMessage.error(errorText(error));
  } finally {
    refreshingKey.value = "";
  }
}
onMounted(loadList);
</script>

<style scoped>
.logistics-governance-page {
  min-height: 100%;
}
.heading-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.heading-summary {
  display: flex;
  align-items: baseline;
  gap: 6px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
.heading-summary strong {
  color: var(--el-color-primary);
  font-size: 24px;
  line-height: 1;
}
.summary-grid {
  margin-bottom: 16px;
}
.summary-card {
  min-height: 126px;
}
.summary-card :deep(.el-card__body) {
  padding: 16px 18px;
}
.summary-card__label {
  color: var(--el-text-color-regular);
  font-size: 14px;
  font-weight: 600;
}
.summary-card :deep(.el-statistic) {
  margin-top: 8px;
}
.summary-card :deep(.el-statistic__content) {
  color: var(--el-text-color-primary);
  font-size: 28px;
  line-height: 1.15;
}
.summary-card--primary :deep(.el-statistic__content) {
  color: var(--el-color-primary);
}
.summary-card--danger :deep(.el-statistic__content) {
  color: var(--el-color-danger);
}
.summary-card--warning :deep(.el-statistic__content) {
  color: var(--el-color-warning);
}
.summary-card__note {
  margin-top: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.filter-card {
  margin-bottom: 16px;
}
.filter-card__header,
.table-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.filter-card__header {
  margin-bottom: 16px;
}
.filter-card__header h3,
.table-card__header h3 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 600;
}
.filter-card__subtitle,
.table-card__subtitle {
  display: block;
  margin-top: 5px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}
.keyword-input {
  width: 250px;
}
.select-input {
  width: 130px;
}
.table-card {
  overflow: hidden;
}
.table-card__header {
  padding: 2px 0 16px;
}
.table-card__tools {
  display: flex;
  align-items: center;
  gap: 12px;
}
.table-card__tools span.table-total {
  display: inline;
  margin: 0;
  white-space: nowrap;
}
.table-card :deep(.el-table) {
  width: 100%;
}
.table-card :deep(.el-table .cell) {
  line-height: 22px;
}
.table-card :deep(.el-pagination) {
  margin-top: 16px;
}
.tracking-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.tracking-cell__meta {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.el-timeline p {
  margin: 8px 0 4px;
  white-space: pre-wrap;
}
.el-timeline small {
  color: var(--el-text-color-secondary);
}

@media (max-width: 768px) {
  .heading-actions,
  .filter-card__header,
  .table-card__header {
    align-items: flex-start;
    flex-direction: column;
  }
  .heading-actions {
    width: 100%;
  }
  .keyword-input,
  .select-input {
    width: 100%;
  }
  .filter-card :deep(.el-form) {
    display: flex;
    flex-direction: column;
    align-items: stretch;
  }
  .filter-card :deep(.el-form-item) {
    margin-right: 0;
  }
  .table-card__tools {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
