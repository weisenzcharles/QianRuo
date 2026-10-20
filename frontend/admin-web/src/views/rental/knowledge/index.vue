<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="文章标题" prop="title"><el-input v-model.trim="queryParams.title" clearable placeholder="请输入文章标题" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item v-if="activeView === 'compliance'" label="商户"><el-input v-model.trim="queryParams.memberKeyword" clearable placeholder="名称或会员编码" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="分类" prop="categoryId"><el-select v-model="queryParams.categoryId" clearable placeholder="全部分类" style="width: 160px"><el-option v-for="item in categories" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" /></el-select></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" clearable placeholder="全部状态" style="width: 130px"><el-option v-for="item in statuses" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-tabs v-model="activeView" class="mb8" @tab-change="handleViewChange">
      <el-tab-pane label="平台知识" name="platform" />
      <el-tab-pane v-if="canReviewMerchantKnowledge" label="商户知识合规" name="compliance" />
    </el-tabs>
    <el-row :gutter="10" class="mb8">
      <el-col v-if="activeView === 'platform'" :span="1.5"><el-button type="primary" plain icon="Plus" @click="openCreate" v-hasPermi="['platform:knowledge:add']">新增文章</el-button></el-col>
      <right-toolbar :show-search="false" @queryTable="getList" />
    </el-row>
    <el-table v-loading="loading" :data="rows">
      <el-table-column label="文章标题" prop="title" min-width="240" show-overflow-tooltip />
      <el-table-column v-if="activeView === 'compliance'" label="所属商户" prop="memberName" min-width="160" show-overflow-tooltip />
      <el-table-column label="分类" prop="categoryName" width="150" />
      <el-table-column label="来源" prop="sourceTypeName" width="90" />
      <el-table-column label="状态" prop="statusName" width="100" align="center"><template #default="{ row }"><el-tag :type="statusMeta(row.status).type">{{ row.statusName || statusMeta(row.status).label }}</el-tag></template></el-table-column>
      <el-table-column label="当前版本" prop="currentVersionNo" width="100" align="center" />
      <el-table-column label="阅读次数" prop="readCount" width="100" align="center" />
      <el-table-column label="更新时间" prop="updateTime" width="175" />
      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <template v-if="activeView === 'platform'">
            <el-button link type="primary" @click="openEdit(row)" v-hasPermi="['platform:knowledge:edit']">编辑</el-button>
            <el-button v-if="row.status !== 1" link type="success" @click="changeStatus(row, true)" v-hasPermi="['platform:knowledge:publish']">发布</el-button>
            <el-button v-else link type="warning" @click="changeStatus(row, false)" v-hasPermi="['platform:knowledge:publish']">下架</el-button>
            <el-button link type="danger" @click="removeArticle(row)" v-hasPermi="['platform:knowledge:remove']">删除</el-button>
          </template>
          <template v-else>
            <el-button v-if="row.status === 1" link type="danger" @click="offlineMerchantArticle(row)" v-hasPermi="['platform:knowledge:compliance:offline']">违规下架</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    <el-empty v-if="!loading && rows.length === 0" description="暂无知识文章" />

    <el-dialog v-model="editorVisible" :title="form.articleId ? '编辑知识文章' : '新增知识文章'" width="760px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="知识分类" prop="categoryId"><el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%"><el-option v-for="item in categories" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" /></el-select></el-form-item>
        <el-form-item label="文章标题" prop="title"><el-input v-model.trim="form.title" maxlength="160" show-word-limit /></el-form-item>
        <el-form-item label="正文内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="12" maxlength="20000" show-word-limit /></el-form-item>
        <el-form-item label="文章附件">
          <file-upload v-model="form.attachmentUrls" :limit="10" :file-size="20" :file-type="['pdf', 'png', 'jpg', 'jpeg', 'gif', 'webp', 'mp4', 'mov', 'avi']" :is-show-tip="false" business-type="knowledge" />
        </el-form-item>
        <el-form-item label="置顶"><el-switch v-model="form.pinned" :active-value="1" :inactive-value="0" active-text="是" inactive-text="否" inline-prompt /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editorVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveArticle">保存</el-button></template>
    </el-dialog>

    <el-drawer v-model="detailVisible" :title="activeView === 'compliance' ? '商户知识合规详情' : '知识文章详情'" size="760px" destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="文章标题" :span="2">{{ detail.article.title }}</el-descriptions-item>
          <el-descriptions-item label="知识分类">{{ detail.article.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="activeView === 'compliance'" label="所属商户">{{ detail.article.memberName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="statusMeta(detail.article.status).type">{{ detail.article.statusName }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="当前版本">第 {{ detail.article.currentVersionNo || 0 }} 版</el-descriptions-item>
          <el-descriptions-item label="阅读次数">{{ detail.article.readCount || 0 }}</el-descriptions-item>
          <el-descriptions-item v-if="activeView === 'compliance'" label="违规原因" :span="2">{{ detail.article.violationReason || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">版本记录</el-divider>
        <el-table :data="detail.versions || []" border size="small">
          <el-table-column label="版本" width="90"><template #default="{ row }">第 {{ row.versionNo }} 版</template></el-table-column>
          <el-table-column label="状态" prop="versionStatusName" width="100" />
          <el-table-column label="内容" prop="content" min-width="260" show-overflow-tooltip />
          <el-table-column label="附件" min-width="220">
            <template #default="{ row }">
              <template v-if="row.attachments?.length">
                <el-link v-for="attachment in row.attachments" :key="attachment.attachmentId" :href="attachment.fileUrl" target="_blank" type="primary" class="knowledge-attachment">{{ attachment.attachmentName }}</el-link>
              </template>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="175" />
        </el-table>
      </template>
      <el-skeleton v-else :rows="8" animated />
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup name="PlatformKnowledge">
import { ElMessageBox } from 'element-plus'
import { addKnowledgeArticle, deleteKnowledgeArticle, getKnowledgeArticle, getKnowledgeComplianceArticle, listKnowledgeArticles, listKnowledgeCategories, listKnowledgeComplianceArticles, offlineKnowledgeArticle, offlineMerchantKnowledgeArticle, publishKnowledgeArticle, updateKnowledgeArticle } from '@/api/rental/governance'
import { checkPermi } from '@/utils/permission'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const rows = ref([])
const categories = ref([])
const detail = ref(null)
const detailVisible = ref(false)
const editorVisible = ref(false)
const formRef = ref()
const activeView = ref('platform')
const canReviewMerchantKnowledge = checkPermi(['platform:knowledge:compliance:list'])
const queryParams = reactive({ pageNum: 1, pageSize: 10, title: undefined, categoryId: undefined, status: undefined, memberKeyword: undefined })
const form = reactive(defaultForm())
const statuses = [{ value: 0, label: '草稿' }, { value: 1, label: '已发布' }, { value: 2, label: '已下架' }]
const rules = { categoryId: [{ required: true, message: '请选择知识分类', trigger: 'change' }], title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }], content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }] }
function defaultForm() { return { articleId: undefined, categoryId: undefined, title: '', content: '', attachmentUrls: '', pinned: 0 } }
function statusMeta(value) { return ({ 0: { label: '草稿', type: 'info' }, 1: { label: '已发布', type: 'success' }, 2: { label: '已下架', type: 'warning' } })[Number(value)] || { label: '未知状态', type: 'info' } }
function getList() { loading.value = true; const loader = activeView.value === 'compliance' ? listKnowledgeComplianceArticles : listKnowledgeArticles; loader({ ...queryParams }).then(response => { rows.value = response.rows || []; total.value = Number(response.total || 0) }).finally(() => { loading.value = false }) }
function loadCategories() { listKnowledgeCategories().then(response => { categories.value = response.data || [] }) }
function handleQuery() { queryParams.pageNum = 1; getList() }
function handleViewChange() { queryParams.pageNum = 1; queryParams.status = undefined; queryParams.memberKeyword = undefined; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function openCreate() { Object.assign(form, defaultForm()); editorVisible.value = true }
async function openEdit(row) { const response = await getKnowledgeArticle(row.articleId); const data = response.data; const latest = data?.versions?.[0]; Object.assign(form, { articleId: data.article.articleId, categoryId: data.article.categoryId, title: data.article.title, content: latest?.content || '', attachmentUrls: (latest?.attachments || []).map(item => item.fileUrl).join(','), pinned: Number(data.article.pinned || 0) }); editorVisible.value = true }
function attachmentName(fileUrl) { return fileUrl.slice(fileUrl.lastIndexOf('/') + 1) || '知识附件' }
function buildArticlePayload() { const payload = { ...form }; payload.attachments = (form.attachmentUrls || '').split(',').filter(Boolean).map(fileUrl => ({ attachmentName: attachmentName(fileUrl), fileUrl })); delete payload.attachmentUrls; return payload }
async function saveArticle() { if (!await formRef.value.validate().catch(() => false)) return; saving.value = true; try { await (form.articleId ? updateKnowledgeArticle(buildArticlePayload()) : addKnowledgeArticle(buildArticlePayload())); ElMessage.success('知识文章保存成功'); editorVisible.value = false; getList() } finally { saving.value = false } }
function openDetail(row) { detailVisible.value = true; detail.value = null; const loader = activeView.value === 'compliance' ? getKnowledgeComplianceArticle : getKnowledgeArticle; loader(row.articleId).then(response => { detail.value = response.data || null }) }
async function changeStatus(row, publish) { await proxy.$modal.confirm(`确认${publish ? '发布' : '下架'}文章“${row.title}”吗？`); await (publish ? publishKnowledgeArticle(row.articleId) : offlineKnowledgeArticle(row.articleId)); ElMessage.success(publish ? '文章已发布' : '文章已下架'); getList() }
async function offlineMerchantArticle(row) { try { const result = await ElMessageBox.prompt(`请输入“${row.title}”的违规原因`, '违规下架', { inputType: 'textarea', inputPlaceholder: '请输入违规原因，最多500个字符', inputValidator: value => value && value.trim() ? true : '违规原因不能为空', inputErrorMessage: '违规原因不能为空', confirmButtonText: '确认下架', cancelButtonText: '取消' }); await offlineMerchantKnowledgeArticle(row.articleId, { reason: result.value.trim() }); ElMessage.success('商户知识文章已违规下架'); getList() } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(error?.message || error) } }
async function removeArticle(row) { await proxy.$modal.confirm(`确认删除平台文章“${row.title}”吗？删除后可在回收站保留 30 天并恢复。`); await deleteKnowledgeArticle(row.articleId); ElMessage.success('文章已移入回收站'); getList() }
loadCategories(); getList()
</script>
