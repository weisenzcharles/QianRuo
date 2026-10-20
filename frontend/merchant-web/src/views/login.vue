<template>
  <div class="login">
    <el-form
      ref="loginRef"
      :model="loginForm"
      :rules="loginRules"
      class="login-form">
      <h3 class="title">{{ title }}</h3>
      <el-tabs
        v-model="loginForm.loginType"
        class="login-tabs"
        stretch
        @tab-change="handleLoginTypeChange">
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
            placeholder="账号或手机号">
            <template #prefix>
              <svg-icon icon-class="user" class="el-input__icon input-icon" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            auto-complete="off"
            placeholder="密码"
            @keyup.enter="handleLogin">
            <template #prefix>
              <svg-icon
                icon-class="password"
                class="el-input__icon input-icon" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code" v-if="captchaEnabled">
          <el-input
            v-model="loginForm.code"
            size="large"
            auto-complete="off"
            placeholder="验证码"
            style="width: 63%"
            @keyup.enter="handleLogin">
            <template #prefix>
              <svg-icon
                icon-class="validCode"
                class="el-input__icon input-icon" />
            </template>
          </el-input>
          <div class="login-code">
            <img :src="codeUrl" @click="getCode" class="login-code-img" />
          </div>
        </el-form-item>
        <el-checkbox
          v-model="loginForm.rememberMe"
          style="margin: 0px 0px 25px 0px">
          记住密码
        </el-checkbox>
      </template>
      <template v-else>
        <el-form-item prop="phone">
          <el-input
            v-model="loginForm.phone"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="手机号">
            <template #prefix>
              <svg-icon icon-class="phone" class="el-input__icon input-icon" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="smsCode">
          <div class="sms-code-row">
            <el-input
              v-model="loginForm.smsCode"
              class="sms-code-input"
              size="large"
              maxlength="4"
              auto-complete="off"
              placeholder="四位验证码"
              @keyup.enter="handleLogin">
              <template #prefix>
                <svg-icon
                  icon-class="validCode"
                  class="el-input__icon input-icon" />
              </template>
            </el-input>
            <el-button
              class="sms-send-button"
              size="large"
              :loading="sendingSms"
              :disabled="sendingSms || smsCountdownActive"
              @click="handleSendSmsCode">
              {{ smsCountdownActive ? `${smsCountdown}s 后重试` : "获取验证码" }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item v-if="captchaEnabled" prop="captchaCode">
          <el-input
            v-model="loginForm.captchaCode"
            size="large"
            maxlength="8"
            auto-complete="off"
            placeholder="图形验证码"
            style="width: 63%"
            @keyup.enter="handleLogin">
            <template #prefix>
              <svg-icon
                icon-class="validCode"
                class="el-input__icon input-icon" />
            </template>
          </el-input>
          <div class="login-code">
            <img :src="codeUrl" @click="getCode" class="login-code-img" />
          </div>
        </el-form-item>
        <el-checkbox
          v-model="loginForm.rememberMe"
          style="margin: 0px 0px 25px 0px">
          记住密码
        </el-checkbox>
        <div class="sms-login-tip">短信验证码有效期 5 分钟</div>
      </template>
      <el-form-item style="width: 100%">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          style="width: 100%"
          @click.prevent="handleLogin">
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
      </el-form-item>
      <div class="register-entry">
        <el-button link type="primary" @click="router.push('/register')">
          申请入驻
        </el-button>
        <!-- <el-button link type="primary" @click="router.push({ path: '/register', query: { mode: 'lookup' } })">查询入驻进度</el-button> -->
      </div>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg, sendSmsCode } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from "@/utils/jsencrypt";
import useUserStore from "@/store/modules/user";
import defaultSettings from "@/settings";

const title = import.meta.env.VITE_APP_TITLE;
const footerContent = defaultSettings.footerContent;
const usernameKey = "merchant-username";
const passwordKey = "merchant-password";
const rememberMeKey = "merchant-rememberMe";
const userStore = useUserStore();
const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance();

const loginForm = ref({
  loginType: "password",
  username: "",
  password: "",
  rememberMe: false,
  code: "",
  uuid: "",
  phone: "",
  smsCode: "",
  captchaCode: "",
});
const captchaEnabled = ref(true);
const sendingSms = ref(false);
const smsCountdown = ref(0);
const smsCountdownPhone = ref("");
let smsCountdownTimer;

const passwordLoginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }],
};
const smsLoginRules = computed(() => ({
  phone: [
    { required: true, trigger: "blur", message: "请输入手机号" },
    {
      pattern: /^1[3-9]\d{9}$/,
      trigger: "blur",
      message: "请输入正确的手机号",
    },
  ],
  smsCode: [
    { required: true, trigger: "change", message: "请输入验证码" },
    { pattern: /^\d{4}$/, trigger: "change", message: "请输入四位数字验证码" },
  ],
  ...(captchaEnabled.value
    ? {
        captchaCode: [
          { required: true, trigger: "change", message: "请输入图形验证码" },
        ],
      }
    : {}),
}));
const loginRules = computed(() =>
  loginForm.value.loginType === "sms" ? smsLoginRules.value : passwordLoginRules,
);

