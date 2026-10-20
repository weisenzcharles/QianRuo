import request from '@/utils/request'

/** 查询平台配置的商户端菜单模板。 */
export function listMerchantMenu(query) {
  return request({ url: '/admin/rental/merchantMenu/list', method: 'get', params: query })
}

/** 查询商户端菜单详情。 */
export function getMerchantMenu(menuId) {
  return request({ url: `/admin/rental/merchantMenu/${menuId}`, method: 'get' })
}

/** 新增商户端菜单。 */
export function addMerchantMenu(data) {
  return request({ url: '/admin/rental/merchantMenu', method: 'post', data })
}

/** 修改商户端菜单。 */
export function updateMerchantMenu(data) {
  return request({ url: '/admin/rental/merchantMenu', method: 'put', data })
}

/** 软删除商户端菜单。 */
export function deleteMerchantMenu(menuId) {
  return request({ url: `/admin/rental/merchantMenu/${menuId}`, method: 'delete' })
}
