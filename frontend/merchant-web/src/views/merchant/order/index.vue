<template>
  <div class="app-container merchant-order-page">
    <header class="page-heading">
      <div>
        <p class="page-kicker">ORDER CENTER</p>
        <h2>订单中心</h2>
        <p class="page-description">集中处理录单、备货、发货、归还和售后节点，订单信息按实际工作流展开。</p>
      </div>
      <div class="heading-actions">
        <el-tag type="info" effect="plain">今日更新 {{ lastUpdated }}</el-tag>
        <el-button type="primary" :icon="Plus" @click="openCreateOrder">新建订单</el-button>
      </div>
    </header>

    <section class="summary-strip" aria-label="订单概览">
      <button
        v-for="item in summaryItems"
        :key="item.value"
        type="button"
        :class="['summary-item', { 'is-active': queryParams.status === item.value }]"
        @click="selectStatus(item.value)"
      >
        <span class="summary-label">{{ item.label }}</span>
        <strong>{{ item.count }}</strong>
        <span :class="['summary-trend', item.trendType]">{{ item.trend }}</span>
      </button>
    </section>

    <section v-show="showSearch" class="filter-panel" aria-label="订单筛选">
      <el-form v-show="showSearch" :model="queryParams" :inline="true" @submit.prevent>
        <el-form-item label="订单搜索">
          <el-input
            v-model="queryParams.keyword"
            clearable
            placeholder="订单号、客户、商品或快递单号"
            class="keyword-input"
            @keyup.enter="handleQuery"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="queryParams.status" clearable placeholder="全部状态" class="filter-select">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单渠道">
          <el-select v-model="queryParams.channelId" clearable placeholder="全部渠道" class="filter-select">
            <el-option v-for="item in channelOptions" :key="item.channelId" :label="item.channelName" :value="item.channelId" />
          </el-select>
        </el-form-item>
        <el-form-item label="发货日期">
          <el-date-picker
            v-model="queryParams.shipDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            class="date-input"
          />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <el-row :gutter="10" class="mb8 order-query-toolbar">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadOrders" />
    </el-row>

    <section class="order-list-shell" aria-label="订单列表">
      <div class="list-toolbar">
        <div>
          <h3>订单列表</h3>
          <span>共 {{ filteredOrders.length }} 条订单</span>
        </div>
        <div class="list-toolbar-actions">
          <el-checkbox v-model="showOnlyAttention">仅看待处理</el-checkbox>
        </div>
      </div>

      <el-empty v-if="filteredOrders.length === 0" description="没有符合条件的订单" :image-size="96" />
      <div v-else class="order-scroll-area">
        <div class="order-grid order-grid-header">
          <div>订单信息</div>
          <div>客户信息</div>
          <div>发货安排</div>
          <div>气象雷达</div>
          <div>配送方案</div>
          <div>订单状态</div>
          <div>商品清单</div>
        </div>

        <article v-for="order in filteredOrders" :key="order.orderNo" class="order-card">
          <div class="order-grid order-grid-content">
            <section class="order-cell order-overview">
              <div class="mobile-cell-title">订单信息</div>
              <div class="product-summary">
                <div class="product-thumb" aria-hidden="true"><Camera /></div>
                <div>
                  <strong>{{ order.model }}</strong>
                  <span>{{ order.packageName }}</span>
                </div>
              </div>
              <dl class="detail-list">
                <div><dt>订单号</dt><dd>{{ order.orderNo }}</dd></div>
                <div><dt>租期</dt><dd>{{ order.rentalPeriod }}</dd></div>
                <div><dt>归还</dt><dd>{{ order.returnDate }}</dd></div>
                <div><dt>租金</dt><dd class="money">¥{{ order.rent }}</dd></div>
                <div><dt>渠道</dt><dd>{{ order.channel }} <em>商户：{{ order.merchant }}</em></dd></div>
              </dl>
            </section>

            <section class="order-cell">
              <div class="mobile-cell-title">客户信息</div>
              <div class="customer-name"><User /> {{ order.customer.nickname }}</div>
              <dl class="detail-list">
                <div><dt>收件人</dt><dd>{{ order.customer.name }}　{{ order.customer.phone }}</dd></div>
                <div><dt>地址</dt><dd>{{ order.customer.address }}</dd></div>
                <div><dt>备注</dt><dd>{{ order.customer.note }}</dd></div>
              </dl>
            </section>

            <section class="order-cell">
              <div class="mobile-cell-title">发货安排</div>
              <dl class="detail-list schedule-list">
                <div><dt>发货仓</dt><dd>{{ order.shipping.warehouse }}</dd></div>
                <div><dt>发货日</dt><dd>{{ order.shipping.shipDate }}</dd></div>
                <div><dt>方式</dt><dd>{{ order.shipping.method }}</dd></div>
                <div><dt>上门</dt><dd>{{ order.shipping.doorTime }}</dd></div>
              </dl>
              <p class="cell-note"><InfoFilled /> {{ order.shipping.note }}</p>
            </section>

            <section class="order-cell weather-cell">
              <div class="mobile-cell-title">气象雷达</div>
              <div class="route-line"><Location /> {{ order.weather.route }}</div>
              <div class="weather-line"><span>发</span><i :class="order.weather.departIcon">{{ order.weather.departIcon === 'sunny' ? '☀' : '☁' }}</i>{{ order.weather.depart }}</div>
              <div class="weather-line"><span>收</span><i :class="order.weather.arriveIcon">{{ order.weather.arriveIcon === 'rain' ? '☔' : '☁' }}</i>{{ order.weather.arrive }}</div>
              <el-tag size="small" :type="order.weather.risk ? 'warning' : 'success'" effect="plain">
                {{ order.weather.risk ? '存在天气提醒' : '天气正常' }}
              </el-tag>
            </section>

            <section class="order-cell delivery-cell">
              <div class="mobile-cell-title">配送方案</div>
              <div v-for="plan in order.delivery.plans" :key="plan.name" class="delivery-plan">
                <span class="carrier-mark">{{ plan.carrier }}</span>
                <span>{{ plan.name }}</span>
                <b>¥{{ plan.price }}</b>
              </div>
              <el-button link type="primary" :icon="Calendar" @click="handleAction('立即预约', order)">立即预约</el-button>
              <div class="delivery-after">预计到库：{{ order.delivery.expectedDate }}</div>
            </section>

            <section class="order-cell status-cell">
              <div class="mobile-cell-title">订单状态</div>
              <el-tag :type="order.statusType" effect="light" class="status-tag">{{ order.status }}</el-tag>
              <dl class="detail-list">
                <div><dt>发货单号</dt><dd>{{ order.trackingNo }}</dd></div>
                <div><dt>归还单号</dt><dd>{{ order.returnNo }}</dd></div>
                <div><dt>创建时间</dt><dd>{{ order.createdAt }}</dd></div>
              </dl>
            </section>

            <section class="order-cell product-list-cell">
              <div class="mobile-cell-title">商品清单</div>
              <strong>{{ order.packageName }} <el-button link type="primary" @click="handleAction('查看套餐详情', order)">详情</el-button></strong>
              <p>主品编号：{{ order.productCode }}</p>
              <p v-if="order.addOns.length" class="addon-title"><ShoppingCart /> 额外增购：</p>
              <p v-for="addon in order.addOns" :key="addon" class="addon-item">{{ addon }}</p>
            </section>
          </div>

          <footer class="order-footer">
            <div class="attention-note" v-if="order.attention">
              <Bell /> <span>批注提醒：</span>{{ order.attention }}
            </div>
            <div class="attention-note muted" v-else>当前订单暂无未读批注</div>
            <div class="order-actions">
              <el-button v-for="action in order.actions" :key="action.label" text :type="action.type || 'default'" :icon="action.icon" @click="handleAction(action.label, order)">
                {{ action.label }}
              </el-button>
              <span class="operator-meta">录入人：{{ order.operator }}　对接人：{{ order.coordinator }}</span>
            </div>
          </footer>
        </article>
      </div>
    </section>

    <el-dialog v-model="detailVisible" title="订单摘要" width="520px">
      <el-descriptions v-if="activeOrder" :column="1" border>
        <el-descriptions-item label="订单号">{{ activeOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ activeOrder.customer.name }} {{ activeOrder.customer.phone }}</el-descriptions-item>
        <el-descriptions-item label="商品">{{ activeOrder.model }} / {{ activeOrder.packageName }}</el-descriptions-item>
        <el-descriptions-item label="租期">{{ activeOrder.rentalPeriod }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">{{ activeOrder.status }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="createVisible" title="新增订单" width="720px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-row :gutter="18">
          <el-col :span="12"><el-form-item label="客户姓名" prop="customerName"><el-input v-model="createForm.customerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话" prop="customerPhone"><el-input v-model="createForm.customerPhone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="订单渠道" prop="channelId"><el-select v-model="createForm.channelId" class="full-width" filterable placeholder="请选择订单渠道"><el-option v-for="item in channelOptions" :key="item.channelId" :label="item.channelName" :value="item.channelId" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="外部订单号"><el-input v-model="createForm.externalOrderNo" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="收货地址"><el-input v-model="createForm.customerAddress" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="租赁开始" prop="leaseStartDate"><el-date-picker v-model="createForm.leaseStartDate" type="date" value-format="YYYY-MM-DD" class="full-width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="租赁结束" prop="leaseEndDate"><el-date-picker v-model="createForm.leaseEndDate" type="date" value-format="YYYY-MM-DD" class="full-width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="租金"><el-input-number v-model="createForm.rentAmount" :min="0" :precision="2" class="full-width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="押金"><el-input-number v-model="createForm.depositAmount" :min="0" :precision="2" class="full-width" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="createForm.remark" type="textarea" :rows="3" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="createVisible = false">取消</el-button><el-button type="primary" :loading="createLoading" @click="submitCreateOrder">保存订单</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="MerchantOrder">
import { Bell, Calendar, Camera, Check, CircleCheck, CopyDocument, Edit, Finished, InfoFilled, Location, Plus, Search, Refresh, ShoppingCart, Switch, User, VideoCamera, Van, Box } from '@element-plus/icons-vue'
import { addMerchantOrder, listMerchantOrders, updateMerchantOrder } from '@/api/merchant/order'
import { listMerchantOrderChannels } from '@/api/merchant/orderChannel'
import { listMerchantWarehouses } from '@/api/merchant/rentalBusiness'

const { proxy } = getCurrentInstance()
const router = useRouter()
const loading = ref(false)
const lastUpdated = ref('刚刚')
const showOnlyAttention = ref(false)
const showSearch = ref(true)
const detailVisible = ref(false)
const createVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref(null)
const activeOrder = ref(null)
const warehouseOptions = ref([])
const orders = ref([])
// 订单列表默认读取业务服务数据库，只有明确配置为 false 时才使用演示数据。
const useRemoteOrders = import.meta.env.VITE_USE_ORDER_API !== 'false'
const createForm = reactive({ customerName: '', customerPhone: '', channelId: null, externalOrderNo: '', customerAddress: '', leaseStartDate: '', leaseEndDate: '', rentAmount: 0, depositAmount: 0, remark: '' })
const editingOrderId = ref(null)
const createRules = { customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }], customerPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }], channelId: [{ required: true, message: '请选择订单渠道', trigger: 'change' }], leaseStartDate: [{ required: true, message: '请选择租赁开始日期', trigger: 'change' }], leaseEndDate: [{ required: true, message: '请选择租赁结束日期', trigger: 'change' }] }

