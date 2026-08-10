import request from '@/utils/request'

/**
 * 查询平台统一业务编码规则。
 *
 * @returns {Promise} 编码规则列表
 */
export function listCodeRules() {
  return request({
    url: '/admin/code/rule/list',
    method: 'get'
  })
}

/**
 * 修改指定编码类型的前缀、总长度和启用状态。
 *
 * @param {string} codeType 编码类型键
 * @param {object} data 编码规则配置
 * @returns {Promise} 更新后的编码规则
 */
export function updateCodeRule(codeType, data) {
  return request({
    url: '/admin/code/rule/' + codeType,
    method: 'put',
    data
  })
}
