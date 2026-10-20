<template>
  <div class="app-container merchant-knowledge-page">
    <header class="page-heading">
      <div>
        <p class="page-kicker">KNOWLEDGE BASE</p>
        <h2>知识库</h2>
        <p class="page-description">查看平台公共知识，并维护本店内部操作文章和版本</p>
      </div>
      <el-button :icon="Refresh" @click="refreshPage">刷新</el-button>
    </header>

    <el-row :gutter="16" class="knowledge-layout">
      <el-col :xs="24" :sm="7" :md="6" :lg="5">
        <el-card shadow="never" class="category-card">
          <template #header>
            <div class="card-header">
              <span>知识分类</span>
              <el-button link type="primary" :icon="Plus" @click="openCategoryCreate()" v-hasPermi="['merchant:knowledge:add']">新增</el-button>
            </div>
          </template>
          <el-button class="all-category-button" :type="query.categoryId === undefined ? 'primary' : 'default'" plain @click="selectAllCategories">全部文章</el-button>
          <el-tree
            v-if="categoryTree.length"
            ref="categoryTreeRef"
            :data="categoryTree"
            node-key="categoryId"
            highlight-current
            default-expand-all
            :props="treeProps"
            @node-click="selectCategory"
          >
            <template #default="{ data }">
              <div class="category-node">
                <span class="category-node-name">{{ data.categoryName }}</span>
                <el-tag v-if="data.platformCategory" size="small" type="info" effect="plain">平台</el-tag>
                <el-tag v-else-if="data.status === 1" size="small" type="warning" effect="plain">停用</el-tag>
                <span v-if="!data.platformCategory" class="category-node-actions">
                  <el-button link type="primary" @click.stop="openCategoryCreate(data)" v-hasPermi="['merchant:knowledge:add']">新增</el-button>
                  <el-button link type="primary" @click.stop="openCategoryEdit(data)" v-hasPermi="['merchant:knowledge:edit']">编辑</el-button>
                  <el-button link type="danger" @click.stop="removeCategory(data)" v-hasPermi="['merchant:knowledge:remove']">删除</el-button>
                </span>
              </div>
            </template>
          </el-tree>
          <el-empty v-else :image-size="64" description="暂无知识分类" />
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="17" :md="18" :lg="19">
        <el-card shadow="never" class="article-card">
          <el-form ref="queryRef" :model="query" :inline="true" class="query-form" @submit.prevent>
            <el-form-item label="文章标题">
              <el-input v-model.trim="query.title" clearable placeholder="请输入文章标题" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="来源">
              <el-select v-model="query.sourceType" clearable placeholder="全部来源" style="width: 130px">
                <el-option v-for="item in sourceOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 130px">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
              <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button type="primary" plain :icon="Plus" @click="openArticleCreate" v-hasPermi="['merchant:knowledge:add']">新增文章</el-button>
            </el-col>
          </el-row>

          <el-table v-loading="loading" :data="rows" border>
            <el-table-column label="文章标题" prop="title" min-width="240" show-overflow-tooltip />
            <el-table-column label="知识分类" prop="categoryName" min-width="140" show-overflow-tooltip />
            <el-table-column label="来源" prop="sourceTypeName" width="110" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" effect="plain">{{ row.statusName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="版本" width="80" align="center">
              <template #default="{ row }">第 {{ row.currentVersionNo || 0 }} 版</template>
            </el-table-column>
            <el-table-column label="阅读次数" prop="readCount" width="90" align="center" />
            <el-table-column label="更新时间" prop="updateTime" width="170" align="center" />
            <el-table-column label="操作" width="260" fixed="right" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDetail(row)">详情</el-button>
                <template v-if="row.sourceType === SOURCE_TYPE.MERCHANT">
                  <el-button link type="primary" @click="openArticleEdit(row)" v-hasPermi="['merchant:knowledge:edit']">编辑</el-button>
                  <el-button v-if="row.status !== ARTICLE_STATUS.PUBLISHED" link type="success" @click="changeArticleStatus(row, true)" v-hasPermi="['merchant:knowledge:edit']">发布</el-button>
                  <el-button v-else link type="warning" @click="changeArticleStatus(row, false)" v-hasPermi="['merchant:knowledge:edit']">下架</el-button>
                  <el-button link type="danger" @click="removeArticle(row)" v-hasPermi="['merchant:knowledge:remove']">删除</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="loadList" />
          <el-empty v-if="!loading && rows.length === 0" description="暂无知识文章" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="categoryDialogVisible" :title="categoryForm.categoryId ? '编辑知识分类' : '新增知识分类'" width="500px" append-to-body destroy-on-close>
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="90px">
        <el-form-item label="上级分类" prop="parentId">
          <el-select v-model="categoryForm.parentId" clearable placeholder="请选择上级分类" style="width: 100%">
            <el-option label="顶级分类" :value="0" />
            <el-option v-for="item in parentOptions" :key="item.categoryId" :label="categoryOptionLabel(item)" :value="item.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName"><el-input v-model.trim="categoryForm.categoryName" maxlength="80" show-word-limit /></el-form-item>
        <el-form-item label="排序号" prop="sortNo"><el-input-number v-model="categoryForm.sortNo" :min="0" :max="999999" controls-position="right" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-switch v-model="categoryForm.status" :active-value="0" :inactive-value="1" active-text="正常" inactive-text="停用" inline-prompt /></el-form-item>
      </el-form>
      <template #footer><el-button @click="categoryDialogVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveCategory">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="articleDialogVisible" :title="articleForm.articleId ? '编辑知识文章' : '新增知识文章'" width="760px" append-to-body destroy-on-close>
      <el-form ref="articleFormRef" :model="articleForm" :rules="articleRules" label-width="90px">
        <el-form-item label="知识分类" prop="categoryId">
          <el-select v-model="articleForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="item in articleCategoryOptions" :key="item.categoryId" :label="categoryOptionLabel(item)" :value="item.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="文章标题" prop="title"><el-input v-model.trim="articleForm.title" maxlength="160" show-word-limit /></el-form-item>
        <el-form-item label="正文内容" prop="content"><el-input v-model="articleForm.content" type="textarea" :rows="14" maxlength="100000" show-word-limit /></el-form-item>
        <el-form-item label="阅读范围" prop="audienceType">
          <el-radio-group v-model="articleForm.audienceType" @change="handleAudienceTypeChange">
            <el-radio :value="AUDIENCE_TYPE.ALL">全体员工</el-radio>
            <el-radio :value="AUDIENCE_TYPE.SPECIFIED_ROLES">指定员工组</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="articleForm.audienceType === AUDIENCE_TYPE.SPECIFIED_ROLES" label="员工组" prop="roleIds">
          <el-select v-model="articleForm.roleIds" multiple clearable filterable placeholder="请选择可阅读的员工组" style="width: 100%">
            <el-option v-for="item in roleOptions" :key="item.roleId" :label="item.roleName" :value="item.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="文章附件">
          <file-upload v-model="articleForm.attachmentUrls" :limit="10" :file-size="20" :file-type="['pdf', 'png', 'jpg', 'jpeg', 'gif', 'webp', 'mp4', 'mov', 'avi']" :is-show-tip="false" business-type="knowledge" />
        </el-form-item>
        <el-form-item label="置顶"><el-switch v-model="articleForm.pinned" :active-value="1" :inactive-value="0" active-text="是" inactive-text="否" inline-prompt /></el-form-item>
      </el-form>
      <template #footer><el-button @click="articleDialogVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveArticle">保存</el-button></template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="知识文章详情" size="760px" destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="文章标题" :span="2">{{ detail.article.title }}</el-descriptions-item>
          <el-descriptions-item label="知识分类">{{ detail.article.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ detail.article.sourceTypeName }}</el-descriptions-item>
          <el-descriptions-item label="阅读范围">{{ detail.article.audienceTypeName || '全体员工' }}<span v-if="detail.article.roleNames">（{{ detail.article.roleNames }}）</span></el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="statusType(detail.article.status)" effect="plain">{{ detail.article.statusName }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="当前版本">第 {{ detail.article.currentVersionNo || 0 }} 版</el-descriptions-item>
          <el-descriptions-item label="阅读次数">{{ detail.article.readCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detail.article.updateTime || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">当前正文</el-divider>
        <div class="article-content">{{ currentContent }}</div>
        <el-divider content-position="left">版本记录</el-divider>
        <el-table :data="detail.versions || []" border size="small">
          <el-table-column label="版本" width="90" align="center"><template #default="{ row }">第 {{ row.versionNo }} 版</template></el-table-column>
          <el-table-column label="状态" prop="versionStatusName" width="100" align="center" />
          <el-table-column label="内容" prop="content" min-width="260" show-overflow-tooltip />
          <el-table-column label="附件" min-width="220">
            <template #default="{ row }">
              <template v-if="row.attachments?.length">
                <el-link v-for="attachment in row.attachments" :key="attachment.attachmentId" :href="attachment.fileUrl" target="_blank" type="primary" class="knowledge-attachment">{{ attachment.attachmentName }}</el-link>
              </template>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="175" align="center" />
        </el-table>
      </template>
      <el-skeleton v-else :rows="8" animated />
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup name="MerchantKnowledge">
import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  createMerchantKnowledgeArticle,
  createMerchantKnowledgeCategory,
  deleteMerchantKnowledgeArticle,
  deleteMerchantKnowledgeCategory,
  getMerchantKnowledgeArticle,
  listMerchantKnowledgeArticles,
  listMerchantKnowledgeCategories,
  listMerchantKnowledgeRoles,
  markMerchantKnowledgeArticleRead,
  offlineMerchantKnowledgeArticle,
  publishMerchantKnowledgeArticle,
  updateMerchantKnowledgeArticle,
  updateMerchantKnowledgeCategory
} from '@/api/merchant/knowledge'

const { proxy } = getCurrentInstance()
const SOURCE_TYPE = Object.freeze({ PLATFORM: 1, MERCHANT: 2 })
const ARTICLE_STATUS = Object.freeze({ DRAFT: 0, PUBLISHED: 1, OFFLINE: 2 })
const AUDIENCE_TYPE = Object.freeze({ ALL: 1, SPECIFIED_ROLES: 2 })
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const rows = ref([])
const categories = ref([])
const detail = ref(null)
const detailVisible = ref(false)
const articleDialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const queryRef = ref()
const categoryTreeRef = ref()
const articleFormRef = ref()
const categoryFormRef = ref()
const roleOptions = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, title: '', categoryId: undefined, status: undefined, sourceType: undefined })
const articleForm = reactive(defaultArticleForm())
const categoryForm = reactive(defaultCategoryForm())
const treeProps = { label: 'categoryName', children: 'children' }
const sourceOptions = [{ value: SOURCE_TYPE.PLATFORM, label: '平台公共' }, { value: SOURCE_TYPE.MERCHANT, label: '商户自建' }]
const statusOptions = [{ value: ARTICLE_STATUS.DRAFT, label: '草稿' }, { value: ARTICLE_STATUS.PUBLISHED, label: '已发布' }, { value: ARTICLE_STATUS.OFFLINE, label: '已下架' }]
const categoryRules = { categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }
const articleRules = { categoryId: [{ required: true, message: '请选择知识分类', trigger: 'change' }], title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }], content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }] }

