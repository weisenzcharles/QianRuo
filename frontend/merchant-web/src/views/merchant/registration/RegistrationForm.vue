<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    :disabled="readonly"
    class="registration-details-form">
    <section class="form-section">
      <div class="section-head">
        <span class="section-number">02</span>
        <h2>店铺与联系人</h2>
        <span class="section-hint">基本资料</span>
      </div>
      <div class="form-grid">
        <el-form-item label="商户类型" prop="memberType" class="span-2">
          <el-segmented
            v-model="form.memberType"
            :options="memberTypeOptions" />
        </el-form-item>
        <el-form-item label="店铺名称" prop="nickname">
          <el-input v-model="form.nickname" maxlength="64" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" maxlength="64" />
        </el-form-item>
        <el-form-item label="手机号" prop="contactPhone">
          <el-input
            v-model="form.contactPhone"
            maxlength="11"
            :disabled="readonly || lockPhone" />
        </el-form-item>
        <el-form-item label="短信验证码" prop="code">
          <div class="code-field">
            <el-input v-model="form.code" maxlength="4" />
            <el-button
              :disabled="readonly || countdown > 0"
              :loading="sendingCode"
              @click="sendCode">
              {{ countdown > 0 ? `${countdown}s` : "获取验证码" }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="客服电话" prop="servicePhone">
          <el-input v-model="form.servicePhone" maxlength="32" />
        </el-form-item>
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="form.contactEmail" maxlength="128" />
        </el-form-item>
      </div>
    </section>

    <section class="form-section">
      <div class="section-head">
        <span class="section-number">03</span>
        <h2>身份认证</h2>
        <span class="section-hint">个人及企业商户均需提供</span>
      </div>
      <div class="form-grid upload-grid">
        <el-form-item
          :label="identityCardFrontLabel"
          prop="legalIdFrontUrl"
          required
          class="upload-field">
          <image-upload
            v-model="form.legalIdFrontUrl"
            action="/file/public/merchant-registration/upload"
            :data="uploadData"
            :limit="1"
            :is-show-tip="false"
            :disabled="readonly || !canUpload"
            :show-disabled-upload="!readonly" />
          <p class="upload-caption">上传清晰的证件照片，确保四角完整</p>
        </el-form-item>
        <el-form-item
          :label="identityCardBackLabel"
          prop="legalIdBackUrl"
          required
          class="upload-field">
          <image-upload
            v-model="form.legalIdBackUrl"
            action="/file/public/merchant-registration/upload"
            :data="uploadData"
            :limit="1"
            :is-show-tip="false"
            :disabled="readonly || !canUpload"
            :show-disabled-upload="!readonly" />
          <p class="upload-caption">上传清晰的证件照片，确保四角完整</p>
        </el-form-item>
      </div>
      <el-alert
        v-if="!readonly && !canUpload"
        title="请先填写手机号并输入四位验证码，之后即可上传店铺及身份资料。"
        type="info"
        show-icon
        :closable="false"
        class="upload-notice" />
    </section>

    <section class="form-section">
      <div class="section-head">
        <span class="section-number">04</span>
        <h2>店铺展示</h2>
        <span class="section-hint">完善店铺资料，选填</span>
      </div>
      <div class="form-grid">
        <el-form-item
          label="店铺 Logo"
          prop="storeLogoUrl"
          class="upload-field span-2">
          <image-upload
            v-model="form.storeLogoUrl"
            action="/file/public/merchant-registration/upload"
            :data="uploadData"
            :limit="1"
            :is-show-tip="false"
            :disabled="readonly || !canUpload"
            :show-disabled-upload="!readonly" />
          <p class="upload-caption">上传后用于店铺展示</p>
        </el-form-item>
        <el-form-item
          label="店铺描述"
          prop="storeDescription"
          class="store-description-field span-2">
          <el-input
            v-model="form.storeDescription"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入店铺描述" />
        </el-form-item>
        <el-form-item label="支付宝实名" prop="alipayRealName">
          <el-input v-model="form.alipayRealName" maxlength="64" />
        </el-form-item>
        <el-form-item label="支付宝账号" prop="alipayAccount">
          <el-input v-model="form.alipayAccount" maxlength="128" />
        </el-form-item>
      </div>
    </section>

    <section
      v-if="form.memberType === RENTAL_CODE.MEMBER_TYPE.ENTERPRISE"
      class="form-section">
      <div class="section-head">
        <span class="section-number">05</span>
        <h2>企业资质</h2>
        <span class="section-hint">企业商户必填信息已标注</span>
      </div>
      <div class="form-grid">
        <el-form-item
          label="营业执照"
          prop="licenseImageUrl"
          :required="isEnterprise"
          class="upload-field span-2">
          <image-upload
            v-model="form.licenseImageUrl"
            action="/file/public/merchant-registration/upload"
            :data="uploadData"
            :limit="1"
            :is-show-tip="false"
            :disabled="readonly || !canUpload"
            :show-disabled-upload="!readonly" />
          <p class="upload-caption">
            请确保主体名称、证件号码和登记地址清晰可辨
          </p>
        </el-form-item>
        <el-form-item
          label="企业名称"
          prop="legalSubjectName"
          :required="isEnterprise">
          <el-input v-model="form.legalSubjectName" maxlength="128" />
        </el-form-item>
        <el-form-item
          label="营业执照号"
          prop="licenseNo"
          :required="isEnterprise">
          <el-input v-model="form.licenseNo" maxlength="64" />
        </el-form-item>
        <el-form-item label="注册资本" prop="registeredCapital">
          <el-input-number
            v-model="form.registeredCapital"
            :min="0"
            :precision="6"
            :step="0.0001"
            controls-position="right"
            style="width: 100%">
            <template #suffix>万元</template>
          </el-input-number>
        </el-form-item>
        <el-form-item
          label="法人姓名"
          prop="legalRepresentativeName"
          :required="isEnterprise">
          <el-input v-model="form.legalRepresentativeName" maxlength="64" />
        </el-form-item>
        <el-form-item
          label="法人身份证号"
          prop="legalRepresentativeIdNo"
          :required="isEnterprise">
          <el-input v-model="form.legalRepresentativeIdNo" maxlength="64" />
        </el-form-item>
        <el-form-item
          label="营业执照地址"
          prop="licenseAddress"
          :required="isEnterprise">
          <el-input v-model="form.licenseAddress" maxlength="500" />
        </el-form-item>
        <el-form-item label="执照有效期开始" prop="licenseValidFrom">
          <el-date-picker
            v-model="form.licenseValidFrom"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="执照有效期结束" prop="licenseValidTo">
          <div class="date-field">
            <el-date-picker
              v-model="form.licenseValidTo"
              type="date"
              value-format="YYYY-MM-DD"
              :disabled="form.licenseLongTerm === 1"
              style="width: 100%" />
            <el-checkbox v-model="licenseLongTerm">长期</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item label="经营范围" prop="businessScope" class="span-2">
          <el-input
            v-model="form.businessScope"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit placeholder="请输入企业经营范围"/>
        </el-form-item>
      </div>
    </section>
  </el-form>
</template>

<script setup>
import { requestRegistrationCode } from "@/api/merchant/registration";
import { RENTAL_CODE } from "@/utils/rental/options";

const props = defineProps({
  readonly: { type: Boolean, default: false },
  lockPhone: { type: Boolean, default: false },
});

const form = defineModel({ type: Object, required: true });
const formRef = ref();
const sendingCode = ref(false);
const countdown = ref(0);
let timer;

const memberTypeOptions = [
  { label: "个人商户", value: RENTAL_CODE.MEMBER_TYPE.PERSONAL },
  { label: "企业商户", value: RENTAL_CODE.MEMBER_TYPE.ENTERPRISE },
];
const uploadData = computed(() => ({
  phone: form.value.contactPhone,
  code: form.value.code,
}));
const canUpload = computed(
  () =>
    /^1[3-9]\d{9}$/.test(form.value.contactPhone || "") &&
    /^\d{4}$/.test(form.value.code || ""),
);
const isEnterprise = computed(
  () => form.value.memberType === RENTAL_CODE.MEMBER_TYPE.ENTERPRISE,
);
const identityCardFrontLabel = computed(() =>
  isEnterprise.value ? "法人身份证人像面" : "身份证人像面",
);
const identityCardBackLabel = computed(() =>
  isEnterprise.value ? "法人身份证国徽面" : "身份证国徽面",
);
const licenseLongTerm = computed({
  get: () => form.value.licenseLongTerm === 1,
  set: (value) => {
    form.value.licenseLongTerm = value ? 1 : 0;
    if (value) form.value.licenseValidTo = undefined;
  },
});

/**
 * 创建只在企业商户场景生效的必填校验器，个人商户不会被企业资质字段阻塞。
 *
 * @param {string} message 字段缺失时的提示
 * @returns {Function} Element Plus 表单校验函数
 */
const enterpriseRequired = (message) => (rule, value, callback) => {
  if (
    form.value.memberType === RENTAL_CODE.MEMBER_TYPE.ENTERPRISE &&
    !String(value || "").trim()
  ) {
    callback(new Error(message));
    return;
  }
  callback();
};
const requiredValue = (message) => (rule, value, callback) => {
  if (!String(value || "").trim()) {
    callback(new Error(message));
    return;
  }
  callback();
};
const rules = {
  memberType: [
    { required: true, message: "请选择商户类型", trigger: "change" },
  ],
  nickname: [{ required: true, message: "请输入店铺名称", trigger: "blur" }],
  contactName: [{ required: true, message: "请输入联系人", trigger: "blur" }],
  contactPhone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号",
      trigger: "blur",
    },
  ],
  code: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { pattern: /^\d{4}$/, message: "请输入四位数字验证码", trigger: "blur" },
  ],
  contactEmail: [
    { type: "email", message: "联系邮箱格式不正确", trigger: "blur" },
  ],
  legalSubjectName: [
    { validator: enterpriseRequired("请输入企业名称"), trigger: "blur" },
  ],
  licenseNo: [
    { validator: enterpriseRequired("请输入营业执照号"), trigger: "blur" },
  ],
  licenseAddress: [
    {
      validator: enterpriseRequired("请输入营业执照登记地址"),
      trigger: "blur",
    },
  ],
  legalRepresentativeName: [
    { validator: enterpriseRequired("请输入法人姓名"), trigger: "blur" },
  ],
  legalRepresentativeIdNo: [
    { validator: enterpriseRequired("请输入法人身份证号"), trigger: "blur" },
  ],
  licenseImageUrl: [
    { validator: enterpriseRequired("请上传营业执照"), trigger: "change" },
  ],
  legalIdFrontUrl: [
    { validator: requiredValue("请上传身份证正面"), trigger: "change" },
  ],
  legalIdBackUrl: [
    { validator: requiredValue("请上传身份证反面"), trigger: "change" },
  ],
  licenseValidTo: [{ validator: validateLicensePeriod, trigger: "change" }],
};

