import request from '@/utils/request'

export function listCompensationRules() {
  return request({ url: '/admin/rental/compensation-rule/list', method: 'get' })
}

export function addCompensationRule(data) {
  return request({ url: '/admin/rental/compensation-rule', method: 'post', data })
}

export function updateCompensationRule(data) {
  return request({ url: '/admin/rental/compensation-rule', method: 'put', data })
}

export function deleteCompensationRule(ruleId) {
  return request({ url: `/admin/rental/compensation-rule/${ruleId}`, method: 'delete' })
}
