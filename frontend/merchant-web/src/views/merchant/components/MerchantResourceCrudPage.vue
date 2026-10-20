<template>
  <div class="app-container merchant-list-page merchant-resource-crud-page">
    <div class="page-heading">
      <div>
        <h2>{{ listConfig.title }}</h2>
        <p>{{ listConfig.description }}</p>
      </div>
    </div>

    <el-form v-show="showSearch" :inline="true" :model="query" @submit.prevent>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="编码、名称或型号"
          @keyup.enter="load" />
      </el-form-item>
      <el-form-item v-if="listConfig.queryProductType" label="商品类型">
        <el-select
          v-model="query.productType"
          clearable
          placeholder="全部"
          @change="load">
          <el-option label="设备" :value="RENTAL_CODE.PRODUCT_TYPE.DEVICE" />
          <el-option label="配件" :value="RENTAL_CODE.PRODUCT_TYPE.ACCESSORY" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="listConfig.queryStatus" label="状态">
        <el-select
          v-model="query.status"
          clearable
          placeholder="全部"
          @change="load">
          <el-option label="启用" :value="0" />
          <el-option label="停用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="load">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col v-if="listConfig.editable" :span="1.5">
        <el-button type="primary" icon="Plus" @click="openCreate">
          {{ listConfig.addLabel }}
        </el-button>
      </el-col>
      <el-col v-if="props.resource === 'inventory'" :span="1.5">
        <el-button type="primary" icon="Edit" @click="openInventoryAdjust">
          调整库存
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="load" />
    </el-row>

    <el-table v-loading="loading" :data="rows" row-key="id" border>
      <el-table-column
        v-for="column in listConfig.columns"
        :key="column.key"
        :label="column.label"
        :prop="column.key"
        :min-width="column.width || 120">
        <template #default="scope">
          <el-tag
            v-if="column.tag"
            :type="tagType(column, scope.row[column.key])">
            {{ displayValue(column, scope.row[column.key]) }}
          </el-tag>
          <span v-else>{{ displayValue(column, scope.row[column.key]) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        v-if="listConfig.editable || listConfig.canRemove"
        label="操作"
        fixed="right"
        width="170">
        <template #default="scope">
          <el-button
            v-if="listConfig.editable"
            link
            type="primary"
            @click="openEdit(scope.row)">
            编辑
          </el-button>
          <el-button
            v-if="listConfig.editable || listConfig.canRemove"
            link
            type="danger"
            @click="removeRow(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="load" />

    <el-dialog
      v-model="dialogVisible"
      :title="formTitle"
      width="720px"
      append-to-body
      destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="120px">
        <el-row :gutter="16">
          <template v-for="field in formConfig.fields" :key="field.key">
            <el-col
              v-if="!field.systemGenerated || editing"
              :span="field.span || 12">
              <el-form-item :label="field.label" :required="field.required">
                <el-select
                  v-if="field.key === 'province'"
                  v-model="form.province"
                  clearable
                  placeholder="请选择省"
                  class="field-control"
                  @change="changeProvince">
                  <el-option
                    v-for="item in provinceOptions"
                    :key="item.code"
                    :label="item.name"
                    :value="item.name" />
                </el-select>
                <el-select
                  v-else-if="field.key === 'city'"
                  v-model="form.city"
                  clearable
                  placeholder="请选择市"
                  class="field-control"
                  @change="changeCity">
                  <el-option
                    v-for="item in cityOptions"
                    :key="item.code"
                    :label="item.name"
                    :value="item.name" />
                </el-select>
                <el-select
                  v-else-if="field.key === 'district'"
                  v-model="form.district"
                  clearable
                  placeholder="请选择区/县"
                  class="field-control">
                  <el-option
                    v-for="item in districtOptions"
                    :key="item.code"
                    :label="item.name"
                    :value="item.name" />
                </el-select>
                <el-input
                  v-else-if="props.resource === 'warehouse' && field.key === 'address'"
                  v-model="form.address"
                  @input="clearWarehouseCoordinates"
                  :placeholder="field.placeholder || `请输入${field.label}`">
                  <template #append>
                    <el-button @click="addressPickerVisible = true">
                      地图选点
                    </el-button>
                  </template>
                </el-input>
                <el-select
                  v-else-if="
                    props.resource === 'accessory' && field.key === 'productId'
                  "
                  v-model="form.productId"
                  clearable
                  filterable
                  placeholder="请选择配件商品"
                  class="field-control">
                  <el-option
                    v-for="item in accessoryProductOptions"
                    :key="item.id || item.productId"
                    :label="`${item.productName}（${item.productCode}）`"
                    :value="item.id || item.productId" />
                </el-select>
                <el-select
                  v-else-if="
                    props.resource === 'accessory' &&
                    field.key === 'warehouseId'
                  "
                  v-model="form.warehouseId"
                  clearable
                  filterable
                  placeholder="请选择仓库"
                  class="field-control">
                  <el-option
                    v-for="item in accessoryWarehouseOptions"
                    :key="item.id || item.warehouseId"
                    :label="`${item.name || item.warehouseName}（${item.code || item.warehouseCode}）`"
                    :value="item.id || item.warehouseId" />
                </el-select>
                <el-switch
                  v-else-if="field.key === 'status'"
                  v-model="form[field.key]"
                  :active-value="NORMAL_DISABLE_CODE.NORMAL"
                  :inactive-value="NORMAL_DISABLE_CODE.DISABLED"
                  active-text="启用"
                  inactive-text="停用"
                  inline-prompt />
                <el-switch
                  v-else-if="field.key === 'enabled'"
                  v-model="form[field.key]"
                  :active-value="YES_NO_CODE.YES"
                  :inactive-value="YES_NO_CODE.NO"
                  active-text="启用"
                  inactive-text="停用"
                  inline-prompt />
                <el-select
                  v-else-if="field.type === 'select'"
                  v-model="form[field.key]"
                  clearable
                  :placeholder="field.placeholder || `请选择${field.label}`"
                  class="field-control">
                  <el-option
                    v-for="option in field.options"
                    :key="String(option.value)"
                    :label="option.label"
                    :value="option.value" />
                </el-select>
                <el-time-picker
                  v-else-if="field.type === 'time-range'"
                  v-model="form[field.key]"
                  is-range
                  value-format="HH:mm"
                  format="HH:mm"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  class="field-control" />
                <el-input-number
                  v-else-if="field.type === 'number'"
                  v-model="form[field.key]"
                  :min="field.min"
                  :max="field.max"
                  controls-position="right"
                  class="field-control" />
                <el-input
                  v-else-if="field.type === 'textarea'"
                  v-model="form[field.key]"
                  type="textarea"
                  :rows="field.rows || 4"
                  :placeholder="field.placeholder" />
                <el-input
                  v-else
                  v-model="form[field.key]"
                  :disabled="field.systemGenerated"
                  :placeholder="field.placeholder || `请输入${field.label}`" />
              </el-form-item>
            </el-col>
          </template>
          <el-col v-if="props.resource === 'warehouse'" :span="24">
            <el-form-item label="地图坐标">
              <span v-if="form.longitude != null && form.latitude != null">
                {{ Number(form.longitude).toFixed(6) }},
                {{ Number(form.latitude).toFixed(6) }}
              </span>
              <span v-else>-</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-alert
          v-if="formConfig.packageHelp && !form.components"
          title="请添加套餐组件"
          type="info"
          :closable="false" />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="inventoryDialogVisible"
      title="调整数量库存"
      width="560px"
      append-to-body>
      <el-form :model="inventoryForm" label-width="110px">
        <el-form-item label="商品编号" required>
          <el-input-number
            v-model="inventoryForm.productId"
            :min="1"
            class="field-control" />
        </el-form-item>
        <el-form-item label="仓库编号" required>
          <el-input-number
            v-model="inventoryForm.warehouseId"
            :min="1"
            class="field-control" />
        </el-form-item>
        <el-form-item label="变更数量" required>
          <el-input-number
            v-model="inventoryForm.changeQty"
            controls-position="right"
            class="field-control" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="inventoryForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inventoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitInventory">
          保存
        </el-button>
      </template>
    </el-dialog>

    <AmapAddressPicker
      v-if="props.resource === 'warehouse'"
      v-model="addressPickerVisible"
      :address="form.address"
      :longitude="form.longitude"
      :latitude="form.latitude"
      @confirm="applyMapSelection" />
  </div>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AmapAddressPicker from "@/components/AmapAddressPicker.vue";
import {
  addMerchantAccessory,
  addMerchantPackage,
  addMerchantProduct,
  addMerchantSn,
  addMerchantWarehouse,
  adjustMerchantInventory,
  deleteMerchantAccessory,
  deleteMerchantInventory,
  deleteMerchantPackage,
  deleteMerchantProduct,
  deleteMerchantSn,
  deleteMerchantWarehouse,
  getMerchantWarehouse,
  listMerchantAccessories,
  listMerchantInventory,
  listMerchantPackages,
  listMerchantProducts,
  listMerchantSns,
  listMerchantWarehouses,
  listMerchantRegionChildren,
  updateMerchantAccessory,
  updateMerchantPackage,
  updateMerchantProduct,
  updateMerchantSn,
  updateMerchantWarehouse,
} from "@/api/merchant/rentalBusiness";
import { NORMAL_DISABLE_CODE, YES_NO_CODE } from "@/constants/options";
import { RENTAL_CODE } from "@/utils/rental/options";

const props = defineProps({
  resource: { type: String, required: true },
});

const statusOptions = [
  { label: "启用", value: NORMAL_DISABLE_CODE.NORMAL },
  { label: "停用", value: NORMAL_DISABLE_CODE.DISABLED },
];
const enabledOptions = [
  { label: "启用", value: YES_NO_CODE.YES },
  { label: "停用", value: YES_NO_CODE.NO },
];
const booleanOptions = [
  { label: "是", value: YES_NO_CODE.YES },
  { label: "否", value: YES_NO_CODE.NO },
];
const productTypeOptions = [
  { label: "设备", value: RENTAL_CODE.PRODUCT_TYPE.DEVICE },
  { label: "配件", value: RENTAL_CODE.PRODUCT_TYPE.ACCESSORY },
];
const stockModeOptions = [
  { label: "逐件", value: RENTAL_CODE.STOCK_MODE.SERIAL },
  { label: "数量", value: RENTAL_CODE.STOCK_MODE.QUANTITY },
];
const assetStatusOptions = [
  { label: "可用", value: RENTAL_CODE.INVENTORY_STATUS.AVAILABLE },
  { label: "已预占", value: RENTAL_CODE.INVENTORY_STATUS.RESERVED },
  { label: "租用中", value: RENTAL_CODE.INVENTORY_STATUS.RENTED },
  { label: "维修中", value: RENTAL_CODE.INVENTORY_STATUS.REPAIRING },
  { label: "已丢失", value: RENTAL_CODE.INVENTORY_STATUS.LOST },
  { label: "已退役", value: RENTAL_CODE.INVENTORY_STATUS.RETIRED },
];
const enumDefaultValues = {
  status: NORMAL_DISABLE_CODE.NORMAL,
  enabled: YES_NO_CODE.YES,
  productType: RENTAL_CODE.PRODUCT_TYPE.DEVICE,
  stockMode: RENTAL_CODE.STOCK_MODE.SERIAL,
  assetStatus: RENTAL_CODE.INVENTORY_STATUS.AVAILABLE,
  itemStatus: RENTAL_CODE.INVENTORY_STATUS.AVAILABLE,
};
const numericEnumFields = new Set(Object.keys(enumDefaultValues));

const listConfigs = {
  warehouse: {
    title: "仓库管理",
    description: "维护商户仓库，所有操作受当前用户仓库权限约束。",
    addLabel: "添加仓库",
    editable: true,
    queryStatus: true,
    columns: [
      { key: "code", label: "仓库编码" },
      { key: "name", label: "仓库名称" },
      { key: "sortNo", label: "排序", width: 80 },
      { key: "province", label: "省份" },
      { key: "city", label: "城市" },
      { key: "contactName", label: "负责人" },
      { key: "contactPhone", label: "联系电话" },
      { key: "address", label: "详细地址", width: 220 },
      { key: "status", label: "状态", tag: true },
    ],
  },
  product: {
    title: "商品管理",
    description: "商品是库存和套餐的基础 SKU，商品类型与库存模式有严格约束。",
    addLabel: "添加商品",
    editable: true,
    queryStatus: true,
    queryProductType: true,
    columns: [
      { key: "productCode", label: "商品编码" },
      { key: "productName", label: "商品名称", width: 180 },
      { key: "productType", label: "类型" },
      { key: "stockMode", label: "库存模式" },
      { key: "brand", label: "品牌" },
      { key: "modelName", label: "型号" },
      { key: "status", label: "状态", tag: true },
    ],
  },
  sn: {
    title: "SN 管理",
    description:
      "逐件商品通过 SN 管理生命周期；已租用或预占的 SN 不允许直接删除。",
    addLabel: "添加 SN",
    editable: true,
    columns: [
      { key: "snCode", label: "SN 编码", width: 180 },
      { key: "productName", label: "商品" },
      { key: "warehouseName", label: "仓库" },
      { key: "assetStatus", label: "状态", tag: true },
      { key: "conditionLevel", label: "成色" },
    ],
  },
  accessory: {
    title: "配件实例",
    description: "逐件配件单独建实例；数量型通用配件请在库存管理中调整总量。",
    addLabel: "添加配件",
    editable: true,
    columns: [
      { key: "itemCode", label: "配件编码", width: 180 },
      { key: "productName", label: "商品" },
      { key: "warehouseName", label: "仓库" },
      { key: "itemStatus", label: "状态", tag: true },
    ],
  },
  package: {
    title: "套餐管理",
    description: "套餐按版本化 BOM 保存，可在不重复统计的前提下切换组合。",
    addLabel: "添加套餐",
    editable: true,
    columns: [
      { key: "packageCode", label: "套餐编码" },
      { key: "packageName", label: "套餐名称", width: 180 },
      { key: "platformCode", label: "订单渠道" },
      { key: "versionNo", label: "当前版本" },
      { key: "enabled", label: "状态", tag: true },
    ],
  },
  inventory: {
    title: "库存管理",
    description:
      "数量库存通过余额和流水维护，逐件库存请进入 SN 或配件实例管理。",
    editable: false,
    canRemove: true,
    columns: [
      { key: "productCode", label: "商品编码" },
      { key: "productName", label: "商品名称", width: 180 },
      { key: "warehouseName", label: "仓库" },
      { key: "stockMode", label: "库存模式" },
      { key: "onHandQty", label: "在库数量" },
      { key: "reservedQty", label: "已预占" },
      { key: "availableQty", label: "可用数量" },
    ],
  },
};

const formConfigs = {
  warehouse: {
    fields: [
      {
        key: "code",
        label: "仓库编码",
        systemGenerated: true,
        placeholder: "保存后由系统自动生成",
      },
      { key: "name", label: "仓库名称", required: true },
      { key: "contactName", label: "负责人" },
      { key: "contactPhone", label: "联系电话" },
      { key: "province", label: "省" },
      { key: "city", label: "城市" },
      { key: "district", label: "区/县" },
      { key: "address", label: "详细地址", span: 24 },
      {
        key: "pickupTimeRange",
        label: "取件时段",
        type: "time-range",
        span: 24,
      },
      {
        key: "returnEnabled",
        label: "允许归还",
        type: "select",
        options: booleanOptions,
      },
      {
        key: "allianceShareEnabled",
        label: "允许联盟共享",
        type: "select",
        options: booleanOptions,
      },
      { key: "remark", label: "备注", type: "textarea", span: 24 },
      { key: "sortNo", label: "排序", type: "number", min: 0 },
      { key: "status", label: "状态", type: "select", options: statusOptions },
    ],
  },
  product: {
    fields: [
      {
        key: "productCode",
        label: "商品编码",
        systemGenerated: true,
        placeholder: "保存后由系统自动生成",
      },
      { key: "productName", label: "商品名称", required: true },
      {
        key: "classId",
        label: "分类编号",
        type: "number",
        required: true,
        min: 1,
      },
      { key: "modelId", label: "型号编号", type: "number", min: 1 },
      {
        key: "productType",
        label: "商品类型",
        type: "select",
        required: true,
        options: productTypeOptions,
      },
      {
        key: "stockMode",
        label: "库存模式",
        type: "select",
        required: true,
        options: stockModeOptions,
      },
      { key: "brand", label: "品牌" },
      { key: "modelName", label: "型号" },
      { key: "memorySpec", label: "规格" },
      { key: "salePrice", label: "销售价", type: "number", min: 0 },
      { key: "status", label: "状态", type: "select", options: statusOptions },
      { key: "remark", label: "备注", type: "textarea", span: 24 },
    ],
  },
  sn: {
    fields: [
      {
        key: "productId",
        label: "商品编号",
        type: "number",
        required: true,
        min: 1,
      },
      {
        key: "warehouseId",
        label: "仓库编号",
        type: "number",
        required: true,
        min: 1,
      },
      { key: "snCode", label: "SN 编码", required: true },
      { key: "barcode", label: "条码" },
      {
        key: "assetStatus",
        label: "资产状态",
        type: "select",
        options: assetStatusOptions,
      },
      { key: "conditionLevel", label: "成色" },
      { key: "remark", label: "备注", type: "textarea", span: 24 },
    ],
  },
  accessory: {
    fields: [
      {
        key: "productId",
        label: "商品编号",
        type: "number",
        required: true,
        min: 1,
      },
      {
        key: "warehouseId",
        label: "仓库编号",
        type: "number",
        required: true,
        min: 1,
      },
      {
        key: "itemCode",
        label: "配件编码",
        systemGenerated: true,
        placeholder: "保存后由系统自动生成",
      },
      {
        key: "itemStatus",
        label: "状态",
        type: "select",
        options: assetStatusOptions,
      },
      { key: "remark", label: "备注", type: "textarea", span: 24 },
    ],
  },
  package: {
    packageHelp: true,
    fields: [
      {
        key: "packageCode",
        label: "套餐编码",
        systemGenerated: true,
        placeholder: "保存后由系统自动生成",
      },
      { key: "packageName", label: "套餐名称", required: true },
      { key: "platformCode", label: "订单渠道" },
      {
        key: "enabled",
        label: "启用状态",
        type: "select",
        options: enabledOptions,
      },
      { key: "sortNo", label: "排序", type: "number", min: 0 },
      {
        key: "components",
        label: "BOM 组件 JSON",
        type: "textarea",
        required: true,
        span: 24,
        rows: 6,
      },
      { key: "remark", label: "备注", type: "textarea", span: 24 },
    ],
  },
  inventory: { fields: [] },
};

const apiMap = {
  warehouse: {
    list: listMerchantWarehouses,
    add: addMerchantWarehouse,
    update: updateMerchantWarehouse,
    remove: deleteMerchantWarehouse,
  },
  product: {
    list: listMerchantProducts,
    add: addMerchantProduct,
    update: updateMerchantProduct,
    remove: deleteMerchantProduct,
  },
  sn: {
    list: listMerchantSns,
    add: addMerchantSn,
    update: updateMerchantSn,
    remove: deleteMerchantSn,
  },
  accessory: {
    list: listMerchantAccessories,
    add: addMerchantAccessory,
    update: updateMerchantAccessory,
    remove: deleteMerchantAccessory,
  },
  package: {
    list: listMerchantPackages,
    add: addMerchantPackage,
    update: updateMerchantPackage,
    remove: deleteMerchantPackage,
  },
  inventory: {
    list: listMerchantInventory,
    remove: (row) => deleteMerchantInventory(row.productId, row.warehouseId),
  },
};

const listConfig = computed(() => listConfigs[props.resource]);
const formConfig = computed(() => formConfigs[props.resource]);
const api = computed(() => apiMap[props.resource]);
const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const showSearch = ref(true);
const saving = ref(false);
const dialogVisible = ref(false);
const addressPickerVisible = ref(false);
const inventoryDialogVisible = ref(false);
const editing = ref(false);
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  status: undefined,
  productType: undefined,
});
const form = reactive({});
const provinceOptions = ref([]);
const cityOptions = ref([]);
const districtOptions = ref([]);
const accessoryProductOptions = ref([]);
const accessoryWarehouseOptions = ref([]);
const inventoryForm = reactive({
  productId: undefined,
  warehouseId: undefined,
  changeQty: undefined,
  reason: "",
});
const formTitleByResource = {
  warehouse: "仓库",
  product: "商品",
  sn: "SN",
  accessory: "配件",
  package: "套餐",
};
const formTitle = computed(
  () => `${editing.value ? "修改" : "新增"}${formTitleByResource[props.resource]}`,
);

