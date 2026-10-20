import request from '@/utils/request'

export function listPlatformMarketPosts(query) {
  return request({ url: '/admin/rental/market/posts', method: 'get', params: query })
}

export function listPlatformMarketReports(query) {
  return request({ url: '/admin/rental/market/reports', method: 'get', params: query })
}

export function reviewPlatformMarketReport(reportId, data) {
  return request({ url: `/admin/rental/market/reports/${reportId}/review`, method: 'post', data })
}

export function offlinePlatformMarketPost(postId) {
  return request({ url: `/admin/rental/market/posts/${postId}/offline`, method: 'post' })
}

export function updatePlatformMarketPostRank(postId, downRankFlag) {
  return request({ url: `/admin/rental/market/posts/${postId}/rank`, method: 'post', data: { downRankFlag } })
}
