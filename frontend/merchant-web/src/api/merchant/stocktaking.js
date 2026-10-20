import request from '@/utils/request'

export function listMerchantStocktakings(params) {
  return request({ url: '/merchant/inventory-check/list', method: 'get', params })
}

export function getMerchantStocktakingSummary(params) {
  return request({ url: '/merchant/inventory-check/summary', method: 'get', params })
}

export function listMerchantStocktakingScopeOptions(params) {
  return request({ url: '/merchant/inventory-check/options', method: 'get', params })
}

export function getMerchantStocktaking(id) {
  return request({ url: `/merchant/inventory-check/${id}`, method: 'get' })
}

export function createMerchantStocktaking(data) {
  return request({ url: '/merchant/inventory-check', method: 'post', data })
}

export function countMerchantStocktakingItem(id, itemId, countedQuantity) {
  return request({
    url: `/merchant/inventory-check/${id}/items/${itemId}`,
    method: 'put',
    data: { countedQuantity }
  })
}

export function scanMerchantStocktaking(id, code) {
  return request({
    url: `/merchant/inventory-check/${id}/scan`,
    method: 'post',
    data: { code }
  })
}

export function completeMerchantStocktaking(id) {
  return request({ url: `/merchant/inventory-check/${id}/complete`, method: 'post' })
}

export function closeMerchantStocktaking(id) {
  return request({ url: `/merchant/inventory-check/${id}/close`, method: 'post' })
}

export function reviewMerchantStocktaking(id) {
  return request({ url: `/merchant/inventory-check/${id}/review`, method: 'post' })
}