/**
 * 校验手机号后请求入驻验证码，并启动六十秒防重复点击倒计时。
 */
async function sendCode() {
  await formRef.value.validateField("contactPhone");
  sendingCode.value = true;
  try {
    await requestRegistrationCode(form.value.contactPhone);
    ElMessage.success("验证码已发送");
    countdown.value = 60;
    timer = window.setInterval(() => {
      countdown.value -= 1;
      if (countdown.value <= 0) window.clearInterval(timer);
    }, 1000);
  } finally {
    sendingCode.value = false;
  }
}

/**
 * 校验营业执照结束日期不得早于开始日期，长期有效执照不检查结束日期。
 *
 * @param {object} rule Element Plus 校验规则
 * @param {string} value 执照有效期结束日期
 * @param {Function} callback 校验完成回调
 */
function validateLicensePeriod(rule, value, callback) {
  if (
    form.value.licenseLongTerm !== 1 &&
    form.value.licenseValidFrom &&
    value &&
    form.value.licenseValidFrom > value
  ) {
    callback(new Error("有效期结束日期不能早于开始日期"));
    return;
  }
  callback();
}

/**
 * 对外暴露完整商户申请表单校验，供提交和重新提交页面调用。
 *
 * @returns {Promise<boolean>} 表单校验结果
 */
