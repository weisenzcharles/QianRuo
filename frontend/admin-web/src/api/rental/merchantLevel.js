import request from '@/utils/request'

/** @returns {Promise} 商户等级列表 */
export function listMerchantLevel() {
  return request({ url: '/admin/platform/merchant-level/list', method: 'get' })
}
/** @param {number|string} id 等级编号 @returns {Promise} 等级详情 */
export function getMerchantLevel(id) {
  return request({ url: `/admin/platform/merchant-level/${id}`, method: 'get' })
}
/** @param {object} data 等级配置 @returns {Promise} 新增结果 */
export function addMerchantLevel(data) {
  return request({ url: '/admin/platform/merchant-level', method: 'post', data })
}
/** @param {object} data 等级配置 @returns {Promise} 修改结果 */
export function updateMerchantLevel(data) {
  return request({ url: '/admin/platform/merchant-level', method: 'put', data })
}
/** @param {number|string} id 等级编号 @returns {Promise} 删除结果 */
export function delMerchantLevel(id) {
  return request({ url: `/admin/platform/merchant-level/${id}`, method: 'delete' })
}
