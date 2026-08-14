<template>
  <div class="merchant-dashboard">
    <section class="dashboard-toolbar">
      <div class="toolbar-title">
        <h1>商户工作台</h1>
      </div>
      <div class="toolbar-filters">
        <el-select v-model="filters.merchant" placeholder="商户" class="filter-control" disabled>
          <el-option label="当前商户" value="current" />
        </el-select>
        <el-select v-model="filters.warehouse" placeholder="仓库：全部" class="filter-control" disabled>
          <el-option label="全部仓库" value="all" />
        </el-select>
        <el-button type="primary" :icon="Search" disabled>查询</el-button>
        <el-button :icon="Refresh" disabled>重置</el-button>
      </div>
    </section>

    <section class="dashboard-section">
      <SectionTitle title="今日订单报表" :icon="DataAnalysis" />
      <div class="today-grid">
        <MetricCard v-for="item in todayMetrics" :key="item.label" v-bind="item" />
      </div>
    </section>

    <section class="dashboard-section">
      <SectionTitle title="汇总报表" :icon="Document" />
      <div class="summary-grid">
        <MetricCard v-for="item in summaryMetrics" :key="item.label" v-bind="item" compact />
      </div>
    </section>

    <section class="dashboard-section">
      <SectionTitle title="营收分析" :icon="Money" />
      <div class="revenue-grid">
        <MetricCard v-for="item in revenueMetrics" :key="item.label" v-bind="item" compact />
      </div>
    </section>

    <section class="dashboard-section inventory-section">
      <SectionTitle title="在库商品营收报表" :icon="Box" />
      <el-table :data="[]" class="inventory-table" height="220">
        <el-table-column v-for="column in inventoryColumns" :key="column" :label="column" min-width="150" />
        <template #empty>
          <el-empty description="暂无报表数据" :image-size="72" />
        </template>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { Box, DataAnalysis, Document, Money, Refresh, Search } from '@element-plus/icons-vue'

const filters = reactive({ merchant: 'current', warehouse: 'all' })

const todayMetrics = [
  { label: '今日待发', value: '--', tone: 'primary' },
  { label: '今日待归还', value: '--', tone: 'lavender' },
  { label: '今日新增', value: '--', tone: 'mint' },
  { label: '今日预计到库', value: '--', tone: 'lavender', compact: true },
  { label: '今日售后', value: '--', tone: 'rose', compact: true },
  { label: '今日签收', value: '--', tone: 'lavender', compact: true },
  { label: '今日完结', value: '--', tone: 'mint', compact: true },
  { label: '晚到签收', value: '--', tone: 'danger' }
]

const summaryMetrics = [
  { label: '明日待寄', value: '--', tone: 'lavender', compact: true },
  { label: '待寄总数', value: '--', tone: 'lavender', compact: true },
  { label: '本月订单数（总数）', value: '--', tone: 'neutral', emphasis: true },
  { label: '已完结订单（总数）', value: '--', tone: 'neutral', compact: true },
  { label: '未完结订单（总数）', value: '--', tone: 'neutral', compact: true },
  { label: '主商品总数', value: '--', tone: 'neutral', compact: true },
  { label: '配件总数', value: '--', tone: 'neutral', compact: true },
  { label: '空闲商品总数', value: '--', tone: 'mint', compact: true },
  { label: '空闲配件总数', value: '--', tone: 'mint', compact: true },
  { label: '归还逾期', value: '--', tone: 'rose', compact: true },
  { label: '已逾期', value: '--', tone: 'rose', compact: true }
]

const revenueMetrics = [
  { label: '今日战绩（成交租金）', value: '--', tone: 'orange', emphasis: true },
  { label: '总收益', value: '--', tone: 'cream', compact: true },
  { label: '联盟收入', value: '--', tone: 'cream', compact: true },
  { label: '设备配件总成本', value: '--', tone: 'cream', compact: true },
  { label: '昨日战绩（成交租金）', value: '--', tone: 'cream', compact: true },
  { label: '本月战绩（实收）', value: '--', tone: 'cream', compact: true },
  { label: '上月战绩（实收）', value: '--', tone: 'cream', compact: true }
]

