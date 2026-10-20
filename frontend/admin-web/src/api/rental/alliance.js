/** 平台联盟管理接口。 */
import request from '@/utils/request'

export function listAlliances(query) {
  return request({ url: '/admin/rental/order/alliance-management', method: 'get', params: query })
}

export function listAllianceMembers(allianceId) {
  return request({ url: `/admin/rental/order/alliance-management/${allianceId}/members`, method: 'get' })
}

export function listAllianceMemberScopes(allianceId, memberId) {
  return request({ url: `/admin/rental/order/alliance-management/${allianceId}/members/${memberId}/scopes`, method: 'get' })
}

export function updateAllianceStatus(data) {
  return request({ url: '/admin/rental/order/alliance-management/status', method: 'post', params: data })
}
