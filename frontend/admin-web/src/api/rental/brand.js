import request from '@/utils/request'

/**
 * 查询平台品牌主数据。
 *
 * @param {object} query 品牌名称和状态查询条件
 * @returns {Promise} 分页品牌列表
 */
export function listBrands(query) {
  return request({
    url: '/admin/rental/brand/list',
    method: 'get',
    params: query
  })
}

/**
 * 查询平台品牌详情。
 *
 * @param {number|string} brandId 品牌编号
 * @returns {Promise} 品牌详情
 */
export function getBrand(brandId) {
  return request({
    url: '/admin/rental/brand/' + brandId,
    method: 'get'
  })
}

/**
 * 新增平台品牌。
 *
 * @param {object} data 品牌表单
 * @returns {Promise} 新增结果
 */
export function addBrand(data) {
  return request({
    url: '/admin/rental/brand',
    method: 'post',
    data
  })
}

/**
 * 修改平台品牌。
 *
 * @param {object} data 品牌表单
 * @returns {Promise} 修改结果
 */
export function updateBrand(data) {
  return request({
    url: '/admin/rental/brand',
    method: 'put',
    data
  })
}

/**
 * 软删除平台品牌。
 *
 * @param {number|string} brandId 品牌编号
 * @returns {Promise} 删除结果
 */
export function deleteBrand(brandId) {
  return request({
    url: '/admin/rental/brand/' + brandId,
    method: 'delete'
  })
}
