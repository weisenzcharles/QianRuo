<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="560px"
    append-to-body
    destroy-on-close
    @update:model-value="handleVisibleChange"
    @closed="resetForm"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="订单编号">
        <span>{{ orderNo || "-" }}</span>
      </el-form-item>
      <el-form-item label="快递公司" prop="carrier">
        <el-select
          v-model="form.carrier"
          filterable
          clearable
          placeholder="请选择快递公司"
          class="full-width"
          @change="validateTracking"
        >
          <el-option
            v-for="item in expressCompanies"
            :key="item.code"
            :label="item.name"
            :value="item.code"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="快递单号" prop="trackingNo">
        <el-input
          v-model="form.trackingNo"
          clearable
          placeholder="请输入快递单号"
          @blur="validateTracking"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleVisibleChange(false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit"
        >确认</el-button
      >
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from "vue";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  orderNo: { type: String, default: "" },
  direction: {
    type: String,
    default: "ship",
    validator: (value) => ["ship", "return"].includes(value),
  },
  carrier: { type: String, default: "" },
  trackingNo: { type: String, default: "" },
  expressCompanies: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
});

const emit = defineEmits(["update:modelValue", "submit"]);
const formRef = ref(null);
const form = reactive({ carrier: "", trackingNo: "" });
const dialogTitle = computed(() =>
  props.direction === "return" ? "编辑归还物流" : "编辑寄出物流",
);

function validateTrackingRule(_rule, _value, callback) {
  const carrier = String(form.carrier || "").trim();
  const trackingNo = String(form.trackingNo || "").trim();
  if (!carrier) {
    callback(new Error("请选择快递公司"));
    return;
  }
  if (!trackingNo) {
    callback(new Error("请输入快递单号"));
    return;
  }
  const company = props.expressCompanies.find(
    (item) => String(item.code) === carrier,
  );
  if (company?.pattern) {
    try {
      if (!new RegExp(company.pattern).test(trackingNo)) {
        callback(new Error(`${company.name}快递单号格式不正确`));
        return;
      }
    } catch (_error) {
      callback(new Error("快递单号校验规则配置错误，请联系平台管理员"));
      return;
    }
  }
  callback();
}

const rules = {
  carrier: [{ required: true, message: "请选择快递公司", trigger: "change" }],
  trackingNo: [{ validator: validateTrackingRule, trigger: ["blur", "change"] }],
};

watch(
  () => [props.modelValue, props.carrier, props.trackingNo],
  ([visible]) => {
    if (!visible) return;
    Object.assign(form, {
      carrier: props.carrier || "",
      trackingNo: props.trackingNo || "",
    });
    formRef.value?.clearValidate();
  },
  { immediate: true },
);

function validateTracking() {
  formRef.value?.validateField(["carrier", "trackingNo"]);
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  emit("submit", {
    carrier: String(form.carrier || "").trim(),
    trackingNo: String(form.trackingNo || "").trim(),
  });
}

function handleVisibleChange(value) {
  emit("update:modelValue", value);
}

function resetForm() {
  formRef.value?.resetFields();
}
</script>

<style scoped>
.full-width {
  width: 100%;
}
</style>
