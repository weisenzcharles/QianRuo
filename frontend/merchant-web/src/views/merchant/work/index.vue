<template>
  <div class="app-container merchant-work-page">
    <header class="page-heading">
      <div>
        <p class="page-kicker">WORK CENTER</p>
        <h2>我的工作台</h2>
        <p class="page-description">集中处理任务、提交完成说明和查看质检结果</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="refresh">
        刷新
      </el-button>
    </header>

    <el-row :gutter="12" class="summary-grid">
      <el-col
        v-for="item in summaryItems"
        :key="item.key"
        :xs="12"
        :sm="8"
        :lg="4">
        <el-card shadow="never" class="summary-card">
          <span>{{ item.label }}</span>
          <strong :class="item.className">{{ summary[item.key] || 0 }}</strong>
        </el-card>
      </el-col>
    </el-row>

    <el-form
      v-show="showSearch"
      :inline="true"
      :model="query"
      class="search-form"
      @submit.prevent>
      <el-form-item label="任务关键词">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="标题或任务内容"
          @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="关联仓库">
        <el-select
          v-model="query.warehouseId"
          clearable
          filterable
          placeholder="全部仓库"
          style="width: 180px">
          <el-option
            v-for="item in warehouses"
            :key="warehouseId(item)"
            :label="warehouseLabel(item)"
            :value="warehouseId(item)" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务状态">
        <el-select
          v-model="query.status"
          clearable
          placeholder="全部状态"
          style="width: 150px">
          <el-option
            v-for="item in taskStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="search">
          查询
        </el-button>
        <el-button :icon="Refresh" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" :icon="Plus" @click="openCreate">
          新建任务
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button :icon="DataAnalysis" @click="openReport">工作日报</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadList" />
    </el-row>

    <el-table v-loading="loading" :data="rows" border row-key="taskId">
      <el-table-column label="任务标题" min-width="190" show-overflow-tooltip>
        <template #default="{ row }">
          <strong>{{ row.title }}</strong>
          <span class="secondary-text">{{ row.taskTypeLabel }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="任务内容"
        prop="content"
        min-width="240"
        show-overflow-tooltip />
      <el-table-column
        label="来源"
        prop="sourceTypeLabel"
        width="105"
        align="center" />
      <el-table-column label="仓库" prop="warehouseName" min-width="130">
        <template #default="{ row }">
          {{ row.warehouseName || "不限定仓库" }}
        </template>
      </el-table-column>
      <el-table-column label="执行人" min-width="115">
        <template #default="{ row }">
          {{ row.assigneeName || "待指派" }}
        </template>
      </el-table-column>
      <el-table-column label="截止时间" prop="dueTime" width="170" />
      <el-table-column label="状态" width="105" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row)">{{ row.taskStatusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="我的指派" width="110" align="center">
        <template #default="{ row }">{{ row.assignmentStatusLabel }}</template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="250" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">
            详情
          </el-button>
          <el-button
            v-if="canSubmit(row)"
            link
            type="success"
            @click="openSubmit(row)">
            提交完成
          </el-button>
          <el-button
            v-if="canReview(row)"
            link
            type="warning"
            @click="openReview(row)">
            质检
          </el-button>
        </template>
      </el-table-column>
      <template #empty><el-empty description="暂无工作任务" /></template>
    </el-table>
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      @pagination="loadList" />

    <el-dialog
      v-model="createVisible"
      title="新建工作任务"
      width="620px"
      append-to-body
      destroy-on-close>
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="90px">
        <el-form-item label="任务标题" prop="title">
          <el-input
            v-model="createForm.title"
            maxlength="128"
            show-word-limit
            placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务来源" prop="sourceType">
          <el-radio-group v-model="createForm.sourceType">
            <el-radio-button :value="SOURCE_TYPE.MANAGEMENT">
              管理任务
            </el-radio-button>
            <el-radio-button :value="SOURCE_TYPE.PERSONAL">
              个人任务
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="createForm.taskType" style="width: 100%">
            <el-option
              v-for="item in taskTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联仓库">
          <el-select
            v-model="createForm.warehouseId"
            clearable
            filterable
            placeholder="不限定仓库"
            style="width: 100%">
            <el-option
              v-for="item in warehouses"
              :key="warehouseId(item)"
              :label="warehouseLabel(item)"
              :value="warehouseId(item)" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行人">
          <el-select
            v-model="createForm.assigneeUserId"
            :disabled="createForm.sourceType === SOURCE_TYPE.PERSONAL"
            clearable
            filterable
            placeholder="暂不指派"
            style="width: 100%">
            <el-option
              v-for="item in users"
              :key="item.userId"
              :label="item.userName || item.loginName"
              :value="item.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="createForm.dueTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="可选"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="任务内容" prop="content">
          <el-input
            v-model="createForm.content"
            type="textarea"
            :rows="5"
            maxlength="2000"
            show-word-limit
            placeholder="说明需要完成的工作" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="createForm.remark"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">
          创建任务
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="reportVisible"
      title="工作日报"
      width="920px"
      append-to-body
      destroy-on-close>
      <el-form :inline="true" class="report-filter" @submit.prevent>
        <el-form-item label="统计日期">
          <el-date-picker
            v-model="reportDate"
            type="date"
            value-format="YYYY-MM-DD"
            :clearable="false" />
        </el-form-item>
        <el-form-item label="关联仓库">
          <el-select
            v-model="reportWarehouseId"
            clearable
            filterable
            placeholder="全部可见仓库"
            style="width: 210px">
            <el-option
              v-for="item in warehouses"
              :key="warehouseId(item)"
              :label="warehouseLabel(item)"
              :value="warehouseId(item)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :icon="Search"
            :loading="reportLoading"
            @click="loadDailyReport">
            查询
          </el-button>
        </el-form-item>
      </el-form>
      <div class="report-summary-grid">
        <el-card
          v-for="item in reportSummaryItems"
          :key="item.key"
          shadow="never"
          class="report-summary-card">
          <span>{{ item.label }}</span>
          <strong>
            {{ reportSummaryValue(item.key) }}{{ item.suffix || "" }}
          </strong>
        </el-card>
      </div>
      <el-table v-loading="reportLoading" :data="dailyReport.rows" border>
        <el-table-column label="执行人" prop="userName" min-width="150" />
        <el-table-column
          label="新增指派"
          prop="assignedCount"
          width="110"
          align="center" />
        <el-table-column
          label="已完成"
          prop="completedCount"
          width="100"
          align="center" />
        <el-table-column
          label="待处理"
          prop="pendingCount"
          width="100"
          align="center" />
        <el-table-column
          label="已逾期"
          prop="overdueCount"
          width="100"
          align="center" />
        <el-table-column
          label="平均质检分"
          prop="averageScore"
          width="120"
          align="center" />
        <template #empty>
          <el-empty description="当天暂无工作指派记录" :image-size="64" />
        </template>
      </el-table>
      <template #footer>
        <el-button @click="reportVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="detailVisible"
      title="工作任务详情"
      size="min(720px, 94vw)"
      destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border label-width="120">
          <el-descriptions-item label="任务标题" :span="2">
            {{ detail.task.title }}
          </el-descriptions-item>
          <el-descriptions-item label="任务类型">
            {{ detail.task.taskTypeLabel }}
          </el-descriptions-item>
          <el-descriptions-item label="任务来源">
            {{ detail.task.sourceTypeLabel }}
          </el-descriptions-item>
          <el-descriptions-item label="关联仓库">
            {{ detail.task.warehouseName || "不限定仓库" }}
          </el-descriptions-item>
          <el-descriptions-item label="截止时间">
            {{ detail.task.dueTime || "未设置" }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.task)">
              {{ detail.task.taskStatusLabel }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建人">
            {{ detail.task.creatorName || "当前商户用户" }}
          </el-descriptions-item>
          <el-descriptions-item label="任务内容" :span="2">
            <div class="detail-content">{{ detail.task.content }}</div>
          </el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">任务指派</el-divider>
        <el-table :data="detail.assignments" border size="small">
          <el-table-column label="执行人" prop="assigneeName" />
          <el-table-column
            label="状态"
            prop="assignmentStatusLabel"
            width="110" />
          <el-table-column label="已阅时间" prop="readTime" width="165" />
          <el-table-column label="指派时间" prop="assignedTime" width="165" />
        </el-table>
        <el-divider content-position="left">完成提交</el-divider>
        <el-timeline v-if="detail.submissions.length">
          <el-timeline-item
            v-for="item in detail.submissions"
            :key="item.submissionId"
            :timestamp="item.submittedTime"
            placement="top">
            <strong>{{ item.submitterName || "执行人" }}</strong>
            <p>{{ item.completionNote }}</p>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无提交记录" :image-size="60" />
        <el-divider content-position="left">质检记录</el-divider>
        <el-table
          v-if="detail.reviews.length"
          :data="detail.reviews"
          border
          size="small">
          <el-table-column label="质检人" prop="reviewerName" />
          <el-table-column
            label="评分"
            prop="score"
            width="80"
            align="center" />
          <el-table-column label="结果" prop="reviewResultLabel" width="110" />
          <el-table-column
            label="质检意见"
            prop="reviewComment"
            min-width="180"
            show-overflow-tooltip />
          <el-table-column label="时间" prop="createTime" width="165" />
        </el-table>
        <el-empty v-else description="暂无质检记录" :image-size="60" />
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-drawer>

    <el-dialog
      v-model="submitVisible"
      title="提交任务完成说明"
      width="520px"
      append-to-body
      destroy-on-close>
      <el-alert
        v-if="selectedRow"
        :title="selectedRow.title"
        type="info"
        :closable="false"
        class="dialog-alert" />
      <el-form :model="submitForm" label-width="90px">
        <el-form-item label="完成说明" required>
          <el-input
            v-model="submitForm.completionNote"
            type="textarea"
            :rows="5"
            maxlength="2000"
            show-word-limit
            placeholder="请说明实际完成内容" />
        </el-form-item>
        <el-form-item label="凭证引用">
          <el-input
            v-model="submitForm.evidenceJson"
            type="textarea"
            :rows="2"
            maxlength="10000"
            placeholder="可填写文件地址或业务凭证引用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitTask">
          提交审核
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="reviewVisible"
      title="任务质检"
      width="520px"
      append-to-body
      destroy-on-close>
      <el-alert
        v-if="selectedRow"
        :title="selectedRow.title"
        type="info"
        :closable="false"
        class="dialog-alert" />
      <el-form :model="reviewForm" label-width="90px">
        <el-form-item label="质检评分" required>
          <el-input-number
            v-model="reviewForm.score"
            :min="0"
            :max="100"
            :precision="0"
            controls-position="right" />
        </el-form-item>
        <el-form-item label="质检结果" required>
          <el-radio-group v-model="reviewForm.reviewResult">
            <el-radio-button :value="REVIEW_RESULT.PASSED">
              质检通过
            </el-radio-button>
            <el-radio-button :value="REVIEW_RESULT.FAILED">
              未达标
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="质检意见">
          <el-input
            v-model="reviewForm.reviewComment"
            type="textarea"
            :rows="4"
            maxlength="2000"
            show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="reviewTask">
          保存质检
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { DataAnalysis, Plus, Refresh, Search } from "@element-plus/icons-vue";
import { listMerchantWarehouses } from "@/api/merchant/rentalBusiness";
import {
  createWorkTask,
  getWorkDailyReport,
  getWorkSummary,
  getWorkTask,
  listWorkTasks,
  listWorkUsers,
  markWorkTaskRead,
  reviewWorkTask,
  submitWorkTask,
} from "@/api/merchant/work";

const TASK_STATUS = Object.freeze({
  PENDING: 0,
  PROCESSING: 1,
  WAITING_REVIEW: 2,
  COMPLETED: 3,
  OVERDUE: 4,
  CLOSED: 5,
});
const SOURCE_TYPE = Object.freeze({ MANAGEMENT: 1, PERSONAL: 2 });
const REVIEW_RESULT = Object.freeze({ PASSED: 1, FAILED: 2 });
const loading = ref(false);
const submitting = ref(false);
const actionLoading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const rows = ref([]);
const warehouses = ref([]);
const users = ref([]);
const summary = ref({});
const createVisible = ref(false);
const reportVisible = ref(false);
const detailVisible = ref(false);
const submitVisible = ref(false);
const reviewVisible = ref(false);
const detail = ref(null);
const selectedRow = ref(null);
const reportLoading = ref(false);
const reportDate = ref(formatReportDate(new Date()));
const reportWarehouseId = ref();
const dailyReport = ref({ reportDate: "", summary: {}, rows: [] });
const createFormRef = ref();
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  warehouseId: undefined,
  status: undefined,
});
const createForm = reactive({
  title: "",
  content: "",
  sourceType: SOURCE_TYPE.MANAGEMENT,
  taskType: 9,
  warehouseId: undefined,
  assigneeUserId: undefined,
  dueTime: undefined,
  remark: "",
});
const submitForm = reactive({ completionNote: "", evidenceJson: "" });
const reviewForm = reactive({
  score: 100,
  reviewResult: REVIEW_RESULT.PASSED,
  reviewComment: "",
});
const taskStatusOptions = [
  { value: TASK_STATUS.PENDING, label: "待处理" },
  { value: TASK_STATUS.PROCESSING, label: "处理中" },
  { value: TASK_STATUS.WAITING_REVIEW, label: "待审核" },
  { value: TASK_STATUS.COMPLETED, label: "已完成" },
  { value: TASK_STATUS.OVERDUE, label: "已逾期" },
  { value: TASK_STATUS.CLOSED, label: "已关闭" },
];
const taskTypeOptions = [
  { value: 1, label: "订单履约" },
  { value: 2, label: "库存管理" },
  { value: 3, label: "售后处理" },
  { value: 9, label: "其他" },
];
const summaryItems = [
  { key: "totalCount", label: "全部任务" },
  { key: "pendingCount", label: "待处理", className: "warning-value" },
  { key: "processingCount", label: "处理中" },
  { key: "reviewCount", label: "待审核", className: "warning-value" },
  { key: "overdueCount", label: "已逾期", className: "danger-value" },
  { key: "completedCount", label: "已完成", className: "success-value" },
];
const reportSummaryItems = [
  { key: "assignedCount", label: "新增指派", suffix: " 项" },
  { key: "completedCount", label: "已完成", suffix: " 项" },
  { key: "pendingCount", label: "待处理", suffix: " 项" },
  { key: "overdueCount", label: "已逾期", suffix: " 项" },
  { key: "averageScore", label: "平均质检分", suffix: " 分" },
  { key: "completionRate", label: "完成率", suffix: "%" },
];
const createRules = computed(() => ({
  title: [{ required: true, message: "请输入任务标题", trigger: "blur" }],
  content: [{ required: true, message: "请输入任务内容", trigger: "blur" }],
}));

