import request from '@/utils/request'

/** 导入指定主数据资源的 Excel 文件。 */
export function importMerchantResource(resource, file) {
  const data = new FormData()
  data.append('file', file)
  return request({
    url: `/merchant/batch/${resource}/import`,
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false }
  })
}

/** 下载指定主数据资源的 Excel 导出文件。 */
export function exportMerchantResource(resource) {
  return request({ url: `/merchant/batch/${resource}/export`, method: 'get', responseType: 'blob' })
}
