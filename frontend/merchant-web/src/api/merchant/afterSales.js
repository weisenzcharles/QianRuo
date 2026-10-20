import request from '@/utils/request'

/** 查询当前商户售后单列表。 */
export function listMerchantAfterSales(query) {
  return request({ url: '/merchant/after-sales/list', method: 'get', params: query })
}

/** 查询售后单详情。 */
export function getMerchantAfterSales(caseNo) {
  return request({ url: `/merchant/after-sales/${caseNo}`, method: 'get' })
}

/** 完成售后单。 */
export function completeMerchantAfterSales(caseNo, data) {
  return request({
    url: `/merchant/after-sales/${caseNo}/complete`,
    method: 'put',
    data
  })
}

/** 查询售后处理时间线。 */
export function listMerchantAfterSalesEvents(caseNo) {
  return request({ url: `/merchant/after-sales/${caseNo}/events`, method: 'get' })
}

/** 查询售后证据记录。 */
export function listMerchantAfterSalesEvidence(caseNo) {
  return request({ url: `/merchant/after-sales/${caseNo}/evidence`, method: 'get' })
}

/** 追加售后处理记录。 */
export function addMerchantAfterSalesEvent(caseNo, data) {
  return request({
    url: `/merchant/after-sales/${caseNo}/events`,
    method: 'post',
    data
  })
}

/** 追加售后证据。 */
export function addMerchantAfterSalesEvidence(caseNo, data) {
  return request({
    url: `/merchant/after-sales/${caseNo}/evidence`,
    method: 'post',
    data
  })
}

/** 申请平台介入并创建或复用平台客服工单。 */
export function requestMerchantAfterSalesPlatformTicket(caseNo) {
  return request({
    url: `/merchant/after-sales/${caseNo}/platform-ticket`,
    method: 'post'
  })
}
