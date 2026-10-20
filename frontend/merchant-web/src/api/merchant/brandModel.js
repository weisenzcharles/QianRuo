import request from '@/utils/request'

/**
 * 查询当前商户经营分类内的标准型号及常用选择状态。
 *
 * @returns {Promise} 商户可用标准型号列表
 */
export function listBrandModelOptions() {
  return request({
    url: '/merchant/brand-model/options',
    method: 'get'
  })
}

/**
 * 查询当前商户已选择的常用型号编号。
 *
 * @returns {Promise} 常用型号编号集合
 */
export function listSelectedBrandModelIds() {
  return request({
    url: '/merchant/brand-model/selected',
    method: 'get'
  })
}

/**
 * 保存当前商户常用型号选择。
 *
 * @param {Array<number>} modelIds 标准型号编号集合
 * @returns {Promise} 保存结果
 */
export function saveSelectedBrandModels(modelIds) {
  return request({
    url: '/merchant/brand-model/selected',
    method: 'put',
    data: { modelIds }
  })
}
