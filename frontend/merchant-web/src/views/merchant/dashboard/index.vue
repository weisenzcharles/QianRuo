<template>
  <div class="app-container">
    <el-button
      v-if="!quickPanelVisible"
      class="quick-panel-tab"
      title="展开快捷面板"
      @click="quickPanelVisible = true">
      我的工作台
    </el-button>
    <el-aside v-else class="quick-panel" width="380px">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>我的工作台</span>
            <el-button
              link
              type="primary"
              :icon="ArrowRight"
              title="收起快捷面板"
              @click="quickPanelVisible = false" />
          </div>
        </template>

        <el-form label-position="top" @submit.prevent>
          <el-form-item label="全局搜索">
            <el-row :gutter="8" class="quick-form-row">
              <el-col :span="18">
                <el-input
                  v-model="quickSearch"
                  clearable
                  placeholder="订单编号、客户或商品">
                  <template #append>
                    <el-button
                      :icon="Search"
                      title="搜索"
                      @click="searchOrders" />
                  </template>
                </el-input>
              </el-col>
              <el-col :span="6">
                <el-button
                  type="primary"
                  plain
                  class="quick-action"
                  @click="go('/merchant/order/form')">
                  新增订单
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>

        <el-divider />
        <el-form label-position="top">
          <el-form-item label="时效查询">
            <el-row :gutter="8" class="quick-form-row">
              <el-col :span="9">
                <el-select
                  v-model="quickWarehouse"
                  placeholder="默认仓库"
                  clearable
                  class="quick-control">
                  <el-option
                    v-for="item in warehouseRows"
                    :key="item.id || item.warehouseId"
                    :label="item.name || item.warehouseName"
                    :value="item.id || item.warehouseId" />
                </el-select>
              </el-col>
              <el-col :span="15">
                <el-input
                  v-model="receiverAddress"
                  placeholder="收件地址"
                  clearable />
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>

        <el-divider />
        <div class="panel-section-title">
          <span>今日工作</span>
          <el-button
            link
            type="primary"
            class="panel-more"
            @click="go('/merchant/order')">
            查看更多
          </el-button>
        </div>
        <el-space
          direction="vertical"
          alignment="stretch"
          :size="8"
          class="work-list">
          <el-button
            v-for="item in todayWorkItems"
            :key="item.label"
            link
            class="work-item"
            @click="go('/merchant/order')">
            <el-tag :type="item.type" size="small" effect="dark">
              {{ item.count }}
            </el-tag>
            <span>{{ item.label }}</span>
          </el-button>
        </el-space>

        <el-divider />
        <div class="panel-section-title">
          <span>我的备忘</span>
          <el-button link type="primary" class="panel-more">查看更多</el-button>
        </div>
        <el-empty
          v-if="memoItems.length === 0"
          description="暂无备忘"
          :image-size="56" />
        <el-space
          v-else
          direction="vertical"
          alignment="stretch"
          :size="4"
          class="memo-list">
          <div v-for="(memo, index) in memoItems" :key="memo" class="memo-item">
            <span>{{ index + 1 }}、{{ memo }}</span>
            <el-button link type="danger" :icon="Delete" title="删除备忘" />
          </div>
        </el-space>

        <el-tabs v-model="activeWarningTab" class="warning-tabs">
          <el-tab-pane label="套餐预警" name="package">
            <el-empty description="暂无套餐预警" :image-size="56" />
          </el-tab-pane>
          <el-tab-pane label="型号预警" name="model">
            <el-empty description="暂无型号预警" :image-size="56" />
          </el-tab-pane>
          <el-tab-pane label="仓库预警" name="warehouse">
            <el-empty description="暂无仓库预警" :image-size="56" />
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </el-aside>

    <header class="page-heading">
      <div>
        <p class="page-kicker">MY DASHBOARD</p>
        <h2>我的仪表盘</h2>
        <p class="page-description">
          查看订单、库存、营收与待办事项，掌握商户经营动态。
        </p>
      </div>
      <div class="heading-actions">
        <!-- <el-tag type="info" effect="plain">今日更新 {{ lastUpdated }}</el-tag> -->
        <el-button type="primary" :icon="Plus" @click="openCreateOrder">
          添加订单
        </el-button>
        <el-button type="primary" :icon="Plus" @click="openCreateOrder">
          添加产品
        </el-button>
      </div>
    </header>
    <!-- <PageHeading
      kicker="MY DASHBOARD"
      title="我的仪表盘"
      description="查看订单、库存、营收与待办事项，掌握商户经营动态。" /> -->
    <el-card shadow="never" class="mb16 dashboard-filter">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="切换仓库">
          <el-select
            v-model="dashboardWarehouse"
            clearable
            placeholder="全部"
            class="filter-control">
            <el-option
              v-for="item in warehouseRows"
              :key="item.id || item.warehouseId"
              :label="item.name || item.warehouseName"
              :value="item.id || item.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :icon="Refresh"
            :loading="loading"
            @click="loadDashboard">
            刷新
          </el-button>
          <!-- <el-button :icon="Plus" @click="go('/merchant/order/form')">
            新增订单
          </el-button> -->
        </el-form-item>
      </el-form>
    </el-card>

    <section class="dashboard-section">
      <div class="section-title">今日订单</div>
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
            <div class="metric-label">{{ item.label }}</div>
            <el-statistic :value="item.value" />
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="dashboard-section">
      <div class="section-title">汇总报表</div>
      <el-row :gutter="16" class="summary-row">
        <el-col :xs="24" :sm="12" :md="5">
          <el-card shadow="never" class="summary-card summary-wide">
            <el-statistic title="明日待发" :value="tomorrowPendingCount" />
            <el-statistic title="全部待发" :value="allPendingCount" />
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
              <el-statistic title="已完结订单" :value="completedOrderCount" />
              <el-statistic title="未完结订单" :value="unfinishedOrderCount" />
            </div>
            <el-statistic
              title="未完结（售后）"
              :value="afterSalesOrderCount" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card shadow="never" class="summary-card summary-wide">
            <div class="summary-pair">
              <el-statistic title="商品总数" :value="deviceProductCount" />
              <el-statistic title="配件总数" :value="accessoryCount" />
            </div>
            <div class="summary-pair">
              <el-statistic title="空闲商品" :value="freeDeviceCount" />
              <el-statistic title="空闲配件" :value="freeAccessoryCount" />
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="3">
          <el-card shadow="never" class="summary-card summary-narrow">
            <el-statistic title="归还逾期" :value="overdueReturnCount" />
            <el-statistic title="全部逾期" :value="allOverdueCount" />
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="dashboard-section">
      <div class="section-title">营收分析</div>
      <div class="revenue-layout">
        <el-card shadow="never" class="revenue-total">
          <div class="revenue-caption">今日战绩（成交租金）</div>
          <strong>¥ {{ revenueMetrics.today.toFixed(2) }}</strong>
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
        <div class="section-title">库存概况</div>
        <el-button link type="primary" @click="go('/merchant/inventory')">
          查看库存
        </el-button>
      </div>
      <el-table v-loading="loading" :data="inventoryBusinessRows" border>
        <el-table-column label="商品名称" prop="productName" min-width="180" />
        <el-table-column label="品牌" prop="brand" min-width="100" />
        <el-table-column label="型号" prop="modelName" min-width="140" />
        <el-table-column label="仓库" prop="warehouseName" min-width="140" />
        <el-table-column
          label="剩余数量"
          prop="availableQuantity"
          width="110"
          align="right" />
        <el-table-column
          label="总库存"
          prop="totalQuantity"
          width="100"
          align="right" />
        <el-table-column
          label="已预占"
          prop="reservedQuantity"
          width="100"
          align="right" />
        <el-table-column
          label="出租中"
          prop="rentedQuantity"
          width="100"
          align="right" />
        <el-table-column
          label="维修中"
          prop="repairingQuantity"
          width="100"
          align="right" />
        <template #empty>
          <el-empty description="暂无在库商品" :image-size="64" />
        </template>
      </el-table>
      <!-- </el-card> -->
    </section>

    <section class="dashboard-section">
      <div class="section-heading">
        <div class="section-title">最新订单</div>
        <el-button link type="primary" @click="go('/merchant/order')">
          查看全部
        </el-button>
      </div>
      <el-table v-loading="loading" :data="recentOrders" border>
        <el-table-column label="订单编号" prop="orderNo" min-width="100" />
        <el-table-column label="客户" prop="customerName" min-width="100" />
        <el-table-column label="商品/套餐" prop="productName" min-width="220" />
        <el-table-column label="租期" min-width="200">
          <template #default="{ row }">
            <span>{{ row.rentalPeriod }}</span>
            <span v-if="row.leaseDays > 0">（共 {{ row.leaseDays }} 天）</span>
          </template>
        </el-table-column>
        <el-table-column label="发货仓库" prop="shippingWarehouse" min-width="120"/>
        <el-table-column label="发货日期" prop="shipDate" min-width="80" />
        <el-table-column label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.statusType">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createdAt" min-width="120" />
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
  ArrowLeft,
  ArrowRight,
  Box,
  Delete,
  Goods,
  Plus,
  Refresh,
  Search,
} from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { listMerchantInventory } from "@/api/merchant/inventory";
import {
  listMerchantAccessories,
  listMerchantPackages,
  listMerchantProducts,
  listMerchantWarehouses,
} from "@/api/merchant/rentalBusiness";
import { listMerchantOrders } from "@/api/merchant/order";
import { RENTAL_CODE } from "@/utils/rental/options";

