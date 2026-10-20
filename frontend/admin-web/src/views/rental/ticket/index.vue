<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="工单号" prop="ticketNo">
        <el-input v-model.trim="queryParams.ticketNo" clearable placeholder="请输入工单号" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="工单类型" prop="ticketType">
        <el-select v-model="queryParams.ticketType" clearable placeholder="全部类型" style="width: 130px">
          <el-option v-for="item in ticketTypes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="工单状态" prop="ticketStatus">
        <el-select v-model="queryParams.ticketStatus" clearable placeholder="全部状态" style="width: 130px">
          <el-option v-for="item in ticketStatuses" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词" prop="keyword">
        <el-input v-model.trim="queryParams.keyword" clearable placeholder="标题或内容" style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="openCreate" v-hasPermi="['platform:ticket:add']">新建工单</el-button>
      </el-col>
      <right-toolbar :show-search="false" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="rows">
      <el-table-column label="工单号" prop="ticketNo" width="220" show-overflow-tooltip />
      <el-table-column label="标题" prop="title" min-width="220" show-overflow-tooltip />
      <el-table-column label="商户" prop="memberName" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" prop="ticketTypeName" width="100" />
      <el-table-column label="优先级" prop="priorityName" width="95" align="center">
        <template #default="{ row }"><el-tag :type="priorityMeta(row.priority).type">{{ row.priorityName || priorityMeta(row.priority).label }}</el-tag></template>
      </el-table-column>
      <el-table-column label="状态" prop="ticketStatusName" width="105" align="center">
        <template #default="{ row }"><el-tag :type="statusMeta(row.ticketStatus).type">{{ row.ticketStatusName || statusMeta(row.ticketStatus).label }}</el-tag></template>
      </el-table-column>
      <el-table-column label="处理人" prop="assigneeName" width="120"><template #default="{ row }">{{ row.assigneeName || '待分配' }}</template></el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="175" />
      <el-table-column label="操作" width="130" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button link type="primary" @click="openEvent(row)" v-hasPermi="['platform:ticket:process']">处理</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    <el-empty v-if="!loading && rows.length === 0" description="暂无客服工单" />

    <el-dialog v-model="createVisible" title="新建客服工单" width="620px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="工单类型" prop="ticketType"><el-select v-model="form.ticketType" style="width: 100%"><el-option v-for="item in ticketTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="优先级" prop="priority"><el-radio-group v-model="form.priority"><el-radio v-for="item in priorities" :key="item.value" :value="item.value">{{ item.label }}</el-radio></el-radio-group></el-form-item>
        <el-form-item label="工单标题" prop="title"><el-input v-model.trim="form.title" maxlength="160" show-word-limit /></el-form-item>
        <el-form-item label="工单内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="6" maxlength="4000" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button @click="createVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveTicket">保存</el-button></template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="客服工单详情" size="680px" destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="工单号">{{ detail.ticket.ticketNo }}</el-descriptions-item>
          <el-descriptions-item label="商户">{{ detail.ticket.memberName || '平台工单' }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.ticket.ticketTypeName }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ detail.ticket.priorityName }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="statusMeta(detail.ticket.ticketStatus).type">{{ detail.ticket.ticketStatusName }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="处理人">{{ detail.ticket.assigneeName || '待分配' }}</el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ detail.ticket.title }}</el-descriptions-item>
          <el-descriptions-item label="内容" :span="2">{{ detail.ticket.content }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">处理时间线</el-divider>
        <el-timeline v-if="detail.events?.length">
          <el-timeline-item v-for="event in detail.events" :key="event.eventId" :timestamp="event.createTime" placement="top">
            <strong>{{ event.eventTypeName }}</strong><span class="timeline-operator">{{ event.actorName || '系统' }}</span>
            <p>{{ event.detail }}</p>
            <small v-if="event.toStatusName">状态：{{ event.fromStatusName }} → {{ event.toStatusName }}</small>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无处理记录" />
        <el-divider content-position="left">指派记录</el-divider>
        <el-table v-if="detail.assignments?.length" :data="detail.assignments" border size="small">
          <el-table-column label="处理人" prop="assigneeName" min-width="150" />
          <el-table-column label="状态" prop="assignmentStatusName" width="110" align="center" />
          <el-table-column label="指派时间" prop="assignedTime" width="175" />
          <el-table-column label="完成时间" prop="completedTime" width="175">
            <template #default="{ row }">{{ row.completedTime || '-' }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无指派记录" />
      </template>
      <el-skeleton v-else :rows="8" animated />
      <template #footer><el-button @click="detailVisible = false">关闭</el-button><el-button v-if="detail" type="primary" @click="openEvent(detail.ticket)">追加处理</el-button></template>
    </el-drawer>

    <el-dialog v-model="eventVisible" title="处理客服工单" width="600px" append-to-body destroy-on-close>
      <el-form ref="eventFormRef" :model="eventForm" :rules="eventRules" label-width="90px">
        <el-form-item label="事件类型" prop="eventType"><el-select v-model="eventForm.eventType" style="width: 100%"><el-option v-for="item in eventTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="更新状态" prop="toStatus"><el-select v-model="eventForm.toStatus" style="width: 100%"><el-option v-for="item in ticketStatuses" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item v-if="Number(eventForm.eventType) === 2" label="处理人" prop="assigneeUserId"><el-select v-model="eventForm.assigneeUserId" filterable placeholder="请选择处理人" style="width: 100%"><el-option v-for="item in assignees" :key="item.userId" :label="item.nickName || item.userName" :value="item.userId" /></el-select></el-form-item>
        <el-form-item label="处理说明" prop="detail"><el-input v-model="eventForm.detail" type="textarea" :rows="5" maxlength="4000" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button @click="eventVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveEvent">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="PlatformTickets">
import { useRoute } from 'vue-router'
import { addPlatformTicket, appendPlatformTicketEvent, getPlatformTicket, listPlatformTicketAssignees, listPlatformTickets } from '@/api/rental/governance'

const { proxy } = getCurrentInstance()
const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const rows = ref([])
const detail = ref(null)
const assignees = ref([])
const detailVisible = ref(false)
const createVisible = ref(false)
const eventVisible = ref(false)
const formRef = ref()
const eventFormRef = ref()
const currentTicketId = ref()
const queryParams = reactive({ pageNum: 1, pageSize: 10, ticketNo: route.query.ticketNo ? String(route.query.ticketNo) : undefined, ticketType: undefined, ticketStatus: undefined, keyword: undefined })
const form = reactive(defaultForm())
const eventForm = reactive(defaultEventForm())
const ticketTypes = [{ value: 1, label: '认证' }, { value: 2, label: '售后' }, { value: 3, label: '财务' }, { value: 4, label: '订单' }, { value: 5, label: '内容' }, { value: 9, label: '其他' }]
const ticketStatuses = [{ value: 0, label: '待分配' }, { value: 1, label: '处理中' }, { value: 2, label: '待补充' }, { value: 3, label: '待审核' }, { value: 4, label: '待确认' }, { value: 5, label: '已解决' }, { value: 6, label: '已驳回' }, { value: 7, label: '已关闭' }]
const priorities = [{ value: 1, label: '普通' }, { value: 2, label: '重要' }, { value: 3, label: '紧急' }]
const eventTypes = [{ value: 2, label: '分配' }, { value: 3, label: '补充材料' }, { value: 4, label: '处理' }, { value: 5, label: '审核' }, { value: 6, label: '状态变更' }, { value: 9, label: '备注' }]
const rules = { title: [{ required: true, message: '请输入工单标题', trigger: 'blur' }], content: [{ required: true, message: '请输入工单内容', trigger: 'blur' }] }
const eventRules = {
  assigneeUserId: [{ validator: (_rule, value, callback) => Number(eventForm.eventType) === 2 && !value ? callback(new Error('请选择处理人')) : callback(), trigger: 'change' }],
  detail: [{ required: true, message: '请输入处理说明', trigger: 'blur' }]
}

function defaultForm() { return { ticketType: 9, priority: 1, title: '', content: '' } }
function defaultEventForm() { return { eventType: 4, toStatus: 1, assigneeUserId: undefined, detail: '' } }
function statusMeta(value) { return ({ 0: { label: '待分配', type: 'info' }, 1: { label: '处理中', type: 'primary' }, 2: { label: '待补充', type: 'warning' }, 3: { label: '待审核', type: 'warning' }, 4: { label: '待确认', type: 'warning' }, 5: { label: '已解决', type: 'success' }, 6: { label: '已驳回', type: 'danger' }, 7: { label: '已关闭', type: 'info' } })[Number(value)] || { label: '未知状态', type: 'info' } }
function priorityMeta(value) { return ({ 1: { label: '普通', type: 'info' }, 2: { label: '重要', type: 'warning' }, 3: { label: '紧急', type: 'danger' } })[Number(value)] || { label: '未知优先级', type: 'info' } }
function getList() { loading.value = true; listPlatformTickets({ ...queryParams }).then(response => { rows.value = response.rows || []; total.value = Number(response.total || 0) }).finally(() => { loading.value = false }) }
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function openCreate() { Object.assign(form, defaultForm()); createVisible.value = true }
async function saveTicket() { if (!await formRef.value.validate().catch(() => false)) return; saving.value = true; try { await addPlatformTicket({ ...form }); ElMessage.success('工单创建成功'); createVisible.value = false; getList() } finally { saving.value = false } }
function openDetail(row) { detailVisible.value = true; detail.value = null; getPlatformTicket(row.ticketId).then(response => { detail.value = response.data || null }) }
function openEvent(row) { currentTicketId.value = row.ticketId; const currentStatus = Number(row.ticketStatus ?? 1); Object.assign(eventForm, defaultEventForm(), { toStatus: currentStatus === 0 ? 1 : currentStatus }); eventVisible.value = true; loadAssignees() }
function loadAssignees() { listPlatformTicketAssignees().then(response => { assignees.value = response.data || [] }) }
async function saveEvent() { if (!await eventFormRef.value.validate().catch(() => false)) return; saving.value = true; try { await appendPlatformTicketEvent(currentTicketId.value, { ...eventForm }); ElMessage.success('工单处理已记录'); eventVisible.value = false; getList(); if (detailVisible.value && detail.value?.ticket?.ticketId === currentTicketId.value) openDetail(detail.value.ticket) } finally { saving.value = false } }
getList()
</script>

<style scoped>
.timeline-operator { margin-left: 12px; color: var(--el-text-color-secondary); }
.el-timeline p { margin: 8px 0 4px; white-space: pre-wrap; }
</style>
