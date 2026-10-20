<template>
  <main class="registration-page">
    <header class="registration-header">
      <div class="brand-lockup">
        <span class="brand-mark" aria-hidden="true">迁</span>
        <span class="brand-name">迁若租赁</span>
        <span class="brand-caption">商户服务平台</span>
      </div>
      <div class="header-actions">
        <span>已有账号？</span>
        <el-button
          link
          type="primary"
          icon="Back"
          @click="router.push('/login')">
          返回登录
        </el-button>
      </div>
    </header>

    <div class="registration-shell">
      <div class="registration-title">
        <div>
          <div class="eyebrow">MERCHANT CENTER</div>
          <h1>商户入驻</h1>
          <p>填写经营资料，完成商户身份认证。</p>
        </div>
        <!-- <el-segmented
          v-model="mode"
          :options="modeOptions"
          class="mode-switch"
          @change="changeMode" /> -->
      </div>

      <div class="registration-layout">
        <section class="registration-card">
          <template v-if="mode === 'submit'">
            <div class="card-head">
              <h2>
                {{
                  submitStep === 0
                    ? "填写入驻资料"
                    : submitStep === 1
                      ? "完成注册支付"
                      : "注册完成"
                }}
              </h2>
              <p>
                请使用真实、有效的信息，带
                <span class="required-mark">*</span>
                为必填项
              </p>
              <ol class="process-steps" aria-label="注册流程">
                <li
                  :class="{
                    'is-active': submitStep === 0,
                    'is-done': submitStep > 0,
                  }">
                  <span class="step-index">1</span>
                  <span class="step-label">填写资料</span>
                </li>
                <li
                  :class="{
                    'is-active': submitStep === 1,
                    'is-done': submitStep > 1,
                  }">
                  <span class="step-index">2</span>
                  <span class="step-label">完成支付</span>
                </li>
                <li :class="{ 'is-active': submitStep === 2 }">
                  <span class="step-index">3</span>
                  <span class="step-label">注册完成</span>
                </li>
              </ol>
            </div>

            <template v-if="submitStep === 0">
              <el-form label-position="top" class="registration-method-form">
                <section class="registration-section">
                  <div class="section-head">
                    <span class="section-number">01</span>
                    <h3>注册方式</h3>
                    <span class="section-hint">选择一种申请方式</span>
                  </div>
                  <el-form-item class="method-form-item">
                    <el-radio-group
                      v-model="submitForm.registrationMode"
                      class="registration-methods">
                      <el-radio-button
                        :value="RENTAL_CODE.REGISTRATION_MODE.PAID">
                        <span class="method-copy">
                          <strong>付费注册</strong>
                          <small>提交资料后继续完成注册费用支付</small>
                        </span>
                      </el-radio-button>
                      <el-radio-button
                        :value="RENTAL_CODE.REGISTRATION_MODE.INVITE">
                        <span class="method-copy">
                          <strong>邀请码注册</strong>
                          <small>填写有效邀请码后提交申请</small>
                        </span>
                      </el-radio-button>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item
                    v-if="
                      submitForm.registrationMode ===
                      RENTAL_CODE.REGISTRATION_MODE.INVITE
                    "
                    label="邀请码"
                    required
                    class="invite-form-item"
                    :span="12">
                    <el-input
                      v-model.trim="submitForm.inviteCode"
                      maxlength="32"
                      placeholder="请输入邀请商户提供的邀请码" />
                  </el-form-item>
                </section>
              </el-form>

              <registration-form ref="submitFormRef" v-model="submitForm" />
              <div class="form-footer">
                <p>
                  提交即表示你确认所填资料真实有效。企业商户请按营业执照信息填写资质资料。
                </p>
                <el-button
                  type="primary"
                  size="large"
                  :loading="submitting"
                  @click="submitApplication">
                  {{
                    submitForm.registrationMode ===
                    RENTAL_CODE.REGISTRATION_MODE.INVITE
                      ? "提交注册申请"
                      : "确认资料并继续"
                  }}
                </el-button>
              </div>
            </template>

            <template v-else-if="submitStep === 1">
              <div class="payment-panel">
                <el-icon size="42"><CreditCard /></el-icon>
                <h2>支付注册费用</h2>
                <div class="payment-amount">
                  ¥ {{ Number(application?.paymentAmount || 0).toFixed(2) }}
                </div>
                <p>支付单号：{{ application?.paymentNo }}</p>
                <el-alert
                  title="当前尚未接入第三方支付渠道，点击确认支付将模拟支付成功。"
                  type="warning"
                  :closable="false"
                  show-icon />
                <el-button
                  type="primary"
                  size="large"
                  :loading="paying"
                  @click="confirmPayment">
                  确认支付
                </el-button>
              </div>
            </template>
            <el-result
              v-else
              icon="success"
              title="注册完成"
              sub-title="商户账号已创建，可使用注册手机号登录。">
              <template #extra>
                <el-button
                  type="primary"
                  size="large"
                  @click="router.replace('/login')">
                  前往登录
                </el-button>
              </template>
            </el-result>
          </template>

          <template v-else>
            <div class="card-head">
              <h2>查询申请进度</h2>
              <p>
                输入申请时使用的手机号和验证码，查看审核状态或修改被驳回的资料。
              </p>
            </div>
            <div class="lookup-content">
              <el-form
                ref="lookupFormRef"
                :model="lookupForm"
                :rules="lookupRules"
                class="lookup-form">
                <div class="lookup-fields">
                  <el-form-item label="手机号" prop="phone">
                    <el-input
                      v-model="lookupForm.phone"
                      maxlength="11"
                      placeholder="请输入申请手机号" />
                  </el-form-item>
                  <el-form-item label="验证码" prop="code">
                    <el-input
                      v-model="lookupForm.code"
                      maxlength="4"
                      placeholder="四位验证码" />
                  </el-form-item>
                  <div class="lookup-actions">
                    <el-button
                      :disabled="countdown > 0"
                      @click="sendLookupCode">
                      {{ countdown > 0 ? `${countdown}s` : "获取验证码" }}
                    </el-button>
                    <el-button
                      type="primary"
                      icon="Search"
                      :loading="lookingUp"
                      @click="lookupApplication">
                      查询申请
                    </el-button>
                  </div>
                </div>
                <p class="field-note">验证码将发送至申请手机号。</p>
              </el-form>

              <div v-if="!application" class="query-empty">
                <div>
                  <strong>申请状态将在查询后显示</strong>
                  <p>审核中可查看资料；审核驳回后可修改并重新提交。</p>
                </div>
                <el-tag type="warning" effect="plain">等待查询</el-tag>
              </div>

              <template v-if="application">
                <el-alert
                  :title="statusTitle"
                  :description="statusDescription"
                  :type="statusType"
                  show-icon
                  :closable="false"
                  class="status-alert" />
                <registration-form
                  ref="resubmitFormRef"
                  v-model="application"
                  :readonly="
                    application.authStatus !== RENTAL_CODE.AUTH_STATUS.REJECTED
                  "
                  lock-phone />
                <div class="form-footer lookup-footer">
                  <p>请核对申请资料；审核未通过时可修改资料后重新提交。</p>
                  <div class="footer-actions">
                    <el-button
                      v-if="
                        application.authStatus ===
                        RENTAL_CODE.AUTH_STATUS.REJECTED
                      "
                      type="primary"
                      icon="RefreshRight"
                      :loading="resubmitting"
                      @click="resubmitApplication">
                      修改后重新提交
                    </el-button>
                    <el-button
                      v-if="
                        [
                          RENTAL_CODE.AUTH_STATUS.PENDING,
                          RENTAL_CODE.AUTH_STATUS.REJECTED,
                          RENTAL_CODE.AUTH_STATUS.VERIFIED,
                        ].includes(application.authStatus)
                      "
                      type="primary"
                      icon="Right"
                      @click="router.push('/login')">
                      前往登录
                    </el-button>
                  </div>
                </div>
              </template>
            </div>
          </template>
        </section>

        <aside class="guide-column" aria-label="申请指引">
          <section class="guide-block">
            <h2>注册流程</h2>
            <ol class="guide-steps">
              <li
                :class="{
                  'is-current': mode === 'submit' && submitStep === 0,
                  'is-complete': mode === 'submit' && submitStep > 0,
                }">
                <span class="guide-index">1</span>
                <div>
                  <strong>填写并提交资料</strong>
                  <small>按商户类型补全对应信息</small>
                </div>
              </li>
              <li
                :class="{
                  'is-current': mode === 'submit' && submitStep === 1,
                  'is-complete': mode === 'submit' && submitStep > 1,
                }">
                <span class="guide-index">2</span>
                <div>
                  <strong>完成注册流程</strong>
                  <small>根据付费或邀请码方式继续</small>
                </div>
              </li>
              <li
                :class="{
                  'is-current':
                    mode === 'lookup' ||
                    (mode === 'submit' && submitStep === 2),
                }">
                <span class="guide-index">3</span>
                <div>
                  <strong>查询审核进度</strong>
                  <small>审核驳回可修改后再次提交</small>
                </div>
              </li>
            </ol>
          </section>
          <section class="guide-block">
            <h2>资料准备</h2>
            <ul class="requirements">
              <li>
                <span class="requirement-mark">✓</span>
                <span>联系人手机号可接收验证码</span>
              </li>
              <li>
                <span class="requirement-mark">✓</span>
                <span>准备身份证正反面清晰照片</span>
              </li>
              <li
                v-if="
                  (mode === 'submit' &&
                    submitForm.memberType ===
                      RENTAL_CODE.MEMBER_TYPE.ENTERPRISE) ||
                  (mode === 'lookup' &&
                    application?.memberType ===
                      RENTAL_CODE.MEMBER_TYPE.ENTERPRISE)
                ">
                <span class="requirement-mark">✓</span>
                <span>企业商户另需营业执照及登记信息</span>
              </li>
              <li>
                <span class="requirement-mark">✓</span>
                <span>请确认资料真实、完整且可辨认</span>
              </li>
            </ul>
          </section>
          <section class="guide-block">
            <div class="notice-strip">
              <strong>上传入口说明</strong>
              图片上传控件始终显示。填写有效手机号并输入四位验证码后即可选择图片。
            </div>
          </section>
        </aside>
      </div>
    </div>
  </main>
