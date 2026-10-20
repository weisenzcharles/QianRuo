// 联盟领域的持久化编码集中维护，页面只引用具名常量，避免散落魔法数字。
export const ALLIANCE_STATUS = Object.freeze({
  ACTIVE: 1,
  DISABLED: 2,
})

export const ALLIANCE_MEMBER_STATUS = Object.freeze({
  ACTIVE: 1,
  DISABLED: 2,
  EXITED: 3,
})

export const ALLIANCE_SCOPE_TYPE = Object.freeze({
  WAREHOUSE: 1,
  PRODUCT: 2,
})

export const ALLIANCE_FLAG = Object.freeze({
  DISABLED: 0,
  ENABLED: 1,
})

export const ALLIANCE_TRANSFER_MODE = Object.freeze({
  ALLIANCE: 1,
  DESIGNATED_MERCHANT: 2,
  PUBLIC_POOL: 3,
})

export const ALLIANCE_ORDER_VIEW_MODE = Object.freeze({
  ASSIST: 1,
  PUBLIC_POOL: 2,
})

export const ALLIANCE_ORDER_STATUS = Object.freeze({
  PENDING: 0,
  ACCEPTED: 1,
  REJECTED: 2,
  CANCELLED: 3,
  EXPIRED: 4,
  PUBLIC_POOL: 5,
  IN_FULFILLMENT: 6,
  COMPLETED: 7,
  EXCEPTION: 8,
})

export const ALLIANCE_EVENT_TYPE = Object.freeze({
  CREATED: 1,
  ACCEPTED: 2,
  REJECTED: 3,
  TRANSFERRED: 4,
  PUBLIC_POOL: 5,
  CHILD_ORDER_CREATED: 6,
  SHIPPED: 7,
  COMPLETED: 8,
  CANCELLED: 9,
  EXCEPTION: 10,
  WAREHOUSE_CHANGED: 11,
})

export const ALLIANCE_SETTLEMENT_STATUS = Object.freeze({
  PENDING: 0,
  CONFIRMED: 1,
  DISPUTED: 2,
  CLOSED: 3,
})

export const ALLIANCE_ROUTE_STATUS = Object.freeze({
  PENDING: 0,
  IN_TRANSIT: 1,
  DELIVERED: 2,
  EXCEPTION: 3,
  CLOSED: 4,
})

export const ALLIANCE_DISPUTE_STATUS = Object.freeze({
  PENDING: 0,
  EVIDENCE: 1,
  MEDIATING: 2,
  CONFIRMING: 3,
  RESOLVED: 4,
  REJECTED: 5,
  CLOSED: 6,
})

export const ALLIANCE_DISPUTE_TYPE = Object.freeze({
  GENERAL: 0,
  DAMAGE: 1,
  LOSS: 2,
  DELAYED_FULFILLMENT: 3,
  FEE: 4,
  OTHER: 9,
})

export const ALLIANCE_EVIDENCE_TYPE = Object.freeze({
  IMAGE: 1,
  VIDEO: 2,
  FILE: 3,
  TEXT: 4,
})

export const COMPENSATION_RULE_STATUS = Object.freeze({
  ENABLED: 0,
  DISABLED: 1,
})
