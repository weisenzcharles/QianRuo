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
