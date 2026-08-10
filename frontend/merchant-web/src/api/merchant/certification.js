import request from '@/utils/request'

/**
 * 查询当前登录商户的完整认证材料、会员类型和接单资格历史。
 * 商户主键由后端从登录上下文解析，前端不传入任何商户标识。
 *
 * @returns {Promise} 当前商户认证聚合资料
 */
export function getMerchantCertification() {
  return request({
    url: '/merchant/certification',
    method: 'get'
  })
}
