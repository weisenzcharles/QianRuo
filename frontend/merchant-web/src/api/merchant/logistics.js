import request from '@/utils/request'

/** 查询当前商户有权访问的物流治理台账。 */
export function listMerchantLogistics(params) {
  return request({ url: '/merchant/logistics/list', method: 'get', params })
}

/** 查询订单指定方向的已缓存物流轨迹。 */
export function getMerchantLogisticsTrack(orderNo, direction) {
  return request({
    url: '/merchant/logistics/track',
    method: 'get',
    params: { orderNo, direction }
  })
}

/** 强制刷新订单指定方向的物流轨迹。 */
export function refreshMerchantLogistics(orderNo, direction) {
  return request({
    url: '/merchant/logistics/refresh',
    method: 'post',
    params: { orderNo, direction }
  })
}
