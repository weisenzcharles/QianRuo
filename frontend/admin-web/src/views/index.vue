<template>
  <div v-loading="loading" class="app-container platform-dashboard">
    <el-card shadow="never" class="dashboard-toolbar">
      <el-row :gutter="16" justify="space-between" align="middle">
        <el-col :xs="24" :sm="16">
          <div class="toolbar-heading">
            <div class="toolbar-title">平台经营工作台</div>
            <div class="toolbar-subtitle">
              汇总平台商户、订单、商品和库存的实时经营数据
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8" class="toolbar-actions">
          <el-tag type="success" effect="plain">全平台</el-tag>
          <el-button
            type="primary"
            :icon="Refresh"
            :loading="loading"
            @click="loadDashboard">
            刷新数据
          </el-button>
        </el-col>
      </el-row>
      <el-alert
        v-if="loadError"
        class="dashboard-alert"
        title="部分统计数据加载失败，请刷新后重试"
        type="warning"
        show-icon
        :closable="false" />
    </el-card>

    <section class="dashboard-section">
      <div class="section-heading">
        <div class="section-title">订单汇总</div>
        <el-button link type="primary" @click="go('/order-center/orders')">
          查看订单中心
        </el-button>
      </div>
      <el-row :gutter="16">
        <el-col
          v-for="item in todayMetrics"
          :key="item.label"
          :xs="12"
          :sm="8"
          :md="4">
          <el-card
            shadow="never"
            class="metric-card"
            :class="`metric-${item.type}`">
            <div class="metric-heading">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </div>
            <el-statistic :value="item.value" />
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="dashboard-section">
      <div class="section-heading">
        <div class="section-title">经营汇总</div>
        <el-button link type="primary" @click="go('/rental/merchant-overview')">
          查看经营概况
        </el-button>
      </div>
      <el-row :gutter="16" class="summary-row">
        <el-col :xs="24" :sm="12" :md="5">
          <el-card shadow="never" class="summary-card summary-wide">
            <el-statistic title="运营商户" :value="merchantCount" />
            <el-statistic title="已认证商户" :value="verifiedMerchantCount" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="3">
          <el-card shadow="never" class="summary-card summary-narrow">
            <el-statistic title="本月订单" :value="monthOrderCount" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="5">
          <el-card shadow="never" class="summary-card summary-wide">
            <div class="summary-pair">
              <el-statistic
                title="待处理订单"
                :value="summary.pendingOrderCount" />
              <el-statistic
                title="进行中订单"
                :value="summary.activeOrderCount" />
            </div>
            <el-statistic title="逾期订单" :value="summary.overdueOrderCount" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card shadow="never" class="summary-card summary-wide">
            <div class="summary-pair">
              <el-statistic
                title="设备商品"
                :value="summary.deviceProductCount" />
              <el-statistic title="配件" :value="summary.accessoryCount" />
            </div>
            <div class="summary-pair">
              <el-statistic
                title="总库存"
                :value="summary.totalInventoryQuantity" />
              <el-statistic
                title="可用库存"
                :value="summary.availableInventoryQuantity" />
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="3">
          <el-card shadow="never" class="summary-card summary-narrow">
            <el-statistic title="待审核商户" :value="pendingMerchantCount" />
            <el-statistic title="冻结商户" :value="frozenMerchantCount" />
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="dashboard-section">
      <div class="section-heading">
        <div class="section-title">营收分析</div>
        <el-tag size="small" type="info" effect="plain">按订单租金汇总</el-tag>
      </div>
      <div class="revenue-layout">
        <el-card shadow="never" class="revenue-total">
          <div class="revenue-caption">今日新增订单租金</div>
          <strong>¥ {{ formatMoney(todayRevenue) }}</strong>
        </el-card>
        <div class="revenue-grid">
          <el-card
            v-for="item in revenueCards"
            :key="item.label"
            shadow="never"
            class="revenue-card">
            <el-statistic
              :title="item.label"
              :value="item.value"
              :precision="2"
              prefix="¥ " />
          </el-card>
        </div>
      </div>
    </section>

    <section class="dashboard-section">
      <div class="section-heading">
        <div class="section-title">商户经营概况</div>
        <el-tag size="small" type="info" effect="plain">
          共 {{ merchantCount }} 家商户
        </el-tag>
      </div>
      <!-- <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>商户经营概况</span>
            <span class="header-note">共 {{ merchantCount }} 家商户</span>
          </div>
        </template> -->
      <el-table :data="merchantDashboardRows" border>
        <el-table-column
          label="商户名称"
          prop="merchantName"
          min-width="180"
          show-overflow-tooltip
          fixed="left" />
        <el-table-column label="认证状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="authStatusMeta(row.authStatus).type">
              {{ authStatusMeta(row.authStatus).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="仓库"
          prop="warehouseCount"
          width="80"
          align="right" />
        <el-table-column
          label="设备商品"
          prop="deviceProductCount"
          width="100"
          align="right" />
        <el-table-column
          label="配件"
          prop="accessoryCount"
          width="80"
          align="right" />
        <el-table-column
          label="套餐"
          prop="packageCount"
          width="80"
          align="right" />
        <el-table-column
          label="总库存"
          prop="totalInventoryQuantity"
          width="90"
          align="right" />
        <el-table-column
          label="可用库存"
          prop="availableInventoryQuantity"
          width="100"
          align="right" />
        <el-table-column
          label="待处理订单"
          prop="pendingOrderCount"
          width="110"
          align="right" />
        <el-table-column
          label="逾期订单"
          prop="overdueOrderCount"
          width="100"
          align="right" />
        <template #empty>
          <el-empty description="暂无商户经营数据" :image-size="64" />
        </template>
      </el-table>
      <!-- </el-card> -->
    </section>

    <section class="dashboard-section">
      <div class="section-heading">
        <div class="section-title">最近订单</div>
        <el-button link type="primary" @click="go('/order-center/orders')">
          查看全部
        </el-button>
        <!-- <el-tag size="small" type="info" effect="plain">
          共 {{ merchantCount }} 家商户
          <el-button link type="primary" @click="go('/order-center/orders')">
            查看全部
          </el-button>
        </el-tag> -->
      </div>
      <!-- <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>最近订单</span>
            <el-button link type="primary" @click="go('/order-center/orders')">
              查看全部
            </el-button>
          </div>
        </template> -->
      <el-table :data="recentOrderRows" border>
        <el-table-column
          label="订单编码"
          prop="orderNo"
          min-width="180"
          show-overflow-tooltip />
        <el-table-column
          label="商户名称"
          prop="merchantName"
          min-width="160"
          show-overflow-tooltip />
        <el-table-column
          label="客户"
          prop="customerName"
          min-width="130"
          show-overflow-tooltip />
        <el-table-column label="租赁日期" min-width="210" align="center">
          <template #default="{ row }">
            {{ row.leaseStartDate || "-" }} 至 {{ row.leaseEndDate || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="租金" width="120" align="right">
          <template #default="{ row }">
            ¥{{ formatMoney(row.rentAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.orderStatus).type">
              {{ statusMeta(row.orderStatus).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="创建时间"
          prop="createTime"
          width="180"
          align="center" />
        <template #empty>
          <el-empty description="暂无订单数据" :image-size="64" />
        </template>
      </el-table>
      <!-- </el-card> -->
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import {
  DataAnalysis,
  Goods,
  List,
  Money,
  OfficeBuilding,
  Refresh,
  Warning,
} from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { listPlatformOrders } from "@/api/rental/order";
import { listPlatformMerchantOverviews } from "@/api/rental/merchantOverview";
import { RENTAL_CODE, rentalAuthStatusOptions } from "@/utils/rental/options";
import { NORMAL_DISABLE_CODE } from "@/utils/stateCode";

const PAGE_SIZE = 100;
const router = useRouter();
const loading = ref(false);
const loadError = ref(false);
const merchantRows = ref([]);
const todayOrderRows = ref([]);
const monthOrderRows = ref([]);
const recentOrderRowsSource = ref([]);

const orderStatusLabels = {
  [RENTAL_CODE.ORDER_STATUS.PENDING_SHIP]: "待发货",
  [RENTAL_CODE.ORDER_STATUS.PREPARED]: "已备货",
  [RENTAL_CODE.ORDER_STATUS.SHIPPING]: "运输中",
  [RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT]: "配送中",
  [RENTAL_CODE.ORDER_STATUS.SHIPPED]: "已发货",
  [RENTAL_CODE.ORDER_STATUS.RENTING]: "租赁中",
  [RENTAL_CODE.ORDER_STATUS.RETURNING]: "归还中",
  [RENTAL_CODE.ORDER_STATUS.COMPLETED]: "已完成",
  [RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN]: "归还逾期",
  [RENTAL_CODE.ORDER_STATUS.OVERDUE]: "已逾期",
  [RENTAL_CODE.ORDER_STATUS.AFTER_SALES]: "售后中",
  [RENTAL_CODE.ORDER_STATUS.CANCELLED]: "已取消",
};

const summary = computed(() =>
  merchantRows.value.reduce(
    (result, row) => {
      result.warehouseCount += number(row.warehouseCount);
      result.deviceProductCount += number(row.deviceProductCount);
      result.accessoryCount += number(row.accessoryCount);
      result.packageCount += number(row.packageCount);
      result.totalInventoryQuantity += number(row.totalInventoryQuantity);
      result.availableInventoryQuantity += number(
        row.availableInventoryQuantity,
      );
      result.pendingOrderCount += number(row.pendingOrderCount);
      result.activeOrderCount += number(row.activeOrderCount);
      result.completedOrderCount += number(row.completedOrderCount);
      result.overdueOrderCount += number(row.overdueOrderCount);
      return result;
    },
    {
      warehouseCount: 0,
      deviceProductCount: 0,
      accessoryCount: 0,
      packageCount: 0,
      totalInventoryQuantity: 0,
      availableInventoryQuantity: 0,
      pendingOrderCount: 0,
      activeOrderCount: 0,
      completedOrderCount: 0,
      overdueOrderCount: 0,
    },
  ),
);

const merchantCount = computed(() => merchantRows.value.length);
const verifiedMerchantCount = computed(
  () =>
    merchantRows.value.filter(
      (row) =>
        Number(row.authStatus) === RENTAL_CODE.AUTH_STATUS.VERIFIED &&
        Number(row.status) !== NORMAL_DISABLE_CODE.DISABLED,
    ).length,
);
const pendingMerchantCount = computed(
  () =>
    merchantRows.value.filter(
      (row) => Number(row.authStatus) === RENTAL_CODE.AUTH_STATUS.PENDING,
    ).length,
);
const frozenMerchantCount = computed(
  () =>
    merchantRows.value.filter(
      (row) => Number(row.status) === NORMAL_DISABLE_CODE.DISABLED,
    ).length,
);
const monthOrderCount = computed(() => monthOrderRows.value.length);
const todayRevenue = computed(() => sumAmount(todayOrderRows.value));

const todayMetrics = computed(() => [
  {
    label: "今日新增",
    value: todayOrderRows.value.length,
    type: "primary",
    icon: List,
  },
  {
    label: "今日新增待发",
    value: countOrders(todayOrderRows.value, [
      RENTAL_CODE.ORDER_STATUS.PENDING_SHIP,
      RENTAL_CODE.ORDER_STATUS.PREPARED,
    ]),
    type: "warning",
    icon: OfficeBuilding,
  },
  {
    label: "今日新增运输中",
    value: countOrders(todayOrderRows.value, [
      RENTAL_CODE.ORDER_STATUS.SHIPPING,
      RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT,
      RENTAL_CODE.ORDER_STATUS.SHIPPED,
    ]),
    type: "info",
    icon: DataAnalysis,
  },
  {
    label: "今日新增已完成",
    value: countOrders(todayOrderRows.value, [
      RENTAL_CODE.ORDER_STATUS.COMPLETED,
    ]),
    type: "success",
    icon: Goods,
  },
  {
    label: "今日新增售后",
    value: countOrders(todayOrderRows.value, [
      RENTAL_CODE.ORDER_STATUS.AFTER_SALES,
    ]),
    type: "danger",
    icon: Warning,
  },
  {
    label: "今日新增逾期",
    value: countOrders(todayOrderRows.value, [
      RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN,
      RENTAL_CODE.ORDER_STATUS.OVERDUE,
    ]),
    type: "warning",
    icon: Money,
  },
]);

const revenueCards = computed(() => {
  const completed = sumAmount(
    monthOrderRows.value.filter(
      (row) => Number(row.orderStatus) === RENTAL_CODE.ORDER_STATUS.COMPLETED,
    ),
  );
  const active = sumAmount(
    monthOrderRows.value.filter((row) =>
      [
        RENTAL_CODE.ORDER_STATUS.PENDING_SHIP,
        RENTAL_CODE.ORDER_STATUS.PREPARED,
        RENTAL_CODE.ORDER_STATUS.SHIPPING,
        RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT,
        RENTAL_CODE.ORDER_STATUS.SHIPPED,
        RENTAL_CODE.ORDER_STATUS.RENTING,
        RENTAL_CODE.ORDER_STATUS.RETURNING,
      ].includes(Number(row.orderStatus)),
    ),
  );
  const total = sumAmount(monthOrderRows.value);
  return [
    { label: "本月订单租金", value: total },
    { label: "本月已完成租金", value: completed },
    { label: "本月进行中租金", value: active },
    {
      label: "本月订单数折算",
      value: monthOrderCount.value ? total / monthOrderCount.value : 0,
    },
    {
      label: "今日新增订单数折算",
      value: todayOrderRows.value.length
        ? todayRevenue.value / todayOrderRows.value.length
        : 0,
    },
    { label: "本月待处理租金", value: Math.max(total - completed, 0) },
  ];
});

const merchantDashboardRows = computed(() =>
  [...merchantRows.value]
    .sort(
      (left, right) =>
        number(right.overdueOrderCount) - number(left.overdueOrderCount) ||
        number(right.pendingOrderCount) - number(left.pendingOrderCount),
    )
    .slice(0, 12),
);

const recentOrderRows = computed(() =>
  recentOrderRowsSource.value.slice(0, 8).map((row) => ({
    ...row,
    orderStatus: Number(row.orderStatus),
  })),
);

function responseRows(response) {
  return Array.isArray(response?.rows) ? response.rows : [];
}

async function fetchAllPages(firstResponse, fetchPage) {
  const firstRows = responseRows(firstResponse);
  const total = Number(firstResponse?.total || firstRows.length);
  const pageCount = Math.ceil(total / PAGE_SIZE);
  if (pageCount <= 1) {
    return firstRows;
  }
  const responses = await Promise.all(
    Array.from({ length: pageCount - 1 }, (_, index) => fetchPage(index + 2)),
  );
  return [firstRows, ...responses.map(responseRows)].flat();
}

async function fetchMerchantRows() {
  const firstResponse = await listPlatformMerchantOverviews({
    pageNum: 1,
    pageSize: PAGE_SIZE,
  });
  return fetchAllPages(firstResponse, (pageNum) =>
    listPlatformMerchantOverviews({ pageNum, pageSize: PAGE_SIZE }),
  );
}

async function fetchOrderRows(query) {
  const firstResponse = await listPlatformOrders({
    ...query,
    pageNum: 1,
    pageSize: PAGE_SIZE,
  });
  return fetchAllPages(firstResponse, (pageNum) =>
    listPlatformOrders({ ...query, pageNum, pageSize: PAGE_SIZE }),
  );
}

async function loadDashboard() {
  loading.value = true;
  loadError.value = false;
  const now = new Date();
  const today = formatDate(now);
  const monthStart = formatDate(new Date(now.getFullYear(), now.getMonth(), 1));
  const monthEnd = formatDate(
    new Date(now.getFullYear(), now.getMonth() + 1, 0),
  );
  const results = await Promise.allSettled([
    fetchMerchantRows(),
    fetchOrderRows({ beginTime: today, endTime: today }),
    fetchOrderRows({ beginTime: monthStart, endTime: monthEnd }),
    listPlatformOrders({ pageNum: 1, pageSize: 8 }),
  ]);
  const [merchantResult, todayResult, monthResult, recentResult] = results;
  if (merchantResult.status === "fulfilled")
    merchantRows.value = merchantResult.value;
  if (todayResult.status === "fulfilled")
    todayOrderRows.value = todayResult.value;
  if (monthResult.status === "fulfilled")
    monthOrderRows.value = monthResult.value;
  if (recentResult.status === "fulfilled")
    recentOrderRowsSource.value = responseRows(recentResult.value);
  loadError.value = results.some((result) => result.status === "rejected");
  loading.value = false;
}

function countOrders(rows, statuses) {
  return rows.filter((row) => statuses.includes(Number(row.orderStatus)))
    .length;
}

function sumAmount(rows) {
  return rows.reduce((total, row) => total + number(row.rentAmount), 0);
}

function number(value) {
  const result = Number(value);
  return Number.isFinite(result) ? result : 0;
}

function formatMoney(value) {
  return number(value).toFixed(2);
}

function formatDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function authStatusMeta(value) {
  const option = rentalAuthStatusOptions.find(
    (item) => item.value === Number(value),
  );
  return option
    ? { label: option.label, type: option.tagType || "info" }
    : { label: "未知", type: "info" };
}

function statusMeta(value) {
  const status = Number(value);
  if (
    [
      RENTAL_CODE.ORDER_STATUS.PENDING_SHIP,
      RENTAL_CODE.ORDER_STATUS.AFTER_SALES,
    ].includes(status)
  )
    return { label: orderStatusLabels[status] || "未知状态", type: "warning" };
  if (
    [
      RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN,
      RENTAL_CODE.ORDER_STATUS.OVERDUE,
    ].includes(status)
  )
    return { label: orderStatusLabels[status] || "未知状态", type: "danger" };
  if (
    [
      RENTAL_CODE.ORDER_STATUS.COMPLETED,
      RENTAL_CODE.ORDER_STATUS.RENTING,
    ].includes(status)
  )
    return { label: orderStatusLabels[status] || "未知状态", type: "success" };
  if (
    [
      RENTAL_CODE.ORDER_STATUS.PREPARED,
      RENTAL_CODE.ORDER_STATUS.SHIPPING,
      RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT,
      RENTAL_CODE.ORDER_STATUS.SHIPPED,
      RENTAL_CODE.ORDER_STATUS.RETURNING,
    ].includes(status)
  )
    return { label: orderStatusLabels[status] || "未知状态", type: "primary" };
  return { label: orderStatusLabels[status] || "未知状态", type: "info" };
}

function go(path) {
  router.push(path);
}

onMounted(loadDashboard);
</script>

<style scoped>
.platform-dashboard {
  min-height: 100%;
}

.dashboard-toolbar,
.dashboard-section {
  margin-bottom: 16px;
}

.toolbar-heading {
  min-width: 0;
}

.toolbar-title {
  color: var(--el-text-color-primary);
  font-size: 22px;
  font-weight: 600;
  line-height: 1.4;
}

.toolbar-subtitle,
.header-note {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.toolbar-subtitle {
  margin-top: 4px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.dashboard-alert {
  margin-top: 16px;
}

.section-heading,
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.section-title {
  padding-left: 10px;
  border-left: 3px solid var(--el-color-primary);
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

.section-heading {
  margin-bottom: 12px;
}

.metric-card,
.summary-card,
.revenue-card,
.revenue-total {
  height: 100%;
}

.metric-card {
  border-top: 3px solid var(--el-color-primary);
}

.metric-success {
  border-top-color: var(--el-color-success);
}

.metric-warning {
  border-top-color: var(--el-color-warning);
}

.metric-danger {
  border-top-color: var(--el-color-danger);
}

.metric-info {
  border-top-color: var(--el-color-info);
}

.metric-heading {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
}

.metric-heading .el-icon {
  color: var(--el-color-primary);
}

.metric-warning .metric-heading .el-icon {
  color: var(--el-color-warning);
}

.metric-danger .metric-heading .el-icon {
  color: var(--el-color-danger);
}

.metric-success .metric-heading .el-icon {
  color: var(--el-color-success);
}

.metric-info .metric-heading .el-icon {
  color: var(--el-color-info);
}

.metric-card :deep(.el-card__body),
.summary-card :deep(.el-card__body),
.revenue-card :deep(.el-card__body) {
  min-height: 92px;
}

.summary-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  row-gap: 10px;
  height: 100%;
  box-sizing: border-box;
}

.summary-row > .el-col {
  display: flex;
}

.summary-row .summary-card {
  width: 100%;
}

.summary-card :deep(.el-statistic) {
  min-width: 0;
  margin-bottom: 0;
}

.summary-pair {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  min-width: 0;
  min-height: 0;
}

.summary-pair :deep(.el-statistic) {
  min-width: 0;
  margin-bottom: 0;
}

.revenue-total {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  min-height: 0;
  height: 100%;
  box-sizing: border-box;
  background: var(--el-color-warning-light-9);
  border-color: var(--el-color-warning-light-5);
}

.revenue-caption {
  color: var(--el-color-warning-dark-2);
  font-size: 16px;
}

.revenue-total strong {
  margin-top: 14px;
  color: var(--el-color-warning-dark-2);
  font-size: 34px;
  line-height: 1.2;
}

.revenue-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 2fr);
  gap: 16px;
  align-items: stretch;
}

.revenue-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  min-width: 0;
}

.revenue-grid .revenue-card {
  width: 100%;
  min-width: 0;
}

@media (max-width: 767px) {
  .toolbar-actions {
    justify-content: flex-start;
    margin-top: 12px;
  }

  .section-heading,
  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .revenue-total {
    min-height: 150px;
  }

  .revenue-layout {
    grid-template-columns: 1fr;
  }

  .revenue-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 480px) {
  .revenue-grid {
    grid-template-columns: 1fr;
  }
}
</style>
