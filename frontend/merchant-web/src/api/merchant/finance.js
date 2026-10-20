import request from '@/utils/request'

/** 查询商户财务总览。 */
export function getMerchantFinanceOverview() {
  return request({ url: '/merchant/finance/overview', method: 'get' })
}

/** 查询财务分户余额。 */
export function listMerchantFinanceAccounts() {
  return request({ url: '/merchant/finance/accounts', method: 'get' })
}

/** 查询资金流水。 */
export function listMerchantFinanceLedger(params) {
  return request({ url: '/merchant/finance/ledger', method: 'get', params })
}

/** 查询资金流水详情。 */
export function getMerchantFinanceLedger(ledgerNo) {
  return request({ url: `/merchant/finance/ledger/${ledgerNo}`, method: 'get' })
}

/** 查询租金与收入。 */
export function listMerchantFinanceRevenue(params) {
  return request({ url: '/merchant/finance/revenue', method: 'get', params })
}

/** 查询快递费用。 */
export function listMerchantFinanceShipping(params) {
  return request({ url: '/merchant/finance/shipping', method: 'get', params })
}

/** 查询联盟结算。 */
export function listMerchantFinanceSettlements(params) {
  return request({ url: '/merchant/finance/settlements', method: 'get', params })
}

/** 查询提现记录。 */
export function listMerchantFinanceWithdrawals(params) {
  return request({ url: '/merchant/finance/withdrawals', method: 'get', params })
}

/** 提交提现申请。 */
export function applyMerchantFinanceWithdrawal(data) {
  return request({ url: '/merchant/finance/withdrawals', method: 'post', data })
}

/** 查询提现申请详情。 */
export function getMerchantFinanceWithdrawal(withdrawalNo) {
  return request({ url: `/merchant/finance/withdrawals/${encodeURIComponent(withdrawalNo)}`, method: 'get' })
}

/** 查询发票相关流水。 */
export function listMerchantFinanceInvoices(params) {
  return request({ url: '/merchant/finance/invoices', method: 'get', params })
}

/** 查询开票申请。 */
export function listMerchantInvoiceApplications(params) {
  return request({ url: '/merchant/finance/invoice-applications', method: 'get', params })
}

/** 查询开票申请详情。 */
export function getMerchantInvoiceApplication(applicationNo) {
  return request({ url: `/merchant/finance/invoice-applications/${encodeURIComponent(applicationNo)}`, method: 'get' })
}

/** 提交开票或补票申请。 */
export function createMerchantInvoiceApplication(data) {
  return request({ url: '/merchant/finance/invoice-applications', method: 'post', data })
}

/** 查询记账簿。 */
export function listMerchantFinanceBook(params) {
  return request({ url: '/merchant/finance/book', method: 'get', params })
}

/** 创建支付宝账户充值支付单。 */
export function createMerchantFinanceRecharge(amount) {
  return request({
    url: '/merchant/payments/recharge',
    method: 'post',
    data: { amount }
  })
}

/** 查询支付宝支付单状态。 */
export function getMerchantPayment(paymentNo) {
  return request({ url: `/merchant/payments/${paymentNo}`, method: 'get' })
}