/** 加载当前资源分页数据。 */
function load() {
  loading.value = true;
  api.value
    .list(query)
    .then((response) => {
      rows.value = response.rows || response.data?.rows || response.data || [];
      total.value = response.total || response.data?.total || 0;
    })
    .finally(() => {
      loading.value = false;
    });
}

/** 清空查询条件并重新加载列表。 */
function resetQuery() {
  query.pageNum = 1;
  query.keyword = "";
  query.status = undefined;
  query.productType = undefined;
  load();
}

/** 打开新增表单并填充资源默认值。 */
function openCreate() {
  editing.value = false;
  Object.keys(form).forEach((key) => delete form[key]);
  formConfig.value.fields.forEach((field) => {
    if (field.key === "components") form[field.key] = "[]";
    else if (field.type === "time-range") form[field.key] = [];
    else if (numericEnumFields.has(field.key))
      form[field.key] = enumDefaultValues[field.key];
    else form[field.key] = undefined;
  });
  if (props.resource === "warehouse") {
    form.sortNo = 0;
    provinceOptions.value = [];
    cityOptions.value = [];
    districtOptions.value = [];
    loadRegions("0", provinceOptions);
  }
  if (props.resource === "accessory") loadAccessoryOptions();
  dialogVisible.value = true;
}

