<template>
  <div class="app-container">
    <h2>{{ pageTitle }}</h2>
    <el-row
      v-if="showSettlementSummary"
      :gutter="12"
      class="summary-row"
      v-loading="summaryLoading"
    >
      <el-col :span="6"
        ><el-card shadow="never"
          ><el-statistic
            title="联盟订单结算笔数"
            :value="settlementSummary.totalOrderCount" /></el-card
      ></el-col>
      <el-col :span="6"
        ><el-card shadow="never"
          ><el-statistic
            title="待确认金额"
            :value="settlementSummary.pendingSettlementAmount"
            :precision="2"
            prefix="¥" /></el-card
      ></el-col>
      <el-col :span="6"
        ><el-card shadow="never"
          ><el-statistic
            title="已确认金额"
            :value="settlementSummary.confirmedSettlementAmount"
            :precision="2"
            prefix="¥" /></el-card
      ></el-col>
      <el-col :span="6"
        ><el-card shadow="never"
          ><el-statistic
            title="赔付金额"
            :value="settlementSummary.compensationAmount"
            :precision="2"
            prefix="¥" /></el-card
      ></el-col>
    </el-row>
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item label="联盟" prop="allianceId">
        <el-select
          v-model="queryParams.allianceId"
          clearable
          filterable
          placeholder="全部联盟"
          style="width: 180px"
          @change="handleAllianceChange"
        >
          <el-option
            v-for="item in allianceOptions"
            :key="item.allianceId"
            :label="item.allianceName"
            :value="item.allianceId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发起商户" prop="sourceMemberId">
        <el-select
          v-model="queryParams.sourceMemberId"
          clearable
          filterable
          placeholder="全部商户"
          style="width: 180px"
          @change="handleQuery"
        >
          <el-option
            v-for="item in memberOptions"
            :key="`source-${item.memberId}`"
            :label="item.merchantName"
            :value="item.memberId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="接收商户" prop="targetMemberId">
        <el-select
          v-model="queryParams.targetMemberId"
          clearable
          filterable
          placeholder="全部商户"
          style="width: 180px"
          @change="handleQuery"
        >
          <el-option
            v-for="item in memberOptions"
            :key="`target-${item.memberId}`"
            :label="item.merchantName"
            :value="item.memberId"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="!isPublicPool" label="订单状态" prop="status">
        <el-select
          v-model="queryParams.status"
          clearable
          placeholder="全部"
          style="width: 160px"
          @change="handleQuery"
        >
          <el-option
            v-for="item in statusFilterOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          style="width: 360px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery"
          >查询</el-button
        >
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>
    <el-table v-loading="loading" :data="rows">
      <el-table-column
        label="发起商户"
        prop="sourceMerchantName"
        min-width="150"
        show-overflow-tooltip
      />
      <el-table-column
        label="接收商户"
        prop="targetMerchantName"
        min-width="150"
        show-overflow-tooltip
      />
      <el-table-column
        label="原订单编码"
        prop="sourceOrderNo"
        width="160"
        align="center"
      />
      <el-table-column
        label="子订单编码"
        prop="childOrderNo"
        width="160"
        align="center"
      >
        <template #default="scope">{{
          scope.row.childOrderNo || "-"
        }}</template>
      </el-table-column>
      <el-table-column
        label="转单方式"
        prop="transferMode"
        width="100"
        align="center"
      >
        <template #default="scope">{{
          transferModeText(scope.row.transferMode)
        }}</template>
      </el-table-column>
      <el-table-column
        label="状态"
        prop="allianceOrderStatus"
        width="100"
        align="center"
      >
        <template #default="scope">
          <el-tag :type="statusMeta(scope.row.allianceOrderStatus).type">{{
            statusMeta(scope.row.allianceOrderStatus).label
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="结算价"
        prop="settlementPrice"
        width="110"
        align="right"
      >
        <template #default="scope">{{
          scope.row.settlementPrice == null
            ? "-"
            : `¥${Number(scope.row.settlementPrice).toFixed(2)}`
        }}</template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        prop="createTime"
        width="180"
        align="center"
      />
      <el-table-column
        label="接受时间"
        prop="acceptedTime"
        width="180"
        align="center"
      >
        <template #default="scope">{{
          scope.row.acceptedTime || "-"
        }}</template>
      </el-table-column>
      <el-table-column label="发货快递" min-width="190"
        ><template #default="scope">{{
          expressText(scope.row.shippingCarrier, scope.row.shippingTrackingNo)
        }}</template></el-table-column
      >
      <el-table-column label="归还快递" min-width="190"
        ><template #default="scope">{{
          expressText(scope.row.returnCarrier, scope.row.returnTrackingNo)
        }}</template></el-table-column
      >
      <el-table-column label="物流状态" width="120" align="center"
        ><template #default="scope">{{
          routeStatusText(
            scope.row.shippingRouteStatus,
            scope.row.returnRouteStatus,
          )
        }}</template></el-table-column
      >
      <el-table-column label="操作" width="90" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row)"
            >详情</el-button
          >
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog v-model="detailVisible" title="联盟订单详情" width="900px">
      <el-descriptions v-if="detail.order" :column="3" border label-width="120">
        <el-descriptions-item label="发起商户">{{
          detail.order.sourceMerchantName
        }}</el-descriptions-item>
        <el-descriptions-item label="接收商户">{{
          detail.order.targetMerchantName
        }}</el-descriptions-item>
        <el-descriptions-item ></el-descriptions-item>
        <el-descriptions-item label="状态">{{
          statusMeta(detail.order.allianceOrderStatus).label
        }}</el-descriptions-item>
        <el-descriptions-item label="原订单编码">{{
          detail.order.sourceOrderNo || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="子订单编码">{{
          detail.order.childOrderNo || "-"
        }}</el-descriptions-item>

        <el-descriptions-item label="创建时间">{{
          detail.order.createTime
        }}</el-descriptions-item>
        <el-descriptions-item label="接受时间">{{
          detail.order.acceptedTime || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="结算价">{{
          detail.order.settlementPrice == null
            ? "-"
            : `¥${Number(detail.order.settlementPrice).toFixed(2)}`
        }}</el-descriptions-item>
        <el-descriptions-item label="发货快递">{{
          expressText(
            detail.order.shippingCarrier,
            detail.order.shippingTrackingNo,
          )
        }}</el-descriptions-item>
        <el-descriptions-item label="归还快递">{{
          expressText(detail.order.returnCarrier, detail.order.returnTrackingNo)
        }}</el-descriptions-item>
        <el-descriptions-item label="物流状态">{{
          routeStatusText(
            detail.order.shippingRouteStatus,
            detail.order.returnRouteStatus,
          )
        }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">事件流水</el-divider>
      <el-table :data="detail.events" size="small" border>
        <el-table-column label="事件类型" width="130">
          <template #default="scope">{{
            eventTypeText(scope.row.eventType)
          }}</template>
        </el-table-column>
        <el-table-column label="操作商户" min-width="140" show-overflow-tooltip>
          <template #default="scope">{{
            scope.row.operatorMerchantName || "系统"
          }}</template>
        </el-table-column>
        <el-table-column label="原状态" width="100">
          <template #default="scope">{{
            allianceStatusText(scope.row.fromStatus)
          }}</template>
        </el-table-column>
        <el-table-column label="目标状态" width="100">
          <template #default="scope">{{
            allianceStatusText(scope.row.toStatus)
          }}</template>
        </el-table-column>
        <el-table-column label="时间" prop="createTime" min-width="170" />
      </el-table>
      <el-divider content-position="left">结算记录</el-divider>
      <el-table :data="detail.settlements" size="small" border>
        <el-table-column label="发起商户" min-width="150"
          ><template #default="scope">{{
            detail.order?.sourceMerchantName || "-"
          }}</template></el-table-column
        >
        <el-table-column label="接收商户" min-width="150"
          ><template #default="scope">{{
            detail.order?.targetMerchantName || "-"
          }}</template></el-table-column
        >
        <el-table-column label="结算金额" min-width="110">
          <template #default="scope">{{
            amountText(scope.row.settlementAmount)
          }}</template>
        </el-table-column>
        <el-table-column label="快递金额" min-width="110">
          <template #default="scope">{{
            amountText(scope.row.expressAmount)
          }}</template>
        </el-table-column>
        <el-table-column label="赔付金额" min-width="110">
          <template #default="scope">{{
            amountText(scope.row.compensationAmount)
          }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">{{
            settlementStatusText(scope.row.settlementStatus)
          }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="PlatformAllianceOrderCenter">
