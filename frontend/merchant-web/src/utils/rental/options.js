/**
 * 接单资格状态的接口编码与页面标签映射。
 *
 * 接口和数据库继续保存稳定的英文编码，商户端只在展示层转换为中文，避免改变跨服务契约。
 */
const QUALIFICATION_STATUS_OPTIONS = [
  { label: '未开通', value: 'disabled', tagType: 'info' },
  { label: '待审核', value: 'pending', tagType: 'warning' },
  { label: '已开通', value: 'enabled', tagType: 'success' },
  { label: '已驳回', value: 'rejected', tagType: 'danger' }
]

/**
 * 将接单资格状态编码转换为商户端展示名称。
 *
 * @param {string} value 接口返回的资格状态编码。
 * @returns {string} 面向商户用户展示的中文状态名称。
 */
export function getQualificationStatusLabel(value) {
  return QUALIFICATION_STATUS_OPTIONS.find(item => item.value === value)?.label || value || '-'
}

/**
 * 根据接单资格状态编码返回 Element Plus 标签类型。
 *
 * @param {string} value 接口返回的资格状态编码。
 * @returns {string} Element Plus 标签类型。
 */
export function getQualificationStatusTagType(value) {
  return QUALIFICATION_STATUS_OPTIONS.find(item => item.value === value)?.tagType || 'info'
}
