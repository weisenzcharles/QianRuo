import request from '@/utils/request'

/** 分页查询平台可见的全部商户订单。 */
export function listPlatformOrders(query) {
  return request({
    url: '/admin/rental/order/list',
    method: 'get',
    params: query
  })
}

/** 查询平台订单详情及处理记录。 */
export function getPlatformOrderDetail(orderId) {
  return request({
    url: `/admin/rental/order/${orderId}`,
    method: 'get'
  })
}

/** 在独立权限和操作日志保护下查看平台订单完整客户信息。 */
export function viewPlatformOrderCustomer(orderId) {
  return request({
    url: `/admin/rental/order/${orderId}/customer`,
    method: 'post'
  })
}
