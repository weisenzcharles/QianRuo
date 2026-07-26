import request from '@/utils/request'

const publicHeaders = { isToken: false }

/**
 * 请求商户入驻短信验证码。当前后端未接入短信供应商，仅校验手机号并保留接口契约。
 *
 * @param {string} phone 商户申请手机号
 * @returns {Promise} 验证码请求结果
 */
export function requestRegistrationCode(phone) {
  return request({
    url: '/merchant/public/registration/code',
    method: 'post',
    headers: publicHeaders,
    data: { phone }
  })
}

/**
 * 提交新的商户入驻申请，租户、商户主键和审核状态由平台服务生成。
 *
 * @param {object} data 商户入驻资料和四位验证码
 * @returns {Promise} 待审核申请资料
 */
export function submitRegistration(data) {
  return request({
    url: '/merchant/public/registration',
    method: 'post',
    headers: publicHeaders,
    data
  })
}

/**
 * 使用手机号和验证码查询已提交的商户申请。
 *
 * @param {{phone: string, code: string}} data 手机号验证码凭证
 * @returns {Promise} 当前申请状态及资料
 */
export function lookupRegistration(data) {
  return request({
    url: '/merchant/public/registration/lookup',
    method: 'post',
    headers: publicHeaders,
    data
  })
}

/**
 * 修改并重新提交审核未通过的商户申请。
 *
 * @param {object} data 修改后的完整申请资料和四位验证码
 * @returns {Promise} 重新进入待审核状态的申请资料
 */
export function resubmitRegistration(data) {
  return request({
    url: '/merchant/public/registration',
    method: 'put',
    headers: publicHeaders,
    data
  })
}
