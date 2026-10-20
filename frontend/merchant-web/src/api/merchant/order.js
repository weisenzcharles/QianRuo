import request from '@/utils/request'

/**
 * 查询当前商户的订单列表。
 *
 * 订单归属、租户和仓库范围由后端从登录上下文确定，前端只提交筛选条件。
 *
 * @param {Object} query 订单筛选条件
 * @returns {Promise} 当前商户订单分页数据
 */
export function listMerchantOrders(query) {
  return request({
    url: '/merchant/orders',
    method: 'get',
    params: query
  })
}

/** 查询订单中心顶部概览，统计不受列表筛选条件影响。 */
export function getMerchantOrderSummary() {
  return request({ url: '/merchant/orders/summary', method: 'get' })
}

/** 创建当前商户订单，租户和商户归属由后端登录上下文补齐。 */
export function addMerchantOrder(data) {
  return request({ url: '/merchant/orders', method: 'post', data })
}

/** 修改当前商户订单，后端会再次校验订单归属、审核状态和仓库权限。 */
export function updateMerchantOrder(data) {
  return request({ url: '/merchant/orders', method: 'put', data })
}

/** 将当前商户订单从待发货置为已备货，只更新订单状态。 */
export function prepareMerchantOrder(orderId) {
  return request({ url: `/merchant/orders/${orderId}/prepare`, method: 'post' })
}

/** 保存发货快递信息并将订单推进到已发货。 */
export function shipMerchantOrder(orderId, data) {
  return request({ url: `/merchant/orders/${orderId}/ship`, method: 'post', data })
}

/** 登记订单已归还入库，并将订单推进到归还中。 */
export function markMerchantOrderReturning(orderId) {
  return request({ url: `/merchant/orders/${orderId}/returning`, method: 'post' })
}

/** 提交归还验机结果，正常完成订单或创建售后单。 */
export function inspectMerchantOrderReturn(orderId, data) {
  return request({
    url: `/merchant/orders/${orderId}/return-inspection`,
    method: 'post',
    data
  })
}

/** 查询当前订单的履约媒体凭证。 */
export function listMerchantOrderMedia(orderId) {
  return request({ url: `/merchant/orders/${orderId}/media`, method: 'get' })
}

/** 登记统一文件服务已上传的订单履约媒体凭证。 */
export function addMerchantOrderMedia(orderId, data) {
  return request({ url: `/merchant/orders/${orderId}/media`, method: 'post', data })
}

/** 查询当前商户待识别或待人工处理的批量发货视频。 */
export function listUnmatchedMerchantOrderMedia() {
  return request({ url: '/merchant/orders/media/unmatched', method: 'get' })
}

/** 批量登记已上传的发货视频，并按 SN 尝试匹配未完成订单。 */
export function addMerchantOrderMediaBatch(data) {
  return request({ url: '/merchant/orders/media/batch', method: 'post', data })
}

/** 对未匹配发货视频进行二次识别或人工补录 SN。 */
export function recognizeMerchantOrderMedia(mediaId, data) {
  return request({
    url: `/merchant/orders/media/${mediaId}/recognize`,
    method: 'put',
    data
  })
}

/** 将归还或售后订单推进到已完成。 */
export function completeMerchantOrder(orderId, data) {
  return request({ url: `/merchant/orders/${orderId}/complete`, method: 'post', data })
}

/** 取消未发货订单并释放库存预占。 */
export function cancelMerchantOrder(orderId, data) {
  return request({ url: `/merchant/orders/${orderId}/cancel`, method: 'post', data })
}

/** 为租赁中或归还阶段订单创建售后单。 */
export function createMerchantOrderAfterSales(orderId, data) {
  return request({ url: `/merchant/orders/${orderId}/after-sales`, method: 'post', data })
}

/** 更新当前订单指定方向的快递信息，不修改订单状态。 */
export function updateMerchantOrderLogistics(orderId, data) {
  return request({ url: `/merchant/orders/${orderId}/logistics`, method: 'put', data })
}

/** 查询当前订单指定方向的物流轨迹。 */
export function getMerchantOrderLogisticsTrack(orderId, direction) {
  return request({
    url: `/merchant/orders/${orderId}/logistics/track`,
    method: 'get',
    params: { direction }
  })
}

/** 更新当前订单的客户、收件人和备注快照信息。 */
export function updateMerchantOrderRecipient(orderId, data) {
  return request({ url: `/merchant/orders/${orderId}/recipient`, method: 'put', data })
}

/** 将运输中的订单转寄到新的收件地址，不改变订单履约状态。 */
export function forwardMerchantOrder(orderId, data) {
  return request({ url: `/merchant/orders/${orderId}/forward`, method: 'post', data })
}

/** 查询当前商户订单详情，用于编辑页面回显。 */
export function getMerchantOrder(orderId) {
  return request({ url: `/merchant/orders/${orderId}`, method: 'get' })
}

/** 查询订单套餐明细可绑定的逐件设备。 */
export function listMerchantOrderPackageDevices(orderId, itemIndex) {
  return request({
    url: `/merchant/orders/${orderId}/package-devices`,
    method: 'get',
    params: { itemIndex }
  })
}

/** 绑定订单套餐明细的逐件设备，不改变订单工作流状态。 */
export function bindMerchantOrderPackageDevices(orderId, data) {
  return request({
    url: `/merchant/orders/${orderId}/package-devices`,
    method: 'put',
    data
  })
}

/** 查询当前商户指定订单的历史批注，后端按订单归属和软删除状态过滤。 */
export function getMerchantOrderNotes(orderId) {
  return request({ url: `/merchant/orders/${orderId}/notes`, method: 'get' })
}

/** 新增当前商户指定订单的批注。 */
export function addMerchantOrderNote(orderId, data) {
  return request({ url: `/merchant/orders/${orderId}/notes`, method: 'post', data })
}

/** 查询当前商户指定订单的字段编辑历史，后端按创建时间倒序返回。 */
export function getMerchantOrderEditLogs(orderId) {
  return request({ url: `/merchant/orders/${orderId}/edit-logs`, method: 'get' })
}

/** 查询当前商户的租商联盟转单目标。 */
export function listMerchantAllianceOptions() {
  return request({ url: '/merchant/orders/alliance-options', method: 'get' })
}

/** 将订单转给租商联盟或指定联盟商户。 */
export function transferMerchantOrder(data) {
  return request({ url: '/merchant/orders/alliance', method: 'post', data })
}

/** 创建当前商户订单的支付宝支付单。 */
export function createMerchantOrderPayment(orderId) {
  return request({ url: `/merchant/payments/orders/${orderId}`, method: 'post' })
}
