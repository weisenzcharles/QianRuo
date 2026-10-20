import request from '@/utils/request'

export function listPlatformTickets(query) {
  return request({ url: '/admin/rental/ticket/list', method: 'get', params: query })
}

export function getPlatformTicket(ticketId) {
  return request({ url: `/admin/rental/ticket/${ticketId}`, method: 'get' })
}

export function listPlatformTicketAssignees() {
  return request({ url: '/admin/rental/ticket/assignees', method: 'get' })
}

export function addPlatformTicket(data) {
  return request({ url: '/admin/rental/ticket', method: 'post', data })
}

export function appendPlatformTicketEvent(ticketId, data) {
  return request({ url: `/admin/rental/ticket/${ticketId}/events`, method: 'post', data })
}

export function listKnowledgeCategories() {
  return request({ url: '/admin/rental/knowledge/categories', method: 'get' })
}

export function listKnowledgeArticles(query) {
  return request({ url: '/admin/rental/knowledge/articles', method: 'get', params: query })
}

export function listKnowledgeComplianceArticles(query) {
  return request({ url: '/admin/rental/knowledge/compliance/articles', method: 'get', params: query })
}

export function getKnowledgeArticle(articleId) {
  return request({ url: `/admin/rental/knowledge/articles/${articleId}`, method: 'get' })
}

export function getKnowledgeComplianceArticle(articleId) {
  return request({ url: `/admin/rental/knowledge/compliance/articles/${articleId}`, method: 'get' })
}

export function addKnowledgeArticle(data) {
  return request({ url: '/admin/rental/knowledge/articles', method: 'post', data })
}

export function updateKnowledgeArticle(data) {
  return request({ url: '/admin/rental/knowledge/articles', method: 'put', data })
}

export function publishKnowledgeArticle(articleId) {
  return request({ url: `/admin/rental/knowledge/articles/${articleId}/publish`, method: 'post' })
}

export function offlineKnowledgeArticle(articleId) {
  return request({ url: `/admin/rental/knowledge/articles/${articleId}/offline`, method: 'post' })
}

export function offlineMerchantKnowledgeArticle(articleId, data) {
  return request({ url: `/admin/rental/knowledge/compliance/articles/${articleId}/offline`, method: 'post', data })
}

export function deleteKnowledgeArticle(articleId) {
  return request({ url: `/admin/rental/knowledge/articles/${articleId}`, method: 'delete' })
}

export function listRecycleRecords(query) {
  return request({ url: '/admin/rental/recycle/list', method: 'get', params: query })
}

export function restoreRecycleRecord(recycleId) {
  return request({ url: `/admin/rental/recycle/${recycleId}/restore`, method: 'post' })
}
