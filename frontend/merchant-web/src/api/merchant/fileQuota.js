import request from '@/utils/request'

/** 查询当前商户文件配额。 */
export function getMerchantFileQuota() {
  return request({ url: '/merchant/file-quota', method: 'get' })
}

/** 查询当前商户文件配额审计流水。 */
export function listMerchantFileQuotaLedger() {
  return request({ url: '/merchant/file-quota/ledger', method: 'get' })
}
