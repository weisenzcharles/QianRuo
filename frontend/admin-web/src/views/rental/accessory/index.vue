<template>
  <div class="app-container">
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item label="配件关键词" prop="keyword"
        ><el-input
          v-model="queryParams.keyword"
          clearable
          placeholder="请输入配件编码或名称"
          @keyup.enter="handleQuery"
      /></el-form-item>
      <el-form-item label="状态" prop="status"
        ><el-select v-model="queryParams.status" clearable placeholder="请选择"
          ><el-option
            label="正常"
            :value="NORMAL_DISABLE_CODE.NORMAL" /><el-option
            label="停用"
            :value="NORMAL_DISABLE_CODE.DISABLED" /></el-select
      ></el-form-item>
      <el-form-item
        ><el-button type="primary" icon="Search" @click="handleQuery"
          >搜索</el-button
        ><el-button icon="Refresh" @click="resetQuery"
          >重置</el-button
        ></el-form-item
      >
    </el-form>
    <el-row :gutter="10" class="mb8"
      ><el-col :span="1.5"
        ><el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['platform:accessory:add']"
          >新增配件</el-button
        ></el-col
      ><right-toolbar v-model:showSearch="showSearch" @queryTable="getList"
    /></el-row>
    <el-table v-loading="loading" :data="list">
      <el-table-column
        label="配件编号"
        prop="accessoryId"
        width="90"
        align="center"
      />
      <el-table-column label="配件编码" prop="accessoryCode" min-width="150" />
      <el-table-column label="配件名称" prop="accessoryName" min-width="150" />
      <el-table-column
        label="所属分类"
        prop="className"
        min-width="220"
        show-overflow-tooltip
      />
      <el-table-column label="品牌" prop="brandName" width="120" />
      <el-table-column
        label="兼容型号数"
        prop="modelCount"
        width="110"
        align="center"
      />
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template #default="{ row }">
          <el-tag
            :type="
              row.status === NORMAL_DISABLE_CODE.NORMAL ? 'success' : 'info'
            "
            >{{ row.status === NORMAL_DISABLE_CODE.NORMAL ? "正常" : "停用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(row)"
            v-hasPermi="['platform:accessory:edit']"
            >修改</el-button
          >
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(row)"
            v-hasPermi="['platform:accessory:remove']"
            >删除</el-button
          ></template
        ></el-table-column
      >
    </el-table>
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
    <el-dialog
      v-model="open"
      :title="title"
      width="720px"
      append-to-body
      @closed="resetDialogForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item v-if="form.accessoryId" label="配件编码" prop="accessoryCode">
          <el-input
            v-model="form.accessoryCode"
            disabled
            placeholder="保存后由编码配置自动生成"
          />
        </el-form-item>
        <el-form-item label="配件名称" prop="accessoryName">
          <el-input v-model="form.accessoryName" maxlength="128" />
        </el-form-item>
        <el-form-item label="所属分类" prop="classId">
          <el-tree-select
            v-model="form.classId"
            :data="classOptions"
            :props="treeProps"
            check-strictly
            filterable
            default-expand-all
            placeholder="请选择二级分类"
            style="width: 100%"
            @change="handleClassChange"
          />
        </el-form-item>
        <el-form-item label="品牌" prop="brandId">
          <el-select
            v-model="form.brandId"
            clearable
            filterable
            placeholder="可选品牌"
            style="width: 100%"
            @change="loadModels"
          >
            <el-option
              v-for="item in brands"
              :key="item.brandId"
              :label="item.brandName"
              :value="item.brandId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="兼容型号" prop="modelIdList">
          <el-select
            v-model="form.modelIdList"
            multiple
            filterable
            placeholder="请选择兼容型号"
            collapse-tags
            collapse-tags-tooltip
            :max-collapse-tags="5"
            style="width: 100%"
          >
            <el-option
              v-for="item in models"
              :key="item.modelId"
              :label="item.modelName"
              :value="item.modelId"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规格" prop="specification">
              <el-input v-model="form.specification" maxlength="255" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" maxlength="32" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="配件说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch
                v-model="form.status"
                :active-value="NORMAL_DISABLE_CODE.NORMAL"
                :inactive-value="NORMAL_DISABLE_CODE.DISABLED"
                active-text="正常"
                inactive-text="停用"
                inline-prompt
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm"> 确定 </el-button>
        <el-button @click="cancel">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Accessory">
import {
  addAccessory,
  deleteAccessory,
  getAccessory,
  listAccessories,
  updateAccessory,
} from "@/api/rental/accessory";
import { listProductClass } from "@/api/rental/productClass";
import { listBrands } from "@/api/rental/brand";
import { listBrandModels } from "@/api/rental/brandModel";
import { NORMAL_DISABLE_CODE } from "@/utils/stateCode";

const { proxy } = getCurrentInstance();
const list = ref([]);
const brands = ref([]);
const models = ref([]);
const classOptions = ref([]);
const secondLevelClassIds = ref(new Set());
const total = ref(0);
const loading = ref(false);
const showSearch = ref(true);
const open = ref(false);
const title = ref("");
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  status: undefined,
});
const form = ref(emptyForm());
const treeProps = {
  value: "classId",
  label: "classLabel",
  children: "children",
  disabled: "disabled",
};
const rules = {
  accessoryName: [
    { required: true, message: "配件名称不能为空", trigger: "blur" },
  ],
  classId: [{ required: true, message: "所属分类不能为空", trigger: "change" }],
  modelIdList: [
    {
      required: true,
      type: "array",
      min: 1,
      message: "至少选择一个兼容型号",
      trigger: "change",
    },
  ],
};