</template>

<script setup>
import {
  confirmRegistrationPayment,
  lookupRegistration,
  requestRegistrationCode,
  resubmitRegistration,
  submitRegistration,
} from "@/api/merchant/registration";
import RegistrationForm from "@/views/merchant/registration/RegistrationForm.vue";
import { RENTAL_CODE } from "@/utils/rental/options";

const router = useRouter();
const route = useRoute();
const mode = ref(route.query.mode === "lookup" ? "lookup" : "submit");
const modeOptions = [
  { label: "新申请", value: "submit" },
  { label: "查询申请", value: "lookup" },
];
const submitFormRef = ref();
const resubmitFormRef = ref();
const lookupFormRef = ref();
const submitting = ref(false);
const paying = ref(false);
const submitStep = ref(0);
const lookingUp = ref(false);
const resubmitting = ref(false);
const countdown = ref(0);
let timer;
const submitForm = ref(defaultForm());
const application = ref(null);
const lookupForm = reactive({ phone: "", code: "" });
const lookupRules = {
  phone: [
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
};

// 根据审核状态生成页面标题、说明和提示类型，避免模板内重复判断。
const statusTitle = computed(
  () =>
    ({
      [RENTAL_CODE.AUTH_STATUS.PENDING]: "申请正在审核中",
      [RENTAL_CODE.AUTH_STATUS.REJECTED]: "申请审核未通过",
      [RENTAL_CODE.AUTH_STATUS.VERIFIED]: "申请已审核通过",
    })[application.value?.authStatus] || "申请状态",
);
const statusDescription = computed(() =>
  application.value?.authStatus === RENTAL_CODE.AUTH_STATUS.REJECTED
    ? application.value.auditRemark || "请修改资料后重新提交"
    : "可使用申请手机号和四位验证码登录；平台审核通过前，商户业务功能暂不开放。",
);
const statusType = computed(
  () =>
    ({
      [RENTAL_CODE.AUTH_STATUS.PENDING]: "warning",
      [RENTAL_CODE.AUTH_STATUS.REJECTED]: "error",
      [RENTAL_CODE.AUTH_STATUS.VERIFIED]: "success",
    })[application.value?.authStatus] || "info",
);

/**
 * 创建不包含任何服务端归属字段的公开商户申请初始值。
 *
 * @returns {object} 商户申请表单默认值
 */
function defaultForm() {
  return {
    registrationMode: RENTAL_CODE.REGISTRATION_MODE.PAID,
    inviteCode: "",
    code: "",
    memberType: RENTAL_CODE.MEMBER_TYPE.PERSONAL,
    nickname: "",
    contactName: "",
    contactPhone: "",
    servicePhone: "",
    contactEmail: "",
    alipayRealName: "",
    alipayAccount: "",
    storeLogoUrl: "",
    storeDescription: "",
    legalSubjectName: "",
    licenseNo: "",
    registeredCapital: undefined,
    businessScope: "",
    licenseAddress: "",
    licenseValidFrom: "",
    licenseValidTo: "",
    licenseLongTerm: 0,
    licenseImageUrl: "",
    legalRepresentativeName: "",
    legalRepresentativeIdNo: "",
    legalIdFrontUrl: "",
    legalIdBackUrl: "",
  };
}

/** 将表单中的万元转换为接口使用的元。 */
function registeredCapitalToYuan(value) {
  if (value === null || value === undefined || value === "") return undefined;
  const amount = Number(value);
  return Number.isFinite(amount)
    ? Number((amount * 10000).toFixed(2))
    : undefined;
}

/** 将接口返回的元转换为表单展示的万元。 */
function registeredCapitalToWan(value) {
  if (value === null || value === undefined || value === "") return undefined;
  const amount = Number(value);
  return Number.isFinite(amount)
    ? Number((amount / 10000).toFixed(6))
    : undefined;
}

/** 将接口申请数据转换为公开表单模型。 */
function toRegistrationForm(data = {}, code = "") {
  return {
    ...defaultForm(),
    ...data,
    registeredCapital: registeredCapitalToWan(data.registeredCapital),
    code,
  };
}

/** 保持现有接口与数据库以元为单位的契约。 */
function toRegistrationPayload(data) {
  return {
    ...data,
    registeredCapital: registeredCapitalToYuan(data.registeredCapital),
  };
}

function getRegistrationData(response) {
  const data = response?.data;
  if (!data || typeof data !== "object") {
    throw new Error("注册申请返回数据为空，请稍后重试");
  }
  return data;
}

/**
 * 校验并提交新的商户申请，成功后直接切换到申请状态查询视图。
 */
async function submitApplication() {
  const valid = await submitFormRef.value.validate().catch(() => false);
  if (!valid) return;
  if (
    submitForm.value.registrationMode ===
      RENTAL_CODE.REGISTRATION_MODE.INVITE &&
    !submitForm.value.inviteCode
  ) {
    ElMessage.warning("请输入邀请码");
    return;
  }
  submitting.value = true;
  try {
    const response = await submitRegistration(
      toRegistrationPayload(submitForm.value),
    );
    const registration = getRegistrationData(response);
    const registrationStatus = Number(registration.registrationStatus);
    if (
      ![
        RENTAL_CODE.REGISTRATION_STATUS.AWAITING_PAYMENT,
        RENTAL_CODE.REGISTRATION_STATUS.COMPLETED,
      ].includes(registrationStatus)
    ) {
      throw new Error("注册申请状态异常，请重新查询申请进度");
    }
    ElMessage.success("注册资料已提交");
    lookupForm.phone = submitForm.value.contactPhone;
    lookupForm.code = submitForm.value.code;
    application.value = toRegistrationForm(
      registration,
      submitForm.value.code,
    );
    submitStep.value =
      registrationStatus === RENTAL_CODE.REGISTRATION_STATUS.AWAITING_PAYMENT
        ? 1
        : 2;
  } finally {
    submitting.value = false;
  }
}

/** 确认注册支付单并进入注册完成页面。 */
async function confirmPayment() {
  paying.value = true;
  try {
    const response = await confirmRegistrationPayment(
      application.value.paymentNo,
    );
    const paymentData = getRegistrationData(response);
    application.value = {
      ...application.value,
      ...paymentData,
      registeredCapital:
        paymentData.registeredCapital == null
          ? application.value.registeredCapital
          : registeredCapitalToWan(paymentData.registeredCapital),
      code: application.value.code,
    };
    submitStep.value = 2;
    ElMessage.success("支付成功，注册已完成");
  } finally {
    paying.value = false;
  }
}

/**
 * 校验查询手机号、请求验证码并启动防重复请求倒计时。
 */
async function sendLookupCode() {
  await lookupFormRef.value.validateField("phone");
  await requestRegistrationCode(lookupForm.phone);
  ElMessage.success("验证码已发送");
  startCountdown();
}

/**
 * 使用手机号和验证码查询申请，并恢复为可展示或可重新编辑的表单模型。
 */
async function lookupApplication() {
  const valid = await lookupFormRef.value.validate().catch(() => false);
  if (!valid) return;
  lookingUp.value = true;
  try {
    const response = await lookupRegistration(lookupForm);
    application.value = toRegistrationForm(
      getRegistrationData(response),
      lookupForm.code,
    );
  } finally {
    lookingUp.value = false;
  }
}

/**
 * 校验驳回后的修改资料并重新提交审核，成功后保留验证码并刷新申请状态。
 */
async function resubmitApplication() {
  const valid = await resubmitFormRef.value.validate().catch(() => false);
  if (!valid) return;
  resubmitting.value = true;
  try {
    const response = await resubmitRegistration(
      toRegistrationPayload(application.value),
    );
    application.value = toRegistrationForm(
      getRegistrationData(response),
      application.value.code,
    );
    ElMessage.success("申请已重新提交");
  } finally {
    resubmitting.value = false;
  }
}

/**
 * 启动六十秒验证码请求倒计时，并先清理旧定时器避免并行计时。
 */
function startCountdown() {
  countdown.value = 60;
  window.clearInterval(timer);
  timer = window.setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) window.clearInterval(timer);
  }, 1000);
}

