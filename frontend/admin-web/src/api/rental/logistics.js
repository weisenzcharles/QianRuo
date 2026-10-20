import request from '@/utils/request'

export function listPlatformLogistics(query) {
  return request({ url: '/admin/rental/logistics/list', method: 'get', params: query })
}

export function getPlatformLogistics(logisticsId) {
  return request({ url: `/admin/rental/logistics/${logisticsId}`, method: 'get' })
}

export function refreshPlatformLogistics() {
  return request({ url: '/admin/rental/logistics/refresh', method: 'post' })
}

export function refreshPlatformLogisticsItem(logisticsId) {
  return request({ url: `/admin/rental/logistics/${logisticsId}/refresh`, method: 'post' })
}
