export const rentalMemberTypeOptions = [
  { label: '个人', value: 'personal', tagType: '' },
  { label: '企业', value: 'enterprise', tagType: 'success' }
]

export const rentalAuthStatusOptions = [
  { label: '未认证', value: 'unverified', tagType: 'info' },
  { label: '待审核', value: 'pending', tagType: 'warning' },
  { label: '已认证', value: 'verified', tagType: 'success' },
  { label: '已驳回', value: 'rejected', tagType: 'danger' }
]

export const rentalVipLevelOptions = [
  { label: '免费版', value: 'free', tagType: 'info' },
  { label: '标准版', value: 'standard', tagType: '' },
  { label: '专业版', value: 'pro', tagType: 'success' },
  { label: '旗舰版', value: 'enterprise', tagType: 'warning' }
]

export const rentalMembershipTypeOptions = [
  { label: '个人会员', value: 'personal_member', tagType: 'info' },
  { label: '普通企业会员', value: 'ordinary_enterprise_member', tagType: '' },
  { label: '入驻会员', value: 'settled_member', tagType: 'success' }
]

export const rentalQualificationOptions = [
  { label: '对外接单', value: 'external_accept' },
  { label: '租商联盟接单', value: 'alliance_accept' },
  { label: '小程序接单', value: 'mini_program_accept' }
]

/**
 * 平台和商户接口统一使用资格状态编码，页面必须通过该映射展示中文，避免将内部编码直接暴露给用户。
 */
export const rentalQualificationStatusOptions = [
  { label: '未开通', value: 'disabled', tagType: 'info' },
  { label: '待审核', value: 'pending', tagType: 'warning' },
  { label: '已开通', value: 'enabled', tagType: 'success' },
  { label: '已驳回', value: 'rejected', tagType: 'danger' }
]

export const rentalYesNoOptions = [
  { label: '否', value: 0, tagType: 'info' },
  { label: '是', value: 1, tagType: 'success' }
]

export function findRentalOption(options, value) {
  return options.find(item => item.value === value) || {
    label: value === undefined || value === null || value === '' ? '-' : value,
    tagType: ''
  }
}

export function getRentalOptionLabel(options, value) {
  return findRentalOption(options, value).label
}

/**
 * 将接单资格状态编码转换为页面展示名称。
 *
 * @param {string} value 接口返回的资格状态编码。
 * @returns {string} 面向用户展示的中文状态名称。
 */
export function getRentalQualificationStatusLabel(value) {
  return getRentalOptionLabel(rentalQualificationStatusOptions, value)
}

/**
 * 根据接单资格状态编码返回 Element Plus 标签类型，保持状态颜色与业务语义一致。
 *
 * @param {string} value 接口返回的资格状态编码。
 * @returns {string} Element Plus 标签类型。
 */
export function getRentalQualificationStatusTagType(value) {
  return findRentalOption(rentalQualificationStatusOptions, value).tagType
}
