<template>
  <div class="app-container inbound-outbound-trend-page">
    <PageHeading kicker="INBOUND &amp; OUTBOUND" title="收发趋势" description="查看未来指定天数的待发货与预计归还数量" summary-label="总预发"
      :summary-value="pendingShipTotal" summary-suffix="单" />

    <el-form :inline="true" :model="query" class="search-form">
      <el-form-item label="所属仓库">
        <el-select v-model="query.warehouseId" clearable filterable :loading="optionsLoading" placeholder="全部仓库"
          @change="load">
          <el-option v-for="item in warehouses" :key="item.id" :label="`${item.name}`" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="品牌名称">
        <el-select v-model="query.brand" clearable filterable placeholder="全部品牌" :disabled="query.mode === 'package'"
          @change="changeBrand">
          <el-option v-for="brand in brands" :key="brand" :label="brand" :value="brand" />
        </el-select>
      </el-form-item>
      <el-form-item label="设备型号">
        <el-select v-model="query.modelId" clearable filterable :loading="optionsLoading" placeholder="全部型号"
          :disabled="query.mode === 'package'" @change="load">
          <el-option v-for="item in models" :key="item.modelId" :label="item.modelName" :value="item.modelId" />
        </el-select>
      </el-form-item>
      <el-form-item label="展示天数">
        <el-select v-model="query.days" placeholder="请选择展示天数" @change="load">
          <el-option v-for="days in dayOptions" :key="days" :label="`${days} 天`" :value="days" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-row class="mb8 trend-toolbar" justify="space-between" align="middle">
      <el-col :span="8" :xs="24">
        <h2 class="page-title">未来 {{ query.days }} 天收发趋势</h2>
      </el-col>
      <el-col :span="8" :xs="24" class="mode-control">
        <el-radio-group v-model="query.mode" @change="changeMode">
          <el-radio-button label="model">按型号</el-radio-button>
          <el-radio-button label="package">按套餐</el-radio-button>
        </el-radio-group>
      </el-col>
    </el-row>
    <el-row class="mb8 trend-toolbar" justify="space-between" align="middle">
      <el-col :span="8" :xs="24">
        <div class="trend-legend" aria-label="趋势图例">
          <span>
            <i class="legend-dot ship-dot" />
            发货
          </span>
          <span>
            <i class="legend-dot return-dot" />
            归还
          </span>
        </div>
      </el-col>

      <el-col :span="8" :xs="24" class="mode-control">
        <el-checkbox v-model="query.showAll" class="show-all-control" @change="load">
          是否展示所有{{ query.mode === 'model' ? '型号' : '套餐' }}
        </el-checkbox>
      </el-col>
    </el-row>

    <section class="chart-panel" aria-label="收发趋势图">
      <div class="chart-panel__heading">
        <div>
          <h3>收发趋势图</h3>
          <p>按当前筛选条件汇总展示物流预计与订单收发数量</p>
        </div>
      </div>
      <TrendLineChart
        :categories="chartCategories"
        :series="chartSeries"
        y-axis-name="数量"
        empty-text="所选条件下暂无收发趋势数据" />
    </section>

    <el-table v-loading="loading" :data="displayRows" border row-key="rowKey" class="trend-table">
      <el-table-column :label="query.mode === 'model' ? '型号' : '套餐名称'" prop="itemName" fixed="left" min-width="230">
        <template #default="{ row }">
          <div class="item-name">{{ row.itemName || "未命名" }}</div>
        </template>
      </el-table-column>
      <el-table-column v-if="query.mode === 'model'" label="品牌" prop="itemBrand" fixed="left" width="140">
        <template #default="{ row }">{{ row.itemBrand || "-" }}</template>
      </el-table-column>
      <el-table-column v-if="query.mode === 'package'" label="套餐编码" prop="itemCode" fixed="left" width="180">
        <template #default="{ row }">{{ row.itemCode || "-" }}</template>
      </el-table-column>
      <el-table-column label="今日趋势" fixed="left" align="center">
        <el-table-column width="120" align="center">
          <template #default>
            <div class="trend-description" aria-label="趋势数据说明">
              <span>物流预计</span>
              <div class="trend-cell-divider" aria-hidden="true" />
              <span>订单库存</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="trendDates.length" width="136" align="center">
          <!-- <template #header>
            <el-tooltip content="上方：物流预计发货 | 预计归还；下方：订单预计发货 | 预计归还" placement="top">
              <span>收发数量</span>
            </el-tooltip>
          </template> -->
          <template #default="{ row }">
            <div class="trend-cell">
              <div class="trend-values trend-values--estimated">
                <button type="button" class="trend-number estimated-ship-value"
                  :disabled="!hasTrendQuantity(row, trendDates[0], 'estimatedShipQuantity')"
                  @click="openTrendOrders(row, trendDates[0], 'ship', 'estimated')">
                  {{ row.daily[trendDates[0]]?.estimatedShipQuantity ?? 0 }}
                </button>
                <span class="trend-divider">|</span>
                <button type="button" class="trend-number estimated-return-value"
                  :disabled="!hasTrendQuantity(row, trendDates[0], 'estimatedReturnQuantity')"
                  @click="openTrendOrders(row, trendDates[0], 'return', 'estimated')">
                  {{ row.daily[trendDates[0]]?.estimatedReturnQuantity ?? 0 }}
                </button>
              </div>
              <div class="trend-cell-divider" aria-hidden="true" />
              <div class="trend-values">
                <button type="button" class="trend-number ship-value"
                  :disabled="!hasTrendQuantity(row, trendDates[0], 'shipQuantity')"
                  @click="openTrendOrders(row, trendDates[0], 'ship', 'order')">
                  {{ row.daily[trendDates[0]]?.shipQuantity ?? 0 }}
                </button>
                <span class="trend-divider">|</span>
                <button type="button" class="trend-number return-value"
                  :disabled="!hasTrendQuantity(row, trendDates[0], 'returnQuantity')"
                  @click="openTrendOrders(row, trendDates[0], 'return', 'order')">
                  {{ row.daily[trendDates[0]]?.returnQuantity ?? 0 }}
                </button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table-column>
      <el-table-column v-for="(date, dateIndex) in trendDates.slice(1)" :key="date"
        :fixed="dateIndex + 1 < ESTIMATED_DISPLAY_DAYS ? 'left' : false" :label="formatDate(date)" width="136"
        align="center">
        <template #header>
          <el-tooltip :content="dateIndex + 1 < 7
            ? '上方：物流预计发货 | 预计归还；下方：订单预计发货 | 预计归还'
            : '发货数量 | 预计归还数量'" placement="top">
            <span>{{ formatDate(date) }}</span>
          </el-tooltip>
        </template>
        <template #default="{ row }">
          <div v-if="dateIndex + 1 < ESTIMATED_DISPLAY_DAYS" class="trend-cell">
            <div class="trend-values trend-values--estimated">
              <button type="button" class="trend-number estimated-ship-value"
                :disabled="!hasTrendQuantity(row, date, 'estimatedShipQuantity')"
                @click="openTrendOrders(row, date, 'ship', 'estimated')">
                {{ row.daily[date]?.estimatedShipQuantity ?? 0 }}
              </button>
              <span class="trend-divider">|</span>
              <button type="button" class="trend-number estimated-return-value"
                :disabled="!hasTrendQuantity(row, date, 'estimatedReturnQuantity')"
                @click="openTrendOrders(row, date, 'return', 'estimated')">
                {{ row.daily[date]?.estimatedReturnQuantity ?? 0 }}
              </button>
            </div>
            <div class="trend-cell-divider" aria-hidden="true" />
            <div class="trend-values">
              <button type="button" class="trend-number ship-value"
                :disabled="!hasTrendQuantity(row, date, 'shipQuantity')"
                @click="openTrendOrders(row, date, 'ship', 'order')">
                {{ row.daily[date]?.shipQuantity ?? 0 }}
              </button>
              <span class="trend-divider">|</span>
              <button type="button" class="trend-number return-value"
                :disabled="!hasTrendQuantity(row, date, 'returnQuantity')"
                @click="openTrendOrders(row, date, 'return', 'order')">
                {{ row.daily[date]?.returnQuantity ?? 0 }}
              </button>
            </div>
          </div>
          <div v-else class="trend-values">
            <button type="button" class="trend-number ship-value"
              :disabled="!hasTrendQuantity(row, date, 'shipQuantity')"
              @click="openTrendOrders(row, date, 'ship', 'order')">
              {{ row.daily[date]?.shipQuantity ?? 0 }}
            </button>
            <span class="trend-divider">|</span>
            <button type="button" class="trend-number return-value"
              :disabled="!hasTrendQuantity(row, date, 'returnQuantity')"
              @click="openTrendOrders(row, date, 'return', 'order')">
              {{ row.daily[date]?.returnQuantity ?? 0 }}
            </button>
          </div>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="所选日期范围内暂无收发安排" :image-size="76" />
      </template>
    </el-table>

    <el-dialog v-model="orderDialogVisible" :title="orderDialogTitle" width="980px" destroy-on-close>
      <el-table v-loading="orderDialogLoading" :data="trendOrders" border max-height="520">
        <el-table-column label="订单编号" min-width="155">
          <template #default="{ row }">
            <el-link
              v-if="row.orderId"
              type="primary"
              :underline="false"
              @click="goToOrderDetail(row)">
              {{ row.orderNo || "-" }}
            </el-link>
            <span v-else>{{ row.orderNo || "-" }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" min-width="110" />
        <el-table-column prop="recipientName" label="收件人" min-width="100" />
        <el-table-column prop="recipientPhone" label="收件电话" min-width="125" />
        <el-table-column prop="itemName" :label="query.mode === 'model' ? '型号' : '套餐'" min-width="160" />
        <el-table-column prop="quantity" label="数量" width="75" align="center" />
        <el-table-column prop="warehouseName" label="仓库" min-width="110" />
        <el-table-column prop="trendTime" label="到达时间" min-width="165" />
        <el-table-column label="订单状态" width="105" align="center">
          <template #default="{ row }">
            <el-tag :type="trendOrderStatusType(row.orderStatus)">
              {{ trendOrderStatusLabel(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无对应订单" :image-size="70" />
        </template>
      </el-table>
      <el-pagination v-if="orderDialogTotal > orderDialogPageSize" v-model:current-page="orderDialogPage"
        :page-size="orderDialogPageSize" :total="orderDialogTotal" layout="prev, pager, next, total"
        class="trend-orders-pagination" @current-change="loadTrendOrders" />
    </el-dialog>

    <MerchantOrderDetailDialog
      v-model="orderDetailVisible"
      :order="selectedOrder"
      :order-id="selectedOrder?.orderId" />
  </div>
</template>

<script setup>
import { computed, onActivated, onMounted, reactive, ref } from "vue";
import {
  listMerchantInboundOutboundTrend,
  listMerchantInboundOutboundTrendOrders,
  listMerchantWarehouses,
} from "@/api/merchant/rentalBusiness";
import { listMerchantInventoryOptions } from "@/api/merchant/inventory";
import { getMerchantStoreSettings } from "@/api/merchant/storeSettings";
import { RENTAL_CODE } from "@/utils/rental/options";
import MerchantOrderDetailDialog from "@/components/MerchantOrderDetailDialog.vue";
import TrendLineChart from "@/components/TrendLineChart.vue";

const loading = ref(false);
const optionsLoading = ref(false);
const rows = ref([]);
const pendingShipTotal = ref(0);
const dayOptions = [3, 5, 7, 15, 30];
const ESTIMATED_DISPLAY_DAYS = 3;
const warehouses = ref([]);
const allOptions = ref([]);
const orderDialogVisible = ref(false);
const orderDialogLoading = ref(false);
const orderDialogTitle = ref("");
const trendOrders = ref([]);
const orderDialogTotal = ref(0);
const orderDialogPage = ref(1);
const orderDialogPageSize = 10;
const orderDetailVisible = ref(false);
const selectedOrder = ref(null);
const orderDialogContext = reactive({
  mode: "model",
  warehouseId: undefined,
  itemId: undefined,
  trendDate: "",
  direction: "ship",
  source: "order",
});
const query = reactive({
  mode: "model",
  warehouseId: undefined,
  brand: "",
  modelId: undefined,
  days: 30,
  showAll: false,
});
let hasBeenActivated = false;

const brands = computed(() => [
  ...new Set(allOptions.value.map((item) => item.brand).filter(Boolean)),
]);
const models = computed(() => {
  const seen = new Set();
  return allOptions.value
    .filter((item) => !query.brand || item.brand === query.brand)
    .filter(
      (item) =>
        item.modelId && !seen.has(item.modelId) && seen.add(item.modelId),
    )
    .map((item) => ({
      modelId: item.modelId,
      modelName: item.modelName || item.productName,
    }));
});
const trendDates = computed(() => {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  return Array.from({ length: query.days }, (_, index) => {
    const current = new Date(date);
    current.setDate(date.getDate() + index);
    return dateKey(current);
  });
});
const displayRows = computed(() => {
  const grouped = new Map();
  rows.value.forEach((row) => {
    const key = `${query.mode}:${row.itemId}`;
    if (!grouped.has(key)) {
      const daily = Object.fromEntries(
        trendDates.value.map((date) => [
          date,
          {
            shipQuantity: 0,
            returnQuantity: 0,
            estimatedShipQuantity: 0,
            estimatedReturnQuantity: 0,
          },
        ]),
      );
      grouped.set(key, {
        rowKey: key,
        itemId: row.itemId,
        itemName: row.itemName,
        itemBrand: row.itemBrand,
        itemCode: row.itemCode,
        daily,
      });
    }
    const date = dateKey(row.trendDate);
    const cell = grouped.get(key).daily[date];
    if (cell) {
      cell.shipQuantity += Number(row.shipQuantity || 0);
      cell.returnQuantity += Number(row.returnQuantity || 0);
      cell.estimatedShipQuantity += Number(row.estimatedShipQuantity || 0);
      cell.estimatedReturnQuantity += Number(row.estimatedReturnQuantity || 0);
    }
  });
  return [...grouped.values()];
});
const chartCategories = computed(() => trendDates.value.map(formatDate));
const chartSeries = computed(() => {
  if (!displayRows.value.length) return [];
  return [
  {
    name: "物流预计发货",
    data: trendDates.value.map((date, dateIndex) =>
      dateIndex < ESTIMATED_DISPLAY_DAYS
        ? displayRows.value.reduce(
            (total, row) =>
              total + Number(row.daily[date]?.estimatedShipQuantity || 0),
            0,
          )
        : null,
    ),
    color: "#e6a23c",
    lineType: "dashed",
    area: false,
  },
  {
    name: "物流预计归还",
    data: trendDates.value.map((date, dateIndex) =>
      dateIndex < ESTIMATED_DISPLAY_DAYS
        ? displayRows.value.reduce(
            (total, row) =>
              total + Number(row.daily[date]?.estimatedReturnQuantity || 0),
            0,
          )
        : null,
    ),
    color: "#67c23a",
    lineType: "dashed",
    area: false,
  },
  {
    name: "订单发货",
    data: trendDates.value.map((date) =>
      displayRows.value.reduce(
        (total, row) => total + Number(row.daily[date]?.shipQuantity || 0),
        0,
      ),
    ),
    color: "#409eff",
  },
  {
    name: "订单归还",
    data: trendDates.value.map((date) =>
      displayRows.value.reduce(
        (total, row) => total + Number(row.daily[date]?.returnQuantity || 0),
        0,
      ),
    ),
    color: "#13c2c2",
  },
  ];
});

function dateKey(value) {
  if (value instanceof Date) {
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, "0");
    const day = String(value.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  }
  return value ? String(value).slice(0, 10) : "";
}

function formatDate(value) {
  const key = dateKey(value);
  return key ? key.slice(5).replace("-", "/") : "-";
}

function load() {
  loading.value = true;
  listMerchantInboundOutboundTrend(query)
    .then((response) => {
      const payload = response?.data || response || {};
      rows.value = Array.isArray(payload.rows) ? payload.rows : [];
      pendingShipTotal.value = Number(payload.pendingShipTotal || 0);
    })
    .finally(() => {
      loading.value = false;
    });
}

function hasTrendQuantity(row, date, field) {
  return Number(row?.daily?.[date]?.[field] || 0) > 0;
}

function openTrendOrders(row, date, direction, source) {
  if (!row || !hasTrendQuantity(row, date, source === "estimated"
    ? direction === "ship" ? "estimatedShipQuantity" : "estimatedReturnQuantity"
    : direction === "ship" ? "shipQuantity" : "returnQuantity")) {
    return;
  }
  orderDialogContext.mode = query.mode;
  orderDialogContext.warehouseId = query.warehouseId;
  orderDialogContext.itemId = row.itemId;
  orderDialogContext.trendDate = dateKey(date);
  orderDialogContext.direction = direction;
  orderDialogContext.source = source;
  orderDialogPage.value = 1;
  orderDialogTitle.value = `${formatDate(date)} ${row.itemName || "未命名"} ${source === "estimated" ? "物流预计" : "订单预计"}${direction === "ship" ? "发货" : "归还"}订单`;
  orderDialogVisible.value = true;
  loadTrendOrders();
}

function loadTrendOrders() {
  orderDialogLoading.value = true;
  listMerchantInboundOutboundTrendOrders({
    ...orderDialogContext,
    pageNum: orderDialogPage.value,
    pageSize: orderDialogPageSize,
  })
    .then((response) => {
      const payload = response?.rows ? response : response?.data || response || {};
      trendOrders.value = Array.isArray(payload.rows) ? payload.rows : [];
      orderDialogTotal.value = Number(payload.total || 0);
    })
    .finally(() => {
      orderDialogLoading.value = false;
    });
}

/** 从趋势订单明细直接打开公共订单详情弹窗。 */
function goToOrderDetail(row) {
  if (!row?.orderId) return;
  selectedOrder.value = row;
  orderDialogVisible.value = false;
  orderDetailVisible.value = true;
}

function trendOrderStatusLabel(status) {
  return ({
    [RENTAL_CODE.ORDER_STATUS.PENDING_SHIP]: "待发货",
    [RENTAL_CODE.ORDER_STATUS.PREPARED]: "已备货",
    [RENTAL_CODE.ORDER_STATUS.SHIPPING]: "运输中",
    [RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT]: "已发货",
    [RENTAL_CODE.ORDER_STATUS.SHIPPED]: "已发货",
    [RENTAL_CODE.ORDER_STATUS.RENTING]: "租用中",
    [RENTAL_CODE.ORDER_STATUS.RETURNING]: "归还中",
    [RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN]: "归还逾期",
    [RENTAL_CODE.ORDER_STATUS.OVERDUE]: "已逾期",
    [RENTAL_CODE.ORDER_STATUS.AFTER_SALES]: "售后中",
    [RENTAL_CODE.ORDER_STATUS.COMPLETED]: "已完成",
    [RENTAL_CODE.ORDER_STATUS.CANCELLED]: "已取消",
  }[Number(status)] || "未知状态");
}

function trendOrderStatusType(status) {
  const code = Number(status);
  if ([RENTAL_CODE.ORDER_STATUS.PENDING_SHIP, RENTAL_CODE.ORDER_STATUS.RETURNING].includes(code)) return "warning";
  if ([RENTAL_CODE.ORDER_STATUS.CANCELLED].includes(code)) return "info";
  if ([RENTAL_CODE.ORDER_STATUS.AFTER_SALES, RENTAL_CODE.ORDER_STATUS.OVERDUE,
  RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN].includes(code)) return "danger";
  return "success";
}

function changeBrand() {
  if (
    !models.value.some((item) => Number(item.modelId) === Number(query.modelId))
  ) {
    query.modelId = undefined;
  }
  load();
}

function changeMode() {
  if (query.mode === "package") {
    query.brand = "";
    query.modelId = undefined;
  }
  load();
}

function responseRows(response) {
  return response?.rows || response?.data?.rows || response?.data || [];
}

async function loadOptions() {
  optionsLoading.value = true;
  try {
    const [warehouseResponse, optionResponse] = await Promise.all([
      listMerchantWarehouses({ pageNum: 1, pageSize: 1000, status: 0 }),
      listMerchantInventoryOptions({}),
    ]);
    warehouses.value = responseRows(warehouseResponse);
    allOptions.value = responseRows(optionResponse);
  } finally {
    optionsLoading.value = false;
  }
}

async function loadPage() {
  const [settingsResult] = await Promise.allSettled([
    getMerchantStoreSettings(),
    loadOptions(),
  ]);
  if (settingsResult.status === "fulfilled") {
    const configuredDays = Number(
      settingsResult.value?.data?.settings?.inboundOutboundTrendDays,
    );
    if (dayOptions.includes(configuredDays)) query.days = configuredDays;
  }
  load();
}

onMounted(loadPage);
onActivated(() => {
  if (hasBeenActivated) loadPage();
  hasBeenActivated = true;
});
</script>

<style scoped>
.trend-toolbar {
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.mode-control {
  text-align: right;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.show-all-control {
  margin-right: 0;
}

.trend-legend {
  display: flex;
  align-items: center;
  gap: 14px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.trend-legend span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.ship-dot {
  background: var(--el-color-primary);
}

.return-dot {
  background: var(--el-color-success);
}

.trend-table {
  width: 100%;
}

.chart-panel {
  margin-bottom: 18px;
  padding: 16px 18px 12px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  background: var(--el-bg-color);
}

.chart-panel__heading {
  margin-bottom: 4px;
}

.chart-panel__heading h3 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 600;
}

.chart-panel__heading p {
  margin: 5px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

/* 分组表头只保留“今日趋势”父表头，子列仍保留独立数据单元格。 */
.trend-table :deep(.el-table__header-wrapper thead tr:nth-child(2)),
.trend-table :deep(.el-table__fixed-header-wrapper thead tr:nth-child(2)) {
  height: 0;
  visibility: collapse;
}

.trend-table :deep(.el-table__header-wrapper thead tr:nth-child(2) th),
.trend-table :deep(.el-table__fixed-header-wrapper thead tr:nth-child(2) th),
.trend-table :deep(.el-table__header-wrapper thead tr:nth-child(2) th .cell),
.trend-table :deep(.el-table__fixed-header-wrapper thead tr:nth-child(2) th .cell) {
  height: 0;
  line-height: 0;
  padding-bottom: 0;
  padding-top: 0;
  border-bottom: 0;
}

.item-name {
  font-weight: 600;
}

.trend-values {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  min-width: 88px;
  font-variant-numeric: tabular-nums;
}

.trend-number {
  border: 0;
  padding: 0;
  background: transparent;
  font: inherit;
  line-height: inherit;
  cursor: pointer;
}

.trend-number:not(:disabled):hover {
  text-decoration: underline;
}

.trend-number:disabled {
  cursor: default;
}

.trend-orders-pagination {
  justify-content: flex-end;
  margin-top: 16px;
}

.trend-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.trend-cell-divider {
  width: 100%;
  border-top: 1px dashed var(--el-border-color-lighter);
}

.trend-description {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  line-height: 1.5;
  white-space: nowrap;
}



.estimated-ship-value {
  color: var(--el-color-warning);
}

.estimated-return-value {
  color: var(--el-color-success);
}

.ship-value {
  color: var(--el-color-primary);
}

.return-value {
  color: var(--el-color-success);
}

.trend-divider {
  color: var(--el-border-color);
}

@media (max-width: 768px) {
  .mode-control {
    align-items: flex-start;
  }
}
</style>
