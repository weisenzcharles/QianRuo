<template>
  <div class="app-container">
    <el-alert title="此处配置商户端可显示菜单；商户管理员默认拥有全部启用菜单，其他商户用户由商户管理员按角色授权。" type="info" :closable="false" class="mb8" />
    <el-form :inline="true" :model="query" class="mb8">
      <el-form-item label="菜单名称"><el-input v-model="query.menuName" clearable placeholder="请输入菜单名称" @keyup.enter="load" /></el-form-item>
      <el-form-item label="显示状态"><el-select v-model="query.visible" clearable placeholder="全部"><el-option label="显示" :value="SYSTEM_CODE.MENU_VISIBILITY.VISIBLE" /><el-option label="隐藏" :value="SYSTEM_CODE.MENU_VISIBILITY.HIDDEN" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="load">查询</el-button><el-button icon="Refresh" @click="reset">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" icon="Plus" @click="openCreate">新增菜单</el-button></el-col></el-row>
    <el-table v-loading="loading" :data="treeRows" row-key="menuId" border :tree-props="{ children: 'children' }">
      <el-table-column prop="menuName" label="菜单名称" min-width="220" />
      <el-table-column prop="path" label="路由" min-width="220" />
      <el-table-column prop="menuType" label="类型" width="90" />
      <el-table-column prop="perms" label="权限标识" min-width="220" />
      <el-table-column prop="orderNum" label="排序" width="80" />
      <el-table-column label="显示" width="90"><template #default="scope"><el-tag :type="scope.row.visible === SYSTEM_CODE.MENU_VISIBILITY.VISIBLE ? 'success' : 'info'">{{ scope.row.visible === SYSTEM_CODE.MENU_VISIBILITY.VISIBLE ? '显示' : '隐藏' }}</el-tag></template></el-table-column>
      <el-table-column label="状态" width="90"><template #default="scope"><el-tag :type="scope.row.status === NORMAL_DISABLE_CODE.NORMAL ? 'success' : 'info'">{{ scope.row.status === NORMAL_DISABLE_CODE.NORMAL ? '启用' : '停用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="150" fixed="right"><template #default="scope"><el-button link type="primary" icon="Edit" @click="openEdit(scope.row)">修改</el-button><el-button link type="danger" icon="Delete" @click="remove(scope.row)">删除</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editing ? '修改商户菜单' : '新增商户菜单'" width="650px" append-to-body>
      <el-form :model="form" label-width="100px">
        <el-form-item label="菜单名称" required><el-input v-model="form.menuName" maxlength="50" /></el-form-item>
        <el-form-item label="父级菜单"><el-tree-select v-model="form.parentId" :data="treeRows" node-key="menuId" :props="{ label: 'menuName', children: 'children' }" check-strictly clearable /></el-form-item>
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="路由"><el-input v-model="form.path" /></el-form-item></el-col><el-col :span="12"><el-form-item label="组件"><el-input v-model="form.component" /></el-form-item></el-col><el-col :span="12"><el-form-item label="类型"><el-select v-model="form.menuType"><el-option label="目录" value="M" /><el-option label="菜单" value="C" /><el-option label="按钮" value="F" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="排序"><el-input-number v-model="form.orderNum" :min="0" /></el-form-item></el-col><el-col :span="12"><el-form-item label="权限标识"><el-input v-model="form.perms" placeholder="merchant:module:action" /></el-form-item></el-col><el-col :span="12"><el-form-item label="图标"><el-input v-model="form.icon" /></el-form-item></el-col><el-col :span="12"><el-form-item label="显示"><el-radio-group v-model="form.visible"><el-radio :value="SYSTEM_CODE.MENU_VISIBILITY.VISIBLE">显示</el-radio><el-radio :value="SYSTEM_CODE.MENU_VISIBILITY.HIDDEN">隐藏</el-radio></el-radio-group></el-form-item></el-col><el-col :span="12"><el-form-item label="状态"><el-switch v-model="form.status" :active-value="NORMAL_DISABLE_CODE.NORMAL" :inactive-value="NORMAL_DISABLE_CODE.DISABLED" active-text="启用" inactive-text="停用" inline-prompt /></el-form-item></el-col></el-row>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="RentalMerchantMenu">
import { ElMessage, ElMessageBox } from 'element-plus'
import { addMerchantMenu, deleteMerchantMenu, listMerchantMenu, updateMerchantMenu } from '@/api/rental/merchantMenu'
import { NORMAL_DISABLE_CODE, SYSTEM_CODE } from '@/utils/stateCode'

const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(false)
const rows = ref([])
const query = reactive({ menuName: '', visible: undefined })
const form = reactive({ menuId: undefined, parentId: 0, menuName: '', orderNum: 0, path: '', component: '', menuType: 'C', perms: '', icon: '#', visible: SYSTEM_CODE.MENU_VISIBILITY.VISIBLE, status: NORMAL_DISABLE_CODE.NORMAL, isFrame: SYSTEM_CODE.MENU_FRAME.INTERNAL, isCache: SYSTEM_CODE.MENU_CACHE.CACHE })
const treeRows = computed(() => buildTree(rows.value))

/** 将平台菜单平铺结果组装为父子树，供表格和父级选择器复用。 */
function buildTree(list) {
  const map = new Map(list.map(item => [item.menuId, { ...item, children: [] }]))
  const roots = []
  map.forEach(item => { const parent = map.get(item.parentId); parent ? parent.children.push(item) : roots.push(item) })
  return roots
}

/** 加载平台配置的商户端菜单。 */
function load() { loading.value = true; listMerchantMenu(query).then(response => { rows.value = response.data || response.rows || [] }).finally(() => { loading.value = false }) }

/** 清空菜单查询条件。 */
function reset() { query.menuName = ''; query.visible = undefined; load() }

/** 打开新增菜单表单。 */
function openCreate() { editing.value = false; Object.assign(form, { menuId: undefined, parentId: 0, menuName: '', orderNum: 0, path: '', component: '', menuType: 'C', perms: '', icon: '#', visible: SYSTEM_CODE.MENU_VISIBILITY.VISIBLE, status: NORMAL_DISABLE_CODE.NORMAL, isFrame: SYSTEM_CODE.MENU_FRAME.INTERNAL, isCache: SYSTEM_CODE.MENU_CACHE.CACHE }); dialogVisible.value = true }

/** 打开菜单编辑表单。 */
function openEdit(row) { editing.value = true; Object.assign(form, row, { visible: Number(row.visible), status: Number(row.status), isFrame: Number(row.isFrame), isCache: Number(row.isCache) }); dialogVisible.value = true }

/** 保存菜单配置。 */
function save() { if (!form.menuName.trim()) return ElMessage.warning('请输入菜单名称'); const action = editing.value ? updateMerchantMenu : addMerchantMenu; action({ ...form }).then(() => { ElMessage.success('保存成功'); dialogVisible.value = false; load() }) }

/** 删除菜单模板。 */
function remove(row) { ElMessageBox.confirm(`确认删除“${row.menuName}”吗？`, '操作确认', { type: 'warning' }).then(() => deleteMerchantMenu(row.menuId)).then(() => { ElMessage.success('删除成功'); load() }) }

load()
</script>