const codeUrl = ref("");
const loading = ref(false);
const redirect = ref(undefined);
const smsCountdownActive = computed(
  () => smsCountdown.value > 0 && smsCountdownPhone.value === loginForm.value.phone.trim(),
);

watch(
  route,
  (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect;
  },
  { immediate: true },
);

function handleLogin() {
  proxy.$refs.loginRef.validate((valid) => {
    if (valid) {
      loading.value = true;
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set(usernameKey, loginForm.value.username, { expires: 30 });
        if (loginForm.value.loginType === "password") {
          Cookies.set(passwordKey, encrypt(loginForm.value.password), {
            expires: 30,
          });
        }
        Cookies.set("merchant-phone", loginForm.value.phone, { expires: 30 });
        Cookies.set(rememberMeKey, loginForm.value.rememberMe, { expires: 30 });
      } else {
        // 否则移除
        Cookies.remove(usernameKey);
        Cookies.remove(passwordKey);
        Cookies.remove("merchant-phone");
        Cookies.remove(rememberMeKey);
      }
      // 调用action的登录方法
      userStore
        .login(loginForm.value)
        .then(async () => {
          const query = route.query;
          const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
            if (cur !== "redirect") {
              acc[cur] = query[cur];
            }
            return acc;
          }, {});
          const failure = await router.replace({
            path: redirect.value || "/merchant/home",
            query: otherQueryParams,
          });
          if (failure) {
            throw failure;
          }
        })
        .catch((error) => {
          loading.value = false;
          if (error?.type || error?.message?.includes("navigation")) {
            proxy.$modal.msgError("登录后页面跳转失败，请刷新页面重试");
          }
          // 重新获取验证码
          if (captchaEnabled.value) {
            getCode();
          }
        });
    }
  });
}

function getCode() {
  getCodeImg().then((res) => {
    captchaEnabled.value =
      res.captchaEnabled === undefined ? true : res.captchaEnabled;
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img;
      loginForm.value.uuid = res.uuid;
    }
  });
}

async function handleSendSmsCode() {
  const fields = ["phone"];
  if (captchaEnabled.value) {
    fields.push("captchaCode");
  }
  try {
    await proxy.$refs.loginRef.validateField(fields);
  } catch {
    return;
  }

  sendingSms.value = true;
  try {
    const phone = loginForm.value.phone.trim();
    const response = await sendSmsCode(
      phone,
      captchaEnabled.value ? loginForm.value.captchaCode.trim() : "",
      loginForm.value.uuid,
    );
    proxy.$modal.msgSuccess(response.msg || "验证码请求已提交");
    loginForm.value.smsCode = "";
    if (captchaEnabled.value) {
      loginForm.value.captchaCode = "";
      getCode();
    }
    startSmsCountdown(phone);
  } catch {
    if (captchaEnabled.value) {
      loginForm.value.captchaCode = "";
      getCode();
    }
  } finally {
    sendingSms.value = false;
  }
}

function startSmsCountdown(phone) {
  if (smsCountdownTimer) {
    clearInterval(smsCountdownTimer);
  }
  smsCountdownPhone.value = phone;
  smsCountdown.value = 60;
  smsCountdownTimer = setInterval(() => {
    if (smsCountdown.value <= 1) {
      clearInterval(smsCountdownTimer);
      smsCountdownTimer = undefined;
      smsCountdown.value = 0;
      smsCountdownPhone.value = "";
      return;
    }
    smsCountdown.value -= 1;
  }, 1000);
}

function getCookie() {
  const username = Cookies.get(usernameKey);
  const password = Cookies.get(passwordKey);
  const rememberMe = Cookies.get(rememberMeKey);
  const phone = Cookies.get("merchant-phone");
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password:
      password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
    loginType: loginForm.value.loginType,
    code: loginForm.value.code,
    uuid: loginForm.value.uuid,
    phone: phone === undefined ? loginForm.value.phone : phone,
    smsCode: loginForm.value.smsCode,
    captchaCode: loginForm.value.captchaCode,
  };
}

function handleLoginTypeChange() {
  proxy.$refs.loginRef.clearValidate();
  if (loginForm.value.loginType === "password" && !loginForm.value.uuid) {
    getCode();
  }
}

onUnmounted(() => {
  if (smsCountdownTimer) {
    clearInterval(smsCountdownTimer);
  }
});

getCode();
getCookie();
</script>

<style lang="scss" scoped>
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
.sms-code-row {
  display: flex;
  width: 100%;
  gap: 10px;
}
.sms-code-input {
  min-width: 0;
  flex: 1;
}
.sms-send-button {
  width: 118px;
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