import { ref, reactive, computed, watch } from "vue";
import { useRoute } from "vue-router";
import {
  getAllianceOrderDetail,
  getAllianceSettlementSummary,
  listAllianceOrders,
} from "@/api/rental/allianceOrder";
import { listAlliances, listAllianceMembers } from "@/api/rental/alliance";
import {
  ALLIANCE_EVENT_TYPE,
  ALLIANCE_ORDER_STATUS,
  ALLIANCE_ORDER_VIEW_MODE,
  ALLIANCE_ROUTE_STATUS,
  ALLIANCE_SETTLEMENT_STATUS,
  ALLIANCE_STATUS,
  ALLIANCE_TRANSFER_MODE,
} from "@/utils/rental/alliance";
import { getExpressCompanyName } from "@/utils/rental/expressCompany";

const loading = ref(false);
const showSearch = ref(true);
const route = useRoute();
const isPublicPool = computed(() =>
  route.path.endsWith("/alliance-public-pool"),
);
const isAssistView = computed(() =>
  route.path.endsWith("/alliance-assist-order"),
);
const pageTitle = computed(() =>
  isPublicPool.value
    ? "公众池订单"
    : isAssistView.value
      ? "协助订单"
      : "联盟订单",
);
const showSettlementSummary = computed(
  () => !isPublicPool.value && !isAssistView.value,
);
const currentViewMode = computed(() =>
  isPublicPool.value
    ? ALLIANCE_ORDER_VIEW_MODE.PUBLIC_POOL
    : isAssistView.value
      ? ALLIANCE_ORDER_VIEW_MODE.ASSIST
      : undefined,
);
const rows = ref([]);
const total = ref(0);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  allianceId: undefined,
  sourceMemberId: undefined,
  targetMemberId: undefined,
  status: undefined,
  viewMode: undefined,
  beginTime: undefined,
  endTime: undefined,
});
const dateRange = ref([]);
const allianceOptions = ref([]);
const memberOptions = ref([]);
const statusOptions = new Map([
  [ALLIANCE_ORDER_STATUS.PENDING, { label: "待接受", type: "warning" }],
  [ALLIANCE_ORDER_STATUS.ACCEPTED, { label: "已接受", type: "success" }],
  [ALLIANCE_ORDER_STATUS.REJECTED, { label: "已拒绝", type: "info" }],
  [ALLIANCE_ORDER_STATUS.CANCELLED, { label: "已取消", type: "info" }],
  [ALLIANCE_ORDER_STATUS.EXPIRED, { label: "已过期", type: "danger" }],
  [ALLIANCE_ORDER_STATUS.PUBLIC_POOL, { label: "公众池中", type: "warning" }],
  [ALLIANCE_ORDER_STATUS.IN_FULFILLMENT, { label: "履约中", type: "primary" }],
  [ALLIANCE_ORDER_STATUS.COMPLETED, { label: "已完成", type: "success" }],
  [ALLIANCE_ORDER_STATUS.EXCEPTION, { label: "异常", type: "danger" }],
]);
const eventTypeOptions = new Map([
  [ALLIANCE_EVENT_TYPE.CREATED, "创建联盟订单"],
  [ALLIANCE_EVENT_TYPE.ACCEPTED, "接受订单"],
  [ALLIANCE_EVENT_TYPE.REJECTED, "拒绝订单"],
  [ALLIANCE_EVENT_TYPE.TRANSFERRED, "转入联盟"],
  [ALLIANCE_EVENT_TYPE.PUBLIC_POOL, "进入公众池"],
  [ALLIANCE_EVENT_TYPE.CHILD_ORDER_CREATED, "创建子订单"],
  [ALLIANCE_EVENT_TYPE.SHIPPED, "开始发货"],
  [ALLIANCE_EVENT_TYPE.COMPLETED, "完成履约"],
  [ALLIANCE_EVENT_TYPE.CANCELLED, "取消订单"],
  [ALLIANCE_EVENT_TYPE.EXCEPTION, "标记异常"],
  [ALLIANCE_EVENT_TYPE.WAREHOUSE_CHANGED, "变更仓库"],
]);
const settlementStatusOptions = new Map([
  [ALLIANCE_SETTLEMENT_STATUS.PENDING, "待确认"],
  [ALLIANCE_SETTLEMENT_STATUS.CONFIRMED, "已确认"],
  [ALLIANCE_SETTLEMENT_STATUS.DISPUTED, "争议中"],
  [ALLIANCE_SETTLEMENT_STATUS.CLOSED, "已关闭"],
]);
const transferModeOptions = new Map([
  [ALLIANCE_TRANSFER_MODE.ALLIANCE, "租商联盟"],
  [ALLIANCE_TRANSFER_MODE.DESIGNATED_MERCHANT, "指定商户"],
  [ALLIANCE_TRANSFER_MODE.PUBLIC_POOL, "公众池"],
]);
const routeStatusOptions = new Map([
  [ALLIANCE_ROUTE_STATUS.PENDING, "待处理"],
  [ALLIANCE_ROUTE_STATUS.IN_TRANSIT, "运输中"],
  [ALLIANCE_ROUTE_STATUS.DELIVERED, "已送达"],
  [ALLIANCE_ROUTE_STATUS.EXCEPTION, "异常"],
  [ALLIANCE_ROUTE_STATUS.CLOSED, "已关闭"],
]);
const statusFilterOptions = computed(() =>
  Array.from(statusOptions.entries())
    .filter(
      ([value]) =>
        !isAssistView.value || value !== ALLIANCE_ORDER_STATUS.PUBLIC_POOL,
    )
    .map(([value, meta]) => ({ value, ...meta })),
);
const detailVisible = ref(false);
const detail = reactive({ order: null, events: [], settlements: [] });
const summaryLoading = ref(false);
const settlementSummary = reactive({
  totalOrderCount: 0,
  pendingSettlementAmount: 0,
  confirmedSettlementAmount: 0,
  compensationAmount: 0,
});