/** 打开编辑表单并复制当前行，防止表格数据被表单双向修改。 */
async function openEdit(row) {
  editing.value = true;
  Object.keys(form).forEach((key) => delete form[key]);
  // 编辑仓库必须重新读取详情，避免列表字段裁剪或旧接口缓存导致负责人等字段为空。
  const detail =
    props.resource === "warehouse"
      ? await getMerchantWarehouse(row.id).then(
          (response) => response.data || response,
        )
      : row;
  form.id = detail.id;
  formConfig.value.fields.forEach((field) => {
    const value = detail[field.key];
    form[field.key] = numericEnumFields.has(field.key)
      ? Number(value ?? enumDefaultValues[field.key])
      : value;
  });
  if (props.resource === "package")
    form.components = JSON.stringify(detail.components || [], null, 2);
  if (props.resource === "warehouse") {
    form.longitude = detail.longitude;
    form.latitude = detail.latitude;
    form.pickupTimeRange = parsePickupRule(detail.pickupRuleJson);
    cityOptions.value = [];
    districtOptions.value = [];
    loadWarehouseRegions(detail.province, detail.city);
  }
  if (props.resource === "accessory") loadAccessoryOptions();
  dialogVisible.value = true;
}

/** 配件实例只能选择当前商户已配置的配件商品和仓库，避免直接提交外部唯一编号。 */
function loadAccessoryOptions() {
  Promise.all([
    listMerchantProducts({
      pageNum: 1,
      pageSize: 1000,
      productType: RENTAL_CODE.PRODUCT_TYPE.ACCESSORY,
      status: NORMAL_DISABLE_CODE.NORMAL,
    }),
    listMerchantWarehouses({
      pageNum: 1,
      pageSize: 1000,
      status: NORMAL_DISABLE_CODE.NORMAL,
    }),
  ]).then(([products, warehouses]) => {
    accessoryProductOptions.value = products.rows || products.data?.rows || [];
    accessoryWarehouseOptions.value =
      warehouses.rows || warehouses.data?.rows || [];
  });
}

