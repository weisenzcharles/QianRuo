/**
 * 与后端 RentalCode 一一对应的租赁业务数字编码。
 * 页面只能使用本对象中的数值，不得重新写入历史英文状态字符串。
 */
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
  WAREHOUSE_SCOPE: Object.freeze({ ALL: 1, SPECIFIED: 2 }),
  ORDER_CHANNEL_TYPE: Object.freeze({ ALLIANCE: 1, CUSTOM: 2 }),
  ORDER_CHANNEL_SCOPE: Object.freeze({ PLATFORM: 1, MERCHANT: 2 }),
  PRODUCT_TYPE: Object.freeze({ DEVICE: 1, ACCESSORY: 2 }),
  RESOURCE_TYPE: Object.freeze({ DEVICE: 1, ACCESSORY: 2, CONSUMABLE: 3 }),
  STOCK_MODE: Object.freeze({ SERIAL: 1, QUANTITY: 2 }),
  INVENTORY_STATUS: Object.freeze({ AVAILABLE: 1, RESERVED: 2, RENTED: 3, REPAIRING: 4, LOST: 5, RETIRED: 6 }),
  RESERVATION_STATUS: Object.freeze({ RESERVED: 1, LOCKED: 2, RELEASED: 3, CONSUMED: 4, EXPIRED: 5 }),
  ORDER_STATUS: Object.freeze({ PENDING_SHIP: 10, PREPARED: 15, SHIPPING: 20, SHIP_IN_TRANSIT: 25, SHIPPED: 30, RENTING: 40, RETURNING: 45, COMPLETED: 50, OVERDUE_RETURN: 60, OVERDUE: 65, AFTER_SALES: 70, CANCELLED: 80 }),
  PAYMENT_STATUS: Object.freeze({ UNPAID: 0, PAID: 1, FAILED: 2, CLOSED: 3, REFUNDED: 4 }),
  FINANCE_DIRECTION: Object.freeze({ INCOME: 1, EXPENSE: 2, FREEZE: 3, UNFREEZE: 4, REVERSE: 5 }),
  LEDGER_STATUS: Object.freeze({ PROCESSING: 0, SUCCESS: 1, FAILED: 2, CLOSED: 3 }),
  INVOICE_STATUS: Object.freeze({ PENDING: 0, ISSUED: 1, REJECTED: 2 }),
  TIME_EFFECT_STATUS: Object.freeze({ NORMAL: 0, OVERDUE: 1 }),
  PROTOCOL_STATUS: Object.freeze({ DRAFT: 0, PUBLISHED: 1, RETIRED: 2 })
})

export const rentalMemberTypeOptions = [
  { label: '个人', value: RENTAL_CODE.MEMBER_TYPE.PERSONAL, tagType: '' },
  { label: '企业', value: RENTAL_CODE.MEMBER_TYPE.ENTERPRISE, tagType: 'success' }
]

export const rentalAuthStatusOptions = [
  { label: '未认证', value: RENTAL_CODE.AUTH_STATUS.UNVERIFIED, tagType: 'info' },
  { label: '待支付', value: RENTAL_CODE.AUTH_STATUS.PAYMENT_PENDING, tagType: 'warning' },
  { label: '待审核', value: RENTAL_CODE.AUTH_STATUS.PENDING, tagType: 'warning' },
  { label: '已认证', value: RENTAL_CODE.AUTH_STATUS.VERIFIED, tagType: 'success' },
  { label: '已驳回', value: RENTAL_CODE.AUTH_STATUS.REJECTED, tagType: 'danger' }
]

export const rentalMembershipTypeOptions = [
  { label: '个人会员', value: RENTAL_CODE.MEMBERSHIP_TYPE.PERSONAL_MEMBER, tagType: 'info' },
  { label: '普通企业会员', value: RENTAL_CODE.MEMBERSHIP_TYPE.ORDINARY_ENTERPRISE_MEMBER, tagType: '' },
  { label: '入驻会员', value: RENTAL_CODE.MEMBERSHIP_TYPE.SETTLED_MEMBER, tagType: 'success' }
]

export const rentalQualificationOptions = [
  { label: '对外接单', value: 'external_accept' },
  { label: '租商联盟接单', value: 'alliance_accept' },
  { label: '小程序接单', value: 'mini_program_accept' }
]