const statusOptions = [
  { label: '待发货', value: '待发货' },
  { label: '已备货', value: '已备货' },
  { label: '已发货', value: '已发货' },
  { label: '租用中', value: '租用中' },
  { label: '归还中', value: '归还中' },
  { label: '已归还', value: '已归还' },
  { label: '售后中', value: '售后中' },
  { label: '已逾期', value: '已逾期' },
  { label: '已完成', value: '已完成' }
]

const channelOptions = ref([])

const queryParams = reactive({
  keyword: '',
  status: '',
  channelId: null,
  shipDate: ''
})

const summaryItems = computed(() => [
  { label: '全部订单', value: '', count: orders.value.length, trend: '总量', trendType: 'neutral' },
  { label: '待发货', value: '待发货', count: orders.value.filter(item => item.status === '待发货').length, trend: '优先处理', trendType: 'warning' },
  { label: '运输中', value: '已发货', count: orders.value.filter(item => item.status === '已发货').length, trend: '配送中', trendType: 'primary' },
  { label: '异常提醒', value: '已逾期', count: orders.value.filter(item => item.status === '已逾期' || item.weather.risk).length, trend: '需关注', trendType: 'danger' }
])

const filteredOrders = computed(() => {
  const keyword = queryParams.keyword.trim().toLowerCase()
  return orders.value.filter(order => {
    const searchable = [order.orderNo, order.model, order.packageName, order.channel, order.customer.nickname, order.customer.name, order.trackingNo].join(' ').toLowerCase()
    const matchesKeyword = !keyword || searchable.includes(keyword)
    const matchesStatus = !queryParams.status || order.status === queryParams.status
    const matchesChannel = !queryParams.channelId || order.channelId === queryParams.channelId
    const matchesShipDate = !queryParams.shipDate || order.shipping.shipDate === queryParams.shipDate
    const matchesAttention = !showOnlyAttention.value || order.attention || order.status === '待发货' || order.weather.risk
    return matchesKeyword && matchesStatus && matchesChannel && matchesShipDate && matchesAttention
  })
})

