import request from '@/utils/request'

/** 查询新架构库存汇总。 */
export function listMerchantInventory(params) {
  return request({ url: '/merchant/inventory/list', method: 'get', params })
}

/** 查询当前商户可录入库存的设备和配件商品。 */
export function listMerchantInventoryOptions(params) {
  return request({ url: '/merchant/inventory/options', method: 'get', params })
}

/** 查询库存序列号明细。 */
export function listMerchantInventoryUnits(inventoryId, params) {
  return request({ url: `/merchant/inventory/${inventoryId}/units`, method: 'get', params })
}

/** 查询数量库存当前仍在预占中的订单。 */
export function listMerchantInventoryReservationOrders(inventoryId, params) {
  return request({ url: `/merchant/inventory/${inventoryId}/reservation-orders`, method: 'get', params })
}

/** 查询系统字典维护的设备成色选项。 */
export function listMerchantInventoryConditionLevels() {
  return request({ url: '/merchant/inventory/condition-levels', method: 'get' })
}

/** 批量录入序列号或初始化数量库存。 */
export function addMerchantInventoryBatch(data) {
  return request({ url: '/merchant/inventory/batch', method: 'post', data })
}

/** 修改库存设备档案，服务端校验商户范围和设备状态流转。 */
export function updateMerchantInventoryUnit(data) {
  return request({ url: '/merchant/inventory/units', method: 'put', data })
}

/** 删除未关联订单的库存序列号。 */
export function deleteMerchantInventoryUnit(unitId) {
  return request({ url: `/merchant/inventory/units/${unitId}`, method: 'delete' })
}

/** 将未关联订单的可用库存单元转移到其他仓库。 */
export function transferMerchantInventoryUnit(data) {
  return request({ url: '/merchant/inventory/units/transfer', method: 'post', data })
}
