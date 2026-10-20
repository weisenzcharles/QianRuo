<template>
  <div class="app-container after-sales-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <div>
            <h2>售后单管理</h2>
            <span>处理订单归还验机和租赁中的异常问题</span>
          </div>
          <el-button :icon="Refresh" :loading="loading" @click="loadList">
            刷新
          </el-button>
        </div>
      </template>

      <el-form :model="queryParams" :inline="true" class="filter-form" @submit.prevent>
        <el-form-item label="售后单或订单">
          <el-input
            v-model="queryParams.keyword"
            clearable
            placeholder="输入售后单号、订单编码或问题描述"
            @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="售后状态">
          <el-select v-model="queryParams.caseStatus" clearable placeholder="全部状态">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="rows">
        <el-table-column label="售后单号" prop="caseNo" min-width="170" />
        <el-table-column label="订单编码" prop="orderNo" min-width="160" />
        <el-table-column label="售后类型" min-width="100">
          <template #default="{ row }">
            {{ row.caseTypeLabel || typeLabel(row.caseType) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.caseStatus === 0 ? 'warning' : 'success'" effect="plain">
              {{ row.caseStatusLabel || statusLabel(row.caseStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="问题描述" prop="problemDesc" min-width="240" show-overflow-tooltip />
        <el-table-column label="赔付金额" min-width="110" align="right">
          <template #default="{ row }">
            {{ row.compensationAmount == null ? "-" : `¥${row.compensationAmount}` }}
          </template>
        </el-table-column>
        <el-table-column label="平台工单" min-width="140">
          <template #default="{ row }">
            {{ row.platformTicketNo || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" min-width="165" />
        <el-table-column label="完成时间" prop="finishTime" min-width="165">
          <template #default="{ row }">{{ row.finishTime || "-" }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.caseStatus === 0"
              link
              type="success"
              @click="completeCase(row)">
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="loadList" />
    </el-card>

    <el-dialog v-model="detailVisible" title="售后单详情" width="820px" destroy-on-close>
      <el-tabs v-if="detail" v-model="detailTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border label-width="120">
            <el-descriptions-item label="售后单号">{{ detail.caseNo }}</el-descriptions-item>
            <el-descriptions-item label="订单编码">{{ detail.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="售后类型">
              {{ detail.caseTypeLabel || typeLabel(detail.caseType) }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              {{ detail.caseStatusLabel || statusLabel(detail.caseStatus) }}
            </el-descriptions-item>
            <el-descriptions-item label="问题描述" :span="2">
              {{ detail.problemDesc || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="赔付金额">
              {{ detail.compensationAmount == null ? "-" : `¥${detail.compensationAmount}` }}
            </el-descriptions-item>
            <el-descriptions-item label="平台工单">
              <el-tag v-if="detail.platformTicketNo" type="success" effect="plain">
                {{ detail.platformTicketNo }}
              </el-tag>
              <el-button
                v-else-if="detail.caseStatus === 0"
                link
                type="primary"
                :loading="platformTicketSubmitting"
                @click="requestPlatformTicket">
                申请平台介入
              </el-button>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="联系客户">
              {{ detail.customerContacted === 1 ? "是" : "否" }}
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">
              {{ detail.remark || "-" }}
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="处理记录" name="events">
          <el-timeline v-if="eventRows.length" class="after-sales-timeline">
            <el-timeline-item
              v-for="(item, index) in eventRows"
              :key="`${item.createTime || 'event'}-${index}`"
              :timestamp="item.createTime"
              placement="top">
              <div class="timeline-title">
                {{ item.eventTypeLabel || "处理记录" }}
                <span>{{ item.operatorName || "系统" }}</span>
              </div>
              <div class="timeline-content">{{ item.content || "-" }}</div>
              <div v-if="item.fromStatusLabel || item.toStatusLabel" class="timeline-status">
                {{ item.fromStatusLabel || "新建" }} → {{ item.toStatusLabel || "-" }}
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无处理记录" :image-size="72" />
          <el-form
            v-if="detail.caseStatus === 0"
            :model="eventForm"
            label-width="82px"
            class="after-sales-entry-form">
            <el-form-item label="处理记录" required>
              <el-input
                v-model="eventForm.content"
                type="textarea"
                :rows="3"
                maxlength="2000"
                show-word-limit
                placeholder="记录联系客户、核验结果或处理进展" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="eventSubmitting" @click="submitEvent">
                添加记录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="证据材料" name="evidence">
          <el-form
            v-if="detail.caseStatus === 0"
            :model="evidenceForm"
            label-width="82px"
            class="after-sales-entry-form">
            <el-form-item label="证据类型" required>
              <el-select v-model="evidenceForm.evidenceType" placeholder="请选择证据类型">
                <el-option
                  v-for="item in evidenceTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="证据文件" required>
              <FileUpload
                v-model="evidenceForm.fileUrl"
                business-type="after-sales"
                :limit="1"
                :file-size="20"
                :file-type="['jpg', 'jpeg', 'png', 'mp4', 'pdf', 'doc', 'docx']"
                :is-show-tip="false" />
            </el-form-item>
            <el-form-item label="证据说明">
              <el-input
                v-model="evidenceForm.description"
                maxlength="1000"
                show-word-limit
                placeholder="补充说明证据内容" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="evidenceSubmitting" @click="submitEvidence">
                保存证据
              </el-button>
            </el-form-item>
          </el-form>
          <el-table v-if="evidenceRows.length" :data="evidenceRows" size="small">
            <el-table-column label="类型" width="90">
              <template #default="{ row }">{{ row.evidenceTypeLabel || evidenceTypeLabel(row.evidenceType) }}</template>
            </el-table-column>
            <el-table-column label="文件" min-width="220">
              <template #default="{ row }">
                <el-link :href="row.fileUrl" target="_blank" type="primary">
                  {{ row.fileName || row.fileUrl }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column label="说明" prop="description" min-width="180" show-overflow-tooltip />
            <el-table-column label="上传人" prop="operatorName" width="100" />
            <el-table-column label="时间" prop="createTime" width="165" />
          </el-table>
          <el-empty v-else description="暂无证据材料" :image-size="72" />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MerchantAfterSales">
import { reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import {
  addMerchantAfterSalesEvidence,
  addMerchantAfterSalesEvent,
  completeMerchantAfterSales,
  getMerchantAfterSales,
  listMerchantAfterSalesEvidence,
  listMerchantAfterSalesEvents,
  listMerchantAfterSales,
  requestMerchantAfterSalesPlatformTicket,
} from "@/api/merchant/afterSales";

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const detailVisible = ref(false);
const detail = ref(null);
const detailTab = ref("basic");
const eventRows = ref([]);
const evidenceRows = ref([]);
const eventSubmitting = ref(false);
const evidenceSubmitting = ref(false);
const platformTicketSubmitting = ref(false);
const eventForm = reactive({ content: "" });
const evidenceForm = reactive({
  evidenceType: 1,
  fileUrl: "",
  description: "",
});
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  caseStatus: undefined,
});
const statusOptions = [
  { value: 0, label: "处理中" },
  { value: 1, label: "已完成" },
  { value: 2, label: "已关闭" },
];
const typeOptions = [
  { value: 1, label: "损坏" },
  { value: 2, label: "丢失" },
  { value: 3, label: "赔付" },
  { value: 9, label: "其他" },
];
const evidenceTypeOptions = [
  { value: 1, label: "图片" },
  { value: 2, label: "视频" },
  { value: 3, label: "文件" },
  { value: 9, label: "其他" },
];

function pageData(response) {
  const body = response?.data ?? response;
  const page = body?.data ?? body;
  return {
    rows: Array.isArray(page) ? page : page?.rows || [],
    total: Number(page?.total || 0),
  };
}

function loadList() {
  loading.value = true;
  listMerchantAfterSales(queryParams)
    .then((response) => {
      const page = pageData(response);
      rows.value = page.rows;
      total.value = page.total;
    })
    .finally(() => {
      loading.value = false;
    });
}

function handleQuery() {
  queryParams.pageNum = 1;
  loadList();
}

function resetQuery() {
  queryParams.keyword = "";
  queryParams.caseStatus = undefined;
  handleQuery();
}

function typeLabel(value) {
  return typeOptions.find((item) => item.value === Number(value))?.label || "未知";
}

function statusLabel(value) {
  return statusOptions.find((item) => item.value === Number(value))?.label || "未知";
}

function evidenceTypeLabel(value) {
  return evidenceTypeOptions.find((item) => item.value === Number(value))?.label || "未知";
}

function listData(response) {
  const body = response?.data ?? response;
  return Array.isArray(body) ? body : [];
}

async function openDetail(row) {
  const [detailResponse, eventResponse, evidenceResponse] = await Promise.all([
    getMerchantAfterSales(row.caseNo),
    listMerchantAfterSalesEvents(row.caseNo),
    listMerchantAfterSalesEvidence(row.caseNo),
  ]);
  detail.value = detailResponse?.data ?? detailResponse;
  eventRows.value = listData(eventResponse);
  evidenceRows.value = listData(evidenceResponse);
  detailTab.value = "basic";
  eventForm.content = "";
  evidenceForm.evidenceType = 1;
  evidenceForm.fileUrl = "";
  evidenceForm.description = "";
  detailVisible.value = true;
}

async function submitEvent() {
  const content = eventForm.content.trim();
  if (!content || !detail.value) {
    ElMessage.warning("请填写处理记录");
    return;
  }
  eventSubmitting.value = true;
  try {
    await addMerchantAfterSalesEvent(detail.value.caseNo, { content });
    eventForm.content = "";
    eventRows.value = listData(await listMerchantAfterSalesEvents(detail.value.caseNo));
    ElMessage.success("处理记录已添加");
  } finally {
    eventSubmitting.value = false;
  }
}

async function submitEvidence() {
  if (!detail.value || !evidenceForm.fileUrl) {
    ElMessage.warning("请先上传证据文件");
    return;
  }
  evidenceSubmitting.value = true;
  try {
    await addMerchantAfterSalesEvidence(detail.value.caseNo, {
      evidenceType: evidenceForm.evidenceType,
      fileUrl: evidenceForm.fileUrl,
      description: evidenceForm.description.trim(),
    });
    evidenceForm.fileUrl = "";
    evidenceForm.description = "";
    evidenceRows.value = listData(await listMerchantAfterSalesEvidence(detail.value.caseNo));
    eventRows.value = listData(await listMerchantAfterSalesEvents(detail.value.caseNo));
    ElMessage.success("证据已保存");
  } finally {
    evidenceSubmitting.value = false;
  }
}

async function requestPlatformTicket() {
  if (!detail.value) {
    return;
  }
  await ElMessageBox.confirm("确认申请平台介入处理此售后单吗？", "操作确认", {
    type: "warning",
  });
  platformTicketSubmitting.value = true;
  try {
    const response = await requestMerchantAfterSalesPlatformTicket(detail.value.caseNo);
    const ticket = response?.data ?? response;
    detail.value.platformTicketNo = ticket?.ticketNo || detail.value.platformTicketNo;
    ElMessage.success(
      ticket?.ticketNo ? `平台工单 ${ticket.ticketNo} 已建立` : "平台工单已建立",
    );
    loadList();
  } finally {
    platformTicketSubmitting.value = false;
  }
}

async function completeCase(row) {
  await ElMessageBox.confirm(`确认完成售后单“${row.caseNo}”吗？`, "操作确认", {
    type: "warning",
  });
  await completeMerchantAfterSales(row.caseNo, { remark: "售后处理完成" });
  ElMessage.success("售后单已完成");
  loadList();
}

loadList();
</script>

<style scoped>
.after-sales-page {
  min-width: 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-header h2 {
  margin: 0 0 6px;
  font-size: 20px;
}

.page-header span {
  color: var(--el-text-color-secondary);
}

.filter-form {
  margin-bottom: 16px;
}

.after-sales-timeline {
  padding: 8px 12px 0;
}

.timeline-title {
  font-weight: 600;
}

.timeline-title span,
.timeline-status {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 400;
}

.timeline-content {
  margin-top: 6px;
  white-space: pre-wrap;
}

.after-sales-entry-form {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>
