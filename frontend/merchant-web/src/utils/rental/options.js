/** 与后端 RentalCode 一一对应的租赁业务数字编码。 */
export const RENTAL_CODE = Object.freeze({
  YES_NO: Object.freeze({ NO: 0, YES: 1 }),
  MEMBER_TYPE: Object.freeze({ PERSONAL: 1, ENTERPRISE: 2 }),
  MEMBERSHIP_TYPE: Object.freeze({ PERSONAL_MEMBER: 1, ORDINARY_ENTERPRISE_MEMBER: 2, SETTLED_MEMBER: 3 }),
  AUTH_STATUS: Object.freeze({ UNVERIFIED: 0, PAYMENT_PENDING: 1, PENDING: 2, VERIFIED: 3, REJECTED: 4 }),
  CERTIFICATION_STATUS: Object.freeze({ PENDING: 0, APPROVED: 1, REJECTED: 2, EXPIRED: 3, SUPPLEMENT_REQUIRED: 4 }),
  REGISTRATION_MODE: Object.freeze({ PAID: 1, INVITE: 2 }),
  REGISTRATION_STATUS: Object.freeze({ AWAITING_PAYMENT: 0, COMPLETED: 1 }),
  INVITATION_STATUS: Object.freeze({ REGISTERED: 1, INVALID: 2 }),
  QUALIFICATION_STATUS: Object.freeze({ DISABLED: 0, PENDING: 1, ENABLED: 2, REJECTED: 3 }),
  ORDER_CHANNEL_TYPE: Object.freeze({ ALLIANCE: 1, CUSTOM: 2 }),
  ORDER_CHANNEL_SCOPE: Object.freeze({ PLATFORM: 1, MERCHANT: 2 }),
  PRODUCT_TYPE: Object.freeze({ DEVICE: 1, ACCESSORY: 2 }),
  STOCK_MODE: Object.freeze({ SERIAL: 1, QUANTITY: 2 }),
  WAREHOUSE_SCOPE: Object.freeze({ ALL: 1, SPECIFIED: 2 }),
  INVENTORY_STATUS: Object.freeze({ AVAILABLE: 1, RESERVED: 2, RENTED: 3, REPAIRING: 4, LOST: 5, RETIRED: 6 }),
  RESERVATION_STATUS: Object.freeze({ RESERVED: 1, LOCKED: 2, RELEASED: 3, CONSUMED: 4, EXPIRED: 5 }),
  DELIVERY_DIRECTION: Object.freeze({ SHIP: 1, RETURN: 2, TRANSFER: 3 }),
  ALLIANCE_ORDER_STATUS: Object.freeze({ PENDING: 0, ACCEPTED: 1, REJECTED: 2, CANCELLED: 3, EXPIRED: 4, PUBLIC_POOL: 5, IN_FULFILLMENT: 6, COMPLETED: 7, EXCEPTION: 8 }),
  STOCKTAKING_MODE: Object.freeze({ MANUAL: 'manual', SCAN: 'scan' }),
  STOCKTAKING_MODE_CODE: Object.freeze({ MANUAL: 0, SCAN: 1 }),
  STOCKTAKING_SCOPE: Object.freeze({ ALL: 'all', CLASS: 'class', MODEL: 'model' }),
  STOCKTAKING_STATUS: Object.freeze({ PROCESSING: 0, COMPLETED: 1, CLOSED: 2, REVIEW_REQUIRED: 3 }),
  STOCKTAKING_SCAN_RESULT: Object.freeze({ NORMAL: 0, ABNORMAL: 1 }),
  ORDER_STATUS: Object.freeze({ PENDING_SHIP: 10, PREPARED: 15, SHIPPING: 20, SHIP_IN_TRANSIT: 25, SHIPPED: 30, RENTING: 40, RETURNING: 45, COMPLETED: 50, OVERDUE_RETURN: 60, OVERDUE: 65, AFTER_SALES: 70, CANCELLED: 80 }),
  PAYMENT_STATUS: Object.freeze({ UNPAID: 0, PAID: 1, FAILED: 2, CLOSED: 3, REFUNDED: 4 }),
  TIME_EFFECT_STATUS: Object.freeze({ NORMAL: 0, OVERDUE: 1 }),
  PROTOCOL_STATUS: Object.freeze({ DRAFT: 0, PUBLISHED: 1, RETIRED: 2 })
})

/** 接单资格状态的接口编码与页面标签映射。 */
const QUALIFICATION_STATUS_OPTIONS = [
  { label: '未开通', value: RENTAL_CODE.QUALIFICATION_STATUS.DISABLED, tagType: 'info' },
  { label: '待审核', value: RENTAL_CODE.QUALIFICATION_STATUS.PENDING, tagType: 'warning' },
  { label: '已开通', value: RENTAL_CODE.QUALIFICATION_STATUS.ENABLED, tagType: 'success' },
  { label: '已驳回', value: RENTAL_CODE.QUALIFICATION_STATUS.REJECTED, tagType: 'danger' }
]

/**
 * 将接单资格状态编码转换为商户端展示名称。
 *
 * @param {number} value 接口返回的资格状态编码。
 * @returns {string} 面向商户用户展示的中文状态名称。
 */
export function getQualificationStatusLabel(value) {
  return QUALIFICATION_STATUS_OPTIONS.find(item => item.value === value)?.label || value || '-'
}

/**
 * 根据接单资格状态编码返回 Element Plus 标签类型。
 *
 * @param {number} value 接口返回的资格状态编码。
 * @returns {string} Element Plus 标签类型。
 */
export function getQualificationStatusTagType(value) {
  return QUALIFICATION_STATUS_OPTIONS.find(item => item.value === value)?.tagType || 'info'
}