/** 加载订单列表，真实订单服务接入后只替换 API 返回数据，不改变页面展示结构。 */
function loadOrders() {
  if (!useRemoteOrders) {
    orders.value = createDemoOrders()
    lastUpdated.value = '演示数据'
    return
  }
  loading.value = true
  listMerchantOrders(queryParams).then(response => {
    orders.value = (response.data?.rows || response.rows || []).map(normalizeOrder)
    lastUpdated.value = '刚刚'
  }).catch(() => {
    orders.value = []
    lastUpdated.value = '加载失败'
  }).finally(() => {
    loading.value = false
  })
}

/** 加载订单筛选和录单使用的启用渠道、启用仓库。 */
function loadOrderOptions() {
  Promise.all([
    listMerchantOrderChannels({ status: 0, pageNum: 1, pageSize: 1000 }),
    listMerchantWarehouses({ status: 0, pageNum: 1, pageSize: 1000 })
  ]).then(([channelResponse, warehouseResponse]) => {
    channelOptions.value = channelResponse.rows || channelResponse.data?.rows || []
    warehouseOptions.value = warehouseResponse.rows || warehouseResponse.data?.rows || []
  })
}

/** 执行订单筛选，并将筛选结果保持在当前页面，不重新创建动态路由。 */
function handleQuery() {
  lastUpdated.value = '已筛选'
}

