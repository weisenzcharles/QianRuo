import request from '@/utils/request'

/** 查询当前用户顶部可见的商户内部通知。 */
export function listMerchantInternalNoticeTop() {
  return request({ url: '/merchant/internal-notice/top', method: 'get' })
}

/** 查询当前用户可见的内部通知详情。 */
export function getMerchantInternalNotice(noticeId) {
  return request({ url: `/merchant/internal-notice/${noticeId}`, method: 'get' })
}

/** 标记一条内部通知已读。 */
export function markMerchantInternalNoticeRead(noticeId) {
  return request({ url: `/merchant/internal-notice/${noticeId}/read`, method: 'post' })
}

/** 批量标记内部通知已读。 */
export function markMerchantInternalNoticeReadAll(ids) {
  return request({ url: '/merchant/internal-notice/read-all', method: 'post', params: { ids } })
}

/** 查询内部通知管理列表。 */
export function listMerchantInternalNotices(params) {
  return request({ url: '/merchant/internal-notice/list', method: 'get', params })
}

/** 查询当前商户员工组。 */
export function listMerchantInternalNoticeRoles() {
  return request({ url: '/merchant/internal-notice/roles', method: 'get' })
}

/** 查询管理详情。 */
export function getMerchantInternalNoticeForManage(noticeId) {
  return request({ url: `/merchant/internal-notice/manage/${noticeId}`, method: 'get' })
}

/** 查询内部通知的员工阅读统计。 */
export function getMerchantInternalNoticeReadStats(noticeId) {
  return request({ url: `/merchant/internal-notice/manage/${noticeId}/read-stats`, method: 'get' })
}

/** 新增内部通知。 */
export function createMerchantInternalNotice(data) {
  return request({ url: '/merchant/internal-notice', method: 'post', data })
}

/** 修改内部通知。 */
export function updateMerchantInternalNotice(noticeId, data) {
  return request({ url: `/merchant/internal-notice/${noticeId}`, method: 'put', data })
}

/** 发布内部通知。 */
export function publishMerchantInternalNotice(noticeId) {
  return request({ url: `/merchant/internal-notice/${noticeId}/publish`, method: 'post' })
}

/** 下架内部通知。 */
export function offlineMerchantInternalNotice(noticeId) {
  return request({ url: `/merchant/internal-notice/${noticeId}/offline`, method: 'post' })
}

/** 删除内部通知。 */
export function deleteMerchantInternalNotice(noticeId) {
  return request({ url: `/merchant/internal-notice/${noticeId}`, method: 'delete' })
}
