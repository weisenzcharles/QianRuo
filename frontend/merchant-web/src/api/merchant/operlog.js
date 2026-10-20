import request from '@/utils/request'

export function listMerchantOperLog(query) {
  return request({
    url: '/merchant/operlog/list',
    method: 'get',
    params: query
  })
}

/**
 * 查询当前商户日志列表使用的模块和操作人下拉选项。
 *
 * @returns {Promise} 当前商户范围内的审计筛选项
 */
export function getMerchantOperLogOptions() {
  return request({
    url: '/merchant/operlog/options',
    method: 'get'
  })
}

/**
 * 查询商户操作日志变更字段的中文名称映射。
 *
 * @returns {Promise} 审计字段名称映射请求
 */
export function getMerchantOperLogFieldLabels() {
  return request({
    url: '/merchant/operlog/field-labels',
    method: 'get'
  })
}

/**
 * 查询一条商户业务审计详情。
 *
 * @param {number|string} auditId 审计记录编号
 * @returns {Promise} 审计详情请求
 */
export function getMerchantBusinessAudit(auditId) {
  return request({
    url: `/merchant/operlog/${auditId}`,
    method: 'get'
  })
}