/**
 * 切换提交和查询模式时清理上一次查询结果，避免不同手机号之间残留资料。
 */
function changeMode() {
  application.value = null;
}

/** 清理查询验证码倒计时，防止页面离开后继续更新响应式状态。 */
function clearCountdown() {
  window.clearInterval(timer);
}

onBeforeUnmount(clearCountdown);
</script>

<style scoped>
.registration-page {
  --registration-ink: #252b33;
  --registration-muted: #707b89;
  --registration-line: #e2e7ed;
  --registration-line-strong: #cbd3dd;
  --registration-blue: #287fdb;
  --registration-blue-soft: #eaf3fd;
  min-height: 100vh;
  min-width: 0;
  background: #f3f5f7;
  color: var(--registration-ink);
}

.registration-header {
  height: 66px;
  padding: 0 max(20px, calc((100vw - 1180px) / 2));
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  background: #ffffff;
  border-bottom: 1px solid var(--registration-line);
}

.brand-lockup,
.header-actions {
  display: flex;
  align-items: center;
}

.brand-lockup {
  gap: 10px;
  min-width: 0;
}

.brand-mark {
  width: 34px;
  height: 34px;
  display: grid;
  flex: 0 0 34px;
  place-items: center;
  border: 1px solid #b6d5f3;
  border-radius: 7px;
  background: var(--registration-blue-soft);
  color: var(--registration-blue);
  font-size: 16px;
  font-weight: 700;
}