function unwrap(response) {
  return response?.data ?? response;
}
function rowsOf(response) {
  const data = unwrap(response);
  return Array.isArray(data) ? data : data?.rows || [];
}
function errorText(error) {
  return error?.msg || error?.message || "操作失败，请稍后重试";
}
function warehouseId(item) {
  return item?.warehouseId ?? item?.id;
}
function warehouseLabel(item) {
  const name = item?.warehouseName ?? item?.name ?? "仓库";
  const code = item?.warehouseCode ?? item?.code;
  return code ? `${name}（${code}）` : name;
}
function formatReportDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}
function reportSummaryValue(key) {
  return dailyReport.value.summary?.[key] ?? 0;
}
function statusType(row) {
  const value = Number(row?.overdueFlag)
    ? TASK_STATUS.OVERDUE
    : Number(row?.taskStatus);
  return (
    {
      0: "warning",
      1: "primary",
      2: "warning",
      3: "success",
      4: "danger",
      5: "info",
    }[value] || "info"
  );
}
function canSubmit(row) {
  return [0, 1, 4].includes(Number(row?.assignmentStatus));
}
function canReview(row) {
  return Number(row?.taskStatus) === TASK_STATUS.WAITING_REVIEW;
}
async function loadList() {
  loading.value = true;
  try {
    const result = unwrap(await listWorkTasks(query));
    rows.value = result?.rows || [];
    total.value = Number(result?.total || 0);
  } catch (error) {
    ElMessage.error(errorText(error));
  } finally {
    loading.value = false;
  }
}
async function loadBaseData() {
  try {
    const [warehouseResult, userResult, summaryResult] = await Promise.all([
      listMerchantWarehouses({ pageNum: 1, pageSize: 1000, status: 0 }),
      listWorkUsers(),
      getWorkSummary(),
    ]);
    warehouses.value = rowsOf(warehouseResult);
    users.value = rowsOf(userResult);
    summary.value = unwrap(summaryResult) || {};
  } catch (error) {
    ElMessage.error(errorText(error));
  }
}
async function loadDailyReport() {
  reportLoading.value = true;
  try {
    const result = unwrap(
      await getWorkDailyReport({
        reportDate: reportDate.value,
        warehouseId: reportWarehouseId.value,
      }),
    );
    dailyReport.value = {
      reportDate: result?.reportDate || reportDate.value,
      summary: result?.summary || {},
      rows: result?.rows || [],
    };
  } catch (error) {
    ElMessage.error(errorText(error));
  } finally {
    reportLoading.value = false;
  }
}
async function refresh() {
  await Promise.all([loadList(), loadBaseData()]);
}
function search() {
  query.pageNum = 1;
  loadList();
}
function reset() {
  Object.assign(query, {
    pageNum: 1,
    pageSize: 10,
    keyword: "",
    warehouseId: undefined,
    status: undefined,
  });
  loadList();
}
function openCreate() {
  Object.assign(createForm, {
    title: "",
    content: "",
    sourceType: SOURCE_TYPE.MANAGEMENT,
    taskType: 9,
    warehouseId: undefined,
    assigneeUserId: undefined,
    dueTime: undefined,
    remark: "",
  });
  createVisible.value = true;
}
function openReport() {
  reportDate.value = formatReportDate(new Date());
  reportWarehouseId.value = undefined;
  reportVisible.value = true;
  loadDailyReport();
}
async function submitCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false);
  if (!valid) return;
  submitting.value = true;
  try {
    await createWorkTask({
      ...createForm,
      assigneeUserId:
        createForm.sourceType === SOURCE_TYPE.PERSONAL
          ? undefined
          : createForm.assigneeUserId,
    });
    ElMessage.success("任务已创建");
    createVisible.value = false;
    await refresh();
  } catch (error) {
    ElMessage.error(errorText(error));
  } finally {
    submitting.value = false;
  }
}
async function openDetail(row) {
  try {
    detail.value = unwrap(await getWorkTask(row.taskId));
    detailVisible.value = true;
    if (canSubmit(row)) await markWorkTaskRead(row.taskId).catch(() => {});
  } catch (error) {
    ElMessage.error(errorText(error));
  }
}
function openSubmit(row) {
  selectedRow.value = row;
  Object.assign(submitForm, { completionNote: "", evidenceJson: "" });
  submitVisible.value = true;
}
async function submitTask() {
  if (!submitForm.completionNote.trim())
    return ElMessage.warning("请填写完成说明");
  actionLoading.value = true;
  try {
    await submitWorkTask(selectedRow.value.taskId, { ...submitForm });
    ElMessage.success("已提交审核");
    submitVisible.value = false;
    await refresh();
  } catch (error) {
    ElMessage.error(errorText(error));
  } finally {
    actionLoading.value = false;
  }
}
function openReview(row) {
  selectedRow.value = row;
  Object.assign(reviewForm, {
    score: 100,
    reviewResult: REVIEW_RESULT.PASSED,
    reviewComment: "",
  });
  reviewVisible.value = true;
}
async function reviewTask() {
  actionLoading.value = true;
  try {
    await reviewWorkTask(selectedRow.value.taskId, { ...reviewForm });
    ElMessage.success("质检结果已保存");
    reviewVisible.value = false;
    await refresh();
  } catch (error) {
    ElMessage.error(errorText(error));
  } finally {
    actionLoading.value = false;
  }
}

