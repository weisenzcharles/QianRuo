<template>
  <div class="app-container">
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item label="品牌名称" prop="brandName">
        <el-input
          v-model="queryParams.brandName"
          placeholder="请输入品牌名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="品牌状态"
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
          v-hasPermi="['platform:brand:add']"
          >新增品牌</el-button
        >
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="brandList">
      <el-table-column
        label="品牌编号"
        prop="brandId"
        width="90"
        align="center"
      />
      <el-table-column
        label="品牌名称"
        prop="brandName"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column
        label="所属分类"
        prop="className"
        min-width="260"
        show-overflow-tooltip
      />
      <el-table-column
        label="型号数量"
        prop="modelCount"
        width="110"
        align="center"
      />
      <el-table-column label="排序" prop="sortNo" width="90" align="center" />
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
      <el-table-column
        label="备注"
        prop="remark"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['platform:brand:edit']"
            >修改</el-button
          >
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['platform:brand:remove']"
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
      width="560px"
      append-to-body
      @closed="resetDialogForm"
    >
      <el-form
        ref="brandFormRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="品牌名称" prop="brandName">
          <el-input
            v-model="form.brandName"
            maxlength="64"
            show-word-limit
            placeholder="请输入品牌名称"
          />
        </el-form-item>
        <el-form-item label="所属分类" prop="classIdList">
          <el-tree
            ref="classTreeRef"
            :data="classOptions"
            node-key="classId"
            show-checkbox
            check-strictly
            default-expand-all
            :props="treeProps"
            class="brand-class-tree"
            @check="handleClassCheck"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入备注"
          />
        </el-form-item>
        <el-row :gutter="20" class="brand-meta-row">
          <el-col :span="12">
            <el-form-item label="排序" prop="sortNo">
              <el-input-number
                v-model="form.sortNo"
                :min="0"
                :max="999999"
                controls-position="right"
                style="width: 100%"
              />
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
                inline-prompt
              />
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

<script setup name="Brand">
import {
  addBrand,
  deleteBrand,
  getBrand,
  listBrands,
  updateBrand,
} from "@/api/rental/brand";
import { listProductClass } from "@/api/rental/productClass";
import { NORMAL_DISABLE_CODE } from "@/utils/stateCode";

const { proxy } = getCurrentInstance();
const brandList = ref([]);
const classOptions = ref([]);
const secondLevelClassIds = ref(new Set());
const total = ref(0);
const loading = ref(false);
const showSearch = ref(true);
const dialogOpen = ref(false);
const dialogTitle = ref("");
const classTreeRef = ref();
const treeProps = {
  value: "classId",
  label: "className",
  children: "children",
  disabled: "disabled",
};
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  brandName: undefined,
  status: undefined,
});
const form = ref(emptyForm());
const rules = {
  brandName: [{ required: true, message: "品牌名称不能为空", trigger: "blur" }],
  classIdList: [
    {
      required: true,
      type: "array",
      min: 1,
      message: "至少选择一个所属二级产品分类",
      trigger: "change",
    },
  ],
  sortNo: [{ required: true, message: "排序不能为空", trigger: "change" }],
  status: [{ required: true, message: "状态不能为空", trigger: "change" }],
};

/** 查询平台品牌列表。 */
function getList() {
  loading.value = true;
  return listBrands(queryParams)
    .then((response) => {
      brandList.value = response.rows || [];
      total.value = response.total || 0;
    })
    .finally(() => {
      loading.value = false;
    });
}

/** 加载正常产品分类，作为品牌经营范围选择树。 */
function loadClassOptions() {
  return listProductClass({ status: NORMAL_DISABLE_CODE.NORMAL }).then(
    (response) => {
      const ids = new Set();
      classOptions.value = buildClassOptions(response.data || [], ids);
      secondLevelClassIds.value = ids;
    },
  );
}