/** 清除订单筛选条件并恢复全部订单。 */
function resetQuery() {
  queryParams.keyword = ''
  queryParams.status = ''
  queryParams.channelId = null
  queryParams.shipDate = ''
  showOnlyAttention.value = false
  handleQuery()
}

/** 点击统计卡片时快速切换对应的订单状态筛选。 */
function selectStatus(status) {
  queryParams.status = queryParams.status === status ? '' : status
  handleQuery()
}

/** 统一处理订单操作，详情类操作打开摘要，其余操作预留给后续状态机接口。 */
function handleAction(action, order) {
  if (action === '编辑') {
    router.push({ path: '/merchant/order/form', query: { orderId: order.orderId } })
    return
  }
  if (action === '查看套餐详情') {
    activeOrder.value = order
    detailVisible.value = true
    return
  }
  const subject = order ? `订单 ${order.orderNo}` : '订单'
  proxy.$modal.msgSuccess(`${subject}：${action}操作入口已准备`)
}

/** 打开新增订单表单并清空上一次录单数据。 */
function openCreateOrder() {
  router.push('/merchant/order/form')
  return
  /* 兼容旧弹窗状态，待历史缓存页面自然销毁后移除。 */
  editingOrderId.value = null
  Object.assign(createForm, { customerName: '', customerPhone: '', channelId: null, externalOrderNo: '', customerAddress: '', leaseStartDate: '', leaseEndDate: '', rentAmount: 0, depositAmount: 0, remark: '' })
  createVisible.value = true
}

/** 打开订单编辑表单并将列表中的可编辑字段映射到提交命令。 */
function openEditOrder(order) {
  editingOrderId.value = order.orderId || null
  Object.assign(createForm, {
    customerName: order.customer?.name || '',
    customerPhone: order.customer?.phone || '',
    channelId: order.channelId || null,
    externalOrderNo: order.externalOrderNo || '',
    customerAddress: order.customer?.address || '',
    leaseStartDate: order.leaseStartDate || '',
    leaseEndDate: order.leaseEndDate || '',
    rentAmount: Number(order.rent || 0),
    depositAmount: Number(order.depositAmount || 0),
    remark: order.customer?.note || ''
  })
  createVisible.value = true
}