function validate() {
  return formRef.value.validate();
}

/**
 * 清理表单字段的校验状态和值，供父页面切换流程时调用。
 *
 * @returns {Promise|undefined} Element Plus 表单重置结果
 */
function resetFields() {
  return formRef.value?.resetFields();
}

/** 清理验证码倒计时，防止组件卸载后继续更新响应式状态。 */
function clearCountdown() {
  window.clearInterval(timer);
}

onBeforeUnmount(clearCountdown);
defineExpose({ validate, resetFields });
</script>

<style scoped>
.registration-details-form {
  margin: 0;
  padding: 0 22px;
}

.form-section {
  padding: 20px 0 21px;
  border-bottom: 1px solid #e2e7ed;
}

.upload-notice {
  margin-top: 18px;
}

.form-section:last-child {
  border-bottom: 0;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-bottom: 15px;
}

.section-number {
  width: 23px;
  height: 23px;
  display: grid;
  flex: 0 0 23px;
  place-items: center;
  border-radius: 5px;
  background: #eaf3fd;
  color: #287fdb;
  font-size: 12px;
  font-weight: 650;
}

.section-head h2 {
  margin: 0;
  color: #252b33;
  font-size: 15px;
  font-weight: 620;
}

.section-hint {
  margin-left: auto;
  color: #707b89;
  font-size: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 16px;
}