const inventoryColumns = ['品牌', '套餐/型号', '剩余数量', '出租次数', '月均出租台次', '月均出租次数', '租金总收入', '月均租金收入', '维修次数', '预计年投入产出比']

/** 渲染看板区域标题，统一左侧标记和图标样式。 */
const SectionTitle = (props, { slots }) => h('div', { class: 'section-title' }, [
  h('span', { class: 'section-mark' }),
  h(resolveComponent('el-icon'), { class: 'section-icon' }, { default: () => h(props.icon) }),
  h('h2', null, props.title),
  slots.default ? slots.default() : null
])

/** 渲染单个指标卡片，统一处理大数字和紧凑数字两种展示密度。 */
const MetricCard = (props) => h('div', {
  class: ['metric-card', `tone-${props.tone}`, { compact: props.compact, emphasis: props.emphasis }]
}, [h('span', { class: 'metric-label' }, props.label), h('strong', null, props.value)])
</script>

<style scoped>
.merchant-dashboard { min-height: 100%; padding: 24px 28px 40px; background: #f7f8fc; color: #263248; }
.dashboard-toolbar { display: flex; align-items: end; justify-content: space-between; gap: 20px; margin-bottom: 22px; }
.toolbar-title h1 { margin: 2px 0 0; font-size: 26px; font-weight: 700; }
.eyebrow { margin: 0; color: #6b7280; font-size: 13px; }
.toolbar-filters { display: flex; align-items: center; gap: 10px; }
.filter-control { width: 150px; }
.dashboard-section { margin-top: 22px; }
.section-title { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.section-title h2 { margin: 0; font-size: 16px; font-weight: 650; }
.section-mark { width: 4px; height: 17px; background: #4f46e5; }
.section-icon { color: #4f46e5; }
.today-grid, .summary-grid, .revenue-grid { display: grid; gap: 12px; }
.today-grid { grid-template-columns: 1.1fr 1.1fr 1.1fr repeat(4, 1fr) 1.1fr; }
.summary-grid { grid-template-columns: repeat(6, 1fr); }
.revenue-grid { grid-template-columns: 1.5fr repeat(3, 1fr); }
.metric-card { display: flex; min-height: 108px; flex-direction: column; justify-content: space-between; padding: 18px; border-radius: 7px; background: #fff; box-shadow: 0 3px 12px rgba(38, 50, 72, .05); }
.metric-card.compact { min-height: 70px; padding: 13px 16px; }
.metric-card.emphasis { min-height: 118px; }
.metric-label { font-size: 14px; color: #52627a; }
.metric-card strong { font-size: 34px; line-height: 1; color: #3747d8; font-weight: 700; }
.metric-card.compact strong { font-size: 22px; }
.tone-primary { background: linear-gradient(135deg, #4f46e5, #746df4); color: #fff; }
.tone-primary .metric-label, .tone-primary strong, .tone-danger .metric-label, .tone-danger strong { color: #fff; }
.tone-lavender { background: #f0edff; }
.tone-mint { background: #e2f8ed; }
.tone-rose { background: #fff0f0; }
.tone-danger { background: linear-gradient(135deg, #ff5b60, #fa7d81); }
.tone-orange { background: linear-gradient(135deg, #ff702c, #ffad58); color: #fff; }
.tone-orange .metric-label, .tone-orange strong { color: #fff; }
.tone-cream { background: #fff8e6; }
.tone-neutral { background: #f0f3f7; }
.inventory-table { width: 100%; background: #fff; border-radius: 7px; }
@media (max-width: 1200px) { .today-grid { grid-template-columns: repeat(4, 1fr); } .summary-grid { grid-template-columns: repeat(4, 1fr); } .revenue-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .merchant-dashboard { padding: 16px; } .dashboard-toolbar { align-items: stretch; flex-direction: column; } .toolbar-filters { flex-wrap: wrap; } .today-grid, .summary-grid, .revenue-grid { grid-template-columns: repeat(2, 1fr); } .filter-control { flex: 1; min-width: 130px; } }
</style>