const router = useRouter();
const loading = ref(false);
const orders = ref([]);
const inventoryRows = ref([]);
const productRows = ref([]);
const accessoryRows = ref([]);
const packageRows = ref([]);
const warehouseRows = ref([]);
const quickPanelVisible = ref(false);
const quickSearch = ref("");
const quickWarehouse = ref();
const receiverAddress = ref("");
const dashboardWarehouse = ref();
const activeWarningTab = ref("package");
const memoItems = ref(["核对待发货订单的发货时间", "补充周末仓库值班安排"]);

const todayWorkItems = computed(() => [
  {
    label: "今日待发",
    count: countOrders([RENTAL_CODE.ORDER_STATUS.PENDING_SHIP]),
    type: "primary",
  },
  { label: "今日接单", count: countTodayOrders(), type: "success" },
  {
    label: "今日完结",
    count: countTodayOrders([RENTAL_CODE.ORDER_STATUS.COMPLETED]),
    type: "success",
  },
  {
    label: "逾期校对",
    count: countOrders([
      RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN,
      RENTAL_CODE.ORDER_STATUS.OVERDUE,
    ]),
    type: "warning",
  },
  {
    label: "售后待办",
    count: countOrders([RENTAL_CODE.ORDER_STATUS.AFTER_SALES]),
    type: "danger",
  },
]);

