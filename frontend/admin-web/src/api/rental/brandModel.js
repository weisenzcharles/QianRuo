import request from '@/utils/request'

/**
 * 查询平台标准品牌型号列表。
 *
 * @param {object} query 品牌、型号、分类和状态查询条件
 * @returns {Promise} 分页标准品牌型号列表
 */
export function listBrandModels(query) {
  return request({
    url: '/admin/rental/brand-model/list',
    method: 'get',
    params: query
  })
}

/**
 * 查询指定平台标准品牌型号详情。
 *
 * @param {number|string} modelId 标准型号编号
 * @returns {Promise} 标准型号详情
 */
export function getBrandModel(modelId) {
  return request({
    url: '/admin/rental/brand-model/' + modelId,
    method: 'get'
  })
}

/**
 * 新增平台标准品牌型号。
 *
 * @param {object} data 标准型号表单
 * @returns {Promise} 新增结果
 */
export function addBrandModel(data) {
  return request({
    url: '/admin/rental/brand-model',
    method: 'post',
    data
  })
}

/**
 * 修改平台标准品牌型号。
 *
 * @param {object} data 标准型号表单
 * @returns {Promise} 修改结果
 */
export function updateBrandModel(data) {
  return request({
    url: '/admin/rental/brand-model',
    method: 'put',
    data
  })
}

/**
 * 软删除平台标准品牌型号。
 *
 * @param {number|string} modelId 标准型号编号
 * @returns {Promise} 删除结果
 */
export function deleteBrandModel(modelId) {
  return request({
    url: '/admin/rental/brand-model/' + modelId,
    method: 'delete'
  })
}
