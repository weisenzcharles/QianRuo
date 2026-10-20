import request from '@/utils/request'

/**
 * 查询平台产品分类树。
 *
 * @param {object} query 分类名称和状态查询条件
 * @returns {Promise} 平台产品分类树
 */
export function listProductClass(query) {
  return request({
    url: '/admin/rental/class/tree',
    method: 'get',
    params: query
  })
}

/**
 * 查询指定平台产品分类的完整资料。
 *
 * @param {number|string} classId 产品分类编号
 * @returns {Promise} 产品分类详情
 */
export function getProductClass(classId) {
  return request({
    url: '/admin/rental/class/' + classId,
    method: 'get'
  })
}

/**
 * 新增平台产品分类。
 *
 * @param {object} data 产品分类表单
 * @returns {Promise} 新增结果
 */
export function addProductClass(data) {
  return request({
    url: '/admin/rental/class',
    method: 'post',
    data
  })
}

/**
 * 修改平台产品分类。
 *
 * @param {object} data 产品分类表单
 * @returns {Promise} 修改结果
 */
export function updateProductClass(data) {
  return request({
    url: '/admin/rental/class',
    method: 'put',
    data
  })
}

/**
 * 软删除指定平台产品分类。
 *
 * @param {number|string} classId 产品分类编号
 * @returns {Promise} 删除结果
 */
export function deleteProductClass(classId) {
  return request({
    url: '/admin/rental/class/' + classId,
    method: 'delete'
  })
}
