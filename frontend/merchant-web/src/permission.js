import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken, removeToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useLockStore from '@/store/modules/lock'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'
import { listMerchantProtocols } from '@/api/merchant/protocol'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register']
const initPasswordPath = '/user/init-password'
const merchantProtocolPath = '/merchant/user/protocol'

let merchantProtocolCheckToken = ''
let merchantProtocolCheckPromise = null

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

/** 清理当前登录令牌对应的协议校验结果，协议确认成功后调用。 */
export function clearMerchantProtocolCheckCache() {
  merchantProtocolCheckToken = ''
  merchantProtocolCheckPromise = null
}

/** 同一登录令牌只发起一次协议校验，避免首页多次导航产生重复请求。 */
function checkMerchantProtocols() {
  const token = getToken()
  if (merchantProtocolCheckToken !== token) {
    merchantProtocolCheckToken = token
    merchantProtocolCheckPromise = null
  }
  if (!merchantProtocolCheckPromise) {
    merchantProtocolCheckPromise = listMerchantProtocols({ scopeType: 1 })
      .then(response => (response.data || []).some(item => !item.accepted))
      .catch(error => {
        merchantProtocolCheckPromise = null
        throw error
      })
  }
  return merchantProtocolCheckPromise
}

/** 登录后强制确认商户网页端当前有效协议，确认页本身和初始密码页允许通过。 */
function continueWithProtocolCheck(target, next, { refreshRoute = false, forceRedirect = false } = {}) {
  if (target.path === merchantProtocolPath) {
    next()
    return
  }
  if (target.path === initPasswordPath) {
    if (forceRedirect) {
      next({ path: target.fullPath || target.path, replace: true })
    } else {
      next()
    }
    return
  }
  const proceed = () => {
    if (refreshRoute) {
      next({ path: target.fullPath || target.path, replace: true })
    } else {
      next()
    }
  }
  checkMerchantProtocols().then(pending => {
    if (pending) {
      next({ path: merchantProtocolPath, query: { redirect: target.fullPath || target.path }, replace: true })
      return
    }
    proceed()
  }).catch(() => {
    // 协议服务短暂不可用时不阻断已登录用户，页面接口恢复后仍会再次检查。
    proceed()
  })
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    const userStore = useUserStore()
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    const isLock = useLockStore().isLock
    /* has token*/
    if (to.path === '/login') {
      removeToken()
      userStore.token = ''
      userStore.roles = []
      userStore.permissions = []
      userStore.mustChangePassword = false
      next()
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else if (userStore.mustChangePassword && to.path !== initPasswordPath) {
      next({ path: initPasswordPath, replace: true })
      NProgress.done()
    } else if (isLock && to.path !== '/lock') {
      next({ path: '/lock' })
      NProgress.done()
    } else if (!isLock && to.path === '/lock') {
      next({ path: '/' })
      NProgress.done()
    } else {
      if (!userStore.mustChangePassword && userStore.roles.length > 0 && to.path === initPasswordPath) {
        next({ path: '/', replace: true })
        NProgress.done()
      } else if (userStore.roles.length === 0) {
        isRelogin.show = true
        // 判断当前用户是否已拉取完user_info信息
        userStore.getInfo().then(() => {
          isRelogin.show = false
          usePermissionStore().generateRoutes().then(accessRoutes => {
            // 根据roles权限生成可访问的路由表
            accessRoutes.forEach(route => {
              if (!isHttp(route.path)) {
                router.addRoute(route) // 动态添加可访问路由表
              }
            })
            const target = userStore.mustChangePassword && to.path !== initPasswordPath
              ? { path: initPasswordPath }
              : to
            continueWithProtocolCheck(target, next, {
              refreshRoute: true,
              forceRedirect: target !== to
            }) // 确保addRoutes完成后再校验协议
          })
        }).catch(err => {
          isRelogin.show = false
          userStore.logOut().catch(() => {}).finally(() => {
            ElMessage.error(err?.message || err)
            next({ path: '/login', replace: true })
          })
        })
      } else {
        continueWithProtocolCheck(to, next)
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`) // 否则全部重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
