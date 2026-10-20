import request from '@/utils/request'

/** 查询平台配置的启用快递公司及其单号校验规则。 */
export function listMerchantExpressCompanies() {
  return request({ url: '/merchant/express-companies', method: 'get' })
}
