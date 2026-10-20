<template>
  <div class="app-container">
    <el-table :data="rows" v-loading="loading">
      <el-table-column label="邀请码" prop="inviteCode" width="140" />
      <el-table-column label="邀请商户编码" prop="inviterMemberCode" width="150" />
      <el-table-column label="邀请商户" prop="inviterName" min-width="170" />
      <el-table-column label="等级" prop="levelName" width="120" />
      <el-table-column label="已用/上限" width="110"><template #default="scope">{{ scope.row.usedCount }}/{{ scope.row.inviteLimit }}</template></el-table-column>
      <el-table-column label="被邀请商户编码" prop="inviteeMemberCode" width="160" />
      <el-table-column label="被邀请商户" prop="inviteeName" min-width="170" />
      <el-table-column label="注册手机号" prop="inviteePhone" width="140" />
      <el-table-column label="注册时间" prop="registeredTime" width="180" />
    </el-table>
  </div>
</template>
<script setup name="PromotionManagement">
import { listPromotion } from '@/api/rental/promotion'
const loading = ref(false)
const rows = ref([])
/** 加载平台全部邀请码和邀请记录。 */
async function loadList() {
  loading.value = true
  try {
    rows.value = (await listPromotion()).data || []
  } finally {
    loading.value = false
  }
}
onMounted(loadList)
</script>
