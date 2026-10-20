<template>
  <div class="app-container order-form-page">
    <el-page-header
      :content="form.orderId ? '编辑订单' : '新增订单'"
      @back="goBack"
    />
    <el-card class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12"
            ><el-form-item label="客户姓名" prop="customerName"
              ><el-input v-model="form.customerName"
            /></el-form-item>
          </el-col>
          <el-col :span="12"
            ><el-form-item label="联系电话" prop="customerPhone"
              ><el-input v-model="form.customerPhone" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="收件人姓名" prop="recipientName"
              ><el-input v-model="form.recipientName" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="收件人电话" prop="recipientPhone"
              ><el-input v-model="form.recipientPhone" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="订单渠道" prop="channelId"
              ><el-select
                v-model="form.channelId"
                class="full-width"
                filterable
                placeholder="请选择订单渠道"
                ><el-option
                  v-for="item in channelOptions"
                  :key="item.channelId"
                  :label="item.channelName"
                  :value="item.channelId" /></el-select></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="来源店铺"
              ><el-input v-model="form.shopName" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="外部单号"
              ><el-input v-model="form.externalOrderNo" /></el-form-item
          ></el-col>
          <el-col :span="24">
            <el-form-item label="收货地址">
              <el-input v-model="form.recipientAddress" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发货仓库" prop="warehouseId">
              <el-select
                v-model="form.warehouseId"
                class="full-width"
                filterable
                placeholder="请选择启用仓库"
                ><el-option
                  v-for="item in warehouseOptions"
                  :key="item.id"
                  :label="`${item.name}`"
                  :value="item.id" /></el-select
            ></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归还仓库" prop="returnWarehouseId"
              ><el-select
                v-model="form.returnWarehouseId"
                class="full-width"
                filterable
                placeholder="请选择启用仓库"
                ><el-option
                  v-for="item in warehouseOptions"
                  :key="item.id"
                  :label="`${item.name}`"
                  :value="item.id" /></el-select></el-form-item
          ></el-col>
          <el-col :span="12">
            <el-form-item label="租期" prop="leasePeriod">
              <el-input
                v-model="form.leasePeriod"
                maxlength="64"
                placeholder="格式：2025-12-09~2025-12-15"
                @input="handleLeasePeriodInput"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="租用天数">
              <el-input :model-value="form.leaseDays || 0" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="租期开始日期" prop="leaseStartDate">
              <el-date-picker
                v-model="form.leaseStartDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="full-width"
                @change="syncLeasePeriodFromDates"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="租期截止日期" prop="leaseEndDate">
              <el-date-picker
                v-model="form.leaseEndDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="full-width"
                @change="syncLeasePeriodFromDates"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12"
            ><el-form-item label="发货日期"
              ><el-date-picker
                v-model="form.shipDueTime"
                type="date"
                value-format="YYYY-MM-DD"
                class="full-width" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="应该归还日期"
              ><el-date-picker
                v-model="form.returnDueTime"
                type="date"
                value-format="YYYY-MM-DD"
                class="full-width" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="租金">
              <el-input-number
                v-model="form.rentAmount"
                :min="0"
                :precision="2"
                class="full-width" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="押金"
              ><el-input-number
                v-model="form.depositAmount"
                :min="0"
                :precision="2"
                class="full-width" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="订单状态"
              ><el-select v-model="form.orderStatus" class="full-width"
                ><el-option
                  v-for="item in orderStatuses"
                  :key="item.value"
                  v-bind="item" /></el-select></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="支付状态"
              ><el-select v-model="form.payStatus" class="full-width"
                ><el-option
                  v-for="item in payStatuses"
                  :key="item.value"
                  v-bind="item" /></el-select></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="型号" prop="modelId"
              ><el-select
                v-model="form.modelId"
                class="full-width"
                filterable
                clearable
                placeholder="请选择型号"
                @change="handleModelChange"
                ><el-option
                  v-for="item in modelOptions"
                  :key="item.modelId"
                  :label="modelOptionLabel(item)"
                  :value="item.modelId" /></el-select></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="产品套餐" prop="packageId"
              ><el-select
                v-model="form.packageId"
                class="full-width"
                filterable
                clearable
                placeholder="请选择产品套餐"
                @change="handlePackageChange"
                ><el-option
                  v-for="item in filteredPackageOptions"
                  :key="item.packageId"
                  :label="item.packageName"
                  :value="item.packageId" /></el-select></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="SN编号"
              ><el-input-number
                v-model="form.snId"
                :min="1"
                class="full-width" /></el-form-item
          ></el-col>
          <el-col :span="12"
            ><el-form-item label="数量"
              ><el-input-number
                v-model="form.quantity"
                :min="1"
                class="full-width" /></el-form-item
          ></el-col>
          <el-col :span="24"
            ><el-form-item label="配件编号列表"
              ><el-input
                v-model="form.accessoryItemIds"
                placeholder="JSON数组，例如 [1,2]" /></el-form-item
          ></el-col>
          <el-col :span="8"
            ><el-form-item label="时效状态"
              ><el-select v-model="form.timeEffectStatus" class="full-width"
                ><el-option
                  v-for="item in timeEffectStatuses"
                  :key="item.value"
                  v-bind="item" /></el-select></el-form-item
          ></el-col>
          <el-col :span="8"
            ><el-form-item label="天气风险"
              ><el-switch
                v-model="form.weatherRiskFlag"
                :active-value="RENTAL_CODE.YES_NO.YES"
                :inactive-value="RENTAL_CODE.YES_NO.NO" /></el-form-item
          ></el-col>
          <el-col :span="8"
            ><el-form-item label="优先处理"
              ><el-switch
                v-model="form.priorityFlag"
                :active-value="RENTAL_CODE.YES_NO.YES"
                :inactive-value="RENTAL_CODE.YES_NO.NO" /></el-form-item
          ></el-col>
          <el-col :span="24"
            ><el-form-item label="来源原文"
              ><el-input
                v-model="form.sourceTextSnapshot"
                type="textarea"
                :rows="3" /></el-form-item
          ></el-col>
          <el-col :span="24"
            ><el-form-item label="备注"
              ><el-input
                v-model="form.remark"
                type="textarea"
                :rows="4" /></el-form-item
          ></el-col>
        </el-row>
        <div class="form-actions">
          <el-button type="primary" :loading="saving" @click="submitForm"
            >保存订单</el-button
          ><el-button @click="goBack">取消</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="MerchantOrderForm">