.brand-name {
  font-size: 16px;
  font-weight: 650;
  white-space: nowrap;
}

.brand-caption {
  margin-left: 2px;
  padding-left: 12px;
  border-left: 1px solid var(--registration-line);
  color: var(--registration-muted);
  font-size: 12px;
  white-space: nowrap;
}

.header-actions {
  gap: 7px;
  color: var(--registration-muted);
  font-size: 13px;
  white-space: nowrap;
}

.registration-shell {
  box-sizing: border-box;
  width: min(1180px, calc(100% - 40px));
  margin: 0 auto;
  padding: 28px 0 42px;
}

.registration-title {
  display: flex;
  min-width: 0;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 20px;
}

.eyebrow {
  margin-bottom: 5px;
  color: var(--registration-blue);
  font-size: 11px;
  font-weight: 650;
  letter-spacing: 1px;
}

.registration-title h1 {
  margin: 0;
  font-size: 25px;
  font-weight: 650;
  line-height: 1.25;
  white-space: nowrap;
}

.registration-title p {
  margin: 6px 0 0;
  color: var(--registration-muted);
  font-size: 13px;
}

.mode-switch {
  flex: 0 0 auto;
}

.mode-switch :deep(.el-segmented) {
  min-height: 40px;
  padding: 3px;
  border: 1px solid var(--registration-line);
  border-radius: 7px;
  background: #ffffff;
}

