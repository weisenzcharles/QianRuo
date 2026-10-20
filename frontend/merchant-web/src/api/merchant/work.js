import request from '@/utils/request'

export function listWorkTasks(params) {
  return request({ url: '/merchant/work/tasks', method: 'get', params })
}

export function getWorkSummary() {
  return request({ url: '/merchant/work/summary', method: 'get' })
}

export function getWorkDailyReport(params) {
  return request({ url: '/merchant/work/report', method: 'get', params })
}

export function listWorkUsers() {
  return request({ url: '/merchant/work/users', method: 'get' })
}

export function getWorkTask(id) {
  return request({ url: `/merchant/work/tasks/${id}`, method: 'get' })
}

export function createWorkTask(data) {
  return request({ url: '/merchant/work/tasks', method: 'post', data })
}

export function submitWorkTask(id, data) {
  return request({ url: `/merchant/work/tasks/${id}/submit`, method: 'post', data })
}

export function reviewWorkTask(id, data) {
  return request({ url: `/merchant/work/tasks/${id}/review`, method: 'post', data })
}

export function markWorkTaskRead(id) {
  return request({ url: `/merchant/work/tasks/${id}/read`, method: 'post' })
}
