<template>
   <div class="app-container merchant-list-page">
      <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true" label-width="68px">
         <el-form-item label="角色名称" prop="roleName">
            <el-input
               v-model="queryParams.roleName"
               placeholder="请输入角色名称"
               clearable
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="权限字符" prop="roleKey">
            <el-input
               v-model="queryParams.roleKey"
               placeholder="请输入权限字符"
               clearable
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select
               v-model="queryParams.status"
               placeholder="角色状态"
               clearable
               style="width: 240px"
            >
               <el-option
                  v-for="dict in sys_normal_disable"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="创建时间" style="width: 308px">
            <el-date-picker
               v-model="dateRange"
               value-format="YYYY-MM-DD"
               type="daterange"
               range-separator="-"
               start-placeholder="开始日期"
               end-placeholder="结束日期"
            ></el-date-picker>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>
      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button
               type="primary"
               plain
               icon="Plus"
               @click="handleAdd"
               v-hasPermi="['merchant:role:add']"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['merchant:role:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['merchant:role:remove']"
            >删除</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <!-- 表格数据 -->
      <el-table v-loading="loading" :data="roleList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="角色名称" prop="roleName" :show-overflow-tooltip="true" width="150" />
         <el-table-column label="权限字符" prop="roleKey" :show-overflow-tooltip="true" width="150" />
         <el-table-column label="显示顺序" prop="roleSort" width="100" />
         <el-table-column label="状态" align="center" width="100">
            <template #default="scope">
               <el-switch
                  v-model="scope.row.status"
                  :active-value="NORMAL_DISABLE_CODE.NORMAL"
                  :inactive-value="NORMAL_DISABLE_CODE.DISABLED"
                  @change="handleStatusChange(scope.row)"
               ></el-switch>
            </template>
         </el-table-column>
         <el-table-column label="创建时间" align="center" prop="createTime">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="210" fixed="right" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-tooltip content="详情" placement="top">
                <el-button link type="primary" icon="View" @click="openDetail(scope.row)" v-hasPermi="['merchant:role:query']"></el-button>
              </el-tooltip>
              <el-tooltip content="修改" placement="top" v-if="scope.row.roleKey !== 'merchant_admin'">
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['merchant:role:edit']"></el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top" v-if="scope.row.roleKey !== 'merchant_admin'">
                <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['merchant:role:remove']"></el-button>
              </el-tooltip>
              <el-tooltip content="分配用户" placement="top" v-if="scope.row.roleKey !== 'merchant_admin'">
                <el-button link type="primary" icon="User" @click="handleAuthUser(scope.row)" v-hasPermi="['merchant:role:edit']"></el-button>
              </el-tooltip>
            </template>
         </el-table-column>
      </el-table>

      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />

      <el-dialog v-model="detailVisible" title="角色详情" width="620px" append-to-body>
         <el-descriptions v-if="detailRole" :column="2" border label-width="120">
            <el-descriptions-item label="角色名称">{{ detailRole.roleName || "-" }}</el-descriptions-item>
            <el-descriptions-item label="权限字符">{{ detailRole.roleKey || "-" }}</el-descriptions-item>
            <el-descriptions-item label="显示顺序">{{ detailRole.roleSort ?? "-" }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ Number(detailRole.status) === NORMAL_DISABLE_CODE.NORMAL ? "正常" : "停用" }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ parseTime(detailRole.createTime) || "-" }}</el-descriptions-item>
            <el-descriptions-item label="菜单权限" :span="2">{{ detailMenuNames.join("、") || "未分配菜单权限" }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detailRole.remark || "-" }}</el-descriptions-item>
         </el-descriptions>
         <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
      </el-dialog>

      <!-- 添加或修改角色配置对话框 -->
      <el-dialog :title="title" v-model="open" width="500px" append-to-body>
         <el-form ref="roleRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="角色名称" prop="roleName">
               <el-input v-model="form.roleName" placeholder="请输入角色名称" />
            </el-form-item>
            <el-form-item prop="roleKey">
               <template #label>
                  <span>
                     <el-tooltip content="控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasRole('admin')`)" placement="top">
                        <el-icon><question-filled /></el-icon>
                     </el-tooltip>
                     权限字符
                  </span>
               </template>
               <el-input v-model="form.roleKey" placeholder="请输入权限字符" />
            </el-form-item>
            <el-form-item label="角色顺序" prop="roleSort">
               <el-input-number v-model="form.roleSort" controls-position="right" :min="0" />
            </el-form-item>
            <el-form-item label="状态">
               <el-switch v-model="form.status" :active-value="NORMAL_DISABLE_CODE.NORMAL" :inactive-value="NORMAL_DISABLE_CODE.DISABLED" active-text="正常" inactive-text="停用" inline-prompt />
            </el-form-item>
            <el-form-item label="菜单权限">
               <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event, 'menu')">展开/折叠</el-checkbox>
               <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event, 'menu')">全选/全不选</el-checkbox>
               <el-checkbox v-model="form.menuCheckStrictly" :true-value="YES_NO_CODE.YES" :false-value="YES_NO_CODE.NO">父子联动</el-checkbox>
               <el-tree
                  class="tree-border"
                  :data="menuOptions"
                  show-checkbox
                  ref="menuRef"
                  node-key="id"
                  :check-strictly="form.menuCheckStrictly !== YES_NO_CODE.YES"
                  empty-text="加载中，请稍候"
                  :props="{ label: 'label', children: 'children' }"
               ></el-tree>
            </el-form-item>
            <el-form-item label="备注">
               <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
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