const categoryTree = computed(() => {
  const nodes = categories.value.map(item => ({ ...item, children: [] }))
  const byId = new Map(nodes.map(item => [item.categoryId, item]))
  const roots = []
  nodes.forEach(node => {
    const parent = byId.get(node.parentId)
    if (parent) parent.children.push(node)
    else roots.push(node)
  })
  return roots
})
const parentOptions = computed(() => categories.value.filter(item => item.status === 0 && item.categoryId !== categoryForm.categoryId))
const articleCategoryOptions = computed(() => categories.value.filter(item => item.status === 0))
const currentContent = computed(() => detail.value?.versions?.[0]?.content || '暂无正文')

function defaultArticleForm() { return { articleId: undefined, categoryId: undefined, title: '', content: '', audienceType: AUDIENCE_TYPE.ALL, roleIds: [], attachmentUrls: '', pinned: 0 } }
function defaultCategoryForm() { return { categoryId: undefined, parentId: 0, categoryName: '', sortNo: 0, status: 0 } }
function unwrap(response) { return response?.data ?? response }
function errorText(error) { return error?.msg || error?.message || '操作失败，请稍后重试' }
function categoryOptionLabel(item) { return item.platformCategory ? `${item.categoryName}（平台）` : item.categoryName }
function statusType(status) { return ({ 0: 'info', 1: 'success', 2: 'warning' })[Number(status)] || 'info' }
function selectAllCategories() { query.categoryId = undefined; query.pageNum = 1; categoryTreeRef.value?.setCurrentKey(null); loadList() }
function selectCategory(node) { query.categoryId = node.categoryId; query.pageNum = 1; loadList() }
function handleQuery() { query.pageNum = 1; loadList() }
function resetQuery() { Object.assign(query, { pageNum: 1, pageSize: 10, title: '', categoryId: undefined, status: undefined, sourceType: undefined }); categoryTreeRef.value?.setCurrentKey(null); loadList() }
async function loadCategories() { const response = await listMerchantKnowledgeCategories(); categories.value = unwrap(response) || [] }
async function loadList() { loading.value = true; try { const response = await listMerchantKnowledgeArticles({ ...query }); rows.value = response.rows || []; total.value = Number(response.total || 0) } catch (error) { ElMessage.error(errorText(error)) } finally { loading.value = false } }
async function loadRoleOptions() { const response = await listMerchantKnowledgeRoles(); roleOptions.value = unwrap(response) || [] }
async function refreshPage() { try { await Promise.all([loadCategories(), loadRoleOptions()]); await loadList() } catch (error) { ElMessage.error(errorText(error)) } }
function openCategoryCreate(parent) { Object.assign(categoryForm, defaultCategoryForm(), { parentId: parent?.categoryId || 0 }); categoryDialogVisible.value = true }
function openCategoryEdit(row) { Object.assign(categoryForm, { categoryId: row.categoryId, parentId: row.parentId || 0, categoryName: row.categoryName, sortNo: row.sortNo || 0, status: row.status }); categoryDialogVisible.value = true }
async function saveCategory() { const valid = await categoryFormRef.value?.validate().catch(() => false); if (!valid) return; saving.value = true; try { if (categoryForm.categoryId) await updateMerchantKnowledgeCategory(categoryForm.categoryId, { ...categoryForm }); else await createMerchantKnowledgeCategory({ ...categoryForm }); ElMessage.success('知识分类保存成功'); categoryDialogVisible.value = false; await refreshPage() } catch (error) { ElMessage.error(errorText(error)) } finally { saving.value = false } }
async function removeCategory(row) { try { await ElMessageBox.confirm(`确认删除知识分类“${row.categoryName}”吗？`, '提示', { type: 'warning' }); await deleteMerchantKnowledgeCategory(row.categoryId); ElMessage.success('知识分类已删除'); await refreshPage() } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(errorText(error)) } }
function openArticleCreate() { Object.assign(articleForm, defaultArticleForm(), { categoryId: query.categoryId }); articleDialogVisible.value = true }
async function openArticleEdit(row) {
  try {
    const response = await getMerchantKnowledgeArticle(row.articleId)
    const data = unwrap(response)
    const latest = data.versions?.[0]
    Object.assign(articleForm, {
      articleId: data.article.articleId,
      categoryId: data.article.categoryId,
      title: data.article.title,
      content: latest?.content || '',
      audienceType: Number(data.article.audienceType || AUDIENCE_TYPE.ALL),
      roleIds: data.article.roleIds || [],
      attachmentUrls: (latest?.attachments || []).map(item => item.fileUrl).join(','),
      pinned: Number(data.article.pinned || 0)
    })
    articleDialogVisible.value = true
  } catch (error) {
    ElMessage.error(errorText(error))
  }
}
function handleAudienceTypeChange(value) { if (Number(value) === AUDIENCE_TYPE.ALL) articleForm.roleIds = [] }
function attachmentName(fileUrl) { return fileUrl.slice(fileUrl.lastIndexOf('/') + 1) || '知识附件' }
function buildArticlePayload() {
  const payload = { ...articleForm }
  payload.attachments = (articleForm.attachmentUrls || '').split(',').filter(Boolean).map(fileUrl => ({ attachmentName: attachmentName(fileUrl), fileUrl }))
  delete payload.attachmentUrls
  return payload
}
async function saveArticle() {
  const valid = await articleFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (articleForm.audienceType === AUDIENCE_TYPE.SPECIFIED_ROLES && !articleForm.roleIds.length) {
    ElMessage.warning('请选择至少一个员工组')
    return
  }
  saving.value = true
  try {
    const payload = buildArticlePayload()
    if (articleForm.articleId) await updateMerchantKnowledgeArticle(articleForm.articleId, payload)
    else await createMerchantKnowledgeArticle(payload)
    ElMessage.success('知识文章保存成功')
    articleDialogVisible.value = false
    await loadList()
  } catch (error) {
    ElMessage.error(errorText(error))
  } finally {
    saving.value = false
  }
}
async function openDetail(row) { detailVisible.value = true; detail.value = null; try { const response = await getMerchantKnowledgeArticle(row.articleId); detail.value = unwrap(response); markMerchantKnowledgeArticleRead(row.articleId).catch(() => {}) } catch (error) { detailVisible.value = false; ElMessage.error(errorText(error)) } }
async function changeArticleStatus(row, publish) { try { await ElMessageBox.confirm(`确认${publish ? '发布' : '下架'}文章“${row.title}”吗？`, '提示', { type: 'warning' }); await (publish ? publishMerchantKnowledgeArticle(row.articleId) : offlineMerchantKnowledgeArticle(row.articleId)); ElMessage.success(publish ? '文章已发布' : '文章已下架'); await loadList() } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(errorText(error)) } }
async function removeArticle(row) { try { await ElMessageBox.confirm(`确认删除文章“${row.title}”吗？`, '提示', { type: 'warning' }); await deleteMerchantKnowledgeArticle(row.articleId); ElMessage.success('文章已删除'); await loadList() } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(errorText(error)) } }

