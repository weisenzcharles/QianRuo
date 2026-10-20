import request from '@/utils/request'

/**
 * 查询平台允许商户选择的有效产品分类树。
 *
 * @returns {Promise} 平台有效产品分类树
 */
export function listProductClassOptions() {
  return request({
    url: '/merchant/class/options',
    method: 'get'
  })
}

/**
 * 查询当前登录商户已选择的经营分类编号。
 *
 * @returns {Promise} 当前商户经营分类编号集合
 */
export function listSelectedProductClassIds() {
  return request({
    url: '/merchant/class/selected',
    method: 'get'
  })
}

/**
 * 查询当前登录商户业务可见的产品分类树。
 *
 * @returns {Promise} 当前商户业务分类树
 */
export function listBusinessProductClasses() {
  return request({
    url: '/merchant/class/tree',
    method: 'get'
  })
}

/**
 * 保存当前登录商户选择的经营分类。
 *
 * @param {Array<number>} classIds 平台产品分类编号集合
 * @returns {Promise} 保存结果
 */
export function saveSelectedProductClasses(classIds) {
  return request({
    url: '/merchant/class/selected',
    method: 'put',
    data: { classIds }
  })
}