.mode-switch :deep(.el-segmented__item) {
  min-height: 32px;
  padding: 0 10px;
  color: var(--registration-muted);
}

.mode-switch :deep(.el-segmented__item.is-selected) {
  color: var(--registration-blue);
  font-weight: 600;
}

.registration-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 20px;
  align-items: start;
}

.registration-card {
  min-width: 0;
  border: 1px solid var(--registration-line);
  border-radius: 6px;
  background: #ffffff;
}

.card-head {
  padding: 19px 22px 16px;
  border-bottom: 1px solid var(--registration-line);
}

.card-head h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 620;
}

.card-head > p {
  margin: 4px 0 0;
  color: var(--registration-muted);
  font-size: 13px;
}

.required-mark {
  color: #d95454;
}

.process-steps {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin: 19px 0 0;
  padding: 0;
  list-style: none;
}

.process-steps li {
  position: relative;
  z-index: 0;
  display: flex;
  align-items: center;
  gap: 9px;
  min-width: 0;
  color: #9ba5b1;
  font-size: 12px;
}

.process-steps li:not(:last-child)::after {
  flex: 1 1 auto;
  height: 1px;
  margin-right: 12px;
  background: var(--registration-line);
  content: "";
}

.step-index {
  width: 26px;
  height: 26px;
  display: grid;
  flex: 0 0 26px;
  place-items: center;
  border: 1px solid var(--registration-line-strong);
  border-radius: 50%;
  background: #ffffff;
  font-size: 12px;
}

