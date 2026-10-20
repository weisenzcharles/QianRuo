<template>
  <el-drawer v-model="visible" title="商户详情" size="min(900px, 94vw)">
    <template v-if="member">
      <section class="detail-section">
        <h3>店铺信息</h3>
        <el-descriptions :column="detailColumns" border label-width="120">
          <el-descriptions-item label="商户编码">
            {{ display(member.memberCode) }}
          </el-descriptions-item>
          <el-descriptions-item label="店铺名称">
            {{ display(member.nickname) }}
          </el-descriptions-item>
          <el-descriptions-item label="店铺账号">
            {{ display(member.mainAccount) }}
          </el-descriptions-item>
          <el-descriptions-item label="商户类型">
            {{ optionLabel(rentalMemberTypeOptions, member.memberType) }}
          </el-descriptions-item>
          <el-descriptions-item label="联系人">
            {{ display(member.contactName) }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ display(member.contactPhone) }}
          </el-descriptions-item>
          <el-descriptions-item label="客服电话">
            {{ display(member.servicePhone) }}
          </el-descriptions-item>
          <el-descriptions-item label="联系邮箱">
            {{ display(member.contactEmail) }}
          </el-descriptions-item>
          <el-descriptions-item label="支付宝实名">
            {{ display(member.alipayRealName) }}
          </el-descriptions-item>
          <el-descriptions-item label="支付宝账号">
            {{ display(member.alipayAccount) }}
          </el-descriptions-item>
          <el-descriptions-item label="会员等级">
            {{ optionLabel(rentalVipLevelOptions, member.vipLevel) }}
          </el-descriptions-item>
          <el-descriptions-item label="存储额度">
            {{ formatQuota(member.storageQuotaMb) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(member.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="对外接单">
            {{
              member.externalAcceptEnabled === RENTAL_CODE.YES_NO.YES
                ? "允许"
                : "禁止"
            }}
          </el-descriptions-item>
          <el-descriptions-item label="店铺描述" :span="2">
            {{ display(member.storeDescription) }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="member.storeLogoUrl"
            label="店铺 Logo"
            :span="2">
            <image-upload
              :model-value="member.storeLogoUrl"
              :limit="1"
              disabled
              :is-show-tip="false" />
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section
        v-if="member.memberType === RENTAL_CODE.MEMBER_TYPE.ENTERPRISE"
        class="detail-section">
        <h3>企业信息</h3>
        <el-descriptions :column="detailColumns" border label-width="120">
          <el-descriptions-item label="企业名称">
            {{ display(member.legalSubjectName) }}
          </el-descriptions-item>
          <el-descriptions-item label="营业执照号">
            {{ display(member.licenseNo) }}
          </el-descriptions-item>
          <el-descriptions-item label="注册资本">
            {{ formatCapital(member.registeredCapital) }}
          </el-descriptions-item>
          <el-descriptions-item label="法人姓名">
            {{ display(member.legalRepresentativeName) }}
          </el-descriptions-item>
          <el-descriptions-item label="法人身份证号" :span="2">
            {{ display(member.legalRepresentativeIdNo) }}
          </el-descriptions-item>
          <el-descriptions-item label="执照地址" :span="2">
            {{ display(member.licenseAddress) }}
          </el-descriptions-item>
          <el-descriptions-item label="执照有效期" :span="2">
            {{ licensePeriod }}
          </el-descriptions-item>
          <el-descriptions-item label="经营范围" :span="2">
            {{ display(member.businessScope) }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="member.licenseImageUrl"
            label="营业执照"
            :span="2">
            <image-upload
              :model-value="member.licenseImageUrl"
              :limit="1"
              disabled
              :is-show-tip="false" />
          </el-descriptions-item>
          <el-descriptions-item
            v-if="member.legalIdFrontUrl || member.legalIdBackUrl"
            label="法人身份证"
            :span="2">
            <div class="identity-images">
              <image-upload
                v-if="member.legalIdFrontUrl"
                :model-value="member.legalIdFrontUrl"
                :limit="1"
                disabled
                :is-show-tip="false" />
              <image-upload
                v-if="member.legalIdBackUrl"
                :model-value="member.legalIdBackUrl"
                :limit="1"
                disabled
                :is-show-tip="false" />
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section v-else class="detail-section">
        <h3>个人信息</h3>
        <el-descriptions :column="detailColumns" border label-width="120">
          <el-descriptions-item label="个人姓名">
            {{ display(member.contactName || member.legalRepresentativeName) }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ display(member.contactPhone) }}
          </el-descriptions-item>
          <el-descriptions-item label="身份证号" :span="2">
            {{ display(member.legalRepresentativeIdNo) }}
          </el-descriptions-item>
          <el-descriptions-item label="身份证照片" :span="2">
            <div
              v-if="member.legalIdFrontUrl || member.legalIdBackUrl"
              class="identity-images">
              <div v-if="member.legalIdFrontUrl" class="identity-image-item">
                <span>身份证人像面</span>
                <image-upload
                  :model-value="member.legalIdFrontUrl"
                  :limit="1"
                  disabled
                  :is-show-tip="false" />
              </div>
              <div v-if="member.legalIdBackUrl" class="identity-image-item">
                <span>身份证国徽面</span>
                <image-upload
                  :model-value="member.legalIdBackUrl"
                  :limit="1"
                  disabled
                  :is-show-tip="false" />
              </div>
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section class="detail-section">
        <h3>签约信息</h3>
        <el-descriptions :column="detailColumns" border label-width="120">
          <el-descriptions-item label="签约日期">
            {{ formatDate(member.signingDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="合同有效期">
            {{ contractPeriod }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="member.contractFileUrl"
            label="签约合同"
            :span="2">
            <file-upload
              :model-value="member.contractFileUrl"
              :limit="1"
              disabled
              :is-show-tip="false" />
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section class="detail-section">
        <h3>审核信息</h3>
        <el-descriptions :column="detailColumns" border label-width="120">
          <el-descriptions-item label="认证状态">
            {{ optionLabel(rentalAuthStatusOptions, member.authStatus) }}
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ formatDateTime(member.auditTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="审核备注" :span="2">
            {{ display(member.auditRemark) }}
          </el-descriptions-item>
          <el-descriptions-item label="冻结状态">
            <el-tag
              :type="
                member.status === NORMAL_DISABLE_CODE.DISABLED
                  ? 'danger'
                  : 'success'
              ">
              {{
                member.status === NORMAL_DISABLE_CODE.DISABLED
                  ? "已冻结"
                  : "未冻结"
              }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="冻结时间">
            {{ formatDateTime(member.freezeTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="冻结原因" :span="2">
            {{ display(member.freezeReason) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ display(member.remark) }}
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section class="detail-section">
        <h3>会员资格</h3>
        <el-descriptions :column="detailColumns" border label-width="120">
          <el-descriptions-item label="会员类型">
            {{
              optionLabel(rentalMembershipTypeOptions, member.membershipType)
            }}
          </el-descriptions-item>
          <el-descriptions-item label="当前接单资格">
            {{
              enabledQualifications.length
                ? enabledQualifications
                    .map((item) => qualificationLabel(item.qualificationCode))
                    .join("、")
                : "暂无"
            }}
          </el-descriptions-item>
        </el-descriptions>
        <el-table
          v-if="member.qualifications?.length"
          :data="member.qualifications"
          size="small"
          class="history-table">
          <el-table-column label="资格" min-width="150">
            <template #default="scope">
              {{ qualificationLabel(scope.row.qualificationCode) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag
                :type="
                  getRentalQualificationStatusTagType(
                    scope.row.qualificationStatus,
                  )
                ">
                {{
                  getRentalQualificationStatusLabel(
                    scope.row.qualificationStatus,
                  )
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核人" prop="reviewedBy" width="130" />
          <el-table-column label="审核时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.reviewedTime) }}
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="detail-section">
        <h3>认证历史</h3>
        <el-timeline v-if="member.certificationHistory?.length">
          <el-timeline-item
            v-for="item in member.certificationHistory"
            :key="item.certificationId">
            <div class="history-title">
              第 {{ item.versionNo }} 版 ·
              {{ certificationStatusLabel(item.certificationStatus) }}
            </div>
            <div class="history-meta">
              提交时间：{{ formatDateTime(item.submittedTime) }}　审核时间：{{
                formatDateTime(item.reviewedTime)
              }}
            </div>
            <div v-if="item.reviewRemark" class="history-remark">
              审核意见：{{ item.reviewRemark }}
            </div>
            <div v-if="item.materials?.length" class="material-list">
              <span
                v-for="material in item.materials"
                :key="material.materialId || material.fileUrl">
                {{ material.materialName }}
              </span>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无认证历史" :image-size="60" />
      </section>

      <section class="detail-section history-grid">
        <div>
          <h3>会员类型变更历史</h3>
          <el-table :data="member.membershipHistory || []" size="small">
            <el-table-column label="变更为" min-width="130">
              <template #default="scope">
                {{
                  optionLabel(
                    rentalMembershipTypeOptions,
                    scope.row.membershipType,
                  )
                }}
              </template>
            </el-table-column>
            <el-table-column label="操作人" prop="operatorName" width="120" />
            <el-table-column label="时间" width="170">
              <template #default="scope">
                {{ formatDateTime(scope.row.operatedTime) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div>
          <h3>接单资格变更历史</h3>
          <el-table :data="member.qualificationHistory || []" size="small">
            <el-table-column label="资格" min-width="130">
              <template #default="scope">
                {{ qualificationLabel(scope.row.qualificationCode) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="scope">
                <el-tag
                  :type="
                    getRentalQualificationStatusTagType(
                      scope.row.qualificationStatus,
                    )
                  ">
                  {{
                    getRentalQualificationStatusLabel(
                      scope.row.qualificationStatus,
                    )
                  }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="170">
              <template #default="scope">
                {{ formatDateTime(scope.row.operatedTime) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </section>

      <section class="detail-section risk-section">
        <div class="section-heading">
          <h3>信用风险</h3>
          <div class="section-actions">
            <el-button
              v-hasPermi="['platform:member:risk:credit']"
              type="primary"
              plain
              size="small"
              @click="openCreditDialog">
              新增信用记录
            </el-button>
            <el-button
              v-hasPermi="['platform:member:risk:blacklist']"
              type="danger"
              plain
              size="small"
              @click="openBlacklistDialog">
              加入黑名单
            </el-button>
          </div>
        </div>

        <el-skeleton v-if="riskLoading" :rows="4" animated />
        <template v-else-if="risk">
          <el-descriptions :column="detailColumns" border label-width="120">
            <el-descriptions-item label="当前信用分">
              {{ risk.creditScore ?? 100 }}
            </el-descriptions-item>
            <el-descriptions-item label="信用等级">
              <el-tag :type="creditLevelTagType(risk.creditLevel)">
                {{ risk.creditLevelName || "正常" }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="黑名单状态">
              <el-tag :type="activeBlacklistCount ? 'danger' : 'success'">
                {{ activeBlacklistCount ? "生效中" : "无" }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="风险说明">
              记录用于平台治理追溯，不会绕过现有认证资格状态机。
            </el-descriptions-item>
          </el-descriptions>

          <el-table
            :data="risk.creditRecords || []"
            size="small"
            border
            class="risk-table">
            <el-table-column label="记录编码" prop="recordNo" min-width="190" />
            <el-table-column label="事件类型" prop="eventTypeName" width="110" />
            <el-table-column label="分值变化" width="100" align="center">
              <template #default="scope">
                <span :class="scoreChangeClass(scope.row.scoreChange)">
                  {{ formatScoreChange(scope.row.scoreChange) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="变更后" prop="scoreAfter" width="80" align="center" />
            <el-table-column label="原因" prop="reason" min-width="180" show-overflow-tooltip />
            <el-table-column label="关联业务" prop="sourceNo" width="150" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.sourceNo || "-" }}</template>
            </el-table-column>
            <el-table-column label="冲正关联" width="190" show-overflow-tooltip>
              <template #default="scope">
                <span v-if="scope.row.reversalOfRecordNo">
                  原流水：{{ scope.row.reversalOfRecordNo }}
                </span>
                <span v-else-if="scope.row.reversalRecordNo">
                  已冲正：{{ scope.row.reversalRecordNo }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="时间" prop="createTime" width="170" />
            <el-table-column label="操作" width="110" fixed="right" align="center">
              <template #default="scope">
                <el-button
                  v-if="!scope.row.reversalOfRecordNo && !scope.row.reversalRecordNo"
                  v-hasPermi="['platform:member:risk:reverse']"
                  link
                  type="warning"
                  @click="openReverseDialog(scope.row)">
                  申诉冲正
                </el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty
            v-if="!risk.creditRecords?.length"
            description="暂无信用流水"
            :image-size="50" />

          <el-table
            :data="risk.blacklistRecords || []"
            size="small"
            border
            class="risk-table">
            <el-table-column label="记录编码" prop="recordNo" min-width="190" />
            <el-table-column label="原因类型" prop="reasonTypeName" width="110" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag
                  :type="Number(scope.row.blacklistStatus) === 1 ? 'danger' : 'info'">
                  {{ scope.row.blacklistStatusName || "未知状态" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="原因" prop="reason" min-width="180" show-overflow-tooltip />
            <el-table-column label="生效时间" prop="effectiveTime" width="170" />
            <el-table-column label="到期时间" width="170">
              <template #default="scope">{{ scope.row.expiresTime || "长期" }}</template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right" align="center">
              <template #default="scope">
                <el-button
                  v-if="Number(scope.row.blacklistStatus) === 1"
                  v-hasPermi="['platform:member:risk:release']"
                  link
                  type="primary"
                  @click="openReleaseDialog(scope.row)">
                  解除
                </el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty
            v-if="!risk.blacklistRecords?.length"
            description="暂无黑名单记录"
            :image-size="50" />
        </template>
        <el-empty v-else description="风险资料加载失败" :image-size="50" />
      </section>

      <el-dialog
        v-model="creditVisible"
        title="新增信用记录"
        width="560px"
        append-to-body
        destroy-on-close>
        <el-form
          ref="creditFormRef"
          :model="creditForm"
          :rules="creditRules"
          label-width="100px">
          <el-form-item label="事件类型" prop="eventType">
            <el-select v-model="creditForm.eventType" style="width: 100%">
              <el-option
                v-for="item in creditEventOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="分值变化" prop="scoreChange">
            <el-input-number
              v-model="creditForm.scoreChange"
              :min="-100"
              :max="100"
              :step="1"
              controls-position="right"
              style="width: 100%" />
          </el-form-item>
          <el-form-item label="变更原因" prop="reason">
            <el-input
              v-model.trim="creditForm.reason"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="请说明本次信用分变更原因" />
          </el-form-item>
          <el-form-item label="关联业务">
            <el-input
              v-model.trim="creditForm.sourceNo"
              maxlength="128"
              placeholder="可填写订单号、售后单号或工单号" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="creditVisible = false">取消</el-button>
          <el-button type="primary" :loading="riskSubmitting" @click="submitCredit">
            保存
          </el-button>
        </template>
      </el-dialog>

      <el-dialog
        v-model="blacklistVisible"
        title="加入黑名单"
        width="560px"
        append-to-body
        destroy-on-close>
        <el-form
          ref="blacklistFormRef"
          :model="blacklistForm"
          :rules="blacklistRules"
          label-width="100px">
          <el-form-item label="原因类型" prop="reasonType">
            <el-select v-model="blacklistForm.reasonType" style="width: 100%">
              <el-option
                v-for="item in blacklistReasonOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="列入原因" prop="reason">
            <el-input
              v-model.trim="blacklistForm.reason"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="请填写列入黑名单的具体原因" />
          </el-form-item>
          <el-form-item label="到期时间">
            <el-date-picker
              v-model="blacklistForm.expiresTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="不填写表示长期"
              style="width: 100%" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="blacklistVisible = false">取消</el-button>
          <el-button type="primary" :loading="riskSubmitting" @click="submitBlacklist">
            保存
          </el-button>
        </template>
      </el-dialog>

      <el-dialog
        v-model="reverseVisible"
        title="申诉冲正信用流水"
        width="560px"
        append-to-body
        destroy-on-close>
        <el-form
          ref="reverseFormRef"
          :model="reverseForm"
          :rules="reverseRules"
          label-width="100px">
          <el-form-item label="原记录编码">
            <span>{{ reverseRecordNo || "-" }}</span>
          </el-form-item>
          <el-form-item label="冲正说明" prop="reason">
            <el-input
              v-model.trim="reverseForm.reason"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="请填写申诉审核通过及信用分恢复原因" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="reverseVisible = false">取消</el-button>
          <el-button type="warning" :loading="riskSubmitting" @click="submitReverse">
            确认冲正
          </el-button>
        </template>
      </el-dialog>

      <el-dialog
        v-model="releaseVisible"
        title="解除黑名单"
        width="520px"
        append-to-body
        destroy-on-close>
        <el-form
          ref="releaseFormRef"
          :model="releaseForm"
          :rules="releaseRules"
          label-width="90px">
          <el-form-item label="记录编码">
            <span>{{ releaseRecordNo || "-" }}</span>
          </el-form-item>
          <el-form-item label="解除说明" prop="remark">
            <el-input
              v-model.trim="releaseForm.remark"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="请填写解除黑名单的原因" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="releaseVisible = false">取消</el-button>
          <el-button type="primary" :loading="riskSubmitting" @click="submitRelease">
            确认解除
          </el-button>
        </template>
      </el-dialog>
    </template>
  </el-drawer>
</template>

<script setup>
import { ElMessage } from "element-plus";
import { computed, reactive, ref, watch } from "vue";
import {
  addMemberBlacklist,
  addMemberCreditRecord,
  getMemberRisk,
  reverseMemberCreditRecord,
  releaseMemberBlacklist,
} from "@/api/rental/member";
import { useDict } from "@/utils/dict";
import { parseTime } from "@/utils/ruoyi";
import {
  getRentalOptionLabel,
  getRentalQualificationStatusLabel,
  getRentalQualificationStatusTagType,
  rentalAuthStatusOptions,
  rentalMemberTypeOptions,
  rentalMembershipTypeOptions,
  rentalQualificationOptions,
  RENTAL_CODE,
} from "@/utils/rental/options";
import { NORMAL_DISABLE_CODE } from "@/utils/stateCode";
import { useWindowSize } from "@vueuse/core";

const visible = defineModel({ type: Boolean, default: false });
const props = defineProps({ member: { type: Object, default: null } });
const { rental_vip_level: rentalVipLevelOptions } = useDict("rental_vip_level");
const { width } = useWindowSize();
const detailColumns = computed(() => (width.value < 760 ? 1 : 2));
const riskLoading = ref(false);
const riskSubmitting = ref(false);
const risk = ref(null);
const creditVisible = ref(false);
const blacklistVisible = ref(false);
const releaseVisible = ref(false);
const reverseVisible = ref(false);
const creditFormRef = ref();
const blacklistFormRef = ref();
const releaseFormRef = ref();
const reverseFormRef = ref();
const releaseRecordNo = ref("");
const reverseRecordNo = ref("");
const creditForm = reactive({
  eventType: 1,
  scoreChange: -1,
  reason: "",
  sourceNo: "",
});
const blacklistForm = reactive({
  reasonType: 9,
  reason: "",
  expiresTime: "",
});
const releaseForm = reactive({ remark: "" });
const reverseForm = reactive({ reason: "" });
const creditEventOptions = [
  { value: 1, label: "人工调整" },
  { value: 2, label: "履约行为" },
  { value: 3, label: "售后纠纷" },
  { value: 4, label: "财务行为" },
  { value: 5, label: "正向行为" },
];
const blacklistReasonOptions = [
  { value: 1, label: "认证风险" },
  { value: 2, label: "履约风险" },
  { value: 3, label: "售后纠纷" },
  { value: 4, label: "财务风险" },
  { value: 9, label: "其他" },
];
const creditRules = {
  eventType: [{ required: true, message: "请选择事件类型", trigger: "change" }],
  scoreChange: [
    { required: true, message: "请输入分值变化", trigger: "change" },
    {
      validator: (_rule, value, callback) =>
        Number(value) === 0
          ? callback(new Error("分值变化不能为0"))
          : callback(),
      trigger: "change",
    },
  ],
  reason: [{ required: true, message: "请输入变更原因", trigger: "blur" }],
};
const blacklistRules = {
  reasonType: [{ required: true, message: "请选择原因类型", trigger: "change" }],
  reason: [{ required: true, message: "请输入列入原因", trigger: "blur" }],
};
const releaseRules = {
  remark: [{ required: true, message: "请输入解除说明", trigger: "blur" }],
};
const reverseRules = {
  reason: [{ required: true, message: "请输入冲正说明", trigger: "blur" }],
};
const activeBlacklistCount = computed(
  () =>
    risk.value?.blacklistRecords?.filter((item) => {
      if (Number(item.blacklistStatus) !== 1) return false;
      if (!item.expiresTime) return true;
      const expiresTime = new Date(String(item.expiresTime).replace(" ", "T"));
      return Number.isNaN(expiresTime.getTime()) || expiresTime.getTime() > Date.now();
    }).length || 0,
);

watch(
  () => [visible.value, props.member?.memberId],
  ([opened, memberId]) => {
    if (opened && memberId) loadRisk(memberId);
  },
  { immediate: true },
);

const licensePeriod = computed(() => {
  if (props.member?.licenseLongTerm === RENTAL_CODE.YES_NO.YES)
    return `${formatDate(props.member.licenseValidFrom)} - 长期`;
  return formatPeriod(
    props.member?.licenseValidFrom,
    props.member?.licenseValidTo,
  );
});
const contractPeriod = computed(() =>
  formatPeriod(props.member?.contractValidFrom, props.member?.contractValidTo),
);
const enabledQualifications = computed(() =>
  (props.member?.qualifications || []).filter(
    (item) =>
      item.qualificationStatus === RENTAL_CODE.QUALIFICATION_STATUS.ENABLED,
  ),
);

function display(value) {
  return value === undefined || value === null || value === "" ? "-" : value;
}

function optionLabel(options, value) {
  return getRentalOptionLabel(options, value) || "-";
}

function qualificationLabel(value) {
  return getRentalOptionLabel(rentalQualificationOptions, value) || "-";
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

function formatDate(value) {
  return value ? parseTime(value, "{y}-{m}-{d}") : "-";
}

function formatDateTime(value) {
  return value ? parseTime(value) : "-";
}

function formatPeriod(start, end) {
  if (!start && !end) return "-";
  return `${formatDate(start)} - ${formatDate(end)}`;
}

function formatCapital(value) {
  return value === undefined || value === null
    ? "-"
    : `¥${Number(value).toLocaleString("zh-CN", { minimumFractionDigits: 2 })}`;
}

function formatQuota(value) {
  if (value === undefined || value === null) return "-";
  return value >= 1024
    ? `${Number(value / 1024).toFixed(1)} GB`
    : `${value} MB`;
}

async function loadRisk(memberId = props.member?.memberId) {
  if (!memberId) return;
  riskLoading.value = true;
  try {
    const response = await getMemberRisk(memberId);
    risk.value = response.data || null;
  } catch (error) {
    risk.value = null;
    ElMessage.error(error?.message || "商户风险资料加载失败");
  } finally {
    riskLoading.value = false;
  }
}

function openCreditDialog() {
  Object.assign(creditForm, {
    eventType: 1,
    scoreChange: -1,
    reason: "",
    sourceNo: "",
  });
  creditVisible.value = true;
}

function openBlacklistDialog() {
  Object.assign(blacklistForm, { reasonType: 9, reason: "", expiresTime: "" });
  blacklistVisible.value = true;
}

function openReleaseDialog(row) {
  releaseRecordNo.value = row.recordNo || "";
  releaseForm.remark = "";
  releaseVisible.value = true;
}

function openReverseDialog(row) {
  reverseRecordNo.value = row.recordNo || "";
  reverseForm.reason = "";
  reverseVisible.value = true;
}

async function submitCredit() {
  if (!(await creditFormRef.value?.validate().catch(() => false))) return;
  riskSubmitting.value = true;
  try {
    await addMemberCreditRecord(props.member.memberId, { ...creditForm });
    ElMessage.success("信用记录已保存");
    creditVisible.value = false;
    await loadRisk();
  } finally {
    riskSubmitting.value = false;
  }
}

async function submitBlacklist() {
  if (!(await blacklistFormRef.value?.validate().catch(() => false))) return;
  riskSubmitting.value = true;
  try {
    await addMemberBlacklist(props.member.memberId, { ...blacklistForm });
    ElMessage.success("黑名单记录已保存");
    blacklistVisible.value = false;
    await loadRisk();
  } finally {
    riskSubmitting.value = false;
  }
}

async function submitRelease() {
  if (!(await releaseFormRef.value?.validate().catch(() => false))) return;
  riskSubmitting.value = true;
  try {
    await releaseMemberBlacklist(props.member.memberId, releaseRecordNo.value, {
      ...releaseForm,
    });
    ElMessage.success("黑名单已解除");
    releaseVisible.value = false;
    await loadRisk();
  } finally {
    riskSubmitting.value = false;
  }
}

async function submitReverse() {
  if (!(await reverseFormRef.value?.validate().catch(() => false))) return;
  riskSubmitting.value = true;
  try {
    await reverseMemberCreditRecord(props.member.memberId, reverseRecordNo.value, {
      ...reverseForm,
    });
    ElMessage.success("信用流水已冲正");
    reverseVisible.value = false;
    await loadRisk();
  } finally {
    riskSubmitting.value = false;
  }
}

function creditLevelTagType(level) {
  return (
    { 1: "success", 2: "warning", 3: "danger" }[Number(level)] || "info"
  );
}

function formatScoreChange(value) {
  const scoreChange = Number(value || 0);
  return scoreChange > 0 ? `+${scoreChange}` : String(scoreChange);
}

function scoreChangeClass(value) {
  return Number(value) > 0 ? "score-positive" : "score-negative";
}
</script>

<style scoped lang="scss">
.detail-section + .detail-section {
  margin-top: 24px;
}

.detail-section h3 {
  margin: 0 0 12px;
  padding-left: 10px;
  border-left: 4px solid var(--el-color-primary);
  font-size: 16px;
  letter-spacing: 0;
}

.identity-images {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.identity-image-item > span {
  display: block;
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
}

.history-table {
  margin-top: 12px;
}

.history-title {
  font-weight: 600;
}

.history-meta,
.history-remark {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
}

.material-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.material-list span {
  padding: 2px 8px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  border-radius: 4px;
}

.history-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.section-heading h3 {
  flex: 1;
}

.section-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.risk-table {
  margin-top: 16px;
}

.score-positive {
  color: var(--el-color-success);
  font-weight: 600;
}

.score-negative {
  color: var(--el-color-danger);
  font-weight: 600;
}

@media (max-width: 760px) {
  .history-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  :deep(.el-descriptions__body .el-descriptions__table) {
    table-layout: fixed;
  }
}
</style>