/**
 * 平台和商户接口统一使用资格状态编码，页面必须通过该映射展示中文，避免将内部编码直接暴露给用户。
 */
export const rentalQualificationStatusOptions = [
  { label: '未开通', value: RENTAL_CODE.QUALIFICATION_STATUS.DISABLED, tagType: 'info' },
  { label: '待审核', value: RENTAL_CODE.QUALIFICATION_STATUS.PENDING, tagType: 'warning' },
  { label: '已开通', value: RENTAL_CODE.QUALIFICATION_STATUS.ENABLED, tagType: 'success' },
  { label: '已驳回', value: RENTAL_CODE.QUALIFICATION_STATUS.REJECTED, tagType: 'danger' }
]

export const rentalYesNoOptions = [
  { label: '否', value: RENTAL_CODE.YES_NO.NO, tagType: 'info' },
  { label: '是', value: RENTAL_CODE.YES_NO.YES, tagType: 'success' }
]

export const platformFinanceDirectionOptions = [
  { label: '收入', value: RENTAL_CODE.FINANCE_DIRECTION.INCOME, tagType: 'success' },
  { label: '支出', value: RENTAL_CODE.FINANCE_DIRECTION.EXPENSE, tagType: 'danger' },
  { label: '冻结', value: RENTAL_CODE.FINANCE_DIRECTION.FREEZE, tagType: 'warning' },
  { label: '解冻', value: RENTAL_CODE.FINANCE_DIRECTION.UNFREEZE, tagType: 'info' },
  { label: '冲正', value: RENTAL_CODE.FINANCE_DIRECTION.REVERSE, tagType: 'warning' }
]

export const platformFinanceLedgerStatusOptions = [
  { label: '处理中', value: RENTAL_CODE.LEDGER_STATUS.PROCESSING, tagType: 'warning' },
  { label: '成功', value: RENTAL_CODE.LEDGER_STATUS.SUCCESS, tagType: 'success' },
  { label: '失败', value: RENTAL_CODE.LEDGER_STATUS.FAILED, tagType: 'danger' },
  { label: '已关闭', value: RENTAL_CODE.LEDGER_STATUS.CLOSED, tagType: 'info' }
]

export const platformFinanceInvoiceStatusOptions = [
  { label: '待开具', value: RENTAL_CODE.INVOICE_STATUS.PENDING, tagType: 'warning' },
  { label: '已开具', value: RENTAL_CODE.INVOICE_STATUS.ISSUED, tagType: 'success' },
  { label: '已驳回', value: RENTAL_CODE.INVOICE_STATUS.REJECTED, tagType: 'danger' }
]

export const platformPaymentStatusOptions = [
  { label: '未支付', value: RENTAL_CODE.PAYMENT_STATUS.UNPAID, tagType: 'warning' },
  { label: '已支付', value: RENTAL_CODE.PAYMENT_STATUS.PAID, tagType: 'success' },
  { label: '支付失败', value: RENTAL_CODE.PAYMENT_STATUS.FAILED, tagType: 'danger' },
  { label: '已关闭', value: RENTAL_CODE.PAYMENT_STATUS.CLOSED, tagType: 'info' },
  { label: '已退款', value: RENTAL_CODE.PAYMENT_STATUS.REFUNDED, tagType: 'warning' }
]

export function findRentalOption(options, value) {
  return options.find(item => item.value === value) || {
    label: value === undefined || value === null || value === '' ? '-' : value,
    tagType: ''
  }
}

export function getRentalOptionLabel(options, value) {
  return findRentalOption(options, value).label
}

/**
 * 将接单资格状态编码转换为页面展示名称。
 *
 * @param {number} value 接口返回的资格状态编码。
 * @returns {string} 面向用户展示的中文状态名称。
 */
export function getRentalQualificationStatusLabel(value) {
  return getRentalOptionLabel(rentalQualificationStatusOptions, value)
}

/**
 * 根据接单资格状态编码返回 Element Plus 标签类型，保持状态颜色与业务语义一致。
 *
 * @param {number} value 接口返回的资格状态编码。
 * @returns {string} Element Plus 标签类型。
 */
export function getRentalQualificationStatusTagType(value) {
  return findRentalOption(rentalQualificationStatusOptions, value).tagType
}