.step-label {
  position: relative;
  z-index: 1;
  padding-right: 8px;
  background: #ffffff;
  line-height: 1.2;
  white-space: nowrap;
}

.process-steps li.is-active {
  color: var(--registration-blue);
  font-weight: 600;
}

.process-steps li.is-active .step-index {
  border-color: var(--registration-blue);
  background: var(--registration-blue);
  color: #ffffff;
}

.process-steps li.is-done {
  color: #32865d;
}

.process-steps li.is-done .step-index {
  border-color: #a9d0b9;
  background: #edf7f1;
  color: #32865d;
}

.registration-method-form {
  margin: 0;
  padding: 0 22px;
}

.registration-section {
  --registration-method-gap: 10px;
  padding: 20px 0 21px;
  border-bottom: 1px solid var(--registration-line);
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
  background: var(--registration-blue-soft);
  color: var(--registration-blue);
  font-size: 12px;
  font-weight: 650;
}

.section-head h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 620;
}

.section-hint {
  margin-left: auto;
  color: var(--registration-muted);
  font-size: 12px;
}

.method-form-item {
  margin-bottom: 0;
}

.registration-methods {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--registration-method-gap);
  width: 100%;
}

.registration-methods :deep(.el-radio-button) {
  width: 100%;
}

.registration-methods :deep(.el-radio-button__inner) {
  width: 100%;
  min-height: 68px;
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid var(--registration-line-strong);
  border-left: 1px solid var(--registration-line-strong);
  border-radius: 5px;
  background: #ffffff;
  box-shadow: none;
  color: var(--registration-ink);
  text-align: left;
  white-space: normal;
}

