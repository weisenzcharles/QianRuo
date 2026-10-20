import request from '@/utils/request'

/** 查询当前商户店铺设置。商户范围由后端登录上下文决定。 */
export function getMerchantStoreSettings() {
  return request({
    url: '/merchant/store/settings',
    method: 'get'
  })
}

/** 修改商户店铺基础资料。 */
export function updateMerchantStoreProfile(data) {
  return request({
    url: '/merchant/store/settings/profile',
    method: 'put',
    data
  })
}

/** 修改商户经营偏好。 */
export function updateMerchantStorePreferences(data) {
  return request({
    url: '/merchant/store/settings/preferences',
    method: 'put',
    data
  })
}

/** 删除当前商户店铺 Logo。 */
export function removeMerchantStoreLogo() {
  return request({
    url: '/merchant/store/settings/logo',
    method: 'delete'
  })
}
