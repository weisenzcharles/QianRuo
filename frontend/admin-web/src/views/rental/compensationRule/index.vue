<template>
  <div class="app-container">
    <el-alert title="调解方案的赔付金额不能超过匹配规则的单笔上限；类型专属规则优先于通用规则。" type="info" :closable="false" class="mb8" />
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button v-hasPermi="['platform:compensationRule:add']" type="primary" plain icon="Plus" @click="openAdd">新增</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="load" />
    </el-row>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="ruleName" label="规则名称" min-width="180" />
      <el-table-column label="纠纷类型" width="150" align="center"><template #default="scope">{{ disputeTypeText(scope.row.disputeType) }}</template></el-table-column>
      <el-table-column label="赔付上限" width="150" align="right"><template #default="scope">¥{{ Number(scope.row.maxAmount || 0).toFixed(2) }}</template></el-table-column>
      <el-table-column prop="priority" label="优先级" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center"><template #default="scope"><el-switch v-model="scope.row.status" :active-value="COMPENSATION_RULE_STATUS.ENABLED" :inactive-value="COMPENSATION_RULE_STATUS.DISABLED" @change="toggle(scope.row)" /></template></el-table-column>
      <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right" align="center"><template #default="scope"><el-button v-hasPermi="['platform:compensationRule:edit']" link type="primary" icon="Edit" @click="openEdit(scope.row)">修改</el-button><el-button v-hasPermi="['platform:compensationRule:remove']" link type="danger" icon="Delete" @click="remove(scope.row)">删除</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="visible" :title="form.ruleId ? '修改赔付规则' : '新增赔付规则'" width="620px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="规则名称" prop="ruleName"><el-input v-model="form.ruleName" maxlength="128" show-word-limit /></el-form-item>
        <el-form-item label="纠纷类型" prop="disputeType"><el-select v-model="form.disputeType" style="width: 100%"><el-option v-for="item in disputeTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="赔付上限" prop="maxAmount"><el-input-number v-model="form.maxAmount" :min="0" :precision="2" :controls="false" style="width: 100%" /></el-form-item>
        <el-form-item label="优先级"><el-input-number v-model="form.priority" :min="1" :max="9999" controls-position="right" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" maxlength="500" show-word-limit :rows="3" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="COMPENSATION_RULE_STATUS.ENABLED" :inactive-value="COMPENSATION_RULE_STATUS.DISABLED" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="PlatformCompensationRule">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCompensationRules, addCompensationRule, updateCompensationRule, deleteCompensationRule } from '@/api/rental/compensationRule'
import { ALLIANCE_DISPUTE_TYPE, COMPENSATION_RULE_STATUS } from '@/utils/rental/alliance'

const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const showSearch = ref(true)
const rows = ref([])
const formRef = ref()
const disputeTypes = [
  { value: ALLIANCE_DISPUTE_TYPE.GENERAL, label: '通用' },
  { value: ALLIANCE_DISPUTE_TYPE.DAMAGE, label: '损坏' },
  { value: ALLIANCE_DISPUTE_TYPE.LOSS, label: '丢失' },
  { value: ALLIANCE_DISPUTE_TYPE.DELAYED_FULFILLMENT, label: '延迟履约' },
  { value: ALLIANCE_DISPUTE_TYPE.FEE, label: '费用' },
  { value: ALLIANCE_DISPUTE_TYPE.OTHER, label: '其他' },
]
const form = reactive({ ruleId: null, ruleName: '', disputeType: ALLIANCE_DISPUTE_TYPE.GENERAL, maxAmount: 0, priority: 100, status: COMPENSATION_RULE_STATUS.ENABLED, remark: '' })
const rules = { ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }], disputeType: [{ required: true, message: '请选择纠纷类型', trigger: 'change' }], maxAmount: [{ required: true, message: '请输入赔付上限', trigger: 'change' }] }
function disputeTypeText(value) { return disputeTypes.find(item => item.value === Number(value))?.label || '未知' }
function load() { loading.value = true; listCompensationRules().then(response => { rows.value = response.rows || response.data || [] }).finally(() => { loading.value = false }) }
function resetForm(row = null) { Object.assign(form, row ? { ...row } : { ruleId: null, ruleName: '', disputeType: ALLIANCE_DISPUTE_TYPE.GENERAL, maxAmount: 0, priority: 100, status: COMPENSATION_RULE_STATUS.ENABLED, remark: '' }) }
function openAdd() { resetForm(); visible.value = true }
function openEdit(row) { resetForm(row); visible.value = true }
function save() { formRef.value.validate(valid => { if (!valid) return; saving.value = true; const action = form.ruleId ? updateCompensationRule : addCompensationRule; action({ ...form }).then(() => { ElMessage.success('保存成功'); visible.value = false; load() }).finally(() => { saving.value = false }) }) }
function toggle(row) { updateCompensationRule({ ...row }).catch(() => { row.status = Number(row.status) === COMPENSATION_RULE_STATUS.ENABLED ? COMPENSATION_RULE_STATUS.DISABLED : COMPENSATION_RULE_STATUS.ENABLED }) }
function remove(row) { ElMessageBox.confirm(`确认删除规则“${row.ruleName}”吗？`, '提示', { type: 'warning' }).then(() => deleteCompensationRule(row.ruleId)).then(() => { ElMessage.success('删除成功'); load() }) }
load()
</script>
