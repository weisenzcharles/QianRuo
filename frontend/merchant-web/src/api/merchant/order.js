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

/** 创建当前商户订单，租户和商户归属由后端登录上下文补齐。 */
export function addMerchantOrder(data) {
  return request({ url: '/merchant/orders', method: 'post', data })
}

/** 修改当前商户订单，后端会再次校验订单归属、审核状态和仓库权限。 */
export function updateMerchantOrder(data) {
  return request({ url: '/merchant/orders', method: 'put', data })
}

/** 查询当前商户订单详情，用于编辑页面回显。 */
export function getMerchantOrder(orderId) {
  return request({ url: `/merchant/orders/${orderId}`, method: 'get' })
}
