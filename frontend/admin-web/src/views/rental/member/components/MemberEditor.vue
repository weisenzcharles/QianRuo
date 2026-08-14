<template>
  <el-form ref="editorFormRef" :model="form" :rules="rules" label-width="120px">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="店铺信息" name="store">
        <div class="form-grid">
          <el-form-item label="商户编码">
            <el-input v-model="form.memberCode" disabled placeholder="保存后由系统自动生成" />
          </el-form-item>
          <el-form-item label="店铺名称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入店铺名称" />
          </el-form-item>
          <el-form-item label="商户类型" prop="memberType">
            <el-select v-model="form.memberType" style="width: 100%">
              <el-option v-for="item in rentalMemberTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="店铺主账号">
            <el-input :model-value="form.mainAccount || '-'" disabled />
          </el-form-item>
          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
          </el-form-item>
          <el-form-item label="联系人电话" prop="contactPhone">
            <el-input v-model="form.contactPhone" placeholder="请输入联系人电话" />
          </el-form-item>
          <el-form-item label="客服电话" prop="servicePhone">
            <el-input v-model="form.servicePhone" placeholder="请输入客服电话" />
          </el-form-item>
          <el-form-item label="联系邮箱" prop="contactEmail">
            <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" />
          </el-form-item>
          <el-form-item label="支付宝实名" prop="alipayRealName">
            <el-input v-model="form.alipayRealName" placeholder="请输入支付宝实名" />
          </el-form-item>
          <el-form-item label="支付宝账号" prop="alipayAccount">
            <el-input v-model="form.alipayAccount" placeholder="请输入支付宝账号" />
          </el-form-item>
          <el-form-item label="会员等级" prop="levelId">
            <el-select v-model="form.levelId" style="width: 100%">
              <el-option v-for="item in levelOptions" :key="item.levelId" :label="item.levelName" :value="item.levelId" :disabled="item.status !== '0'" />
            </el-select>
          </el-form-item>
          <el-form-item label="存储额度MB" prop="storageQuotaMb">
            <el-input-number v-model="form.storageQuotaMb" controls-position="right" :min="0" style="width: 100%" />
          </el-form-item>
          <el-form-item label="对外接单" prop="externalAcceptEnabled">
            <el-switch v-model="form.externalAcceptEnabled" :active-value="1" :inactive-value="0" active-text="允许" inactive-text="禁止" />
          </el-form-item>
          <el-form-item label="店铺Logo" class="form-grid-span">
            <image-upload v-model="form.storeLogoUrl" :limit="1" />
          </el-form-item>
          <el-form-item label="店铺描述" prop="storeDescription" class="form-grid-span">
            <el-input v-model="form.storeDescription" type="textarea" :rows="3" maxlength="1000" show-word-limit />
          </el-form-item>
        </div>
      </el-tab-pane>

      <el-tab-pane label="企业信息" name="enterprise">
        <el-empty v-if="form.memberType !== 'enterprise'" description="个人商户无需填写企业资料" :image-size="72" />
        <div v-else class="form-grid">
          <el-form-item label="企业名称" prop="legalSubjectName">
            <el-input v-model="form.legalSubjectName" placeholder="请输入企业名称" />
          </el-form-item>
          <el-form-item label="营业执照号" prop="licenseNo">
            <el-input v-model="form.licenseNo" placeholder="请输入统一社会信用代码" />
          </el-form-item>
          <el-form-item label="注册资本" prop="registeredCapital">
            <el-input-number v-model="form.registeredCapital" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="法人姓名" prop="legalRepresentativeName">
            <el-input v-model="form.legalRepresentativeName" placeholder="请输入法人姓名" />
          </el-form-item>
          <el-form-item label="法人身份证号" prop="legalRepresentativeIdNo" class="form-grid-span">
            <el-input v-model="form.legalRepresentativeIdNo" placeholder="请输入法人身份证号" />
          </el-form-item>
          <el-form-item label="执照地址" prop="licenseAddress" class="form-grid-span">
            <el-input v-model="form.licenseAddress" placeholder="请输入营业执照地址" />
          </el-form-item>
          <el-form-item label="执照有效期" prop="licenseValidTo" class="form-grid-span">
            <div class="date-row">
              <el-date-picker v-model="form.licenseValidFrom" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
              <el-date-picker v-model="form.licenseValidTo" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" :disabled="form.licenseLongTerm === 1" />
              <el-checkbox v-model="form.licenseLongTerm" :true-value="1" :false-value="0">长期</el-checkbox>
            </div>
          </el-form-item>
          <el-form-item label="经营范围" prop="businessScope" class="form-grid-span">
            <el-input v-model="form.businessScope" type="textarea" :rows="4" maxlength="2000" show-word-limit />
          </el-form-item>
          <el-form-item label="营业执照" prop="licenseImageUrl" class="form-grid-span">
            <image-upload v-model="form.licenseImageUrl" :limit="1" />
          </el-form-item>
          <el-form-item label="法人身份证" prop="legalIdFrontUrl" class="form-grid-span">
            <div class="identity-images">
              <div>
                <span>人像面</span>
                <image-upload v-model="form.legalIdFrontUrl" :limit="1" />
              </div>
              <div>
                <span>国徽面</span>
                <image-upload v-model="form.legalIdBackUrl" :limit="1" />
              </div>
            </div>
          </el-form-item>
        </div>
      </el-tab-pane>

      <el-tab-pane label="签约信息" name="contract">
        <div class="form-grid">
          <el-form-item label="签约日期" prop="signingDate">
            <el-date-picker v-model="form.signingDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="合同有效期" prop="contractValidTo">
            <el-date-picker v-model="contractRange" type="daterange" value-format="YYYY-MM-DD" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%" />
          </el-form-item>
          <el-form-item label="签约合同" class="form-grid-span">
            <file-upload v-model="form.contractFileUrl" :limit="1" :file-size="20" />
          </el-form-item>
        </div>
      </el-tab-pane>

      <el-tab-pane label="审核与状态" name="audit">
        <div class="form-grid">
          <el-form-item label="认证状态" prop="authStatus">
            <el-select v-model="form.authStatus" disabled style="width: 100%">
              <el-option v-for="item in rentalAuthStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核时间">
            <el-date-picker v-model="form.auditTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" disabled style="width: 100%" />
          </el-form-item>
          <el-form-item label="审核备注" prop="auditRemark" class="form-grid-span">
            <el-input v-model="form.auditRemark" type="textarea" :rows="3" disabled />
          </el-form-item>
          <el-form-item label="是否冻结" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio value="0">未冻结</el-radio>
              <el-radio value="1">冻结</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="冻结时间">
            <el-date-picker v-model="form.freezeTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" disabled style="width: 100%" />
          </el-form-item>
          <el-form-item v-if="form.status === '1'" label="冻结原因" prop="freezeReason" class="form-grid-span">
            <el-input v-model="form.freezeReason" type="textarea" :rows="3" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="备注" prop="remark" class="form-grid-span">
            <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
          </el-form-item>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-form>
