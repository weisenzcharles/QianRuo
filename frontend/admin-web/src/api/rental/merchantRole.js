import request from '@/utils/request'

export function listMerchantRole(query) {
  return request({
    url: '/admin/rental/merchantRole/list',
    method: 'get',
    params: query
  })
}

export function getMerchantRole(memberId, roleId) {
  return request({
    url: `/admin/rental/merchantRole/${memberId}/${roleId}`,
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

export function delMerchantRole(memberId, roleIds) {
  return request({
    url: `/admin/rental/merchantRole/${memberId}/${roleIds}`,
    method: 'delete'
  })
}

export function merchantMenuTreeselect(memberId) {
  return request({
    url: `/admin/rental/merchantRole/menuTreeselect/${memberId}`,
    method: 'get'
  })
}

export function merchantRoleMenuTreeselect(memberId, roleId) {
  return request({
    url: `/admin/rental/merchantRole/roleMenuTreeselect/${memberId}/${roleId}`,
    method: 'get'
  })
}
