<template>
  <el-dialog
    :model-value="modelValue"
    title="物流轨迹"
    width="720px"
    append-to-body
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-descriptions v-if="tracking" :column="2" border label-width="120">
      <el-descriptions-item label="订单编号">
        {{ tracking.orderNo || "-" }}
      </el-descriptions-item>
      <el-descriptions-item label="物流方向">
        {{ tracking.direction || "-" }}
      </el-descriptions-item>
      <el-descriptions-item label="快递公司">
        {{ getExpressCompanyName(carrierName) || "-" }}
      </el-descriptions-item>
      <el-descriptions-item label="快递单号">
        {{ tracking.trackingNo || "-" }}
      </el-descriptions-item>
    </el-descriptions>

    <div v-loading="loading" class="tracking-body">
      <el-alert
        v-if="!loading && error"
        :title="error"
        type="warning"
        show-icon
        :closable="false"
      />
      <el-alert
        v-else-if="!loading && tracking?.message && !tracking?.available"
        :title="tracking.message"
        type="warning"
        show-icon
        :closable="false"
      />
      <el-empty
        v-else-if="!loading && !tracking?.events?.length"
        description="暂无物流轨迹"
        :image-size="96"
      />
      <el-timeline v-else-if="tracking?.events?.length">
        <el-timeline-item
          v-for="(event, index) in tracking.events"
          :key="`${event.time || 'event'}-${index}`"
          :timestamp="event.time || '-'"
          :type="index === 0 ? 'primary' : 'info'"
          placement="top"
        >
          <div class="tracking-event-description">
            {{ event.description || "状态更新" }}
          </div>
          <div v-if="event.status || event.location" class="tracking-event-meta">
            <span v-if="event.status">状态：{{ event.status }}</span>
            <span v-if="event.location">地点：{{ event.location }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { getExpressCompanyName } from "@/utils/rental/expressCompany";

defineProps({
  modelValue: { type: Boolean, default: false },
  tracking: { type: Object, default: null },
  carrierName: { type: String, default: "" },
  loading: { type: Boolean, default: false },
  error: { type: String, default: "" },
});

const emit = defineEmits(["update:modelValue"]);
</script>

<style scoped>
.tracking-body {
  min-height: 180px;
  margin-top: 18px;
}

.tracking-event-description {
  color: var(--el-text-color-primary);
  line-height: 1.6;
}

.tracking-event-meta {
  display: flex;
  gap: 16px;
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