const orderStatusLabels = {
  [RENTAL_CODE.ORDER_STATUS.PENDING_SHIP]: "待发货",
  [RENTAL_CODE.ORDER_STATUS.PREPARED]: "已备货",
  [RENTAL_CODE.ORDER_STATUS.SHIPPING]: "运输中",
  [RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT]: "运输中",
  [RENTAL_CODE.ORDER_STATUS.SHIPPED]: "已发货",
  [RENTAL_CODE.ORDER_STATUS.RENTING]: "租用中",
  [RENTAL_CODE.ORDER_STATUS.RETURNING]: "归还中",
  [RENTAL_CODE.ORDER_STATUS.COMPLETED]: "已完成",
  [RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN]: "归还逾期",
  [RENTAL_CODE.ORDER_STATUS.OVERDUE]: "已逾期",
  [RENTAL_CODE.ORDER_STATUS.AFTER_SALES]: "售后中",
  [RENTAL_CODE.ORDER_STATUS.CANCELLED]: "已取消",
};

const todayMetrics = computed(() => [
  {
    label: "今日待发",
    value: countTodayOrders([RENTAL_CODE.ORDER_STATUS.PENDING_SHIP]),
    type: "primary",
  },
  {
    label: "今日待归还",
    value: countTodayOrders([RENTAL_CODE.ORDER_STATUS.RETURNING]),
    type: "info",
  },
  { label: "今日新增", value: countTodayOrders(), type: "success" },
  {
    label: "今日预计到库",
    value: countTodayOrders([
      RENTAL_CODE.ORDER_STATUS.RENTING,
      RENTAL_CODE.ORDER_STATUS.RETURNING,
    ]),
    type: "info",
  },
  {
    label: "今日售后",
    value: countTodayOrders([RENTAL_CODE.ORDER_STATUS.AFTER_SALES]),
    type: "danger",
  },
  {
    label: "晚到签收",
    value: countOrders([RENTAL_CODE.ORDER_STATUS.OVERDUE]),
    type: "warning",
  },
]);

