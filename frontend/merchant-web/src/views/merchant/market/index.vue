<template>
  <div class="app-container merchant-market-page">
    <PageHeading
      kicker="MARKET"
      title="周转市场"
      description="发布、管理和维护商户的出售、出租与求购信息">
      <template #aside>
        <el-button :icon="Refresh" @click="refreshActiveSection">刷新</el-button>
        <el-button
          v-hasPermi="['merchant:market:add']"
          type="primary"
          :icon="Plus"
          @click="openCreate">
          发布帖子
        </el-button>
      </template>
    </PageHeading>

    <el-tabs v-model="activeSection" class="market-tabs" @tab-change="handleSectionChange">
      <el-tab-pane label="我的帖子" name="mine" />
      <el-tab-pane label="市场浏览" name="browse" />
      <el-tab-pane label="站内消息" name="messages" />
      <el-tab-pane label="我的举报" name="reports" />
    </el-tabs>

    <template v-if="activeSection === 'mine'">

    <el-card shadow="never" class="market-panel">
      <el-form :model="query" :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            clearable
            placeholder="帖子编号、品牌或型号"
            @keyup.enter="load" />
        </el-form-item>
        <el-form-item label="板块">
          <el-select v-model="query.boardType" clearable placeholder="全部板块" @change="load">
            <el-option v-for="item in boardOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.postStatus" clearable placeholder="全部状态" @change="load">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="query.city" clearable placeholder="发货城市" @keyup.enter="load" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="market-panel">
      <el-table v-loading="loading" :data="rows" border row-key="postNo">
        <el-table-column label="帖子编号" prop="postNo" min-width="210" show-overflow-tooltip />
        <el-table-column label="板块" prop="boardTypeName" width="90" align="center" />
        <el-table-column label="商品" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="product-name">{{ row.brand || '-' }} {{ row.modelName || '-' }}</div>
            <div class="product-spec">{{ row.category || '未填写品类' }} · {{ row.memorySpec || '未填写规格' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="成色" prop="conditionLevel" width="90" align="center">
          <template #default="{ row }">{{ row.conditionLevel || '-' }}</template>
        </el-table-column>
        <el-table-column label="数量" prop="quantity" width="80" align="center" />
        <el-table-column label="发货城市" prop="city" min-width="110" />
        <el-table-column label="近30天周转" width="110" align="center">
          <template #default="{ row }">{{ row.recent30dTurnover ?? 0 }} 次</template>
        </el-table-column>
        <el-table-column label="状态" prop="postStatusName" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.postStatus)" effect="plain">
              {{ row.postStatusName || '未知状态' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updateTime" width="170" />
        <el-table-column label="操作" fixed="right" width="250" align="center">
          <template #default="{ row }">
            <el-button
              v-if="canEdit(row)"
              v-hasPermi="['merchant:market:edit']"
              link
              type="primary"
              icon="Edit"
              @click="openEdit(row)">
              编辑
            </el-button>
            <el-button
              v-if="canPublish(row)"
              v-hasPermi="['merchant:market:edit']"
              link
              type="success"
              @click="changeStatus(row, 'publish')">
              发布
            </el-button>
            <el-button
              v-if="Number(row.postStatus) === POST_STATUS.PUBLISHED"
              v-hasPermi="['merchant:market:edit']"
              link
              type="warning"
              @click="changeStatus(row, 'off-shelf')">
              下架
            </el-button>
            <el-button
              v-if="Number(row.postStatus) !== POST_STATUS.CLOSED"
              v-hasPermi="['merchant:market:edit']"
              link
              type="danger"
              @click="changeStatus(row, 'close')">
              关闭
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        v-model:page="query.pageNum"
        v-model:limit="query.pageSize"
        :total="total"
        @pagination="load" />
      <el-empty v-if="!loading && rows.length === 0" description="暂无周转市场帖子" />
    </el-card>

    </template>

    <template v-if="activeSection === 'browse'">
      <el-card shadow="never" class="market-panel">
        <el-form :model="publicQuery" :inline="true" class="search-form" @submit.prevent>
          <el-form-item label="关键词">
            <el-input
              v-model="publicQuery.keyword"
              clearable
              placeholder="帖子编号、商户、品牌或型号"
              @keyup.enter="loadPublic" />
          </el-form-item>
          <el-form-item label="板块">
            <el-select v-model="publicQuery.boardType" clearable placeholder="全部板块" @change="loadPublic">
              <el-option v-for="item in boardOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="城市">
            <el-input v-model="publicQuery.city" clearable placeholder="发货城市" @keyup.enter="loadPublic" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadPublic">查询</el-button>
            <el-button :icon="Refresh" @click="resetPublic">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never" class="market-panel">
        <el-table v-loading="publicLoading" :data="publicRows" border row-key="postNo">
          <el-table-column label="发布商户" prop="publisherName" min-width="150" show-overflow-tooltip />
          <el-table-column label="帖子编号" prop="postNo" min-width="210" show-overflow-tooltip />
          <el-table-column label="板块" prop="boardTypeName" width="90" align="center" />
          <el-table-column label="商品" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="product-name">{{ row.brand || '-' }} {{ row.modelName || '-' }}</div>
              <div class="product-spec">{{ row.category || '未填写品类' }} · {{ row.memorySpec || '未填写规格' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="成色" prop="conditionLevel" width="90" align="center">
            <template #default="{ row }">{{ row.conditionLevel || '-' }}</template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" width="80" align="center" />
          <el-table-column label="发货城市" prop="city" min-width="110" />
          <el-table-column label="操作" fixed="right" width="150" align="center">
            <template #default="{ row }">
              <el-button
                v-hasPermi="['merchant:market:message']"
                link
                type="primary"
                @click="openMessage(row)">
                私信
              </el-button>
              <el-button
                v-hasPermi="['merchant:market:report']"
                link
                type="danger"
                @click="openReport(row)">
                举报
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="publicTotal > 0"
          v-model:page="publicQuery.pageNum"
          v-model:limit="publicQuery.pageSize"
          :total="publicTotal"
          @pagination="loadPublic" />
        <el-empty v-if="!publicLoading && publicRows.length === 0" description="暂无公开市场帖子" />
      </el-card>
    </template>

    <template v-if="activeSection === 'messages'">
      <el-card shadow="never" class="market-panel">
        <el-form :model="messageQuery" :inline="true" class="search-form" @submit.prevent>
          <el-form-item label="关键词">
            <el-input
              v-model="messageQuery.keyword"
              clearable
              placeholder="帖子编号、商户或消息内容"
              @keyup.enter="loadMessages" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="messageQuery.readStatus" clearable placeholder="全部状态" @change="loadMessages">
              <el-option v-for="item in readStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadMessages">查询</el-button>
            <el-button :icon="Refresh" @click="resetMessages">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <el-card shadow="never" class="market-panel">
        <el-table v-loading="messageLoading" :data="messageRows" border row-key="messageId">
          <el-table-column label="帖子编号" prop="postNo" min-width="210" show-overflow-tooltip />
          <el-table-column label="发送方" prop="fromMerchantName" min-width="140" show-overflow-tooltip />
          <el-table-column label="接收方" prop="toMerchantName" min-width="140" show-overflow-tooltip />
          <el-table-column label="消息内容" prop="messageContent" min-width="260" show-overflow-tooltip />
          <el-table-column label="状态" prop="readFlagName" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="Number(row.readFlag) === 1 ? 'info' : 'warning'" effect="plain">
                {{ row.readFlagName || '未知状态' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发送时间" prop="createTime" width="170" />
          <el-table-column label="操作" fixed="right" width="100" align="center">
            <template #default="{ row }">
              <el-button
                v-if="Number(row.readFlag) === 0"
                link
                type="primary"
                @click="markRead(row)">
                标记已读
              </el-button>
              <span v-else class="muted-text">-</span>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="messageTotal > 0"
          v-model:page="messageQuery.pageNum"
          v-model:limit="messageQuery.pageSize"
          :total="messageTotal"
          @pagination="loadMessages" />
        <el-empty v-if="!messageLoading && messageRows.length === 0" description="暂无站内消息" />
      </el-card>
    </template>

    <template v-if="activeSection === 'reports'">
      <el-card shadow="never" class="market-panel">
        <el-table v-loading="reportLoading" :data="reportRows" border row-key="postNo">
          <el-table-column label="帖子编号" prop="postNo" min-width="210" show-overflow-tooltip />
          <el-table-column label="被举报商户" prop="reportedMerchantName" min-width="160" show-overflow-tooltip />
          <el-table-column label="举报类型" prop="reportTypeName" width="130" align="center" />
          <el-table-column label="举报说明" prop="evidence" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">{{ row.evidence || '-' }}</template>
          </el-table-column>
          <el-table-column label="处理状态" prop="reportStatusName" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="reportTagType(row.reportStatus)" effect="plain">
                {{ row.reportStatusName || '未知状态' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理结果" prop="reviewResult" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">{{ row.reviewResult || '-' }}</template>
          </el-table-column>
          <el-table-column label="提交时间" prop="createTime" width="170" />
          <el-table-column label="处理时间" prop="reviewTime" width="170">
            <template #default="{ row }">{{ row.reviewTime || '-' }}</template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="reportTotal > 0"
          v-model:page="reportQuery.pageNum"
          v-model:limit="reportQuery.pageSize"
          :total="reportTotal"
          @pagination="loadReports" />
        <el-empty v-if="!reportLoading && reportRows.length === 0" description="暂无举报记录" />
      </el-card>
    </template>

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑市场帖子' : '发布市场帖子'"
      width="680px"
      destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="板块" prop="boardType">
          <el-radio-group v-model="form.boardType" @change="handleBoardTypeChange">
            <el-radio-button v-for="item in boardOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="品类" prop="category">
              <el-select
                v-model="form.category"
                filterable
                clearable
                placeholder="请选择品类"
                class="field-control"
                @change="handleCategoryChange">
                <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-select
                v-model="form.brand"
                filterable
                clearable
                placeholder="请选择品牌"
                class="field-control"
                @change="handleBrandChange">
                <el-option v-for="item in brandOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号" prop="modelName">
              <el-select
                v-model="form.modelName"
                filterable
                clearable
                placeholder="请选择型号"
                class="field-control"
                @change="handleModelChange">
                <el-option v-for="item in modelOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格" prop="memorySpec">
              <el-select v-model="form.memorySpec" clearable placeholder="请选择规格" class="field-control">
                <el-option v-for="item in memoryOptions" :key="item || 'default'" :label="item || '标准规格'" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成色" prop="conditionLevel">
              <el-select v-model="form.conditionLevel" clearable placeholder="请选择成色" class="field-control">
                <el-option
                  v-for="item in conditionOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="1" :max="100000" :precision="0" controls-position="right" class="field-control" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.boardType !== BOARD_TYPE.BUY" :span="24">
            <el-form-item label="实物设备" prop="inventoryUnitIds">
              <el-select
                v-model="form.inventoryUnitIds"
                class="field-control"
                multiple
                filterable
                clearable
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="3"
                :loading="inventoryOptionsLoading"
                placeholder="请选择已认证的可用实物设备"
                @focus="loadInventoryOptions">
                <el-option
                  v-for="item in inventoryOptions"
                  :key="item.unitId"
                  :label="inventoryOptionLabel(item)"
                  :value="item.unitId" />
              </el-select>
              <div class="form-help">
                出售或出租帖子发布前必须绑定与数量一致、已上传实物照片的可用设备；求购帖子不需要绑定库存。
              </div>
            </el-form-item>
          </el-col>
          <el-col v-if="form.boardType !== BOARD_TYPE.RENT" :span="12">
            <el-form-item label="价格区间" prop="priceRangeCode">
              <el-select v-model="form.priceRangeCode" clearable placeholder="请选择价格区间" class="field-control">
                <el-option v-for="item in priceRangeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发货城市" prop="city">
              <el-input v-model="form.city" maxlength="64" placeholder="请输入城市" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-alert
          title="商品、规格、成色和价格均使用平台固定选项；出租板块不展示价格。保存后默认为草稿，确认内容无误后再发布。"
          type="info"
          :closable="false"
          show-icon />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存草稿</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="messageDialogVisible" title="发送市场私信" width="560px" destroy-on-close>
      <el-alert
        v-if="selectedPost"
        :title="`${selectedPost.publisherName || '其他商户'} · ${selectedPost.brand || ''} ${selectedPost.modelName || ''}`"
        type="info"
        :closable="false"
        show-icon
        class="dialog-alert" />
      <el-form label-width="80px">
        <el-form-item label="消息内容" required>
          <el-input
            v-model="messageForm.content"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="请输入要发送给对方的内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="messageDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="messageSubmitting" @click="submitMessage">发送</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reportDialogVisible" title="举报市场帖子" width="560px" destroy-on-close>
      <el-alert
        v-if="selectedPost"
        :title="`${selectedPost.publisherName || '其他商户'} · ${selectedPost.brand || ''} ${selectedPost.modelName || ''}`"
        type="warning"
        :closable="false"
        show-icon
        class="dialog-alert" />
      <el-form label-width="80px">
        <el-form-item label="举报类型" required>
          <el-select v-model="reportForm.reportType" class="field-control" placeholder="请选择举报类型">
            <el-option v-for="item in reportTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="举报说明">
          <el-input
            v-model="reportForm.evidence"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="请补充问题描述或相关证据" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="reportSubmitting" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import PageHeading from '@/components/PageHeading.vue'
import {
  closeMerchantMarketPost,
  createMerchantMarketPost,
  getMerchantMarketPost,
  getMerchantMarketOptions,
  listMarketMessages,
  listMarketReports,
  listMerchantMarketInventoryOptions,
  listPublicMarketPosts,
  listMerchantMarketPosts,
  markMarketMessageRead,
  offShelfMerchantMarketPost,
  publishMerchantMarketPost,
  sendMarketMessage,
  submitMarketReport,
  updateMerchantMarketPost
} from '@/api/merchant/market'

const BOARD_TYPE = Object.freeze({ SALE: 'SALE', RENT: 'RENT', BUY: 'BUY' })
const POST_STATUS = Object.freeze({ DRAFT: 0, PUBLISHED: 1, CLOSED: 2, OFF_SHELF: 3 })
const boardOptions = [
  { value: BOARD_TYPE.SALE, label: '出售' },
  { value: BOARD_TYPE.RENT, label: '出租' },
  { value: BOARD_TYPE.BUY, label: '求购' }
]
const statusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'PUBLISHED', label: '已发布' },
  { value: 'OFF_SHELF', label: '已下架' },
  { value: 'CLOSED', label: '已关闭' }
]
const activeSection = ref('mine')
const readStatusOptions = [
  { value: 'UNREAD', label: '未读' },
  { value: 'READ', label: '已读' }
]
const reportTypeOptions = [
  { value: 'FAKE_INVENTORY', label: '虚假库存' },
  { value: 'ILLEGAL_CONTENT', label: '违规内容' },
  { value: 'CONTACT_OUTSIDE', label: '引导站外联系' },
  { value: 'OTHER', label: '其他' }
]
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', boardType: undefined, postStatus: undefined, city: '' })
const publicQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', boardType: undefined, city: '' })
const messageQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', readStatus: undefined })
const reportQuery = reactive({ pageNum: 1, pageSize: 10 })
const rows = ref([])
const publicRows = ref([])
const messageRows = ref([])
const reportRows = ref([])
const total = ref(0)
const publicTotal = ref(0)
const messageTotal = ref(0)
const reportTotal = ref(0)
const loading = ref(false)
const publicLoading = ref(false)
const messageLoading = ref(false)
const reportLoading = ref(false)
const saving = ref(false)
const messageSubmitting = ref(false)
const reportSubmitting = ref(false)
const dialogVisible = ref(false)
const messageDialogVisible = ref(false)
const reportDialogVisible = ref(false)
const selectedPost = ref(null)
const editing = ref(false)
const formRef = ref()
const editingPostNo = ref('')
const form = reactive(defaultForm())
const marketOptions = reactive({ models: [], conditionLevels: [], priceRanges: [] })
const inventoryOptions = ref([])
const inventoryOptionsLoading = ref(false)
const categoryOptions = computed(() => uniqueValues(marketOptions.models.map((item) => item.category)))
const brandOptions = computed(() => uniqueValues(
  marketOptions.models
    .filter((item) => !form.category || item.category === form.category)
    .map((item) => item.brand)
))
const modelOptions = computed(() => uniqueValues(
  marketOptions.models
    .filter((item) => (!form.category || item.category === form.category) && (!form.brand || item.brand === form.brand))
    .map((item) => item.modelName)
))
const memoryOptions = computed(() => uniqueValues(
  marketOptions.models
    .filter((item) => (!form.category || item.category === form.category)
      && (!form.brand || item.brand === form.brand)
      && (!form.modelName || item.modelName === form.modelName))
    .map((item) => item.memorySpec || '')
))
const conditionOptions = computed(() => marketOptions.conditionLevels)
const priceRangeOptions = computed(() => marketOptions.priceRanges)
const messageForm = reactive({ content: '' })
const reportForm = reactive({ reportType: 'FAKE_INVENTORY', evidence: '' })
const rules = computed(() => ({
  boardType: [{ required: true, message: '请选择板块', trigger: 'change' }],
  category: [{ required: true, message: '请选择品类', trigger: 'change' }],
  brand: [{ required: true, message: '请选择品牌', trigger: 'change' }],
  modelName: [{ required: true, message: '请选择型号', trigger: 'change' }],
  conditionLevel: [{ required: true, message: '请选择成色', trigger: 'change' }],
  priceRangeCode: form.boardType === BOARD_TYPE.RENT
    ? []
    : [{ required: true, message: '请选择价格区间', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'change' }],
  city: [{ required: true, message: '请输入发货城市', trigger: 'blur' }]
}))

function defaultForm() {
  return {
    boardType: BOARD_TYPE.SALE,
    category: '',
    brand: '',
    modelName: '',
    memorySpec: '',
    conditionLevel: '',
    quantity: 1,
    priceRangeCode: '',
    inventoryUnitIds: [],
    city: '',
    serviceTags: '',
    remark: ''
  }
}

function unwrapRows(response) {
  return response?.rows || response?.data?.rows || response?.data || []
}

function responseTotal(response) {
  return response?.total || response?.data?.total || 0
}

function uniqueValues(values) {
  return [...new Set(values.filter((value) => value !== null && value !== undefined))]
}

function optionValue(options, value) {
  if (!value) return ''
  const option = options.find((item) => item.value === value || item.label === value)
  return option?.value || value
}

async function loadMarketOptions() {
  try {
    const response = await getMerchantMarketOptions()
    const data = response?.data || {}
    marketOptions.models = Array.isArray(data.models) ? data.models : []
    marketOptions.conditionLevels = Array.isArray(data.conditionLevels) ? data.conditionLevels : []
    marketOptions.priceRanges = Array.isArray(data.priceRanges) ? data.priceRanges : []
  } catch {
    ElMessage.error('周转市场固定选项加载失败')
  }
}

async function load() {
  loading.value = true
  try {
    const response = await listMerchantMarketPosts(query)
    rows.value = unwrapRows(response)
    total.value = responseTotal(response)
  } finally {
    loading.value = false
  }
}

async function loadPublic() {
  publicLoading.value = true
  try {
    const response = await listPublicMarketPosts(publicQuery)
    publicRows.value = unwrapRows(response)
    publicTotal.value = responseTotal(response)
  } finally {
    publicLoading.value = false
  }
}

async function loadMessages() {
  messageLoading.value = true
  try {
    const response = await listMarketMessages(messageQuery)
    messageRows.value = unwrapRows(response)
    messageTotal.value = responseTotal(response)
  } finally {
    messageLoading.value = false
  }
}

async function loadReports() {
  reportLoading.value = true
  try {
    const response = await listMarketReports(reportQuery)
    reportRows.value = unwrapRows(response)
    reportTotal.value = responseTotal(response)
  } finally {
    reportLoading.value = false
  }
}

function handleSectionChange(section) {
  if (section === 'browse') loadPublic()
  if (section === 'messages') loadMessages()
  if (section === 'reports') loadReports()
}

function refreshActiveSection() {
  if (activeSection.value === 'browse') return loadPublic()
  if (activeSection.value === 'messages') return loadMessages()
  if (activeSection.value === 'reports') return loadReports()
  return load()
}

function reset() {
  Object.assign(query, { pageNum: 1, keyword: '', boardType: undefined, postStatus: undefined, city: '' })
  load()
}

function resetPublic() {
  Object.assign(publicQuery, { pageNum: 1, keyword: '', boardType: undefined, city: '' })
  loadPublic()
}

function resetMessages() {
  Object.assign(messageQuery, { pageNum: 1, keyword: '', readStatus: undefined })
  loadMessages()
}

function openMessage(row) {
  selectedPost.value = row
  messageForm.content = ''
  messageDialogVisible.value = true
}

async function submitMessage() {
  if (!selectedPost.value || !messageForm.content.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  messageSubmitting.value = true
  try {
    await sendMarketMessage({ postNo: selectedPost.value.postNo, content: messageForm.content.trim() })
    ElMessage.success('私信已发送')
    messageDialogVisible.value = false
    if (activeSection.value === 'messages') await loadMessages()
  } finally {
    messageSubmitting.value = false
  }
}

function openReport(row) {
  selectedPost.value = row
  Object.assign(reportForm, { reportType: 'FAKE_INVENTORY', evidence: '' })
  reportDialogVisible.value = true
}

async function submitReport() {
  if (!selectedPost.value || !reportForm.reportType) {
    ElMessage.warning('请选择举报类型')
    return
  }
  reportSubmitting.value = true
  try {
    await submitMarketReport({
      postNo: selectedPost.value.postNo,
      reportType: reportForm.reportType,
      evidence: reportForm.evidence.trim()
    })
    ElMessage.success('举报已提交')
    reportDialogVisible.value = false
    if (activeSection.value === 'reports') await loadReports()
  } finally {
    reportSubmitting.value = false
  }
}

async function markRead(row) {
  await markMarketMessageRead(row.messageId)
  ElMessage.success('消息已标记为已读')
  await loadMessages()
}

function openCreate() {
  Object.assign(form, defaultForm())
  inventoryOptions.value = []
  editing.value = false
  editingPostNo.value = ''
  dialogVisible.value = true
}

async function openEdit(row) {
  const response = await getMerchantMarketPost(row.postNo)
  const detail = response?.data || {}
  Object.assign(form, {
    boardType: Number(detail.boardType) === 1 ? BOARD_TYPE.SALE : Number(detail.boardType) === 2 ? BOARD_TYPE.RENT : BOARD_TYPE.BUY,
    category: detail.category || '',
    brand: detail.brand || '',
    modelName: detail.modelName || '',
    memorySpec: detail.memorySpec || '',
    conditionLevel: optionValue(conditionOptions.value, detail.conditionLevel),
    quantity: detail.quantity || 1,
    priceRangeCode: optionValue(priceRangeOptions.value, detail.priceRangeCode),
    inventoryUnitIds: Array.isArray(detail.inventoryUnitIds) ? detail.inventoryUnitIds : [],
    city: detail.city || '',
    serviceTags: detail.serviceTags || '',
    remark: detail.remark || ''
  })
  inventoryOptions.value = []
  editing.value = true
  editingPostNo.value = detail.postNo || row.postNo
  dialogVisible.value = true
  await loadInventoryOptions()
}

function handleBoardTypeChange(value) {
  if (value === BOARD_TYPE.RENT) form.priceRangeCode = ''
  if (value === BOARD_TYPE.BUY) {
    form.inventoryUnitIds = []
    inventoryOptions.value = []
  } else {
    loadInventoryOptions()
  }
}

function handleCategoryChange() {
  form.brand = ''
  form.modelName = ''
  form.memorySpec = ''
  form.inventoryUnitIds = []
  inventoryOptions.value = []
}

function handleBrandChange() {
  form.modelName = ''
  form.memorySpec = ''
  form.inventoryUnitIds = []
  inventoryOptions.value = []
}

function handleModelChange() {
  form.memorySpec = ''
  form.inventoryUnitIds = []
  inventoryOptions.value = []
  loadInventoryOptions()
}

async function loadInventoryOptions() {
  if (form.boardType === BOARD_TYPE.BUY || !form.brand || !form.modelName) {
    inventoryOptions.value = []
    return
  }
  inventoryOptionsLoading.value = true
  try {
    const response = await listMerchantMarketInventoryOptions({
      brand: form.brand,
      modelName: form.modelName,
      memorySpec: form.memorySpec || undefined
    })
    inventoryOptions.value = Array.isArray(response?.data) ? response.data : []
  } finally {
    inventoryOptionsLoading.value = false
  }
}

function inventoryOptionLabel(item) {
  const code = item.serialNumber || item.unitCode || '未填写序列号'
  return `${code} · ${item.warehouseName || '未设置仓库'}`
}

async function save() {
  if (!formRef.value) return
  if (form.boardType === BOARD_TYPE.RENT) form.priceRangeCode = ''
  if (form.boardType === BOARD_TYPE.BUY) form.inventoryUnitIds = []
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editing.value) await updateMerchantMarketPost(editingPostNo.value, form)
    else await createMerchantMarketPost(form)
    ElMessage.success('市场帖子已保存')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function changeStatus(row, action) {
  const messages = {
    publish: '确认发布这条市场帖子吗？',
    'off-shelf': '确认下架这条市场帖子吗？',
    close: '关闭后不能再次发布，确认关闭吗？'
  }
  await ElMessageBox.confirm(messages[action], '操作确认', { type: action === 'close' ? 'warning' : 'info' })
  const actions = { publish: publishMerchantMarketPost, 'off-shelf': offShelfMerchantMarketPost, close: closeMerchantMarketPost }
  await actions[action](row.postNo)
  ElMessage.success('帖子状态已更新')
  await load()
}

function canEdit(row) {
  return [POST_STATUS.DRAFT, POST_STATUS.OFF_SHELF].includes(Number(row.postStatus))
}

function canPublish(row) {
  return [POST_STATUS.DRAFT, POST_STATUS.OFF_SHELF].includes(Number(row.postStatus))
}

function statusTagType(status) {
  return { [POST_STATUS.DRAFT]: 'info', [POST_STATUS.PUBLISHED]: 'success', [POST_STATUS.OFF_SHELF]: 'warning', [POST_STATUS.CLOSED]: 'danger' }[Number(status)] || 'info'
}

function reportTagType(status) {
  return { 0: 'warning', 1: 'success', 2: 'info' }[Number(status)] || 'info'
}

loadMarketOptions().finally(() => load())
</script>

<style scoped>
.merchant-market-page {
  min-width: 0;
}

.market-panel {
  margin-bottom: 16px;
}

.market-tabs {
  margin-bottom: 16px;
}

.product-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.product-spec {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.field-control {
  width: 100%;
}

.form-help {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 18px;
}

.dialog-alert {
  margin-bottom: 18px;
}

.muted-text {
  color: var(--el-text-color-secondary);
}
</style>