function statusMeta(status) {
  return (
    statusOptions.get(Number(status)) || { label: "未知状态", type: "info" }
  );
}
function eventTypeText(value) {
  return eventTypeOptions.get(Number(value)) || "未知事件";
}
function transferModeText(value) {
  return transferModeOptions.get(Number(value)) || "未知转单方式";
}
function allianceStatusText(value) {
  return value === null || value === undefined || value === ""
    ? "-"
    : statusMeta(value).label;
}
function settlementStatusText(value) {
  return value === null || value === undefined || value === ""
    ? "-"
    : settlementStatusOptions.get(Number(value)) || "未知状态";
}
function amountText(value) {
  return value === null || value === undefined || value === ""
    ? "-"
    : Number(value).toFixed(2);
}
function expressText(carrier, trackingNo) {
  return carrier || trackingNo
    ? `${getExpressCompanyName(carrier) || "未知快递"} / ${trackingNo || "待填写"}`
    : "暂无";
}
function routeStatusText(shippingStatus, returnStatus) {
  const status = returnStatus ?? shippingStatus;
  return routeStatusOptions.get(Number(status)) || "暂无";
}
function getList() {
  queryParams.beginTime = dateRange.value?.[0] || undefined;
  queryParams.endTime = dateRange.value?.[1] || undefined;
  loading.value = true;
  listAllianceOrders(queryParams)
    .then((response) => {
      rows.value = response.rows || [];
      total.value = Number(response.total || 0);
    })
    .finally(() => {
      loading.value = false;
    });
}
function getSettlementSummary() {
  summaryLoading.value = true;
  getAllianceSettlementSummary()
    .then((response) => {
      const data = response.data || {};
      settlementSummary.totalOrderCount = Number(data.totalOrderCount || 0);
      settlementSummary.pendingSettlementAmount = Number(
        data.pendingSettlementAmount || 0,
      );
      settlementSummary.confirmedSettlementAmount = Number(
        data.confirmedSettlementAmount || 0,
      );
      settlementSummary.compensationAmount = Number(
        data.compensationAmount || 0,
      );
    })
    .finally(() => {
      summaryLoading.value = false;
    });
}
function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}
function applyRouteScope() {
  queryParams.viewMode = currentViewMode.value;
  queryParams.status = isPublicPool.value
    ? ALLIANCE_ORDER_STATUS.PUBLIC_POOL
    : undefined;
}
function resetFilters() {
  Object.assign(queryParams, {
    pageNum: 1,
    allianceId: undefined,
    sourceMemberId: undefined,
    targetMemberId: undefined,
    status: undefined,
    viewMode: undefined,
    beginTime: undefined,
    endTime: undefined,
  });
  dateRange.value = [];
  memberOptions.value = [];
  applyRouteScope();
}
function resetQuery() {
  resetFilters();
  getList();
}
function handleAllianceChange() {
  queryParams.sourceMemberId = undefined;
  queryParams.targetMemberId = undefined;
  memberOptions.value = [];
  if (queryParams.allianceId) {
    listAllianceMembers(queryParams.allianceId).then((response) => {
      memberOptions.value = response.data || [];
    });
  }
  handleQuery();
}
function openDetail(row) {
  getAllianceOrderDetail(row.allianceOrderId).then((response) => {
    detail.order = response.data?.order || null;
    detail.events = response.data?.events || [];
    detail.settlements = response.data?.settlements || [];
    detailVisible.value = true;
  });
}
watch(
  () => route.path,
  () => {
    resetFilters();
    getList();
    if (showSettlementSummary.value) getSettlementSummary();
  },
);
resetFilters();
getList();
  listAlliances({
    pageNum: 1,
    pageSize: 500,
    status: ALLIANCE_STATUS.ACTIVE,
  }).then((response) => {
  allianceOptions.value = response.rows || [];
});
if (showSettlementSummary.value) getSettlementSummary();
</script>

<style scoped>
.summary-row {
  margin-bottom: 16px;
}
</style>
