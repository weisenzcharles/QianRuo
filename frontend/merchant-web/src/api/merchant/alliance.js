import request from '@/utils/request'

export function getAlliance() {
  return request({ url: '/merchant/alliances', method: 'get' })
}

export function createAlliance(data) {
  return request({ url: '/merchant/alliances', method: 'post', data })
}

export function addAllianceMember(data) {
  return request({ url: '/merchant/alliances/members', method: 'post', data })
}

export function listAllianceMemberOptions() {
  return request({ url: '/merchant/alliances/member-options', method: 'get' })
}

export function updateAllianceMember(memberId, data) {
  return request({ url: `/merchant/alliances/members/${memberId}`, method: 'put', data })
}

export function removeAllianceMember(memberId) {
  return request({ url: `/merchant/alliances/members/${memberId}`, method: 'delete' })
}

export function getAllianceScopes() {
  return request({ url: '/merchant/alliance-scopes', method: 'get' })
}

export function saveAllianceWarehouseScope(data) {
  return request({ url: '/merchant/alliance-scopes/warehouses', method: 'post', data })
}

export function saveAllianceProductScope(data) {
  return request({ url: '/merchant/alliance-scopes/products', method: 'post', data })
}

export function removeAllianceWarehouseScope(id) {
  return request({ url: `/merchant/alliance-scopes/warehouses/${id}`, method: 'delete' })
}

export function removeAllianceProductScope(id) {
  return request({ url: `/merchant/alliance-scopes/products/${id}`, method: 'delete' })
}

export function listSharedInventory(params) {
  return request({ url: '/merchant/inventory/shared', method: 'get', params })
}

export function listAllianceTasks(params) {
  return request({ url: '/merchant/orders/alliance-tasks', method: 'get', params })
}

export function getAllianceTaskSummary(params) {
  return request({ url: '/merchant/orders/alliance-tasks/summary', method: 'get', params })
}

export function acceptAllianceTask(id, data) {
  return request({ url: `/merchant/orders/alliance-tasks/${id}/accept`, method: 'post', data })
}

export function changeAllianceWarehouse(id, data) {
  return request({ url: `/merchant/orders/alliance-tasks/${id}/warehouse`, method: 'post', data })
}

export function rejectAllianceTask(id) {
  return request({ url: `/merchant/orders/alliance-tasks/${id}/reject`, method: 'post' })
}

export function transferAllianceTask(id, data) {
  return request({ url: `/merchant/orders/alliance-tasks/${id}/transfer`, method: 'post', data })
}

export function cancelAllianceTask(id) {
  return request({ url: `/merchant/orders/alliance-tasks/${id}/cancel`, method: 'post' })
}


export function publishAlliancePublicPool(id) {
  return request({ url: `/merchant/orders/alliance-tasks/${id}/public-pool`, method: 'post' })
}

export function listAllianceDisputes(params) {
  return request({ url: '/merchant/alliance-disputes', method: 'get', params })
}

export function getAllianceDispute(id) {
  return request({ url: `/merchant/alliance-disputes/${id}`, method: 'get' })
}

export function createAllianceDispute(data) {
  return request({ url: '/merchant/alliance-disputes', method: 'post', data })
}

export function addAllianceDisputeEvidence(id, data) {
  return request({ url: `/merchant/alliance-disputes/${id}/evidence`, method: 'post', data })
}

export function confirmAllianceMediation(id, data) {
  return request({ url: `/merchant/alliance-disputes/${id}/mediation/confirm`, method: 'post', data })
}
