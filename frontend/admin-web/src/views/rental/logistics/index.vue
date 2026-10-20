<template>
  <div class="app-container platform-logistics-page">
    <PageHeading
      kicker="LOGISTICS GOVERNANCE"
      title="物流治理"
      description="统一查看商户物流状态、缓存轨迹和异常运单，降低重复查询与履约风险">
      <template #aside>
        <el-button :icon="Refresh" :loading="loading" @click="getList">
          刷新列表
        </el-button>
        <el-button
          v-hasPermi="['platform:logistics:refresh']"
          type="primary"
          :icon="RefreshRight"
          :loading="refreshing"
          @click="refreshRoutes">
          刷新未签收路线
        </el-button>
      </template>
    </PageHeading>

    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      class="filter-form"
      @submit.prevent>
      <el-form-item label="关键词" prop="keyword">
        <el-input
          v-model.trim="queryParams.keyword"
          clearable
          placeholder="订单编码、快递公司或单号"
          style="width: 250px"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="商户" prop="merchantKeyword">
        <el-input
          v-model.trim="queryParams.merchantKeyword"
          clearable
          placeholder="商户名称或编码"
          style="width: 180px"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="物流方向" prop="direction">
        <el-select
          v-model="queryParams.direction"
          clearable
          placeholder="全部方向"
          style="width: 120px">
          <el-option
            v-for="item in directionOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="路由状态" prop="routeStatus">
        <el-select
          v-model="queryParams.routeStatus"
          clearable
          placeholder="全部状态"
          style="width: 120px">
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
          搜索
        </el-button>
        <el-button :icon="RefreshRight" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column
        label="订单编码"
        prop="orderNo"
        width="180"
        show-overflow-tooltip />
      <el-table-column
        label="商户"
        prop="merchantName"
        min-width="160"
        show-overflow-tooltip />
      <el-table-column
        label="方向"
        prop="directionName"
        width="80"
        align="center" />
      <el-table-column label="物流信息" min-width="230" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getExpressCompanyName(row.carrier) || "-" }} / {{ row.trackingNo || "待填写" }}
        </template>
      </el-table-column>
      <el-table-column
        label="路由状态"
        prop="routeStatusName"
        width="110"
        align="center">
        <template #default="{ row }">
          <el-tag
            :type="routeStatusType(row.routeStatus)"
            :effect="Number(row.abnormalFlag) === 1 ? 'dark' : 'light'">
            {{ row.routeStatusName || "未知状态" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="仓库"
        prop="warehouseName"
        width="130"
        show-overflow-tooltip />
      <el-table-column label="查询时间" prop="routeQueryTime" width="170" />
      <el-table-column label="供应商" prop="routeProvider" width="110" />
      <el-table-column label="来源" prop="sourceTypeName" width="95" />
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">
            详情
          </el-button>
          <el-button
            v-if="
              Number(row.routeStatus) !== 2 && Number(row.routeStatus) !== 4
            "
            v-hasPermi="['platform:logistics:refresh']"
            link
            type="primary"
            :loading="refreshingLogisticsId === row.logisticsId"
            @click="refreshOne(row)">
            刷新
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList" />
    <el-empty v-if="!loading && rows.length === 0" description="暂无物流记录" />

    <el-drawer
      v-model="detailVisible"
      title="物流详情"
      size="720px"
      destroy-on-close>
      <template v-if="detail?.logistics">
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="订单编码">
            {{ detail.logistics.orderNo }}
          </el-descriptions-item>
          <el-descriptions-item label="商户">
            {{ detail.logistics.merchantName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="物流方向">
            {{ detail.logistics.directionName }}
          </el-descriptions-item>
          <el-descriptions-item label="物流来源">
            {{ detail.logistics.sourceTypeName }}
          </el-descriptions-item>
          <el-descriptions-item label="快递公司">
            {{ getExpressCompanyName(detail.logistics.carrier) || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="快递单号">
            {{ detail.logistics.trackingNo || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="路由状态">
            <el-tag :type="routeStatusType(detail.logistics.routeStatus)">
              {{ detail.logistics.routeStatusName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="仓库">
            {{ detail.logistics.warehouseName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="查询供应商">
            {{ detail.logistics.routeProvider || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="最近查询">
            {{ detail.logistics.routeQueryTime || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="路线更新时间">
            {{ detail.logistics.lastRouteTime || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="最近消息" :span="2">
            {{ detail.logistics.routeMessage || "-" }}
          </el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">物流轨迹</el-divider>
        <el-timeline v-if="detail.routes?.length">
          <el-timeline-item
            v-for="(route, index) in detail.routes"
            :key="`${route.routeTime}-${index}`"
            :timestamp="route.routeTime"
            placement="top">
            <el-tag size="small" :type="routeStatusType(route.routeStatus)">
              {{ route.routeStatusName }}
            </el-tag>
            <span class="route-location">
              {{ route.location || "未知位置" }}
            </span>
            <p>{{ route.routeDesc || "-" }}</p>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无已保存的物流轨迹" />
        <el-divider content-position="left">供应商调用记录</el-divider>
        <el-table
          v-if="detail.calls?.length"
          :data="detail.calls"
          border
          size="small">
          <el-table-column label="供应商" prop="providerName" width="90" />
          <el-table-column
            label="快递公司"
            prop="carrier"
            width="120"
            show-overflow-tooltip>
            <template #default="{ row }">
              {{ getExpressCompanyName(row.carrier) || "-" }}
            </template>
          </el-table-column>
          <el-table-column label="单号" prop="trackingNoMasked" width="145" />
          <el-table-column
            label="结果"
            prop="availableName"
            width="90"
            align="center">
            <template #default="{ row }">
              <el-tag
                :type="Number(row.available) === 1 ? 'success' : 'danger'"
                size="small">
                {{ row.availableName }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            label="统一状态"
            prop="routeStatusName"
            width="95"
            align="center" />
          <el-table-column
            label="供应商状态"
            prop="providerStatus"
            width="100"
            align="center">
            <template #default="{ row }">
              {{ row.providerStatus || "-" }}
            </template>
          </el-table-column>
          <el-table-column
            label="异常分类"
            prop="errorTypeName"
            width="155"
            align="center" />
          <el-table-column label="耗时" width="85" align="right">
            <template #default="{ row }">
              {{ row.durationMs == null ? "-" : `${row.durationMs} ms` }}
            </template>
          </el-table-column>
          <el-table-column
            label="返回消息"
            prop="responseMessage"
            min-width="180"
            show-overflow-tooltip />
          <el-table-column label="时间" prop="createTime" width="165" />
        </el-table>
        <el-empty v-else description="暂无供应商调用记录" />
      </template>
      <el-skeleton v-else :rows="8" animated />
      <template #footer>
        <el-button
          v-if="
            detail?.logistics &&
            Number(detail.logistics.routeStatus) !== 2 &&
            Number(detail.logistics.routeStatus) !== 4
          "
          v-hasPermi="['platform:logistics:refresh']"
          type="primary"
          :loading="refreshingLogisticsId === detail.logistics.logisticsId"
          @click="refreshOne(detail.logistics, true)">
          刷新路线
        </el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="PlatformLogistics">
import { getCurrentInstance, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { Refresh, RefreshRight, Search } from "@element-plus/icons-vue";
import PageHeading from "@/components/PageHeading.vue";
import {
  getPlatformLogistics,
  listPlatformLogistics,
  refreshPlatformLogistics,
  refreshPlatformLogisticsItem,
} from "@/api/rental/logistics";
import { getExpressCompanyName } from "@/utils/rental/expressCompany";

const route = useRoute();
const { proxy } = getCurrentInstance();
const loading = ref(false);
const refreshing = ref(false);
const total = ref(0);
const rows = ref([]);
const detail = ref(null);
const detailVisible = ref(false);
const refreshingLogisticsId = ref(null);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: route.query.trackingNo ? String(route.query.trackingNo) : undefined,
  merchantKeyword: undefined,
  direction: undefined,
  routeStatus: undefined,
  abnormalOnly: false,
});
const directionOptions = [
  { value: 1, label: "发货" },
  { value: 2, label: "归还" },
  { value: 3, label: "调货" },
];
const routeStatusOptions = [
  { value: 0, label: "待处理" },
  { value: 1, label: "运输中" },
  { value: 2, label: "已签收" },
  { value: 3, label: "异常" },
  { value: 4, label: "已关闭" },
];

function getList() {
  loading.value = true;
  listPlatformLogistics({ ...queryParams })
    .then((response) => {
      rows.value = response.rows || [];
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
  Object.assign(queryParams, {
    pageNum: 1,
    keyword: undefined,
    merchantKeyword: undefined,
    direction: undefined,
    routeStatus: undefined,
    abnormalOnly: false,
  });
  getList();
}
function openDetail(row) {
  detailVisible.value = true;
  detail.value = null;
  getPlatformLogistics(row.logisticsId).then((response) => {
    detail.value = response.data || null;
  });
}
async function refreshRoutes() {
  refreshing.value = true;
  try {
    const response = await refreshPlatformLogistics();
    ElMessage.success(`已刷新 ${Number(response.refreshed || 0)} 条物流路线`);
    getList();
  } finally {
    refreshing.value = false;
  }
}

async function refreshOne(row, keepDetail = false) {
  if (!row?.logisticsId || refreshingLogisticsId.value) return;
  refreshingLogisticsId.value = row.logisticsId;
  try {
    await refreshPlatformLogisticsItem(row.logisticsId);
    ElMessage.success("物流路线已刷新");
    getList();
    if (keepDetail) {
      const response = await getPlatformLogistics(row.logisticsId);
      detail.value = response.data || null;
    }
  } finally {
    refreshingLogisticsId.value = null;
  }
}
function routeStatusType(value) {
  return (
    { 0: "info", 1: "primary", 2: "success", 3: "danger", 4: "warning" }[
      Number(value)
    ] || "info"
  );
}

getList();
</script>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}
.route-location {
  margin-left: 10px;
  color: var(--el-text-color-secondary);
}
.el-timeline p {
  margin: 8px 0 4px;
  white-space: pre-wrap;
}
</style>
