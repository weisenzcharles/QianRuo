<template>
  <div class="app-container merchant-list-page">
    <section class="invite-summary" v-loading="loading">
      <div>
        <div class="section-label">我的邀请码</div>
        <div class="invite-code">{{ overview.inviteCode || '-' }}</div>
      </div>
      <el-button type="primary" icon="CopyDocument" :disabled="!overview.inviteCode" @click="copyCode">复制邀请码</el-button>
      <el-descriptions :column="3" border label-width="120">
        <el-descriptions-item label="当前等级">{{ overview.levelName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邀请名额">{{ overview.inviteLimit || 0 }}</el-descriptions-item>
        <el-descriptions-item label="剩余名额">{{ overview.remainingCount || 0 }}</el-descriptions-item>
      </el-descriptions>
    </section>

    <h3>已邀请商户</h3>
    <el-row :gutter="10" class="mb8">
      <right-toolbar :search="false" @queryTable="loadOverview" />
    </el-row>
    <el-table :data="overview.invitations" v-loading="loading">
      <el-table-column label="商户编码" prop="inviteeMemberCode" min-width="150" />
      <el-table-column label="店铺名称" prop="inviteeName" min-width="180" />
      <el-table-column label="注册手机号" prop="inviteePhone" width="150" />
      <el-table-column label="状态" prop="inviteStatus" width="120">
        <template #default="scope"><el-tag :type="scope.row.inviteStatus === RENTAL_CODE.INVITATION_STATUS.REGISTERED ? 'success' : 'info'">{{ invitationStatusLabel(scope.row.inviteStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="注册时间" prop="registeredTime" width="180" />
    </el-table>
  </div>
</template>

<script setup name="MerchantInvitation">
import { getInvitationOverview } from '@/api/merchant/invitation'
import { RENTAL_CODE } from '@/utils/rental/options'

const loading = ref(false)
const overview = reactive({ inviteCode: '', levelName: '', inviteLimit: 0, usedCount: 0, remainingCount: 0, invitations: [] })

/** 将邀请状态编码转换为中文展示。 */
function invitationStatusLabel(status) {
  return {
    [RENTAL_CODE.INVITATION_STATUS.REGISTERED]: '已注册',
    [RENTAL_CODE.INVITATION_STATUS.INVALID]: '已失效'
  }[Number(status)] || '-'
}

/** 加载当前商户邀请概览。 */
async function loadOverview() {
  loading.value = true
  try { Object.assign(overview, (await getInvitationOverview()).data || {}) } finally { loading.value = false }
}

/** 将邀请码写入系统剪贴板并反馈结果。 */
async function copyCode() {
  await navigator.clipboard.writeText(overview.inviteCode)
  ElMessage.success('邀请码已复制')
}

onMounted(loadOverview)
</script>

<style scoped>
.invite-summary { display: grid; grid-template-columns: 1fr auto; gap: 20px; align-items: center; margin-bottom: 28px; }
.invite-summary .el-descriptions { grid-column: 1 / -1; }
.section-label { color: #606266; font-size: 14px; }
.invite-code { margin-top: 8px; font-size: 28px; font-weight: 700; letter-spacing: 2px; }
@media (max-width: 760px) { .invite-summary { grid-template-columns: 1fr; } .invite-summary .el-descriptions { grid-column: auto; } }
</style>