const monthOrderCount = computed(
  () =>
    orders.value.filter(
      (item) =>
        String(item.createdAt || "").slice(0, 7) ===
        new Date().toISOString().slice(0, 7),
    ).length,
);

const allPendingCount = computed(() =>
  countOrders([RENTAL_CODE.ORDER_STATUS.PENDING_SHIP]),
);
const tomorrowPendingCount = computed(
  () =>
    orders.value.filter(
      (item) =>
        item.status === RENTAL_CODE.ORDER_STATUS.PENDING_SHIP &&
        isTomorrow(item.shipDueTime || item.shipDate || item.leaseStartDate),
    ).length,
);
const completedOrderCount = computed(() =>
  countOrders([RENTAL_CODE.ORDER_STATUS.COMPLETED]),
);
const unfinishedOrderCount = computed(
  () =>
    orders.value.length -
    countOrders([
      RENTAL_CODE.ORDER_STATUS.COMPLETED,
      RENTAL_CODE.ORDER_STATUS.CANCELLED,
    ]),
);
const afterSalesOrderCount = computed(() =>
  countOrders([RENTAL_CODE.ORDER_STATUS.AFTER_SALES]),
);
const deviceProductCount = computed(
  () =>
    productRows.value.filter(
      (item) => Number(item.productType) !== RENTAL_CODE.PRODUCT_TYPE.ACCESSORY,
    ).length,
);
const accessoryCount = computed(
  () =>
    accessoryRows.value.length ||
    productRows.value.filter(
      (item) => Number(item.productType) === RENTAL_CODE.PRODUCT_TYPE.ACCESSORY,
    ).length,
);
const freeDeviceCount = computed(() =>
  sumInventoryByType(RENTAL_CODE.PRODUCT_TYPE.DEVICE, "availableQuantity"),
);
const freeAccessoryCount = computed(() =>
  sumInventoryByType(RENTAL_CODE.PRODUCT_TYPE.ACCESSORY, "availableQuantity"),
);
const overdueReturnCount = computed(() =>
  countOrders([RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN]),
);
const allOverdueCount = computed(() =>
  countOrders([
    RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN,
    RENTAL_CODE.ORDER_STATUS.OVERDUE,
  ]),
);

const revenueMetrics = computed(() => ({
  today: sumOrderAmount(orders.value.filter((item) => isToday(item.createdAt))),
  month: sumOrderAmount(
    orders.value.filter(
      (item) =>
        String(item.createdAt || "").slice(0, 7) ===
        new Date().toISOString().slice(0, 7),
    ),
  ),
  completed: sumOrderAmount(
    orders.value.filter(
      (item) => item.status === RENTAL_CODE.ORDER_STATUS.COMPLETED,
    ),
  ),
  pending: sumOrderAmount(
    orders.value.filter(
      (item) => item.status !== RENTAL_CODE.ORDER_STATUS.CANCELLED,
    ),
  ),
}));

