<template>
  <div class="app-container merchant-list-page">
    <el-form
      ref="queryRef"
      v-show="showSearch"
      :model="query"
      :inline="true"
      label-width="68px"
      class="account-query-form"
      @submit.prevent>
      <el-form-item label="用户名" prop="userName">
        <el-input v-model="query.userName" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="query.phone" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="query.status" clearable>
          <el-option label="正常" :value="NORMAL_DISABLE_OPTIONS[0].value" />
          <el-option label="停用" :value="NORMAL_DISABLE_OPTIONS[1].value" />
        </el-select>
      </el-form-item>
      <el-form-item label-width="0" class="query-actions">
        <el-button type="primary" icon="Search" @click="load">查询</el-button>
        <el-button icon="Refresh" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="openAdd">
          添加用户
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="load" />
    </el-row>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="userName" label="用户名" min-width="130" />
      <el-table-column prop="nickName" label="昵称" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column
        prop="roleNames"
        label="角色"
        min-width="180"
        show-overflow-tooltip />
      <el-table-column label="仓库权限" width="120" align="center">
        <template #default="scope">
          <el-tag
            :type="
              scope.row.warehouseScopeType === RENTAL_CODE.WAREHOUSE_SCOPE.ALL
                ? 'success'
                : 'warning'
            ">
            {{
              scope.row.warehouseScopeType === RENTAL_CODE.WAREHOUSE_SCOPE.ALL
                ? "全部仓库"
                : "指定仓库"
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="scope">
          <el-tag
            :type="
              scope.row.status === NORMAL_DISABLE_OPTIONS[0].value
                ? 'success'
                : 'danger'
            ">
            {{
              scope.row.status === NORMAL_DISABLE_OPTIONS[0].value
                ? "正常"
                : "停用"
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="初始化密码" width="110">
        <template #default="scope">
          {{
            scope.row.mustChangePassword === YES_NO_CODE.YES
              ? "待初始化"
              : "已设置"
          }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" align="center" fixed="right" width="310">
        <template #default="scope">
          <div class="account-action-buttons">
            <el-button
              link
              type="primary"
              icon="View"
              @click="openDetail(scope.row)">
              详情
            </el-button>
            <el-button
              link
              type="primary"
              icon="Edit"
              @click="openEdit(scope.row)">
              编辑权限
            </el-button>
            <el-button
              link
              type="warning"
              icon="Refresh"
              @click="resetPassword(scope.row)">
              重置密码
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total > 0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="load" />
    <el-dialog
      v-model="detailVisible"
      title="商户用户详情"
      width="620px"
      append-to-body>
      <el-descriptions v-if="detailUser" :column="2" border label-width="120">
        <el-descriptions-item label="用户名">{{ detailUser.userName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detailUser.nickName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailUser.phone || "-" }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailUser.email || "-" }}</el-descriptions-item>
        <el-descriptions-item label="角色" :span="2">{{ detailRoleNames }}</el-descriptions-item>
        <el-descriptions-item label="仓库权限">{{ detailWarehouseScope }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ Number(detailUser.status) === NORMAL_DISABLE_OPTIONS[0].value ? "正常" : "停用" }}</el-descriptions-item>
        <el-descriptions-item label="归属仓库" :span="2">{{ detailWarehouseNames }}</el-descriptions-item>
        <el-descriptions-item label="密码状态">{{ Number(detailUser.mustChangePassword) === YES_NO_CODE.YES ? "待初始化" : "已设置" }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailUser.createTime || "-" }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
    <el-dialog
      v-model="visible"
      :title="editing ? '编辑商户用户' : '添加商户用户'"
      width="560px"
      destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="form.userName" :disabled="editing" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item v-if="!editing" label="初始密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="留空时仅可使用验证码登录" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="NORMAL_DISABLE_OPTIONS[0].value"
            :inactive-value="NORMAL_DISABLE_OPTIONS[1].value"
            active-text="正常"
            inactive-text="停用"
            inline-prompt />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="form.roleIds"
            multiple
            placeholder="请选择角色"
            style="width: 100%"
            @change="handleRoleChange">
            <el-option
              v-for="role in roles"
              :key="role.roleId"
              :label="role.roleName"
              :value="role.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库权限">
          <el-radio-group
            v-model="form.warehouseScopeType"
            :disabled="isAdministrator"
            @change="handleScopeChange">
            <el-radio :value="RENTAL_CODE.WAREHOUSE_SCOPE.ALL">
              全部仓库
            </el-radio>
            <el-radio :value="RENTAL_CODE.WAREHOUSE_SCOPE.SPECIFIED">
              指定仓库
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="
            form.warehouseScopeType === RENTAL_CODE.WAREHOUSE_SCOPE.SPECIFIED
          "
          label="归属仓库">
          <el-select
            v-model="form.warehouseIds"
            multiple
            filterable
            collapse-tags
            placeholder="请选择可访问仓库"
            max-collapse-tags="5"
            style="width: 100%">
            <el-option
              v-for="warehouse in warehouseOptions"
              :key="warehouse.warehouseId"
              :label="warehouse.warehouseName || warehouse.warehouseCode"
              :value="warehouse.warehouseId" />
          </el-select>
        </el-form-item>
        <el-alert
          title="角色对应菜单权限，用户首次登录后需要初始化密码。"
          type="info"
          :closable="false" />
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {
  addMerchantUser,
  getMerchantUser,
  listMerchantUsers,
  listMerchantWarehouseOptions,
  resetMerchantUserPassword,
  updateMerchantUser,
} from "@/api/merchant/user";
import { listRole } from "@/api/merchant/role";
import { ElMessage, ElMessageBox } from "element-plus";
import { NORMAL_DISABLE_OPTIONS, YES_NO_CODE } from "@/constants/options";
import { RENTAL_CODE } from "@/utils/rental/options";

const loading = ref(false);
const saving = ref(false);
const visible = ref(false);
const detailVisible = ref(false);
const detailUser = ref(null);
const rows = ref([]);
const total = ref(0);
const roles = ref([]);
const warehouseOptions = ref([]);
const showSearch = ref(true);
const editing = ref(false);
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: undefined,
  phone: undefined,
  status: undefined,
});
const form = reactive({});
const isAdministrator = computed(
  () =>
    form.memberRole === "owner" ||
    (form.roleIds || []).some((roleId) =>
      roles.value.some(
        (role) => role.roleId === roleId && role.roleKey === "merchant_admin",
      ),
    ),
);
const detailRoleNames = computed(() => {
  const roleIds = detailUser.value?.roleIds || [];
  const names = roleIds
    .map((roleId) => roles.value.find((role) => Number(role.roleId) === Number(roleId))?.roleName)
    .filter(Boolean);
  return names.join("、") || detailUser.value?.roleNames || "-";
});
const detailWarehouseScope = computed(() =>
  Number(detailUser.value?.warehouseScopeType) === RENTAL_CODE.WAREHOUSE_SCOPE.ALL
    ? "全部仓库"
    : "指定仓库",
);
const detailWarehouseNames = computed(() => {
  if (Number(detailUser.value?.warehouseScopeType) === RENTAL_CODE.WAREHOUSE_SCOPE.ALL) {
    return "全部仓库";
  }
  const names = (detailUser.value?.warehouseIds || [])
    .map((warehouseId) =>
      warehouseOptions.value.find(
        (warehouse) => Number(warehouse.warehouseId) === Number(warehouseId),
      )?.warehouseName,
    )
    .filter(Boolean);
  return names.join("、") || "未分配仓库";
});

/** 加载当前商户用户和角色列表。 */
function load() {
  loading.value = true;
  Promise.all([
    listMerchantUsers(query),
    listRole({ pageNum: 1, pageSize: 100 }),
  ])
    .then(([users, roleResult]) => {
      rows.value = users.rows || [];
      total.value = users.total || 0;
      roles.value = roleResult.rows || [];
    })
    .finally(() => {
      loading.value = false;
    });
}
/** 清空筛选条件。 */
function reset() {
  query.pageNum = 1;
  query.userName = undefined;
  query.phone = undefined;
  query.status = undefined;
  load();
}
/** 打开新增用户表单并设置默认状态。 */
function openAdd() {
  Object.assign(form, {
    userId: undefined,
    userName: "",
    nickName: "",
    phone: "",
    email: "",
    password: "",
    status: NORMAL_DISABLE_OPTIONS[0].value,
    roleIds: [],
    memberRole: "staff",
    warehouseScopeType: RENTAL_CODE.WAREHOUSE_SCOPE.ALL,
    warehouseIds: [],
  });
  editing.value = false;
  visible.value = true;
  loadWarehouseOptions();
}
/** 加载用户详情并打开权限编辑窗口。 */
function openEdit(row) {
  getMerchantUser(row.userId).then((response) => {
    Object.assign(form, response.data || response);
    form.roleIds = form.roleIds || [];
    form.warehouseIds = form.warehouseIds || [];
    if (isAdministrator.value) {
      form.warehouseScopeType = RENTAL_CODE.WAREHOUSE_SCOPE.ALL;
      form.warehouseIds = [];
    }
    editing.value = true;
    visible.value = true;
    loadWarehouseOptions();
  });
}
/** 读取最新用户资料并将角色、仓库权限转换为可读名称。 */
async function openDetail(row) {
  const response = await getMerchantUser(row.userId);
  detailUser.value = response.data || response;
  await loadWarehouseOptions();
  detailVisible.value = true;
}
/** 加载当前登录用户允许分配的启用仓库。 */
function loadWarehouseOptions() {
  return listMerchantWarehouseOptions()
    .then((response) => {
      warehouseOptions.value = Array.isArray(response.data)
        ? response.data
        : [];
    })
    .catch(() => {
      warehouseOptions.value = [];
    });
}
/** 管理员角色不受仓库范围限制。 */
function handleRoleChange() {
  if (isAdministrator.value) {
    form.warehouseScopeType = RENTAL_CODE.WAREHOUSE_SCOPE.ALL;
    form.warehouseIds = [];
  }
}
/** 切换到全部仓库时清理旧的指定仓库值，避免提交无效范围。 */
function handleScopeChange(value) {
  if (value === RENTAL_CODE.WAREHOUSE_SCOPE.ALL) form.warehouseIds = [];
}
/** 保存用户资料和角色授权。 */
function save() {
  saving.value = true;
  const action = editing.value ? updateMerchantUser : addMerchantUser;
  action(form)
    .then(() => {
      ElMessage.success(editing.value ? "保存成功" : "添加成功");
      visible.value = false;
      load();
    })
    .finally(() => {
      saving.value = false;
    });
}
/** 重置密码并强制用户下次登录初始化。 */
function resetPassword(row) {
  ElMessageBox.confirm(
    `确认将 ${row.userName} 的密码重置为初始密码吗？`,
    "操作确认",
    { type: "warning" },
  )
    .then(() => resetMerchantUserPassword(row.userId))
    .then(() => {
      ElMessage.success("密码已随机重置，请通知用户使用验证码登录并设置新密码");
      load();
    });
}
load();
</script>

<style scoped>
/* 筛选按钮必须作为表单项参与布局，保证与输入框垂直居中并保持统一间距。 */
.query-actions {
  margin-right: 0;
}

.query-actions :deep(.el-button) {
  margin-left: 0;
}

.query-actions :deep(.el-button + .el-button) {
  margin-left: 10px;
}

/* 操作列使用固定间距，避免 Element Plus 默认按钮外边距造成意外换行。 */
.account-action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-width: 292px;
  white-space: nowrap;
}

.account-action-buttons :deep(.el-button) {
  margin-left: 0;
  padding-right: 0;
  padding-left: 0;
}

@media (max-width: 768px) {
  .account-query-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .account-query-form .query-actions {
    justify-content: flex-end;
  }
}
</style>
