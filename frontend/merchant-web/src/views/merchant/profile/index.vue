<template>
  <div class="app-container merchant-list-page merchant-certification-page">
    <el-card shadow="never">
      <template #header>
        <div class="section-title">商户认证状态</div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="认证状态">
          <el-tag :type="statusType(certification.authStatus)">{{ statusLabel(certification.authStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="会员类型">{{ membershipLabel(certification.membershipType) }}</el-descriptions-item>
        <el-descriptions-item label="功能状态" :span="2">
          <el-alert v-if="certification.authStatus !== 'verified'" title="商户资料审核通过后开放业务功能，当前仅允许查看认证状态和维护账号信息。" type="warning" :closable="false" />
          <el-tag v-else type="success">业务功能已开放</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="section-title">当前接单资格</div>
      </template>
      <el-row :gutter="10" class="mb8">
        <right-toolbar :search="false" @queryTable="loadCertification" />
      </el-row>
      <el-table :data="certification.qualifications || []" size="small">
        <el-table-column label="资格" prop="qualificationCode">
          <template #default="scope">{{ qualificationLabel(scope.row.qualificationCode) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getQualificationStatusTagType(scope.row.qualificationStatus)">
              {{ getQualificationStatusLabel(scope.row.qualificationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核时间" width="190">
          <template #default="scope">{{ formatTime(scope.row.reviewedAt) }}</template>
        </el-table-column>
        <el-table-column label="审核意见" prop="reviewRemark" min-width="220" />
      </el-table>
      <el-empty v-if="!certification.qualifications?.length" description="暂无接单资格" :image-size="60" />
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="section-title">认证材料及历史</div>
      </template>
      <el-timeline v-if="certification.certificationHistory?.length">
        <el-timeline-item v-for="item in certification.certificationHistory" :key="item.certificationId">
          <div class="history-title">第 {{ item.versionNo }} 版 · {{ statusLabel(item.certificationStatus) }}</div>
          <div class="history-meta">提交：{{ formatTime(item.submittedAt) }}　审核：{{ formatTime(item.reviewedAt) }}</div>
          <div v-if="item.reviewRemark" class="history-remark">审核意见：{{ item.reviewRemark }}</div>
          <div class="material-list">
            <a v-for="material in item.materials || []" :key="material.materialType" :href="material.fileUrl" target="_blank" rel="noopener">{{ material.materialName }}</a>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无认证历史" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup>
import { getMerchantCertification } from '@/api/merchant/certification'
import { parseTime } from '@/utils/ruoyi'
import { getQualificationStatusLabel, getQualificationStatusTagType } from '@/utils/rental/options'

const certification = ref({
  authStatus: 'pending',
  membershipType: '',
  qualifications: [],
  certificationHistory: []
})

/** 加载当前登录商户的认证聚合资料。 */
async function loadCertification() {
  const response = await getMerchantCertification()
  certification.value = response.data || certification.value
}

/** 将认证状态转换为商户可理解的中文标签。 */
function statusLabel(value) {
  return { pending: '待审核', rejected: '审核驳回', verified: '审核通过', approved: '审核通过' }[value] || value || '-'
}

/** 根据认证状态返回 Element Plus 标签颜色。 */
function statusType(value) {
  return { pending: 'warning', rejected: 'danger', verified: 'success', approved: 'success' }[value] || 'info'
}

/** 将会员类型编码转换为页面展示名称。 */
function membershipLabel(value) {
  return { personal_member: '个人会员', ordinary_enterprise_member: '普通企业会员', settled_member: '入驻会员' }[value] || value || '-'
}

/** 将接单资格编码转换为页面展示名称。 */
function qualificationLabel(value) {
  return { external_accept: '对外接单', alliance_accept: '租商联盟接单', mini_program_accept: '小程序接单' }[value] || value || '-'
}

/** 统一格式化跨服务返回的毫秒时间戳。 */
function formatTime(value) {
  return value ? parseTime(value) : '-'
}

loadCertification()
</script>

<style scoped>
.merchant-certification-page {
  padding: 20px;
}

.section-card {
  margin-top: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
}

.history-title {
  font-weight: 600;
}

.history-meta,
.history-remark {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
}

.material-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
}
</style>