const revenueCards = computed(() => [
  { label: "总收益", value: revenueMetrics.value.month },
  { label: "已完成收益", value: revenueMetrics.value.completed },
  {
    label: "待结算收益",
    value: Math.max(
      revenueMetrics.value.pending - revenueMetrics.value.completed,
      0,
    ),
  },
  { label: "昨日战绩（成交租金）", value: 0 },
  { label: "本月战绩（实收）", value: revenueMetrics.value.month },
  { label: "上月战绩（实收）", value: 0 },
]);

const inventoryBusinessRows = computed(() => {
  const grouped = new Map();
  filteredInventoryRows.value.forEach((row) => {
    const key = [row.productId, row.warehouseId, row.productName].join("-");
    const current = grouped.get(key) || {
      ...row,
      totalQuantity: 0,
      availableQuantity: 0,
      reservedQuantity: 0,
      rentedQuantity: 0,
      repairingQuantity: 0,
    };
    current.totalQuantity += Number(row.totalQuantity || 0);
    current.availableQuantity += Number(row.availableQuantity || 0);
    current.reservedQuantity += Number(row.reservedQuantity || 0);
    current.rentedQuantity += Number(row.rentedQuantity || 0);
    current.repairingQuantity += Number(row.repairingQuantity || 0);
    grouped.set(key, current);
  });
  return [...grouped.values()];
});

const filteredInventoryRows = computed(() =>
  inventoryRows.value.filter(
    (row) =>
      !dashboardWarehouse.value ||
      Number(row.warehouseId) === Number(dashboardWarehouse.value),
  ),
);

const recentOrders = computed(() =>
  orders.value.slice(0, 8).map((order) => ({
    orderNo: order.orderNo || "-",
    customerName: order.customer?.name || order.customer?.nickname || "-",
    productName:
      [order.model, order.packageName].filter(Boolean).join(" / ") || "-",
    rentalPeriod: order.rentalPeriod || "-",
    leaseDays: order.leaseDays,
    shippingWarehouse: order.shippingWarehouse || "-",
    shipDate: order.shipDate || "-",
    statusLabel: orderStatusLabels[order.status] || "未知状态",
    statusType: statusType(order.status),
    createdAt: order.createdAt || "-",
  })),
);

function responseRows(response) {
  return response?.rows || response?.data?.rows || response?.data || [];
}

function countOrders(statuses) {
  return orders.value.filter((item) => statuses.includes(item.status)).length;
}

function countTodayOrders(statuses) {
  const today = new Date().toISOString().slice(0, 10);
  return orders.value.filter((item) => {
    const createdDate = String(item.createdAt || "").slice(0, 10);
    return (
      createdDate === today && (!statuses || statuses.includes(item.status))
    );
  }).length;
}

function sumInventory(field) {
  return inventoryRows.value.reduce(
    (total, row) => total + Number(row[field] || 0),
    0,
  );
}

function sumInventoryByType(productType, field) {
  return inventoryRows.value
    .filter((row) => Number(row.productType) === productType)
    .reduce((total, row) => total + Number(row[field] || 0), 0);
}

function isToday(value) {
  return (
    String(value || "").slice(0, 10) === new Date().toISOString().slice(0, 10)
  );
}

function isTomorrow(value) {
  if (!value) return false;
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  return String(value).slice(0, 10) === tomorrow.toISOString().slice(0, 10);
}

function sumOrderAmount(rows) {
  return rows.reduce(
    (total, row) =>
      total + Number(row.rentAmount ?? row.rent ?? row.amount ?? 0),
    0,
  );
}