/** 校验并提交新增订单，成功后刷新当前商户订单列表。 */
function submitCreateOrder() {
  createFormRef.value.validate(valid => {
    if (!valid) return
    createLoading.value = true
    const request = editingOrderId.value
      ? updateMerchantOrder({ ...createForm, orderId: editingOrderId.value })
      : addMerchantOrder(createForm)
    request.then(() => {
      proxy.$modal.msgSuccess('订单创建成功')
      createVisible.value = false
      loadOrders()
    }).finally(() => { createLoading.value = false })
  })
}

/** 构造页面联调阶段使用的订单数据，接口上线后由服务端数据替换。 */
function createDemoOrders() {
  return [
    createOrder({
      orderNo: 'QY202608020001',
      model: '大疆 Osmo Pocket 3',
      packageName: '大疆pt3标准套餐',
      rent: 106,
      customerNickname: '王者荣耀金多多',
      customerName: '李光瑞',
      customerPhone: '13051569395',
      status: '待发货',
      statusType: 'warning',
      weatherRisk: true,
      attention: '这里展示管理对这个订单的批注，和系统的提醒。'
    }),
    createOrder({
      orderNo: 'QY202608010018',
      model: '大疆 Osmo Pocket 4',
      packageName: 'Pocket 4 全能套装',
      rentalPeriod: '2026-08-08 ~ 2026-08-18',
      rent: 168,
      customerNickname: '山河旅拍工作室',
      customerName: '周明远',
      customerPhone: '13800138000',
      status: '已发货',
      statusType: 'primary',
      shippingMethod: '快递',
      weatherRisk: false,
      attention: ''
    }),
    createOrder({
      orderNo: 'QY202607280006',
      model: '大疆 Osmo Action 5',
      packageName: '户外影像基础套餐',
      rentalPeriod: '2026-07-30 ~ 2026-08-05',
      returnDate: '2026-08-05',
      rent: 92,
      channel: '商户自有渠道',
      customerNickname: '北纬四十三度',
      customerName: '陈思远',
      customerPhone: '13900001234',
      status: '已归还',
      statusType: 'success',
      shippingMethod: '自提',
      weatherRisk: false,
      attention: ''
    }),
    createOrder({
      orderNo: 'QY202607250003',
      model: '大疆 Osmo Pocket 3',
      packageName: '大疆pt3标准套餐',
      rentalPeriod: '2026-07-26 ~ 2026-08-03',
      returnDate: '2026-08-03',
      rent: 106,
      channel: '租仓联盟',
      customerNickname: '漫游记录社',
      customerName: '赵一鸣',
      customerPhone: '13700005678',
      status: '已逾期',
      statusType: 'danger',
      shippingMethod: '跑腿',
      weatherRisk: true,
      attention: '客户尚未上传归还面单，请跟进。'
    })
  ]
}

/** 补齐后端订单响应中的可选展示节点，保证页面对历史订单结构保持稳定。 */
function normalizeOrder(order) {
  return {
    ...order,
    status: normalizeStatusLabel(order.status),
    customer: {
      nickname: '-',
      name: '-',
      phone: '-',
      address: '-',
      note: '暂无客户备注',
      ...(order.customer || {})
    },
    shipping: {
      warehouse: '未分配仓库',
      shipDate: '-',
      method: '待安排',
      doorTime: '待安排',
      note: '暂无发货备注',
      ...(order.shipping || {})
    },
    weather: {
      route: '发货地 → 收货地',
      depart: '待查询',
      arrive: '待查询',
      departIcon: 'sunny',
      arriveIcon: 'rain',
      risk: false,
      ...(order.weather || {})
    },
    delivery: {
      plans: [],
      expectedDate: '待确认',
      ...(order.delivery || {})
    },
    addOns: Array.isArray(order.addOns) ? order.addOns : [],
    actions: Array.isArray(order.actions) ? order.actions : createOrderActions()
  }
}

/** 将历史订单接口可能返回的英文状态编码统一转换为中文显示。 */
function normalizeStatusLabel(status) {
  const labels = { pending_ship: '待发货', prepared: '已备货', ship_in_transit: '已发货', shipped: '已发货', shipping: '运输中', renting: '租用中', returning: '归还中', overdue_return: '归还逾期', overdue: '已逾期', after_sales: '售后中', completed: '已完成', cancelled: '已取消' }
  return labels[status] || status || '未知状态'
}

