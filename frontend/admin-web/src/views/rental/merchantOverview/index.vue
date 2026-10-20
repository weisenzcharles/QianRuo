<template>
  <div class="app-container">
    <el-form
      ref="queryRef"
      v-show="showSearch"
      :model="queryParams"
      :inline="true"
      @submit.prevent>
      <el-form-item label="商户编码" prop="memberCode">
        <el-input
          v-model="queryParams.memberCode"
          clearable
          placeholder="请输入商户编码"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="商户名称" prop="nickname">
        <el-input
          v-model="queryParams.nickname"
          clearable
          placeholder="请输入商户名称"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="主体名称" prop="legalSubjectName">
        <el-input
          v-model="queryParams.legalSubjectName"
          clearable
          placeholder="请输入主体名称"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="商户类型" prop="memberType">
        <el-select
          v-model="queryParams.memberType"
          clearable
          placeholder="全部">
          <el-option
            v-for="item in rentalMemberTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="认证状态" prop="authStatus">
        <el-select
          v-model="queryParams.authStatus"
          clearable
          placeholder="全部">
          <el-option
            v-for="item in rentalAuthStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="冻结状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="全部">
          <el-option label="未冻结" :value="NORMAL_DISABLE_CODE.NORMAL" />
          <el-option label="已冻结" :value="NORMAL_DISABLE_CODE.DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">
          查询
        </el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="merchantList" border>
      <el-table-column
        label="商户名称"
        prop="merchantName"
        min-width="160"
        show-overflow-tooltip
        fixed="left" />
      <el-table-column
        label="主体名称"
        prop="legalSubjectName"
        min-width="180"
        show-overflow-tooltip />
      <el-table-column label="商户编码" prop="memberCode" min-width="140" />
      <el-table-column
        label="认证状态"
        prop="authStatus"
        width="100"
        align="center">
        <template #default="scope">
          <el-tag
            :type="
              findRentalOption(rentalAuthStatusOptions, scope.row.authStatus)
                .tagType
            ">
            {{
              findRentalOption(rentalAuthStatusOptions, scope.row.authStatus)
                .label
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="冻结状态"
        prop="status"
        width="100"
        align="center">
        <template #default="scope">
          <el-tag
            :type="
              Number(scope.row.status) === NORMAL_DISABLE_CODE.DISABLED
                ? 'danger'
                : 'success'
            ">
            {{
              Number(scope.row.status) === NORMAL_DISABLE_CODE.DISABLED
                ? "已冻结"
                : "未冻结"
            }}
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
        label="预占库存"
        prop="reservedInventoryQuantity"
        width="100"
        align="right" />
      <el-table-column
        label="出租库存"
        prop="rentedInventoryQuantity"
        width="100"
        align="right" />
      <el-table-column
        label="维修库存"
        prop="repairingInventoryQuantity"
        width="100"
        align="right" />
      <el-table-column
        label="损坏库存"
        prop="damagedInventoryQuantity"
        width="100"
        align="right" />
      <el-table-column
        label="待处理订单"
        prop="pendingOrderCount"
        width="110"
        align="right" />
      <el-table-column
        label="进行中订单"
        prop="activeOrderCount"
        width="110"
        align="right" />
      <el-table-column
        label="已完成订单"
        prop="completedOrderCount"
        width="110"
        align="right" />
      <el-table-column
        label="逾期订单"
        prop="overdueOrderCount"
        width="100"
        align="right" />
      <el-table-column
        label="注册时间"
        prop="createTime"
        width="180"
        align="center">
        <template #default="scope">
          {{ parseTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right" align="center">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="View"
            @click="openBusiness(scope.row)">
            经营明细
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

    <el-drawer
      v-model="businessOpen"
      title="商户经营明细"
      size="75%"
      destroy-on-close>
      <template #header>
        <div class="business-title">
          <span>{{ businessDetail.merchantName || "商户经营明细" }}</span>
          <el-tag v-if="businessDetail.memberCode" type="info">
            {{ businessDetail.memberCode }}
          </el-tag>
        </div>
      </template>
      <el-tabs v-model="businessTab">
        <el-tab-pane
          v-if="canViewGovernance"
          label="履约与风险"
          name="governance">
          <el-descriptions
            title="履约质量"
            :column="3"
            border
            label-width="120">
            <el-descriptions-item label="订单总数">
              {{ businessDetail.governance.totalOrderCount }}
            </el-descriptions-item>
            <el-descriptions-item label="待处理订单">
              {{ businessDetail.governance.pendingOrderCount }}
            </el-descriptions-item>
            <el-descriptions-item label="进行中订单">
              {{ businessDetail.governance.activeOrderCount }}
            </el-descriptions-item>
            <el-descriptions-item label="已完成订单">
              {{ businessDetail.governance.completedOrderCount }}
            </el-descriptions-item>
            <el-descriptions-item label="逾期订单">
              {{ businessDetail.governance.overdueOrderCount }}
            </el-descriptions-item>
            <el-descriptions-item label="售后案件">
              {{ businessDetail.governance.afterSalesCaseCount }}
            </el-descriptions-item>
            <el-descriptions-item label="处理中售后">
              {{ businessDetail.governance.openAfterSalesCaseCount }}
            </el-descriptions-item>
          </el-descriptions>
          <el-descriptions
            title="物流治理"
            :column="3"
            border
            label-width="120"
            class="governance-section">
            <el-descriptions-item label="物流单总数">
              {{ businessDetail.governance.logisticsCount }}
            </el-descriptions-item>
            <el-descriptions-item label="待完成物流">
              {{ businessDetail.governance.pendingRouteCount }}
            </el-descriptions-item>
            <el-descriptions-item label="异常物流">
              {{ businessDetail.governance.abnormalLogisticsCount }}
            </el-descriptions-item>
          </el-descriptions>
          <el-descriptions
            title="风险明细"
            :column="3"
            border
            label-width="120"
            class="governance-section">
            <el-descriptions-item label="未解除库存告警">
              {{ businessDetail.governance.openInventoryAlertCount }}
            </el-descriptions-item>
            <el-descriptions-item label="未结联盟纠纷">
              {{ businessDetail.governance.openAllianceDisputeCount }}
            </el-descriptions-item>
            <el-descriptions-item label="数据范围">
              <span v-if="governanceLoading">查询中</span>
              <span v-else>当前商户</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="仓库" name="warehouses">
          <el-table
            v-loading="businessLoading || governanceLoading"
            :data="businessDetail.warehouses"
            border>
            <el-table-column
              label="仓库编码"
              prop="warehouseCode"
              min-width="140" />
            <el-table-column
              label="仓库名称"
              prop="warehouseName"
              min-width="180"
              show-overflow-tooltip />
            <el-table-column label="城市" prop="city" min-width="120" />
            <el-table-column label="归还仓" width="100" align="center">
              <template #default="scope">
                {{ yesNoLabel(scope.row.returnEnabled) }}
              </template>
            </el-table-column>
            <el-table-column label="联盟共享" width="100" align="center">
              <template #default="scope">
                {{ yesNoLabel(scope.row.allianceShareEnabled) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="scope">
                <el-tag :type="enableTag(scope.row.status)">
                  {{ enableLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty
            v-if="!businessLoading && !businessDetail.warehouses.length"
            description="暂无仓库数据" />
        </el-tab-pane>
        <el-tab-pane label="商品" name="products">
          <el-table
            v-loading="businessLoading || governanceLoading"
            :data="businessDetail.products"
            border>
            <el-table-column
              label="商品编码"
              prop="productCode"
              min-width="140" />
            <el-table-column
              label="商品名称"
              prop="productName"
              min-width="180"
              show-overflow-tooltip />
            <el-table-column label="商品类型" width="100" align="center">
              <template #default="scope">
                {{ productTypeLabel(scope.row.productType) }}
              </template>
            </el-table-column>
            <el-table-column label="品牌" prop="brand" min-width="120" />
            <el-table-column label="型号" prop="modelName" min-width="140" />
            <el-table-column label="库存模式" width="100" align="center">
              <template #default="scope">
                {{ stockModeLabel(scope.row.stockMode) }}
              </template>
            </el-table-column>
            <el-table-column label="自定义" width="90" align="center">
              <template #default="scope">
                {{ yesNoLabel(scope.row.customFlag) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="scope">
                <el-tag :type="enableTag(scope.row.status)">
                  {{ enableLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty
            v-if="!businessLoading && !businessDetail.products.length"
            description="暂无商品数据" />
        </el-tab-pane>
        <el-tab-pane label="套餐" name="packages">
          <el-table
            v-loading="businessLoading || governanceLoading"
            :data="businessDetail.packages"
            border>
            <el-table-column
              label="套餐编码"
              prop="packageCode"
              min-width="140" />
            <el-table-column
              label="套餐名称"
              prop="packageName"
              min-width="180"
              show-overflow-tooltip />
            <el-table-column label="主型号" prop="modelName" min-width="140" />
            <el-table-column label="来源" width="100" align="center">
              <template #default="scope">
                {{ packageSourceLabel(scope.row.sourceType) }}
              </template>
            </el-table-column>
            <el-table-column label="版本" width="80" align="center">
              <template #default="scope">
                {{ scope.row.versionNo || "-" }}
              </template>
            </el-table-column>
            <el-table-column label="版本状态" width="100" align="center">
              <template #default="scope">
                {{ packageVersionLabel(scope.row.versionStatus) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="scope">
                <el-tag :type="enableTag(scope.row.status)">
                  {{ enableLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty
            v-if="!businessLoading && !businessDetail.packages.length"
            description="暂无套餐数据" />
        </el-tab-pane>
        <el-tab-pane label="库存" name="inventories">
          <el-table
            v-loading="businessLoading || governanceLoading"
            :data="businessDetail.inventories"
            border>
            <el-table-column
              label="仓库"
              prop="warehouseName"
              min-width="150"
              show-overflow-tooltip />
            <el-table-column
              label="商品编码"
              prop="productCode"
              min-width="140" />
            <el-table-column
              label="商品名称"
              prop="productName"
              min-width="180"
              show-overflow-tooltip />
            <el-table-column label="商品类型" width="100" align="center">
              <template #default="scope">
                {{ productTypeLabel(scope.row.productType) }}
              </template>
            </el-table-column>
            <el-table-column
              label="总库存"
              prop="totalQuantity"
              width="90"
              align="right" />
            <el-table-column
              label="可用"
              prop="availableQuantity"
              width="80"
              align="right" />
            <el-table-column
              label="预占"
              prop="reservedQuantity"
              width="80"
              align="right" />
            <el-table-column
              label="出租"
              prop="rentedQuantity"
              width="80"
              align="right" />
            <el-table-column
              label="维修"
              prop="repairingQuantity"
              width="80"
              align="right" />
            <el-table-column
              label="损坏"
              prop="damagedQuantity"
              width="80"
              align="right" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="scope">
                <el-tag :type="enableTag(scope.row.status)">
                  {{ enableLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty
            v-if="!businessLoading && !businessDetail.inventories.length"
            description="暂无库存数据" />
        </el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<script setup name="PlatformMerchantOverview">
import { useRoute } from "vue-router";
import {
  getPlatformMerchantGovernance,
  getPlatformMerchantBusiness,
  listPlatformMerchantOverviews,
} from "@/api/rental/merchantOverview";
import {
  findRentalOption,
  rentalAuthStatusOptions,
  rentalMemberTypeOptions,
} from "@/utils/rental/options";
import { checkPermi } from "@/utils/permission";
import { NORMAL_DISABLE_CODE } from "@/utils/stateCode";

const route = useRoute();
const { proxy } = getCurrentInstance();
const loading = ref(false);
const showSearch = ref(true);
const merchantList = ref([]);
const total = ref(0);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  memberCode: route.query.memberCode
    ? String(route.query.memberCode)
    : undefined,
  nickname: undefined,
  legalSubjectName: undefined,
  memberType: undefined,
  authStatus: undefined,
  status: undefined,
});
const businessOpen = ref(false);
const businessLoading = ref(false);
const governanceLoading = ref(false);
const businessTab = ref("warehouses");
const businessDetail = ref(emptyBusinessDetail());
const canViewGovernance = checkPermi([
  "platform:merchantOverview:governance:view",
]);

function getList() {
  loading.value = true;
  listPlatformMerchantOverviews(queryParams)
    .then((response) => {
      merchantList.value = response.rows || [];
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
  handleQuery();
}

function emptyBusinessDetail() {
  return {
    merchantName: "",
    memberCode: "",
    warehouses: [],
    products: [],
    packages: [],
    inventories: [],
    governance: emptyGovernance(),
  };
}

function emptyGovernance() {
  return {
    totalOrderCount: 0,
    pendingOrderCount: 0,
    activeOrderCount: 0,
    completedOrderCount: 0,
    overdueOrderCount: 0,
    afterSalesCaseCount: 0,
    openAfterSalesCaseCount: 0,
    logisticsCount: 0,
    pendingRouteCount: 0,
    abnormalLogisticsCount: 0,
    openInventoryAlertCount: 0,
    openAllianceDisputeCount: 0,
  };
}

function openBusiness(row) {
  businessOpen.value = true;
  businessTab.value = "warehouses";
  businessDetail.value = {
    ...emptyBusinessDetail(),
    merchantName: row.merchantName,
    memberCode: row.memberCode,
  };
  businessLoading.value = true;
  governanceLoading.value = canViewGovernance;
  const requests = [getPlatformMerchantBusiness(row.memberCode)];
  if (canViewGovernance) {
    requests.push(getPlatformMerchantGovernance(row.memberCode));
  }
  Promise.allSettled(requests)
    .then(([businessResult, governanceResult]) => {
      if (businessResult.status === "fulfilled") {
        const detail =
          businessResult.value?.data &&
          typeof businessResult.value.data === "object"
            ? businessResult.value.data
            : businessResult.value;
        businessDetail.value = {
          ...emptyBusinessDetail(),
          ...(detail || {}),
        };
      }
      if (governanceResult?.status === "fulfilled") {
        const governance =
          governanceResult.value?.data &&
          typeof governanceResult.value.data === "object"
            ? governanceResult.value.data
            : governanceResult.value;
        businessDetail.value = {
          ...businessDetail.value,
          governance: { ...emptyGovernance(), ...(governance || {}) },
        };
      }
    })
    .finally(() => {
      businessLoading.value = false;
      governanceLoading.value = false;
    });
}

function yesNoLabel(value) {
  return Number(value) === 1 ? "是" : "否";
}

function enableLabel(value) {
  return Number(value) === NORMAL_DISABLE_CODE.DISABLED ? "停用" : "启用";
}

function enableTag(value) {
  return Number(value) === NORMAL_DISABLE_CODE.DISABLED ? "danger" : "success";
}

function productTypeLabel(value) {
  return ["2", "accessory"].includes(String(value)) ? "配件" : "设备";
}

function stockModeLabel(value) {
  return ["2", "qty", "quantity"].includes(String(value)) ? "数量" : "序列号";
}

function packageSourceLabel(value) {
  return Number(value) === 1 ? "平台套餐" : "自定义套餐";
}

function packageVersionLabel(value) {
  const labels = { 0: "草稿", 1: "生效", 2: "废弃" };
  return labels[Number(value)] || "-";
}

getList();
</script>

<style scoped>
.business-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.governance-section {
  margin-top: 20px;
}
</style>