onMounted(async () => {
  await loadBaseData();
  await loadList();
});
</script>

<style scoped>
.merchant-work-page {
  min-height: 100%;
}
.page-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}
.page-kicker {
  margin: 0 0 4px;
  color: var(--el-color-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
}
.page-heading h2 {
  margin: 0;
}
.page-description {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
}
.summary-grid {
  margin-bottom: 16px;
}
.summary-card {
  min-height: 92px;
}
.summary-card span,
.summary-card strong {
  display: block;
}
.summary-card span {
  color: var(--el-text-color-secondary);
}
.summary-card strong {
  margin-top: 10px;
  font-size: 28px;
  line-height: 1;
}
.warning-value {
  color: var(--el-color-warning);
}
.danger-value {
  color: var(--el-color-danger);
}
.success-value {
  color: var(--el-color-success);
}
.secondary-text {
  display: block;
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.detail-content {
  white-space: pre-wrap;
  line-height: 1.7;
}
.dialog-alert {
  margin-bottom: 16px;
}
.report-filter {
  margin-bottom: 8px;
}
.report-summary-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}
.report-summary-card {
  min-height: 78px;
}
.report-summary-card span,
.report-summary-card strong {
  display: block;
}
.report-summary-card span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.report-summary-card strong {
  margin-top: 8px;
  font-size: 22px;
  line-height: 1.2;
}
@media (max-width: 900px) {
  .report-summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
@media (max-width: 560px) {
  .report-summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
