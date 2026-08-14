import request from '@/utils/request'

/** @returns {Promise} 平台邀请码及邀请记录列表 */
export function listPromotion() {
  return request({ url: '/admin/platform/promotion/list', method: 'get' })
}
