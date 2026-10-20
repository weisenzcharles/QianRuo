<template>
  <div class="app-container">
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item label="品牌" prop="brand">
        <el-input
          v-model="queryParams.brand"
          placeholder="请输入品牌"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="型号" prop="modelName">
        <el-input
          v-model="queryParams.modelName"
          placeholder="请输入型号"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分类" prop="classId">
        <el-tree-select
          v-model="queryParams.classId"
          :data="classOptions"
          :props="treeProps"
          check-strictly
          clearable
          filterable
          placeholder="请选择分类"
          style="width: 220px"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="型号状态"
          clearable
          style="width: 140px"
        >
          <el-option label="正常" :value="NORMAL_DISABLE_CODE.NORMAL" />
          <el-option label="停用" :value="NORMAL_DISABLE_CODE.DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery"
          >搜索</el-button
        >
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
          v-hasPermi="['platform:brandModel:add']"
          >新增型号</el-button
        >
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="modelList">
      <el-table-column
        label="型号编号"
        prop="modelId"
        width="90"
        align="center"
      />
      <el-table-column
        label="产品分类"
        prop="className"
        min-width="150"
        show-overflow-tooltip
      />
      <el-table-column label="品牌" prop="brand" width="140" />
      <el-table-column label="型号" prop="modelName" min-width="180" />
      <el-table-column
        label="内存/规格"
        prop="memorySpec"
        min-width="130"
        show-overflow-tooltip
      />
      <el-table-column
        label="官方标准"
        prop="officialFlag"
        width="100"
        align="center"
      >
        <template #default="scope">
          <el-tag
            :type="
              scope.row.officialFlag === YES_NO_CODE.YES ? 'primary' : 'info'
            "
          >
            {{ scope.row.officialFlag === YES_NO_CODE.YES ? "是" : "否" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template #default="scope">
          <el-tag
            :type="
              scope.row.status === NORMAL_DISABLE_CODE.NORMAL
                ? 'success'
                : 'info'
            "
          >
            {{
              scope.row.status === NORMAL_DISABLE_CODE.NORMAL ? "正常" : "停用"
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        prop="createTime"
        width="180"
        align="center"
      />
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['platform:brandModel:edit']"
            >修改</el-button
          >
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['platform:brandModel:remove']"
            >删除</el-button
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

    <el-dialog
      v-model="dialogOpen"
      :title="dialogTitle"
      width="620px"
      append-to-body
      @closed="resetDialogForm"
    >
      <el-form
        ref="modelFormRef"
        :model="form"
        :rules="rules"
        label-width="110px"
      >
        <el-form-item label="产品分类" prop="classId">
          <el-tree-select
            v-model="form.classId"
            :data="classOptions"
            :props="treeProps"
            check-strictly
            filterable
            default-expand-all
            placeholder="请选择产品分类"
            style="width: 100%"
            @change="handleClassChange"
          />
        </el-form-item>
        <el-form-item label="品牌" prop="brandId">
          <el-select
            v-model="form.brandId"
            filterable
            clearable
            :disabled="!form.classId"
            :placeholder="form.classId ? '请选择品牌' : '请先选择产品分类'"
            style="width: 100%"
          >
            <el-option
              v-for="item in brandOptions"
              :key="item.brandId"
              :label="item.brandName"
              :value="item.brandId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="型号" prop="modelName">
          <el-input
            v-model="form.modelName"
            maxlength="128"
            show-word-limit
            placeholder="请输入型号"
          />
        </el-form-item>

        <el-form-item label="别名" prop="alias">
          <el-input
            v-model="form.alias"
            maxlength="128"
            show-word-limit
            placeholder="请输入别名，可选"
          />
        </el-form-item>

        <el-form-item label="规格" prop="memorySpec"
          ><el-input
            v-model="form.memorySpec"
            maxlength="64"
            show-word-limit
            placeholder="请输入规格，可选"
        /></el-form-item>
        <el-form-item label="备注" prop="remark"
          ><el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入备注"
        /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"
            ><el-form-item label="官方标准" prop="officialFlag"
              ><el-radio-group v-model="form.officialFlag"
                ><el-radio :value="YES_NO_CODE.YES">是</el-radio
                ><el-radio :value="YES_NO_CODE.NO">否</el-radio></el-radio-group
              ></el-form-item
            ></el-col
          >
          <el-col :span="12"
            ><el-form-item label="状态" prop="status"
              ><el-switch
                v-model="form.status"
                :active-value="NORMAL_DISABLE_CODE.NORMAL"
                :inactive-value="NORMAL_DISABLE_CODE.DISABLED"
                active-text="正常"
                inactive-text="停用"
                inline-prompt /></el-form-item
          ></el-col>
        </el-row>
      </el-form>
      <template #footer
        ><el-button type="primary" @click="submitForm">确定</el-button
        ><el-button @click="cancel">取消</el-button></template
      >
    </el-dialog>
  </div>
</template>

<script setup name="BrandModel">
import { listProductClass } from "@/api/rental/productClass";
import { listBrands } from "@/api/rental/brand";
import {
  addBrandModel,
  deleteBrandModel,
  getBrandModel,
  listBrandModels,
  updateBrandModel,
} from "@/api/rental/brandModel";
import { NORMAL_DISABLE_CODE, YES_NO_CODE } from "@/utils/stateCode";

const { proxy } = getCurrentInstance();
const modelList = ref([]);
const classOptions = ref([]);
const brandOptions = ref([]);
const total = ref(0);
const loading = ref(false);
const showSearch = ref(true);
const dialogOpen = ref(false);
const dialogTitle = ref("");
const treeProps = {
  value: "classId",
  label: "classLabel",
  children: "children",
  disabled: "disabled",
};
const secondLevelClassIds = ref(new Set());
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  brand: undefined,
  modelName: undefined,
  classId: undefined,
  status: undefined,
});
const form = ref({});
const rules = {
  classId: [{ required: true, message: "产品分类不能为空", trigger: "change" }],
  brandId: [{ required: true, message: "品牌不能为空", trigger: "change" }],
  modelName: [{ required: true, message: "型号不能为空", trigger: "blur" }],
  officialFlag: [
    { required: true, message: "官方标准标识不能为空", trigger: "change" },
  ],
  status: [{ required: true, message: "状态不能为空", trigger: "change" }],
};

/** 查询平台标准品牌型号列表。 */
function getList() {
  loading.value = true;
  return listBrandModels(queryParams)
    .then((response) => {
      modelList.value = response.rows || [];
      total.value = response.total || 0;
    })
    .finally(() => {
      loading.value = false;
    });
}

/** 加载正常产品分类，作为型号归属选择。 */
function loadClassOptions() {
  return listProductClass({ status: NORMAL_DISABLE_CODE.NORMAL }).then((response) => {
    const ids = new Set();
    classOptions.value = buildClassOptions(response.data || [], [], ids);
    secondLevelClassIds.value = ids;
  });
}

/** 加载正常品牌，标准型号只允许关联平台已启用品牌。 */
function loadBrandOptions(classId) {
  if (!classId) {
    brandOptions.value = [];
    return Promise.resolve();
  }
  return listBrands({
    pageNum: 1,
    pageSize: 1000,
    status: NORMAL_DISABLE_CODE.NORMAL,
    classId,
  }).then((response) => {
    brandOptions.value = response.rows || [];
  });
}

/** 切换产品分类后，仅加载已绑定该分类的启用品牌。 */
function handleClassChange(classId) {
  const normalizedClassId = Number(classId);
  if (classId && !secondLevelClassIds.value.has(normalizedClassId)) {
    form.value.classId = undefined;
    form.value.brandId = undefined;
    brandOptions.value = [];
    proxy.$modal.msgWarning("产品型号所属分类只能选择二级分类");
    return;
  }
  form.value.brandId = undefined;
  loadBrandOptions(normalizedClassId);
}

/** 重置查询条件并回到第一页。 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.pageNum = 1;
  getList();
}

/** 根据当前查询条件刷新标准型号列表。 */
function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

/** 打开新增标准型号表单。 */
function handleAdd() {
  form.value = emptyForm();
  dialogTitle.value = "新增标准型号";
  Promise.all([loadClassOptions(), getList()]).then(() => {
    if (secondLevelClassIds.value.size === 0) {
      proxy.$modal.msgWarning("请先新增并启用产品分类");
      return;
    }
    dialogOpen.value = true;
  });
}

/** 加载指定标准型号详情并打开编辑表单。 */
function handleUpdate(row) {
  Promise.all([getBrandModel(row.modelId), loadClassOptions(), getList()])
    .then(([response]) => {
    form.value = {
      modelId: response.data.modelId,
      classId: response.data.classId,
      brandId: response.data.brandId,
      modelName: response.data.modelName,
      alias: response.data.alias,
      memorySpec: response.data.memorySpec,
      officialFlag: response.data.officialFlag,
      status: response.data.status,
      remark: response.data.remark,
    };
      return loadBrandOptions(response.data.classId);
    })
    .then(() => {
      dialogTitle.value = "修改标准型号";
      dialogOpen.value = true;
    });
}

/** 确认后软删除未被引用的标准型号。 */
function handleDelete(row) {
  proxy.$modal
    .confirm(`是否确认删除标准型号“${row.brand} ${row.modelName}”？`)
    .then(() => deleteBrandModel(row.modelId))
    .then(() => {
      proxy.$modal.msgSuccess("删除成功");
      getList();
    })
    .catch(() => {});
}

/** 校验表单后新增或修改标准型号。 */
function submitForm() {
  proxy.$refs.modelFormRef.validate((valid) => {
    if (!valid) return;
    const payload = {
      modelId: form.value.modelId,
      classId: form.value.classId,
      brandId: form.value.brandId,
      modelName: form.value.modelName,
      alias: form.value.alias,
      memorySpec: form.value.memorySpec,
      officialFlag: form.value.officialFlag,
      status: form.value.status,
      remark: form.value.remark,
    };
    const request = form.value.modelId
      ? updateBrandModel(payload)
      : addBrandModel(payload);
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.modelId ? "修改成功" : "新增成功");
      dialogOpen.value = false;
      getList();
    });
  });
}

/** 关闭标准型号编辑对话框。 */
function cancel() {
  dialogOpen.value = false;
}

/** 对话框关闭后清理表单和品牌选项，避免新增型号继承上一次编辑数据。 */
function resetDialogForm() {
  form.value = emptyForm();
  brandOptions.value = [];
  proxy.resetForm("modelFormRef");
}

/** 创建标准型号录入表单的稳定初始值，避免新增操作复用上一次编辑数据。 */
function emptyForm() {
  return {
    modelId: undefined,
    classId: undefined,
    brandId: undefined,
    modelName: "",
    alias: "",
    memorySpec: "",
    officialFlag: YES_NO_CODE.YES,
    status: NORMAL_DISABLE_CODE.NORMAL,
    remark: "",
  };
}

/**
 * 将产品分类树转换为带完整层级路径的下拉选项，避免同名子分类无法区分。
 *
 * @param {Array<object>} nodes 当前层级的产品分类节点
 * @param {Array<string>} parentNames 当前节点之前的分类名称路径
 * @returns {Array<object>} 带 classLabel 的产品分类树
 */
function buildClassOptions(nodes, parentNames, secondLevelIds) {
  return nodes.map((node) => {
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

loadClassOptions();
getList();
</script>