.form-grid > * {
  min-width: 0;
}

.registration-details-form :deep(.el-form-item) {
  min-width: 0;
  margin-bottom: 0;
}

.registration-details-form :deep(.el-form-item__label) {
  margin-bottom: 6px;
  color: #505b68;
  font-size: 12px;
  font-weight: 550;
  word-break: normal;
}

.registration-details-form :deep(.el-form-item__content),
.registration-details-form :deep(.el-input),
.registration-details-form :deep(.el-textarea),
.registration-details-form :deep(.el-input-number),
.registration-details-form :deep(.el-date-editor) {
  box-sizing: border-box;
  min-width: 0;
  width: 100%;
}

.registration-details-form :deep(.el-segmented) {
  width: 100%;
}

.span-2 {
  grid-column: 1 / -1;
}

.code-field,
.date-field {
  display: flex;
  align-items: center;
  min-width: 0;
  width: 100%;
  gap: 10px;
}

.code-field .el-input {
  min-width: 0;
  flex: 1 1 auto;
}

.code-field .el-button {
  flex: 0 0 112px;
}

.date-field .el-checkbox {
  flex: 0 0 auto;
}

.upload-field :deep(.component-upload-image),
.upload-field :deep(.el-upload) {
  width: 100%;
}

.upload-field.span-2 :deep(.component-upload-image) {
  width: calc((100% - 16px) / 2);
  margin-inline: 0;
}

.upload-field.span-2 .upload-caption {
  flex: 0 0 100%;
}

.upload-field :deep(.el-upload-list--picture-card) {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  gap: 8px;
  margin: 0;
}

.upload-field :deep(.el-upload-list--picture-card .el-upload-list__item),
.upload-field :deep(.el-upload--picture-card) {
  box-sizing: border-box;
  width: 100%;
  height: auto;
  aspect-ratio: 16 / 9;
  margin: 0;
  border-radius: 5px;
}

.upload-field :deep(.el-upload--picture-card) {
  display: flex;
  align-items: center;
  justify-content: center;
  border-style: dashed;
  background: #fbfcfd;
  line-height: normal;
}

.upload-field :deep(.el-upload--picture-card.is-disabled) {
  border-color: #d5dce4;
  background: #f7f8fa;
}

.upload-field :deep(.avatar-uploader-icon) {
  color: #287fdb;
  font-size: 22px;
}

.upload-field :deep(.el-upload__tip) {
  display: none;
}

.upload-caption {
  margin: 5px 0 0;
  color: #707b89;
  font-size: 11px;
  line-height: 1.45;
}

@media (max-width: 760px) {
  .registration-details-form {
    padding: 0 15px;
  }

  .form-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .span-2 {
    grid-column: auto;
  }

  .upload-field.span-2 :deep(.component-upload-image) {
    width: 100%;
  }

  .date-field {
    align-items: flex-start;
    flex-direction: column;
  }

  .section-hint {
    font-size: 11px;
  }
}
</style>