import {
  addMerchantOrder,
  getMerchantOrder,
  updateMerchantOrder,
} from "@/api/merchant/order";
import { listMerchantOrderChannels } from "@/api/merchant/orderChannel";
import { listBrandModelOptions } from "@/api/merchant/brandModel";
import {
  listMerchantPackages,
  listMerchantWarehouses,
} from "@/api/merchant/rentalBusiness";
import { RENTAL_CODE } from "@/utils/rental/options";

const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance();
const formRef = ref(null);
const saving = ref(false);
const channelOptions = ref([]);
const warehouseOptions = ref([]);
const modelOptions = ref([]);
const packageOptions = ref([]);
const form = reactive({
  orderId: null,
  externalOrderNo: "",
  channelId: null,
  shopName: "",
  customerName: "",
  customerPhone: "",
  recipientName: "",
  recipientPhone: "",
  recipientAddress: "",
  warehouseId: null,
  returnWarehouseId: null,
  leasePeriod: "",
  leaseStartDate: "",
  leaseEndDate: "",
  shipDueTime: "",
  returnDueTime: "",
  leaseDays: 0,
  rentAmount: 0,
  depositAmount: 0,
  orderStatus: RENTAL_CODE.ORDER_STATUS.PENDING_SHIP,
  payStatus: RENTAL_CODE.PAYMENT_STATUS.UNPAID,
  timeEffectStatus: RENTAL_CODE.TIME_EFFECT_STATUS.NORMAL,
  weatherRiskFlag: RENTAL_CODE.YES_NO.NO,
  priorityFlag: RENTAL_CODE.YES_NO.NO,
  sourceTextSnapshot: "",
  shareSnapshotJson: "",
  sourceOrderId: null,
  version: 0,
  productId: null,
  modelId: null,
  packageId: null,
  snId: null,
  accessoryItemIds: "",
  quantity: 1,
  remark: "",
});
const orderStatuses = [
  { value: RENTAL_CODE.ORDER_STATUS.PENDING_SHIP, label: "待发货" },
  { value: RENTAL_CODE.ORDER_STATUS.SHIPPED, label: "已发货" },
  { value: RENTAL_CODE.ORDER_STATUS.RENTING, label: "租用中" },
  { value: RENTAL_CODE.ORDER_STATUS.RETURNING, label: "归还中" },
  { value: RENTAL_CODE.ORDER_STATUS.COMPLETED, label: "已完成" },
];
const payStatuses = [
  { value: RENTAL_CODE.PAYMENT_STATUS.UNPAID, label: "未支付" },
  { value: RENTAL_CODE.PAYMENT_STATUS.PAID, label: "已支付" },
  { value: RENTAL_CODE.PAYMENT_STATUS.REFUNDED, label: "已退款" },
];
const timeEffectStatuses = [
  { value: RENTAL_CODE.TIME_EFFECT_STATUS.NORMAL, label: "正常" },
  { value: RENTAL_CODE.TIME_EFFECT_STATUS.OVERDUE, label: "逾期" },
];
const rules = {
  modelId: [
    { required: true, message: "请选择型号", trigger: "change" },
  ],
  packageId: [
    { required: true, message: "请选择产品套餐", trigger: "change" },
  ],
  leasePeriod: [
    { required: true, message: "请输入租期", trigger: ["blur", "change"] },
    { validator: validateLeasePeriodRule, trigger: ["blur", "change"] },
  ],
  recipientName: [
    { required: true, message: "请输入收件人姓名", trigger: "blur" },
  ],
  recipientPhone: [
    { required: true, message: "请输入收件人电话", trigger: "blur" },
  ],
  channelId: [{ required: true, message: "请选择订单渠道", trigger: "change" }],
  warehouseId: [
    { required: true, message: "请选择发货仓库", trigger: "change" },
  ],
  returnWarehouseId: [
    { required: true, message: "请选择归还仓库", trigger: "change" },
  ],
  leaseStartDate: [
    { required: true, message: "请选择租赁开始日期", trigger: "change" },
  ],
  leaseEndDate: [
    { required: true, message: "请选择租赁结束日期", trigger: "change" },
  ],
};

