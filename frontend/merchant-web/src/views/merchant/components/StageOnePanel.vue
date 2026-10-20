<template>
  <div class="merchant-stage-page">
    <div class="stage-header">
      <div>
        <p class="stage-kicker">{{ kicker }}</p>
        <h2>{{ title }}</h2>
      </div>
      <el-tag type="info" effect="plain">第 1 阶段骨架</el-tag>
    </div>
    <el-row :gutter="16">
      <el-col v-for="item in items" :key="item.title" :xs="24" :sm="12" :lg="8">
        <div class="stage-item">
          <span>{{ item.title }}</span>
          <small>{{ item.desc }}</small>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
defineProps({
  kicker: {
    type: String,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  items: {
    type: Array,
    required: true
  }
})
</script>

<style scoped>
.merchant-stage-page {
  padding: 20px;
}

.stage-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.stage-header h2 {
  margin: 4px 0 0;
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
}

.stage-kicker {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.stage-item {
  min-height: 96px;
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
}

.stage-item span {
  display: block;
  margin-bottom: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #334155;
}

.stage-item small {
  display: block;
  line-height: 1.6;
  color: #64748b;
}
</style>