function statusType(status) {
  if (
    [
      RENTAL_CODE.ORDER_STATUS.PENDING_SHIP,
      RENTAL_CODE.ORDER_STATUS.RETURNING,
    ].includes(status)
  )
    return "warning";
  if (
    [
      RENTAL_CODE.ORDER_STATUS.SHIPPING,
      RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT,
      RENTAL_CODE.ORDER_STATUS.SHIPPED,
      RENTAL_CODE.ORDER_STATUS.PREPARED,
    ].includes(status)
  )
    return "primary";
  if (
    [
      RENTAL_CODE.ORDER_STATUS.COMPLETED,
      RENTAL_CODE.ORDER_STATUS.RENTING,
    ].includes(status)
  )
    return "success";
  if (
    [
      RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN,
      RENTAL_CODE.ORDER_STATUS.OVERDUE,
      RENTAL_CODE.ORDER_STATUS.AFTER_SALES,
    ].includes(status)
  )
    return "danger";
  return "info";
}

function go(path) {
  router.push(path);
}

function searchOrders() {
  router.push({
    path: "/merchant/order",
    query: { keyword: quickSearch.value },
  });
}

async function loadDashboard() {
  loading.value = true;
  try {
    const [
      orderResponse,
      inventoryResponse,
      productResponse,
      packageResponse,
      warehouseResponse,
      accessoryResponse,
    ] = await Promise.all([
      listMerchantOrders({ pageNum: 1, pageSize: 1000 }),
      listMerchantInventory({ pageNum: 1, pageSize: 1000 }),
      listMerchantProducts({ pageNum: 1, pageSize: 1000 }),
      listMerchantPackages({ pageNum: 1, pageSize: 1000 }),
      listMerchantWarehouses({ pageNum: 1, pageSize: 1000, status: 0 }),
      listMerchantAccessories({ pageNum: 1, pageSize: 1000 }),
    ]);
    orders.value = responseRows(orderResponse)
      .map(normalizeOrder)
      .sort((left, right) =>
        String(right.createdAt).localeCompare(String(left.createdAt)),
      );
    inventoryRows.value = responseRows(inventoryResponse);
    productRows.value = responseRows(productResponse);
    packageRows.value = responseRows(packageResponse);
    warehouseRows.value = responseRows(warehouseResponse);
    accessoryRows.value = responseRows(accessoryResponse);
  } finally {
    loading.value = false;
  }
}

function normalizeOrder(row) {
  const status = Number(row.status ?? row.orderStatus);
  const leaseStartDate = row.leaseStartDate || row.leaseStart;
  const leaseEndDate = row.leaseEndDate || row.leaseEnd;
  const leaseDays = Number(row.leaseDays || 0);
  const shippingWarehouse =
    row.shippingWarehouse ||
    row.shippingWarehouseName ||
    row.warehouseName ||
    row.shipping?.warehouse;
  const shipDate =
    row.shipDueTime || row.shipDate || row.shipping?.shipDate || row.createTime;
  return {
    ...row,
    status,
    createdAt: row.createdAt || row.createTime || row.createdTime,
    customer: row.customer || {
      name: row.customerName,
      nickname: row.customerName,
    },
    packageName: row.packageName || row.productName,
    model: row.model || row.modelName,
    rentalPeriod:
      row.rentalPeriod ||
      [leaseStartDate, leaseEndDate].filter(Boolean).join(" ~ "),
    leaseDays:
      leaseDays > 0 ? leaseDays : calculateLeaseDays(leaseStartDate, leaseEndDate),
    shippingWarehouse,
    shipDate: shipDate ? String(shipDate).slice(0, 10) : null,
  };
}

function calculateLeaseDays(startDate, endDate) {
  const start = String(startDate || "").slice(0, 10);
  const end = String(endDate || "").slice(0, 10);
  if (!start || !end) return 0;

  const startTime = Date.parse(`${start}T00:00:00`);
  const endTime = Date.parse(`${end}T00:00:00`);
  if (!Number.isFinite(startTime) || !Number.isFinite(endTime)) return 0;

  return Math.max(0, Math.floor((endTime - startTime) / 86400000) + 1);
}

