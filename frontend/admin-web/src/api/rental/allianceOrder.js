import request from '@/utils/request'

/** 分页查询平台联盟协助订单。 */
export function listAllianceOrders(query) {
  return request({ url: '/admin/rental/order/alliance', method: 'get', params: query })
}

/** 查询平台联盟订单详情、事件和结算记录。 */
export function getAllianceOrderDetail(allianceOrderId) {
  return request({ url: `/admin/rental/order/alliance/${allianceOrderId}`, method: 'get' })
}

/** 查询平台联盟结算汇总。 */
export function getAllianceSettlementSummary() {
  return request({ url: '/admin/rental/order/alliance/settlements/summary', method: 'get' })
}

export function listAllianceDisputes(query) {
  return request({ url: '/admin/rental/order/alliance/disputes', method: 'get', params: query })
}

export function getAllianceDisputeDetail(disputeId) {
  return request({ url: `/admin/rental/order/alliance/disputes/${disputeId}`, method: 'get' })
}

export function proposeAllianceMediation(data) {
  return request({ url: '/admin/rental/order/alliance/disputes/mediation', method: 'post', data })
}