function getList() {
  loading.value = true;
  return listAccessories(queryParams)
    .then((r) => {
      list.value = r.rows || [];
      total.value = r.total || 0;
    })
    .finally(() => {
      loading.value = false;
    });
}
function loadOptions() {
  return Promise.all([
    listProductClass({ status: NORMAL_DISABLE_CODE.NORMAL }).then((r) => {
      const ids = new Set();
      classOptions.value = buildClassOptions(r.data || [], [], ids);
      secondLevelClassIds.value = ids;
    }),
    listBrands({ status: NORMAL_DISABLE_CODE.NORMAL }).then((r) => {
      brands.value = r.rows || [];
    }),
  ]);
}
function buildClassOptions(nodes, parentNames, secondLevelIds) {
  return (nodes || []).map((node) => {
    const names = parentNames.concat(node.className);
    const classId = Number(node.classId);
    const isSecondLevel = Number(node.classLevel) === 2;
    if (isSecondLevel) secondLevelIds.add(classId);
    return {
      ...node,
      classId,
      classLabel: names.join(" / "),
      disabled: !isSecondLevel,
      children: buildClassOptions(node.children || [], names, secondLevelIds),
    };
  });
}
function loadModels() {
  if (!form.value.classId) {
    models.value = [];
    return Promise.resolve();
  }
  return listBrandModels({
    classId: form.value.classId,
    brandId: form.value.brandId,
    status: NORMAL_DISABLE_CODE.NORMAL,
    pageNum: 1,
    pageSize: 500,
  }).then((r) => {
    models.value = r.rows || [];
  });
}
function handleClassChange(classId) {
  if (classId && !secondLevelClassIds.value.has(Number(classId))) {
    form.value.classId = undefined;
    form.value.modelIdList = [];
    models.value = [];
    proxy.$modal.msgWarning("配件所属分类只能选择二级分类");
    return;
  }
  form.value.modelIdList = [];
  loadModels();
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
  models.value = [];
  title.value = "新增配件";
  Promise.all([loadOptions(), getList()]).then(() => {
    open.value = true;
  });
}
function handleUpdate(row) {
  Promise.all([getAccessory(row.accessoryId), loadOptions(), getList()])
    .then(([r]) => {
      form.value = {
        ...emptyForm(),
        ...r.data,
        modelIdList: (r.data.modelIdList || []).map(Number),
      };
      return loadModels();
    })
    .then(() => {
      title.value = "修改配件";
      open.value = true;
    });
}
function handleDelete(row) {
  proxy.$modal
    .confirm(`是否确认删除配件“${row.accessoryName}”？`)
    .then(() => deleteAccessory(row.accessoryId))
    .then(() => {
      proxy.$modal.msgSuccess("删除成功");
      getList();
    })
    .catch(() => {});
}
function submitForm() {
  proxy.$refs.formRef.validate((valid) => {
    if (!valid) return;
    const action = form.value.accessoryId ? updateAccessory : addAccessory;
    action(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.accessoryId ? "修改成功" : "新增成功");
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
  models.value = [];
  proxy.resetForm("formRef");
}
function emptyForm() {
  return {
    accessoryId: undefined,
    accessoryCode: "",
    accessoryName: "",
    classId: undefined,
    brandId: undefined,
    modelIdList: [],
    specification: "",
    unit: "",
    description: "",
    status: NORMAL_DISABLE_CODE.NORMAL,
    remark: "",
  };
}
loadOptions();
getList();
</script>
