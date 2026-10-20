<template>
  <div class="app-container">
    <div class="page-toolbar">
      <div>
        <h2>常用型号</h2>
        <span class="selection-count">
          已选择 {{ selectedIds.length }} 项，仅展示当前商户经营分类内的标准型号
        </span>
      </div>
    </div>

    <el-alert
      v-if="options.length === 0 && !loading"
      title="暂无可选型号，请先在经营分类中选择业务范围，或等待平台维护标准型号。"
      type="info"
      :closable="false"
      show-icon />
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          icon="Check"
          :loading="saving"
          @click="saveSelections"
          v-hasPermi="['merchant:brandModel:edit']">
          保存
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="openApplyDialog">
          申请品牌型号
        </el-button>
      </el-col>
      <right-toolbar :search="false" @queryTable="loadOptions" />
    </el-row>
    <el-table
      ref="modelTableRef"
      v-loading="loading"
      :data="options"
      row-key="modelId"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" reserve-selection />
      <el-table-column
        label="产品分类"
        prop="className"
        min-width="160"
        show-overflow-tooltip />
      <el-table-column label="品牌" prop="brand" width="150" />
      <el-table-column label="型号" prop="modelName" min-width="180" />
      <el-table-column
        label="内存/规格"
        prop="memorySpec"
        min-width="150"
        show-overflow-tooltip />
      <el-table-column label="选择状态" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.selected" type="success">常用</el-tag>
          <span v-else class="muted">未选择</span>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="applyVisible"
      title="品牌型号申请"
      width="760px"
      append-to-body
      destroy-on-close
      class="brand-model-apply-dialog">
      <el-form
        ref="applyFormRef"
        :model="applyForm"
        :rules="applyRules"
        label-width="96px"
        class="apply-form">
        <el-form-item label="所属分类" prop="classPath">
          <el-cascader
            v-model="applyForm.classPath"
            :options="classOptions"
            :props="classCascaderProps"
            :show-all-levels="true"
            clearable
            filterable
            placeholder="请选择所属分类"
            class="apply-field" />
        </el-form-item>
        <el-form-item label="型号" prop="modelName">
          <el-input
            v-model="applyForm.modelName"
            clearable
            maxlength="128"
            show-word-limit
            placeholder="请输入型号" />
          <div class="apply-field-tip">
            1、仅有中文型号填写中文名称，仅有英文型号填写英文名称，优先填写中文名称；2、纯阿拉伯数字直接填写。
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="applyForm.remark"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请输入备注" />
          <div class="apply-field-tip">请填写所申请品牌型号的说明。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeApplyDialog">取消</el-button>
        <el-button type="primary" @click="submitApply">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MerchantBrandModel">
import {
  listBrandModelOptions,
  listSelectedBrandModelIds,
  saveSelectedBrandModels,
} from "@/api/merchant/brandModel";
import { listProductClassOptions } from "@/api/merchant/productClass";

const { proxy } = getCurrentInstance();
const modelTableRef = ref();
const options = ref([]);
const selectedIds = ref([]);
const loading = ref(false);
const saving = ref(false);
const applyVisible = ref(false);
const applyFormRef = ref();
const classOptions = ref([]);
const classCascaderProps = {
  value: "classId",
  label: "className",
  children: "children",
  checkStrictly: true,
  emitPath: true,
};
const applyForm = reactive({
  classPath: [],
  modelName: "",
  remark: "",
});
const applyRules = {
  classPath: [
    { required: true, message: "所属分类不能为空", trigger: "change" },
  ],
  modelName: [
    { required: true, message: "型号不能为空", trigger: "blur" },
    { max: 128, message: "型号长度不能超过128个字符", trigger: "blur" },
  ],
  remark: [{ max: 500, message: "备注长度不能超过500个字符", trigger: "blur" }],
};

/** 加载当前商户可见标准型号和常用选择状态。 */
function loadOptions() {
  loading.value = true;
  Promise.all([
    listBrandModelOptions(),
    listSelectedBrandModelIds(),
    listProductClassOptions(),
  ])
    .then(([optionResponse, selectedResponse, classResponse]) => {
      options.value = optionResponse.data || [];
      classOptions.value = classResponse.data || [];
      const selected = new Set(
        (selectedResponse.data || []).map((modelId) => String(modelId)),
      );
      selectedIds.value = options.value
        .filter((item) => selected.has(String(item.modelId)))
        .map((item) => item.modelId);
      nextTick(() => {
        options.value.forEach((item) => {
          if (selected.has(String(item.modelId))) {
            modelTableRef.value?.toggleRowSelection(item, true);
          }
        });
      });
    })
    .finally(() => {
      loading.value = false;
    });
}

/** 打开品牌型号申请表单，并清理上一次未提交的内容。 */
function openApplyDialog() {
  applyForm.classPath = [];
  applyForm.modelName = "";
  applyForm.remark = "";
  applyVisible.value = true;
}

/** 关闭申请弹窗并清理表单校验状态。 */
function closeApplyDialog() {
  applyVisible.value = false;
  nextTick(() => applyFormRef.value?.clearValidate());
}

/** 校验申请表单；申请落库接口待平台审核流程接入后再提交。 */
function submitApply() {
  applyFormRef.value?.validate((valid) => {
    if (!valid) return;
    proxy.$modal.msgWarning("品牌型号申请接口尚未接入，请联系平台管理员录入");
  });
}

/** 同步表格勾选状态到待保存的标准型号编号集合。 */
function handleSelectionChange(rows) {
  selectedIds.value = rows.map((row) => row.modelId);
}

/** 保存当前商户常用型号，服务端会再次校验分类和型号归属。 */
function saveSelections() {
  saving.value = true;
  saveSelectedBrandModels(selectedIds.value)
    .then(() => {
      proxy.$modal.msgSuccess("常用品牌型号保存成功");
      loadOptions();
    })
    .finally(() => {
      saving.value = false;
    });
}

loadOptions();
</script>

<style scoped>
.merchant-brand-model-page {
  max-width: 1280px;
}
.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 56px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-light);
}
.page-toolbar h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0;
}
.selection-count {
  display: block;
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.muted {
  color: var(--el-text-color-placeholder);
}

.apply-form {
  padding: 12px 28px 4px 8px;
}

.apply-field {
  width: 100%;
}

.apply-field-tip {
  width: 100%;
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.brand-model-apply-dialog :deep(.el-dialog__body) {
  padding-top: 18px;
  padding-bottom: 12px;
}
</style>
