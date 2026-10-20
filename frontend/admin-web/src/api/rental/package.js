import request from '@/utils/request'

export function listPackages(query) { return request({ url: '/admin/rental/package/list', method: 'get', params: query }) }
export function getPackage(packageId) { return request({ url: '/admin/rental/package/' + packageId, method: 'get' }) }
export function addPackage(data) { return request({ url: '/admin/rental/package', method: 'post', data }) }
export function updatePackage(data) { return request({ url: '/admin/rental/package', method: 'put', data }) }
export function publishPackage(packageId) { return request({ url: '/admin/rental/package/' + packageId + '/publish', method: 'post' }) }
export function deletePackage(packageId) { return request({ url: '/admin/rental/package/' + packageId, method: 'delete' }) }
