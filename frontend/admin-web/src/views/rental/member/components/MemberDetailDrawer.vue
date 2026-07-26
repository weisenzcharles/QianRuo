<template>
  <el-drawer v-model="visible" title="商户详情" size="min(900px, 94vw)">
    <template v-if="member">
      <section class="detail-section">
        <h3>店铺信息</h3>
        <el-descriptions :column="detailColumns" border>
          <el-descriptions-item label="商户编码">{{ display(member.memberCode) }}</el-descriptions-item>
          <el-descriptions-item label="店铺名称">{{ display(member.nickname) }}</el-descriptions-item>
          <el-descriptions-item label="店铺主账号">{{ display(member.mainAccount) }}</el-descriptions-item>
          <el-descriptions-item label="商户类型">{{ optionLabel(rentalMemberTypeOptions, member.memberType) }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ display(member.contactName) }}</el-descriptions-item>
          <el-descriptions-item label="联系人电话">{{ display(member.contactPhone) }}</el-descriptions-item>
          <el-descriptions-item label="客服电话">{{ display(member.servicePhone) }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{ display(member.contactEmail) }}</el-descriptions-item>
          <el-descriptions-item label="支付宝实名">{{ display(member.alipayRealName) }}</el-descriptions-item>
          <el-descriptions-item label="支付宝账号">{{ display(member.alipayAccount) }}</el-descriptions-item>
          <el-descriptions-item label="会员等级">{{ optionLabel(rentalVipLevelOptions, member.vipLevel) }}</el-descriptions-item>
          <el-descriptions-item label="存储额度">{{ formatQuota(member.storageQuotaMb) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(member.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="对外接单">{{ member.externalAcceptEnabled === 1 ? '允许' : '禁止' }}</el-descriptions-item>
          <el-descriptions-item label="店铺描述" :span="2">{{ display(member.storeDescription) }}</el-descriptions-item>
          <el-descriptions-item v-if="member.storeLogoUrl" label="店铺Logo" :span="2">
            <image-upload :model-value="member.storeLogoUrl" :limit="1" disabled :is-show-tip="false" />
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section v-if="member.memberType === 'enterprise'" class="detail-section">
        <h3>企业信息</h3>
        <el-descriptions :column="detailColumns" border>
          <el-descriptions-item label="企业名称">{{ display(member.legalSubjectName) }}</el-descriptions-item>
          <el-descriptions-item label="营业执照号">{{ display(member.licenseNo) }}</el-descriptions-item>
          <el-descriptions-item label="注册资本">{{ formatCapital(member.registeredCapital) }}</el-descriptions-item>
          <el-descriptions-item label="法人姓名">{{ display(member.legalRepresentativeName) }}</el-descriptions-item>
          <el-descriptions-item label="法人身份证号" :span="2">{{ display(member.legalRepresentativeIdNo) }}</el-descriptions-item>
          <el-descriptions-item label="执照地址" :span="2">{{ display(member.licenseAddress) }}</el-descriptions-item>
          <el-descriptions-item label="执照有效期" :span="2">{{ licensePeriod }}</el-descriptions-item>
          <el-descriptions-item label="经营范围" :span="2">{{ display(member.businessScope) }}</el-descriptions-item>
          <el-descriptions-item v-if="member.licenseImageUrl" label="营业执照" :span="2">
            <image-upload :model-value="member.licenseImageUrl" :limit="1" disabled :is-show-tip="false" />
          </el-descriptions-item>
          <el-descriptions-item v-if="member.legalIdFrontUrl || member.legalIdBackUrl" label="法人身份证" :span="2">
            <div class="identity-images">
              <image-upload v-if="member.legalIdFrontUrl" :model-value="member.legalIdFrontUrl" :limit="1" disabled :is-show-tip="false" />
              <image-upload v-if="member.legalIdBackUrl" :model-value="member.legalIdBackUrl" :limit="1" disabled :is-show-tip="false" />
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section class="detail-section">
        <h3>签约信息</h3>
        <el-descriptions :column="detailColumns" border>
          <el-descriptions-item label="签约日期">{{ formatDate(member.signingDate) }}</el-descriptions-item>
          <el-descriptions-item label="合同有效期">{{ contractPeriod }}</el-descriptions-item>
          <el-descriptions-item v-if="member.contractFileUrl" label="签约合同" :span="2">
            <file-upload :model-value="member.contractFileUrl" :limit="1" disabled :is-show-tip="false" />
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section class="detail-section">
        <h3>审核与状态</h3>
        <el-descriptions :column="detailColumns" border>
          <el-descriptions-item label="认证状态">{{ optionLabel(rentalAuthStatusOptions, member.authStatus) }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ formatDateTime(member.auditTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核备注" :span="2">{{ display(member.auditRemark) }}</el-descriptions-item>
          <el-descriptions-item label="冻结状态">
            <el-tag :type="member.status === '1' ? 'danger' : 'success'">{{ member.status === '1' ? '已冻结' : '未冻结' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="冻结时间">{{ formatDateTime(member.freezeTime) }}</el-descriptions-item>
          <el-descriptions-item label="冻结原因" :span="2">{{ display(member.freezeReason) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ display(member.remark) }}</el-descriptions-item>
        </el-descriptions>
      </section>
    </template>
  </el-drawer>
</template>

<script setup>
import { parseTime } from '@/utils/ruoyi'
import { getRentalOptionLabel, rentalAuthStatusOptions, rentalMemberTypeOptions, rentalVipLevelOptions } from '@/utils/rental/options'
import { useWindowSize } from '@vueuse/core'

const visible = defineModel({ type: Boolean, default: false })
const props = defineProps({ member: { type: Object, default: null } })
const { width } = useWindowSize()
const detailColumns = computed(() => width.value < 760 ? 1 : 2)

const licensePeriod = computed(() => {
  if (props.member?.licenseLongTerm === 1) return `${formatDate(props.member.licenseValidFrom)} - 长期`
  return formatPeriod(props.member?.licenseValidFrom, props.member?.licenseValidTo)
})
const contractPeriod = computed(() => formatPeriod(props.member?.contractValidFrom, props.member?.contractValidTo))

function display(value) {
  return value === undefined || value === null || value === '' ? '-' : value
}

function optionLabel(options, value) {
  return getRentalOptionLabel(options, value) || '-'
}

function formatDate(value) {
  return value ? parseTime(value, '{y}-{m}-{d}') : '-'
}

function formatDateTime(value) {
  return value ? parseTime(value) : '-'
}

function formatPeriod(start, end) {
  if (!start && !end) return '-'
  return `${formatDate(start)} - ${formatDate(end)}`
}

function formatCapital(value) {
  return value === undefined || value === null ? '-' : `¥${Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}`
}

function formatQuota(value) {
  if (value === undefined || value === null) return '-'
  return value >= 1024 ? `${Number(value / 1024).toFixed(1)} GB` : `${value} MB`
}
</script>

<style scoped lang="scss">
.detail-section + .detail-section {
  margin-top: 24px;
}

.detail-section h3 {
  margin: 0 0 12px;
  padding-left: 10px;
  border-left: 4px solid var(--el-color-primary);
  font-size: 16px;
  letter-spacing: 0;
}

.identity-images {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

@media (max-width: 760px) {
  :deep(.el-descriptions__body .el-descriptions__table) {
    table-layout: fixed;
  }
}
</style>
