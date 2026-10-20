<template>
  <div class="app-container">
    <el-form ref="queryRef" v-show="showSearch" :model="queryParams" :inline="true">
      <el-form-item label="联盟名称" prop="keyword">
        <el-input v-model="queryParams.keyword" clearable placeholder="联盟名称、盟主名称或编码" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="全部" @change="handleQuery">
          <el-option label="正常" :value="ALLIANCE_STATUS.ACTIVE" />
          <el-option label="停用" :value="ALLIANCE_STATUS.DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" /></el-row>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="联盟名称" prop="allianceName" min-width="180" />
      <el-table-column label="盟主" prop="ownerMerchantName" min-width="160" />
      <el-table-column label="成员数量" prop="memberCount" width="100" align="center" />
      <el-table-column label="状态" prop="allianceStatus" width="110" align="center"><template #default="scope"><el-switch v-model="scope.row.allianceStatus" :active-value="ALLIANCE_STATUS.ACTIVE" :inactive-value="ALLIANCE_STATUS.DISABLED" inline-prompt active-text="正常" inactive-text="停用" v-hasPermi="['platform:alliance:edit']" @change="changeStatus(scope.row, null, scope.row.allianceStatus)" /></template></el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
      <el-table-column label="操作" width="150" fixed="right" align="center"><template #default="scope"><el-button link type="primary" @click="openMembers(scope.row)">成员</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    <el-dialog v-model="memberVisible" title="联盟成员" width="1100px">
      <el-table v-loading="memberLoading" :data="members" border>
        <el-table-column label="商户名称" prop="merchantName" min-width="180" />
        <el-table-column label="共享库存" prop="shareStockEnabled" width="110" align="center"><template #default="scope">{{ Number(scope.row.shareStockEnabled) === ALLIANCE_FLAG.ENABLED ? '允许' : '关闭' }}</template></el-table-column>
        <el-table-column label="承接订单上限" prop="acceptOrderLimit" width="130" align="center" />
        <el-table-column label="加入时间" prop="joinTime" width="180" align="center" />
        <el-table-column label="退出时间" prop="exitTime" width="180" align="center" />
        <el-table-column label="状态" prop="memberStatus" width="110" align="center"><template #default="scope"><el-tag v-if="Number(scope.row.memberStatus) === ALLIANCE_MEMBER_STATUS.EXITED" type="info">已退出</el-tag><el-switch v-else v-model="scope.row.memberStatus" :active-value="ALLIANCE_MEMBER_STATUS.ACTIVE" :inactive-value="ALLIANCE_MEMBER_STATUS.DISABLED" inline-prompt active-text="正常" inactive-text="停用" v-hasPermi="['platform:alliance:edit']" @change="changeStatus(currentAlliance, scope.row, scope.row.memberStatus)" /></template></el-table-column>
        <el-table-column label="操作" width="100" align="center"><template #default="scope"><el-button link type="primary" @click="openScopes(scope.row)">共享范围</el-button></template></el-table-column>
      </el-table>
    </el-dialog>
    <el-dialog v-model="scopeVisible" title="共享范围" width="900px">
      <el-table v-loading="scopeLoading" :data="scopes" border>
        <el-table-column label="类型" width="100" align="center"><template #default="scope">{{ Number(scope.row.scopeType) === ALLIANCE_SCOPE_TYPE.WAREHOUSE ? '仓库' : '商品' }}</template></el-table-column>
        <el-table-column label="名称" prop="scopeName" min-width="180" />
        <el-table-column label="编码" prop="scopeCode" min-width="150" />
        <el-table-column label="品牌" prop="brand" min-width="120" />
        <el-table-column label="型号" prop="modelName" min-width="150" />
        <el-table-column label="共享比例" width="100" align="center"><template #default="scope">{{ scope.row.shareQuantityRatio == null ? '-' : `${Number(scope.row.shareQuantityRatio) * 100}%` }}</template></el-table-column>
        <el-table-column label="共享上限" width="100" align="center"><template #default="scope">{{ scope.row.shareQuantityLimit == null ? '不限' : scope.row.shareQuantityLimit }}</template></el-table-column>
        <el-table-column label="允许协助订单" width="130" align="center"><template #default="scope">{{ Number(scope.row.acceptOrderEnabled) === ALLIANCE_FLAG.ENABLED ? '允许' : '关闭' }}</template></el-table-column>
      </el-table>
      <el-empty v-if="!scopeLoading && scopes.length === 0" description="暂无共享范围" />
    </el-dialog>
  </div>
</template>

<script setup name="PlatformAllianceManagement">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { listAlliances, listAllianceMembers, listAllianceMemberScopes, updateAllianceStatus } from '@/api/rental/alliance'
import { ALLIANCE_FLAG, ALLIANCE_MEMBER_STATUS, ALLIANCE_SCOPE_TYPE, ALLIANCE_STATUS } from '@/utils/rental/alliance'

const loading = ref(false)
const showSearch = ref(true)
const rows = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, keyword: undefined, status: undefined })
const memberVisible = ref(false)
const memberLoading = ref(false)
const members = ref([])
const currentAlliance = ref(null)
const scopeVisible = ref(false)
const scopeLoading = ref(false)
const scopes = ref([])

function getList() {
  loading.value = true
  listAlliances(queryParams).then(response => { rows.value = response.rows || []; total.value = Number(response.total || 0) }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryParams.keyword = undefined; queryParams.status = undefined; handleQuery() }
function openMembers(row) {
  currentAlliance.value = row
  memberVisible.value = true
  memberLoading.value = true
  listAllianceMembers(row.allianceId).then(response => { members.value = response.data || [] }).finally(() => { memberLoading.value = false })
}
function changeStatus(alliance, member, status) {
  updateAllianceStatus({ allianceId: alliance.allianceId, memberId: member?.memberId, status }).then(() => {
    ElMessage.success('状态已更新')
    if (member) openMembers(alliance)
    else getList()
  }).catch(() => {
    if (member) member.memberStatus = Number(status) === ALLIANCE_MEMBER_STATUS.ACTIVE ? ALLIANCE_MEMBER_STATUS.DISABLED : ALLIANCE_MEMBER_STATUS.ACTIVE
    else alliance.allianceStatus = Number(status) === ALLIANCE_STATUS.ACTIVE ? ALLIANCE_STATUS.DISABLED : ALLIANCE_STATUS.ACTIVE
  })
}
function openScopes(row) {
  scopeVisible.value = true
  scopeLoading.value = true
  scopes.value = []
  listAllianceMemberScopes(currentAlliance.value.allianceId, row.memberId).then(response => { scopes.value = response.data || [] }).finally(() => { scopeLoading.value = false })
}
getList()
</script>
