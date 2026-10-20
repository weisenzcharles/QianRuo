<template>
  <div class="app-container merchant-list-page merchant-inventory-page">
    <el-form
      v-if="showSearch"
      :model="query"
      :inline="true"
      class="search-form"
      @submit.prevent>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="商品编码、名称或型号"
          @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="仓库">
        <el-select
          v-model="query.warehouseId"
          clearable
          filterable
          placeholder="全部仓库">
          <el-option
            v-for="item in warehouses"
            :key="item.id"
            :label="`${item.name}`"
            :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类">
        <el-select
          v-model="query.classId"
          clearable
          filterable
          placeholder="全部分类">
          <el-option
            v-for="item in secondClasses"
            :key="item.classId"
            :label="item.label"
            :value="item.classId" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.productType" clearable placeholder="全部类型">
          <el-option label="设备" :value="PRODUCT_TYPE.DEVICE" />
          <el-option label="配件" :value="PRODUCT_TYPE.ACCESSORY" />
        </el-select>
      </el-form-item>
      <el-form-item label="品牌">
        <el-select
          v-model="query.brand"
          clearable
          filterable
          placeholder="全部品牌">
          <el-option
            v-for="item in brands"
            :key="item"
            :label="item"
            :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.unitStatus" clearable placeholder="全部状态">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="load">查询</el-button>
        <el-button icon="Refresh" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="openEntry('manual')">
          录入设备
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Upload" @click="openImport">
          导入设备
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="load" />
    </el-row>
    <el-table v-loading="loading" :data="rows" border row-key="inventoryId">
      <el-table-column label="商品编码" prop="productCode" min-width="160" />
      <el-table-column label="商品名称" prop="productName" min-width="240" />
      <el-table-column label="品牌" prop="brand" min-width="100" />
      <el-table-column label="仓库" prop="warehouseName" min-width="140" />
      <el-table-column label="类型" width="90" align="center">
        <template #default="{ row }">{{ typeText(row.productType) }}</template>
      </el-table-column>
      <el-table-column label="分类" min-width="180">
        <template #default="{ row }">
          {{ classNameText(row.classId) }}
        </template>
      </el-table-column>

      <el-table-column
        label="总库存"
        prop="totalQuantity"
        width="100"
        align="center" />
      <el-table-column
        label="可用"
        prop="availableQuantity"
        width="90"
        align="center" />
      <el-table-column
        label="已预占"
        prop="reservedQuantity"
        width="100"
        align="center" />
      <el-table-column
        label="出租中"
        prop="rentedQuantity"
        width="100"
        align="center" />
      <el-table-column
        label="维修中"
        prop="repairingQuantity"
        width="100"
        align="center" />
      <el-table-column
        label="损坏/报废"
        prop="damagedQuantity"
        width="120"
        align="center" />
      <el-table-column
        label="可接单"
        prop="orderableQuantity"
        width="100"
        align="center" />
      <el-table-column label="操作" fixed="right" width="190" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="showUnits(row)">
            设备详细
          </el-button>
          <el-button
            link
            type="primary"
            icon="Setting"
            v-hasPermi="['merchant:inventory:edit']"
            @click="openPolicy(row)">
            接单口径
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
    <el-dialog
      v-model="policyVisible"
      title="可接单口径"
      width="560px"
      destroy-on-close>
      <el-descriptions
        v-if="policyTarget"
        :column="2"
        border
        label-width="120"
        class="policy-summary">
        <el-descriptions-item label="商品">
          {{ policyTarget.productName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="仓库">
          {{ policyTarget.warehouseName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="总库存">
          {{ policyTarget.totalQuantity ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="当前可接单">
          {{ policyTarget.orderableQuantity ?? 0 }}
        </el-descriptions-item>
      </el-descriptions>
      <el-form
        ref="policyFormRef"
        :model="policyForm"
        :rules="policyRules"
        label-width="110px">
        <el-form-item label="计算方式" prop="calculationMode">
          <el-radio-group v-model="policyForm.calculationMode">
            <el-radio :value="INVENTORY_CALCULATION_MODE.PROTECTED_QUANTITY">
              扣除保护数量
            </el-radio>
            <el-radio :value="INVENTORY_CALCULATION_MODE.TOTAL_PERCENT">
              总库存比例
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="policyForm.calculationMode === INVENTORY_CALCULATION_MODE.PROTECTED_QUANTITY"
          label="保护数量"
          prop="stockProtectionQty">
          <el-input-number
            v-model="policyForm.stockProtectionQty"
            :min="0"
            :max="policyTarget?.totalQuantity ?? 0"
            :precision="0"
            controls-position="right"
            class="field-control" />
        </el-form-item>
        <el-form-item v-else label="总库存比例" prop="calculationPercent">
          <el-input-number
            v-model="policyForm.calculationPercent"
            :min="0.01"
            :max="100"
            :precision="2"
            :step="1"
            controls-position="right"
            class="field-control">
            <template #suffix>%</template>
          </el-input-number>
        </el-form-item>
        <el-alert
          title="可接单数量 = 可用库存扣除保护数量，或按总库存比例计算后取较小值。"
          type="info"
          :closable="false" />
      </el-form>
      <template #footer>
        <el-button @click="policyVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="policyLoading"
          @click="savePolicy">
          保存
        </el-button>
      </template>
    </el-dialog>
    <el-dialog
      v-model="entryVisible"
      title="录入设备"
      width="680px"
      destroy-on-close
      @opened="focusSerialInput">
      <el-form
        ref="entryFormRef"
        :model="entry"
        :rules="entryRules"
        label-width="110px">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select
            v-model="entry.warehouseId"
            filterable
            placeholder="请选择仓库"
            class="field-control">
            <el-option
              v-for="item in warehouses"
              :key="item.id"
              :label="`${item.name}`"
              :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品分类" prop="classId">
          <el-select
            v-model="entry.classId"
            filterable
            placeholder="请选择产品分类"
            class="field-control"
            @change="changeEntryClass">
            <el-option
              v-for="item in secondClasses"
              :key="item.classId"
              :label="item.label"
              :value="item.classId" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-select
            v-model="entry.brand"
            filterable
            clearable
            placeholder="请选择品牌"
            class="field-control"
            @change="loadEntryOptions">
            <el-option
              v-for="item in entryBrands"
              :key="item"
              :label="item"
              :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备或配件" prop="productId">
          <el-select
            v-model="entry.productId"
            filterable
            placeholder="请选择设备或配件"
            class="field-control">
            <el-option
              v-for="item in entryOptions"
              :key="item.productId"
              :label="optionLabel(item)"
              :value="item.productId" />
          </el-select>
        </el-form-item>
        <el-form-item label="序列号" prop="serialText">
          <el-input
            ref="serialInputRef"
            v-model="entry.serialText"
            class="serial-input"
            type="textarea"
            :rows="8"
            :readonly="false"
            placeholder="每行一个序列号，也支持逗号、空格分隔" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="entry.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="entryVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEntry">
          保存
        </el-button>
      </template>
    </el-dialog>
    <el-dialog
      v-model="importVisible"
      title="创建导入任务"
      width="680px"
      destroy-on-close>
      <el-alert
        title="操作提示"
        description="格式为 xls、xlsx、csv 文件，最多导入 1000 条。请先下载模板并按模板填写设备信息。"
        type="info"
        :closable="false"
        show-icon
        class="import-tip" />
      <el-form label-width="110px">
        <el-form-item label="文件导入">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept=".xls,.xlsx,.csv"
            :show-file-list="true"
            :on-change="handleImportFileChange"
            :on-remove="clearImportFile">
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <span class="el-upload__tip import-upload-tip">
                上传导入的文件
              </span>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="模板下载">
          <el-link
            type="primary"
            :underline="false"
            @click="downloadImportTemplate">
            批量导入设备模板.xlsx
          </el-link>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="importLoading"
          :disabled="!importFile"
          @click="submitImport">
          确认
        </el-button>
      </template>
    </el-dialog>
    <el-dialog
      v-model="unitsVisible"
      :title="isQuantityInventory ? '库存详情' : '设备明细'"
      width="960px"
      @closed="clearUnitInventoryTarget">
      <el-descriptions
        v-if="unitInventoryTarget"
        :column="3"
        border
        label-width="120"
        class="unit-inventory-summary">
        <el-descriptions-item label="商品名称" :span="2">
          {{ unitInventoryTarget.productName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="商品编码">
          {{ unitInventoryTarget.productCode || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="品牌">
          {{ unitInventoryTarget.brand || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="型号">
          {{ unitInventoryTarget.modelName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="类型">
          {{ typeText(unitInventoryTarget.productType) }}
        </el-descriptions-item>
        <el-descriptions-item label="当前仓库">
          {{ unitInventoryTarget.warehouseName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="仓库编码">
          {{ unitInventoryTarget.warehouseCode || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="所在城市">
          {{ unitInventoryTarget.warehouseCity || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="仓库地址" :span="3">
          {{ unitInventoryTarget.warehouseAddress || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="总库存">
          {{ unitInventoryTarget.totalQuantity ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="可用">
          {{ unitInventoryTarget.availableQuantity ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="已预占">
          {{ unitInventoryTarget.reservedQuantity ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="出租中">
          {{ unitInventoryTarget.rentedQuantity ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="维修中">
          {{ unitInventoryTarget.repairingQuantity ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="损坏/报废">
          {{ unitInventoryTarget.damagedQuantity ?? 0 }}
        </el-descriptions-item>
      </el-descriptions>
      <template v-if="isQuantityInventory">
        <div class="inventory-detail-title">预占订单</div>
        <el-table
          v-loading="reservationOrdersLoading"
          :data="reservationOrders"
          border>
          <el-table-column label="订单编号" prop="orderNo" min-width="180" show-overflow-tooltip />
          <el-table-column label="客户" prop="customerName" min-width="120" show-overflow-tooltip />
          <el-table-column label="收件人" prop="recipientName" min-width="120" show-overflow-tooltip />
          <el-table-column label="预占数量" prop="quantity" width="100" align="center" />
          <el-table-column label="订单状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="orderStatusTagType(row.orderStatus)">
                {{ orderStatusText(row.orderStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="预占时间" prop="reservedTime" min-width="170" />
        </el-table>
        <pagination
          v-show="reservationOrderTotal > 0"
          v-model:page="reservationOrderQuery.pageNum"
          v-model:limit="reservationOrderQuery.pageSize"
          :total="reservationOrderTotal"
          @pagination="loadReservationOrders" />
      </template>
      <template v-else>
        <el-table v-loading="unitsLoading" :data="units" border>
          <el-table-column label="库存编码" prop="unitCode" min-width="180" />
          <el-table-column label="序列号" prop="serialNumber" min-width="220" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              {{ statusText(row.unitStatus) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="310" align="center">
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                icon="View"
                @click="openUnitDetail(row)">
                详情
              </el-button>
              <el-button
                link
                type="primary"
                icon="Sort"
                :disabled="
                  hasOrderAssociation(row) ||
                  Number(row.unitStatus) !== UNIT_STATUS.AVAILABLE ||
                  unitActionLoading
                "
                :title="
                  hasOrderAssociation(row)
                    ? '已关联订单，不能转仓'
                    : Number(row.unitStatus) !== UNIT_STATUS.AVAILABLE
                      ? '仅可转移可用状态的设备'
                      : '转仓'
                "
                v-hasPermi="['merchant:inventory:transfer']"
                @click="openUnitTransfer(row)">
                转仓
              </el-button>
              <el-button
                link
                type="primary"
                icon="Edit"
                v-hasPermi="['merchant:inventory:edit']"
                @click="openUnitEdit(row)">
                编辑
              </el-button>
              <el-button
                link
                type="danger"
                icon="Delete"
                :disabled="hasOrderAssociation(row) || unitActionLoading"
                :title="
                  hasOrderAssociation(row) ? '已关联订单，不能删除' : '删除设备'
                "
                v-hasPermi="['merchant:inventory:remove']"
                @click="removeUnit(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="unitTotal > 0"
          v-model:page="unitQuery.pageNum"
          v-model:limit="unitQuery.pageSize"
          :total="unitTotal"
          @pagination="loadUnits" />
      </template>
    </el-dialog>
    <el-dialog
      v-model="unitDetailVisible"
      title="设备详情"
      width="680px"
      append-to-body>
      <el-descriptions v-if="unitDetail" :column="2" border label-width="120">
        <el-descriptions-item label="商品名称">{{ unitDetail.productName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ unitDetail.modelName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="库存编码">{{ unitDetail.unitCode || "-" }}</el-descriptions-item>
        <el-descriptions-item label="序列号">{{ unitDetail.serialNumber || "-" }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ unitDetail.warehouseName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(unitDetail.unitStatus) }}</el-descriptions-item>
        <el-descriptions-item label="成色">{{ conditionLevelText(unitDetail.conditionLevel) }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ unitDetail.memorySpec || "-" }}</el-descriptions-item>
        <el-descriptions-item label="购入价格">{{ unitDetail.purchaseCost ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="保险有效期至">{{ unitDetail.insuranceUntil || "-" }}</el-descriptions-item>
        <el-descriptions-item label="保险费用">{{ unitDetail.insuranceAmount ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="实物照片">
          <el-image
            v-if="unitDetail.serialPhotoUrl"
            :src="unitDetail.serialPhotoUrl"
            :preview-src-list="[unitDetail.serialPhotoUrl]"
            fit="cover"
            style="width: 80px; height: 60px" />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="市场认证">
          {{ Number(unitDetail.marketVerifiedFlag) === 1 ? "已认证" : "未认证" }}
        </el-descriptions-item>
        <el-descriptions-item label="订单关联">{{ hasOrderAssociation(unitDetail) ? "已关联订单" : "未关联订单" }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ unitDetail.remark || "-" }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="unitDetailVisible = false">关闭</el-button></template>
    </el-dialog>
    <el-dialog
      v-model="unitEditVisible"
      title="编辑设备"
      width="680px"
      destroy-on-close>
      <el-form
        ref="unitEditFormRef"
        :model="unitEdit"
        :rules="unitEditRules"
        label-width="110px">
        <el-form-item label="序列号" prop="serialNumber">
          <el-input
            v-model="unitEdit.serialNumber"
            maxlength="128"
            show-word-limit
            disabled
            placeholder="请输入序列号"
            @keyup.enter="submitUnitEdit" />
        </el-form-item>
        <el-form-item label="状态" prop="unitStatus">
          <el-select
            v-model="unitEdit.unitStatus"
            filterable
            placeholder="请选择设备状态"
            class="field-control">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
              :disabled="
                (isWorkflowStatus(unitEdit.unitStatus) &&
                  item.value !== unitEdit.unitStatus) ||
                (!isWorkflowStatus(unitEdit.unitStatus) &&
                  isWorkflowStatus(item.value))
              " />
          </el-select>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="购入价格">
              <el-input-number
                v-model="unitEdit.purchaseCost"
                :min="0"
                :precision="2"
                :step="0.01"
                controls-position="right"
                class="field-control"
                placeholder="请输入购入价格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成色">
              <el-select
                v-model="unitEdit.conditionLevel"
                clearable
                filterable
                :loading="conditionLevelLoading"
                placeholder="请选择设备成色"
                class="field-control">
                <el-option
                  v-for="item in conditionLevelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="规格">
          <el-input
            v-model="unitEdit.memorySpec"
            maxlength="64"
            show-word-limit
            placeholder="请输入设备规格" />
        </el-form-item>
        <el-form-item label="实物照片">
          <image-upload
            v-model="unitEdit.serialPhotoUrl"
            :limit="1"
            :is-show-tip="false"
            business-type="inventory-unit" />
          <div class="form-help">上传并保存后，设备才可作为周转市场的认证实物库存。</div>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="保险有效期至">
              <el-date-picker
                v-model="unitEdit.insuranceUntil"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择日期"
                class="field-control" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保险费用">
              <el-input-number
                v-model="unitEdit.insuranceAmount"
                :min="0"
                :precision="2"
                :step="0.01"
                controls-position="right"
                class="field-control"
                placeholder="请输入保险费用" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input
            v-model="unitEdit.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入设备备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="unitEditVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="unitActionLoading"
          @click="submitUnitEdit">
          保存
        </el-button>
      </template>
    </el-dialog>
    <el-dialog
      v-model="unitTransferVisible"
      title="转仓"
      width="620px"
      destroy-on-close>
      <el-descriptions
        v-if="unitTransferTarget"
        :column="2"
        border
        label-width="120"
        class="unit-transfer-descriptions">
        <el-descriptions-item label="商品">
          {{ unitTransferTarget.productName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="型号">
          {{ unitTransferTarget.modelName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="库存编码">
          {{ unitTransferTarget.unitCode || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="序列号">
          {{ unitTransferTarget.serialNumber || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="当前仓库">
          {{ unitTransferTarget.warehouseName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ statusText(unitTransferTarget.unitStatus) }}
        </el-descriptions-item>
      </el-descriptions>
      <el-form
        ref="unitTransferFormRef"
        :model="unitTransfer"
        :rules="unitTransferRules"
        label-width="90px">
        <el-form-item label="目标仓库" prop="targetWarehouseId">
          <el-select
            v-model="unitTransfer.targetWarehouseId"
            filterable
            placeholder="请选择目标仓库"
            class="field-control">
            <el-option
              v-for="item in transferWarehouses"
              :key="item.id"
              :label="`${item.name}`"
              :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="unitTransferVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="unitActionLoading"
          :disabled="!transferWarehouses.length"
          @click="submitUnitTransfer">
          确认转仓
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { saveAs } from "file-saver";
import { listBusinessProductClasses } from "@/api/merchant/productClass";
import { listMerchantWarehouses } from "@/api/merchant/rentalBusiness";
import {
  addMerchantInventoryBatch,
  deleteMerchantInventoryUnit,
  listMerchantInventory,
  listMerchantInventoryConditionLevels,
  listMerchantInventoryOptions,
  listMerchantInventoryReservationOrders,
  listMerchantInventoryUnits,
  transferMerchantInventoryUnit,
  updateMerchantInventoryUnit,
} from "@/api/merchant/inventory";
import {
  exportMerchantResource,
  importMerchantResource,
} from "@/api/merchant/batch";
import { updateMerchantInventoryPolicy } from "@/api/merchant/inventoryGovernance";
import { RENTAL_CODE } from "@/utils/rental/options";

const PRODUCT_TYPE = { DEVICE: 1, ACCESSORY: 2 };
const UNIT_STATUS = { AVAILABLE: 1, RESERVED: 2, RENTED: 3 };
const INVENTORY_CALCULATION_MODE = {
  PROTECTED_QUANTITY: "PROTECTED_QUANTITY",
  TOTAL_PERCENT: "TOTAL_PERCENT",
};
const statusOptions = [
  { label: "可用", value: 1 },
  { label: "已预占", value: 2 },
  { label: "出租中", value: 3 },
  { label: "维修中", value: 4 },
  { label: "已丢失", value: 5 },
  { label: "已报废", value: 6 },
];
const showSearch = ref(true);
const loading = ref(false);
const saving = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  warehouseId: undefined,
  classId: undefined,
  productType: undefined,
  brand: "",
  unitStatus: undefined,
});
const warehouses = ref([]);
const classTree = ref([]);
const allOptions = ref([]);
const entryVisible = ref(false);
const entryFormRef = ref();
const serialInputRef = ref();
const entryOptions = ref([]);
const importVisible = ref(false);
const importLoading = ref(false);
const importFile = ref(null);
const policyVisible = ref(false);
const policyLoading = ref(false);
const policyFormRef = ref();
const policyTarget = ref(null);
const policyForm = reactive({
  inventoryId: undefined,
  calculationMode: INVENTORY_CALCULATION_MODE.PROTECTED_QUANTITY,
  stockProtectionQty: 0,
  calculationPercent: 90,
});
const entry = reactive({
  warehouseId: undefined,
  classId: undefined,
  brand: "",
  productId: undefined,
  serialText: "",
  remark: "",
});
const unitsVisible = ref(false);
const unitsLoading = ref(false);
const units = ref([]);
const unitTotal = ref(0);
const reservationOrdersLoading = ref(false);
const reservationOrders = ref([]);
const reservationOrderTotal = ref(0);
const unitInventoryTarget = ref(null);
const unitDetailVisible = ref(false);
const unitDetail = ref(null);
const unitActionLoading = ref(false);
const unitEditVisible = ref(false);
const unitEditFormRef = ref();
const conditionLevelOptions = ref([]);
const conditionLevelLoading = ref(false);
const unitEdit = reactive({
  unitId: undefined,
  serialNumber: "",
  unitStatus: undefined,
  memorySpec: "",
  purchaseCost: undefined,
  conditionLevel: undefined,
  serialPhotoUrl: "",
  insuranceUntil: "",
  insuranceAmount: undefined,
  remark: "",
});
const unitTransferVisible = ref(false);
const unitTransferFormRef = ref();
const unitTransferTarget = ref(null);
const unitTransfer = reactive({
  unitId: undefined,
  targetWarehouseId: undefined,
});
const unitQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  inventoryId: undefined,
  productId: undefined,
  warehouseId: undefined,
});
const reservationOrderQuery = reactive({
  pageNum: 1,
  pageSize: 10,
});
const isQuantityInventory = computed(
  () => Number(unitInventoryTarget.value?.stockMode) === RENTAL_CODE.STOCK_MODE.QUANTITY,
);
const secondClasses = computed(() => {
  const result = [];
  const walk = (nodes, parent = "") =>
    (nodes || []).forEach((node) => {
      if (Number(node.classLevel) === 2 || (parent && !node.children))
        result.push({
          classId: node.classId,
          label: parent ? `${parent} / ${node.className}` : node.className,
        });
      else walk(node.children, node.className);
    });
  walk(classTree.value);
  return result;
});
const classLabels = computed(() =>
  Object.fromEntries(
    secondClasses.value.map((item) => [Number(item.classId), item.label]),
  ),
);
const brands = computed(() => [
  ...new Set(allOptions.value.map((item) => item.brand).filter(Boolean)),
]);
const entryBrands = computed(() => [
  ...new Set(
    allOptions.value
      .filter(
        (item) =>
          !entry.classId || Number(item.classId) === Number(entry.classId),
      )
      .map((item) => item.brand)
      .filter(Boolean),
  ),
]);
const entryRules = {
  warehouseId: [{ required: true, message: "请选择仓库", trigger: "change" }],
  classId: [{ required: true, message: "请选择产品分类", trigger: "change" }],
  productId: [
    { required: true, message: "请选择设备或配件", trigger: "change" },
  ],
  serialText: [{ required: true, message: "请输入序列号", trigger: "blur" }],
};
const policyRules = {
  calculationMode: [
    { required: true, message: "请选择计算方式", trigger: "change" },
  ],
  stockProtectionQty: [
    { required: true, message: "请输入保护数量", trigger: "change" },
  ],
  calculationPercent: [
    { required: true, message: "请输入总库存比例", trigger: "change" },
  ],
};
const unitEditRules = {
  unitStatus: [
    { required: true, message: "请选择设备状态", trigger: "change" },
  ],
  serialNumber: [{ required: true, message: "请输入序列号", trigger: "blur" }],
};
const unitTransferRules = {
  targetWarehouseId: [
    { required: true, message: "请选择目标仓库", trigger: "change" },
  ],
};
const transferWarehouses = computed(() => {
  const sourceWarehouseId = unitTransferTarget.value?.warehouseId;
  return warehouses.value.filter(
    (item) => Number(item.id) !== Number(sourceWarehouseId),
  );
});
function responseRows(response) {
  return response.rows || response.data?.rows || response.data || [];
}
async function load() {
  loading.value = true;
  try {
    const response = await listMerchantInventory(query);
    rows.value = responseRows(response);
    total.value = response.total || response.data?.total || 0;
  } finally {
    loading.value = false;
  }
}
function reset() {
  Object.assign(query, {
    pageNum: 1,
    keyword: "",
    warehouseId: undefined,
    classId: undefined,
    productType: undefined,
    brand: "",
    unitStatus: undefined,
  });
  load();
}
function openPolicy(row) {
  policyTarget.value = row;
  Object.assign(policyForm, {
    inventoryId: row.inventoryId,
    calculationMode:
      Number(row.calculationMode) === 2
        ? INVENTORY_CALCULATION_MODE.TOTAL_PERCENT
        : INVENTORY_CALCULATION_MODE.PROTECTED_QUANTITY,
    stockProtectionQty: Number(row.stockProtectionQty || 0),
    calculationPercent: Number(row.calculationPercent || 90),
  });
  policyVisible.value = true;
}
async function savePolicy() {
  if (!(await policyFormRef.value?.validate().catch(() => false))) return;
  policyLoading.value = true;
  try {
    await updateMerchantInventoryPolicy({ ...policyForm });
    ElMessage.success("库存接单口径保存成功");
    policyVisible.value = false;
    await load();
  } finally {
    policyLoading.value = false;
  }
}
async function loadOptions() {
  const [warehouseResponse, classResponse, optionResponse] = await Promise.all([
    listMerchantWarehouses({ pageNum: 1, pageSize: 1000, status: 0 }),
    listBusinessProductClasses(),
    listMerchantInventoryOptions({}),
  ]);
  warehouses.value = responseRows(warehouseResponse);
  classTree.value = classResponse.data || [];
  allOptions.value = responseRows(optionResponse);
}
function loadEntryOptions() {
  entryOptions.value = allOptions.value.filter(
    (item) =>
      (!entry.classId || Number(item.classId) === Number(entry.classId)) &&
      (!entry.brand || item.brand === entry.brand),
  );
}
function changeEntryClass() {
  entry.brand = "";
  entry.productId = undefined;
  loadEntryOptions();
}
function openEntry() {
  Object.assign(entry, {
    warehouseId: undefined,
    classId: undefined,
    brand: "",
    productId: undefined,
    serialText: "",
    remark: "",
  });
  entryOptions.value = allOptions.value;
  entryVisible.value = true;
}
function openImport() {
  importFile.value = null;
  importVisible.value = true;
}
function handleImportFileChange(uploadFile) {
  importFile.value = uploadFile.raw;
}
function clearImportFile() {
  importFile.value = null;
}
function downloadImportTemplate() {
  exportMerchantResource("sn").then((data) =>
    saveAs(new Blob([data]), "批量导入设备模板.xlsx"),
  );
}
async function submitImport() {
  if (!importFile.value) return ElMessage.warning("请选择导入文件");
  importLoading.value = true;
  try {
    const response = await importMerchantResource("sn", importFile.value);
    const result = response.data || {};
    ElMessage.success(
      `导入处理完成，成功 ${result.success || 0} 条，失败 ${
        result.failed || 0
      } 条`,
    );
    importVisible.value = false;
    await load();
  } finally {
    importLoading.value = false;
  }
}
function focusSerialInput() {
  serialInputRef.value?.focus?.();
}
function optionLabel(item) {
  return `${item.productType === PRODUCT_TYPE.ACCESSORY ? "配件" : "设备"} | ${
    item.productName
  }（${item.productCode}）${item.modelName ? ` | ${item.modelName}` : ""}`;
}
async function submitEntry() {
  if (!(await entryFormRef.value?.validate().catch(() => false))) return;
  const serialNumbers = entry.serialText
    .split(/[\s,，;；]+/)
    .map((item) => item.trim())
    .filter(Boolean);
  if (!serialNumbers.length) {
    ElMessage.warning("请输入序列号");
    return;
  }
  saving.value = true;
  try {
    await addMerchantInventoryBatch({
      productId: entry.productId,
      warehouseId: entry.warehouseId,
      serialNumbers,
      remark: entry.remark,
    });
    ElMessage.success("设备录入成功");
    entryVisible.value = false;
    await load();
  } finally {
    saving.value = false;
  }
}
function showUnits(row) {
  unitInventoryTarget.value = row;
  unitQuery.inventoryId = row.inventoryId;
  unitQuery.productId = row.productId;
  unitQuery.warehouseId = row.warehouseId;
  unitQuery.pageNum = 1;
  reservationOrderQuery.pageNum = 1;
  unitsVisible.value = true;
  if (Number(row.stockMode) === RENTAL_CODE.STOCK_MODE.QUANTITY) {
    units.value = [];
    unitTotal.value = 0;
    loadReservationOrders();
  } else {
    reservationOrders.value = [];
    reservationOrderTotal.value = 0;
    loadUnits();
  }
}
function clearUnitInventoryTarget() {
  unitInventoryTarget.value = null;
  units.value = [];
  reservationOrders.value = [];
}
async function loadUnits() {
  unitsLoading.value = true;
  try {
    const response = await listMerchantInventoryUnits(
      unitQuery.inventoryId,
      unitQuery,
    );
    units.value = responseRows(response);
    unitTotal.value = response.total || response.data?.total || 0;
  } finally {
    unitsLoading.value = false;
  }
}
async function loadReservationOrders() {
  if (!unitInventoryTarget.value?.inventoryId) return;
  reservationOrdersLoading.value = true;
  try {
    const response = await listMerchantInventoryReservationOrders(
      unitInventoryTarget.value.inventoryId,
      reservationOrderQuery,
    );
    reservationOrders.value = responseRows(response);
    reservationOrderTotal.value = response.total || response.data?.total || 0;
  } finally {
    reservationOrdersLoading.value = false;
  }
}
function hasOrderAssociation(row) {
  return row.orderAssociated === true || Number(row.orderAssociated) === 1;
}
function conditionLevelText(value) {
  if (value == null || value === "") return "-";
  return conditionLevelOptions.value.find((item) => Number(item.value) === Number(value))?.label || "未知成色";
}
function openUnitDetail(row) {
  unitDetail.value = row;
  unitDetailVisible.value = true;
  if (!conditionLevelOptions.value.length) {
    loadConditionLevelOptions().catch(() => {
      conditionLevelOptions.value = [];
    });
  }
}
function isWorkflowStatus(status) {
  return (
    Number(status) === UNIT_STATUS.RESERVED ||
    Number(status) === UNIT_STATUS.RENTED
  );
}
async function openUnitEdit(row) {
  Object.assign(unitEdit, {
    unitId: row.unitId,
    serialNumber: row.serialNumber || row.unitCode || "",
    unitStatus: row.unitStatus == null ? undefined : Number(row.unitStatus),
    memorySpec: row.memorySpec || "",
    purchaseCost:
      row.purchaseCost == null ? undefined : Number(row.purchaseCost),
    conditionLevel:
      row.conditionLevel == null ? undefined : Number(row.conditionLevel),
    serialPhotoUrl: row.serialPhotoUrl || "",
    insuranceUntil: row.insuranceUntil || "",
    insuranceAmount:
      row.insuranceAmount == null ? undefined : Number(row.insuranceAmount),
    remark: row.remark || "",
  });
  unitEditVisible.value = true;
  await loadConditionLevelOptions();
}
async function loadConditionLevelOptions() {
  conditionLevelLoading.value = true;
  try {
    const response = await listMerchantInventoryConditionLevels();
    conditionLevelOptions.value = Array.isArray(response.data)
      ? response.data.map((item) => ({
          label: item.label,
          value: Number(item.value),
        }))
      : [];
  } finally {
    conditionLevelLoading.value = false;
  }
}
function openUnitTransfer(row) {
  if (hasOrderAssociation(row)) {
    ElMessage.warning("该设备已关联订单，不能转仓");
    return;
  }
  if (Number(row.unitStatus) !== UNIT_STATUS.AVAILABLE) {
    ElMessage.warning("仅可转移可用状态的设备");
    return;
  }
  unitTransferTarget.value = row;
  Object.assign(unitTransfer, {
    unitId: row.unitId,
    targetWarehouseId: undefined,
  });
  unitTransferVisible.value = true;
}
async function submitUnitEdit() {
  if (!(await unitEditFormRef.value?.validate().catch(() => false))) return;
  unitActionLoading.value = true;
  try {
    await updateMerchantInventoryUnit({
      unitId: unitEdit.unitId,
      serialNumber: unitEdit.serialNumber.trim(),
      unitStatus: unitEdit.unitStatus,
      memorySpec: unitEdit.memorySpec?.trim() || null,
      purchaseCost: unitEdit.purchaseCost ?? null,
      conditionLevel: unitEdit.conditionLevel ?? null,
      serialPhotoUrl: unitEdit.serialPhotoUrl?.trim() || null,
      insuranceUntil: unitEdit.insuranceUntil || null,
      insuranceAmount: unitEdit.insuranceAmount ?? null,
      remark: unitEdit.remark?.trim() || null,
    });
    ElMessage.success("设备信息更新成功");
    unitEditVisible.value = false;
    await Promise.all([loadUnits(), load()]);
  } finally {
    unitActionLoading.value = false;
  }
}
async function removeUnit(row) {
  if (hasOrderAssociation(row)) {
    ElMessage.warning("该设备已关联订单，不能删除");
    return;
  }
  try {
    await ElMessageBox.confirm(
      "确认删除该设备吗？删除后将从库存中移除。",
      "删除确认",
      {
        type: "warning",
      },
    );
  } catch (error) {
    if (error === "cancel" || error === "close") return;
    throw error;
  }
  unitActionLoading.value = true;
  try {
    await deleteMerchantInventoryUnit(row.unitId);
    ElMessage.success("设备删除成功");
    await Promise.all([loadUnits(), load()]);
  } finally {
    unitActionLoading.value = false;
  }
}
async function submitUnitTransfer() {
  if (!(await unitTransferFormRef.value?.validate().catch(() => false))) {
    return;
  }
  unitActionLoading.value = true;
  try {
    await transferMerchantInventoryUnit({
      unitId: unitTransfer.unitId,
      targetWarehouseId: unitTransfer.targetWarehouseId,
    });
    ElMessage.success("转仓成功");
    unitTransferVisible.value = false;
    await Promise.all([loadUnits(), load()]);
  } finally {
    unitActionLoading.value = false;
  }
}
function typeText(type) {
  return Number(type) === PRODUCT_TYPE.ACCESSORY ? "配件" : "设备";
}
function classNameText(classId) {
  return classLabels.value[Number(classId)] || classId || "-";
}
function statusText(status) {
  return (
    statusOptions.find((item) => item.value === Number(status))?.label || "未知"
  );
}
function orderStatusText(status) {
  const labels = {
    10: "待发货",
    15: "已备货",
    20: "运输中",
    25: "运输中",
    30: "已发货",
    40: "租用中",
    45: "归还中",
    50: "已完成",
    60: "归还逾期",
    65: "已逾期",
    70: "售后中",
    80: "已取消",
    pending_ship: "待发货",
    prepared: "已备货",
    shipping: "运输中",
    ship_in_transit: "运输中",
    shipped: "已发货",
    renting: "租用中",
    returning: "归还中",
    completed: "已完成",
    overdue_return: "归还逾期",
    overdue: "已逾期",
    after_sales: "售后中",
    cancelled: "已取消",
  };
  return labels[String(status ?? "").trim().toLowerCase()] || (status || "预占中");
}
function orderStatusTagType(status) {
  const value = String(status ?? "").trim().toLowerCase();
  if (["50", "completed"].includes(value)) return "success";
  if (["80", "cancelled"].includes(value)) return "info";
  if (["65", "60", "overdue", "overdue_return", "70", "after_sales"].includes(value)) return "danger";
  return "warning";
}
loadOptions();
load();
</script>

<style scoped>
.merchant-inventory-page :deep(.el-input-number),
.field-control {
  width: 100%;
}

.form-help {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 18px;
}

.serial-input :deep(textarea) {
  pointer-events: auto;
  user-select: text;
}

.import-tip {
  margin-bottom: 24px;
}

.import-upload-tip {
  display: inline-block;
  margin-top: 0;
  margin-left: 16px;
}

.unit-transfer-descriptions {
  margin-bottom: 20px;
}

.unit-inventory-summary {
  margin-bottom: 20px;
}

.inventory-detail-title {
  margin: 20px 0 12px;
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 600;
}
</style>
