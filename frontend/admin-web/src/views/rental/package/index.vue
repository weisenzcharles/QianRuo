<template>
  <div class="app-container">
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch">
      <el-form-item label="套餐关键词" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          clearable
          placeholder="请输入套餐编码或名称"
          @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择">
          <el-option label="正常" :value="NORMAL_DISABLE_CODE.NORMAL" />
          <el-option label="停用" :value="NORMAL_DISABLE_CODE.DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">
          搜索
        </el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['platform:package:add']">
          新增套餐
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>
    <el-table v-loading="loading" :data="list">
      <el-table-column
        label="套餐编号"
        prop="packageId"
        width="90"
        align="center" />
      <el-table-column label="套餐编码" prop="packageCode" min-width="120" />
      <el-table-column label="套餐名称" prop="packageName" min-width="200" />
      <el-table-column label="主产品型号" prop="modelName" min-width="160" />
      <el-table-column label="所属分类" prop="className" min-width="200" />
      <el-table-column
        label="版本"
        prop="versionNo"
        width="80"
        align="center" />
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template #default="{ row }">
          <el-tag
            :type="
              row.status === NORMAL_DISABLE_CODE.NORMAL ? 'success' : 'info'
            ">
            {{ row.status === NORMAL_DISABLE_CODE.NORMAL ? "正常" : "停用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(row)"
            v-hasPermi="['platform:package:edit']">
            修改
          </el-button>
          <el-button
            link
            type="success"
            icon="Check"
            @click="handlePublish(row)"
            v-hasPermi="['platform:package:edit']">
            发布
          </el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(row)"
            v-hasPermi="['platform:package:remove']">
            删除
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
      v-model="open"
      :title="title"
      width="760px"
      append-to-body
      @closed="resetDialogForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item v-if="form.packageId" label="套餐编码" prop="packageCode">
          <el-input
            v-model="form.packageCode"
            disabled
            placeholder="保存后由编码配置自动生成" />
        </el-form-item>

        <el-form-item label="套餐名称" prop="packageName">
          <el-input v-model="form.packageName" maxlength="128" />
        </el-form-item>

        <el-form-item label="产品分类" prop="classId">
          <el-select
            v-model="form.classId"
            filterable
            style="width: 100%"
            @change="handleClassChange">
            <el-option
              v-for="item in classes"
              :key="item.classId"
              :label="item.fullName"
              :value="item.classId" />
          </el-select>
        </el-form-item>

        <el-form-item label="主产品型号" prop="modelId">
          <el-select
            v-model="form.modelId"
            filterable
            style="width: 100%"
            @change="handleModelChange">
            <el-option
              v-for="item in models"
              :key="item.modelId"
              :label="item.modelName"
              :value="item.modelId" />
          </el-select>
        </el-form-item>

        <el-form-item label="产品明细">
          <div class="item-list">
            <div class="item-row">
              <el-select
                :model-value="form.modelId"
                disabled
                placeholder="请选择主产品型号">
                <el-option
                  v-for="model in models"
                  :key="model.modelId"
                  :label="model.modelName"
                  :value="model.modelId" />
              </el-select>
              <el-input-number
                :model-value="1"
                disabled
                :min="1"
                controls-position="right" />
            </div>
            <div
              v-for="(item, index) in productItems"
              :key="item._key"
              class="item-row">
              <el-select
                v-model="item.modelId"
                filterable
                placeholder="选择产品型号">
                <el-option
                  v-for="model in productModels"
                  :key="model.modelId"
                  :label="model.modelName"
                  :value="model.modelId" />
              </el-select>
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="999"
                controls-position="right" />
              <el-button
                link
                type="danger"
                icon="Delete"
                @click="removeProductItem(index)" />
            </div>
            <el-button
              link
              type="primary"
              icon="Plus"
              :disabled="!form.modelId"
              @click="addProductItem">
              添加产品
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="配件明细">
          <div class="item-list">
            <div
              v-for="(item, index) in accessoryItems"
              :key="item._key"
              class="item-row">
              <el-select
                v-model="item.accessoryId"
                filterable
                placeholder="选择平台配件">
                <el-option
                  v-for="accessory in accessories"
                  :key="accessory.accessoryId"
                  :label="accessory.accessoryName"
                  :value="accessory.accessoryId" />
              </el-select>
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="999"
                controls-position="right" />
              <el-button
                link
                type="danger"
                icon="Delete"
                @click="removeItem(index)" />
            </div>
            <el-button
              link
              type="primary"
              icon="Plus"
              :disabled="!form.modelId"
              @click="addItem">
              添加配件
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序" prop="sortNo">
              <el-input-number
                v-model="form.sortNo"
                :min="0"
                controls-position="right"
                style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch
                v-model="form.status"
                :active-value="NORMAL_DISABLE_CODE.NORMAL"
                :inactive-value="NORMAL_DISABLE_CODE.DISABLED"
                active-text="正常"
                inactive-text="停用"
                inline-prompt />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RentalPackage">
import {
  addPackage,
  deletePackage,
  getPackage,
  listPackages,
  publishPackage,
  updatePackage,
} from "@/api/rental/package";
import { listProductClass } from "@/api/rental/productClass";
import { listBrandModels } from "@/api/rental/brandModel";
import { listAccessories } from "@/api/rental/accessory";
import { NORMAL_DISABLE_CODE } from "@/utils/stateCode";
const { proxy } = getCurrentInstance();
const list = ref([]);
const classes = ref([]);
const models = ref([]);
const productModels = ref([]);
const accessories = ref([]);
const total = ref(0);
const loading = ref(false);
const showSearch = ref(true);
const open = ref(false);
const title = ref("");
const sequence = ref(0);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  status: undefined,
});
const form = ref(emptyForm());
const productItems = ref([]);
const accessoryItems = ref([]);
const PACKAGE_ITEM_ROLE = Object.freeze({
  MAIN: 1,
  ACCESSORY: 2,
  NON_MAIN_PRODUCT: 4,
});
const rules = {
  packageName: [
    { required: true, message: "套餐名称不能为空", trigger: "blur" },
  ],
  classId: [{ required: true, message: "产品分类不能为空", trigger: "change" }],
  modelId: [
    { required: true, message: "主产品型号不能为空", trigger: "change" },
  ],
};
function getList() {
  loading.value = true;
  return listPackages(queryParams)
    .then((r) => {
      list.value = r.rows || [];
      total.value = r.total || 0;
    })
    .finally(() => {
      loading.value = false;
    });
}
function flatten(nodes, parent = "") {
  return (nodes || []).reduce((out, node) => {
    const fullName = parent ? `${parent} / ${node.className}` : node.className;
    if (Number(node.classLevel) === 2)
      out.push({ ...node, classId: Number(node.classId), fullName });
    return out.concat(flatten(node.children, fullName));
  }, []);
}
function loadOptions() {
  return Promise.all([
    listProductClass({ status: NORMAL_DISABLE_CODE.NORMAL }).then((r) => {
      classes.value = flatten(r.data || []);
    }),
    listBrandModels({
      status: NORMAL_DISABLE_CODE.NORMAL,
      pageNum: 1,
      pageSize: 500,
    }).then((r) => {
      productModels.value = r.rows || [];
    }),
  ]);
}
function loadModels(reset = true) {
  if (reset) form.value.modelId = undefined;
  models.value = [];
  if (!form.value.classId) return Promise.resolve();
  return listBrandModels({
    classId: form.value.classId,
    status: NORMAL_DISABLE_CODE.NORMAL,
    pageNum: 1,
    pageSize: 500,
  }).then((r) => {
    models.value = r.rows || [];
  });
}
function loadAccessories() {
  accessories.value = [];
  if (!form.value.modelId) return Promise.resolve();
  return listAccessories({
    status: NORMAL_DISABLE_CODE.NORMAL,
    modelId: form.value.modelId,
    pageNum: 1,
    pageSize: 500,
  }).then((r) => {
    accessories.value = r.rows || [];
  });
}
function handleClassChange() {
  form.value.modelId = undefined;
  models.value = [];
  productItems.value = [];
  accessoryItems.value = [];
  accessories.value = [];
  return loadModels(false);
}
function handleModelChange() {
  productItems.value = productItems.value.filter(
    (item) => Number(item.modelId) !== Number(form.value.modelId),
  );
  accessoryItems.value = [];
  syncMainItem();
  return loadAccessories().then(syncMainItem);
}
function syncMainItem() {
  form.value.items = [
    {
      itemRole: PACKAGE_ITEM_ROLE.MAIN,
      modelId: form.value.modelId,
      quantity: 1,
      requiredFlag: 1,
      sortNo: 0,
    },
    ...productItems.value.map((item, index) => ({
      modelId: item.modelId,
      itemRole: PACKAGE_ITEM_ROLE.NON_MAIN_PRODUCT,
      quantity: item.quantity,
      requiredFlag: item.requiredFlag ?? 1,
      sortNo: index + 1,
    })),
    ...accessoryItems.value.map((item, index) => ({
      ...item,
      itemRole: PACKAGE_ITEM_ROLE.ACCESSORY,
      sortNo: productItems.value.length + index + 1,
    })),
  ];
}
function addProductItem() {
  if (!form.value.modelId) {
    proxy.$modal.msgWarning("请先选择主产品型号");
    return;
  }
  productItems.value.push({
    _key: sequence.value++,
    modelId: undefined,
    quantity: 1,
    requiredFlag: 1,
  });
  syncMainItem();
}
function removeProductItem(index) {
  productItems.value.splice(index, 1);
  syncMainItem();
}
function addItem() {
  if (!form.value.modelId) {
    proxy.$modal.msgWarning("请先选择主产品型号");
    return;
  }
  accessoryItems.value.push({
    _key: sequence.value++,
    accessoryId: undefined,
    quantity: 1,
    requiredFlag: 1,
  });
  syncMainItem();
}
function removeItem(index) {
  accessoryItems.value.splice(index, 1);
  syncMainItem();
}
function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}
function handleAdd() {
  form.value = emptyForm();
  productItems.value = [];
  accessoryItems.value = [];
  models.value = [];
  accessories.value = [];
  title.value = "新增套餐";
  Promise.all([loadOptions(), getList()]).then(() => {
    open.value = true;
  });
}
function handleUpdate(row) {
  Promise.all([getPackage(row.packageId), loadOptions(), getList()])
    .then(([r]) => {
      const data = r.data || {};
      form.value = { ...emptyForm(), ...data };
      productItems.value = (data.items || [])
        .filter(
          (item) =>
            Number(item.itemRole) === PACKAGE_ITEM_ROLE.NON_MAIN_PRODUCT,
        )
        .map((item) => ({ ...item, _key: sequence.value++ }));
      accessoryItems.value = (data.items || [])
        .filter((item) => Number(item.itemRole) === PACKAGE_ITEM_ROLE.ACCESSORY)
        .map((item) => ({ ...item, _key: sequence.value++ }));
      return Promise.all([loadModels(false), loadAccessories()]).then(
        syncMainItem,
      );
    })
    .then(() => {
      title.value = "修改套餐";
      open.value = true;
    });
}
function handlePublish(row) {
  proxy.$modal
    .confirm(`是否确认发布套餐“${row.packageName}”？`)
    .then(() => publishPackage(row.packageId))
    .then(() => {
      proxy.$modal.msgSuccess("发布成功");
      getList();
    })
    .catch(() => {});
}
function handleDelete(row) {
  proxy.$modal
    .confirm(`是否确认删除套餐“${row.packageName}”？`)
    .then(() => deletePackage(row.packageId))
    .then(() => {
      proxy.$modal.msgSuccess("删除成功");
      getList();
    })
    .catch(() => {});
}
function submitForm() {
  syncMainItem();
  proxy.$refs.formRef.validate((valid) => {
    if (!valid) return;
    const payload = {
      ...form.value,
      items: form.value.items.map(({ _key, ...item }) => item),
    };
    const action = form.value.packageId ? updatePackage : addPackage;
    action(payload).then(() => {
      proxy.$modal.msgSuccess(form.value.packageId ? "修改成功" : "新增成功");
      open.value = false;
      getList();
    });
  });
}
function cancel() {
  open.value = false;
}
function resetDialogForm() {
  form.value = emptyForm();
  productItems.value = [];
  accessoryItems.value = [];
  models.value = [];
  accessories.value = [];
  proxy.resetForm("formRef");
}
function emptyForm() {
  return {
    packageId: undefined,
    packageCode: "",
    packageName: "",
    classId: undefined,
    modelId: undefined,
    status: 0,
    sortNo: 0,
    items: [],
    remark: "",
  };
}
loadOptions();
getList();
</script>

<style scoped>
.item-list {
  width: 100%;
}
.item-row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 8px;
}
.item-row .el-select {
  flex: 1;
}
.item-row .el-input {
  flex: 1;
}
.item-role-label {
  flex: 0 0 68px;
  color: var(--el-text-color-secondary);
}
</style>
