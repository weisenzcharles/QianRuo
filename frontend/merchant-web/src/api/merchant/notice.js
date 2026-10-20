import request from '@/utils/request'

// 查询商户端顶部公告
export function listMerchantNoticeTop() {
  return request({ url: '/merchant/merchant-notice/top', method: 'get' })
}

// 查询商户端公告详情
export function getMerchantNotice(noticeId) {
  return request({ url: `/merchant/merchant-notice/${noticeId}`, method: 'get' })
}

// 标记一条公告已读
export function markMerchantNoticeRead(noticeId) {
  return request({ url: `/merchant/merchant-notice/${noticeId}/read`, method: 'post' })
}

// 批量标记公告已读
export function markMerchantNoticeReadAll(ids) {
  return request({ url: '/merchant/merchant-notice/read-all', method: 'post', params: { ids } })
}
