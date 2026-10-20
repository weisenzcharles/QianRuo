<template>
  <div class="app-container merchant-list-page verification-desk-page">
    <header class="page-heading">
      <div>
        <p class="page-kicker">VERIFICATION DESK</p>
        <h2>{{ isInbound ? "扫码入库" : "扫码出库" }}</h2>
        <p class="page-description">
          {{
            isInbound
              ? "扫描设备或库存单元，快速完成归还、调货和新增库存入库。"
              : "按订单扫描并绑定设备，确认无误后批量完成出库。"
          }}
        </p>
      </div>
      <div class="heading-actions">
        <el-button
          text
          :icon="Refresh"
          :loading="pageLoading"
          @click="refreshAll">
          刷新
        </el-button>
        <el-radio-group
          v-model="currentMode"
          class="mode-tabs"
          @change="switchMode">
          <el-radio-button value="inbound">扫码入库</el-radio-button>
          <el-radio-button value="outbound">扫码出库</el-radio-button>
        </el-radio-group>
      </div>
    </header>

    <section class="verification-context" aria-label="核销台上下文">
      <div class="context-item context-warehouse">
        <span class="context-label">当前仓库</span>
        <el-select
          v-model="selectedWarehouseId"
          filterable
          placeholder="请选择仓库"
          @change="handleWarehouseChange">
          <el-option
            v-for="item in warehouses"
            :key="item.id"
            :label="warehouseLabel(item)"
            :value="item.id" />
        </el-select>
      </div>
      <span class="context-divider" aria-hidden="true" />
      <div class="context-item">
        <span class="context-label">扫描方式</span>
        <el-radio-group v-model="scanMethod" size="small">
          <el-radio-button value="gun">扫码枪</el-radio-button>
          <el-radio-button value="camera">摄像头</el-radio-button>
        </el-radio-group>
      </div>
      <span class="context-date">{{ currentDateText }}</span>
    </section>

    <section class="verification-stats" aria-label="核销台统计">
      <div class="stat-card pending-stat">
        <span>待提交</span>
        <strong>{{ stats.pending }}</strong>
      </div>
      <div class="stat-card completed-stat">
        <span>本次已完成</span>
        <strong>{{ stats.completed }}</strong>
      </div>
      <div class="stat-card">
        <span>当前仓库可用设备</span>
        <strong>{{ stats.available }}</strong>
      </div>
      <div class="stat-card error-stat">
        <span>异常记录</span>
        <strong>{{ stats.errors }}</strong>
      </div>
    </section>

    <section v-if="!isInbound" class="panel outbound-order-panel">
      <div class="panel-header">
        <div>
          <h3>出库订单</h3>
          <span>必须先选择订单，再绑定设备</span>
        </div>
        <span v-if="selectedOutboundOrder" type="primary" effect="plain">
          {{ outboundProgressText }}
        </span>
      </div>
      <div class="order-summary-form">
        <el-form label-position="top">
          <el-form-item label="出库订单" required>
            <el-select
              v-model="selectedOutboundOrderId"
              filterable
              clearable
              placeholder="请选择已备货订单"
              @change="handleOutboundOrderChange">
              <el-option
                v-for="item in outboundOrders"
                :key="item.id"
                :label="orderLabel(item)"
                :value="item.id" />
            </el-select>
          </el-form-item>
        </el-form>
        <div v-if="selectedOutboundOrder" class="summary-item">
          <span>产品套餐</span>
          <strong>
            {{ selectedOutboundOrder.packageName || "未设置套餐" }}
          </strong>
        </div>
        <div v-if="selectedOutboundOrder" class="summary-item">
          <span>租期</span>
          <strong>{{ selectedOutboundOrder.rentalPeriod || "-" }}</strong>
        </div>
        <div v-if="selectedOutboundOrder" class="summary-item">
          <span>归还仓库</span>
          <strong>
            {{ selectedOutboundOrder.returnWarehouseName || "-" }}
          </strong>
        </div>
        <div v-if="selectedOutboundOrder" class="required-progress">
          <div class="progress-line">
            <span>设备绑定进度</span>
            <strong>
              {{ selectedScannedDeviceCount }} / {{ requiredDeviceCount }}
            </strong>
          </div>
          <el-progress
            :percentage="outboundProgress"
            :show-text="false"
            :stroke-width="6"
            status="success" />
        </div>
        <div v-else class="verification-empty outbound-order-empty" role="status">
          请选择已备货订单
        </div>
      </div>
    </section>

    <div class="workbench-grid">
      <section class="panel scanner-panel">
        <div class="panel-header">
          <div>
            <h3>{{ isInbound ? "扫描入库设备" : "扫描出库设备" }}</h3>
            <span>扫描后先校验，提交时才改变库存状态</span>
          </div>
          <span class="auto-identification-label">自动识别</span>
        </div>
        <div class="panel-body">
          <el-form label-position="top">
            <el-form-item v-if="isInbound" label="入库来源" required>
              <el-select
                v-model="selectedSource"
                placeholder="请选择入库来源"
                @change="handleSourceChange">
                <el-option
                  v-for="item in sourceOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item
              v-if="isInbound && selectedSource === sourceCodes.NEW_STOCK"
              label="入库商品"
              required>
              <el-select
                v-model="selectedProductId"
                filterable
                clearable
                placeholder="请选择逐件库存商品">
                <el-option
                  v-for="item in productOptions"
                  :key="item.id"
                  :label="productLabel(item)"
                  :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item
              v-if="isInbound && selectedSource !== sourceCodes.NEW_STOCK"
              label="关联订单">
              <el-select
                v-model="selectedInboundOrderId"
                filterable
                clearable
                placeholder="请选择订单（可选）">
                <el-option
                  v-for="item in inboundOrders"
                  :key="item.id"
                  :label="orderLabel(item)"
                  :value="item.id" />
              </el-select>
              <div class="field-helper">
                归还入库可关联订单，系统会校验当前仓库和设备状态。
              </div>
            </el-form-item>
          </el-form>

          <div
            class="scanner-zone"
            :class="{ 'is-camera-mode': scanMethod === 'camera' }"
            role="button"
            tabindex="0"
            @click="focusScanInput"
            @keydown.enter.prevent="focusScanInput">
            <Camera class="scanner-icon" />
            <strong>
              {{ scanMethod === "camera" ? "摄像头输入模式" : "等待扫描" }}
            </strong>
            <span>
              {{
                scanMethod === "camera"
                  ? "请使用下方输入框接收摄像头或扫码设备结果"
                  : "将序列号对准扫描区域"
              }}
            </span>
            <small>也可以使用下方输入框录入</small>
            <el-icon class="scanner-corner scanner-corner-left">
              <FullScreen />
            </el-icon>
            <el-icon class="scanner-corner scanner-corner-right">
              <FullScreen />
            </el-icon>
          </div>

          <div class="scan-input-row">
            <el-input
              ref="scanInputRef"
              v-model="scanCodeValue"
              clearable
              autofocus
              :placeholder="
                isInbound ? '扫描或输入库存单元编码' : '扫描或输入设备序列号'
              "
              @keyup.enter="scanCode">
              <template #prefix><Search /></template>
            </el-input>
            <el-button
              type="primary"
              :loading="scanning"
              :icon="Check"
              @click="scanCode">
              {{ isInbound ? "确认识别" : "绑定设备" }}
            </el-button>
          </div>
          <div class="scanner-actions">
            <el-button :icon="Search" @click="focusScanInput">
              手动录入
            </el-button>
            <el-button
              type="danger"
              plain
              :icon="Delete"
              :disabled="!scannedRows.length"
              @click="clearScans">
              清空本批
            </el-button>
          </div>
          <el-alert
            :title="operationHint"
            type="info"
            :closable="false"
            show-icon
            class="operation-hint" />
        </div>
      </section>

      <section class="panel result-panel">
        <div class="panel-header">
          <div>
            <h3>{{ isInbound ? "本批入库清单" : "本批出库清单" }}</h3>
            <span>通过校验后才可提交</span>
          </div>
          <div class="result-tools">
            <span>已识别 {{ scannedRows.length }} 件</span>
            <el-button text :icon="Refresh" @click="refreshCurrentData">
              刷新
            </el-button>
          </div>
        </div>
        <el-table
          v-loading="scanning"
          :data="scannedRows"
          class="scan-result-table"
          row-key="rowKey">
          <el-table-column label="设备 / 配件" min-width="210">
            <template #default="{ row }">
              <strong class="product-name">
                {{ row.productName || "未识别商品" }}
              </strong>
              <span class="product-meta">
                {{ row.modelName || row.productTypeLabel || "-" }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="库存编码" min-width="160">
            <template #default="{ row }">
              {{ row.unitCode || row.code || "-" }}
            </template>
          </el-table-column>
          <el-table-column label="序列号" min-width="170">
            <template #default="{ row }">
              {{ row.serialNumber || "-" }}
            </template>
          </el-table-column>
          <el-table-column label="当前状态" width="110">
            <template #default="{ row }">{{ row.statusLabel || "-" }}</template>
          </el-table-column>
          <el-table-column label="校验结果" width="110" align="center">
            <template #default="{ row }">
              <el-tag type="success" effect="plain">
                {{ row.validationLabel || (isInbound ? "可入库" : "可出库") }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }">
              <el-button
                link
                type="danger"
                :icon="Delete"
                title="移除"
                @click="removeScan($index)" />
            </template>
          </el-table-column>
          <template #empty>
            <div class="verification-empty scan-result-empty" role="status">
              本批还没有扫描记录
            </div>
          </template>
        </el-table>
        <div class="result-footer">
          <span>
            通过校验
            <strong>{{ scannedRows.length }}</strong>
            件，异常
            <strong>{{ stats.errors }}</strong>
            件
          </span>
          <el-button
            :type="isInbound ? 'primary' : 'success'"
            :icon="isInbound ? Upload : Download"
            :loading="committing"
            :disabled="!canCommit"
            @click="commitBatch">
            {{ isInbound ? "确认入库" : "确认出库" }}
          </el-button>
        </div>
      </section>
    </div>

    <section class="recent-section">
      <div class="section-heading">
        <div>
          <h3>最近操作</h3>
          <span>仅展示本次工作台会话的操作结果</span>
        </div>
        <el-button
          v-if="recentOperations.length"
          text
          type="danger"
          :icon="Delete"
          @click="clearRecentOperations">
          清空记录
        </el-button>
      </div>
      <div class="panel recent-panel">
        <el-table :data="recentOperations" row-key="operationKey">
          <el-table-column label="时间" prop="time" width="180" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">{{ row.operationLabel }}</template>
          </el-table-column>
          <el-table-column label="设备 / 订单" min-width="220">
            <template #default="{ row }">
              <span>{{ row.subject }}</span>
              <span v-if="row.orderNo" class="table-secondary">
                订单 {{ row.orderNo }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="结果" width="100">
            <template #default="{ row }">
              <el-tag :type="row.success ? 'success' : 'danger'" effect="plain">
                {{ row.success ? "成功" : "异常" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="说明" min-width="260" prop="message" />
          <template #empty>
            <el-empty description="暂无操作记录" :image-size="64" />
          </template>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  Camera,
  Check,
  Delete,
  Download,
  FullScreen,
  Refresh,
  Search,
  Upload,
} from "@element-plus/icons-vue";
import {
  listMerchantInventory,
  listMerchantInventoryOptions,
} from "@/api/merchant/inventory";
import { listMerchantOrders } from "@/api/merchant/order";
import {
  commitMerchantVerificationInbound,
  commitMerchantVerificationOutbound,
  scanMerchantVerification,
} from "@/api/merchant/verification";
import { listMerchantWarehouses } from "@/api/merchant/rentalBusiness";
import { createIdempotencyKey } from "@/utils/idempotency";
import { RENTAL_CODE } from "@/utils/rental/options";

const props = defineProps({
  mode: { type: String, required: true },
});

const router = useRouter();
const currentMode = ref(props.mode);
const pageLoading = ref(false);
const scanning = ref(false);
const committing = ref(false);
const scanInputRef = ref();
const scanMethod = ref("gun");
const scanCodeValue = ref("");
const selectedWarehouseId = ref();
const selectedSource = ref("rental-return");
const selectedProductId = ref();
const selectedInboundOrderId = ref();
const selectedOutboundOrderId = ref();
const warehouses = ref([]);
const productOptions = ref([]);
const inboundOrders = ref([]);
const outboundOrders = ref([]);
const scannedRows = ref([]);
const recentOperations = ref([]);
const stats = reactive({ pending: 0, completed: 0, available: 0, errors: 0 });

const sourceCodes = Object.freeze({
  RENTAL_RETURN: "rental-return",
  ALLIANCE_RETURN: "alliance-return",
  REPAIR_RETURN: "repair-return",
  NEW_STOCK: "new-stock",
});
const sourceOptions = [
  { value: sourceCodes.RENTAL_RETURN, label: "租赁归还" },
  { value: sourceCodes.ALLIANCE_RETURN, label: "联盟调货入库" },
  { value: sourceCodes.REPAIR_RETURN, label: "维修归还" },
  { value: sourceCodes.NEW_STOCK, label: "采购入库" },
];

const isInbound = computed(() => currentMode.value === "inbound");
const currentDateText = computed(() => {
  const now = new Date();
  const date = now.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
  const weekday = now.toLocaleDateString("zh-CN", { weekday: "long" });
  return `${date} ${weekday}`;
});
const selectedOutboundOrder = computed(() =>
  outboundOrders.value.find(
    (item) => item.id === selectedOutboundOrderId.value,
  ),
);
const requiredDeviceCount = computed(() => {
  const items = selectedOutboundOrder.value?.packageDetail?.items;
  if (!Array.isArray(items)) return 0;
  return items.reduce((total, item) => {
    const role = Number(item.itemRole ?? item.item_role);
    const stockMode = Number(item.stockMode ?? item.stock_mode);
    const selectable =
      item.selectableDevice === true ||
      ((role === 1 || role === 4) &&
        stockMode === RENTAL_CODE.STOCK_MODE.SERIAL);
    return selectable ? total + Math.max(1, Number(item.quantity) || 1) : total;
  }, 0);
});
const selectedScannedDeviceCount = computed(() => scannedRows.value.length);
const outboundProgress = computed(() => {
  if (!requiredDeviceCount.value) return 0;
  return Math.min(
    100,
    Math.round(
      (selectedScannedDeviceCount.value / requiredDeviceCount.value) * 100,
    ),
  );
});
const outboundProgressText = computed(
  () => `${selectedScannedDeviceCount.value} / ${requiredDeviceCount.value} 件`,
);
const canCommit = computed(() => {
  if (!scannedRows.value.length || committing.value) return false;
  if (!selectedWarehouseId.value) return false;
  if (!isInbound.value) {
    return (
      Boolean(selectedOutboundOrderId.value) &&
      requiredDeviceCount.value > 0 &&
      scannedRows.value.length === requiredDeviceCount.value
    );
  }
  return selectedSource.value === sourceCodes.NEW_STOCK
    ? Boolean(selectedProductId.value)
    : true;
});
const operationHint = computed(() => {
  if (!selectedWarehouseId.value) return "请先选择当前操作仓库。";
  if (!isInbound.value && !selectedOutboundOrderId.value)
    return "出库前请先选择一个已备货订单。";
  if (
    !isInbound.value &&
    selectedOutboundOrder.value &&
    !requiredDeviceCount.value
  )
    return "当前套餐没有可核销的逐件设备，请使用普通发货流程。";
  if (isInbound.value && selectedSource.value === sourceCodes.NEW_STOCK)
    return "采购入库只支持逐件库存商品，提交后会生成新的库存编码。";
  if (isInbound.value)
    return "仅可入库到当前仓库，重复扫描、已占用或状态异常的设备会被拦截。";
  return "设备必须属于当前订单套餐，并且只能绑定当前发货仓库中可用或本订单预占的设备。";
});

watch(
  () => props.mode,
  (value) => {
    if (value === "inbound" || value === "outbound") {
      currentMode.value = value;
      resetBatch();
      refreshCurrentData();
    }
  },
);

onMounted(() => {
  refreshAll();
});

function responsePayload(response) {
  const body = response?.data ?? response;
  return body?.data ?? body;
}

function responseRows(response) {
  const body = responsePayload(response);
  if (Array.isArray(body)) return body;
  return Array.isArray(body?.rows) ? body.rows : [];
}

async function refreshAll() {
  pageLoading.value = true;
  try {
    await loadWarehouses();
    await Promise.all([loadProducts(), loadOrders(), loadAvailableInventory()]);
  } finally {
    pageLoading.value = false;
    await focusScanInput();
  }
}

async function refreshCurrentData() {
  await Promise.all([loadOrders(), loadAvailableInventory()]);
}

async function loadWarehouses() {
  try {
    const rows = responseRows(
      await listMerchantWarehouses({ pageNum: 1, pageSize: 1000, status: 0 }),
    );
    warehouses.value = rows.map(normalizeWarehouse).filter((item) => item.id);
    if (!selectedWarehouseId.value && warehouses.value.length)
      selectedWarehouseId.value = warehouses.value[0].id;
    if (
      selectedWarehouseId.value &&
      !warehouses.value.some((item) => item.id === selectedWarehouseId.value)
    )
      selectedWarehouseId.value = warehouses.value[0]?.id;
  } catch {
    warehouses.value = [];
    selectedWarehouseId.value = undefined;
  }
}

async function loadProducts() {
  try {
    const rows = responseRows(await listMerchantInventoryOptions({}));
    productOptions.value = rows
      .map(normalizeProduct)
      .filter(
        (item) => item.id && item.stockMode === RENTAL_CODE.STOCK_MODE.SERIAL,
      );
  } catch {
    productOptions.value = [];
  }
}

async function loadOrders() {
  try {
    const preparedQuery = {
      pageNum: 1,
      pageSize: 1000,
      status: RENTAL_CODE.ORDER_STATUS.PREPARED,
    };
    if (selectedWarehouseId.value) {
      preparedQuery.warehouseId = selectedWarehouseId.value;
    }
    const [allResponse, preparedResponse] = await Promise.all([
      listMerchantOrders({ pageNum: 1, pageSize: 1000 }),
      listMerchantOrders(preparedQuery),
    ]);
    inboundOrders.value = responseRows(allResponse)
      .map(normalizeOrder)
      .filter((item) => item.id);
    outboundOrders.value = responseRows(preparedResponse)
      .map(normalizeOrder)
      .filter((item) => item.id);
    if (
      selectedOutboundOrderId.value &&
      !outboundOrders.value.some(
        (item) => item.id === selectedOutboundOrderId.value,
      )
    )
      selectedOutboundOrderId.value = undefined;
  } catch {
    inboundOrders.value = [];
    outboundOrders.value = [];
  }
}

async function loadAvailableInventory() {
  if (!selectedWarehouseId.value) {
    stats.available = 0;
    return;
  }
  try {
    const rows = responseRows(
      await listMerchantInventory({
        pageNum: 1,
        pageSize: 1000,
        warehouseId: selectedWarehouseId.value,
        productType: RENTAL_CODE.PRODUCT_TYPE.DEVICE,
      }),
    );
    stats.available = rows.reduce(
      (total, item) => total + Math.max(0, Number(item.availableQuantity) || 0),
      0,
    );
  } catch {
    stats.available = 0;
  }
}

function normalizeWarehouse(item) {
  return {
    id: item.id ?? item.warehouseId,
    code: item.code ?? item.warehouseCode ?? "",
    name: item.name ?? item.warehouseName ?? "未命名仓库",
  };
}

function normalizeProduct(item) {
  return {
    id: item.productId ?? item.id,
    code: item.productCode ?? item.code ?? "",
    name: item.productName ?? item.name ?? "未命名商品",
    modelName: item.modelName,
    brand: item.brand,
    productType: Number(item.productType),
    stockMode: Number(item.stockMode),
  };
}

function normalizeOrder(item) {
  const packageDetail = item.packageDetail || {};
  const start = item.leaseStartDate || "";
  const end = item.leaseEndDate || "";
  return {
    id: item.orderId ?? item.id,
    orderNo: item.orderNo || item.orderCode || "-",
    customerName:
      item.recipientName || item.customer?.name || item.customerName || "-",
    packageName: item.packageName || packageDetail.packageName || "-",
    rentalPeriod:
      item.rentalPeriod || (start && end ? `${start} 至 ${end}` : "-"),
    returnDate: item.returnDate || item.returnDueTime || end || "-",
    returnWarehouseName:
      item.returnWarehouseName ||
      item.returnWarehouse ||
      item.shipping?.returnWarehouse ||
      "-",
    packageDetail,
    warehouseId: item.warehouseId,
  };
}

function warehouseLabel(item) {
  return item.code ? `${item.name}` : item.name;
}

function productLabel(item) {
  const type =
    item.productType === RENTAL_CODE.PRODUCT_TYPE.ACCESSORY ? "配件" : "设备";
  return `${type} | ${item.name}${item.code ? `（${item.code}）` : ""}${item.modelName ? ` | ${item.modelName}` : ""}`;
}

function orderLabel(item) {
  return `${item.orderNo} · ${item.customerName}${item.packageName && item.packageName !== "-" ? ` · ${item.packageName}` : ""}`;
}

async function handleWarehouseChange() {
  selectedOutboundOrderId.value = undefined;
  resetBatch();
  outboundOrders.value = [];
  await Promise.all([loadOrders(), loadAvailableInventory()]);
}

function handleSourceChange() {
  selectedProductId.value = undefined;
  selectedInboundOrderId.value = undefined;
  resetBatch();
}

function handleOutboundOrderChange() {
  resetBatch();
}

function switchMode(value) {
  if (value !== "inbound" && value !== "outbound") return;
  currentMode.value = value;
  resetBatch();
  const path =
    value === "inbound"
      ? "/merchant/verification/check-in"
      : "/merchant/verification/check-out";
  if (router.currentRoute.value.path !== path) router.push(path);
  refreshCurrentData();
}

async function focusScanInput() {
  await nextTick();
  scanInputRef.value?.focus?.();
}

async function scanCode() {
  const code = scanCodeValue.value.trim();
  if (!code) {
    ElMessage.warning("请扫描或输入库存编码");
    return focusScanInput();
  }
  if (!selectedWarehouseId.value) return ElMessage.warning("请先选择当前仓库");
  if (!isInbound.value && !selectedOutboundOrderId.value)
    return ElMessage.warning("请先选择出库订单");
  if (
    isInbound.value &&
    selectedSource.value === sourceCodes.NEW_STOCK &&
    !selectedProductId.value
  )
    return ElMessage.warning("请选择入库商品");
  if (
    scannedRows.value.some((row) =>
      [row.code, row.unitCode, row.serialNumber].filter(Boolean).includes(code),
    )
  ) {
    recordFailure(code, "本批已扫描过该设备");
    ElMessage.warning("本批已扫描过该设备");
    return;
  }
  scanning.value = true;
  try {
    const result = responsePayload(
      await scanMerchantVerification({
        mode: currentMode.value,
        source: isInbound.value ? selectedSource.value : undefined,
        orderId: isInbound.value
          ? selectedInboundOrderId.value
          : selectedOutboundOrderId.value,
        warehouseId: selectedWarehouseId.value,
        productId:
          isInbound.value && selectedSource.value === sourceCodes.NEW_STOCK
            ? selectedProductId.value
            : undefined,
        code,
      }),
    );
    if (!result?.valid) {
      const message = result?.validationLabel || "设备校验未通过";
      recordFailure(code, message);
      ElMessage.warning(message);
      return;
    }
    scannedRows.value.push({
      ...result,
      code,
      rowKey: `${code}-${Date.now()}-${scannedRows.value.length}`,
    });
    stats.pending = scannedRows.value.length;
    scanCodeValue.value = "";
  } catch {
    recordFailure(code, "扫码校验失败");
  } finally {
    scanning.value = false;
    await focusScanInput();
  }
}

async function commitBatch() {
  if (!canCommit.value) {
    if (!scannedRows.value.length) return ElMessage.warning("请先扫描设备");
    if (!isInbound.value && !requiredDeviceCount.value)
      return ElMessage.warning(
        "当前套餐没有可核销的逐件设备，请使用普通发货流程",
      );
    if (
      !isInbound.value &&
      requiredDeviceCount.value &&
      scannedRows.value.length !== requiredDeviceCount.value
    )
      return ElMessage.warning(
        `还需要扫描 ${requiredDeviceCount.value - scannedRows.value.length} 件设备`,
      );
    return ElMessage.warning("请先完成必填选择");
  }
  committing.value = true;
  const rows = [...scannedRows.value];
  const command = {
    mode: currentMode.value,
    source: isInbound.value ? selectedSource.value : undefined,
    orderId: isInbound.value
      ? selectedInboundOrderId.value
      : selectedOutboundOrderId.value,
    warehouseId: selectedWarehouseId.value,
    productId:
      isInbound.value && selectedSource.value === sourceCodes.NEW_STOCK
        ? selectedProductId.value
        : undefined,
    codes: rows.map((item) => item.code),
    idempotencyKey: createIdempotencyKey(),
  };
  try {
    const result = responsePayload(
      isInbound.value
        ? await commitMerchantVerificationInbound(command)
        : await commitMerchantVerificationOutbound(command),
    );
    const successCount = Number(result?.successCount) || rows.length;
    stats.completed += successCount;
    rows.forEach((row) =>
      addRecentOperation({
        success: true,
        operationLabel: isInbound.value ? "入库" : "出库",
        subject: row.serialNumber || row.unitCode || row.code,
        orderNo: result?.orderNo || row.orderNo,
        message:
          result?.message ||
          (isInbound.value ? "扫码入库成功" : "扫码出库成功"),
      }),
    );
    resetBatch();
    await refreshCurrentData();
    ElMessage.success(
      result?.message || (isInbound.value ? "扫码入库成功" : "扫码出库成功"),
    );
  } catch {
    rows.forEach((row) =>
      addRecentOperation({
        success: false,
        operationLabel: isInbound.value ? "入库" : "出库",
        subject: row.serialNumber || row.unitCode || row.code,
        orderNo: row.orderNo,
        message: "提交失败，请根据提示处理后重试",
      }),
    );
  } finally {
    committing.value = false;
    await focusScanInput();
  }
}

function removeScan(index) {
  scannedRows.value.splice(index, 1);
  stats.pending = scannedRows.value.length;
  focusScanInput();
}

function resetBatch() {
  scannedRows.value = [];
  scanCodeValue.value = "";
  stats.pending = 0;
}

function clearScans() {
  resetBatch();
  focusScanInput();
}

function recordFailure(code, message) {
  stats.errors += 1;
  addRecentOperation({
    success: false,
    operationLabel: isInbound.value ? "入库" : "出库",
    subject: code,
    message,
  });
}

function addRecentOperation(operation) {
  recentOperations.value.unshift({
    ...operation,
    time: new Date().toLocaleString("zh-CN", { hour12: false }),
    operationKey: `${Date.now()}-${Math.random()}`,
  });
  recentOperations.value = recentOperations.value.slice(0, 30);
}

function clearRecentOperations() {
  recentOperations.value = [];
}
</script>

<style scoped>
.verification-desk-page {
  min-width: 0;
  padding: 20px 24px 32px;
  color: var(--el-text-color-primary);
}

.page-heading,
.heading-actions,
.verification-context,
.context-item,
.panel-header,
.result-tools,
.result-footer,
.section-heading,
.progress-line {
  display: flex;
  align-items: center;
}

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
}

.page-description {
  margin: 7px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.heading-actions,
.result-tools {
  gap: 12px;
}

.mode-tabs {
  padding: 3px;
  border-radius: 6px;
  background: var(--el-bg-color-overlay);
}

.verification-context {
  gap: 18px;
  min-height: 62px;
  margin-bottom: 16px;
  padding: 12px 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-bg-color);
}

.context-item {
  gap: 10px;
}

.context-label,
.context-date {
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.context-warehouse .el-select {
  width: 230px;
}

.context-divider {
  width: 1px;
  height: 24px;
  background: var(--el-border-color-light);
}

.context-date {
  margin-left: auto;
}

.verification-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 74px;
  padding: 14px 18px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-bg-color);
}

.stat-card span {
  color: var(--el-text-color-secondary);
}

.stat-card strong {
  font-size: 25px;
  font-weight: 650;
}

.pending-stat strong {
  color: var(--el-color-warning);
}

.completed-stat strong {
  color: var(--el-color-success);
}

.error-stat strong {
  color: var(--el-color-danger);
}

.panel {
  overflow: hidden;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-bg-color);
}

