<template>
  <div class="app-container inventory-overview-page">
    <header class="page-heading">
      <div>
        <p class="page-kicker">INVENTORY OVERVIEW</p>
        <h2>库存概况</h2>
        <p class="page-description">
          集中处理录单、备货、发货、归还和售后节点，订单信息按实际工作流展开。
        </p>
      </div>
      <!-- <div class="heading-actions">
        <el-tag type="info" effect="plain">今日更新 {{ lastUpdated }}</el-tag>
        <el-button type="primary" :icon="Plus" @click="openCreateOrder">
          新建订单
        </el-button>
      </div> -->
    </header>
    <!-- <div class="page-heading">
      <div>
        <h2>库存概况</h2>
        <p>已选择项，仅展示当前商户经营分类内的标准型号</p>
      </div>
    </div> -->
    <!-- <div class="page-toolbar">
      <div>
        <h2>库存概况</h2>
        <span class="selection-count">
          已选择项，仅展示当前商户经营分类内的标准型号
        </span>
      </div>
    </div> -->
    <el-form :inline="true" :model="query" class="search-form">
      <el-form-item label="仓库">
        <el-select
          v-model="query.warehouseId"
          clearable
          filterable
          placeholder="全部仓库"
          @change="load">
          <el-option
            v-for="item in warehouses"
            :key="item.id"
            :label="`${item.name}`"
            :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="品牌">
        <el-select
          v-model="query.brand"
          clearable
          filterable
          placeholder="全部品牌"
          :disabled="query.mode === 'package'"
          @change="changeBrand">
          <el-option
            v-for="brand in brands"
            :key="brand"
            :label="brand"
            :value="brand" />
        </el-select>
      </el-form-item>
      <el-form-item label="型号">
        <el-select
          v-model="query.modelId"
          clearable
          filterable
          placeholder="全部型号"
          :disabled="query.mode === 'package'"
          @change="load">
          <el-option
            v-for="item in models"
            :key="item.modelId"
            :label="item.modelName"
            :value="item.modelId" />
        </el-select>
      </el-form-item>
      <el-form-item label="展示天数">
        <el-select
          v-model="query.days"
          placeholder="请选择展示天数"
          style="width: 140px"
          @change="load">
          <el-option
            v-for="days in dayOptions"
            :key="days"
            :label="`${days} 天`"
            :value="days" />
        </el-select>
      </el-form-item>
    </el-form>
    <el-row class="mb8" justify="space-between" align="middle">
      <el-col :span="12">
        <h2 class="page-title">未来 {{ query.days }} 天库存概况</h2>
      </el-col>
      <el-col :span="12" class="mode-control">
        <el-radio-group v-model="query.mode" @change="load">
          <el-radio-button label="model">按型号</el-radio-button>
          <el-radio-button label="package">按套餐</el-radio-button>
        </el-radio-group>
      </el-col>
    </el-row>
    <section class="chart-panel" aria-label="库存趋势图">
      <div class="chart-panel__heading">
        <div>
          <h3>库存趋势图</h3>
          <p>按当前筛选条件展示各型号或套餐的可用库存变化</p>
        </div>
      </div>
      <TrendLineChart
        :categories="chartCategories"
        :series="chartSeries"
        y-axis-name="可用库存"
        empty-text="所选条件下暂无库存趋势数据" />
    </section>
    <el-table
      v-loading="loading"
      :data="displayRows"
      border
      class="overview-table">
      <el-table-column
        :label="query.mode === 'model' ? '型号' : '套餐名称'"
        prop="itemName"
        fixed="left"
        min-width="220" />
      <el-table-column
        v-if="query.mode === 'model'"
        label="品牌"
        prop="brand"
        width="140"
        fixed="left" />
      <el-table-column
        v-if="query.mode === 'package'"
        label="套餐编码"
        prop="itemCode"
        width="180"
        fixed="left" />
      <el-table-column
        v-if="query.mode === 'model'"
        label="当前总库存"
        prop="totalQuantity"
        width="130"
        align="center"
        fixed="left" />
      <el-table-column
        v-for="date in forecastDates"
        :key="date"
        :label="formatDate(date)"
        width="120"
        align="center">
        <template #default="scope">
          <span :class="{ 'low-stock': scope.row.forecast[date] === 0 }">
            {{ scope.row.forecast[date] ?? 0 }}
          </span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { computed, onActivated, onMounted, reactive, ref } from "vue";
import { listMerchantWarehouses } from "@/api/merchant/rentalBusiness";
import { listMerchantInventoryOptions } from "@/api/merchant/inventory";
import { listMerchantInventoryOverview } from "@/api/merchant/rentalBusiness";
import { getMerchantStoreSettings } from "@/api/merchant/storeSettings";
import TrendLineChart from "@/components/TrendLineChart.vue";

const loading = ref(false);
const optionsLoading = ref(false);
const rows = ref([]);
const dayOptions = [3, 5, 7, 15, 30];
const warehouses = ref([]);
const allOptions = ref([]);
const query = reactive({
  mode: "model",
  warehouseId: undefined,
  brand: "",
  modelId: undefined,
  days: 30,
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
const forecastDates = computed(() => {
  const dates = new Set();
  rows.value.forEach((row) => dates.add(dateKey(row.forecastDate)));
  return [...dates].sort();
});
const displayRows = computed(() => {
  const grouped = new Map();
  rows.value.forEach((row) => {
    const id = query.mode === "model" ? row.modelId : row.packageId;
    const key = `${query.mode}:${id ?? row.itemName}`;
    if (!grouped.has(key)) {
      grouped.set(key, {
        itemName: row.itemName,
        brand: row.brand,
        itemCode: row.itemCode,
        totalQuantity: row.totalQuantity,
        forecast: {},
      });
    }
    grouped.get(key).forecast[dateKey(row.forecastDate)] =
      row.availableQuantity;
  });
  return [...grouped.values()];
});
const chartCategories = computed(() => forecastDates.value.map(formatDate));
const chartSeries = computed(() =>
  displayRows.value.map((row) => ({
    name: row.brand
      ? `${row.brand} · ${row.itemName || "未命名"}`
      : row.itemName || "未命名",
    data: forecastDates.value.map((date) => row.forecast[date] ?? 0),
  })),
);

function dateKey(value) {
  if (!value) return "";
  return String(value).slice(0, 10);
}
function formatDate(value) {
  const key = dateKey(value);
  return key ? key.slice(5).replace("-", "/") : "-";
}
function load() {
  loading.value = true;
  listMerchantInventoryOverview(query)
    .then(({ rows: data }) => {
      rows.value = data || [];
    })
    .finally(() => {
      loading.value = false;
    });
}
function changeBrand() {
  if (
    !models.value.some((item) => Number(item.modelId) === Number(query.modelId))
  )
    query.modelId = undefined;
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
      settingsResult.value?.data?.settings?.inventoryOverviewDays,
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
.page-heading {
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}
.page-kicker {
  margin: 0 0 4px;
  color: var(--el-color-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.page-heading h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 650;
  letter-spacing: 0;
}

.page-description {
  margin: 7px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.heading-actions,
.list-toolbar-actions,
.compact-action-bar {
  gap: 12px;
}

.inventory-overview-page {
  width: 100%;
}
.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}
.mode-control {
  text-align: right;
}
.overview-table {
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
.low-stock {
  color: var(--el-color-danger);
  font-weight: 600;
}
</style>
