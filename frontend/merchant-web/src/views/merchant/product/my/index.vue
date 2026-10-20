<template>
  <div class="app-container merchant-list-page merchant-product-page">
    <div class="page-heading">
      <div>
        <h2>
          {{
            accessoryOnly ? "我的配件" : createOnly ? "发布商品" : "商品管理"
          }}
        </h2>
        <p>
          {{
            accessoryOnly
              ? "维护当前商户自定义配件，编码由系统统一生成。"
              : createOnly
                ? "创建当前商户可经营的商品 SKU。"
                : "维护当前商户的商品 SKU，编码由系统统一生成。"
          }}
        </p>
      </div>
    </div>

    <el-alert v-if="!optionLoading && classOptions.length === 0" type="warning" :closable="false" class="option-alert">
      <template #title>
        <div class="option-alert-content">
          <span>请先在经营分类中选择可经营的产品分类</span>
          <el-button v-hasPermi="['merchant:class:list']" link type="primary" @click="openClassSettings">
            前往设置
          </el-button>
        </div>
      </template>
    </el-alert>

    <el-form v-if="!createOnly" v-show="showSearch" :inline="true" :model="query" @submit.prevent>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="商品编码、名称、品牌或型号" @keyup.enter="loadProducts" />
      </el-form-item>
      <el-form-item label="经营分类">
        <el-select v-model="query.classId" clearable filterable placeholder="全部分类" @change="loadProducts">
          <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="库存模式">
        <el-select v-model="query.stockMode" clearable placeholder="全部" @change="loadProducts">
          <el-option v-for="item in stockModeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" @change="loadProducts">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="loadProducts">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row v-if="!createOnly" :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button v-hasPermi="[
        accessoryOnly ? 'merchant:accessory:add' : 'merchant:product:add',
      ]" type="primary" icon="Plus" @click="openCreate">{{ accessoryOnly ? "新增配件" : "新增商品" }}</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadProducts" />
    </el-row>

    <template v-if="!createOnly">
      <el-table v-loading="loading" :data="rows" row-key="id" border @row-dblclick="openDetail">
        <el-table-column prop="productCode" :label="accessoryOnly ? '配件编码' : '商品编码'" min-width="150"
          show-overflow-tooltip />
        <el-table-column prop="productName" :label="accessoryOnly ? '配件名称' : '商品名称'" min-width="180"
          show-overflow-tooltip />
        <el-table-column label="所属分类" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            {{ classLabel(scope.row.classId) }}
          </template>
        </el-table-column>
        <el-table-column :label="accessoryOnly ? '兼容型号' : '标准型号'" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            {{ modelLabel(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column v-if="accessoryOnly" label="类型" width="110" align="center">
          <template #default>自定义配件</template>
        </el-table-column>
        <el-table-column v-if="!accessoryOnly" label="商品类型" width="100">
          <template #default="scope">
            {{ productTypeLabel(scope.row.productType) }}
          </template>
        </el-table-column>
        <el-table-column v-if="!accessoryOnly" label="库存模式" width="110">
          <template #default="scope">
            {{ stockModeLabel(scope.row.stockMode) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="Number(scope.row.status) === NORMAL_DISABLE_CODE.NORMAL
              ? 'success'
              : 'info'
              ">
              {{
                Number(scope.row.status) === NORMAL_DISABLE_CODE.NORMAL
                  ? "启用"
                  : "停用"
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="210">
          <template #default="scope">
            <el-button v-hasPermi="[
              accessoryOnly
                ? 'merchant:accessory:query'
                : 'merchant:product:query',
            ]" link type="primary" @click="openDetail(scope.row)">
              详情
            </el-button>
            <el-button v-hasPermi="[
              accessoryOnly
                ? 'merchant:accessory:edit'
                : 'merchant:product:edit',
            ]" link type="primary" @click="openEdit(scope.row)">
              编辑
            </el-button>
            <el-button v-hasPermi="[
              accessoryOnly
                ? 'merchant:accessory:remove'
                : 'merchant:product:remove',
            ]" link type="danger" @click="removeProduct(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total"
        @pagination="loadProducts" />
    </template>

    <el-drawer v-model="detailVisible" title="商品详情" size="520px" destroy-on-close>
      <el-skeleton v-if="detailLoading" :rows="8" animated />
      <el-descriptions v-else :column="1" border label-width="120">
        <el-descriptions-item label="商品编码">{{
          detail.productCode || "-"
          }}</el-descriptions-item>
        <el-descriptions-item v-if="accessoryOnly" label="类型">自定义配件</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{
          detail.productName || "-"
          }}</el-descriptions-item>
        <el-descriptions-item label="经营分类">{{
          classLabel(detail.classId)
          }}</el-descriptions-item>
        <el-descriptions-item label="标准型号">{{
          modelLabel(detail)
          }}</el-descriptions-item>
        <el-descriptions-item label="品牌">
          {{ detail.brand || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="商品型号">
          {{ detail.modelName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="商品类型">{{
          productTypeLabel(detail.productType)
          }}</el-descriptions-item>
        <el-descriptions-item label="库存模式">{{
          stockModeLabel(detail.stockMode)
          }}</el-descriptions-item>

        <!-- <el-descriptions-item label="规格">{{
          detail.memorySpec || "-"
          }}</el-descriptions-item>
        <el-descriptions-item label="采购成本">{{
          money(detail.purchaseCost)
          }}</el-descriptions-item>
        <el-descriptions-item label="销售价格">{{
          money(detail.salePrice)
          }}</el-descriptions-item> -->
        <el-descriptions-item label="状态">{{
          Number(detail.status) === NORMAL_DISABLE_CODE.NORMAL ? "启用" : "停用"
          }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{
          detail.remark || "-"
          }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <el-dialog v-model="formVisible" :title="editing
      ? accessoryOnly
        ? '修改配件'
        : '修改商品'
      : accessoryOnly
        ? '新增配件'
        : createOnly
          ? '发布商品'
          : '新增商品'
      " width="640px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="112px">
        <el-row :gutter="16">
          <el-col :span="18">
            <el-form-item v-if="editing" :label="accessoryOnly ? '配件编码' : '商品编码'">
              <el-input :model-value="form.productCode || '保存后由系统生成'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="18">
            <el-form-item :label="accessoryOnly ? '配件名称' : '商品名称'" prop="productName">
              <el-input v-model="form.productName" maxlength="128" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="18">
            <el-form-item label="分类" prop="classId">
              <el-select v-model="form.classId" filterable placeholder="请选择分类" class="field-control"
                @change="handleClassChange">
                <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="18">
            <el-form-item label="品牌">
              <el-select v-model="form.brand" clearable filterable :disabled="!form.classId" placeholder="请选择品牌"
                class="field-control" @change="handleBrandChange">
                <el-option v-for="brand in brandOptions" :key="brand" :label="brand" :value="brand" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="18">
            <el-form-item label="型号" prop="modelId">
              <el-select v-model="form.modelId" clearable filterable :disabled="!form.classId || !form.brand"
                :placeholder="accessoryOnly ? '请选择兼容型号' : '请选择标准型号'" class="field-control" @change="handleModelChange">
                <el-option v-for="item in filteredModels" :key="item.modelId" :label="modelOptionLabel(item)"
                  :value="item.modelId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="18">
            <el-form-item v-if="!accessoryOnly" label="商品类型" prop="productType">
              <el-select v-model="form.productType" placeholder="请选择商品类型" class="field-control"
                @change="handleProductTypeChange">
                <el-option v-for="item in productTypeOptions" :key="item.value" :label="item.label"
                  :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <!-- <el-col :span="12">
            <el-form-item v-if="!accessoryOnly" label="商品型号">
              <el-input v-model="form.modelName" maxlength="128" />
            </el-form-item>
          </el-col> -->
          <el-col :span="18">
            <el-form-item v-if="!accessoryOnly" label="库存模式" prop="stockMode">
              <el-select v-model="form.stockMode" placeholder="请选择库存模式" class="field-control"
                @change="handleStockModeChange">
                <el-option v-for="item in stockModeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="
            !editing &&
            !accessoryOnly &&
            Number(form.productType) === RENTAL_CODE.PRODUCT_TYPE.DEVICE
          " :span="24">
            <el-form-item label="库存设置">
              <el-checkbox v-model="form.setInventory">
                是否设置库存
              </el-checkbox>
            </el-form-item>
          </el-col>
          <el-col v-if="
            inventorySetupVisible &&
            Number(form.stockMode) === RENTAL_CODE.STOCK_MODE.SERIAL
          " :span="24">
            <el-form-item label="设置方式">
              <el-radio-group v-model="form.inventoryInputMode">
                <el-radio value="manual">录入设备</el-radio>
                <el-radio value="import">导入设备</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <template v-if="
            inventorySetupVisible &&
            Number(form.stockMode) === RENTAL_CODE.STOCK_MODE.QUANTITY
          ">
            <el-col :span="12">
              <el-form-item label="选择仓库" prop="inventoryWarehouseId">
                <el-select v-model="form.inventoryWarehouseId" filterable clearable placeholder="请选择仓库"
                  class="field-control">
                  <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="数量" prop="inventoryQuantity">
                <el-input-number v-model="form.inventoryQuantity" :min="1" :max="5000" :step="1"
                  controls-position="right" class="field-control" />
              </el-form-item>
            </el-col>
          </template>
          <template v-if="
            inventorySetupVisible &&
            Number(form.stockMode) === RENTAL_CODE.STOCK_MODE.SERIAL &&
            form.inventoryInputMode === 'manual'
          ">
            <el-col :span="12">
              <el-form-item label="选择仓库" prop="inventoryWarehouseId">
                <el-select v-model="form.inventoryWarehouseId" filterable clearable placeholder="请选择仓库"
                  class="field-control">
                  <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="20">
              <el-form-item label="序列号" prop="inventorySerialText">
                <el-input v-model="form.inventorySerialText" type="textarea" :rows="5"
                  placeholder="每行一个序列号，也支持逗号、空格分隔" />
              </el-form-item>
            </el-col>
            <el-col :span="20">
              <el-form-item label="设备备注">
                <el-input v-model="form.inventoryRemark" type="textarea" :rows="2" maxlength="500" show-word-limit
                  placeholder="请输入设备备注" />
              </el-form-item>
            </el-col>
          </template>
          <el-col v-if="inventorySetupVisible && form.inventoryInputMode === 'import'" :span="20">
            <el-form-item label="文件导入" class="import-form-item">
              <div class="import-upload-content">
                <div class="import-upload-actions">
                  <div class="import-upload-trigger">
                    <el-upload class="import-uploader" :auto-upload="false" :limit="1" accept=".xls,.xlsx,.csv"
                      :show-file-list="true" :on-change="handleImportFileChange" :on-remove="clearImportFile">
                      <el-button type="primary">选择文件</el-button>
                    </el-upload>
                    <span class="import-upload-tip">上传导入的文件</span>
                  </div>
                  <el-link type="primary" :underline="false" class="import-template-link"
                    @click="downloadImportTemplate">
                    批量导入设备模板.xlsx
                  </el-link>
                </div>
                <el-alert description="格式为 xls、xlsx、csv 文件，最多导入 1000 条。请先下载模板并按模板填写设备信息。" type="warning"
                  :closable="false" show-icon class="import-tip" />
              </div>
            </el-form-item>
          </el-col>

          <!-- <el-col :span="12">
            <el-form-item label="规格">
              <el-input v-model="form.memorySpec" maxlength="64" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="!accessoryOnly" label="采购成本">
              <el-input-number v-model="form.purchaseCost" :min="0" :precision="2" :step="0.01"
                controls-position="right" class="field-control" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="!accessoryOnly" label="销售价格">
              <el-input-number v-model="form.salePrice" :min="0" :precision="2" :step="0.01" controls-position="right"
                class="field-control" />
            </el-form-item>
          </el-col> -->
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch v-model="form.status" :active-value="NORMAL_DISABLE_CODE.NORMAL"
                :inactive-value="NORMAL_DISABLE_CODE.DISABLED" active-text="启用" inactive-text="停用" inline-prompt />
            </el-form-item>
          </el-col>
          <el-col :span="20">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button v-hasPermi="[
          accessoryOnly
            ? editing
              ? 'merchant:accessory:edit'
              : 'merchant:accessory:add'
            : editing
              ? 'merchant:product:edit'
              : 'merchant:product:add',
        ]" type="primary" :loading="saving" @click="submitForm">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { saveAs } from "file-saver";
import { useRouter } from "vue-router";
import {
  addMerchantProduct,
  deleteMerchantProduct,
  getMerchantProduct,
  listMerchantProducts,
  listMerchantWarehouses,
  updateMerchantProduct,
} from "@/api/merchant/rentalBusiness";
import { addMerchantInventoryBatch } from "@/api/merchant/inventory";
import {
  exportMerchantResource,
  importMerchantResource,
} from "@/api/merchant/batch";
import { listBrandModelOptions } from "@/api/merchant/brandModel";
import {
  listBusinessProductClasses,
  listSelectedProductClassIds,
} from "@/api/merchant/productClass";
import { NORMAL_DISABLE_CODE } from "@/constants/options";
import { RENTAL_CODE } from "@/utils/rental/options";

const props = defineProps({
  createOnly: {
    type: Boolean,
    default: false,
  },
  accessoryOnly: {
    type: Boolean,
    default: false,
  },
});

const router = useRouter();

const statusOptions = [
  { label: "启用", value: NORMAL_DISABLE_CODE.NORMAL },
  { label: "停用", value: NORMAL_DISABLE_CODE.DISABLED },
];
const productTypeOptions = [
  { label: "设备", value: RENTAL_CODE.PRODUCT_TYPE.DEVICE },
  { label: "配件", value: RENTAL_CODE.PRODUCT_TYPE.ACCESSORY },
];
const stockModeOptions = [
  { label: "逐件", value: RENTAL_CODE.STOCK_MODE.SERIAL },
  { label: "数量", value: RENTAL_CODE.STOCK_MODE.QUANTITY },
];
const formRef = ref(null);
const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const showSearch = ref(true);
const saving = ref(false);
const optionLoading = ref(false);
const formVisible = ref(false);
const detailVisible = ref(false);
const detailLoading = ref(false);
const editing = ref(false);
const classTree = ref([]);
const selectedClassIds = ref(new Set());
const modelOptions = ref([]);
const warehouseOptions = ref([]);
const importFile = ref(null);
const detail = ref({});
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  classId: undefined,
  productType: props.accessoryOnly
    ? RENTAL_CODE.PRODUCT_TYPE.ACCESSORY
    : RENTAL_CODE.PRODUCT_TYPE.DEVICE,
  stockMode: undefined,
  status: undefined,
});
const form = reactive(createEmptyForm());

const rules = {
  productName: [{ required: true, message: "请输入商品名称", trigger: "blur" }],
  classId: [{ required: true, message: "请选择经营分类", trigger: "change" }],
  productType: [
    { required: true, message: "请选择商品类型", trigger: "change" },
  ],
  stockMode: [{ required: true, message: "请选择库存模式", trigger: "change" }],
  inventoryWarehouseId: [
    {
      validator: (_rule, value, callback) => {
        if (inventorySetupVisible.value && !value) {
          callback(new Error("请选择仓库"));
          return;
        }
        callback();
      },
      trigger: "change",
    },
  ],
  inventorySerialText: [
    {
      validator: (_rule, value, callback) => {
        if (
          inventorySetupVisible.value &&
          Number(form.stockMode) === RENTAL_CODE.STOCK_MODE.SERIAL &&
          form.inventoryInputMode === "manual" &&
          !String(value || "").trim()
        ) {
          callback(new Error("请输入序列号"));
          return;
        }
        callback();
      },
      trigger: "blur",
    },
  ],
  inventoryQuantity: [
    {
      validator: (_rule, value, callback) => {
        const quantity = Number(value);
        if (
          inventorySetupVisible.value &&
          Number(form.stockMode) === RENTAL_CODE.STOCK_MODE.QUANTITY &&
          (!Number.isInteger(quantity) || quantity < 1 || quantity > 5000)
        ) {
          callback(new Error("请输入 1-5000 的整数数量"));
          return;
        }
        callback();
      },
      trigger: "change",
    },
  ],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
};

const classOptions = computed(() =>
  flattenClasses(classTree.value, [], selectedClassIds.value),
);

const brandOptions = computed(() => {
  const brands = modelOptions.value
    .filter(
      (item) =>
        !form.classId || Number(item.classId) === Number(form.classId),
    )
    .map((item) => item.brand)
    .filter(Boolean);
  return [...new Set(brands)];
});

const filteredModels = computed(() =>
  modelOptions.value.filter(
    (item) =>
      (!form.classId || Number(item.classId) === Number(form.classId)) &&
      (!form.brand || item.brand === form.brand),
  ),
);

const inventorySetupVisible = computed(
  () =>
    !editing.value &&
    !props.accessoryOnly &&
    Number(form.productType) === RENTAL_CODE.PRODUCT_TYPE.DEVICE &&
    form.setInventory,
);

/** 创建商品表单的默认值，避免新增和编辑复用上一次表单状态。 */
function createEmptyForm() {
  return {
    id: undefined,
    productCode: "",
    productName: "",
    classId: undefined,
    modelId: undefined,
    productType: props.accessoryOnly
      ? RENTAL_CODE.PRODUCT_TYPE.ACCESSORY
      : RENTAL_CODE.PRODUCT_TYPE.DEVICE,
    stockMode: RENTAL_CODE.STOCK_MODE.SERIAL,
    brand: "",
    modelName: "",
    memorySpec: "",
    purchaseCost: undefined,
    salePrice: undefined,
    status: NORMAL_DISABLE_CODE.NORMAL,
    remark: "",
    setInventory: false,
    inventoryInputMode: "manual",
    inventoryWarehouseId: undefined,
    inventoryQuantity: undefined,
    inventorySerialText: "",
    inventoryRemark: "",
  };
}

/** 加载分类和标准型号，为商品表单提供可信选项。 */
async function loadOptions() {
  optionLoading.value = true;
  try {
    const [classResponse, selectedResponse, modelResponse] = await Promise.all([
      listBusinessProductClasses(),
      listSelectedProductClassIds(),
      listBrandModelOptions(),
    ]);
    classTree.value = responseData(classResponse) || [];
    selectedClassIds.value = new Set(
      (responseData(selectedResponse) || []).map(Number),
    );
    modelOptions.value = responseData(modelResponse) || [];
  } finally {
    optionLoading.value = false;
  }
}

/** 只有打开新增商品表单时才读取仓库，避免列表页额外依赖仓库权限和接口。 */
async function loadWarehouseOptions() {
  if (warehouseOptions.value.length) {
    return;
  }
  try {
    const response = await listMerchantWarehouses({
      pageNum: 1,
      pageSize: 1000,
      status: 0,
    });
    warehouseOptions.value = responseRows(response);
  } catch {
    ElMessage.error("仓库列表加载失败，请稍后重试");
  }
}

/** 加载当前商户商品分页列表，并保留后端返回的总数。 */
async function loadProducts() {
  loading.value = true;
  try {
    const response = await listMerchantProducts({
      ...query,
      productType: props.accessoryOnly
        ? RENTAL_CODE.PRODUCT_TYPE.ACCESSORY
        : RENTAL_CODE.PRODUCT_TYPE.DEVICE,
    });
    rows.value = responseRows(response);
    total.value = responseTotal(response, rows.value.length);
  } finally {
    loading.value = false;
  }
}

/** 重置商品筛选条件并回到第一页。 */
function resetQuery() {
  Object.assign(query, {
    pageNum: 1,
    keyword: "",
    classId: undefined,
    productType: props.accessoryOnly
      ? RENTAL_CODE.PRODUCT_TYPE.ACCESSORY
      : RENTAL_CODE.PRODUCT_TYPE.DEVICE,
    stockMode: undefined,
    status: undefined,
  });
  loadProducts();
}

/**
 * 跳转到当前商户经营分类设置页面，分类保存后可返回商品页面继续发布流程。
 */
function openClassSettings() {
  router.push("/merchant/product/class");
}

/** 打开新增商品表单并清空历史编辑值。 */
async function openCreate() {
  editing.value = false;
  Object.assign(form, createEmptyForm());
  importFile.value = null;
  await loadWarehouseOptions();
  formVisible.value = true;
}

/** 读取商品详情后打开编辑表单，确保编辑数据来自商户归属接口。 */
async function openEdit(row) {
  editing.value = true;
  importFile.value = null;
  const response = await getMerchantProduct(row.id);
  Object.assign(form, createEmptyForm(), responseData(response) || row);
  form.status = Number(form.status ?? 0);
  formVisible.value = true;
}

/** 读取商品详情并打开右侧详情抽屉。 */
async function openDetail(row) {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const response = await getMerchantProduct(row.id);
    detail.value = responseData(response) || row;
  } finally {
    detailLoading.value = false;
  }
}

/** 提交商品新增或修改请求，并在提交前执行前端表单校验。 */
async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }
  if (form.productType === RENTAL_CODE.PRODUCT_TYPE.DEVICE && !form.modelId) {
    ElMessage.warning("设备商品必须选择标准型号");
    return;
  }
  if (form.setInventory) {
    if (Number(form.stockMode) === RENTAL_CODE.STOCK_MODE.QUANTITY) {
      const inventoryValid = await formRef.value
        ?.validateField(["inventoryWarehouseId", "inventoryQuantity"])
        .then(() => true)
        .catch(() => false);
      if (!inventoryValid) {
        return;
      }
    } else if (form.inventoryInputMode === "manual") {
      const inventoryValid = await formRef.value
        ?.validateField(["inventoryWarehouseId", "inventorySerialText"])
        .then(() => true)
        .catch(() => false);
      if (!inventoryValid) {
        return;
      }
    } else if (!importFile.value) {
      ElMessage.warning("请选择导入文件");
      return;
    }
  }
  saving.value = true;
  try {
    const payload = { ...form };
    delete payload.setInventory;
    delete payload.inventoryInputMode;
    delete payload.inventoryWarehouseId;
    delete payload.inventoryQuantity;
    delete payload.inventorySerialText;
    delete payload.inventoryRemark;
    if (props.accessoryOnly) {
      payload.productType = RENTAL_CODE.PRODUCT_TYPE.ACCESSORY;
    }
    if (!editing.value) {
      delete payload.id;
      delete payload.productCode;
    }
    const response = editing.value
      ? await updateMerchantProduct(payload)
      : await addMerchantProduct(payload);
    if (response && response.code !== undefined && response.code !== 200) {
      return;
    }
    if (!editing.value && form.setInventory) {
      const created = responseData(response) || {};
      const productId = created.id ?? created.productId;
      if (!productId) {
        ElMessage.warning("商品已创建，但未返回商品编号，库存暂未设置");
        return;
      }
      if (Number(form.stockMode) === RENTAL_CODE.STOCK_MODE.QUANTITY) {
        await addMerchantInventoryBatch({
          productId,
          warehouseId: form.inventoryWarehouseId,
          quantity: Number(form.inventoryQuantity),
          remark: form.inventoryRemark?.trim() || null,
        });
      } else if (form.inventoryInputMode === "manual") {
        const serialNumbers = form.inventorySerialText
          .split(/[\s,，;；]+/)
          .map((item) => item.trim())
          .filter(Boolean);
        await addMerchantInventoryBatch({
          productId,
          warehouseId: form.inventoryWarehouseId,
          serialNumbers,
          remark: form.inventoryRemark?.trim() || null,
        });
      } else {
        await importMerchantResource("sn", importFile.value);
      }
    }
    ElMessage.success(editing.value ? "商品修改成功" : "商品新增成功");
    formVisible.value = false;
    if (!props.createOnly) {
      await loadProducts();
    }
  } finally {
    saving.value = false;
  }
}

/** 删除当前商品，后端会检查库存、套餐和订单引用后执行软删除。 */
function removeProduct(row) {
  ElMessageBox.confirm(
    `确认删除商品“${row.productName || row.productCode}”吗？`,
    "操作确认",
    { type: "warning" },
  )
    .then(() => deleteMerchantProduct(row.id))
    .then(() => {
      ElMessage.success("商品已删除");
      loadProducts();
    })
    .catch(() => undefined);
}

/** 切换分类时清理品牌和型号，避免提交跨分类关系。 */
function handleClassChange() {
  form.brand = "";
  form.modelId = undefined;
  form.modelName = "";
  form.memorySpec = "";
}

/** 切换品牌时清理标准型号及其快照字段。 */
function handleBrandChange() {
  form.modelId = undefined;
  form.modelName = "";
  form.memorySpec = "";
}

/** 选择标准型号后自动补齐商品的品牌、型号和规格快照。 */
function handleModelChange(modelId) {
  const model = modelOptions.value.find(
    (item) => Number(item.modelId) === Number(modelId),
  );
  if (!model) {
    return;
  }
  form.brand = model.brand || form.brand;
  form.modelName = model.modelName || form.modelName;
  form.memorySpec = model.memorySpec || form.memorySpec;
}

/** 切换商品类型时给新增商品设置合理库存默认值。 */
function handleProductTypeChange(productType) {
  if (!editing.value) {
    form.stockMode =
      productType === RENTAL_CODE.PRODUCT_TYPE.DEVICE
        ? RENTAL_CODE.STOCK_MODE.SERIAL
        : RENTAL_CODE.STOCK_MODE.QUANTITY;
    if (productType !== RENTAL_CODE.PRODUCT_TYPE.DEVICE) {
      form.setInventory = false;
      importFile.value = null;
    }
  }
}

/** 切换库存模式时只清理不适用的输入值，保留“是否设置库存”选择。 */
function handleStockModeChange(stockMode) {
  if (editing.value) {
    return;
  }
  if (Number(stockMode) === RENTAL_CODE.STOCK_MODE.QUANTITY) {
    form.inventoryInputMode = "manual";
    form.inventorySerialText = "";
    importFile.value = null;
  } else {
    form.inventoryQuantity = undefined;
  }
}

/** 记录新增商品表单中的导入文件，实际提交仍沿用现有批量导入接口。 */
function handleImportFileChange(uploadFile) {
  importFile.value = uploadFile.raw;
}

function clearImportFile() {
  importFile.value = null;
}

function downloadImportTemplate() {
  exportMerchantResource("sn").then((data) =>
    saveAs(new Blob([data]), "批量导入设备模板.xlsx"),
  );
}

/** 将平台分类树转换为带层级路径的下拉选项，只允许商户已选择的分类。 */
function flattenClasses(nodes, parents, selectedIds, result = []) {
  nodes.forEach((node) => {
    const path = parents.concat(node.className || []);
    if (selectedIds.has(Number(node.classId))) {
      result.push({ value: node.classId, label: path.join(" / ") });
    }
    flattenClasses(node.children || [], path, selectedIds, result);
  });
  return result;
}

/** 从标准型号对象生成商品表单中的可读选项名称。 */
function modelOptionLabel(model) {
  return [model.brand, model.modelName, model.memorySpec]
    .filter(Boolean)
    .join(" / ");
}

/** 根据分类编号返回列表和详情中的分类名称。 */
function classLabel(classId) {
  const option = classOptions.value.find(
    (item) => Number(item.value) === Number(classId),
  );
  return option ? option.label : classId || "-";
}

/** 组合标准型号的品牌、型号和规格展示文本。 */
function modelLabel(product) {
  return (
    [product.brand, product.modelName, product.memorySpec]
      .filter(Boolean)
      .join(" / ") || "-"
  );
}

/** 将商品类型编码转换为中文名称。 */
function productTypeLabel(value) {
  const option = productTypeOptions.find((item) => item.value === value);
  return option ? option.label : value || "-";
}

/** 将库存模式编码转换为中文名称。 */
function stockModeLabel(value) {
  const option = stockModeOptions.find((item) => item.value === value);
  return option ? option.label : value || "-";
}

/** 将金额格式化为两位小数，空值保持未设置状态。 */
function money(value) {
  return value === undefined || value === null || value === ""
    ? "-"
    : Number(value).toFixed(2);
}

/** 兼容分页接口和 Axios 包装响应，统一提取行数据。 */
function responseRows(response) {
  const body =
    response?.data && !Array.isArray(response.data) ? response.data : response;
  if (Array.isArray(body)) {
    return body;
  }
  return body?.rows || [];
}

/** 兼容普通 AjaxResult 和分页响应，统一提取业务数据。 */
function responseData(response) {
  return response?.data !== undefined ? response.data : response;
}

/** 兼容不同分页响应结构，统一读取总记录数。 */
function responseTotal(response, fallback) {
  const body =
    response?.data && !Array.isArray(response.data) ? response.data : response;
  return body?.total === undefined ? fallback : body.total;
}

onMounted(async () => {
  await loadOptions();
  if (props.createOnly) {
    openCreate();
  } else {
    await loadProducts();
  }
});
</script>

<style scoped>
.merchant-product-page {
  min-height: calc(100vh - 84px);
}

.option-alert {
  margin-bottom: 16px;
}

.option-alert-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 16px;
}

.field-control {
  width: 100%;
}

.form-help {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 18px;
}

.import-tip {
  width: 100%;
  margin-top: 12px;
}

.import-tip :deep(.el-alert__icon) {
  width: 16px;
  margin-right: 8px;
  font-size: 16px;
}

.import-form-item :deep(.el-form-item__content) {
  display: block;
  line-height: normal;
}

.import-upload-content {
  width: 100%;
  min-width: 0;
}

.import-upload-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  min-height: 40px;
  line-height: normal;
}

.import-upload-trigger {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 40px;
  line-height: normal;
}

.import-uploader {
  flex: 0 0 auto;
}

.import-uploader :deep(.el-upload) {
  display: flex;
  align-items: center;
}

.import-upload-tip {
  display: inline-flex;
  align-items: center;
  flex: 0 0 auto;
  height: 40px;
  color: var(--el-text-color-regular);
  line-height: normal;
}

.import-template-link {
  display: inline-flex;
  align-items: center;
  flex: 0 0 auto;
  margin-left: auto;
  height: 40px;
}
</style>
