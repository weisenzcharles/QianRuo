<template>
  <div class="trend-line-chart" :style="{ height }">
    <div ref="chartRef" class="trend-line-chart__canvas" />
    <el-empty v-if="!hasData" :description="emptyText" :image-size="64" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import * as echarts from "echarts/core";
import { LineChart } from "echarts/charts";
import {
  GridComponent,
  LegendComponent,
  TooltipComponent,
} from "echarts/components";
import { CanvasRenderer } from "echarts/renderers";

echarts.use([
  LineChart,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  CanvasRenderer,
]);

const COLORS = [
  "#409eff",
  "#f56c6c",
  "#67c23a",
  "#e6a23c",
  "#8e6de7",
  "#13c2c2",
  "#ff8c42",
  "#5b6bff",
];

const props = defineProps({
  categories: { type: Array, default: () => [] },
  series: { type: Array, default: () => [] },
  height: { type: String, default: "320px" },
  yAxisName: { type: String, default: "数量" },
  emptyText: { type: String, default: "暂无图表数据" },
});

const chartRef = ref(null);
let chartInstance;

const hasData = computed(
  () => props.categories.length > 0 && props.series.length > 0,
);

function normalizeValue(value) {
  if (value === null || value === undefined || value === "") return null;
  const number = Number(value);
  return Number.isFinite(number) ? number : 0;
}

function buildSeries() {
  return props.series.map((item, index) => ({
    name: item.name || `系列${index + 1}`,
    type: "line",
    data: Array.isArray(item.data) ? item.data.map(normalizeValue) : [],
    smooth: true,
    connectNulls: false,
    showSymbol: props.categories.length <= 45,
    symbolSize: 6,
    lineStyle: {
      width: 2,
      type: item.lineType || "solid",
    },
    itemStyle: {
      color: item.color || COLORS[index % COLORS.length],
    },
    areaStyle: item.area === false ? undefined : { opacity: 0.08 },
    emphasis: { focus: "series" },
  }));
}

function updateChart() {
  if (!chartInstance) return;
  if (!hasData.value) {
    chartInstance.clear();
    return;
  }
  chartInstance.setOption(
    {
      animationDuration: 280,
      grid: {
        top: 54,
        right: 24,
        bottom: 42,
        left: 52,
        containLabel: true,
      },
      tooltip: {
        trigger: "axis",
        axisPointer: { type: "line" },
      },
      legend: {
        type: "scroll",
        top: 6,
        left: 8,
        right: 8,
        itemWidth: 18,
        itemHeight: 8,
        textStyle: { color: "#606266" },
      },
      xAxis: {
        type: "category",
        boundaryGap: false,
        data: props.categories,
        axisTick: { alignWithLabel: true },
      },
      yAxis: {
        type: "value",
        min: 0,
        name: props.yAxisName,
        nameLocation: "middle",
        nameGap: 36,
        splitLine: { lineStyle: { color: "#ebeef5" } },
      },
      series: buildSeries(),
    },
    true,
  );
}

function resizeChart() {
  chartInstance?.resize();
}

onMounted(() => {
  chartInstance = echarts.init(chartRef.value);
  updateChart();
  window.addEventListener("resize", resizeChart);
});

watch(
  () => [props.categories, props.series],
  updateChart,
  { deep: true },
);

onBeforeUnmount(() => {
  window.removeEventListener("resize", resizeChart);
  chartInstance?.dispose();
  chartInstance = undefined;
});
</script>

<style scoped>
.trend-line-chart {
  position: relative;
  width: 100%;
  min-height: 220px;
}

.trend-line-chart__canvas {
  width: 100%;
  height: 100%;
}

.trend-line-chart :deep(.el-empty) {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-bg-color);
}
</style>