onMounted(refreshPage)
</script>

<style scoped>
.merchant-knowledge-page { min-height: 100%; }
.page-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.page-kicker { margin: 0 0 4px; color: var(--el-color-primary); font-size: 12px; font-weight: 700; letter-spacing: 1px; }
.page-heading h2 { margin: 0; }
.page-description { margin: 6px 0 0; color: var(--el-text-color-secondary); }
.knowledge-layout { align-items: flex-start; }
.category-card, .article-card { min-height: 520px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.all-category-button { width: 100%; margin-bottom: 12px; }
.category-node { display: flex; align-items: center; width: 100%; min-width: 0; gap: 6px; }
.category-node-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.category-node-actions { display: none; margin-left: auto; }
.category-node:hover .category-node-actions { display: inline-flex; }
.category-node-actions :deep(.el-button) { padding: 0 2px; font-size: 12px; }
.query-form { margin-bottom: 6px; }
.article-content { min-height: 120px; padding: 12px 16px; border: 1px solid var(--el-border-color-lighter); border-radius: 4px; white-space: pre-wrap; line-height: 1.7; color: var(--el-text-color-regular); }
.knowledge-attachment { display: block; margin-bottom: 4px; }
@media (max-width: 768px) {
  .page-heading { flex-direction: column; }
  .category-card { margin-bottom: 16px; }
  .category-node-actions { display: inline-flex; }
}
</style>
