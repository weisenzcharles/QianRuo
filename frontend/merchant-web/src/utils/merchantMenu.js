import { isExternal } from '@/utils/validate'

/** 商户端内嵌页面的统一本地路由。 */
export const MERCHANT_INNER_LINK_PATH = '/merchant/inner-link'

/**
 * 判断菜单目标是否为需要在当前内容区打开的外部地址。
 *
 * @param {string|Object} target 菜单目标路径或路由位置对象
 * @returns {boolean} true 表示目标是 HTTP、HTTPS、mailto 或 tel 地址
 */
export function isMerchantExternalMenu(target) {
  const path = typeof target === 'string' ? target : target?.path
  return typeof path === 'string' && isExternal(path)
}

/**
 * 将菜单目标转换为商户端的站内路由位置。
 *
 * <p>外部地址统一转为内嵌页面路由，并通过查询参数传递原始地址；普通地址保持原有
 * Vue Router 跳转方式。该方法是侧边栏、顶部菜单和菜单搜索共同使用的导航入口，
 * 从源头上禁止菜单调用浏览器新窗口。</p>
 *
 * @param {string|Object} target 菜单目标路径或路由位置对象
 * @param {Object} query 菜单查询参数
 * @returns {string|Object} Vue Router 可识别的路由位置
 */
export function resolveMerchantMenuTarget(target, query) {
  if (isMerchantExternalMenu(target)) {
    const path = typeof target === 'string' ? target : target.path
    return {
      path: MERCHANT_INNER_LINK_PATH,
      query: { url: path }
    }
  }
  if (query) {
    const path = typeof target === 'string' ? target : target.path
    return { path, query }
  }
  return target
}
