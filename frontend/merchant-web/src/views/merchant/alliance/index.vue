<template>
  <div class="app-container alliance-page">
    <el-tabs v-model="activeTab" @tab-change="loadTab">
      <el-tab-pane label="联盟关系" name="relations">
        <el-card shadow="never" class="mb8">
          <template #header><span>当前联盟</span></template>
          <el-empty v-if="!alliance" description="暂未加入租商联盟"
            ><el-button type="primary" @click="createVisible = true"
              >创建联盟</el-button
            ></el-empty
          >
          <template v-else>
            <el-descriptions :column="4" border label-width="120" class="alliance-overview">
              <el-descriptions-item label="联盟名称">{{
                alliance.allianceName
              }}</el-descriptions-item>
              <el-descriptions-item label="联盟状态">{{
                alliance.allianceStatus === 1 ? "正常" : "已停用"
              }}</el-descriptions-item>
              <el-descriptions-item label="成员数量"
                >{{ members.length
                }}{{
                  alliance.maxMemberCount ? ` / ${alliance.maxMemberCount}` : ""
                }}</el-descriptions-item
              >
              <el-descriptions-item label="接单上限">{{
                currentMember?.acceptOrderLimit ?? "不限"
              }}</el-descriptions-item>
              <el-descriptions-item label="当前承接数量">{{
                currentMember?.currentAcceptOrderCount ?? 0
              }}</el-descriptions-item>
              <el-descriptions-item label="共享库存">{{
                currentMember?.shareStockEnabled === 1 ? "已开启" : "未开启"
              }}</el-descriptions-item>
              <el-descriptions-item label="可见共享仓库">{{
                warehouseScopes.length
              }}</el-descriptions-item>
              <el-descriptions-item label="待处理协助订单">{{
                pendingTaskCount
              }}</el-descriptions-item>
            </el-descriptions>
            <el-row class="mb8 alliance-toolbar"
              ><el-button type="primary" icon="Plus" @click="openMemberDialog"
                >添加联盟成员</el-button
              ></el-row
            >
            <el-table v-loading="loading" :data="members" border
              ><el-table-column
                label="商户名称"
                prop="merchantName"
                min-width="180"
              /><el-table-column label="共享库存" width="130" align="center"
                ><template #default="scope"
                  ><el-switch
                    v-model="scope.row.shareStockEnabled"
                    :active-value="1"
                    :inactive-value="0"
                    @change="
                      saveMember(scope.row)
                    " /></template></el-table-column
              ><el-table-column label="承接订单上限" width="160" align="center"
                ><template #default="scope"
                  ><el-input-number
                    v-model="scope.row.acceptOrderLimit"
                    :min="0"
                    :max="100000"
                    size="small"
                    @change="
                      saveMember(scope.row)
                    " /></template></el-table-column
              ><el-table-column label="操作" width="160" align="center"
                ><template #default="scope"
                  ><el-button link type="primary" icon="View" @click="openMemberDetail(scope.row)">详情</el-button><el-button
                    link
                    type="danger"
                    @click="deleteMember(scope.row)"
                    >移除</el-button
                  ></template
                ></el-table-column
              ></el-table
            >
            <el-divider content-position="left">共享仓库</el-divider>
            <el-row :gutter="12" class="scope-toolbar">
              <el-col :span="5">
                <el-select
                  v-model="warehouseScopeForm.warehouseId"
                  clearable
                  filterable
                  placeholder="选择共享仓库"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in warehouseOptions"
                    :key="item.id"
                    :label="`${item.name}`"
                    :value="item.id"
                  />
                </el-select>
              </el-col>
              <el-col :span="3">
                <el-switch
                  v-model="warehouseScopeForm.acceptOrderEnabled"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="允许协助订单"
                />
              </el-col>

              <el-col :span="3">
                <el-button type="primary" @click="saveWarehouseScope">
                  共享仓库
                </el-button>
              </el-col>
            </el-row>
            <el-row :gutter="12" class="scope-toolbar">
              <el-table :data="warehouseScopes" border class="scope-table">
                <el-table-column
                  label="共享仓库"
                  prop="warehouseName"
                  min-width="180"
                />
                <el-table-column
                  label="允许协助订单"
                  width="140"
                  align="center"
                >
                  <template #default="scope">{{
                    scope.row.acceptOrderEnabled === 1 ? "是" : "否"
                  }}</template>
                </el-table-column>
                <el-table-column label="操作" width="160" align="center">
                  <template #default="scope">
                    <el-button link type="primary" icon="View" @click="openWarehouseScopeDetail(scope.row)">详情</el-button>
                    <el-button
                      link
                      type="danger"
                      @click="deleteWarehouseScope(scope.row)"
                      >删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-row>
            <el-divider content-position="left">共享商品</el-divider>
            <el-row :gutter="12" class="scope-toolbar">
              <el-col :span="5">
                <el-select
                  v-model="productScopeForm.productId"
                  clearable
                  filterable
                  placeholder="选择共享商品"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in productOptions"
                    :key="item.id"
                    :label="`${item.productName}（${item.productCode}）`"
                    :value="item.id"
                  />
                </el-select>
              </el-col>

              <el-col :span="3">
                <el-input-number
                  v-model="productScopeForm.shareQuantityRatio"
                  :min="0.0001"
                  :max="1"
                  :precision="4"
                  :step="0.1"
                  controls-position="right"
                  placeholder="共享比例"
                />
              </el-col>
              <el-col :span="3">
                <el-input-number
                  v-model="productScopeForm.shareQuantityLimit"
                  :min="0"
                  :max="1000"
                  controls-position="right"
                  placeholder="共享数量上限"
              /></el-col>
              <el-col :span="3">
                <el-switch
                  v-model="productScopeForm.acceptOrderEnabled"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="允许协助订单"
                />
              </el-col>
              <el-col :span="4">
                <el-button type="primary" @click="saveProductScope">
                  共享商品
                </el-button>
              </el-col>
            </el-row>
            <el-row :gutter="12" class="scope-toolbar">
              <el-table :data="productScopes" border class="scope-table"
                ><el-table-column label="共享商品" min-width="220"
                  ><template #default="scope"
                    >{{ scope.row.productName }}（{{
                      scope.row.productCode
                    }}）</template
                  ></el-table-column
                ><el-table-column label="共享比例" width="110" align="center"
                  ><template #default="scope">{{
                    scope.row.shareQuantityRatio
                  }}</template></el-table-column
                ><el-table-column label="共享上限" width="110" align="center"
                  ><template #default="scope">{{
                    scope.row.shareQuantityLimit ?? "不限"
                  }}</template></el-table-column
                ><el-table-column label="操作" width="160" align="center"
                  ><template #default="scope"
                    ><el-button link type="primary" icon="View" @click="openProductScopeDetail(scope.row)">详情</el-button><el-button
                      link
                      type="danger"
                      @click="deleteProductScope(scope.row)"
                      >删除</el-button
                    ></template
                  ></el-table-column
                ></el-table
              >
            </el-row>
          </template>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="共享库存" name="inventory">
        <el-form
          :inline="true"
          :model="inventoryQuery"
          class="search-form"
          @submit.prevent
          ><el-form-item label="关键词"
            ><el-input
              v-model="inventoryQuery.keyword"
              clearable
              placeholder="编码或商品名称"
              @keyup.enter="loadInventory" /></el-form-item
          ><el-form-item label="商品类型"
            ><el-select
              v-model="inventoryQuery.productType"
              clearable
              placeholder="全部"
              @change="loadInventory"
              ><el-option label="设备" value="device" /><el-option
                label="配件"
                value="accessory" /></el-select></el-form-item
          ><el-form-item
            ><el-button type="primary" icon="Search" @click="loadInventory"
              >查询</el-button
            ><el-button icon="Refresh" @click="resetInventory"
              >重置</el-button
            ></el-form-item
          ></el-form
        >
        <el-table v-loading="loading" :data="sharedInventory" border
          ><el-table-column
            label="库存所有者"
            prop="ownerMerchantName"
            width="150"
            align="center" /><el-table-column
            label="商品编码"
            prop="productCode"
            width="160" /><el-table-column
            label="商品名称"
            prop="productName"
            min-width="180" /><el-table-column
            label="类型"
            prop="productType"
            width="100"
            align="center" /><el-table-column
            label="仓库"
            prop="warehouseName"
            min-width="140" /><el-table-column
            label="可用库存"
            prop="onHandQty"
            width="110"
            align="center"
        /></el-table>
        <pagination
          v-show="inventoryTotal > 0"
          :total="inventoryTotal"
          v-model:page="inventoryQuery.pageNum"
          v-model:limit="inventoryQuery.pageSize"
          @pagination="loadInventory"
        />
      </el-tab-pane>
      <el-tab-pane label="协助订单" name="orders">
        <el-form :inline="true" class="search-form"
          ><el-form-item label="状态"
            ><el-select
              v-model="taskStatus"
              clearable
              placeholder="全部"
              @change="loadTasks"
              ><el-option label="待接受" :value="0" /><el-option
                label="已接受"
                :value="1" /><el-option label="已拒绝" :value="2" /><el-option
                label="已取消"
                :value="3" /><el-option label="已过期" :value="4" /><el-option
                label="公众池中"
                :value="5" /><el-option label="履约中" :value="6" /><el-option
                label="已完成"
                :value="7" /><el-option
                label="异常"
                :value="8" /></el-select></el-form-item
        ></el-form>
        <el-table v-loading="loading" :data="tasks" border>
          <el-table-column label="转单方向" width="110" align="center"
            ><template #default="scope">{{
              scope.row.targetMemberId === currentMemberId
                ? "收到"
                : scope.row.targetMemberId == null
                  ? "公众池"
                  : "发出"
            }}</template></el-table-column
          >
          <el-table-column
            label="商户名称"
            prop="merchantName"
            min-width="160"
            show-overflow-tooltip
          />
          <el-table-column label="订单号" prop="orderNo" min-width="170" />
          <el-table-column
            label="订单创建时间"
            prop="orderCreateTime"
            min-width="170"
          />
          <el-table-column
            label="转单时间"
            prop="transferTime"
            min-width="170"
          />
          <el-table-column
            label="套餐"
            prop="packageName"
            min-width="180"
            show-overflow-tooltip
          />
          <el-table-column
            label="履约仓库"
            prop="targetWarehouseName"
            min-width="140"
          />
          <el-table-column label="租期" min-width="210"
            ><template #default="scope">{{
              leaseText(scope.row)
            }}</template></el-table-column
          >
          <el-table-column label="金额" min-width="160" align="right"
            ><template #default="scope"
              >租金 {{ scope.row.rentAmount ?? "-" }} / 押金
              {{ scope.row.depositAmount ?? "-" }}</template
            ></el-table-column
          >
          <el-table-column
            label="数量"
            prop="quantity"
            width="80"
            align="center"
          />
          <el-table-column label="发货快递" min-width="190"
            ><template #default="scope">{{
              expressText(
                scope.row.shippingCarrier,
                scope.row.shippingTrackingNo,
              )
            }}</template></el-table-column
          >
          <el-table-column label="归还快递" min-width="190"
            ><template #default="scope">{{
              expressText(scope.row.returnCarrier, scope.row.returnTrackingNo)
            }}</template></el-table-column
          >
          <el-table-column label="物流状态" width="120" align="center"
            ><template #default="scope">{{
              routeStatusText(
                scope.row.shippingRouteStatus,
                scope.row.returnRouteStatus,
              )
            }}</template></el-table-column
          >
          <el-table-column label="状态" width="100" align="center"
            ><template #default="scope">{{
              statusText(scope.row.allianceOrderStatus)
            }}</template></el-table-column
          >
          <el-table-column label="操作" width="250" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="openTaskDetail(scope.row)">详情</el-button>
              <el-button
                v-if="
                  scope.row.targetMemberId === currentMemberId &&
                  scope.row.allianceOrderStatus === 0
                "
                link
                type="primary"
                @click="acceptTask(scope.row)"
                >接受</el-button
              >
              <el-button
                v-if="
                  scope.row.targetMemberId === currentMemberId &&
                  scope.row.allianceOrderStatus === 1
                "
                link
                type="primary"
                @click="openChangeWarehouse(scope.row)"
                >更换仓库</el-button
              >
              <el-button
                v-if="
                  scope.row.targetMemberId === currentMemberId &&
                  scope.row.allianceOrderStatus === 0
                "
                link
                type="danger"
                @click="rejectTask(scope.row)"
                >拒绝</el-button
              >
              <el-button
                v-if="
                  scope.row.targetMemberId === currentMemberId &&
                  scope.row.allianceOrderStatus === 0
                "
                link
                type="warning"
                @click="openReassign(scope.row)"
                >转派</el-button
              >
              <el-button
                v-if="
                  scope.row.sourceMemberId === currentMemberId &&
                  scope.row.allianceOrderStatus === 2
                "
                link
                type="primary"
                @click="publishPublicPoolTask(scope.row)"
                >发布公众池</el-button
              >
              <el-button
                v-if="
                  scope.row.sourceMemberId === currentMemberId &&
                  [0, 5].includes(scope.row.allianceOrderStatus)
                "
                link
                type="danger"
                @click="cancelTask(scope.row)"
                >取消</el-button
              >
              <el-button
                v-if="
                  scope.row.targetMemberId == null &&
                  scope.row.allianceOrderStatus === 5
                "
                link
                type="primary"
                @click="acceptTask(scope.row)"
                >领取</el-button
              >
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="taskTotal > 0"
          :total="taskTotal"
          v-model:page="taskQuery.pageNum"
          v-model:limit="taskQuery.pageSize"
          @pagination="loadTasks"
        />
      </el-tab-pane>
      <el-tab-pane label="联盟纠纷" name="disputes">
        <el-row class="mb8 alliance-toolbar"
          ><el-button type="primary" icon="Plus" @click="openCreateDispute"
            >发起纠纷</el-button
          ></el-row
        >
        <el-form
          :inline="true"
          :model="disputeQuery"
          class="search-form"
          @submit.prevent
        >
          <el-form-item label="状态"
            ><el-select
              v-model="disputeQuery.disputeStatus"
              clearable
              placeholder="全部"
              @change="loadDisputes"
              ><el-option label="待处理" :value="0" /><el-option
                label="举证中"
                :value="1" /><el-option label="调解中" :value="2" /><el-option
                label="待确认"
                :value="3" /><el-option label="已解决" :value="4" /><el-option
                label="已驳回"
                :value="5" /><el-option label="已关闭" :value="6" /></el-select
          ></el-form-item>
        </el-form>
        <el-table v-loading="loading" :data="disputes" border
          ><el-table-column
            label="订单编码"
            prop="orderNo"
            width="170"
            align="center"
          /><el-table-column label="纠纷类型" width="110" align="center"
            ><template #default="scope">{{
              disputeTypeText(scope.row.disputeType)
            }}</template></el-table-column
          ><el-table-column
            label="申请赔付金额"
            prop="claimAmount"
            width="130"
            align="right"
          /><el-table-column label="状态" width="100" align="center"
            ><template #default="scope">{{
              disputeStatusText(scope.row.disputeStatus)
            }}</template></el-table-column
          ><el-table-column
            label="创建时间"
            prop="createTime"
            min-width="170"
          /><el-table-column label="操作" width="100" align="center"
            ><template #default="scope"
              ><el-button link type="primary" @click="openDispute(scope.row)"
                >详情</el-button
              ></template
            ></el-table-column
          ></el-table
        >
        <pagination
          v-show="disputeTotal > 0"
          :total="disputeTotal"
          v-model:page="disputeQuery.pageNum"
          v-model:limit="disputeQuery.pageSize"
          @pagination="loadDisputes"
        />
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="scopeDetailVisible" :title="scopeDetailTitle" width="620px" append-to-body>
      <el-descriptions v-if="scopeDetailItems.length" :column="2" border label-width="120">
        <el-descriptions-item v-for="item in scopeDetailItems" :key="item.label" :label="item.label" :span="item.span || 1">{{ item.value }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="scopeDetailVisible = false">关闭</el-button></template>
    </el-dialog>
    <el-dialog v-model="taskDetailVisible" title="协助订单详情" width="720px" append-to-body>
      <el-descriptions v-if="taskDetail" :column="2" border label-width="120">
        <el-descriptions-item label="订单编码">{{ taskDetail.orderNo || "-" }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ taskDetail.merchantName || "联盟商户" }}</el-descriptions-item>
        <el-descriptions-item label="转单方向">{{ taskDirection(taskDetail) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(taskDetail.allianceOrderStatus) }}</el-descriptions-item>
        <el-descriptions-item label="套餐">{{ taskDetail.packageName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="履约仓库">{{ taskDetail.targetWarehouseName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="租期">{{ leaseText(taskDetail) }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ taskDetail.quantity ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="租金">{{ taskDetail.rentAmount ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="押金">{{ taskDetail.depositAmount ?? "-" }}</el-descriptions-item>
        <el-descriptions-item label="发货快递">{{ expressText(taskDetail.shippingCarrier, taskDetail.shippingTrackingNo) }}</el-descriptions-item>
        <el-descriptions-item label="归还快递">{{ expressText(taskDetail.returnCarrier, taskDetail.returnTrackingNo) }}</el-descriptions-item>
        <el-descriptions-item label="订单创建时间">{{ taskDetail.orderCreateTime || "-" }}</el-descriptions-item>
        <el-descriptions-item label="转单时间">{{ taskDetail.transferTime || "-" }}</el-descriptions-item>
        <el-descriptions-item label="物流状态" :span="2">{{ routeStatusText(taskDetail.shippingRouteStatus, taskDetail.returnRouteStatus) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="taskDetailVisible = false">关闭</el-button></template>
    </el-dialog>
    <el-dialog v-model="createVisible" title="创建租商联盟" width="460px"
      ><el-form :model="createForm" label-width="100px"
        ><el-form-item label="联盟名称" required
          ><el-input
            v-model="createForm.allianceName"
            maxlength="128" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="createVisible = false">取消</el-button
        ><el-button type="primary" @click="create">确定</el-button></template
      ></el-dialog
    >
    <el-dialog v-model="memberVisible" title="添加联盟成员" width="460px"
      ><el-form label-width="120px"
        ><el-form-item label="商户" required
          ><el-select
            v-model="memberForm.memberId"
            clearable
            filterable
            placeholder="请选择商户"
            style="width: 100%"
            ><el-option
              v-for="item in merchantOptions"
              :key="item.memberId"
              :label="item.merchantName"
              :value="item.memberId" /></el-select></el-form-item></el-form
      ><template #footer
        ><el-button @click="memberVisible = false">取消</el-button
        ><el-button type="primary" @click="addMember">确定</el-button></template
      ></el-dialog
    >
    <el-dialog v-model="acceptVisible" title="接受协助订单" width="460px"
      ><el-form label-width="120px"
        ><el-form-item label="履约仓库" required
          ><el-select
            v-model="acceptForm.warehouseId"
            clearable
            filterable
            placeholder="请选择发货仓库"
            style="width: 100%"
            ><el-option
              v-for="item in warehouseOptions"
              :key="item.warehouseId"
              :label="`${item.warehouseName}（${item.warehouseCode}）`"
              :value="item.warehouseId" /></el-select></el-form-item></el-form
      ><template #footer
        ><el-button @click="acceptVisible = false">取消</el-button
        ><el-button type="primary" @click="confirmAcceptTask"
          >确定</el-button
        ></template
      ></el-dialog
    >
    <el-dialog
      v-model="changeWarehouseVisible"
      title="更换履约仓库"
      width="460px"
      ><el-form label-width="120px"
        ><el-form-item label="履约仓库" required
          ><el-select
            v-model="changeWarehouseForm.warehouseId"
            clearable
            filterable
            placeholder="请选择履约仓库"
            style="width: 100%"
            ><el-option
              v-for="item in warehouseOptions"
              :key="item.warehouseId"
              :label="`${item.warehouseName}（${item.warehouseCode}）`"
              :value="item.warehouseId" /></el-select></el-form-item></el-form
      ><template #footer
        ><el-button @click="changeWarehouseVisible = false">取消</el-button
        ><el-button type="primary" @click="confirmChangeWarehouse"
          >确定</el-button
        ></template
      ></el-dialog
    >
    <el-dialog v-model="reassignVisible" title="转派协助订单" width="460px"
      ><el-form label-width="120px"
        ><el-form-item label="接收商户" required
          ><el-select
            v-model="reassignForm.targetMemberId"
            clearable
            filterable
            placeholder="请选择接收商户"
            style="width: 100%"
            ><el-option
              v-for="item in reassignOptions"
              :key="item.memberId"
              :label="item.merchantName"
              :value="item.memberId" /></el-select></el-form-item></el-form
      ><template #footer
        ><el-button @click="reassignVisible = false">取消</el-button
        ><el-button type="primary" @click="confirmReassign"
          >确定</el-button
        ></template
      ></el-dialog
    >
    <el-dialog v-model="createDisputeVisible" title="发起联盟纠纷" width="560px"
      ><el-form :model="disputeForm" label-width="120px"
        ><el-form-item label="订单编码" required
          ><el-select
            v-model="disputeForm.allianceOrderId"
            clearable
            filterable
            placeholder="请选择订单"
            style="width: 100%"
            ><el-option
              v-for="item in tasks"
              :key="item.allianceOrderId"
              :label="item.orderNo || '协助订单'"
              :value="item.allianceOrderId" /></el-select></el-form-item
        ><el-form-item label="纠纷类型" required
          ><el-select
            v-model="disputeForm.disputeType"
            placeholder="请选择"
            style="width: 100%"
            ><el-option label="损坏" :value="1" /><el-option
              label="丢失"
              :value="2" /><el-option label="延迟履约" :value="3" /><el-option
              label="费用"
              :value="4" /><el-option
              label="其他"
              :value="9" /></el-select></el-form-item
        ><el-form-item label="申请赔付金额"
          ><el-input-number
            v-model="disputeForm.claimAmount"
            :min="0"
            :precision="2"
            :step="10"
            controls-position="right" /></el-form-item
        ><el-form-item label="纠纷说明" required
          ><el-input
            v-model="disputeForm.description"
            type="textarea"
            maxlength="2000"
            show-word-limit
            :rows="4" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="createDisputeVisible = false">取消</el-button
        ><el-button type="primary" @click="submitCreateDispute"
          >确定</el-button
        ></template
      ></el-dialog
    >
    <el-dialog v-model="disputeDetailVisible" title="联盟纠纷详情" width="760px"
      ><el-descriptions v-if="disputeDetail" :column="2" border label-width="120"
        ><el-descriptions-item label="订单编码">{{
          disputeDetail.orderNo || "-"
        }}</el-descriptions-item
        ><el-descriptions-item label="纠纷类型">{{
          disputeTypeText(disputeDetail.disputeType)
        }}</el-descriptions-item
        ><el-descriptions-item label="状态">{{
          disputeStatusText(disputeDetail.disputeStatus)
        }}</el-descriptions-item
        ><el-descriptions-item label="申请赔付金额">{{
          disputeDetail.claimAmount
        }}</el-descriptions-item
        ><el-descriptions-item label="最终赔付金额">{{
          disputeDetail.awardedAmount
        }}</el-descriptions-item
        ><el-descriptions-item label="纠纷说明" :span="2">{{
          disputeDetail.description
        }}</el-descriptions-item></el-descriptions
      ><el-divider content-position="left">证据</el-divider
      ><el-table :data="disputeDetail?.evidence || []" border size="small"
        ><el-table-column label="证据类型" prop="evidenceType" width="100"
          ><template #default="scope">{{
            evidenceTypeText(scope.row.evidenceType)
          }}</template></el-table-column
        ><el-table-column label="内容" prop="content" min-width="240"
          ><template #default="scope">{{
            scope.row.content || scope.row.fileName || scope.row.fileUrl || "-"
          }}</template></el-table-column
        ><el-table-column
          label="提交时间"
          prop="createTime"
          min-width="160" /></el-table
      ><el-form
        v-if="disputeDetail && Number(disputeDetail.disputeStatus) < 4"
        :model="evidenceForm"
        inline
        class="evidence-form"
        ><el-form-item label="证据类型"
          ><el-select v-model="evidenceForm.evidenceType" style="width: 120px"
            ><el-option label="图片" :value="1" /><el-option
              label="视频"
              :value="2" /><el-option label="文件" :value="3" /><el-option
              label="文字"
              :value="4" /></el-select></el-form-item
        ><el-form-item v-if="evidenceForm.evidenceType === 4" label="证据内容"
          ><el-input
            v-model="evidenceForm.content"
            placeholder="请输入证据内容"
            clearable /></el-form-item
        ><el-form-item v-else label="文件地址"
          ><el-input
            v-model="evidenceForm.fileUrl"
            placeholder="请输入已上传文件地址"
            clearable /></el-form-item
        ><el-form-item
          ><el-button type="primary" @click="submitEvidence"
            >添加证据</el-button
          ></el-form-item
        ></el-form
      ><el-divider content-position="left">当前调解</el-divider
      ><el-form v-if="disputeDetail?.mediation" :model="mediationForm" inline
        ><el-form-item label="调解方案">{{
          disputeDetail.mediation.proposalContent
        }}</el-form-item
        ><el-form-item label="赔付金额">{{
          disputeDetail.mediation.proposalAmount
        }}</el-form-item
        ><el-form-item
          ><el-button type="primary" @click="confirmMediation(true)"
            >接受</el-button
          ><el-button type="danger" @click="confirmMediation(false)"
            >拒绝</el-button
          ></el-form-item
        ></el-form
      ><el-empty v-else description="暂无调解方案"
    /></el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getAlliance,
  createAlliance,
  addAllianceMember,
  listAllianceMemberOptions,
  updateAllianceMember,
  removeAllianceMember,
  getAllianceScopes,
  saveAllianceWarehouseScope,
  saveAllianceProductScope,
  removeAllianceWarehouseScope,
  removeAllianceProductScope,
  listSharedInventory,
  listAllianceTasks,
  acceptAllianceTask,
  changeAllianceWarehouse,
  rejectAllianceTask,
  transferAllianceTask,
  cancelAllianceTask,
  publishAlliancePublicPool,
  listAllianceDisputes,
  getAllianceDispute,
  createAllianceDispute,
  addAllianceDisputeEvidence,
  confirmAllianceMediation,
} from "@/api/merchant/alliance";
import {
  listMerchantWarehouses,
  listMerchantProducts,
} from "@/api/merchant/rentalBusiness";
import { getExpressCompanyName } from "@/utils/rental/expressCompany";

const activeTab = ref("relations");
const loading = ref(false);
const alliance = ref(null);
const members = ref([]);
const sharedInventory = ref([]);
const tasks = ref([]);
const disputes = ref([]);
const inventoryTotal = ref(0);
const taskTotal = ref(0);
const disputeTotal = ref(0);
const pendingTaskCount = ref(0);
const currentMemberId = ref(null);
const taskStatus = ref(null);
const scopeDetailVisible = ref(false);
const scopeDetailTitle = ref("");
const scopeDetailItems = ref([]);
const taskDetailVisible = ref(false);
const taskDetail = ref(null);
const createVisible = ref(false);
const memberVisible = ref(false);
const acceptVisible = ref(false);
const changeWarehouseVisible = ref(false);
const reassignVisible = ref(false);
const createDisputeVisible = ref(false);
const disputeDetailVisible = ref(false);
const disputeDetail = ref(null);
const createForm = reactive({ allianceName: "" });
const memberForm = reactive({ memberId: null });
const acceptForm = reactive({ allianceOrderId: null, warehouseId: null });
const changeWarehouseForm = reactive({
  allianceOrderId: null,
  warehouseId: null,
});
const reassignForm = reactive({ allianceOrderId: null, targetMemberId: null });
const disputeForm = reactive({
  allianceOrderId: null,
  disputeType: 1,
  claimAmount: 0,
  description: "",
});
const evidenceForm = reactive({
  evidenceType: 4,
  fileUrl: "",
  fileName: "",
  content: "",
});
const mediationForm = reactive({});
const warehouseOptions = ref([]);
const productOptions = ref([]);
const merchantOptions = ref([]);
const reassignOptions = ref([]);
const warehouseScopes = ref([]);
const productScopes = ref([]);
const warehouseScopeForm = reactive({
  warehouseId: null,
  acceptOrderEnabled: 1,
});
const productScopeForm = reactive({
  productId: null,
  shareQuantityRatio: 1,
  shareQuantityLimit: null,
  acceptOrderEnabled: 1,
});
const inventoryQuery = reactive({
  keyword: "",
  productType: "",
  pageNum: 1,
  pageSize: 10,
});
const taskQuery = reactive({ pageNum: 1, pageSize: 10 });
const disputeQuery = reactive({
  disputeStatus: undefined,
  pageNum: 1,
  pageSize: 10,
});
function unwrap(data) {
  return data?.data ?? data;
}
const currentMember = computed(() =>
  members.value.find(
    (item) => Number(item.memberId) === Number(currentMemberId.value),
  ),
);
async function loadRelations() {
  loading.value = true;
  try {
    const result = unwrap(await getAlliance());
    currentMemberId.value = result?.memberId;
    alliance.value = result?.alliance || null;
    members.value = result?.members || [];
    if (alliance.value) {
      await Promise.all([loadScopes(), loadPendingTaskCount()]);
    }
  } finally {
    loading.value = false;
  }
}
async function loadPendingTaskCount() {
  const result = unwrap(
    await listAllianceTasks({ pageNum: 1, pageSize: 1, status: 0 }),
  );
  pendingTaskCount.value = Number(result?.total || 0);
}
async function loadScopes() {
  const [scopeResult, warehouseResult, productResult] = await Promise.all([
    getAllianceScopes(),
    listMerchantWarehouses({ pageNum: 1, pageSize: 500 }),
    listMerchantProducts({ pageNum: 1, pageSize: 500 }),
  ]);
  const scopes = unwrap(scopeResult) || {};
  warehouseScopes.value = scopes.warehouses || [];
  productScopes.value = scopes.products || [];
  warehouseOptions.value = unwrap(warehouseResult)?.rows || [];
  productOptions.value = unwrap(productResult)?.rows || [];
}
async function saveWarehouseScope() {
  console.log(warehouseScopeForm);
  if (!warehouseScopeForm.warehouseId)
    return ElMessage.warning("请选择共享仓库");
  await saveAllianceWarehouseScope({
    warehouseId: warehouseScopeForm.warehouseId,
    acceptOrderEnabled: warehouseScopeForm.acceptOrderEnabled,
  });
  Object.assign(warehouseScopeForm, {
    warehouseId: null,
    acceptOrderEnabled: 1,
  });
  await loadScopes();
  ElMessage.success("共享仓库已保存");
}
async function saveProductScope() {
  console.log(productScopeForm);
  if (!productScopeForm.productId) return ElMessage.warning("请选择共享商品");
  await saveAllianceProductScope({
    productId: productScopeForm.productId,
    shareQuantityRatio: productScopeForm.shareQuantityRatio,
    shareQuantityLimit: productScopeForm.shareQuantityLimit,
    acceptOrderEnabled: productScopeForm.acceptOrderEnabled,
  });
  Object.assign(productScopeForm, {
    productId: null,
    shareQuantityRatio: 1,
    shareQuantityLimit: null,
    acceptOrderEnabled: 1,
  });
  await loadScopes();
  ElMessage.success("共享商品已保存");
}
async function deleteWarehouseScope(row) {
  await ElMessageBox.confirm(
    `确认取消共享仓库“${row.warehouseName}”吗？`,
    "提示",
    { type: "warning" },
  );
  await removeAllianceWarehouseScope(row.scopeId);
  await loadScopes();
  ElMessage.success("共享仓库已删除");
}
async function deleteProductScope(row) {
  await ElMessageBox.confirm(
    `确认取消共享商品“${row.productName}”吗？`,
    "提示",
    { type: "warning" },
  );
  await removeAllianceProductScope(row.scopeId);
  await loadScopes();
  ElMessage.success("共享商品已删除");
}
async function create() {
  if (!createForm.allianceName.trim())
    return ElMessage.warning("请输入联盟名称");
  await createAlliance(createForm);
  createVisible.value = false;
  createForm.allianceName = "";
  await loadRelations();
  ElMessage.success("联盟创建成功");
}
async function openMemberDialog() {
  merchantOptions.value = unwrap(await listAllianceMemberOptions()) || [];
  memberForm.memberId = null;
  memberVisible.value = true;
}
async function addMember() {
  if (!memberForm.memberId) return ElMessage.warning("请选择商户");
  await addAllianceMember(memberForm);
  memberVisible.value = false;
  memberForm.memberId = null;
  await loadRelations();
  ElMessage.success("联盟成员已添加");
}
async function saveMember(row) {
  await updateAllianceMember(row.memberId, {
    shareStockEnabled: row.shareStockEnabled,
    acceptOrderLimit: row.acceptOrderLimit,
  });
  ElMessage.success("成员配置已保存");
}
async function deleteMember(row) {
  await ElMessageBox.confirm(`确认移除商户“${row.merchantName}”吗？`, "提示", {
    type: "warning",
  });
  await removeAllianceMember(row.memberId);
  await loadRelations();
  ElMessage.success("成员已移除");
}
async function loadInventory() {
  loading.value = true;
  try {
    const result = unwrap(await listSharedInventory(inventoryQuery));
    sharedInventory.value = result?.rows || result || [];
    inventoryTotal.value = result?.total || 0;
  } finally {
    loading.value = false;
  }
}
function resetInventory() {
  inventoryQuery.keyword = "";
  inventoryQuery.productType = "";
  inventoryQuery.pageNum = 1;
  loadInventory();
}
async function loadTasks() {
  loading.value = true;
  try {
    const result = unwrap(
      await listAllianceTasks({ ...taskQuery, status: taskStatus.value }),
    );
    tasks.value = result?.rows || result || [];
    taskTotal.value = result?.total || 0;
  } finally {
    loading.value = false;
  }
}
function loadTab(name) {
  if (name === "relations") loadRelations();
  if (name === "inventory") loadInventory();
  if (name === "orders") loadTasks();
  if (name === "disputes") loadDisputes();
}
function openScopeDetail(title, items) {
  scopeDetailTitle.value = title;
  scopeDetailItems.value = items;
  scopeDetailVisible.value = true;
}
function openMemberDetail(row) {
  openScopeDetail("联盟成员详情", [
    { label: "商户名称", value: row.merchantName || "-" },
    { label: "共享库存", value: Number(row.shareStockEnabled) === 1 ? "已开启" : "未开启" },
    { label: "承接订单上限", value: row.acceptOrderLimit ?? "不限" },
    { label: "当前承接数量", value: row.currentAcceptOrderCount ?? 0 },
  ]);
}
function openWarehouseScopeDetail(row) {
  openScopeDetail("共享仓库详情", [
    { label: "仓库名称", value: row.warehouseName || "-" },
    { label: "仓库编码", value: row.warehouseCode || "-" },
    { label: "所在城市", value: row.city || "-" },
    { label: "允许协助订单", value: Number(row.acceptOrderEnabled) === 1 ? "是" : "否" },
  ]);
}
function openProductScopeDetail(row) {
  openScopeDetail("共享商品详情", [
    { label: "商品名称", value: row.productName || "-" },
    { label: "商品编码", value: row.productCode || "-" },
    { label: "共享比例", value: row.shareQuantityRatio ?? "-" },
    { label: "共享数量上限", value: row.shareQuantityLimit ?? "不限" },
    { label: "允许协助订单", value: Number(row.acceptOrderEnabled) === 1 ? "是" : "否" },
  ]);
}
function openTaskDetail(row) {
  taskDetail.value = row;
  taskDetailVisible.value = true;
}
function taskDirection(row) {
  if (row.targetMemberId == null) return "公众池";
  return Number(row.targetMemberId) === Number(currentMemberId.value) ? "收到" : "发出";
}
function taskSummary(row) {
  try {
    const data = JSON.parse(row.maskedSnapshotJson || "{}");
    return (
      [
        data.productName || data.modelName,
        data.packageName,
        data.leaseStartDate && `${data.leaseStartDate} 至 ${data.leaseEndDate}`,
      ]
        .filter(Boolean)
        .join(" / ") || "协助订单"
    );
  } catch {
    return "协助订单";
  }
}
function leaseText(row) {
  return row.leaseStartDate && row.leaseEndDate
    ? `${row.leaseStartDate} 至 ${row.leaseEndDate}`
    : "未设置";
}
function statusText(status) {
  return (
    [
      "待接受",
      "已接受",
      "已拒绝",
      "已取消",
      "已过期",
      "公众池中",
      "履约中",
      "已完成",
      "异常",
    ][status] || "未知"
  );
}
function expressText(carrier, trackingNo) {
  return carrier || trackingNo
    ? `${getExpressCompanyName(carrier) || "未知快递"} / ${trackingNo || "待填写"}`
    : "暂无";
}
function routeStatusText(shippingStatus, returnStatus) {
  const status = returnStatus ?? shippingStatus;
  return (
    ["待处理", "运输中", "已送达", "异常", "已关闭"][Number(status)] || "暂无"
  );
}
async function acceptTask(row) {
  if (!warehouseOptions.value.length) await loadScopes();
  acceptForm.allianceOrderId = row.allianceOrderId;
  acceptForm.warehouseId = null;
  acceptVisible.value = true;
}
async function confirmAcceptTask() {
  if (!acceptForm.warehouseId) return ElMessage.warning("请选择履约仓库");
  await ElMessageBox.confirm(
    "接受后将复制为当前商户的子订单并预占库存，是否继续？",
    "确认接受",
  );
  await acceptAllianceTask(acceptForm.allianceOrderId, {
    warehouseId: acceptForm.warehouseId,
  });
  acceptVisible.value = false;
  await loadTasks();
  ElMessage.success("协助订单已接受");
}
async function openChangeWarehouse(row) {
  if (!warehouseOptions.value.length) await loadScopes();
  changeWarehouseForm.allianceOrderId = row.allianceOrderId;
  changeWarehouseForm.warehouseId = null;
  changeWarehouseVisible.value = true;
}
async function confirmChangeWarehouse() {
  if (!changeWarehouseForm.warehouseId)
    return ElMessage.warning("请选择履约仓库");
  await ElMessageBox.confirm(
    "更换后将释放原仓库预占并重新预占新仓库，是否继续？",
    "确认更换",
  );
  await changeAllianceWarehouse(changeWarehouseForm.allianceOrderId, {
    warehouseId: changeWarehouseForm.warehouseId,
  });
  changeWarehouseVisible.value = false;
  await loadTasks();
  ElMessage.success("履约仓库已更新");
}
async function rejectTask(row) {
  await ElMessageBox.confirm("确认拒绝该协助订单吗？", "确认拒绝");
  await rejectAllianceTask(row.allianceOrderId);
  await loadTasks();
  ElMessage.success("协助订单已拒绝");
}
async function openReassign(row) {
  reassignOptions.value = (
    unwrap(await listAllianceMemberOptions()) || []
  ).filter((item) => Number(item.memberId) !== Number(currentMemberId.value));
  reassignForm.allianceOrderId = row.allianceOrderId;
  reassignForm.targetMemberId = null;
  reassignVisible.value = true;
}
async function confirmReassign() {
  if (!reassignForm.targetMemberId) return ElMessage.warning("请选择接收商户");
  await transferAllianceTask(reassignForm.allianceOrderId, {
    targetMemberId: reassignForm.targetMemberId,
  });
  reassignVisible.value = false;
  await loadTasks();
  ElMessage.success("协助订单已转派");
}
async function cancelTask(row) {
  await ElMessageBox.confirm(
    "取消后该协助订单将不再继续分配，是否继续？",
    "确认取消",
    { type: "warning" },
  );
  await cancelAllianceTask(row.allianceOrderId);
  await loadTasks();
  ElMessage.success("协助订单已取消");
}
async function publishPublicPoolTask(row) {
  await ElMessageBox.confirm("确认将该订单发布到公众池吗？", "确认发布");
  await publishAlliancePublicPool(row.allianceOrderId);
  await loadTasks();
  ElMessage.success("订单已发布到公众池");
}
async function loadDisputes() {
  loading.value = true;
  try {
    const result = unwrap(await listAllianceDisputes(disputeQuery));
    disputes.value = result?.rows || result || [];
    disputeTotal.value = Number(result?.total || 0);
  } finally {
    loading.value = false;
  }
}
async function openCreateDispute() {
  const result = unwrap(await listAllianceTasks({ pageNum: 1, pageSize: 500 }));
  tasks.value = result?.rows || result || [];
  Object.assign(disputeForm, {
    allianceOrderId: null,
    disputeType: 1,
    claimAmount: 0,
    description: "",
  });
  createDisputeVisible.value = true;
}
async function submitCreateDispute() {
  if (!disputeForm.allianceOrderId || !disputeForm.description.trim())
    return ElMessage.warning("请填写联盟订单和纠纷说明");
  await createAllianceDispute(disputeForm);
  createDisputeVisible.value = false;
  await loadDisputes();
  ElMessage.success("纠纷已提交");
}
async function openDispute(row) {
  const result = unwrap(await getAllianceDispute(row.disputeId));
  disputeDetail.value = result;
  Object.assign(evidenceForm, {
    evidenceType: 4,
    fileUrl: "",
    fileName: "",
    content: "",
  });
  disputeDetailVisible.value = true;
}
async function submitEvidence() {
  if (
    evidenceForm.evidenceType === 4
      ? !evidenceForm.content.trim()
      : !evidenceForm.fileUrl.trim()
  )
    return ElMessage.warning(
      evidenceForm.evidenceType === 4 ? "请输入证据内容" : "请输入文件地址",
    );
  await addAllianceDisputeEvidence(disputeDetail.value.disputeId, evidenceForm);
  disputeDetail.value = unwrap(
    await getAllianceDispute(disputeDetail.value.disputeId),
  );
  Object.assign(evidenceForm, { fileUrl: "", fileName: "", content: "" });
  ElMessage.success("证据已添加");
}
async function confirmMediation(accepted) {
  await confirmAllianceMediation(disputeDetail.value.disputeId, { accepted });
  disputeDetailVisible.value = false;
  await loadDisputes();
  ElMessage.success(accepted ? "已接受调解" : "已拒绝调解");
}
function disputeTypeText(value) {
  return (
    { 1: "损坏", 2: "丢失", 3: "延迟履约", 4: "费用", 9: "其他" }[
      Number(value)
    ] || "未知"
  );
}
function evidenceTypeText(value) {
  return (
    { 1: "图片", 2: "视频", 3: "文件", 4: "文字" }[Number(value)] || "未知"
  );
}
function disputeStatusText(value) {
  return (
    ["待处理", "举证中", "调解中", "待确认", "已解决", "已驳回", "已关闭"][
      Number(value)
    ] || "未知"
  );
}
onMounted(loadRelations);
</script>

<style scoped>
.alliance-page {
  width: 100%;
}
.alliance-overview {
  margin-bottom: 16px;
}
.alliance-toolbar {
  margin-top: 16px;
}
.scope-toolbar {
  margin-bottom: 12px;
}
.scope-table {
  margin-top: 12px;
}
.search-form {
  padding: 16px 18px 2px;
  margin-bottom: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-bg-color);
}
</style>
