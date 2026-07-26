import request from '@/utils/request'

/**
 * 按平台筛选条件分页查询商户及入驻申请。
 *
 * @param {object} query 平台商户查询条件
 * @returns {Promise} 商户分页数据
 */
export function listMember(query) {
  return request({
    url: '/admin/rental/member/list',
    method: 'get',
    params: query
  })
}

/**
 * 查询指定商户的完整平台管理资料。
 *
 * @param {number|string} memberId 商户主键
 * @returns {Promise} 商户详情
 */
export function getMember(memberId) {
  return request({
    url: '/admin/rental/member/' + memberId,
    method: 'get'
  })
}

/**
 * 由平台人员手工创建待审核商户资料。
 *
 * @param {object} data 商户资料
 * @returns {Promise} 新增结果
 */
export function addMember(data) {
  return request({
    url: '/admin/rental/member',
    method: 'post',
    data: data
  })
}

/**
 * 修改商户基础资料，不允许通过该接口绕过独立审核状态机。
 *
 * @param {object} data 商户资料
 * @returns {Promise} 修改结果
 */
export function updateMember(data) {
  return request({
    url: '/admin/rental/member',
    method: 'put',
    data: data
  })
}

/**
 * 审核待处理的商户入驻申请，审核通过后后端同步开通商户管理员。
 *
 * @param {number|string} memberId 商户主键
 * @param {{authStatus: string, auditRemark: string}} data 审核结果和审核意见
 * @returns {Promise} 审核结果
 */
export function auditMember(memberId, data) {
  return request({
    url: '/admin/rental/member/' + memberId + '/audit',
    method: 'put',
    data
  })
}

/**
 * 删除一个或多个平台商户记录。
 *
 * @param {number|string|Array} memberId 商户主键或主键集合
 * @returns {Promise} 删除结果
 */
export function delMember(memberId) {
  return request({
    url: '/admin/rental/member/' + memberId,
    method: 'delete'
  })
}
