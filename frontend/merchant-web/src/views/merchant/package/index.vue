<template>
  <div class="app-container">
    <el-form
      v-show="showSearch"
      :model="query"
      inline
      class="mb8"
      @submit.prevent>
      <el-form-item label="套餐名称">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="请输入套餐名称或编码"
          @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="状态">
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
        <el-button icon="Refresh" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="openCreate">
          新增自定义套餐
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="CopyDocument" @click="openCopy">
          复制平台套餐
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="load" />
    </el-row>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column
        label="套餐编号"
        prop="packageId"
        width="90"
        align="center" />
      <el-table-column label="套餐编码" prop="packageCode" min-width="170" />
      <el-table-column label="套餐名称" prop="packageName" min-width="180" />
      <el-table-column label="产品分类" min-width="150">
        <template #default="{ row }">{{ classLabel(row.classId) }}</template>
      </el-table-column>
      <el-table-column label="主产品型号" min-width="180">
        <template #default="{ row }">{{ modelLabel(row.modelId) }}</template>
      </el-table-column>
      <el-table-column label="类型" min-width="110">
        <template #default>自定义套餐</template>
      </el-table-column>
      <el-table-column
        label="版本"
        prop="versionNo"
        width="90"
        align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="Number(row.status) === 0 ? 'success' : 'info'">
            {{ Number(row.status) === 0 ? "启用" : "停用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="230" align="center">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="openDetail(row)">
            详情
          </el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeRow(row)">删除</el-button>
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
      v-model="dialogOpen"
      :title="editing ? '编辑套餐' : '新增自定义套餐'"
      width="760px"
      destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="24">
          <el-col v-if="editing" :span="24">
            <el-form-item label="套餐编码">
              <el-input
                v-model="form.packageCode"
                disabled
                placeholder="保存后由编码配置自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="套餐名称" prop="packageName">
              <el-input
                v-model="form.packageName"
                maxlength="128"
                show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="产品分类" prop="classId">
              <el-select
                v-model="form.classId"
                filterable
                class="full-width"
                placeholder="请选择产品分类"
                @change="changeClass">
                <el-option
                  v-for="item in classOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主产品型号" prop="productId">
              <el-select
                v-model="form.productId"
                filterable
                class="full-width"
                placeholder="请选择设备商品"
                @change="changeProduct">
                <el-option
                  v-for="item in filteredDeviceProducts"
                  :key="item.productId || item.id"
                  :label="modelOptionLabel(item)"
                  :value="item.productId || item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="产品明细">
              <el-table :data="productDetailRows" border class="item-table">
                <el-table-column label="类型" width="100" align="center">
                  <template #default="{ row }">
                    {{ row.isMain ? "主产品" : "非主产品" }}
                  </template>
                </el-table-column>
                <el-table-column label="产品" min-width="300">
                  <template #default="{ row }">
                    <el-select
                      v-if="row.isMain"
                      :model-value="row.productId"
                      disabled
                      class="full-width"
                      placeholder="请选择主产品">
                      <el-option
                        v-for="item in filteredDeviceProducts"
                        :key="item.productId || item.id"
                        :label="modelOptionLabel(item)"
                        :value="item.productId || item.id" />
                    </el-select>
                    <el-select
                      v-else
                      v-model="row.productId"
                      filterable
                      class="full-width"
                      placeholder="请选择产品">
                      <el-option
                        v-for="item in packageProductOptions"
                        :key="item.productId || item.id"
                        :label="productOptionLabel(item)"
                        :value="item.productId || item.id" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="数量" width="106" align="center">
                  <template #default="{ row }">
                    <el-input-number
                      v-model="row.quantity"
                      :disabled="row.isMain"
                      :min="1"
                      controls-position="right"
                      style="width: 80px" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="60" align="center">
                  <template #default="{ row, $index }">
                    <el-button
                      v-if="!row.isMain"
                      link
                      type="danger"
                      @click="removeProductItem($index - 1)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button
                link
                class="add-item"
                plain
                type="primary"
                icon="Plus"
                :disabled="!form.productId"
                @click="addProductItem">
                添加产品
              </el-button>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="配件明细">
              <el-table :data="form.items" border class="item-table">
                <el-table-column label="配件" min-width="360">
                  <template #default="{ row }">
                    <el-select
                      v-model="row.accessoryOptionKey"
                      filterable
                      class="full-width"
                      placeholder="请选择我的配件">
                      <el-option
                        v-for="item in compatibleAccessoryProducts"
                        :key="item.optionKey"
                        :label="`${item.productName}`"
                        :value="item.optionKey" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="数量" width="106" align="center">
                  <template #default="{ row }">
                    <el-input-number
                      v-model="row.quantity"
                      :min="1"
                      controls-position="right"
                      style="width: 80px" />
                  </template>
                </el-table-column>
                <!-- <el-table-column label="必选" width="100">
                  <template #default="{ row }">
                    <el-switch
                      v-model="row.requiredFlag"
                      :active-value="1"
                      :inactive-value="0"
                    />
                  </template>
                </el-table-column> -->
                <el-table-column label="操作" width="60" align="center">
                  <template #default="{ $index }">
                    <el-button
                      link
                      type="danger"
                      @click="form.items.splice($index, 1)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button
                link
                class="add-item"
                plain
                type="primary"
                icon="Plus"
                :disabled="!form.modelId"
                @click="addItem">
                添加配件
              </el-button>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number
                v-model="form.sortNo"
                :min="0"
                controls-position="right"
                class="full-width" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch
                v-model="form.status"
                :active-value="0"
                :inactive-value="1"
                active-text="启用"
                inactive-text="停用"
                inline-prompt />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">
          确定
        </el-button>
      </template>
    </el-dialog>
    <el-dialog
      v-model="detailOpen"
      title="套餐详情"
      width="860px"
      destroy-on-close>
      <el-skeleton v-if="detailLoading" :rows="6" animated />
      <template v-else-if="detail">
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="套餐名称">
            {{ detail.packageName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="套餐编码">
            {{ detail.packageCode || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="产品分类">
            {{ detailClassLabel(detail.classId) }}
          </el-descriptions-item>
          <el-descriptions-item label="主产品型号">
            {{ detailModelLabel(detail.modelId) }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">自定义套餐</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="Number(detail.status) === 0 ? 'success' : 'info'">
              {{ Number(detail.status) === 0 ? "启用" : "停用" }}
            </el-tag>
          </el-descriptions-item>
          <!-- <el-descriptions
          v-if="detail.remark"
          :column="1"
          border
          class="detail-remark" > -->
          <el-descriptions-item label="备注" label-width="120">
            {{ detail.remark }}
          </el-descriptions-item>
        <!-- </el-descriptions> -->
        </el-descriptions>
        <el-divider content-position="left">套餐明细</el-divider>
        <el-table :data="detail.items || []" border>
          <el-table-column label="明细类型" width="120" align="center">
            <template #default="{ row }">
              {{ packageItemRoleLabel(row.itemRole) }}
            </template>
          </el-table-column>
          <el-table-column label="名称" min-width="280">
            <template #default="{ row }">
              {{ detailItemName(row) }}
            </template>
          </el-table-column>
          <!-- <el-table-column label="型号" min-width="220">
            <template #default="{ row }">
              {{ row.itemModelName || "-" }}
            </template>
          </el-table-column> -->
          <el-table-column label="数量" width="100" align="center">
            <template #default="{ row }">{{ row.quantity || 0 }}</template>
          </el-table-column>
        </el-table>

      </template>
      <el-empty v-else description="暂无套餐详情" />
    </el-dialog>
    <el-dialog
      v-model="copyOpen"
      title="复制平台套餐"
      width="560px"
      destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="平台套餐" required>
          <el-select
            v-model="copyForm.platformPackageId"
            class="full-width"
            filterable
            placeholder="请选择平台套餐">
            <el-option
              v-for="item in platformOptions"
              :key="item.packageId"
              :label="`${item.packageName}（${item.modelName}）`"
              :value="item.packageId" />
          </el-select>
        </el-form-item>
        <el-form-item label="套餐名称">
          <el-input
            v-model="copyForm.packageName"
            placeholder="默认使用平台套餐名称" />
        </el-form-item>
        <el-form-item label="备注" >
          <el-input v-model="copyForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="copyOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="copy">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { computed, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  addMerchantPackage,
  copyPlatformPackage,
  deleteMerchantPackage,
  getMerchantPackage,
  listMerchantPackages,
  listMerchantPlatformProducts,
  listMerchantPackageAccessoryOptions,
  listPlatformPackageOptions,
  updateMerchantPackage,
} from "@/api/merchant/rentalBusiness";
import { listBusinessProductClasses } from "@/api/merchant/productClass";
const loading = ref(false);
const saving = ref(false);
const showSearch = ref(true);
const rows = ref([]);
const total = ref(0);
const dialogOpen = ref(false);
const copyOpen = ref(false);
const detailOpen = ref(false);
const detailLoading = ref(false);
const detail = ref(null);
const editing = ref(false);
const platformOptions = ref([]);
const packageProducts = ref([]);
const deviceProducts = ref([]);
const classOptions = ref([]);
const accessoryProducts = ref([]);
const productItems = ref([]);
const formRef = ref();
const PACKAGE_ITEM_ROLE = Object.freeze({
  MAIN: 1,
  OFFICIAL_ACCESSORY: 2,
  MERCHANT_ACCESSORY: 3,
  NON_MAIN_PRODUCT: 4,
});
const query = reactive({
  keyword: "",
  status: undefined,
  pageNum: 1,
  pageSize: 10,
});
const form = reactive({
  id: undefined,
  packageCode: "",
  productId: undefined,
  classId: undefined,
  modelId: undefined,
  packageName: "",
  status: 0,
  sortNo: 0,
  items: [],
  remark: "",
});
const copyForm = reactive({
  platformPackageId: undefined,
  packageName: "",
  remark: "",
});
const compatibleAccessoryProducts = computed(() =>
  accessoryProducts.value.filter((item) => {
    if (!form.modelId) return false;
    const compatibleModelIds = Array.isArray(item.compatibleModelIds)
      ? item.compatibleModelIds
      : String(item.compatibleModelIds || "")
          .split(",")
          .filter(Boolean)
          .map(Number);
    return (
      compatibleModelIds.includes(Number(form.modelId)) ||
      Number(item.modelId) === Number(form.modelId)
    );
  }),
);
const packageProductOptions = computed(() =>
  packageProducts.value.filter(
    (item) => Number(item.productId || item.id) !== Number(form.productId),
  ),
);
const productDetailRows = computed(() => [
  { isMain: true, productId: form.productId, quantity: 1 },
  ...productItems.value,
]);
const filteredDeviceProducts = computed(() =>
  deviceProducts.value.filter(
    (item) => !form.classId || Number(item.classId) === Number(form.classId),
  ),
);
const rules = {
  productId: [{ required: true, message: "请选择主商品", trigger: "change" }],
  packageName: [{ required: true, message: "请输入套餐名称", trigger: "blur" }],
};
function unwrap(response) {
  return response?.data || response;
}
function load() {
  loading.value = true;
  listMerchantPackages(query)
    .then(({ rows: data, total: count }) => {
      rows.value = data || [];
      total.value = count || 0;
    })
    .finally(() => {
      loading.value = false;
    });
}
function reset() {
  query.keyword = "";
  query.status = undefined;
  query.pageNum = 1;
  load();
}
function loadProducts() {
  return listMerchantPlatformProducts({
    pageNum: 1,
    pageSize: 1000,
    status: 0,
  }).then((response) => {
    const result = unwrap(response);
    const data = result?.rows || result?.data?.rows || [];
    packageProducts.value = data;
    deviceProducts.value = data.filter(
      (item) => Number(item.productType) === 1 || item.productType === "device",
    );
  });
}
function loadAccessoryOptions() {
  accessoryProducts.value = [];
  if (!form.modelId) return Promise.resolve();
  return listMerchantPackageAccessoryOptions(form.modelId).then((response) => {
    const optionResult = unwrap(response) || [];
    accessoryProducts.value = optionResult.map((item) => {
      const official = item.sourceType === "official";
      return {
        ...item,
        productId: official ? undefined : item.productId,
        productCode: official ? item.accessoryCode : item.productCode,
        productName: official ? item.accessoryName : item.productName,
        compatibleModelIds: official ? item.modelIds : item.compatibleModelIds,
        optionKey: `${official ? "official" : "custom"}:${official ? item.accessoryId : item.merchantAccessoryId}`,
      };
    });
  });
}
function loadClasses() {
  return listBusinessProductClasses().then((response) => {
    classOptions.value = flattenClasses(unwrap(response) || []);
  });
}
function flattenClasses(nodes, parents = [], result = []) {
  (nodes || []).forEach((node) => {
    const path = parents.concat(node.className || []);
    if (Number(node.classLevel) === 2)
      result.push({ value: node.classId, label: path.join(" / ") });
    flattenClasses(node.children, path, result);
  });
  return result;
}
function classLabel(classId) {
  return (
    classOptions.value.find((item) => Number(item.value) === Number(classId))
      ?.label ||
    classId ||
    "-"
  );
}
function modelOptionLabel(item) {
  return (
    [item.brand, item.modelName, item.memorySpec].filter(Boolean).join(" / ") ||
    `${item.productName || "型号"}（${item.productCode || ""}）`
  );
}
function productOptionLabel(item) {
  const parts = [
    item.productName,
    item.brand,
    item.modelName,
    item.memorySpec,
  ].filter(Boolean);
  return [...new Set(parts)].join(" / ") || item.productCode || "产品";
}
function modelLabel(modelId) {
  const item = deviceProducts.value.find(
    (product) => Number(product.modelId) === Number(modelId),
  );
  return item ? modelOptionLabel(item) : modelId || "-";
}
function detailClassLabel(classId) {
  return (
    classOptions.value.find((item) => Number(item.value) === Number(classId))
      ?.label || "-"
  );
}
function detailModelLabel(modelId) {
  const item = deviceProducts.value.find(
    (product) => Number(product.modelId) === Number(modelId),
  );
  return item ? modelOptionLabel(item) : "-";
}
function packageItemRoleLabel(itemRole) {
  const labels = {
    [PACKAGE_ITEM_ROLE.MAIN]: "产品",
    [PACKAGE_ITEM_ROLE.OFFICIAL_ACCESSORY]: "配件",
    [PACKAGE_ITEM_ROLE.MERCHANT_ACCESSORY]: "自定义配件",
    [PACKAGE_ITEM_ROLE.NON_MAIN_PRODUCT]: "产品",
  };
  return labels[Number(itemRole)] || "套餐明细";
}
function detailItemName(item) {
  return item.itemName || item.itemModelName || "-";
}
function changeClass() {
  form.productId = undefined;
  form.modelId = undefined;
  productItems.value = [];
  form.items = [];
  accessoryProducts.value = [];
}
function openCreate() {
  editing.value = false;
  Object.assign(form, {
    id: undefined,
    packageCode: "",
    productId: undefined,
    classId: undefined,
    modelId: undefined,
    packageName: "",
    status: 0,
    sortNo: 0,
    items: [],
    remark: "",
  });
  productItems.value = [];
  accessoryProducts.value = [];
  loadProducts().then(() => {
    dialogOpen.value = true;
  });
}
function openEdit(row) {
  editing.value = true;
  Promise.all([loadProducts(), getMerchantPackage(row.packageId)]).then(
    ([, response]) => {
      const data = unwrap(response);
      const main = (data.items || []).find(
        (item) => Number(item.itemRole) === PACKAGE_ITEM_ROLE.MAIN,
      );
      Object.assign(form, {
        id: data.packageId,
        productId: main?.productId,
        classId: data.classId,
        modelId: data.modelId,
        packageName: data.packageName,
        status: Number(data.status),
        sortNo: data.sortNo || 0,
        remark: data.remark || "",
        items: [],
      });
      productItems.value = (data.items || [])
        .filter(
          (item) =>
            Number(item.itemRole) === PACKAGE_ITEM_ROLE.NON_MAIN_PRODUCT,
        )
        .map((item, index) => ({
          _key: `product-${item.packageItemId || index}`,
          productId: item.productId,
          quantity: item.quantity || 1,
          requiredFlag: item.requiredFlag ?? 1,
        }));
      return loadAccessoryOptions().then(() => {
        form.items = (data.items || [])
          .filter((item) =>
            [
              PACKAGE_ITEM_ROLE.OFFICIAL_ACCESSORY,
              PACKAGE_ITEM_ROLE.MERCHANT_ACCESSORY,
            ].includes(Number(item.itemRole)),
          )
          .map((item, index) => ({
            accessoryId:
              Number(item.itemRole) === PACKAGE_ITEM_ROLE.OFFICIAL_ACCESSORY
                ? item.accessoryId
                : undefined,
            accessoryItemProductId: item.accessoryItemProductId,
            merchantAccessoryId: item.merchantAccessoryId,
            accessoryOptionKey: accessoryProducts.value.find((option) =>
              Number(item.itemRole) === PACKAGE_ITEM_ROLE.OFFICIAL_ACCESSORY
                ? Number(option.accessoryId) === Number(item.accessoryId)
                : Number(option.merchantAccessoryId) ===
                  Number(item.merchantAccessoryId),
            )?.optionKey,
            quantity: item.quantity || 1,
            requiredFlag: item.requiredFlag ?? 1,
            sortNo: item.sortNo || 0,
          }));
        dialogOpen.value = true;
      });
    },
  );
}
async function openDetail(row) {
  detailOpen.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    const response = await getMerchantPackage(row.packageId);
    detail.value = unwrap(response);
  } finally {
    detailLoading.value = false;
  }
}
function changeProduct(productId) {
  const product = deviceProducts.value.find(
    (item) => Number(item.productId || item.id) === Number(productId),
  );
  form.items = [];
  accessoryProducts.value = [];
  if (!product) {
    form.modelId = undefined;
    productItems.value = [];
    return Promise.resolve();
  }
  form.classId = product.classId;
  form.modelId = product.modelId;
  return loadAccessoryOptions();
}
function addProductItem() {
  if (!form.productId) return ElMessage.warning("请先选择主产品");
  productItems.value.push({
    _key: `product-${Date.now()}-${productItems.value.length}`,
    productId: undefined,
    quantity: 1,
    requiredFlag: 1,
  });
}
function removeProductItem(index) {
  productItems.value.splice(index, 1);
}
function addItem() {
  if (!form.modelId) return ElMessage.warning("请先选择主产品");
  form.items.push({
    accessoryItemProductId: undefined,
    merchantAccessoryId: undefined,
    accessoryId: undefined,
    accessoryOptionKey: undefined,
    quantity: 1,
    requiredFlag: 1,
    sortNo: form.items.length,
  });
}
function submit() {
  formRef.value.validate((valid) => {
    if (!valid) return;
    if (productItems.value.some((item) => !item.productId))
      return ElMessage.warning("请完整填写产品明细");
    if (form.items.some((item) => !item.accessoryOptionKey))
      return ElMessage.warning("请完整填写配件明细");
    const productDetailItems = productItems.value.map((item, index) => ({
      itemRole: PACKAGE_ITEM_ROLE.NON_MAIN_PRODUCT,
      productId: item.productId,
      quantity: item.quantity || 1,
      requiredFlag: item.requiredFlag ?? 1,
      sortNo: index + 1,
    }));
    const accessoryItems = form.items.map((item, index) => {
      const option = accessoryProducts.value.find(
        (candidate) => candidate.optionKey === item.accessoryOptionKey,
      );
      return {
        itemRole:
          option?.sourceType === "official"
            ? PACKAGE_ITEM_ROLE.OFFICIAL_ACCESSORY
            : PACKAGE_ITEM_ROLE.MERCHANT_ACCESSORY,
        accessoryId:
          option?.sourceType === "official" ? option.accessoryId : undefined,
        merchantAccessoryId:
          option?.sourceType === "custom"
            ? option.merchantAccessoryId
            : undefined,
        accessoryItemProductId:
          option?.sourceType === "custom" ? option.productId : undefined,
        quantity: item.quantity || 1,
        requiredFlag: item.requiredFlag ?? 1,
        sortNo: productItems.value.length + index + 1,
      };
    });
    const data = {
      ...form,
      items: [
        {
          itemRole: PACKAGE_ITEM_ROLE.MAIN,
          productId: form.productId,
          quantity: 1,
          requiredFlag: 1,
          sortNo: 0,
        },
        ...productDetailItems,
        ...accessoryItems,
      ],
    };
    saving.value = true;
    (editing.value ? updateMerchantPackage : addMerchantPackage)(data)
      .then(() => {
        ElMessage.success("保存成功");
        dialogOpen.value = false;
        load();
      })
      .finally(() => {
        saving.value = false;
      });
  });
}
function openCopy() {
  listPlatformPackageOptions().then((response) => {
    platformOptions.value = unwrap(response) || [];
    Object.assign(copyForm, {
      platformPackageId: undefined,
      packageName: "",
      remark: "",
    });
    copyOpen.value = true;
  });
}
function copy() {
  if (!copyForm.platformPackageId) return ElMessage.warning("请选择平台套餐");
  saving.value = true;
  copyPlatformPackage({ ...copyForm })
    .then(() => {
      ElMessage.success("复制成功");
      copyOpen.value = false;
      load();
    })
    .finally(() => {
      saving.value = false;
    });
}
function removeRow(row) {
  ElMessageBox.confirm(`确认删除“${row.packageName}”吗？`, "操作确认", {
    type: "warning",
  })
    .then(() => deleteMerchantPackage(row.packageId))
    .then(() => {
      ElMessage.success("删除成功");
      load();
    });
}
loadClasses();
loadProducts();
load();
</script>
<style scoped>
.full-width {
  width: 100%;
}
.item-table {
  width: 100%;
}
.add-item {
  margin-top: 10px;
}
.detail-remark {
  margin-top: 16px;
}
</style>
