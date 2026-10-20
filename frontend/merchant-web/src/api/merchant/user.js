import request from '@/utils/request'

/** 查询当前商户用户列表。 */
export function listMerchantUsers(params) { return request({ url: '/merchant/user/manage/list', method: 'get', params }) }
/** 查询当前商户用户详情及角色。 */
export function getMerchantUser(userId) { return request({ url: `/merchant/user/manage/${userId}`, method: 'get' }) }
/** 查询当前登录用户可用于分配的启用仓库。 */
export function listMerchantWarehouseOptions() { return request({ url: '/merchant/context/warehouses', method: 'get' }) }
/** 修改当前商户用户。 */
export function updateMerchantUser(data) { return request({ url: '/merchant/user/manage', method: 'put', data }) }
export function addMerchantUser(data) { return request({ url: '/merchant/user/manage', method: 'post', data }) }
/** 重置当前商户用户密码。 */
export function resetMerchantUserPassword(userId) { return request({ url: `/merchant/user/manage/${userId}/reset-password`, method: 'put' }) }