/** 从本地数据库加载行政区划选项，避免表单依赖外部网络。 */
function loadRegions(parentCode, target) {
  listMerchantRegionChildren(parentCode).then((response) => {
    target.value = response.data || response.rows || [];
  });
}

/** 编辑仓库时按已保存的省市名称恢复两级下拉选项，确保原值正常回显。 */
function loadWarehouseRegions(province, city) {
  listMerchantRegionChildren("0")
    .then((response) => {
      provinceOptions.value = response.data || response.rows || [];
      const provinceItem = provinceOptions.value.find(
        (item) => item.name === province,
      );
      if (!provinceItem) return;
      return listMerchantRegionChildren(provinceItem.code);
    })
    .then((response) => {
      if (!response) return;
      cityOptions.value = response.data || response.rows || [];
      const cityItem = cityOptions.value.find((item) => item.name === city);
      if (cityItem) return listMerchantRegionChildren(cityItem.code);
    })
    .then((response) => {
      if (response)
        districtOptions.value = response.data || response.rows || [];
    });
}

/** 省份变更后加载城市，并清空下级选择。 */
function changeProvince(name) {
  const item = provinceOptions.value.find((option) => option.name === name);
  form.city = undefined;
  form.district = undefined;
  cityOptions.value = [];
  districtOptions.value = [];
  if (item) loadRegions(item.code, cityOptions);
}

