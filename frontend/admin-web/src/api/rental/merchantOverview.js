import request from '@/utils/request'

/** 分页查询平台可见的商户经营概况。 */
export function listPlatformMerchantOverviews(query) {
  return request({
    url: '/admin/rental/merchant-overview/list',
    method: 'get',
    params: query
  })
}

/** 查询指定商户的仓库、商品、套餐和库存只读明细。 */
export function getPlatformMerchantBusiness(memberCode) {
  return request({
    url: `/admin/rental/merchant-overview/${encodeURIComponent(memberCode)}/business`,
    method: 'get'
  })
}

/** 查询指定商户的履约、物流和风险治理汇总。 */
export function getPlatformMerchantGovernance(memberCode) {
  return request({
    url: `/admin/rental/merchant-overview/${encodeURIComponent(memberCode)}/governance`,
    method: 'get'
  })
}
