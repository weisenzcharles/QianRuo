<template>
  <div class="app-container password-page">
    <section class="password-panel">
      <header class="panel-header">
        <el-icon><Lock /></el-icon>
        <div>
          <h2>设置初始密码</h2>
          <p>{{ accountName }}</p>
        </div>
      </header>

      <el-form ref="passwordRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入 5-20 位新密码"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            autocomplete="new-password"
            @keyup.enter="submit"
          />
        </el-form-item>
        <div class="form-actions">
          <el-button :icon="SwitchButton" @click="logout">退出登录</el-button>
          <el-button type="primary" :icon="Check" :loading="submitting" @click="submit">保存密码</el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { Check, Lock, SwitchButton } from '@element-plus/icons-vue'
import { initializeMerchantPassword } from '@/api/merchant/account'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const router = useRouter()
const userStore = useUserStore()
const passwordRef = ref()
const submitting = ref(false)

const accountName = computed(() => userStore.nickName || userStore.name)
const form = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const passwordRules = [
  { required: true, message: '新密码不能为空', trigger: 'blur' },
  { min: 5, max: 20, message: '密码长度必须在 5 到 20 个字符之间', trigger: 'blur' },
  { pattern: /^[^<>"'|\\]+$/, message: '密码不能包含非法字符', trigger: 'blur' }
]

const rules = {
  newPassword: passwordRules,
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function submit() {
  const valid = await passwordRef.value.validate().catch(() => false)
  if (!valid || submitting.value) {
    return
  }
  submitting.value = true
  try {
    await initializeMerchantPassword(form.newPassword)
    userStore.completePasswordInitialization()
    proxy.$modal.msgSuccess('初始密码设置成功')
    await router.replace('/')
  } finally {
    submitting.value = false
  }
}

async function logout() {
  await userStore.logOut().catch(() => {})
  window.location.href = '/login'
}
</script>

<style lang="scss" scoped>
.password-page {
  min-height: calc(100vh - 84px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: clamp(32px, 8vh, 88px);
}

.password-panel {
  width: min(100%, 520px);
  padding: 28px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
  box-shadow: 0 8px 24px rgb(0 0 0 / 8%);
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 26px;

  > .el-icon {
    width: 44px;
    height: 44px;
    flex: 0 0 44px;
    border-radius: 50%;
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    font-size: 22px;
  }

  h2 {
    margin: 0 0 6px;
    font-size: 22px;
    line-height: 1.3;
    letter-spacing: 0;
  }

  p {
    margin: 0;
    color: var(--el-text-color-secondary);
    overflow-wrap: anywhere;
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

@media (max-width: 600px) {
  .password-page {
    min-height: calc(100vh - 64px);
    padding: 20px 14px;
  }

  .password-panel {
    padding: 22px 18px;
  }

  .form-actions {
    flex-direction: column-reverse;

    .el-button {
      width: 100%;
      margin-left: 0;
    }
  }
}
</style>
