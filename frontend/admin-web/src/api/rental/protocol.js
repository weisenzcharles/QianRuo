import request from '@/utils/request'

/** 查询平台协议全部版本。 */
export function listPlatformProtocols(params) {
  return request({ url: '/platform/protocol/list', method: 'get', params })
}

/** 新增平台协议草稿。 */
export function addPlatformProtocol(data) {
  return request({ url: '/platform/protocol', method: 'post', data })
}

/** 修改平台协议草稿。 */
export function updatePlatformProtocol(data) {
  return request({ url: '/platform/protocol', method: 'put', data })
}

/** 发布指定平台协议版本。 */
export function publishPlatformProtocol(protocolId) {
  return request({ url: `/platform/protocol/${protocolId}/publish`, method: 'post' })
}
