import request from '@/utils/request'

/** 查询库存告警规则。 */
export function listInventoryAlertRules(params) {
  return request({ url: '/merchant/inventory/governance/alerts/rules', method: 'get', params })
}

/** 新增或修改库存告警规则。 */
export function saveInventoryAlertRule(data) {
  return request({ url: '/merchant/inventory/governance/alerts/rules', method: 'put', data })
}

/** 删除库存告警规则。 */
export function deleteInventoryAlertRule(ruleId) {
  return request({ url: `/merchant/inventory/governance/alerts/rules/${ruleId}`, method: 'delete' })
}

/** 查询并刷新库存告警事件。 */
export function listInventoryAlertEvents(params) {
  return request({ url: '/merchant/inventory/governance/alerts/events', method: 'get', params })
}

/** 确认库存告警事件。 */
export function acknowledgeInventoryAlert(alertId) {
  return request({ url: `/merchant/inventory/governance/alerts/events/${alertId}/ack`, method: 'post' })
}

/** 查询外部库存同步令牌。 */
export function listInventorySyncTokens() {
  return request({ url: '/merchant/inventory/governance/sync/tokens', method: 'get' })
}

/** 查询外部库存同步调用记录。 */
export function listInventorySyncLogs(params) {
  return request({ url: '/merchant/inventory/governance/sync/logs', method: 'get', params })
}

/** 创建外部库存同步令牌。 */
export function createInventorySyncToken(data) {
  return request({ url: '/merchant/inventory/governance/sync/tokens', method: 'post', data })
}

/** 吊销外部库存同步令牌。 */
export function revokeInventorySyncToken(tokenId) {
  return request({ url: `/merchant/inventory/governance/sync/tokens/${tokenId}`, method: 'delete' })
}

/** 修改库存可接单口径。 */
export function updateMerchantInventoryPolicy(data) {
  return request({ url: '/merchant/inventory/policy', method: 'put', data })
}