.panel-header {
  justify-content: space-between;
  gap: 12px;
  padding: 15px 18px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.panel-header h3,
.section-heading h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 650;
}

.panel-header span,
.section-heading span {
  display: block;
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.panel-header .auto-identification-label {
  margin-top: 0;
  white-space: nowrap;
}

.outbound-order-panel {
  margin-bottom: 16px;
}

.order-summary-form {
  display: grid;
  grid-template-columns: minmax(280px, 1.3fr) repeat(3, minmax(150px, 1fr));
  gap: 18px;
  align-items: end;
  padding: 16px 18px;
}

.order-summary-form > .el-form {
  width: 100%;
  min-width: 0;
}

.order-summary-form .el-form-item {
  margin-bottom: 0;
  width: 100%;
}

.order-summary-form .el-select {
  width: 100%;
}

.verification-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
  text-align: center;
}

.outbound-order-empty {
  grid-column: 1 / -1;
  min-height: 50px;
  box-sizing: border-box;
  padding: 14px 16px;
  border-top: 1px solid var(--el-border-color-light);
}

.summary-item span {
  display: block;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.summary-item strong {
  display: block;
  margin-top: 5px;
  color: var(--el-text-color-primary);
  font-size: 15px;
  font-weight: 500;
}

.required-progress {
  grid-column: 1 / -1;
  min-height: 50px;
  box-sizing: border-box;
  display: grid;
  grid-template-columns: minmax(280px, 1.3fr) repeat(3, minmax(150px, 1fr));
  grid-template-rows: auto auto;
  column-gap: 18px;
  row-gap: 8px;
  align-content: start;
  padding-top: 14px;
  border-top: 1px solid var(--el-border-color-light);
}

.progress-line {
  grid-column: 1;
  grid-row: 1;
  justify-content: space-between;
  color: var(--el-text-color-secondary);
}

.progress-line strong {
  color: var(--el-color-success);
}

.required-progress .el-progress {
  grid-column: 1;
  grid-row: 2;
  width: 100%;
  margin-top: 0;
}

.workbench-grid {
  display: grid;
  grid-template-columns: minmax(320px, 380px) minmax(0, 1fr);
  gap: 16px;
}

.panel-body {
  padding: 18px;
}

.panel-body .el-form-item {
  margin-bottom: 14px;
}

.panel-body .el-select,
.panel-body .el-input {
  width: 100%;
}

.field-helper {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
}

.scanner-zone {
  position: relative;
  display: grid;
  min-height: 205px;
  place-content: center;
  gap: 7px;
  overflow: hidden;
  border: 1px dashed var(--el-color-primary-light-3);
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
  color: var(--el-text-color-secondary);
  text-align: center;
  cursor: text;
}

.scanner-zone.is-camera-mode {
  border-color: var(--el-color-warning-light-3);
  background: var(--el-color-warning-light-9);
}

.scanner-zone strong {
  color: var(--el-text-color-primary);
  font-size: 18px;
  font-weight: 500;
}

.scanner-zone small {
  color: var(--el-text-color-placeholder);
}

.scanner-icon {
  width: 38px;
  height: 38px;
  margin: 0 auto 4px;
  color: var(--el-color-primary);
}

.scanner-corner {
  position: absolute;
  width: 25px;
  height: 25px;
  color: var(--el-color-primary);
}

.scanner-corner-left {
  top: 16px;
  left: 16px;
}

.scanner-corner-right {
  right: 16px;
  bottom: 16px;
}

.scan-input-row {
  display: flex;
  gap: 8px;
  margin-top: 14px;
}

.scan-input-row .el-input {
  min-width: 0;
}

.scanner-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.scanner-actions .el-button {
  flex: 1;
}

.operation-hint {
  margin-top: 16px;
}

.result-panel {
  min-width: 0;
}

.result-tools > span {
  display: inline-flex;
  align-items: center;
  height: 32px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  top:-2px;
  line-height: 1;
}

.result-tools :deep(.el-button) {
  display: inline-flex;
  align-items: center;
  height: 32px;
  margin: 0;
  line-height: 1;
}

.scan-result-table :deep(.el-table__empty-block),
.recent-panel :deep(.el-table__empty-block) {
  min-height: 180px;
}

.scan-result-empty {
  width: 100%;
  min-height: 180px;
}

.product-name,
.product-meta,
.table-secondary {
  display: block;
}

.product-name {
  color: var(--el-text-color-primary);
  font-weight: 550;
}

.product-meta,
.table-secondary {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.result-footer {
  justify-content: space-between;
  gap: 12px;
  min-height: 64px;
  padding: 12px 18px;
  border-top: 1px solid var(--el-border-color-light);
  color: var(--el-text-color-secondary);
}

.result-footer strong {
  color: var(--el-text-color-primary);
}

.recent-section {
  margin-top: 16px;
}

.section-heading {
  justify-content: space-between;
  margin-bottom: 10px;
}

.recent-panel {
  min-width: 0;
}

@media (max-width: 1180px) {
  .page-heading,
  .verification-context {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .context-date {
    margin-left: 0;
  }

  .workbench-grid {
    grid-template-columns: 1fr;
  }

  .order-summary-form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .required-progress {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .verification-desk-page {
    padding: 16px 12px 24px;
  }

  .page-heading,
  .heading-actions,
  .verification-context,
  .scan-input-row,
  .result-footer {
    align-items: stretch;
    flex-direction: column;
  }

  .heading-actions,
  .mode-tabs,
  .context-item,
  .context-warehouse .el-select {
    width: 100%;
  }

  .context-divider {
    display: none;
  }

  .verification-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .order-summary-form {
    grid-template-columns: 1fr;
  }

  .required-progress {
    grid-column: auto;
    grid-template-columns: 1fr;
  }
}
</style>
