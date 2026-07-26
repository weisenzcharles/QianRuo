<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="readonly">
    <section class="form-section">
      <h2>店铺资料</h2>
      <div class="form-grid">
        <el-form-item label="商户类型" prop="memberType">
          <el-segmented v-model="form.memberType" :options="memberTypeOptions" />
        </el-form-item>
        <el-form-item label="店铺名称" prop="nickname">
          <el-input v-model="form.nickname" maxlength="64" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" maxlength="64" />
        </el-form-item>
        <el-form-item label="手机号" prop="contactPhone">
          <el-input v-model="form.contactPhone" maxlength="11" :disabled="readonly || lockPhone" />
        </el-form-item>
        <el-form-item label="短信验证码" prop="code">
          <div class="code-field">
            <el-input v-model="form.code" maxlength="4" />
            <el-button :disabled="readonly || countdown > 0" :loading="sendingCode" @click="sendCode">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="客服电话" prop="servicePhone">
          <el-input v-model="form.servicePhone" maxlength="32" />
        </el-form-item>
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="form.contactEmail" maxlength="128" />
        </el-form-item>
        <el-form-item label="支付宝实名" prop="alipayRealName">
          <el-input v-model="form.alipayRealName" maxlength="64" />
        </el-form-item>
        <el-form-item label="支付宝账号" prop="alipayAccount">
          <el-input v-model="form.alipayAccount" maxlength="128" />
        </el-form-item>
        <el-form-item label="店铺Logo" prop="storeLogoUrl">
          <image-upload v-model="form.storeLogoUrl" action="/file/public/merchant-registration/upload" :data="uploadData" :limit="1" :disabled="readonly || !canUpload" />
        </el-form-item>
        <el-form-item label="店铺描述" prop="storeDescription" class="span-2">
          <el-input v-model="form.storeDescription" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
      </div>
    </section>

    <section v-if="form.memberType === 'enterprise'" class="form-section">
      <h2>企业资质</h2>
      <div class="form-grid">
        <el-form-item label="企业名称" prop="legalSubjectName">
          <el-input v-model="form.legalSubjectName" maxlength="128" />
        </el-form-item>
        <el-form-item label="营业执照号" prop="licenseNo">
          <el-input v-model="form.licenseNo" maxlength="64" />
        </el-form-item>
        <el-form-item label="注册资本" prop="registeredCapital">
          <el-input-number v-model="form.registeredCapital" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="法人姓名" prop="legalRepresentativeName">
          <el-input v-model="form.legalRepresentativeName" maxlength="64" />
        </el-form-item>
        <el-form-item label="法人身份证号" prop="legalRepresentativeIdNo">
          <el-input v-model="form.legalRepresentativeIdNo" maxlength="64" />
        </el-form-item>
        <el-form-item label="营业执照地址" prop="licenseAddress">
          <el-input v-model="form.licenseAddress" maxlength="500" />
        </el-form-item>
        <el-form-item label="执照有效期开始" prop="licenseValidFrom">
          <el-date-picker v-model="form.licenseValidFrom" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="执照有效期结束" prop="licenseValidTo">
          <div class="date-field">
            <el-date-picker v-model="form.licenseValidTo" type="date" value-format="YYYY-MM-DD" :disabled="form.licenseLongTerm === 1" style="width: 100%" />
            <el-checkbox v-model="licenseLongTerm">长期</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item label="经营范围" prop="businessScope" class="span-2">
          <el-input v-model="form.businessScope" type="textarea" :rows="3" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="营业执照" prop="licenseImageUrl">
          <image-upload v-model="form.licenseImageUrl" action="/file/public/merchant-registration/upload" :data="uploadData" :limit="1" :disabled="readonly || !canUpload" />
        </el-form-item>
        <el-form-item label="法人身份证人像面" prop="legalIdFrontUrl">
          <image-upload v-model="form.legalIdFrontUrl" action="/file/public/merchant-registration/upload" :data="uploadData" :limit="1" :disabled="readonly || !canUpload" />
        </el-form-item>
        <el-form-item label="法人身份证国徽面" prop="legalIdBackUrl">
          <image-upload v-model="form.legalIdBackUrl" action="/file/public/merchant-registration/upload" :data="uploadData" :limit="1" :disabled="readonly || !canUpload" />
        </el-form-item>
      </div>
    </section>
  </el-form>
</template>

<script setup>
import { requestRegistrationCode } from '@/api/merchant/registration'

const props = defineProps({
  readonly: { type: Boolean, default: false },
  lockPhone: { type: Boolean, default: false }
})

