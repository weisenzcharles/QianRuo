<template>
  <main class="registration-page">
    <header class="registration-header">
      <div class="brand">迁若租赁商家端</div>
      <el-button link type="primary" icon="Back" @click="router.push('/login')">返回登录</el-button>
    </header>

    <div class="registration-shell">
      <div class="registration-title">
        <h1>商户入驻</h1>
        <el-segmented v-model="mode" :options="modeOptions" @change="changeMode" />
      </div>

      <template v-if="mode === 'submit'">
        <registration-form ref="submitFormRef" v-model="submitForm" />
        <div class="page-actions">
          <el-button type="primary" size="large" icon="Check" :loading="submitting" @click="submitApplication">提交审核</el-button>
        </div>
      </template>

      <template v-else>
        <el-form ref="lookupFormRef" :model="lookupForm" :rules="lookupRules" class="lookup-form" inline>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="lookupForm.phone" maxlength="11" style="width: 220px" />
          </el-form-item>
          <el-form-item label="验证码" prop="code">
            <el-input v-model="lookupForm.code" maxlength="4" style="width: 140px" />
          </el-form-item>
          <el-form-item>
            <el-button :disabled="countdown > 0" @click="sendLookupCode">{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</el-button>
            <el-button type="primary" icon="Search" :loading="lookingUp" @click="lookupApplication">查询申请</el-button>
          </el-form-item>
        </el-form>

        <template v-if="application">
          <el-alert :title="statusTitle" :description="statusDescription" :type="statusType" show-icon :closable="false" class="status-alert" />
          <registration-form ref="resubmitFormRef" v-model="application" :readonly="application.authStatus !== 'rejected'" lock-phone />
          <div class="page-actions">
            <el-button v-if="application.authStatus === 'rejected'" type="primary" size="large" icon="RefreshRight" :loading="resubmitting" @click="resubmitApplication">修改后重新提交</el-button>
            <el-button v-if="application.authStatus === 'verified'" type="primary" size="large" icon="Right" @click="router.push('/login')">前往登录</el-button>
          </div>
        </template>
      </template>
    </div>
  </main>
</template>

<script setup>
import { lookupRegistration, requestRegistrationCode, resubmitRegistration, submitRegistration } from '@/api/merchant/registration'
import RegistrationForm from '@/views/merchant/registration/RegistrationForm.vue'

const router = useRouter()
const route = useRoute()
const mode = ref(route.query.mode === 'lookup' ? 'lookup' : 'submit')
const modeOptions = [
  { label: '提交入驻申请', value: 'submit' },
  { label: '查询或修改申请', value: 'lookup' }
]
const submitFormRef = ref()
const resubmitFormRef = ref()
const lookupFormRef = ref()
const submitting = ref(false)
const lookingUp = ref(false)
const resubmitting = ref(false)
const countdown = ref(0)
let timer
const submitForm = ref(defaultForm())
const application = ref(null)
const lookupForm = reactive({ phone: '', code: '' })
const lookupRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{4}$/, message: '请输入四位数字验证码', trigger: 'blur' }
  ]
}

// 根据审核状态生成页面标题、说明和提示类型，避免模板内重复判断。
const statusTitle = computed(() => ({
  pending: '申请正在审核中',
  rejected: '申请审核未通过',
  verified: '申请已审核通过'
}[application.value?.authStatus] || '申请状态') )
const statusDescription = computed(() => application.value?.authStatus === 'rejected'
  ? application.value.auditRemark || '请修改资料后重新提交'
  : application.value?.authStatus === 'verified'
    ? '可使用申请手机号和四位验证码首次登录'
    : `申请编号：${application.value?.memberCode || '-'}`)
const statusType = computed(() => ({ pending: 'warning', rejected: 'error', verified: 'success' }[application.value?.authStatus] || 'info'))

/**
 * 创建不包含任何服务端归属字段的公开商户申请初始值。
 *
 * @returns {object} 商户申请表单默认值
 */
function defaultForm() {
  return {
    code: '',
    memberType: 'personal',
    nickname: '',
    contactName: '',
    contactPhone: '',
    servicePhone: '',
    contactEmail: '',
    alipayRealName: '',
    alipayAccount: '',
    storeLogoUrl: '',
    storeDescription: '',
    legalSubjectName: '',
    licenseNo: '',
    registeredCapital: undefined,
    businessScope: '',
    licenseAddress: '',
    licenseValidFrom: '',
    licenseValidTo: '',
    licenseLongTerm: 0,
    licenseImageUrl: '',
    legalRepresentativeName: '',
    legalRepresentativeIdNo: '',
    legalIdFrontUrl: '',
    legalIdBackUrl: ''
  }
}