</template>

<script setup>
import { rentalAuthStatusOptions, rentalMemberTypeOptions } from '@/utils/rental/options'

defineProps({ levelOptions: { type: Array, default: () => [] } })

const form = defineModel({ type: Object, required: true })
const editorFormRef = ref()
const activeTab = ref('store')

const rules = {
  nickname: [{ required: true, message: '店铺名称不能为空', trigger: 'blur' }],
  memberType: [{ required: true, message: '商户类型不能为空', trigger: 'change' }],
  contactPhone: [{ required: true, message: '联系电话不能为空', trigger: 'blur' }],
  contactEmail: [{ type: 'email', message: '联系邮箱格式不正确', trigger: 'blur' }],
  legalSubjectName: [{ validator: verifiedEnterpriseRequired('企业名称不能为空'), trigger: 'blur' }],
  licenseNo: [{ validator: verifiedEnterpriseRequired('营业执照号不能为空'), trigger: 'blur' }],
  licenseImageUrl: [{ validator: verifiedEnterpriseRequired('请上传营业执照'), trigger: 'change' }],
  legalRepresentativeName: [{ validator: verifiedEnterpriseRequired('法人姓名不能为空'), trigger: 'blur' }],
  legalRepresentativeIdNo: [{ validator: verifiedEnterpriseRequired('法人身份证号不能为空'), trigger: 'blur' }],
  legalIdFrontUrl: [{ validator: verifiedEnterpriseIdentityRequired, trigger: 'change' }],
  licenseValidTo: [{ validator: validateLicensePeriod, trigger: 'change' }],
  contractValidTo: [{ validator: validateContractPeriod, trigger: 'change' }],
  authStatus: [{ required: true, message: '认证状态不能为空', trigger: 'change' }],
  levelId: [{ required: true, message: '会员等级不能为空', trigger: 'change' }],
  storageQuotaMb: [{ required: true, message: '存储额度不能为空', trigger: 'blur' }],
  externalAcceptEnabled: [{ required: true, message: '对外接单状态不能为空', trigger: 'change' }],
  status: [{ required: true, message: '冻结状态不能为空', trigger: 'change' }]
}

