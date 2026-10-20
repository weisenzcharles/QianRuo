import request from '@/utils/request'

/** 查询平台通用工作台待办汇总。 */
export function getPlatformWorkbenchOverview() {
  return request({ url: '/admin/rental/workbench/overview', method: 'get' })
}
