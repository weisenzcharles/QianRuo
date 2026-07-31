import request from '@/utils/request'

/**
 * 查询平台产品类目树。
 *
 * @param {object} query 类目名称和状态查询条件
 * @returns {Promise} 平台产品类目树
 */
export function listProductClass(query) {
  return request({
    url: '/admin/rental/class/tree',
    method: 'get',
    params: query
  })
}

/**
 * 查询指定平台产品类目的完整资料。
 *
 * @param {number|string} classId 产品类目主键
 * @returns {Promise} 产品类目详情
 */
export function getProductClass(classId) {
  return request({
    url: '/admin/rental/class/' + classId,
    method: 'get'
  })
}

/**
 * 新增平台产品类目。
 *
 * @param {object} data 产品类目表单
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
 * 修改平台产品类目。
 *
 * @param {object} data 产品类目表单
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
 * 软删除指定平台产品类目。
 *
 * @param {number|string} classId 产品类目主键
 * @returns {Promise} 删除结果
 */
export function deleteProductClass(classId) {
  return request({
    url: '/admin/rental/class/' + classId,
    method: 'delete'
  })
}
