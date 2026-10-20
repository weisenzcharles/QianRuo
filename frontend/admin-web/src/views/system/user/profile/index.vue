<template>
  <div class="app-container platform-account-profile-page">
    <el-row :gutter="20">
      <el-col :span="6" :xs="24" class="profile-sidebar-column">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>个人信息</span>
            </div>
          </template>
          <el-row justify="center" class="profile-avatar">
            <userAvatar />
          </el-row>
          <el-descriptions
            :column="1"
            border
            size="small"
            label-width="120"
            class="profile-descriptions">
            <el-descriptions-item label="用户名称">
              {{ state.user.userName || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="手机号码">
              {{ state.user.phonenumber || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="用户邮箱">
              {{ state.user.email || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="所属部门">
              {{ departmentLabel }}
            </el-descriptions-item>
            <el-descriptions-item label="所属岗位">
              <el-space v-if="postNames.length" wrap :size="6">
                <el-tag
                  v-for="postName in postNames"
                  :key="postName"
                  size="small"
                  type="info"
                  effect="plain">
                  {{ postName }}
                </el-tag>
              </el-space>
              <span v-else>未分配岗位</span>
            </el-descriptions-item>
            <el-descriptions-item label="所属角色">
              <el-space v-if="roleNames.length" wrap :size="6">
                <el-tag
                  v-for="roleName in roleNames"
                  :key="roleName"
                  size="small"
                  effect="plain">
                  {{ roleName }}
                </el-tag>
              </el-space>
              <span v-else>未分配角色</span>
            </el-descriptions-item>
            <el-descriptions-item label="账号状态">
              <el-tag :type="accountStatus.type" size="small">
                {{ accountStatus.label }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建日期">
              {{ state.user.createTime || "-" }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="18" :xs="24">
        <el-card>
          <template #header>
            <div class="clearfix">
              <span>基本资料</span>
            </div>
          </template>
          <el-tabs v-model="selectedTab">
            <el-tab-pane label="基本资料" name="userinfo">
              <userInfo :user="state.user" />
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="resetPwd">
              <resetPwd />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Profile">
import userAvatar from "./userAvatar"
import userInfo from "./userInfo"
import resetPwd from "./resetPwd"
import { getUserProfile } from "@/api/system/user"
import { NORMAL_DISABLE_CODE } from "@/utils/stateCode"

const route = useRoute()
const selectedTab = ref("userinfo")
const state = reactive({
  user: {},
  roleGroup: "",
  postGroup: ""
})

function splitNames(value) {
  return String(value || "")
    .split(/[,，]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

const departmentLabel = computed(() => state.user.dept?.deptName || "未分配部门")
const roleNames = computed(() => splitNames(state.roleGroup))
const postNames = computed(() => splitNames(state.postGroup))
const accountStatus = computed(() => {
  const status = Number(state.user.status)
  if (status === NORMAL_DISABLE_CODE.NORMAL) {
    return { label: "正常", type: "success" }
  }
  if (status === NORMAL_DISABLE_CODE.DISABLED) {
    return { label: "停用", type: "danger" }
  }
  return { label: "未知", type: "info" }
})

function getUser() {
  getUserProfile().then(response => {
    state.user = response.data || {}
    state.roleGroup = response.roleGroup || ""
    state.postGroup = response.postGroup || ""
  })
}

onMounted(() => {
  const activeTab = route.params && route.params.activeTab
  if (activeTab) {
    selectedTab.value = activeTab
  }
  getUser()
})
</script>

<style scoped>
.profile-avatar {
  margin-bottom: 18px;
}

.profile-descriptions {
  width: 100%;
}

.profile-descriptions :deep(.el-descriptions__content) {
  min-width: 0;
  overflow-wrap: anywhere;
}

.profile-descriptions :deep(.el-space) {
  max-width: 100%;
}
</style>
