<template>
  <div class="app-container">
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch">
      <el-form-item label="商铺名称" prop="shopName">
        <el-input
          v-model="queryParams.shopName"
          clearable
          placeholder="名称、主体或编码"
          style="width: 240px"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="关键词" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          clearable
          placeholder="订单编码、外部单号或联系人"
          style="width: 240px"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="订单状态" prop="orderStatus">
        <el-select
          v-model="queryParams.orderStatus"
          clearable
          placeholder="全部"
          style="width: 240px">
          <el-option
            v-for="item in orderStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">
          搜索
        </el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="orderList">
      <el-table-column
        label="订单编码"
        prop="orderNo"
        min-width="140"
        show-overflow-tooltip />
      <el-table-column
        label="商铺名称"
        prop="merchantName"
        min-width="140"
        show-overflow-tooltip />
      <el-table-column
        label="来源店铺"
        prop="sourceShopName"
        min-width="120"
        show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.sourceShopName || "-" }}
        </template>
      </el-table-column>
      <el-table-column
        label="联系人"
        prop="customerName"
        width="120"
        show-overflow-tooltip />
      <el-table-column label="联系电话" prop="customerPhone" width="140" />
      <el-table-column
        label="收件人"
        prop="recipientName"
        width="120"
        show-overflow-tooltip />
      <el-table-column label="收件电话" prop="recipientPhone" width="140" />
      <el-table-column label="租赁日期" min-width="200" align="center">
        <template #default="scope">
          {{ scope.row.leaseStartDate || "-" }} 至
          {{ scope.row.leaseEndDate || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="租金" prop="rentAmount" width="110" align="right">
        <template #default="scope">¥{{ money(scope.row.rentAmount) }}</template>
      </el-table-column>
      <el-table-column
        label="押金"
        prop="depositAmount"
        width="110"
        align="right">
        <template #default="scope">
          ¥{{ money(scope.row.depositAmount) }}
        </template>
      </el-table-column>
      <el-table-column
        label="订单状态"
        prop="orderStatus"
        width="110"
        align="center">
        <template #default="scope">
          <el-tag :type="statusMeta(scope.row.orderStatus).type">
            {{ statusMeta(scope.row.orderStatus).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        prop="createTime"
        width="180"
        align="center" />
      <el-table-column label="操作" width="80" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row)">
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList" />

    <el-dialog
      v-model="detailVisible"
      title="平台订单详情"
      width="1000px"
      append-to-body
      destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="订单编码">
            {{ detail.orderNo || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="商户">
            {{ detail.merchantName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="来源店铺">
            {{ detail.sourceShopName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="外部单号">
            {{ detail.externalOrderNo || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusMeta(detail.orderStatus).type">
              {{ statusMeta(detail.orderStatus).label }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ detail.createTime || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="客户">
            {{ detail.customerName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="客户电话">
            {{ detail.customerPhone || "-" }}
            <el-button
              v-hasPermi="['platform:order:customer:view']"
              link
              type="warning"
              class="customer-view-button"
              @click="openCustomerDetail">
              查看完整信息
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="收件人">
            {{ detail.recipientName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="收件电话">
            {{ detail.recipientPhone || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">
            {{ detail.recipientAddress || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="发货仓库">
            {{ detail.warehouseName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="归还仓库">
            {{ detail.returnWarehouseName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="租期">
            {{ leaseText(detail) }}
          </el-descriptions-item>
          <el-descriptions-item label="产品型号">
            {{ detail.modelName || detail.productName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="套餐">
            {{ detail.packageName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="租金">
            {{ money(detail.rentAmount) }}
          </el-descriptions-item>
          <el-descriptions-item label="押金">
            {{ money(detail.depositAmount) }}
          </el-descriptions-item>
          <el-descriptions-item label="物流费用">
            {{ money(detail.deliveryAmount) }}
          </el-descriptions-item>
          <el-descriptions-item label="寄出物流">
            {{ expressText(detail.shippingCarrier, detail.shippingTrackingNo) }}
          </el-descriptions-item>
          <el-descriptions-item label="归还物流">
            {{ expressText(detail.returnCarrier, detail.returnTrackingNo) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">
            {{ detail.remark || detail.customerNote || "-" }}
          </el-descriptions-item>
        </el-descriptions>

        <el-tabs v-model="detailTab" class="detail-tabs">
          <el-tab-pane label="履约进度" name="fulfillment">
            <el-timeline v-if="detail.fulfillmentEvents?.length">
              <el-timeline-item
                v-for="(event, index) in detail.fulfillmentEvents"
                :key="`${event.createTime}-${index}`"
                :timestamp="event.createTime">
                {{ event.fromStatusLabel || "初始状态" }} →
                {{ event.toStatusLabel || "未知状态" }}
                <span class="detail-muted">
                  （{{ event.reason || "订单履约状态变更" }}，{{
                    event.operatorName || "系统"
                  }}）
                </span>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无履约状态记录" />
          </el-tab-pane>
          <el-tab-pane label="产品明细" name="items">
            <el-table :data="detail.items || []" border size="small">
              <el-table-column
                label="类型"
                prop="itemTypeName"
                width="100"
                align="center" />
              <el-table-column
                label="产品"
                prop="itemName"
                min-width="180"
                show-overflow-tooltip />
              <el-table-column
                label="型号"
                prop="modelName"
                min-width="160"
                show-overflow-tooltip>
                <template #default="scope">
                  {{ scope.row.modelName || "-" }}
                </template>
              </el-table-column>
              <el-table-column
                label="编码"
                prop="itemCode"
                min-width="150"
                show-overflow-tooltip>
                <template #default="scope">
                  {{ scope.row.itemCode || "-" }}
                </template>
              </el-table-column>
              <el-table-column
                label="序列号"
                min-width="180"
                show-overflow-tooltip>
                <template #default="scope">
                  {{ joinValues(scope.row.serialNumbers) }}
                </template>
              </el-table-column>
              <el-table-column
                label="库存编码"
                min-width="180"
                show-overflow-tooltip>
                <template #default="scope">
                  {{ joinValues(scope.row.inventoryCodes) }}
                </template>
              </el-table-column>
              <el-table-column
                label="数量"
                prop="quantity"
                width="80"
                align="center" />
              <el-table-column
                label="状态"
                prop="itemStatusName"
                width="120"
                align="center" />
            </el-table>
            <el-empty v-if="!detail.items?.length" description="暂无产品明细" />
          </el-tab-pane>
          <el-tab-pane label="物流信息" name="logistics">
            <el-table :data="detail.logistics || []" border size="small">
              <el-table-column
                label="方向"
                prop="directionName"
                width="120"
                align="center" />
              <el-table-column label="物流公司" prop="carrier" width="140">
                <template #default="{ row }">
                  {{ getExpressCompanyName(row.carrier) || "-" }}
                </template>
              </el-table-column>
              <el-table-column
                label="物流单号"
                prop="trackingNo"
                min-width="140"
                show-overflow-tooltip />
              <el-table-column
                label="来源"
                prop="sourceTypeName"
                width="100"
                align="center" />
              <el-table-column
                label="物流状态"
                prop="routeStatusName"
                width="110"
                align="center" />
              <el-table-column
                label="最近查询"
                prop="routeQueryTime"
                width="170" />
              <el-table-column
                label="费用"
                prop="feeAmount"
                width="100"
                align="right">
                <template #default="scope">
                  ¥{{ money(scope.row.feeAmount) }}
                </template>
              </el-table-column>
            </el-table>
            <el-empty
              v-if="!detail.logistics?.length"
              description="暂无物流记录" />
            <div
              v-for="(logistics, index) in detail.logistics || []"
              :key="`${logistics.directionName}-${logistics.trackingNo}-${index}`"
              class="detail-subsection">
              <div class="detail-subsection__title">
                {{ logistics.directionName || "物流" }} 路由节点
              </div>
              <el-table :data="logistics.routes || []" border size="small">
                <el-table-column
                  label="状态"
                  prop="routeStatusName"
                  width="120"
                  align="center" />
                <el-table-column label="时间" prop="routeTime" width="140" />
                <el-table-column
                  label="位置"
                  prop="location"
                  width="80"
                  show-overflow-tooltip />
                <el-table-column
                  label="轨迹"
                  prop="routeDesc"
                  min-width="280"
                  show-overflow-tooltip />
              </el-table>
              <el-empty
                v-if="!logistics.routes?.length"
                description="暂无已保存路由节点" />
            </div>
          </el-tab-pane>
          <el-tab-pane label="归还验机" name="returnInspection">
            <template v-if="detail.returnInspection">
              <el-descriptions
                :column="2"
                border
                label-width="120"
                size="small">
                <el-descriptions-item label="验机结果">
                  <el-tag
                    :type="
                      detail.returnInspection.resultLabel === '验机正常'
                        ? 'success'
                        : 'warning'
                    ">
                    {{ detail.returnInspection.resultLabel || "未知结果" }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="售后类型">
                  {{ detail.returnInspection.caseTypeLabel || "-" }}
                </el-descriptions-item>
                <el-descriptions-item label="客户联系">
                  {{
                    detail.returnInspection.customerContactedLabel || "未记录"
                  }}
                </el-descriptions-item>
                <el-descriptions-item label="验机时间">
                  {{ detail.returnInspection.inspectedTime || "-" }}
                </el-descriptions-item>
                <el-descriptions-item label="问题描述" :span="2">
                  {{ detail.returnInspection.problemDesc || "-" }}
                </el-descriptions-item>
                <el-descriptions-item label="缺失物品记录" :span="2">
                  <pre class="inspection-text">{{
                    detail.returnInspection.lostItemsJson || "-"
                  }}</pre>
                </el-descriptions-item>
                <el-descriptions-item label="赔付金额">
                  ¥{{ money(detail.returnInspection.compensationAmount) }}
                </el-descriptions-item>
                <el-descriptions-item label="备注">
                  {{ detail.returnInspection.remark || "-" }}
                </el-descriptions-item>
              </el-descriptions>
            </template>
            <el-empty v-else description="暂无归还验机记录" />
          </el-tab-pane>
          <el-tab-pane label="售后记录" name="afterSales">
            <el-table :data="detail.afterSales || []" border size="small">
              <el-table-column label="工单号" prop="caseNo" min-width="150" />
              <el-table-column
                label="类型"
                prop="caseTypeName"
                width="120"
                align="center" />
              <el-table-column
                label="状态"
                prop="caseStatusName"
                width="120"
                align="center" />
              <el-table-column
                label="问题描述"
                prop="problemDesc"
                min-width="240"
                show-overflow-tooltip />
              <el-table-column
                label="赔付金额"
                prop="compensationAmount"
                width="110"
                align="right">
                <template #default="scope">
                  ¥{{ money(scope.row.compensationAmount) }}
                </template>
              </el-table-column>
              <el-table-column label="创建时间" prop="createTime" width="170" />
              <el-table-column label="完成时间" prop="finishTime" width="170">
                <template #default="scope">
                  {{ scope.row.finishTime || "-" }}
                </template>
              </el-table-column>
            </el-table>
            <el-empty
              v-if="!detail.afterSales?.length"
              description="暂无售后记录" />
            <el-collapse
              v-if="detail.afterSales?.length"
              class="detail-subsection">
              <el-collapse-item
                v-for="afterSales in detail.afterSales"
                :key="afterSales.caseNo"
                :title="`${afterSales.caseNo || '售后工单'} · ${afterSales.caseStatusName || '未知状态'}`">
                <el-timeline v-if="afterSales.events?.length">
                  <el-timeline-item
                    v-for="(event, index) in afterSales.events"
                    :key="`${afterSales.caseNo}-${event.createTime}-${index}`"
                    :timestamp="event.createTime">
                    {{ event.eventTypeName || "处理事件" }}：{{
                      event.content || "-"
                    }}
                    <span class="detail-muted">
                      （{{ event.operatorName || "系统" }}）
                    </span>
                  </el-timeline-item>
                </el-timeline>
                <el-empty v-else description="暂无处理事件" />
                <el-table
                  v-if="afterSales.evidence?.length"
                  :data="afterSales.evidence"
                  border
                  size="small">
                  <el-table-column
                    label="证据类型"
                    prop="evidenceTypeName"
                    width="120" />
                  <el-table-column
                    label="文件"
                    min-width="220"
                    show-overflow-tooltip>
                    <template #default="scope">
                      <el-link
                        v-if="scope.row.fileUrl"
                        :href="scope.row.fileUrl"
                        target="_blank"
                        type="primary">
                        {{ scope.row.fileName || "查看文件" }}
                      </el-link>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                  <el-table-column
                    label="说明"
                    prop="description"
                    min-width="220"
                    show-overflow-tooltip />
                  <el-table-column
                    label="操作人"
                    prop="operatorName"
                    width="120" />
                  <el-table-column label="时间" prop="createTime" width="170" />
                </el-table>
              </el-collapse-item>
            </el-collapse>
          </el-tab-pane>
          <el-tab-pane label="批注记录" name="notes">
            <el-table :data="detail.notes || []" border size="small">
              <el-table-column
                label="批注内容"
                prop="noteContent"
                min-width="280"
                show-overflow-tooltip />
              <el-table-column label="类型" width="100">
                <template #default="scope">
                  {{ noteTypeText(scope.row.noteType) }}
                </template>
              </el-table-column>
              <el-table-column label="操作人" prop="operatorName" width="130" />
              <el-table-column label="时间" prop="createTime" width="170" />
            </el-table>
            <el-empty v-if="!detail.notes?.length" description="暂无批注记录" />
          </el-tab-pane>
          <el-tab-pane label="变更记录" name="editLogs">
            <el-table :data="detail.editLogs || []" border size="small">
              <el-table-column label="变更字段" prop="fieldName" width="120" />
              <el-table-column
                label="修改前"
                prop="beforeDisplayValue"
                min-width="120"
                show-overflow-tooltip />
              <el-table-column
                label="修改后"
                prop="afterDisplayValue"
                min-width="120"
                show-overflow-tooltip />
              <el-table-column
                label="变更原因"
                prop="changeReason"
                min-width="150"
                show-overflow-tooltip />
              <el-table-column label="操作人" prop="operatorName" width="130" />
              <el-table-column label="时间" prop="createTime" width="170" />
            </el-table>
            <el-empty
              v-if="!detail.editLogs?.length"
              description="暂无字段变更记录" />
          </el-tab-pane>
        </el-tabs>
      </template>
      <el-skeleton v-else :rows="8" animated />
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="customerVisible"
      title="完整客户信息"
      width="560px"
      append-to-body
      destroy-on-close>
      <el-skeleton v-if="customerLoading" :rows="5" animated />
      <el-descriptions
        v-else-if="customerDetail"
        :column="1"
        border
        label-width="120">
        <el-descriptions-item label="订单编码">
          {{ customerDetail.orderNo || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="客户">
          {{ customerDetail.customerName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="客户电话">
          {{ customerDetail.customerPhone || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="收件人">
          {{ customerDetail.recipientName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="收件电话">
          {{ customerDetail.recipientPhone || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="收货地址">
          {{ customerDetail.recipientAddress || "-" }}
        </el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无完整客户信息" />
      <template #footer>
        <el-button @click="customerVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PlatformOrderCenter">
import { useRoute } from "vue-router";
import {
  getPlatformOrderDetail,
  listPlatformOrders,
  viewPlatformOrderCustomer,
} from "@/api/rental/order";
import { RENTAL_CODE } from "@/utils/rental/options";
import { getExpressCompanyName } from "@/utils/rental/expressCompany";

const route = useRoute();
const { proxy } = getCurrentInstance();
const loading = ref(false);
const showSearch = ref(true);
const orderList = ref([]);
const total = ref(0);
const dateRange = ref([]);
const detailVisible = ref(false);
const detail = ref(null);
const detailTab = ref("items");
const detailOrderId = ref(null);
const customerVisible = ref(false);
const customerLoading = ref(false);
const customerDetail = ref(null);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  shopName: undefined,
  keyword: route.query.orderNo ? String(route.query.orderNo) : undefined,
  orderStatus: undefined,
  beginTime: undefined,
  endTime: undefined,
});

const orderStatusOptions = [
  {
    value: RENTAL_CODE.ORDER_STATUS.PENDING_SHIP,
    label: "待发货",
    type: "warning",
  },
  {
    value: RENTAL_CODE.ORDER_STATUS.PREPARED,
    label: "已备货",
    type: "primary",
  },
  {
    value: RENTAL_CODE.ORDER_STATUS.SHIPPING,
    label: "运输中",
    type: "primary",
  },
  {
    value: RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT,
    label: "配送中",
    type: "primary",
  },
  { value: RENTAL_CODE.ORDER_STATUS.SHIPPED, label: "已发货", type: "primary" },
  { value: RENTAL_CODE.ORDER_STATUS.RENTING, label: "租赁中", type: "success" },
  {
    value: RENTAL_CODE.ORDER_STATUS.RETURNING,
    label: "归还中",
    type: "primary",
  },
  {
    value: RENTAL_CODE.ORDER_STATUS.COMPLETED,
    label: "已完成",
    type: "success",
  },
  {
    value: RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN,
    label: "归还逾期",
    type: "danger",
  },
  { value: RENTAL_CODE.ORDER_STATUS.OVERDUE, label: "已逾期", type: "danger" },
  {
    value: RENTAL_CODE.ORDER_STATUS.AFTER_SALES,
    label: "售后中",
    type: "warning",
  },
  { value: RENTAL_CODE.ORDER_STATUS.CANCELLED, label: "已取消", type: "info" },
];
const statusOptions = new Map(
  orderStatusOptions.map((item) => [item.value, item]),
);

function statusMeta(status) {
  return (
    statusOptions.get(Number(status)) || { label: "未知状态", type: "info" }
  );
}

function money(value) {
  return Number(value || 0).toFixed(2);
}

function expressText(carrier, trackingNo) {
  return carrier || trackingNo
    ? `${getExpressCompanyName(carrier) || "-"} ${trackingNo || "-"}`
    : "-";
}

function joinValues(values) {
  return Array.isArray(values) && values.length ? values.join("、") : "-";
}

function leaseText(row) {
  if (!row?.leaseStartDate && !row?.leaseEndDate) return "-";
  return `${row.leaseStartDate || "-"} 至 ${row.leaseEndDate || "-"}${row.leaseDays ? `（共 ${row.leaseDays} 天）` : ""}`;
}

function noteTypeText(value) {
  return { 1: "发货", 2: "收货" }[Number(value)] || "其他";
}

function openDetail(row) {
  detailVisible.value = true;
  detail.value = null;
  detailOrderId.value = row.orderId;
  customerVisible.value = false;
  customerDetail.value = null;
  detailTab.value = "items";
  getPlatformOrderDetail(row.orderId).then((response) => {
    detail.value = response.data || null;
  });
}

function openCustomerDetail() {
  if (!detailOrderId.value) return;
  customerVisible.value = true;
  customerDetail.value = null;
  customerLoading.value = true;
  viewPlatformOrderCustomer(detailOrderId.value)
    .then((response) => {
      customerDetail.value = response.data || null;
    })
    .finally(() => {
      customerLoading.value = false;
    });
}

function getList() {
  loading.value = true;
  queryParams.beginTime = dateRange.value?.[0] || undefined;
  queryParams.endTime = dateRange.value?.[1] || undefined;
  listPlatformOrders({ ...queryParams })
    .then((response) => {
      orderList.value = response.rows || [];
      total.value = Number(response.total || 0);
    })
    .finally(() => {
      loading.value = false;
    });
}

function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  dateRange.value = [];
  queryParams.beginTime = undefined;
  queryParams.endTime = undefined;
  handleQuery();
}

getList();
</script>

<style scoped>
.detail-tabs {
  margin-top: 18px;
}

.detail-subsection {
  margin-top: 16px;
}

.detail-subsection__title {
  margin-bottom: 8px;
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.detail-muted {
  color: var(--el-text-color-secondary);
}

.customer-view-button {
  margin-left: 8px;
}

.inspection-text {
  margin: 0;
  white-space: pre-wrap;
  font: inherit;
  line-height: 1.6;
}
</style>