/** 加载当前商户启用的订单渠道和仓库，选项均由后端商户上下文过滤。 */
function loadSelectOptions() {
  Promise.all([
    listMerchantOrderChannels({ status: 0, pageNum: 1, pageSize: 1000 }),
    listMerchantWarehouses({ status: 0, pageNum: 1, pageSize: 1000 }),
    listBrandModelOptions(),
    listMerchantPackages({ status: 0, pageNum: 1, pageSize: 1000 }),
  ]).then(([channelResponse, warehouseResponse, modelResponse, packageResponse]) => {
    const channels = responseRows(channelResponse);
    channelOptions.value = channels.filter(
      (item) =>
        Number(item.channelType) !== RENTAL_CODE.ORDER_CHANNEL_TYPE.ALLIANCE,
    );
    warehouseOptions.value = responseRows(warehouseResponse);
    modelOptions.value = responseRows(modelResponse);
    packageOptions.value = responseRows(packageResponse);
    if (form.packageId) {
      form.modelId = modelIdForPackage(form.packageId);
    }
    setDefaultWarehouses();
  });
}

function responseRows(response) {
  return response?.rows || response?.data?.rows || response?.data || [];
}

const filteredPackageOptions = computed(() => {
  if (!form.modelId) return packageOptions.value;
  return packageOptions.value.filter(
    (item) => Number(item.modelId) === Number(form.modelId),
  );
});

function modelOptionLabel(item) {
  return [item.brandName || item.brand, item.modelName || item.productName]
    .filter(Boolean)
    .join(" / ");
}

function modelIdForPackage(packageId) {
  const item = packageOptions.value.find(
    (candidate) => Number(candidate.packageId) === Number(packageId),
  );
  return item?.modelId ? Number(item.modelId) : null;
}

function handleModelChange(modelId) {
  if (!modelId) {
    form.packageId = null;
    return;
  }
  const selectedPackage = packageOptions.value.find(
    (item) => Number(item.packageId) === Number(form.packageId),
  );
  if (selectedPackage && Number(selectedPackage.modelId) !== Number(modelId)) {
    form.packageId = null;
  }
}

function handlePackageChange(packageId) {
  form.modelId = modelIdForPackage(packageId);
}

/** 仅接受完整的日期范围，兼容波浪号、中文“至”和空格三种录入分隔符。 */
const LEASE_PERIOD_PATTERN =
  /^(\d{4}-\d{2}-\d{2})(?:\s*~\s*|\s*至\s*|\s+)(\d{4}-\d{2}-\d{2})$/;

function normalizeDateOnly(value) {
  if (!value) return "";
  return String(value).slice(0, 10);
}

function dateOnlyTimestamp(value) {
  const normalized = normalizeDateOnly(value);
  const match = normalized.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (!match) return null;
  const [, year, month, day] = match.map(Number);
  const timestamp = Date.UTC(year, month - 1, day);
  const date = new Date(timestamp);
  if (
    date.getUTCFullYear() !== year ||
    date.getUTCMonth() !== month - 1 ||
    date.getUTCDate() !== day
  ) {
    return null;
  }
  return timestamp;
}

function calculateLeaseDays(startDate, endDate) {
  const startTimestamp = dateOnlyTimestamp(startDate);
  const endTimestamp = dateOnlyTimestamp(endDate);
  if (startTimestamp === null || endTimestamp === null) return null;
  const difference = endTimestamp - startTimestamp;
  if (difference < 0 || difference % 86400000 !== 0) return null;
  return difference / 86400000 + 1;
}

