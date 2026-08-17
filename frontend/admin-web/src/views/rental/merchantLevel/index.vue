<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="openEditor()" v-hasPermi="['platform:merchantLevel:add']">新增</el-button></el-col>
      <right-toolbar :show-search="false" @queryTable="loadList" />
    </el-row>
    <el-table :data="levels" v-loading="loading">
      <el-table-column label="等级名称" prop="levelName" min-width="140" />
      <el-table-column label="等级编码" prop="levelCode" min-width="140" />
      <el-table-column label="月完成订单" prop="monthlyOrderRequirement" width="130" />
      <el-table-column label="库存要求" prop="inventoryRequirement" width="120" />
      <el-table-column label="邀请人数" prop="inviteLimit" width="110" />
      <el-table-column label="注册费用" width="120"><template #default="scope">¥ {{ Number(scope.row.registrationFee).toFixed(2) }}</template></el-table-column>
      <el-table-column label="状态" width="100"><template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="openEditor(scope.row)" v-hasPermi="['platform:merchantLevel:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="remove(scope.row)" v-hasPermi="['platform:merchantLevel:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="open" :title="form.levelId ? '修改商户等级' : '新增商户等级'" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="等级名称" prop="levelName"><el-input v-model.trim="form.levelName" maxlength="64" /></el-form-item>
        <el-form-item label="等级排序" prop="levelOrder"><el-input-number v-model="form.levelOrder" :min="0" /></el-form-item>
        <el-form-item label="月完成订单" prop="monthlyOrderRequirement"><el-input-number v-model="form.monthlyOrderRequirement" :min="0" /></el-form-item>
        <el-form-item label="库存数量要求" prop="inventoryRequirement"><el-input-number v-model="form.inventoryRequirement" :min="0" /></el-form-item>
        <el-form-item label="可邀请人数" prop="inviteLimit"><el-input-number v-model="form.inviteLimit" :min="0" /></el-form-item>
        <el-form-item label="付费注册金额" prop="registrationFee"><el-input-number v-model="form.registrationFee" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" active-value="0" inactive-value="1" active-text="启用" inactive-text="停用" inline-prompt /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" maxlength="500" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="open=false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup name="MerchantLevel">
import { addMerchantLevel, delMerchantLevel, listMerchantLevel, updateMerchantLevel } from '@/api/rental/merchantLevel'
const { proxy } = getCurrentInstance()
const levels = ref([])
const loading = ref(false)
const saving = ref(false)
const open = ref(false)
const formRef = ref()
const form = reactive(defaultForm())
const rules = { levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }] }
/** 创建等级表单默认值。 */
function defaultForm() {
  return { levelId: undefined, levelName: '', levelOrder: 0, monthlyOrderRequirement: 0, inventoryRequirement: 0, inviteLimit: 0, registrationFee: 0, status: '0', remark: '' }
}
/** 加载等级列表。 */
async function loadList() {
  loading.value = true
  try {
    levels.value = (await listMerchantLevel()).data || []
  } finally {
    loading.value = false
  }
}
/** 打开新增或修改弹窗。 */
function openEditor(row) {
  Object.assign(form, defaultForm(), row || {})
  open.value = true
}
/** 校验并保存等级。 */
async function save() {
  if (!await formRef.value.validate().catch(() => false)) return
  saving.value = true
  try {
    await (form.levelId ? updateMerchantLevel(form) : addMerchantLevel(form))
    ElMessage.success('保存成功')
    open.value = false
    loadList()
  } finally {
    saving.value = false
  }
}
/** 确认后软删除未使用等级。 */
async function remove(row) {
  await proxy.$modal.confirm(`确认删除等级“${row.levelName}”吗？`)
  await delMerchantLevel(row.levelId)
  ElMessage.success('删除成功')
  loadList()
}
onMounted(loadList)
</script>
