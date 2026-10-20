import request from '@/utils/request'

/** 查询当前商户需要确认的有效协议版本。 */
export function listMerchantProtocols(params) {
  return request({ url: '/merchant/protocol/list', method: 'get', params })
}

/** 确认当前商户协议版本。 */
export function acceptMerchantProtocol(data) {
  return request({ url: '/merchant/protocol/accept', method: 'post', data })
}
