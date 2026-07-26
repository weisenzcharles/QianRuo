<template>
  <div class="login">
    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{ title }}</h3>
      <el-tabs v-model="loginForm.loginType" class="login-tabs" stretch @tab-change="handleLoginTypeChange">
        <el-tab-pane label="账号密码" name="password" />
        <el-tab-pane label="手机号验证码" name="sms" />
      </el-tabs>
      <template v-if="loginForm.loginType === 'password'">
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="账号"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="loginForm.code"
          size="large"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      </template>
      <template v-else>
      <el-form-item prop="phone">
        <el-input
          v-model="loginForm.phone"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="手机号"
        >
          <template #prefix><svg-icon icon-class="phone" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="smsCode">
        <el-input
          v-model="loginForm.smsCode"
          size="large"
          maxlength="4"
          auto-complete="off"
          placeholder="四位验证码"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <div class="sms-login-tip">当前未接入短信，输入任意四位数字验证码</div>
      </template>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          style="width:100%;"
          @click.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
      </el-form-item>
      <div class="register-entry">
        <el-button link type="primary" @click="router.push('/register')">申请商户入驻</el-button>
        <el-button link type="primary" @click="router.push({ path: '/register', query: { mode: 'lookup' } })">查询入驻进度</el-button>
      </div>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const usernameKey = "merchant-username"
const passwordKey = "merchant-password"
const rememberMeKey = "merchant-rememberMe"
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({
  loginType: "password",
  username: "",
  password: "",
  rememberMe: false,
  code: "",
  uuid: "",
  phone: "",
  smsCode: ""
})

const passwordLoginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}
const smsLoginRules = {
  phone: [
    { required: true, trigger: "blur", message: "请输入手机号" },
    { pattern: /^1[3-9]\d{9}$/, trigger: "blur", message: "请输入正确的手机号" }
  ],
  smsCode: [
    { required: true, trigger: "change", message: "请输入验证码" },
    { pattern: /^\d{4}$/, trigger: "change", message: "请输入四位数字验证码" }
  ]
}
const loginRules = computed(() => loginForm.value.loginType === "sms" ? smsLoginRules : passwordLoginRules)

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关
const captchaEnabled = ref(true)
const redirect = ref(undefined)

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.loginType === "password" && loginForm.value.rememberMe) {
        Cookies.set(usernameKey, loginForm.value.username, { expires: 30 })
        Cookies.set(passwordKey, encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set(rememberMeKey, loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove(usernameKey)
        Cookies.remove(passwordKey)
        Cookies.remove(rememberMeKey)
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
        if (loginForm.value.loginType === "password" && captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get(usernameKey)
  const password = Cookies.get(passwordKey)
  const rememberMe = Cookies.get(rememberMeKey)
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
    loginType: loginForm.value.loginType,
    code: loginForm.value.code,
    uuid: loginForm.value.uuid,
    phone: loginForm.value.phone,
    smsCode: loginForm.value.smsCode
  }
}

function handleLoginTypeChange() {
  proxy.$refs.loginRef.clearValidate()
  if (loginForm.value.loginType === "password" && !loginForm.value.uuid) {
    getCode()
  }
}

getCode()
getCookie()
</script>

<style lang='scss' scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  z-index: 1;
  .el-input {
    height: 40px;
    input {
      height: 40px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 0px;
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-tabs {
  margin-bottom: 18px;
}
.sms-login-tip {
  margin: -8px 0 22px;
  font-size: 13px;
  color: #909399;
}
.register-entry {
  display: flex;
  justify-content: space-between;
  margin: -8px 0 16px;
}
.login-code {
  width: 33%;
  height: 40px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  height: 40px;
  padding-left: 12px;
}
</style>