/** 城市变更后加载区县，并清空旧区县。 */
function changeCity(name) {
  const item = cityOptions.value.find((option) => option.name === name);
  form.district = undefined;
  districtOptions.value = [];
  if (item) loadRegions(item.code, districtOptions);
}

/** 把旧版仓库 JSON 规则映射为表单时间段。 */
function parsePickupRule(value) {
  if (!value) return [];
  try {
    const rule = typeof value === "string" ? JSON.parse(value) : value;
    const start = String(rule.start || rule.startTime || "").slice(0, 5);
    const end = String(rule.end || rule.endTime || "").slice(0, 5);
    return start && end ? [start, end] : [];
  } catch {
    return [];
  }
}

/** 手工修改地址后清空旧坐标，避免地址与地图位置不一致。 */
function clearWarehouseCoordinates() {
  form.longitude = undefined;
  form.latitude = undefined;
}

/** 把地图选点回填到仓库地址、坐标和行政区划字段。 */
async function applyMapSelection(location) {
  form.address = location.address;
  form.longitude = location.longitude;
  form.latitude = location.latitude;
  form.province = undefined;
  form.city = undefined;
  form.district = undefined;
  cityOptions.value = [];
  districtOptions.value = [];

  try {
    const provinceResponse = await listMerchantRegionChildren("0");
    provinceOptions.value = provinceResponse.data || provinceResponse.rows || [];
    const province = findRegion(provinceOptions.value, location.province);
    if (!province) {
      ElMessage.warning("地址和坐标已更新，省市区请手动选择");
      return;
    }
    form.province = province.name;

    const cityResponse = await listMerchantRegionChildren(province.code);
    cityOptions.value = cityResponse.data || cityResponse.rows || [];
    const city = findRegion(cityOptions.value, location.city || location.province);
    if (!city) {
      ElMessage.warning("地址和坐标已更新，城市和区县请手动选择");
      return;
    }
    form.city = city.name;

    const districtResponse = await listMerchantRegionChildren(city.code);
    districtOptions.value = districtResponse.data || districtResponse.rows || [];
    const district = findRegion(districtOptions.value, location.district);
    if (district) {
      form.district = district.name;
    } else if (location.district) {
      ElMessage.warning("地址和坐标已更新，区县请手动选择");
    }
  } catch {
    ElMessage.warning("地址和坐标已更新，行政区划请手动确认");
  }
}

