<template>
  <div class="app-container merchant-internal-notice-page">
    <header class="page-heading">
      <div>
        <p class="page-kicker">INTERNAL NOTICES</p>
        <h2>内部通知</h2>
        <p class="page-description">向本店员工发布运营安排，并按员工组控制可见范围</p>
      </div>
      <el-button :icon="Refresh" @click="refreshPage">刷新</el-button>
    </header>

    <el-form v-show="showSearch" ref="queryRef" :model="query" :inline="true" class="query-form" @submit.prevent>
      <el-form-item label="通知标题" prop="noticeTitle">
        <el-input v-model.trim="query.noticeTitle" clearable placeholder="请输入通知标题" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="通知类型" prop="noticeType">
        <el-select v-model="query.noticeType" clearable placeholder="全部类型" style="width: 130px">
          <el-option v-for="item in noticeTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="投放范围" prop="targetScope">
        <el-select v-model="query.targetScope" clearable placeholder="全部范围" style="width: 140px">
          <el-option v-for="item in scopeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
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
        <el-button type="primary" plain :icon="Plus" v-hasPermi="['merchant:internalNotice:add']" @click="openCreate">新增通知</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadList" />
    </el-row>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="通知标题" prop="noticeTitle" min-width="240" show-overflow-tooltip />
      <el-table-column label="类型" prop="noticeTypeName" width="100" align="center" />
      <el-table-column label="投放范围" min-width="190" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.targetScopeName }}<span v-if="row.targetScope === SCOPE.SPECIFIED_ROLES && row.roleNames">：{{ row.roleNames }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }"><el-tag :type="statusType(row.status)" effect="plain">{{ row.statusName }}</el-tag></template>
      </el-table-column>
      <el-table-column label="生效时间" prop="effectiveTime" width="175" align="center">
        <template #default="{ row }">{{ row.effectiveTime || '发布后立即生效' }}</template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="175" align="center" />
      <el-table-column label="操作" width="300" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button link type="primary" @click="openReadStats(row)">阅读统计</el-button>
          <el-button link type="primary" v-hasPermi="['merchant:internalNotice:edit']" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status !== STATUS.PUBLISHED" link type="success" v-hasPermi="['merchant:internalNotice:edit']" @click="changeStatus(row, true)">发布</el-button>
          <el-button v-else link type="warning" v-hasPermi="['merchant:internalNotice:edit']" @click="changeStatus(row, false)">下架</el-button>
          <el-button v-if="row.status !== STATUS.PUBLISHED" link type="danger" v-hasPermi="['merchant:internalNotice:remove']" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="loadList" />
    <el-empty v-if="!loading && rows.length === 0" description="暂无内部通知" />

    <el-dialog v-model="formVisible" :title="form.internalNoticeId ? '编辑内部通知' : '新增内部通知'" width="720px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="通知标题" prop="noticeTitle"><el-input v-model.trim="form.noticeTitle" maxlength="160" show-word-limit placeholder="请输入通知标题" /></el-form-item>
        <el-form-item label="通知类型" prop="noticeType">
          <el-radio-group v-model="form.noticeType"><el-radio v-for="item in noticeTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="投放范围" prop="targetScope">
          <el-radio-group v-model="form.targetScope"><el-radio v-for="item in scopeOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.targetScope === SCOPE.SPECIFIED_ROLES" label="员工组" prop="roleIds">
          <el-select v-model="form.roleIds" multiple filterable clearable placeholder="请选择员工组" style="width: 100%">
            <el-option v-for="item in roleOptions" :key="item.roleId" :label="item.roleName" :value="item.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="正文内容" prop="noticeContent"><el-input v-model="form.noticeContent" type="textarea" :rows="10" maxlength="100000" show-word-limit placeholder="请输入通知内容" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="生效时间"><el-date-picker v-model="form.effectiveTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="发布后立即生效" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="下架时间"><el-date-picker v-model="form.offlineTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="不自动下架" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model.trim="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button @click="formVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveForm">保存草稿</el-button></template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="内部通知详情" size="640px" destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="通知标题" :span="2">{{ detail.noticeTitle }}</el-descriptions-item>
          <el-descriptions-item label="通知类型">{{ detail.noticeTypeName }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="statusType(detail.status)" effect="plain">{{ detail.statusName }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="投放范围" :span="2">{{ detail.targetScopeName }}<span v-if="detail.roleNames">：{{ detail.roleNames }}</span></el-descriptions-item>
          <el-descriptions-item label="生效时间">{{ detail.effectiveTime || '发布后立即生效' }}</el-descriptions-item>
          <el-descriptions-item label="下架时间">{{ detail.offlineTime || '不自动下架' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">通知正文</el-divider>
        <div class="notice-content">{{ formatNoticeContent(detail.noticeContent) }}</div>
      </template>
      <el-skeleton v-else :rows="8" animated />
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-drawer>

    <el-drawer v-model="readStatsVisible" title="内部通知阅读统计" size="720px" destroy-on-close>
      <template v-if="readStatsNotice">
        <el-descriptions :column="2" border label-width="120" class="stats-summary">
          <el-descriptions-item label="通知标题" :span="2">{{ readStatsNotice.noticeTitle }}</el-descriptions-item>
          <el-descriptions-item label="应读人数">{{ readStats.length }}</el-descriptions-item>
          <el-descriptions-item label="已读人数">{{ readCount }}</el-descriptions-item>
          <el-descriptions-item label="未读人数">{{ readStats.length - readCount }}</el-descriptions-item>
        </el-descriptions>
        <el-table v-loading="readStatsLoading" :data="readStats" border class="stats-table">
          <el-table-column label="员工" prop="displayName" min-width="150" />
          <el-table-column label="员工组" prop="roleNames" min-width="180" show-overflow-tooltip />
          <el-table-column label="阅读状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="Number(row.readStatus) === 1 ? 'success' : 'info'" effect="plain">{{ row.readStatusName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="阅读时间" prop="readTime" width="180" align="center">
            <template #default="{ row }">{{ row.readTime || '-' }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!readStatsLoading && readStats.length === 0" description="暂无可见员工" />
      </template>
      <el-skeleton v-else :rows="8" animated />
      <template #footer><el-button @click="readStatsVisible = false">关闭</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup name="MerchantInternalNotice">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  createMerchantInternalNotice,
  deleteMerchantInternalNotice,
  getMerchantInternalNoticeForManage,
  getMerchantInternalNoticeReadStats,
  listMerchantInternalNoticeRoles,
  listMerchantInternalNotices,
  offlineMerchantInternalNotice,
  publishMerchantInternalNotice,
  updateMerchantInternalNotice
} from '@/api/merchant/internalNotice'

const STATUS = Object.freeze({ DRAFT: 0, PUBLISHED: 1, OFFLINE: 2 })
const SCOPE = Object.freeze({ ALL_USERS: 1, SPECIFIED_ROLES: 2 })
const loading = ref(false)
const saving = ref(false)
const showSearch = ref(true)
const total = ref(0)
const rows = ref([])
const roleOptions = ref([])
const formVisible = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const readStatsVisible = ref(false)
const readStatsLoading = ref(false)
const readStatsNotice = ref(null)
const readStats = ref([])
const formRef = ref()
const query = reactive({ pageNum: 1, pageSize: 10, noticeTitle: '', noticeType: undefined, targetScope: undefined, status: undefined })
const form = reactive(defaultForm())
const noticeTypeOptions = [{ value: 1, label: '通知' }, { value: 2, label: '公告' }]
const scopeOptions = [{ value: SCOPE.ALL_USERS, label: '全体员工' }, { value: SCOPE.SPECIFIED_ROLES, label: '指定员工组' }]
const statusOptions = [{ value: STATUS.DRAFT, label: '草稿' }, { value: STATUS.PUBLISHED, label: '已发布' }, { value: STATUS.OFFLINE, label: '已下架' }]
const rules = {
  noticeTitle: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  targetScope: [{ required: true, message: '请选择投放范围', trigger: 'change' }],
  roleIds: [{ required: true, message: '请选择员工组', trigger: 'change' }],
  noticeContent: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
}

function defaultForm() { return { internalNoticeId: undefined, noticeTitle: '', noticeType: 1, noticeContent: '', targetScope: SCOPE.ALL_USERS, roleIds: [], effectiveTime: '', offlineTime: '', remark: '' } }
function unwrap(response) { return response?.data ?? response }
function errorText(error) { return error?.msg || error?.message || '操作失败，请稍后重试' }
function statusType(status) { return ({ 0: 'info', 1: 'success', 2: 'warning' })[Number(status)] || 'info' }
const readCount = computed(() => readStats.value.filter((item) => Number(item.readStatus) === 1).length)
function formatNoticeContent(content) {
  return String(content || '-').replace(/\\r\\n/g, '\n').replace(/\\n/g, '\n').replace(/\\r/g, '\n')
}
async function loadList() { loading.value = true; try { const response = await listMerchantInternalNotices({ ...query }); rows.value = response.rows || []; total.value = Number(response.total || 0) } catch (error) { ElMessage.error(errorText(error)) } finally { loading.value = false } }
async function loadRoles() { try { roleOptions.value = unwrap(await listMerchantInternalNoticeRoles()) || [] } catch (error) { ElMessage.error(errorText(error)) } }
async function refreshPage() { await Promise.all([loadRoles(), loadList()]) }
function handleQuery() { query.pageNum = 1; loadList() }
function resetQuery() { Object.assign(query, { pageNum: 1, pageSize: 10, noticeTitle: '', noticeType: undefined, targetScope: undefined, status: undefined }); loadList() }
function openCreate() { Object.assign(form, defaultForm()); formVisible.value = true }
async function openEdit(row) { try { const response = await getMerchantInternalNoticeForManage(row.internalNoticeId); Object.assign(form, { ...defaultForm(), ...unwrap(response) }); formVisible.value = true } catch (error) { ElMessage.error(errorText(error)) } }
async function openDetail(row) { detail.value = null; detailVisible.value = true; try { detail.value = unwrap(await getMerchantInternalNoticeForManage(row.internalNoticeId)) } catch (error) { detailVisible.value = false; ElMessage.error(errorText(error)) } }
async function openReadStats(row) { readStatsNotice.value = row; readStats.value = []; readStatsVisible.value = true; readStatsLoading.value = true; try { readStats.value = unwrap(await getMerchantInternalNoticeReadStats(row.internalNoticeId)) || [] } catch (error) { readStatsVisible.value = false; ElMessage.error(errorText(error)) } finally { readStatsLoading.value = false } }
async function saveForm() { const valid = await formRef.value?.validate().catch(() => false); if (!valid) return; saving.value = true; try { if (form.internalNoticeId) await updateMerchantInternalNotice(form.internalNoticeId, { ...form }); else await createMerchantInternalNotice({ ...form }); ElMessage.success('内部通知草稿已保存'); formVisible.value = false; await loadList() } catch (error) { ElMessage.error(errorText(error)) } finally { saving.value = false } }
async function changeStatus(row, publish) { try { await ElMessageBox.confirm(publish ? `确认发布“${row.noticeTitle}”吗？` : `确认下架“${row.noticeTitle}”吗？`, '提示', { type: 'warning' }); if (publish) await publishMerchantInternalNotice(row.internalNoticeId); else await offlineMerchantInternalNotice(row.internalNoticeId); ElMessage.success(publish ? '内部通知已发布' : '内部通知已下架'); await loadList() } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(errorText(error)) } }
async function removeRow(row) { try { await ElMessageBox.confirm(`确认删除“${row.noticeTitle}”吗？`, '提示', { type: 'warning' }); await deleteMerchantInternalNotice(row.internalNoticeId); ElMessage.success('内部通知已删除'); await loadList() } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(errorText(error)) } }
onMounted(refreshPage)
</script>

<style scoped lang="scss">
.merchant-internal-notice-page {
  .notice-content {
    min-height: 220px;
    max-width: 100%;
    padding: 4px 2px;
    white-space: pre-wrap;
    overflow-wrap: anywhere;
    word-break: break-word;
    line-height: 1.8;
    color: var(--el-text-color-primary);
  }
  .stats-summary { margin-bottom: 16px; }
  .stats-table { margin-top: 16px; }
}
</style>
