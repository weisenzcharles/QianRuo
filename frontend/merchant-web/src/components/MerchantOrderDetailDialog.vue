<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="min(92vw, 1240px)"
    class="order-detail-dialog"
    append-to-body
    destroy-on-close
    @update:model-value="handleVisibleChange">
    <el-skeleton v-if="detailLoading" :rows="10" animated />
    <template v-else-if="orderDetail">
      <el-descriptions :column="2" border label-width="120">
        <el-descriptions-item label="订单编码">
          {{ orderDetail.orderNo || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="外部单号">
          {{ orderDetail.externalOrderNo || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="orderStatusType(orderDetail.status ?? orderDetail.orderStatus)" effect="light">
            {{ orderStatusLabel(orderDetail.status ?? orderDetail.orderStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单渠道">
          {{ orderDetail.channel || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="来源店铺">
          {{ orderDetail.merchant || orderDetail.shopName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ orderDetail.createdAt || orderDetail.createTime || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="客户姓名">
          {{ orderDetail.customer?.name || orderDetail.customerName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="客户昵称">
          {{ orderDetail.customer?.nickname || orderDetail.customerNickname || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="客户电话">
          {{ orderDetail.customer?.phone || orderDetail.customerPhone || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="收件人">
          {{ orderDetail.recipientName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="收件电话">
          {{ orderDetail.recipientPhone || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">
          {{ orderDetail.recipientAddress || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="客户地址" :span="2">
          {{ orderDetail.customer?.address || orderDetail.customerAddress || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="发货仓库">
          {{ orderDetail.shipping?.warehouse || orderDetail.warehouseName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="归还仓库">
          {{ orderDetail.returnWarehouseName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="租赁时间">
          {{ orderDetail.rentalPeriod || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="租赁天数">
          {{ orderDetail.leaseDays || 0 }} 天
        </el-descriptions-item>
        <el-descriptions-item label="发货日期">
          {{ orderDetail.shipping?.shipDate || orderDetail.shipDueTime || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="发货时段">
          {{ getShipTimeSlotLabel(orderDetail.shipping?.timeSlot || orderDetail.shipTimeSlot) }}
        </el-descriptions-item>
        <el-descriptions-item label="应还日期">
          {{ orderDetail.returnDate || orderDetail.returnDueTime || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="预计到库">
          {{ orderDetail.delivery?.expectedDate || orderDetail.arrivalDate || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="发货方式">
          {{ getDeliveryMethodLabel(orderDetail.shipping?.method || orderDetail.deliveryMethod || orderDetail.shippingMethod) }}
        </el-descriptions-item>
        <el-descriptions-item label="归还方式">
          {{ getDeliveryMethodLabel(orderDetail.returnDeliveryMethod || orderDetail.returnMethod) }}
        </el-descriptions-item>
        <el-descriptions-item label="租金">
          ¥{{ formatOrderAmount(orderDetail.rent ?? orderDetail.rentAmount) }}
        </el-descriptions-item>
        <el-descriptions-item label="押金">
          ¥{{ formatOrderAmount(orderDetail.depositAmount) }}
        </el-descriptions-item>
        <el-descriptions-item label="配送费用">
          ¥{{ formatOrderAmount(orderDetail.deliveryAmount) }}
        </el-descriptions-item>
        <el-descriptions-item label="保险费用">
          ¥{{ formatOrderAmount(orderDetail.insuranceAmount) }}
        </el-descriptions-item>
        <el-descriptions-item label="订单备注" :span="2">
          {{ orderDetail.remark || "-" }}
        </el-descriptions-item>
      </el-descriptions>

      <section class="order-detail-section">
        <h4>商品与套餐</h4>
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="产品型号">
            {{ orderDetail.model || orderDetail.packageDetail?.mainProduct?.model || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="主品编码">
            {{ orderDetail.productCode || orderDetail.packageDetail?.mainProduct?.code || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="套餐名称">
            {{ orderDetail.packageName || orderDetail.packageDetail?.packageName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="数量">
            {{ orderDetail.quantity || 0 }}
          </el-descriptions-item>
        </el-descriptions>
        <el-table
          v-if="packageDetailItems.length"
          :data="packageDetailItems"
          border
          class="order-detail-items-table">
          <el-table-column prop="itemType" label="明细类型" width="130" />
          <el-table-column prop="name" label="产品" min-width="220" />
          <el-table-column prop="model" label="型号" min-width="160" />
          <el-table-column prop="serialNumbers" label="序列号" min-width="220">
            <template #default="{ row }">
              {{ row.serialNumbers?.length ? row.serialNumbers.join("、") : "-" }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
        </el-table>
        <el-empty v-else description="暂无商品明细" :image-size="64" />
      </section>

      <section class="order-detail-section">
        <h4>物流信息</h4>
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="寄出承运商">
            {{ orderDetail.expressCompany ? getExpressCompanyName(orderDetail.expressCompany) : "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="寄出单号">
            {{ orderDetail.trackingNo || orderDetail.expressTrackingNo || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="归还承运商">
            {{ orderDetail.returnExpressCompany ? getExpressCompanyName(orderDetail.returnExpressCompany) : "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="归还单号">
            {{ orderDetail.returnNo || orderDetail.returnExpressTrackingNo || "-" }}
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section class="order-detail-section" v-loading="mediaLoading">
        <h4>履约凭证</h4>
        <el-table v-if="orderMedia.length" :data="orderMedia" border>
          <el-table-column prop="mediaTypeLabel" label="凭证类型" width="140" />
          <el-table-column label="文件" min-width="260">
            <template #default="{ row }">
              <el-link :href="row.fileUrl" target="_blank" type="primary">
                {{ row.fileName || row.fileUrl || "查看文件" }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="登记时间" width="180" />
        </el-table>
        <el-empty v-else description="暂无履约凭证" :image-size="64" />
      </section>

      <section v-if="orderDetail.returnInspection" class="order-detail-section">
        <h4>归还验机</h4>
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="验机结果">
            {{ orderDetail.returnInspection.result || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="验机时间">
            {{ orderDetail.returnInspection.inspectedTime || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="问题描述" :span="2">
            {{ orderDetail.returnInspection.problemDesc || "-" }}
          </el-descriptions-item>
        </el-descriptions>
      </section>
    </template>
    <el-empty v-else description="暂无订单详情" />
    <template #footer>
      <el-button @click="handleVisibleChange(false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import {
  getMerchantOrder,
  listMerchantOrderMedia,
} from "@/api/merchant/order";
import { RENTAL_CODE } from "@/utils/rental/options";
import { getExpressCompanyName as formatExpressCompanyName } from "@/utils/rental/expressCompany";

const DELIVERY_METHOD_OPTIONS = Object.freeze([
  { label: "快递", value: "express" },
  { label: "闪送", value: "flash" },
  { label: "上门", value: "door_to_door" },
]);

const SHIP_TIME_SLOT_OPTIONS = Object.freeze([
  { label: "上午", value: "morning" },
  { label: "下午", value: "afternoon" },
]);

/**
 * 公共订单详情弹窗。调用方只需传入 order 或 orderId，并通过 v-model 控制显示。
 */
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  order: { type: Object, default: null },
  orderId: { type: [Number, String], default: null },
  remote: { type: Boolean, default: true },
  expressCompanyOptions: { type: Array, default: () => [] },
});

const emit = defineEmits(["update:modelValue"]);
const orderDetail = ref(null);
const detailLoading = ref(false);
const orderMedia = ref([]);
const mediaLoading = ref(false);
let requestToken = 0;

const currentOrderId = computed(() => {
  const value = props.orderId ?? props.order?.orderId;
  const orderId = Number(value);
  return Number.isSafeInteger(orderId) && orderId > 0 ? orderId : null;
});

const dialogTitle = computed(() =>
  orderDetail.value?.orderNo
    ? `订单详情：${orderDetail.value.orderNo}`
    : "订单详情",
);

const packageDetailItems = computed(() => {
  const order = orderDetail.value || {};
  const packageItems = Array.isArray(order.packageDetail?.items)
    ? order.packageDetail.items
    : Array.isArray(order.packageItems)
      ? order.packageItems
      : [];
  const items = packageItems.map((item, index) => {
    if (typeof item === "string") {
      return {
        itemType: index === 0 ? "主商品" : "套餐明细",
        name: item || "-",
        model: index === 0 ? order.model || "-" : "-",
        serialNumbers: [],
        quantity: 1,
      };
    }
    return {
      itemType: item?.itemType || "套餐明细",
      name: item?.name || item?.itemName || "-",
      model: item?.model || "-",
      serialNumbers: Array.isArray(item?.serialNumbers)
        ? item.serialNumbers.filter(Boolean)
        : [],
      quantity: Number(item?.quantity) > 0 ? Number(item.quantity) : 1,
    };
  });
  if (!items.length) {
    const mainProduct = order.packageDetail?.mainProduct || {};
    const mainProductName =
      mainProduct.name || order.productName || order.itemName || order.model;
    if (mainProductName && mainProductName !== "-") {
      items.push({
        itemType: "主商品",
        name: mainProductName,
        model: mainProduct.model || order.model || "-",
        serialNumbers: Array.isArray(order.serialNumbers)
          ? order.serialNumbers.filter(Boolean)
          : [],
        quantity: Number(order.quantity) > 0 ? Number(order.quantity) : 1,
      });
    }
  }
  const extraAccessories = Array.isArray(order.packageDetail?.extraAccessories)
    ? order.packageDetail.extraAccessories
    : Array.isArray(order.addOns)
      ? order.addOns
      : [];
  return [
    ...items,
    ...extraAccessories.map((item) => ({
      itemType: "额外配件",
      name: extraAccessoryName(item),
      model: "-",
      serialNumbers: [],
      quantity: extraAccessoryQuantity(item),
    })),
  ];
});

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) loadOrderDetail();
  },
  { immediate: true },
);

watch(
  currentOrderId,
  (orderId, previousOrderId) => {
    if (props.modelValue && orderId !== previousOrderId) loadOrderDetail();
  },
);

watch(
  () => props.order,
  (order) => {
    if (!props.modelValue || !order) return;
    orderDetail.value = {
      ...order,
      ...orderDetail.value,
      packageDetail: mergePackageDetail(
        orderDetail.value?.packageDetail,
        order.packageDetail,
      ),
    };
  },
);

async function loadOrderDetail() {
  const token = ++requestToken;
  const baseOrder = props.order || (currentOrderId.value ? { orderId: currentOrderId.value } : null);
  orderDetail.value = baseOrder;
  orderMedia.value = [];
  if (!baseOrder) return;

  if (props.remote && currentOrderId.value) {
    detailLoading.value = true;
    try {
      const response = await getMerchantOrder(currentOrderId.value);
      if (token !== requestToken) return;
      const detail = response?.data ?? response ?? {};
      const latestOrder = props.order || baseOrder;
      orderDetail.value = {
        ...latestOrder,
        ...detail,
        packageDetail: mergePackageDetail(detail.packageDetail, latestOrder.packageDetail),
      };
    } catch (_error) {
      // 请求拦截器负责提示错误，弹窗保留列表快照作为降级展示。
    } finally {
      if (token === requestToken) detailLoading.value = false;
    }
  }
  loadOrderMedia(currentOrderId.value, token);
}

async function loadOrderMedia(orderId, token) {
  if (!props.remote || !orderId) return;
  mediaLoading.value = true;
  try {
    const response = await listMerchantOrderMedia(orderId);
    if (token === requestToken) orderMedia.value = responseRows(response);
  } catch (_error) {
    if (token === requestToken) orderMedia.value = [];
  } finally {
    if (token === requestToken) mediaLoading.value = false;
  }
}

function responseRows(response) {
  const body = response?.data ?? response;
  const page = body?.data ?? body;
  return Array.isArray(page) ? page : page?.rows || [];
}

function mergePackageDetail(primary, fallback) {
  const primaryDetail = primary && typeof primary === "object" ? primary : {};
  const fallbackDetail = fallback && typeof fallback === "object" ? fallback : {};
  if (!Object.keys(primaryDetail).length && !Object.keys(fallbackDetail).length) {
    return undefined;
  }
  return {
    ...fallbackDetail,
    ...primaryDetail,
    mainProduct: {
      ...(fallbackDetail.mainProduct || {}),
      ...(primaryDetail.mainProduct || {}),
    },
    items:
      Array.isArray(primaryDetail.items) && primaryDetail.items.length
        ? primaryDetail.items
        : Array.isArray(fallbackDetail.items)
          ? fallbackDetail.items
          : [],
    extraAccessories:
      Array.isArray(primaryDetail.extraAccessories) && primaryDetail.extraAccessories.length
        ? primaryDetail.extraAccessories
        : Array.isArray(fallbackDetail.extraAccessories)
          ? fallbackDetail.extraAccessories
          : [],
  };
}

function getOrderOptionLabel(options, value) {
  const normalizedValue = String(value ?? "").trim();
  if (!normalizedValue) return "-";
  return (
    options.find((option) => String(option.value) === normalizedValue)?.label ||
    normalizedValue
  );
}

function getShipTimeSlotLabel(value) {
  return getOrderOptionLabel(SHIP_TIME_SLOT_OPTIONS, value);
}

function getDeliveryMethodLabel(value) {
  return getOrderOptionLabel(DELIVERY_METHOD_OPTIONS, value);
}

function formatOrderAmount(value) {
  if (value === null || value === undefined || value === "") return "-";
  const amount = Number(value);
  return Number.isFinite(amount) ? amount.toFixed(2) : "-";
}

function orderStatusLabel(status) {
  return (
    {
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
    }[Number(status)] || "-"
  );
}

function orderStatusType(status) {
  const code = Number(status);
  if (
    [
      RENTAL_CODE.ORDER_STATUS.PENDING_SHIP,
      RENTAL_CODE.ORDER_STATUS.RETURNING,
    ].includes(code)
  ) {
    return "warning";
  }
  if (
    [
      RENTAL_CODE.ORDER_STATUS.SHIPPING,
      RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT,
      RENTAL_CODE.ORDER_STATUS.SHIPPED,
      RENTAL_CODE.ORDER_STATUS.PREPARED,
    ].includes(code)
  ) {
    return "primary";
  }
  if (
    [
      RENTAL_CODE.ORDER_STATUS.COMPLETED,
      RENTAL_CODE.ORDER_STATUS.RENTING,
    ].includes(code)
  ) {
    return "success";
  }
  if (
    [
      RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN,
      RENTAL_CODE.ORDER_STATUS.OVERDUE,
      RENTAL_CODE.ORDER_STATUS.AFTER_SALES,
    ].includes(code)
  ) {
    return "danger";
  }
  return "info";
}

function getExpressCompanyName(carrier) {
  return formatExpressCompanyName(carrier, props.expressCompanyOptions) || "快递公司";
}

function extraAccessoryName(item) {
  if (typeof item === "string") {
    return item.replace(/\s+x\d+$/, "") || "额外配件";
  }
  return item?.itemName || item?.name || item?.productName || "额外配件";
}

function extraAccessoryQuantity(item) {
  if (typeof item === "string") {
    const match = item.match(/\s+x(\d+)$/);
    return Number(match?.[1]) > 0 ? Number(match[1]) : 1;
  }
  return Number(item?.quantity) > 0 ? Number(item.quantity) : 1;
}

function handleVisibleChange(value) {
  emit("update:modelValue", value);
}
</script>

<style scoped>
.order-detail-section {
  margin-top: 18px;
}

.order-detail-section h4 {
  margin: 0 0 10px;
  color: var(--el-text-color-primary);
  font-size: 15px;
  font-weight: 650;
}

:deep(.el-descriptions__content) {
  overflow-wrap: anywhere;
}

.order-detail-items-table {
  margin-top: 12px;
}
</style>
