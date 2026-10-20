import request from '@/utils/request'

export function listPlatformAfterSales(query) {
  return request({ url: '/admin/rental/after-sales/list', method: 'get', params: query })
}

export function getPlatformAfterSales(caseId) {
  return request({ url: `/admin/rental/after-sales/${caseId}`, method: 'get' })
}

export function appendPlatformAfterSalesEvent(caseId, data) {
  return request({ url: `/admin/rental/after-sales/${caseId}/events`, method: 'post', data })
}

export function appendPlatformAfterSalesEvidence(caseId, data) {
  return request({ url: `/admin/rental/after-sales/${caseId}/evidence`, method: 'post', data })
}
