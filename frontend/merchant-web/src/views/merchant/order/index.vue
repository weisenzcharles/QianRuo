<template>
  <div class="app-container merchant-order-page">
    <header class="page-heading">
      <div>
        <p class="page-kicker">ORDER CENTER</p>
        <h2>订单中心</h2>
        <p class="page-description">
          集中处理录单、备货、发货、归还和售后节点，订单信息按实际工作流展开。
        </p>
      </div>
      <div class="heading-actions">
        <el-tag type="info" effect="plain">今日更新 {{ lastUpdated }}</el-tag>
        <el-button type="primary" :icon="Plus" @click="openCreateOrder">
          新建订单
        </el-button>
      </div>
    </header>

    <section class="summary-strip" aria-label="订单概览">
      <button
        v-for="item in summaryItems"
        :key="item.value"
        type="button"
        :class="['summary-item', { 'is-active': isSummaryItemActive(item) }]"
        @click="selectSummaryItem(item)">
        <span class="summary-label">{{ item.label }}</span>
        <strong>{{ item.count }}</strong>
        <span :class="['summary-trend', item.trendType]">{{ item.trend }}</span>
      </button>
    </section>

    <section v-show="showSearch" class="filter-panel" aria-label="订单筛选">
      <el-form
        v-show="showSearch"
        :model="queryParams"
        :inline="true"
        @submit.prevent>
        <el-form-item label="订单搜索">
          <el-input
            v-model="queryParams.keyword"
            clearable
            placeholder="订单编号、客户、商品或快递单号"
            class="keyword-input"
            @keyup.enter="handleQuery">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select
            v-model="queryParams.status"
            clearable
            placeholder="全部状态"
            class="filter-select"
            @change="handleQuery">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单渠道">
          <el-select
            v-model="queryParams.channelId"
            clearable
            placeholder="全部渠道"
            class="filter-select"
            @change="handleQuery">
            <el-option
              v-for="item in channelOptions"
              :key="item.channelId"
              :label="item.channelName"
              :value="item.channelId" />
          </el-select>
        </el-form-item>
        <el-form-item label="发货仓库">
          <el-select
            v-model="queryParams.warehouseId"
            clearable
            filterable
            placeholder="全部仓库"
            class="filter-select"
            @change="handleQuery">
            <el-option
              v-for="item in warehouseOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期类型">
          <el-select
            v-model="queryParams.dateType"
            class="filter-select"
            @change="handleQuery">
            <el-option
              v-for="item in orderDateTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期筛选">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            clearable
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="date-range-input"
            @change="handleQuery" />
        </el-form-item>
        <el-form-item label="排序类型">
          <el-select
            v-model="queryParams.sortType"
            class="filter-select"
            @change="handleQuery">
            <el-option
              v-for="item in orderSortTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序方式">
          <el-select
            v-model="queryParams.sortDirection"
            class="filter-select"
            @change="handleQuery">
            <el-option
              v-for="item in orderSortDirectionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" :icon="Search" @click="handleQuery">
            搜索
          </el-button>
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
          <h3>
            订单列表
            <el-tag
              v-if="currentOrderScopeTitle"
              class="order-scope-tag"
              type="primary"
              size="small"
              effect="plain">
              {{ currentOrderScopeTitle }}
            </el-tag>
          </h3>
          <span>共 {{ total }} 条订单</span>
        </div>
        <div class="list-toolbar-actions">
          <el-button
            type="primary"
            plain
            :icon="VideoCamera"
            @click="openBatchMediaDialog">
            批量上传视频
          </el-button>
          <el-radio-group
            v-model="viewMode"
            class="order-view-switch"
            size="small"
            aria-label="订单列表模式">
            <el-radio-button value="card">卡片模式</el-radio-button>
            <el-radio-button value="compact">精简模式</el-radio-button>
          </el-radio-group>
          <el-checkbox
            v-model="queryParams.attentionOnly"
            @change="handleQuery">
            仅看待处理
          </el-checkbox>
        </div>
      </div>

      <el-empty
        v-if="orders.length === 0 && !loading"
        description="没有符合条件的订单"
        :image-size="96" />
      <div v-else class="order-scroll-area">
        <template v-if="viewMode === 'card'">
          <div class="order-grid order-grid-header">
            <div>订单信息</div>
            <div>客户信息</div>
            <div>发货安排</div>
            <div>气象雷达</div>
            <div>配送方案</div>
            <div>订单状态</div>
            <div>商品清单</div>
          </div>

          <article
            v-for="order in filteredOrders"
            :key="order.orderNo"
            class="order-card">
            <div class="order-grid order-grid-content">
              <section class="order-cell order-overview">
                <div class="mobile-cell-title">订单信息</div>
                <div class="product-summary">
                  <dt>
                    <div class="product-thumb" aria-hidden="true">
                      <Camera />
                    </div>
                  </dt>
                  <dd>
                    <!-- <div> -->
                    <p>
                      <el-tag
                        v-if="
                          order.isAllianceChild || order.isAllianceSourceLocked
                        "
                        type="warning"
                        size="small">
                        联盟
                      </el-tag>
                      <strong>{{ order.model }}</strong>
                    </p>
                    <span>{{ order.packageName }}</span>
                    <span class="order-number-line">
                      订单编码：
                      <el-link
                        type="primary"
                        :underline="false"
                        @click="openOrderDetail(order)">
                        {{ order.orderNo || "-" }}
                      </el-link>
                    </span>
                    <!-- <span>外部单号：{{ order.externalOrderNo }}</span> -->
                    <!-- </div> -->
                  </dd>
                </div>
                <dl class="detail-list">
                  <div>
                    <dt>外部单号</dt>
                    <dd>{{ order.externalOrderNo }}</dd>
                  </div>
                  <div>
                    <dt>租期</dt>
                    <dd>
                      {{ order.rentalPeriod }}（共 {{ order.leaseDays }} 天）
                    </dd>
                  </div>
                  <div>
                    <dt>归还</dt>
                    <dd>{{ order.returnDate }}</dd>
                  </div>
                  <div>
                    <dt>租金</dt>
                    <dd class="money">¥{{ order.rent }}</dd>
                  </div>
                  <div>
                    <dt>渠道</dt>
                    <dd>
                      {{ order.channel }}
                      <em>商户：{{ order.merchant }}</em>
                    </dd>
                  </div>
                </dl>
              </section>

              <section class="order-cell">
                <div class="mobile-cell-title">客户信息</div>
                <div class="customer-name">
                  <User />
                  <span>{{ order.recipientName || order.customer.name }}</span>
                  <el-button
                    link
                    type="primary"
                    :icon="Edit"
                    aria-label="编辑客户信息"
                    title="编辑客户信息"
                    @click="openCustomerEdit(order)" />
                </div>
                <dl class="detail-list">
                  <div>
                    <dt>电话</dt>
                    <dd>
                      {{ order.recipientPhone || order.customer.phone }}
                    </dd>
                  </div>
                  <div>
                    <dt>地址</dt>
                    <dd>
                      {{ order.recipientAddress || order.customer.address }}
                    </dd>
                  </div>
                  <div>
                    <dt>备注</dt>
                    <dd class="cell-note">
                      <InfoFilled />
                      {{ order.customer.note || "暂无客户备注" }}
                    </dd>
                  </div>
                </dl>
              </section>

              <section class="order-cell">
                <div class="mobile-cell-title">发货安排</div>
                <dl class="detail-list schedule-list">
                  <div>
                    <dt>发货仓</dt>
                    <dd>{{ order.shipping.warehouse }}</dd>
                  </div>
                  <div>
                    <dt>发货日</dt>
                    <dd>{{ order.shipping.shipDate }}</dd>
                  </div>
                  <div>
                    <dt>方式</dt>
                    <dd>{{ order.shipping.method }}</dd>
                  </div>
                  <div>
                    <dt>上门</dt>
                    <dd>{{ order.shipping.doorTime }}</dd>
                  </div>
                </dl>
                <!-- <p class="cell-note">
                  <InfoFilled /> {{ order.shipping.note }}
                </p> -->
              </section>

              <section class="order-cell weather-cell">
                <div class="mobile-cell-title">气象雷达</div>
                <div class="route-line">
                  <Location />
                  {{ order.weather.route }}
                </div>
                <div class="weather-line">
                  <span class="weather-label">发货地</span>
                  <div class="weather-summary">
                    <span class="weather-summary-item">
                      <span class="weather-period">今日</span>
                      <i
                        :class="order.weather.today.departIcon"
                        role="img"
                        :title="
                          weatherDetailText(order.weather.today.depart, '今日')
                        "
                        :aria-label="
                          weatherDetailText(order.weather.today.depart, '今日')
                        ">
                        {{ weatherIconSymbol(order.weather.today.departIcon) }}
                      </i>
                    </span>
                    <span class="weather-divider" aria-hidden="true">|</span>
                    <span class="weather-summary-item">
                      <span class="weather-period">明日</span>
                      <i
                        :class="order.weather.tomorrow.departIcon"
                        role="img"
                        :title="
                          weatherDetailText(
                            order.weather.tomorrow.depart,
                            '明日',
                          )
                        "
                        :aria-label="
                          weatherDetailText(
                            order.weather.tomorrow.depart,
                            '明日',
                          )
                        ">
                        {{
                          weatherIconSymbol(order.weather.tomorrow.departIcon)
                        }}
                      </i>
                    </span>
                  </div>
                </div>
                <div class="weather-line">
                  <span class="weather-label">收货地</span>
                  <div class="weather-summary">
                    <span class="weather-summary-item">
                      <span class="weather-period">今日</span>
                      <i
                        :class="order.weather.today.arriveIcon"
                        role="img"
                        :title="
                          weatherDetailText(order.weather.today.arrive, '今日')
                        "
                        :aria-label="
                          weatherDetailText(order.weather.today.arrive, '今日')
                        ">
                        {{ weatherIconSymbol(order.weather.today.arriveIcon) }}
                      </i>
                    </span>
                    <span class="weather-divider" aria-hidden="true">|</span>
                    <span class="weather-summary-item">
                      <span class="weather-period">明日</span>
                      <i
                        :class="order.weather.tomorrow.arriveIcon"
                        role="img"
                        :title="
                          weatherDetailText(
                            order.weather.tomorrow.arrive,
                            '明日',
                          )
                        "
                        :aria-label="
                          weatherDetailText(
                            order.weather.tomorrow.arrive,
                            '明日',
                          )
                        ">
                        {{
                          weatherIconSymbol(order.weather.tomorrow.arriveIcon)
                        }}
                      </i>
                    </span>
                  </div>
                </div>
                <el-tag
                  size="small"
                  :type="order.weather.risk ? 'warning' : 'success'"
                  effect="plain">
                  {{ order.weather.risk ? "存在天气提醒" : "天气正常" }}
                </el-tag>
              </section>

              <section class="order-cell delivery-cell">
                <div class="mobile-cell-title">配送方案</div>
                <div
                  v-for="plan in order.delivery.plans"
                  :key="plan.name"
                  class="delivery-plan">
                  <span class="carrier-mark">{{ getExpressCompanyName(plan.carrier) }}</span>
                  <span>{{ plan.name }}</span>
                  <b>¥{{ plan.price }}</b>
                </div>
                <el-button
                  link
                  type="primary"
                  :icon="Calendar"
                  @click="handleAction('立即预约', order)">
                  立即预约
                </el-button>
                <div class="delivery-after">
                  预计到库：{{ order.delivery.expectedDate }}
                </div>
              </section>

              <section class="order-cell status-cell">
                <div class="mobile-cell-title">订单状态</div>
                <el-tag
                  :type="orderStatusType(order.status)"
                  effect="light"
                  class="status-tag">
                  {{ orderStatusLabel(order.status) }}
                </el-tag>
                <dl class="detail-list">
                  <div>
                    <dt>寄出单号</dt>
                    <dd class="logistics-value">
                      <el-link
                        v-if="hasLogisticsTrackingNo(order.trackingNo)"
                        type="primary"
                        :underline="false"
                        title="查看寄出物流轨迹"
                        @click="openLogisticsTracking(order, 'ship')">
                        {{ order.trackingNo }}
                      </el-link>
                      <span v-else>{{ order.trackingNo }}</span>
                      <el-button
                        link
                        type="primary"
                        :icon="Edit"
                        :disabled="isLogisticsEditDisabled(order)"
                        :title="logisticsEditDisabledReason(order)"
                        aria-label="编辑寄出物流"
                        @click="openLogisticsEdit(order, 'ship')" />
                    </dd>
                  </div>
                  <div>
                    <dt>归还单号</dt>
                    <dd class="logistics-value">
                      <el-link
                        v-if="hasLogisticsTrackingNo(order.returnNo)"
                        type="primary"
                        :underline="false"
                        title="查看归还物流轨迹"
                        @click="openLogisticsTracking(order, 'return')">
                        {{ order.returnNo }}
                      </el-link>
                      <span v-else>{{ order.returnNo }}</span>
                      <el-button
                        link
                        type="primary"
                        :icon="Edit"
                        :disabled="isLogisticsEditDisabled(order)"
                        :title="logisticsEditDisabledReason(order)"
                        aria-label="编辑归还物流"
                        @click="openLogisticsEdit(order, 'return')" />
                    </dd>
                  </div>
                  <div>
                    <dt>预计到库</dt>
                    <dd>{{ order.delivery.expectedDate }}</dd>
                  </div>
                </dl>
              </section>

              <section class="order-cell product-list-cell">
                <div class="mobile-cell-title">商品清单</div>
                <strong>
                  {{ order.packageName }}
                  <el-button
                    link
                    type="primary"
                    @click="openPackageDetail(order)">
                    详情
                  </el-button>
                </strong>
                <p>主品编号：{{ order.productCode }}</p>
                <p v-if="order.packageItems.length" class="addon-title">
                  <ShoppingCart />
                  套餐明细：
                </p>
                <p
                  v-for="(item, itemIndex) in order.packageItems"
                  :key="`package-${order.orderNo}-${itemIndex}-${item}`"
                  class="addon-item">
                  {{ item }}
                </p>
                <p v-if="order.addOns.length" class="addon-title">
                  <ShoppingCart />
                  额外配件：
                </p>
                <p
                  v-for="addon in order.addOns"
                  :key="addon"
                  class="addon-item">
                  {{ addon }}
                </p>
              </section>
            </div>

            <footer class="order-footer">
              <div class="attention-note" v-if="order.attention">
                <Bell />
                <span>批注：</span>
                <el-link
                  class="attention-link"
                  type="primary"
                  :underline="false"
                  @click="openAnnotationHistory(order)">
                  {{ order.attention }}
                </el-link>
              </div>
              <div class="attention-note muted" v-else>
                <Bell />
                <span>当前订单暂无批注。</span>
              </div>
              <div class="order-actions">
                <el-button
                  v-for="action in order.actions"
                  :key="action.label"
                  text
                  :type="action.type || 'default'"
                  :icon="action.icon"
                  :disabled="action.disabled"
                  :title="action.disabledReason || action.label"
                  @click="handleAction(action.label, order)">
                  {{ action.label }}
                </el-button>
                <span class="operator-meta">
                  录入人：{{ order.operator }}　对接人：{{ order.coordinator }}
                </span>
              </div>
            </footer>
          </article>
        </template>

        <div v-else class="compact-order-table-wrap">
          <table class="compact-order-table">
            <colgroup>
              <col class="compact-col-recipient" />
              <col class="compact-col-phone" />
              <col class="compact-col-channel" />
              <col class="compact-col-period" />
              <col class="compact-col-status" />
              <col class="compact-col-shipping-tracking" />
              <col class="compact-col-return-tracking" />
              <col class="compact-col-shipping" />
              <col class="compact-col-warehouse" />
              <col class="compact-col-product" />
              <col class="compact-col-package-items" />
              <col class="compact-col-addon" />
              <col class="compact-col-return" />
              <col class="compact-col-receipt" />
              <col class="compact-col-actions" />
            </colgroup>
            <thead>
              <tr>
                <th>收件人 / 昵称</th>
                <th>手机号</th>
                <th>渠道 / 店铺</th>
                <th>租期</th>
                <th>订单状态</th>
                <th>寄出单号</th>
                <th>归还单号</th>
                <th>发货安排</th>
                <th>发货仓</th>
                <th>套餐 / 型号</th>
                <th>套餐明细</th>
                <th>额外配件 +</th>
                <th>归还 / 到库</th>
                <th>收货 / 备注</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="order in filteredOrders" :key="order.orderNo">
                <tr class="compact-order-row">
                  <td>
                    <div class="compact-recipient-name">
                      <strong class="compact-primary">
                        {{ order.recipientName || order.customer.name }}
                      </strong>
                      <el-button
                        link
                        type="primary"
                        :icon="Edit"
                        aria-label="编辑客户信息"
                        title="编辑客户信息"
                        @click="openCustomerEdit(order)" />
                    </div>
                    <span class="compact-secondary">
                      {{ order.customer.nickname }}
                    </span>
                  </td>
                  <td>{{ order.recipientPhone || order.customer.phone }}</td>
                  <td>
                    <span class="compact-primary">{{ order.channel }}</span>
                    <span class="compact-secondary">{{ order.merchant }}</span>
                  </td>
                  <td>
                    <span class="compact-primary">
                      {{ order.rentalPeriod }}
                    </span>
                    <span class="compact-secondary">
                      订单号：
                      <el-link
                        type="primary"
                        :underline="false"
                        @click="openOrderDetail(order)">
                        {{ order.orderNo || "-" }}
                      </el-link>
                    </span>
                  </td>
                  <td>
                    <el-tag
                      :type="orderStatusType(order.status)"
                      effect="light">
                      {{ orderStatusLabel(order.status) }}
                    </el-tag>
                  </td>
                  <td>
                    <span class="compact-logistics-value">
                      <el-link
                        v-if="hasLogisticsTrackingNo(order.trackingNo)"
                        type="primary"
                        :underline="false"
                        title="查看寄出物流轨迹"
                        @click="openLogisticsTracking(order, 'ship')">
                        {{ order.trackingNo }}
                      </el-link>
                      <span v-else>{{ order.trackingNo }}</span>
                      <el-button
                        link
                        type="primary"
                        :icon="Edit"
                        :disabled="isLogisticsEditDisabled(order)"
                        :title="logisticsEditDisabledReason(order)"
                        aria-label="编辑寄出物流"
                        @click="openLogisticsEdit(order, 'ship')" />
                    </span>
                  </td>
                  <td>
                    <span class="compact-logistics-value">
                      <el-link
                        v-if="hasLogisticsTrackingNo(order.returnNo)"
                        type="primary"
                        :underline="false"
                        title="查看归还物流轨迹"
                        @click="openLogisticsTracking(order, 'return')">
                        {{ order.returnNo }}
                      </el-link>
                      <span v-else>{{ order.returnNo }}</span>
                      <el-button
                        link
                        type="primary"
                        :icon="Edit"
                        :disabled="isLogisticsEditDisabled(order)"
                        :title="logisticsEditDisabledReason(order)"
                        aria-label="编辑归还物流"
                        @click="openLogisticsEdit(order, 'return')" />
                    </span>
                  </td>
                  <td>
                    <span class="compact-primary">
                      {{ order.shipping.shipDate }}
                    </span>
                    <span class="compact-secondary">
                      {{ order.shipping.doorTime }} ·
                      {{ order.shipping.method }}
                    </span>
                  </td>
                  <td>{{ order.shipping.warehouse }}</td>
                  <td>
                    <span class="compact-primary">
                      {{ order.packageName }}
                      <el-tag
                        v-if="
                          order.isAllianceChild || order.isAllianceSourceLocked
                        "
                        type="warning"
                        size="small">
                        商户联盟
                      </el-tag>
                    </span>
                    <span class="compact-secondary">{{ order.model }}</span>
                  </td>
                  <td>
                    <span
                      v-if="order.packageItems.length"
                      class="compact-multiline">
                      {{ order.packageItems.join("、") }}
                    </span>
                    <span v-else class="compact-secondary">暂无套餐明细</span>
                  </td>
                  <td>
                    <span v-if="order.addOns.length" class="compact-multiline">
                      {{ order.addOns.join("、") }}
                    </span>
                    <span v-else class="compact-secondary">暂无配件</span>
                  </td>
                  <td>
                    <span class="compact-primary">
                      归还：{{ order.returnDate }}
                    </span>
                    <span class="compact-secondary">
                      到库：{{ order.delivery.expectedDate }}
                    </span>
                  </td>
                  <td>
                    <span class="compact-multiline">
                      {{ order.recipientAddress || order.customer.address }}
                    </span>
                    <span class="compact-secondary">
                      {{ order.customer.note || "暂无客户备注" }}
                    </span>
                  </td>
                  <td class="compact-operation-cell">
                    <el-button
                      link
                      type="primary"
                      @click="openOrderDetail(order)">
                      订单详情
                    </el-button>
                    <el-divider direction="vertical" />
                    <el-button
                      link
                      type="primary"
                      @click="openPackageDetail(order)">
                      详情
                    </el-button>
                  </td>
                </tr>
                <tr class="compact-action-row">
                  <td colspan="15">
                    <div class="compact-action-bar">
                      <div v-if="order.attention" class="attention-note">
                        <Bell />
                        <span>批注：</span>
                        <el-link
                          class="attention-link"
                          type="primary"
                          :underline="false"
                          @click="openAnnotationHistory(order)">
                          {{ order.attention }}
                        </el-link>
                      </div>
                      <div v-else class="attention-note muted">
                        <Bell />
                        <span>当前订单暂无批注。</span>
                      </div>
                      <div class="order-actions">
                        <el-button
                          v-for="action in order.actions"
                          :key="action.label"
                          text
                          :type="action.type || 'default'"
                          :icon="action.icon"
                          :disabled="action.disabled"
                          :title="action.disabledReason || action.label"
                          @click="handleAction(action.label, order)">
                          {{ action.label }}
                        </el-button>
                        <span class="operator-meta">
                          录入人：{{ order.operator }}　对接人：{{
                            order.coordinator
                          }}
                        </span>
                      </div>
                    </div>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadOrders" />

    <el-dialog
      v-model="detailVisible"
      title="套餐详情"
      width="760px"
      append-to-body
      destroy-on-close>
      <template v-if="activeOrder?.packageDetail">
        <el-descriptions :column="1" border label-width="120">
          <el-descriptions-item label="套餐名称">
            {{ activeOrder.packageDetail.packageName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="套餐编码">
            {{ activeOrder.packageDetail.packageCode || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="主商品">
            {{ activeOrder.packageDetail.mainProduct?.name || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="主品编码">
            {{
              activeOrder.packageDetail.mainProduct?.code ||
              activeOrder.productCode ||
              "-"
            }}
          </el-descriptions-item>
          <el-descriptions-item label="产品型号">
            {{ activeOrder.packageDetail.mainProduct?.model || "-" }}
          </el-descriptions-item>
        </el-descriptions>
        <section class="package-detail-section">
          <h4>产品明细</h4>
          <el-table
            v-if="activePackageDetailItems.length"
            :data="activePackageDetailItems"
            border>
            <el-table-column prop="itemType" label="类型" width="120" />
            <el-table-column label="产品" min-width="220">
              <template #default="{ row }">
                {{ packageProductLabel(row) }}
              </template>
            </el-table-column>
            <el-table-column label="序列号" min-width="220">
              <template #default="{ row }">
                <div class="package-device-value">
                  <span v-if="row.serialNumbers?.length">
                    {{ row.serialNumbers.join("、") }}
                  </span>
                  <span v-else class="package-device-empty">
                    {{ row.selectableDevice ? "未选择设备" : "-" }}
                  </span>
                  <el-link
                    v-if="row.selectableDevice"
                    type="primary"
                    :underline="false"
                    :disabled="activeOrder.isAllianceSourceLocked"
                    class="package-device-link"
                    @click="openPackageDeviceSelector(row)">
                    选择设备
                  </el-link>
                </div>
              </template>
            </el-table-column>
            <!-- <el-table-column
              prop="requiredText"
              label="选择要求"
              width="100"
              align="center" /> -->
            <el-table-column
              prop="quantity"
              label="数量"
              width="80"
              align="center" />
          </el-table>
          <el-empty v-else description="暂无产品明细" />
        </section>
        <section class="package-detail-section" v-loading="orderMediaLoading">
          <h4>履约凭证</h4>
          <el-table v-if="orderMedia.length" :data="orderMedia" border>
            <el-table-column prop="mediaTypeLabel" label="凭证类型" width="140" />
            <el-table-column label="文件" min-width="260">
              <template #default="{ row }">
                <el-link :href="row.fileUrl" target="_blank" type="primary">
                  {{ row.fileName || row.fileUrl || "查看文件" }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="登记时间" width="180" />
          </el-table>
          <el-empty v-else description="暂无履约凭证" :image-size="64" />
        </section>
      </template>
      <el-empty v-else description="暂无套餐详情" />
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <MerchantOrderDetailDialog
      v-model="orderDetailVisible"
      :order="orderDetail"
      :remote="useRemoteOrders"
      :express-company-options="expressCompanyOptions" />

    <el-dialog
      v-model="packageDeviceVisible"
      title="选择设备"
      width="820px"
      append-to-body
      destroy-on-close>
      <el-alert
        v-if="packageDeviceTarget"
        type="primary"
        :closable="false"
        show-icon
        class="package-device-tip"
        style="margin-bottom: 10px"
        :title="`请选择 ${packageDeviceTarget.quantity || 1} 台 ${packageDeviceTarget.name || '设备'}，设备来自订单发货仓库`" />
      <el-table
        ref="packageDeviceTableRef"
        v-loading="packageDeviceLoading"
        :data="packageDeviceOptions"
        row-key="unitId"
        border
        @selection-change="handlePackageDeviceSelection">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序列号" min-width="200">
          <template #default="{ row }">
            {{ row.serialNumber || "未录入序列号" }}
          </template>
        </el-table-column>
        <el-table-column label="库存编码" min-width="180">
          <template #default="{ row }">
            {{ row.unitCode || "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品" min-width="150" />
        <el-table-column prop="warehouseName" label="仓库" min-width="130" />
      </el-table>
      <el-empty
        v-if="!packageDeviceLoading && !packageDeviceOptions.length"
        description="当前发货仓库没有可选择的设备" />
      <template #footer>
        <el-button @click="packageDeviceVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="packageDeviceBinding"
          :disabled="
            packageDeviceSelectedUnitIds.length !==
            (packageDeviceTarget?.quantity || 1)
          "
          @click="submitPackageDeviceBinding">
          确定
        </el-button>
      </template>
    </el-dialog>

    <MerchantLogisticsDialog
      v-model="logisticsEditVisible"
      :order-no="logisticsEditOrder?.orderNo || ''"
      :direction="logisticsEditDirection"
      :carrier="logisticsEditCarrier"
      :tracking-no="logisticsEditTrackingNo"
      :express-companies="expressCompanyOptions"
      :loading="logisticsEditLoading"
      @submit="submitLogisticsEdit" />

    <MerchantLogisticsTrackingDialog
      v-model="logisticsTrackingVisible"
      :tracking="logisticsTracking"
      :carrier-name="logisticsTracking?.carrierName || ''"
      :loading="logisticsTrackingLoading"
      :error="logisticsTrackingError" />

    <el-dialog
      v-model="customerEditVisible"
      title="更改客户信息"
      width="680px"
      append-to-body
      destroy-on-close>
      <el-form
        ref="customerEditFormRef"
        :model="customerEditForm"
        :rules="customerEditRules"
        label-width="100px">
        <el-form-item label="客户姓名">
          <el-input
            v-model="customerEditForm.customerName"
            placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input
            v-model="customerEditForm.customerPhone"
            placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="买家昵称">
          <el-input
            v-model="customerEditForm.customerNickname"
            placeholder="请输入买家昵称" />
        </el-form-item>
        <el-form-item label="收件人姓名" prop="recipientName">
          <el-input
            v-model="customerEditForm.recipientName"
            placeholder="请输入收件人姓名" />
        </el-form-item>
        <el-form-item label="收件人电话" prop="recipientPhone">
          <el-input
            v-model="customerEditForm.recipientPhone"
            placeholder="请输入收件人电话" />
        </el-form-item>
        <el-form-item label="收货地址" prop="recipientAddress">
          <el-input
            v-model="customerEditForm.recipientAddress"
            placeholder="请输入收货地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="customerEditForm.remark"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="customerEditVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="customerEditLoading"
          @click="submitCustomerEdit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="annotationVisible"
      title="订单批注"
      width="min(92vw, 1120px)"
      class="annotation-dialog"
      append-to-body>
      <div v-loading="annotationLoading" class="annotation-history">
        <el-empty
          v-if="!annotationLoading && annotationHistory.length === 0"
          description="暂无批注记录"
          :image-size="88" />
        <div v-else class="annotation-history-list">
          <article
            v-for="note in annotationHistory"
            :key="note.noteId"
            class="annotation-record">
            <div class="annotation-record-type">{{ note.typeLabel }}</div>
            <div class="annotation-record-content">{{ note.content }}</div>
            <div class="annotation-record-meta">
              <span>{{ note.operatorName }}</span>
              <time>{{ note.createTime }}</time>
            </div>
          </article>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="annotationCreateVisible"
      title="添加批注"
      width="620px"
      append-to-body
      destroy-on-close>
      <el-form
        ref="annotationCreateFormRef"
        :model="annotationCreateForm"
        :rules="annotationCreateRules"
        label-width="90px">
        <el-form-item label="批注内容" prop="content">
          <el-input
            v-model="annotationCreateForm.content"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="请输入订单批注" />
        </el-form-item>
        <el-form-item label="批注类型" prop="noteType">
          <el-radio-group v-model="annotationCreateForm.noteType">
            <el-radio :value="1">发货</el-radio>
            <el-radio :value="2">收货</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="annotationCreateVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="annotationCreateLoading"
          @click="submitAnnotation">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="allianceTransferVisible"
      title="转单"
      width="520px"
      append-to-body
      destroy-on-close>
      <el-form
        ref="allianceTransferFormRef"
        :model="allianceTransferForm"
        :rules="allianceTransferRules"
        label-width="100px">
        <el-form-item label="转单对象" prop="target">
          <el-select
            v-model="allianceTransferForm.target"
            placeholder="请选择转单对象"
            style="width: 100%">
            <el-option label="租商联盟" value="alliance" />
            <el-option label="公众池" value="public-pool" />
            <el-option
              v-for="item in allianceOptions"
              :key="item.memberId"
              :label="item.merchantName"
              :value="`merchant:${item.memberId}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="结算价">
          <el-input-number
            v-model="allianceTransferForm.settlementPrice"
            :min="0"
            :precision="2"
            :step="10"
            controls-position="right"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="发货方式">
          <el-select
            v-model="allianceTransferForm.shippingMode"
            clearable
            placeholder="请选择发货方式"
            style="width: 100%">
            <el-option label="快递" :value="1" />
            <el-option label="自提" :value="2" />
            <el-option label="配送" :value="3" />
            <el-option label="线下" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用承担方">
          <el-select
            v-model="allianceTransferForm.shippingPayer"
            clearable
            placeholder="请选择费用承担方"
            style="width: 100%">
            <el-option label="发起方" :value="1" />
            <el-option label="接收方" :value="2" />
            <el-option label="双方协商" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="allianceTransferForm.remark"
            type="textarea"
            maxlength="500"
            show-word-limit
            :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="allianceTransferVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="allianceTransferLoading"
          @click="submitAllianceTransfer">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="editLogVisible"
      title="订单编辑记录"
      width="min(92vw, 1120px)"
      class="edit-log-dialog"
      append-to-body>
      <div v-loading="editLogLoading" class="edit-log-history">
        <el-empty
          v-if="!editLogLoading && editLogHistory.length === 0"
          description="暂无字段变更记录"
          :image-size="88" />
        <div v-else class="edit-log-list">
          <article
            v-for="log in editLogHistory"
            :key="log.logId"
            class="edit-log-record">
            <div class="edit-log-heading">
              <strong>{{ log.fieldName }}</strong>
              <time>{{ log.createTime }}</time>
            </div>
            <div class="edit-log-values">
              <span class="edit-log-before">
                {{ log.beforeDisplayValue || "空" }}
              </span>
              <span class="edit-log-arrow">→</span>
              <span class="edit-log-after">
                {{ log.afterDisplayValue || "空" }}
              </span>
            </div>
            <div class="edit-log-meta">
              <span>操作人：{{ log.operatorName }}</span>
              <span v-if="log.changeReason">原因：{{ log.changeReason }}</span>
            </div>
          </article>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="workflowVisible"
      :title="workflowTitle"
      width="560px"
      append-to-body
      destroy-on-close>
      <el-form
        ref="workflowFormRef"
        :model="workflowForm"
        :rules="workflowRules"
        label-width="100px">
        <el-form-item label="订单编号">
          <span>{{ activeOrder?.orderNo || "-" }}</span>
        </el-form-item>
        <el-form-item
          v-if="workflowAction === '上传视频'"
          label="视频地址"
          prop="videoUrl">
          <file-upload
            v-model="workflowForm.videoUrl"
            :limit="1"
            :file-size="200"
            :file-type="['mp4', 'avi', 'rmvb']"
            :is-show-tip="true"
            business-type="order" />
        </el-form-item>
        <el-form-item
          v-if="workflowAction === '发货'"
          label="快递公司"
          prop="carrier">
          <el-select
            v-model="workflowForm.carrier"
            filterable
            clearable
            placeholder="请选择快递公司"
            class="full-width"
            @change="validateTrackingNo">
            <el-option
              v-for="item in expressCompanyOptions"
              :key="item.code"
              :label="item.name"
              :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="workflowAction === '发货'"
          label="快递单号"
          prop="trackingNo">
          <el-input
            v-model="workflowForm.trackingNo"
            placeholder="请输入快递单号"
            @blur="validateTrackingNo" />
        </el-form-item>
        <el-form-item
          v-if="workflowAction === '转寄'"
          label="收件人姓名"
          prop="recipientName">
          <el-input
            v-model="workflowForm.recipientName"
            placeholder="请输入新的收件人姓名"
            clearable />
        </el-form-item>
        <el-form-item
          v-if="workflowAction === '转寄'"
          label="收件人电话"
          prop="recipientPhone">
          <el-input
            v-model="workflowForm.recipientPhone"
            placeholder="请输入新的收件人电话"
            clearable />
        </el-form-item>
        <el-form-item
          v-if="workflowAction === '转寄'"
          label="收货地址"
          prop="recipientAddress">
          <el-input
            v-model="workflowForm.recipientAddress"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="请输入新的收货地址" />
        </el-form-item>
        <el-form-item
          v-if="workflowAction === '跨仓归还'"
          label="归还仓库"
          prop="returnWarehouseId">
          <el-select
            v-model="workflowForm.returnWarehouseId"
            class="full-width"
            filterable
            placeholder="请选择归还仓库">
            <el-option
              v-for="item in warehouseOptions"
              :key="item.id"
              :label="`${item.name}`"
              :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="workflowAction === '续租'"
          label="续租日期"
          prop="leaseEndDate">
          <el-date-picker
            v-model="workflowForm.leaseEndDate"
            type="date"
            value-format="YYYY-MM-DD"
            class="full-width" />
        </el-form-item>
        <el-form-item
          v-if="workflowAction === '归还验机'"
          label="验机结果"
          prop="result">
          <el-radio-group v-model="workflowForm.result">
            <el-radio :value="RETURN_INSPECTION_RESULT.NORMAL">
              验机正常
            </el-radio>
            <el-radio :value="RETURN_INSPECTION_RESULT.AFTER_SALES">
              转售后
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="
            workflowAction === '售后' ||
            (workflowAction === '归还验机' &&
              workflowForm.result === RETURN_INSPECTION_RESULT.AFTER_SALES)
          "
          label="售后类型"
          prop="caseType">
          <el-select
            v-model="workflowForm.caseType"
            class="full-width"
            placeholder="请选择售后类型">
            <el-option
              v-for="item in afterSalesTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="
            workflowAction === '售后' ||
            (workflowAction === '归还验机' &&
              workflowForm.result === RETURN_INSPECTION_RESULT.AFTER_SALES)
          "
          label="问题描述"
          prop="problemDesc">
          <el-input
            v-model="workflowForm.problemDesc"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="请描述设备或配件问题" />
        </el-form-item>
        <el-form-item
          v-if="
            workflowAction === '售后' ||
            (workflowAction === '归还验机' &&
              workflowForm.result === RETURN_INSPECTION_RESULT.AFTER_SALES)
          "
          label="赔付金额">
          <el-input-number
            v-model="workflowForm.compensationAmount"
            :min="0"
            :precision="2"
            controls-position="right"
            class="full-width" />
        </el-form-item>
        <el-form-item
          v-if="
            workflowAction === '归还验机' ||
            workflowAction === '登记归还' ||
            workflowAction === '完成' ||
            workflowAction === '取消' ||
            workflowAction === '售后'
          "
          label="操作说明">
          <el-input
            v-model="workflowForm.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入操作说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="workflowVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="workflowLoading"
          @click="submitWorkflow">
          确认
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="batchMediaVisible"
      title="批量匹配发货视频"
      width="min(92vw, 1080px)"
      append-to-body
      destroy-on-close>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="视频上传后会优先按填写的 SN 或文件名中的 SN 匹配未完成订单；无法匹配的记录可在本窗口补录 SN。" />
      <div class="batch-media-upload">
        <file-upload
          v-model="batchMediaForm.fileUrls"
          :limit="50"
          :file-size="200"
          :file-type="['mp4', 'avi', 'rmvb']"
          :is-show-tip="true"
          business-type="order" />
      </div>
      <el-table
        v-if="batchMediaRows.length"
        :data="batchMediaRows"
        border
        class="batch-media-table">
        <el-table-column label="视频文件" min-width="260">
          <template #default="{ row }">
            {{ row.fileName || fileNameFromUrl(row.fileUrl) || "履约视频" }}
          </template>
        </el-table-column>
        <el-table-column label="识别 SN" min-width="220">
          <template #default="{ row }">
            <el-input
              v-model="row.detectedSn"
              clearable
              :disabled="Number(row.matchStatus) === 1"
              placeholder="可补录设备SN" />
          </template>
        </el-table-column>
        <el-table-column label="匹配状态" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="batchMediaStatusType(row.matchStatus)" effect="plain">
              {{ row.matchStatusLabel || "待上传" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单编号" width="170" />
        <el-table-column label="操作" width="110" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.mediaId && Number(row.matchStatus) !== 1"
              link
              type="primary"
              @click="recognizeBatchMediaRow(row)">
              补录匹配
            </el-button>
            <span v-else-if="Number(row.matchStatus) === 1" class="text-success">
              已绑定
            </span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty
        v-else
        description="暂无待处理视频，请先选择视频上传"
        :image-size="72" />
      <template #footer>
        <el-button @click="batchMediaVisible = false">关闭</el-button>
        <el-button
          type="primary"
          :loading="batchMediaLoading"
          :disabled="!batchMediaRows.some((row) => !row.mediaId)"
          @click="submitBatchMedia">
          上传并匹配
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="createVisible"
      :title="editingOrderId ? '编辑订单' : '新增订单'"
      width="min(94vw, 1480px)"
      class="create-order-dialog"
      destroy-on-close>
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="120px">
        <section v-if="!alliancePackageOnly" class="create-order-section">
          <h3>订单来源</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="订单渠道" prop="channelId">
                <el-select
                  v-model="createForm.channelId"
                  class="full-width"
                  filterable
                  placeholder="请选择订单渠道">
                  <el-option
                    v-for="item in orderFormChannelOptions"
                    :key="item.channelId"
                    :label="item.channelName"
                    :value="item.channelId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="来源店铺">
                <el-input
                  v-model="createForm.shopName"
                  placeholder="请输入来源店铺" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="外部单号">
                <el-input
                  v-model="createForm.externalOrderNo"
                  placeholder="请输入外部单号" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section v-if="!alliancePackageOnly" class="create-order-section">
          <h3>客户信息</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="客户姓名">
                <el-input
                  v-model="createForm.customerName"
                  placeholder="请输入客户姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="联系电话">
                <el-input
                  v-model="createForm.customerPhone"
                  placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="买家昵称">
                <el-input
                  v-model="createForm.customerNickname"
                  maxlength="128"
                  placeholder="请输入买家昵称" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="收件人姓名" prop="recipientName">
                <el-input
                  v-model="createForm.recipientName"
                  placeholder="请输入收件人姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="收件人电话" prop="recipientPhone">
                <el-input
                  v-model="createForm.recipientPhone"
                  placeholder="请输入收件人电话" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="收货地址" prop="recipientAddress">
                <el-input
                  v-model="createForm.recipientAddress"
                  placeholder="请输入完整收货地址" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="create-order-section">
          <h3>{{ alliancePackageOnly ? "设置套餐" : "商品与租期" }}</h3>
          <el-row v-if="!alliancePackageOnly" :gutter="20">
            <el-col :span="6">
              <el-form-item label="型号" prop="modelId">
                <el-select
                  v-model="createForm.modelId"
                  clearable
                  filterable
                  placeholder="请选择型号"
                  class="field-control"
                  @change="handleModelChange">
                  <el-option
                    v-for="item in filteredModels"
                    :key="item.modelId"
                    :label="modelOptionLabel(item)"
                    :value="item.modelId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="产品套餐" prop="packageId">
                <el-select
                  v-model="createForm.packageId"
                  class="full-width"
                  filterable
                  clearable
                  placeholder="请选择产品套餐"
                  @change="handlePackageChange">
                  <el-option
                    v-for="item in filteredPackageOptions"
                    :key="item.packageId"
                    :label="`${item.packageName}`"
                    :value="item.packageId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-button
                type="primary"
                icon="Plus"
                :disabled="!selectedPackageModelId"
                @click="addExtraAccessory">
                添加额外配件
              </el-button>
            </el-col>

            <el-col :span="18">
              <el-form-item label="套餐明细">
                <el-table
                  v-loading="packageDetailLoading"
                  :data="selectedPackageItems"
                  border
                  class="item-table">
                  <el-table-column prop="itemType" label="类型" width="120" />
                  <el-table-column
                    prop="itemName"
                    label="商品 / 配件"
                    min-width="260" />
                  <!-- <el-table-column prop="itemModelName" label="型号" min-width="180" /> -->
                  <el-table-column
                    prop="quantity"
                    label="数量"
                    width="100"
                    align="center" />
                  <!-- <el-table-column label="是否必选" width="110" align="center">
                    <template #default="{ row }">
                      {{ Number(row.requiredFlag) === 0 ? "可选" : "必选" }}
                    </template>
                  </el-table-column> -->
                </el-table>
                <el-empty
                  v-if="
                    createForm.packageId &&
                    !packageDetailLoading &&
                    !selectedPackageItems.length
                  "
                  description="当前套餐暂无明细" />
              </el-form-item>
            </el-col>
            <el-col v-if="extraAccessoryItems.length" :span="18">
              <el-form-item label="额外配件">
                <el-table :data="extraAccessoryItems" border class="item-table">
                  <el-table-column label="配件" min-width="360">
                    <template #default="{ row }">
                      <el-select
                        v-model="row.accessoryOptionKey"
                        filterable
                        class="full-width"
                        placeholder="请选择额外配件">
                        <el-option
                          v-for="item in compatibleAccessoryProducts"
                          :key="item.optionKey"
                          :label="`${item.productName}`"
                          :value="item.optionKey" />
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column label="数量" width="180">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.quantity"
                        :min="1"
                        :max="100"
                        controls-position="right" />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="80">
                    <template #default="{ $index }">
                      <el-button
                        link
                        type="danger"
                        @click="extraAccessoryItems.splice($index, 1)">
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="发货仓库" prop="warehouseId">
                <el-select
                  v-model="createForm.warehouseId"
                  class="full-width"
                  filterable
                  placeholder="请选择发货仓库">
                  <el-option
                    v-for="item in warehouseOptions"
                    :key="item.id"
                    :label="`${item.name}`"
                    :value="item.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="归还仓库" prop="returnWarehouseId">
                <el-select
                  v-model="createForm.returnWarehouseId"
                  class="full-width"
                  filterable
                  placeholder="请选择归还仓库">
                  <el-option
                    v-for="item in warehouseOptions"
                    :key="item.id"
                    :label="`${item.name}`"
                    :value="item.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="租期" prop="leasePeriod">
                <el-input
                  v-model="createForm.leasePeriod"
                  maxlength="64"
                  placeholder="格式：2025-12-09~2025-12-15"
                  @input="handleLeasePeriodInput" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="租期开始日期" prop="leaseStartDate">
                <el-date-picker
                  v-model="createForm.leaseStartDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  class="full-width"
                  @change="syncLeasePeriodFromDates" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="租期截止日期" prop="leaseEndDate">
                <el-date-picker
                  v-model="createForm.leaseEndDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  class="full-width"
                  @change="syncLeasePeriodFromDates" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="租用天数">
                <el-input :model-value="createForm.leaseDays || 0" disabled />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="发货日期">
                <el-date-picker
                  v-model="createForm.shipDueTime"
                  type="date"
                  value-format="YYYY-MM-DD"
                  class="full-width" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="发货时段">
                <el-select
                  v-model="createForm.shipTimeSlot"
                  class="full-width"
                  filterable
                  placeholder="请选择发货时段">
                  <el-option
                    v-for="item in shipTimeSlotOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="配送方式">
                <el-select
                  v-model="createForm.deliveryMethod"
                  class="full-width">
                  <el-option
                    v-for="item in deliveryMethodOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="寄出快递单号" prop="expressTrackingNo">
                <el-input
                  v-model="createForm.expressTrackingNo"
                  clearable
                  placeholder="请输入寄出快递单号"
                  @blur="validateCreateShippingTracking" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="应该归还日期">
                <el-date-picker
                  v-model="createForm.returnDueTime"
                  type="date"
                  value-format="YYYY-MM-DD"
                  class="full-width" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="预计到库日期">
                <el-input
                  :model-value="createForm.arrivalDate || '保存后自动计算'"
                  disabled />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="寄回配送方式">
                <el-select
                  v-model="createForm.returnDeliveryMethod"
                  class="full-width">
                  <el-option
                    v-for="item in deliveryMethodOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="寄回快递单号" prop="returnExpressTrackingNo">
                <el-input
                  v-model="createForm.returnExpressTrackingNo"
                  clearable
                  placeholder="请输入寄回快递单号"
                  @blur="validateCreateReturnTracking" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section
          v-if="!alliancePackageOnly"
          class="create-order-section create-order-section-last">
          <h3>费用与备注</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="租金">
                <el-input-number
                  v-model="createForm.rentAmount"
                  :min="0"
                  :precision="2"
                  class="full-width" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="押金">
                <el-input-number
                  v-model="createForm.depositAmount"
                  :min="0"
                  :precision="2"
                  class="full-width" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="配送费用">
                <el-input-number
                  v-model="createForm.deliveryAmount"
                  :min="0"
                  :precision="2"
                  class="full-width" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="保险费用">
                <el-input-number
                  v-model="createForm.insuranceAmount"
                  :min="0"
                  :precision="2"
                  class="full-width" />
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="订单备注">
                <el-input
                  v-model="createForm.remark"
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入订单备注" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="createLoading"
          @click="submitCreateOrder">
          保存订单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MerchantOrder">
import {
  Bell,
  Calendar,
  Camera,
  Check,
  ChatDotRound,
  CircleClose,
  CircleCheck,
  CopyDocument,
  Edit,
  Finished,
  InfoFilled,
  Location,
  Plus,
  Search,
  Refresh,
  ShoppingCart,
  Switch,
  User,
  VideoCamera,
  Van,
  Box,
} from "@element-plus/icons-vue";
import {
  addMerchantOrder,
  addMerchantOrderNote,
  bindMerchantOrderPackageDevices,
  cancelMerchantOrder,
  completeMerchantOrder,
  createMerchantOrderAfterSales,
  forwardMerchantOrder,
  getMerchantOrder,
  getMerchantOrderEditLogs,
  getMerchantOrderNotes,
  getMerchantOrderSummary,
  getMerchantOrderLogisticsTrack,
  addMerchantOrderMedia,
  addMerchantOrderMediaBatch,
  listMerchantAllianceOptions,
  listMerchantOrderPackageDevices,
  listMerchantOrderMedia,
  listUnmatchedMerchantOrderMedia,
  listMerchantOrders,
  markMerchantOrderReturning,
  prepareMerchantOrder,
  inspectMerchantOrderReturn,
  shipMerchantOrder,
  transferMerchantOrder,
  updateMerchantOrderLogistics,
  updateMerchantOrder,
  updateMerchantOrderRecipient,
  recognizeMerchantOrderMedia,
} from "@/api/merchant/order";
import { listMerchantOrderChannels } from "@/api/merchant/orderChannel";
import { listBrandModelOptions } from "@/api/merchant/brandModel";
import { listMerchantExpressCompanies } from "@/api/merchant/expressCompany";
import { getMerchantStoreSettings } from "@/api/merchant/storeSettings";
import {
  getMerchantPackage,
  listMerchantPackageAccessoryOptions,
  listMerchantPackages,
  listMerchantWarehouses,
} from "@/api/merchant/rentalBusiness";
import { RENTAL_CODE } from "@/utils/rental/options";
import { getExpressCompanyName as formatExpressCompanyName } from "@/utils/rental/expressCompany";
import { createIdempotencyKey } from "@/utils/idempotency";
import { useRoute } from "vue-router";
import { nextTick } from "vue";
import MerchantLogisticsDialog from "@/components/MerchantLogisticsDialog.vue";
import MerchantLogisticsTrackingDialog from "@/components/MerchantLogisticsTrackingDialog.vue";
import MerchantOrderDetailDialog from "@/components/MerchantOrderDetailDialog.vue";
import FileUpload from "@/components/FileUpload/index.vue";

function weatherIconSymbol(icon) {
  switch (icon) {
    case "sunny":
      return "☀";
    case "rain":
      return "☔";
    case "snow":
      return "❄";
    case "storm":
      return "⛈";
    default:
      return "☁";
  }
}

function weatherDetailText(value, label) {
  const text = String(value || "待查询").trim();
  return `${label}天气：${text.startsWith(label) ? text.slice(label.length) : text}`;
}

const { proxy } = getCurrentInstance();
const route = useRoute();
const loading = ref(false);
const lastUpdated = ref("刚刚");
const showSearch = ref(true);
const viewMode = ref("card");
const detailVisible = ref(false);
const orderDetailVisible = ref(false);
const orderDetail = ref(null);
const orderMedia = ref([]);
const orderMediaLoading = ref(false);
const batchMediaVisible = ref(false);
const batchMediaLoading = ref(false);
const batchMediaForm = reactive({ fileUrls: "" });
const batchMediaRows = ref([]);
const packageDeviceVisible = ref(false);
const packageDeviceLoading = ref(false);
const packageDeviceBinding = ref(false);
const packageDeviceTableRef = ref(null);
const packageDeviceTarget = ref(null);
const packageDeviceOptions = ref([]);
const packageDeviceSelectedUnitIds = ref([]);
const customerEditVisible = ref(false);
const customerEditLoading = ref(false);
const customerEditFormRef = ref(null);
const customerEditForm = reactive({
  customerName: "",
  customerPhone: "",
  customerNickname: "",
  recipientName: "",
  recipientPhone: "",
  recipientAddress: "",
  remark: "",
});
const customerEditRules = {
  customerName: [
    { required: true, message: "请输入客户姓名", trigger: "blur" },
  ],
  customerPhone: [
    { required: true, message: "请输入联系电话", trigger: "blur" },
  ],
  recipientName: [
    { required: true, message: "请输入收件人姓名", trigger: "blur" },
  ],
  recipientPhone: [
    { required: true, message: "请输入收件人电话", trigger: "blur" },
  ],
  recipientAddress: [
    { required: true, message: "请输入收货地址", trigger: "blur" },
  ],
};
const annotationVisible = ref(false);
const annotationLoading = ref(false);
const annotationHistory = ref([]);
let annotationRequestToken = 0;
const annotationCreateVisible = ref(false);
const annotationCreateLoading = ref(false);
const annotationCreateFormRef = ref(null);
const annotationCreateForm = reactive({ content: "", noteType: 1 });
const annotationCreateRules = {
  content: [{ required: true, message: "请输入批注内容", trigger: "blur" }],
};
const allianceTransferVisible = ref(false);
const allianceTransferLoading = ref(false);
const allianceTransferFormRef = ref(null);
const allianceOptions = ref([]);
const allianceTransferOrder = ref(null);
const allianceTransferForm = reactive({
  target: "",
  settlementPrice: null,
  shippingMode: null,
  shippingPayer: null,
  remark: "",
});
const allianceTransferRules = {
  target: [{ required: true, message: "请选择转单对象", trigger: "change" }],
};
const editLogVisible = ref(false);
const editLogLoading = ref(false);
const editLogHistory = ref([]);
let editLogRequestToken = 0;
const workflowVisible = ref(false);
const workflowLoading = ref(false);
const workflowAction = ref("");
const workflowFormRef = ref(null);
const RETURN_INSPECTION_RESULT = Object.freeze({
  NORMAL: 1,
  AFTER_SALES: 2,
});
const AFTER_SALES_CASE_TYPE = Object.freeze({
  DAMAGE: 1,
  LOST: 2,
  COMPENSATION: 3,
  OTHER: 9,
});
const workflowForm = reactive({
  videoUrl: "",
  carrier: "",
  trackingNo: "",
  recipientName: "",
  recipientPhone: "",
  recipientAddress: "",
  result: RETURN_INSPECTION_RESULT.NORMAL,
  caseType: AFTER_SALES_CASE_TYPE.DAMAGE,
  problemDesc: "",
  compensationAmount: 0,
  warehouseId: null,
  returnWarehouseId: null,
  leaseEndDate: "",
  remark: "",
});
const expressCompanyOptions = ref([]);
const logisticsEditVisible = ref(false);
const logisticsEditLoading = ref(false);
const logisticsEditOrder = ref(null);
const logisticsEditDirection = ref("ship");
const logisticsEditCarrier = computed(() => {
  const order = logisticsEditOrder.value || {};
  return logisticsEditDirection.value === "return"
    ? order.returnExpressCompany || order.returnCarrier || ""
    : order.expressCompany || order.shippingCarrier || "";
});
const logisticsEditTrackingNo = computed(() => {
  const order = logisticsEditOrder.value || {};
  const trackingNo =
    logisticsEditDirection.value === "return"
      ? order.returnNo
      : order.trackingNo;
  return trackingNo === "待安排" ? "" : trackingNo || "";
});
const logisticsTrackingVisible = ref(false);
const logisticsTrackingLoading = ref(false);
const logisticsTrackingError = ref("");
const logisticsTracking = ref(null);
let logisticsTrackingRequestToken = 0;
const createVisible = ref(false);
const createLoading = ref(false);
const createFormRef = ref(null);
const activeOrder = ref(null);
const activePackageExtraAccessories = computed(() => {
  const detailItems = activeOrder.value?.packageDetail?.extraAccessories;
  const items = Array.isArray(detailItems)
    ? detailItems
    : activeOrder.value?.addOns || [];
  return items.map(normalizeExtraAccessoryLabel).filter(Boolean);
});
const activePackageDetailItems = computed(() => {
  const packageItems = Array.isArray(activeOrder.value?.packageDetail?.items)
    ? activeOrder.value.packageDetail.items
    : [];
  const extraItems = activePackageExtraAccessories.value.map((label) => {
    const detail = parseExtraAccessoryLabel(label);
    return {
      itemIndex: null,
      itemType: "额外配件",
      name: detail.name,
      code: "-",
      model: "",
      quantity: detail.quantity,
      requiredText: "额外添加",
      selectableDevice: false,
      serialNumbers: [],
      unitIds: [],
    };
  });
  return [...packageItems, ...extraItems];
});
const warehouseOptions = ref([]);
const packageOptions = ref([]);
const modelOptions = ref([]);
const accessoryOptions = ref([]);
const selectedPackageDetail = ref(null);
const packageDetailLoading = ref(false);
const extraAccessoryItems = ref([]);
let accessoryOptionsRequestToken = 0;
const orders = ref([]);
const total = ref(0);
const summary = reactive({
  total: 0,
  pendingShip: 0,
  shipping: 0,
  attention: 0,
});
// 订单列表默认读取业务服务数据库，只有明确配置为 false 时才使用演示数据。
const useRemoteOrders = import.meta.env.VITE_USE_ORDER_API !== "false";
const DEFAULT_ORDER_COPY_FIELDS = Object.freeze([
  "customer",
  "recipient",
  "package",
  "lease",
  "warehouse",
  "amount",
  "remark",
]);
const ORDER_COPY_FIELD_LABELS = Object.freeze({
  customer: "客户信息",
  recipient: "收件信息",
  package: "产品套餐",
  lease: "租期信息",
  warehouse: "仓库信息",
  amount: "费用信息",
  remark: "订单备注",
});
const orderCopyFields = ref([...DEFAULT_ORDER_COPY_FIELDS]);
const createForm = reactive({
  customerName: "",
  customerNickname: "",
  customerPhone: "",
  recipientName: "",
  recipientPhone: "",
  channelId: null,
  shopName: "",
  externalOrderNo: "",
  recipientAddress: "",
  productId: null,
  modelId: null,
  packageId: null,
  accessoryItemIds: null,
  warehouseId: null,
  returnWarehouseId: null,
  leasePeriod: "",
  leaseStartDate: "",
  leaseEndDate: "",
  shipDueTime: "",
  shipTimeSlot: "morning",
  returnDueTime: "",
  deliveryMethod: "express",
  expressCompany: "",
  expressTrackingNo: "",
  returnDeliveryMethod: "express",
  returnExpressCompany: "",
  returnExpressTrackingNo: "",
  deliveryAmount: 0,
  insuranceAmount: 0,
  leaseDays: 0,
  arrivalDate: "",
  rentAmount: 0,
  depositAmount: 0,
  remark: "",
});
const deliveryMethodOptions = [
  { label: "快递", value: "express" },
  { label: "闪送", value: "flash" },
  { label: "上门", value: "door_to_door" },
];

/* 发货时段选项 */
const shipTimeSlotOptions = [
  {
    value: "morning",
    label: "上午",
  },
  {
    value: "afternoon",
    label: "下午",
  },
];
const editingOrderId = ref(null);
const alliancePackageOnly = ref(false);
const createRules = {
  modelId: [{ required: true, message: "请选择型号", trigger: "change" }],
  packageId: [{ required: true, message: "请选择产品套餐", trigger: "change" }],
  leasePeriod: [
    { required: true, message: "请输入租期", trigger: ["blur", "change"] },
    { validator: validateLeasePeriodRule, trigger: ["blur", "change"] },
  ],
  recipientName: [
    { required: true, message: "请输入收件人姓名", trigger: "blur" },
  ],
  recipientPhone: [
    { required: true, message: "请输入收件人电话", trigger: "blur" },
  ],
  channelId: [{ required: true, message: "请选择订单渠道", trigger: "change" }],
  warehouseId: [
    { required: true, message: "请选择发货仓库", trigger: "change" },
  ],
  returnWarehouseId: [
    { required: true, message: "请选择归还仓库", trigger: "change" },
  ],
  leaseStartDate: [
    { required: true, message: "请选择租赁开始日期", trigger: "change" },
  ],
  leaseEndDate: [
    { required: true, message: "请选择租赁结束日期", trigger: "change" },
  ],
  expressCompany: [
    {
      validator: validateCreateShippingTrackingRule,
      trigger: ["blur", "change"],
    },
  ],
  expressTrackingNo: [
    {
      validator: validateCreateShippingTrackingRule,
      trigger: ["blur", "change"],
    },
  ],
  returnExpressCompany: [
    {
      validator: validateCreateReturnTrackingRule,
      trigger: ["blur", "change"],
    },
  ],
  returnExpressTrackingNo: [
    {
      validator: validateCreateReturnTrackingRule,
      trigger: ["blur", "change"],
    },
  ],
};
const workflowRules = {
  videoUrl: [{ required: true, message: "请上传履约视频", trigger: "change" }],
  result: [
    {
      required: true,
      message: "请选择验机结果",
      trigger: "change",
    },
  ],
  caseType: [
    {
      validator: (_rule, value, callback) => {
        const required =
          workflowAction.value === "售后" ||
          (workflowAction.value === "归还验机" &&
            workflowForm.result === RETURN_INSPECTION_RESULT.AFTER_SALES);
        if (required && !value) return callback(new Error("请选择售后类型"));
        callback();
      },
      trigger: "change",
    },
  ],
  problemDesc: [
    {
      validator: (_rule, value, callback) => {
        const required =
          workflowAction.value === "售后" ||
          (workflowAction.value === "归还验机" &&
            workflowForm.result === RETURN_INSPECTION_RESULT.AFTER_SALES);
        if (required && !String(value || "").trim()) {
          return callback(new Error("请填写问题描述"));
        }
        callback();
      },
      trigger: "blur",
    },
  ],
  carrier: [{ required: true, message: "请选择快递公司", trigger: "change" }],
  trackingNo: [
    { validator: validateTrackingNoRule, trigger: ["blur", "change"] },
  ],
  recipientName: [
    {
      validator: (_rule, value, callback) => {
        if (
          workflowAction.value === "转寄" &&
          !String(value || "").trim()
        ) {
          return callback(new Error("请输入新的收件人姓名"));
        }
        callback();
      },
      trigger: "blur",
    },
  ],
  recipientPhone: [
    {
      validator: (_rule, value, callback) => {
        if (
          workflowAction.value === "转寄" &&
          !String(value || "").trim()
        ) {
          return callback(new Error("请输入新的收件人电话"));
        }
        callback();
      },
      trigger: "blur",
    },
  ],
  recipientAddress: [
    {
      validator: (_rule, value, callback) => {
        if (
          workflowAction.value === "转寄" &&
          !String(value || "").trim()
        ) {
          return callback(new Error("请输入新的收货地址"));
        }
        callback();
      },
      trigger: "blur",
    },
  ],
  returnWarehouseId: [
    { required: true, message: "请选择归还仓库", trigger: "change" },
  ],
  leaseEndDate: [
    { required: true, message: "请选择新的归还日期", trigger: "change" },
  ],
};

const workflowTitle = computed(() => workflowAction.value);

const statusOptions = [
  { label: "待发货", value: RENTAL_CODE.ORDER_STATUS.PENDING_SHIP },
  { label: "已备货", value: RENTAL_CODE.ORDER_STATUS.PREPARED },
  { label: "运输中", value: RENTAL_CODE.ORDER_STATUS.SHIPPING },
  { label: "已发货", value: RENTAL_CODE.ORDER_STATUS.SHIPPED },
  { label: "租用中", value: RENTAL_CODE.ORDER_STATUS.RENTING },
  { label: "归还中", value: RENTAL_CODE.ORDER_STATUS.RETURNING },
  { label: "归还逾期", value: RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN },
  { label: "售后中", value: RENTAL_CODE.ORDER_STATUS.AFTER_SALES },
  { label: "已逾期", value: RENTAL_CODE.ORDER_STATUS.OVERDUE },
  { label: "已完成", value: RENTAL_CODE.ORDER_STATUS.COMPLETED },
  { label: "已取消", value: RENTAL_CODE.ORDER_STATUS.CANCELLED },
];

const afterSalesTypeOptions = [
  { label: "损坏", value: AFTER_SALES_CASE_TYPE.DAMAGE },
  { label: "丢失", value: AFTER_SALES_CASE_TYPE.LOST },
  { label: "赔付", value: AFTER_SALES_CASE_TYPE.COMPENSATION },
  { label: "其他", value: AFTER_SALES_CASE_TYPE.OTHER },
];

const orderDateTypeOptions = [
  { label: "发货日期", value: "ship" },
  { label: "归还日期", value: "return" },
  { label: "到库日期", value: "arrival" },
  { label: "起租日期", value: "lease_start" },
  { label: "下单日期", value: "order" },
  { label: "创建日期", value: "create" },
];

const orderSortTypeOptions = [
  { label: "订单状态", value: "status" },
  { label: "发货日期", value: "ship" },
  { label: "归还日期", value: "return" },
  { label: "到库日期", value: "arrival" },
  { label: "起租日期", value: "lease_start" },
  { label: "下单日期", value: "order" },
  { label: "创建日期", value: "create" },
];

const orderSortDirectionOptions = [
  { label: "正序", value: "asc" },
  { label: "倒序", value: "desc" },
];

const channelOptions = ref([]);
const orderFormChannelOptions = computed(() =>
  channelOptions.value.filter(
    (item) =>
      Number(item.channelType) !== RENTAL_CODE.ORDER_CHANNEL_TYPE.ALLIANCE,
  ),
);

const ORDER_SCOPE = Object.freeze({
  TODAY_SHIPPING: "today-shipping",
  TODAY_RETURN: "today-return",
  TODAY_ARRIVAL: "today-arrival",
  TOMORROW_ARRIVAL: "tomorrow-arrival",
  TODAY_CREATED: "today-created",
  TODAY_COMPLETED: "today-completed",
});

const ORDER_SCOPE_LABELS = Object.freeze({
  [ORDER_SCOPE.TODAY_SHIPPING]: "今日发货",
  [ORDER_SCOPE.TODAY_RETURN]: "今日归还",
  [ORDER_SCOPE.TODAY_ARRIVAL]: "今日到货",
  [ORDER_SCOPE.TOMORROW_ARRIVAL]: "明日到货",
  [ORDER_SCOPE.TODAY_CREATED]: "今日新增",
  [ORDER_SCOPE.TODAY_COMPLETED]: "今日完结",
});

const ORDER_SCOPE_VALUES = new Set(Object.keys(ORDER_SCOPE_LABELS));

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  status: undefined,
  channelId: null,
  warehouseId: null,
  attentionOnly: false,
  shippingOnly: false,
  dateType: "ship",
  dateRange: [],
  sortType: "create",
  sortDirection: "desc",
  orderScope: "",
});

const currentOrderScopeTitle = computed(
  () => ORDER_SCOPE_LABELS[queryParams.orderScope] || "",
);
const filteredModels = computed(() => {
  const packageModelIds = new Set(
    packageOptions.value
      .map((item) => Number(item.modelId))
      .filter((modelId) => Number.isFinite(modelId) && modelId > 0),
  );
  return modelOptions.value.filter(
    (item) =>
      packageModelIds.size === 0 || packageModelIds.has(Number(item.modelId)),
  );
});

/** 型号用于筛选可履约套餐，套餐保存时仍以其唯一编号作为订单关联。 */
const filteredPackageOptions = computed(() => {
  if (!createForm.modelId) return packageOptions.value;
  return packageOptions.value.filter(
    (item) => Number(item.modelId) === Number(createForm.modelId),
  );
});
const selectedPackageModelId = computed(() => {
  const detailModelId = Number(selectedPackageDetail.value?.modelId);
  if (Number.isFinite(detailModelId) && detailModelId > 0) return detailModelId;
  const selectedPackage = packageOptions.value.find(
    (item) => Number(item.packageId) === Number(createForm.packageId),
  );
  return Number(selectedPackage?.modelId || createForm.modelId) || null;
});
const selectedPackageItems = computed(() =>
  (selectedPackageDetail.value?.items || []).map((item) => ({
    ...item,
    itemType:
      Number(item.itemRole) === 1
        ? "主商品"
        : Number(item.itemRole) === 2
          ? "官方配件"
          : "自定义配件",
    itemName: packageItemName(item),
  })),
);
const compatibleAccessoryProducts = computed(() =>
  accessoryOptions.value.filter((item) => {
    const modelId = selectedPackageModelId.value;
    if (!modelId) return false;
    const compatibleModelIds = Array.isArray(item.compatibleModelIds)
      ? item.compatibleModelIds
      : String(item.compatibleModelIds || "")
          .split(",")
          .filter(Boolean)
          .map(Number);
    return compatibleModelIds.includes(Number(modelId));
  }),
);

/** 仅接受完整的日期范围，兼容波浪号、中文“至”和空格三种录入分隔符。 */
const LEASE_PERIOD_PATTERN =
  /^(\d{4}-\d{2}-\d{2})(?:\s*~\s*|\s*至\s*|\s+)(\d{4}-\d{2}-\d{2})$/;

function normalizeDateOnly(value) {
  if (!value) return "";
  return String(value).slice(0, 10);
}

function dateOnlyTimestamp(value) {
  const normalized = normalizeDateOnly(value);
  const match = normalized.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (!match) return null;
  const [, year, month, day] = match.map(Number);
  const timestamp = Date.UTC(year, month - 1, day);
  const date = new Date(timestamp);
  if (
    date.getUTCFullYear() !== year ||
    date.getUTCMonth() !== month - 1 ||
    date.getUTCDate() !== day
  ) {
    return null;
  }
  return timestamp;
}

function calculateLeaseDays(startDate, endDate) {
  const startTimestamp = dateOnlyTimestamp(startDate);
  const endTimestamp = dateOnlyTimestamp(endDate);
  if (startTimestamp === null || endTimestamp === null) return null;
  const difference = endTimestamp - startTimestamp;
  if (difference < 0 || difference % 86400000 !== 0) return null;
  return difference / 86400000 + 1;
}

function parseLeasePeriod(value) {
  const match = String(value || "")
    .trim()
    .match(LEASE_PERIOD_PATTERN);
  if (!match) return null;
  const leaseDays = calculateLeaseDays(match[1], match[2]);
  if (!leaseDays) return null;
  return {
    leaseStartDate: match[1],
    leaseEndDate: match[2],
    leaseDays,
  };
}

/** 校验订单表单中的租期文本，并确保其能解析出有效的日期范围。 */
function validateLeasePeriodRule(_rule, value, callback) {
  if (!String(value || "").trim()) {
    callback(new Error("请输入租期"));
    return;
  }
  if (!parseLeasePeriod(value)) {
    callback(new Error("租期格式不正确，例如 2025-12-09~2025-12-15"));
    return;
  }
  callback();
}

function formatLeasePeriod(startDate, endDate) {
  const start = normalizeDateOnly(startDate);
  const end = normalizeDateOnly(endDate);
  return calculateLeaseDays(start, end) ? `${start}~${end}` : "";
}

/** 输入完整租期后同步三个派生字段，后端仍会按起止日期再次计算租用天数。 */
function handleLeasePeriodInput(value) {
  const text = String(value || "").trim();
  if (!text) {
    createForm.leaseStartDate = "";
    createForm.leaseEndDate = "";
    createForm.leaseDays = 0;
    return;
  }
  const parsed = parseLeasePeriod(text);
  if (!parsed) {
    createForm.leaseStartDate = "";
    createForm.leaseEndDate = "";
    createForm.leaseDays = 0;
    return;
  }
  Object.assign(createForm, parsed);
}

/** 手动修改日期时保持租期文本和租用天数与日期选择器一致。 */
function syncLeasePeriodFromDates() {
  const leaseDays = calculateLeaseDays(
    createForm.leaseStartDate,
    createForm.leaseEndDate,
  );
  createForm.leaseDays = leaseDays || 0;
  createForm.leasePeriod = formatLeasePeriod(
    createForm.leaseStartDate,
    createForm.leaseEndDate,
  );
}

/** 新增订单默认使用排序后的第一个仓库，编辑订单不覆盖已有仓库。 */
function setDefaultCreateWarehouses() {
  if (editingOrderId.value !== null || !warehouseOptions.value.length) return;
  const firstWarehouseId = warehouseOptions.value[0].id;
  if (createForm.warehouseId === null || createForm.warehouseId === undefined) {
    createForm.warehouseId = firstWarehouseId;
  }
  if (
    createForm.returnWarehouseId === null ||
    createForm.returnWarehouseId === undefined
  ) {
    createForm.returnWarehouseId = createForm.warehouseId;
  }
}

const summaryItems = computed(() => [
  {
    key: "all",
    label: "全部订单",
    value: undefined,
    attentionOnly: false,
    shippingOnly: false,
    count: summary.total,
    trend: "总量",
    trendType: "neutral",
  },
  {
    key: "pendingShip",
    label: "待发货",
    value: RENTAL_CODE.ORDER_STATUS.PENDING_SHIP,
    attentionOnly: false,
    shippingOnly: false,
    count: summary.pendingShip,
    trend: "优先处理",
    trendType: "warning",
  },
  {
    key: "shipping",
    label: "运输中",
    value: undefined,
    attentionOnly: false,
    shippingOnly: true,
    count: summary.shipping,
    trend: "配送中",
    trendType: "primary",
  },
  {
    key: "attention",
    label: "异常提醒",
    value: undefined,
    attentionOnly: true,
    shippingOnly: false,
    count: summary.attention,
    trend: "需关注",
    trendType: "danger",
  },
]);

const filteredOrders = computed(() => orders.value);

/** 加载订单列表，真实订单服务接入后只替换 API 返回数据，不改变页面展示结构。 */
function loadOrders() {
  if (!useRemoteOrders) {
    orders.value = createDemoOrders().map(normalizeOrder);
    total.value = orders.value.length;
    lastUpdated.value = "演示数据";
    mergeRouteOrderSnapshot();
    return;
  }
  loading.value = true;
  const query = {
    ...queryParams,
    dateStart: queryParams.dateRange?.[0] || undefined,
    dateEnd: queryParams.dateRange?.[1] || undefined,
  };
  delete query.dateRange;
  listMerchantOrders(query)
    .then((response) => {
      // request 拦截器已返回响应体，分页字段通常位于顶层；兼容仍被 AjaxResult 包装的响应。
      const rows = response.rows || response.data?.rows || [];
      const pageTotal = response.total ?? response.data?.total ?? 0;
      orders.value = rows.map(normalizeOrder);
      total.value = Number(pageTotal);
      lastUpdated.value = "刚刚";
      mergeRouteOrderSnapshot();
    })
    .catch(() => {
      orders.value = [];
      total.value = 0;
      lastUpdated.value = "加载失败";
    })
    .finally(() => {
      loading.value = false;
    });
}
/** 选择型号后仅保留该型号下可履约的套餐，避免提交型号与套餐不一致的订单。 */
function handleModelChange(modelId) {
  if (!modelId) {
    createForm.packageId = null;
    selectedPackageDetail.value = null;
    extraAccessoryItems.value = [];
    return loadOrderAccessoryOptions(null);
  }
  const selectedPackage = packageOptions.value.find(
    (item) => Number(item.packageId) === Number(createForm.packageId),
  );
  if (selectedPackage && Number(selectedPackage.modelId) !== Number(modelId)) {
    createForm.packageId = null;
    selectedPackageDetail.value = null;
    extraAccessoryItems.value = [];
  }
  return loadOrderAccessoryOptions(modelId);
}

/** 选择套餐时反向回填所属型号，保证两个选择控件始终表示同一业务关系。 */
async function handlePackageChange(packageId) {
  selectedPackageDetail.value = null;
  extraAccessoryItems.value = [];
  await loadOrderAccessoryOptions(null);
  if (!packageId) return;
  const selectedPackage = packageOptions.value.find(
    (item) => Number(item.packageId) === Number(packageId),
  );
  if (selectedPackage?.modelId) {
    createForm.modelId = Number(selectedPackage.modelId);
  }
  packageDetailLoading.value = true;
  try {
    const response = await getMerchantPackage(packageId);
    selectedPackageDetail.value = response?.data ?? response ?? null;
    const modelId = Number(
      selectedPackageDetail.value?.modelId || createForm.modelId,
    );
    await loadOrderAccessoryOptions(modelId);
  } catch (_error) {
    selectedPackageDetail.value = null;
  } finally {
    packageDetailLoading.value = false;
  }
}

/** 额外配件只允许从套餐主型号兼容的官方或自定义配件中选择。 */
function addExtraAccessory() {
  if (!selectedPackageModelId.value) {
    proxy.$modal.msgWarning("请先选择产品套餐");
    return;
  }
  if (!compatibleAccessoryProducts.value.length) {
    proxy.$modal.msgWarning("当前套餐主型号暂无可添加的兼容配件");
    return;
  }
  extraAccessoryItems.value.push({ accessoryOptionKey: "", quantity: 1 });
}

function packageItemName(item) {
  const option = accessoryOptions.value.find(
    (candidate) =>
      (Number(item.itemRole) === 2 &&
        Number(candidate.accessoryId) === Number(item.accessoryId)) ||
      (Number(item.itemRole) === 3 &&
        Number(candidate.merchantAccessoryId) ===
          Number(item.merchantAccessoryId)),
  );
  return option?.productName || item.itemName || "-";
}

function serializeExtraAccessories() {
  const selected = extraAccessoryItems.value
    .map((item) => {
      const option = accessoryOptions.value.find(
        (candidate) => candidate.optionKey === item.accessoryOptionKey,
      );
      return option
        ? {
            sourceType: option.sourceType,
            accessoryId:
              option.sourceType === "official" ? option.accessoryId : undefined,
            merchantAccessoryId:
              option.sourceType === "custom"
                ? option.merchantAccessoryId
                : undefined,
            quantity: Number(item.quantity) || 1,
          }
        : null;
    })
    .filter(Boolean);
  if (selected.length !== extraAccessoryItems.value.length) {
    throw new Error("请完整选择额外配件");
  }
  return selected.length ? JSON.stringify(selected) : null;
}

function restoreExtraAccessories(value) {
  extraAccessoryItems.value = [];
  if (!value) return;
  try {
    const items = JSON.parse(value);
    if (!Array.isArray(items)) return;
    extraAccessoryItems.value = items
      .map((item) => {
        const option = accessoryOptions.value.find(
          (candidate) =>
            (item.sourceType === "official" &&
              Number(candidate.accessoryId) === Number(item.accessoryId)) ||
            (item.sourceType === "custom" &&
              Number(candidate.merchantAccessoryId) ===
                Number(item.merchantAccessoryId)),
        );
        return option
          ? {
              accessoryOptionKey: option.optionKey,
              quantity: Number(item.quantity) || 1,
            }
          : null;
      })
      .filter(Boolean);
  } catch (error) {
    extraAccessoryItems.value = [];
  }
}

function normalizeAccessoryOptions(rows) {
  return (Array.isArray(rows) ? rows : []).map((item) => {
    const official = item.sourceType === "official";
    return {
      ...item,
      productCode: official ? item.accessoryCode : item.productCode,
      productName: official ? item.accessoryName : item.productName,
      compatibleModelIds: official ? item.modelIds : item.compatibleModelIds,
      optionKey: `${official ? "official" : "custom"}:${official ? item.accessoryId : item.merchantAccessoryId}`,
    };
  });
}

/** 仅在订单主型号确定后加载兼容配件，并丢弃切换型号前的旧响应。 */
async function loadOrderAccessoryOptions(modelId) {
  const requestToken = ++accessoryOptionsRequestToken;
  accessoryOptions.value = [];
  const normalizedModelId = Number(modelId);
  if (!Number.isFinite(normalizedModelId) || normalizedModelId <= 0) return;
  try {
    const response =
      await listMerchantPackageAccessoryOptions(normalizedModelId);
    if (requestToken !== accessoryOptionsRequestToken) return;
    accessoryOptions.value = normalizeAccessoryOptions(
      response?.data ?? response,
    );
  } catch (_error) {
    if (requestToken === accessoryOptionsRequestToken) {
      accessoryOptions.value = [];
    }
  }
}

/** 标准型号下拉展示品牌、型号和规格，提交时仍只传递型号唯一编号。 */
function modelOptionLabel(model) {
  return [model.brand, model.modelName, model.memorySpec]
    .filter(Boolean)
    .join(" / ");
}
/** 顶部统计单独请求，避免列表筛选或分页影响概览数字。 */
function loadOrderSummary() {
  getMerchantOrderSummary()
    .then((response) => {
      const data = response.data || response;
      summary.total = Number(data.total || 0);
      summary.pendingShip = Number(data.pendingShip || 0);
      summary.shipping = Number(data.shipping || 0);
      summary.attention = Number(data.attention || 0);
    })
    .catch(() => {
      summary.total = 0;
      summary.pendingShip = 0;
      summary.shipping = 0;
      summary.attention = 0;
    });
}

/** 读取店铺设置中的订单复制字段，失败时保留后端默认字段。 */
function loadOrderCopySettings() {
  if (!useRemoteOrders) return;
  getMerchantStoreSettings()
    .then((response) => {
      const data = response?.data || response || {};
      const settings = data.settings || {};
      if (Array.isArray(settings.orderCopyFields)) {
        orderCopyFields.value = settings.orderCopyFields.filter(
          (field) => ORDER_COPY_FIELD_LABELS[field],
        );
      }
    })
    .catch(() => {
      orderCopyFields.value = [...DEFAULT_ORDER_COPY_FIELDS];
    });
}

/** 加载订单筛选和录单使用的启用渠道、启用仓库。 */
function loadOrderOptions() {
  Promise.all([
    listMerchantOrderChannels({ status: 0, pageNum: 1, pageSize: 1000 }),
    listMerchantWarehouses({ status: 0, pageNum: 1, pageSize: 1000 }),
    listMerchantPackages({ status: 0, pageNum: 1, pageSize: 1000 }),
    listBrandModelOptions(),
  ])
    .then(
      ([
        channelResponse,
        warehouseResponse,
        packageResponse,
        modelResponse,
      ]) => {
        channelOptions.value = responseRows(channelResponse);
        warehouseOptions.value = responseRows(warehouseResponse);
        setDefaultCreateWarehouses();
        packageOptions.value = responseRows(packageResponse);
        modelOptions.value = responseRows(modelResponse);
        if (createVisible.value && createForm.packageId) {
          createForm.modelId = modelIdForPackage(createForm.packageId);
          loadOrderAccessoryOptions(createForm.modelId);
        } else {
          loadOrderAccessoryOptions(null);
        }
      },
    )
    .catch(() => {
      channelOptions.value = [];
      warehouseOptions.value = [];
      packageOptions.value = [];
      modelOptions.value = [];
      loadOrderAccessoryOptions(null);
    });
  // 快递公司仅影响发货/转寄弹窗，不能阻断订单创建表单的核心选项加载。
  listMerchantExpressCompanies()
    .then((expressResponse) => {
      const rows = expressResponse.data || expressResponse;
      expressCompanyOptions.value = (Array.isArray(rows) ? rows : [])
        .map((item) => {
          try {
            const rule = JSON.parse(item.dictValue);
            return {
              code: rule.code,
              name: item.dictLabel,
              pattern: rule.pattern,
            };
          } catch (error) {
            return { code: item.dictValue, name: item.dictLabel, pattern: "" };
          }
        })
        .filter((item) => item.code && item.name);
    })
    .catch(() => {
      expressCompanyOptions.value = [];
    });
}

/** 统一解包分页接口，兼容 TableDataInfo 和 AjaxResult(data=TableDataInfo)。 */
function responseRows(response) {
  const body = response?.data ?? response;
  const page = body?.data ?? body;
  return Array.isArray(page) ? page : page?.rows || [];
}

/** 根据平台字典值中的正则校验当前快递公司的单号。 */
function validateLogisticsTrackingRule(carrierValue, trackingValue, callback) {
  const carrier = String(carrierValue || "").trim();
  const trackingNo = String(trackingValue || "").trim();
  if (!carrier && !trackingNo) return callback();
  if (!carrier) return callback(new Error("请选择快递公司"));
  if (!trackingNo) return callback(new Error("请输入快递单号"));
  const company = expressCompanyOptions.value.find(
    (item) => String(item.code) === carrier,
  );
  if (company?.pattern) {
    try {
      if (!new RegExp(company.pattern).test(trackingNo)) {
        return callback(new Error(`${company.name}快递单号格式不正确`));
      }
    } catch (_error) {
      return callback(new Error("快递单号校验规则配置错误，请联系平台管理员"));
    }
  }
  callback();
}

function validateTrackingNoRule(_rule, _value, callback) {
  if (workflowAction.value !== "发货")
    return callback();
  validateLogisticsTrackingRule(
    workflowForm.carrier,
    workflowForm.trackingNo,
    callback,
  );
}

function validateCreateShippingTrackingRule(_rule, _value, callback) {
  validateLogisticsTrackingRule(
    createForm.expressCompany,
    createForm.expressTrackingNo,
    callback,
  );
}

function validateCreateReturnTrackingRule(_rule, _value, callback) {
  validateLogisticsTrackingRule(
    createForm.returnExpressCompany,
    createForm.returnExpressTrackingNo,
    callback,
  );
}

function validateTrackingNo() {
  workflowFormRef.value?.validateField("trackingNo");
}

function validateCreateShippingTracking() {
  createFormRef.value?.validateField(["expressCompany", "expressTrackingNo"]);
}

function validateCreateReturnTracking() {
  createFormRef.value?.validateField([
    "returnExpressCompany",
    "returnExpressTrackingNo",
  ]);
}

/** 执行订单筛选，并将筛选结果保持在当前页面，不重新创建动态路由。 */
function handleQuery() {
  queryParams.pageNum = 1;
  loadOrders();
  lastUpdated.value = "已筛选";
}

/** 清除订单筛选条件并恢复全部订单。 */
function resetQuery() {
  queryParams.keyword = "";
  queryParams.status = undefined;
  queryParams.channelId = null;
  queryParams.warehouseId = null;
  queryParams.attentionOnly = false;
  queryParams.shippingOnly = false;
  queryParams.dateType = "ship";
  queryParams.dateRange = [];
  queryParams.sortType = "create";
  queryParams.sortDirection = "desc";
  queryParams.orderScope = normalizeOrderScope(route.query.orderScope);
  handleQuery();
}

/** 将动态菜单传入的路由参数限制为订单中心支持的快捷视图。 */
function normalizeOrderScope(value) {
  const scope = Array.isArray(value) ? value[0] : value;
  return typeof scope === "string" && ORDER_SCOPE_VALUES.has(scope.trim())
    ? scope.trim()
    : "";
}

/** 点击统计卡片时快速切换对应的订单状态筛选。 */
function isSummaryItemActive(item) {
  return item.attentionOnly
    ? queryParams.attentionOnly
    : item.shippingOnly
      ? queryParams.shippingOnly
      : !queryParams.attentionOnly &&
        !queryParams.shippingOnly &&
        !queryParams.orderScope &&
        queryParams.status === item.value;
}

/** 汇总卡片与列表筛选保持同一业务语义，异常提醒不再伪装成单一订单状态。 */
function selectSummaryItem(item) {
  queryParams.orderScope = "";
  queryParams.attentionOnly = item.attentionOnly;
  queryParams.shippingOnly = item.shippingOnly;
  queryParams.status =
    item.attentionOnly || item.shippingOnly || queryParams.status === item.value
      ? undefined
      : item.value;
  handleQuery();
}

/** 联盟原订单已有子订单时由子订单维护物流，原订单入口只读。 */
function isLogisticsEditDisabled(order) {
  return Boolean(order?.isAllianceSourceLocked);
}

function logisticsEditDisabledReason(order) {
  return isLogisticsEditDisabled(order)
    ? "原订单已有已接受的商户联盟订单，仅允许查看"
    : "编辑物流信息";
}

/** 只有真实填写的快递单号才允许打开物流轨迹弹窗。 */
function hasLogisticsTrackingNo(value) {
  const trackingNo = String(value || "").trim();
  return trackingNo !== "" && trackingNo !== "待安排" && trackingNo !== "-";
}

function logisticsTrackingDirectionLabel(direction) {
  return direction === "return" ? "归还" : "寄出";
}

function getExpressCompanyName(carrier) {
  return formatExpressCompanyName(carrier, expressCompanyOptions.value) || "快递公司";
}

function createDemoLogisticsTracking(order, direction, trackingNo) {
  const directionLabel = logisticsTrackingDirectionLabel(direction);
  return {
    orderNo: order.orderNo,
    direction: directionLabel,
    carrierName: getExpressCompanyName(
      direction === "return"
        ? order.returnExpressCompany
        : order.expressCompany,
    ),
    trackingNo,
    available: true,
    status: "demo",
    events: [
      {
        time: order.createdAt || "-",
        status: "演示",
        description: `演示模式：${directionLabel}物流轨迹待接入真实快递服务`,
        location: "",
      },
    ],
  };
}

/** 打开寄出或归还方向的物流轨迹弹窗，并按订单归属查询真实轨迹。 */
async function openLogisticsTracking(order, direction) {
  if (!order || (useRemoteOrders && !order.orderId)) return;
  const trackingValue =
    direction === "return" ? order.returnNo : order.trackingNo;
  if (!hasLogisticsTrackingNo(trackingValue)) return;

  const requestToken = ++logisticsTrackingRequestToken;
  const carrier =
    direction === "return" ? order.returnExpressCompany : order.expressCompany;
  logisticsTracking.value = {
    orderNo: order.orderNo,
    direction: logisticsTrackingDirectionLabel(direction),
    carrierName: getExpressCompanyName(carrier),
    trackingNo: trackingValue,
    available: false,
    events: [],
  };
  logisticsTrackingError.value = "";
  logisticsTrackingVisible.value = true;

  if (!useRemoteOrders) {
    logisticsTracking.value = createDemoLogisticsTracking(
      order,
      direction,
      trackingValue,
    );
    return;
  }

  logisticsTrackingLoading.value = true;
  try {
    const response = await getMerchantOrderLogisticsTrack(
      order.orderId,
      direction === "return"
        ? RENTAL_CODE.DELIVERY_DIRECTION.RETURN
        : RENTAL_CODE.DELIVERY_DIRECTION.SHIP,
    );
    if (requestToken !== logisticsTrackingRequestToken) return;
    const data = response?.data ?? response ?? {};
    logisticsTracking.value = {
      ...data,
      orderNo: data.orderNo || order.orderNo,
      direction: data.direction || logisticsTrackingDirectionLabel(direction),
      carrierName: getExpressCompanyName(data.carrier || carrier),
      trackingNo: data.trackingNo || trackingValue,
      events: Array.isArray(data.events) ? data.events : [],
    };
  } catch (error) {
    if (requestToken !== logisticsTrackingRequestToken) return;
    const message = error?.message;
    logisticsTrackingError.value =
      message && message !== "error" ? message : "物流轨迹加载失败，请稍后重试";
  } finally {
    if (requestToken === logisticsTrackingRequestToken) {
      logisticsTrackingLoading.value = false;
    }
  }
}

/** 打开寄出或归还方向共用的物流编辑弹窗。 */
function openLogisticsEdit(order, direction) {
  if (!order?.orderId) return;
  if (isLogisticsEditDisabled(order)) {
    proxy.$modal.msgWarning(logisticsEditDisabledReason(order));
    return;
  }
  logisticsEditOrder.value = order;
  logisticsEditDirection.value = direction;
  logisticsEditVisible.value = true;
}

/** 保存物流信息时只调用物流专用接口，订单状态和其他订单字段保持不变。 */
async function submitLogisticsEdit({ carrier, trackingNo }) {
  const order = logisticsEditOrder.value;
  if (!order?.orderId) return;
  if (!useRemoteOrders) {
    if (logisticsEditDirection.value === "return") {
      order.returnExpressCompany = carrier;
      order.returnNo = trackingNo;
    } else {
      order.expressCompany = carrier;
      order.trackingNo = trackingNo;
    }
    logisticsEditVisible.value = false;
    proxy.$modal.msgSuccess("物流信息更新成功");
    return;
  }

  logisticsEditLoading.value = true;
  try {
    await updateMerchantOrderLogistics(order.orderId, {
      direction:
        logisticsEditDirection.value === "return"
          ? RENTAL_CODE.DELIVERY_DIRECTION.RETURN
          : RENTAL_CODE.DELIVERY_DIRECTION.SHIP,
      carrier,
      trackingNo,
    });
    logisticsEditVisible.value = false;
    proxy.$modal.msgSuccess("物流信息更新成功");
    await loadOrders();
  } catch (_error) {
    // request 拦截器已经展示服务端错误，保留弹窗让用户修正后重试。
  } finally {
    logisticsEditLoading.value = false;
  }
}

/** 统一处理订单操作；禁用动作由订单状态机计算，后端仍执行最终校验。 */
function handleAction(action, order) {
  const actionState = order?.actions?.find((item) => item.label === action);
  if (actionState?.disabled) {
    proxy.$modal.msgWarning(
      actionState.disabledReason || "当前订单状态不支持此操作",
    );
    return;
  }
  if (action === "编辑") {
    if (order.isAllianceChild || order.isAllianceSourceLocked) {
      proxy.$modal.msgWarning(
        order.isAllianceChild
          ? "商户联盟子订单仅允许设置套餐"
          : "原订单已有已接受的商户联盟订单，仅允许查看",
      );
      return;
    }
    openEditOrder(order);
    return;
  }
  if (action === "设置套餐") {
    openEditOrder(order);
    return;
  }
  if (action === "复制") {
    copyOrderToClipboard(order);
    return;
  }
  if (action === "批注") {
    openAnnotationCreate(order);
    return;
  }
  if (action === "转单") {
    openAllianceTransfer(order);
    return;
  }
  if (action === "编辑记录") {
    openEditLogHistory(order);
    return;
  }
  openWorkflowDialog(action, order);
}

/** 商品清单详情只展示订单已固化的套餐快照，不参与订单工作流处理。 */
function openPackageDetail(order) {
  activeOrder.value = order;
  orderMedia.value = [];
  detailVisible.value = true;
  loadOrderMedia(order?.orderId);
}

/** 打开公共订单详情组件，详情接口和履约凭证由组件统一加载。 */
function openOrderDetail(order) {
  if (!order?.orderId) return;
  const baseOrder = normalizeOrder(order);
  orderDetail.value = baseOrder;
  orderDetailVisible.value = true;
}

/** 列表异步返回后补齐从趋势页跳转详情所需的名称和快照字段。 */
function mergeRouteOrderSnapshot() {
  const rawOrderId = Array.isArray(route.query.orderId)
    ? route.query.orderId[0]
    : route.query.orderId;
  const orderId = Number(rawOrderId);
  if (
    !Number.isSafeInteger(orderId) ||
    orderId <= 0 ||
    !orderDetailVisible.value ||
    Number(orderDetail.value?.orderId) !== orderId
  ) {
    return;
  }
  const cachedOrder = orders.value.find(
    (item) => Number(item.orderId) === orderId,
  );
  if (!cachedOrder) return;
  orderDetail.value = normalizeOrder({
    ...cachedOrder,
    ...orderDetail.value,
    packageDetail: cachedOrder.packageDetail || orderDetail.value.packageDetail,
  });
  activeOrder.value = orderDetail.value;
}

/** 打开批量视频匹配入口，同时加载上次未完成的人工处理记录。 */
async function openBatchMediaDialog() {
  batchMediaForm.fileUrls = "";
  batchMediaRows.value = [];
  batchMediaVisible.value = true;
  const initialFileUrls = batchMediaForm.fileUrls;
  try {
    const response = await listUnmatchedMerchantOrderMedia();
    // 上传可能先于初始化查询完成，不能用旧查询结果覆盖刚上传的文件。
    if (batchMediaForm.fileUrls === initialFileUrls) {
      batchMediaRows.value = responseRows(response);
    }
  } catch (_error) {
    if (batchMediaForm.fileUrls === initialFileUrls) {
      batchMediaRows.value = [];
    }
  }
}

/** 文件选择变化时只补充新文件，避免用户已录入的 SN 被上传组件刷新覆盖。 */
watch(
  () => batchMediaForm.fileUrls,
  (value) => {
    const urls = String(value || "")
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean);
    if (!urls.length) return;
    const previous = new Map(
      batchMediaRows.value
        .filter((row) => row?.fileUrl)
        .map((row) => [row.fileUrl, row]),
    );
    batchMediaRows.value = urls.map((fileUrl) => {
      const row = previous.get(fileUrl);
      return (
        row || {
          fileUrl,
          fileName: fileNameFromUrl(fileUrl),
          detectedSn: "",
          matchStatus: null,
          matchStatusLabel: "待上传",
        }
      );
    });
  },
);

/** 提交新上传的视频；服务端返回每个文件的匹配结果，失败项留在表格中补录。 */
async function submitBatchMedia() {
  const rows = batchMediaRows.value.filter((row) => !row.mediaId && row.fileUrl);
  if (!rows.length) {
    proxy.$modal.msgWarning("请先选择需要上传的视频");
    return;
  }
  batchMediaLoading.value = true;
  try {
    const response = await addMerchantOrderMediaBatch({
      mediaType: "ship_video",
      items: rows.map((row) => ({
        fileUrl: row.fileUrl,
        fileName: row.fileName || fileNameFromUrl(row.fileUrl),
        detectedSn: row.detectedSn || undefined,
      })),
    });
    batchMediaRows.value = responseRows(response);
    proxy.$modal.msgSuccess("视频已上传，匹配结果已更新");
    await loadOrders();
  } catch (_error) {
    // request 拦截器已展示服务端错误，保留当前列表方便重试或补录。
  } finally {
    batchMediaLoading.value = false;
  }
}

/** 对服务端无法自动匹配的视频补录 SN，并立即刷新绑定结果。 */
async function recognizeBatchMediaRow(row) {
  if (!row?.mediaId) return;
  let detectedSn = String(row.detectedSn || "").trim();
  if (!detectedSn) {
    try {
      const promptResult = await proxy.$modal.prompt("请输入视频对应的设备SN");
      detectedSn = String(promptResult?.value || "").trim();
    } catch (_error) {
      return;
    }
  }
  if (!detectedSn) {
    proxy.$modal.msgWarning("请输入设备SN后再匹配");
    return;
  }
  try {
    const response = await recognizeMerchantOrderMedia(row.mediaId, { detectedSn });
    const updated = response?.data ?? response;
    const index = batchMediaRows.value.findIndex(
      (item) => Number(item.mediaId) === Number(row.mediaId),
    );
    if (index >= 0) batchMediaRows.value[index] = updated;
    if (Number(updated?.matchStatus) === 1) {
      proxy.$modal.msgSuccess(`已匹配订单 ${updated.orderNo || ""}`);
      await loadOrders();
    } else {
      proxy.$modal.msgWarning("未找到可匹配的未完成订单，请检查SN或订单状态");
    }
  } catch (_error) {
    // request 拦截器已展示服务端错误。
  }
}

function batchMediaStatusType(status) {
  if (Number(status) === 1) return "success";
  if (Number(status) === 2) return "warning";
  return "info";
}

/** 异步加载订单履约凭证，详情主数据不因媒体服务异常而无法打开。 */
async function loadOrderMedia(orderId) {
  if (!useRemoteOrders || !orderId) return;
  orderMediaLoading.value = true;
  try {
    const response = await listMerchantOrderMedia(orderId);
    orderMedia.value = responseRows(response);
  } catch (_error) {
    orderMedia.value = [];
  } finally {
    orderMediaLoading.value = false;
  }
}

/** 打开套餐逐件设备选择器，只允许服务端标记为逐件设备的明细进入。 */
async function openPackageDeviceSelector(item) {
  if (
    !activeOrder.value?.orderId ||
    !item?.selectableDevice ||
    activeOrder.value.isAllianceSourceLocked
  ) {
    return;
  }
  packageDeviceTarget.value = item;
  packageDeviceOptions.value = [];
  packageDeviceSelectedUnitIds.value = [];
  packageDeviceVisible.value = true;
  packageDeviceLoading.value = true;
  try {
    const response = await listMerchantOrderPackageDevices(
      activeOrder.value.orderId,
      item.itemIndex,
    );
    const rows = responseRows(response);
    packageDeviceOptions.value = rows;
    const selectedIds = new Set(
      (item.unitIds || []).map((unitId) => Number(unitId)),
    );
    packageDeviceSelectedUnitIds.value = rows
      .filter((row) => selectedIds.has(Number(row.unitId)))
      .map((row) => Number(row.unitId));
    await nextTick();
    packageDeviceOptions.value.forEach((row) => {
      if (selectedIds.has(Number(row.unitId))) {
        packageDeviceTableRef.value?.toggleRowSelection(row, true);
      }
    });
  } catch (_error) {
    packageDeviceOptions.value = [];
    proxy.$modal.msgError("设备列表加载失败，请稍后重试");
  } finally {
    packageDeviceLoading.value = false;
  }
}

/** 记录设备表格的多选结果，提交前由服务端再次校验数量和占用状态。 */
function handlePackageDeviceSelection(selection) {
  packageDeviceSelectedUnitIds.value = (selection || [])
    .map((row) => Number(row.unitId))
    .filter((unitId) => Number.isFinite(unitId) && unitId > 0);
}

/** 保存套餐明细设备绑定，并立即更新当前详情中的序列号展示。 */
async function submitPackageDeviceBinding() {
  const orderId = activeOrder.value?.orderId;
  const target = packageDeviceTarget.value;
  const quantity = target?.quantity || 1;
  if (
    !orderId ||
    !target ||
    packageDeviceSelectedUnitIds.value.length !== quantity
  ) {
    proxy.$modal.msgWarning(`请选择 ${quantity} 台设备`);
    return;
  }
  packageDeviceBinding.value = true;
  try {
    await bindMerchantOrderPackageDevices(orderId, {
      itemIndex: target.itemIndex,
      unitIds: packageDeviceSelectedUnitIds.value,
    });
    const selectedRows = packageDeviceOptions.value.filter((row) =>
      packageDeviceSelectedUnitIds.value.includes(Number(row.unitId)),
    );
    target.unitIds = selectedRows.map((row) => Number(row.unitId));
    target.serialNumbers = selectedRows.map(
      (row) => row.serialNumber || row.unitCode,
    );
    packageDeviceVisible.value = false;
    proxy.$modal.msgSuccess("设备绑定成功");
    loadOrders();
  } catch (_error) {
    // request 拦截器已经展示服务端业务错误，保留详情弹窗方便用户重试。
  } finally {
    packageDeviceBinding.value = false;
  }
}

/** 将空值和页面占位文案统一转换为可复制的短文本。 */
function copyText(value) {
  const text = String(value ?? "").trim();
  return text && !["-", "暂无客户备注", "待安排", "待查询"].includes(text)
    ? text
    : "-";
}

/** 表单回显时清除列表占位符，避免把占位符当成新的收件人数据入库。 */
function workflowText(value) {
  const text = String(value ?? "").trim();
  return text && !["-", "暂无客户备注", "待安排", "待查询"].includes(text)
    ? text
    : "";
}

/** 费用使用订单原始数值，避免复制卡片中的格式化占位内容。 */
function copyAmount(value) {
  if (value === null || value === undefined || value === "") return "-";
  const amount = Number(value);
  return Number.isFinite(amount) ? `${amount.toFixed(2)} 元` : copyText(value);
}

/** 根据店铺设置的字段分组生成复制文本，不携带订单主键或技术字段。 */
function buildOrderCopyLines(order, field) {
  const customer = order?.customer || {};
  const packageDetail = order?.packageDetail || {};
  const mainProduct = packageDetail.mainProduct || {};
  const customerName = copyText(order?.customerName || customer.name);
  const customerPhone = copyText(order?.customerPhone || customer.phone);
  const customerNickname = copyText(
    order?.customerNickname || customer.nickname,
  );
  const recipientName = copyText(
    order?.recipientName || customer.name || order?.customerName,
  );
  const recipientPhone = copyText(
    order?.recipientPhone || customer.phone || order?.customerPhone,
  );
  const recipientAddress = copyText(
    order?.recipientAddress || order?.customerAddress || customer.address,
  );
  const packageName = copyText(
    order?.packageName || packageDetail.packageName,
  );
  const modelName = copyText(
    order?.model ||
      order?.modelName ||
      mainProduct.model ||
      mainProduct.name,
  );
  const leasePeriod = copyText(
    order?.rentalPeriod ||
      formatLeasePeriod(order?.leaseStartDate, order?.leaseEndDate),
  );
  const leaseDays = Number(order?.leaseDays);
  const shippingWarehouse = copyText(
    order?.warehouseName || order?.shipping?.warehouse,
  );
  const returnWarehouse = copyText(
    order?.returnWarehouseName || order?.returnWarehouse,
  );
  const arrivalDate = copyText(
    normalizeDateOnly(order?.arrivalDate || order?.delivery?.expectedDate),
  );

  switch (field) {
    case "customer":
      return [
        `客户姓名：${customerName}`,
        `联系电话：${customerPhone}`,
        `买家昵称：${customerNickname}`,
      ];
    case "recipient":
      return [
        `收件人姓名：${recipientName}`,
        `收件人电话：${recipientPhone}`,
        `收货地址：${recipientAddress}`,
      ];
    case "package":
      return [`产品套餐：${packageName}`, `型号：${modelName}`];
    case "lease":
      return [
        `租期：${leasePeriod}`,
        `租用天数：${Number.isFinite(leaseDays) && leaseDays > 0 ? leaseDays : "-"}`,
        `发货日期：${copyText(order?.shipDueTime || order?.shipping?.shipDate)}`,
        `应该归还日期：${copyText(order?.returnDueTime || order?.returnDate)}`,
        `预计到库日期：${arrivalDate}`,
      ];
    case "warehouse":
      return [`发货仓库：${shippingWarehouse}`, `归还仓库：${returnWarehouse}`];
    case "amount":
      return [
        `租金：${copyAmount(order?.rentAmount ?? order?.rent)}`,
        `押金：${copyAmount(order?.depositAmount ?? order?.deposit)}`,
        `配送费用：${copyAmount(order?.deliveryAmount)}`,
        `保险费用：${copyAmount(order?.insuranceAmount)}`,
      ];
    case "remark":
      return [
        `订单备注：${copyText(
          order?.remark || order?.customerNote || customer.note,
        )}`,
      ];
    default:
      return [];
  }
}

/** 兼容非安全上下文，保证本地 HTTP 和正式 HTTPS 都能复制订单内容。 */
function fallbackCopyText(text) {
  const textarea = document.createElement("textarea");
  textarea.value = text;
  textarea.setAttribute("readonly", "");
  textarea.style.position = "fixed";
  textarea.style.opacity = "0";
  document.body.appendChild(textarea);
  textarea.select();
  const copied = document.execCommand("copy");
  document.body.removeChild(textarea);
  return copied;
}

async function writeClipboardText(text) {
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(text);
      return true;
    }
  } catch (_error) {
    // 浏览器拒绝剪切板权限时继续使用兼容方案。
  }
  return fallbackCopyText(text);
}

/** 按店铺设置的顺序复制订单业务信息，不打开新增订单表单。 */
async function copyOrderToClipboard(order) {
  const fields = orderCopyFields.value.filter(
    (field) => ORDER_COPY_FIELD_LABELS[field],
  );
  if (!fields.length) {
    proxy.$modal.msgWarning("请先在店铺设置中选择订单复制字段");
    return;
  }
  const text = fields
    .flatMap((field) => buildOrderCopyLines(order, field))
    .join("\n");
  try {
    const copied = await writeClipboardText(text);
    if (!copied) throw new Error("clipboard unavailable");
    proxy.$modal.msgSuccess("订单信息已复制到剪切板");
  } catch (_error) {
    proxy.$modal.msgError("复制失败，请检查浏览器剪切板权限");
  }
}

/** 打开当前订单的客户和收件人信息编辑弹窗。 */
function openCustomerEdit(order) {
  activeOrder.value = order;
  const customerRemark =
    order.customer?.note || order.customerNote || order.remark || "";
  Object.assign(customerEditForm, {
    customerName: order.customerName || order.customer?.name || "",
    customerPhone: order.customerPhone || order.customer?.phone || "",
    customerNickname: order.customerNickname || order.customer?.nickname || "",
    recipientName: order.recipientName || order.customer?.name || "",
    recipientPhone: order.recipientPhone || order.customer?.phone || "",
    recipientAddress: order.recipientAddress || order.customer?.address || "",
    // 兼容旧版本把列表占位文案误保存为订单备注的历史数据。
    remark: customerRemark === "暂无客户备注" ? "" : customerRemark,
  });
  customerEditVisible.value = true;
}

/** 打开转单弹窗，目标商户由后端按当前商户的有效联盟关系返回。 */
async function openAllianceTransfer(order) {
  allianceTransferOrder.value = order;
  Object.assign(allianceTransferForm, {
    target: "",
    settlementPrice: null,
    shippingMode: null,
    shippingPayer: null,
    remark: "",
  });
  allianceOptions.value = [];
  allianceTransferVisible.value = true;
  try {
    const response = await listMerchantAllianceOptions();
    allianceOptions.value = Array.isArray(response?.data)
      ? response.data
      : response || [];
  } catch (error) {
    allianceTransferVisible.value = false;
  }
}

/** 提交转单请求，幂等键避免重复点击产生重复协作任务。 */
async function submitAllianceTransfer() {
  if (!allianceTransferFormRef.value || !allianceTransferOrder.value) return;
  const valid = await allianceTransferFormRef.value
    .validate()
    .catch(() => false);
  if (!valid) return;
  const publicPool = allianceTransferForm.target === "public-pool";
  const designated = allianceTransferForm.target.startsWith("merchant:");
  allianceTransferLoading.value = true;
  try {
    const response = await transferMerchantOrder({
      orderId: allianceTransferOrder.value.orderId,
      transferMode: publicPool ? 3 : designated ? 2 : 1,
      targetMemberId: designated
        ? Number(allianceTransferForm.target.slice(8))
        : null,
      settlementPrice: allianceTransferForm.settlementPrice,
      shippingMode: allianceTransferForm.shippingMode,
      shippingPayer: allianceTransferForm.shippingPayer,
      remark: allianceTransferForm.remark,
      idempotencyKey: createIdempotencyKey(),
    });
    const result = response?.data ?? response;
    proxy.$modal.msgSuccess(
      `转单已提交，共生成 ${Number(result?.createdCount || 1)} 个协作任务`,
    );
    allianceTransferVisible.value = false;
  } finally {
    allianceTransferLoading.value = false;
  }
}

/** 保存当前订单的客户和收件人信息，并重新读取列表使卡片和精简视图保持一致。 */
function submitCustomerEdit() {
  customerEditFormRef.value.validate((valid) => {
    if (!valid || !activeOrder.value?.orderId) return;
    customerEditLoading.value = true;
    updateMerchantOrderRecipient(activeOrder.value.orderId, {
      ...customerEditForm,
    })
      .then(() => {
        proxy.$modal.msgSuccess("客户信息更新成功");
        customerEditVisible.value = false;
        loadOrders();
      })
      .finally(() => {
        customerEditLoading.value = false;
      });
  });
}

/** 将列表订单转换为订单表单可提交的字段，避免把展示字段直接提交给后端。 */
function orderFormValues(order) {
  const customer = order.customer || {};
  return {
    customerName: order.customerName || customer.name || "",
    customerNickname: order.customerNickname || "",
    customerPhone: order.customerPhone || customer.phone || "",
    recipientName:
      order.recipientName || customer.name || order.customerName || "",
    recipientPhone:
      order.recipientPhone || customer.phone || order.customerPhone || "",
    channelId: order.channelId || null,
    shopName: order.shopName || "",
    externalOrderNo: order.externalOrderNo || "",
    recipientAddress:
      order.recipientAddress || order.customerAddress || customer.address || "",
    productId: order.productId || null,
    modelId: modelIdForPackage(order.packageId),
    packageId: order.packageId || null,
    accessoryItemIds: order.accessoryItemIds || null,
    warehouseId: order.warehouseId || null,
    returnWarehouseId: order.returnWarehouseId || null,
    leasePeriod: formatLeasePeriod(order.leaseStartDate, order.leaseEndDate),
    leaseStartDate: normalizeDateOnly(order.leaseStartDate),
    leaseEndDate: normalizeDateOnly(order.leaseEndDate),
    shipDueTime: normalizeDateOnly(order.shipDueTime),
    shipTimeSlot: order.shipTimeSlot || "morning",
    returnDueTime: normalizeDateOnly(order.returnDueTime),
    deliveryMethod: order.deliveryMethod || "express",
    expressCompany: order.expressCompany || "",
    expressTrackingNo: order.expressTrackingNo || "",
    returnDeliveryMethod: order.returnDeliveryMethod || "express",
    returnExpressCompany: order.returnExpressCompany || "",
    returnExpressTrackingNo: order.returnExpressTrackingNo || "",
    deliveryAmount: Number(order.deliveryAmount || 0),
    insuranceAmount: Number(order.insuranceAmount || 0),
    leaseDays: Number(order.leaseDays || 0),
    arrivalDate: normalizeDateOnly(order.arrivalDate),
    rentAmount: Number(order.rentAmount ?? order.rent ?? 0),
    depositAmount: Number(order.depositAmount || 0),
    remark: order.remark || customer.note || "",
  };
}

/** 从订单套餐反查所属型号，供编辑和复制订单时回填型号选择。 */
function modelIdForPackage(packageId) {
  const packageOption = packageOptions.value.find(
    (item) => Number(item.packageId) === Number(packageId),
  );
  return packageOption?.modelId ? Number(packageOption.modelId) : null;
}

const workflowStatus = {
  备货: RENTAL_CODE.ORDER_STATUS.PREPARED,
  发货: RENTAL_CODE.ORDER_STATUS.SHIPPED,
  归还验机: RENTAL_CODE.ORDER_STATUS.COMPLETED,
  完成: RENTAL_CODE.ORDER_STATUS.COMPLETED,
};

/** 打开订单工作流弹窗，转寄、跨仓归还和续租同时编辑对应业务字段。 */
function openWorkflowDialog(action, order) {
  activeOrder.value = order;
  workflowAction.value = action;
  Object.assign(workflowForm, {
    videoUrl: "",
    carrier: "",
    trackingNo: "",
    recipientName: workflowText(
      order?.recipientName || order?.customer?.name,
    ),
    recipientPhone: workflowText(
      order?.recipientPhone || order?.customer?.phone,
    ),
    recipientAddress: workflowText(
      order?.recipientAddress || order?.customer?.address,
    ),
    result: RETURN_INSPECTION_RESULT.NORMAL,
    caseType: AFTER_SALES_CASE_TYPE.DAMAGE,
    problemDesc: "",
    compensationAmount: 0,
    warehouseId: order?.warehouseId || null,
    returnWarehouseId: order?.returnWarehouseId || null,
    leaseEndDate: order?.leaseEndDate || "",
    remark: "",
  });
  workflowVisible.value = true;
}

/** 提交订单工作流动作；状态动作使用专用接口，普通编辑字段不参与状态流转。 */
function submitWorkflow() {
  workflowFormRef.value.validate(async (valid) => {
    if (!valid || !activeOrder.value) return;
    if (workflowAction.value === "备货") {
      workflowLoading.value = true;
      prepareMerchantOrder(activeOrder.value.orderId)
        .then(() => {
          proxy.$modal.msgSuccess("备货成功");
          workflowVisible.value = false;
          loadOrders();
          loadOrderSummary();
        })
        .finally(() => {
          workflowLoading.value = false;
        });
      return;
    }
    if (workflowAction.value === "上传视频") {
      workflowLoading.value = true;
      try {
        const orderId = activeOrder.value.orderId;
        await addMerchantOrderMedia(orderId, {
          mediaType: "ship_video",
          fileUrl: workflowForm.videoUrl,
          fileName: fileNameFromUrl(workflowForm.videoUrl),
        });
        proxy.$modal.msgSuccess("履约视频登记成功");
        workflowVisible.value = false;
        await loadOrders();
        await loadOrderMedia(orderId);
      } catch (_error) {
        // request 拦截器已经展示服务端业务错误，保留弹窗让用户重试。
      } finally {
        workflowLoading.value = false;
      }
      return;
    }
    workflowLoading.value = true;
    try {
      const orderId = activeOrder.value.orderId;
      if (workflowAction.value === "发货") {
        await shipMerchantOrder(orderId, {
          carrier: workflowForm.carrier,
          trackingNo: workflowForm.trackingNo.trim(),
          remark: workflowForm.remark,
        });
      } else if (workflowAction.value === "登记归还") {
        await markMerchantOrderReturning(orderId);
      } else if (workflowAction.value === "归还验机") {
        await inspectMerchantOrderReturn(orderId, {
          result: workflowForm.result,
          caseType: workflowForm.caseType,
          problemDesc: workflowForm.problemDesc,
          compensationAmount: workflowForm.compensationAmount,
          remark: workflowForm.remark,
        });
      } else if (workflowAction.value === "完成") {
        await completeMerchantOrder(orderId, { remark: workflowForm.remark });
      } else if (workflowAction.value === "取消") {
        await cancelMerchantOrder(orderId, { remark: workflowForm.remark });
      } else if (workflowAction.value === "售后") {
        await createMerchantOrderAfterSales(orderId, {
          caseType: workflowForm.caseType,
          problemDesc: workflowForm.problemDesc,
          compensationAmount: workflowForm.compensationAmount,
          remark: workflowForm.remark,
        });
      } else if (workflowAction.value === "转寄") {
        await forwardMerchantOrder(orderId, {
          recipientName: workflowForm.recipientName.trim(),
          recipientPhone: workflowForm.recipientPhone.trim(),
          recipientAddress: workflowForm.recipientAddress.trim(),
          remark: workflowForm.remark,
        });
      } else {
        let order = activeOrder.value;
        if (useRemoteOrders) {
          const response = await getMerchantOrder(orderId);
          order = response?.data ?? response;
        }
        const payload = {
          ...orderFormValues(order),
          orderId,
          orderStatus:
            workflowStatus[workflowAction.value] ||
            order.status ||
            order.orderStatus,
          remark:
            workflowForm.remark || order.customer?.note || order.remark || "",
        };
        if (workflowAction.value === "跨仓归还")
          payload.returnWarehouseId = workflowForm.returnWarehouseId;
        if (workflowAction.value === "续租")
          payload.leaseEndDate = workflowForm.leaseEndDate;
        await updateMerchantOrder(payload);
      }
      proxy.$modal.msgSuccess(`${workflowAction.value}成功`);
      workflowVisible.value = false;
      loadOrders();
      loadOrderSummary();
    } catch (_error) {
      // request 拦截器已经展示服务端错误，保留弹窗让用户修正后重试。
    } finally {
      workflowLoading.value = false;
    }
  });
}

/** 从统一文件服务返回地址中提取展示用文件名。 */
function fileNameFromUrl(value) {
  const text = String(value || "").split("?")[0];
  const name = text.slice(text.lastIndexOf("/") + 1);
  try {
    return decodeURIComponent(name) || "履约视频";
  } catch (_error) {
    return name || "履约视频";
  }
}

/** 打开当前订单的字段编辑历史，按服务端返回的倒序记录展示。 */
function openEditLogHistory(order) {
  const requestToken = ++editLogRequestToken;
  editLogHistory.value = [];
  editLogLoading.value = false;
  editLogVisible.value = true;

  if (!useRemoteOrders || !order?.orderId) {
    return;
  }

  editLogLoading.value = true;
  getMerchantOrderEditLogs(order.orderId)
    .then((response) => {
      if (requestToken !== editLogRequestToken) return;
      const rows = Array.isArray(response.data)
        ? response.data
        : response.data?.rows || response.rows || [];
      editLogHistory.value = rows
        .map((log, index) => normalizeEditLog(log, index))
        .sort(
          (left, right) =>
            parseAnnotationTime(right.createTime) -
            parseAnnotationTime(left.createTime),
        );
    })
    .catch(() => {
      if (requestToken !== editLogRequestToken) return;
      editLogHistory.value = [];
      proxy.$modal.msgError("订单编辑记录加载失败，请稍后重试");
    })
    .finally(() => {
      if (requestToken !== editLogRequestToken) return;
      editLogLoading.value = false;
    });
}

/** 将订单编辑日志响应转换为弹层统一消费的数据结构。 */
function normalizeEditLog(log, index) {
  return {
    logId: log.logId ?? log.id ?? `edit-log-${index}`,
    fieldName: log.fieldName ?? "订单字段",
    beforeDisplayValue: log.beforeDisplayValue ?? "",
    afterDisplayValue: log.afterDisplayValue ?? "",
    changeReason: log.changeReason ?? "",
    operatorName: log.operatorName ?? "系统",
    createTime: log.createTime ?? "-",
  };
}

/** 打开当前订单的批注历史弹层，并按真实订单或演示数据加载记录。 */
function openAnnotationHistory(order) {
  const requestToken = ++annotationRequestToken;
  annotationHistory.value = [];
  annotationLoading.value = false;
  annotationVisible.value = true;

  if (!useRemoteOrders || !order?.orderId) {
    annotationHistory.value = createDemoAnnotationHistory(order);
    return;
  }

  annotationLoading.value = true;
  getMerchantOrderNotes(order.orderId)
    .then((response) => {
      if (requestToken !== annotationRequestToken) return;
      const rows = Array.isArray(response.data)
        ? response.data
        : response.data?.rows || response.rows || [];
      annotationHistory.value = rows
        .map((note, index) => normalizeAnnotation(note, index))
        .sort(
          (left, right) =>
            parseAnnotationTime(right.createTime) -
            parseAnnotationTime(left.createTime),
        );
    })
    .catch(() => {
      if (requestToken !== annotationRequestToken) return;
      annotationHistory.value = [];
      proxy.$modal.msgError("批注记录加载失败，请稍后重试");
    })
    .finally(() => {
      if (requestToken !== annotationRequestToken) return;
      annotationLoading.value = false;
    });
}

/** 打开当前订单的新增批注弹层，订单由当前操作行绑定。 */
function openAnnotationCreate(order) {
  activeOrder.value = order;
  annotationCreateForm.content = "";
  annotationCreateForm.noteType = 1;
  annotationCreateVisible.value = true;
}

/** 提交当前订单的新批注，并刷新订单列表中的提醒摘要。 */
async function submitAnnotation() {
  if (!activeOrder.value?.orderId || !annotationCreateFormRef.value) return;
  const valid = await annotationCreateFormRef.value
    .validate()
    .catch(() => false);
  if (!valid) return;

  annotationCreateLoading.value = true;
  try {
    await addMerchantOrderNote(activeOrder.value.orderId, {
      noteContent: annotationCreateForm.content.trim(),
      noteType: annotationCreateForm.noteType,
    });
    annotationCreateVisible.value = false;
    proxy.$modal.msgSuccess("批注添加成功");
    await loadOrders();
  } catch (_error) {
    proxy.$modal.msgError("批注添加失败，请稍后重试");
  } finally {
    annotationCreateLoading.value = false;
  }
}

/** 将服务端批注响应转换为弹层统一消费的数据结构。 */
function normalizeAnnotation(note, index) {
  const noteType = Number(note.noteType) === 2 ? 2 : 1;
  return {
    noteId: note.noteId ?? note.id ?? `note-${index}`,
    content: note.noteContent ?? note.content ?? note.remark ?? "暂无批注内容",
    typeLabel: noteType === 2 ? "收货批注" : "发货批注",
    operatorName:
      note.operatorName ?? note.createByName ?? note.createBy ?? "系统",
    createTime: note.createTime ?? "-",
  };
}

/** 仅在本地演示数据模式下生成当前页面已有批注，真实模式完全以接口数据为准。 */
function createDemoAnnotationHistory(order) {
  if (!order?.attention) return [];
  return [
    normalizeAnnotation(
      {
        noteId: `${order.orderNo}-attention`,
        noteContent: order.attention,
        operatorName: order.operator || "系统",
        createTime: order.createdAt || "-",
      },
      0,
    ),
  ];
}

/** 兼容后端统一返回的本地时间字符串，保证倒序比较不受浏览器日期解析差异影响。 */
function parseAnnotationTime(value) {
  if (!value) return 0;
  const timestamp = Date.parse(String(value).replace(" ", "T"));
  return Number.isNaN(timestamp) ? 0 : timestamp;
}

/** 打开新增订单表单并清空上一次录单数据。 */
function openCreateOrder() {
  editingOrderId.value = null;
  alliancePackageOnly.value = false;
  Object.assign(createForm, {
    customerName: "",
    customerNickname: "",
    customerPhone: "",
    recipientName: "",
    recipientPhone: "",
    channelId: null,
    shopName: "",
    externalOrderNo: "",
    recipientAddress: "",
    productId: null,
    modelId: null,
    packageId: null,
    accessoryItemIds: null,
    warehouseId: null,
    returnWarehouseId: null,
    leasePeriod: "",
    leaseStartDate: "",
    leaseEndDate: "",
    shipDueTime: "",
    shipTimeSlot: "morning",
    returnDueTime: "",
    deliveryMethod: "express",
    expressCompany: "",
    expressTrackingNo: "",
    returnDeliveryMethod: "express",
    returnExpressCompany: "",
    returnExpressTrackingNo: "",
    deliveryAmount: 0,
    insuranceAmount: 0,
    leaseDays: 0,
    arrivalDate: "",
    rentAmount: 0,
    depositAmount: 0,
    remark: "",
  });
  setDefaultCreateWarehouses();
  selectedPackageDetail.value = null;
  extraAccessoryItems.value = [];
  loadOrderAccessoryOptions(null);
  createVisible.value = true;
}

/** 打开订单编辑表单并将列表中的可编辑字段映射到提交命令。 */
async function openEditOrder(order) {
  if (!order?.orderId) return;
  try {
    const response = await getMerchantOrder(order.orderId);
    const detail = response?.data ?? response;
    editingOrderId.value = order.orderId;
    alliancePackageOnly.value = Boolean(order.isAllianceChild);
    Object.assign(createForm, orderFormValues(detail));
    await handlePackageChange(createForm.packageId);
    restoreExtraAccessories(detail.accessoryItemIds);
    createVisible.value = true;
  } catch (error) {
    // request 拦截器已经展示服务端错误，这里不打开半填充的编辑表单。
    createVisible.value = false;
  }
}

/** 校验并提交新增订单，成功后刷新当前商户订单列表。 */
function submitCreateOrder() {
  createFormRef.value.validate((valid) => {
    if (!valid) return;
    const command = { ...createForm };
    try {
      command.accessoryItemIds = serializeExtraAccessories();
    } catch (error) {
      proxy.$modal.msgWarning(error.message);
      return;
    }
    delete command.leasePeriod;
    createLoading.value = true;
    const request = editingOrderId.value
      ? updateMerchantOrder({ ...command, orderId: editingOrderId.value })
      : addMerchantOrder(command);
    request
      .then(() => {
        proxy.$modal.msgSuccess(
          alliancePackageOnly.value ? "套餐设置成功" : "订单保存成功",
        );
        createVisible.value = false;
        loadOrders();
        loadOrderSummary();
      })
      .finally(() => {
        createLoading.value = false;
      });
  });
}

/** 构造页面联调阶段使用的订单数据，接口上线后由服务端数据替换。 */
function createDemoOrders() {
  return [
    createOrder({
      orderNo: "QY202608020001",
      model: "大疆 Osmo Pocket 3",
      packageName: "大疆pt3标准套餐",
      rent: 106,
      customerNickname: "演示客户A",
      customerName: "演示收件人A",
      customerPhone: "13800138000",
      status: RENTAL_CODE.ORDER_STATUS.PENDING_SHIP,
      weatherRisk: true,
      attention: "这里展示管理对这个订单的批注，和系统的提醒。",
    }),
    createOrder({
      orderNo: "QY202608010018",
      model: "大疆 Osmo Pocket 4",
      packageName: "Pocket 4 全能套装",
      rentalPeriod: "2026-08-08 ~ 2026-08-18",
      rent: 168,
      customerNickname: "演示客户B",
      customerName: "演示收件人B",
      customerPhone: "13800138000",
      status: RENTAL_CODE.ORDER_STATUS.SHIPPED,
      shippingMethod: "快递",
      weatherRisk: false,
      attention: "",
    }),
    createOrder({
      orderNo: "QY202607280006",
      model: "大疆 Osmo Action 5",
      packageName: "户外影像基础套餐",
      rentalPeriod: "2026-07-30 ~ 2026-08-05",
      returnDate: "2026-08-05",
      rent: 92,
      channel: "商户自有渠道",
      customerNickname: "演示客户C",
      customerName: "演示收件人C",
      customerPhone: "13900001234",
      status: RENTAL_CODE.ORDER_STATUS.COMPLETED,
      shippingMethod: "自提",
      weatherRisk: false,
      attention: "",
    }),
    createOrder({
      orderNo: "QY202607250003",
      model: "大疆 Osmo Pocket 3",
      packageName: "大疆pt3标准套餐",
      rentalPeriod: "2026-07-26 ~ 2026-08-03",
      returnDate: "2026-08-03",
      rent: 106,
      channel: "租仓联盟",
      customerNickname: "演示客户D",
      customerName: "演示收件人D",
      customerPhone: "13700005678",
      status: RENTAL_CODE.ORDER_STATUS.OVERDUE,
      shippingMethod: "跑腿",
      weatherRisk: true,
      attention: "客户尚未上传归还面单，请跟进。",
    }),
  ];
}

/** 补齐后端订单响应中的可选展示节点，保证页面对历史订单结构保持稳定。 */
function normalizeOrder(order) {
  const weather = order.weather || {};
  const todayWeather = weather.today || {};
  const tomorrowWeather = weather.tomorrow || {};
  const normalized = {
    ...order,
    status: Number(
      order.orderStatus ??
        order.status ??
        RENTAL_CODE.ORDER_STATUS.PENDING_SHIP,
    ),
    customer: {
      nickname: "-",
      name: "-",
      phone: "-",
      address: "-",
      note: "",
      ...(order.customer || {}),
    },
    recipientName:
      order.recipientName || order.customer?.name || order.customerName || "-",
    recipientPhone:
      order.recipientPhone ||
      order.customer?.phone ||
      order.customerPhone ||
      "-",
    recipientAddress:
      order.recipientAddress ||
      order.customerAddress ||
      order.customer?.address ||
      "-",
    shipping: {
      warehouse: "未分配仓库",
      shipDate: "-",
      method: "待安排",
      doorTime: "待安排",
      note: "暂无发货备注",
      ...(order.shipping || {}),
    },
    weather: {
      route: "发货地 → 收货地",
      risk: false,
      ...weather,
      today: {
        depart: todayWeather.depart || weather.depart || "待查询",
        arrive: todayWeather.arrive || weather.arrive || "待查询",
        departIcon: todayWeather.departIcon || weather.departIcon || "cloudy",
        arriveIcon: todayWeather.arriveIcon || weather.arriveIcon || "cloudy",
        ...todayWeather,
      },
      tomorrow: {
        depart: "待查询",
        arrive: "待查询",
        departIcon: "cloudy",
        arriveIcon: "cloudy",
        ...tomorrowWeather,
      },
    },
    delivery: {
      plans: [],
      ...(order.delivery || {}),
      expectedDate: order.delivery?.expectedDate || "待确认",
    },
    addOns: Array.isArray(order.addOns) ? order.addOns : [],
    packageItems: Array.isArray(order.packageItems) ? order.packageItems : [],
    packageDetail: normalizePackageDetail(order.packageDetail, order),
    isAllianceChild: Boolean(order.isAllianceChild),
    isAllianceSourceLocked: Boolean(order.isAllianceSourceLocked),
  };
  normalized.actions = createOrderActions(normalized);
  return normalized;
}

/** 规范化套餐详情并兼容尚未生成详情快照的历史订单。 */
function normalizePackageDetail(detail, order) {
  const source = detail && typeof detail === "object" ? detail : {};
  const sourceMainProduct =
    source.mainProduct && typeof source.mainProduct === "object"
      ? source.mainProduct
      : {};
  const sourceItems = Array.isArray(source.items) ? source.items : [];
  const items = sourceItems.length
    ? sourceItems.map((item) => ({
        itemIndex: item.itemIndex ?? sourceItems.indexOf(item),
        itemType: item.itemType || "套餐明细",
        name: item.name || "-",
        code: item.code || "-",
        model: item.model || "-",
        quantity: Number(item.quantity) > 0 ? Number(item.quantity) : 1,
        requiredText: item.requiredText || "必选",
        selectableDevice:
          item.selectableDevice !== undefined && item.selectableDevice !== null
            ? Boolean(item.selectableDevice)
            : isSerialStockMode(item.stockMode),
        serialNumbers: Array.isArray(item.serialNumbers)
          ? item.serialNumbers.filter(Boolean)
          : [],
        unitIds: Array.isArray(item.unitIds)
          ? item.unitIds.map((unitId) => Number(unitId)).filter(Boolean)
          : [],
      }))
    : (Array.isArray(order.packageItems) ? order.packageItems : []).map(
        (name, index) => ({
          itemIndex: index,
          itemType: index === 0 ? "主商品" : "套餐明细",
          name: name || "-",
          code: index === 0 ? order.productCode || "-" : "-",
          model: index === 0 ? order.model || "-" : "-",
          quantity: 1,
          requiredText: "必选",
          selectableDevice: false,
          serialNumbers: [],
          unitIds: [],
        }),
      );

  return {
    packageName: source.packageName || order.packageName || "未绑定套餐",
    packageCode: source.packageCode || "",
    mainProduct: {
      name: sourceMainProduct.name || order.model || "-",
      code: sourceMainProduct.code || order.productCode || "-",
      model: sourceMainProduct.model || "",
    },
    items,
    extraAccessories: (Array.isArray(source.extraAccessories)
      ? source.extraAccessories
      : order.addOns || []
    )
      .map(normalizeExtraAccessoryLabel)
      .filter(Boolean),
  };
}

function isSerialStockMode(value) {
  const text = String(value ?? "")
    .trim()
    .toLowerCase();
  return ["1", "sn", "serial", "逐件", "逐件库存"].includes(text);
}

function packageProductLabel(row) {
  const name = row?.name || "-";
  const model = row?.model && row.model !== name ? `（${row.model}）` : "";
  return `${name}`;
}

function normalizeExtraAccessoryLabel(item) {
  if (typeof item === "string") return item;
  if (!item || typeof item !== "object") return "";
  const name = item.itemName || item.name || item.productName || "额外配件";
  const quantity = Number(item.quantity) > 0 ? Number(item.quantity) : 1;
  return `${name} x${quantity}`;
}

function parseExtraAccessoryLabel(label) {
  const text = String(label || "").trim();
  const match = text.match(/^(.*)\s+x(\d+)$/);
  if (!match) return { name: text || "额外配件", quantity: 1 };
  return {
    name: match[1].trim() || "额外配件",
    quantity: Number(match[2]) || 1,
  };
}

/** 将订单状态数值编码转换为中文展示。 */
function orderStatusLabel(status) {
  return (
    {
      [RENTAL_CODE.ORDER_STATUS.PENDING_SHIP]: "待发货",
      [RENTAL_CODE.ORDER_STATUS.PREPARED]: "已备货",
      [RENTAL_CODE.ORDER_STATUS.SHIPPING]: "运输中",
      [RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT]: "运输中",
      [RENTAL_CODE.ORDER_STATUS.SHIPPED]: "已发货",
      [RENTAL_CODE.ORDER_STATUS.RENTING]: "租用中",
      [RENTAL_CODE.ORDER_STATUS.RETURNING]: "归还中",
      [RENTAL_CODE.ORDER_STATUS.COMPLETED]: "已完成",
      [RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN]: "归还逾期",
      [RENTAL_CODE.ORDER_STATUS.OVERDUE]: "已逾期",
      [RENTAL_CODE.ORDER_STATUS.AFTER_SALES]: "售后中",
      [RENTAL_CODE.ORDER_STATUS.CANCELLED]: "已取消",
    }[Number(status)] || "-"
  );
}

/** 返回订单状态对应的 Element Plus 标签颜色。 */
function orderStatusType(status) {
  const code = Number(status);
  if (
    [
      RENTAL_CODE.ORDER_STATUS.PENDING_SHIP,
      RENTAL_CODE.ORDER_STATUS.RETURNING,
    ].includes(code)
  )
    return "warning";
  if (
    [
      RENTAL_CODE.ORDER_STATUS.SHIPPING,
      RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT,
      RENTAL_CODE.ORDER_STATUS.SHIPPED,
      RENTAL_CODE.ORDER_STATUS.PREPARED,
    ].includes(code)
  )
    return "primary";
  if (
    [
      RENTAL_CODE.ORDER_STATUS.COMPLETED,
      RENTAL_CODE.ORDER_STATUS.RENTING,
    ].includes(code)
  )
    return "success";
  if (
    [
      RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN,
      RENTAL_CODE.ORDER_STATUS.OVERDUE,
      RENTAL_CODE.ORDER_STATUS.AFTER_SALES,
    ].includes(code)
  )
    return "danger";
  return "info";
}

/** 将订单基础参数补齐为页面统一消费的数据结构。 */
function createOrder(options) {
  const shipDate = options.shipDate || "2026-08-02";
  return {
    orderNo: options.orderNo,
    model: options.model,
    packageName: options.packageName,
    rentalPeriod: options.rentalPeriod || "2026-08-11 ~ 2026-08-31",
    returnDate: options.returnDate || "2026-08-11",
    rent: options.rent || 106,
    channel: options.channel || "人人租",
    merchant: options.merchant || "示例商户",
    customer: {
      nickname: options.customerNickname,
      name: options.customerName,
      phone: options.customerPhone,
      address:
        options.recipientAddress ||
        options.customerAddress ||
        "示例收货地址",
      note: options.customerNote || "请保持设备整洁，配件齐全",
    },
    recipientName: options.recipientName || options.customerName,
    recipientPhone: options.recipientPhone || options.customerPhone,
    recipientAddress: options.recipientAddress || options.customerAddress || "",
    shipping: {
      warehouse: options.warehouse || "杭州仓",
      shipDate,
      method: options.shippingMethod || "快递 / 自提 / 跑腿",
      doorTime: options.doorTime || "XXXX点上门",
      note: options.shippingNote || "加一个保护套，清洗干净",
    },
    weather: {
      route: options.route || "杭州 → 北京",
      depart: options.weatherDepart || "今日晴",
      arrive: options.weatherArrive || "明日雨",
      departIcon: "sunny",
      arriveIcon: "rain",
      risk: options.weatherRisk,
    },
    delivery: {
      plans: [
        { carrier: "SF", name: "标快", price: 11 },
        { carrier: "SF", name: "陆运", price: 11 },
      ],
      expectedDate: options.expectedDate || "2026-08-30",
    },
    status: options.status,
    trackingNo: options.trackingNo || "SF123456778909",
    returnNo: options.returnNo || "JD128990988888",
    createdAt: options.createdAt || "2026-08-02 10:20:18",
    productCode: options.productCode || "ANGZNCQ002FAWF",
    addOns: options.addOns || ["下拉选配件", "SN码"],
    attention: options.attention,
    operator: options.operator || "示例操作员",
    coordinator: options.coordinator || "示例协调员",
    actions: createOrderActions(),
  };
}

const ORDER_STATUS_GROUPS = Object.freeze({
  PRE_SHIPMENT: new Set([
    RENTAL_CODE.ORDER_STATUS.PENDING_SHIP,
    RENTAL_CODE.ORDER_STATUS.OVERDUE,
  ]),
  PREPARED: new Set([RENTAL_CODE.ORDER_STATUS.PREPARED]),
  SHIPPING: new Set([
    RENTAL_CODE.ORDER_STATUS.SHIPPING,
    RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT,
    RENTAL_CODE.ORDER_STATUS.SHIPPED,
  ]),
  RENTING: new Set([RENTAL_CODE.ORDER_STATUS.RENTING]),
  RETURNING: new Set([
    RENTAL_CODE.ORDER_STATUS.RETURNING,
    RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN,
  ]),
  RETURNABLE: new Set([
    RENTAL_CODE.ORDER_STATUS.SHIPPING,
    RENTAL_CODE.ORDER_STATUS.SHIP_IN_TRANSIT,
    RENTAL_CODE.ORDER_STATUS.SHIPPED,
    RENTAL_CODE.ORDER_STATUS.RENTING,
    RENTAL_CODE.ORDER_STATUS.OVERDUE,
    RENTAL_CODE.ORDER_STATUS.OVERDUE_RETURN,
  ]),
  AFTER_SALES: new Set([RENTAL_CODE.ORDER_STATUS.AFTER_SALES]),
});

const ORDER_ACTION_STATUS = Object.freeze({
  编辑: ORDER_STATUS_GROUPS.PRE_SHIPMENT,
  设置套餐: ORDER_STATUS_GROUPS.PRE_SHIPMENT,
  转单: ORDER_STATUS_GROUPS.PRE_SHIPMENT,
  备货: ORDER_STATUS_GROUPS.PRE_SHIPMENT,
  取消: new Set([
    ...ORDER_STATUS_GROUPS.PRE_SHIPMENT,
    ...ORDER_STATUS_GROUPS.PREPARED,
  ]),
  上传视频: ORDER_STATUS_GROUPS.PREPARED,
  发货: ORDER_STATUS_GROUPS.PREPARED,
  登记归还: ORDER_STATUS_GROUPS.RETURNABLE,
  转寄: ORDER_STATUS_GROUPS.SHIPPING,
  跨仓归还: new Set([
    ...ORDER_STATUS_GROUPS.SHIPPING,
    ...ORDER_STATUS_GROUPS.RENTING,
    ...ORDER_STATUS_GROUPS.RETURNING,
  ]),
  归还验机: ORDER_STATUS_GROUPS.RETURNING,
  售后: new Set([
    ...ORDER_STATUS_GROUPS.RENTING,
    ...ORDER_STATUS_GROUPS.RETURNING,
  ]),
  续租: new Set([
    ...ORDER_STATUS_GROUPS.RENTING,
    ...ORDER_STATUS_GROUPS.RETURNING,
  ]),
  完成: new Set([
    ...ORDER_STATUS_GROUPS.RETURNING,
    ...ORDER_STATUS_GROUPS.AFTER_SALES,
  ]),
});

const ORDER_ACTION_REASON = Object.freeze({
  编辑: "只有待发货或发货逾期订单可以编辑",
  设置套餐: "只有待发货或发货逾期订单可以设置套餐",
  转单: "只有待发货或发货逾期订单可以转单",
  备货: "只有待发货或发货逾期订单可以备货",
  取消: "只有未发货订单可以取消",
  上传视频: "只有已备货订单可以上传视频",
  发货: "只有已备货订单可以发货",
  登记归还: "只有已发货或租用中的订单可以登记归还",
  转寄: "只有运输中的订单可以转寄",
  跨仓归还: "当前订单阶段不支持跨仓归还",
  归还验机: "只有归还中的订单可以验机",
  售后: "只有租用中或归还中的订单可以创建售后单",
  续租: "只有租用中、归还中或归还逾期订单可以续租",
  完成: "当前订单尚未进入可完结阶段",
});

/** 根据订单当前状态给底部按钮附加禁用状态，不改变订单对象的其他字段。 */
function withOrderActionState(action, order) {
  const allowedStatuses = ORDER_ACTION_STATUS[action.label];
  if (!allowedStatuses) return action;
  if (order?.isAllianceSourceLocked) {
    return {
      ...action,
      disabled: true,
      disabledReason: "原订单已有已接受的商户联盟订单，仅允许查看",
    };
  }
  const status = Number(order?.status);
  if (allowedStatuses.has(status)) return action;
  return {
    ...action,
    disabled: true,
    disabledReason:
      ORDER_ACTION_REASON[action.label] || "当前订单状态不支持此操作",
  };
}

/** 创建订单行底部的标准操作集合，并按订单状态机禁用不适用动作。 */
function createOrderActions(order = null) {
  let actions;
  if (order?.isAllianceChild) {
    actions = [
      { label: "设置套餐", icon: Edit },
      { label: "复制", icon: CopyDocument },
      { label: "批注", icon: ChatDotRound },
      { label: "备货", icon: Box },
      { label: "取消", icon: CircleClose },
      { label: "上传视频", icon: VideoCamera },
      { label: "发货", icon: Van, type: "success" },
      { label: "登记归还", icon: Finished },
      { label: "转寄", icon: Switch },
      { label: "跨仓归还", icon: Location },
      { label: "归还验机", icon: Check },
      { label: "售后", icon: CircleClose, type: "warning" },
      { label: "续租", icon: Finished },
      { label: "完成", icon: CircleCheck },
      { label: "编辑记录", icon: Edit },
    ];
  } else if (order?.isAllianceSourceLocked) {
    actions = [
      { label: "复制", icon: CopyDocument },
      { label: "批注", icon: ChatDotRound },
      { label: "备货", icon: Box },
      { label: "取消", icon: CircleClose },
      { label: "上传视频", icon: VideoCamera },
      { label: "发货", icon: Van, type: "success" },
      { label: "登记归还", icon: Finished },
      { label: "转寄", icon: Switch },
      { label: "跨仓归还", icon: Location },
      { label: "归还验机", icon: Check },
      { label: "售后", icon: CircleClose, type: "warning" },
      { label: "续租", icon: Finished },
      { label: "完成", icon: CircleCheck },
      { label: "编辑记录", icon: Edit },
    ];
  } else {
    actions = [
      { label: "编辑", icon: Edit },
      { label: "复制", icon: CopyDocument },
      { label: "转单", icon: Switch },
      { label: "批注", icon: ChatDotRound },
      { label: "备货", icon: Box },
      { label: "取消", icon: CircleClose },
      { label: "上传视频", icon: VideoCamera },
      { label: "发货", icon: Van, type: "success" },
      { label: "登记归还", icon: Finished },
      { label: "转寄", icon: Switch },
      { label: "跨仓归还", icon: Location },
      { label: "归还验机", icon: Check },
      { label: "售后", icon: CircleClose, type: "warning" },
      { label: "续租", icon: Finished },
      { label: "完成", icon: CircleCheck },
      { label: "编辑记录", icon: Edit },
    ];
  }
  return actions.map((action) => withOrderActionState(action, order));
}

queryParams.orderScope = normalizeOrderScope(route.query.orderScope);
loadOrders();
watch(
  () => route.query.orderScope,
  (value) => {
    const nextScope = normalizeOrderScope(value);
    if (queryParams.orderScope === nextScope) return;
    queryParams.orderScope = nextScope;
    queryParams.pageNum = 1;
    loadOrders();
  },
);
watch(
  () => route.query.orderId,
  (value) => {
    const rawOrderId = Array.isArray(value) ? value[0] : value;
    const orderId = Number(rawOrderId);
    if (!Number.isSafeInteger(orderId) || orderId <= 0) return;
    const cachedOrder = orders.value.find(
      (item) => Number(item.orderId) === orderId,
    );
    openOrderDetail(cachedOrder || { orderId });
  },
  { immediate: true },
);
loadOrderSummary();
loadOrderCopySettings();
loadOrderOptions();
</script>

<style scoped>
.merchant-order-page {
  min-width: 0;
  padding: 20px 24px 32px;
  color: var(--el-text-color-primary);
}

.batch-media-upload {
  margin-top: 16px;
}

.batch-media-table {
  margin-top: 12px;
}

.text-success {
  color: var(--el-color-success);
}

.page-heading,
.list-toolbar,
.order-footer,
.heading-actions,
.list-toolbar-actions,
.compact-action-bar,
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
.list-toolbar-actions,
.compact-action-bar {
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
  content: "";
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

.summary-trend.warning {
  color: var(--el-color-warning);
}
.summary-trend.primary {
  color: var(--el-color-primary);
}
.summary-trend.danger {
  color: var(--el-color-danger);
}
.summary-trend.neutral {
  color: var(--el-text-color-placeholder);
}

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

.keyword-input {
  width: 270px;
}
.filter-select {
  width: 150px;
}
.date-range-input {
  width: 260px;
}
.filter-actions {
  margin-right: 0 !important;
}

.order-list-shell {
  min-width: 0;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  background: var(--el-bg-color);
}

.list-toolbar {
  justify-content: space-between;
  gap: 16px;
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

.order-scope-tag {
  margin-left: 8px;
  vertical-align: middle;
}

.list-toolbar-actions {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.order-view-switch :deep(.el-radio-button__inner) {
  padding: 7px 12px;
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

.order-card:last-child {
  border-bottom: 0;
}

.order-grid-content {
  align-items: stretch;
  padding: 16px;
}

.order-cell {
  min-width: 0;
  padding: 0 16px;
  border-right: 1px solid var(--el-border-color-lighter);
}

.order-cell:first-child {
  padding-left: 0;
}
.order-cell:last-child {
  padding-right: 0;
  border-right: 0;
}

.mobile-cell-title {
  display: none;
  margin-bottom: 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 650;
}

.product-summary {
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
}
.product-summary dt,
.product-summary dd {
  margin: 0;
}

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

.product-thumb :deep(.el-icon) {
  font-size: 25px;
}
.product-thumb > svg {
  width: 25px;
  height: 25px;
}
.product-summary strong,
.product-summary span {
  display: block;
  line-height: 1.5;
}
.product-summary p {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
  margin: 0 0 4px;
  white-space: nowrap;
}
.product-summary p :deep(.el-tag) {
  display: inline-flex;
  flex: 0 0 auto;
  width: auto;
}
.product-summary p strong {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-summary strong {
  font-size: 14px;
}
.product-summary span {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.detail-list {
  margin: 0;
  font-size: 12px;
  line-height: 1.55;
}
.detail-list > div {
  display: grid;
  grid-template-columns: 5em minmax(0, 1fr);
  gap: 5px;
  margin-bottom: 5px;
}
.detail-list dt {
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
.detail-list dd {
  min-width: 0;
  margin: 0;
  overflow-wrap: anywhere;
}
.detail-list em {
  display: inline;
  margin-left: 8px;
  color: var(--el-text-color-secondary);
  font-style: normal;
  white-space: nowrap;
}
.money {
  color: var(--el-color-danger);
  font-weight: 650;
}

.customer-name {
  gap: 5px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 650;
}
.compact-recipient-name {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.compact-recipient-name .el-button {
  padding: 0;
}
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
.route-line {
  gap: 5px;
  margin-bottom: 11px;
  color: var(--el-text-color-primary);
  font-size: 13px;
  font-weight: 650;
}
.route-line :deep(.el-icon) {
  color: var(--el-color-danger);
}
.route-line > svg {
  flex: 0 0 14px;
  width: 14px;
  height: 14px;
  color: var(--el-color-danger);
}
.weather-line {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 6px;
  margin-bottom: 7px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.weather-line > .weather-label {
  width: auto;
  color: var(--el-text-color-primary);
}
.weather-summary {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: flex-start;
}
.weather-summary-item {
  display: flex;
  min-width: 0;
  height: 16px;
  align-items: center;
  gap: 3px;
  line-height: 16px;
}
.weather-period {
  white-space: nowrap;
  line-height: 16px;
}
.weather-divider {
  display: inline-flex;
  flex: 0 0 4px;
  /* width: 16px; */
  height: 16px;
  margin: 0 6px;
  align-items: center;
  justify-content: center;
  color: var(--el-border-color);
  line-height: 16px;
  text-align: center;
}
.weather-line i {
  display: inline-flex;
  flex: 0 0 16px;
  width: 16px;
  height: 16px;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-style: normal;
  line-height: 16px;
  text-align: center;
}
.weather-line i.sunny {
  color: #f59e0b;
}
.weather-line i.rain {
  color: #5b7cba;
}
.weather-cell :deep(.el-tag) {
  margin-top: 5px;
}

.delivery-plan {
  gap: 6px;
  margin-bottom: 9px;
  font-size: 12px;
}
.carrier-mark {
  display: inline-grid;
  place-items: center;
  width: 23px;
  height: 23px;
  border: 1px solid #d5ad45;
  border-radius: 50%;
  color: #906e18;
  font-size: 10px;
  font-weight: 700;
}
.delivery-plan b {
  margin-left: auto;
  color: var(--el-text-color-primary);
  font-weight: 500;
}
.delivery-cell :deep(.el-button) {
  padding-left: 0;
}
.delivery-after {
  margin-top: 7px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.status-tag {
  margin-bottom: 12px;
}
.status-cell .detail-list > div {
  grid-template-columns: 5em minmax(0, 1fr);
}
.status-cell .detail-list dd {
  white-space: nowrap;
}
.logistics-value,
.compact-logistics-value {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
.logistics-value :deep(.el-link),
.compact-logistics-value :deep(.el-link) {
  font-size: inherit;
  line-height: inherit;
}
.logistics-value :deep(.el-button),
.compact-logistics-value :deep(.el-button) {
  padding: 0 2px;
}
.product-list-cell strong {
  display: block;
  line-height: 1.5;
}
.product-list-cell p {
  margin: 8px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
}
.product-list-cell strong :deep(.el-button) {
  padding: 0;
  font-size: 12px;
}
.addon-title {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--el-text-color-primary) !important;
  font-weight: 650;
}
.addon-title > svg {
  flex: 0 0 14px;
  width: 14px;
  height: 14px;
}
.addon-item {
  padding-left: 18px;
}
.full-width {
  width: 100%;
}

.package-detail-section {
  margin-top: 18px;
}

.package-detail-section h4 {
  margin: 0 0 10px;
  color: var(--el-text-color-primary);
  font-size: 15px;
  font-weight: 650;
}

.package-detail-section:first-of-type {
  margin-top: 16px;
}

.order-number-line {
  display: block;
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
}

.order-number-line :deep(.el-link) {
  font-size: inherit;
  vertical-align: baseline;
}

.package-device-empty {
  color: var(--el-text-color-placeholder);
}

.package-device-value {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  min-height: 24px;
  line-height: 1.5;
}

.package-device-link {
  margin-left: 0;
  position: relative;
  line-height: 1.5;
}

.create-order-dialog :deep(.el-dialog__body) {
  padding: 8px 28px 4px;
}

.create-order-section {
  padding: 12px 0 2px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.create-order-section-last {
  border-bottom: 0;
}

.create-order-section h3 {
  margin: 0 0 14px;
  color: var(--el-text-color-primary);
  font-size: 15px;
  font-weight: 650;
}

.create-order-section :deep(.el-form-item) {
  margin-bottom: 18px;
}

.create-order-dialog :deep(.el-dialog__footer) {
  padding: 14px 28px 22px;
}

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

.attention-note {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 260px;
  color: var(--el-text-color-primary);
  font-size: 12px;
}
.attention-note :deep(.el-icon) {
  color: var(--el-color-warning);
}
.attention-note > svg {
  flex: 0 0 14px;
  width: 14px;
  height: 14px;
  color: var(--el-color-warning);
}
.attention-note span {
  color: var(--el-text-color-secondary);
}
.attention-note.muted {
  color: var(--el-text-color-placeholder);
}
.attention-link {
  min-width: 0;
  max-width: min(52vw, 720px);
  overflow: hidden;
  font-size: inherit;
  text-align: left;
}
.attention-link :deep(.el-link__inner) {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.annotation-history {
  max-height: 620px;
  min-height: 120px;
  overflow-y: auto;
  padding: 2px 8px 2px 0;
}
.annotation-history-list {
  display: grid;
  gap: 16px;
}
.annotation-record {
  padding: 22px 28px;
  border-radius: 8px;
  background: var(--el-fill-color-lighter);
}
.annotation-record-type {
  margin-bottom: 12px;
  color: var(--el-color-primary);
  font-size: 14px;
  font-weight: 600;
}
.annotation-record-content {
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  color: var(--el-text-color-primary);
  font-size: 16px;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
.annotation-record-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-top: 14px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
.annotation-record-meta time {
  color: var(--el-text-color-placeholder);
}
.annotation-dialog :deep(.el-dialog__body) {
  padding: 10px 24px 24px;
}
.edit-log-history {
  max-height: 620px;
  min-height: 120px;
  overflow-y: auto;
  padding: 2px 8px 2px 0;
}
.edit-log-list {
  display: grid;
  gap: 12px;
}
.edit-log-record {
  padding: 16px 20px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-lighter);
}
.edit-log-heading,
.edit-log-values,
.edit-log-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}
.edit-log-heading {
  justify-content: space-between;
}
.edit-log-heading strong {
  color: var(--el-text-color-primary);
  font-size: 15px;
}
.edit-log-heading time,
.edit-log-meta {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.edit-log-values {
  align-items: flex-start;
  margin-top: 12px;
  padding: 12px 0;
  border-top: 1px solid var(--el-border-color-lighter);
  border-bottom: 1px solid var(--el-border-color-lighter);
  line-height: 1.5;
}
.edit-log-before,
.edit-log-after {
  min-width: 0;
  overflow-wrap: anywhere;
}
.edit-log-before {
  color: var(--el-color-danger);
}
.edit-log-after {
  color: var(--el-color-success);
}
.edit-log-arrow {
  color: var(--el-text-color-placeholder);
  flex: 0 0 auto;
}
.edit-log-meta {
  margin-top: 10px;
}
.edit-log-dialog :deep(.el-dialog__body) {
  padding: 10px 24px 24px;
}
.order-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 2px;
}
.order-actions :deep(.el-button) {
  margin-left: 0;
  padding: 4px 6px;
  font-size: 12px;
}
.operator-meta {
  margin-left: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  white-space: nowrap;
}

.compact-order-table-wrap {
  overflow-x: auto;
}

.compact-order-table {
  width: 100%;
  min-width: 2610px;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 12px;
}

.compact-order-table th,
.compact-order-table td {
  padding: 12px 10px;
  border-bottom: 1px solid var(--el-border-color-light);
  text-align: left;
  vertical-align: top;
  overflow-wrap: anywhere;
}

.compact-order-table th {
  background: var(--el-fill-color-lighter);
  color: var(--el-text-color-secondary);
  font-size: 13px;
  font-weight: 650;
  white-space: nowrap;
}

.compact-order-table tbody tr.compact-order-row:hover td {
  background: var(--el-fill-color-light);
}

.compact-order-table tbody tr:last-child td {
  border-bottom: 0;
}

.compact-order-row > td {
  height: 92px;
}

.compact-col-recipient {
  width: 150px;
}
.compact-col-phone {
  width: 130px;
}
.compact-col-channel {
  width: 140px;
}
.compact-col-period {
  width: 180px;
}
.compact-col-status {
  width: 110px;
}
.compact-col-shipping-tracking,
.compact-col-return-tracking {
  width: 150px;
}
.compact-col-shipping {
  width: 175px;
}
.compact-col-warehouse {
  width: 115px;
}
.compact-col-product {
  width: 190px;
}
.compact-col-package-items {
  width: 260px;
}
.compact-col-addon {
  width: 220px;
}
.compact-col-return {
  width: 145px;
}
.compact-col-receipt {
  width: 365px;
}
.compact-col-actions {
  width: 130px;
}

.compact-primary,
.compact-secondary,
.compact-multiline {
  display: block;
  line-height: 1.55;
}

.compact-primary {
  color: var(--el-text-color-primary);
  font-weight: 600;
}
.compact-secondary {
  color: var(--el-text-color-secondary);
}
.compact-multiline {
  white-space: normal;
}
.compact-operation-cell {
  white-space: nowrap;
}
.compact-operation-cell :deep(.el-button) {
  padding: 0;
}

.compact-action-row td {
  padding: 8px 10px;
  background: var(--el-fill-color-blank);
}

.compact-action-bar {
  min-height: 32px;
  justify-content: space-between;
}

.compact-action-bar .attention-note {
  min-width: 260px;
}

@media (max-width: 1100px) {
  .page-heading {
    align-items: flex-start;
    flex-direction: column;
  }
  .heading-actions {
    width: 100%;
    justify-content: flex-end;
  }
  .filter-panel :deep(.el-form) {
    display: flex;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .merchant-order-page {
    padding: 16px 12px 24px;
  }
  .summary-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .filter-panel {
    padding: 14px 14px 2px;
  }
  .filter-panel :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }
  .keyword-input,
  .filter-select,
  .date-range-input {
    width: 100%;
  }
  .filter-actions {
    display: flex;
    justify-content: flex-end;
  }
  .list-toolbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
    padding: 14px;
  }
  .list-toolbar-actions {
    width: 100%;
    justify-content: space-between;
  }
  .order-view-switch {
    display: flex;
    flex: 1;
  }
  .order-view-switch :deep(.el-radio-button) {
    flex: 1;
  }
  .order-view-switch :deep(.el-radio-button__inner) {
    width: 100%;
  }
  .order-scroll-area {
    overflow-x: visible;
  }
  .order-grid {
    display: block;
    min-width: 0;
  }
  .order-card,
  .order-footer {
    min-width: 0;
  }
  .order-grid-header {
    display: none;
  }
  .order-grid-content {
    padding: 0;
  }
  .order-cell {
    padding: 16px;
    border-right: 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }
  .order-cell:first-child {
    padding-left: 16px;
  }
  .order-cell:last-child {
    padding-right: 16px;
    border-bottom: 0;
  }
  .mobile-cell-title {
    display: block;
  }
  .order-footer {
    align-items: flex-start;
    flex-direction: column;
    padding: 12px 16px;
  }
  .attention-note {
    min-width: 0;
  }
  .attention-link {
    max-width: 100%;
  }
  .order-actions {
    justify-content: flex-start;
  }
  .operator-meta {
    margin: 6px 0 0;
    white-space: normal;
  }
  .compact-order-table {
    min-width: 2610px;
  }
  .compact-action-bar {
    align-items: flex-start;
    flex-direction: column;
  }
  .compact-action-bar .attention-note {
    min-width: 0;
  }
}

@media (max-width: 768px) {
  .annotation-dialog :deep(.el-dialog) {
    width: calc(100% - 24px) !important;
    margin-top: 8vh;
  }
  .annotation-dialog :deep(.el-dialog__body) {
    padding: 8px 14px 16px;
  }
  .annotation-history {
    max-height: 65vh;
    padding-right: 2px;
  }
  .annotation-record {
    padding: 16px;
  }
  .annotation-record-content {
    font-size: 14px;
  }
  .annotation-record-meta {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }
  .edit-log-dialog :deep(.el-dialog) {
    width: calc(100% - 24px) !important;
    margin-top: 8vh;
  }
  .edit-log-dialog :deep(.el-dialog__body) {
    padding: 8px 14px 16px;
  }
  .create-order-dialog :deep(.el-dialog) {
    width: calc(100% - 24px) !important;
    margin-top: 6vh;
  }
  .create-order-dialog :deep(.el-dialog__body) {
    padding: 8px 14px 4px;
  }
  .create-order-dialog :deep(.el-col) {
    width: 100%;
    max-width: 100%;
    flex: 0 0 100%;
  }
  .create-order-dialog :deep(.el-dialog__footer) {
    padding: 12px 14px 18px;
  }
  .edit-log-history {
    max-height: 65vh;
    padding-right: 2px;
  }
  .edit-log-record {
    padding: 14px;
  }
  .edit-log-heading {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }
  .edit-log-values {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }
  .edit-log-arrow {
    transform: rotate(90deg);
  }
  .edit-log-meta {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }
}
</style>