/** 将订单基础参数补齐为页面统一消费的数据结构。 */
function createOrder(options) {
  const shipDate = options.shipDate || '2026-08-02'
  return {
    orderNo: options.orderNo,
    model: options.model,
    packageName: options.packageName,
    rentalPeriod: options.rentalPeriod || '2026-08-11 ~ 2026-08-31',
    returnDate: options.returnDate || '2026-08-11',
    rent: options.rent || 106,
    channel: options.channel || '人人租',
    merchant: options.merchant || '八方来客',
    customer: {
      nickname: options.customerNickname,
      name: options.customerName,
      phone: options.customerPhone,
      address: options.customerAddress || '黑龙江省哈尔滨市香坊区香坊区、王岗镇、红旗大街',
      note: options.customerNote || '请保持设备整洁，配件齐全'
    },
    shipping: {
      warehouse: options.warehouse || '杭州仓',
      shipDate,
      method: options.shippingMethod || '快递 / 自提 / 跑腿',
      doorTime: options.doorTime || 'XXXX点上门',
      note: options.shippingNote || '加一个保护套，清洗干净'
    },
    weather: {
      route: options.route || '杭州 → 北京',
      depart: options.weatherDepart || '今日晴',
      arrive: options.weatherArrive || '明日雨',
      departIcon: 'sunny',
      arriveIcon: 'rain',
      risk: options.weatherRisk
    },
    delivery: {
      plans: [
        { carrier: 'SF', name: '标快', price: 11 },
        { carrier: 'SF', name: '陆运', price: 11 }
      ],
      expectedDate: options.expectedDate || '2026-08-30'
    },
    status: options.status,
    statusType: options.statusType,
    trackingNo: options.trackingNo || 'SF123456778909',
    returnNo: options.returnNo || 'JD128990988888',
    createdAt: options.createdAt || '2026-08-02 10:20:18',
    productCode: options.productCode || 'ANGZNCQ002FAWF',
    addOns: options.addOns || ['下拉选配件', 'SN码'],
    attention: options.attention,
    operator: options.operator || '小张',
    coordinator: options.coordinator || '小李',
    actions: createOrderActions()
  }
}

/** 创建订单行底部的标准操作集合，后续状态机接口接入时复用同一动作标识。 */
function createOrderActions() {
  return [
    { label: '编辑', icon: Edit },
    { label: '复制', icon: CopyDocument },
    { label: '备货', icon: Box },
    { label: '上传视频', icon: VideoCamera },
    { label: '发货', icon: Van, type: 'success' },
    { label: '转寄', icon: Switch },
    { label: '跨仓归还', icon: Location },
    { label: '归还验机', icon: Check },
    { label: '续租', icon: Finished },
    { label: '完成', icon: CircleCheck },
    { label: '编辑记录', icon: Edit }
  ]
}

loadOrders()
loadOrderOptions()
</script>

<style scoped>
.merchant-order-page {
  min-width: 0;
  padding: 20px 24px 32px;
  color: var(--el-text-color-primary);
}

.page-heading,
.list-toolbar,
.order-footer,
.heading-actions,
.list-toolbar-actions,
.product-summary,
.customer-name,
.route-line,
.weather-line,
.delivery-plan,
.operator-meta {
  display: flex;
  align-items: center;
}

