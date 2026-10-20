import request from '@/utils/request'

export function listAccessories(query) {
  return request({ url: '/admin/rental/accessory/list', method: 'get', params: query })
}

export function getAccessory(accessoryId) {
  return request({ url: '/admin/rental/accessory/' + accessoryId, method: 'get' })
}

export function addAccessory(data) {
  return request({ url: '/admin/rental/accessory', method: 'post', data })
}

export function updateAccessory(data) {
  return request({ url: '/admin/rental/accessory', method: 'put', data })
}

export function deleteAccessory(accessoryId) {
  return request({ url: '/admin/rental/accessory/' + accessoryId, method: 'delete' })
}