.registration-methods
  :deep(.el-radio-button:first-child .el-radio-button__inner),
.registration-methods
  :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 5px;
}

.registration-methods
  :deep(.el-radio-button.is-active .el-radio-button__inner) {
  border-color: var(--registration-blue);
  background: #f5faff;
  box-shadow: inset 0 0 0 1px var(--registration-blue);
  color: var(--registration-ink);
}

.method-copy {
  display: block;
}

.method-copy strong,
.method-copy small {
  display: block;
}

.method-copy strong {
  font-size: 13px;
  font-weight: 600;
}

.method-copy small {
  margin-top: 3px;
  color: var(--registration-muted);
  font-size: 11px;
  line-height: 1.4;
}

.invite-form-item {
  width: calc((100% - var(--registration-method-gap)) / 2);
  margin: 13px 0 0;
}

.payment-panel {
  max-width: 560px;
  margin: 52px auto;
  padding: 0 22px;
  text-align: center;
}

.payment-panel h2 {
  margin: 10px 0 0;
  font-size: 20px;
}

.payment-panel .payment-amount {
  margin: 16px 0;
  color: var(--registration-blue);
  font-size: 36px;
  font-weight: 700;
}

.payment-panel .el-alert {
  margin: 24px 0;
  text-align: left;
}

.guide-column {
  position: sticky;
  top: 18px;
  display: block;
  margin: 0;
  padding: 0;
  border-radius: 0;
  background: transparent;
  color: inherit;
  font-family: inherit;
  font-size: inherit;
  line-height: normal;
}

.guide-block {
  padding: 16px 0;
  border-bottom: 1px solid var(--registration-line);
}

.guide-block:first-child {
  padding-top: 5px;
}

.guide-block:last-child {
  border-bottom: 0;
}

.guide-block h2 {
  margin: 0 0 11px;
  font-size: 14px;
  font-weight: 620;
}

.guide-steps,
.requirements {
  display: grid;
  gap: 11px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.guide-steps li,
.requirements li {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  min-width: 0;
  color: #4d5967;
  font-size: 12px;
  line-height: 1.45;
}

.guide-index {
  box-sizing: border-box;
  width: 20px;
  height: 20px;
  display: grid;
  align-self: flex-start;
  flex: 0 0 20px;
  place-items: center;
  border: 1px solid var(--registration-line-strong);
  border-radius: 50%;
  color: var(--registration-muted);
  font-size: 10px;
  line-height: 1;
  text-align: center;
}

.guide-steps li.is-current .guide-index {
  border-color: #b8d5f2;
  background: var(--registration-blue-soft);
  color: var(--registration-blue);
}

.guide-steps li.is-complete .guide-index {
  border-color: #a9d0b9;
  background: #edf7f1;
  color: #32865d;
}

.guide-steps strong {
  display: block;
  color: var(--registration-ink);
  font-size: 12px;
  font-weight: 600;
}

.guide-steps small {
  display: block;
  margin-top: 2px;
  color: var(--registration-muted);
  font-size: 11px;
}

.requirement-mark {
  width: 16px;
  height: 16px;
  display: grid;
  flex: 0 0 16px;
  place-items: center;
  border-radius: 50%;
  background: #edf7f1;
  color: #32865d;
  font-size: 10px;
  font-weight: 700;
}

.notice-strip {
  padding: 11px 12px;
  border-left: 3px solid #91bde9;
  background: #f3f8fd;
  color: #51677d;
  font-size: 11px;
}

.notice-strip strong {
  display: block;
  margin-bottom: 3px;
  color: #365d82;
  font-size: 12px;
}

.lookup-content {
  padding: 22px;
}

.lookup-form {
  padding-bottom: 15px;
  border-bottom: 1px solid var(--registration-line);
}

.lookup-fields {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(120px, 0.65fr) auto;
  gap: 12px;
  align-items: start;
}

.lookup-fields > .el-form-item {
  min-width: 0;
  margin-bottom: 0;
}

.lookup-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 24px;
}

