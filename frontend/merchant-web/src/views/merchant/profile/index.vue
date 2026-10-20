<template>
  <div class="app-container merchant-store-settings-page">
    <el-card shadow="never" class="store-summary-card">
      <div class="store-summary-header">
        <div class="store-identity">
          <el-avatar :size="64" :src="page.profile.storeLogoUrl">
            {{ storeInitial }}
          </el-avatar>
          <div>
            <div class="store-name">
              {{ page.profile.nickname || "未设置店铺名称" }}
            </div>
            <div class="store-subtitle">商户店铺设置</div>
          </div>
        </div>
        <div class="store-summary-actions">
          <el-tag :type="statusType(page.profile.authStatus)">
            {{ statusLabel(page.profile.authStatus) }}
          </el-tag>
          <el-tag v-if="page.canEdit" type="success" effect="plain">
            管理员可编辑
          </el-tag>
          <el-tag v-else type="info" effect="plain">只读用户</el-tag>
        </div>
      </div>
      <el-descriptions :column="4" border label-width="120" class="store-summary-descriptions">
        <el-descriptions-item label="联系人">
          {{ page.profile.contactName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="客服电话">
          {{ page.profile.servicePhone || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="会员类型">
          {{ membershipLabel(page.profile.membershipType) }}
        </el-descriptions-item>
        <el-descriptions-item label="会员等级">
          {{ page.profile.vipLevelName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="对外接单">
          <el-tag
            :type="page.profile.externalAcceptEnabled ? 'success' : 'info'"
            size="small">
            {{ page.profile.externalAcceptEnabled ? "已开启" : "未开启" }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="存储额度">
          {{ page.profile.storageQuotaMb || 0 }} MB
        </el-descriptions-item>
        <el-descriptions-item label="微信二维码">
          {{ page.profile.wechatQrConfigured ? "已配置" : "未配置" }}
        </el-descriptions-item>
        <el-descriptions-item label="店铺状态">
          <el-tag
            :type="page.profile.status === 0 ? 'success' : 'danger'"
            size="small">
            {{ page.profile.status === 0 ? "正常" : "停用" }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="16" class="settings-layout">
      <el-col :span="5" :xs="24">
        <el-card shadow="never" class="settings-menu-card">
          <div class="settings-menu-title">店铺设置</div>
          <el-menu
            :default-active="activeSection"
            @select="handleSectionSelect">
            <el-menu-item
              v-for="item in menuItems"
              :key="item.name"
              :index="item.name">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <el-col :span="19" :xs="24">
        <el-card
          v-loading="loading"
          shadow="never"
          class="settings-content-card">
          <section v-show="activeSection === 'basic'" class="settings-section">
            <div class="section-heading">
              <div>
                <h2>店铺信息</h2>
                <p>
                  维护店铺对外展示的基础资料，认证主体信息由平台审核流程维护。
                </p>
              </div>
              <el-button
                v-if="page.canEdit"
                type="primary"
                :loading="savingProfile"
                @click="saveProfile">
                保存基本信息
              </el-button>
            </div>

            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-position="top"
              :disabled="!page.canEdit">
              <div class="form-grid">
                <el-form-item label="店铺名称" prop="nickname" class="span-2">
                  <el-input
                    v-model="profileForm.nickname"
                    maxlength="64"
                    show-word-limit
                    placeholder="请输入店铺名称" />
                </el-form-item>
                <el-form-item label="联系人">
                  <el-input
                    v-model="profileForm.contactName"
                    maxlength="64"
                    placeholder="请输入联系人" />
                </el-form-item>
                <el-form-item label="客服电话">
                  <el-input
                    v-model="profileForm.servicePhone"
                    maxlength="32"
                    placeholder="请输入客服电话" />
                </el-form-item>
                <el-form-item label="联系邮箱">
                  <el-input
                    v-model="profileForm.contactEmail"
                    maxlength="128"
                    placeholder="请输入联系邮箱" />
                </el-form-item>
                <el-form-item label="支付宝实名">
                  <el-input
                    v-model="profileForm.alipayRealName"
                    maxlength="64"
                    placeholder="请输入支付宝实名" />
                </el-form-item>
                <el-form-item label="支付宝账号">
                  <el-input
                    v-model="profileForm.alipayAccount"
                    maxlength="128"
                    placeholder="请输入支付宝账号" />
                </el-form-item>
                <el-form-item label="对外接单">
                  <el-switch
                    v-model="profileForm.externalAcceptEnabled"
                    active-text="允许"
                    inactive-text="关闭" />
                </el-form-item>
                <el-form-item label="店铺 Logo" class="span-2">
                  <image-upload
                    v-model="profileForm.storeLogoUrl"
                    action="/merchant/store/settings/logo"
                    :limit="1"
                    :disabled="!page.canEdit"
                    @update:model-value="handleLogoChange" />
                </el-form-item>
                <el-form-item label="店铺描述" class="span-2">
                  <el-input
                    v-model="profileForm.storeDescription"
                    type="textarea"
                    :rows="4"
                    maxlength="1000"
                    show-word-limit
                    placeholder="请输入店铺描述" />
                </el-form-item>
              </div>
            </el-form>

            <el-divider />
            <div class="subsection-heading">认证资料</div>
            <el-descriptions :column="2" border label-width="120">
              <el-descriptions-item label="主体名称">
                {{ page.profile.legalSubjectName || "-" }}
              </el-descriptions-item>
              <el-descriptions-item label="营业执照号">
                {{ page.profile.licenseNo || "-" }}
              </el-descriptions-item>
              <el-descriptions-item label="法人姓名">
                {{ page.profile.legalRepresentativeName || "-" }}
              </el-descriptions-item>
              <el-descriptions-item label="注册资本">
                {{ page.profile.registeredCapital || "-" }}
              </el-descriptions-item>
              <el-descriptions-item label="执照有效期">
                {{ page.profile.licenseValidFrom || "-" }} 至
                {{
                  page.profile.licenseLongTerm
                    ? "长期"
                    : page.profile.licenseValidTo || "-"
                }}
              </el-descriptions-item>
              <el-descriptions-item label="执照地址">
                {{ page.profile.licenseAddress || "-" }}
              </el-descriptions-item>
              <el-descriptions-item label="经营范围" :span="2">
                {{ page.profile.businessScope || "-" }}
              </el-descriptions-item>
            </el-descriptions>
          </section>

          <section v-show="activeSection === 'order'" class="settings-section">
            <div class="section-heading">
              <div>
                <h2>订单与复制</h2>
                <p>
                  设置订单列表的默认展示方式，以及复制订单时带入的业务字段。
                </p>
              </div>
              <el-button
                v-if="page.canEdit"
                type="primary"
                :loading="savingSettings"
                @click="saveSettings">
                保存订单设置
              </el-button>
            </div>
            <el-form
              :model="settingsForm"
              label-position="top"
              :disabled="!page.canEdit">
              <el-form-item label="订单列表默认展示">
                <el-radio-group v-model="settingsForm.orderViewMode">
                  <el-radio-button label="CARD">卡片模式</el-radio-button>
                  <el-radio-button label="COMPACT">精简模式</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="订单复制带入字段">
                <el-checkbox-group
                  v-model="settingsForm.orderCopyFields"
                  class="copy-fields-grid">
                  <el-checkbox
                    v-for="item in orderCopyFieldOptions"
                    :key="item.value"
                    :label="item.value">
                    {{ item.label }}
                  </el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-form>
          </section>

          <section
            v-show="activeSection === 'inventory'"
            class="settings-section">
            <div class="section-heading">
              <div>
                <h2>库存提醒</h2>
                <p>设置库存保护数量与低库存提醒阈值。</p>
              </div>
              <el-button
                v-if="page.canEdit"
                type="primary"
                :loading="savingSettings"
                @click="saveSettings">
                保存库存设置
              </el-button>
            </div>
            <el-form
              :model="settingsForm"
              label-position="top"
              :disabled="!page.canEdit">
              <div class="form-grid">
                <el-form-item label="库存保护数量">
                  <el-input-number
                    v-model="settingsForm.protectedQuantity"
                    :min="0"
                    :max="1000000"
                    controls-position="right" />
                  <span class="form-unit">件</span>
                </el-form-item>
                <el-form-item label="库存提醒方式">
                  <el-radio-group v-model="settingsForm.inventoryAlertType">
                    <el-radio label="COUNT">按数量</el-radio>
                    <el-radio label="PERCENT">按占用率</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item
                  :label="
                    settingsForm.inventoryAlertType === 'PERCENT'
                      ? '库存提醒占用率'
                      : '库存提醒数量'
                  ">
                  <el-input-number
                    v-model="settingsForm.inventoryAlertValue"
                    :min="0"
                    :max="
                      settingsForm.inventoryAlertType === 'PERCENT'
                        ? 100
                        : 1000000
                    "
                    controls-position="right" />
                  <span class="form-unit">
                    {{
                      settingsForm.inventoryAlertType === "PERCENT" ? "%" : "件"
                    }}
                  </span>
                </el-form-item>
              </div>
            </el-form>
          </section>

          <section
            v-show="activeSection === 'delivery'"
            class="settings-section">
            <div class="section-heading">
              <div>
                <h2>配送与履约</h2>
                <p>
                  设置新增订单时的默认发货时段与配送方式，订单保存后的快照不受后续设置影响。
                </p>
              </div>
              <el-button
                v-if="page.canEdit"
                type="primary"
                :loading="savingSettings"
                @click="saveSettings">
                保存履约设置
              </el-button>
            </div>
            <el-form
              :model="settingsForm"
              label-position="top"
              :disabled="!page.canEdit">
              <div class="form-grid">
                <el-form-item label="默认发货时段">
                  <el-select
                    v-model="settingsForm.defaultShipTimeSlot"
                    class="field-control">
                    <el-option
                      v-for="item in shipTimeSlotOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value" />
                  </el-select>
                </el-form-item>
                <el-form-item label="默认配送方式">
                  <el-select
                    v-model="settingsForm.defaultDeliveryMethod"
                    class="field-control">
                    <el-option
                      v-for="item in deliveryMethodOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value" />
                  </el-select>
                </el-form-item>
                <el-form-item label="视频自动清理">
                  <el-switch
                    v-model="settingsForm.videoAutoDeleteEnabled"
                    active-text="开启"
                    inactive-text="关闭" />
                </el-form-item>
                <el-form-item label="视频保留天数">
                  <el-input-number
                    v-model="settingsForm.videoRetentionDays"
                    :min="1"
                    :max="3650"
                    controls-position="right" />
                  <span class="form-unit">天</span>
                </el-form-item>
              </div>
            </el-form>
          </section>

          <section
            v-show="activeSection === 'display'"
            class="settings-section">
            <div class="section-heading">
              <div>
                <h2>展示设置</h2>
                <p>
                  设置趋势页面默认展示天数，以及公开页面和订单协作页面的信息范围。
                </p>
              </div>
              <el-button
                v-if="page.canEdit"
                type="primary"
                :loading="savingSettings"
                @click="saveSettings">
                保存展示设置
              </el-button>
            </div>
            <el-form
              :model="settingsForm"
              label-position="top"
              :disabled="!page.canEdit">
              <div class="form-grid">
                <el-form-item label="收发趋势默认显示天数">
                  <el-select
                    v-model="settingsForm.inboundOutboundTrendDays"
                    class="field-control">
                    <el-option
                      v-for="days in displayDayOptions"
                      :key="days"
                      :label="`${days} 天`"
                      :value="days" />
                  </el-select>
                </el-form-item>
                <el-form-item label="库存概况默认显示天数">
                  <el-select
                    v-model="settingsForm.inventoryOverviewDays"
                    class="field-control">
                    <el-option
                      v-for="days in displayDayOptions"
                      :key="days"
                      :label="`${days} 天`"
                      :value="days" />
                  </el-select>
                </el-form-item>
              </div>
              <div class="switch-list">
                <div class="switch-row">
                  <div>
                    <strong>显示店铺联系方式</strong>
                    <p>公开店铺和协助订单页面显示客服电话。</p>
                  </div>
                  <el-switch v-model="settingsForm.contactVisible" />
                </div>
                <div class="switch-row">
                  <div>
                    <strong>允许免押租赁</strong>
                    <p>仅作为业务默认偏好，实际是否免押仍以订单规则为准。</p>
                  </div>
                  <el-switch v-model="settingsForm.depositFreeEnabled" />
                </div>
                <div class="switch-row">
                  <div>
                    <strong>展示租商联盟店铺</strong>
                    <p>允许联盟协作页面展示当前店铺名称和可履约范围。</p>
                  </div>
                  <el-switch v-model="settingsForm.allianceStoreVisible" />
                </div>
              </div>
            </el-form>
          </section>

          <section
            v-show="activeSection === 'qualification'"
            class="settings-section">
            <div class="section-heading">
              <div>
                <h2>接单资格与认证历史</h2>
                <p>这里展示平台审核结果，商户端不能直接修改认证状态。</p>
              </div>
              <el-button text :icon="Refresh" @click="refreshQualification">
                刷新
              </el-button>
            </div>
            <el-table :data="certification.qualifications || []" size="small">
              <el-table-column label="资格" prop="qualificationCode">
                <template #default="scope">
                  {{ qualificationLabel(scope.row.qualificationCode) }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="scope">
                  <el-tag
                    :type="
                      getQualificationStatusTagType(
                        scope.row.qualificationStatus,
                      )
                    ">
                    {{
                      getQualificationStatusLabel(scope.row.qualificationStatus)
                    }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="审核时间" width="190">
                <template #default="scope">
                  {{ formatTime(scope.row.reviewedAt) }}
                </template>
              </el-table-column>
              <el-table-column
                label="审核意见"
                prop="reviewRemark"
                min-width="220" />
            </el-table>
            <el-empty
              v-if="!certification.qualifications?.length"
              description="暂无接单资格"
              :image-size="60" />
            <section class="credit-panel">
              <div class="subsection-heading credit-heading">
                <div>
                  <h3>信用与申诉</h3>
                  <p>信用分只由平台治理，存在异议的扣分流水可提交一次申诉。</p>
                </div>
                <div class="credit-summary">
                  <strong>{{ creditRisk.creditScore ?? 100 }}</strong>
                  <span>{{ creditRisk.creditLevelName || "正常" }}</span>
                </div>
              </div>
              <el-table
                v-loading="riskLoading"
                :data="creditRisk.creditRecords || []"
                size="small"
                empty-text="暂无信用流水">
                <el-table-column label="流水编码" prop="recordNo" min-width="220" />
                <el-table-column label="事件" prop="eventTypeName" width="120" />
                <el-table-column label="分值变化" width="100" align="center">
                  <template #default="scope">
                    <span :class="scoreChangeClass(scope.row.scoreChange)">
                      {{ formatScoreChange(scope.row.scoreChange) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="原因" prop="reason" min-width="220" show-overflow-tooltip />
                <el-table-column label="时间" prop="createTime" width="170" />
                <el-table-column label="操作" width="100" align="center">
                  <template #default="scope">
                    <el-button
                      v-if="canAppealCreditRecord(scope.row)"
                      link
                      type="warning"
                      @click="openAppealDialog(scope.row)">
                      申诉
                    </el-button>
                    <span v-else-if="scope.row.reversalOfRecordNo">冲正流水</span>
                    <span v-else-if="scope.row.reversalRecordNo">已申诉</span>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty
                v-if="!riskLoading && !creditRisk.creditRecords?.length"
                description="暂无信用流水"
                :image-size="60" />
            </section>
            <section v-if="supplementRequired" class="supplement-panel">
              <el-alert
                title="平台要求补交认证材料"
                :description="
                  latestCertification?.reviewRemark ||
                  '请按要求补交材料，提交后等待平台重新审核。'
                "
                type="warning"
                show-icon
                :closable="false" />
              <el-form label-position="top" class="supplement-form">
                <el-form-item
                  v-for="materialType in supplementMaterialTypes"
                  :key="materialType"
                  :label="certificationMaterialLabel(materialType)"
                  required>
                  <image-upload
                    v-if="isImageCertificationMaterial(materialType)"
                    v-model="supplementFiles[materialType]"
                    :limit="1"
                    :is-show-tip="false"
                    business-type="merchant-certification" />
                  <file-upload
                    v-else
                    v-model="supplementFiles[materialType]"
                    :limit="1"
                    :is-show-tip="false"
                    :file-type="['pdf', 'doc', 'docx']"
                    business-type="merchant-certification" />
                </el-form-item>
                <el-button
                  type="primary"
                  :loading="supplementSubmitting"
                  @click="submitSupplement">
                  提交补件
                </el-button>
              </el-form>
            </section>
            <el-divider />
            <div class="subsection-heading">认证历史</div>
            <el-timeline v-if="certification.certificationHistory?.length">
              <el-timeline-item
                v-for="item in certification.certificationHistory"
                :key="item.certificationId">
                <div class="history-title">
                  第 {{ item.versionNo }} 版 ·
                  {{ certificationStatusLabel(item.certificationStatus) }}
                </div>
                <div class="history-meta">
                  提交：{{ formatTime(item.submittedAt) }}　审核：{{
                    formatTime(item.reviewedAt)
                  }}
                </div>
                <div v-if="item.reviewRemark" class="history-remark">
                  审核意见：{{ item.reviewRemark }}
                </div>
                <div class="material-list">
                  <a
                    v-for="material in item.materials || []"
                    :key="material.materialType"
                    :href="material.fileUrl"
                    target="_blank"
                    rel="noopener">
                    {{ material.materialName }}
                  </a>
                </div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无认证历史" :image-size="60" />
          </section>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="appealVisible"
      title="提交信用申诉"
      width="520px"
      destroy-on-close>
      <el-form
        ref="appealFormRef"
        :model="appealForm"
        :rules="appealRules"
        label-width="90px">
        <el-form-item label="申诉流水">
          <el-input v-model="appealForm.recordNo" disabled />
        </el-form-item>
        <el-form-item label="申诉理由" prop="reason">
          <el-input
            v-model="appealForm.reason"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="请说明该扣分流水存在异议的原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="appealSubmitting"
          @click="submitCreditAppeal">
          提交申诉
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {
  Refresh,
  Setting,
  ShoppingCart,
  Box,
  Van,
  View,
  CircleCheck,
} from "@element-plus/icons-vue";
import {
  getMerchantCertification,
  getMerchantCreditRisk,
  submitMerchantCreditAppeal,
  submitMerchantCertificationSupplement,
} from "@/api/merchant/certification";
import {
  getMerchantStoreSettings,
  removeMerchantStoreLogo,
  updateMerchantStorePreferences,
  updateMerchantStoreProfile,
} from "@/api/merchant/storeSettings";
import { parseTime } from "@/utils/ruoyi";
import {
  getQualificationStatusLabel,
  getQualificationStatusTagType,
  RENTAL_CODE,
} from "@/utils/rental/options";

const defaultProfile = () => ({
  nickname: "",
  contactName: "",
  servicePhone: "",
  contactEmail: "",
  alipayRealName: "",
  alipayAccount: "",
  storeLogoUrl: "",
  storeDescription: "",
  externalAcceptEnabled: false,
});

const defaultSettings = () => ({
  inboundOutboundTrendDays: 30,
  inventoryOverviewDays: 30,
  inventoryAlertType: "COUNT",
  inventoryAlertValue: 2,
  protectedQuantity: 0,
  orderCopyFields: [
    "customer",
    "recipient",
    "package",
    "lease",
    "warehouse",
    "amount",
    "remark",
  ],
  orderViewMode: "CARD",
  defaultDeliveryMethod: "EXPRESS",
  defaultShipTimeSlot: "MORNING",
  contactVisible: true,
  depositFreeEnabled: false,
  allianceStoreVisible: true,
  videoAutoDeleteEnabled: false,
  videoRetentionDays: 15,
});

const page = reactive({
  profile: {
    authStatus: RENTAL_CODE.AUTH_STATUS.PENDING,
    membershipType: null,
    vipLevelName: "",
    status: 0,
    nickname: "",
    contactName: "",
    servicePhone: "",
    storageQuotaMb: 0,
    externalAcceptEnabled: false,
    wechatQrConfigured: false,
  },
  settings: defaultSettings(),
  canEdit: false,
});
const profileForm = reactive(defaultProfile());
const settingsForm = reactive(defaultSettings());
const certification = reactive({
  qualifications: [],
  certificationHistory: [],
});
const creditRisk = reactive({
  creditScore: 100,
  creditLevelName: "正常",
  creditRecords: [],
});
const supplementFiles = reactive({});
const supplementSubmitting = ref(false);
const riskLoading = ref(false);
const appealVisible = ref(false);
const appealSubmitting = ref(false);
const appealFormRef = ref();
const appealForm = reactive({ recordNo: "", reason: "" });
const { proxy } = getCurrentInstance();
const profileFormRef = ref();
const activeSection = ref("basic");
const loading = ref(false);
const savingProfile = ref(false);
const savingSettings = ref(false);
const logoLoaded = ref(false);

const menuItems = [
  { name: "basic", label: "店铺信息", icon: Setting },
  { name: "order", label: "订单设置", icon: ShoppingCart },
  { name: "inventory", label: "库存提醒", icon: Box },
  { name: "delivery", label: "配送与履约", icon: Van },
  { name: "display", label: "展示设置", icon: View },
  { name: "qualification", label: "认证与接单资格", icon: CircleCheck },
];

const orderCopyFieldOptions = [
  { label: "客户信息", value: "customer" },
  { label: "收件信息", value: "recipient" },
  { label: "产品套餐", value: "package" },
  { label: "租期信息", value: "lease" },
  { label: "仓库信息", value: "warehouse" },
  { label: "费用信息", value: "amount" },
  { label: "订单备注", value: "remark" },
];

const shipTimeSlotOptions = [
  { label: "上午", value: "MORNING" },
  { label: "下午", value: "AFTERNOON" },
  { label: "晚上", value: "EVENING" },
];

const deliveryMethodOptions = [
  { label: "快递", value: "EXPRESS" },
  { label: "闪送", value: "FLASH" },
  { label: "自提", value: "SELF_PICKUP" },
  { label: "上门", value: "DOOR_TO_DOOR" },
];

const displayDayOptions = [3, 5, 7, 15, 30];

const certificationMaterialOptions = [
  { value: 1, label: "营业执照", image: true },
  { value: 2, label: "身份证人像面", image: true },
  { value: 3, label: "身份证国徽面", image: true },
  { value: 4, label: "签约合同", image: false },
  { value: 9, label: "其他认证材料", image: false },
];

const profileRules = {
  nickname: [{ required: true, message: "请输入店铺名称", trigger: "blur" }],
};

const appealRules = {
  reason: [
    { required: true, message: "请输入申诉理由", trigger: "blur" },
    { min: 5, max: 1000, message: "申诉理由长度为5至1000个字符", trigger: "blur" },
  ],
};

const storeInitial = computed(() =>
  (page.profile.nickname || "店").slice(0, 1),
);

function handleSectionSelect(section) {
  activeSection.value = section;
}

async function loadPage() {
  loading.value = true;
  try {
    const response = await getMerchantStoreSettings();
    const data = response.data || {};
    page.profile = { ...page.profile, ...(data.profile || {}) };
    page.settings = { ...defaultSettings(), ...(data.settings || {}) };
    page.canEdit = data.canEdit === true;
    Object.assign(profileForm, defaultProfile(), {
      nickname: page.profile.nickname || "",
      contactName: page.profile.contactName || "",
      servicePhone: page.profile.servicePhone || "",
      contactEmail: page.profile.contactEmail || "",
      alipayRealName: page.profile.alipayRealName || "",
      alipayAccount: page.profile.alipayAccount || "",
      storeLogoUrl: page.profile.storeLogoUrl || "",
      storeDescription: page.profile.storeDescription || "",
      externalAcceptEnabled: page.profile.externalAcceptEnabled === true,
    });
    Object.assign(settingsForm, defaultSettings(), page.settings);
    logoLoaded.value = true;
  } finally {
    loading.value = false;
  }
  await loadCertification();
  await loadCreditRisk();
}

async function loadCertification() {
  const response = await getMerchantCertification();
  Object.assign(certification, response.data || {});
  resetSupplementFiles();
}

async function loadCreditRisk() {
  riskLoading.value = true;
  try {
    const response = await getMerchantCreditRisk();
    Object.assign(creditRisk, {
      creditScore: 100,
      creditLevelName: "正常",
      creditRecords: [],
      ...(response.data || {}),
    });
  } finally {
    riskLoading.value = false;
  }
}

async function refreshQualification() {
  await Promise.all([loadCertification(), loadCreditRisk()]);
}

const latestCertification = computed(
  () => certification.certificationHistory?.[0] || null,
);
const supplementMaterialTypes = computed(
  () => latestCertification.value?.supplementMaterialTypes || [],
);
const supplementRequired = computed(
  () =>
    latestCertification.value?.certificationStatus ===
    RENTAL_CODE.CERTIFICATION_STATUS.SUPPLEMENT_REQUIRED,
);

function certificationMaterialLabel(type) {
  return (
    certificationMaterialOptions.find((item) => item.value === type)?.label ||
    "认证材料"
  );
}

function isImageCertificationMaterial(type) {
  return certificationMaterialOptions.find((item) => item.value === type)
    ?.image;
}

function resetSupplementFiles() {
  Object.keys(supplementFiles).forEach((type) => delete supplementFiles[type]);
  supplementMaterialTypes.value.forEach((type) => {
    supplementFiles[type] = "";
  });
}

async function submitSupplement() {
  const missingType = supplementMaterialTypes.value.find(
    (type) => !supplementFiles[type],
  );
  if (missingType) {
    proxy.$modal.msgError(
      `请先上传${certificationMaterialLabel(missingType)}，再提交补件`,
    );
    return;
  }
  supplementSubmitting.value = true;
  try {
    const response = await submitMerchantCertificationSupplement({
      materials: supplementMaterialTypes.value.map((materialType) => ({
        materialType,
        fileUrl: supplementFiles[materialType],
      })),
    });
    Object.assign(certification, response.data || {});
    resetSupplementFiles();
    proxy.$modal.msgSuccess("补件材料已提交，等待平台重新审核");
  } finally {
    supplementSubmitting.value = false;
  }
}

function canAppealCreditRecord(record) {
  return (
    Number(record?.scoreChange) < 0 &&
    !record?.reversalOfRecordNo &&
    !record?.reversalRecordNo
  );
}

function formatScoreChange(value) {
  const number = Number(value || 0);
  return number > 0 ? `+${number}` : String(number);
}

function scoreChangeClass(value) {
  return Number(value) < 0 ? "score-negative" : "score-positive";
}

function openAppealDialog(record) {
  Object.assign(appealForm, { recordNo: record.recordNo || "", reason: "" });
  appealVisible.value = true;
  nextTick(() => appealFormRef.value?.clearValidate());
}

async function submitCreditAppeal() {
  if (!appealFormRef.value || !(await appealFormRef.value.validate().catch(() => false))) {
    return;
  }
  appealSubmitting.value = true;
  try {
    const response = await submitMerchantCreditAppeal({ ...appealForm });
    appealVisible.value = false;
    proxy.$modal.msgSuccess(
      `申诉已提交，工单号：${response.data?.ticketNo || "已生成"}`,
    );
    await loadCreditRisk();
  } finally {
    appealSubmitting.value = false;
  }
}

async function saveProfile() {
  if (!profileFormRef.value) return;
  await profileFormRef.value.validate();
  savingProfile.value = true;
  try {
    const response = await updateMerchantStoreProfile({ ...profileForm });
    page.profile = { ...page.profile, ...(response.data || {}) };
    profileForm.storeLogoUrl =
      page.profile.storeLogoUrl || profileForm.storeLogoUrl;
    proxy.$modal.msgSuccess("店铺基本信息已保存");
  } finally {
    savingProfile.value = false;
  }
}

async function saveSettings() {
  savingSettings.value = true;
  try {
    const response = await updateMerchantStorePreferences({ ...settingsForm });
    page.settings = { ...defaultSettings(), ...(response.data || {}) };
    Object.assign(settingsForm, page.settings);
    proxy.$modal.msgSuccess("店铺设置已保存");
  } finally {
    savingSettings.value = false;
  }
}

async function handleLogoChange(value) {
  profileForm.storeLogoUrl = value || "";
  if (logoLoaded.value && !value && page.canEdit) {
    await removeMerchantStoreLogo();
    page.profile.storeLogoUrl = "";
  }
}

function statusLabel(value) {
  return (
    {
      [RENTAL_CODE.AUTH_STATUS.UNVERIFIED]: "未认证",
      [RENTAL_CODE.AUTH_STATUS.PAYMENT_PENDING]: "待支付",
      [RENTAL_CODE.AUTH_STATUS.PENDING]: "待审核",
      [RENTAL_CODE.AUTH_STATUS.REJECTED]: "审核驳回",
      [RENTAL_CODE.AUTH_STATUS.VERIFIED]: "审核通过",
    }[value] || "-"
  );
}

function statusType(value) {
  return (
    {
      [RENTAL_CODE.AUTH_STATUS.UNVERIFIED]: "info",
      [RENTAL_CODE.AUTH_STATUS.PAYMENT_PENDING]: "warning",
      [RENTAL_CODE.AUTH_STATUS.PENDING]: "warning",
      [RENTAL_CODE.AUTH_STATUS.REJECTED]: "danger",
      [RENTAL_CODE.AUTH_STATUS.VERIFIED]: "success",
    }[value] || "info"
  );
}

function certificationStatusLabel(value) {
  return (
    {
      [RENTAL_CODE.CERTIFICATION_STATUS.PENDING]: "待审核",
      [RENTAL_CODE.CERTIFICATION_STATUS.APPROVED]: "审核通过",
      [RENTAL_CODE.CERTIFICATION_STATUS.REJECTED]: "审核驳回",
      [RENTAL_CODE.CERTIFICATION_STATUS.EXPIRED]: "已过期",
      [RENTAL_CODE.CERTIFICATION_STATUS.SUPPLEMENT_REQUIRED]: "待补件",
    }[value] || "-"
  );
}

function membershipLabel(value) {
  return (
    {
      [RENTAL_CODE.MEMBERSHIP_TYPE.PERSONAL_MEMBER]: "个人会员",
      [RENTAL_CODE.MEMBERSHIP_TYPE.ORDINARY_ENTERPRISE_MEMBER]: "普通企业会员",
      [RENTAL_CODE.MEMBERSHIP_TYPE.SETTLED_MEMBER]: "入驻会员",
    }[value] || "-"
  );
}

function qualificationLabel(value) {
  return (
    {
      external_accept: "对外接单",
      alliance_accept: "租商联盟接单",
      mini_program_accept: "小程序接单",
    }[value] ||
    value ||
    "-"
  );
}

function formatTime(value) {
  return value ? parseTime(value) : "-";
}

loadPage();
</script>

<style scoped>
.merchant-store-settings-page {
  padding: 20px;
}

.store-summary-card,
.settings-menu-card,
.settings-content-card {
  border: 1px solid var(--el-border-color-lighter);
}

.store-summary-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.store-identity,
.store-summary-actions,
.section-heading,
.switch-row {
  display: flex;
  align-items: center;
}

.store-identity {
  gap: 14px;
}

.store-name {
  color: var(--el-text-color-primary);
  font-size: 22px;
  font-weight: 600;
}

.store-subtitle,
.section-heading p,
.switch-row p {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.store-summary-actions {
  flex-wrap: wrap;
  gap: 8px;
}

.store-summary-descriptions :deep(.el-descriptions__label) {
  width: 110px;
}

.settings-layout {
  margin-top: 16px;
}

.settings-menu-card {
  min-height: 620px;
}

.settings-menu-title {
  padding: 4px 18px 14px;
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 600;
}

.settings-menu-card :deep(.el-menu) {
  border-right: 0;
}

.settings-content-card {
  min-height: 620px;
}

.settings-section {
  min-height: 580px;
}

.section-heading {
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.section-heading h2 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 20px;
}

.section-heading p {
  margin-top: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 28px;
}

.span-2 {
  grid-column: span 2;
}

.field-control {
  width: 100%;
}

.form-unit {
  margin-left: 8px;
  color: var(--el-text-color-secondary);
}

.copy-fields-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px 20px;
}

.switch-list {
  max-width: 860px;
}

.switch-row {
  justify-content: space-between;
  gap: 24px;
  min-height: 72px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.switch-row strong {
  color: var(--el-text-color-primary);
  font-size: 15px;
}

.subsection-heading {
  margin-bottom: 14px;
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 600;
}

.credit-panel {
  margin: 24px 0 18px;
  padding-top: 18px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.credit-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.credit-heading h3,
.credit-heading p {
  margin: 0;
}

.credit-heading p {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  font-weight: 400;
}

.credit-summary {
  display: flex;
  align-items: baseline;
  gap: 8px;
  white-space: nowrap;
}

.credit-summary strong {
  color: var(--el-color-primary);
  font-size: 26px;
}

.credit-summary span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  font-weight: 400;
}

.score-negative {
  color: var(--el-color-danger);
  font-weight: 600;
}

.score-positive {
  color: var(--el-color-success);
  font-weight: 600;
}

.history-title {
  font-weight: 600;
}

.history-meta,
.history-remark {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
}

.material-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
}

.supplement-panel {
  margin: 18px 0;
  padding: 16px;
  border: 1px solid var(--el-color-warning-light-5);
  background: var(--el-color-warning-light-9);
}

.supplement-form {
  margin-top: 16px;
}

.supplement-form :deep(.component-upload-image),
.supplement-form :deep(.upload-file) {
  min-height: 40px;
}

@media (max-width: 992px) {
  .settings-menu-card {
    min-height: auto;
    margin-bottom: 16px;
  }

  .settings-content-card,
  .settings-section {
    min-height: auto;
  }
}

@media (max-width: 768px) {
  .merchant-store-settings-page {
    padding: 12px;
  }

  .store-summary-header,
  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .form-grid,
  .copy-fields-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: auto;
  }
}
</style>
