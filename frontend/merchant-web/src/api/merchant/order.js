import request from '@/utils/request'

/**
 * 查询当前商户的订单列表。
 *
 * 订单归属、租户和仓库范围由后端从登录上下文确定，前端只提交筛选条件。
 *
 * @param {Object} query 订单筛选条件
 * @returns {Promise} 当前商户订单分页数据
 */
export function listMerchantOrders(query) {
  return request({
    url: '/merchant/orders',
    method: 'get',
    params: query
  })
}