.page-heading {
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.page-kicker {
  margin: 0 0 4px;
  color: var(--el-color-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.page-heading h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 650;
  letter-spacing: 0;
}

.page-description {
  margin: 7px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.heading-actions,
.list-toolbar-actions {
  gap: 12px;
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1px;
  margin-bottom: 16px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-border-color-light);
}

.summary-item {
  position: relative;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 3px 12px;
  min-width: 0;
  padding: 14px 18px;
  border: 0;
  background: var(--el-bg-color);
  color: inherit;
  cursor: pointer;
  text-align: left;
  transition: background-color 0.2s ease;
}

.summary-item:hover,
.summary-item.is-active {
  background: var(--el-color-primary-light-9);
}

.summary-item.is-active::before {
  position: absolute;
  inset: 0 auto 0 0;
  width: 3px;
  background: var(--el-color-primary);
  content: '';
}

.summary-label {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.summary-item strong {
  grid-row: span 2;
  align-self: center;
  color: var(--el-text-color-primary);
  font-size: 23px;
  line-height: 1;
}

.summary-trend {
  font-size: 12px;
}

.summary-trend.warning { color: var(--el-color-warning); }
.summary-trend.primary { color: var(--el-color-primary); }
.summary-trend.danger { color: var(--el-color-danger); }
.summary-trend.neutral { color: var(--el-text-color-placeholder); }

.filter-panel {
  margin-bottom: 16px;
  padding: 16px 18px 2px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-bg-color);
}

.filter-panel :deep(.el-form-item) {
  margin-right: 18px;
  margin-bottom: 14px;
}

.keyword-input { width: 270px; }
.filter-select { width: 150px; }
.date-input { width: 170px; }
.filter-actions { margin-right: 0 !important; }

.order-list-shell {
  min-width: 0;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-bg-color);
}

.list-toolbar {
  justify-content: space-between;
  min-height: 60px;
  padding: 0 18px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.list-toolbar h3 {
  display: inline;
  margin: 0 12px 0 0;
  font-size: 16px;
  font-weight: 650;
}

.list-toolbar span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.order-scroll-area {
  overflow-x: auto;
}

.order-grid {
  display: grid;
  grid-template-columns: 280px 240px 220px 220px 220px 220px 250px;
  min-width: 1650px;
}

.order-grid-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-fill-color-lighter);
  color: var(--el-text-color-secondary);
  font-size: 13px;
  font-weight: 650;
}

