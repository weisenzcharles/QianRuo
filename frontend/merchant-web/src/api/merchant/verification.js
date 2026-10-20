import request from '@/utils/request'

/** 校验极速核销台单次扫码，不修改订单和库存。 */
export function scanMerchantVerification(data) {
  return request({ url: '/merchant/verification/scan', method: 'post', data })
}

/** 提交极速核销台扫码入库结果。 */
export function commitMerchantVerificationInbound(data) {
  return request({ url: '/merchant/verification/inbound/commit', method: 'post', data })
}

/** 提交极速核销台扫码出库结果。 */
export function commitMerchantVerificationOutbound(data) {
  return request({ url: '/merchant/verification/outbound/commit', method: 'post', data })
}