<script setup name="Role">
import { addRole, changeRoleStatus, delRole, getRole, listRole, updateRole } from "@/api/merchant/role"
import { roleMenuTreeselect, treeselect as menuTreeselect } from "@/api/merchant/menu"
import { NORMAL_DISABLE_CODE, NORMAL_DISABLE_OPTIONS, YES_NO_CODE } from "@/constants/options"

const router = useRouter()
const { proxy } = getCurrentInstance()
const sys_normal_disable = NORMAL_DISABLE_OPTIONS

const roleList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const menuOptions = ref([])
const menuExpand = ref(false)
const menuNodeAll = ref(false)
const menuRef = ref(null)
const detailVisible = ref(false)
const detailRole = ref(null)
const detailMenuNames = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    roleName: undefined,
    roleKey: undefined,
    status: undefined
  },
  rules: {
    roleName: [{ required: true, message: "角色名称不能为空", trigger: "blur" }],
    roleKey: [{ required: true, message: "权限字符不能为空", trigger: "blur" }],
    roleSort: [{ required: true, message: "角色顺序不能为空", trigger: "blur" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

/** 查询角色列表 */
function getList() {
  loading.value = true
  listRole(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    roleList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 删除按钮操作 */
function handleDelete(row) {
  const roleIds = row.roleId || ids.value
  const roleName = row.roleName ? `“${row.roleName}”` : "所选角色"
  proxy.$modal.confirm(`是否确认删除${roleName}？`).then(function () {
    return delRole(roleIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 读取最新角色资料及其菜单名称，不向页面展示角色或菜单编号。 */
async function openDetail(row) {
  const [roleResponse, menuResponse] = await Promise.all([
    getRole(row.roleId),
    getRoleMenuTreeselect(row.roleId),
  ])
  detailRole.value = roleResponse.data || roleResponse
  const checkedKeys = new Set((menuResponse.checkedKeys || []).map(String))
  detailMenuNames.value = collectCheckedMenuNames(menuResponse.menus || [], checkedKeys)
  detailVisible.value = true
}

function collectCheckedMenuNames(nodes, checkedKeys) {
  return (nodes || []).reduce((names, node) => {
    if (checkedKeys.has(String(node.id))) names.push(node.label || node.menuName)
    return names.concat(collectCheckedMenuNames(node.children || [], checkedKeys))
  }, [])
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.roleId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 角色状态修改 */
function handleStatusChange(row) {
  let text = row.status === NORMAL_DISABLE_CODE.NORMAL ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + text + '""' + row.roleName + '"角色吗?').then(function () {
    return changeRoleStatus(row.roleId, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(function () {
    row.status = row.status === NORMAL_DISABLE_CODE.NORMAL ? NORMAL_DISABLE_CODE.DISABLED : NORMAL_DISABLE_CODE.NORMAL
  })
}

/** 分配用户 */
function handleAuthUser(row) {
  router.push("/merchant/role-auth/user/" + row.roleId)
}

/** 查询菜单树结构 */
function getMenuTreeselect() {
  menuTreeselect().then(response => {
    menuOptions.value = response.data
  })
}

/** 重置新增的表单以及其他数据  */
function reset() {
  if (menuRef.value != undefined) {
    menuRef.value.setCheckedKeys([])
  }
  menuExpand.value = false
  menuNodeAll.value = false
  form.value = {
    roleId: undefined,
    roleName: undefined,
    roleKey: undefined,
    roleSort: 0,
    status: NORMAL_DISABLE_CODE.NORMAL,
    menuIds: [],
    menuCheckStrictly: YES_NO_CODE.YES,
    remark: undefined
  }
  proxy.resetForm("roleRef")
}

/** 添加角色 */
function handleAdd() {
  reset()
  getMenuTreeselect()
  open.value = true
  title.value = "添加角色"
}

/** 修改角色 */
function handleUpdate(row) {
  reset()
  const roleId = row.roleId || ids.value
  const roleMenu = getRoleMenuTreeselect(roleId)
  getRole(roleId).then(response => {
    form.value = response.data
    form.value.roleSort = Number(form.value.roleSort)
    open.value = true
    nextTick(() => {
      roleMenu.then((res) => {
        let checkedKeys = res.checkedKeys
        checkedKeys.forEach((v) => {
          nextTick(() => {
            menuRef.value.setChecked(v, true, false)
          })
        })
      })
    })
  })
  title.value = "修改角色"
}

/** 根据角色编号查询菜单树结构 */
function getRoleMenuTreeselect(roleId) {
  return roleMenuTreeselect(roleId).then(response => {
    menuOptions.value = response.menus
    return response
  })
}

/** 树权限（展开/折叠）*/
function handleCheckedTreeExpand(value) {
  let treeList = menuOptions.value
  for (let i = 0; i < treeList.length; i++) {
    menuRef.value.store.nodesMap[treeList[i].id].expanded = value
  }
}

/** 树权限（全选/全不选） */
function handleCheckedTreeNodeAll(value) {
  menuRef.value.setCheckedNodes(value ? menuOptions.value : [])
}

/** 所有菜单节点数据 */
function getMenuAllCheckedKeys() {
  // 目前被选中的菜单节点
  let checkedKeys = menuRef.value.getCheckedKeys()
  // 半选中的菜单节点
  let halfCheckedKeys = menuRef.value.getHalfCheckedKeys()
  checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys)
  return checkedKeys
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["roleRef"].validate(valid => {
    if (valid) {
      if (form.value.roleId != undefined) {
        form.value.menuIds = getMenuAllCheckedKeys()
        updateRole(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        form.value.menuIds = getMenuAllCheckedKeys()
        addRole(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

getList()
</script>
