<template>
  <div class="app-container platform-market-page">
    <PageHeading
      kicker="MARKET GOVERNANCE"
      title="周转市场治理"
      description="审核市场帖子、处理商户举报并维护平台内容秩序"
    >
      <template #aside><el-button :icon="Refresh" :loading="loading" @click="refresh">刷新</el-button></template>
    </PageHeading>

    <el-tabs v-model="activeTab" @tab-change="loadActiveTab">
      <el-tab-pane label="帖子审核" name="posts">
        <el-form :inline="true" :model="postQuery" class="filter-form" @submit.prevent>
          <el-form-item label="关键词"><el-input v-model.trim="postQuery.keyword" clearable placeholder="帖子编号、商户、品牌或型号" @keyup.enter="searchPosts" /></el-form-item>
          <el-form-item label="板块"><el-select v-model="postQuery.boardType" clearable placeholder="全部板块" style="width: 120px"><el-option v-for="item in boardTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
          <el-form-item label="帖子状态"><el-select v-model="postQuery.postStatus" clearable placeholder="全部状态" style="width: 120px"><el-option v-for="item in postStatuses" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
          <el-form-item label="城市"><el-input v-model.trim="postQuery.city" clearable placeholder="发货城市" @keyup.enter="searchPosts" /></el-form-item>
          <el-form-item><el-button type="primary" :icon="Search" @click="searchPosts">搜索</el-button><el-button :icon="RefreshRight" @click="resetPosts">重置</el-button></el-form-item>
        </el-form>
        <el-table v-loading="loading" :data="postRows" border>
          <el-table-column label="帖子编号" prop="postNo" width="220" show-overflow-tooltip />
          <el-table-column label="商户" prop="merchantName" min-width="160" show-overflow-tooltip />
          <el-table-column label="板块" prop="boardTypeName" width="80" />
          <el-table-column label="商品" min-width="220" show-overflow-tooltip><template #default="{ row }">{{ [row.brand, row.modelName].filter(Boolean).join(' / ') || '-' }}</template></el-table-column>
          <el-table-column label="城市" prop="city" width="110" />
          <el-table-column label="数量" prop="quantity" width="80" align="right" />
          <el-table-column label="状态" prop="postStatusName" width="100" align="center"><template #default="{ row }"><el-tag :type="postStatusType(row.postStatus)">{{ row.postStatusName }}</el-tag></template></el-table-column>
          <el-table-column label="待处理举报" prop="pendingReportCount" width="105" align="right"><template #default="{ row }"><span :class="{ 'danger-text': Number(row.pendingReportCount) > 0 }">{{ row.pendingReportCount || 0 }}</span></template></el-table-column>
          <el-table-column label="降权" width="80" align="center"><template #default="{ row }"><el-tag v-if="Number(row.downRankFlag) === 1" type="warning" effect="plain">已降权</el-tag><span v-else>正常</span></template></el-table-column>
          <el-table-column label="操作" width="210" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="showPost(row)">详情</el-button>
              <el-button v-if="Number(row.postStatus) !== 3 && Number(row.postStatus) !== 2" v-hasPermi="['platform:market:moderate']" link type="warning" @click="offlinePost(row)">下架</el-button>
              <el-button v-hasPermi="['platform:market:moderate']" link type="primary" @click="toggleRank(row)">{{ Number(row.downRankFlag) === 1 ? '取消降权' : '降权' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="postTotal > 0" v-model:page="postQuery.pageNum" v-model:limit="postQuery.pageSize" :total="postTotal" @pagination="loadPosts" />
        <el-empty v-if="!loading && postRows.length === 0" description="暂无市场帖子" />
      </el-tab-pane>

      <el-tab-pane label="举报处理" name="reports">
        <el-form :inline="true" :model="reportQuery" class="filter-form" @submit.prevent>
          <el-form-item label="关键词"><el-input v-model.trim="reportQuery.keyword" clearable placeholder="帖子编号、商户或举报类型" @keyup.enter="searchReports" /></el-form-item>
          <el-form-item label="处理状态"><el-select v-model="reportQuery.reportStatus" clearable placeholder="全部状态" style="width: 120px"><el-option v-for="item in reportStatuses" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
          <el-form-item label="城市"><el-input v-model.trim="reportQuery.city" clearable placeholder="帖子城市" @keyup.enter="searchReports" /></el-form-item>
          <el-form-item><el-button type="primary" :icon="Search" @click="searchReports">搜索</el-button><el-button :icon="RefreshRight" @click="resetReports">重置</el-button></el-form-item>
        </el-form>
        <el-table v-loading="loading" :data="reportRows" border>
          <el-table-column label="帖子编号" prop="postNo" width="220" show-overflow-tooltip />
          <el-table-column label="举报商户" prop="reporterName" min-width="150" show-overflow-tooltip />
          <el-table-column label="被举报商户" prop="reportedMerchantName" min-width="150" show-overflow-tooltip />
          <el-table-column label="举报类型" prop="reportType" width="130" show-overflow-tooltip />
          <el-table-column label="处理状态" prop="reportStatusName" width="100" align="center"><template #default="{ row }"><el-tag :type="reportStatusType(row.reportStatus)">{{ row.reportStatusName }}</el-tag></template></el-table-column>
          <el-table-column label="处理结果" prop="reviewResult" min-width="180" show-overflow-tooltip />
          <el-table-column label="提交时间" prop="createTime" width="175" />
          <el-table-column label="操作" width="100" fixed="right" align="center"><template #default="{ row }"><el-button v-if="Number(row.reportStatus) === 0" v-hasPermi="['platform:market:review']" link type="primary" @click="openReview(row)">处理</el-button><el-button v-else link type="primary" @click="showReport(row)">详情</el-button></template></el-table-column>
        </el-table>
        <pagination v-show="reportTotal > 0" v-model:page="reportQuery.pageNum" v-model:limit="reportQuery.pageSize" :total="reportTotal" @pagination="loadReports" />
        <el-empty v-if="!loading && reportRows.length === 0" description="暂无市场举报" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="postDetailVisible" title="市场帖子详情" width="720px" append-to-body destroy-on-close>
      <el-descriptions v-if="currentPost" :column="2" border label-width="120">
        <el-descriptions-item label="帖子编号">{{ currentPost.postNo }}</el-descriptions-item>
        <el-descriptions-item label="商户">{{ currentPost.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="板块">{{ currentPost.boardTypeName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentPost.postStatusName }}</el-descriptions-item>
        <el-descriptions-item label="品类">{{ currentPost.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商品">{{ [currentPost.brand, currentPost.modelName].filter(Boolean).join(' / ') || '-' }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ currentPost.memorySpec || '-' }}</el-descriptions-item>
        <el-descriptions-item label="成色">{{ currentPost.conditionLevel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ currentPost.city || '-' }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentPost.quantity || 0 }}</el-descriptions-item>
        <el-descriptions-item label="服务标签" :span="2">{{ currentPost.serviceTags || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentPost.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-skeleton v-else :rows="5" animated />
      <template #footer><el-button @click="postDetailVisible = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="reportDetailVisible" title="举报详情" width="680px" append-to-body destroy-on-close>
      <el-descriptions v-if="currentReport" :column="2" border label-width="120">
        <el-descriptions-item label="帖子编号">{{ currentReport.postNo }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">{{ currentReport.reportStatusName }}</el-descriptions-item>
        <el-descriptions-item label="举报商户">{{ currentReport.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="被举报商户">{{ currentReport.reportedMerchantName }}</el-descriptions-item>
        <el-descriptions-item label="举报类型">{{ currentReport.reportType }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentReport.createTime }}</el-descriptions-item>
        <el-descriptions-item label="证据" :span="2"><pre class="evidence">{{ currentReport.evidence || '-' }}</pre></el-descriptions-item>
        <el-descriptions-item label="处理结果" :span="2">{{ currentReport.reviewResult || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="reportDetailVisible = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="处理市场举报" width="560px" append-to-body destroy-on-close>
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="100px">
        <el-form-item label="举报帖子"><span>{{ currentReport?.postNo || '-' }}</span></el-form-item>
        <el-form-item label="处理结果" prop="reportStatus"><el-radio-group v-model="reviewForm.reportStatus"><el-radio :value="1">认定并处理</el-radio><el-radio :value="2">驳回举报</el-radio></el-radio-group></el-form-item>
        <el-form-item label="处理说明" prop="reviewResult"><el-input v-model.trim="reviewForm.reviewResult" type="textarea" :rows="5" maxlength="1000" show-word-limit placeholder="请填写处理结果" /></el-form-item>
        <el-form-item v-if="reviewForm.reportStatus === 1" label="帖子治理"><el-checkbox v-model="reviewForm.offlinePost">同时下架被举报帖子</el-checkbox></el-form-item>
      </el-form>
      <template #footer><el-button @click="reviewVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitReview">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="PlatformMarketGovernance">
import { reactive, ref, getCurrentInstance } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, RefreshRight, Search } from '@element-plus/icons-vue'
import PageHeading from '@/components/PageHeading.vue'
import { listPlatformMarketPosts, listPlatformMarketReports, offlinePlatformMarketPost, reviewPlatformMarketReport, updatePlatformMarketPostRank } from '@/api/rental/market'

const { proxy } = getCurrentInstance()
const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const activeTab = ref(route.query.tab === 'reports' ? 'reports' : 'posts')
const postRows = ref([])
const reportRows = ref([])
const postTotal = ref(0)
const reportTotal = ref(0)
const currentPost = ref(null)
const currentReport = ref(null)
const postDetailVisible = ref(false)
const reportDetailVisible = ref(false)
const reviewVisible = ref(false)
const reviewFormRef = ref()
const postQuery = reactive({ pageNum: 1, pageSize: 10, keyword: undefined, boardType: undefined, postStatus: undefined, city: undefined })
const reportQuery = reactive({ pageNum: 1, pageSize: 10, keyword: route.query.keyword ? String(route.query.keyword) : undefined, reportStatus: route.query.tab === 'reports' ? 0 : undefined, city: undefined })
const reviewForm = reactive({ reportStatus: 1, reviewResult: '', offlinePost: false })
const boardTypes = [{ value: 1, label: '出售' }, { value: 2, label: '出租' }, { value: 3, label: '求购' }]
const postStatuses = [{ value: 0, label: '草稿' }, { value: 1, label: '已发布' }, { value: 2, label: '已关闭' }, { value: 3, label: '已下架' }]
const reportStatuses = [{ value: 0, label: '待处理' }, { value: 1, label: '已处理' }, { value: 2, label: '已驳回' }]
const reviewRules = { reportStatus: [{ required: true, message: '请选择处理结果', trigger: 'change' }], reviewResult: [{ required: true, message: '请输入处理说明', trigger: 'blur' }] }

function loadActiveTab() { activeTab.value === 'posts' ? loadPosts() : loadReports() }
function refresh() { loadActiveTab() }
function loadPosts() { loading.value = true; listPlatformMarketPosts({ ...postQuery }).then(response => { postRows.value = response.rows || []; postTotal.value = Number(response.total || 0) }).finally(() => { loading.value = false }) }
function loadReports() { loading.value = true; listPlatformMarketReports({ ...reportQuery }).then(response => { reportRows.value = response.rows || []; reportTotal.value = Number(response.total || 0) }).finally(() => { loading.value = false }) }
function searchPosts() { postQuery.pageNum = 1; loadPosts() }
function searchReports() { reportQuery.pageNum = 1; loadReports() }
function resetPosts() { Object.assign(postQuery, { pageNum: 1, keyword: undefined, boardType: undefined, postStatus: undefined, city: undefined }); loadPosts() }
function resetReports() { Object.assign(reportQuery, { pageNum: 1, keyword: undefined, reportStatus: undefined, city: undefined }); loadReports() }
function showPost(row) { currentPost.value = row; postDetailVisible.value = true }
function showReport(row) { currentReport.value = row; reportDetailVisible.value = true }
function openReview(row) { currentReport.value = row; Object.assign(reviewForm, { reportStatus: 1, reviewResult: '', offlinePost: false }); reviewVisible.value = true }
async function submitReview() { if (!await reviewFormRef.value.validate().catch(() => false)) return; saving.value = true; try { await reviewPlatformMarketReport(currentReport.value.reportId, { ...reviewForm }); ElMessage.success('举报处理完成'); reviewVisible.value = false; loadReports(); if (activeTab.value === 'posts') loadPosts() } finally { saving.value = false } }
async function offlinePost(row) { await proxy.$modal.confirm(`确认下架帖子“${row.postNo}”吗？`); await offlinePlatformMarketPost(row.postId); ElMessage.success('帖子已下架'); loadPosts() }
async function toggleRank(row) { const next = Number(row.downRankFlag) === 1 ? 0 : 1; await proxy.$modal.confirm(`确认${next === 1 ? '将帖子降权' : '取消帖子降权'}吗？`); await updatePlatformMarketPostRank(row.postId, next); ElMessage.success(next === 1 ? '帖子已降权' : '已取消降权'); loadPosts() }
function postStatusType(value) { return ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' })[Number(value)] || 'info' }
function reportStatusType(value) { return ({ 0: 'warning', 1: 'success', 2: 'info' })[Number(value)] || 'info' }

activeTab.value === 'reports' ? loadReports() : loadPosts()
</script>

<style scoped>
.filter-form { margin-bottom: 12px; }
.danger-text { color: var(--el-color-danger); font-weight: 600; }
.evidence { margin: 0; white-space: pre-wrap; word-break: break-all; font: inherit; }
</style>