const form = defineModel({ type: Object, required: true })
const formRef = ref()
const sendingCode = ref(false)
const countdown = ref(0)
let timer

const memberTypeOptions = [
  { label: '个人商户', value: 'personal' },
  { label: '企业商户', value: 'enterprise' }
]
const uploadData = computed(() => ({ phone: form.value.contactPhone, code: form.value.code }))
const canUpload = computed(() => /^1[3-9]\d{9}$/.test(form.value.contactPhone || '') && /^\d{4}$/.test(form.value.code || ''))
const licenseLongTerm = computed({
  get: () => form.value.licenseLongTerm === 1,
  set: value => {
    form.value.licenseLongTerm = value ? 1 : 0
    if (value) form.value.licenseValidTo = undefined
  }
})

/**
 * 创建只在企业商户场景生效的必填校验器，个人商户不会被企业资质字段阻塞。
 *
 * @param {string} message 字段缺失时的提示
 * @returns {Function} Element Plus 表单校验函数
 */
const enterpriseRequired = message => (rule, value, callback) => {
  if (form.value.memberType === 'enterprise' && !String(value || '').trim()) {
    callback(new Error(message))
    return
  }
  callback()
}
const rules = {
  memberType: [{ required: true, message: '请选择商户类型', trigger: 'change' }],
  nickname: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{4}$/, message: '请输入四位数字验证码', trigger: 'blur' }
  ],
  contactEmail: [{ type: 'email', message: '联系邮箱格式不正确', trigger: 'blur' }],
  legalSubjectName: [{ validator: enterpriseRequired('请输入企业名称'), trigger: 'blur' }],
  licenseNo: [{ validator: enterpriseRequired('请输入营业执照号'), trigger: 'blur' }],
  legalRepresentativeName: [{ validator: enterpriseRequired('请输入法人姓名'), trigger: 'blur' }],
  legalRepresentativeIdNo: [{ validator: enterpriseRequired('请输入法人身份证号'), trigger: 'blur' }],
  licenseImageUrl: [{ validator: enterpriseRequired('请上传营业执照'), trigger: 'change' }],
  legalIdFrontUrl: [{ validator: enterpriseRequired('请上传法人身份证人像面'), trigger: 'change' }],
  legalIdBackUrl: [{ validator: enterpriseRequired('请上传法人身份证国徽面'), trigger: 'change' }],
  licenseValidTo: [{ validator: validateLicensePeriod, trigger: 'change' }]
}

/**
 * 校验手机号后请求入驻验证码，并启动六十秒防重复点击倒计时。
 * 当前短信未接入，页面仍按正式接口流程组织，方便后续无缝替换。
 */
async function sendCode() {
  await formRef.value.validateField('contactPhone')
  sendingCode.value = true
  try {
    await requestRegistrationCode(form.value.contactPhone)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    timer = window.setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) window.clearInterval(timer)
    }, 1000)
  } finally {
    sendingCode.value = false
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
  if (form.value.licenseLongTerm !== 1 && form.value.licenseValidFrom && value && form.value.licenseValidFrom > value) {
    callback(new Error('有效期结束日期不能早于开始日期'))
    return
  }
  callback()
}

/**
 * 对外暴露完整商户申请表单校验，供提交和重新提交页面调用。
 *
 * @returns {Promise<boolean>} 表单校验结果
 */
function validate() {
  return formRef.value.validate()
}

/**
 * 清理表单字段的校验状态和值，供父页面切换流程时调用。
 *
 * @returns {Promise|undefined} Element Plus 表单重置结果
 */
function resetFields() {
  return formRef.value?.resetFields()
}

/** 清理验证码倒计时，防止组件卸载后继续更新响应式状态。 */
function clearCountdown() {
  window.clearInterval(timer)
}

onBeforeUnmount(clearCountdown)
defineExpose({ validate, resetFields })
</script>

<style scoped>
.form-section {
  padding: 8px 0 24px;
  border-bottom: 1px solid #e5e7eb;
}

.form-section:last-child {
  border-bottom: 0;
}

.form-section h2 {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 24px;
}

.form-grid > * {
  min-width: 0;
}

:deep(.el-form-item__label),
:deep(.el-form-item__content),
:deep(.el-input),
:deep(.el-textarea),
:deep(.el-input-number),
:deep(.el-date-editor) {
  box-sizing: border-box;
  min-width: 0;
  width: 100%;
}

:deep(.el-form-item__label) {
  word-break: normal;
}

.span-2 {
  grid-column: 1 / -1;
}

.code-field,
.date-field {
  display: flex;
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

@media (max-width: 760px) {
  .form-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .span-2 {
    grid-column: auto;
  }

  .date-field {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
