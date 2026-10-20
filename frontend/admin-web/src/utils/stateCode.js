export const YES_NO_CODE = Object.freeze({
  NO: 0,
  YES: 1
})

export const NORMAL_DISABLE_CODE = Object.freeze({
  NORMAL: 0,
  DISABLED: 1
})

/** 与后端 SystemCode 一一对应的系统管理数值编码。 */
export const SYSTEM_CODE = Object.freeze({
  MENU_FRAME: Object.freeze({ EXTERNAL: 0, INTERNAL: 1 }),
  MENU_CACHE: Object.freeze({ CACHE: 0, NO_CACHE: 1 }),
  MENU_VISIBILITY: Object.freeze({ VISIBLE: 0, HIDDEN: 1 }),
  NOTICE_TYPE: Object.freeze({ NOTIFICATION: 1, ANNOUNCEMENT: 2 }),
  MERCHANT_NOTICE_SCOPE: Object.freeze({ ALL_MERCHANTS: 1, SPECIFIED_MERCHANTS: 2 }),
  JOB_STATUS: Object.freeze({ NORMAL: 0, PAUSED: 1 }),
  JOB_MISFIRE: Object.freeze({ DEFAULT: 0, IGNORE_MISFIRES: 1, FIRE_AND_PROCEED: 2, DO_NOTHING: 3 }),
  JOB_CONCURRENCY: Object.freeze({ ALLOWED: 0, DISALLOWED: 1 })
})
