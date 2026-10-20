<template>
  <div class="app-container merchant-finance-page">
    <PageHeading
      kicker="FINANCE CENTER"
      :title="pageMeta.title"
      :description="pageMeta.description">
      <template #aside>
        <el-button :icon="Refresh" @click="loadPage">刷新</el-button>
      </template>
    </PageHeading>

    <el-tabs
      v-model="pageKey"
      class="finance-tabs"
      @tab-change="navigateToPage">
      <el-tab-pane
        v-for="item in pageItems"
        :key="item.key"
        :label="item.label"
        :name="item.key" />
    </el-tabs>

    <template v-if="pageKey === 'overview'">
      <el-row v-loading="loading" :gutter="16" class="summary-grid">
        <el-col v-for="item in overviewCards" :key="item.key" :xs="24" :sm="12" :lg="8">
          <el-card class="summary-card" shadow="never">
            <div class="summary-label">{{ item.label }}</div>
            <div class="summary-value" :class="item.tone">{{ item.value }}</div>
            <div class="summary-help">{{ item.help }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="content-grid">
        <el-col :xs="24" :lg="14">
          <el-card class="finance-panel" shadow="never">
            <template #header>
              <div class="panel-heading">
                <span>最近资金流水</span>
                <el-button link type="primary" @click="navigateToPage('ledger')">查看全部</el-button>
              </div>
            </template>
            <FinanceLedgerTable :rows="overview.recentLedgers || []" compact @detail="openLedgerDetail" />
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="10">
          <el-card class="finance-panel" shadow="never">
            <template #header><div class="panel-heading"><span>待处理事项</span><span class="panel-note">按当前权限显示</span></div></template>
            <div class="task-list">
              <div class="task-row">
                <span>待开票流水</span>
                <strong class="warning-text">{{ overview.pendingInvoiceCount || 0 }}</strong>
                <el-button link type="primary" @click="navigateToPage('invoice')">查看</el-button>
              </div>
              <div class="task-row">
                <span>提现流水</span>
                <strong>{{ overview.pendingWithdrawalCount || 0 }}</strong>
                <el-button link type="primary" @click="navigateToPage('withdrawal')">查看</el-button>
              </div>
              <div class="task-row">
                <span>异常流水</span>
                <strong class="danger-text">{{ overview.abnormalLedgerCount || 0 }}</strong>
                <el-button link type="primary" @click="navigateToPage('ledger')">查看</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <template v-else-if="pageKey === 'accounts'">
      <div class="accounts-toolbar">
        <div>
          <h3>账户余额</h3>
          <p>账户充值通过支付宝完成，到账后自动记录为收入流水。</p>
        </div>
        <el-button type="primary" @click="openRecharge">账户充值</el-button>
      </div>
      <el-alert
        title="账户余额按资金性质独立核算，冻结和解冻会追加新的资金流水。"
        type="info"
        :closable="false"
        show-icon
        class="finance-alert" />
      <el-row v-loading="loading" :gutter="16" class="account-grid">
        <el-col v-for="account in accounts" :key="account.accountType" :xs="24" :sm="12" :lg="6">
          <el-card class="account-card" shadow="never">
            <div class="account-card__heading">
              <span>{{ account.accountName }}</span>
              <el-tag size="small" type="success" effect="plain">{{ account.statusName || '正常' }}</el-tag>
            </div>
            <div class="account-card__amount">{{ money(account.availableAmount) }}</div>
            <div class="account-card__line">可用余额</div>
            <div class="account-card__line">冻结余额：{{ money(account.frozenAmount) }}</div>
            <div class="account-card__help">{{ account.description }}</div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && accounts.length === 0" description="暂无账户余额数据" />
    </template>

    <template v-else-if="pageKey === 'settlement'">
      <el-card class="finance-panel" shadow="never">
        <template #header><div class="panel-heading"><span>联盟结算</span><span class="panel-note">结算确认后进入租金 / 分账资金</span></div></template>
        <div class="filter-bar">
          <el-select v-model="settlementQuery.status" clearable placeholder="全部状态" @change="loadPage" style="width: 150px">
            <el-option v-for="item in settlementStatuses" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-date-picker v-model="settlementQuery.range" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="loadPage" />
        </div>
        <el-table v-loading="loading" :data="settlements" border>
          <el-table-column label="订单编码" prop="orderNo" min-width="170" show-overflow-tooltip />
          <el-table-column label="原订单" prop="sourceOrderNo" min-width="170" show-overflow-tooltip />
          <el-table-column label="子订单" prop="childOrderNo" min-width="170" show-overflow-tooltip />
          <el-table-column label="结算角色" prop="roleName" width="110" align="center" />
          <el-table-column label="结算金额" prop="settlementAmount" width="120" align="right"><template #default="{ row }">{{ money(row.settlementAmount) }}</template></el-table-column>
          <el-table-column label="快递金额" prop="expressAmount" width="120" align="right"><template #default="{ row }">{{ moneyOrDash(row.expressAmount) }}</template></el-table-column>
          <el-table-column label="赔付金额" prop="compensationAmount" width="120" align="right"><template #default="{ row }">{{ moneyOrDash(row.compensationAmount) }}</template></el-table-column>
          <el-table-column label="状态" prop="settlementStatusName" width="110" align="center"><template #default="{ row }"><el-tag :type="settlementTagType(row.settlementStatus)">{{ row.settlementStatusName || '未知状态' }}</el-tag></template></el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="170" />
        </el-table>
        <el-empty v-if="!loading && settlements.length === 0" description="暂无联盟结算记录" />
      </el-card>
    </template>

    <template v-else-if="pageKey === 'withdrawal'">
      <el-card class="finance-panel" shadow="never">
        <template #header>
          <div class="panel-heading">
            <div>
              <span>提现申请</span>
              <span class="panel-note">申请后先冻结可用余额，平台审核通过后完成出款</span>
            </div>
            <el-button type="primary" @click="openWithdrawalApply">申请提现</el-button>
          </div>
        </template>
        <div class="filter-bar">
          <el-select v-model="withdrawalQuery.status" clearable placeholder="全部状态" style="width: 150px" @change="loadWithdrawals">
            <el-option v-for="item in withdrawalStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button :icon="Refresh" @click="loadWithdrawals">刷新</el-button>
        </div>
        <el-table v-loading="loading" :data="withdrawals" border>
          <el-table-column label="提现申请号" prop="withdrawalNo" min-width="210" show-overflow-tooltip />
          <el-table-column label="申请金额" prop="amount" width="125" align="right"><template #default="{ row }">{{ money(row.amount) }}</template></el-table-column>
          <el-table-column label="实际到账" prop="actualAmount" width="125" align="right"><template #default="{ row }">{{ money(row.actualAmount) }}</template></el-table-column>
          <el-table-column label="提现渠道" prop="withdrawalChannelName" width="110" />
          <el-table-column label="收款人" prop="payeeName" width="120" />
          <el-table-column label="收款账户" prop="payeeAccount" min-width="150" />
          <el-table-column label="状态" prop="withdrawalStatusName" width="105" align="center"><template #default="{ row }"><el-tag :type="withdrawalTagType(row.withdrawalStatus)">{{ row.withdrawalStatusName || '未知状态' }}</el-tag></template></el-table-column>
          <el-table-column label="审核备注" prop="reviewRemark" min-width="170" show-overflow-tooltip><template #default="{ row }">{{ row.reviewRemark || '-' }}</template></el-table-column>
          <el-table-column label="申请时间" prop="createTime" width="170" />
        </el-table>
        <pagination v-show="withdrawalTotal > 0" :total="withdrawalTotal" v-model:page="withdrawalQuery.pageNum" v-model:limit="withdrawalQuery.pageSize" @pagination="loadWithdrawals" />
        <el-empty v-if="!loading && withdrawals.length === 0" description="暂无提现申请" />
      </el-card>
    </template>

    <template v-else-if="pageKey === 'invoice'">
      <el-card class="finance-panel" shadow="never">
        <template #header>
          <div class="panel-heading">
            <div><span>开票申请</span><span class="panel-note">成功收入流水一笔只能保留一张有效发票</span></div>
            <el-button type="primary" @click="openInvoiceCreate">申请开票</el-button>
          </div>
        </template>
        <div class="filter-bar">
          <el-select v-model="invoiceApplicationQuery.status" clearable placeholder="全部状态" style="width: 150px" @change="loadInvoiceApplications">
            <el-option v-for="item in invoiceApplicationStatuses" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="invoiceApplicationQuery.applicationType" clearable placeholder="全部类型" style="width: 150px" @change="loadInvoiceApplications">
            <el-option v-for="item in invoiceApplicationTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button :icon="Refresh" @click="loadInvoiceApplications">刷新</el-button>
        </div>
        <el-table v-loading="loading" :data="invoiceApplications" border>
          <el-table-column label="申请编号" prop="applicationNo" min-width="215" show-overflow-tooltip />
          <el-table-column label="流水号" prop="ledgerNo" min-width="180" show-overflow-tooltip />
          <el-table-column label="申请金额" prop="amount" width="125" align="right"><template #default="{ row }">{{ money(row.amount) }}</template></el-table-column>
          <el-table-column label="申请类型" prop="applicationTypeName" width="100" />
          <el-table-column label="发票抬头" prop="invoiceTitle" min-width="180" show-overflow-tooltip />
          <el-table-column label="状态" prop="applicationStatusName" width="105" align="center"><template #default="{ row }"><el-tag :type="invoiceApplicationTagType(row.applicationStatus)">{{ row.applicationStatusName || '-' }}</el-tag></template></el-table-column>
          <el-table-column label="申请时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="80" fixed="right" align="center"><template #default="{ row }"><el-button link type="primary" @click="openInvoiceDetail(row)">详情</el-button></template></el-table-column>
        </el-table>
        <pagination v-show="invoiceApplicationTotal > 0" :total="invoiceApplicationTotal" v-model:page="invoiceApplicationQuery.pageNum" v-model:limit="invoiceApplicationQuery.pageSize" @pagination="loadInvoiceApplications" />
        <el-empty v-if="!loading && invoiceApplications.length === 0" description="暂无开票申请" />
      </el-card>
    </template>

    <template v-else>
      <el-card class="finance-panel" shadow="never">
        <template #header>
          <div class="panel-heading">
            <span>{{ pageMeta.tableTitle }}</span>
            <el-button :icon="Refresh" @click="loadPage">刷新</el-button>
          </div>
        </template>
        <div class="filter-bar">
          <el-input v-model="query.ledgerNo" clearable placeholder="流水号" style="width: 190px" @keyup.enter="loadPage" />
          <el-select v-model="query.accountType" clearable placeholder="全部资金账户" style="width: 165px" @change="loadPage">
            <el-option v-for="item in accountTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="query.direction" clearable placeholder="全部方向" style="width: 130px" @change="loadPage">
            <el-option v-for="item in directions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="query.ledgerStatus" clearable placeholder="全部状态" style="width: 130px" @change="loadPage">
            <el-option v-for="item in ledgerStatuses" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-date-picker v-model="query.range" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="loadPage" />
          <el-button type="primary" :icon="Search" @click="loadPage">查询</el-button>
        </div>
        <el-alert v-if="pageKey === 'invoice'" title="当前页面展示已进入财务流水的发票状态，申请开票后续将关联订单和发票抬头。" type="info" :closable="false" show-icon class="finance-alert" />
        <FinanceLedgerTable v-loading="loading" :rows="rows" :show-reference="pageKey === 'shipping' || pageKey === 'book'" @detail="openLedgerDetail" />
        <pagination v-show="total > 0" :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="loadPage" />
        <el-empty v-if="!loading && rows.length === 0" :description="pageMeta.emptyText" />
      </el-card>
    </template>

    <el-dialog v-model="detailVisible" title="资金流水详情" width="680px" append-to-body destroy-on-close>
      <el-descriptions v-loading="detailLoading" :column="2" border label-width="120">
        <el-descriptions-item label="流水号">{{ detail.ledgerNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资金账户">{{ detail.accountTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ detail.bizTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流水方向">{{ detail.directionName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ money(detail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="变更后余额">{{ money(detail.balanceAfter) }}</el-descriptions-item>
        <el-descriptions-item label="关联类型">{{ detail.refType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付渠道">{{ detail.payChannel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付单号">{{ detail.payOrderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票状态">{{ detail.invoiceStatusName || '未申请' }}</el-descriptions-item>
        <el-descriptions-item label="流水状态">{{ detail.ledgerStatusName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="原流水">{{ detail.originalLedgerNo || '无' }}</el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ detail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="rechargeVisible" title="账户充值" width="440px" append-to-body destroy-on-close>
      <el-form ref="rechargeFormRef" :model="rechargeForm" :rules="rechargeRules" label-width="90px">
        <el-form-item label="充值金额" prop="amount">
          <el-input-number
            v-model="rechargeForm.amount"
            :min="0.01"
            :max="1000000"
            :precision="2"
            :step="100"
            controls-position="right"
            class="recharge-amount-input" />
          <span class="recharge-unit">元</span>
        </el-form-item>
      </el-form>
      <el-alert
        title="提交后将在新窗口打开支付宝收银台，支付成功后余额会自动入账。"
        type="info"
        :closable="false"
        show-icon />
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="rechargeLoading" @click="submitRecharge">去支付</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="withdrawalApplyVisible" title="申请提现" width="520px" append-to-body destroy-on-close>
      <el-form ref="withdrawalFormRef" :model="withdrawalForm" :rules="withdrawalRules" label-width="95px">
        <el-form-item label="提现金额" prop="amount">
          <el-input-number v-model="withdrawalForm.amount" :min="0.01" :max="1000000" :precision="2" :step="100" controls-position="right" class="recharge-amount-input" />
          <span class="recharge-unit">元</span>
        </el-form-item>
        <el-form-item label="提现渠道" prop="withdrawalChannel">
          <el-select v-model="withdrawalForm.withdrawalChannel" placeholder="请选择提现渠道" style="width: 100%">
            <el-option v-for="item in withdrawalChannelOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款人" prop="payeeName"><el-input v-model="withdrawalForm.payeeName" maxlength="128" show-word-limit /></el-form-item>
        <el-form-item label="收款账户" prop="payeeAccount"><el-input v-model="withdrawalForm.payeeAccount" maxlength="256" show-word-limit /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="withdrawalForm.remark" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <el-alert title="提交后金额会立即冻结，审核拒绝时自动解冻。" type="info" :closable="false" show-icon />
      <template #footer>
        <el-button @click="withdrawalApplyVisible = false">取消</el-button>
        <el-button type="primary" :loading="withdrawalApplyLoading" @click="submitWithdrawal">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="invoiceCreateVisible" title="申请开票" width="560px" append-to-body destroy-on-close>
      <el-form ref="invoiceCreateFormRef" :model="invoiceCreateForm" :rules="invoiceCreateRules" label-width="100px">
        <el-form-item label="财务流水号" prop="ledgerNo"><el-input v-model="invoiceCreateForm.ledgerNo" placeholder="请输入成功收入流水号" /></el-form-item>
        <el-form-item label="发票类型" prop="invoiceType"><el-select v-model="invoiceCreateForm.invoiceType" style="width: 100%"><el-option label="企业发票" :value="1" /><el-option label="个人发票" :value="2" /></el-select></el-form-item>
        <el-form-item label="发票抬头" prop="invoiceTitle"><el-input v-model="invoiceCreateForm.invoiceTitle" maxlength="256" show-word-limit /></el-form-item>
        <el-form-item v-if="invoiceCreateForm.invoiceType === 1" label="纳税人识别号" prop="taxNo"><el-input v-model="invoiceCreateForm.taxNo" maxlength="64" /></el-form-item>
        <el-form-item label="接收邮箱" prop="invoiceEmail"><el-input v-model="invoiceCreateForm.invoiceEmail" maxlength="128" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="invoiceCreateVisible = false">取消</el-button>
        <el-button type="primary" :loading="invoiceCreateLoading" @click="submitInvoiceCreate">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="invoiceDetailVisible" title="开票申请详情" width="680px" append-to-body destroy-on-close>
      <el-descriptions v-loading="detailLoading" :column="2" border label-width="120">
        <el-descriptions-item label="申请编号">{{ invoiceDetail.applicationNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流水号">{{ invoiceDetail.ledgerNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">{{ money(invoiceDetail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="申请类型">{{ invoiceDetail.applicationTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ invoiceDetail.applicationStatusName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票类型">{{ invoiceDetail.invoiceType === 1 ? '企业发票' : '个人发票' }}</el-descriptions-item>
        <el-descriptions-item label="发票抬头" :span="2">{{ invoiceDetail.invoiceTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="纳税人识别号">{{ invoiceDetail.taxNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票号码">{{ invoiceDetail.invoiceNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核备注" :span="2">{{ invoiceDetail.reviewRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ invoiceDetail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ invoiceDetail.reviewTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="invoiceDetailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageHeading from '@/components/PageHeading.vue'
import FinanceLedgerTable from './FinanceLedgerTable.vue'
import { RENTAL_CODE } from '@/utils/rental/options'
import {
  createMerchantFinanceRecharge,
  applyMerchantFinanceWithdrawal,
  getMerchantFinanceLedger,
  getMerchantFinanceOverview,
  getMerchantInvoiceApplication,
  getMerchantPayment,
  listMerchantFinanceAccounts,
  listMerchantFinanceBook,
  listMerchantInvoiceApplications,
  listMerchantFinanceLedger,
  listMerchantFinanceRevenue,
  listMerchantFinanceSettlements,
  listMerchantFinanceShipping,
  listMerchantFinanceWithdrawals,
  createMerchantInvoiceApplication
} from '@/api/merchant/finance'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const detail = ref({})
const rows = ref([])
const total = ref(0)
const accounts = ref([])
const settlements = ref([])
const overview = ref({ recentLedgers: [] })
const rechargeVisible = ref(false)
const rechargeLoading = ref(false)
const rechargeFormRef = ref(null)
const rechargeForm = reactive({ amount: null })
const withdrawals = ref([])
const withdrawalTotal = ref(0)
const withdrawalQuery = reactive({ pageNum: 1, pageSize: 10, status: undefined })
const withdrawalApplyVisible = ref(false)
const withdrawalApplyLoading = ref(false)
const withdrawalFormRef = ref(null)
const withdrawalForm = reactive({ amount: null, withdrawalChannel: 'ALIPAY', payeeName: '', payeeAccount: '', remark: '' })
const invoiceApplications = ref([])
const invoiceApplicationTotal = ref(0)
const invoiceApplicationQuery = reactive({ pageNum: 1, pageSize: 10, status: undefined, applicationType: undefined })
const invoiceDetail = ref({})
const invoiceDetailVisible = ref(false)
const invoiceCreateVisible = ref(false)
const invoiceCreateLoading = ref(false)
const invoiceCreateFormRef = ref(null)
const invoiceCreateForm = reactive({ ledgerNo: '', applicationType: 1, invoiceType: 1, invoiceTitle: '', taxNo: '', invoiceEmail: '' })
const invoiceCreateRules = {
  ledgerNo: [{ required: true, message: '请输入财务流水号', trigger: 'blur' }],
  invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
  invoiceTitle: [{ required: true, message: '请输入发票抬头', trigger: 'blur' }],
  taxNo: [{ validator: (_, value, callback) => {
    if (invoiceCreateForm.invoiceType === 1 && !String(value || '').trim()) {
      callback(new Error('企业发票必须填写纳税人识别号'))
      return
    }
    callback()
  }, trigger: 'blur' }]
}
const withdrawalRules = {
  amount: [{ required: true, message: '请输入提现金额', trigger: 'blur' }],
  withdrawalChannel: [{ required: true, message: '请选择提现渠道', trigger: 'change' }],
  payeeName: [{ required: true, message: '请输入收款人姓名', trigger: 'blur' }],
  payeeAccount: [{ required: true, message: '请输入收款账户', trigger: 'blur' }]
}
const rechargeRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', min: 0.01, max: 1000000, message: '充值金额应在 0.01 至 1000000 元之间', trigger: 'blur' }
  ]
}
let paymentPollingTimer = null
let paymentPollingBusy = false
const query = reactive({ pageNum: 1, pageSize: 10, ledgerNo: '', accountType: undefined, direction: undefined, ledgerStatus: undefined, range: [] })
const settlementQuery = reactive({ status: undefined, range: [] })

const pageItems = [
  { key: 'overview', label: '财务总览' },
  { key: 'accounts', label: '账户余额' },
  { key: 'ledger', label: '资金流水' },
  { key: 'revenue', label: '租金收入' },
  { key: 'shipping', label: '快递费用' },
  { key: 'settlement', label: '联盟结算' },
  { key: 'withdrawal', label: '提现管理' },
  { key: 'invoice', label: '发票管理' },
  { key: 'book', label: '记账簿' }
]
const pageDetails = {
  overview: { title: '财务总览', description: '查看账户余额、收入支出、待办事项和最近资金变动', tableTitle: '最近资金流水', emptyText: '暂无财务流水' },
  accounts: { title: '账户余额', description: '不同资金性质独立记账，余额不可互相抵扣' },
  ledger: { title: '资金流水', description: '所有资金变化追加记录，成功流水不能编辑或删除', tableTitle: '全部资金流水', emptyText: '暂无资金流水' },
  revenue: { title: '租金收入', description: '按订单和业务类型查看应收、已结算与实际入账', tableTitle: '租金与收入流水', emptyText: '暂无租金与收入流水' },
  shipping: { title: '快递费用', description: '核对寄出、归还物流产生的实际费用和入账情况', tableTitle: '快递费用流水', emptyText: '暂无快递费用流水' },
  settlement: { title: '联盟结算', description: '查看联盟订单的结算、快递和赔付金额', tableTitle: '联盟结算', emptyText: '暂无联盟结算记录' },
  withdrawal: { title: '提现管理', description: '查看租金和分账资金产生的提现流水', tableTitle: '提现流水', emptyText: '暂无提现流水' },
  invoice: { title: '发票管理', description: '查看订单财务流水的开票状态和关联记录', tableTitle: '发票流水', emptyText: '暂无发票相关流水' },
  book: { title: '记账簿', description: '查看赔付、出售设备和冲正等业务形成的账务记录', tableTitle: '记账簿', emptyText: '暂无记账记录' }
}
const directions = [{ value: 1, label: '收入' }, { value: 2, label: '支出' }, { value: 3, label: '冻结' }, { value: 4, label: '解冻' }, { value: 5, label: '冲正' }]
const accountTypes = [{ value: 1, label: '增值服务余额' }, { value: 2, label: '履约保证金' }, { value: 3, label: '租金 / 分账资金' }]
const ledgerStatuses = [{ value: 0, label: '处理中' }, { value: 1, label: '成功' }, { value: 2, label: '失败' }, { value: 3, label: '已关闭' }]
const settlementStatuses = [{ value: 0, label: '待确认' }, { value: 1, label: '已确认' }, { value: 2, label: '争议中' }, { value: 3, label: '已关闭' }]
const withdrawalStatusOptions = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已完成' },
  { value: 2, label: '已拒绝' },
  { value: 3, label: '已取消' }
]
const withdrawalChannelOptions = [{ value: 'ALIPAY', label: '支付宝' }, { value: 'BANK_TRANSFER', label: '银行转账' }]
const invoiceApplicationTypes = [{ value: 1, label: '开票' }, { value: 2, label: '补票' }]
const invoiceApplicationStatuses = [{ value: 0, label: '待审核' }, { value: 1, label: '已驳回' }, { value: 2, label: '待开具' }, { value: 3, label: '已开具' }, { value: 4, label: '已红冲' }]

const pageKey = computed({
  get: () => pageItems.some(item => item.key === route.path.split('/').pop()) ? route.path.split('/').pop() : 'overview',
  set: value => navigateToPage(value)
})
const pageMeta = computed(() => pageDetails[pageKey.value] || pageDetails.overview)
const overviewCards = computed(() => [
  { key: 'availableBalance', label: '可用余额', value: money(overview.value.availableBalance), help: '当前可用于业务结算的余额', tone: 'blue' },
  { key: 'rentalSettlementBalance', label: '租金 / 分账可用', value: money(overview.value.rentalSettlementBalance), help: '可申请提现的租金与分账资金', tone: 'green' },
  { key: 'performanceDepositFrozen', label: '冻结保证金', value: money(overview.value.performanceDepositFrozen), help: '纠纷处理中暂不可用的履约保证金', tone: 'warning' },
  { key: 'monthIncome', label: '本月收入', value: money(overview.value.monthIncome), help: '本月已成功入账的收入', tone: 'green' },
  { key: 'monthExpense', label: '本月支出', value: money(overview.value.monthExpense), help: '本月已成功入账的支出', tone: 'orange' },
  { key: 'frozenAmount', label: '冻结金额', value: money(overview.value.frozenAmount), help: '履约和业务流程中暂不可用的金额', tone: 'purple' },
  { key: 'pendingSettlementAmount', label: '待结算金额', value: money(overview.value.pendingSettlementAmount), help: '联盟结算确认前的金额', tone: 'warning' },
  { key: 'abnormalLedgerCount', label: '异常流水', value: `${overview.value.abnormalLedgerCount || 0} 笔`, help: '失败或已关闭的财务流水', tone: 'danger' }
])

function navigateToPage(key) {
  if (key !== pageKey.value) router.push(`/merchant/finance/${key}`)
}

function rangeParams(range) {
  return { beginTime: range?.[0] ? `${range[0]} 00:00:00` : undefined, endTime: range?.[1] ? `${range[1]} 23:59:59` : undefined }
}

async function loadPage() {
  loading.value = true
  try {
    if (pageKey.value === 'overview') {
      const response = await getMerchantFinanceOverview()
      overview.value = response.data || {}
    } else if (pageKey.value === 'accounts') {
      const response = await listMerchantFinanceAccounts()
      accounts.value = Array.isArray(response.data) ? response.data : []
    } else if (pageKey.value === 'settlement') {
      const response = await listMerchantFinanceSettlements({ status: settlementQuery.status, ...rangeParams(settlementQuery.range) })
      settlements.value = Array.isArray(response.data) ? response.data : []
    } else if (pageKey.value === 'withdrawal') {
      await loadWithdrawals()
    } else if (pageKey.value === 'invoice') {
      await loadInvoiceApplications()
    } else {
      const params = { ...query, ...rangeParams(query.range) }
      delete params.range
      const api = {
        ledger: listMerchantFinanceLedger,
        revenue: listMerchantFinanceRevenue,
        shipping: listMerchantFinanceShipping,
        withdrawal: listMerchantFinanceWithdrawals,
        book: listMerchantFinanceBook
      }[pageKey.value]
      const response = await api(params)
      rows.value = response.rows || []
      total.value = response.total || 0
    }
  } catch (error) {
    rows.value = []
    settlements.value = []
  } finally {
    loading.value = false
  }
}

async function loadInvoiceApplications() {
  loading.value = true
  try {
    const response = await listMerchantInvoiceApplications(invoiceApplicationQuery)
    invoiceApplications.value = response.rows || []
    invoiceApplicationTotal.value = Number(response.total || 0)
  } finally {
    loading.value = false
  }
}

async function loadWithdrawals() {
  loading.value = true
  try {
    const response = await listMerchantFinanceWithdrawals(withdrawalQuery)
    withdrawals.value = response.rows || []
    withdrawalTotal.value = Number(response.total || 0)
  } finally {
    loading.value = false
  }
}

async function openLedgerDetail(row) {
  if (!row?.ledgerNo) return
  detailVisible.value = true
  detailLoading.value = true
  try {
    const response = await getMerchantFinanceLedger(row.ledgerNo)
    detail.value = response.data || {}
  } finally {
    detailLoading.value = false
  }
}

function openRecharge() {
  rechargeForm.amount = null
  rechargeVisible.value = true
}

function openWithdrawalApply() {
  Object.assign(withdrawalForm, { amount: null, withdrawalChannel: 'ALIPAY', payeeName: '', payeeAccount: '', remark: '' })
  withdrawalFormRef.value?.clearValidate()
  withdrawalApplyVisible.value = true
}

function openInvoiceCreate() {
  Object.assign(invoiceCreateForm, { ledgerNo: '', applicationType: 1, invoiceType: 1, invoiceTitle: '', taxNo: '', invoiceEmail: '' })
  invoiceCreateFormRef.value?.clearValidate()
  invoiceCreateVisible.value = true
}

async function submitInvoiceCreate() {
  const valid = await invoiceCreateFormRef.value?.validate().catch(() => false)
  if (!valid) return
  invoiceCreateLoading.value = true
  try {
    await createMerchantInvoiceApplication({ ...invoiceCreateForm, idempotencyKey: window.crypto?.randomUUID?.() || `invoice-${Date.now()}` })
    ElMessage.success('开票申请已提交')
    invoiceCreateVisible.value = false
    await loadInvoiceApplications()
  } catch (error) {
    ElMessage.error(error?.message || '开票申请提交失败')
  } finally {
    invoiceCreateLoading.value = false
  }
}

async function openInvoiceDetail(row) {
  invoiceDetailVisible.value = true
  detailLoading.value = true
  invoiceDetail.value = row
  try {
    const response = await getMerchantInvoiceApplication(row.applicationNo)
    invoiceDetail.value = response.data || row
  } finally {
    detailLoading.value = false
  }
}

async function submitWithdrawal() {
  const valid = await withdrawalFormRef.value?.validate().catch(() => false)
  if (!valid) return
  withdrawalApplyLoading.value = true
  try {
    await applyMerchantFinanceWithdrawal({
      ...withdrawalForm,
      idempotencyKey: window.crypto?.randomUUID?.() || `withdrawal-${Date.now()}`
    })
    ElMessage.success('提现申请已提交，金额已冻结')
    withdrawalApplyVisible.value = false
    await loadWithdrawals()
  } catch (error) {
    ElMessage.error(error?.message || '提现申请提交失败')
  } finally {
    withdrawalApplyLoading.value = false
  }
}

/** 在用户点击事件中先打开窗口，避免异步请求结束后被浏览器拦截弹窗。 */
async function submitRecharge() {
  const valid = await rechargeFormRef.value?.validate().catch(() => false)
  if (!valid) return
  const paymentWindow = window.open('', '_blank')
  if (!paymentWindow) {
    ElMessage.warning('支付宝支付窗口被浏览器拦截，请允许弹出窗口后重试')
    return
  }
  rechargeLoading.value = true
  try {
    const response = await createMerchantFinanceRecharge(rechargeForm.amount)
    const payment = response?.data ?? response
    if (!payment?.paymentForm || !payment.paymentNo) {
      throw new Error('支付宝支付单创建失败')
    }
    paymentWindow.document.open()
    paymentWindow.document.write(payment.paymentForm)
    paymentWindow.document.close()
    rechargeVisible.value = false
    startPaymentPolling(payment.paymentNo)
  } catch (error) {
    paymentWindow.close()
    ElMessage.error(error?.message || '充值支付单创建失败')
  } finally {
    rechargeLoading.value = false
  }
}

function startPaymentPolling(paymentNo) {
  stopPaymentPolling()
  paymentPollingTimer = window.setInterval(async () => {
    if (paymentPollingBusy) return
    paymentPollingBusy = true
    try {
      const response = await getMerchantPayment(paymentNo)
      const payment = response?.data ?? response
      if (payment?.paymentStatus === RENTAL_CODE.PAYMENT_STATUS.PAID) {
        stopPaymentPolling()
        await loadPage()
        ElMessage.success('充值成功，余额已入账')
      }
    } catch (_error) {
      // 支付页面可能尚未完成，继续等待支付宝异步通知。
    } finally {
      paymentPollingBusy = false
    }
  }, 2000)
}

function stopPaymentPolling() {
  if (paymentPollingTimer) {
    window.clearInterval(paymentPollingTimer)
    paymentPollingTimer = null
  }
  paymentPollingBusy = false
}

function money(value) {
  const amount = Number(value || 0)
  return `¥ ${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function moneyOrDash(value) {
  return value === null || value === undefined ? '-' : money(value)
}

function settlementTagType(status) {
  return { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[status] || 'info'
}

function withdrawalTagType(status) {
  return { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[status] || 'info'
}

function invoiceApplicationTagType(status) {
  return { 0: 'warning', 1: 'danger', 2: 'info', 3: 'success', 4: 'danger' }[status] || 'info'
}

watch(() => route.path, () => {
  query.pageNum = 1
  loadPage()
})
onMounted(loadPage)
onBeforeUnmount(stopPaymentPolling)
</script>

<style scoped>
.merchant-finance-page { min-height: calc(100vh - 84px); }
.finance-tabs { margin-bottom: 18px; }
.summary-grid, .content-grid, .account-grid { margin-bottom: 16px; }
.summary-card, .finance-panel, .account-card { border-color: var(--el-border-color-light); }
.summary-label { color: var(--el-text-color-secondary); font-size: 14px; }
.summary-value { margin-top: 12px; font-size: 26px; font-weight: 650; line-height: 1.1; }
.summary-value.blue { color: var(--el-color-primary); }
.summary-value.green { color: var(--el-color-success); }
.summary-value.orange, .summary-value.warning { color: var(--el-color-warning); }
.summary-value.purple { color: var(--el-color-primary-light-3); }
.summary-value.danger { color: var(--el-color-danger); }
.summary-help, .panel-note, .account-card__help { margin-top: 10px; color: var(--el-text-color-secondary); font-size: 12px; line-height: 1.5; }
.panel-heading, .account-card__heading, .task-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.task-list { display: grid; gap: 2px; }
.task-row { min-height: 48px; border-bottom: 1px solid var(--el-border-color-lighter); }
.task-row:last-child { border-bottom: 0; }
.task-row strong { margin-left: auto; }
.warning-text { color: var(--el-color-warning); }
.danger-text { color: var(--el-color-danger); }
.finance-alert { margin-bottom: 16px; }
.accounts-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.accounts-toolbar h3 { margin: 0; font-size: 18px; }
.accounts-toolbar p { margin: 6px 0 0; color: var(--el-text-color-secondary); font-size: 13px; }
.recharge-amount-input { width: 240px; }
.recharge-unit { margin-left: 8px; color: var(--el-text-color-secondary); }
.panel-heading > div { display: flex; align-items: center; gap: 12px; }
.account-card { min-height: 210px; }
.account-card__heading { color: var(--el-text-color-primary); font-weight: 600; }
.account-card__amount { margin-top: 22px; color: var(--el-color-primary); font-size: 25px; font-weight: 650; }
.account-card__line { margin-top: 8px; color: var(--el-text-color-secondary); font-size: 13px; }
.filter-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; margin-bottom: 16px; }
@media (max-width: 768px) {
  .filter-bar > * { max-width: 100%; }
  .summary-value { font-size: 23px; }
  .accounts-toolbar { align-items: flex-start; flex-direction: column; }
}
</style>
