import request from '@/utils/request'

/**
 * 统一请求商户租赁主数据列表。
 *
 * @param {string} resource 资源路径
 * @param {Object} params 查询参数
 * @returns {Promise} 分页结果
 */
function list(resource, params) {
  return request({ url: `/merchant/${resource}/list`, method: 'get', params })
}

/**
 * 统一读取商户租赁主数据详情。
 *
 * @param {string} resource 资源路径
 * @param {number} id 主键
 * @returns {Promise} 详情结果
 */
function get(resource, id) {
  return request({ url: `/merchant/${resource}/${id}`, method: 'get' })
}

/**
 * 统一新增商户租赁主数据。
 *
 * @param {string} resource 资源路径
 * @param {Object} data 写入命令
 * @returns {Promise} 新增结果
 */
function add(resource, data) {
  return request({ url: `/merchant/${resource}`, method: 'post', data })
}

/**
 * 统一修改商户租赁主数据。
 *
 * @param {string} resource 资源路径
 * @param {Object} data 写入命令
 * @returns {Promise} 修改结果
 */
function update(resource, data) {
  return request({ url: `/merchant/${resource}`, method: 'put', data })
}

/**
 * 统一软删除商户租赁主数据。
 *
 * @param {string} resource 资源路径
 * @param {number} id 主键
 * @returns {Promise} 删除结果
 */
function remove(resource, id) {
  return request({ url: `/merchant/${resource}/${id}`, method: 'delete' })
}

/** 查询仓库。 */
export function listMerchantWarehouses(params) { return list('warehouse', params) }

/** 查询当前商户仓库详情，用于编辑页面完整回显负责人、地址和仓库配置。 */
export function getMerchantWarehouse(id) { return get('warehouse', id) }

/** 查询本地数据库中的省、市、区县行政区划。 */
export function listMerchantRegionChildren(parentCode) {
  return request({ url: '/merchant/region/children', method: 'get', params: { parentCode: parentCode || '0' } })
}

/** 新增仓库。 */
export function addMerchantWarehouse(data) { return add('warehouse', data) }

/** 修改仓库。 */
export function updateMerchantWarehouse(data) { return update('warehouse', data) }

/** 删除仓库。 */
export function deleteMerchantWarehouse(id) { return remove('warehouse', id) }

/** 查询资源池。 */
export function listMerchantPools(params) { return list('pool', params) }

/** 新增资源池。 */
export function addMerchantPool(data) { return add('pool', data) }

/** 修改资源池。 */
export function updateMerchantPool(data) { return update('pool', data) }

/** 删除资源池。 */
export function deleteMerchantPool(id) { return remove('pool', id) }

/** 查询商品。 */
export function listMerchantProducts(params) { return list('product', params) }

/**
 * 查询当前登录商户的商品详情。
 *
 * <p>商品归属由 rental-business 从可信登录上下文解析，前端只提交商品主键，
 * 不携带也不拼接 tenantId、memberId 等隔离字段。</p>
 *
 * @param {number} id 商品主键
 * @returns {Promise} 当前商户商品详情
 */
export function getMerchantProduct(id) { return get('product', id) }

/** 新增商品。 */
export function addMerchantProduct(data) { return add('product', data) }

/** 修改商品。 */
export function updateMerchantProduct(data) { return update('product', data) }

/** 删除商品。 */
export function deleteMerchantProduct(id) { return remove('product', id) }

/** 查询 SN。 */
export function listMerchantSns(params) { return list('sn', params) }

/** 新增 SN。 */
export function addMerchantSn(data) { return add('sn', data) }

/** 修改 SN。 */
export function updateMerchantSn(data) { return update('sn', data) }

/** 删除 SN。 */
export function deleteMerchantSn(id) { return remove('sn', id) }

/** 查询逐件配件。 */
export function listMerchantAccessories(params) { return list('accessory', params) }

/** 新增逐件配件。 */
export function addMerchantAccessory(data) { return add('accessory', data) }

/** 修改逐件配件。 */
export function updateMerchantAccessory(data) { return update('accessory', data) }

/** 删除逐件配件。 */
export function deleteMerchantAccessory(id) { return remove('accessory', id) }

/** 查询套餐。 */
export function listMerchantPackages(params) { return list('package', params) }

/** 新增套餐。 */
export function addMerchantPackage(data) { return add('package', data) }

/** 修改套餐。 */
export function updateMerchantPackage(data) { return update('package', data) }

/** 删除套餐。 */
export function deleteMerchantPackage(id) { return remove('package', id) }

/** 查询库存。 */
export function listMerchantInventory(params) { return list('inventory', params) }

/** 删除零库存余额，库存流水不会被删除。 */
export function deleteMerchantInventory(productId, warehouseId) {
  return request({ url: `/merchant/inventory/${productId}/${warehouseId}`, method: 'delete' })
}

/** 调整数量型库存。 */
export function adjustMerchantInventory(data) {
  return request({ url: '/merchant/inventory/adjust', method: 'post', data })
}

/** 校验套餐资源池库存。 */
export function checkMerchantPackageInventory(data) {
  return request({ url: '/merchant/inventory/check-package', method: 'post', data })
}
