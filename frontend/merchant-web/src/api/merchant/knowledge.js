import request from '@/utils/request'

/** 查询平台公共和当前商户知识分类。 */
export function listMerchantKnowledgeCategories() {
  return request({ url: '/merchant/knowledge/categories', method: 'get' })
}

/** 查询当前商户可用的员工组。 */
export function listMerchantKnowledgeRoles() {
  return request({ url: '/merchant/knowledge/roles', method: 'get' })
}

/** 新增商户知识分类。 */
export function createMerchantKnowledgeCategory(data) {
  return request({ url: '/merchant/knowledge/categories', method: 'post', data })
}

/** 修改商户知识分类。 */
export function updateMerchantKnowledgeCategory(categoryId, data) {
  return request({ url: `/merchant/knowledge/categories/${categoryId}`, method: 'put', data })
}

/** 删除商户知识分类。 */
export function deleteMerchantKnowledgeCategory(categoryId) {
  return request({ url: `/merchant/knowledge/categories/${categoryId}`, method: 'delete' })
}

/** 分页查询知识文章。 */
export function listMerchantKnowledgeArticles(params) {
  return request({ url: '/merchant/knowledge/articles', method: 'get', params })
}

/** 查询知识文章详情和历史版本。 */
export function getMerchantKnowledgeArticle(articleId) {
  return request({ url: `/merchant/knowledge/articles/${articleId}`, method: 'get' })
}

/** 新增商户知识文章。 */
export function createMerchantKnowledgeArticle(data) {
  return request({ url: '/merchant/knowledge/articles', method: 'post', data })
}

/** 修改商户知识文章并追加版本。 */
export function updateMerchantKnowledgeArticle(articleId, data) {
  return request({ url: `/merchant/knowledge/articles/${articleId}`, method: 'put', data })
}

/** 发布商户知识文章。 */
export function publishMerchantKnowledgeArticle(articleId) {
  return request({ url: `/merchant/knowledge/articles/${articleId}/publish`, method: 'post' })
}

/** 下架商户知识文章。 */
export function offlineMerchantKnowledgeArticle(articleId) {
  return request({ url: `/merchant/knowledge/articles/${articleId}/offline`, method: 'post' })
}

/** 删除商户知识文章。 */
export function deleteMerchantKnowledgeArticle(articleId) {
  return request({ url: `/merchant/knowledge/articles/${articleId}`, method: 'delete' })
}

/** 记录当前用户的知识文章阅读。 */
export function markMerchantKnowledgeArticleRead(articleId) {
  return request({ url: `/merchant/knowledge/articles/${articleId}/read`, method: 'post' })
}
