import request from '@/utils/request'

// 查询平台发布给商户端的公告列表
export function listMerchantNotice(query) {
  return request({ url: '/admin/rental/merchantNotice/list', method: 'get', params: query })
}

// 查询公告详情
export function getMerchantNotice(merchantNoticeId) {
  return request({ url: `/admin/rental/merchantNotice/${merchantNoticeId}`, method: 'get' })
}

// 查询可指定接收公告的商户选项
export function listMerchantNoticeMembers() {
  return request({ url: '/admin/rental/merchantNotice/options', method: 'get' })
}

// 新增公告
export function addMerchantNotice(data) {
  return request({ url: '/admin/rental/merchantNotice', method: 'post', data })
}

// 修改公告
export function updateMerchantNotice(data) {
  return request({ url: '/admin/rental/merchantNotice', method: 'put', data })
}

// 删除公告
export function deleteMerchantNotice(merchantNoticeIds) {
  return request({ url: `/admin/rental/merchantNotice/${merchantNoticeIds}`, method: 'delete' })
}
