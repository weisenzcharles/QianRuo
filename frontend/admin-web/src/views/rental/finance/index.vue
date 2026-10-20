<template>
  <div class="app-container platform-finance-page">
    <PageHeading
      kicker="FINANCE CENTER"
      :title="pageMeta.title"
      :description="pageMeta.description"
    >
      <template #aside>
        <el-button :icon="Refresh" @click="loadPage">刷新</el-button>
      </template>
    </PageHeading>

    <el-tabs v-model="pageKey" class="finance-tabs" @tab-change="navigateToPage">
      <el-tab-pane v-for="item in pageItems" :key="item.key" :label="item.label" :name="item.key" />
    </el-tabs>

    <template v-if="pageKey === 'overview'">
      <el-row v-loading="loading" :gutter="16" class="summary-grid">
        <el-col v-for="item in overviewCards" :key="item.key" :xs="24" :sm="12" :lg="6">
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">{{ item.label }}</div>
            <div class="summary-value" :class="item.tone">{{ item.value }}</div>
            <div class="summary-help">{{ item.help }}</div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="16" class="overview-grid">
        <el-col :xs="24" :lg="14">
          <el-card shadow="never" class="finance-panel">
            <template #header>
              <div class="panel-heading"><span>支付与商户概况</span><span class="panel-note">当前有效数据</span></div>
            </template>
            <div class="overview-metrics">
              <div><span>运营商户</span><strong>{{ summary.merchantCount || 0 }}</strong></div>
              <div><span>支付订单</span><strong>{{ summary.paymentOrderCount || 0 }}</strong></div>
              <div><span>待支付订单</span><strong class="warning-text">{{ summary.unpaidPaymentCount || 0 }}</strong></div>
              <div><span>已支付金额</span><strong>{{ money(summary.paidPaymentAmount) }}</strong></div>
              <div><span>待支付金额</span><strong class="warning-text">{{ money(summary.unpaidPaymentAmount) }}</strong></div>
              <div><span>本月收入</span><strong class="success-text">{{ money(summary.monthIncome) }}</strong></div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="10">
          <el-card shadow="never" class="finance-panel">
            <template #header>
              <div class="panel-heading"><span>财务待处理事项</span><span class="panel-note">只读监控</span></div>
            </template>
            <div class="task-list">
              <div class="task-row"><span>待开票流水</span><strong class="warning-text">{{ summary.pendingInvoiceCount || 0 }}</strong><el-button link type="primary" @click="navigateToPage('invoice')">查看</el-button></div>
              <div class="task-row"><span>异常流水</span><strong class="danger-text">{{ summary.abnormalLedgerCount || 0 }}</strong><el-button link type="primary" @click="navigateToPage('ledger')">查看</el-button></div>
              <div class="task-row"><span>未支付订单</span><strong class="warning-text">{{ summary.unpaidPaymentCount || 0 }}</strong><el-button link type="primary" @click="navigateToPage('payments')">查看</el-button></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-alert class="finance-alert" title="平台财务中心为跨商户治理视图，资金流水和支付单只允许查看，不在此处直接编辑或删除。" type="info" :closable="false" show-icon />
    </template>

    <template v-else-if="pageKey === 'accounts'">
      <el-card shadow="never" class="finance-panel">
        <template #header><div class="panel-heading"><span>商户账户汇总</span><span class="panel-note">按有效财务流水计算</span></div></template>
        <div class="filter-bar">
          <el-input v-model="accountQuery.merchantKeyword" clearable placeholder="商户名称、编码或主体" class="merchant-filter" @keyup.enter="handleQuery" />
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </div>
        <el-table v-loading="loading" :data="accounts" border>
          <el-table-column label="商户名称" prop="merchantName" min-width="180" show-overflow-tooltip />
          <el-table-column label="商户编码" prop="memberCode" width="150" />
          <el-table-column label="主体名称" prop="legalSubjectName" min-width="180" show-overflow-tooltip />
          <el-table-column label="可用余额" prop="availableAmount" width="140" align="right"><template #default="{ row }">{{ money(row.availableAmount) }}</template></el-table-column>
          <el-table-column label="冻结金额" prop="frozenAmount" width="140" align="right"><template #default="{ row }">{{ money(row.frozenAmount) }}</template></el-table-column>
          <el-table-column label="增值余额" prop="valueAddedAvailableAmount" width="140" align="right"><template #default="{ row }">{{ money(row.valueAddedAvailableAmount) }}</template></el-table-column>
          <el-table-column label="保证金可用" prop="performanceDepositAvailableAmount" width="140" align="right"><template #default="{ row }">{{ money(row.performanceDepositAvailableAmount) }}</template></el-table-column>
          <el-table-column label="租金 / 分账可用" prop="rentalSettlementAvailableAmount" width="155" align="right"><template #default="{ row }">{{ money(row.rentalSettlementAvailableAmount) }}</template></el-table-column>
          <el-table-column label="累计收入" prop="totalIncome" width="140" align="right"><template #default="{ row }">{{ money(row.totalIncome) }}</template></el-table-column>
          <el-table-column label="累计支出" prop="totalExpense" width="140" align="right"><template #default="{ row }">{{ money(row.totalExpense) }}</template></el-table-column>
          <el-table-column label="待开票" prop="pendingInvoiceCount" width="90" align="center" />
          <el-table-column label="异常流水" prop="abnormalLedgerCount" width="100" align="center" />
        </el-table>
        <pagination v-show="total > 0" v-model:page="accountQuery.pageNum" v-model:limit="accountQuery.pageSize" :total="total" @pagination="loadPage" />
        <el-empty v-if="!loading && accounts.length === 0" description="暂无商户账户数据" />
      </el-card>
    </template>

    <template v-else-if="pageKey === 'payments'">
      <el-card shadow="never" class="finance-panel">
        <template #header><div class="panel-heading"><span>支付订单</span><span class="panel-note">支付宝支付单只读查询</span></div></template>
        <div class="filter-bar">
          <el-input v-model="paymentQuery.paymentNo" clearable placeholder="支付单号" class="compact-filter" @keyup.enter="handleQuery" />
          <el-input v-model="paymentQuery.merchantKeyword" clearable placeholder="商户名称或编码" class="merchant-filter" @keyup.enter="handleQuery" />
          <el-select v-model="paymentQuery.paymentStatus" clearable placeholder="全部状态" class="status-filter" @change="handleQuery">
            <el-option v-for="item in paymentStatuses" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-date-picker v-model="paymentQuery.range" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" class="date-filter" @change="handleQuery" />
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </div>
        <el-table v-loading="loading" :data="paymentRows" border>
          <el-table-column label="支付单号" prop="paymentNo" min-width="175" show-overflow-tooltip />
          <el-table-column label="商户名称" prop="merchantName" min-width="150" show-overflow-tooltip />
          <el-table-column label="业务类型" prop="bizTypeName" width="115" />
          <el-table-column label="支付标题" prop="subject" min-width="180" show-overflow-tooltip />
          <el-table-column label="金额" prop="amount" width="120" align="right"><template #default="{ row }">{{ money(row.amount) }}</template></el-table-column>
          <el-table-column label="状态" prop="paymentStatusName" width="105" align="center"><template #default="{ row }"><el-tag :type="option(null, row.paymentStatus, paymentStatuses).tagType">{{ row.paymentStatusName || option(null, row.paymentStatus, paymentStatuses).label }}</el-tag></template></el-table-column>
          <el-table-column label="支付宝交易号" prop="alipayTradeNo" min-width="180" show-overflow-tooltip />
          <el-table-column label="创建时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="80" fixed="right" align="center"><template #default="{ row }"><el-button link type="primary" @click="openPaymentDetail(row)">详情</el-button></template></el-table-column>
        </el-table>
        <pagination v-show="total > 0" v-model:page="paymentQuery.pageNum" v-model:limit="paymentQuery.pageSize" :total="total" @pagination="loadPage" />
        <el-empty v-if="!loading && paymentRows.length === 0" description="暂无支付订单" />
      </el-card>
    </template>

    <template v-else-if="pageKey === 'settlement'">
      <el-card shadow="never" class="finance-panel">
        <template #header><div class="panel-heading"><span>联盟结算</span><span class="panel-note">按联盟订单结算金额查看</span></div></template>
        <div class="filter-bar">
          <el-select v-model="settlementQuery.status" clearable placeholder="全部状态" class="status-filter" @change="handleQuery">
            <el-option v-for="item in settlementStatuses" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-date-picker v-model="settlementQuery.range" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" class="date-filter" @change="handleQuery" />
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </div>
        <el-table v-loading="loading" :data="settlements" border>
          <el-table-column label="发起商户" prop="sourceMerchantName" min-width="160" show-overflow-tooltip />
          <el-table-column label="接收商户" prop="targetMerchantName" min-width="160" show-overflow-tooltip />
          <el-table-column label="原订单编码" prop="sourceOrderNo" width="170" />
          <el-table-column label="子订单编码" prop="childOrderNo" width="170"><template #default="{ row }">{{ row.childOrderNo || '-' }}</template></el-table-column>
          <el-table-column label="结算金额" prop="settlementPrice" width="130" align="right"><template #default="{ row }">{{ money(row.settlementPrice) }}</template></el-table-column>
          <el-table-column label="状态" prop="allianceOrderStatus" width="110" align="center"><template #default="{ row }"><el-tag :type="option(null, row.allianceOrderStatus, settlementStatuses).tagType">{{ allianceStatusName(row.allianceOrderStatus) }}</el-tag></template></el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="175" />
        </el-table>
        <pagination v-show="total > 0" v-model:page="settlementQuery.pageNum" v-model:limit="settlementQuery.pageSize" :total="total" @pagination="loadPage" />
        <el-empty v-if="!loading && settlements.length === 0" description="暂无联盟结算记录" />
      </el-card>
    </template>

    <template v-else-if="pageKey === 'withdrawal'">
      <el-card shadow="never" class="finance-panel">
        <template #header><div class="panel-heading"><div><span>提现申请</span><span class="panel-note">审核通过后完成出款，拒绝后自动解冻余额</span></div><el-button :icon="Refresh" @click="loadWithdrawals">刷新</el-button></div></template>
        <div class="filter-bar">
          <el-input v-model="withdrawalQuery.withdrawalNo" clearable placeholder="提现申请号" class="compact-filter" @keyup.enter="handleQuery" />
          <el-input v-model="withdrawalQuery.merchantKeyword" clearable placeholder="商户名称、编码或主体" class="merchant-filter" @keyup.enter="handleQuery" />
          <el-select v-model="withdrawalQuery.withdrawalStatus" clearable placeholder="全部状态" class="status-filter" @change="handleQuery">
            <el-option v-for="item in withdrawalStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-date-picker v-model="withdrawalQuery.range" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" class="date-filter" @change="handleQuery" />
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </div>
        <el-table v-loading="loading" :data="withdrawalRows" border>
          <el-table-column label="提现申请号" prop="withdrawalNo" min-width="210" show-overflow-tooltip />
          <el-table-column label="商户名称" prop="merchantName" min-width="150" show-overflow-tooltip />
          <el-table-column label="申请金额" prop="amount" width="125" align="right"><template #default="{ row }">{{ money(row.amount) }}</template></el-table-column>
          <el-table-column label="实际到账" prop="actualAmount" width="125" align="right"><template #default="{ row }">{{ money(row.actualAmount) }}</template></el-table-column>
          <el-table-column label="提现渠道" prop="withdrawalChannelName" width="110" />
          <el-table-column label="收款人" prop="payeeName" width="120" />
          <el-table-column label="收款账户" prop="payeeAccount" min-width="150" />
          <el-table-column label="状态" prop="withdrawalStatusName" width="105" align="center"><template #default="{ row }"><el-tag :type="withdrawalTagType(row.withdrawalStatus)">{{ row.withdrawalStatusName || '未知状态' }}</el-tag></template></el-table-column>
          <el-table-column label="申请时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="130" fixed="right" align="center"><template #default="{ row }"><el-button link type="primary" @click="openWithdrawalDetail(row)">详情</el-button><el-button v-if="row.withdrawalStatus === 0" link type="primary" @click="openWithdrawalReview(row)">审核</el-button></template></el-table-column>
        </el-table>
        <pagination v-show="withdrawalTotal > 0" v-model:page="withdrawalQuery.pageNum" v-model:limit="withdrawalQuery.pageSize" :total="withdrawalTotal" @pagination="loadWithdrawals" />
        <el-empty v-if="!loading && withdrawalRows.length === 0" description="暂无提现申请" />
      </el-card>
    </template>

    <template v-else-if="pageKey === 'invoice'">
      <el-card shadow="never" class="finance-panel">
        <template #header><div class="panel-heading"><div><span>发票申请</span><span class="panel-note">审核通过后开具，红冲必须保留原发票记录</span></div><el-button :icon="Refresh" @click="loadInvoiceApplications">刷新</el-button></div></template>
        <div class="filter-bar">
          <el-input v-model="invoiceApplicationQuery.applicationNo" clearable placeholder="申请编号" class="compact-filter" @keyup.enter="handleQuery" />
          <el-input v-model="invoiceApplicationQuery.merchantKeyword" clearable placeholder="商户名称、编码或主体" class="merchant-filter" @keyup.enter="handleQuery" />
          <el-select v-model="invoiceApplicationQuery.applicationStatus" clearable placeholder="全部状态" class="status-filter" @change="handleQuery"><el-option v-for="item in invoiceApplicationStatuses" :key="item.value" :label="item.label" :value="item.value" /></el-select>
          <el-select v-model="invoiceApplicationQuery.applicationType" clearable placeholder="全部类型" class="status-filter" @change="handleQuery"><el-option v-for="item in invoiceApplicationTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select>
          <el-date-picker v-model="invoiceApplicationQuery.range" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" class="date-filter" @change="handleQuery" />
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </div>
        <el-table v-loading="loading" :data="invoiceApplications" border>
          <el-table-column label="申请编号" prop="applicationNo" min-width="215" show-overflow-tooltip />
          <el-table-column label="商户名称" prop="merchantName" min-width="150" show-overflow-tooltip />
          <el-table-column label="流水号" prop="ledgerNo" min-width="175" show-overflow-tooltip />
          <el-table-column label="申请金额" prop="amount" width="125" align="right"><template #default="{ row }">{{ money(row.amount) }}</template></el-table-column>
          <el-table-column label="申请类型" prop="applicationTypeName" width="100" />
          <el-table-column label="状态" prop="applicationStatusName" width="105" align="center"><template #default="{ row }"><el-tag :type="invoiceApplicationTagType(row.applicationStatus)">{{ row.applicationStatusName || '-' }}</el-tag></template></el-table-column>
          <el-table-column label="申请时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="210" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="openInvoiceDetail(row)">详情</el-button>
              <el-button v-if="row.applicationStatus === 0" link type="primary" @click="openInvoiceReview(row)">审核</el-button>
              <el-button v-if="row.applicationStatus === 2" link type="primary" @click="openInvoiceIssue(row)">开具</el-button>
              <el-button v-if="row.applicationStatus === 3" link type="danger" @click="openInvoiceRedFlush(row)">红冲</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="invoiceApplicationTotal > 0" v-model:page="invoiceApplicationQuery.pageNum" v-model:limit="invoiceApplicationQuery.pageSize" :total="invoiceApplicationTotal" @pagination="loadInvoiceApplications" />
        <el-empty v-if="!loading && invoiceApplications.length === 0" description="暂无发票申请" />
      </el-card>
    </template>

    <template v-else>
      <el-card shadow="never" class="finance-panel">
        <template #header><div class="panel-heading"><span>{{ pageMeta.tableTitle }}</span><span class="panel-note">平台只读治理视图</span></div></template>
        <div class="filter-bar">
          <el-input v-model="ledgerQuery.ledgerNo" clearable placeholder="流水号" class="compact-filter" @keyup.enter="handleQuery" />
          <el-input v-model="ledgerQuery.merchantKeyword" clearable placeholder="商户名称、编码或主体" class="merchant-filter" @keyup.enter="handleQuery" />
          <el-select v-model="ledgerQuery.accountType" clearable placeholder="全部资金账户" class="account-filter" @change="handleQuery"><el-option v-for="item in accountTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select>
          <el-select v-model="ledgerQuery.direction" clearable placeholder="全部方向" class="status-filter" @change="handleQuery"><el-option v-for="item in financeDirections" :key="item.value" :label="item.label" :value="item.value" /></el-select>
          <el-select v-model="ledgerQuery.ledgerStatus" clearable placeholder="全部流水状态" class="status-filter" @change="handleQuery"><el-option v-for="item in ledgerStatuses" :key="item.value" :label="item.label" :value="item.value" /></el-select>
          <el-date-picker v-model="ledgerQuery.range" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" class="date-filter" @change="handleQuery" />
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </div>
        <el-alert v-if="pageKey === 'invoice'" title="当前展示已进入财务流水的发票状态，开票申请与审核动作暂不在平台页面伪造。" type="info" :closable="false" show-icon class="finance-alert" />
        <el-table v-loading="loading" :data="ledgerRows" border>
          <el-table-column label="流水号" prop="ledgerNo" min-width="175" show-overflow-tooltip />
          <el-table-column label="商户名称" prop="merchantName" min-width="150" show-overflow-tooltip />
          <el-table-column label="资金账户" prop="accountTypeName" width="145" />
          <el-table-column label="业务类型" prop="bizTypeName" width="120" />
          <el-table-column label="方向" prop="directionName" width="90" align="center"><template #default="{ row }"><el-tag :type="option(null, row.direction, financeDirections).tagType">{{ row.directionName || option(null, row.direction, financeDirections).label }}</el-tag></template></el-table-column>
          <el-table-column label="金额" prop="amount" width="125" align="right"><template #default="{ row }">{{ money(row.amount) }}</template></el-table-column>
          <el-table-column label="变更后余额" prop="balanceAfter" width="140" align="right"><template #default="{ row }">{{ moneyOrDash(row.balanceAfter) }}</template></el-table-column>
          <el-table-column label="支付渠道" prop="payChannel" width="110"><template #default="{ row }">{{ row.payChannel || '-' }}</template></el-table-column>
          <el-table-column label="发票状态" prop="invoiceStatusName" width="105" align="center"><template #default="{ row }">{{ row.invoiceStatusName || '未申请' }}</template></el-table-column>
          <el-table-column label="流水状态" prop="ledgerStatusName" width="105" align="center"><template #default="{ row }"><el-tag :type="option(null, row.ledgerStatus, ledgerStatuses).tagType">{{ row.ledgerStatusName || option(null, row.ledgerStatus, ledgerStatuses).label }}</el-tag></template></el-table-column>
          <el-table-column label="发生时间" prop="createTime" width="175" />
          <el-table-column label="操作" width="80" fixed="right" align="center"><template #default="{ row }"><el-button link type="primary" @click="openLedgerDetail(row)">详情</el-button></template></el-table-column>
        </el-table>
        <pagination v-show="total > 0" v-model:page="ledgerQuery.pageNum" v-model:limit="ledgerQuery.pageSize" :total="total" @pagination="loadPage" />
        <el-empty v-if="!loading && ledgerRows.length === 0" :description="pageMeta.emptyText" />
      </el-card>
    </template>

    <el-dialog v-model="ledgerDetailVisible" title="资金流水详情" width="720px" append-to-body destroy-on-close>
      <el-descriptions v-loading="detailLoading" :column="2" border label-width="120">
        <el-descriptions-item label="流水号">{{ ledgerDetail.ledgerNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ ledgerDetail.merchantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资金账户">{{ ledgerDetail.accountTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ ledgerDetail.bizTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流水方向">{{ ledgerDetail.directionName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ money(ledgerDetail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="变更后余额">{{ moneyOrDash(ledgerDetail.balanceAfter) }}</el-descriptions-item>
        <el-descriptions-item label="关联类型">{{ ledgerDetail.refType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付渠道">{{ ledgerDetail.payChannel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付单号">{{ ledgerDetail.payOrderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票状态">{{ ledgerDetail.invoiceStatusName || '未申请' }}</el-descriptions-item>
        <el-descriptions-item label="流水状态">{{ ledgerDetail.ledgerStatusName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="原流水">{{ ledgerDetail.originalLedgerNo || '无' }}</el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ ledgerDetail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ ledgerDetail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="ledgerDetailVisible = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="paymentDetailVisible" title="支付订单详情" width="680px" append-to-body destroy-on-close>
      <el-descriptions v-loading="detailLoading" :column="2" border label-width="120">
        <el-descriptions-item label="支付单号">{{ paymentDetail.paymentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ paymentDetail.merchantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ paymentDetail.bizTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务编码">{{ paymentDetail.bizNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付标题" :span="2">{{ paymentDetail.subject || '-' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ money(paymentDetail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ paymentDetail.paymentStatusName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付宝交易号" :span="2">{{ paymentDetail.alipayTradeNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ paymentDetail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ paymentDetail.paidTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="过期时间">{{ paymentDetail.expireTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ paymentDetail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="paymentDetailVisible = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="withdrawalDetailVisible" title="提现申请详情" width="720px" append-to-body destroy-on-close>
      <el-descriptions v-loading="detailLoading" :column="2" border label-width="120">
        <el-descriptions-item label="提现申请号">{{ withdrawalDetail.withdrawalNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ withdrawalDetail.merchantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">{{ money(withdrawalDetail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="实际到账">{{ money(withdrawalDetail.actualAmount) }}</el-descriptions-item>
        <el-descriptions-item label="提现渠道">{{ withdrawalDetail.withdrawalChannelName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收款人">{{ withdrawalDetail.payeeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收款账户">{{ withdrawalDetail.payeeAccount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ withdrawalDetail.withdrawalStatusName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ withdrawalDetail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ withdrawalDetail.reviewTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核备注" :span="2">{{ withdrawalDetail.reviewRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请备注" :span="2">{{ withdrawalDetail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="withdrawalDetailVisible = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="withdrawalReviewVisible" title="审核提现申请" width="520px" append-to-body destroy-on-close>
      <el-descriptions :column="2" border label-width="120">
        <el-descriptions-item label="提现申请号">{{ withdrawalReviewForm.withdrawalNo }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ withdrawalReviewForm.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">{{ money(withdrawalReviewForm.amount) }}</el-descriptions-item>
        <el-descriptions-item label="收款账户">{{ withdrawalReviewForm.payeeAccount }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="withdrawalReviewForm" label-width="80px" class="review-form">
        <el-form-item label="审核结果">
          <el-radio-group v-model="withdrawalReviewForm.approved">
            <el-radio :value="true">通过并出款</el-radio>
            <el-radio :value="false">拒绝并解冻</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注"><el-input v-model="withdrawalReviewForm.reviewRemark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="拒绝时必须填写原因" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="withdrawalReviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="withdrawalReviewLoading" @click="submitWithdrawalReview">提交审核</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="invoiceDetailVisible" title="发票申请详情" width="720px" append-to-body destroy-on-close>
      <el-descriptions v-loading="detailLoading" :column="2" border label-width="120">
        <el-descriptions-item label="申请编号">{{ invoiceDetail.applicationNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ invoiceDetail.merchantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流水号">{{ invoiceDetail.ledgerNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">{{ money(invoiceDetail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="申请类型">{{ invoiceDetail.applicationTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ invoiceDetail.applicationStatusName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票抬头" :span="2">{{ invoiceDetail.invoiceTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="纳税人识别号">{{ invoiceDetail.taxNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票号码">{{ invoiceDetail.invoiceNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票代码">{{ invoiceDetail.invoiceCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="原申请编号">{{ invoiceDetail.originalApplicationNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核备注" :span="2">{{ invoiceDetail.reviewRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ invoiceDetail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ invoiceDetail.reviewTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="invoiceDetailVisible = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="invoiceReviewVisible" title="审核发票申请" width="560px" append-to-body destroy-on-close>
      <el-descriptions :column="2" border label-width="120">
        <el-descriptions-item label="申请编号">{{ invoiceReviewForm.applicationNo }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ invoiceReviewForm.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">{{ money(invoiceReviewForm.amount) }}</el-descriptions-item>
        <el-descriptions-item label="发票抬头" :span="2">{{ invoiceReviewForm.invoiceTitle }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="invoiceReviewForm" label-width="90px" class="review-form">
        <el-form-item label="审核结果"><el-radio-group v-model="invoiceReviewForm.approved"><el-radio :value="true">通过</el-radio><el-radio :value="false">驳回</el-radio></el-radio-group></el-form-item>
        <el-form-item label="发票号码"><el-input v-model="invoiceReviewForm.invoiceNo" maxlength="128" placeholder="填写后直接完成开具，留空则进入待开具" /></el-form-item>
        <el-form-item label="发票代码"><el-input v-model="invoiceReviewForm.invoiceCode" maxlength="64" /></el-form-item>
        <el-form-item label="审核备注"><el-input v-model="invoiceReviewForm.reviewRemark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="驳回时必须填写原因" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="invoiceReviewVisible = false">取消</el-button><el-button type="primary" :loading="invoiceActionLoading" @click="submitInvoiceReview">提交审核</el-button></template>
    </el-dialog>

    <el-dialog v-model="invoiceIssueVisible" title="开具发票" width="520px" append-to-body destroy-on-close>
      <el-form :model="invoiceIssueForm" label-width="90px">
        <el-form-item label="申请编号"><span>{{ invoiceIssueForm.applicationNo }}</span></el-form-item>
        <el-form-item label="发票号码"><el-input v-model="invoiceIssueForm.invoiceNo" maxlength="128" /></el-form-item>
        <el-form-item label="发票代码"><el-input v-model="invoiceIssueForm.invoiceCode" maxlength="64" /></el-form-item>
        <el-form-item label="文件地址"><el-input v-model="invoiceIssueForm.invoiceFileUrl" maxlength="500" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="invoiceIssueVisible = false">取消</el-button><el-button type="primary" :loading="invoiceActionLoading" @click="submitInvoiceIssue">完成开具</el-button></template>
    </el-dialog>

    <el-dialog v-model="invoiceRedFlushVisible" title="发起发票红冲" width="520px" append-to-body destroy-on-close>
      <el-form :model="invoiceRedFlushForm" label-width="90px">
        <el-form-item label="原申请编号"><span>{{ invoiceRedFlushForm.applicationNo }}</span></el-form-item>
        <el-form-item label="红冲原因"><el-input v-model="invoiceRedFlushForm.reason" type="textarea" :rows="4" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button @click="invoiceRedFlushVisible = false">取消</el-button><el-button type="danger" :loading="invoiceActionLoading" @click="submitInvoiceRedFlush">提交红冲</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="PlatformFinanceCenter">
import { computed, reactive, ref, watch } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageHeading from '@/components/PageHeading.vue'
import { createPlatformInvoiceRedFlush, getPlatformFinanceLedger, getPlatformFinanceSummary, getPlatformFinanceWithdrawal, getPlatformInvoiceApplication, getPlatformPaymentOrder, issuePlatformInvoiceApplication, listPlatformFinanceAccounts, listPlatformFinanceLedger, listPlatformFinanceSettlements, listPlatformFinanceWithdrawals, listPlatformInvoiceApplications, listPlatformPaymentOrders, reviewPlatformFinanceWithdrawal, reviewPlatformInvoiceApplication } from '@/api/rental/finance'
import { findRentalOption, platformFinanceDirectionOptions, platformFinanceLedgerStatusOptions, platformPaymentStatusOptions } from '@/utils/rental/options'
import { ALLIANCE_ORDER_STATUS, ALLIANCE_SETTLEMENT_STATUS } from '@/utils/rental/alliance'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detailLoading = ref(false)
const total = ref(0)
const summary = ref({})
const accounts = ref([])
const paymentRows = ref([])
const ledgerRows = ref([])
const settlements = ref([])
const ledgerDetail = ref({})
const paymentDetail = ref({})
const ledgerDetailVisible = ref(false)
const paymentDetailVisible = ref(false)
const withdrawalRows = ref([])
const withdrawalTotal = ref(0)
const withdrawalDetail = ref({})
const withdrawalDetailVisible = ref(false)
const withdrawalReviewVisible = ref(false)
const withdrawalReviewLoading = ref(false)
const withdrawalReviewForm = reactive({ withdrawalNo: '', merchantName: '', amount: null, payeeAccount: '', approved: true, reviewRemark: '' })
const invoiceApplications = ref([])
const invoiceApplicationTotal = ref(0)
const invoiceApplicationQuery = reactive({ pageNum: 1, pageSize: 10, applicationNo: route.query.applicationNo ? String(route.query.applicationNo) : '', merchantKeyword: '', applicationStatus: undefined, applicationType: undefined, range: [] })
const invoiceDetail = ref({})
const invoiceDetailVisible = ref(false)
const invoiceReviewVisible = ref(false)
const invoiceIssueVisible = ref(false)
const invoiceRedFlushVisible = ref(false)
const invoiceActionLoading = ref(false)
const invoiceReviewForm = reactive({ applicationNo: '', merchantName: '', amount: null, invoiceTitle: '', approved: true, invoiceNo: '', invoiceCode: '', reviewRemark: '' })
const invoiceIssueForm = reactive({ applicationNo: '', invoiceNo: '', invoiceCode: '', invoiceFileUrl: '' })
const invoiceRedFlushForm = reactive({ applicationNo: '', reason: '' })

const pageItems = [
  { key: 'overview', label: '财务总览' },
  { key: 'accounts', label: '商户账户' },
  { key: 'payments', label: '支付订单' },
  { key: 'ledger', label: '资金流水' },
  { key: 'value-added', label: '增值服务' },
  { key: 'shipping', label: '快递费用' },
  { key: 'settlement', label: '联盟结算' },
  { key: 'withdrawal', label: '提现管理' },
  { key: 'invoice', label: '发票管理' },
  { key: 'book', label: '记账簿' }
]

const pageDetails = {
  overview: { title: '财务总览', description: '查看平台收入、支出、账户余额和待处理事项' },
  accounts: { title: '商户账户', description: '按商户汇总可用余额、冻结金额和累计收支' },
  payments: { title: '支付订单', description: '查看商户充值和订单支付形成的支付宝支付单' },
  ledger: { title: '资金流水', description: '查看平台全部不可变财务流水和账户余额变化', tableTitle: '全部资金流水', emptyText: '暂无资金流水' },
  'value-added': { title: '增值服务', description: '查看服务订阅、存储和其他平台增值服务流水', tableTitle: '增值服务流水', emptyText: '暂无增值服务流水' },
  shipping: { title: '快递费用', description: '查看寄出、归还物流产生的费用流水', tableTitle: '快递费用流水', emptyText: '暂无快递费用流水' },
  settlement: { title: '联盟结算', description: '查看联盟订单的商户结算金额和履约状态' },
  withdrawal: { title: '提现管理', description: '审核商户提现申请并维护账户冻结状态', tableTitle: '提现申请', emptyText: '暂无提现申请' },
  invoice: { title: '发票管理', description: '查看财务流水的开票状态和关联记录', tableTitle: '发票流水', emptyText: '暂无发票相关流水' },
  book: { title: '记账簿', description: '查看赔付、设备出售和冲正等账务记录', tableTitle: '记账簿', emptyText: '暂无记账记录' }
}

const financeDirections = platformFinanceDirectionOptions
const accountTypes = [{ value: 1, label: '增值服务余额' }, { value: 2, label: '履约保证金' }, { value: 3, label: '租金 / 分账资金' }]
const ledgerStatuses = platformFinanceLedgerStatusOptions
const paymentStatuses = platformPaymentStatusOptions
const settlementStatuses = [
  { label: '待确认', value: ALLIANCE_SETTLEMENT_STATUS.PENDING, tagType: 'warning' },
  { label: '已确认', value: ALLIANCE_SETTLEMENT_STATUS.CONFIRMED, tagType: 'success' },
  { label: '争议中', value: ALLIANCE_SETTLEMENT_STATUS.DISPUTED, tagType: 'danger' },
  { label: '已关闭', value: ALLIANCE_SETTLEMENT_STATUS.CLOSED, tagType: 'info' }
]
const withdrawalStatusOptions = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已完成' },
  { value: 2, label: '已拒绝' },
  { value: 3, label: '已取消' }
]
const invoiceApplicationTypes = [{ value: 1, label: '开票' }, { value: 2, label: '补票' }, { value: 3, label: '红冲' }]
const invoiceApplicationStatuses = [{ value: 0, label: '待审核' }, { value: 1, label: '已驳回' }, { value: 2, label: '待开具' }, { value: 3, label: '已开具' }, { value: 4, label: '已红冲' }]
const accountQuery = reactive({ pageNum: 1, pageSize: 10, merchantKeyword: '' })
const paymentQuery = reactive({ pageNum: 1, pageSize: 10, merchantKeyword: '', paymentNo: '', paymentStatus: undefined, range: [] })
const ledgerQuery = reactive({ pageNum: 1, pageSize: 10, merchantKeyword: '', ledgerNo: '', accountType: undefined, direction: undefined, ledgerStatus: undefined, range: [] })
const settlementQuery = reactive({ pageNum: 1, pageSize: 10, status: undefined, range: [] })
const withdrawalQuery = reactive({ pageNum: 1, pageSize: 10, withdrawalNo: route.query.withdrawalNo ? String(route.query.withdrawalNo) : '', merchantKeyword: '', withdrawalStatus: undefined, range: [] })

const pageKey = computed({
  get: () => pageItems.some(item => item.key === route.path.split('/').pop()) ? route.path.split('/').pop() : 'overview',
  set: value => navigateToPage(value)
})
const financeMenuBasePath = computed(() => {
  const currentPath = route.path.replace(/\/$/, '')
  const currentKey = pageItems.some(item => item.key === currentPath.split('/').pop())
    ? currentPath.split('/').pop()
    : ''
  return currentKey ? currentPath.slice(0, -(currentKey.length + 1)) : '/finance/finance'
})
const pageMeta = computed(() => pageDetails[pageKey.value] || pageDetails.overview)
const overviewCards = computed(() => [
  { key: 'netAmount', label: '平台净余额', value: money(summary.value.netAmount), help: '收入、支出、解冻和冲正后的净额', tone: 'blue' },
  { key: 'monthIncome', label: '本月收入', value: money(summary.value.monthIncome), help: '本月成功入账的收入', tone: 'green' },
  { key: 'monthExpense', label: '本月支出', value: money(summary.value.monthExpense), help: '本月成功入账的支出', tone: 'orange' },
  { key: 'frozenAmount', label: '冻结金额', value: money(summary.value.frozenAmount), help: '履约流程中暂不可用的金额', tone: 'purple' },
  { key: 'totalIncome', label: '累计收入', value: money(summary.value.totalIncome), help: '全部成功收入流水', tone: 'green' },
  { key: 'totalExpense', label: '累计支出', value: money(summary.value.totalExpense), help: '全部成功支出流水', tone: 'orange' },
  { key: 'pendingInvoiceCount', label: '待开票流水', value: `${summary.value.pendingInvoiceCount || 0} 笔`, help: '需要后续开票处理的流水', tone: 'warning' },
  { key: 'abnormalLedgerCount', label: '异常流水', value: `${summary.value.abnormalLedgerCount || 0} 笔`, help: '失败或已关闭流水', tone: 'danger' }
])

function navigateToPage(key) {
  const targetPath = `${financeMenuBasePath.value}/${key}`
  if (targetPath !== route.path) router.push(targetPath)
}

function rangeParams(range) {
  return { beginTime: range?.[0] ? `${range[0]} 00:00:00` : undefined, endTime: range?.[1] ? `${range[1]} 23:59:59` : undefined }
}

async function loadPage() {
  loading.value = true
  total.value = 0
  try {
    if (pageKey.value === 'overview') {
      const response = await getPlatformFinanceSummary()
      summary.value = response.data || {}
    } else if (pageKey.value === 'accounts') {
      const response = await listPlatformFinanceAccounts(accountQuery)
      accounts.value = response.rows || []
      total.value = Number(response.total || 0)
    } else if (pageKey.value === 'payments') {
      const params = { ...paymentQuery, ...rangeParams(paymentQuery.range) }
      delete params.range
      const response = await listPlatformPaymentOrders(params)
      paymentRows.value = response.rows || []
      total.value = Number(response.total || 0)
    } else if (pageKey.value === 'settlement') {
      const params = { status: settlementQuery.status, pageNum: settlementQuery.pageNum, pageSize: settlementQuery.pageSize, ...rangeParams(settlementQuery.range) }
      const response = await listPlatformFinanceSettlements(params)
      settlements.value = response.rows || []
      total.value = Number(response.total || 0)
    } else if (pageKey.value === 'withdrawal') {
      await loadWithdrawals()
    } else if (pageKey.value === 'invoice') {
      await loadInvoiceApplications()
    } else {
      const params = { ...ledgerQuery, scene: pageKey.value === 'ledger' ? undefined : pageKey.value, ...rangeParams(ledgerQuery.range) }
      delete params.range
      const response = await listPlatformFinanceLedger(params)
      ledgerRows.value = response.rows || []
      total.value = Number(response.total || 0)
    }
  } finally {
    loading.value = false
  }
}

async function loadWithdrawals() {
  loading.value = true
  try {
    const params = { ...withdrawalQuery, ...rangeParams(withdrawalQuery.range) }
    delete params.range
    const response = await listPlatformFinanceWithdrawals(params)
    withdrawalRows.value = response.rows || []
    withdrawalTotal.value = Number(response.total || 0)
  } finally {
    loading.value = false
  }
}

async function loadInvoiceApplications() {
  loading.value = true
  try {
    const params = { ...invoiceApplicationQuery, ...rangeParams(invoiceApplicationQuery.range) }
    delete params.range
    const response = await listPlatformInvoiceApplications(params)
    invoiceApplications.value = response.rows || []
    invoiceApplicationTotal.value = Number(response.total || 0)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  if (pageKey.value === 'accounts') accountQuery.pageNum = 1
  else if (pageKey.value === 'payments') paymentQuery.pageNum = 1
  else if (pageKey.value === 'settlement') settlementQuery.pageNum = 1
  else if (pageKey.value === 'withdrawal') withdrawalQuery.pageNum = 1
  else if (pageKey.value === 'invoice') invoiceApplicationQuery.pageNum = 1
  else ledgerQuery.pageNum = 1
  loadPage()
}

function resetQuery() {
  Object.assign(accountQuery, { pageNum: 1, merchantKeyword: '' })
  Object.assign(paymentQuery, { pageNum: 1, merchantKeyword: '', paymentNo: '', paymentStatus: undefined, range: [] })
  Object.assign(ledgerQuery, { pageNum: 1, merchantKeyword: '', ledgerNo: '', accountType: undefined, direction: undefined, ledgerStatus: undefined, range: [] })
  Object.assign(settlementQuery, { pageNum: 1, status: undefined, range: [] })
  Object.assign(withdrawalQuery, { pageNum: 1, withdrawalNo: '', merchantKeyword: '', withdrawalStatus: undefined, range: [] })
  Object.assign(invoiceApplicationQuery, { pageNum: 1, applicationNo: '', merchantKeyword: '', applicationStatus: undefined, applicationType: undefined, range: [] })
  loadPage()
}

async function openLedgerDetail(row) {
  ledgerDetailVisible.value = true
  detailLoading.value = true
  ledgerDetail.value = {}
  try {
    const response = await getPlatformFinanceLedger(row.ledgerNo)
    ledgerDetail.value = response.data || row
  } finally {
    detailLoading.value = false
  }
}

async function openPaymentDetail(row) {
  paymentDetailVisible.value = true
  detailLoading.value = true
  paymentDetail.value = {}
  try {
    const response = await getPlatformPaymentOrder(row.paymentNo)
    paymentDetail.value = response.data || row
  } finally {
    detailLoading.value = false
  }
}

async function openWithdrawalDetail(row) {
  withdrawalDetailVisible.value = true
  detailLoading.value = true
  withdrawalDetail.value = row
  try {
    const response = await getPlatformFinanceWithdrawal(row.withdrawalNo)
    withdrawalDetail.value = response.data || row
  } finally {
    detailLoading.value = false
  }
}

async function openInvoiceDetail(row) {
  invoiceDetailVisible.value = true
  detailLoading.value = true
  invoiceDetail.value = row
  try {
    const response = await getPlatformInvoiceApplication(row.applicationNo)
    invoiceDetail.value = response.data || row
  } finally {
    detailLoading.value = false
  }
}

function openInvoiceReview(row) {
  Object.assign(invoiceReviewForm, {
    applicationNo: row.applicationNo,
    merchantName: row.merchantName,
    amount: row.amount,
    invoiceTitle: row.invoiceTitle,
    approved: true,
    invoiceNo: '',
    invoiceCode: '',
    reviewRemark: ''
  })
  invoiceReviewVisible.value = true
}

function openInvoiceIssue(row) {
  Object.assign(invoiceIssueForm, { applicationNo: row.applicationNo, invoiceNo: '', invoiceCode: '', invoiceFileUrl: '' })
  invoiceIssueVisible.value = true
}

function openInvoiceRedFlush(row) {
  Object.assign(invoiceRedFlushForm, { applicationNo: row.applicationNo, reason: '' })
  invoiceRedFlushVisible.value = true
}

async function submitInvoiceReview() {
  if (!invoiceReviewForm.approved && !invoiceReviewForm.reviewRemark.trim()) {
    ElMessage.warning('驳回申请时必须填写审核备注')
    return
  }
  invoiceActionLoading.value = true
  try {
    await reviewPlatformInvoiceApplication(invoiceReviewForm.applicationNo, {
      approved: invoiceReviewForm.approved,
      invoiceNo: invoiceReviewForm.invoiceNo.trim() || undefined,
      invoiceCode: invoiceReviewForm.invoiceCode.trim() || undefined,
      reviewRemark: invoiceReviewForm.reviewRemark.trim() || undefined
    })
    ElMessage.success(invoiceReviewForm.approved ? '发票申请已审核' : '发票申请已驳回')
    invoiceReviewVisible.value = false
    await loadInvoiceApplications()
  } catch (error) {
    ElMessage.error(error?.message || '发票审核失败')
  } finally {
    invoiceActionLoading.value = false
  }
}

async function submitInvoiceIssue() {
  if (!invoiceIssueForm.invoiceNo.trim()) {
    ElMessage.warning('请填写发票号码')
    return
  }
  invoiceActionLoading.value = true
  try {
    await issuePlatformInvoiceApplication(invoiceIssueForm.applicationNo, {
      invoiceNo: invoiceIssueForm.invoiceNo.trim(),
      invoiceCode: invoiceIssueForm.invoiceCode.trim() || undefined,
      invoiceFileUrl: invoiceIssueForm.invoiceFileUrl.trim() || undefined
    })
    ElMessage.success('发票已完成开具')
    invoiceIssueVisible.value = false
    await loadInvoiceApplications()
  } catch (error) {
    ElMessage.error(error?.message || '发票开具失败')
  } finally {
    invoiceActionLoading.value = false
  }
}

async function submitInvoiceRedFlush() {
  if (!invoiceRedFlushForm.reason.trim()) {
    ElMessage.warning('请填写红冲原因')
    return
  }
  invoiceActionLoading.value = true
  try {
    await createPlatformInvoiceRedFlush(invoiceRedFlushForm.applicationNo, { reason: invoiceRedFlushForm.reason.trim() })
    ElMessage.success('红冲申请已提交')
    invoiceRedFlushVisible.value = false
    await loadInvoiceApplications()
  } catch (error) {
    ElMessage.error(error?.message || '红冲申请提交失败')
  } finally {
    invoiceActionLoading.value = false
  }
}

function openWithdrawalReview(row) {
  Object.assign(withdrawalReviewForm, {
    withdrawalNo: row.withdrawalNo,
    merchantName: row.merchantName,
    amount: row.amount,
    payeeAccount: row.payeeAccount,
    approved: true,
    reviewRemark: ''
  })
  withdrawalReviewVisible.value = true
}

async function submitWithdrawalReview() {
  if (!withdrawalReviewForm.approved && !withdrawalReviewForm.reviewRemark.trim()) {
    ElMessage.warning('拒绝提现时必须填写审核备注')
    return
  }
  withdrawalReviewLoading.value = true
  try {
    await reviewPlatformFinanceWithdrawal(withdrawalReviewForm.withdrawalNo, {
      approved: withdrawalReviewForm.approved,
      reviewRemark: withdrawalReviewForm.reviewRemark.trim()
    })
    ElMessage.success(withdrawalReviewForm.approved ? '提现已审核通过并完成出款' : '提现已拒绝，金额已解冻')
    withdrawalReviewVisible.value = false
    await loadWithdrawals()
  } catch (error) {
    ElMessage.error(error?.message || '提现审核失败')
  } finally {
    withdrawalReviewLoading.value = false
  }
}

function option(_, value, options) {
  return findRentalOption(options, value)
}

function allianceStatusName(value) {
  const labels = {
    [ALLIANCE_ORDER_STATUS.PENDING]: '待处理',
    [ALLIANCE_ORDER_STATUS.ACCEPTED]: '已接受',
    [ALLIANCE_ORDER_STATUS.REJECTED]: '已拒绝',
    [ALLIANCE_ORDER_STATUS.CANCELLED]: '已取消',
    [ALLIANCE_ORDER_STATUS.EXPIRED]: '已过期',
    [ALLIANCE_ORDER_STATUS.PUBLIC_POOL]: '公众池',
    [ALLIANCE_ORDER_STATUS.IN_FULFILLMENT]: '履约中',
    [ALLIANCE_ORDER_STATUS.COMPLETED]: '已完成',
    [ALLIANCE_ORDER_STATUS.EXCEPTION]: '异常'
  }
  return labels[value] || '未知状态'
}

function money(value) {
  const amount = Number(value || 0)
  return `¥${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function moneyOrDash(value) {
  return value === null || value === undefined || value === '' ? '-' : money(value)
}

function withdrawalTagType(status) {
  return { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[status] || 'info'
}

function invoiceApplicationTagType(status) {
  return { 0: 'warning', 1: 'danger', 2: 'info', 3: 'success', 4: 'danger' }[status] || 'info'
}

watch(() => route.path, loadPage, { immediate: true })
</script>

<style scoped>
.platform-finance-page { min-height: 100%; }
.finance-tabs { margin: 4px 0 18px; }
.summary-grid, .overview-grid { margin-bottom: 16px; }
.summary-card, .finance-panel { border-radius: 4px; }
.summary-card { min-height: 142px; }
.summary-label, .summary-help, .panel-note { color: var(--el-text-color-secondary); }
.summary-label { font-size: 14px; }
.summary-value { margin: 12px 0 8px; color: var(--el-text-color-primary); font-size: 25px; font-weight: 600; }
.summary-value.blue { color: var(--el-color-primary); }
.summary-value.green, .success-text { color: var(--el-color-success); }
.summary-value.orange { color: var(--el-color-warning); }
.summary-value.purple { color: #8e7dff; }
.summary-value.warning-text, .warning-text { color: var(--el-color-warning); }
.summary-value.danger, .danger-text { color: var(--el-color-danger); }
.summary-help { font-size: 12px; }
.panel-heading, .filter-bar, .task-row, .overview-metrics { display: flex; align-items: center; }
.panel-heading { justify-content: space-between; }
.filter-bar { flex-wrap: wrap; gap: 10px; margin-bottom: 16px; }
.compact-filter { width: 190px; }
.account-filter { width: 165px; }
.merchant-filter { width: 240px; }
.status-filter { width: 140px; }
.date-filter { width: 300px; }
.overview-metrics { flex-wrap: wrap; gap: 20px 42px; }
.overview-metrics div { display: flex; min-width: 150px; flex-direction: column; gap: 6px; }
.overview-metrics span { color: var(--el-text-color-secondary); font-size: 13px; }
.overview-metrics strong { color: var(--el-text-color-primary); font-size: 20px; }
.task-list { display: grid; gap: 8px; }
.task-row { min-height: 42px; gap: 14px; border-bottom: 1px solid var(--el-border-color-lighter); }
.task-row span { flex: 1; color: var(--el-text-color-regular); }
.task-row strong { min-width: 70px; text-align: right; }
.finance-alert { margin-top: 16px; }
@media (max-width: 1200px) {
  .date-filter, .merchant-filter { width: 220px; }
}
</style>
