import request from '@/utils/request'

/**
 * 查询当前登录商户的完整认证材料、会员类型和接单资格历史。
 * 商户编号由后端从登录上下文解析，前端不传入任何商户标识。
 *
 * @returns {Promise} 当前商户认证聚合资料
 */
export function getMerchantCertification() {
  return request({
    url: '/merchant/certification',
    method: 'get'
  })
}

/**
 * 提交平台要求补交的认证材料，服务端会生成新的待审核认证版本。
 *
 * @param {{materials: Array<{materialType: number, fileUrl: string}>}} data 补件材料
 * @returns {Promise} 更新后的认证聚合资料
 */
export function submitMerchantCertificationSupplement(data) {
  return request({
    url: '/merchant/certification/supplement',
    method: 'post',
    data
  })
}

/** 查询当前商户信用摘要和可申诉流水。 */
export function getMerchantCreditRisk() {
  return request({
    url: '/merchant/certification/credit-risk',
    method: 'get'
  })
}

/** 提交信用流水申诉，平台审核通过后追加冲正流水。 */
export function submitMerchantCreditAppeal(data) {
  return request({
    url: '/merchant/certification/credit-appeals',
    method: 'post',
    data
  })
}
