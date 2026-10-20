/**
 * 平台统一物流公司编码与中文名称。
 *
 * 数据库和接口继续保存平台编码，页面展示时统一转换为用户可读名称。
 */
const EXPRESS_COMPANY_NAME_MAP = Object.freeze({
  sf: '顺丰快递',
  shunfeng: '顺丰快递',
  shunfengkuaidi: '顺丰快递',
  jd: '京东快递',
  jdwl: '京东快递',
  jingdong: '京东快递',
  jingdongwuliu: '京东快递',
  cainiao: '菜鸟快递',
  cainiaowuliu: '菜鸟快递',
  zto: '中通快递',
  zhongtong: '中通快递',
  zhongtongkuaidi: '中通快递',
  yunda: '韵达快递',
  yundakuaidi: '韵达快递',
  yto: '圆通快递',
  yuantong: '圆通快递',
  yuantongkuaidi: '圆通快递',
  sto: '申通快递',
  shentong: '申通快递',
  ems: '中国邮政 EMS',
  youzhengguonei: '中国邮政',
  jt: '极兔速递',
  jtexpress: '极兔速递',
  best: '百世快递',
  deppon: '德邦快递',
  debangwuliu: '德邦快递',
  dhl: 'DHL',
  ups: 'UPS',
  fedex: 'FedEx'
})

/**
 * 将物流公司编码转换为中文展示名称。
 *
 * @param {string|number} value 物流公司编码或已格式化的名称。
 * @param {Array<{code?: string|number, name?: string}>} options 动态字典选项。
 * @returns {string} 页面展示名称；未知编码保留原值，避免丢失信息。
 */
export function getExpressCompanyName(value, options = []) {
  const rawValue = String(value ?? '').trim()
  if (!rawValue) return ''

  const normalizedValue = rawValue.toLowerCase()
  const option = Array.isArray(options)
    ? options.find(item => String(item?.code ?? '').trim().toLowerCase() === normalizedValue)
    : null
  return option?.name || EXPRESS_COMPANY_NAME_MAP[normalizedValue] || rawValue
}

/** 将以物流公司编码开头的提示文本转换为中文名称。 */
export function formatExpressCompanyText(value, options = []) {
  const rawValue = String(value ?? '')
  const match = rawValue.match(/^([A-Za-z][A-Za-z0-9_-]*)(\s+.*)$/)
  if (!match) return rawValue

  const companyName = getExpressCompanyName(match[1], options)
  return companyName === match[1] ? rawValue : `${companyName}${match[2]}`
}