.order-card {
  min-width: 1650px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.order-card:last-child { border-bottom: 0; }

.order-grid-content {
  align-items: stretch;
  padding: 16px;
}

.order-cell {
  min-width: 0;
  padding: 0 16px;
  border-right: 1px solid var(--el-border-color-lighter);
}

.order-cell:first-child { padding-left: 0; }
.order-cell:last-child { padding-right: 0; border-right: 0; }

.mobile-cell-title {
  display: none;
  margin-bottom: 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 650;
}

.product-summary { align-items: flex-start; gap: 10px; margin-bottom: 12px; }

.product-thumb {
  display: grid;
  flex: 0 0 48px;
  place-items: center;
  width: 48px;
  height: 58px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  background: linear-gradient(145deg, #e8f1f7, #c2d1d9);
  color: #2d5966;
}

.product-thumb :deep(.el-icon) { font-size: 25px; }
.product-summary strong,
.product-summary span { display: block; line-height: 1.5; }
.product-summary strong { font-size: 14px; }
.product-summary span { color: var(--el-text-color-secondary); font-size: 12px; }

.detail-list { margin: 0; font-size: 12px; line-height: 1.55; }
.detail-list > div { display: grid; grid-template-columns: 42px minmax(0, 1fr); gap: 5px; margin-bottom: 5px; }
.detail-list dt { color: var(--el-text-color-secondary); }
.detail-list dd { min-width: 0; margin: 0; overflow-wrap: anywhere; }
.detail-list em { display: block; color: var(--el-text-color-secondary); font-style: normal; }
.money { color: var(--el-color-danger); font-weight: 650; }

.customer-name { gap: 5px; margin-bottom: 12px; font-size: 14px; font-weight: 650; }
.customer-name :deep(.el-icon) {
  flex: 0 0 14px;
  width: 14px;
  height: 14px;
  color: var(--el-color-primary);
  font-size: 14px;
}
.customer-name > svg {
  flex: 0 0 14px;
  width: 14px;
  height: 14px;
}

.cell-note {
  display: flex;
  gap: 4px;
  margin: 12px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
}

.cell-note :deep(.el-icon) {
  flex: 0 0 13px;
  width: 13px;
  height: 13px;
  margin-top: 2px;
  color: var(--el-color-warning);
  font-size: 13px;
}
.cell-note > svg {
  flex: 0 0 13px;
  width: 13px;
  height: 13px;
  margin-top: 2px;
}
.route-line { gap: 5px; margin-bottom: 11px; color: var(--el-text-color-primary); font-size: 13px; font-weight: 650; }
.route-line :deep(.el-icon) { color: var(--el-color-danger); }
.route-line > svg {
  flex: 0 0 14px;
  width: 14px;
  height: 14px;
  color: var(--el-color-danger);
}
.weather-line { gap: 6px; margin-bottom: 7px; color: var(--el-text-color-secondary); font-size: 12px; }
.weather-line > span { width: 14px; color: var(--el-text-color-primary); }
.weather-line i { display: inline-flex; width: 16px; height: 16px; align-items: center; justify-content: center; font-size: 13px; font-style: normal; line-height: 16px; text-align: center; }
.weather-line i.sunny { color: #f59e0b; }
.weather-line i.rain { color: #5b7cba; }
.weather-cell :deep(.el-tag) { margin-top: 5px; }

.delivery-plan { gap: 6px; margin-bottom: 9px; font-size: 12px; }
.carrier-mark { display: inline-grid; place-items: center; width: 23px; height: 23px; border: 1px solid #d5ad45; border-radius: 50%; color: #906e18; font-size: 10px; font-weight: 700; }
.delivery-plan b { margin-left: auto; color: var(--el-text-color-primary); font-weight: 500; }
.delivery-cell :deep(.el-button) { padding-left: 0; }
.delivery-after { margin-top: 7px; color: var(--el-text-color-secondary); font-size: 12px; }

.status-tag { margin-bottom: 12px; }
.status-cell .detail-list dd { white-space: nowrap; }
.product-list-cell strong { display: block; line-height: 1.5; }
.product-list-cell p { margin: 8px 0 0; color: var(--el-text-color-secondary); font-size: 12px; line-height: 1.5; }
.product-list-cell strong :deep(.el-button) { padding: 0; font-size: 12px; }
.addon-title { display: flex; align-items: center; gap: 4px; color: var(--el-text-color-primary) !important; font-weight: 650; }
.addon-title > svg { flex: 0 0 14px; width: 14px; height: 14px; }
.addon-item { padding-left: 18px; }
.full-width { width: 100%; }

.order-footer {
  justify-content: space-between;
  gap: 16px;
  box-sizing: border-box;
  min-width: 1650px;
  min-height: 48px;
  padding: 8px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-blank);
}

.attention-note { display: flex; align-items: center; gap: 4px; min-width: 260px; color: var(--el-text-color-primary); font-size: 12px; }
.attention-note :deep(.el-icon) { color: var(--el-color-warning); }
.attention-note span { color: var(--el-text-color-secondary); }
.attention-note.muted { color: var(--el-text-color-placeholder); }
.order-actions { display: flex; flex-wrap: wrap; align-items: center; justify-content: flex-end; gap: 2px; }
.order-actions :deep(.el-button) { margin-left: 0; padding: 4px 6px; font-size: 12px; }
.operator-meta { margin-left: 8px; color: var(--el-text-color-secondary); font-size: 12px; white-space: nowrap; }

@media (max-width: 1100px) {
  .page-heading { align-items: flex-start; flex-direction: column; }
  .heading-actions { width: 100%; justify-content: flex-end; }
  .filter-panel :deep(.el-form) { display: flex; flex-wrap: wrap; }
}

@media (max-width: 768px) {
  .merchant-order-page { padding: 16px 12px 24px; }
  .summary-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .filter-panel { padding: 14px 14px 2px; }
  .filter-panel :deep(.el-form-item) { width: 100%; margin-right: 0; }
  .keyword-input,
  .filter-select,
  .date-input { width: 100%; }
  .filter-actions { display: flex; justify-content: flex-end; }
  .list-toolbar { align-items: flex-start; flex-direction: column; gap: 10px; padding: 14px; }
  .list-toolbar-actions { width: 100%; justify-content: space-between; }
  .order-scroll-area { overflow-x: visible; }
  .order-grid { display: block; min-width: 0; }
  .order-card,
  .order-footer { min-width: 0; }
  .order-grid-header { display: none; }
  .order-grid-content { padding: 0; }
  .order-cell { padding: 16px; border-right: 0; border-bottom: 1px solid var(--el-border-color-lighter); }
  .order-cell:first-child { padding-left: 16px; }
  .order-cell:last-child { padding-right: 16px; border-bottom: 0; }
  .mobile-cell-title { display: block; }
  .order-footer { align-items: flex-start; flex-direction: column; padding: 12px 16px; }
  .attention-note { min-width: 0; }
  .order-actions { justify-content: flex-start; }
  .operator-meta { margin: 6px 0 0; white-space: normal; }
}
</style>