function findRegion(options, name) {
  const normalize = (value) =>
    String(value || "").replace(/(特别行政区|自治区|省|市)$/, "");
  const expected = normalize(name);
  return options.find((item) => normalize(item.name) === expected);
}

/** 提交新增或修改命令，并转换兼容型号和套餐 BOM 的输入格式。 */
function submit() {
  const data = { ...form };
  data.id = editing.value ? form.id : undefined;
  if (props.resource === "warehouse") {
    const [start, end] = form.pickupTimeRange || [];
    data.pickupRuleJson = start && end ? JSON.stringify({ start, end }) : null;
    delete data.pickupTimeRange;
  }
  if (props.resource === "package") {
    try {
      data.components = JSON.parse(data.components || "[]");
    } catch (error) {
      ElMessage.error("BOM 组件 JSON 格式不正确");
      return;
    }
  }
  saving.value = true;
  const action = editing.value ? api.value.update : api.value.add;
  action(data)
    .then(() => {
      ElMessage.success("保存成功");
      dialogVisible.value = false;
      load();
    })
    .finally(() => {
      saving.value = false;
    });
}

/** 删除当前行，后端仍会校验引用关系并执行软删除。 */
function removeRow(row) {
  const removeAction = () =>
    props.resource === "inventory"
      ? api.value.remove(row)
      : api.value.remove(row.id);
  ElMessageBox.confirm(
    `确认删除“${row.name || row.productName || row.packageName || row.code || row.itemCode || row.snCode || row.productCode}”吗？`,
    "操作确认",
    { type: "warning" },
  )
    .then(removeAction)
    .then(() => {
      ElMessage.success("删除成功");
      load();
    });
}

