<template>
  <div class="app-container merchant-list-page">
    <el-form v-show="showSearch" :model="query" inline @submit.prevent>
      <el-form-item label="用户名"><el-input v-model="query.userName" clearable @keyup.enter="load" /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="query.phone" clearable @keyup.enter="load" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="query.status" clearable><el-option label="正常" value="0" /><el-option label="停用" value="1" /></el-select></el-form-item>
      <el-button type="primary" icon="Search" @click="load">查询</el-button>
      <el-button icon="Refresh" @click="reset">重置</el-button>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" icon="Plus" @click="openAdd">添加用户</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="load" />
    </el-row>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="userName" label="用户名" min-width="130" />
      <el-table-column prop="nickName" label="昵称" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column prop="roleNames" label="角色" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="90"><template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag></template></el-table-column>
      <el-table-column label="初始化密码" width="110"><template #default="scope">{{ scope.row.mustChangePassword ? '待初始化' : '已设置' }}</template></el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" fixed="right" width="190">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="openEdit(scope.row)">编辑权限</el-button>
          <el-button link type="warning" icon="Refresh" @click="resetPassword(scope.row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />
    <el-dialog v-model="visible" :title="editing ? '编辑商户用户' : '添加商户用户'" width="560px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名"><el-input v-model="form.userName" :disabled="editing" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item v-if="!editing" label="初始密码"><el-input v-model="form.password" type="password" show-password placeholder="默认 123456" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="角色"><el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%"><el-option v-for="role in roles" :key="role.roleId" :label="role.roleName" :value="role.roleId" /></el-select></el-form-item>
        <el-alert title="角色对应菜单权限，用户首次登录后需要初始化密码。" type="info" :closable="false" />
      </el-form>
      <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { addMerchantUser, getMerchantUser, listMerchantUsers, resetMerchantUserPassword, updateMerchantUser } from '@/api/merchant/user'
import { listRole } from '@/api/merchant/role'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false); const saving = ref(false); const visible = ref(false)
const rows = ref([]); const total = ref(0); const roles = ref([])
const showSearch = ref(true)
const editing = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, userName: undefined, phone: undefined, status: undefined })
const form = reactive({})

/** 加载当前商户用户和角色列表。 */
function load() { loading.value = true; Promise.all([listMerchantUsers(query), listRole({ pageNum: 1, pageSize: 100 })]).then(([users, roleResult]) => { rows.value = users.rows || []; total.value = users.total || 0; roles.value = roleResult.rows || [] }).finally(() => { loading.value = false }) }
/** 清空筛选条件。 */
function reset() { query.pageNum = 1; query.userName = undefined; query.phone = undefined; query.status = undefined; load() }
/** 打开新增用户表单并设置默认状态。 */
function openAdd() { Object.assign(form, { userId: undefined, userName: '', nickName: '', phone: '', email: '', password: '', status: '0', roleIds: [] }); editing.value = false; visible.value = true }
/** 加载用户详情并打开权限编辑窗口。 */
function openEdit(row) { getMerchantUser(row.userId).then(response => { Object.assign(form, response.data || response); form.roleIds = form.roleIds || []; editing.value = true; visible.value = true }) }
/** 保存用户资料和角色授权。 */
function save() { saving.value = true; const action = editing.value ? updateMerchantUser : addMerchantUser; action(form).then(() => { ElMessage.success(editing.value ? '保存成功' : '添加成功'); visible.value = false; load() }).finally(() => { saving.value = false }) }
/** 重置密码并强制用户下次登录初始化。 */
function resetPassword(row) { ElMessageBox.confirm(`确认将 ${row.userName} 的密码重置为初始密码吗？`, '操作确认', { type: 'warning' }).then(() => resetMerchantUserPassword(row.userId)).then(() => { ElMessage.success('密码已重置'); load() }) }
load()
</script>
