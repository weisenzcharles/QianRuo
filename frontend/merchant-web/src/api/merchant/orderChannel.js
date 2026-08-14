import request from '@/utils/request'

/** 查询平台联盟渠道和当前商户自定义渠道。 */
export function listMerchantOrderChannels(params) {
  return request({ url: '/merchant/order-channel/list', method: 'get', params })
}

/** 新增当前商户自定义订单渠道。 */
export function addMerchantOrderChannel(data) {
  return request({ url: '/merchant/order-channel', method: 'post', data })
}

/** 修改当前商户自定义订单渠道。 */
export function updateMerchantOrderChannel(data) {
  return request({ url: '/merchant/order-channel', method: 'put', data })
}

/** 软删除当前商户自定义订单渠道。 */
export function deleteMerchantOrderChannel(channelId) {
  return request({ url: `/merchant/order-channel/${channelId}`, method: 'delete' })
}

/** 查询套餐当前渠道绑定。 */
export function listPackageChannels(packageId) {
  return request({ url: `/merchant/order-channel/package/${packageId}`, method: 'get' })
}

/** 将套餐默认渠道转移到目标渠道。 */
export function transferPackageChannel(data) {
  return request({ url: '/merchant/order-channel/package/transfer', method: 'post', data })
}

/** 批量将订单转移到目标渠道。 */
export function transferOrderChannels(data) {
  return request({ url: '/merchant/order-channel/orders/transfer', method: 'post', data })
}