/** 打开数量库存调整表单。 */
function openInventoryAdjust() {
  Object.assign(inventoryForm, {
    productId: undefined,
    warehouseId: undefined,
    changeQty: undefined,
    reason: "",
  });
  inventoryDialogVisible.value = true;
}

/** 提交数量库存调整并写入流水。 */
function submitInventory() {
  saving.value = true;
  adjustMerchantInventory(inventoryForm)
    .then(() => {
      ElMessage.success("库存调整成功");
      inventoryDialogVisible.value = false;
      load();
    })
    .finally(() => {
      saving.value = false;
    });
}

/** 将字典值转换成页面可读文本。 */
function displayValue(column, value) {
  if (value === undefined || value === null || value === "") return "-";
  if (column.key === "status") return Number(value) === 0 ? "启用" : "停用";
  if (column.key === "enabled") return Number(value) === 1 ? "启用" : "停用";
  if (column.key === "productType")
    return findOptionLabel(productTypeOptions, value);
  if (column.key === "stockMode")
    return findOptionLabel(stockModeOptions, value);
  if (column.key === "assetStatus" || column.key === "itemStatus")
    return findOptionLabel(assetStatusOptions, value);
  if (column.key === "compatibleModelIds" && Array.isArray(value))
    return value.join(", ");
  return value;
}

/** 返回数值枚举在页面上的可读标签。 */
function findOptionLabel(options, value) {
  return options.find((option) => option.value === Number(value))?.label || "-";
}

/** 根据状态值返回 Element Plus 标签颜色。 */
function tagType(column, value) {
  const numericValue = Number(value);
  if (column.key === "status") return numericValue === 0 ? "success" : "info";
  if (column.key === "enabled") return numericValue === 1 ? "success" : "info";
  if (column.key === "assetStatus" || column.key === "itemStatus") {
    if (numericValue === RENTAL_CODE.INVENTORY_STATUS.AVAILABLE)
      return "success";
    if (
      [
        RENTAL_CODE.INVENTORY_STATUS.REPAIRING,
        RENTAL_CODE.INVENTORY_STATUS.LOST,
        RENTAL_CODE.INVENTORY_STATUS.RETIRED,
      ].includes(numericValue)
    )
      return "danger";
    return "warning";
  }
  return "info";
}

load();
</script>

<style scoped>
.merchant-resource-crud-page {
  min-height: calc(100vh - 84px);
}
.field-control {
  width: 100%;
}
</style>
