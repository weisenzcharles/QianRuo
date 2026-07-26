import request from '@/utils/request'

export function listMerchantOperLog(query) {
  return request({
    url: '/merchant/operlog/list',
    method: 'get',
    params: query
  })
}
