<template>
  <div class="app-container merchant-list-page sn-management-page">
    <div class="page-heading">
      <div>
        <h2>SN 管理</h2>
        <p>维护逐件设备的序列号、仓库和资产状态。</p>
      </div>
    </div>

    <el-form v-show="showSearch" :inline="true" :model="query" @submit.prevent>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="SN 编码、条码或商品"
          @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="商品">
        <el-select v-model="query.productId" clearable filterable placeholder="全部商品">
          <el-option
            v-for="item in productOptions"
            :key="item.productId"
            :label="productLabel(item)"
            :value="item.productId" />
        </el-select>
      </el-form-item>
      <el-form-item label="仓库">
        <el-select v-model="query.warehouseId" clearable filterable placeholder="全部仓库">
          <el-option
            v-for="item in warehouseOptions"
            :key="item.warehouseId"
            :label="warehouseLabel(item)"
            :value="item.warehouseId" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.assetStatus" clearable placeholder="全部状态">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="openCreate" v-hasPermi="['merchant:sn:add']">
          添加 SN
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadList" />
    </el-row>

    <el-table v-loading="loading" :data="rows" row-key="id" border>
      <el-table-column prop="snCode" label="SN 编码" min-width="180" />
      <el-table-column prop="barcode" label="条码" min-width="160" />
      <el-table-column label="商品" min-width="240" show-overflow-tooltip>
        <template #default="{ row }">
          <strong>{{ row.productName || "-" }}</strong>
          <span class="secondary-text">{{ row.productCode || "" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="仓库" min-width="170">
        <template #default="{ row }">{{ row.warehouseName || "-" }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.assetStatus)">{{ statusText(row.assetStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="成色" width="120" align="center">
        <template #default="{ row }">{{ row.conditionLevel || "-" }}</template>
      </el-table-column>
      <el-table-column label="操作" width="210" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="openDetail(row)" v-hasPermi="['merchant:sn:query']">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="openEdit(row)" v-hasPermi="['merchant:sn:edit']">编辑</el-button>
          <el-button link type="danger" icon="Delete" @click="removeRow(row)" v-hasPermi="['merchant:sn:remove']">删除</el-button>
        </template>
      </el-table-column>
      <template #empty><el-empty description="暂无 SN 记录" :image-size="76" /></template>
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="loadList" />

    <el-dialog v-model="detailVisible" title="SN 详情" width="640px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border label-width="120">
        <el-descriptions-item label="SN 编码">{{ detail.snCode || "-" }}</el-descriptions-item>
        <el-descriptions-item label="条码">{{ detail.barcode || "-" }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ detail.productName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="商品编码">{{ detail.productCode || "-" }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ detail.warehouseName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="所在城市">{{ detail.warehouseCity || "-" }}</el-descriptions-item>
        <el-descriptions-item label="资产状态">
          <el-tag :type="statusType(detail.assetStatus)">{{ statusText(detail.assetStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="成色">{{ detail.conditionLevel || "-" }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || "-" }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog
      v-model="formVisible"
      :title="editing ? '编辑 SN' : '添加 SN'"
      width="640px"
      append-to-body
      destroy-on-close
      @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" filterable placeholder="请选择逐件商品" class="field-control">
            <el-option
              v-for="item in productOptions"
              :key="item.productId"
              :label="productLabel(item)"
              :value="item.productId" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" filterable placeholder="请选择仓库" class="field-control">
            <el-option
              v-for="item in warehouseOptions"
              :key="item.warehouseId"
              :label="warehouseLabel(item)"
              :value="item.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="SN 编码" prop="snCode">
          <el-input v-model="form.snCode" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="条码" prop="barcode">
          <el-input v-model="form.barcode" maxlength="128" />
        </el-form-item>
        <el-form-item label="资产状态" prop="assetStatus">
          <el-select v-model="form.assetStatus" class="field-control">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="成色" prop="conditionLevel">
          <el-input v-model="form.conditionLevel" maxlength="32" placeholder="例如：全新、99 新" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  addMerchantSn,
  deleteMerchantSn,
  getMerchantSn,
  listMerchantProducts,
  listMerchantSns,
  listMerchantWarehouses,
  updateMerchantSn,
} from "@/api/merchant/rentalBusiness";
import { RENTAL_CODE } from "@/utils/rental/options";

const loading = ref(false);
const saving = ref(false);
const showSearch = ref(true);
const rows = ref([]);
const total = ref(0);
const productOptions = ref([]);
const warehouseOptions = ref([]);
const detailVisible = ref(false);
const detail = ref(null);
const formVisible = ref(false);
const editing = ref(false);
const formRef = ref();
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  productId: undefined,
  warehouseId: undefined,
  assetStatus: undefined,
});
const form = reactive({
  id: undefined,
  productId: undefined,
  warehouseId: undefined,
  snCode: "",
  barcode: "",
  assetStatus: RENTAL_CODE.INVENTORY_STATUS.AVAILABLE,
  conditionLevel: "",
  remark: "",
});
const rules = {
  productId: [{ required: true, message: "请选择商品", trigger: "change" }],
  warehouseId: [{ required: true, message: "请选择仓库", trigger: "change" }],
  snCode: [{ required: true, message: "请输入 SN 编码", trigger: "blur" }],
  assetStatus: [{ required: true, message: "请选择资产状态", trigger: "change" }],
};
const statusOptions = [
  { label: "可用", value: RENTAL_CODE.INVENTORY_STATUS.AVAILABLE },
  { label: "已预占", value: RENTAL_CODE.INVENTORY_STATUS.RESERVED },
  { label: "出租中", value: RENTAL_CODE.INVENTORY_STATUS.RENTED },
  { label: "维修中", value: RENTAL_CODE.INVENTORY_STATUS.REPAIRING },
  { label: "已丢失", value: RENTAL_CODE.INVENTORY_STATUS.LOST },
  { label: "已退役", value: RENTAL_CODE.INVENTORY_STATUS.RETIRED },
];

function responseData(response) {
  return response?.data ?? response;
}

function responseRows(response) {
  const payload = responseData(response);
  return payload?.rows || [];
}

function optionId(item, primary, fallback) {
  return Number(item?.[primary] ?? item?.[fallback]);
}

function productLabel(item) {
  return item.productCode ? `${item.productName}（${item.productCode}）` : item.productName;
}

function warehouseLabel(item) {
  return item.warehouseCode ? `${item.warehouseName}（${item.warehouseCode}）` : item.warehouseName;
}

function statusText(value) {
  return statusOptions.find((item) => item.value === Number(value))?.label || "未知状态";
}

function statusType(value) {
  return {
    [RENTAL_CODE.INVENTORY_STATUS.AVAILABLE]: "success",
    [RENTAL_CODE.INVENTORY_STATUS.RESERVED]: "warning",
    [RENTAL_CODE.INVENTORY_STATUS.RENTED]: "primary",
    [RENTAL_CODE.INVENTORY_STATUS.REPAIRING]: "warning",
    [RENTAL_CODE.INVENTORY_STATUS.LOST]: "danger",
    [RENTAL_CODE.INVENTORY_STATUS.RETIRED]: "info",
  }[Number(value)] || "info";
}

async function loadList() {
  loading.value = true;
  try {
    const response = await listMerchantSns(query);
    rows.value = responseRows(response);
    total.value = Number(response?.total ?? responseData(response)?.total ?? 0);
  } finally {
    loading.value = false;
  }
}

async function loadOptions() {
  const [productsResponse, warehousesResponse] = await Promise.all([
    listMerchantProducts({ pageNum: 1, pageSize: 1000 }),
    listMerchantWarehouses({ pageNum: 1, pageSize: 1000, status: 0 }),
  ]);
  productOptions.value = responseRows(productsResponse)
    .filter((item) => Number(item.stockMode) === RENTAL_CODE.STOCK_MODE.SERIAL)
    .map((item) => ({
      ...item,
      productId: optionId(item, "productId", "id"),
    }));
  warehouseOptions.value = responseRows(warehousesResponse).map((item) => ({
    ...item,
    warehouseId: optionId(item, "warehouseId", "id"),
    warehouseName: item.warehouseName || item.name,
    warehouseCode: item.warehouseCode || item.code,
  }));
}

function search() {
  query.pageNum = 1;
  loadList();
}

function resetQuery() {
  Object.assign(query, {
    pageNum: 1,
    pageSize: 10,
    keyword: "",
    productId: undefined,
    warehouseId: undefined,
    assetStatus: undefined,
  });
  loadList();
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    productId: undefined,
    warehouseId: undefined,
    snCode: "",
    barcode: "",
    assetStatus: RENTAL_CODE.INVENTORY_STATUS.AVAILABLE,
    conditionLevel: "",
    remark: "",
  });
  formRef.value?.clearValidate();
}

function openCreate() {
  editing.value = false;
  resetForm();
  formVisible.value = true;
}

async function openEdit(row) {
  const response = await getMerchantSn(row.id);
  const record = responseData(response);
  editing.value = true;
  Object.assign(form, {
    id: record.id,
    productId: record.productId,
    warehouseId: record.warehouseId,
    snCode: record.snCode || "",
    barcode: record.barcode || "",
    assetStatus: Number(record.assetStatus),
    conditionLevel: record.conditionLevel || "",
    remark: record.remark || "",
  });
  if (!productOptions.value.some((item) => item.productId === Number(record.productId))) {
    productOptions.value.push({
      productId: Number(record.productId),
      productName: record.productName,
      productCode: record.productCode,
      stockMode: RENTAL_CODE.STOCK_MODE.SERIAL,
    });
  }
  formVisible.value = true;
}

async function openDetail(row) {
  const response = await getMerchantSn(row.id);
  detail.value = responseData(response);
  detailVisible.value = true;
}

async function submitForm() {
  if (!(await formRef.value?.validate().catch(() => false))) return;
  saving.value = true;
  try {
    const action = editing.value ? updateMerchantSn : addMerchantSn;
    await action({
      ...form,
      snCode: form.snCode.trim(),
      barcode: form.barcode?.trim() || null,
      conditionLevel: form.conditionLevel?.trim() || null,
      remark: form.remark?.trim() || null,
    });
    ElMessage.success(editing.value ? "SN 已更新" : "SN 已添加");
    formVisible.value = false;
    await loadList();
  } finally {
    saving.value = false;
  }
}

async function removeRow(row) {
  await ElMessageBox.confirm(`确定删除 SN“${row.snCode}”吗？`, "删除确认", { type: "warning" });
  await deleteMerchantSn(row.id);
  ElMessage.success("SN 已删除");
  await loadList();
}

onMounted(async () => {
  await Promise.all([loadOptions(), loadList()]);
});
</script>

<style scoped>
.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.page-heading h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.page-heading p,
.secondary-text {
  margin: 5px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.secondary-text {
  display: block;
  margin-top: 3px;
}

.field-control {
  width: 100%;
}
</style>