onMounted(loadDashboard);
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}

.dashboard-filter {
  margin-bottom: 16px;
}

.dashboard-filter :deep(.el-form) {
  align-items: center;
  margin-bottom: 0;
}

.dashboard-filter :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-control {
  width: 180px;
}

.dashboard-section {
  margin-bottom: 16px;
}

.section-title {
  margin: 0 0 12px;
  padding-left: 10px;
  border-left: 3px solid var(--el-color-primary);
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 600;
}

.metric-card,
.summary-card,
.revenue-card,
.revenue-total {
  height: 100%;
}

.metric-card :deep(.el-card__body),
.summary-card :deep(.el-card__body),
.revenue-card :deep(.el-card__body) {
  min-height: 92px;
}

.metric-label {
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
}

.metric-primary {
  border-top: 3px solid var(--el-color-primary);
}
.metric-success {
  border-top: 3px solid var(--el-color-success);
}
.metric-warning {
  border-top: 3px solid var(--el-color-warning);
}
.metric-danger {
  border-top: 3px solid var(--el-color-danger);
}
.metric-info {
  border-top: 3px solid var(--el-color-info);
}

.summary-primary {
  background: var(--el-fill-color-light);
}

.summary-row > .el-col {
  display: flex;
}

.summary-row .summary-card {
  width: 100%;
}

.summary-card :deep(.el-statistic) {
  margin-bottom: 20px;
}

.summary-card :deep(.el-statistic:last-child) {
  margin-bottom: 0;
}

.summary-pair {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 0px;
}

.summary-card :deep(.summary-pair:first-child) {
  margin-bottom: 20px;
}

.summary-pair :deep(.el-statistic) {
  margin-bottom: 0;
}

.summary-pair :deep(.el-statistic:last-child) {
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
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  background: var(--el-color-warning-light-9);
  color: var(--el-color-warning);
}

.revenue-caption {
  color: var(--el-color-warning);
}

.revenue-total strong {
  display: block;
  margin-top: 18px;
  color: var(--el-color-warning-dark-2);
  font-size: 30px;
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

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quick-links {
  width: 100%;
}

.quick-links :deep(.el-button) {
  width: 100%;
  margin-left: 0;
}

.quick-panel-tab {
  position: fixed;
  top: 42%;
  right: 0;
  z-index: 20;
  writing-mode: vertical-rl;
  min-height: 150px;
  padding: 14px 10px;
  border-radius: 6px 0 0 6px;
}

.quick-panel {
  position: fixed;
  top: 72px;
  right: 0;
  bottom: 0;
  z-index: 19;
  overflow: auto;
  padding: 0;
}

.quick-panel :deep(.el-card) {
  height: 100%;
  border-radius: 0;
}

.quick-form-row,
.quick-action,
.quick-control,
.work-list,
.memo-list {
  width: 100%;
}

.quick-form-row .el-col {
  min-width: 0;
}

.panel-section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  font-weight: 600;
}

.work-item,
.memo-item {
  display: flex;
  align-items: center;
  justify-content: flex-start !important;
  gap: 8px;
  width: 100%;
  padding: 4px 0;
}

.work-item :deep(.el-tag) {
  flex: 0 0 42px;
  margin-right: 4px;
  text-align: center;
}

.memo-item {
  justify-content: space-between;
}

.panel-more {
  flex: 0 0 auto;
  margin: 0;
}

.warning-tabs {
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
@media (max-width: 768px) {
  .filter-control {
    width: 100%;
  }

  .revenue-layout {
    grid-template-columns: 1fr;
  }

  .revenue-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .quick-panel {
    top: 56px;
    width: calc(100vw - 16px) !important;
    padding-right: 8px;
  }
}

@media (max-width: 480px) {
  .revenue-grid {
    grid-template-columns: 1fr;
  }
}
</style>
