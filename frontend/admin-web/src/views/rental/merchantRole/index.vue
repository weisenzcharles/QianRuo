<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="所属商户" prop="memberId">
        <el-select v-model="queryParams.memberId" placeholder="请选择商户" filterable clearable style="width: 260px" @change="handleQuery">
          <el-option v-for="item in memberOptions" :key="item.memberId" :label="formatMember(item)" :value="item.memberId" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="角色状态" clearable style="width: 140px">
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
        <el-button type="primary" plain icon="Plus" :disabled="!queryParams.memberId" @click="handleAdd" v-hasPermi="['admin:merchantRole:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['admin:merchantRole:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['admin:merchantRole:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="roleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="角色ID" prop="roleId" width="90" align="center" />
      <el-table-column label="角色名称" prop="roleName" min-width="160" />
      <el-table-column label="权限标识" prop="roleKey" min-width="180" />
      <el-table-column label="显示顺序" prop="roleSort" width="100" align="center" />
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['admin:merchantRole:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['admin:merchantRole:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="roleRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="角色名称" prop="roleName">
              <el-input v-model="form.roleName" placeholder="请输入角色名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限标识" prop="roleKey">
              <el-input v-model="form.roleKey" placeholder="请输入权限标识" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示顺序" prop="roleSort">
              <el-input-number v-model="form.roleSort" controls-position="right" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="菜单权限">
              <el-checkbox v-model="form.menuCheckStrictly">父子联动</el-checkbox>
              <el-tree
                class="tree-border"
                :data="menuOptions"
                show-checkbox
                ref="menuRef"
                node-key="id"
                empty-text="加载中，请稍候"
                :check-strictly="!form.menuCheckStrictly"
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

<script setup name="RentalMerchantRole">
import { listMember } from '@/api/rental/member'
import {
  addMerchantRole,
  delMerchantRole,
  getMerchantRole,
  listMerchantRole,
  merchantMenuTreeselect,
  merchantRoleMenuTreeselect,
  updateMerchantRole
} from '@/api/rental/merchantRole'

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict('sys_normal_disable')

const memberOptions = ref([])
const roleList = ref([])
const menuOptions = ref([])
const open = ref(false)
const loading = ref(false)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    memberId: undefined,
    roleName: undefined,
    status: undefined
  },
  rules: {
    roleName: [{ required: true, message: '角色名称不能为空', trigger: 'blur' }],
    roleKey: [{ required: true, message: '权限标识不能为空', trigger: 'blur' }],
    roleSort: [{ required: true, message: '显示顺序不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function buildQuery() {
  return {
    pageNum: queryParams.value.pageNum,
    pageSize: queryParams.value.pageSize,
    roleName: queryParams.value.roleName,
    status: queryParams.value.status,
    'params[memberId]': queryParams.value.memberId
  }
}

function getMembers() {
  listMember({ pageNum: 1, pageSize: 1000, status: '0' }).then(response => {
    memberOptions.value = response.rows || []
  })
}

function getList() {
  if (!queryParams.value.memberId) {
    roleList.value = []
    total.value = 0
    return
  }
  loading.value = true
  listMerchantRole(buildQuery()).then(response => {
    roleList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
  })
}

function getMenuTreeselect() {
  return merchantMenuTreeselect(queryParams.value.memberId).then(response => {
    menuOptions.value = response.data
  })
}

function getRoleMenuTreeselect(roleId) {
  return merchantRoleMenuTreeselect(queryParams.value.memberId, roleId).then(response => {
    menuOptions.value = response.menus
    return response
  })
}

function reset() {
  form.value = {
    roleId: undefined,
    roleName: undefined,
    roleKey: undefined,
    roleSort: 1,
    menuIds: [],
    menuCheckStrictly: true,
    deptCheckStrictly: true,
    status: '0',
    remark: undefined,
    params: { memberId: queryParams.value.memberId }
  }
  proxy.resetForm('roleRef')
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
  ids.value = selection.map(item => item.roleId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  getMenuTreeselect()
  open.value = true
  title.value = '新增商户角色'
}

function handleUpdate(row) {
  reset()
  const roleId = row.roleId || ids.value[0]
  const roleMenu = getRoleMenuTreeselect(roleId)
  getMerchantRole(queryParams.value.memberId, roleId).then(response => {
    form.value = { ...response.data, params: { memberId: queryParams.value.memberId } }
    open.value = true
    title.value = '修改商户角色'
    nextTick(() => {
      roleMenu.then(res => {
        const checkedKeys = res.checkedKeys || []
        checkedKeys.forEach(key => proxy.$refs.menuRef.setChecked(key, true, false))
      })
    })
  })
}

function getMenuAllCheckedKeys() {
  const checkedKeys = proxy.$refs.menuRef.getCheckedKeys()
  const halfCheckedKeys = proxy.$refs.menuRef.getHalfCheckedKeys()
  return checkedKeys.concat(halfCheckedKeys)
}

function submitForm() {
  proxy.$refs.roleRef.validate(valid => {
    if (!valid) return
    form.value.menuIds = getMenuAllCheckedKeys()
    form.value.params = { memberId: queryParams.value.memberId }
    const action = form.value.roleId ? updateMerchantRole : addMerchantRole
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

function handleDelete(row) {
  const roleIds = row.roleId || ids.value
  proxy.$modal.confirm('是否确认删除商户角色编号为"' + roleIds + '"的数据项？').then(() => {
    return delMerchantRole(queryParams.value.memberId, roleIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function formatMember(item) {
  return `${item.legalSubjectName || item.nickname || item.memberCode} / ${item.memberCode}`
}

getMembers()
</script>