/** 构造分类树并只允许选择二级分类，一级分类仅作为浏览路径。 */
function buildClassOptions(nodes, secondLevelIds) {
  return (nodes || []).map((node) => {
    const classId = Number(node.classId);
    const isSecondLevel = Number(node.classLevel) === 2;
    if (isSecondLevel) secondLevelIds.add(classId);
    return {
      ...node,
      classId,
      disabled: !isSecondLevel,
      children: buildClassOptions(node.children || [], secondLevelIds),
    };
  });
}

/** 重置查询条件并回到第一页。 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.pageNum = 1;
  getList();
}

/** 根据查询条件刷新列表。 */
function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

/** 打开新增品牌表单。 */
function handleAdd() {
  form.value = emptyForm();
  dialogTitle.value = "新增品牌";
  Promise.all([loadClassOptions(), getList()]).then(() => {
    dialogOpen.value = true;
    nextTick(() => classTreeRef.value?.setCheckedKeys([]));
  });
}

/** 加载品牌详情并打开编辑表单。 */
function handleUpdate(row) {
  Promise.all([getBrand(row.brandId), loadClassOptions(), getList()]).then(
    ([response]) => {
      const data = response.data || {};
      form.value = {
        ...emptyForm(),
        ...data,
        classIdList: (data.classIdList || []).map((value) => Number(value)),
        status: data.status,
      };
      dialogTitle.value = "修改品牌";
      dialogOpen.value = true;
      nextTick(() =>
        classTreeRef.value?.setCheckedKeys(form.value.classIdList),
      );
    },
  );
}

/** 删除品牌前由后端校验是否存在型号引用。 */
function handleDelete(row) {
  proxy.$modal
    .confirm(`是否确认删除品牌“${row.brandName}”？`)
    .then(() => deleteBrand(row.brandId))
    .then(() => {
      proxy.$modal.msgSuccess("删除成功");
      getList();
    })
    .catch(() => {});
}

/** 校验表单后新增或修改品牌。 */
function submitForm() {
  proxy.$refs.brandFormRef.validate((valid) => {
    if (!valid) return;
    const classIdList = [
      ...new Set((form.value.classIdList || []).map((value) => Number(value))),
    ];
    if (
      classIdList.length === 0 ||
      classIdList.some((classId) => !secondLevelClassIds.value.has(classId))
    ) {
      proxy.$modal.msgError("所属分类只能选择一个或多个二级产品分类");
      return;
    }
    const payload = {
      brandId: form.value.brandId,
      brandName: form.value.brandName,
      classIdList,
      sortNo: form.value.sortNo,
      status: form.value.status,
      remark: form.value.remark,
    };
    const request = form.value.brandId
      ? updateBrand(payload)
      : addBrand(payload);
    request.then(() => {
      proxy.$modal.msgSuccess(form.value.brandId ? "修改成功" : "新增成功");
      dialogOpen.value = false;
      getList();
    });
  });
}

/** 关闭品牌编辑对话框。 */
function cancel() {
  dialogOpen.value = false;
}

/** 对话框关闭后清理表单和分类树，避免下一次新增继承编辑数据。 */
function resetDialogForm() {
  form.value = emptyForm();
  proxy.resetForm("brandFormRef");
  nextTick(() => classTreeRef.value?.setCheckedKeys([]));
}

/** 创建稳定的品牌表单初始值。 */
function emptyForm() {
  return {
    brandId: undefined,
    brandName: "",
    classIdList: [],
    sortNo: 0,
    status: NORMAL_DISABLE_CODE.NORMAL,
    remark: "",
  };
}

/** 将树控件当前勾选项同步为二级分类编号列表，父级节点永远不进入提交数据。 */
function handleClassCheck(_data, checkState) {
  form.value.classIdList = (checkState.checkedNodes || [])
    .filter((node) => secondLevelClassIds.value.has(Number(node.classId)))
    .map((node) => Number(node.classId));
}

loadClassOptions();
getList();
</script>

<style scoped>
.brand-class-tree {
  width: 100%;
  box-sizing: border-box;
  max-height: 280px;
  overflow-y: auto;
  padding: 8px 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}
</style>
