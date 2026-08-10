import router from '@/router'
import { ElMessageBox } from 'element-plus'
import { login, smsLogin, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { isEmpty } from "@/utils/validate"
import useLockStore from '@/store/modules/lock'
import defAva from '@/assets/images/profile.jpg'

const useUserStore = defineStore(
  'user',
  {
    state: () => ({
      token: getToken(),
      id: '',
      name: '',
      nickName: '',
      avatar: '',
      roles: [],
      permissions: [],
      mustChangePassword: false,
      merchantAuthStatus: '',
      featureEnabled: false
    }),
    actions: {
      // 登录
      login(userInfo) {
        removeToken()
        this.token = ''
        this.roles = []
        this.permissions = []
        this.mustChangePassword = false
        this.merchantAuthStatus = ''
        this.featureEnabled = false
        const loginType = userInfo.loginType || 'password'
        const username = (userInfo.username || '').trim()
        const password = userInfo.password
        const code = userInfo.code
        const uuid = userInfo.uuid
        const phone = (userInfo.phone || '').trim()
        const smsCode = userInfo.smsCode
        const loginAction = loginType === 'sms' ? smsLogin(phone, smsCode) : login(username, password, code, uuid)
        return new Promise((resolve, reject) => {
          loginAction.then(res => {
            let data = res.data
            setToken(data.access_token)
            this.token = data.access_token
            useLockStore().unlockScreen()
            resolve()
          }).catch(error => {
            this.token = ''
            this.roles = []
            this.permissions = []
            this.mustChangePassword = false
            this.merchantAuthStatus = ''
            this.featureEnabled = false
            removeToken()
            reject(error)
          })
        })
      },
      // 获取用户信息
      getInfo() {
        return new Promise((resolve, reject) => {
          getInfo().then(res => {
            const user = res.user
            const avatar = (isEmpty(user.avatar)) ? defAva : user.avatar
            if (res.roles && res.roles.length > 0) { // 验证返回的roles是否是一个非空数组
              this.roles = res.roles
            this.permissions = res.permissions
            } else {
              this.roles = ['ROLE_DEFAULT']
            }
            this.merchantAuthStatus = res.merchantAuthStatus || ''
            this.featureEnabled = res.featureEnabled === true
            this.id = user.userId
            this.name = user.userName
            this.nickName = user.nickName
            this.avatar = avatar
            /* 过期密码提示 */
            this.mustChangePassword = res.mustChangePassword === true
            if(!this.mustChangePassword && res.isPasswordExpired) {
              ElMessageBox.confirm('您的密码已过期，请尽快修改密码！',  '安全提示', {  confirmButtonText: '确定',  cancelButtonText: '取消',  type: 'warning' }).then(() => {
                router.push({ name: 'Profile', params: { activeTab: 'resetPwd' } })
              }).catch(() => {})
            }
            resolve(res)
          }).catch(error => {
            this.token = ''
            this.roles = []
            this.permissions = []
            this.mustChangePassword = false
            this.merchantAuthStatus = ''
            this.featureEnabled = false
            removeToken()
            reject(error)
          })
        })
      },
      // 退出系统
      logOut() {
        return new Promise((resolve, reject) => {
          logout(this.token).then(() => {
            this.token = ''
            this.roles = []
            this.permissions = []
            this.mustChangePassword = false
            this.merchantAuthStatus = ''
            this.featureEnabled = false
            removeToken()
            resolve()
          }).catch(error => {
            this.token = ''
            this.roles = []
            this.permissions = []
            this.mustChangePassword = false
            this.merchantAuthStatus = ''
            this.featureEnabled = false
            removeToken()
            reject(error)
          })
        })
      },
      completePasswordInitialization() {
        this.mustChangePassword = false
      }
    }
  })

export default useUserStore
