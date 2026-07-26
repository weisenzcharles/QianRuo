import request from '@/utils/request'

export function listMerchantUser(query) {
  return request({
    url: '/admin/rental/merchantUser/list',
    method: 'get',
    params: query
  })
}

export function getMerchantUser(merchantUserId) {
  return request({
    url: '/admin/rental/merchantUser/' + merchantUserId,
    method: 'get'
  })
}

export function getMerchantWarehouseOptions(memberId) {
  return request({
    url: '/admin/rental/merchantUser/warehouseOptions/' + memberId,
    method: 'get'
  })
}

export function addMerchantUser(data) {
  return request({
    url: '/admin/rental/merchantUser',
    method: 'post',
    data
  })
}

export function updateMerchantUser(data) {
  return request({
    url: '/admin/rental/merchantUser',
    method: 'put',
    data
  })
}

export function delMerchantUser(merchantUserIds) {
  return request({
    url: '/admin/rental/merchantUser/' + merchantUserIds,
    method: 'delete'
  })
}

export function changeMerchantUserStatus(merchantUserId, status) {
  return request({
    url: '/admin/rental/merchantUser/changeStatus',
    method: 'put',
    data: { merchantUserId, status }
  })
}

export function resetMerchantUserPwd(merchantUserId, password) {
  return request({
    url: '/admin/rental/merchantUser/resetPwd',
    method: 'put',
    data: { merchantUserId, password }
  })
}