.field-note {
  margin: 6px 0 0;
  color: var(--registration-muted);
  font-size: 11px;
}

.query-empty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 20px;
  padding: 14px;
  border: 1px solid var(--registration-line);
  border-radius: 5px;
  background: #f7f9fb;
}

.query-empty strong {
  display: block;
  font-size: 13px;
}

.query-empty p {
  margin: 3px 0 0;
  color: var(--registration-muted);
  font-size: 11px;
}

.status-alert {
  margin: 18px 0;
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 17px 22px;
  border-top: 1px solid var(--registration-line);
  background: #fbfcfd;
}

.form-footer p {
  max-width: 440px;
  margin: 0;
  color: var(--registration-muted);
  font-size: 11px;
}

.lookup-footer {
  margin: 18px -22px -22px;
}

.footer-actions {
  display: flex;
  flex: 0 0 auto;
  gap: 8px;
}

.registration-method-form :deep(.el-form-item__content),
.lookup-content :deep(.el-form-item__content) {
  min-width: 0;
}

.registration-method-form :deep(.el-input),
.lookup-content :deep(.el-input) {
  width: 100%;
}

.registration-method-form :deep(.el-form-item__label),
.lookup-content :deep(.el-form-item__label) {
  color: #505b68;
  font-size: 12px;
  font-weight: 550;
}

@media (max-width: 760px) {
  .registration-header {
    padding: 0 12px;
    height: 58px;
  }

  .brand-caption,
  .header-actions > span {
    display: none;
  }

  .registration-shell {
    width: calc(100% - 24px);
    padding-top: 19px;
  }

  .registration-title {
    display: block;
    margin-bottom: 14px;
  }

  .registration-title h1 {
    font-size: 22px;
  }

  .mode-switch {
    width: 100%;
    margin-top: 15px;
  }

  .mode-switch :deep(.el-segmented) {
    width: 100%;
  }

  .mode-switch :deep(.el-segmented__item) {
    flex: 1;
  }

  .card-head {
    padding: 16px 15px 14px;
  }

  .registration-method-form {
    padding: 0 15px;
  }

  .registration-section {
    padding: 17px 0;
  }

  .registration-methods {
    grid-template-columns: minmax(0, 1fr);
  }

  .invite-form-item {
    width: 100%;
  }

  .process-steps li {
    gap: 6px;
    font-size: 11px;
  }

  .process-steps li:not(:last-child)::after {
    margin-right: 5px;
  }

  .section-hint {
    font-size: 11px;
  }

  .form-footer {
    align-items: flex-start;
    flex-direction: column;
    padding: 15px;
  }

  .form-footer > .el-button,
  .footer-actions,
  .footer-actions .el-button {
    width: 100%;
  }

  .footer-actions {
    flex-direction: column;
  }

  .payment-panel {
    margin: 36px auto;
    padding: 0 15px;
  }

  .lookup-content {
    padding: 17px 15px;
  }

  .lookup-fields {
    grid-template-columns: minmax(0, 1fr);
  }

  .lookup-actions {
    padding-top: 0;
  }

  .lookup-actions .el-button {
    flex: 1;
  }

  .lookup-footer {
    margin: 18px -15px -17px;
  }

  .registration-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .guide-column {
    position: static;
    display: grid;
    grid-template-columns: minmax(0, 1fr);
  }

  .guide-block:first-child {
    padding-top: 16px;
  }

  .guide-block:last-child {
    grid-column: auto;
  }

  .query-empty {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
