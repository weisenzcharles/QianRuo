import request from '@/utils/request'

/** 查询当前商户周转市场帖子。 */
export function listMerchantMarketPosts(params) {
  return request({ url: '/merchant/market/posts', method: 'get', params })
}

/** 查询周转市场平台固定的发帖选项。 */
export function getMerchantMarketOptions() {
  return request({ url: '/merchant/market/posts/options', method: 'get' })
}

/** 查询当前商户可绑定的已认证逐件库存。 */
export function listMerchantMarketInventoryOptions(params) {
  return request({ url: '/merchant/market/posts/inventory-options', method: 'get', params })
}

/** 查询市场帖子详情，编辑时用于恢复实物设备绑定。 */
export function getMerchantMarketPost(postNo) {
  return request({ url: `/merchant/market/posts/${encodeURIComponent(postNo)}`, method: 'get' })
}

/** 新增市场帖子草稿。 */
export function createMerchantMarketPost(data) {
  return request({ url: '/merchant/market/posts', method: 'post', data })
}

/** 修改草稿或已下架市场帖子。 */
export function updateMerchantMarketPost(postNo, data) {
  return request({ url: `/merchant/market/posts/${encodeURIComponent(postNo)}`, method: 'put', data })
}

/** 发布市场帖子。 */
export function publishMerchantMarketPost(postNo) {
  return request({ url: `/merchant/market/posts/${encodeURIComponent(postNo)}/publish`, method: 'post' })
}

/** 下架市场帖子。 */
export function offShelfMerchantMarketPost(postNo) {
  return request({ url: `/merchant/market/posts/${encodeURIComponent(postNo)}/off-shelf`, method: 'post' })
}

/** 关闭市场帖子。 */
export function closeMerchantMarketPost(postNo) {
  return request({ url: `/merchant/market/posts/${encodeURIComponent(postNo)}/close`, method: 'post' })
}

/** 查询其他商户公开发布的周转市场帖子。 */
export function listPublicMarketPosts(params) {
  return request({ url: '/merchant/market/public/posts', method: 'get', params })
}

/** 查询当前商户参与的周转市场私信。 */
export function listMarketMessages(params) {
  return request({ url: '/merchant/market/messages', method: 'get', params })
}

/** 发送周转市场私信。 */
export function sendMarketMessage(data) {
  return request({ url: '/merchant/market/messages', method: 'post', data })
}

/** 将收到的周转市场私信标记为已读。 */
export function markMarketMessageRead(messageId) {
  return request({ url: `/merchant/market/messages/${messageId}/read`, method: 'put' })
}

/** 查询当前商户提交的周转市场举报。 */
export function listMarketReports(params) {
  return request({ url: '/merchant/market/reports', method: 'get', params })
}

/** 提交周转市场举报。 */
export function submitMarketReport(data) {
  return request({ url: '/merchant/market/reports', method: 'post', data })
}
