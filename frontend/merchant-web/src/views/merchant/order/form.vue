<template>
  <div class="app-container order-form-page">
    <el-page-header :content="form.orderId ? '编辑订单' : '新增订单'" @back="goBack" />
    <el-card class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="客户姓名" prop="customerName"><el-input v-model="form.customerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话" prop="customerPhone"><el-input v-model="form.customerPhone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="订单渠道" prop="channelId"><el-select v-model="form.channelId" class="full-width" filterable placeholder="请选择订单渠道"><el-option v-for="item in channelOptions" :key="item.channelId" :label="item.channelName" :value="item.channelId" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来源店铺"><el-input v-model="form.shopName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="外部订单号"><el-input v-model="form.externalOrderNo" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="收货地址"><el-input v-model="form.customerAddress" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="发货仓库" prop="warehouseId"><el-select v-model="form.warehouseId" class="full-width" filterable placeholder="请选择启用仓库"><el-option v-for="item in warehouseOptions" :key="item.id" :label="`${item.name}（${item.code}）`" :value="item.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="归还仓库" prop="returnWarehouseId"><el-select v-model="form.returnWarehouseId" class="full-width" filterable placeholder="请选择启用仓库"><el-option v-for="item in warehouseOptions" :key="item.id" :label="`${item.name}（${item.code}）`" :value="item.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="租赁开始" prop="leaseStartDate"><el-date-picker v-model="form.leaseStartDate" type="date" value-format="YYYY-MM-DD" class="full-width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="租赁结束" prop="leaseEndDate"><el-date-picker v-model="form.leaseEndDate" type="date" value-format="YYYY-MM-DD" class="full-width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="应发货时间"><el-date-picker v-model="form.shipDueTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="full-width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="应归还时间"><el-date-picker v-model="form.returnDueTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="full-width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="租金"><el-input-number v-model="form.rentAmount" :min="0" :precision="2" class="full-width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="押金"><el-input-number v-model="form.depositAmount" :min="0" :precision="2" class="full-width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="订单状态"><el-select v-model="form.orderStatus" class="full-width"><el-option v-for="item in orderStatuses" :key="item.value" v-bind="item" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="支付状态"><el-select v-model="form.payStatus" class="full-width"><el-option v-for="item in payStatuses" :key="item.value" v-bind="item" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="商品ID"><el-input-number v-model="form.productId" :min="1" class="full-width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="套餐ID"><el-input-number v-model="form.packageId" :min="1" class="full-width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="SN ID"><el-input-number v-model="form.snId" :min="1" class="full-width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="数量"><el-input-number v-model="form.quantity" :min="1" class="full-width" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="配件ID列表"><el-input v-model="form.accessoryItemIds" placeholder="JSON数组，例如 [1,2]" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="时效状态"><el-input v-model="form.timeEffectStatus" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="天气风险"><el-switch v-model="form.weatherRiskFlag" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="优先处理"><el-switch v-model="form.priorityFlag" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="来源原文"><el-input v-model="form.sourceTextSnapshot" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="4" /></el-form-item></el-col>
        </el-row>
        <div class="form-actions"><el-button type="primary" :loading="saving" @click="submitForm">保存订单</el-button><el-button @click="goBack">取消</el-button></div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="MerchantOrderForm">
import { addMerchantOrder, getMerchantOrder, updateMerchantOrder } from '@/api/merchant/order'
import { listMerchantOrderChannels } from '@/api/merchant/orderChannel'
import { listMerchantWarehouses } from '@/api/merchant/rentalBusiness'

const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const formRef = ref(null)
const saving = ref(false)
const channelOptions = ref([])
const warehouseOptions = ref([])
const form = reactive({ orderId: null, externalOrderNo: '', channelId: null, shopName: '', customerName: '', customerPhone: '', customerAddress: '', warehouseId: null, returnWarehouseId: null, leaseStartDate: '', leaseEndDate: '', shipDueTime: '', returnDueTime: '', rentAmount: 0, depositAmount: 0, orderStatus: 'pending_ship', payStatus: 'unpaid', timeEffectStatus: '', weatherRiskFlag: false, priorityFlag: false, sourceTextSnapshot: '', shareSnapshotJson: '', sourceOrderId: null, version: 0, productId: null, packageId: null, snId: null, accessoryItemIds: '', quantity: 1, remark: '' })
const orderStatuses = [{ value: 'pending_ship', label: '待发货' }, { value: 'shipped', label: '已发货' }, { value: 'renting', label: '租用中' }, { value: 'returning', label: '归还中' }, { value: 'completed', label: '已完成' }]
const payStatuses = [{ value: 'unpaid', label: '未支付' }, { value: 'paid', label: '已支付' }, { value: 'refunded', label: '已退款' }]
const rules = { customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }], customerPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }], channelId: [{ required: true, message: '请选择订单渠道', trigger: 'change' }], warehouseId: [{ required: true, message: '请选择发货仓库', trigger: 'change' }], returnWarehouseId: [{ required: true, message: '请选择归还仓库', trigger: 'change' }], leaseStartDate: [{ required: true, message: '请选择租赁开始日期', trigger: 'change' }], leaseEndDate: [{ required: true, message: '请选择租赁结束日期', trigger: 'change' }] }

/** 加载当前商户启用的订单渠道和仓库，选项均由后端商户上下文过滤。 */
function loadSelectOptions() {
  Promise.all([
    listMerchantOrderChannels({ status: 0, pageNum: 1, pageSize: 1000 }),
    listMerchantWarehouses({ status: 0, pageNum: 1, pageSize: 1000 })
  ]).then(([channelResponse, warehouseResponse]) => {
    channelOptions.value = channelResponse.rows || channelResponse.data?.rows || []
    warehouseOptions.value = warehouseResponse.rows || warehouseResponse.data?.rows || []
  })
}

/** 返回订单列表页面，保留统一的商户端页面导航体验。 */
function goBack() { router.push('/merchant/order') }

/** 校验订单表单并按路由模式调用新增或编辑接口。 */
function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    saving.value = true
    const request = form.orderId ? updateMerchantOrder(form) : addMerchantOrder(form)
    request.then(() => { proxy.$modal.msgSuccess(form.orderId ? '订单修改成功' : '订单创建成功'); goBack() }).finally(() => { saving.value = false })
  })
}

/** 编辑模式加载服务端订单详情，确保表单显示数据库真实数据。 */
function loadOrder() {
  if (!route.query.orderId) return
  form.orderId = Number(route.query.orderId)
  getMerchantOrder(form.orderId).then(response => {
    // 统一兼容 AjaxResult 的 data 包装，避免详情对象被错误当成响应根节点。
    const detail = response.data || response
    Object.assign(form, detail)
    form.weatherRiskFlag = Boolean(detail.weatherRiskFlag)
    form.priorityFlag = Boolean(detail.priorityFlag)
    form.quantity = detail.quantity || 1
  })
}

loadSelectOptions()
loadOrder()
</script>

<style scoped>
.order-form-page { max-width: 960px; }
.form-card { margin-top: 18px; }
.full-width { width: 100%; }
.form-actions { display: flex; gap: 12px; justify-content: center; margin-top: 12px; }
</style>
