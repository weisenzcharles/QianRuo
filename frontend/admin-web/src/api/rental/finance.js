import request from '@/utils/request'

/** 查询平台财务总览。 */
export function getPlatformFinanceSummary() {
  return request({ url: '/admin/rental/finance/summary', method: 'get' })
}

/** 分页查询平台商户账户汇总。 */
export function listPlatformFinanceAccounts(query) {
  return request({ url: '/admin/rental/finance/accounts', method: 'get', params: query })
}

/** 分页查询平台支付订单。 */
export function listPlatformPaymentOrders(query) {
  return request({ url: '/admin/rental/finance/payments', method: 'get', params: query })
}

/** 查询平台支付订单详情。 */
export function getPlatformPaymentOrder(paymentNo) {
  return request({ url: `/admin/rental/finance/payments/${encodeURIComponent(paymentNo)}`, method: 'get' })
}

/** 分页查询商户提现申请。 */
export function listPlatformFinanceWithdrawals(query) {
  return request({ url: '/admin/rental/finance/withdrawals', method: 'get', params: query })
}

/** 查询商户提现申请详情。 */
export function getPlatformFinanceWithdrawal(withdrawalNo) {
  return request({ url: `/admin/rental/finance/withdrawals/${encodeURIComponent(withdrawalNo)}`, method: 'get' })
}

/** 审核商户提现申请。 */
export function reviewPlatformFinanceWithdrawal(withdrawalNo, data) {
  return request({
    url: `/admin/rental/finance/withdrawals/${encodeURIComponent(withdrawalNo)}/review`,
    method: 'post',
    data
  })
}

/** 分页查询平台资金流水。 */
export function listPlatformFinanceLedger(query) {
  return request({ url: '/admin/rental/finance/ledger', method: 'get', params: query })
}

/** 查询平台资金流水详情。 */
export function getPlatformFinanceLedger(ledgerNo) {
  return request({ url: `/admin/rental/finance/ledger/${encodeURIComponent(ledgerNo)}`, method: 'get' })
}

/** 分页查询联盟结算明细。 */
export function listPlatformFinanceSettlements(query) {
  return request({ url: '/admin/rental/finance/settlements', method: 'get', params: query })
}

/** 分页查询平台开票申请。 */
export function listPlatformInvoiceApplications(query) {
  return request({ url: '/admin/rental/finance/invoice-applications', method: 'get', params: query })
}

/** 查询平台开票申请详情。 */
export function getPlatformInvoiceApplication(applicationNo) {
  return request({ url: `/admin/rental/finance/invoice-applications/${encodeURIComponent(applicationNo)}`, method: 'get' })
}

/** 审核平台开票申请。 */
export function reviewPlatformInvoiceApplication(applicationNo, data) {
  return request({ url: `/admin/rental/finance/invoice-applications/${encodeURIComponent(applicationNo)}/review`, method: 'post', data })
}

/** 为待开具申请补录发票。 */
export function issuePlatformInvoiceApplication(applicationNo, data) {
  return request({ url: `/admin/rental/finance/invoice-applications/${encodeURIComponent(applicationNo)}/issue`, method: 'post', data })
}

/** 创建发票红冲申请。 */
export function createPlatformInvoiceRedFlush(applicationNo, data) {
  return request({ url: `/admin/rental/finance/invoice-applications/${encodeURIComponent(applicationNo)}/red-flush`, method: 'post', data })
}
