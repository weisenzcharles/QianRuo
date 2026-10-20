<template>
  <div class="app-container merchant-list-page merchant-account-profile-page">
    <el-row :gutter="20">
      <el-col :span="6" :xs="24" class="profile-sidebar-column">
        <el-card class="box-card">
          <template v-slot:header>
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
            <el-descriptions-item label="账号身份">
              {{ memberRoleLabel }}
            </el-descriptions-item>
            <el-descriptions-item label="仓库权限">
              {{ warehouseScopeLabel }}
            </el-descriptions-item>
            <el-descriptions-item label="可访问仓库">
              <el-space v-if="warehouseNames.length" wrap :size="6">
                <el-tag
                  v-for="warehouseName in warehouseNames"
                  :key="warehouseName"
                  size="small"
                  type="info"
                  effect="plain">
                  {{ warehouseName }}
                </el-tag>
              </el-space>
              <span v-else>{{ emptyWarehouseLabel }}</span>
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
          <template v-slot:header>
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
import userAvatar from "./userAvatar";
import userInfo from "./userInfo";
import resetPwd from "./resetPwd";
import { getMerchantProfile } from "@/api/merchant/account";
import { listMerchantWarehouseOptions } from "@/api/merchant/user";
import { NORMAL_DISABLE_CODE } from "@/constants/options";
import { RENTAL_CODE } from "@/utils/rental/options";

const route = useRoute();
const selectedTab = ref("userinfo");
const state = reactive({
  user: {},
  roleGroup: "",
  warehouses: [],
});

const roleNames = computed(() => {
  const names = state.user.roleNames;
  if (Array.isArray(names)) return names.filter(Boolean);
  return state.roleGroup ? [state.roleGroup] : [];
});
const memberRoleLabel = computed(() => {
  if (state.user.memberRole === "owner") return "商户所有者";
  if (state.user.memberRole === "staff") return "商户成员";
  return "-";
});
const warehouseScopeLabel = computed(() => {
  const scope = Number(state.user.warehouseScopeType);
  if (scope === RENTAL_CODE.WAREHOUSE_SCOPE.ALL) return "全部仓库";
  if (scope === RENTAL_CODE.WAREHOUSE_SCOPE.SPECIFIED) return "指定仓库";
  return "-";
});
const warehouseNames = computed(() =>
  [...new Set(state.warehouses.map((warehouse) => warehouse.warehouseName).filter(Boolean))],
);
const emptyWarehouseLabel = computed(() =>
  Number(state.user.warehouseScopeType) === RENTAL_CODE.WAREHOUSE_SCOPE.ALL
    ? "全部启用仓库"
    : "未分配可用仓库",
);
const accountStatus = computed(() => {
  const rawStatus = state.user.status;
  if (rawStatus === null || rawStatus === undefined || rawStatus === "") {
    return { label: "未知", type: "info" };
  }
  const status = Number(rawStatus);
  if (status === NORMAL_DISABLE_CODE.NORMAL) {
    return { label: "正常", type: "success" };
  }
  if (status === NORMAL_DISABLE_CODE.DISABLED) {
    return { label: "停用", type: "danger" };
  }
  return { label: "未知", type: "info" };
});

function getUser() {
  Promise.all([
    getMerchantProfile(),
    listMerchantWarehouseOptions()
      .then((response) => response.data || [])
      .catch(() => []),
  ]).then(([response, warehouses]) => {
    state.user = response.data || {};
    state.roleGroup = response.roleGroup || "";
    state.warehouses = warehouses;
  });
}

onMounted(() => {
  const activeTab = route.params && route.params.activeTab;
  if (activeTab) {
    selectedTab.value = activeTab;
  }
  getUser();
});
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
