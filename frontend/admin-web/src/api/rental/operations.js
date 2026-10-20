import request from '@/utils/request'

/** 查询平台经营分析与运营风控概览。 */
export function getPlatformOperationsOverview(query) {
  return request({ url: '/admin/rental/operations/overview', method: 'get', params: query })
}

/** 确认平台运营风险事件。 */
export function acknowledgePlatformRisk(riskEventId) {
  return request({ url: `/admin/rental/operations/risks/${riskEventId}/acknowledge`, method: 'post' })
}

/** 查询平台运维告警事件。 */
export function listPlatformAlertEvents(query) {
  return request({ url: '/admin/rental/operations/alerts', method: 'get', params: query })
}

/** 确认平台运维告警事件。 */
export function acknowledgePlatformAlert(alertEventId) {
  return request({ url: `/admin/rental/operations/alerts/${alertEventId}/acknowledge`, method: 'post' })
}

/** 解除平台运维告警事件。 */
export function resolvePlatformAlert(alertEventId, data) {
  return request({ url: `/admin/rental/operations/alerts/${alertEventId}/resolve`, method: 'post', data })
}

/** 查询平台运维台账摘要。 */
export function getPlatformOperationsLedgerSummary() {
  return request({ url: '/admin/rental/operations/ledger/summary', method: 'get' })
}

/** 查询平台备份记录。 */
export function listPlatformBackups(query) {
  return request({ url: '/admin/rental/operations/backups', method: 'get', params: query })
}

/** 登记平台备份结果。 */
export function registerPlatformBackup(data) {
  return request({ url: '/admin/rental/operations/backups', method: 'post', data })
}

/** 查询平台容量流水。 */
export function listPlatformCapacity(query) {
  return request({ url: '/admin/rental/operations/capacity', method: 'get', params: query })
}

/** 登记平台容量快照。 */
export function registerPlatformCapacity(data) {
  return request({ url: '/admin/rental/operations/capacity', method: 'post', data })
}
