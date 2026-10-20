import request from '@/utils/request'

export function getMerchantProfile() {
  return request({
    url: '/merchant/user/profile',
    method: 'get'
  })
}

export function updateMerchantProfile(data) {
  return request({
    url: '/merchant/user/profile',
    method: 'put',
    data
  })
}

export function updateMerchantPassword(oldPassword, newPassword) {
  return request({
    url: '/merchant/user/profile/updatePwd',
    method: 'put',
    data: { oldPassword, newPassword }
  })
}

export function initializeMerchantPassword(newPassword) {
  return request({
    url: '/merchant/user/profile/initPassword',
    method: 'put',
    data: { newPassword }
  })
}

export function uploadMerchantAvatar(data) {
  return request({
    url: '/merchant/user/profile/avatar',
    method: 'post',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    data
  })
}