function parseLeasePeriod(value) {
  const match = String(value || "").trim().match(LEASE_PERIOD_PATTERN);
  if (!match) return null;
  const leaseDays = calculateLeaseDays(match[1], match[2]);
  if (!leaseDays) return null;
  return {
    leaseStartDate: match[1],
    leaseEndDate: match[2],
    leaseDays,
  };
}

function validateLeasePeriodRule(_rule, value, callback) {
  if (!String(value || "").trim()) {
    callback(new Error("请输入租期"));
    return;
  }
  if (!parseLeasePeriod(value)) {
    callback(new Error("租期格式不正确，例如 2025-12-09~2025-12-15"));
    return;
  }
  callback();
}

function formatLeasePeriod(startDate, endDate) {
  const start = normalizeDateOnly(startDate);
  const end = normalizeDateOnly(endDate);
  return calculateLeaseDays(start, end) ? `${start}~${end}` : "";
}

function handleLeasePeriodInput(value) {
  const text = String(value || "").trim();
  if (!text) {
    form.leaseStartDate = "";
    form.leaseEndDate = "";
    form.leaseDays = 0;
    return;
  }
  const parsed = parseLeasePeriod(text);
  if (!parsed) {
    form.leaseStartDate = "";
    form.leaseEndDate = "";
    form.leaseDays = 0;
    return;
  }
  Object.assign(form, parsed);
}

function syncLeasePeriodFromDates() {
  const leaseDays = calculateLeaseDays(
    form.leaseStartDate,
    form.leaseEndDate,
  );
  form.leaseDays = leaseDays || 0;
  form.leasePeriod = formatLeasePeriod(
    form.leaseStartDate,
    form.leaseEndDate,
  );
}

/** 新增订单默认使用排序后的第一个仓库，编辑订单不覆盖已有仓库。 */
function setDefaultWarehouses() {
  if (form.orderId !== null || !warehouseOptions.value.length) return;
  const firstWarehouseId = warehouseOptions.value[0].id;
  if (form.warehouseId === null || form.warehouseId === undefined) {
    form.warehouseId = firstWarehouseId;
  }
  if (
    form.returnWarehouseId === null ||
    form.returnWarehouseId === undefined
  ) {
    form.returnWarehouseId = form.warehouseId;
  }
}

/** 返回订单列表页面，保留统一的商户端页面导航体验。 */
function goBack() {
  router.push("/merchant/order");
}

/** 校验订单表单并按路由模式调用新增或编辑接口。 */
function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) return;
    saving.value = true;
    const payload = { ...form };
    delete payload.leasePeriod;
    const request = form.orderId
      ? updateMerchantOrder(payload)
      : addMerchantOrder(payload);
    request
      .then(() => {
        proxy.$modal.msgSuccess(form.orderId ? "订单修改成功" : "订单创建成功");
        goBack();
      })
      .finally(() => {
        saving.value = false;
      });
  });
}

/** 编辑模式加载服务端订单详情，确保表单显示数据库真实数据。 */
function loadOrder() {
  if (!route.query.orderId) return;
  form.orderId = Number(route.query.orderId);
  getMerchantOrder(form.orderId).then((response) => {
    // 统一兼容 AjaxResult 的 data 包装，避免详情对象被错误当成响应根节点。
    const detail = response.data || response;
    Object.assign(form, {
      ...detail,
      recipientName: detail.recipientName || detail.customerName || "",
      recipientPhone: detail.recipientPhone || detail.customerPhone || "",
      recipientAddress:
        detail.recipientAddress || detail.customerAddress || "",
      modelId: detail.modelId || modelIdForPackage(detail.packageId),
      leaseStartDate: normalizeDateOnly(detail.leaseStartDate),
      leaseEndDate: normalizeDateOnly(detail.leaseEndDate),
      shipDueTime: normalizeDateOnly(detail.shipDueTime),
      returnDueTime: normalizeDateOnly(detail.returnDueTime),
    });
    form.leaseDays =
      Number(detail.leaseDays) ||
      calculateLeaseDays(form.leaseStartDate, form.leaseEndDate) ||
      0;
    form.leasePeriod = formatLeasePeriod(
      form.leaseStartDate,
      form.leaseEndDate,
    );
    form.weatherRiskFlag = detail.weatherRiskFlag ?? RENTAL_CODE.YES_NO.NO;
    form.priorityFlag = detail.priorityFlag ?? RENTAL_CODE.YES_NO.NO;
    form.quantity = detail.quantity || 1;
  });
}

loadSelectOptions();
loadOrder();
</script>

<style scoped>
.order-form-page {
  max-width: 960px;
}
.form-card {
  margin-top: 18px;
}
.full-width {
  width: 100%;
}
.form-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 12px;
}
</style>
