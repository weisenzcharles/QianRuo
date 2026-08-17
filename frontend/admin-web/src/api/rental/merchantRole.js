import request from '@/utils/request'

export function listMerchantRole(query) {
  return request({
    url: '/admin/rental/merchantRole/list',
    method: 'get',
    params: query
  })
}

export function getMerchantRole(roleTemplateId) {
  return request({
    url: `/admin/rental/merchantRole/${roleTemplateId}`,
    method: 'get'
  })
}

export function addMerchantRole(data) {
  return request({
    url: '/admin/rental/merchantRole',
    method: 'post',
    data
  })
}

export function updateMerchantRole(data) {
  return request({
    url: '/admin/rental/merchantRole',
    method: 'put',
    data
  })
}

export function delMerchantRole(roleTemplateIds) {
  return request({
    url: `/admin/rental/merchantRole/${roleTemplateIds}`,
    method: 'delete'
  })
}

export function merchantMenuTreeselect() {
  return request({
    url: '/admin/rental/merchantRole/menuTreeselect',
    method: 'get'
  })
}

export function merchantRoleMenuTreeselect(roleTemplateId) {
  return request({
    url: `/admin/rental/merchantRole/roleMenuTreeselect/${roleTemplateId}`,
    method: 'get'
  })
}