const contractRange = computed({
  get: () => form.value.contractValidFrom || form.value.contractValidTo
    ? [form.value.contractValidFrom, form.value.contractValidTo]
    : [],
  set: value => {
    form.value.contractValidFrom = value?.[0]
    form.value.contractValidTo = value?.[1]
  }
})

watch(() => form.value.licenseLongTerm, value => {
  if (value === 1) form.value.licenseValidTo = undefined
})

function validate() {
  return editorFormRef.value.validate().catch(fields => {
    activeTab.value = tabForField(Object.keys(fields || {})[0])
    return Promise.reject(fields)
  })
}

function resetFields() {
  activeTab.value = 'store'
  editorFormRef.value?.clearValidate()
}

defineExpose({ validate, resetFields })

function verifiedEnterpriseRequired(message) {
  return (_rule, value, callback) => {
    if (isVerifiedEnterprise() && !value) callback(new Error(message))
    else callback()
  }
}

function verifiedEnterpriseIdentityRequired(_rule, value, callback) {
  if (isVerifiedEnterprise() && (!value || !form.value.legalIdBackUrl)) {
    callback(new Error('请上传法人身份证人像面和国徽面'))
  } else {
    callback()
  }
}

function validateLicensePeriod(_rule, _value, callback) {
  if (form.value.licenseLongTerm !== 1 && isEndBeforeStart(form.value.licenseValidFrom, form.value.licenseValidTo)) {
    callback(new Error('执照结束日期不能早于开始日期'))
  } else {
    callback()
  }
}

function validateContractPeriod(_rule, _value, callback) {
  if (isEndBeforeStart(form.value.contractValidFrom, form.value.contractValidTo)) {
    callback(new Error('合同结束日期不能早于开始日期'))
  } else {
    callback()
  }
}

function isVerifiedEnterprise() {
  return form.value.memberType === 'enterprise' && form.value.authStatus === 'verified'
}

function isEndBeforeStart(start, end) {
  return start && end && new Date(end).getTime() < new Date(start).getTime()
}

function tabForField(field) {
  if (['legalSubjectName', 'licenseNo', 'licenseImageUrl', 'legalRepresentativeName',
    'legalRepresentativeIdNo', 'legalIdFrontUrl', 'licenseValidTo'].includes(field)) return 'enterprise'
  if (['signingDate', 'contractValidTo', 'contractFileUrl'].includes(field)) return 'contract'
  if (['authStatus', 'auditRemark', 'status', 'freezeReason', 'remark'].includes(field)) return 'audit'
  return 'store'
}
</script>

<style scoped lang="scss">
.form-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 0 20px;
  padding: 14px 4px 0;
}

.form-grid-span {
  grid-column: 1 / -1;
}

.date-row,
.identity-images {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  width: 100%;
}

.date-row :deep(.el-date-editor) {
  flex: 1;
  min-width: 0;
}

.identity-images > div {
  min-width: 150px;
}

.identity-images span {
  display: block;
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 760px) {
  .form-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .form-grid-span {
    grid-column: auto;
  }

  .date-row,
  .identity-images {
    flex-direction: column;
  }
}
</style>
