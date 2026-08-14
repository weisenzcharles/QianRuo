import auth from '@/plugins/auth'
import router, { constantRoutes, dynamicRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index'
import ParentView from '@/components/ParentView'
import InnerLink from '@/layout/components/InnerLink'

// 匹配views里面所有的.vue文件
const modules = import.meta.glob('./../../views/**/*.vue')

const usePermissionStore = defineStore(
  'permission',
  {
    state: () => ({
      routes: [],
      addRoutes: [],
      defaultRoutes: [],
      topbarRouters: [],
      sidebarRouters: []
    }),
    actions: {
      setRoutes(routes) {
        this.addRoutes = routes
        this.routes = constantRoutes.concat(routes)
      },
      setDefaultRoutes(routes) {
        this.defaultRoutes = constantRoutes.concat(routes)
      },
      setTopbarRoutes(routes) {
        this.topbarRouters = routes
      },
      setSidebarRouters(routes) {
        this.sidebarRouters = routes
      },
      generateRoutes(roles) {
        return new Promise(resolve => {
          // 向后端请求路由数据
          getRouters().then(res => {
            const normalizedRoutes = normalizeMerchantRoutes(res.data || [])
            const sdata = JSON.parse(JSON.stringify(normalizedRoutes))
            const rdata = JSON.parse(JSON.stringify(normalizedRoutes))
            const defaultData = JSON.parse(JSON.stringify(normalizedRoutes))
            const sidebarRoutes = filterAsyncRouter(sdata)
            const rewriteRoutes = filterAsyncRouter(rdata, false, true)
            const defaultRoutes = filterAsyncRouter(defaultData)
            const asyncRoutes = filterDynamicRoutes(dynamicRoutes)
            asyncRoutes.forEach(route => { router.addRoute(route) })
            this.setRoutes(rewriteRoutes)
            this.setSidebarRouters(constantRoutes.concat(sidebarRoutes))
            this.setDefaultRoutes(sidebarRoutes)
            this.setTopbarRoutes(defaultRoutes)
            resolve(rewriteRoutes)
          })
        })
      }
    }
  })

// 遍历后台传来的路由字符串，转换为组件对象
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      // Layout ParentView 组件特殊处理
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        route.component = loadView(route.component)
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}

function filterChildren(childrenMap, lastRouter = false) {
  var children = []
  childrenMap.forEach(el => {
    // 绝对路由已经由后端定义完整路径，不能再次拼接父路由；兼容目录型相对路由时再拼接。
    if (lastRouter && !el.path.startsWith('/')) {
      el.path = lastRouter.path + '/' + el.path
    }
    if (el.children && el.children.length && el.component === 'ParentView') {
      children = children.concat(filterChildren(el.children, el))
    } else {
      children.push(el)
    }
  })
  return children
}

/**
 * 规范化商户动态菜单路径。
 *
 * <p>商户菜单表保存业务完整路径，例如 merchant/product/my。Vue Router 的嵌套路由
 * 默认会把子路径拼接到父路径，因此这里统一转换为带前导斜杠的绝对路径，保证每个
 * 商户菜单都在当前单页应用内跳转，不会因为父子路径重复而产生错误页面。</p>
 *
 * @param {Array} routes 后端返回的商户路由树
 * @returns {Array} 规范化后的商户路由树
 */
function normalizeMerchantRoutes(routes) {
  const iconAliases = {
    shop: 'shopping',
    goods: 'component',
    house: 'server',
    van: 'upload',
    coin: 'money',
    connection: 'link',
    operation: 'tool',
    box: 'component',
    folder: 'zip'
  }
  return routes.map(route => {
    if (route.path && !route.path.startsWith('/') && !/^https?:\/\//.test(route.path)) {
      route.path = '/' + route.path
    }
    if (Array.isArray(route.children)) {
      route.children = normalizeMerchantRoutes(route.children)
    }
    if (route.meta) {
      route.meta.icon = iconAliases[route.meta.icon] || route.meta.icon || 'component'
    }
    return route
  })
}

// 动态路由遍历，验证是否具备权限
export function filterDynamicRoutes(routes) {
  const res = []
  routes.forEach(route => {
    if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route)
      }
    } else if (route.roles) {
      if (auth.hasRoleOr(route.roles)) {
        res.push(route)
      }
    }
  })
  return res
}

export const loadView = (view) => {
  let res
  for (const path in modules) {
    const dir = path.split('views/')[1].split('.vue')[0]
    if (dir === view) {
      res = () => modules[path]()
    }
  }
  return res
}

export default usePermissionStore
