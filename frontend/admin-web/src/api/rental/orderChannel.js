import request from '@/utils/request'

/** 查询平台联盟订单渠道。 */
export function listPlatformOrderChannels(params) {
  return request({ url: '/admin/rental/order-channel/list', method: 'get', params })
}

/** 新增平台联盟订单渠道。 */
export function addPlatformOrderChannel(data) {
  return request({ url: '/admin/rental/order-channel', method: 'post', data })
}

/** 修改平台联盟订单渠道。 */
export function updatePlatformOrderChannel(data) {
  return request({ url: '/admin/rental/order-channel', method: 'put', data })
}

/** 软删除平台联盟订单渠道。 */
export function deletePlatformOrderChannel(channelId) {
  return request({ url: `/admin/rental/order-channel/${channelId}`, method: 'delete' })
}