/**
 * 校验并提交新的商户申请，成功后直接切换到申请状态查询视图。
 */
async function submitApplication() {
  const valid = await submitFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const response = await submitRegistration(submitForm.value)
    ElMessage.success('入驻申请已提交')
    lookupForm.phone = submitForm.value.contactPhone
    lookupForm.code = submitForm.value.code
    application.value = { ...defaultForm(), ...response.data, code: submitForm.value.code }
    mode.value = 'lookup'
  } finally {
    submitting.value = false
  }
}

/**
 * 校验查询手机号、请求验证码并启动防重复请求倒计时。
 */
async function sendLookupCode() {
  await lookupFormRef.value.validateField('phone')
  await requestRegistrationCode(lookupForm.phone)
  ElMessage.success('验证码已发送')
  startCountdown()
}

/**
 * 使用手机号和验证码查询申请，并恢复为可展示或可重新编辑的表单模型。
 */
async function lookupApplication() {
  const valid = await lookupFormRef.value.validate().catch(() => false)
  if (!valid) return
  lookingUp.value = true
  try {
    const response = await lookupRegistration(lookupForm)
    application.value = { ...defaultForm(), ...response.data, code: lookupForm.code }
  } finally {
    lookingUp.value = false
  }
}

/**
 * 校验驳回后的修改资料并重新提交审核，成功后保留验证码并刷新申请状态。
 */
async function resubmitApplication() {
  const valid = await resubmitFormRef.value.validate().catch(() => false)
  if (!valid) return
  resubmitting.value = true
  try {
    const response = await resubmitRegistration(application.value)
    application.value = { ...defaultForm(), ...response.data, code: application.value.code }
    ElMessage.success('申请已重新提交')
  } finally {
    resubmitting.value = false
  }
}

/**
 * 启动六十秒验证码请求倒计时，并先清理旧定时器避免并行计时。
 */
function startCountdown() {
  countdown.value = 60
  window.clearInterval(timer)
  timer = window.setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) window.clearInterval(timer)
  }, 1000)
}

/**
 * 切换提交和查询模式时清理上一次查询结果，避免不同手机号之间残留资料。
 */
function changeMode() {
  application.value = null
}

/** 清理查询验证码倒计时，防止页面离开后继续更新响应式状态。 */
function clearCountdown() {
  window.clearInterval(timer)
}

onBeforeUnmount(clearCountdown)
</script>

<style scoped>
.registration-page {
  min-height: 100vh;
  min-width: 0;
  background: #f5f7fa;
  color: #1f2937;
}

.registration-header {
  height: 64px;
  padding: 0 max(24px, calc((100vw - 1120px) / 2));
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.brand {
  min-width: 0;
  flex: 0 1 auto;
  font-size: 20px;
  font-weight: 700;
  white-space: nowrap;
}

.registration-shell {
  box-sizing: border-box;
  width: min(1080px, calc(100% - 32px));
  margin: 24px auto;
  padding: 28px 36px 36px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.registration-title {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.registration-title h1 {
  margin: 0;
  font-size: 26px;
  white-space: nowrap;
}

.lookup-form {
  padding: 18px 0 6px;
  border-bottom: 1px solid #e5e7eb;
}

.status-alert {
  margin: 24px 0;
}

.page-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 24px;
}

@media (max-width: 760px) {
  .registration-header {
    padding: 0 16px;
    gap: 12px;
  }

  .registration-header .el-button {
    flex: 0 0 auto;
  }

  .registration-shell {
    width: calc(100% - 20px);
    margin: 10px auto;
    padding: 22px 18px 28px;
  }

  .registration-title {
    width: 100%;
    align-items: flex-start;
    flex-direction: column;
  }

  .registration-title h1 {
    width: 100%;
    font-size: 24px;
  }

  .registration-title :deep(.el-segmented) {
    box-sizing: border-box;
    width: 100%;
  }

  .lookup-form :deep(.el-form-item) {
    display: block;
    margin-right: 0;
  }
}
</style>
