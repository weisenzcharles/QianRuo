<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="所属商户" prop="memberId">
        <el-select v-model="queryParams.memberId" placeholder="全部商户" filterable clearable style="width: 260px" @change="handleQuery">
          <el-option v-for="item in memberOptions" :key="item.memberId" :label="formatMember(item)" :value="item.memberId" />
        </el-select>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="用户昵称" prop="nickName">
        <el-input v-model="queryParams.nickName" placeholder="请输入昵称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 140px">
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" :disabled="!queryParams.memberId" @click="handleAdd" v-hasPermi="['admin:merchantUser:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['admin:merchantUser:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['admin:merchantUser:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="用户ID" prop="merchantUserId" width="100" align="center" />
      <el-table-column label="所属商户" min-width="200">
        <template #default="scope">
          <div>{{ scope.row.legalSubjectName || scope.row.memberNickname || '-' }}</div>
          <small class="muted">商户ID：{{ scope.row.memberId }}</small>
        </template>
      </el-table-column>
      <el-table-column label="登录账号" prop="userName" min-width="140" />
      <el-table-column label="用户昵称" prop="nickName" min-width="140" />
      <el-table-column label="手机号" prop="phone" min-width="130" />
      <el-table-column label="邮箱" prop="email" min-width="180" show-overflow-tooltip />
      <el-table-column label="仓库权限" align="center" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.warehouseScopeType === 'all'" type="success">全部仓库</el-tag>
          <el-tag v-else type="warning">指定仓库</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['admin:merchantUser:edit']"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['admin:merchantUser:edit']">修改</el-button>
          <el-button link type="primary" icon="Key" @click="handleResetPwd(scope.row)" v-hasPermi="['admin:merchantUser:resetPwd']">重置密码</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['admin:merchantUser:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="userRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="登录账号" prop="userName">
              <el-input v-model="form.userName" placeholder="默认同手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户昵称" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请输入用户昵称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" active-value="0" inactive-value="1" active-text="正常" inactive-text="停用" inline-prompt />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!form.merchantUserId">
            <el-form-item label="初始密码">
              <el-input v-model="form.password" placeholder="留空则仅验证码登录" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="角色">
              <el-select v-model="form.roleIds" multiple placeholder="请选择商户角色" style="width: 100%">
                <el-option v-for="role in roleOptions" :key="role.roleId" :label="role.roleName" :value="role.roleId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="仓库权限" prop="warehouseScopeType">
              <el-radio-group v-model="form.warehouseScopeType" :disabled="form.memberRole === 'owner'">
                <el-radio value="all">全部仓库</el-radio>
                <el-radio value="specified">指定仓库</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="form.warehouseScopeType === 'specified'">
            <el-form-item label="授权仓库" prop="warehouseIds">
              <el-select v-model="form.warehouseIds" multiple filterable placeholder="请选择可访问仓库" style="width: 100%">
                <el-option
                  v-for="warehouse in warehouseOptions"
                  :key="warehouse.warehouseId"
                  :label="`${warehouse.warehouseName} / ${warehouse.warehouseCode}`"
                  :value="warehouse.warehouseId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="菜单权限">
              <span class="muted">按用户精确授权，保存时会自动补齐路由父级</span>
              <el-tree
                class="tree-border"
                :data="menuOptions"
                show-checkbox
                ref="menuRef"
                node-key="id"
                empty-text="加载中，请稍候"
                :check-strictly="true"
                :props="{ label: 'label', children: 'children' }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="RentalMerchantUser">
import { listMember } from '@/api/rental/member'
import {
  addMerchantUser,
  changeMerchantUserStatus,
  delMerchantUser,
  getMerchantUser,
  getMerchantWarehouseOptions,
  listMerchantUserRoleOptions,
  listMerchantUser,
  merchantUserMenuTreeselect,
  resetMerchantUserPwd,
  updateMerchantUser
} from '@/api/rental/merchantUser'

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict('sys_normal_disable')

const memberOptions = ref([])
const roleOptions = ref([])
const menuOptions = ref([])
const warehouseOptions = ref([])
const menuRef = ref(null)
const userList = ref([])
const open = ref(false)
const loading = ref(false)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

function validateWarehouseIds(rule, value, callback) {
  if (data.form.warehouseScopeType === 'specified' && (!value || value.length === 0)) {
    callback(new Error('请至少选择一个授权仓库'))
    return
  }
  callback()
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    memberId: undefined,
    phone: undefined,
    nickName: undefined,
    status: undefined
  },
  rules: {
    phone: [
      { required: true, message: '手机号不能为空', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ],
    nickName: [{ required: true, message: '用户昵称不能为空', trigger: 'blur' }],
    warehouseIds: [{ validator: validateWarehouseIds, trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getMembers() {
  listMember({ pageNum: 1, pageSize: 1000, status: '0' }).then(response => {
    memberOptions.value = response.rows || []
  })
}

function getRoles(memberId) {
  if (!memberId) {
    roleOptions.value = []
    return Promise.resolve()
  }
  return listMerchantUserRoleOptions(memberId).then(response => {
    roleOptions.value = response.data || []
  })
}

function getMenuTreeselect(memberId) {
  if (!memberId) {
    menuOptions.value = []
    return Promise.resolve()
  }
  return merchantUserMenuTreeselect(memberId).then(response => {
    menuOptions.value = response.data || []
  })
}

function getList() {
  loading.value = true
  listMerchantUser(queryParams.value).then(response => {
    userList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
  })
}

function reset() {
  form.value = {
    merchantUserId: undefined,
    memberId: queryParams.value.memberId,
    userName: undefined,
    nickName: undefined,
    phone: undefined,
    email: undefined,
    password: undefined,
    status: '0',
    roleIds: [],
    menuIds: [],
    memberRole: 'staff',
    warehouseScopeType: 'all',
    warehouseIds: [],
    remark: undefined
  }
  proxy.resetForm('userRef')
  menuRef.value?.setCheckedKeys([])
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.merchantUserId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  getRoles(queryParams.value.memberId)
  getMerchantWarehouseOptions(queryParams.value.memberId).then(response => {
    warehouseOptions.value = response.data || []
  })
  getMenuTreeselect(queryParams.value.memberId).then(() => {
    nextTick(() => menuRef.value?.setCheckedKeys([]))
  })
  open.value = true
  title.value = '新增商户用户'
}

function handleUpdate(row) {
  reset()
  const merchantUserId = row.merchantUserId || ids.value[0]
  getMerchantUser(merchantUserId).then(response => {
    form.value = response.data
    form.value.roleIds = response.roleIds || []
    form.value.menuIds = response.checkedKeys || []
    roleOptions.value = response.roles || []
    menuOptions.value = response.menus || []
    warehouseOptions.value = response.warehouses || []
    form.value.memberRole = form.value.memberRole || 'staff'
    form.value.warehouseScopeType = form.value.memberRole === 'owner' ? 'all' : (form.value.warehouseScopeType || 'all')
    form.value.warehouseIds = form.value.warehouseIds || []
    open.value = true
    title.value = '修改商户用户'
    nextTick(() => {
      const checkedKeys = response.checkedKeys || []
      menuRef.value?.setCheckedKeys([])
      menuRef.value?.setCheckedKeys(checkedKeys)
    })
  })
}

function getMenuAllCheckedKeys() {
  return menuRef.value?.getCheckedKeys() || []
}

function submitForm() {
  proxy.$refs.userRef.validate(valid => {
    if (!valid) return
    if (!form.value.userName) {
      form.value.userName = form.value.phone
    }
    form.value.menuIds = getMenuAllCheckedKeys()
    const action = form.value.merchantUserId ? updateMerchantUser : addMerchantUser
    action(form.value).then(() => {
      proxy.$modal.msgSuccess('保存成功')
      open.value = false
      getList()
    })
  })
}

function cancel() {
  open.value = false
  reset()
}

function handleStatusChange(row) {
  const text = row.status === '0' ? '启用' : '停用'
  proxy.$modal.confirm('确认要"' + text + '""' + row.userName + '"用户吗？').then(() => {
    return changeMerchantUserStatus(row.merchantUserId, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + '成功')
  }).catch(() => {
    row.status = row.status === '0' ? '1' : '0'
  })
}

function handleResetPwd(row) {
  proxy.$prompt('请输入"' + row.userName + '"的新密码', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^.{5,20}$/,
    inputErrorMessage: '密码长度必须在 5 到 20 位之间'
  }).then(({ value }) => {
    return resetMerchantUserPwd(row.merchantUserId, value)
  }).then(() => {
    proxy.$modal.msgSuccess('密码重置成功')
  }).catch(() => {})
}

function handleDelete(row) {
  const merchantUserIds = row.merchantUserId || ids.value
  proxy.$modal.confirm('是否确认删除商户用户编号为"' + merchantUserIds + '"的数据项？').then(() => {
    return delMerchantUser(merchantUserIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function formatMember(item) {
  return `${item.legalSubjectName || item.nickname || item.memberCode} / ${item.memberCode}`
}

getMembers()
getList()
</script>

<style scoped>
.muted {
  color: #909399;
}
</style>
