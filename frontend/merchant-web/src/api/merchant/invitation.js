import request from '@/utils/request'

/**
 * 查询当前商户的邀请码、邀请名额和邀请记录。
 *
 * @returns {Promise} 当前商户邀请概览
 */
export function getInvitationOverview() {
  return request({ url: '/merchant/invitation/overview', method: 'get' })
}
