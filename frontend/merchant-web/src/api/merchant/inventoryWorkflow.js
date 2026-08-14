import request from '@/utils/request'

/** 创建套餐库存预占。 */
export function reserveInventory(data) {
  return request({ url: '/merchant/inventory/workflow/reserve', method: 'post', data })
}

/** 查询库存预占详情。 */
export function getInventoryReservation(reservationId) {
  return request({ url: `/merchant/inventory/workflow/reservation/${reservationId}`, method: 'get' })
}

/** 释放库存预占。 */
export function releaseInventoryReservation(reservationId, data) {
  return request({ url: `/merchant/inventory/workflow/reservation/${reservationId}/release`, method: 'post', data })
}

/** 消耗库存预占。 */
export function consumeInventoryReservation(reservationId, data) {
  return request({ url: `/merchant/inventory/workflow/reservation/${reservationId}/consume`, method: 'post', data })
}

/** 将超时库存预占标记为 expired。 */
export function expireInventoryReservation(reservationId, data) {
  return request({ url: `/merchant/inventory/workflow/reservation/${reservationId}/expire`, method: 'post', data })
}

/** 执行逐件或数量库存状态迁移。 */
export function transitionInventory(data) {
  return request({ url: '/merchant/inventory/workflow/transition', method: 'post', data })
}
